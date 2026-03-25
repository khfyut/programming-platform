package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserKnowledgeMastery {
    private Long id;
    private Long userId;
    private Long knowledgeId;
    private Integer masteryLevel;
    private Integer score;
    private LocalDateTime lastPracticeTime;
    private Integer practiceCount;
    private Integer correctCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private KnowledgePoint knowledgePoint;
}