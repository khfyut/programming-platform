package com.programming.mapper;

import com.programming.entity.ReferenceSolution;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReferenceSolutionMapper {

    /**
     * 根据题目ID和语言获取参考答案
     */
    ReferenceSolution findByProblemIdAndLanguage(@Param("problemId") Long problemId, @Param("language") String language);

    /**
     * 根据题目ID获取所有语言的参考答案
     */
    List<ReferenceSolution> findByProblemId(@Param("problemId") Long problemId);

    /**
     * 插入参考答案
     */
    void insert(ReferenceSolution referenceSolution);

    /**
     * 更新参考答案
     */
    void update(ReferenceSolution referenceSolution);

    /**
     * 删除参考答案
     */
    void delete(@Param("id") Long id);

    /**
     * 获取题目支持的语言列表
     */
    List<String> findLanguagesByProblemId(@Param("problemId") Long problemId);
}