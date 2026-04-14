package com.programming.service;

import com.alibaba.fastjson2.JSON;
import com.programming.agent.AgentClient;
import com.programming.agent.AgentContextBuilder;
import com.programming.agent.AgentDecisionEnforcer;
import com.programming.agent.AgentExecutionResult;
import com.programming.agent.AgentProtocolConstants;
import com.programming.agent.dto.AgentContextDTO;
import com.programming.agent.dto.AgentDecisionDTO;
import com.programming.entity.LearningPath;
import com.programming.entity.LearningEventLog;
import com.programming.entity.PathChapter;
import com.programming.entity.PathLevel;
import com.programming.entity.Problem;
import com.programming.entity.Submit;
import com.programming.entity.UserProblemInteraction;
import com.programming.entity.WrongBookItem;
import com.programming.mapper.LearningEventLogMapper;
import com.programming.mapper.ProblemMapper;
import com.programming.mapper.SubmitMapper;
import com.programming.mapper.UserProblemInteractionMapper;
import com.programming.service.problemagent.ProblemCoachContext;
import com.programming.vo.problemagent.ProblemAgentChatRequestVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class AgentService {

    @Autowired
    private AgentClient agentClient;

    @Autowired
    private AgentContextBuilder contextBuilder;

    @Autowired
    private AgentDecisionEnforcer decisionEnforcer;

    @Autowired
    private SubmitMapper submitMapper;

    @Autowired
    private ProblemMapper problemMapper;

    @Autowired
    private LearningEventLogMapper learningEventLogMapper;

    @Autowired
    private UserProblemInteractionMapper userProblemInteractionMapper;

    @Autowired
    private WrongBookService wrongBookService;

    @Autowired
    private LearningService learningService;

    @Transactional
    public AgentDecisionDTO processSubmission(Long userId, Long submitId) {
        Submit submit = requireOwnedSubmit(userId, submitId);
        Problem problem = requireProblem(submit.getProblemId());
        UserProblemInteraction interaction = getOrCreateInteraction(userId, problem.getId());

        int consecutiveFailures = calculateConsecutiveFailures(userId, problem.getId(), submitId, isFailure(submit));
        interaction.setConsecutiveFailures(consecutiveFailures);
        interaction.setLastSubmitId(submitId);
        interaction.setUpdateTime(LocalDateTime.now());
        userProblemInteractionMapper.update(interaction);

        AgentContextDTO context = contextBuilder.buildForSubmission(problem, submit, interaction, consecutiveFailures);
        return decideAndApply(userId, problem.getId(), submitId, context);
    }

    @Transactional
    public AgentDecisionDTO processProblemCoach(ProblemCoachContext coachContext, ProblemAgentChatRequestVO request) {
        if (coachContext == null || coachContext.getCurrentProblem() == null) {
            throw new RuntimeException("problem context is required");
        }

        Long userId = coachContext.getUserId();
        Problem problem = coachContext.getCurrentProblem();
        UserProblemInteraction interaction = getOrCreateInteraction(userId, problem.getId());
        sanitizeStaleInteraction(interaction, userId, problem.getId());

        Submit latestSubmit = trustedSubmit(userId, problem.getId(), coachContext.getLatestSubmit());
        Long submitId = latestSubmit == null ? null : latestSubmit.getId();
        boolean triggeredFailure = isTriggeredFailure(coachContext, request, latestSubmit);
        int consecutiveFailures = calculateConsecutiveFailures(userId, problem.getId(), submitId, triggeredFailure);
        if (!triggeredFailure && submitId == null && coachContext.isHasFailure()) {
            consecutiveFailures = Math.max(consecutiveFailures, safeInt(interaction.getConsecutiveFailures()));
        }

        interaction.setConsecutiveFailures(consecutiveFailures);
        if (submitId != null) {
            interaction.setLastSubmitId(submitId);
        }
        interaction.setUpdateTime(LocalDateTime.now());
        userProblemInteractionMapper.update(interaction);

        coachContext.setLatestSubmit(latestSubmit);
        AgentContextDTO context = contextBuilder.buildForProblemCoach(coachContext, request, interaction, consecutiveFailures);
        return decideAndApply(userId, problem.getId(), submitId, context);
    }

    @Transactional
    public AgentDecisionDTO processWrongBookReflection(Long userId, Long wrongItemId) {
        WrongBookItem wrongItem = wrongBookService.getWrongBookItemById(userId, wrongItemId);
        if (wrongItem == null || wrongItem.getProblemId() == null) {
            throw new RuntimeException("Wrong book item not found");
        }

        Problem problem = requireProblem(wrongItem.getProblemId());
        UserProblemInteraction interaction = getOrCreateInteraction(userId, problem.getId());
        AgentContextDTO context = contextBuilder.buildForWrongBookEntry(problem, wrongItem, interaction);
        return decideAndApply(
                userId,
                problem.getId(),
                wrongItem.getSubmitId(),
                context,
                true,
                "WRONG_BOOK",
                wrongItemId,
                null,
                null,
                wrongItemId
        );
    }

    @Transactional
    public AgentDecisionDTO processLearningPathRecommendation(Long userId, Long pathId, Long levelId) {
        LearningPath path = learningService.getPathDetail(pathId);
        if (path == null) {
            throw new RuntimeException("Learning path not found");
        }
        PathLevel level = learningService.getLevelDetail(levelId);
        if (level == null) {
            throw new RuntimeException("Learning path level not found");
        }
        if (!levelBelongsToPath(path, levelId)) {
            throw new RuntimeException("Learning path level does not belong to path");
        }

        List<Problem> problems = learningService.getProblemsByLevelId(levelId);
        if (problems == null) {
            problems = Collections.emptyList();
        }
        AgentContextDTO context = contextBuilder.buildForLearningPathEntry(path, level, problems, null);
        return decideAndApply(
                userId,
                null,
                null,
                context,
                false,
                "LEARNING_PATH_LEVEL",
                levelId,
                pathId,
                levelId,
                null
        );
    }

    public Map<String, Object> getAgentReportSummary(Long userId) {
        List<LearningEventLog> events = learningEventLogMapper.findRecentByUser(userId, 30);
        if (events == null) {
            events = Collections.emptyList();
        }
        List<UserProblemInteraction> interactions = userProblemInteractionMapper.findRecentByUser(userId, 20);
        if (interactions == null) {
            interactions = Collections.emptyList();
        }

        Map<String, Long> actionCounts = new LinkedHashMap<>();
        Map<String, Integer> weakPointCounts = new LinkedHashMap<>();
        List<Map<String, Object>> recentEvents = new ArrayList<>();
        long revealedSolutionCount = 0;
        long blockedCount = 0;

        for (LearningEventLog event : events) {
            if (event == null) {
                continue;
            }
            String actionType = event.getActionType();
            if (actionType != null && !actionType.isBlank()) {
                actionCounts.put(actionType, actionCounts.getOrDefault(actionType, 0L) + 1L);
                if (AgentProtocolConstants.ACTION_REVEAL_ANSWER.equals(actionType)) {
                    revealedSolutionCount++;
                }
            }
            if (!Boolean.TRUE.equals(event.getExecuted())) {
                blockedCount++;
            }

            Map<String, Object> eventView = new LinkedHashMap<>();
            eventView.put("request_id", event.getRequestId());
            eventView.put("trigger_source", event.getTriggerSource());
            eventView.put("action_type", event.getActionType());
            eventView.put("pedagogical_goal", event.getPedagogicalGoal());
            eventView.put("content_type", event.getContentType());
            eventView.put("executed", event.getExecuted());
            eventView.put("blocked_reason", event.getBlockedReason());
            eventView.put("create_time", event.getCreateTime());
            recentEvents.add(eventView);
        }

        for (UserProblemInteraction interaction : interactions) {
            for (String weakPoint : parseWeakPointList(interaction == null ? null : interaction.getWeakPoints())) {
                weakPointCounts.put(weakPoint, weakPointCounts.getOrDefault(weakPoint, 0) + 1);
            }
        }

        List<String> weakPoints = new ArrayList<>(weakPointCounts.keySet());
        weakPoints.sort((left, right) -> weakPointCounts.get(right).compareTo(weakPointCounts.get(left)));
        if (weakPoints.size() > 8) {
            weakPoints = new ArrayList<>(weakPoints.subList(0, 8));
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("action_counts", actionCounts);
        summary.put("weak_points", weakPoints);
        summary.put("revealed_solution_count", revealedSolutionCount);
        summary.put("blocked_count", blockedCount);
        summary.put("feedback_hint", feedbackHint(revealedSolutionCount, blockedCount));
        summary.put("recent_events", recentEvents);
        return summary;
    }

    private AgentDecisionDTO decideAndApply(Long userId, Long problemId, Long submitId, AgentContextDTO context) {
        return decideAndApply(userId, problemId, submitId, context, true, "PROBLEM", problemId, null, null, null);
    }

    private AgentDecisionDTO decideAndApply(Long userId, Long problemId, Long submitId, AgentContextDTO context,
                                            boolean applyLearningState,
                                            String entryRefType,
                                            Long entryRefId,
                                            Long pathId,
                                            Long levelId,
                                            Long wrongItemId) {
        AgentDecisionDTO decision = agentClient.getDecision(context);
        if (decision == null) {
            decision = createLocalFallback(context);
        }
        normalizeDecision(decision);

        AgentExecutionResult result = decisionEnforcer.enforce(context, decision);
        decision.setExecuted(result.isExecuted());
        decision.setBlockedReason(result.getBlockedReason());

        if (result.isExecuted() && (!applyLearningState || problemId == null)) {
            recordLearningEvent(userId, problemId, submitId, context, decision, entryRefType, entryRefId, pathId, levelId, wrongItemId);
            return decision;
        }

        if (result.isExecuted() && applyLearningState && problemId != null) {
            executeDecisionState(userId, problemId, submitId, context, decision);
        } else {
            decision.setMainResponse("这次请求被学习约束层拦截：" + result.getBlockedReason());
            decision.setContent(decision.getMainResponse());
            decision.setNextSuggestion("先继续尝试一次，或请求一个更轻量的提示。");
            decision.setSuggestedNextAction(decision.getNextSuggestion());
        }
        recordLearningEvent(userId, problemId, submitId, context, decision, entryRefType, entryRefId, pathId, levelId, wrongItemId);
        return decision;
    }

    private void normalizeDecision(AgentDecisionDTO decision) {
        if (decision == null) {
            return;
        }
        if ((decision.getMainResponse() == null || decision.getMainResponse().isBlank()) && decision.getContent() != null) {
            decision.setMainResponse(decision.getContent());
        }
        if ((decision.getContent() == null || decision.getContent().isBlank()) && decision.getMainResponse() != null) {
            decision.setContent(decision.getMainResponse());
        }
        if ((decision.getNextSuggestion() == null || decision.getNextSuggestion().isBlank())
                && decision.getSuggestedNextAction() != null) {
            decision.setNextSuggestion(decision.getSuggestedNextAction());
        }
        if ((decision.getSuggestedNextAction() == null || decision.getSuggestedNextAction().isBlank())
                && decision.getNextSuggestion() != null) {
            decision.setSuggestedNextAction(decision.getNextSuggestion());
        }
        if ((decision.getContentType() == null || decision.getContentType().isBlank())
                && decision.getActionType() != null) {
            decision.setContentType(AgentProtocolConstants.CONTENT_TYPE_BY_ACTION.get(decision.getActionType()));
        }
        if ((decision.getPedagogicalGoal() == null || decision.getPedagogicalGoal().isBlank())
                && decision.getActionType() != null) {
            decision.setPedagogicalGoal(AgentProtocolConstants.GOAL_BY_ACTION.get(decision.getActionType()));
        }
    }

    private AgentDecisionDTO createLocalFallback(AgentContextDTO context) {
        AgentDecisionDTO fallback = new AgentDecisionDTO();
        fallback.setResponseId(context.getRequestId());
        fallback.setTimestamp(LocalDateTime.now());
        fallback.setDecisionSummary("Agent返回空结果，使用本地兜底");
        fallback.setSelectedStrategy("LOCAL_FALLBACK");
        fallback.setDecisionReason("Agent decision is null");
        fallback.setRecommendedActionId("hint_local_fallback");
        fallback.setActionType(AgentProtocolConstants.ACTION_HINT);
        fallback.setPedagogicalGoal("GIVE_LIGHT_HINT");
        fallback.setContentType("hint");
        fallback.setMainResponse("暂时没有生成有效建议。先从一个最小失败样例开始，检查状态更新顺序和边界条件。");
        fallback.setNextSuggestion("确认一个小样例后再继续追问。");
        fallback.setContent(fallback.getMainResponse());
        fallback.setSuggestedNextAction(fallback.getNextSuggestion());
        fallback.setConfidence(0.2);
        return fallback;
    }

    private void executeDecisionState(Long userId, Long problemId, Long submitId,
                                      AgentContextDTO context, AgentDecisionDTO decision) {
        if (AgentProtocolConstants.INTENT_GENERAL_CHAT.equals(context.getUserIntent())) {
            return;
        }

        String actionType = decision.getActionType();
        switch (actionType) {
            case AgentProtocolConstants.ACTION_GUIDE_IDEA ->
                    updateActionState(userId, problemId, decision, "guidance", currentStage(userId, problemId));
            case AgentProtocolConstants.ACTION_HINT -> {
                userProblemInteractionMapper.incrementHintCount(userId, problemId);
                updateActionState(userId, problemId, decision, "hint", stageAfter(userId, problemId, "HINTED"));
            }
            case AgentProtocolConstants.ACTION_DIAGNOSE -> {
                userProblemInteractionMapper.incrementDiagnoseCount(userId, problemId);
                String diagnosisStage = stageAfter(userId, problemId, "DIAGNOSED");
                userProblemInteractionMapper.updateLearningState(
                        userId,
                        problemId,
                        decision.getErrorTag(),
                        decision.getWeakPoints() == null ? null : JSON.toJSONString(decision.getWeakPoints()),
                        diagnosisStage
                );
                updateActionState(userId, problemId, decision, "diagnosis", diagnosisStage);
            }
            case AgentProtocolConstants.ACTION_EXPLAIN -> {
                userProblemInteractionMapper.incrementExplainCount(userId, problemId);
                updateActionState(userId, problemId, decision, "explanation", stageAfter(userId, problemId, "EXPLAINED"));
            }
            case AgentProtocolConstants.ACTION_RECOMMEND -> {
                userProblemInteractionMapper.incrementRecommendCount(userId, problemId);
                updateActionState(userId, problemId, decision, "recommendation", currentStage(userId, problemId));
            }
            case AgentProtocolConstants.ACTION_REFLECT -> {
                userProblemInteractionMapper.incrementReflectCount(userId, problemId);
                updateActionState(userId, problemId, decision, "reflection", currentStage(userId, problemId));
            }
            case AgentProtocolConstants.ACTION_REVEAL_ANSWER -> {
                executeRevealAnswerAction(userId, problemId);
                updateActionState(userId, problemId, decision, "solution", currentStage(userId, problemId));
            }
            default -> {
            }
        }
    }

    private void updateActionState(Long userId, Long problemId, AgentDecisionDTO decision,
                                   String guidanceType, String learningStage) {
        userProblemInteractionMapper.updateActionSemanticState(
                userId,
                problemId,
                decision.getActionType(),
                decision.getPedagogicalGoal(),
                guidanceType,
                learningStage
        );
    }

    private void executeRevealAnswerAction(Long userId, Long problemId) {
        UserProblemInteraction interaction = userProblemInteractionMapper.findByUserAndProblem(userId, problemId);
        if (interaction != null) {
            interaction.setHasViewedAnswer(true);
            interaction.setLastActionTime(LocalDateTime.now());
            interaction.setUpdateTime(LocalDateTime.now());
            userProblemInteractionMapper.update(interaction);
        }
    }

    private String stageAfter(Long userId, Long problemId, String targetStage) {
        String currentStage = currentStage(userId, problemId);
        return getStageLevel(targetStage) > getStageLevel(currentStage) ? targetStage : currentStage;
    }

    private String currentStage(Long userId, Long problemId) {
        UserProblemInteraction interaction = userProblemInteractionMapper.findByUserAndProblem(userId, problemId);
        if (interaction == null || interaction.getLearningStage() == null || interaction.getLearningStage().isBlank()) {
            return "FIRST_TRY";
        }
        return interaction.getLearningStage();
    }

    private int getStageLevel(String stage) {
        if (stage == null) return 0;
        return switch (stage) {
            case "FIRST_TRY" -> 1;
            case "HINTED" -> 2;
            case "DIAGNOSED" -> 3;
            case "EXPLAINED" -> 4;
            case "MASTERED" -> 5;
            default -> 0;
        };
    }

    private void recordLearningEvent(Long userId, Long problemId, Long submitId,
                                     AgentContextDTO context, AgentDecisionDTO decision) {
        recordLearningEvent(userId, problemId, submitId, context, decision, "PROBLEM", problemId, null, null, null);
    }

    private void recordLearningEvent(Long userId, Long problemId, Long submitId,
                                     AgentContextDTO context, AgentDecisionDTO decision,
                                     String entryRefType, Long entryRefId,
                                     Long pathId, Long levelId, Long wrongItemId) {
        LearningEventLog log = new LearningEventLog();
        log.setUserId(userId);
        log.setProblemId(problemId);
        log.setSubmitId(submitId);
        log.setTriggerSource(context.getTriggerSource());
        log.setUserIntent(context.getUserIntent());
        log.setActionType(decision.getActionType());
        log.setActionId(decision.getRecommendedActionId());
        log.setPedagogicalGoal(decision.getPedagogicalGoal());
        log.setContentType(decision.getContentType());
        log.setStrategy(decision.getSelectedStrategy());
        log.setDecisionReason(decision.getDecisionReason());
        log.setContent(decision.getContent());
        log.setRequestId(decision.getResponseId());
        log.setExecuted(Boolean.TRUE.equals(decision.getExecuted()));
        log.setBlockedReason(decision.getBlockedReason());
        log.setEntryRefType(entryRefType);
        log.setEntryRefId(entryRefId);
        log.setPathId(pathId);
        log.setLevelId(levelId);
        log.setWrongItemId(wrongItemId);
        log.setCreateTime(LocalDateTime.now());
        learningEventLogMapper.insert(log);
    }

    private UserProblemInteraction getOrCreateInteraction(Long userId, Long problemId) {
        UserProblemInteraction interaction = userProblemInteractionMapper.findByUserAndProblem(userId, problemId);
        if (interaction != null) {
            return interaction;
        }

        interaction = new UserProblemInteraction();
        interaction.setUserId(userId);
        interaction.setProblemId(problemId);
        interaction.setHintCount(0);
        interaction.setDiagnoseCount(0);
        interaction.setExplainCount(0);
        interaction.setRecommendCount(0);
        interaction.setReflectCount(0);
        interaction.setHasViewedAnswer(false);
        interaction.setConsecutiveFailures(0);
        interaction.setLearningStage("FIRST_TRY");
        interaction.setCreateTime(LocalDateTime.now());
        interaction.setUpdateTime(LocalDateTime.now());
        userProblemInteractionMapper.insert(interaction);
        return interaction;
    }

    private void sanitizeStaleInteraction(UserProblemInteraction interaction, Long userId, Long problemId) {
        if (interaction == null || interaction.getLastSubmitId() == null) {
            return;
        }
        Submit submit = submitMapper.findById(interaction.getLastSubmitId());
        if (submit == null || !Objects.equals(submit.getUserId(), userId) || !Objects.equals(submit.getProblemId(), problemId)) {
            interaction.setLastSubmitId(null);
            interaction.setConsecutiveFailures(calculateConsecutiveFailures(userId, problemId, null, false));
            interaction.setUpdateTime(LocalDateTime.now());
            userProblemInteractionMapper.update(interaction);
        }
    }

    private int calculateConsecutiveFailures(Long userId, Long problemId, Long currentSubmitId, boolean triggeredFailure) {
        List<Submit> recentSubmits = submitMapper.findRecentByUserAndProblem(userId, problemId, 20);
        int consecutiveFailures = 0;
        boolean sawCurrent = currentSubmitId == null;

        if (recentSubmits != null) {
            for (Submit submit : recentSubmits) {
                if (currentSubmitId != null && currentSubmitId.equals(submit.getId())) {
                    sawCurrent = true;
                }
                if (isAccepted(submit)) {
                    break;
                }
                consecutiveFailures++;
            }
        }

        if (!sawCurrent && currentSubmitId != null) {
            Submit current = submitMapper.findById(currentSubmitId);
            if (current != null && isFailure(current)) {
                consecutiveFailures++;
            }
        }

        if (triggeredFailure && consecutiveFailures == 0) {
            consecutiveFailures = 1;
        }
        return consecutiveFailures;
    }

    private Submit requireOwnedSubmit(Long userId, Long submitId) {
        Submit submit = submitMapper.findById(submitId);
        if (submit == null) {
            throw new RuntimeException("提交记录不存在");
        }
        if (!Objects.equals(submit.getUserId(), userId)) {
            throw new RuntimeException("无权访问该提交记录");
        }
        return submit;
    }

    private Problem requireProblem(Long problemId) {
        Problem problem = problemMapper.findById(problemId);
        if (problem == null) {
            throw new RuntimeException("题目不存在");
        }
        return problem;
    }

    private Submit trustedSubmit(Long userId, Long problemId, Submit submit) {
        if (submit == null || submit.getId() == null) {
            return null;
        }
        if (Objects.equals(submit.getUserId(), userId) && Objects.equals(submit.getProblemId(), problemId)) {
            return submit;
        }
        Submit fromDb = submitMapper.findById(submit.getId());
        if (fromDb != null && Objects.equals(fromDb.getUserId(), userId) && Objects.equals(fromDb.getProblemId(), problemId)) {
            return fromDb;
        }
        return null;
    }

    private boolean isTriggeredFailure(ProblemCoachContext context, ProblemAgentChatRequestVO request, Submit submit) {
        boolean requestFullSolution = request != null && Boolean.TRUE.equals(request.getRequestFullSolution());
        if (requestFullSolution) {
            return false;
        }
        return "run_failed".equalsIgnoreCase(context.getTriggerType())
                || "submit_failed".equalsIgnoreCase(context.getTriggerType())
                || (context.getLatestResultCode() != null && context.getLatestResultCode() != 0)
                || (context.getLatestErrorMessage() != null && !context.getLatestErrorMessage().isBlank())
                || isFailure(submit);
    }

    private boolean isAccepted(Submit submit) {
        return submit != null && submit.getResult() != null && submit.getResult() == 0;
    }

    private boolean isFailure(Submit submit) {
        return submit != null && (submit.getResult() == null || submit.getResult() != 0);
    }

    private boolean levelBelongsToPath(LearningPath path, Long levelId) {
        if (path == null || path.getChapters() == null || path.getChapters().isEmpty()) {
            return true;
        }
        for (PathChapter chapter : path.getChapters()) {
            if (chapter == null || chapter.getLevels() == null) {
                continue;
            }
            for (PathLevel level : chapter.getLevels()) {
                if (level != null && Objects.equals(level.getId(), levelId)) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<String> parseWeakPointList(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return JSON.parseArray(value, String.class);
        } catch (Exception ignored) {
            String[] parts = value.split("[,;，；\\s]+");
            List<String> result = new ArrayList<>();
            for (String part : parts) {
                if (part != null && !part.isBlank()) {
                    result.add(part.trim());
                }
            }
            return result;
        }
    }

    private String feedbackHint(long revealedSolutionCount, long blockedCount) {
        if (revealedSolutionCount > 0) {
            return "近期有查看答案行为，建议优先安排复盘或同知识点再练。";
        }
        if (blockedCount > 0) {
            return "近期有被约束层拦截的请求，可关注是否过早请求完整答案。";
        }
        return "近期 Agent 事件以引导和建议为主，可继续观察用户是否采纳。";
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
