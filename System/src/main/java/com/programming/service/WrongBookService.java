package com.programming.service;

import com.programming.entity.*;
import java.util.List;
import java.util.Map;

public interface WrongBookService {
    // 错题本管理
    WrongBook getUserWrongBook(Long userId);
    List<WrongBookItem> getWrongBookItems(Long userId, String knowledgePoint, Integer difficulty);
    WrongBookItem getWrongBookItemById(Long userId, Long id);
    void addWrongBookItem(Long userId, Long problemId, Long submitId, String code, String language, String errorMessage, String knowledgePoints);
    void updateWrongBookItemStatus(Long userId, Long id, Integer status);
    void removeWrongBookItem(Long userId, Long id);
    
    // 复习计划
    List<ReviewPlan> getReviewPlans(Long userId);
    List<ReviewPlan> getPendingReviewPlans(Long userId);
    void createReviewPlan(Long userId, Long wrongItemId);
    void updateReviewPlan(Long id, boolean reviewed);
    void deleteReviewPlan(Long id);
    
    // 统计分析
    Map<String, Object> getWrongBookStatistics(Long userId);
    List<Map<String, Object>> getWrongDistributionByKnowledgePoint(Long userId);
    List<Map<String, Object>> getWrongDistributionByDifficulty(Long userId);
    
    // 推荐题目
    List<Problem> getRecommendedProblems(Long userId, Long wrongItemId, Integer limit);
    List<Problem> getRecommendedProblemsByKnowledgePoint(String knowledgePoints, Integer difficulty, Integer limit);
    
    // 自动处理
    void autoAddWrongItem(Long userId, Submit submit, Problem problem);
    void autoGenerateReviewPlans(Long userId);
}
