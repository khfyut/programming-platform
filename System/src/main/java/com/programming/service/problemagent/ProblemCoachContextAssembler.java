package com.programming.service.problemagent;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.programming.entity.AiSession;
import com.programming.entity.KnowledgePoint;
import com.programming.entity.Problem;
import com.programming.entity.Submit;
import com.programming.entity.WrongBookItem;
import com.programming.mapper.AiSessionMapper;
import com.programming.mapper.ProblemMapper;
import com.programming.mapper.SubmitMapper;
import com.programming.service.LearnService;
import com.programming.service.WrongBookService;
import com.programming.vo.problemagent.ProblemAgentChatRequestVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class ProblemCoachContextAssembler {

    @Autowired
    private ProblemMapper problemMapper;

    @Autowired
    private SubmitMapper submitMapper;

    @Autowired
    private LearnService learnService;

    @Autowired
    private WrongBookService wrongBookService;

    @Autowired
    private AiSessionMapper aiSessionMapper;

    public ProblemCoachContext assemble(Long userId, ProblemAgentChatRequestVO request) {
        if (request == null || request.getProblemId() == null) {
            throw new RuntimeException("problemId is required");
        }

        Problem problem = problemMapper.findById(request.getProblemId());
        if (problem == null) {
            throw new RuntimeException("题目不存在");
        }

        ProblemCoachContext context = new ProblemCoachContext();
        context.setUserId(userId);
        context.setCurrentProblem(problem);
        context.setCurrentCode(request.getCode());
        context.setLanguage(request.getLanguage());
        context.setTriggerType(normalizeTriggerType(request.getTriggerType()));
        context.setLatestResultCode(request.getLatestResultCode());
        context.setLatestErrorMessage(request.getErrorMessage());
        context.setExecutionOutput(request.getExecutionOutput());

        if (request.getSessionId() != null && !request.getSessionId().isBlank()) {
            context.setSession(aiSessionMapper.selectSessionBySessionId(request.getSessionId()));
        } else {
            context.setSession(aiSessionMapper.selectLatestProblemCoachSession(userId, request.getProblemId()));
        }

        List<Submit> recentSubmissions = submitMapper.findByUserId(userId, request.getProblemId(), 0, 10);
        context.setRecentSubmissions(recentSubmissions == null ? new ArrayList<>() : recentSubmissions);

        Submit latestSubmit = findLatestSubmit(userId, request, context.getRecentSubmissions());
        context.setLatestSubmit(latestSubmit);
        if (latestSubmit != null && context.getLatestResultCode() == null) {
            context.setLatestResultCode(latestSubmit.getResult());
        }

        List<WrongBookItem> relatedWrongItems = wrongBookService.getWrongBookItems(userId, null, null).stream()
                .filter(item -> Objects.equals(item.getProblemId(), request.getProblemId()))
                .limit(5)
                .collect(Collectors.toList());
        context.setRelatedWrongItems(relatedWrongItems);

        Map<String, Object> recommend = learnService.getRecommend(userId);
        if (recommend == null) {
            recommend = new HashMap<>();
        }
        context.setRecommendedProblems(castProblems(recommend.get("problems")));
        context.setRecommendedLevels(castMaps(recommend.get("modules")));
        context.setWeakKnowledgePoints(toWeakKnowledgeMaps(recommend.get("weakPoints")));

        boolean hasFailedSubmit = context.getRecentSubmissions().stream()
                .anyMatch(submit -> submit != null && submit.getResult() != null && submit.getResult() != 0);
        boolean triggeredFailure = "run_failed".equals(context.getTriggerType())
                || "submit_failed".equals(context.getTriggerType())
                || (context.getLatestResultCode() != null && context.getLatestResultCode() != 0)
                || (context.getLatestErrorMessage() != null && !context.getLatestErrorMessage().isBlank());
        context.setHasFailure(hasFailedSubmit || triggeredFailure || sessionAlreadyHasFailure(context.getSession()));
        return context;
    }

    private boolean sessionAlreadyHasFailure(AiSession session) {
        if (session == null || session.getMetadataJson() == null || session.getMetadataJson().isBlank()) {
            return false;
        }
        try {
            JSONObject metadata = JSON.parseObject(session.getMetadataJson());
            return Boolean.TRUE.equals(metadata.getBoolean("hasFailure"));
        } catch (Exception ignored) {
            return false;
        }
    }

    private Submit findLatestSubmit(Long userId, ProblemAgentChatRequestVO request, List<Submit> recentSubmissions) {
        if (request.getSubmitId() != null) {
            Submit submit = submitMapper.findById(request.getSubmitId());
            if (submit != null
                    && Objects.equals(submit.getUserId(), userId)
                    && Objects.equals(submit.getProblemId(), request.getProblemId())) {
                return submit;
            }
        }
        return recentSubmissions == null || recentSubmissions.isEmpty() ? null : recentSubmissions.get(0);
    }

    @SuppressWarnings("unchecked")
    private List<Problem> castProblems(Object value) {
        if (!(value instanceof List<?> list)) {
            return Collections.emptyList();
        }
        return list.stream()
                .filter(Problem.class::isInstance)
                .map(Problem.class::cast)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> castMaps(Object value) {
        if (!(value instanceof List<?> list)) {
            return Collections.emptyList();
        }
        return list.stream()
                .filter(Map.class::isInstance)
                .map(item -> (Map<String, Object>) item)
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> toWeakKnowledgeMaps(Object value) {
        if (!(value instanceof List<?> list)) {
            return Collections.emptyList();
        }
        return list.stream()
                .map(item -> {
                    if (item instanceof KnowledgePoint point) {
                        Map<String, Object> result = new LinkedHashMap<>();
                        result.put("knowledgeId", point.getId());
                        result.put("name", point.getName());
                        result.put("description", point.getDescription());
                        return result;
                    }
                    if (item instanceof Map<?, ?> map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> typed = (Map<String, Object>) map;
                        return typed;
                    }
                    return Map.<String, Object>of();
                })
                .collect(Collectors.toList());
    }

    private String normalizeTriggerType(String triggerType) {
        if (triggerType == null || triggerType.isBlank()) {
            return "manual";
        }
        return triggerType;
    }
}
