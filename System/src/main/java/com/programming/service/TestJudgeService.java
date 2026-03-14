package com.programming.service;

import com.programming.entity.TestCase;
import java.util.List;

public interface TestJudgeService {
    TestCase getTestCase(Long problemId);
    List<TestCase> getTestCases(Long problemId);
}