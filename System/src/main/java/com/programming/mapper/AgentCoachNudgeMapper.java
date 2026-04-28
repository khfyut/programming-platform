package com.programming.mapper;

import com.programming.entity.AgentCoachNudge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AgentCoachNudgeMapper {
    int insert(AgentCoachNudge nudge);

    AgentCoachNudge findByIdAndUser(@Param("id") Long id,
                                    @Param("userId") Long userId);

    List<AgentCoachNudge> findActiveByUser(@Param("userId") Long userId,
                                           @Param("now") LocalDateTime now,
                                           @Param("limit") Integer limit);

    List<AgentCoachNudge> findRecentSimilar(@Param("userId") Long userId,
                                            @Param("triggerSource") String triggerSource,
                                            @Param("actionType") String actionType,
                                            @Param("after") LocalDateTime after);

    int updateStatus(@Param("id") Long id,
                     @Param("userId") Long userId,
                     @Param("status") String status,
                     @Param("updatedAt") LocalDateTime updatedAt,
                     @Param("expiresAt") LocalDateTime expiresAt);
}
