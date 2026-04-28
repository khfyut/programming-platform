package com.programming.agent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AgentCoachChatResponseDTO {
    @JsonProperty("session_id")
    private String sessionId;

    private String reply;
    private List<AgentCoachActionDTO> actions = new ArrayList<>();
    private String scene;
    private String source;
    private boolean fallback;
}
