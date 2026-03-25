package com.programming.mapper;

import com.programming.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface KnowledgeMasteryMapper {
    // 知识点相关
    List<KnowledgePoint> selectAllKnowledgePoints();
    KnowledgePoint selectKnowledgePointById(Long id);
    List<KnowledgePoint> selectKnowledgePointsByParentId(Long parentId);
    void insertKnowledgePoint(KnowledgePoint knowledgePoint);
    void updateKnowledgePoint(KnowledgePoint knowledgePoint);
    void deleteKnowledgePoint(Long id);
    
    // 知识点关系
    List<KnowledgeRelationship> selectRelationshipsBySourceId(Long sourceId);
    List<KnowledgeRelationship> selectRelationshipsByTargetId(Long targetId);
    void insertKnowledgeRelationship(KnowledgeRelationship relationship);
    void deleteKnowledgeRelationship(Long id);
    
    // 用户知识点掌握度
    UserKnowledgeMastery selectUserKnowledgeMastery(@Param("userId") Long userId, @Param("knowledgeId") Long knowledgeId);
    List<UserKnowledgeMastery> selectUserKnowledgeMasteriesByUserId(Long userId);
    List<UserKnowledgeMastery> selectUserWeakKnowledgePoints(@Param("userId") Long userId, @Param("threshold") Integer threshold);
    void insertUserKnowledgeMastery(UserKnowledgeMastery mastery);
    void updateUserKnowledgeMastery(UserKnowledgeMastery mastery);
    
    // 学习报告
    Map<String, Object> selectWeeklyReport(@Param("userId") Long userId, @Param("startDate") String startDate, @Param("endDate") String endDate);
    Map<String, Object> selectMonthlyReport(@Param("userId") Long userId, @Param("year") Integer year, @Param("month") Integer month);
    
    // 统计数据
    List<Map<String, Object>> selectKnowledgeMasteryDistribution(@Param("userId") Long userId);
    List<Map<String, Object>> selectGlobalKnowledgeMasteryDistribution();
    
    // 高频错误分析
    List<Map<String, Object>> selectHighFrequencyErrors(@Param("userId") Long userId, @Param("limit") Integer limit);
    
    // 获取知识点及其绑定的题目数量
    List<Map<String, Object>> selectKnowledgePointWithProblemCount();
}