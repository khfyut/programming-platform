package com.programming.agent.dto;

import lombok.Data;

import java.util.Map;

@Data
public class AgentCoachDecideRequestDTO {
    private String triggerSource;
    private String currentRoute;
    private String pageType;
    private Long problemId;
    private Long submitId;
    private Long pathId;
    private Long levelId;
    private Long wrongItemId;
    private Integer dwellSeconds;
    private Map<String, Object> metadata;
}
