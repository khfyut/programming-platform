package com.programming.agent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AgentCoachActionDTO {
    private String type;
    private String label;
    private String route;

    @JsonProperty("event_name")
    private String eventName;

    private String prompt;
}
