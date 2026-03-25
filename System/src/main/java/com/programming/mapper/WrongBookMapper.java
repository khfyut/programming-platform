package com.programming.mapper;

import com.programming.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface WrongBookMapper {
    // 错题本相关
    WrongBook selectWrongBookByUserId(Long userId);
    void insertWrongBook(WrongBook wrongBook);
    
    // 错题项相关
    List<WrongBookItem> selectWrongBookItems(@Param("wrongBookId") Long wrongBookId, @Param("knowledgePoint") String knowledgePoint, @Param("difficulty") Integer difficulty);
    WrongBookItem selectWrongBookItemById(Long id);
    WrongBookItem selectWrongBookItemByProblemId(@Param("wrongBookId") Long wrongBookId, @Param("problemId") Long problemId);
    void insertWrongBookItem(WrongBookItem item);
    void updateWrongBookItem(WrongBookItem item);
    void deleteWrongBookItem(Long id);
    
    // 复习计划相关
    List<ReviewPlan> selectReviewPlansByUserId(Long userId);
    List<ReviewPlan> selectPendingReviewPlans(@Param("userId") Long userId, @Param("currentTime") String currentTime);
    ReviewPlan selectReviewPlanByWrongItemId(Long wrongItemId);
    void insertReviewPlan(ReviewPlan plan);
    void updateReviewPlan(ReviewPlan plan);
    void deleteReviewPlan(Long id);
    
    // 统计信息
    Map<String, Object> getWrongBookStatistics(Long userId);
    List<Map<String, Object>> getWrongDistributionByKnowledgePoint(Long userId);
    List<Map<String, Object>> getWrongDistributionByDifficulty(Long userId);
    
    // 推荐题目
    List<Problem> selectRecommendedProblems(@Param("knowledgePoints") String knowledgePoints, @Param("difficulty") Integer difficulty, @Param("limit") Integer limit);
}