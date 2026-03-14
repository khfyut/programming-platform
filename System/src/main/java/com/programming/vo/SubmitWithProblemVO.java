package com.programming.vo;

import com.programming.entity.SubmitTestCaseResult;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SubmitWithProblemVO {
    private Long id;
    private Long userId;
    private Long problemId;
    private String code;
    private String language;
    private Integer result;
    private Integer timeCost;
    private Integer memoryCost;
    private LocalDateTime createTime;
    private LocalDateTime submitTime;
    
    private String problemTitle;
    private String difficulty;
    private List<String> tags;
    
    private List<SubmitTestCaseResult> testCaseResults;
}
