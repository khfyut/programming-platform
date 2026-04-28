package com.programming.agent.orchestrator;

import java.util.LinkedHashMap;
import java.util.Map;

public class ToolResult {
    private final String toolName;
    private final boolean success;
    private final Map<String, Object> data;
    private final String error;

    private ToolResult(String toolName, boolean success, Map<String, Object> data, String error) {
        this.toolName = toolName;
        this.success = success;
        this.data = data == null ? new LinkedHashMap<>() : new LinkedHashMap<>(data);
        this.error = error;
    }

    public static ToolResult success(String toolName, Map<String, Object> data) {
        return new ToolResult(toolName, true, data, null);
    }

    public static ToolResult failure(String toolName, String error) {
        return new ToolResult(toolName, false, Map.of(), error);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("tool_name", toolName);
        result.put("success", success);
        result.put("data", data);
        if (error != null && !error.isBlank()) {
            result.put("error", error);
        }
        return result;
    }

    public String getToolName() {
        return toolName;
    }

    public boolean isSuccess() {
        return success;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public String getError() {
        return error;
    }
}
