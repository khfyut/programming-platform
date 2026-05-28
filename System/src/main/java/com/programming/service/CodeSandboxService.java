package com.programming.service;

import com.programming.vo.CodeExecutionResult;
import java.util.List;

public interface CodeSandboxService {

    String runCode(String code, String language, String input);

    CodeExecutionResult executeCode(String code, String language, String input);

    CodeExecutionResult executeCode(String code, String language, String input, Integer timeLimit, Integer memoryLimit);
    
    List<CodeExecutionResult> runCodeBatch(String code, String language, List<String> inputs);
    
    List<CodeExecutionResult> runCodeBatch(String code, String language, List<String> inputs, Integer timeLimit, Integer memoryLimit, Double cpuLimit);
}
