package com.programming.util.sandbox;

public record SandboxLimits(long timeoutMillis, long memoryBytes, Double cpuLimit) {
    public SandboxLimits(long timeoutMillis, long memoryBytes) {
        this(timeoutMillis, memoryBytes, null);
    }
}
