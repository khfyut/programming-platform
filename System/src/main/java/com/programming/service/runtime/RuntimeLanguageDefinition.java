package com.programming.service.runtime;

import lombok.Getter;

@Getter
public class RuntimeLanguageDefinition {
    private final String code;
    private final String label;
    private final String monacoLanguage;
    private final String defaultFileName;
    private final String imageName;
    private final String defaultStarterCode;
    private final boolean supportsJudge;
    private final boolean enabled;
    private final int sortOrder;

    public RuntimeLanguageDefinition(String code, String label, String monacoLanguage, String defaultFileName,
                                     String imageName, String defaultStarterCode, boolean supportsJudge,
                                     boolean enabled, int sortOrder) {
        this.code = code;
        this.label = label;
        this.monacoLanguage = monacoLanguage;
        this.defaultFileName = defaultFileName;
        this.imageName = imageName;
        this.defaultStarterCode = defaultStarterCode;
        this.supportsJudge = supportsJudge;
        this.enabled = enabled;
        this.sortOrder = sortOrder;
    }
}
