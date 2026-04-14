package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AiSession {
    private Long id;
    private String sessionId;
    private Long userId;
    private String topic;
    private Integer status;
    private String sessionType;
    private Long relatedProblemId;
    private Long lastSubmitId;
    private String metadataJson;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<AiMessage> messages;
}
