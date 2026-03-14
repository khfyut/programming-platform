package com.programming.service;

import com.programming.entity.TestCase;

import java.util.List;

public interface TestCaseService {
    List<TestCase> getTestCasesByProblemId(Long problemId);
    
    List<TestCase> getSampleTestCasesByProblemId(Long problemId);
    
    TestCase getTestCaseById(Long id);
    
    TestCase createTestCase(TestCase testCase);
    
    List<TestCase> batchCreateTestCases(Long problemId, List<TestCase> testCases);
    
    TestCase updateTestCase(TestCase testCase);
    
    boolean deleteTestCase(Long id);
    
    boolean deleteTestCasesByProblemId(Long problemId);
    
    int countTestCases(Long problemId);
    
    int countSampleTestCases(Long problemId);
}
