package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SystemLog {
    private Long id;
    private String level;
    private String category;
    private String message;
    private String stackTrace;
    private LocalDateTime createTime;
}