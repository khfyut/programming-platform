package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PathLevel {
    private Long id;
    private Long chapterId;
    private String name;
    private Integer orderNum;
    private String problemIds;
    private String knowledgePoints;
    private String unlockCondition;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}