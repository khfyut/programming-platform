package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SandboxStatus {
    private Long id;
    private String containerId;
    private String status;
    private Double cpuUsage;
    private Double memoryUsage;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}