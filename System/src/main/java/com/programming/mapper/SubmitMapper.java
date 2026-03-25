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
    
    // 获取用户已解决的题目ID列表
    List<Long> findPassedProblemIdsByUserId(@Param("userId") Long userId);
    
    // 获取用户尝试过的题目ID列表（但未解决）
    List<Long> findAttemptedProblemIdsByUserId(@Param("userId") Long userId);
    
    // 获取题目的提交统计
    Map<String, Object> countByProblemId(@Param("problemId") Long problemId);
}
