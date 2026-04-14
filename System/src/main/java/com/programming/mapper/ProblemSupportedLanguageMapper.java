package com.programming.mapper;

import com.programming.entity.ProblemSupportedLanguage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ProblemSupportedLanguageMapper {
    List<ProblemSupportedLanguage> findByProblemId(@Param("problemId") Long problemId);

    ProblemSupportedLanguage findByProblemIdAndLanguage(@Param("problemId") Long problemId,
                                                        @Param("languageCode") String languageCode);

    List<String> findActiveLanguageCodesByProblemId(@Param("problemId") Long problemId);

    int countByProblemId(@Param("problemId") Long problemId);

    int deleteByProblemId(@Param("problemId") Long problemId);

    int batchInsert(@Param("items") List<ProblemSupportedLanguage> items);

    List<Map<String, Object>> getLanguageStats();
}
