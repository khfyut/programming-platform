package com.programming.agent.orchestrator;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class LearningToolRegistry {
    private final List<LearningTool> tools;

    public LearningToolRegistry(List<LearningTool> tools) {
        this.tools = tools == null ? Collections.emptyList() : List.copyOf(tools);
    }

    public List<Map<String, Object>> executeFor(LearningAgentEvent event) {
        List<Map<String, Object>> results = new ArrayList<>();
        for (LearningTool tool : tools) {
            if (tool == null || !tool.supports(event)) {
                continue;
            }
            try {
                ToolResult result = tool.execute(event);
                if (result != null) {
                    results.add(result.toMap());
                }
            } catch (Exception ex) {
                results.add(ToolResult.failure(tool.name(), ex.getMessage()).toMap());
            }
        }
        return results;
    }
}
