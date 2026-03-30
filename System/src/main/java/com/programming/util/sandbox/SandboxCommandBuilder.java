package com.programming.util.sandbox;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;

@Component
public class SandboxCommandBuilder {
    private static final int COMPILE_ERROR_EXIT_CODE = 21;
    private static final int RUNTIME_ERROR_EXIT_CODE = 22;

    public String resolveImageName(String language) {
        if (language == null) {
            return null;
        }

        String normalizedLanguage = normalizeLanguage(language);
        if ("java".equals(normalizedLanguage)) {
            return "openjdk:11-jdk-slim";
        }
        if ("python".equals(normalizedLanguage)) {
            return "python:3.9-slim";
        }
        return null;
    }

    public String buildRunCommand(String code, String language, String input) {
        String normalizedLanguage = normalizeLanguage(language);
        if ("java".equals(normalizedLanguage)) {
            return buildJavaRunCommand(code, input);
        }
        if ("python".equals(normalizedLanguage)) {
            return buildPythonRunCommand(code, input);
        }
        throw new IllegalArgumentException("Unsupported language: " + language);
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

    private String normalizeLanguage(String language) {
        return language == null ? "" : language.trim().toLowerCase(Locale.ROOT);
    }
}
