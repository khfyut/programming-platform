package com.programming.mapper;

import com.programming.entity.SubmitTestCaseResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SubmitTestCaseResultMapper {
    int batchInsert(@Param("list") List<SubmitTestCaseResult> list);
    List<SubmitTestCaseResult> findBySubmitId(@Param("submitId") Long submitId);
}