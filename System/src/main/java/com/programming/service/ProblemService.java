package com.programming.service;

import com.programming.entity.Problem;
import com.programming.entity.ProblemSupportedLanguage;

import java.util.List;
import java.util.Map;

public interface ProblemService {
    Map<String, Object> getProblemList(int page, int size, Integer difficulty, String language, String knowledge, Long categoryId, Long userId);

    Problem getProblemDetail(Long id);

    List<ProblemSupportedLanguage> getSupportedLanguages(Long id);

    boolean isLanguageEnabledForProblem(Long problemId, String language);

    List<Problem> getProblemsByTag(String tag);

    List<Problem> getProblemsByDifficulty(Integer difficulty);

    List<Map<String, Object>> getLanguages();

    List<Map<String, Object>> getCategories();
}
