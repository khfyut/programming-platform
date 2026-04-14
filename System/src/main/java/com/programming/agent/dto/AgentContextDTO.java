package com.programming.agent.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

public class AgentContextDTO {

    @JsonProperty("request_id")
    @JSONField(name = "request_id")
    private String requestId;

    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JSONField(name = "timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("problem")
    @JSONField(name = "problem")
    private ProblemContextDTO problem;

    @JsonProperty("submission")
    @JSONField(name = "submission")
    private SubmissionContextDTO submission;

    @JsonProperty("trigger_source")
    @JSONField(name = "trigger_source")
    private String triggerSource;

    @JsonProperty("user_intent")
    @JSONField(name = "user_intent")
    private String userIntent;

    @JsonProperty("user_message")
    @JSONField(name = "user_message")
    private String userMessage;

    @JsonProperty("requested_full_solution")
    @JSONField(name = "requested_full_solution")
    private boolean requestedFullSolution;

    @JsonProperty("consecutive_failures")
    @JSONField(name = "consecutive_failures")
    private int consecutiveFailures;

    @JsonProperty("has_viewed_solution")
    @JSONField(name = "has_viewed_solution")
    private boolean hasViewedSolution;

    @JsonProperty("hint_count")
    @JSONField(name = "hint_count")
    private int hintCount;

    @JsonProperty("diagnose_count")
    @JSONField(name = "diagnose_count")
    private int diagnoseCount;

    @JsonProperty("explain_count")
    @JSONField(name = "explain_count")
    private int explainCount;

    @JsonProperty("recommend_count")
    @JSONField(name = "recommend_count")
    private int recommendCount;

    @JsonProperty("reflect_count")
    @JSONField(name = "reflect_count")
    private int reflectCount;

    @JsonProperty("last_action_type")
    @JSONField(name = "last_action_type")
    private String lastActionType;

    @JsonProperty("last_goal")
    @JSONField(name = "last_goal")
    private String lastGoal;

    @JsonProperty("last_guidance_type")
    @JSONField(name = "last_guidance_type")
    private String lastGuidanceType;

    @JsonProperty("last_error_tag")
    @JSONField(name = "last_error_tag")
    private String lastErrorTag;

    @JsonProperty("weak_points")
    @JSONField(name = "weak_points")
    private List<String> weakPoints;

    @JsonProperty("learning_stage")
    @JSONField(name = "learning_stage")
    private String learningStage;

    // Getters and Setters
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public ProblemContextDTO getProblem() { return problem; }
    public void setProblem(ProblemContextDTO problem) { this.problem = problem; }

    public SubmissionContextDTO getSubmission() { return submission; }
    public void setSubmission(SubmissionContextDTO submission) { this.submission = submission; }

    public String getTriggerSource() { return triggerSource; }
    public void setTriggerSource(String triggerSource) { this.triggerSource = triggerSource; }

    public String getUserIntent() { return userIntent; }
    public void setUserIntent(String userIntent) { this.userIntent = userIntent; }

    public String getUserMessage() { return userMessage; }
    public void setUserMessage(String userMessage) { this.userMessage = userMessage; }

    public boolean isRequestedFullSolution() { return requestedFullSolution; }
    public void setRequestedFullSolution(boolean requestedFullSolution) { this.requestedFullSolution = requestedFullSolution; }

    public int getConsecutiveFailures() { return consecutiveFailures; }
    public void setConsecutiveFailures(int consecutiveFailures) { this.consecutiveFailures = consecutiveFailures; }

    public boolean isHasViewedSolution() { return hasViewedSolution; }
    public void setHasViewedSolution(boolean hasViewedSolution) { this.hasViewedSolution = hasViewedSolution; }

    public int getHintCount() { return hintCount; }
    public void setHintCount(int hintCount) { this.hintCount = hintCount; }

    public int getDiagnoseCount() { return diagnoseCount; }
    public void setDiagnoseCount(int diagnoseCount) { this.diagnoseCount = diagnoseCount; }

    public int getExplainCount() { return explainCount; }
    public void setExplainCount(int explainCount) { this.explainCount = explainCount; }

    public int getRecommendCount() { return recommendCount; }
    public void setRecommendCount(int recommendCount) { this.recommendCount = recommendCount; }

    public int getReflectCount() { return reflectCount; }
    public void setReflectCount(int reflectCount) { this.reflectCount = reflectCount; }

    public String getLastActionType() { return lastActionType; }
    public void setLastActionType(String lastActionType) { this.lastActionType = lastActionType; }

    public String getLastGoal() { return lastGoal; }
    public void setLastGoal(String lastGoal) { this.lastGoal = lastGoal; }

    public String getLastGuidanceType() { return lastGuidanceType; }
    public void setLastGuidanceType(String lastGuidanceType) { this.lastGuidanceType = lastGuidanceType; }

    public String getLastErrorTag() { return lastErrorTag; }
    public void setLastErrorTag(String lastErrorTag) { this.lastErrorTag = lastErrorTag; }

    public List<String> getWeakPoints() { return weakPoints; }
    public void setWeakPoints(List<String> weakPoints) { this.weakPoints = weakPoints; }

    public String getLearningStage() { return learningStage; }
    public void setLearningStage(String learningStage) { this.learningStage = learningStage; }
}
