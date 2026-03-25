package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserPathProgress {
    private Long id;
    private Long userId;
    private Long pathId;
    private Long currentChapterId;
    private Long currentLevelId;
    private String completedChapters;
    private String completedLevels;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}