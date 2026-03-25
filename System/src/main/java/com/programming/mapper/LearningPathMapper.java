package com.programming.mapper;

import com.programming.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface LearningPathMapper {
    // 学习路径相关
    List<LearningPath> selectAllPaths();
    LearningPath selectPathById(Long id);
    List<LearningPath> selectPathsByLanguageAndDirection(@Param("language") String language, @Param("direction") String direction);
    void insertPath(LearningPath path);
    void updatePath(LearningPath path);
    void deletePath(Long id);
    
    // 章节相关
    List<PathChapter> selectChaptersByPathId(Long pathId);
    PathChapter selectChapterById(Long id);
    void insertChapter(PathChapter chapter);
    void updateChapter(PathChapter chapter);
    void deleteChapter(Long id);
    
    // 关卡相关
    List<PathLevel> selectLevelsByChapterId(Long chapterId);
    PathLevel selectLevelById(Long id);
    void insertLevel(PathLevel level);
    void updateLevel(PathLevel level);
    void deleteLevel(Long id);
    
    // 用户进度相关
    UserPathProgress selectUserPathProgress(@Param("userId") Long userId, @Param("pathId") Long pathId);
    List<UserPathProgress> selectUserProgressByUserId(Long userId);
    void insertUserPathProgress(UserPathProgress progress);
    void updateUserPathProgress(UserPathProgress progress);
    
    // 测评相关
    List<AssessmentQuestion> selectAssessmentQuestions(@Param("language") String language, @Param("direction") String direction, @Param("limit") Integer limit);
    AssessmentQuestion selectAssessmentQuestionById(Long id);
    void insertAssessmentQuestion(AssessmentQuestion question);
    void insertUserAssessment(UserAssessment assessment);
    List<UserAssessment> selectUserAssessmentsByUserId(Long userId);
    UserAssessment selectLatestUserAssessment(@Param("userId") Long userId, @Param("language") String language, @Param("direction") String direction);
}