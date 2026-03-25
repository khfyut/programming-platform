package com.programming.vo;

import lombok.Data;

import java.util.Map;

@Data
public class ReferenceSolutionVO {
    private Long problemId;
    private String language;
    private String solutionCode;
    private String timeComplexity;
    private String spaceComplexity;
    private String explanation;
    private Map<String, String> hints;
    private String[] availableLanguages;
}