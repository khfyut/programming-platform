package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TestCase {
    private Long id;
    private Long problemId;
    private String input;
    private String output;
    private Boolean isSample;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}