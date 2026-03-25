package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ClassInfo {
    private Long id;
    private String name;
    private Long teacherId;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private User teacher;
    private List<UserClass> members;
}