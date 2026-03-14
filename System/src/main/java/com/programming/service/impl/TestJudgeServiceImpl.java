package com.programming.service.impl;

import com.programming.entity.Problem;
import com.programming.entity.TestCase;
import com.programming.mapper.ProblemMapper;
import com.programming.mapper.TestCaseMapper;
import com.programming.service.CodeSandboxService;
import com.programming.service.TestJudgeService;
import com.programming.vo.SubmitResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TestJudgeServiceImpl implements TestJudgeService {

    @Autowired
    private TestCaseMapper testCaseMapper;

    @Autowired
    private ProblemMapper problemMapper;

    @Autowired
    private CodeSandboxService codeSandboxService;

    @Override
    public TestCase getTestCase(Long problemId) {
        return testCaseMapper.getTestCase(problemId);
    }

    @Override
    public List<TestCase> getTestCases(Long problemId) {
        return testCaseMapper.getTestCases(problemId);
    }

    public SubmitResultVO judgeCode(Long userId, Long problemId, String code, String language) {
        Problem problem = problemMapper.findById(problemId);
        if (problem == null) {
            throw new RuntimeException("题目不存在");
        }

        List<TestCase> testCases = testCaseMapper.getTestCases(problemId);
        if (testCases == null || testCases.isEmpty()) {
            throw new RuntimeException("题目没有配置测试用例");
        }

        long startTime = System.currentTimeMillis();
        int passedCount = 0;
        int totalCount = testCases.size();
        List<String> errors = new ArrayList<>();
        String sampleOutput = null;
        String performanceWarning = null;

        for (int i = 0; i < testCases.size(); i++) {
            TestCase testCase = testCases.get(i);
            String testCaseOutput = codeSandboxService.runCode(code, language, testCase.getInput());
            
            if (testCase.getIsSample()) {
                sampleOutput = testCaseOutput;
            }

            String expectedOutput = testCase.getOutput().trim();
            String actualOutput = testCaseOutput.trim();

            if (expectedOutput.equals(actualOutput)) {
                passedCount++;
                log.info("测试用例{}通过 - 题目ID: {}, 预期: {}, 实际: {}", i + 1, problemId, expectedOutput, actualOutput);
            } else {
                String error = String.format("测试用例%d失败 - 预期: %s, 实际: %s", i + 1, expectedOutput, actualOutput);
                errors.add(error);
                log.warn(error);
            }
        }

        long endTime = System.currentTimeMillis();
        int timeCost = (int) (endTime - startTime);

        SubmitResultVO resultVO = new SubmitResultVO();
        
        if (passedCount == totalCount) {
            resultVO.setResult(0);
            resultVO.setOutput(sampleOutput != null ? sampleOutput : "所有测试用例通过");
            log.info("判题通过 - 题目ID: {}, 用户ID: {}, 耗时: {}ms, 通过率: 100%", problemId, userId, timeCost);
        } else {
            resultVO.setResult(1);
            resultVO.setOutput(String.join("; ", errors));
            log.warn("判题失败 - 题目ID: {}, 用户ID: {}, 通过率: {}/{}", problemId, userId, passedCount, totalCount);
        }

        resultVO.setTimeCost(timeCost);
        resultVO.setMemoryCost(0);
        resultVO.setCompileError(null);
        resultVO.setRuntimeError(null);
        resultVO.setErrorMessage(null);

        return resultVO;
    }

    public Map<String, Object> getJudgeDetail(Long userId, Long problemId, String code, String language) {
        Problem problem = problemMapper.findById(problemId);
        if (problem == null) {
            throw new RuntimeException("题目不存在");
        }

        List<TestCase> testCases = testCaseMapper.getTestCases(problemId);
        if (testCases == null || testCases.isEmpty()) {
            throw new RuntimeException("题目没有配置测试用例");
        }

        long startTime = System.currentTimeMillis();
        int passedCount = 0;
        int totalCount = testCases.size();
        List<Map<String, Object>> testCaseResults = new ArrayList<>();
        String sampleOutput = null;

        for (int i = 0; i < testCases.size(); i++) {
            TestCase testCase = testCases.get(i);
            String testCaseOutput = codeSandboxService.runCode(code, language, testCase.getInput());
            
            if (testCase.getIsSample()) {
                sampleOutput = testCaseOutput;
            }

            String expectedOutput = testCase.getOutput().trim();
            String actualOutput = testCaseOutput.trim();
            boolean passed = expectedOutput.equals(actualOutput);

            if (passed) {
                passedCount++;
            }

            Map<String, Object> testCaseResult = new HashMap<>();
            testCaseResult.put("sortOrder", testCase.getSortOrder());
            testCaseResult.put("input", testCase.getInput());
            testCaseResult.put("expectedOutput", expectedOutput);
            testCaseResult.put("actualOutput", actualOutput);
            testCaseResult.put("passed", passed);
            testCaseResults.add(testCaseResult);
        }

        long endTime = System.currentTimeMillis();
        int timeCost = (int) (endTime - startTime);

        Map<String, Object> result = new HashMap<>();
        result.put("total", totalCount);
        result.put("passed", passedCount);
        result.put("failed", totalCount - passedCount);
        result.put("passRate", String.format("%.2f%%", (double) passedCount / totalCount * 100));
        result.put("timeCost", timeCost);
        result.put("sampleOutput", sampleOutput);
        result.put("testCaseResults", testCaseResults);

        return result;
    }
}