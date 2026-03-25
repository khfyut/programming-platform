package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LearningResource {
    private Long id;
    private Long levelId;
    private String name;
    private String type;
    private String url;
    private String description;
    private String knowledgePoints;
    private Integer orderNum;
    private Integer visibility;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}