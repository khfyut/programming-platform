package com.programming.util;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.StreamType;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class DockerUtil {

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
        initDockerClient();

        String imageName = "";
        String runCommand = "";
        String containerId = null;

        try {
            if ("java".equals(language)) {
                imageName = "openjdk:11-jdk-slim";
                // 修复命令格式，使用双引号包裹代码
                runCommand = String.format(
                        "echo \"%s\" > Main.java && javac Main.java && java Main",
                        code.replace("\"", "\\\"")
                );
            } else if ("python".equals(language)) {
                imageName = "python:3.9-slim";
                // 修复命令格式，使用双引号包裹代码
                runCommand = String.format(
                        "python3 -c \"%s\"",
                        code.replace("\"", "\\\"")
                );
            } else {
                return "不支持的编程语言：" + language;
            }

            containerId = dockerClient.createContainerCmd(imageName)
                    .withHostConfig(HostConfig.newHostConfig()
                            .withMemory(memory)
                            .withNetworkMode("none")
                    )
                    .withCmd("/bin/sh", "-c", runCommand)
                    .exec().getId();

            dockerClient.startContainerCmd(containerId).exec();

            try {
                Thread.sleep(2000); // 减少等待时间
            } catch (InterruptedException e) {
                log.error("等待容器启动失败", e);
            }

            StringBuilder output = new StringBuilder();
            StringBuilder errorOutput = new StringBuilder();

            dockerClient.logContainerCmd(containerId)
                    .withStdOut(true)
                    .withStdErr(true)
                    .withTail(100)
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
                    }).awaitCompletion(timeout, TimeUnit.MILLISECONDS);

            int exitCode = dockerClient.inspectContainerCmd(containerId).exec().getState().getExitCode();

            String fullOutput = output.toString().trim();
            String fullError = errorOutput.toString().trim();

            if (exitCode != 0) {
                if (!fullError.isEmpty()) {
                    log.error("运行错误：{}", fullError);
                    return "运行错误：" + fullError;
                } else {
                    log.error("运行失败：容器退出码 {}，输出：{}，错误：{}", exitCode, fullOutput, fullError);
                    return "运行失败：容器退出码 " + exitCode;
                }
            }

            log.info("代码运行成功，输出：{}", fullOutput);
            return fullOutput;
        } catch (Exception e) {
            log.error("运行代码失败", e);
            return "运行异常：" + e.getMessage();
        } finally {
            // 确保容器被删除
            if (containerId != null) {
                try {
                    dockerClient.removeContainerCmd(containerId).withForce(true).exec();
                } catch (Exception e) {
                    log.error("删除容器失败", e);
                }
            }
        }
    }

    public String runCode(String code, String language, String input, Integer timeLimit, Integer memoryLimit) {
        initDockerClient();

        String imageName = "";
        String runCommand = "";
        String containerId = null;
        
        long actualTimeout = timeLimit != null ? timeLimit : timeout;
        long actualMemory = memoryLimit != null ? memoryLimit : memory;

        try {
            if ("java".equals(language)) {
                imageName = "openjdk:11-jdk-slim";
                // 修复命令格式，使用双引号包裹代码
                runCommand = String.format(
                        "echo \"%s\" > Main.java && javac Main.java && java Main",
                        code.replace("\"", "\\\"")
                );
            } else if ("python".equals(language)) {
                imageName = "python:3.9-slim";
                // 修复命令格式，使用双引号包裹代码
                runCommand = String.format(
                        "python3 -c \"%s\"",
                        code.replace("\"", "\\\"")
                );
            } else {
                return "不支持的编程语言：" + language;
            }
            
            containerId = dockerClient.createContainerCmd(imageName)
                    .withHostConfig(HostConfig.newHostConfig()
                            .withMemory(actualMemory)
                            .withNetworkMode("none")
                    )
                    .withCmd("/bin/sh", "-c", runCommand)
                    .exec().getId();
            
            dockerClient.startContainerCmd(containerId).exec();
            
            try {
                Thread.sleep(2000); // 减少等待时间
            } catch (InterruptedException e) {
                log.error("等待容器启动失败", e);
            }

            StringBuilder output = new StringBuilder();
            StringBuilder errorOutput = new StringBuilder();

            dockerClient.logContainerCmd(containerId)
                    .withStdOut(true)
                    .withStdErr(true)
                    .withTail(100)
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
                    }).awaitCompletion(actualTimeout, TimeUnit.MILLISECONDS);
            
            int exitCode = dockerClient.inspectContainerCmd(containerId).exec().getState().getExitCode();
            
            String fullOutput = output.toString().trim();
            String fullError = errorOutput.toString().trim();
            
            if (exitCode != 0) {
                if (!fullError.isEmpty()) {
                    log.error("运行错误：{}", fullError);
                    return "运行错误：" + fullError;
                } else {
                    log.error("运行失败：容器退出码 {}，输出：{}，错误：{}", exitCode, fullOutput, fullError);
                    return "运行失败：容器退出码 " + exitCode;
                }
            }
            
            log.info("代码运行成功，输出：{}", fullOutput);
            return fullOutput;
        } catch (Exception e) {
            log.error("运行代码失败", e);
            return "运行异常：" + e.getMessage();
        } finally {
            // 确保容器被删除
            if (containerId != null) {
                try {
                    dockerClient.removeContainerCmd(containerId).withForce(true).exec();
                } catch (Exception e) {
                    log.error("删除容器失败", e);
                }
            }
        }
    }
}
