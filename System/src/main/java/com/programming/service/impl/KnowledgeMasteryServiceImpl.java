package com.programming.service.impl;

import com.programming.entity.KnowledgePoint;
import com.programming.entity.KnowledgeRelationship;
import com.programming.entity.Problem;
import com.programming.entity.Submit;
import com.programming.entity.User;
import com.programming.entity.UserKnowledgeMastery;
import com.programming.mapper.KnowledgeMasteryMapper;
import com.programming.mapper.ProblemMapper;
import com.programming.mapper.SubmitMapper;
import com.programming.mapper.UserMapper;
import com.programming.service.KnowledgeMasteryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class KnowledgeMasteryServiceImpl implements KnowledgeMasteryService {

    private static final DateTimeFormatter REPORT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private KnowledgeMasteryMapper knowledgeMasteryMapper;

    @Autowired
    private SubmitMapper submitMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProblemMapper problemMapper;

    @Override
    public List<KnowledgePoint> getAllKnowledgePoints() {
        return knowledgeMasteryMapper.selectAllKnowledgePoints();
    }

    @Override
    public KnowledgePoint getKnowledgePointById(Long id) {
        return knowledgeMasteryMapper.selectKnowledgePointById(id);
    }

    @Override
    public List<KnowledgePoint> getKnowledgePointsByParentId(Long parentId) {
        return knowledgeMasteryMapper.selectKnowledgePointsByParentId(parentId);
    }

    @Override
    public void createKnowledgePoint(KnowledgePoint knowledgePoint) {
        knowledgeMasteryMapper.insertKnowledgePoint(knowledgePoint);
    }

    @Override
    public void updateKnowledgePoint(KnowledgePoint knowledgePoint) {
        knowledgeMasteryMapper.updateKnowledgePoint(knowledgePoint);
    }

    @Override
    public void deleteKnowledgePoint(Long id) {
        knowledgeMasteryMapper.deleteKnowledgePoint(id);
    }

    @Override
    public List<KnowledgeRelationship> getRelationshipsBySourceId(Long sourceId) {
        return knowledgeMasteryMapper.selectRelationshipsBySourceId(sourceId);
    }

    @Override
    public List<KnowledgeRelationship> getRelationshipsByTargetId(Long targetId) {
        return knowledgeMasteryMapper.selectRelationshipsByTargetId(targetId);
    }

    @Override
    public void createRelationship(KnowledgeRelationship relationship) {
        knowledgeMasteryMapper.insertKnowledgeRelationship(relationship);
    }

    @Override
    public void deleteRelationship(Long id) {
        knowledgeMasteryMapper.deleteKnowledgeRelationship(id);
    }

    @Override
    public Map<String, Object> getKnowledgeGraph() {
        return getKnowledgeGraph(null, null, null, null);
    }

    @Override
    public Map<String, Object> getKnowledgeGraph(Long userId, String domain, Integer minMastery, Integer maxMastery) {
        Map<String, Object> graph = new HashMap<>();
        List<KnowledgePoint> rawNodes = knowledgeMasteryMapper.selectAllKnowledgePoints();
        Map<Long, UserKnowledgeMastery> masteryMap = loadMasteryMap(userId);
        List<KnowledgePoint> filteredNodes = new ArrayList<>();

        int minScore = normalizeMasteryBound(minMastery, 0);
        int maxScore = normalizeMasteryBound(maxMastery, 100);
        if (minScore > maxScore) {
            int temp = minScore;
            minScore = maxScore;
            maxScore = temp;
        }

        for (KnowledgePoint node : rawNodes) {
            if (node == null || node.getId() == null) {
                continue;
            }
            if (!matchesDomain(node, domain) || !matchesMasteryFilter(masteryMap.get(node.getId()), minScore, maxScore)) {
                continue;
            }
            filteredNodes.add(node);
        }

        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> edges = new ArrayList<>();
        Set<Long> visibleNodeIds = new LinkedHashSet<>();
        int nodeCount = filteredNodes.size();
        double angleStep = 2 * Math.PI / Math.max(nodeCount, 1);
        double radius = 200;

        for (int i = 0; i < filteredNodes.size(); i++) {
            KnowledgePoint node = filteredNodes.get(i);
            Map<String, Object> nodeInfo = buildKnowledgeNode(node, masteryMap.get(node.getId()));
            double angle = i * angleStep;
            nodeInfo.put("x", radius * Math.cos(angle));
            nodeInfo.put("y", radius * Math.sin(angle));
            nodes.add(nodeInfo);
            visibleNodeIds.add(node.getId());
        }

        for (KnowledgePoint node : filteredNodes) {
            List<KnowledgeRelationship> relationships = knowledgeMasteryMapper.selectRelationshipsBySourceId(node.getId());
            for (KnowledgeRelationship relationship : relationships) {
                if (relationship == null
                        || relationship.getSourceId() == null
                        || relationship.getTargetId() == null
                        || !visibleNodeIds.contains(relationship.getTargetId())) {
                    continue;
                }

                Map<String, Object> edge = new HashMap<>();
                edge.put("source", relationship.getSourceId());
                edge.put("target", relationship.getTargetId());
                edge.put("type", relationship.getRelationType());
                edge.put("strength", relationship.getStrength());
                edge.put("description", relationship.getDescription());
                edges.add(edge);
            }
        }

        List<Map<String, Object>> levels = new ArrayList<>();
        Map<Integer, List<Map<String, Object>>> nodesByDifficulty = new LinkedHashMap<>();
        for (Map<String, Object> node : nodes) {
            Integer difficulty = (Integer) node.get("difficulty");
            nodesByDifficulty.computeIfAbsent(difficulty == null ? 0 : difficulty, key -> new ArrayList<>()).add(node);
        }

        for (Map.Entry<Integer, List<Map<String, Object>>> entry : nodesByDifficulty.entrySet()) {
            Map<String, Object> levelInfo = new HashMap<>();
            levelInfo.put("level", entry.getKey());
            levelInfo.put("nodes", entry.getValue());
            levels.add(levelInfo);
        }

        Map<String, Object> hierarchy = new HashMap<>();
        hierarchy.put("levels", levels);

        graph.put("nodes", nodes);
        graph.put("edges", edges);
        graph.put("pathConnections", new ArrayList<>());
        graph.put("hierarchy", hierarchy);
        graph.put("metadata", Map.of(
                "totalNodes", nodes.size(),
                "totalEdges", edges.size(),
                "lastUpdated", LocalDateTime.now().toString(),
                "personalized", userId != null,
                "selectedDomain", domain == null ? "" : domain,
                "minMastery", minScore,
                "maxMastery", maxScore
        ));
        return graph;
    }

    @Override
    public Map<String, Object> getKnowledgePointDetail(Long userId, Long knowledgeId) {
        KnowledgePoint knowledgePoint = knowledgeMasteryMapper.selectKnowledgePointById(knowledgeId);
        if (knowledgePoint == null) {
            return null;
        }
        UserKnowledgeMastery mastery = userId == null ? null : knowledgeMasteryMapper.selectUserKnowledgeMastery(userId, knowledgeId);
        return buildKnowledgeNode(knowledgePoint, mastery);
    }

    @Override
    public UserKnowledgeMastery getUserKnowledgeMastery(Long userId, Long knowledgeId) {
        return knowledgeMasteryMapper.selectUserKnowledgeMastery(userId, knowledgeId);
    }

    @Override
    public List<UserKnowledgeMastery> getUserKnowledgeMasteries(Long userId) {
        return knowledgeMasteryMapper.selectUserKnowledgeMasteriesByUserId(userId);
    }

    @Override
    public List<UserKnowledgeMastery> getUserWeakKnowledgePoints(Long userId, Integer threshold) {
        return knowledgeMasteryMapper.selectUserWeakKnowledgePoints(userId, threshold);
    }

    @Override
    public void updateKnowledgeMastery(Long userId, Long knowledgeId, boolean correct) {
        UserKnowledgeMastery mastery = knowledgeMasteryMapper.selectUserKnowledgeMastery(userId, knowledgeId);
        if (mastery == null) {
            mastery = new UserKnowledgeMastery();
            mastery.setUserId(userId);
            mastery.setKnowledgeId(knowledgeId);
            mastery.setMasteryLevel(correct ? 1 : 0);
            mastery.setScore(correct ? 60 : 30);
            mastery.setLastPracticeTime(LocalDateTime.now());
            mastery.setPracticeCount(1);
            mastery.setCorrectCount(correct ? 1 : 0);
            knowledgeMasteryMapper.insertUserKnowledgeMastery(mastery);
            return;
        }

        int practiceCount = mastery.getPracticeCount() == null ? 0 : mastery.getPracticeCount();
        int correctCount = mastery.getCorrectCount() == null ? 0 : mastery.getCorrectCount();
        practiceCount++;
        if (correct) {
            correctCount++;
        }

        double correctRate = practiceCount > 0 ? (double) correctCount / practiceCount : 0;
        int newScore = (int) Math.round(correctRate * 100);
        int newLevel;
        if (newScore >= 90) {
            newLevel = 3;
        } else if (newScore >= 70) {
            newLevel = 2;
        } else if (newScore >= 40) {
            newLevel = 1;
        } else {
            newLevel = 0;
        }

        mastery.setPracticeCount(practiceCount);
        mastery.setCorrectCount(correctCount);
        mastery.setScore(newScore);
        mastery.setMasteryLevel(newLevel);
        mastery.setLastPracticeTime(LocalDateTime.now());
        knowledgeMasteryMapper.updateUserKnowledgeMastery(mastery);
    }

    @Override
    public Map<String, Object> getWeeklyReport(Long userId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);
        return buildPeriodReport(userId, startDate, endDate, "weekly");
    }

    @Override
    public Map<String, Object> getMonthlyReport(Long userId, Integer year, Integer month) {
        LocalDate now = LocalDate.now();
        int targetYear = year != null ? year : now.getYear();
        int targetMonth = month != null ? month : now.getMonthValue();

        LocalDate startDate = LocalDate.of(targetYear, targetMonth, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        return buildPeriodReport(userId, startDate, endDate, "monthly");
    }

    @Override
    public List<Map<String, Object>> getKnowledgeMasteryDistribution(Long userId) {
        return knowledgeMasteryMapper.selectKnowledgeMasteryDistribution(userId);
    }

    @Override
    public List<Map<String, Object>> getGlobalKnowledgeMasteryDistribution() {
        return knowledgeMasteryMapper.selectGlobalKnowledgeMasteryDistribution();
    }

    @Override
    public List<Map<String, Object>> getHighFrequencyErrors(Long userId, Integer limit) {
        return knowledgeMasteryMapper.selectHighFrequencyErrors(userId, limit);
    }

    @Override
    public List<Map<String, Object>> getWeaknessAnalysis(Long userId) {
        Map<Long, KnowledgePoint> knowledgePointMap = loadKnowledgePointMap();
        List<UserKnowledgeMastery> weakPoints = knowledgeMasteryMapper.selectUserWeakKnowledgePoints(userId, 2);
        return buildWeakPointItems(weakPoints, knowledgePointMap, Integer.MAX_VALUE);
    }

    @Override
    public List<Problem> getRecommendedProblemsForWeakness(Long userId, Long knowledgeId, Integer limit) {
        KnowledgePoint knowledgePoint = knowledgeMasteryMapper.selectKnowledgePointById(knowledgeId);
        if (knowledgePoint == null || knowledgePoint.getName() == null || knowledgePoint.getName().isBlank()) {
            return new ArrayList<>();
        }

        int safeLimit = limit != null && limit > 0 ? limit : 5;
        List<Problem> candidates = problemMapper.findByKnowledgePoint(knowledgePoint.getName(), safeLimit * 2);
        Set<Long> passedProblemIds = new LinkedHashSet<>(submitMapper.findPassedProblemIdsByUserId(userId));
        List<Problem> results = new ArrayList<>();

        for (Problem problem : candidates) {
            if (problem == null || problem.getId() == null || passedProblemIds.contains(problem.getId())) {
                continue;
            }
            results.add(problem);
            if (results.size() >= safeLimit) {
                break;
            }
        }

        return results;
    }

    @Override
    public List<Map<String, Object>> getKnowledgePointWithProblemCount() {
        return knowledgeMasteryMapper.selectKnowledgePointWithProblemCount();
    }

    private Map<Long, UserKnowledgeMastery> loadMasteryMap(Long userId) {
        Map<Long, UserKnowledgeMastery> masteryMap = new HashMap<>();
        if (userId == null) {
            return masteryMap;
        }

        for (UserKnowledgeMastery mastery : knowledgeMasteryMapper.selectUserKnowledgeMasteriesByUserId(userId)) {
            if (mastery != null && mastery.getKnowledgeId() != null) {
                masteryMap.put(mastery.getKnowledgeId(), mastery);
            }
        }
        return masteryMap;
    }

    private Map<String, Object> buildKnowledgeNode(KnowledgePoint knowledgePoint, UserKnowledgeMastery mastery) {
        Map<String, Object> nodeInfo = new HashMap<>();
        String category = resolveCategory(knowledgePoint);
        Integer masteryScore = resolveMasteryScore(mastery);

        nodeInfo.put("id", knowledgePoint.getId());
        nodeInfo.put("name", knowledgePoint.getName());
        nodeInfo.put("description", knowledgePoint.getDescription());
        nodeInfo.put("parentId", knowledgePoint.getParentId());
        nodeInfo.put("level", knowledgePoint.getLevel());
        nodeInfo.put("difficulty", knowledgePoint.getDifficulty());
        nodeInfo.put("domain", category);
        nodeInfo.put("category", category);
        nodeInfo.put("importance", knowledgePoint.getImportance() != null ? knowledgePoint.getImportance() : 1);
        nodeInfo.put("hasMasteryData", mastery != null);
        nodeInfo.put("masteryScore", masteryScore);
        nodeInfo.put("mastery", masteryScore == null ? null : masteryScore / 100.0);
        nodeInfo.put("practiceCount", mastery == null || mastery.getPracticeCount() == null ? 0 : mastery.getPracticeCount());
        nodeInfo.put("correctCount", mastery == null || mastery.getCorrectCount() == null ? 0 : mastery.getCorrectCount());
        nodeInfo.put("accuracy", calculateAccuracy(mastery));
        nodeInfo.put("lastPracticeTime", mastery == null ? null : mastery.getLastPracticeTime());
        return nodeInfo;
    }

    private String resolveCategory(KnowledgePoint knowledgePoint) {
        if (knowledgePoint == null || knowledgePoint.getDomain() == null || knowledgePoint.getDomain().isBlank()) {
            return "其他";
        }
        return knowledgePoint.getDomain().trim();
    }

    private boolean matchesDomain(KnowledgePoint knowledgePoint, String domain) {
        if (domain == null || domain.isBlank()) {
            return true;
        }
        return resolveCategory(knowledgePoint).equalsIgnoreCase(domain.trim());
    }

    private boolean matchesMasteryFilter(UserKnowledgeMastery mastery, int minMastery, int maxMastery) {
        Integer masteryScore = resolveMasteryScore(mastery);
        if (masteryScore == null) {
            return minMastery <= 0 && maxMastery >= 100;
        }
        return masteryScore >= minMastery && masteryScore <= maxMastery;
    }

    private Integer resolveMasteryScore(UserKnowledgeMastery mastery) {
        if (mastery == null) {
            return null;
        }
        if (mastery.getScore() != null) {
            return normalizeMasteryBound(mastery.getScore(), 0);
        }

        int practiceCount = mastery.getPracticeCount() == null ? 0 : mastery.getPracticeCount();
        int correctCount = mastery.getCorrectCount() == null ? 0 : mastery.getCorrectCount();
        if (practiceCount > 0) {
            return normalizeMasteryBound((int) Math.round(correctCount * 100.0 / practiceCount), 0);
        }

        if (mastery.getMasteryLevel() == null) {
            return 0;
        }
        if (mastery.getMasteryLevel() >= 3) {
            return 90;
        }
        if (mastery.getMasteryLevel() == 2) {
            return 75;
        }
        if (mastery.getMasteryLevel() == 1) {
            return 50;
        }
        return 25;
    }

    private Double calculateAccuracy(UserKnowledgeMastery mastery) {
        if (mastery == null) {
            return null;
        }
        int practiceCount = mastery.getPracticeCount() == null ? 0 : mastery.getPracticeCount();
        int correctCount = mastery.getCorrectCount() == null ? 0 : mastery.getCorrectCount();
        if (practiceCount <= 0) {
            return null;
        }
        return correctCount / (double) practiceCount;
    }

    private int normalizeMasteryBound(Integer mastery, int defaultValue) {
        if (mastery == null) {
            return defaultValue;
        }
        return Math.max(0, Math.min(100, mastery));
    }

    private Map<String, Object> buildPeriodReport(Long userId, LocalDate startDate, LocalDate endDate, String periodType) {
        Map<String, Object> report = new HashMap<>();
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endExclusive = endDate.plusDays(1).atStartOfDay();

        List<Submit> periodSubmits = new ArrayList<>();
        for (Submit submit : loadUserSubmits(userId)) {
            if (isWithinRange(submit.getCreateTime(), startTime, endExclusive)) {
                periodSubmits.add(submit);
            }
        }

        int submissionCount = periodSubmits.size();
        int correctCount = 0;
        Set<Long> solvedProblemIds = new LinkedHashSet<>();
        Set<Long> attemptedProblemIds = new LinkedHashSet<>();
        for (Submit submit : periodSubmits) {
            if (submit.getProblemId() != null) {
                attemptedProblemIds.add(submit.getProblemId());
            }
            if (submit.getResult() != null && submit.getResult() == 0) {
                correctCount++;
                if (submit.getProblemId() != null) {
                    solvedProblemIds.add(submit.getProblemId());
                }
            }
        }

        Map<Long, KnowledgePoint> knowledgePointMap = loadKnowledgePointMap();
        List<UserKnowledgeMastery> allMasteries = knowledgeMasteryMapper.selectUserKnowledgeMasteriesByUserId(userId);
        int newMasteredKnowledge = 0;
        for (UserKnowledgeMastery mastery : allMasteries) {
            if (mastery == null || mastery.getMasteryLevel() == null || mastery.getMasteryLevel() < 2) {
                continue;
            }
            if (isWithinRange(mastery.getLastPracticeTime(), startTime, endExclusive)) {
                newMasteredKnowledge++;
            }
        }

        List<UserKnowledgeMastery> weakMasteries = knowledgeMasteryMapper.selectUserWeakKnowledgePoints(userId, 2);
        List<Map<String, Object>> weakPoints = buildWeakPointItems(weakMasteries, knowledgePointMap, 5);
        double correctRate = submissionCount > 0 ? roundToOneDecimal(correctCount * 100.0 / submissionCount) : 0.0;

        User user = userMapper.findById(userId);
        int cumulativeStudyHours = user != null && user.getStudyHours() != null ? user.getStudyHours() : 0;

        report.put("periodType", periodType);
        report.put("startDate", startDate.format(REPORT_DATE_FORMAT));
        report.put("endDate", endDate.format(REPORT_DATE_FORMAT));
        report.put("submissionCount", submissionCount);
        report.put("problemCount", attemptedProblemIds.size());
        report.put("solvedProblems", solvedProblemIds.size());
        report.put("correctCount", correctCount);
        report.put("correctRate", correctRate);
        report.put("accuracy", (int) Math.round(correctRate));
        report.put("newMasteredKnowledge", newMasteredKnowledge);
        report.put("studyTime", "No tracked study duration");
        report.put("studyTimeTracked", false);
        report.put("cumulativeStudyHours", cumulativeStudyHours);
        report.put("weakPoints", weakPoints);
        report.put("insight", buildInsight(submissionCount, solvedProblemIds.size(), correctRate, weakPoints));
        report.put("suggestions", buildSuggestions(submissionCount, correctRate, weakPoints, newMasteredKnowledge));
        report.put("dailyProgress", buildDailyProgress(periodSubmits, startDate, endDate));
        report.put("hasRealData", submissionCount > 0 || !allMasteries.isEmpty());
        return report;
    }

    private List<Submit> loadUserSubmits(Long userId) {
        int totalCount = submitMapper.countByUserId(userId, null);
        return submitMapper.findByUserId(userId, null, 0, totalCount > 0 ? totalCount : 1);
    }

    private boolean isWithinRange(LocalDateTime target, LocalDateTime startInclusive, LocalDateTime endExclusive) {
        return target != null && !target.isBefore(startInclusive) && target.isBefore(endExclusive);
    }

    private double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private Map<Long, KnowledgePoint> loadKnowledgePointMap() {
        Map<Long, KnowledgePoint> knowledgePointMap = new HashMap<>();
        for (KnowledgePoint knowledgePoint : knowledgeMasteryMapper.selectAllKnowledgePoints()) {
            if (knowledgePoint != null && knowledgePoint.getId() != null) {
                knowledgePointMap.put(knowledgePoint.getId(), knowledgePoint);
            }
        }
        return knowledgePointMap;
    }

    private List<Map<String, Object>> buildWeakPointItems(List<UserKnowledgeMastery> weakMasteries,
                                                          Map<Long, KnowledgePoint> knowledgePointMap,
                                                          int limit) {
        List<Map<String, Object>> analysis = new ArrayList<>();
        for (UserKnowledgeMastery mastery : weakMasteries) {
            if (analysis.size() >= limit || mastery == null || mastery.getKnowledgeId() == null) {
                continue;
            }

            KnowledgePoint knowledgePoint = knowledgePointMap.get(mastery.getKnowledgeId());
            if (knowledgePoint == null) {
                continue;
            }

            int practiceCount = mastery.getPracticeCount() == null ? 0 : mastery.getPracticeCount();
            int correctCount = mastery.getCorrectCount() == null ? 0 : mastery.getCorrectCount();
            Map<String, Object> item = new HashMap<>();
            item.put("knowledgeId", knowledgePoint.getId());
            item.put("knowledgeName", knowledgePoint.getName());
            item.put("knowledgePoint", knowledgePoint.getName());
            item.put("masteryLevel", mastery.getMasteryLevel());
            item.put("score", mastery.getScore() == null ? 0 : mastery.getScore());
            item.put("practiceCount", practiceCount);
            item.put("correctCount", correctCount);
            item.put("accuracy", practiceCount > 0 ? roundToOneDecimal(correctCount * 100.0 / practiceCount) : 0.0);
            analysis.add(item);
        }
        return analysis;
    }

    private List<Map<String, Object>> buildDailyProgress(List<Submit> periodSubmits, LocalDate startDate, LocalDate endDate) {
        LinkedHashMap<LocalDate, Integer> submissionsByDay = new LinkedHashMap<>();
        LinkedHashMap<LocalDate, Integer> correctByDay = new LinkedHashMap<>();
        LinkedHashMap<LocalDate, Set<Long>> solvedByDay = new LinkedHashMap<>();

        for (LocalDate current = startDate; !current.isAfter(endDate); current = current.plusDays(1)) {
            submissionsByDay.put(current, 0);
            correctByDay.put(current, 0);
            solvedByDay.put(current, new LinkedHashSet<>());
        }

        for (Submit submit : periodSubmits) {
            if (submit.getCreateTime() == null) {
                continue;
            }

            LocalDate submitDate = submit.getCreateTime().toLocalDate();
            if (!submissionsByDay.containsKey(submitDate)) {
                continue;
            }

            submissionsByDay.put(submitDate, submissionsByDay.get(submitDate) + 1);
            if (submit.getResult() != null && submit.getResult() == 0) {
                correctByDay.put(submitDate, correctByDay.get(submitDate) + 1);
                if (submit.getProblemId() != null) {
                    solvedByDay.get(submitDate).add(submit.getProblemId());
                }
            }
        }

        List<Map<String, Object>> dailyProgress = new ArrayList<>();
        for (Map.Entry<LocalDate, Integer> entry : submissionsByDay.entrySet()) {
            LocalDate date = entry.getKey();
            int submissions = entry.getValue();
            int correct = correctByDay.get(date);

            Map<String, Object> item = new HashMap<>();
            item.put("date", date.format(REPORT_DATE_FORMAT));
            item.put("submissions", submissions);
            item.put("solved", solvedByDay.get(date).size());
            item.put("accuracy", submissions > 0 ? (int) Math.round(correct * 100.0 / submissions) : 0);
            dailyProgress.add(item);
        }

        return dailyProgress;
    }

    private String buildInsight(int submissionCount, int solvedProblems, double correctRate, List<Map<String, Object>> weakPoints) {
        if (submissionCount == 0) {
            return "No new submissions were recorded in this period.";
        }

        if (weakPoints.isEmpty()) {
            return String.format("You made %d submissions, solved %d problems, and reached %.1f%% accuracy.", submissionCount, solvedProblems, correctRate);
        }

        List<String> names = new ArrayList<>();
        for (Map<String, Object> weakPoint : weakPoints) {
            Object knowledgeName = weakPoint.get("knowledgeName");
            if (knowledgeName != null) {
                names.add(String.valueOf(knowledgeName));
            }
            if (names.size() >= 2) {
                break;
            }
        }

        return String.format(
                "You made %d submissions, solved %d problems, and reached %.1f%% accuracy. Priority weak areas: %s.",
                submissionCount,
                solvedProblems,
                correctRate,
                names.isEmpty() ? "none identified" : String.join(", ", names)
        );
    }

    private List<String> buildSuggestions(int submissionCount,
                                          double correctRate,
                                          List<Map<String, Object>> weakPoints,
                                          int newMasteredKnowledge) {
        List<String> suggestions = new ArrayList<>();

        if (submissionCount == 0) {
            suggestions.add("Start a new practice session to generate period analytics.");
        } else if (correctRate < 60) {
            suggestions.add("Review wrong submissions first and focus on basic and medium problems.");
        } else if (correctRate < 80) {
            suggestions.add("Keep current practice frequency and add targeted review for unstable topics.");
        } else {
            suggestions.add("Your recent accuracy is stable. Increase variety to avoid topic concentration.");
        }

        if (!weakPoints.isEmpty()) {
            List<String> weakNames = new ArrayList<>();
            for (Map<String, Object> weakPoint : weakPoints) {
                Object knowledgeName = weakPoint.get("knowledgeName");
                if (knowledgeName != null) {
                    weakNames.add(String.valueOf(knowledgeName));
                }
                if (weakNames.size() >= 3) {
                    break;
                }
            }
            suggestions.add("Prioritize these weak knowledge points: " + String.join(", ", weakNames));
        }

        if (newMasteredKnowledge == 0 && submissionCount > 0) {
            suggestions.add("Try consecutive practice on one topic to improve mastery progression.");
        }

        if (suggestions.isEmpty()) {
            suggestions.add("Keep a steady pace and continue reviewing the wrong-book.");
        }

        return suggestions;
    }
}
