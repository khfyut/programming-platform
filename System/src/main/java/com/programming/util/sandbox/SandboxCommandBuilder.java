package com.programming.util.sandbox;

import com.programming.service.runtime.RuntimeCatalogService;
import com.programming.service.runtime.RuntimeLanguageDefinition;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class SandboxCommandBuilder {
    private static final int COMPILE_ERROR_EXIT_CODE = 21;
    private static final int RUNTIME_ERROR_EXIT_CODE = 22;

    private final RuntimeCatalogService runtimeCatalogService;

    public SandboxCommandBuilder(RuntimeCatalogService runtimeCatalogService) {
        this.runtimeCatalogService = runtimeCatalogService;
    }

    public String resolveImageName(String language) {
        RuntimeLanguageDefinition definition = runtimeCatalogService.getJudgeLanguage(language);
        return definition == null ? null : definition.getImageName();
    }

    public String buildRunCommand(String code, String language, String input) {
        String normalizedLanguage = runtimeCatalogService.normalizeLanguageCode(language);
        return switch (normalizedLanguage) {
            case "java" -> buildJavaRunCommand(code, input);
            case "python" -> buildPythonRunCommand(code, input);
            case "cpp" -> buildCppRunCommand(code, input);
            case "javascript" -> buildJavaScriptRunCommand(code, input);
            case "typescript" -> buildTypeScriptRunCommand(code, input);
            case "go" -> buildGoRunCommand(code, input);
            default -> throw new IllegalArgumentException("Unsupported language: " + language);
        };
    }

    public int getCompileErrorExitCode() {
        return COMPILE_ERROR_EXIT_CODE;
    }

    public int getRuntimeErrorExitCode() {
        return RUNTIME_ERROR_EXIT_CODE;
    }

    private String buildJavaRunCommand(String code, String input) {
        String encodedCode = encodeBase64(code);
        return String.format(
                "printf '%%s' '%s' | base64 -d > Main.java && javac Main.java 2> compile.err || { cat compile.err >&2; exit %d; }; %s",
                encodedCode,
                COMPILE_ERROR_EXIT_CODE,
                buildExecuteCommand("java Main", input)
        );
    }

    private String buildPythonRunCommand(String code, String input) {
        String encodedCode = encodeBase64(code);
        return String.format(
                "printf '%%s' '%s' | base64 -d > main.py && %s",
                encodedCode,
                buildExecuteCommand("python3 main.py", input)
        );
    }

    private String buildCppRunCommand(String code, String input) {
        String encodedCode = encodeBase64(code);
        return String.format(
                "printf '%%s' '%s' | base64 -d > main.cpp && g++ -std=c++17 -O2 -o main main.cpp 2> compile.err || { cat compile.err >&2; exit %d; }; %s",
                encodedCode,
                COMPILE_ERROR_EXIT_CODE,
                buildExecuteCommand("./main", input)
        );
    }

    private String buildJavaScriptRunCommand(String code, String input) {
        String encodedCode = encodeBase64(code);
        return String.format(
                "printf '%%s' '%s' | base64 -d > main.js && %s",
                encodedCode,
                buildExecuteCommand("node main.js", input)
        );
    }

    private String buildTypeScriptRunCommand(String code, String input) {
        String encodedCode = encodeBase64(code);
        return String.format(
                "printf '%%s' '%s' | base64 -d > main.ts && deno check main.ts 2> compile.err || { cat compile.err >&2; exit %d; }; %s",
                encodedCode,
                COMPILE_ERROR_EXIT_CODE,
                buildExecuteCommand("deno run --quiet main.ts", input)
        );
    }

    private String buildGoRunCommand(String code, String input) {
        String encodedCode = encodeBase64(code);
        return String.format(
                "printf '%%s' '%s' | base64 -d > main.go && go build -o main main.go 2> compile.err || { cat compile.err >&2; exit %d; }; %s",
                encodedCode,
                COMPILE_ERROR_EXIT_CODE,
                buildExecuteCommand("./main", input)
        );
    }

    private String buildExecuteCommand(String command, String input) {
        if (input == null || input.isEmpty()) {
            return String.format("%s 2> runtime.err || { cat runtime.err >&2; exit %d; }", command, RUNTIME_ERROR_EXIT_CODE);
        }

        return String.format(
                "printf '%%s' '%s' | base64 -d | %s 2> runtime.err || { cat runtime.err >&2; exit %d; }",
                encodeBase64(input),
                command,
                RUNTIME_ERROR_EXIT_CODE
        );
    }

    private String encodeBase64(String content) {
        String actualContent = content == null ? "" : content;
        return Base64.getEncoder().encodeToString(actualContent.getBytes(StandardCharsets.UTF_8));
    }
}
