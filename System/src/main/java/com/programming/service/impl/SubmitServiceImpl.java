package com.programming.service.impl;

import com.programming.entity.LearnRecord;
import com.programming.entity.Problem;
import com.programming.entity.Submit;
import com.programming.entity.SubmitTestCaseResult;
import com.programming.entity.TestCase;
import com.programming.handler.JudgeProgressHandler;
import com.programming.mapper.LearnRecordMapper;
import com.programming.mapper.ProblemMapper;
import com.programming.mapper.SubmitMapper;
import com.programming.mapper.SubmitTestCaseResultMapper;
import com.programming.mapper.TestCaseMapper;
import com.programming.service.CodeSandboxService;
import com.programming.service.SubmitService;
import com.programming.service.WrongBookService;
import com.programming.vo.CodeExecutionResult;
import com.programming.vo.SubmitResultVO;
import com.programming.vo.SubmitWithProblemVO;
import com.programming.vo.TestCaseResultVO;
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
    private static final int RESULT_ACCEPTED = 0;
    private static final int RESULT_WRONG_ANSWER = 1;
    private static final int RESULT_COMPILE_ERROR = 2;
    private static final int RESULT_RUNTIME_ERROR = 3;
    private static final int RESULT_EXECUTION_FAILED = 4;

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
        Problem problem = requireProblem(problemId);
        List<TestCase> testCases = requireTestCases(problemId);

        sendJudgeStart(userId, testCases.size());

        List<CodeExecutionResult> executionResults = executeBatch(code, language, problem, testCases);
        JudgingSummary summary = evaluateTestCases(userId, problemId, testCases, executionResults);

        Submit submit = persistSubmit(userId, problemId, code, language, summary);
        updateLearnRecord(userId, problemId, summary.finalResult());

        SubmitResultVO resultVO = buildSubmitResult(summary, testCases);
        autoAddWrongBook(userId, submit, problem, summary.finalResult());
        sendJudgeComplete(userId, resultVO);

        return resultVO;
    }

    private Problem requireProblem(Long problemId) {
        Problem problem = problemMapper.findById(problemId);
        if (problem == null) {
            throw new RuntimeException("题目不存在");
        }
        return problem;
    }

    private List<TestCase> requireTestCases(Long problemId) {
        List<TestCase> testCases = testCaseMapper.findByProblemId(problemId);
        if (testCases == null || testCases.isEmpty()) {
            throw new RuntimeException("题目没有配置测试用例");
        }
        return testCases;
    }

    private void sendJudgeStart(Long userId, int totalTestCases) {
        Map<String, Object> startMessage = Map.of(
                "type", "start",
                "message", "开始判题",
                "totalTestCases", totalTestCases
        );
        judgeProgressHandler.sendProgress(userId, startMessage);
    }

    private List<CodeExecutionResult> executeBatch(String code, String language, Problem problem, List<TestCase> testCases) {
        List<String> inputs = new ArrayList<>();
        for (TestCase testCase : testCases) {
            inputs.add(testCase.getInput());
        }

        return codeSandboxService.runCodeBatch(
                code,
                language,
                inputs,
                problem.getTimeLimit(),
                problem.getMemoryLimit()
        );
    }

    private JudgingSummary evaluateTestCases(Long userId, Long problemId, List<TestCase> testCases,
                                             List<CodeExecutionResult> executionResults) {
        int finalResult = RESULT_ACCEPTED;
        int maxTimeCost = 0;
        int maxMemoryCost = 0;
        int passedCount = 0;
        List<SubmitTestCaseResult> testCaseResults = new ArrayList<>();

        for (int i = 0; i < testCases.size(); i++) {
            TestCase testCase = testCases.get(i);
            CodeExecutionResult execResult = executionResults.get(i);

            sendTestCaseStart(userId, i + 1, testCases.size());
            EvaluatedCase evaluatedCase = evaluateSingleCase(problemId, testCase, execResult);
            sendTestCaseComplete(userId, i + 1, testCases.size(), evaluatedCase.statusText(), evaluatedCase.result(), passedCount + (evaluatedCase.passed() ? 1 : 0));

            if (evaluatedCase.passed()) {
                passedCount++;
            }

            if (evaluatedCase.result().getTimeCost() != null && evaluatedCase.result().getTimeCost() > maxTimeCost) {
                maxTimeCost = evaluatedCase.result().getTimeCost();
            }
            if (evaluatedCase.result().getMemoryCost() != null && evaluatedCase.result().getMemoryCost() > maxMemoryCost) {
                maxMemoryCost = evaluatedCase.result().getMemoryCost();
            }
            if (finalResult == RESULT_ACCEPTED && evaluatedCase.resultCode() != RESULT_ACCEPTED) {
                finalResult = evaluatedCase.resultCode();
            }

            testCaseResults.add(evaluatedCase.result());
        }

        return new JudgingSummary(finalResult, maxTimeCost, maxMemoryCost, passedCount, testCaseResults);
    }

    private EvaluatedCase evaluateSingleCase(Long problemId, TestCase testCase, CodeExecutionResult execResult) {
        String actualOutputText = execResult.getOutput() == null ? "" : execResult.getOutput();

        SubmitTestCaseResult result = new SubmitTestCaseResult();
        result.setTestCaseId(testCase.getId());
        result.setTimeCost((int) execResult.getTimeCost());
        result.setMemoryCost((int) execResult.getMemoryCost());
        result.setActualOutput(actualOutputText);

        if (execResult.getExitCode() == RESULT_COMPILE_ERROR) {
            result.setResult(RESULT_COMPILE_ERROR);
            result.setErrorMessage(execResult.getErrorMessage());
            log.error("Compile error - problemId: {}, testCaseId: {}, error: {}",
                    problemId, testCase.getId(), execResult.getErrorMessage());
            return new EvaluatedCase(result, RESULT_COMPILE_ERROR, "编译错误", false);
        }

        if (execResult.getExitCode() == RESULT_RUNTIME_ERROR) {
            result.setResult(RESULT_RUNTIME_ERROR);
            result.setErrorMessage(execResult.getErrorMessage());
            log.error("Runtime error - problemId: {}, testCaseId: {}, error: {}",
                    problemId, testCase.getId(), execResult.getErrorMessage());
            return new EvaluatedCase(result, RESULT_RUNTIME_ERROR, "运行错误", false);
        }

        if (execResult.getExitCode() == RESULT_EXECUTION_FAILED) {
            result.setResult(RESULT_EXECUTION_FAILED);
            result.setErrorMessage(execResult.getErrorMessage());
            log.error("Execution failed - problemId: {}, testCaseId: {}, error: {}",
                    problemId, testCase.getId(), execResult.getErrorMessage());
            return new EvaluatedCase(result, RESULT_EXECUTION_FAILED, "运行失败", false);
        }

        String expectedOutput = testCase.getOutput() == null ? "" : testCase.getOutput().trim();
        String actualOutput = actualOutputText.trim();
        if (expectedOutput.equals(actualOutput)) {
            result.setResult(RESULT_ACCEPTED);
            log.info("Test case passed - problemId: {}, testCaseId: {}, timeCost: {}ms",
                    problemId, testCase.getId(), execResult.getTimeCost());
            return new EvaluatedCase(result, RESULT_ACCEPTED, "通过", true);
        }

        result.setResult(RESULT_WRONG_ANSWER);
        log.warn("Wrong answer - problemId: {}, testCaseId: {}, expected: {}, actual: {}",
                problemId, testCase.getId(), expectedOutput, actualOutput);
        return new EvaluatedCase(result, RESULT_WRONG_ANSWER, "答案错误", false);
    }

    private void sendTestCaseStart(Long userId, int currentIndex, int totalTestCases) {
        Map<String, Object> testCaseStartMessage = Map.of(
                "type", "testCaseStart",
                "testCaseIndex", currentIndex,
                "totalTestCases", totalTestCases,
                "message", "执行测试用例 " + currentIndex
        );
        judgeProgressHandler.sendProgress(userId, testCaseStartMessage);
    }

    private void sendTestCaseComplete(Long userId, int currentIndex, int totalTestCases, String status,
                                      SubmitTestCaseResult result, int passedCount) {
        Map<String, Object> testCaseCompleteMessage = Map.of(
                "type", "testCaseComplete",
                "testCaseIndex", currentIndex,
                "totalTestCases", totalTestCases,
                "status", status,
                "timeCost", result.getTimeCost(),
                "memoryCost", result.getMemoryCost(),
                "passedCount", passedCount
        );
        judgeProgressHandler.sendProgress(userId, testCaseCompleteMessage);
    }

    private Submit persistSubmit(Long userId, Long problemId, String code, String language, JudgingSummary summary) {
        Submit submit = new Submit();
        submit.setUserId(userId);
        submit.setProblemId(problemId);
        submit.setCode(code);
        submit.setLanguage(language);
        submit.setResult(summary.finalResult());
        submit.setTimeCost(summary.maxTimeCost());
        submit.setMemoryCost(summary.maxMemoryCost());
        submitMapper.insert(submit);

        for (SubmitTestCaseResult result : summary.testCaseResults()) {
            result.setSubmitId(submit.getId());
        }
        submitTestCaseResultMapper.batchInsert(summary.testCaseResults());
        return submit;
    }

    private void updateLearnRecord(Long userId, Long problemId, int finalResult) {
        LearnRecord learnRecord = learnRecordMapper.findByUserId(userId);
        if (learnRecord == null) {
            learnRecord = new LearnRecord();
            learnRecord.setUserId(userId);
            learnRecord.setProblemCount(1);
            learnRecord.setCorrectCount(finalResult == RESULT_ACCEPTED ? 1 : 0);
            learnRecord.setLastProblemId(problemId);
            learnRecordMapper.insert(learnRecord);
            return;
        }

        learnRecord.setProblemCount(learnRecord.getProblemCount() + 1);
        if (finalResult == RESULT_ACCEPTED) {
            learnRecord.setCorrectCount(learnRecord.getCorrectCount() + 1);
        }
        learnRecord.setLastProblemId(problemId);
        learnRecordMapper.update(learnRecord);
    }

    private SubmitResultVO buildSubmitResult(JudgingSummary summary, List<TestCase> testCases) {
        SubmitResultVO resultVO = new SubmitResultVO();
        resultVO.setResult(summary.finalResult());
        resultVO.setTimeCost(summary.maxTimeCost());
        resultVO.setMemoryCost(summary.maxMemoryCost());
        resultVO.setTestCaseResults(buildTestCaseResultVOs(summary.testCaseResults(), testCases));
        resultVO.setPassedCount(summary.passedCount());
        resultVO.setTotalCount(summary.testCaseResults().size());

        if (!summary.testCaseResults().isEmpty()) {
            SubmitTestCaseResult firstResult = summary.testCaseResults().get(0);
            resultVO.setOutput(firstResult.getActualOutput());
            if (firstResult.getResult() == RESULT_COMPILE_ERROR) {
                resultVO.setCompileError(firstResult.getErrorMessage());
            } else if (firstResult.getResult() == RESULT_RUNTIME_ERROR) {
                resultVO.setRuntimeError(firstResult.getErrorMessage());
            } else if (firstResult.getResult() == RESULT_EXECUTION_FAILED) {
                resultVO.setErrorMessage(firstResult.getErrorMessage());
            }
        }

        return resultVO;
    }

    private List<TestCaseResultVO> buildTestCaseResultVOs(List<SubmitTestCaseResult> testCaseResults, List<TestCase> testCases) {
        List<TestCaseResultVO> resultVOs = new ArrayList<>();
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
            vo.setSortOrder(testCase.getSortOrder());
            resultVOs.add(vo);
        }
        return resultVOs;
    }

    private void autoAddWrongBook(Long userId, Submit submit, Problem problem, int finalResult) {
        if (finalResult == RESULT_ACCEPTED) {
            return;
        }

        try {
            wrongBookService.autoAddWrongItem(userId, submit, problem);
        } catch (Exception e) {
            log.error("Failed to add wrong book item", e);
        }
    }

    private void sendJudgeComplete(Long userId, SubmitResultVO resultVO) {
        Map<String, Object> completeMessage = Map.of(
                "type", "complete",
                "result", resultVO,
                "message", "判题完成"
        );
        judgeProgressHandler.sendComplete(userId, completeMessage);
    }

    @Override
    public Map<String, Object> getMySubmits(Long userId, Long problemId, int page, int size) {
        int offset = (page - 1) * size;
        List<Submit> list = submitMapper.findByUserId(userId, problemId, offset, size);
        int total = submitMapper.countByUserId(userId, problemId);

        List<SubmitWithProblemVO> voList = new ArrayList<>();
        for (Submit submit : list) {
            voList.add(convertToVO(submit));
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
    public SubmitWithProblemVO getSubmitDetail(Long userId, Long submitId) {
        Submit submit = requireOwnedSubmit(userId, submitId);
        List<SubmitTestCaseResult> results = submitTestCaseResultMapper.findBySubmitId(submitId);
        submit.setTestCaseResults(results);
        return convertToVO(submit);
    }

    private Submit requireOwnedSubmit(Long userId, Long submitId) {
        Submit submit = submitMapper.findById(submitId);
        if (submit == null || submit.getUserId() == null || !submit.getUserId().equals(userId)) {
            throw new RuntimeException("Submission not found");
        }
        return submit;
    }

    private record EvaluatedCase(SubmitTestCaseResult result, int resultCode, String statusText, boolean passed) {
    }

    private record JudgingSummary(int finalResult, int maxTimeCost, int maxMemoryCost, int passedCount,
                                  List<SubmitTestCaseResult> testCaseResults) {
    }
}
