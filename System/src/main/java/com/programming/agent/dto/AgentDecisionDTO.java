package com.programming.agent.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;

public class AgentDecisionDTO {
    @JSONField(name = "response_id")
    private String responseId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JSONField(name = "timestamp", format = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime timestamp;

    @JSONField(name = "decision_summary")
    private String decisionSummary;
    @JSONField(name = "selected_strategy")
    private String selectedStrategy;
    @JSONField(name = "decision_reason")
    private String decisionReason;
    @JSONField(name = "applied_constraints")
    private List<String> appliedConstraints;
    @JSONField(name = "blocked_actions")
    private List<String> blockedActions;
    @JSONField(name = "candidate_actions")
    private List<CandidateActionDTO> candidateActions;
    @JSONField(name = "recommended_action_id")
    private String recommendedActionId;
    @JSONField(name = "action_type")
    private String actionType;
    @JSONField(name = "error_tag")
    private String errorTag;
    @JSONField(name = "weak_points")
    private List<String> weakPoints;
    @JSONField(name = "suggested_next_action")
    private String suggestedNextAction;
    @JSONField(name = "content")
    private String content;
    @JSONField(name = "content_type")
    private String contentType;
    @JSONField(name = "main_response")
    private String mainResponse;
    @JSONField(name = "next_suggestion")
    private String nextSuggestion;
    @JSONField(name = "pedagogical_goal")
    private String pedagogicalGoal;
    @JSONField(name = "confidence")
    private Double confidence;
    @JSONField(name = "used_knowledge_refs")
    private List<String> usedKnowledgeRefs;
    @JSONField(name = "executed")
    private Boolean executed;
    @JSONField(name = "blocked_reason")
    private String blockedReason;

    // Getters and Setters
    public String getResponseId() { return responseId; }
    public void setResponseId(String responseId) { this.responseId = responseId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getDecisionSummary() { return decisionSummary; }
    public void setDecisionSummary(String decisionSummary) { this.decisionSummary = decisionSummary; }

    public String getSelectedStrategy() { return selectedStrategy; }
    public void setSelectedStrategy(String selectedStrategy) { this.selectedStrategy = selectedStrategy; }

    public String getDecisionReason() { return decisionReason; }
    public void setDecisionReason(String decisionReason) { this.decisionReason = decisionReason; }

    public List<String> getAppliedConstraints() { return appliedConstraints; }
    public void setAppliedConstraints(List<String> appliedConstraints) { this.appliedConstraints = appliedConstraints; }

    public List<String> getBlockedActions() { return blockedActions; }
    public void setBlockedActions(List<String> blockedActions) { this.blockedActions = blockedActions; }

    public List<CandidateActionDTO> getCandidateActions() { return candidateActions; }
    public void setCandidateActions(List<CandidateActionDTO> candidateActions) { this.candidateActions = candidateActions; }

    public String getRecommendedActionId() { return recommendedActionId; }
    public void setRecommendedActionId(String recommendedActionId) { this.recommendedActionId = recommendedActionId; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getErrorTag() { return errorTag; }
    public void setErrorTag(String errorTag) { this.errorTag = errorTag; }

    public List<String> getWeakPoints() { return weakPoints; }
    public void setWeakPoints(List<String> weakPoints) { this.weakPoints = weakPoints; }

    public String getSuggestedNextAction() { return suggestedNextAction; }
    public void setSuggestedNextAction(String suggestedNextAction) { this.suggestedNextAction = suggestedNextAction; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getMainResponse() { return mainResponse; }
    public void setMainResponse(String mainResponse) { this.mainResponse = mainResponse; }

    public String getNextSuggestion() { return nextSuggestion; }
    public void setNextSuggestion(String nextSuggestion) { this.nextSuggestion = nextSuggestion; }

    public String getPedagogicalGoal() { return pedagogicalGoal; }
    public void setPedagogicalGoal(String pedagogicalGoal) { this.pedagogicalGoal = pedagogicalGoal; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }

    public List<String> getUsedKnowledgeRefs() { return usedKnowledgeRefs; }
    public void setUsedKnowledgeRefs(List<String> usedKnowledgeRefs) { this.usedKnowledgeRefs = usedKnowledgeRefs; }

    public Boolean getExecuted() { return executed; }
    public void setExecuted(Boolean executed) { this.executed = executed; }

    public String getBlockedReason() { return blockedReason; }
    public void setBlockedReason(String blockedReason) { this.blockedReason = blockedReason; }
}
