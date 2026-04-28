package com.programming.agent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class AgentCoachChatRequestDTO {
    @JsonProperty("session_id")
    private String sessionId;

    private String message;

    @JsonProperty("current_route")
    private String currentRoute;

    @JsonProperty("page_type")
    private String pageType;

    @JsonProperty("problem_id")
    private Long problemId;

    @JsonProperty("submit_id")
    private Long submitId;

    @JsonProperty("path_id")
    private Long pathId;

    @JsonProperty("level_id")
    private Long levelId;

    @JsonProperty("wrong_item_id")
    private Long wrongItemId;

    private Map<String, Object> metadata;
}
