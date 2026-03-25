package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AlertConfig {
    private Long id;
    private String metricName;
    private Double threshold;
    private String operator;
    private String alertLevel;
    private Boolean enabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}