package com.programming.vo.problemagent;

import lombok.Data;

@Data
public class ProblemAgentChatRequestVO {
    private String sessionId;
    private Long problemId;
    private Long submitId;
    private String message;
    private String code;
    private String language;
    private String triggerType;
    private Boolean requestFullSolution;
    private Integer latestResultCode;
    private String errorMessage;
    private String executionOutput;
}
