package com.programming.agent;

public class AgentExecutionResult {
    private final boolean executed;
    private final String blockedReason;

    public AgentExecutionResult(boolean executed, String blockedReason) {
        this.executed = executed;
        this.blockedReason = blockedReason;
    }

    public static AgentExecutionResult executed() {
        return new AgentExecutionResult(true, null);
    }

    public static AgentExecutionResult blocked(String reason) {
        return new AgentExecutionResult(false, reason);
    }

    public boolean isExecuted() {
        return executed;
    }

    public String getBlockedReason() {
        return blockedReason;
    }
}
