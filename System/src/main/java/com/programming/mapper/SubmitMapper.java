package com.programming.mapper;

import com.programming.entity.Submit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface SubmitMapper {
    int insert(Submit submit);

    List<Submit> findByUserId(@Param("userId") Long userId, @Param("problemId") Long problemId,
                              @Param("page") int page, @Param("size") int size);

    int countByUserId(@Param("userId") Long userId, @Param("problemId") Long problemId);

    List<Submit> findAll(@Param("page") int page, @Param("size") int size);

    int countAll();

    Submit findById(@Param("id") Long id);

    int countPassedByUserId(@Param("userId") Long userId);

    List<Submit> findRecentByUserId(@Param("userId") Long userId, @Param("page") int page, @Param("size") int size);

    List<Long> findPassedProblemIdsByUserId(@Param("userId") Long userId);

    List<Long> findAttemptedProblemIdsByUserId(@Param("userId") Long userId);

    Map<String, Object> countByProblemId(@Param("problemId") Long problemId);

    List<Map<String, Object>> countByProblemIds(@Param("problemIds") List<Long> problemIds);

    /**
     * 查询用户对该题目的最近N次提交（按时间倒序）
     * 用于计算连续失败次数
     *
     * @param userId 用户ID
     * @param problemId 题目ID
     * @param limit 查询条数
     * @return 提交记录列表（按createTime DESC）
     */
    List<Submit> findRecentByUserAndProblem(@Param("userId") Long userId,
                                            @Param("problemId") Long problemId,
                                            @Param("limit") int limit);
}
