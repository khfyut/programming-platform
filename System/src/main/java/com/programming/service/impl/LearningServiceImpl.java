package com.programming.service.impl;

import com.programming.entity.AssessmentQuestion;
import com.programming.entity.KnowledgePoint;
import com.programming.entity.LearningPath;
import com.programming.entity.LearningResource;
import com.programming.entity.PathChapter;
import com.programming.entity.PathLevel;
import com.programming.entity.PathLevelProblem;
import com.programming.entity.Problem;
import com.programming.entity.Submit;
import com.programming.entity.UserAssessment;
import com.programming.entity.UserKnowledgeMastery;
import com.programming.entity.UserPathProgress;
import com.programming.mapper.KnowledgeMasteryMapper;
import com.programming.mapper.LearningPathMapper;
import com.programming.mapper.LearningResourceMapper;
import com.programming.mapper.PathLevelProblemMapper;
import com.programming.mapper.ProblemMapper;
import com.programming.mapper.SubmitMapper;
import com.programming.service.LearningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class LearningServiceImpl implements LearningService {

    @Autowired
    private LearningPathMapper learningPathMapper;

    @Autowired
    private PathLevelProblemMapper pathLevelProblemMapper;

    @Autowired
    private LearningResourceMapper learningResourceMapper;

    @Autowired
    private KnowledgeMasteryMapper knowledgeMasteryMapper;

    @Autowired
    private SubmitMapper submitMapper;

    @Autowired
    private ProblemMapper problemMapper;

    private Set<Long> parseIdSet(String rawIds) {
        if (rawIds == null || rawIds.isBlank()) {
            return new LinkedHashSet<>();
        }

        Set<Long> ids = new LinkedHashSet<>();
        for (String part : rawIds.split(",")) {
            if (part == null || part.isBlank()) {
                continue;
            }
            try {
                ids.add(Long.parseLong(part.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        return ids;
    }

    private Set<String> parseTextSet(String rawText) {
        if (rawText == null || rawText.isBlank() || "NULL".equalsIgnoreCase(rawText.trim())) {
            return new LinkedHashSet<>();
        }

        Set<String> values = new LinkedHashSet<>();
        for (String part : rawText.split(",")) {
            if (part == null) {
                continue;
            }
            String value = part.trim();
            if (!value.isEmpty() && !"NULL".equalsIgnoreCase(value)) {
                values.add(value);
            }
        }
        return values;
    }

    private boolean isCompleted(Set<Long> completedIds, Long targetId) {
        return targetId != null && completedIds.contains(targetId);
    }

    private String joinIds(Set<Long> ids) {
        return ids.stream()
                .map(String::valueOf)
                .reduce((left, right) -> left + "," + right)
                .orElse("");
    }

    private List<Submit> loadUserSubmits(Long userId) {
        int totalCount = submitMapper.countByUserId(userId, null);
        return submitMapper.findByUserId(userId, null, 0, totalCount > 0 ? totalCount : 1);
    }

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
        resultData.put("analysis", getAbilityAnalysis(score));
        resultData.put("suggestions", getLearningSuggestions(abilityLevel));
        resultData.put("recommendedPath", getRecommendedPath(abilityLevel));
        return resultData;
    }

    private String getLevelDescription(String abilityLevel) {
        Map<String, String> descriptions = new HashMap<>();
        descriptions.put("零基础", "建议先从编程语法和基础题入门。");
        descriptions.put("入门", "已经掌握基础概念，适合继续巩固数据结构和常见题型。");
        descriptions.put("进阶", "具备稳定的解题能力，可以继续挑战中高难度内容。");
        descriptions.put("精通", "建议加强综合项目和高阶算法训练。");
        return descriptions.getOrDefault(abilityLevel, "保持稳定练习节奏。");
    }

    private List<Map<String, Object>> getAbilityAnalysis(int score) {
        List<Map<String, Object>> analysis = new ArrayList<>();
        analysis.add(buildAnalysisItem("算法基础", Math.min(100, score + 10)));
        analysis.add(buildAnalysisItem("数据结构", Math.min(100, score)));
        analysis.add(buildAnalysisItem("问题解决", Math.min(100, score + 5)));
        return analysis;
    }

    private Map<String, Object> buildAnalysisItem(String name, int score) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("score", score);
        return item;
    }

    private List<String> getLearningSuggestions(String abilityLevel) {
        List<String> suggestions = new ArrayList<>();
        if ("零基础".equals(abilityLevel)) {
            suggestions.add("先完成基础语法和简单题训练。");
            suggestions.add("重点建立变量、条件和循环的基本理解。");
            suggestions.add("建议每次练习后整理一道代表题。");
        } else if ("入门".equals(abilityLevel)) {
            suggestions.add("继续巩固基础，并开始系统练习数据结构。");
            suggestions.add("增加中等难度题目的训练比例。");
            suggestions.add("结合错题本复盘常见错误。");
        } else if ("进阶".equals(abilityLevel)) {
            suggestions.add("可以开始挑战更复杂的算法题。");
            suggestions.add("建议按专题集中训练，提高稳定性。");
            suggestions.add("结合项目练习加强综合能力。");
        } else {
            suggestions.add("保持当前节奏，增加高难度综合题训练。");
            suggestions.add("尝试总结自己的题型方法论。");
            suggestions.add("可结合实际项目提升工程能力。");
        }
        return suggestions;
    }

    private Map<String, Object> getRecommendedPath(String abilityLevel) {
        Map<String, Object> path = new HashMap<>();
        if ("零基础".equals(abilityLevel)) {
            path.put("id", 1);
            path.put("name", "编程基础入门");
            path.put("description", "适合从基础语法和简单练习开始。");
        } else if ("入门".equals(abilityLevel)) {
            path.put("id", 2);
            path.put("name", "算法与数据结构进阶");
            path.put("description", "适合逐步提升解题速度和稳定性。");
        } else if ("进阶".equals(abilityLevel)) {
            path.put("id", 3);
            path.put("name", "高级编程技能");
            path.put("description", "适合继续挑战更复杂的算法和工程内容。");
        } else {
            path.put("id", 4);
            path.put("name", "专家级技术研究");
            path.put("description", "适合进行高阶专题和综合项目训练。");
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
                    progress.setCurrentLevelId(levels.get(0).getId());
                }
            }

            progress.setCompletedChapters("");
            progress.setCompletedLevels("");
            learningPathMapper.insertUserPathProgress(progress);
        }
        return progress;
    }

    @Override
    public Map<String, Object> getUserPathProgressWithPercentage(Long userId, Long pathId) {
        Map<String, Object> result = new HashMap<>();
        UserPathProgress progress = getUserPathProgress(userId, pathId);
        Set<Long> completedLevelIds = parseIdSet(progress.getCompletedLevels());
        Set<Long> completedChapterIds = parseIdSet(progress.getCompletedChapters());

        int totalLevels = 0;
        int completedLevelsCount = completedLevelIds.size();
        List<Map<String, Object>> chapterProgress = new ArrayList<>();
        Set<Long> pathProblemIds = new LinkedHashSet<>();
        Set<String> pathKnowledgePoints = new LinkedHashSet<>();

        List<PathChapter> chapters = learningPathMapper.selectChaptersByPathId(pathId);
        for (PathChapter chapter : chapters) {
            List<PathLevel> levels = learningPathMapper.selectLevelsByChapterId(chapter.getId());
            totalLevels += levels.size();

            int chapterCompletedLevels = 0;
            for (PathLevel level : levels) {
                pathProblemIds.addAll(parseIdSet(level.getProblemIds()));
                pathKnowledgePoints.addAll(parseTextSet(level.getKnowledgePoints()));
                if (isCompleted(completedLevelIds, level.getId())) {
                    chapterCompletedLevels++;
                }
            }

            Map<String, Object> chapterInfo = new HashMap<>();
            chapterInfo.put("chapterId", chapter.getId());
            chapterInfo.put("chapterName", chapter.getName());
            chapterInfo.put("totalLevels", levels.size());
            chapterInfo.put("completedLevels", chapterCompletedLevels);
            chapterInfo.put("progress", levels.isEmpty() ? 0 : (int) ((double) chapterCompletedLevels / levels.size() * 100));
            chapterProgress.add(chapterInfo);
        }

        if (pathKnowledgePoints.isEmpty() && !pathProblemIds.isEmpty()) {
            for (Long problemId : pathProblemIds) {
                Problem problem = problemMapper.findById(problemId);
                if (problem != null) {
                    pathKnowledgePoints.addAll(parseTextSet(problem.getKnowledgePoints()));
                }
            }
        }

        int progressPercentage = totalLevels > 0 ? (int) ((double) completedLevelsCount / totalLevels * 100) : 0;
        int remainingLevels = Math.max(totalLevels - completedLevelsCount, 0);

        List<Submit> userSubmits = loadUserSubmits(userId);
        boolean filterByPathProblems = !pathProblemIds.isEmpty();
        boolean filterByKnowledgePoints = !pathKnowledgePoints.isEmpty();
        boolean hasPathBinding = filterByPathProblems || filterByKnowledgePoints;
        int pathSubmitCount = 0;
        LocalDateTime firstPathSubmit = null;
        LocalDateTime lastPathSubmit = null;
        Map<Long, Problem> problemCache = new HashMap<>();

        for (Submit submit : userSubmits) {
            if (submit == null || submit.getCreateTime() == null) {
                continue;
            }
            if (!hasPathBinding) {
                continue;
            }

            boolean matchesProblem = filterByPathProblems
                    && submit.getProblemId() != null
                    && pathProblemIds.contains(submit.getProblemId());
            boolean matchesKnowledge = filterByKnowledgePoints
                    && matchesPathKnowledgePoints(submit.getProblemId(), pathKnowledgePoints, problemCache);
            if (!matchesProblem && !matchesKnowledge) {
                continue;
            }

            pathSubmitCount++;
            if (firstPathSubmit == null || submit.getCreateTime().isBefore(firstPathSubmit)) {
                firstPathSubmit = submit.getCreateTime();
            }
            if (lastPathSubmit == null || submit.getCreateTime().isAfter(lastPathSubmit)) {
                lastPathSubmit = submit.getCreateTime();
            }
        }

        LocalDateTime lastActivity = lastPathSubmit != null ? lastPathSubmit : progress.getUpdateTime();
        long activeDays = 0;
        if (firstPathSubmit != null && lastActivity != null) {
            activeDays = Duration.between(
                    firstPathSubmit.toLocalDate().atStartOfDay(),
                    lastActivity.toLocalDate().plusDays(1).atStartOfDay()
            ).toDays();
        } else if (progress.getCreateTime() != null && lastActivity != null && !lastActivity.isBefore(progress.getCreateTime())) {
            activeDays = Duration.between(
                    progress.getCreateTime().toLocalDate().atStartOfDay(),
                    lastActivity.toLocalDate().plusDays(1).atStartOfDay()
            ).toDays();
        }

        Map<String, Object> efficiency = new HashMap<>();
        List<Map<String, Object>> weakPoints;
        int estimatedDays;
        if (hasPathBinding) {
            efficiency.put("averageCompletionTime", formatAverageCompletionTime(progress, lastActivity, completedLevelsCount));
            efficiency.put("learningSpeed", describeLearningSpeed(completedLevelsCount, pathSubmitCount, activeDays));
            efficiency.put("suggestedPace", buildSuggestedPace(completedLevelsCount, pathSubmitCount, remainingLevels, activeDays));
            weakPoints = buildPathWeakPoints(userId, pathKnowledgePoints);
            estimatedDays = estimateCompletionDays(completedLevelsCount, remainingLevels, activeDays);
        } else {
            efficiency.put("averageCompletionTime", "Path bindings are missing, estimation is unavailable.");
            efficiency.put("learningSpeed", "Insufficient path data");
            efficiency.put("suggestedPace", "Bind problems or knowledge points to this path before generating pace advice.");
            weakPoints = new ArrayList<>();
            estimatedDays = 0;
        }

        result.put("progress", progressPercentage);
        result.put("completedLevels", completedLevelsCount);
        result.put("totalLevels", totalLevels);
        result.put("currentChapterId", progress.getCurrentChapterId());
        result.put("currentLevelId", progress.getCurrentLevelId());
        result.put("completedLevelIds", new ArrayList<>(completedLevelIds));
        result.put("completedChapterIds", new ArrayList<>(completedChapterIds));
        result.put("chapterProgress", chapterProgress);
        result.put("efficiency", efficiency);
        result.put("weakPoints", weakPoints);
        result.put("estimatedCompletionDays", estimatedDays);
        result.put("remainingLevels", remainingLevels);
        result.put("lastActivity", lastActivity);
        result.put("analysisReady", hasPathBinding);
        result.put("analysisMessage", hasPathBinding ? null : "The current path has no bound problems or knowledge points.");
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

        if (currentLevelIndex > 0) {
            UserPathProgress progress = learningPathMapper.selectUserPathProgress(userId, chapter.getPathId());
            if (progress == null) {
                return false;
            }

            Set<Long> completedLevels = parseIdSet(progress.getCompletedLevels());
            PathLevel previousLevel = chapterLevels.get(currentLevelIndex - 1);
            if (!isCompleted(completedLevels, previousLevel.getId())) {
                return false;
            }
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

        Set<Long> completedLevels = parseIdSet(progress.getCompletedLevels());
        if (!isCompleted(completedLevels, levelId)) {
            completedLevels.add(levelId);
            progress.setCompletedLevels(joinIds(completedLevels));
        }

        List<PathLevel> chapterLevels = learningPathMapper.selectLevelsByChapterId(chapterId);
        boolean chapterCompleted = true;
        for (PathLevel chapterLevel : chapterLevels) {
            if (!isCompleted(completedLevels, chapterLevel.getId())) {
                chapterCompleted = false;
                break;
            }
        }

        if (chapterCompleted) {
            Set<Long> completedChapters = parseIdSet(progress.getCompletedChapters());
            if (!isCompleted(completedChapters, chapterId)) {
                completedChapters.add(chapterId);
                progress.setCompletedChapters(joinIds(completedChapters));
            }

            List<PathChapter> chapters = learningPathMapper.selectChaptersByPathId(pathId);
            int currentChapterIndex = -1;
            for (int i = 0; i < chapters.size(); i++) {
                if (chapters.get(i).getId().equals(chapterId)) {
                    currentChapterIndex = i;
                    break;
                }
            }

            if (currentChapterIndex > -1 && currentChapterIndex < chapters.size() - 1) {
                PathChapter nextChapter = chapters.get(currentChapterIndex + 1);
                progress.setCurrentChapterId(nextChapter.getId());

                List<PathLevel> nextLevels = learningPathMapper.selectLevelsByChapterId(nextChapter.getId());
                if (!nextLevels.isEmpty()) {
                    progress.setCurrentLevelId(nextLevels.get(0).getId());
                }
            }
        } else {
            int currentLevelIndex = -1;
            for (int i = 0; i < chapterLevels.size(); i++) {
                if (chapterLevels.get(i).getId().equals(levelId)) {
                    currentLevelIndex = i;
                    break;
                }
            }

            if (currentLevelIndex > -1 && currentLevelIndex < chapterLevels.size() - 1) {
                progress.setCurrentLevelId(chapterLevels.get(currentLevelIndex + 1).getId());
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
        List<UserPathProgress> progresses = getUserProgressList(userId);

        int completedLevels = 0;
        int totalLevels = 0;
        for (UserPathProgress progress : progresses) {
            completedLevels += parseIdSet(progress.getCompletedLevels()).size();
            List<PathChapter> chapters = learningPathMapper.selectChaptersByPathId(progress.getPathId());
            for (PathChapter chapter : chapters) {
                totalLevels += learningPathMapper.selectLevelsByChapterId(chapter.getId()).size();
            }
        }

        statistics.put("activePathCount", progresses.size());
        statistics.put("completedLevels", completedLevels);
        statistics.put("totalLevels", totalLevels);
        statistics.put("progressRate", totalLevels > 0 ? (int) Math.round(completedLevels * 100.0 / totalLevels) : 0);
        statistics.put("submissionCount", submitMapper.countByUserId(userId, null));
        statistics.put("solvedProblemCount", submitMapper.countPassedByUserId(userId));
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
        double percentage = totalQuestions > 0 ? (double) score / (totalQuestions * 10) * 100 : 0;
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
                .map(problem -> String.valueOf(problem.getId()))
                .reduce((left, right) -> left + "," + right)
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

    private boolean matchesPathKnowledgePoints(Long problemId,
                                               Set<String> pathKnowledgePoints,
                                               Map<Long, Problem> problemCache) {
        if (problemId == null || pathKnowledgePoints.isEmpty()) {
            return false;
        }

        Problem problem = problemCache.get(problemId);
        if (problem == null) {
            problem = problemMapper.findById(problemId);
            if (problem != null) {
                problemCache.put(problemId, problem);
            }
        }
        if (problem == null) {
            return false;
        }

        Set<String> problemKnowledgePoints = parseTextSet(problem.getKnowledgePoints());
        for (String knowledgePoint : problemKnowledgePoints) {
            if (pathKnowledgePoints.contains(knowledgePoint)) {
                return true;
            }
        }
        return false;
    }

    private String formatAverageCompletionTime(UserPathProgress progress, LocalDateTime lastActivity, int completedLevelsCount) {
        if (completedLevelsCount <= 0 || progress.getCreateTime() == null || lastActivity == null || lastActivity.isBefore(progress.getCreateTime())) {
            return "暂无已完成关卡数据";
        }

        Duration totalDuration = Duration.between(progress.getCreateTime(), lastActivity);
        double averageMinutes = totalDuration.toMinutes() / (double) completedLevelsCount;
        if (averageMinutes >= 1440) {
            return String.format("%.1f 天/关卡", averageMinutes / 1440);
        }
        if (averageMinutes >= 60) {
            return String.format("%.1f 小时/关卡", averageMinutes / 60);
        }
        return String.format("%.0f 分钟/关卡", averageMinutes);
    }

    private String describeLearningSpeed(int completedLevelsCount, int pathSubmitCount, long activeDays) {
        if (pathSubmitCount == 0) {
            return "暂无学习记录";
        }
        if (completedLevelsCount == 0 || activeDays <= 0) {
            return "起步中";
        }

        double levelsPerWeek = completedLevelsCount * 7.0 / activeDays;
        if (levelsPerWeek >= 4) {
            return "较快";
        }
        if (levelsPerWeek >= 2) {
            return "稳定";
        }
        return "逐步推进";
    }

    private String buildSuggestedPace(int completedLevelsCount, int pathSubmitCount, int remainingLevels, long activeDays) {
        if (remainingLevels <= 0) {
            return "当前路径已完成";
        }
        if (pathSubmitCount == 0) {
            return "建议先完成当前关卡，再生成个性化节奏";
        }
        if (completedLevelsCount == 0 || activeDays <= 0) {
            return "建议每周完成 1 个关卡";
        }

        int suggestedWeeklyLevels = Math.max(1, (int) Math.round(completedLevelsCount * 7.0 / activeDays));
        return "建议每周完成 " + suggestedWeeklyLevels + " 个关卡";
    }

    private int estimateCompletionDays(int completedLevelsCount, int remainingLevels, long activeDays) {
        if (remainingLevels <= 0) {
            return 0;
        }
        if (completedLevelsCount <= 0 || activeDays <= 0) {
            return 0;
        }

        double levelsPerDay = completedLevelsCount / (double) activeDays;
        if (levelsPerDay <= 0) {
            return 0;
        }
        return (int) Math.ceil(remainingLevels / levelsPerDay);
    }

    private List<Map<String, Object>> buildPathWeakPoints(Long userId, Set<String> pathKnowledgePoints) {
        Map<Long, KnowledgePoint> knowledgePointMap = new HashMap<>();
        for (KnowledgePoint knowledgePoint : knowledgeMasteryMapper.selectAllKnowledgePoints()) {
            if (knowledgePoint != null && knowledgePoint.getId() != null) {
                knowledgePointMap.put(knowledgePoint.getId(), knowledgePoint);
            }
        }

        List<UserKnowledgeMastery> weakMasteries = knowledgeMasteryMapper.selectUserWeakKnowledgePoints(userId, 2);
        List<Map<String, Object>> matchedWeakPoints = new ArrayList<>();
        List<Map<String, Object>> globalWeakPoints = new ArrayList<>();

        for (UserKnowledgeMastery mastery : weakMasteries) {
            if (mastery == null || mastery.getKnowledgeId() == null) {
                continue;
            }

            KnowledgePoint knowledgePoint = knowledgePointMap.get(mastery.getKnowledgeId());
            if (knowledgePoint == null || knowledgePoint.getName() == null || knowledgePoint.getName().isBlank()) {
                continue;
            }

            Map<String, Object> item = new HashMap<>();
            item.put("knowledgeId", knowledgePoint.getId());
            item.put("knowledgePoint", knowledgePoint.getName());
            item.put("masteryLevel", mastery.getMasteryLevel());
            item.put("score", mastery.getScore() == null ? 0 : mastery.getScore());
            item.put("suggestion", buildWeakPointSuggestion(mastery));
            globalWeakPoints.add(item);

            if (pathKnowledgePoints.contains(knowledgePoint.getName())) {
                matchedWeakPoints.add(item);
            }
        }

        List<Map<String, Object>> source = matchedWeakPoints.isEmpty() ? globalWeakPoints : matchedWeakPoints;
        return source.size() > 3 ? new ArrayList<>(source.subList(0, 3)) : source;
    }

    private String buildWeakPointSuggestion(UserKnowledgeMastery mastery) {
        int score = mastery.getScore() == null ? 0 : mastery.getScore();
        if (score < 40) {
            return "建议优先复习基础概念并配合错题重练";
        }
        if (score < 70) {
            return "建议补充专项练习，提升稳定性";
        }
        return "建议通过综合题进一步巩固";
    }
}
