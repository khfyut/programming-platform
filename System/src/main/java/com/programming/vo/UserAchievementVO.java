package com.programming.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserAchievementVO {
    private Long achievementId;
    private String name;
    private String description;
    private String icon;
    private LocalDateTime unlockTime;
}