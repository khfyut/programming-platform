package com.programming.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AgentCoachNudge {
    private Long id;
    private Long userId;
    private String requestId;
    private String triggerSource;
    private String actionType;
    private String pedagogicalGoal;
    private String title;
    private String summary;
    private String targetRoute;
    private Integer priority;
    private String status;
    private String metadataJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiresAt;
}
