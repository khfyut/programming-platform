package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Achievement {
    private Long id;
    private String name;
    private String description;
    private String icon;
    private String conditionType;
    private Integer conditionValue;
    private LocalDateTime createTime;
}