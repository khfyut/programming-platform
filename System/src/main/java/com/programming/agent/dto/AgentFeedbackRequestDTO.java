package com.programming.agent.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class AgentFeedbackRequestDTO {
    @JsonProperty("request_id")
    @JsonAlias("requestId")
    private String requestId;

    @JsonProperty("problem_id")
    @JsonAlias("problemId")
    private Long problemId;

    @JsonProperty("entry_ref_type")
    @JsonAlias("entryRefType")
    private String entryRefType;

    @JsonProperty("entry_ref_id")
    @JsonAlias("entryRefId")
    private Long entryRefId;

    @JsonProperty("path_id")
    @JsonAlias("pathId")
    private Long pathId;

    @JsonProperty("level_id")
    @JsonAlias("levelId")
    private Long levelId;

    @JsonProperty("wrong_item_id")
    @JsonAlias("wrongItemId")
    private Long wrongItemId;

    @JsonProperty("submit_id")
    @JsonAlias("submitId")
    private Long submitId;

    @JsonProperty("action_type")
    @JsonAlias("actionType")
    private String actionType;

    @JsonProperty("feedback_type")
    @JsonAlias("feedbackType")
    private String feedbackType;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;
}
