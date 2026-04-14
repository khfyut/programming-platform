package com.programming.service.impl;

import com.alibaba.fastjson2.JSON;
import com.programming.entity.Problem;
import com.programming.entity.ProblemSupportedLanguage;
import com.programming.entity.ReferenceSolution;
import com.programming.entity.UserBehavior;
import com.programming.mapper.ProblemMapper;
import com.programming.mapper.ProblemSupportedLanguageMapper;
import com.programming.mapper.ReferenceSolutionMapper;
import com.programming.mapper.UserBehaviorMapper;
import com.programming.service.ReferenceSolutionService;
import com.programming.service.runtime.RuntimeCatalogService;
import com.programming.vo.ReferenceSolutionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReferenceSolutionServiceImpl implements ReferenceSolutionService {
    @Autowired
    private ReferenceSolutionMapper referenceSolutionMapper;

    @Autowired
    private UserBehaviorMapper userBehaviorMapper;

    @Autowired
    private ProblemSupportedLanguageMapper problemSupportedLanguageMapper;

    @Autowired
    private ProblemMapper problemMapper;

    @Autowired
    private RuntimeCatalogService runtimeCatalogService;

    @Override
    public PermissionCheckResult checkViewPermission(Long userId, Long problemId) {
        Integer submitCount = userBehaviorMapper.countSubmitRecords(userId, problemId);
        if (submitCount == null || submitCount == 0) {
            return new PermissionCheckResult(false, "请先尝试提交代码后再查看参考答案");
        }
        return new PermissionCheckResult(true, "");
    }

    @Override
    public ReferenceSolutionVO getSolution(Long problemId, String language) {
        ReferenceSolution solution = resolveSolution(problemId, language);
        if (solution == null) {
            return null;
        }

        ReferenceSolutionVO vo = new ReferenceSolutionVO();
        vo.setProblemId(solution.getProblemId());
        vo.setLanguage(runtimeCatalogService.normalizeLanguageCode(solution.getLanguage()));
        vo.setSolutionCode(normalizeEscapedText(solution.getSolutionCode()));
        vo.setTimeComplexity(solution.getTimeComplexity());
        vo.setSpaceComplexity(solution.getSpaceComplexity());
        vo.setExplanation(normalizeEscapedText(solution.getExplanation()));
        vo.setHints(parseHints(solution.getHints()));
        vo.setAvailableLanguages(getAvailableLanguages(problemId));
        return vo;
    }

    @Override
    public void recordViewBehavior(Long userId, Long problemId) {
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setBehaviorType("VIEW_REFERENCE_SOLUTION");
        behavior.setTargetType("PROBLEM");
        behavior.setTargetId(problemId);
        behavior.setDetails("{}");
        userBehaviorMapper.insert(behavior);
    }

    @Override
    public String getHint(Long problemId, Integer hintLevel, String language) {
        ReferenceSolution solution = resolveSolution(problemId, language);
        if (solution == null || solution.getHints() == null) {
            return "暂无提示";
        }

        Map<String, String> hints = parseHints(solution.getHints());
        return hints.getOrDefault(String.valueOf(hintLevel), "暂无提示");
    }

    @Override
    public String[] getAvailableLanguages(Long problemId) {
        List<String> activeLanguages = problemSupportedLanguageMapper.findActiveLanguageCodesByProblemId(problemId);
        if (activeLanguages != null && !activeLanguages.isEmpty()) {
            return activeLanguages.stream()
                    .map(runtimeCatalogService::normalizeLanguageCode)
                    .distinct()
                    .toArray(String[]::new);
        }

        List<String> referenceLanguages = referenceSolutionMapper.findLanguagesByProblemId(problemId);
        if (referenceLanguages != null && !referenceLanguages.isEmpty()) {
            return referenceLanguages.stream()
                    .map(runtimeCatalogService::normalizeLanguageCode)
                    .distinct()
                    .toArray(String[]::new);
        }

        Problem problem = problemMapper.findById(problemId);
        if (problem != null && problem.getLanguage() != null && !problem.getLanguage().isBlank()) {
            return new String[]{runtimeCatalogService.normalizeLanguageCode(problem.getLanguage())};
        }
        return new String[0];
    }

    private ReferenceSolution resolveSolution(Long problemId, String language) {
        if (language != null && !language.isBlank()) {
            ReferenceSolution solution = referenceSolutionMapper.findByProblemIdAndLanguage(
                    problemId,
                    runtimeCatalogService.normalizeLanguageCode(language)
            );
            if (solution != null) {
                return solution;
            }
        }

        for (String availableLanguage : getAvailableLanguages(problemId)) {
            ReferenceSolution fallback = referenceSolutionMapper.findByProblemIdAndLanguage(problemId, availableLanguage);
            if (fallback != null) {
                return fallback;
            }
        }

        List<ReferenceSolution> allSolutions = referenceSolutionMapper.findByProblemId(problemId);
        return allSolutions == null || allSolutions.isEmpty() ? null : allSolutions.get(0);
    }

    private Map<String, String> parseHints(String rawHints) {
        if (rawHints == null || rawHints.isBlank()) {
            return new LinkedHashMap<>();
        }

        try {
            Map<String, String> hints = JSON.parseObject(rawHints, Map.class);
            return hints == null ? new LinkedHashMap<>() : hints;
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    private String normalizeEscapedText(String rawText) {
        if (rawText == null || rawText.isEmpty() || !rawText.contains("\\")) {
            return rawText;
        }

        String normalized = rawText
                .replace("\\r\\n", "\n")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"");

        return normalized.replaceAll("(?<!\\\\)\\\\([AbBdDsSwWZ])", "\\\\\\\\$1");
    }
}
