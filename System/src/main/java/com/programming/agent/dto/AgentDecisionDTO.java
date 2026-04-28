package com.programming.agent.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
    @JSONField(name = "user_intent")
    private String userIntent;
    @JSONField(name = "teaching_goal")
    private String teachingGoal;
    @JSONField(name = "requested_scope")
    private String requestedScope;
    @JSONField(name = "answer_scope")
    private String answerScope;
    @JSONField(name = "risk_level")
    private String riskLevel;
    @JSONField(name = "confidence")
    private Double confidence;
    @JSONField(name = "used_tool_signals")
    private List<String> usedToolSignals;
    @JSONField(name = "content_plan")
    private Map<String, Object> contentPlan;
    @JSONField(name = "quality_flags")
    private List<String> qualityFlags;
    @JSONField(name = "normalized_intent")
    private String normalizedIntent;
    @JSONField(name = "intent_confidence")
    private Double intentConfidence;
    @JSONField(name = "intent_reason")
    private String intentReason;
    @JSONField(name = "clarification_question")
    private String clarificationQuestion;
    @JSONField(name = "answered_user_question")
    private Boolean answeredUserQuestion;
    @JSONField(name = "alignment_reason")
    private String alignmentReason;
    @JSONField(name = "decision_stage")
    private String decisionStage;
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

    public String getUserIntent() { return userIntent; }
    public void setUserIntent(String userIntent) { this.userIntent = userIntent; }

    public String getTeachingGoal() { return teachingGoal; }
    public void setTeachingGoal(String teachingGoal) { this.teachingGoal = teachingGoal; }

    public String getRequestedScope() { return requestedScope; }
    public void setRequestedScope(String requestedScope) { this.requestedScope = requestedScope; }

    public String getAnswerScope() { return answerScope; }
    public void setAnswerScope(String answerScope) { this.answerScope = answerScope; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }

    public List<String> getUsedToolSignals() { return usedToolSignals; }
    public void setUsedToolSignals(List<String> usedToolSignals) { this.usedToolSignals = usedToolSignals; }

    public Map<String, Object> getContentPlan() { return contentPlan; }
    public void setContentPlan(Map<String, Object> contentPlan) { this.contentPlan = contentPlan; }

    public List<String> getQualityFlags() { return qualityFlags; }
    public void setQualityFlags(List<String> qualityFlags) { this.qualityFlags = qualityFlags; }

    public String getNormalizedIntent() { return normalizedIntent; }
    public void setNormalizedIntent(String normalizedIntent) { this.normalizedIntent = normalizedIntent; }

    public Double getIntentConfidence() { return intentConfidence; }
    public void setIntentConfidence(Double intentConfidence) { this.intentConfidence = intentConfidence; }

    public String getIntentReason() { return intentReason; }
    public void setIntentReason(String intentReason) { this.intentReason = intentReason; }

    public String getClarificationQuestion() { return clarificationQuestion; }
    public void setClarificationQuestion(String clarificationQuestion) { this.clarificationQuestion = clarificationQuestion; }

    public Boolean getAnsweredUserQuestion() { return answeredUserQuestion; }
    public void setAnsweredUserQuestion(Boolean answeredUserQuestion) { this.answeredUserQuestion = answeredUserQuestion; }

    public String getAlignmentReason() { return alignmentReason; }
    public void setAlignmentReason(String alignmentReason) { this.alignmentReason = alignmentReason; }

    public String getDecisionStage() { return decisionStage; }
    public void setDecisionStage(String decisionStage) { this.decisionStage = decisionStage; }

    public List<String> getUsedKnowledgeRefs() { return usedKnowledgeRefs; }
    public void setUsedKnowledgeRefs(List<String> usedKnowledgeRefs) { this.usedKnowledgeRefs = usedKnowledgeRefs; }

    public Boolean getExecuted() { return executed; }
    public void setExecuted(Boolean executed) { this.executed = executed; }

    public String getBlockedReason() { return blockedReason; }
    public void setBlockedReason(String blockedReason) { this.blockedReason = blockedReason; }
}
