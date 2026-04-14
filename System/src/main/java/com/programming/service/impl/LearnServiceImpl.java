package com.programming.service.impl;

import com.programming.entity.AssessmentQuestion;
import com.programming.entity.KnowledgePoint;
import com.programming.entity.LearnRecord;
import com.programming.entity.LearningPath;
import com.programming.entity.PathChapter;
import com.programming.entity.PathLevel;
import com.programming.entity.Problem;
import com.programming.entity.Submit;
import com.programming.entity.UserKnowledgeMastery;
import com.programming.entity.UserPathProgress;
import com.programming.mapper.KnowledgeMasteryMapper;
import com.programming.mapper.LearnRecordMapper;
import com.programming.mapper.LearningPathMapper;
import com.programming.mapper.ProblemMapper;
import com.programming.mapper.SubmitMapper;
import com.programming.service.KnowledgeMasteryService;
import com.programming.service.LearnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class LearnServiceImpl implements LearnService {

    @Autowired
    private LearnRecordMapper learnRecordMapper;

    @Autowired
    private SubmitMapper submitMapper;

    @Autowired
    private ProblemMapper problemMapper;

    @Autowired
    private LearningPathMapper learningPathMapper;

    @Autowired
    private KnowledgeMasteryMapper knowledgeMasteryMapper;

    @Autowired
    private KnowledgeMasteryService knowledgeMasteryService;

    @Override
    public Map<String, Object> getStatistics(Long userId) {
        LearnRecord record = learnRecordMapper.findByUserId(userId);

        int solved = 0;
        int submitted = 0;
        int accuracy = 0;
        int streak = 0;

        Map<String, Object> difficultyStats = new HashMap<>();
        difficultyStats.put("easy", 0);
        difficultyStats.put("medium", 0);
        difficultyStats.put("hard", 0);

        if (record != null) {
            solved = record.getCorrectCount() != null ? record.getCorrectCount() : 0;
            submitted = record.getProblemCount() != null ? record.getProblemCount() : 0;
        }

        if (submitted > 0 && solved > 0) {
            accuracy = (int) Math.round((double) solved / submitted * 100);
        }

        int totalCount = submitMapper.countByUserId(userId, null);
        List<Submit> allSubmits = submitMapper.findByUserId(userId, null, 0, totalCount > 0 ? totalCount : 1);

        for (Submit submit : allSubmits) {
            if (submit.getProblemId() == null) {
                continue;
            }

            Problem problem = problemMapper.findById(submit.getProblemId());
            if (problem == null || submit.getResult() == null || submit.getResult() != 0 || problem.getDifficulty() == null) {
                continue;
            }

            if (problem.getDifficulty() == 0) {
                difficultyStats.put("easy", (int) difficultyStats.get("easy") + 1);
            } else if (problem.getDifficulty() == 1) {
                difficultyStats.put("medium", (int) difficultyStats.get("medium") + 1);
            } else if (problem.getDifficulty() == 2) {
                difficultyStats.put("hard", (int) difficultyStats.get("hard") + 1);
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("solved", solved);
        stats.put("submitted", submitted);
        stats.put("accuracy", accuracy);
        stats.put("streak", streak);

        Map<String, Object> result = new HashMap<>();
        result.put("stats", stats);
        result.put("difficultyStats", difficultyStats);
        return result;
    }

    @Override
    public Map<String, Object> getRecommend(Long userId) {
        Map<String, Object> result = new HashMap<>();
        List<KnowledgePoint> weakPoints = getWeakKnowledgePoints(userId);
        List<Submit> recentSubmits = submitMapper.findByUserId(userId, null, 0, 20);
        String preferredLanguage = detectPreferredLanguage(recentSubmits);
        if (!"java".equalsIgnoreCase(preferredLanguage)) {
            preferredLanguage = "java";
        }
        Set<Long> passedProblemIds = new LinkedHashSet<>(submitMapper.findPassedProblemIdsByUserId(userId));

        LinkedHashMap<Long, Problem> recommendedProblemMap = new LinkedHashMap<>();
        for (KnowledgePoint point : weakPoints) {
            if (point == null || point.getName() == null || point.getName().isBlank()) {
                continue;
            }

            List<Problem> problems = problemMapper.findByKnowledgePoint(point.getName(), 5);
            for (Problem problem : problems) {
                if (problem == null
                        || problem.getId() == null
                        || passedProblemIds.contains(problem.getId())
                        || !matchesPreferredLanguage(problem, preferredLanguage)) {
                    continue;
                }
                recommendedProblemMap.putIfAbsent(problem.getId(), problem);
                if (recommendedProblemMap.size() >= 5) {
                    break;
                }
            }

            if (recommendedProblemMap.size() >= 5) {
                break;
            }
        }

        fillRecommendedProblems(recommendedProblemMap, preferredLanguage, 0, passedProblemIds);
        fillRecommendedProblems(recommendedProblemMap, preferredLanguage, null, passedProblemIds);
        List<Problem> recommendedProblems = new ArrayList<>(recommendedProblemMap.values());

        List<Map<String, Object>> recommendedModules = new ArrayList<>();
        List<LearningPath> paths = learningPathMapper.selectAllPaths();
        for (LearningPath path : paths) {
            if (path == null || path.getId() == null) {
                continue;
            }

            if (!isJavaAgentPath(path)) {
                continue;
            }

            recommendedModules.add(buildModule(path));
            if (recommendedModules.size() >= 3) {
                break;
            }
        }

        if (recommendedModules.size() < 3) {
            for (LearningPath path : paths) {
                if (path == null || path.getId() == null) {
                    continue;
                }

                boolean alreadyAdded = false;
                for (Map<String, Object> module : recommendedModules) {
                    if (path.getId().equals(module.get("pathId"))) {
                        alreadyAdded = true;
                        break;
                    }
                }
                if (alreadyAdded) {
                    continue;
                }

                if (!isJavaAgentPath(path)) {
                    continue;
                }

                recommendedModules.add(buildModule(path));
                if (recommendedModules.size() >= 3) {
                    break;
                }
            }
        }

        List<String> suggestions = new ArrayList<>();
        if (!weakPoints.isEmpty()) {
            List<String> weakPointNames = new ArrayList<>();
            for (KnowledgePoint point : weakPoints) {
                if (point != null && point.getName() != null && !point.getName().isBlank()) {
                    weakPointNames.add(point.getName());
                }
            }
            if (!weakPointNames.isEmpty()) {
                suggestions.add("Focus on these knowledge points first: " + String.join(", ", weakPointNames));
            }
        }
        if (preferredLanguage != null && !preferredLanguage.isBlank()) {
            suggestions.add("Keep practicing with " + preferredLanguage + " to build stable problem-solving rhythm.");
        } else {
            suggestions.add("Keep a steady practice rhythm and review the problems you miss.");
        }
        suggestions.add("Summarize recurring mistakes after each session and revisit the wrong-book regularly.");

        result.put("problems", recommendedProblems);
        result.put("modules", recommendedModules);
        result.put("suggestions", suggestions);
        result.put("weakPoints", weakPoints);
        result.put("preferredLanguage", preferredLanguage);
        return result;
    }

    @Override
    public List<AssessmentQuestion> getAssessmentQuestions() {
        return new ArrayList<>();
    }

    @Override
    @Transactional
    public Map<String, Object> submitAssessment(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        result.put("pathId", 1L);
        result.put("recommendedPath", "introductory-path");
        return result;
    }

    @Override
    public LearningPath getLearningPath(Long userId) {
        List<UserPathProgress> progresses = learningPathMapper.selectUserProgressByUserId(userId);
        if (!progresses.isEmpty() && progresses.get(0).getPathId() != null) {
            return learningPathMapper.selectPathById(progresses.get(0).getPathId());
        }

        List<LearningPath> paths = learningPathMapper.selectAllPaths();
        return paths.isEmpty() ? null : paths.get(0);
    }

    @Override
    @Transactional
    public Map<String, Object> unlockLevel(Long userId, Long levelId) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", learningPathMapper.selectLevelById(levelId) != null);
        result.put("message", learningPathMapper.selectLevelById(levelId) != null ? "pending-learning-service-flow" : "level-not-found");
        return result;
    }

    @Override
    public Map<String, Object> getPathProgress(Long userId) {
        Map<String, Object> result = new HashMap<>();
        List<UserPathProgress> progresses = learningPathMapper.selectUserProgressByUserId(userId);
        if (progresses.isEmpty()) {
            result.put("completedLevels", 0);
            result.put("totalLevels", 0);
            result.put("progress", 0);
            return result;
        }

        UserPathProgress progress = progresses.get(0);
        int totalLevels = 0;
        List<PathChapter> chapters = learningPathMapper.selectChaptersByPathId(progress.getPathId());
        for (PathChapter chapter : chapters) {
            List<PathLevel> levels = learningPathMapper.selectLevelsByChapterId(chapter.getId());
            totalLevels += levels.size();
        }

        int completedLevels = parseIdSet(progress.getCompletedLevels()).size();
        int percent = totalLevels > 0 ? (int) Math.round(completedLevels * 100.0 / totalLevels) : 0;
        result.put("pathId", progress.getPathId());
        result.put("completedLevels", completedLevels);
        result.put("totalLevels", totalLevels);
        result.put("progress", percent);
        result.put("currentChapterId", progress.getCurrentChapterId());
        result.put("currentLevelId", progress.getCurrentLevelId());
        return result;
    }

    @Override
    public Map<String, Object> getKnowledgeGraph() {
        return knowledgeMasteryService.getKnowledgeGraph();
    }

    @Override
    public List<UserKnowledgeMastery> getKnowledgeMastery(Long userId) {
        return knowledgeMasteryMapper.selectUserKnowledgeMasteriesByUserId(userId);
    }

    @Override
    public Map<String, Object> getWeeklyReport(Long userId) {
        return knowledgeMasteryService.getWeeklyReport(userId);
    }

    @Override
    public List<KnowledgePoint> getWeakKnowledgePoints(Long userId) {
        List<UserKnowledgeMastery> weakMasteries = knowledgeMasteryMapper.selectUserWeakKnowledgePoints(userId, 2);
        LinkedHashMap<Long, KnowledgePoint> weakPointMap = new LinkedHashMap<>();

        for (UserKnowledgeMastery mastery : weakMasteries) {
            if (mastery == null || mastery.getKnowledgeId() == null) {
                continue;
            }

            KnowledgePoint knowledgePoint = knowledgeMasteryMapper.selectKnowledgePointById(mastery.getKnowledgeId());
            if (knowledgePoint != null && knowledgePoint.getId() != null) {
                weakPointMap.putIfAbsent(knowledgePoint.getId(), knowledgePoint);
            }
        }

        return new ArrayList<>(weakPointMap.values());
    }

    @Override
    public Map<String, Integer> getDifficultyStats(Long userId) {
        Map<String, Integer> difficultyStats = new HashMap<>();
        difficultyStats.put("easy", 0);
        difficultyStats.put("medium", 0);
        difficultyStats.put("hard", 0);

        int totalCount = submitMapper.countByUserId(userId, null);
        List<Submit> allSubmits = submitMapper.findByUserId(userId, null, 0, totalCount > 0 ? totalCount : 1);

        for (Submit submit : allSubmits) {
            if (submit.getProblemId() == null) {
                continue;
            }

            Problem problem = problemMapper.findById(submit.getProblemId());
            if (problem == null || submit.getResult() == null || submit.getResult() != 0 || problem.getDifficulty() == null) {
                continue;
            }

            if (problem.getDifficulty() == 0) {
                difficultyStats.put("easy", difficultyStats.get("easy") + 1);
            } else if (problem.getDifficulty() == 1) {
                difficultyStats.put("medium", difficultyStats.get("medium") + 1);
            } else if (problem.getDifficulty() == 2) {
                difficultyStats.put("hard", difficultyStats.get("hard") + 1);
            }
        }

        return difficultyStats;
    }

    private String detectPreferredLanguage(List<Submit> submits) {
        Map<String, Integer> languageCount = new HashMap<>();
        for (Submit submit : submits) {
            if (submit == null || submit.getLanguage() == null || submit.getLanguage().isBlank()) {
                continue;
            }
            languageCount.put(submit.getLanguage(), languageCount.getOrDefault(submit.getLanguage(), 0) + 1);
        }

        String preferredLanguage = null;
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : languageCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                preferredLanguage = entry.getKey();
            }
        }
        return preferredLanguage;
    }

    private boolean matchesPreferredLanguage(Problem problem, String preferredLanguage) {
        if (preferredLanguage == null || preferredLanguage.isBlank()) {
            return true;
        }
        if (problem.getLanguage() == null || problem.getLanguage().isBlank()) {
            return true;
        }

        String problemLanguage = problem.getLanguage().toLowerCase();
        String preferred = preferredLanguage.toLowerCase();
        return problemLanguage.equals(preferred)
                || problemLanguage.contains(preferred)
                || preferred.contains(problemLanguage);
    }

    private boolean isJavaAgentPath(LearningPath path) {
        if (path == null || path.getId() == null) {
            return false;
        }
        if (path.getId() == 1L || path.getId() == 3L || path.getId() == 4L
                || path.getId() == 5L || path.getId() == 6L) {
            return true;
        }
        String name = path.getName() == null ? "" : path.getName();
        String language = path.getLanguage() == null ? "" : path.getLanguage();
        String direction = path.getDirection() == null ? "" : path.getDirection();
        String scopeText = (name + " " + language + " " + direction).toLowerCase();
        return scopeText.contains("java")
                || name.contains("算法")
                || name.contains("后端开发基础")
                || name.contains("Java后端");
    }

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

    private void fillRecommendedProblems(LinkedHashMap<Long, Problem> recommendedProblemMap,
                                         String preferredLanguage,
                                         Integer difficulty,
                                         Set<Long> excludedProblemIds) {
        if (recommendedProblemMap.size() >= 5) {
            return;
        }

        List<Problem> fallbackProblems = problemMapper.findByPage(0, 10, difficulty, preferredLanguage, null);
        for (Problem problem : fallbackProblems) {
            if (problem == null || problem.getId() == null || excludedProblemIds.contains(problem.getId())) {
                continue;
            }
            recommendedProblemMap.putIfAbsent(problem.getId(), problem);
            if (recommendedProblemMap.size() >= 5) {
                break;
            }
        }
    }

    private Map<String, Object> buildModule(LearningPath path) {
        Map<String, Object> module = new HashMap<>();
        module.put("pathId", path.getId());
        module.put("pathName", path.getName());
        module.put("description", path.getDescription());
        module.put("language", path.getLanguage());
        module.put("direction", path.getDirection());
        return module;
    }
}
