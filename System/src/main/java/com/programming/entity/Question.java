package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Question {
    private Long id;
    private Long userId;
    private String content;
    private String answer;
    private String code;
    private LocalDateTime createTime;
}
