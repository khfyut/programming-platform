package com.programming.controller;

import com.programming.entity.Problem;
import com.programming.entity.TestCase;
import com.programming.service.ProblemService;
import com.programming.service.TestCaseService;
import com.programming.util.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/problem")
public class ProblemController {

    @Autowired
    private ProblemService problemService;

    @Autowired
    private TestCaseService testCaseService;

    @GetMapping("/list")
    public ResultUtil getProblemList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) String language) {
        try {
            return ResultUtil.success(problemService.getProblemList(page, size, difficulty, language));
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/detail/{id}")
    public ResultUtil<Problem> getProblemDetail(@PathVariable Long id) {
        try {
            Problem problem = problemService.getProblemDetail(id);
            List<TestCase> testCases = testCaseService.getTestCasesByProblemId(id);
            problem.setTestCases(testCases);
            return ResultUtil.success(problem);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/{id}/test-cases/sample")
    public ResultUtil<List<TestCase>> getSampleTestCases(@PathVariable Long id) {
        try {
            List<TestCase> testCases = testCaseService.getSampleTestCasesByProblemId(id);
            return ResultUtil.success(testCases);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/{id}/test-cases/all")
    public ResultUtil<List<TestCase>> getAllTestCases(@PathVariable Long id) {
        try {
            List<TestCase> testCases = testCaseService.getTestCasesByProblemId(id);
            return ResultUtil.success(testCases);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/test-cases")
    public ResultUtil<TestCase> createTestCase(@PathVariable Long id, @RequestBody TestCase testCase) {
        try {
            testCase.setProblemId(id);
            TestCase created = testCaseService.createTestCase(testCase);
            return ResultUtil.success(created);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/test-cases/batch")
    public ResultUtil<List<TestCase>> batchCreateTestCases(@PathVariable Long id, @RequestBody List<TestCase> testCases) {
        try {
            List<TestCase> created = testCaseService.batchCreateTestCases(id, testCases);
            return ResultUtil.success(created);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PutMapping("/test-cases/{testCaseId}")
    public ResultUtil<TestCase> updateTestCase(@PathVariable Long testCaseId, @RequestBody TestCase testCase) {
        try {
            testCase.setId(testCaseId);
            TestCase updated = testCaseService.updateTestCase(testCase);
            return ResultUtil.success(updated);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @DeleteMapping("/test-cases/{testCaseId}")
    public ResultUtil<Boolean> deleteTestCase(@PathVariable Long testCaseId) {
        try {
            boolean deleted = testCaseService.deleteTestCase(testCaseId);
            return ResultUtil.success(deleted);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/tag/{tag}")
    public ResultUtil<List<Problem>> getProblemsByTag(@PathVariable String tag) {
        try {
            List<Problem> problems = problemService.getProblemsByTag(tag);
            return ResultUtil.success(problems);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/difficulty/{difficulty}")
    public ResultUtil<List<Problem>> getProblemsByDifficulty(@PathVariable Integer difficulty) {
        try {
            List<Problem> problems = problemService.getProblemsByDifficulty(difficulty);
            return ResultUtil.success(problems);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }
}
