package com.programming.vo;

import lombok.Data;
import java.util.List;

@Data
public class SubmitResultVO {
    private Integer result;
    private Integer timeCost;
    private Integer memoryCost;
    private String output;
    private String compileError;
    private String runtimeError;
    private String errorMessage;
    
    private List<TestCaseResultVO> testCaseResults;
    
    private Integer passedCount;
    
    private Integer totalCount;
}