package com.programming.util;

import com.programming.util.sandbox.SandboxLimits;
import com.programming.vo.CodeExecutionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Slf4j
@Component
public class LocalExecutionUtil {
    private static final int SERVICE_COMPILE_ERROR_EXIT_CODE = 2;
    private static final int SERVICE_RUNTIME_ERROR_EXIT_CODE = 3;
    private static final int SERVICE_EXECUTION_FAILED_EXIT_CODE = 4;
    private static final int SERVICE_EXCEPTION_EXIT_CODE = -1;
    private static final long ONE_MB = 1024L * 1024L;
    private static final long MIN_LOCAL_JAVA_HEAP_MB = 16L;

    public CodeExecutionResult executeCode(String code, String language, String input, SandboxLimits limits) {
        String normalizedLanguage = normalizeLanguage(language);

        try {
            return switch (normalizedLanguage) {
                case "java" -> executeJava(code, input, limits);
                case "python" -> executePython(code, input, limits);
                case "cpp" -> executeCpp(code, input, limits);
                case "javascript" -> executeJavaScript(code, input, limits);
                case "typescript" -> executeTypeScript(code, input, limits);
                case "go" -> executeGo(code, input, limits);
                default -> buildUnsupportedLanguageResult(language);
            };
        } catch (Exception e) {
            log.error("Failed to execute code locally", e);
            return buildExecutionExceptionResult("Local execution exception: " + e.getMessage(), 0);
        }
    }

    private CodeExecutionResult executeJava(String code, String input, SandboxLimits limits)
            throws IOException, InterruptedException, ExecutionException {
        Path sandboxDir = Files.createTempDirectory("programming-local-java-");
        try {
            Files.writeString(sandboxDir.resolve("Main.java"), safeText(code), StandardCharsets.UTF_8);

            ProcessSnapshot compileSnapshot = runProcess(
                    List.of("javac", "-encoding", "UTF-8", "Main.java"),
                    sandboxDir,
                    "",
                    limits.timeoutMillis(),
                    false
            );
            if (compileSnapshot.timedOut()) {
                return buildExecutionFailedResult("Compilation timed out", compileSnapshot.durationMs());
            }
            if (compileSnapshot.exitCode() != 0) {
                return buildCompileErrorResult(preferErrorText(compileSnapshot), compileSnapshot.durationMs());
            }

            ProcessSnapshot runSnapshot = runProcess(
                    buildJavaRunCommand(limits.memoryBytes()),
                    sandboxDir,
                    input,
                    limits.timeoutMillis(),
                    false
            );
            return mapRuntimeSnapshot(runSnapshot);
        } finally {
            deleteQuietly(sandboxDir);
        }
    }

    private CodeExecutionResult executePython(String code, String input, SandboxLimits limits)
            throws IOException, InterruptedException, ExecutionException {
        Path sandboxDir = Files.createTempDirectory("programming-local-python-");
        try {
            Files.writeString(sandboxDir.resolve("main.py"), safeText(code), StandardCharsets.UTF_8);

            ProcessSnapshot runSnapshot = runProcess(
                    List.of("python", "main.py"),
                    sandboxDir,
                    input,
                    limits.timeoutMillis(),
                    true
            );
            return mapRuntimeSnapshot(runSnapshot);
        } finally {
            deleteQuietly(sandboxDir);
        }
    }

    private CodeExecutionResult executeCpp(String code, String input, SandboxLimits limits)
            throws IOException, InterruptedException, ExecutionException {
        Path sandboxDir = Files.createTempDirectory("programming-local-cpp-");
        try {
            String executableName = isWindows() ? "main.exe" : "main";
            Files.writeString(sandboxDir.resolve("main.cpp"), safeText(code), StandardCharsets.UTF_8);

            ProcessSnapshot compileSnapshot = runProcess(
                    List.of("g++", "-std=c++17", "-O2", "-o", executableName, "main.cpp"),
                    sandboxDir,
                    "",
                    limits.timeoutMillis(),
                    false
            );
            if (compileSnapshot.timedOut()) {
                return buildExecutionFailedResult("Compilation timed out", compileSnapshot.durationMs());
            }
            if (compileSnapshot.exitCode() != 0) {
                return buildCompileErrorResult(preferErrorText(compileSnapshot), compileSnapshot.durationMs());
            }

            ProcessSnapshot runSnapshot = runProcess(
                    List.of(sandboxDir.resolve(executableName).toString()),
                    sandboxDir,
                    input,
                    limits.timeoutMillis(),
                    false
            );
            return mapRuntimeSnapshot(runSnapshot);
        } finally {
            deleteQuietly(sandboxDir);
        }
    }

    private CodeExecutionResult executeJavaScript(String code, String input, SandboxLimits limits)
            throws IOException, InterruptedException, ExecutionException {
        Path sandboxDir = Files.createTempDirectory("programming-local-js-");
        try {
            Files.writeString(sandboxDir.resolve("main.js"), safeText(code), StandardCharsets.UTF_8);

            ProcessSnapshot runSnapshot = runProcess(
                    List.of("node", "main.js"),
                    sandboxDir,
                    input,
                    limits.timeoutMillis(),
                    false
            );
            return mapRuntimeSnapshot(runSnapshot);
        } finally {
            deleteQuietly(sandboxDir);
        }
    }

    private CodeExecutionResult executeTypeScript(String code, String input, SandboxLimits limits)
            throws IOException, InterruptedException, ExecutionException {
        Path sandboxDir = Files.createTempDirectory("programming-local-ts-");
        try {
            Files.writeString(sandboxDir.resolve("main.ts"), safeText(code), StandardCharsets.UTF_8);

            try {
                ProcessSnapshot compileSnapshot = runProcess(
                        List.of("tsc", "--target", "es2019", "--module", "commonjs", "main.ts"),
                        sandboxDir,
                        "",
                        limits.timeoutMillis(),
                        false
                );
                if (compileSnapshot.timedOut()) {
                    return buildExecutionFailedResult("Compilation timed out", compileSnapshot.durationMs());
                }
                if (compileSnapshot.exitCode() != 0) {
                    return buildCompileErrorResult(preferErrorText(compileSnapshot), compileSnapshot.durationMs());
                }

                ProcessSnapshot runSnapshot = runProcess(
                        List.of("node", "main.js"),
                        sandboxDir,
                        input,
                        limits.timeoutMillis(),
                        false
                );
                return mapRuntimeSnapshot(runSnapshot);
            } catch (IOException compilerMissing) {
                ProcessSnapshot compileSnapshot = runProcess(
                        List.of("deno", "check", "main.ts"),
                        sandboxDir,
                        "",
                        limits.timeoutMillis(),
                        false
                );
                if (compileSnapshot.timedOut()) {
                    return buildExecutionFailedResult("Compilation timed out", compileSnapshot.durationMs());
                }
                if (compileSnapshot.exitCode() != 0) {
                    return buildCompileErrorResult(preferErrorText(compileSnapshot), compileSnapshot.durationMs());
                }

                ProcessSnapshot runSnapshot = runProcess(
                        List.of("deno", "run", "--quiet", "main.ts"),
                        sandboxDir,
                        input,
                        limits.timeoutMillis(),
                        false
                );
                return mapRuntimeSnapshot(runSnapshot);
            }
        } finally {
            deleteQuietly(sandboxDir);
        }
    }

    private CodeExecutionResult executeGo(String code, String input, SandboxLimits limits)
            throws IOException, InterruptedException, ExecutionException {
        Path sandboxDir = Files.createTempDirectory("programming-local-go-");
        try {
            String executableName = isWindows() ? "main.exe" : "main";
            Files.writeString(sandboxDir.resolve("main.go"), safeText(code), StandardCharsets.UTF_8);

            ProcessSnapshot compileSnapshot = runProcess(
                    List.of("go", "build", "-o", executableName, "main.go"),
                    sandboxDir,
                    "",
                    limits.timeoutMillis(),
                    false
            );
            if (compileSnapshot.timedOut()) {
                return buildExecutionFailedResult("Compilation timed out", compileSnapshot.durationMs());
            }
            if (compileSnapshot.exitCode() != 0) {
                return buildCompileErrorResult(preferErrorText(compileSnapshot), compileSnapshot.durationMs());
            }

            ProcessSnapshot runSnapshot = runProcess(
                    List.of(sandboxDir.resolve(executableName).toString()),
                    sandboxDir,
                    input,
                    limits.timeoutMillis(),
                    false
            );
            return mapRuntimeSnapshot(runSnapshot);
        } finally {
            deleteQuietly(sandboxDir);
        }
    }

    private CodeExecutionResult mapRuntimeSnapshot(ProcessSnapshot snapshot) {
        if (snapshot.timedOut()) {
            return buildExecutionFailedResult("Execution timed out", snapshot.durationMs());
        }

        if (snapshot.exitCode() != 0) {
            return buildRuntimeErrorResult(preferErrorText(snapshot), snapshot.durationMs());
        }

        CodeExecutionResult result = new CodeExecutionResult();
        result.setStatus("SUCCESS");
        result.setExitCode(0);
        result.setOutput(snapshot.stdout().trim());
        result.setErrorMessage(null);
        result.setTimeCost(snapshot.durationMs());
        result.setMemoryCost(0);
        return result;
    }

    private ProcessSnapshot runProcess(List<String> command, Path workingDirectory, String input,
                                       long timeoutMillis, boolean pythonProcess)
            throws IOException, InterruptedException, ExecutionException {
        long startTime = System.currentTimeMillis();
        ProcessBuilder processBuilder = new ProcessBuilder(new ArrayList<>(command));
        processBuilder.directory(workingDirectory.toFile());
        processBuilder.redirectErrorStream(false);
        if (pythonProcess) {
            processBuilder.environment().put("PYTHONIOENCODING", "UTF-8");
        }

        Process process = processBuilder.start();
        CompletableFuture<String> stdoutFuture = readStreamAsync(process.getInputStream());
        CompletableFuture<String> stderrFuture = readStreamAsync(process.getErrorStream());

        try (OutputStream outputStream = process.getOutputStream()) {
            String actualInput = input == null ? "" : input;
            outputStream.write(actualInput.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        }

        boolean finished = process.waitFor(timeoutMillis, TimeUnit.MILLISECONDS);
        if (!finished) {
            process.destroyForcibly();
            process.waitFor(5, TimeUnit.SECONDS);
        }

        String stdout = stdoutFuture.get();
        String stderr = stderrFuture.get();
        long durationMs = System.currentTimeMillis() - startTime;

        if (!finished) {
            return new ProcessSnapshot(SERVICE_EXECUTION_FAILED_EXIT_CODE, stdout, stderr, true, durationMs);
        }
        return new ProcessSnapshot(process.exitValue(), stdout, stderr, false, durationMs);
    }

    private CompletableFuture<String> readStreamAsync(InputStream inputStream) {
        return CompletableFuture.supplyAsync(() -> {
            try (InputStream stream = inputStream) {
                return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.debug("Failed to read process stream", e);
                return "";
            }
        });
    }

    private List<String> buildJavaRunCommand(long memoryBytes) {
        long heapMb = Math.max(MIN_LOCAL_JAVA_HEAP_MB, memoryBytes / ONE_MB);
        return List.of("java", "-Dfile.encoding=UTF-8", "-Xmx" + heapMb + "m", "Main");
    }

    private CodeExecutionResult buildUnsupportedLanguageResult(String language) {
        return buildExecutionExceptionResult("Unsupported language: " + language, 0);
    }

    private CodeExecutionResult buildCompileErrorResult(String errorMessage, long durationMs) {
        CodeExecutionResult result = new CodeExecutionResult();
        result.setStatus("COMPILE_ERROR");
        result.setExitCode(SERVICE_COMPILE_ERROR_EXIT_CODE);
        result.setOutput("");
        result.setCompileOutput(errorMessage);
        result.setErrorMessage(errorMessage);
        result.setTimeCost(durationMs);
        result.setMemoryCost(0);
        return result;
    }

    private CodeExecutionResult buildRuntimeErrorResult(String errorMessage, long durationMs) {
        CodeExecutionResult result = new CodeExecutionResult();
        result.setStatus("RUNTIME_ERROR");
        result.setExitCode(SERVICE_RUNTIME_ERROR_EXIT_CODE);
        result.setOutput("");
        result.setErrorMessage(errorMessage);
        result.setTimeCost(durationMs);
        result.setMemoryCost(0);
        return result;
    }

    private CodeExecutionResult buildExecutionFailedResult(String errorMessage, long durationMs) {
        CodeExecutionResult result = new CodeExecutionResult();
        result.setStatus("TIMEOUT");
        result.setExitCode(SERVICE_EXECUTION_FAILED_EXIT_CODE);
        result.setOutput("");
        result.setErrorMessage(errorMessage);
        result.setTimeCost(durationMs);
        result.setMemoryCost(0);
        return result;
    }

    private CodeExecutionResult buildExecutionExceptionResult(String errorMessage, long durationMs) {
        CodeExecutionResult result = new CodeExecutionResult();
        result.setStatus("EXECUTION_EXCEPTION");
        result.setExitCode(SERVICE_EXCEPTION_EXIT_CODE);
        result.setOutput("");
        result.setErrorMessage(errorMessage);
        result.setTimeCost(durationMs);
        result.setMemoryCost(0);
        return result;
    }

    private String preferErrorText(ProcessSnapshot snapshot) {
        String stderr = snapshot.stderr() == null ? "" : snapshot.stderr().trim();
        if (!stderr.isEmpty()) {
            return stderr;
        }
        String stdout = snapshot.stdout() == null ? "" : snapshot.stdout().trim();
        return stdout;
    }

    private String normalizeLanguage(String language) {
        return language == null ? "" : language.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }

    private String safeText(String text) {
        return text == null ? "" : text;
    }

    private void deleteQuietly(Path directory) {
        if (directory == null || !Files.exists(directory)) {
            return;
        }

        try (Stream<Path> paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    log.debug("Failed to delete sandbox path {}", path, e);
                }
            });
        } catch (IOException e) {
            log.debug("Failed to clean local sandbox directory {}", directory, e);
        }
    }

    private record ProcessSnapshot(int exitCode, String stdout, String stderr, boolean timedOut, long durationMs) {
    }
}
