package com.programming.util;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.MemoryStatsConfig;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.api.model.StreamType;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.programming.util.sandbox.SandboxCommandBuilder;
import com.programming.util.sandbox.SandboxLimits;
import com.programming.util.sandbox.SandboxResourceLimitResolver;
import com.programming.vo.CodeExecutionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class DockerUtil {
    private static final int SERVICE_COMPILE_ERROR_EXIT_CODE = 2;
    private static final int SERVICE_RUNTIME_ERROR_EXIT_CODE = 3;
    private static final int SERVICE_EXECUTION_FAILED_EXIT_CODE = 4;
    private static final int SERVICE_EXCEPTION_EXIT_CODE = -1;

    @Value("${programming.docker.host}")
    private String dockerHost;

    private final SandboxCommandBuilder commandBuilder;
    private final SandboxResourceLimitResolver limitResolver;

    private DockerClient dockerClient;

    public DockerUtil(SandboxCommandBuilder commandBuilder, SandboxResourceLimitResolver limitResolver) {
        this.commandBuilder = commandBuilder;
        this.limitResolver = limitResolver;
    }

    private void initDockerClient() {
        if (dockerClient == null) {
            DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                    .withDockerHost(dockerHost)
                    .build();

            DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                    .dockerHost(config.getDockerHost())
                    .sslConfig(config.getSSLConfig())
                    .maxConnections(100)
                    .connectionTimeout(Duration.ofSeconds(30))
                    .responseTimeout(Duration.ofSeconds(30))
                    .build();

            dockerClient = DockerClientBuilder.getInstance(config)
                    .withDockerHttpClient(httpClient)
                    .build();
        }
    }

    public String runCode(String code, String language, String input) {
        return toOutputMessage(executeCode(code, language, input));
    }

    public String runCode(String code, String language, String input, Integer timeLimit, Integer memoryLimit) {
        return toOutputMessage(executeCode(code, language, input, timeLimit, memoryLimit));
    }

    public CodeExecutionResult executeCode(String code, String language, String input) {
        return executeCode(code, language, input, null, null);
    }

    public CodeExecutionResult executeCode(String code, String language, String input, Integer timeLimit, Integer memoryLimit) {
        SandboxLimits limits = limitResolver.resolve(timeLimit, memoryLimit);
        return executeCode(code, language, input, limits);
    }

    private CodeExecutionResult executeCode(String code, String language, String input, SandboxLimits limits) {
        initDockerClient();

        CodeExecutionResult result = new CodeExecutionResult();
        result.setOutput("");
        long startTime = System.currentTimeMillis();

        String imageName = commandBuilder.resolveImageName(language);
        if (imageName == null) {
            return buildUnsupportedLanguageResult(result, language);
        }

        String runCommand = commandBuilder.buildRunCommand(code, language, input);
        String containerId = null;
        ResultCallback.Adapter<Statistics> statsCallback = null;
        LogContainerResultCallback logCallback = null;
        WaitContainerResultCallback waitCallback = null;

        try {
            containerId = createContainer(imageName, runCommand, limits.memoryBytes());

            AtomicLong peakMemoryBytes = new AtomicLong(0);
            dockerClient.startContainerCmd(containerId).exec();
            statsCallback = startStatsCollection(containerId, peakMemoryBytes);

            StringBuilder output = new StringBuilder();
            StringBuilder errorOutput = new StringBuilder();
            logCallback = startLogCollection(containerId, output, errorOutput);

            waitCallback = dockerClient.waitContainerCmd(containerId).start();
            Integer exitCode = awaitContainerExit(containerId, waitCallback, result, startTime, limits.timeoutMillis());
            if (exitCode == null) {
                return result;
            }

            result.setTimeCost(System.currentTimeMillis() - startTime);
            logCallback.awaitCompletion(1, TimeUnit.SECONDS);
            result.setMemoryCost(limitResolver.bytesToKb(peakMemoryBytes.get()));

            String fullOutput = output.toString().trim();
            String fullError = errorOutput.toString().trim();
            fillExecutionResult(result, exitCode, fullOutput, fullError);

            if (result.getExitCode() == 0) {
                log.info("Code executed successfully, output: {}", result.getOutput());
            } else {
                log.warn("Code execution failed, exitCode={}, error={}", result.getExitCode(), result.getErrorMessage());
            }
            return result;
        } catch (Exception e) {
            log.error("Failed to execute code", e);
            result.setExitCode(SERVICE_EXCEPTION_EXIT_CODE);
            result.setErrorMessage("Execution exception: " + e.getMessage());
            result.setTimeCost(System.currentTimeMillis() - startTime);
            return result;
        } finally {
            closeWaitQuietly(waitCallback);
            closeLogQuietly(logCallback);
            closeStatsQuietly(statsCallback);
            removeContainerQuietly(containerId);
        }
    }

    private CodeExecutionResult buildUnsupportedLanguageResult(CodeExecutionResult result, String language) {
        result.setExitCode(SERVICE_EXCEPTION_EXIT_CODE);
        result.setErrorMessage("Unsupported language: " + language);
        result.setTimeCost(0);
        result.setMemoryCost(0);
        return result;
    }

    private String createContainer(String imageName, String runCommand, long memoryBytes) {
        return dockerClient.createContainerCmd(imageName)
                .withHostConfig(HostConfig.newHostConfig()
                        .withMemory(memoryBytes)
                        .withNetworkMode("none")
                )
                .withCmd("/bin/sh", "-c", runCommand)
                .exec()
                .getId();
    }

    private LogContainerResultCallback startLogCollection(String containerId, StringBuilder output, StringBuilder errorOutput) {
        return dockerClient.logContainerCmd(containerId)
                .withStdOut(true)
                .withStdErr(true)
                .withFollowStream(true)
                .withTailAll()
                .exec(new LogContainerResultCallback() {
                    @Override
                    public void onNext(Frame frame) {
                        String payload = new String(frame.getPayload(), StandardCharsets.UTF_8);
                        if (frame.getStreamType() == StreamType.STDOUT) {
                            output.append(payload);
                        } else if (frame.getStreamType() == StreamType.STDERR) {
                            errorOutput.append(payload);
                        }
                    }
                });
    }

    private Integer awaitContainerExit(String containerId, WaitContainerResultCallback waitCallback, CodeExecutionResult result,
                                       long startTime, long timeoutMillis) {
        try {
            Integer exitCode = waitCallback.awaitStatusCode(timeoutMillis, TimeUnit.MILLISECONDS);
            if (exitCode != null) {
                return exitCode;
            }
        } catch (DockerClientException e) {
            if (!isWaitTimeout(e)) {
                throw e;
            }
        }

        stopContainerQuietly(containerId);
        result.setTimeCost(System.currentTimeMillis() - startTime);
        result.setExitCode(SERVICE_EXECUTION_FAILED_EXIT_CODE);
        result.setErrorMessage("Execution timed out");
        return null;
    }

    private ResultCallback.Adapter<Statistics> startStatsCollection(String containerId, AtomicLong peakMemoryBytes) {
        ResultCallback.Adapter<Statistics> callback = new ResultCallback.Adapter<>() {
            @Override
            public void onNext(Statistics statistics) {
                Long memoryUsage = extractMemoryUsage(statistics);
                if (memoryUsage != null && memoryUsage > 0) {
                    peakMemoryBytes.accumulateAndGet(memoryUsage, Math::max);
                }
                super.onNext(statistics);
            }

            @Override
            public void onError(Throwable throwable) {
                log.debug("Container stats stream closed for {}", containerId, throwable);
                super.onError(throwable);
            }
        };

        dockerClient.statsCmd(containerId).exec(callback);
        return callback;
    }

    private Long extractMemoryUsage(Statistics statistics) {
        if (statistics == null) {
            return null;
        }

        MemoryStatsConfig memoryStats = statistics.getMemoryStats();
        if (memoryStats == null) {
            return null;
        }

        if (memoryStats.getMaxUsage() != null && memoryStats.getMaxUsage() > 0) {
            return memoryStats.getMaxUsage();
        }

        return memoryStats.getUsage();
    }

    private void fillExecutionResult(CodeExecutionResult result, Integer exitCode, String fullOutput, String fullError) {
        String errorDetail = !fullError.isEmpty() ? fullError : fullOutput;

        if (exitCode == null) {
            result.setExitCode(SERVICE_EXECUTION_FAILED_EXIT_CODE);
            result.setOutput("");
            result.setErrorMessage("Execution failed: missing container exit code");
            return;
        }

        if (exitCode == 0) {
            result.setExitCode(0);
            result.setOutput(fullOutput);
            return;
        }

        if (exitCode == commandBuilder.getCompileErrorExitCode()) {
            result.setExitCode(SERVICE_COMPILE_ERROR_EXIT_CODE);
            result.setErrorMessage(errorDetail);
            result.setOutput(fullOutput == null ? "" : fullOutput);
            return;
        }

        if (exitCode == commandBuilder.getRuntimeErrorExitCode()) {
            result.setExitCode(SERVICE_RUNTIME_ERROR_EXIT_CODE);
            result.setErrorMessage(errorDetail);
            result.setOutput(fullOutput == null ? "" : fullOutput);
            return;
        }

        result.setExitCode(SERVICE_EXECUTION_FAILED_EXIT_CODE);
        result.setErrorMessage(errorDetail.isEmpty()
                ? "Execution failed with container exit code " + exitCode
                : errorDetail);
        result.setOutput(fullOutput == null ? "" : fullOutput);
    }

    private String toOutputMessage(CodeExecutionResult result) {
        if (result.getExitCode() == 0) {
            return result.getOutput();
        }
        if (result.getExitCode() == SERVICE_COMPILE_ERROR_EXIT_CODE) {
            return "Compile Error: " + nullToEmpty(result.getErrorMessage());
        }
        if (result.getExitCode() == SERVICE_RUNTIME_ERROR_EXIT_CODE) {
            return "Runtime Error: " + nullToEmpty(result.getErrorMessage());
        }
        if (result.getExitCode() == SERVICE_EXECUTION_FAILED_EXIT_CODE) {
            return "Execution Failed: " + nullToEmpty(result.getErrorMessage());
        }
        return "Execution Exception: " + nullToEmpty(result.getErrorMessage());
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private void stopContainerQuietly(String containerId) {
        try {
            dockerClient.killContainerCmd(containerId).exec();
        } catch (Exception e) {
            log.warn("Failed to kill timed out container {}", containerId, e);
        }
    }

    private void removeContainerQuietly(String containerId) {
        if (containerId == null) {
            return;
        }

        try {
            dockerClient.removeContainerCmd(containerId).withForce(true).exec();
        } catch (Exception e) {
            log.error("Failed to remove container", e);
        }
    }

    private void closeStatsQuietly(ResultCallback.Adapter<Statistics> statsCallback) {
        if (statsCallback == null) {
            return;
        }

        try {
            statsCallback.close();
        } catch (IOException e) {
            log.debug("Failed to close stats callback", e);
        }
    }

    private void closeLogQuietly(LogContainerResultCallback logCallback) {
        if (logCallback == null) {
            return;
        }

        try {
            logCallback.close();
        } catch (IOException e) {
            log.debug("Failed to close log callback", e);
        }
    }

    private void closeWaitQuietly(WaitContainerResultCallback waitCallback) {
        if (waitCallback == null) {
            return;
        }

        try {
            waitCallback.close();
        } catch (IOException e) {
            log.debug("Failed to close wait callback", e);
        }
    }

    private boolean isWaitTimeout(DockerClientException e) {
        String message = e.getMessage();
        return message != null && message.contains("Awaiting status code timeout");
    }
}
