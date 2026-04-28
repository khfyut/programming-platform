package com.programming.agent.orchestrator;

@FunctionalInterface
public interface LearningTool {
    ToolResult execute(LearningAgentEvent event);

    default String name() {
        return getClass().getSimpleName();
    }

    default boolean supports(LearningAgentEvent event) {
        return true;
    }
}
