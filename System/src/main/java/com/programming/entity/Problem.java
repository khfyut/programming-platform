package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Problem {
    private Long id;
    private String title;
    private String content;
    private Integer difficulty;
    private String language;
    private String status;
    private String input;
    private String output;
    
    private Integer timeLimit;
    private Integer memoryLimit;
    private Double  cpuLimit;
    private String tags;
    private String knowledgePoints;
    private String hints;
    private String sampleExplanation;
    private Long chapterId;
    private Long levelId;
    private Long categoryId;
    private Long subCategoryId;
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    private List<TestCase> testCases;
    private PathChapter chapter;
    private PathLevel level;
}
