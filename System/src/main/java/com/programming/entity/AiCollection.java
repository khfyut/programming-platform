package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AiCollection {
    private Long id;
    private Long userId;
    private String sessionId;
    private String content;
    private LocalDateTime createTime;
}