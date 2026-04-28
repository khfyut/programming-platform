package com.programming.agent.dto;

import lombok.Data;

@Data
public class AgentCoachDecisionDTO {
    private boolean created;
    private String reason;
    private AgentCoachNudgeDTO nudge;
}
