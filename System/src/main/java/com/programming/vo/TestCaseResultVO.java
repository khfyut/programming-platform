package com.programming.vo;

import lombok.Data;

@Data
public class TestCaseResultVO {
    private Long testCaseId;
    private Integer sortOrder;
    private String input;
    private String expectedOutput;
    private String actualOutput;
    private Integer result;
    private Integer timeCost;
    private Integer memoryCost;
    private String errorMessage;
}