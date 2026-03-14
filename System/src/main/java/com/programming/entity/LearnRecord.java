package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LearnRecord {
    private Long id;
    private Long userId;
    private Integer problemCount;
    private Integer correctCount;
    private Long lastProblemId;
    private LocalDateTime lastLearnTime;
}
