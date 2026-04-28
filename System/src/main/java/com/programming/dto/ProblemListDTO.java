package com.programming.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProblemListDTO {
    private Long id;
    private String title;
    private Integer difficulty;
    private String language;
    private String tags;
    private String knowledgePoints;
    private Long categoryId;
    private Long subCategoryId;
    private LocalDateTime createTime;
    
    // 用户相关状态
    private Boolean isSolved;
    private Boolean isAttempted;
    private Boolean isFavorited;
    
    // 统计信息
    private Integer passRate;
    private Integer totalSubmissions;
    private Integer passedSubmissions;
}
