package com.programming.service;

import com.programming.entity.*;
import java.util.List;
import java.util.Map;

public interface KnowledgeMasteryService {
    // 知识点管理
    List<KnowledgePoint> getAllKnowledgePoints();
    KnowledgePoint getKnowledgePointById(Long id);
    List<KnowledgePoint> getKnowledgePointsByParentId(Long parentId);
    void createKnowledgePoint(KnowledgePoint knowledgePoint);
    void updateKnowledgePoint(KnowledgePoint knowledgePoint);
    void deleteKnowledgePoint(Long id);
    
    // 知识图谱
    List<KnowledgeRelationship> getRelationshipsBySourceId(Long sourceId);
    List<KnowledgeRelationship> getRelationshipsByTargetId(Long targetId);
    void createRelationship(KnowledgeRelationship relationship);
    void deleteRelationship(Long id);
    Map<String, Object> getKnowledgeGraph();
    
    // 知识点掌握度
    UserKnowledgeMastery getUserKnowledgeMastery(Long userId, Long knowledgeId);
    List<UserKnowledgeMastery> getUserKnowledgeMasteries(Long userId);
    List<UserKnowledgeMastery> getUserWeakKnowledgePoints(Long userId, Integer threshold);
    void updateKnowledgeMastery(Long userId, Long knowledgeId, boolean correct);
    
    // 学习报告
    Map<String, Object> getWeeklyReport(Long userId);
    Map<String, Object> getMonthlyReport(Long userId, Integer year, Integer month);
    
    // 统计分析
    List<Map<String, Object>> getKnowledgeMasteryDistribution(Long userId);
    List<Map<String, Object>> getGlobalKnowledgeMasteryDistribution();
    List<Map<String, Object>> getHighFrequencyErrors(Long userId, Integer limit);
    
    // 获取知识点及其绑定的题目数量
    List<Map<String, Object>> getKnowledgePointWithProblemCount();
    
    // 薄弱项分析
    List<Map<String, Object>> getWeaknessAnalysis(Long userId);
    List<Problem> getRecommendedProblemsForWeakness(Long userId, Long knowledgeId, Integer limit);
}