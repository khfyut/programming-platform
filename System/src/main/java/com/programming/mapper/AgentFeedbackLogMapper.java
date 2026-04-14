package com.programming.mapper;

import com.programming.entity.AgentFeedbackLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AgentFeedbackLogMapper {
    int insert(AgentFeedbackLog log);

    List<AgentFeedbackLog> findByRequestId(@Param("requestId") String requestId);

    List<AgentFeedbackLog> findRecentByUser(@Param("userId") Long userId,
                                            @Param("limit") Integer limit);
}
