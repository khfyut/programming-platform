package com.programming.service.impl;

import com.programming.service.CodeSandboxService;
import com.programming.util.DockerUtil;
import com.programming.vo.CodeExecutionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
@Service
public class CodeSandboxServiceImpl implements CodeSandboxService {

    @Autowired
    private DockerUtil dockerUtil;

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Override
    public String runCode(String code, String language, String input) {
        return dockerUtil.runCode(code, language, input);
    }

    @Override
    public CodeExecutionResult executeCode(String code, String language, String input) {
        return dockerUtil.executeCode(code, language, input);
    }

    @Override
    public CodeExecutionResult executeCode(String code, String language, String input, Integer timeLimit, Integer memoryLimit) {
        return dockerUtil.executeCode(code, language, input, timeLimit, memoryLimit);
    }

    @Override
    public List<CodeExecutionResult> runCodeBatch(String code, String language, List<String> inputs) {
        return executeBatch(code, language, inputs, null, null);
    }

    @Override
    public List<CodeExecutionResult> runCodeBatch(String code, String language, List<String> inputs, Integer timeLimit, Integer memoryLimit) {
        return executeBatch(code, language, inputs, timeLimit, memoryLimit);
    }

    private List<CodeExecutionResult> executeBatch(String code, String language, List<String> inputs,
                                                   Integer timeLimit, Integer memoryLimit) {
        List<CodeExecutionResult> results = new ArrayList<>();
        List<Future<CodeExecutionResult>> futures = new ArrayList<>();

        for (String input : inputs) {
            Future<CodeExecutionResult> future = executorService.submit(() -> {
                try {
                    if (timeLimit == null && memoryLimit == null) {
                        return dockerUtil.executeCode(code, language, input);
                    }
                    return dockerUtil.executeCode(code, language, input, timeLimit, memoryLimit);
                } catch (Exception e) {
                    log.error("Code execution exception", e);
                    CodeExecutionResult result = new CodeExecutionResult();
                    result.setExitCode(-1);
                    result.setErrorMessage("Execution exception: " + e.getMessage());
                    result.setTimeCost(0);
                    result.setMemoryCost(0);
                    return result;
                }
            });
            futures.add(future);
        }

        for (Future<CodeExecutionResult> future : futures) {
            try {
                results.add(future.get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Interrupted while reading execution result", e);
                results.add(buildFutureErrorResult(e));
            } catch (ExecutionException e) {
                log.error("Failed to read execution result", e);
                results.add(buildFutureErrorResult(e));
            }
        }

        return results;
    }

    private CodeExecutionResult buildFutureErrorResult(Exception e) {
        CodeExecutionResult errorResult = new CodeExecutionResult();
        errorResult.setExitCode(-1);
        errorResult.setErrorMessage("Execution exception: " + e.getMessage());
        errorResult.setTimeCost(0);
        errorResult.setMemoryCost(0);
        return errorResult;
    }
}
