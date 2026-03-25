package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Course {
    private Long id;
    private String name;
    private String description;
    private String language;
    private Long pathId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LearningPath path;
    private List<CourseChapter> chapters;
}