package com.programming.service.impl;

import com.programming.service.CodeSandboxService;
import com.programming.util.DockerUtil;
import com.programming.vo.CodeExecutionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

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
    public List<CodeExecutionResult> runCodeBatch(String code, String language, List<String> inputs) {
        List<CodeExecutionResult> results = new ArrayList<>();
        List<Future<CodeExecutionResult>> futures = new ArrayList<>();
        
        for (String input : inputs) {
            Future<CodeExecutionResult> future = executorService.submit(() -> {
                CodeExecutionResult result = new CodeExecutionResult();
                long startTime = System.currentTimeMillis();
                
                try {
                    String output = dockerUtil.runCode(code, language, input);
                    long endTime = System.currentTimeMillis();
                    
                    result.setOutput(output);
                    result.setTimeCost(endTime - startTime);
                    result.setMemoryCost(0);
                    
                    if (output.startsWith("编译错误：")) {
                        result.setErrorMessage(output.substring("编译错误：".length()));
                        result.setExitCode(2);
                    } else if (output.startsWith("运行错误：")) {
                        result.setErrorMessage(output.substring("运行错误：".length()));
                        result.setExitCode(3);
                    } else if (output.startsWith("运行失败：")) {
                        result.setErrorMessage(output.substring("运行失败：".length()));
                        result.setExitCode(4);
                    } else {
                        result.setExitCode(0);
                    }
                } catch (Exception e) {
                    log.error("代码执行异常", e);
                    result.setErrorMessage("执行异常：" + e.getMessage());
                    result.setExitCode(-1);
                }
                
                return result;
            });
            futures.add(future);
        }
        
        for (Future<CodeExecutionResult> future : futures) {
            try {
                results.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                log.error("获取执行结果失败", e);
                CodeExecutionResult errorResult = new CodeExecutionResult();
                errorResult.setErrorMessage("执行异常：" + e.getMessage());
                errorResult.setExitCode(-1);
                results.add(errorResult);
            }
        }
        
        return results;
    }

    @Override
    public List<CodeExecutionResult> runCodeBatch(String code, String language, List<String> inputs, Integer timeLimit, Integer memoryLimit) {
        List<CodeExecutionResult> results = new ArrayList<>();
        List<Future<CodeExecutionResult>> futures = new ArrayList<>();
        
        for (String input : inputs) {
            Future<CodeExecutionResult> future = executorService.submit(() -> {
                CodeExecutionResult result = new CodeExecutionResult();
                long startTime = System.currentTimeMillis();
                
                try {
                    String output = dockerUtil.runCode(code, language, input, timeLimit, memoryLimit);
                    long endTime = System.currentTimeMillis();
                    
                    result.setOutput(output);
                    result.setTimeCost(endTime - startTime);
                    result.setMemoryCost(0);
                    
                    if (output.startsWith("编译错误：")) {
                        result.setErrorMessage(output.substring("编译错误：".length()));
                        result.setExitCode(2);
                    } else if (output.startsWith("运行错误：")) {
                        result.setErrorMessage(output.substring("运行错误：".length()));
                        result.setExitCode(3);
                    } else if (output.startsWith("运行失败：")) {
                        result.setErrorMessage(output.substring("运行失败：".length()));
                        result.setExitCode(4);
                    } else {
                        result.setExitCode(0);
                    }
                } catch (Exception e) {
                    log.error("代码执行异常", e);
                    result.setErrorMessage("执行异常：" + e.getMessage());
                    result.setExitCode(-1);
                }
                
                return result;
            });
            futures.add(future);
        }
        
        for (Future<CodeExecutionResult> future : futures) {
            try {
                results.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                log.error("获取执行结果失败", e);
                CodeExecutionResult errorResult = new CodeExecutionResult();
                errorResult.setErrorMessage("执行异常：" + e.getMessage());
                errorResult.setExitCode(-1);
                results.add(errorResult);
            }
        }
        
        return results;
    }
}
