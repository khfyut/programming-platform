package com.programming.vo.problemagent;

import com.programming.entity.AiMessage;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class ProblemAgentSessionVO {
    private String sessionId;
    private List<AiMessage> messages = new ArrayList<>();
    private String summary;
    private List<CoachActionVO> actions = new ArrayList<>();
    private List<CoachRecommendationCardVO> recommendations = new ArrayList<>();
    private String draftCode;
    private Boolean canRevealFullSolution;
    private Map<String, Object> contextSnapshot = new LinkedHashMap<>();
}
