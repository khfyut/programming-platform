package com.programming.vo.problemagent;

import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class ProblemAgentResponseVO {
    private String sessionId;
    private String reply;
    private String summary;
    private List<CoachActionVO> actions = new ArrayList<>();
    private List<CoachRecommendationCardVO> recommendations = new ArrayList<>();
    private String draftCode;
    private Boolean canRevealFullSolution;
    private String requestId;
    private String actionType;
    private String pedagogicalGoal;
    private String contentType;
    private String nextSuggestion;
    private String errorTag;
    private List<String> weakPoints = new ArrayList<>();
    private Map<String, Object> contextSnapshot = new LinkedHashMap<>();
}
