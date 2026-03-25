package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApiMetric {
    private Long id;
    private String apiPath;
    private String method;
    private Integer responseTime;
    private Integer statusCode;
    private LocalDateTime createTime;
}