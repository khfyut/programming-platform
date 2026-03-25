package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AiMessage {
    private Long id;
    private String sessionId;
    private String role;
    private String content;
    private Long relatedProblemId;
    private Long relatedSubmitId;
    private LocalDateTime createTime;
}