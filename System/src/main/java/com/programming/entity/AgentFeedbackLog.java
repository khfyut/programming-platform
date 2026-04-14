package com.programming.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AgentFeedbackLog {
    private Long id;
    private String requestId;
    private Long userId;
    private Long problemId;
    private String entryRefType;
    private Long entryRefId;
    private Long pathId;
    private Long levelId;
    private Long wrongItemId;
    private Long submitId;
    private String actionType;
    private String feedbackType;
    private String metadataJson;
    private LocalDateTime createTime;
}
