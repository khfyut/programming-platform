package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AssessmentQuestion {
    private Long id;
    private String language;
    private String direction;
    private String question;
    private String options;
    private String correctAnswer;
    private Integer difficulty;
    private String knowledgePoint;
    private LocalDateTime createTime;
}