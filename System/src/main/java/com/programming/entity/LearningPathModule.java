package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LearningPathModule {
    private Long id;
    private Long pathId;
    private Long moduleId;
    private Integer orderNum;
    private LocalDateTime createTime;
}
