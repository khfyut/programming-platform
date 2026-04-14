package com.programming.mapper;

import com.programming.entity.LearningEventLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LearningEventLogMapper {

    int insert(LearningEventLog log);

    List<LearningEventLog> findByUserAndProblem(@Param("userId") Long userId,
                                                 @Param("problemId") Long problemId);

    List<LearningEventLog> findBySubmitId(@Param("submitId") Long submitId);

    LearningEventLog findByRequestIdAndUser(@Param("requestId") String requestId,
                                            @Param("userId") Long userId);

    List<LearningEventLog> findRecentByUser(@Param("userId") Long userId,
                                            @Param("limit") Integer limit);
}
