package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PathChapter {
    private Long id;
    private Long pathId;
    private String name;
    private Integer orderNum;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<PathLevel> levels;
}