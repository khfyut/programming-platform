package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AuditLog {
    private Long id;
    private Long userId;
    private String operation;
    private String targetType;
    private Long targetId;
    private String details;
    private String ipAddress;
    private LocalDateTime createTime;
    private User user;
}