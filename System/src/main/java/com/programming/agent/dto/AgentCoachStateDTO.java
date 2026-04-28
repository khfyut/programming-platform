package com.programming.agent.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AgentCoachStateDTO {
    private String displayState;
    private Integer unreadCount;
    private AgentCoachNudgeDTO activeNudge;
    private List<AgentCoachNudgeDTO> activeNudges;
    private Map<String, Object> recentSummary;
}
