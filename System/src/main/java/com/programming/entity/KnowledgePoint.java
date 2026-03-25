package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class KnowledgePoint {
    private Long id;
    private String name;
    private Long parentId;
    private Integer level;
    private String description;
    private Integer difficulty;
    private String domain;
    private Integer importance;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<KnowledgePoint> children;
    private List<KnowledgeRelationship> relationships;
}