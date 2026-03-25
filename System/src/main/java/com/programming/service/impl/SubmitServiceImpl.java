package com.programming.service.impl;

import com.programming.entity.*;
import com.programming.handler.JudgeProgressHandler;
import com.programming.mapper.*;
import com.programming.service.CodeSandboxService;
import com.programming.service.SubmitService;
import com.programming.service.WrongBookService;
import com.programming.vo.SubmitResultVO;
import com.programming.vo.SubmitWithProblemVO;
import com.programming.vo.TestCaseResultVO;
import com.programming.vo.CodeExecutionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SubmitServiceImpl implements SubmitService {

    @Autowired
    private SubmitMapper submitMapper;

    @Autowired
    private ProblemMapper problemMapper;

    @Autowired
    private TestCaseMapper testCaseMapper;

    @Autowired
    private SubmitTestCaseResultMapper submitTestCaseResultMapper;

    @Autowired
    private CodeSandboxService codeSandboxService;

    @Autowired
    private LearnRecordMapper learnRecordMapper;

    @Autowired
    private WrongBookService wrongBookService;

    @Autowired
    private JudgeProgressHandler judgeProgressHandler;

    @Override
    @Transactional
    public SubmitResultVO commit(Long userId, Long problemId, String code, String language) {
        Problem problem = problemMapper.findById(problemId);
        if (problem == null) {
            throw new RuntimeException("题目不存在");
        }

        List<TestCase> testCases = testCaseMapper.findByProblemId(problemId);
        if (testCases == null || testCases.isEmpty()) {
            throw new RuntimeException("题目没有配置测试用例");
        }

        // 发送开始判题消息
        Map<String, Object> startMessage = Map.of(
            "type", "start",
            "message", "开始判题",
            "totalTestCases", testCases.size()
        );
        judgeProgressHandler.sendProgress(userId, startMessage);

        List<String> inputs = new ArrayList<>();
        for (TestCase testCase : testCases) {
            inputs.add(testCase.getInput());
        }
        
        Integer timeLimit = problem.getTimeLimit();
        Integer memoryLimit = problem.getMemoryLimit();
        
        long startTime = System.currentTimeMillis();
        List<CodeExecutionResult> executionResults = codeSandboxService.runCodeBatch(code, language, inputs, timeLimit, memoryLimit);
        long endTime = System.currentTimeMillis();
        int totalTimeCost = (int) (endTime - startTime);

        int finalResult = 0;
        int maxTimeCost = 0;
        int maxMemoryCost = 0;
        List<SubmitTestCaseResult> testCaseResults = new ArrayList<>();
        int passedCount = 0;

        for (int i = 0; i < testCases.size(); i++) {
            TestCase testCase = testCases.get(i);
            CodeExecutionResult execResult = executionResults.get(i);
            
            // 发送测试用例开始执行消息
            Map<String, Object> testCaseStartMessage = Map.of(
                "type", "testCaseStart",
                "testCaseIndex", i + 1,
                "totalTestCases", testCases.size(),
                "message", "执行测试用例 " + (i + 1)
            );
            judgeProgressHandler.sendProgress(userId, testCaseStartMessage);
            
            SubmitTestCaseResult result = new SubmitTestCaseResult();
            result.setTestCaseId(testCase.getId());
            result.setTimeCost((int) execResult.getTimeCost());
            result.setMemoryCost((int) execResult.getMemoryCost());
            result.setActualOutput(execResult.getOutput());
            
            String testCaseStatus = "";
            if (execResult.getExitCode() == 2) {
                result.setResult(2);
                result.setErrorMessage(execResult.getErrorMessage());
                finalResult = 2;
                testCaseStatus = "编译错误";
                log.error("编译错误 - 题目ID: {}, 测试用例ID: {}, 错误: {}", problemId, testCase.getId(), execResult.getErrorMessage());
            } else if (execResult.getExitCode() == 3) {
                result.setResult(3);
                result.setErrorMessage(execResult.getErrorMessage());
                finalResult = 3;
                testCaseStatus = "运行错误";
                log.error("运行错误 - 题目ID: {}, 测试用例ID: {}, 错误: {}", problemId, testCase.getId(), execResult.getErrorMessage());
            } else if (execResult.getExitCode() == 4) {
                result.setResult(4);
                result.setErrorMessage(execResult.getErrorMessage());
                finalResult = 4;
                testCaseStatus = "运行失败";
                log.error("运行失败 - 题目ID: {}, 测试用例ID: {}, 错误: {}", problemId, testCase.getId(), execResult.getErrorMessage());
            } else {
                String expectedOutput = testCase.getOutput().trim();
                String actualOutput = execResult.getOutput().trim();
                
                if (expectedOutput.equals(actualOutput)) {
                    result.setResult(0);
                    testCaseStatus = "通过";
                    passedCount++;
                    log.info("测试用例通过 - 题目ID: {}, 测试用例ID: {}, 耗时: {}ms", problemId, testCase.getId(), execResult.getTimeCost());
                } else {
                    result.setResult(1);
                    finalResult = 1;
                    testCaseStatus = "答案错误";
                    log.warn("答案错误 - 题目ID: {}, 测试用例ID: {}, 预期: {}, 实际: {}", 
                            problemId, testCase.getId(), expectedOutput, actualOutput);
                }
            }
            
            // 发送测试用例执行完成消息
            Map<String, Object> testCaseCompleteMessage = Map.of(
                "type", "testCaseComplete",
                "testCaseIndex", i + 1,
                "totalTestCases", testCases.size(),
                "status", testCaseStatus,
                "timeCost", result.getTimeCost(),
                "memoryCost", result.getMemoryCost(),
                "passedCount", passedCount
            );
            judgeProgressHandler.sendProgress(userId, testCaseCompleteMessage);
            
            if (result.getTimeCost() > maxTimeCost) {
                maxTimeCost = result.getTimeCost();
            }
            if (result.getMemoryCost() > maxMemoryCost) {
                maxMemoryCost = result.getMemoryCost();
            }
            
            testCaseResults.add(result);
        }

        Submit submit = new Submit();
        submit.setUserId(userId);
        submit.setProblemId(problemId);
        submit.setCode(code);
        submit.setLanguage(language);
        submit.setResult(finalResult);
        submit.setTimeCost(maxTimeCost);
        submit.setMemoryCost(maxMemoryCost);
        submitMapper.insert(submit);

        for (SubmitTestCaseResult result : testCaseResults) {
            result.setSubmitId(submit.getId());
        }
        submitTestCaseResultMapper.batchInsert(testCaseResults);

        LearnRecord learnRecord = learnRecordMapper.findByUserId(userId);
        if (learnRecord == null) {
            learnRecord = new LearnRecord();
            learnRecord.setUserId(userId);
            learnRecord.setProblemCount(1);
            learnRecord.setCorrectCount(finalResult == 0 ? 1 : 0);
            learnRecord.setLastProblemId(problemId);
            learnRecordMapper.insert(learnRecord);
        } else {
            learnRecord.setProblemCount(learnRecord.getProblemCount() + 1);
            if (finalResult == 0) {
                learnRecord.setCorrectCount(learnRecord.getCorrectCount() + 1);
            }
            learnRecord.setLastProblemId(problemId);
            learnRecordMapper.update(learnRecord);
        }

        SubmitResultVO resultVO = new SubmitResultVO();
        resultVO.setResult(finalResult);
        resultVO.setTimeCost(maxTimeCost);
        resultVO.setMemoryCost(maxMemoryCost);
        
        if (!testCaseResults.isEmpty()) {
            SubmitTestCaseResult firstResult = testCaseResults.get(0);
            resultVO.setOutput(firstResult.getActualOutput());
            
            if (firstResult.getResult() == 2) {
                resultVO.setCompileError(firstResult.getErrorMessage());
            } else if (firstResult.getResult() == 3) {
                resultVO.setRuntimeError(firstResult.getErrorMessage());
            } else if (firstResult.getResult() == 4) {
                resultVO.setErrorMessage(firstResult.getErrorMessage());
            }
        }

        List<TestCaseResultVO> testCaseResultVOs = new ArrayList<>();
        int testCasePassedCount = 0;
        for (int i = 0; i < testCaseResults.size(); i++) {
            SubmitTestCaseResult result = testCaseResults.get(i);
            TestCase testCase = testCases.get(i);
            TestCaseResultVO vo = new TestCaseResultVO();
            vo.setTestCaseId(result.getTestCaseId());
            vo.setResult(result.getResult());
            vo.setTimeCost(result.getTimeCost());
            vo.setMemoryCost(result.getMemoryCost());
            vo.setActualOutput(result.getActualOutput());
            vo.setErrorMessage(result.getErrorMessage());
            vo.setInput(testCase.getInput());
            vo.setExpectedOutput(testCase.getOutput());
            testCaseResultVOs.add(vo);
            
            if (result.getResult() == 0) {
                testCasePassedCount++;
            }
        }
        resultVO.setTestCaseResults(testCaseResultVOs);
        resultVO.setPassedCount(testCasePassedCount);
        resultVO.setTotalCount(testCaseResults.size());

        // 自动添加错题到错题本
        if (finalResult != 0) {
            try {
                wrongBookService.autoAddWrongItem(userId, submit, problem);
            } catch (Exception e) {
                log.error("添加错题到错题本失败", e);
            }
        }

        // 发送判题完成消息
        Map<String, Object> completeMessage = Map.of(
            "type", "complete",
            "result", resultVO,
            "message", "判题完成"
        );
        judgeProgressHandler.sendComplete(userId, completeMessage);

        return resultVO;
    }

    @Override
    public Map<String, Object> getMySubmits(Long userId, Long problemId, int page, int size) {
        int offset = (page - 1) * size;
        List<Submit> list = submitMapper.findByUserId(userId, problemId, offset, size);
        int total = submitMapper.countByUserId(userId, problemId);

        List<SubmitWithProblemVO> voList = new ArrayList<>();
        for (Submit submit : list) {
            SubmitWithProblemVO vo = convertToVO(submit);
            voList.add(vo);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", voList);

        return result;
    }

    private SubmitWithProblemVO convertToVO(Submit submit) {
        SubmitWithProblemVO vo = new SubmitWithProblemVO();
        vo.setId(submit.getId());
        vo.setUserId(submit.getUserId());
        vo.setProblemId(submit.getProblemId());
        vo.setCode(submit.getCode());
        vo.setLanguage(submit.getLanguage());
        vo.setResult(submit.getResult());
        vo.setTimeCost(submit.getTimeCost());
        vo.setMemoryCost(submit.getMemoryCost());
        vo.setCreateTime(submit.getCreateTime());
        vo.setSubmitTime(submit.getCreateTime());
        vo.setTestCaseResults(submit.getTestCaseResults());

        if (submit.getProblemId() != null) {
            Problem problem = problemMapper.findById(submit.getProblemId());
            if (problem != null) {
                vo.setProblemTitle(problem.getTitle());
                
                if (problem.getDifficulty() != null) {
                    if (problem.getDifficulty() == 0) {
                        vo.setDifficulty("EASY");
                    } else if (problem.getDifficulty() == 1) {
                        vo.setDifficulty("MEDIUM");
                    } else if (problem.getDifficulty() == 2) {
                        vo.setDifficulty("HARD");
                    }
                }
                
                if (problem.getTags() != null && !problem.getTags().isEmpty()) {
                    String[] tagArray = problem.getTags().split(",");
                    vo.setTags(java.util.Arrays.asList(tagArray));
                }
            }
        }

        return vo;
    }

    @Override
    public SubmitWithProblemVO getSubmitDetail(Long submitId) {
        Submit submit = submitMapper.findById(submitId);
        if (submit != null) {
            List<SubmitTestCaseResult> results = submitTestCaseResultMapper.findBySubmitId(submitId);
            submit.setTestCaseResults(results);
        }
        return submit != null ? convertToVO(submit) : null;
    }
}
