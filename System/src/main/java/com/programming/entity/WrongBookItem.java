package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WrongBookItem {
    private Long id;
    private Long wrongBookId;
    private Long problemId;
    private Long submitId;
    private String code;
    private String language;
    private String errorMessage;
    private String knowledgePoints;
    private Integer reviewStatus;
    private LocalDateTime lastReviewTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Problem problem;
}