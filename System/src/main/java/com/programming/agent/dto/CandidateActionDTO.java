package com.programming.agent.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import java.util.List;

public class CandidateActionDTO {
    @JSONField(name = "action_id")
    private String actionId;
    @JSONField(name = "action_type")
    private String actionType;
    @JSONField(name = "title")
    private String title;
    @JSONField(name = "description")
    private String description;
    @JSONField(name = "priority")
    private String priority;
    @JSONField(name = "content")
    private String content;
    @JSONField(name = "required_conditions")
    private List<String> requiredConditions;

    // Getters and Setters
    public String getActionId() { return actionId; }
    public void setActionId(String actionId) { this.actionId = actionId; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public List<String> getRequiredConditions() { return requiredConditions; }
    public void setRequiredConditions(List<String> requiredConditions) { this.requiredConditions = requiredConditions; }
}
