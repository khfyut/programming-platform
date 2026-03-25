package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AlertRecord {
    private Long id;
    private Long configId;
    private Double metricValue;
    private String message;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}