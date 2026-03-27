package com.programming.util;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
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
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.programming.vo.CodeExecutionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class DockerUtil {
    private static final long ONE_KB = 1024L;
    private static final long ONE_MB = 1024L * 1024L;
    private static final long ONE_SECOND_MS = 1000L;
    private static final long TIMEOUT_GRACE_MS = 1000L;
    private static final long MIN_DOCKER_MEMORY_BYTES = 6L * ONE_MB;
    private static final long MAX_REASONABLE_PROBLEM_MEMORY_MB = 10240L;
    private static final long MAX_REASONABLE_PROBLEM_MEMORY_KB = MAX_REASONABLE_PROBLEM_MEMORY_MB * ONE_KB;
    private static final long MAX_REASONABLE_PROBLEM_TIME_SECONDS = 60L;
    private static final int COMPILE_ERROR_EXIT_CODE = 21;
    private static final int RUNTIME_ERROR_EXIT_CODE = 22;
    private static final int SERVICE_COMPILE_ERROR_EXIT_CODE = 2;
    private static final int SERVICE_RUNTIME_ERROR_EXIT_CODE = 3;
    private static final int SERVICE_EXECUTION_FAILED_EXIT_CODE = 4;
    private static final int SERVICE_EXCEPTION_EXIT_CODE = -1;

    @Value("${programming.docker.host}")
    private String dockerHost;

    @Value("${programming.docker.timeout}")
    private int timeout;

    @Value("${programming.docker.memory}")
    private long memory;

    private DockerClient dockerClient;

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
        return executeCode(code, language, input, timeout, memory);
    }

    public CodeExecutionResult executeCode(String code, String language, String input, Integer timeLimit, Integer memoryLimit) {
        long actualTimeout = resolveTimeoutMillis(timeLimit);
        long actualMemory = resolveMemoryLimitBytes(memoryLimit);
        return executeCode(code, language, input, actualTimeout, actualMemory);
    }

    private CodeExecutionResult executeCode(String code, String language, String input, long actualTimeout, long actualMemory) {
        initDockerClient();

        CodeExecutionResult result = new CodeExecutionResult();
        result.setOutput("");
        long startTime = System.currentTimeMillis();
        String imageName = resolveImageName(language);
        if (imageName == null) {
            result.setExitCode(SERVICE_EXCEPTION_EXIT_CODE);
            result.setErrorMessage("Unsupported language: " + language);
            result.setTimeCost(0);
            result.setMemoryCost(0);
            return result;
        }

        String runCommand = buildRunCommand(code, language, input);
        String containerId = null;
        ResultCallback.Adapter<Statistics> statsCallback = null;
        LogContainerResultCallback logCallback = null;
        WaitContainerResultCallback waitCallback = null;

        try {
            containerId = dockerClient.createContainerCmd(imageName)
                    .withHostConfig(HostConfig.newHostConfig()
                            .withMemory(actualMemory)
                            .withNetworkMode("none")
                    )
                    .withCmd("/bin/sh", "-c", runCommand)
                    .exec()
                    .getId();

            AtomicLong peakMemoryBytes = new AtomicLong(0);
            dockerClient.startContainerCmd(containerId).exec();
            statsCallback = startStatsCollection(containerId, peakMemoryBytes);

            StringBuilder output = new StringBuilder();
            StringBuilder errorOutput = new StringBuilder();

            logCallback = dockerClient.logContainerCmd(containerId)
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

            waitCallback = dockerClient.waitContainerCmd(containerId).start();
            Integer exitCode;
            try {
                exitCode = waitCallback.awaitStatusCode(actualTimeout, TimeUnit.MILLISECONDS);
            } catch (DockerClientException e) {
                if (isWaitTimeout(e)) {
                    stopContainerQuietly(containerId);
                    result.setTimeCost(System.currentTimeMillis() - startTime);
                    result.setExitCode(SERVICE_EXECUTION_FAILED_EXIT_CODE);
                    result.setErrorMessage("Execution timed out");
                    return result;
                }
                throw e;
            }

            result.setTimeCost(System.currentTimeMillis() - startTime);

            if (exitCode == null) {
                stopContainerQuietly(containerId);
                result.setExitCode(SERVICE_EXECUTION_FAILED_EXIT_CODE);
                result.setErrorMessage("Execution timed out");
                return result;
            }

            logCallback.awaitCompletion(1, TimeUnit.SECONDS);
            result.setMemoryCost(bytesToKb(peakMemoryBytes.get()));

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
            if (result.getMemoryCost() == 0) {
                result.setMemoryCost(0);
            }
            return result;
        } finally {
            closeWaitQuietly(waitCallback);
            closeLogQuietly(logCallback);
            closeStatsQuietly(statsCallback);
            if (containerId != null) {
                try {
                    dockerClient.removeContainerCmd(containerId).withForce(true).exec();
                } catch (Exception e) {
                    log.error("Failed to remove container", e);
                }
            }
        }
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

        if (exitCode == COMPILE_ERROR_EXIT_CODE) {
            result.setExitCode(SERVICE_COMPILE_ERROR_EXIT_CODE);
            result.setErrorMessage(errorDetail);
            result.setOutput(fullOutput == null ? "" : fullOutput);
            return;
        }

        if (exitCode == RUNTIME_ERROR_EXIT_CODE) {
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

    private String resolveImageName(String language) {
        if (language == null) {
            return null;
        }

        String normalizedLanguage = language.trim().toLowerCase(Locale.ROOT);
        if ("java".equals(normalizedLanguage)) {
            return "openjdk:11-jdk-slim";
        }
        if ("python".equals(normalizedLanguage)) {
            return "python:3.9-slim";
        }
        return null;
    }

    private String buildRunCommand(String code, String language, String input) {
        String normalizedLanguage = language == null ? "" : language.trim().toLowerCase(Locale.ROOT);
        if ("java".equals(normalizedLanguage)) {
            return buildJavaRunCommand(code, input);
        }
        if ("python".equals(normalizedLanguage)) {
            return buildPythonRunCommand(code, input);
        }
        throw new IllegalArgumentException("Unsupported language: " + language);
    }

    private String buildJavaRunCommand(String code, String input) {
        String encodedCode = encodeBase64(code);
        return String.format(
                "printf '%%s' '%s' | base64 -d > Main.java && javac Main.java 2> compile.err || { cat compile.err >&2; exit %d; }; %s",
                encodedCode,
                COMPILE_ERROR_EXIT_CODE,
                buildJavaExecuteCommand(input)
        );
    }

    private String buildPythonRunCommand(String code, String input) {
        String encodedCode = encodeBase64(code);
        return String.format(
                "printf '%%s' '%s' | base64 -d > main.py && %s",
                encodedCode,
                buildPythonExecuteCommand(input)
        );
    }

    private String buildJavaExecuteCommand(String input) {
        if (input == null || input.isEmpty()) {
            return String.format("java Main 2> runtime.err || { cat runtime.err >&2; exit %d; }", RUNTIME_ERROR_EXIT_CODE);
        }
        return String.format(
                "printf '%%s' '%s' | base64 -d | java Main 2> runtime.err || { cat runtime.err >&2; exit %d; }",
                encodeBase64(input),
                RUNTIME_ERROR_EXIT_CODE
        );
    }

    private String buildPythonExecuteCommand(String input) {
        if (input == null || input.isEmpty()) {
            return String.format("python3 main.py 2> runtime.err || { cat runtime.err >&2; exit %d; }", RUNTIME_ERROR_EXIT_CODE);
        }
        return String.format(
                "printf '%%s' '%s' | base64 -d | python3 main.py 2> runtime.err || { cat runtime.err >&2; exit %d; }",
                encodeBase64(input),
                RUNTIME_ERROR_EXIT_CODE
        );
    }

    private String encodeBase64(String content) {
        String actualContent = content == null ? "" : content;
        return Base64.getEncoder().encodeToString(actualContent.getBytes(StandardCharsets.UTF_8));
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

    private long resolveMemoryLimitBytes(Integer requestedMemoryLimit) {
        if (requestedMemoryLimit == null || requestedMemoryLimit <= 0) {
            return memory;
        }

        long actualMemory;
        if (requestedMemoryLimit <= MAX_REASONABLE_PROBLEM_MEMORY_MB) {
            actualMemory = requestedMemoryLimit.longValue() * ONE_MB;
        } else if (requestedMemoryLimit <= MAX_REASONABLE_PROBLEM_MEMORY_KB
                && requestedMemoryLimit.longValue() % ONE_MB != 0) {
            actualMemory = requestedMemoryLimit.longValue() * ONE_KB;
        } else {
            actualMemory = requestedMemoryLimit.longValue();
        }

        if (actualMemory < MIN_DOCKER_MEMORY_BYTES) {
            log.warn("Problem memory limit {} is below Docker minimum, clamped to {} bytes",
                    requestedMemoryLimit, MIN_DOCKER_MEMORY_BYTES);
            return MIN_DOCKER_MEMORY_BYTES;
        }

        return actualMemory;
    }

    private long resolveTimeoutMillis(Integer requestedTimeLimit) {
        if (requestedTimeLimit == null || requestedTimeLimit <= 0) {
            return timeout;
        }

        long normalizedTimeout;
        if (requestedTimeLimit <= MAX_REASONABLE_PROBLEM_TIME_SECONDS) {
            normalizedTimeout = requestedTimeLimit.longValue() * ONE_SECOND_MS;
        } else {
            normalizedTimeout = requestedTimeLimit.longValue();
        }

        return Math.max(timeout, normalizedTimeout + TIMEOUT_GRACE_MS);
    }

    private long bytesToKb(long bytes) {
        if (bytes <= 0) {
            return 0;
        }
        return Math.max(1, (bytes + ONE_KB - 1) / ONE_KB);
    }

    private void stopContainerQuietly(String containerId) {
        try {
            dockerClient.killContainerCmd(containerId).exec();
        } catch (Exception e) {
            log.warn("Failed to kill timed out container {}", containerId, e);
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
