package com.programming.vo;

import lombok.Data;

@Data
public class CodeExecutionResult {
    private String status;
    private String output;
    private int exitCode;
    private String errorMessage;
    private String compileOutput;
    private long timeCost;
    private long memoryCost;
}
