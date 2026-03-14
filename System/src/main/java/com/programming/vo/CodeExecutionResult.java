package com.programming.vo;

import lombok.Data;

@Data
public class CodeExecutionResult {
    private String output;
    private int exitCode;
    private String errorMessage;
    private long timeCost;
    private long memoryCost;
}
