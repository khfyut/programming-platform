package com.programming.mapper;

import com.programming.entity.TestCase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TestCaseMapper {
    TestCase getTestCase(@Param("problemId") Long problemId);
    List<TestCase> getTestCases(@Param("problemId") Long problemId);
    
    List<TestCase> findByProblemId(@Param("problemId") Long problemId);
    List<TestCase> findSampleByProblemId(@Param("problemId") Long problemId);
    TestCase findById(@Param("id") Long id);
    int insert(TestCase testCase);
    int batchInsert(@Param("list") List<TestCase> list);
    int update(TestCase testCase);
    int deleteById(@Param("id") Long id);
    int deleteByProblemId(@Param("problemId") Long problemId);
    int countByProblemId(@Param("problemId") Long problemId);
    int countSampleByProblemId(@Param("problemId") Long problemId);
}