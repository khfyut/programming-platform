package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class KnowledgeModule {
    private Long id;
    private String name;
    private String description;
    private Long parentId;
    private Integer level;
    private String language;
    private String direction;
    private Integer difficulty;
    private Integer orderNum;
    private Boolean isActive;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    private List<KnowledgeModule> children;
    private List<KnowledgeModule> prerequisites;
}
