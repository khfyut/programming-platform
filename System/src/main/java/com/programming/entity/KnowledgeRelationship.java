package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KnowledgeRelationship {
    private Long id;
    private Long sourceId;
    private Long targetId;
    private String relationType;
    private Integer strength;
    private String description;
    private LocalDateTime createTime;
}