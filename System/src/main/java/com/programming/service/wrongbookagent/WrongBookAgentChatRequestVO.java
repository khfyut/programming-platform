package com.programming.service.wrongbookagent;

import lombok.Data;

@Data
public class WrongBookAgentChatRequestVO {
    private Long wrongItemId;
    private String sessionId;
    private String message;
    private String actionType;
    private String taskType;
    private String answer;
    private Integer hintLevel;
}
