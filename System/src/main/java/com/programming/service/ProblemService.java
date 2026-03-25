package com.programming.service;

import com.programming.dto.ProblemListDTO;
import com.programming.entity.Problem;

import java.util.List;
import java.util.Map;

public interface ProblemService {
    Map<String, Object> getProblemList(int page, int size, Integer difficulty, String language, String knowledge, Long userId);
    Problem getProblemDetail(Long id);
    List<Problem> getProblemsByTag(String tag);
    List<Problem> getProblemsByDifficulty(Integer difficulty);
    List<Map<String, Object>> getLanguages();
}
