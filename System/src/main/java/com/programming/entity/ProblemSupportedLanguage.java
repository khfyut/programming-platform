package com.programming.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProblemSupportedLanguage {
    private Long id;
    private Long problemId;
    private String languageCode;
    private Integer isDefault;
    private String starterCode;
    private String starterFilename;
    private String status;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
