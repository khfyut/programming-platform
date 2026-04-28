package com.programming.agent.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AgentCoachNudgeDTO {
    private Long id;
    private String requestId;
    private String triggerSource;
    private String actionType;
    private String pedagogicalGoal;
    private String title;
    private String summary;
    private String targetRoute;
    private Integer priority;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiresAt;
}
