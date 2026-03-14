package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Submit {
    private Long id;
    private Long userId;
    private Long problemId;
    private String code;
    private String language;
    private Integer result;
    private Integer timeCost;
    private Integer memoryCost;
    private LocalDateTime createTime;
    
    private List<SubmitTestCaseResult> testCaseResults;
}
