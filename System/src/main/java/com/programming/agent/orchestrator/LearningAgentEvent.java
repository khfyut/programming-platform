package com.programming.agent.orchestrator;

import com.programming.agent.AgentPolicyProfile;

import java.util.Map;

public class LearningAgentEvent {
    private String eventType;
    private AgentPolicyProfile policyProfile;
    private Long userId;
    private String userMessage;
    private Long problemId;
    private Long submitId;
    private Long wrongItemId;
    private Long pathId;
    private Long levelId;
    private Map<String, Object> routeContext;
    private Map<String, Object> runtimeContext;

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public AgentPolicyProfile getPolicyProfile() { return policyProfile; }
    public void setPolicyProfile(AgentPolicyProfile policyProfile) { this.policyProfile = policyProfile; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserMessage() { return userMessage; }
    public void setUserMessage(String userMessage) { this.userMessage = userMessage; }

    public Long getProblemId() { return problemId; }
    public void setProblemId(Long problemId) { this.problemId = problemId; }

    public Long getSubmitId() { return submitId; }
    public void setSubmitId(Long submitId) { this.submitId = submitId; }

    public Long getWrongItemId() { return wrongItemId; }
    public void setWrongItemId(Long wrongItemId) { this.wrongItemId = wrongItemId; }

    public Long getPathId() { return pathId; }
    public void setPathId(Long pathId) { this.pathId = pathId; }

    public Long getLevelId() { return levelId; }
    public void setLevelId(Long levelId) { this.levelId = levelId; }

    public Map<String, Object> getRouteContext() { return routeContext; }
    public void setRouteContext(Map<String, Object> routeContext) { this.routeContext = routeContext; }

    public Map<String, Object> getRuntimeContext() { return runtimeContext; }
    public void setRuntimeContext(Map<String, Object> runtimeContext) { this.runtimeContext = runtimeContext; }
}
