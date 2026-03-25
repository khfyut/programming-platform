
package com.programming.service.impl;

import com.programming.entity.*;
import com.programming.mapper.LearningPathMapper;
import com.programming.mapper.PathLevelProblemMapper;
import com.programming.mapper.LearningResourceMapper;
import com.programming.service.LearningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class LearningServiceImpl implements LearningService {

    @Autowired
    private LearningPathMapper learningPathMapper;
    
    @Autowired
    private PathLevelProblemMapper pathLevelProblemMapper;
    
    @Autowired
    private LearningResourceMapper learningResourceMapper;

    @Override
    public List<AssessmentQuestion> getAssessmentQuestions(String language, String direction, Integer limit) {
        return learningPathMapper.selectAssessmentQuestions(language, direction, limit);
    }

    @Override
    public Map<String, Object> submitAssessment(Long userId, String language, String direction, Map<Long, String> answers) {
        int score = 0;
        StringBuilder result = new StringBuilder();
        
        for (Map.Entry<Long, String> entry : answers.entrySet()) {
            Long questionId = entry.getKey();
            String userAnswer = entry.getValue();
            
            AssessmentQuestion question = learningPathMapper.selectAssessmentQuestionById(questionId);
            if (question != null && question.getCorrectAnswer().equals(userAnswer)) {
                score += 10;
                result.append(questionId).append(":正确;");
            } else {
                result.append(questionId).append(":错误;");
            }
        }
        
        String abilityLevel = getAbilityLevel(score, answers.size());
        
        UserAssessment assessment = new UserAssessment();
        assessment.setUserId(userId);
        assessment.setLanguage(language);
        assessment.setDirection(direction);
        assessment.setScore(score);
        assessment.setResult(result.toString());
        assessment.setAbilityLevel(abilityLevel);
        
        learningPathMapper.insertUserAssessment(assessment);
        
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("score", score);
        resultData.put("level", abilityLevel);
        resultData.put("levelDescription", getLevelDescription(abilityLevel));
        resultData.put("analysis", getAbilityAnalysis(score, language, direction));
        resultData.put("suggestions", getLearningSuggestions(abilityLevel, language, direction));
        resultData.put("recommendedPath", getRecommendedPath(abilityLevel, language, direction));
        
        return resultData;
    }
    
    private String getLevelDescription(String abilityLevel) {
        Map<String, String> descriptions = new HashMap<>();
        descriptions.put("零基础", "您对编程基础概念了解有限，建议从基础开始学习。");
        descriptions.put("入门", "您已经掌握了基本的编程概念，需要进一步巩固基础。");
        descriptions.put("进阶", "您已经具备一定的编程能力，可以挑战更复杂的问题。");
        descriptions.put("精通", "您的编程能力已经达到较高水平，可以尝试高级编程技术。");
        return descriptions.getOrDefault(abilityLevel, "继续努力！");
    }
    
    private List<Map<String, Object>> getAbilityAnalysis(int score, String language, String direction) {
        List<Map<String, Object>> analysis = new ArrayList<>();
        
        Map<String, Object> algorithm = new HashMap<>();
        algorithm.put("name", "算法基础");
        algorithm.put("score", Math.min(100, score + 10));
        analysis.add(algorithm);
        
        Map<String, Object> dataStructure = new HashMap<>();
        dataStructure.put("name", "数据结构");
        dataStructure.put("score", Math.min(100, score));
        analysis.add(dataStructure);
        
        Map<String, Object> problemSolving = new HashMap<>();
        problemSolving.put("name", "问题解决");
        problemSolving.put("score", Math.min(100, score + 5));
        analysis.add(problemSolving);
        
        return analysis;
    }
    
    private List<String> getLearningSuggestions(String abilityLevel, String language, String direction) {
        List<String> suggestions = new ArrayList<>();
        
        if (abilityLevel.equals("零基础")) {
            suggestions.add("从基础语法开始学习，掌握基本的编程概念");
            suggestions.add("多做简单的编程练习，培养编程思维");
            suggestions.add("学习基本的数据结构和算法");
        } else if (abilityLevel.equals("入门")) {
            suggestions.add("巩固基础，加强算法练习");
            suggestions.add("学习更复杂的数据结构");
            suggestions.add("尝试解决中等难度的编程问题");
        } else if (abilityLevel.equals("进阶")) {
            suggestions.add("学习高级算法和设计模式");
            suggestions.add("参与开源项目或实际项目开发");
            suggestions.add("深入学习系统设计和架构");
        } else if (abilityLevel.equals("精通")) {
            suggestions.add("研究前沿技术和算法");
            suggestions.add("分享知识，参与技术社区");
            suggestions.add("挑战高难度的编程问题");
        }
        
        return suggestions;
    }
    
    private Map<String, Object> getRecommendedPath(String abilityLevel, String language, String direction) {
        Map<String, Object> path = new HashMap<>();
        
        if (abilityLevel.equals("零基础")) {
            path.put("id", 1);
            path.put("name", "编程基础入门");
            path.put("description", "适合初学者的编程基础学习路径，从语法开始，逐步深入");
        } else if (abilityLevel.equals("入门")) {
            path.put("id", 2);
            path.put("name", "算法与数据结构进阶");
            path.put("description", "深入学习算法和数据结构，提升编程能力");
        } else if (abilityLevel.equals("进阶")) {
            path.put("id", 3);
            path.put("name", "高级编程技术");
            path.put("description", "学习高级编程技术和设计模式，准备进入专业开发");
        } else if (abilityLevel.equals("精通")) {
            path.put("id", 4);
            path.put("name", "专家级技术研究");
            path.put("description", "研究前沿技术，参与开源项目，成为技术专家");
        }
        
        return path;
    }

    @Override
    public List<LearningPath> getAvailablePaths(String language, String direction) {
        return learningPathMapper.selectPathsByLanguageAndDirection(language, direction);
    }

    @Override
    public LearningPath getPathDetail(Long pathId) {
        LearningPath path = learningPathMapper.selectPathById(pathId);
        if (path != null) {
            List<PathChapter> chapters = learningPathMapper.selectChaptersByPathId(pathId);
            for (PathChapter chapter : chapters) {
                List<PathLevel> levels = learningPathMapper.selectLevelsByChapterId(chapter.getId());
                chapter.setLevels(levels);
            }
            path.setChapters(chapters);
        }
        return path;
    }

    @Override
    public UserPathProgress getUserPathProgress(Long userId, Long pathId) {
        UserPathProgress progress = learningPathMapper.selectUserPathProgress(userId, pathId);
        if (progress == null) {
            progress = new UserPathProgress();
            progress.setUserId(userId);
            progress.setPathId(pathId);
            
            List<PathChapter> chapters = learningPathMapper.selectChaptersByPathId(pathId);
            if (!chapters.isEmpty()) {
                PathChapter firstChapter = chapters.get(0);
                progress.setCurrentChapterId(firstChapter.getId());
                
                List<PathLevel> levels = learningPathMapper.selectLevelsByChapterId(firstChapter.getId());
                if (!levels.isEmpty()) {
                    PathLevel firstLevel = levels.get(0);
                    progress.setCurrentLevelId(firstLevel.getId());
                }
            }
            
            progress.setCompletedChapters("");
            progress.setCompletedLevels("");
            learningPathMapper.insertUserPathProgress(progress);
        }
        return progress;
    }
    
    public Map<String, Object> getUserPathProgressWithPercentage(Long userId, Long pathId) {
        Map<String, Object> result = new HashMap<>();
        UserPathProgress progress = getUserPathProgress(userId, pathId);
        
        int totalLevels = 0;
        int completedLevelsCount = 0;
        List<Map<String, Object>> chapterProgress = new ArrayList<>();
        List<Map<String, Object>> weakPoints = new ArrayList<>();
        
        List<PathChapter> chapters = learningPathMapper.selectChaptersByPathId(pathId);
        for (PathChapter chapter : chapters) {
            List<PathLevel> levels = learningPathMapper.selectLevelsByChapterId(chapter.getId());
            totalLevels += levels.size();
            
            // 分析章节进度
            int chapterCompletedLevels = 0;
            if (progress.getCompletedLevels() != null && !progress.getCompletedLevels().isEmpty()) {
                for (PathLevel level : levels) {
                    if (progress.getCompletedLevels().contains(String.valueOf(level.getId()))) {
                        chapterCompletedLevels++;
                    }
                }
            }
            
            Map<String, Object> chapterInfo = new HashMap<>();
            chapterInfo.put("chapterId", chapter.getId());
            chapterInfo.put("chapterName", chapter.getName());
            chapterInfo.put("totalLevels", levels.size());
            chapterInfo.put("completedLevels", chapterCompletedLevels);
            chapterInfo.put("progress", levels.size() > 0 ? (int) ((double) chapterCompletedLevels / levels.size() * 100) : 0);
            chapterProgress.add(chapterInfo);
        }
        
        if (progress.getCompletedLevels() != null && !progress.getCompletedLevels().isEmpty()) {
            String[] completedLevelIds = progress.getCompletedLevels().split(",");
            completedLevelsCount = completedLevelIds.length;
        }
        
        int progressPercentage = totalLevels > 0 ? (int) ((double) completedLevelsCount / totalLevels * 100) : 0;
        
        // 分析学习效率（基于完成时间）
        Map<String, Object> efficiency = new HashMap<>();
        efficiency.put("averageCompletionTime", "30分钟/关卡"); // 示例数据，实际需要从数据库获取
        efficiency.put("learningSpeed", "中等");
        efficiency.put("suggestedPace", "每天1-2个关卡");
        
        // 分析薄弱环节
        // 这里可以根据用户的答题情况和完成时间来分析薄弱环节
        // 暂时添加示例数据
        weakPoints.add(Map.of("knowledgePoint", "算法基础", "masteryLevel", 2, "suggestion", "加强算法练习"));
        weakPoints.add(Map.of("knowledgePoint", "数据结构", "masteryLevel", 1, "suggestion", "学习基本数据结构"));
        
        // 预测完成时间
        int remainingLevels = totalLevels - completedLevelsCount;
        int estimatedDays = remainingLevels > 0 ? (int) Math.ceil(remainingLevels / 1.5) : 0; // 假设每天完成1.5个关卡
        
        result.put("progress", progressPercentage);
        result.put("completedLevels", completedLevelsCount);
        result.put("totalLevels", totalLevels);
        result.put("chapterProgress", chapterProgress);
        result.put("efficiency", efficiency);
        result.put("weakPoints", weakPoints);
        result.put("estimatedCompletionDays", estimatedDays);
        result.put("remainingLevels", remainingLevels);
        result.put("lastActivity", progress.getUpdateTime());
        
        return result;
    }

    @Override
    public void updateUserPathProgress(UserPathProgress progress) {
        learningPathMapper.updateUserPathProgress(progress);
    }

    @Override
    public PathLevel getLevelDetail(Long levelId) {
        return learningPathMapper.selectLevelById(levelId);
    }

    @Override
    public boolean unlockLevel(Long userId, Long levelId) {
        PathLevel level = learningPathMapper.selectLevelById(levelId);
        if (level == null) {
            return false;
        }
        
        // 1. 检查前置关卡是否完成
        Long chapterId = level.getChapterId();
        PathChapter chapter = learningPathMapper.selectChapterById(chapterId);
        if (chapter == null) {
            return false;
        }
        
        List<PathLevel> chapterLevels = learningPathMapper.selectLevelsByChapterId(chapterId);
        int currentLevelIndex = -1;
        for (int i = 0; i < chapterLevels.size(); i++) {
            if (chapterLevels.get(i).getId().equals(levelId)) {
                currentLevelIndex = i;
                break;
            }
        }
        
        // 检查前置关卡是否完成
        if (currentLevelIndex > 0) {
            UserPathProgress progress = learningPathMapper.selectUserPathProgress(userId, chapter.getPathId());
            if (progress != null) {
                String completedLevels = progress.getCompletedLevels();
                PathLevel previousLevel = chapterLevels.get(currentLevelIndex - 1);
                if (!completedLevels.contains(String.valueOf(previousLevel.getId()))) {
                    return false;
                }
            }
        }
        
        // 2. 检查知识点掌握度
        if (level.getKnowledgePoints() != null && !level.getKnowledgePoints().isEmpty()) {
            String[] knowledgePointIds = level.getKnowledgePoints().split(",");
            for (String kpId : knowledgePointIds) {
                try {
                    Long knowledgePointId = Long.parseLong(kpId.trim());
                    // 这里可以调用KnowledgeMasteryService检查知识点掌握度
                    // 暂时简化处理，后续可以增强
                } catch (NumberFormatException e) {
                    // 忽略无效的知识点ID
                }
            }
        }
        
        // 3. 检查解锁条件
        if (level.getUnlockCondition() != null && !level.getUnlockCondition().isEmpty()) {
            // 这里可以实现更复杂的解锁条件逻辑
            // 例如：需要完成特定任务、达到特定分数等
        }
        
        return true;
    }

    @Override
    public boolean completeLevel(Long userId, Long levelId) {
        PathLevel level = learningPathMapper.selectLevelById(levelId);
        if (level == null) {
            return false;
        }
        
        Long chapterId = level.getChapterId();
        PathChapter chapter = learningPathMapper.selectChapterById(chapterId);
        if (chapter == null) {
            return false;
        }
        
        Long pathId = chapter.getPathId();
        UserPathProgress progress = learningPathMapper.selectUserPathProgress(userId, pathId);
        if (progress == null) {
            return false;
        }
        
        String completedLevels = progress.getCompletedLevels();
        if (!completedLevels.contains(String.valueOf(levelId))) {
            if (completedLevels.isEmpty()) {
                completedLevels = String.valueOf(levelId);
            } else {
                completedLevels += "," + levelId;
            }
            progress.setCompletedLevels(completedLevels);
        }
        
        List<PathLevel> chapterLevels = learningPathMapper.selectLevelsByChapterId(chapterId);
        boolean chapterCompleted = true;
        for (PathLevel l : chapterLevels) {
            if (!completedLevels.contains(String.valueOf(l.getId()))) {
                chapterCompleted = false;
                break;
            }
        }
        
        if (chapterCompleted) {
            String completedChapters = progress.getCompletedChapters();
            if (!completedChapters.contains(String.valueOf(chapterId))) {
                if (completedChapters.isEmpty()) {
                    completedChapters = String.valueOf(chapterId);
                } else {
                    completedChapters += "," + chapterId;
                }
                progress.setCompletedChapters(completedChapters);
            }
            
            List<PathChapter> chapters = learningPathMapper.selectChaptersByPathId(pathId);
            int currentChapterIndex = -1;
            for (int i = 0; i < chapters.size(); i++) {
                if (chapters.get(i).getId().equals(chapterId)) {
                    currentChapterIndex = i;
                    break;
                }
            }
            
            if (currentChapterIndex < chapters.size() - 1) {
                PathChapter nextChapter = chapters.get(currentChapterIndex + 1);
                progress.setCurrentChapterId(nextChapter.getId());
                
                List<PathLevel> nextLevels = learningPathMapper.selectLevelsByChapterId(nextChapter.getId());
                if (!nextLevels.isEmpty()) {
                    progress.setCurrentLevelId(nextLevels.get(0).getId());
                }
            }
        } else {
            List<PathLevel> levels = learningPathMapper.selectLevelsByChapterId(chapterId);
            int currentLevelIndex = -1;
            for (int i = 0; i < levels.size(); i++) {
                if (levels.get(i).getId().equals(levelId)) {
                    currentLevelIndex = i;
                    break;
                }
            }
            
            if (currentLevelIndex < levels.size() - 1) {
                progress.setCurrentLevelId(levels.get(currentLevelIndex + 1).getId());
            }
        }
        
        learningPathMapper.updateUserPathProgress(progress);
        return true;
    }

    @Override
    public List<UserPathProgress> getUserProgressList(Long userId) {
        return learningPathMapper.selectUserProgressByUserId(userId);
    }

    @Override
    public Map<String, Object> getLearningStatistics(Long userId) {
        Map<String, Object> statistics = new HashMap<>();
        return statistics;
    }

    @Override
    public void createPath(LearningPath path) {
        learningPathMapper.insertPath(path);
    }

    @Override
    public void updatePath(LearningPath path) {
        learningPathMapper.updatePath(path);
    }

    @Override
    public void deletePath(Long pathId) {
        learningPathMapper.deletePath(pathId);
    }

    @Override
    public void createChapter(PathChapter chapter) {
        learningPathMapper.insertChapter(chapter);
    }

    @Override
    public void updateChapter(PathChapter chapter) {
        learningPathMapper.updateChapter(chapter);
    }

    @Override
    public void deleteChapter(Long chapterId) {
        learningPathMapper.deleteChapter(chapterId);
    }

    @Override
    public void createLevel(PathLevel level) {
        learningPathMapper.insertLevel(level);
    }

    @Override
    public void updateLevel(PathLevel level) {
        learningPathMapper.updateLevel(level);
    }

    @Override
    public void deleteLevel(Long levelId) {
        learningPathMapper.deleteLevel(levelId);
    }

    @Override
    public void addAssessmentQuestion(AssessmentQuestion question) {
        learningPathMapper.insertAssessmentQuestion(question);
    }

    private String getAbilityLevel(int score, int totalQuestions) {
        double percentage = (double) score / (totalQuestions * 10) * 100;
        if (percentage >= 90) {
            return "精通";
        } else if (percentage >= 70) {
            return "进阶";
        } else if (percentage >= 40) {
            return "入门";
        } else {
            return "零基础";
        }
    }
    
    @Override
    public List<Problem> getProblemsByLevelId(Long levelId) {
        return pathLevelProblemMapper.findProblemsByLevelId(levelId);
    }
    
    @Override
    public void bindProblemToLevel(Long levelId, Long problemId, Integer orderNum) {
        PathLevelProblem plp = new PathLevelProblem();
        plp.setLevelId(levelId);
        plp.setProblemId(problemId);
        plp.setOrderNum(orderNum != null ? orderNum : 1);
        pathLevelProblemMapper.insert(plp);
        updatePathLevelProblemIds(levelId);
    }
    
    @Override
    public void unbindProblemFromLevel(Long levelId, Long problemId) {
        pathLevelProblemMapper.deleteByLevelIdAndProblemId(levelId, problemId);
        updatePathLevelProblemIds(levelId);
    }
    
    @Override
    public void batchBindProblemsToLevel(Long levelId, List<Long> problemIds) {
        List<PathLevelProblem> list = new ArrayList<>();
        for (int i = 0; i < problemIds.size(); i++) {
            PathLevelProblem plp = new PathLevelProblem();
            plp.setLevelId(levelId);
            plp.setProblemId(problemIds.get(i));
            plp.setOrderNum(i + 1);
            list.add(plp);
        }
        pathLevelProblemMapper.batchInsert(list);
        updatePathLevelProblemIds(levelId);
    }
    
    @Override
    public void updateLevelProblems(Long levelId, List<Long> problemIds) {
        pathLevelProblemMapper.deleteByLevelId(levelId);
        if (problemIds != null && !problemIds.isEmpty()) {
            batchBindProblemsToLevel(levelId, problemIds);
        } else {
            updatePathLevelProblemIds(levelId);
        }
    }
    
    private void updatePathLevelProblemIds(Long levelId) {
        List<Problem> problems = pathLevelProblemMapper.findProblemsByLevelId(levelId);
        String problemIdsStr = problems.stream()
                .map(p -> String.valueOf(p.getId()))
                .reduce((a, b) -> a + "," + b)
                .orElse("");
        
        PathLevel level = new PathLevel();
        level.setId(levelId);
        level.setProblemIds(problemIdsStr);
        learningPathMapper.updateLevel(level);
    }
    
    @Override
    public List<LearningResource> getResourcesByLevelId(Long levelId) {
        return learningResourceMapper.selectResourcesByLevelId(levelId);
    }
}
