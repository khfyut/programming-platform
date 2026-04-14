package com.programming.service.runtime;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class RuntimeCatalogService {
    private final Map<String, RuntimeLanguageDefinition> judgeLanguages = new LinkedHashMap<>();

    public RuntimeCatalogService() {
        register(new RuntimeLanguageDefinition(
                "java",
                "Java",
                "java",
                "Main.java",
                "openjdk:11-jdk-slim",
                "public class Main {\n    public static void main(String[] args) throws Exception {\n        \n    }\n}\n",
                true,
                true,
                1
        ));
        register(new RuntimeLanguageDefinition(
                "python",
                "Python",
                "python",
                "main.py",
                "python:3.9-slim",
                "import sys\n\n\ndef main():\n    pass\n\n\nif __name__ == \"__main__\":\n    main()\n",
                true,
                true,
                2
        ));
        register(new RuntimeLanguageDefinition(
                "cpp",
                "C++",
                "cpp",
                "main.cpp",
                "gcc:13",
                "#include <iostream>\nusing namespace std;\n\nint main() {\n    return 0;\n}\n",
                true,
                true,
                3
        ));
        register(new RuntimeLanguageDefinition(
                "javascript",
                "JavaScript",
                "javascript",
                "main.js",
                "node:20-slim",
                "function main() {\n}\n\nmain();\n",
                true,
                true,
                4
        ));
        register(new RuntimeLanguageDefinition(
                "typescript",
                "TypeScript",
                "typescript",
                "main.ts",
                "denoland/deno:bin-1.46.3",
                "function main(): void {\n}\n\nmain();\n",
                true,
                true,
                5
        ));
        register(new RuntimeLanguageDefinition(
                "go",
                "Go",
                "go",
                "main.go",
                "golang:1.22-alpine",
                "package main\n\nfunc main() {\n}\n",
                true,
                true,
                6
        ));
    }

    public List<RuntimeLanguageDefinition> getJudgeLanguages() {
        return new ArrayList<>(judgeLanguages.values());
    }

    public RuntimeLanguageDefinition getJudgeLanguage(String language) {
        return judgeLanguages.get(normalizeLanguageCode(language));
    }

    public boolean isJudgeLanguageSupported(String language) {
        RuntimeLanguageDefinition definition = getJudgeLanguage(language);
        return definition != null && definition.isEnabled() && definition.isSupportsJudge();
    }

    public String normalizeLanguageCode(String language) {
        return language == null ? "" : language.trim().toLowerCase(Locale.ROOT);
    }

    public String resolveDisplayLabel(String language) {
        RuntimeLanguageDefinition definition = getJudgeLanguage(language);
        return definition == null ? normalizeLanguageCode(language) : definition.getLabel();
    }

    public String resolveDefaultStarterCode(String language) {
        RuntimeLanguageDefinition definition = getJudgeLanguage(language);
        return definition == null ? "" : definition.getDefaultStarterCode();
    }

    public String resolveDefaultFileName(String language) {
        RuntimeLanguageDefinition definition = getJudgeLanguage(language);
        return definition == null ? "main.txt" : definition.getDefaultFileName();
    }

    private void register(RuntimeLanguageDefinition definition) {
        judgeLanguages.put(definition.getCode(), definition);
    }
}
