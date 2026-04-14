package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Agent学习事件日志实体
 * 记录Agent决策产生的动作执行历史
 */
@Data
public class LearningEventLog {
    private Long id;
    private Long userId;
    private Long problemId;
    private Long submitId;
    private String actionType;
    private String actionId;
    private String triggerSource;
    private String userIntent;
    private String pedagogicalGoal;
    private String contentType;
    private String strategy;
    private String decisionReason;
    private String content;
    private String requestId;
    private Boolean executed;
    private String blockedReason;
    private String entryRefType;
    private Long entryRefId;
    private Long pathId;
    private Long levelId;
    private Long wrongItemId;
    private LocalDateTime createTime;
}
