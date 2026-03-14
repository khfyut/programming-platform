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
    private String input;
    private String output;
    
    private Integer timeLimit;
    private Integer memoryLimit;
    private String tags;
    private String knowledgePoints;
    private String hints;
    private String sampleExplanation;
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    private List<TestCase> testCases;
}
