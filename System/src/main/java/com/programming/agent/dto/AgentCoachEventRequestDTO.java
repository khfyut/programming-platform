package com.programming.agent.dto;

import lombok.Data;

import java.util.Map;

@Data
public class AgentCoachEventRequestDTO {
    private Long nudgeId;
    private String eventType;
    private String feedbackType;
    private String currentRoute;
    private Map<String, Object> metadata;
}
