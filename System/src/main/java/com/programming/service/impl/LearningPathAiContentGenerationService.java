package com.programming.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.programming.entity.LearningPath;
import com.programming.entity.LearningResource;
import com.programming.entity.PathChapter;
import com.programming.entity.PathLevel;
import com.programming.entity.PathLevelProblem;
import com.programming.entity.Problem;
import com.programming.mapper.LearningPathMapper;
import com.programming.mapper.LearningResourceMapper;
import com.programming.mapper.PathLevelProblemMapper;
import com.programming.mapper.ProblemMapper;
import com.programming.service.ai.AiLlmClient;
import com.programming.service.ai.AiLlmConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LearningPathAiContentGenerationService {

    private static final int MAX_CANDIDATES = 12;
    private static final int MAX_RECOMMENDED_PROBLEMS = 5;

    @Autowired
    private LearningPathMapper learningPathMapper;
    @Autowired
    private LearningResourceMapper learningResourceMapper;
    @Autowired
    private PathLevelProblemMapper pathLevelProblemMapper;
    @Autowired
    private ProblemMapper problemMapper;
    @Autowired
    private AiLlmClient aiLlmClient;
    @Autowired(required = false)
    private TransactionTemplate transactionTemplate;

    @Value("${programming.ai.provider:openai-compatible}")
    private String aiProvider;
    @Value("${programming.ai.model:deepseek-v4-flash}")
    private String aiModel;
    @Value("${programming.ai.ollama-url:http://127.0.0.1:11434}")
    private String ollamaUrl;
    @Value("${programming.ai.api-url:https://api.deepseek.com/v1}")
    private String apiUrl;
    @Value("${programming.ai.api-key:}")
    private String apiKey;

    public GenerationSummary generate(Long pathId, Long levelId) {
        GenerationSummary summary = new GenerationSummary();
        for (LevelTarget target : resolveTargets(pathId, levelId)) {
            try {
                generateOne(target);
                summary.successCount++;
            } catch (Exception error) {
                String failure = "levelId=" + target.level.getId() + ": " + error.getMessage();
                summary.failures.add(failure);
                log.warn("Learning path AI content generation skipped {}", failure);
                log.debug("Learning path AI content generation failure detail", error);
            }
        }
        return summary;
    }

    protected void generateOne(LevelTarget target) throws IOException {
        List<CandidateProblem> candidates = buildCandidates(target.path, target.level);
        if (candidates.isEmpty()) {
            throw new IllegalStateException("no same-language candidate problems");
        }

        JSONObject payload = requestGeneration(target, candidates);
        List<Long> problemIds = validatedProblemIds(payload, candidates);
        if (problemIds.isEmpty()) {
            problemIds.add(candidates.get(0).problem.getId());
        }

        persistGeneratedContent(target, payload, problemIds, candidates);
    }

    private void persistGeneratedContent(LevelTarget target,
                                         JSONObject payload,
                                         List<Long> problemIds,
                                         List<CandidateProblem> candidates) {
        if (transactionTemplate == null) {
            persistGeneratedContentInternal(target, payload, problemIds, candidates);
            return;
        }
        transactionTemplate.executeWithoutResult(status ->
                persistGeneratedContentInternal(target, payload, problemIds, candidates)
        );
    }

    private void persistGeneratedContentInternal(LevelTarget target,
                                                 JSONObject payload,
                                                 List<Long> problemIds,
                                                 List<CandidateProblem> candidates) {
        String guideName = generatedName(target.path, "学习指引");
        String practiceName = generatedName(target.path, "练习建议");
        deleteGeneratedResourceNames(target, "tutorial", guideName, "学习指引");
        deleteGeneratedResourceNames(target, "example", practiceName, "练习建议");
        learningResourceMapper.insertResource(buildGuideResource(target, payload.getJSONObject("guide"), guideName));
        learningResourceMapper.insertResource(buildPracticeResource(target, payload.getJSONObject("practice"), practiceName, problemIds, candidates));

        pathLevelProblemMapper.deleteByLevelId(target.level.getId());
        pathLevelProblemMapper.batchInsert(toBindings(target.level.getId(), problemIds));
        PathLevel update = new PathLevel();
        update.setId(target.level.getId());
        update.setProblemIds(problemIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
        learningPathMapper.updateLevel(update);
    }

    private void deleteGeneratedResourceNames(LevelTarget target, String type, String pathScopedName, String suffix) {
        learningResourceMapper.deleteByLevelIdAndTypeAndName(target.level.getId(), type, pathScopedName);
        String levelScopedName = firstNonBlank(target.level.getName(), "关卡") + " " + suffix;
        if (!levelScopedName.equals(pathScopedName)) {
            learningResourceMapper.deleteByLevelIdAndTypeAndName(target.level.getId(), type, levelScopedName);
        }
    }

    private List<LevelTarget> resolveTargets(Long pathId, Long levelId) {
        List<LevelTarget> targets = new ArrayList<>();
        if (levelId != null) {
            PathLevel level = learningPathMapper.selectLevelById(levelId);
            if (level == null) {
                throw new IllegalArgumentException("level not found: " + levelId);
            }
            PathChapter chapter = learningPathMapper.selectChapterById(level.getChapterId());
            if (chapter == null) {
                throw new IllegalArgumentException("chapter not found for level: " + levelId);
            }
            LearningPath path = learningPathMapper.selectPathById(chapter.getPathId());
            if (path == null) {
                throw new IllegalArgumentException("path not found for level: " + levelId);
            }
            if (pathId != null && !pathId.equals(path.getId())) {
                throw new IllegalArgumentException("level does not belong to path: " + levelId);
            }
            targets.add(new LevelTarget(path, chapter, level));
            return targets;
        }

        List<LearningPath> paths = pathId == null ? learningPathMapper.selectAllPaths() : List.of(learningPathMapper.selectPathById(pathId));
        for (LearningPath path : paths) {
            if (path == null || path.getId() == null) {
                continue;
            }
            for (PathChapter chapter : safeList(learningPathMapper.selectChaptersByPathId(path.getId()))) {
                for (PathLevel level : safeList(learningPathMapper.selectLevelsByChapterId(chapter.getId()))) {
                    targets.add(new LevelTarget(path, chapter, level));
                }
            }
        }
        return targets;
    }

    private JSONObject requestGeneration(LevelTarget target, List<CandidateProblem> candidates) throws IOException {
        List<JSONObject> messages = new ArrayList<>();
        messages.add(message("system", systemPrompt()));
        messages.add(message("user", userPrompt(target, candidates)));
        String raw = aiLlmClient.chat(currentLlmConfig(), messages, 0.2);
        return parsePayload(raw);
    }

    private JSONObject parsePayload(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("AI returned empty content");
        }
        String text = raw.trim();
        if (text.startsWith("```")) {
            text = text.replaceFirst("^```[a-zA-Z]*\\s*", "");
            int fenceEnd = text.lastIndexOf("```");
            if (fenceEnd >= 0) {
                text = text.substring(0, fenceEnd).trim();
            }
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end < start) {
            throw new IllegalArgumentException("AI JSON object not found");
        }
        JSONObject payload = JSON.parseObject(text.substring(start, end + 1));
        if (payload.getJSONObject("guide") == null || payload.getJSONObject("practice") == null) {
            throw new IllegalArgumentException("AI payload must contain guide and practice");
        }
        return payload;
    }

    private List<CandidateProblem> buildCandidates(LearningPath path, PathLevel level) {
        Set<String> levelPoints = textSet(level.getKnowledgePoints());
        String pathLanguage = normalize(path.getLanguage());
        List<CandidateProblem> matchedCandidates = new ArrayList<>();
        List<CandidateProblem> sameLanguageCandidates = new ArrayList<>();
        for (Problem problem : safeList(problemMapper.selectAll())) {
            if (problem == null || problem.getId() == null) {
                continue;
            }
            if (!isPublished(problem)) {
                continue;
            }
            if (!languageMatches(pathLanguage, problem.getLanguage())) {
                continue;
            }
            Set<String> problemPoints = textSet(problem.getKnowledgePoints());
            int score = matchScore(levelPoints, problemPoints);
            sameLanguageCandidates.add(new CandidateProblem(problem, Math.max(score, 0)));
            if (score > 0) {
                matchedCandidates.add(new CandidateProblem(problem, score));
            }
        }
        List<CandidateProblem> candidates = matchedCandidates.isEmpty() ? sameLanguageCandidates : matchedCandidates;
        candidates.sort(Comparator
                .comparingInt(CandidateProblem::score).reversed()
                .thenComparing(candidate -> safeInt(candidate.problem.getDifficulty()))
                .thenComparing(candidate -> candidate.problem.getId()));
        return candidates.size() > MAX_CANDIDATES ? new ArrayList<>(candidates.subList(0, MAX_CANDIDATES)) : candidates;
    }

    private boolean isPublished(Problem problem) {
        String status = normalize(problem.getStatus());
        return status.isBlank() || "published".equals(status);
    }

    private List<Long> validatedProblemIds(JSONObject payload, List<CandidateProblem> candidates) {
        Map<Long, CandidateProblem> candidateById = candidates.stream()
                .collect(Collectors.toMap(candidate -> candidate.problem.getId(), candidate -> candidate, (left, right) -> left, LinkedHashMap::new));
        LinkedHashSet<Long> result = new LinkedHashSet<>();
        JSONArray ids = payload.getJSONObject("practice").getJSONArray("recommended_problem_ids");
        if (ids != null) {
            for (Object rawId : ids) {
                Long id = toLong(rawId);
                if (id != null && candidateById.containsKey(id)) {
                    result.add(id);
                }
                if (result.size() >= MAX_RECOMMENDED_PROBLEMS) {
                    break;
                }
            }
        }
        for (CandidateProblem candidate : candidates) {
            if (result.size() >= Math.min(MAX_RECOMMENDED_PROBLEMS, candidates.size())) {
                break;
            }
            result.add(candidate.problem.getId());
        }
        return new ArrayList<>(result);
    }

    private LearningResource buildGuideResource(LevelTarget target, JSONObject guide, String name) {
        LearningResource resource = baseResource(target, name, "tutorial", 1);
        resource.setDescription(firstNonBlank(guide.getString("summary"), "用于快速掌握“" + target.level.getName() + "”的学习指引"));
        resource.setUrl(markdown(
                firstNonBlank(guide.getString("title"), name),
                section("学习目标", stringList(guide.getJSONArray("learning_objectives"))),
                section("关键知识点", stringList(guide.getJSONArray("key_points"))),
                section("学习步骤", stringList(guide.getJSONArray("study_steps"))),
                section("完成标准", stringList(guide.getJSONArray("completion_criteria")))
        ));
        return resource;
    }

    private LearningResource buildPracticeResource(LevelTarget target,
                                                   JSONObject practice,
                                                   String name,
                                                   List<Long> problemIds,
                                                   List<CandidateProblem> candidates) {
        LearningResource resource = baseResource(target, name, "example", 2);
        resource.setDescription(firstNonBlank(practice.getString("summary"), "围绕“" + target.level.getName() + "”的练习与复盘建议"));
        Map<Long, Problem> problemById = candidates.stream()
                .map(candidate -> candidate.problem)
                .collect(Collectors.toMap(Problem::getId, problem -> problem, (left, right) -> left));
        List<String> recommended = new ArrayList<>();
        for (Long problemId : problemIds) {
            Problem problem = problemById.get(problemId);
            recommended.add(problemId + ". " + (problem == null ? "推荐题目" : problem.getTitle()));
        }
        resource.setUrl(markdown(
                firstNonBlank(practice.getString("title"), name),
                section("推荐题目", recommended),
                section("练习顺序", stringList(practice.getJSONArray("practice_order"))),
                section("易错提醒", stringList(practice.getJSONArray("pitfalls"))),
                section("通过标准", stringList(practice.getJSONArray("pass_criteria")))
        ));
        return resource;
    }

    private LearningResource baseResource(LevelTarget target, String name, String type, int orderNum) {
        LearningResource resource = new LearningResource();
        resource.setLevelId(target.level.getId());
        resource.setName(name);
        resource.setType(type);
        resource.setKnowledgePoints(target.level.getKnowledgePoints());
        resource.setOrderNum(orderNum);
        resource.setVisibility(0);
        return resource;
    }

    private List<PathLevelProblem> toBindings(Long levelId, List<Long> problemIds) {
        List<PathLevelProblem> bindings = new ArrayList<>();
        for (int i = 0; i < problemIds.size(); i++) {
            PathLevelProblem binding = new PathLevelProblem();
            binding.setLevelId(levelId);
            binding.setProblemId(problemIds.get(i));
            binding.setOrderNum(i + 1);
            bindings.add(binding);
        }
        return bindings;
    }

    private String systemPrompt() {
        return """
                你是编程学习路径内容生成器。必须只返回一个合法 JSON 对象，不要 Markdown 代码块，不要额外解释。
                只能基于输入的学习路径、关卡、知识点和候选题目生成内容，禁止编造题目 ID。
                JSON 模板：
                {
                  "guide": {
                    "title": "string",
                    "summary": "string",
                    "learning_objectives": ["string"],
                    "key_points": ["string"],
                    "study_steps": ["string"],
                    "completion_criteria": ["string"]
                  },
                  "practice": {
                    "title": "string",
                    "summary": "string",
                    "recommended_problem_ids": [1],
                    "practice_order": ["string"],
                    "pitfalls": ["string"],
                    "pass_criteria": ["string"]
                  }
                }
                """;
    }

    private String userPrompt(LevelTarget target, List<CandidateProblem> candidates) {
        JSONObject payload = new JSONObject();
        payload.put("path", Map.of(
                "id", target.path.getId(),
                "name", target.path.getName(),
                "language", target.path.getLanguage(),
                "direction", target.path.getDirection(),
                "description", firstNonBlank(target.path.getDescription(), "")
        ));
        payload.put("chapter", Map.of(
                "id", target.chapter.getId(),
                "name", firstNonBlank(target.chapter.getName(), "")
        ));
        payload.put("level", Map.of(
                "id", target.level.getId(),
                "name", firstNonBlank(target.level.getName(), ""),
                "knowledge_points", firstNonBlank(target.level.getKnowledgePoints(), ""),
                "unlock_condition", firstNonBlank(target.level.getUnlockCondition(), "")
        ));
        List<Map<String, Object>> candidateViews = new ArrayList<>();
        for (CandidateProblem candidate : candidates) {
            candidateViews.add(Map.of(
                    "id", candidate.problem.getId(),
                    "title", firstNonBlank(candidate.problem.getTitle(), ""),
                    "difficulty", safeInt(candidate.problem.getDifficulty()),
                    "language", firstNonBlank(candidate.problem.getLanguage(), ""),
                    "knowledge_points", firstNonBlank(candidate.problem.getKnowledgePoints(), ""),
                    "match_score", candidate.score
            ));
        }
        payload.put("candidate_problems", candidateViews);
        return "请按模板生成该关卡的学习指引和练习建议，并从 candidate_problems 中选择推荐题目 ID：\n"
                + payload.toJSONString();
    }

    private String markdown(String title, String... sections) {
        StringBuilder builder = new StringBuilder("# ").append(title).append("\n\n");
        for (String section : sections) {
            if (section != null && !section.isBlank()) {
                builder.append(section).append("\n");
            }
        }
        return builder.toString().trim();
    }

    private String section(String title, List<String> items) {
        if (items == null || items.isEmpty()) {
            return "";
        }
        return "## " + title + "\n" + items.stream().map(item -> "- " + item).collect(Collectors.joining("\n")) + "\n";
    }

    private List<String> stringList(JSONArray array) {
        if (array == null || array.isEmpty()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (Object item : array) {
            if (item != null && !String.valueOf(item).isBlank()) {
                result.add(String.valueOf(item).trim());
            }
        }
        return result;
    }

    private Set<String> textSet(String value) {
        Set<String> result = new HashSet<>();
        if (value == null || value.isBlank()) {
            return result;
        }
        for (String part : value.split("[,，;；\\s]+")) {
            String normalized = normalize(part);
            if (!normalized.isBlank() && !"null".equals(normalized)) {
                result.add(normalized);
            }
        }
        return result;
    }

    private int matchScore(Set<String> levelPoints, Set<String> problemPoints) {
        if (levelPoints.isEmpty() || problemPoints.isEmpty()) {
            return 0;
        }
        int score = 0;
        for (String levelPoint : levelPoints) {
            for (String problemPoint : problemPoints) {
                if (levelPoint.equals(problemPoint) || levelPoint.contains(problemPoint) || problemPoint.contains(levelPoint)) {
                    score++;
                }
            }
        }
        return score;
    }

    private boolean languageMatches(String pathLanguage, String problemLanguage) {
        return pathLanguage.isBlank()
                || "all".equals(pathLanguage)
                || pathLanguage.equals(normalize(problemLanguage));
    }

    private String generatedName(LearningPath path, String suffix) {
        return firstNonBlank(path.getName(), "学习路径") + " " + suffix;
    }

    private AiLlmConfig currentLlmConfig() {
        return new AiLlmConfig(aiProvider, aiModel, ollamaUrl, apiUrl, apiKey);
    }

    private JSONObject message(String role, String content) {
        JSONObject message = new JSONObject();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }

    private record LevelTarget(LearningPath path, PathChapter chapter, PathLevel level) {
    }

    private record CandidateProblem(Problem problem, int score) {
    }

    @Data
    public static class GenerationSummary {
        private int successCount;
        private List<String> failures = new ArrayList<>();

        public int getFailureCount() {
            return failures.size();
        }
    }
}
