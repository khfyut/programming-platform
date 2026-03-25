package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserAssessment {
    private Long id;
    private Long userId;
    private String language;
    private String direction;
    private Integer score;
    private String result;
    private String abilityLevel;
    private LocalDateTime createTime;
}