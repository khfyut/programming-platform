package com.programming.service;

import java.util.List;

public interface LearnService {
    java.util.Map<String, Object> getStatistics(Long userId);
    List<com.programming.entity.Problem> getRecommend(Long userId);
}