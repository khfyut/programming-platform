package com.programming.util.sandbox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SandboxResourceLimitResolver {
    private static final long ONE_KB = 1024L;
    private static final long ONE_MB = 1024L * 1024L;
    private static final long ONE_SECOND_MS = 1000L;
    private static final long TIMEOUT_GRACE_MS = 1000L;
    private static final long MIN_DOCKER_MEMORY_BYTES = 6L * ONE_MB;
    private static final long MAX_REASONABLE_PROBLEM_MEMORY_MB = 10240L;
    private static final long MAX_REASONABLE_PROBLEM_MEMORY_KB = MAX_REASONABLE_PROBLEM_MEMORY_MB * ONE_KB;
    private static final long MAX_REASONABLE_PROBLEM_TIME_SECONDS = 60L;

    @Value("${programming.docker.timeout}")
    private int defaultTimeout;

    @Value("${programming.docker.memory}")
    private long defaultMemory;

    @Value("${programming.docker.cpu}")
    private double defaultCpu;

    public SandboxLimits resolve(Integer requestedTimeLimit, Integer requestedMemoryLimit) {
        return resolve(requestedTimeLimit, requestedMemoryLimit, null);
    }

    public SandboxLimits resolve(Integer requestedTimeLimit, Integer requestedMemoryLimit, Double cpuLimit) {
        return new SandboxLimits(
                resolveTimeoutMillis(requestedTimeLimit),
                resolveMemoryLimitBytes(requestedMemoryLimit),
                cpuLimit != null ? cpuLimit : defaultCpu
        );
    }

    public long bytesToKb(long bytes) {
        if (bytes <= 0) {
            return 0;
        }
        return Math.max(1, (bytes + ONE_KB - 1) / ONE_KB);
    }

    private long resolveMemoryLimitBytes(Integer requestedMemoryLimit) {
        if (requestedMemoryLimit == null || requestedMemoryLimit <= 0) {
            return defaultMemory;
        }

        long actualMemory;
        if (requestedMemoryLimit <= MAX_REASONABLE_PROBLEM_MEMORY_MB) {
            actualMemory = requestedMemoryLimit.longValue() * ONE_MB;
        } else if (requestedMemoryLimit <= MAX_REASONABLE_PROBLEM_MEMORY_KB
                && requestedMemoryLimit.longValue() % ONE_MB != 0) {
            actualMemory = requestedMemoryLimit.longValue() * ONE_KB;
        } else {
            actualMemory = requestedMemoryLimit.longValue();
        }

        if (actualMemory < MIN_DOCKER_MEMORY_BYTES) {
            log.warn("Problem memory limit {} is below Docker minimum, clamped to {} bytes",
                    requestedMemoryLimit, MIN_DOCKER_MEMORY_BYTES);
            return MIN_DOCKER_MEMORY_BYTES;
        }

        return actualMemory;
    }

    private long resolveTimeoutMillis(Integer requestedTimeLimit) {
        if (requestedTimeLimit == null || requestedTimeLimit <= 0) {
            return defaultTimeout;
        }

        long normalizedTimeout;
        if (requestedTimeLimit <= MAX_REASONABLE_PROBLEM_TIME_SECONDS) {
            normalizedTimeout = requestedTimeLimit.longValue() * ONE_SECOND_MS;
        } else {
            normalizedTimeout = requestedTimeLimit.longValue();
        }

        return Math.max(defaultTimeout, normalizedTimeout + TIMEOUT_GRACE_MS);
    }
}
