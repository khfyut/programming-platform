
package com.programming.service;

import com.programming.entity.*;
import java.util.List;
import java.util.Map;

public interface LearningService {
    List<AssessmentQuestion> getAssessmentQuestions(String language, String direction, Integer limit);
    Map<String, Object> submitAssessment(Long userId, String language, String direction, Map<Long, String> answers);
    
    List<LearningPath> getAvailablePaths(String language, String direction);
    LearningPath getPathDetail(Long pathId);
    UserPathProgress getUserPathProgress(Long userId, Long pathId);
    Map<String, Object> getUserPathProgressWithPercentage(Long userId, Long pathId);
    void updateUserPathProgress(UserPathProgress progress);
    
    PathLevel getLevelDetail(Long levelId);
    boolean unlockLevel(Long userId, Long levelId);
    boolean completeLevel(Long userId, Long levelId);
    
    List<UserPathProgress> getUserProgressList(Long userId);
    Map<String, Object> getLearningStatistics(Long userId);
    
    void createPath(LearningPath path);
    void updatePath(LearningPath path);
    void deletePath(Long pathId);
    void createChapter(PathChapter chapter);
    void updateChapter(PathChapter chapter);
    void deleteChapter(Long chapterId);
    void createLevel(PathLevel level);
    void updateLevel(PathLevel level);
    void deleteLevel(Long levelId);
    void addAssessmentQuestion(AssessmentQuestion question);
    
    List<com.programming.entity.Problem> getProblemsByLevelId(Long levelId);
    void bindProblemToLevel(Long levelId, Long problemId, Integer orderNum);
    void unbindProblemFromLevel(Long levelId, Long problemId);
    void batchBindProblemsToLevel(Long levelId, List<Long> problemIds);
    void updateLevelProblems(Long levelId, List<Long> problemIds);
    
    List<com.programming.entity.LearningResource> getResourcesByLevelId(Long levelId);
}
