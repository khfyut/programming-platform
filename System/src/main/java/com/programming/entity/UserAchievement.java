package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserAchievement {
    private Long id;
    private Long userId;
    private Long achievementId;
    private LocalDateTime createTime;
}