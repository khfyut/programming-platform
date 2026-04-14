package com.programming.agent.dto;

import com.alibaba.fastjson2.annotation.JSONField;

public class SubmissionContextDTO {
    @JSONField(name = "submit_id")
    private int submitId;
    @JSONField(name = "status")
    private String status;
    @JSONField(name = "error_message")
    private String errorMessage;
    @JSONField(name = "is_first_attempt")
    private boolean isFirstAttempt;
    @JSONField(name = "code_content")
    private String codeContent;
    @JSONField(name = "execution_output")
    private String executionOutput;

    // Getters and Setters
    public int getSubmitId() { return submitId; }
    public void setSubmitId(int submitId) { this.submitId = submitId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    @JSONField(name = "is_first_attempt")
    public boolean isFirstAttempt() { return isFirstAttempt; }

    @JSONField(name = "is_first_attempt")
    public void setFirstAttempt(boolean firstAttempt) { isFirstAttempt = firstAttempt; }

    public String getCodeContent() { return codeContent; }
    public void setCodeContent(String codeContent) { this.codeContent = codeContent; }

    public String getExecutionOutput() { return executionOutput; }
    public void setExecutionOutput(String executionOutput) { this.executionOutput = executionOutput; }
}
