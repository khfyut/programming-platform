package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SystemMonitor {
    private Long id;
    private String metricName;
    private Double metricValue;
    private String metricUnit;
    private LocalDateTime createTime;
}