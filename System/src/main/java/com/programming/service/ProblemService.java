package com.programming.service;

import com.programming.entity.Problem;

import java.util.List;
import java.util.Map;

public interface ProblemService {
    Map<String, Object> getProblemList(int page, int size, Integer difficulty, String language);
    Problem getProblemDetail(Long id);
    List<Problem> getProblemsByTag(String tag);
    List<Problem> getProblemsByDifficulty(Integer difficulty);
}
