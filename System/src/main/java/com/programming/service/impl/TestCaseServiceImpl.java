package com.programming.service.impl;

import com.programming.entity.TestCase;
import com.programming.mapper.TestCaseMapper;
import com.programming.service.TestCaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class TestCaseServiceImpl implements TestCaseService {

    @Autowired
    private TestCaseMapper testCaseMapper;

    @Override
    public List<TestCase> getTestCasesByProblemId(Long problemId) {
        return testCaseMapper.findByProblemId(problemId);
    }

    @Override
    public List<TestCase> getSampleTestCasesByProblemId(Long problemId) {
        return testCaseMapper.findSampleByProblemId(problemId);
    }

    @Override
    public TestCase getTestCaseById(Long id) {
        return testCaseMapper.findById(id);
    }

    @Override
    @Transactional
    public TestCase createTestCase(TestCase testCase) {
        testCaseMapper.insert(testCase);
        log.info("创建测试用例成功 - ID: {}, 题目ID: {}", testCase.getId(), testCase.getProblemId());
        return testCase;
    }

    @Override
    @Transactional
    public List<TestCase> batchCreateTestCases(Long problemId, List<TestCase> testCases) {
        for (TestCase testCase : testCases) {
            testCase.setProblemId(problemId);
        }
        testCaseMapper.batchInsert(testCases);
        log.info("批量创建测试用例成功 - 题目ID: {}, 数量: {}", problemId, testCases.size());
        return testCases;
    }

    @Override
    @Transactional
    public TestCase updateTestCase(TestCase testCase) {
        testCaseMapper.update(testCase);
        log.info("更新测试用例成功 - ID: {}", testCase.getId());
        return testCase;
    }

    @Override
    @Transactional
    public boolean deleteTestCase(Long id) {
        int result = testCaseMapper.deleteById(id);
        log.info("删除测试用例成功 - ID: {}, 结果: {}", id, result > 0);
        return result > 0;
    }

    @Override
    @Transactional
    public boolean deleteTestCasesByProblemId(Long problemId) {
        int result = testCaseMapper.deleteByProblemId(problemId);
        log.info("删除题目测试用例成功 - 题目ID: {}, 删除数量: {}", problemId, result);
        return result >= 0;
    }

    @Override
    public int countTestCases(Long problemId) {
        return testCaseMapper.countByProblemId(problemId);
    }

    @Override
    public int countSampleTestCases(Long problemId) {
        return testCaseMapper.countSampleByProblemId(problemId);
    }
}
