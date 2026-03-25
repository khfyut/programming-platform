package com.programming.service;

import com.programming.entity.*;
import java.util.List;
import java.util.Map;

public interface LearnService {
    Map<String, Object> getStatistics(Long userId);
    Map<String, Object> getRecommend(Long userId);
    
    // 个性化自适应学习路径
    List<AssessmentQuestion> getAssessmentQuestions();
    Map<String, Object> submitAssessment(Map<String, Object> params);
    LearningPath getLearningPath(Long userId);
    Map<String, Object> unlockLevel(Long userId, Long levelId);
    Map<String, Object> getPathProgress(Long userId);
    
    // 知识点掌握度与学情分析
    Map<String, Object> getKnowledgeGraph();
    List<UserKnowledgeMastery> getKnowledgeMastery(Long userId);
    Map<String, Object> getWeeklyReport(Long userId);
    List<KnowledgePoint> getWeakKnowledgePoints(Long userId);
    Map<String, Integer> getDifficultyStats(Long userId);
}