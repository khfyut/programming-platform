package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Favorite {
    private Long id;
    private Long userId;
    private String targetType;
    private Long targetId;
    private LocalDateTime createTime;
}