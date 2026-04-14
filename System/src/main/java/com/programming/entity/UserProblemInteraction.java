package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户题目交互状态实体
 * 记录用户对某道题的交互状态和学习语义状态
 *
 * 学习语义状态（新增）：
 * - errorTag: 最近错误标签
 * - weakPoints: 薄弱知识点（JSON字符串）
 * - learningStage: 学习阶段
 */
@Data
public class UserProblemInteraction {
    private Long id;
    private Long userId;
    private Long problemId;
    private Integer hintCount;
    private Integer diagnoseCount;
    private Integer explainCount;
    private Integer recommendCount;
    private Integer reflectCount;
    private Boolean hasViewedAnswer;
    private Integer consecutiveFailures;
    private Long lastSubmitId;
    private LocalDateTime lastActionTime;
    private String lastActionType;
    private String lastGoal;
    private String lastGuidanceType;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 学习语义状态（新增）
    private String errorTag;        // 最近错误标签
    private String weakPoints;      // 薄弱知识点（JSON格式）
    private String learningStage;   // 学习阶段
}
