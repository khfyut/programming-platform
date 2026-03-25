package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class LearningPath {
    private Long id;
    private String name;
    private String direction;
    private String language;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<PathChapter> chapters;
}