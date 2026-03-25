package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CourseChapter {
    private Long id;
    private Long courseId;
    private String name;
    private Integer orderNum;
    private String content;
    private String videoUrl;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Course course;
}