package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SubmitTestCaseResult {
    private Long id;
    private Long submitId;
    private Long testCaseId;
    private Integer result;
    private Integer timeCost;
    private Integer memoryCost;
    private String actualOutput;
    private String errorMessage;
    private LocalDateTime createTime;
}