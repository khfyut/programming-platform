package com.programming.service;

import com.alibaba.fastjson2.JSON;
import com.programming.agent.AgentClient;
import com.programming.agent.AgentContextBuilder;
import com.programming.agent.AgentDecisionEnforcer;
import com.programming.agent.DecisionContextReducer;
import com.programming.agent.AgentExecutionResult;
import com.programming.agent.AgentPolicyProfile;
import com.programming.agent.AgentProtocolConstants;
import com.programming.agent.dto.AgentContextDTO;
import com.programming.agent.dto.AgentCoachActionDTO;
import com.programming.agent.dto.AgentCoachChatRequestDTO;
import com.programming.agent.dto.AgentCoachChatResponseDTO;
import com.programming.agent.dto.AgentDecisionDTO;
import com.programming.agent.orchestrator.LearningAgentEvent;
import com.programming.agent.orchestrator.LearningAgentOrchestrator;
import com.programming.agent.orchestrator.LearningAgentRun;
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
import java.util.UUID;

@Service
public class AgentService {

    @Autowired
    private AgentClient agentClient;

    @Autowired
    private AgentContextBuilder contextBuilder;

    @Autowired
    private AgentDecisionEnforcer decisionEnforcer;

    @Autowired(required = false)
    private DecisionContextReducer decisionContextReducer;

    @Autowired(required = false)
    private LearningAgentOrchestrator learningAgentOrchestrator;

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
        LearningAgentRun run = prepareAgentRun(
                userId,
                context,
                AgentPolicyProfile.PROBLEM_COACH,
                AgentProtocolConstants.TRIGGER_SUBMISSION_RESULT,
                null,
                problem.getId(),
                submitId,
                null,
                null,
                null
        );
        return decideAndApply(userId, problem.getId(), submitId, run.getContext(), run.getPolicyProfile());
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
        LearningAgentRun run = prepareAgentRun(
                userId,
                context,
                AgentPolicyProfile.PROBLEM_COACH,
                context.getTriggerSource(),
                request == null ? null : request.getMessage(),
                problem.getId(),
                submitId,
                null,
                null,
                null
        );
        return decideAndApply(userId, problem.getId(), submitId, run.getContext(), run.getPolicyProfile());
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
        LearningAgentRun run = prepareAgentRun(
                userId,
                context,
                AgentPolicyProfile.WRONG_BOOK_REFLECTION,
                AgentProtocolConstants.TRIGGER_WRONG_BOOK_ENTRY,
                null,
                problem.getId(),
                wrongItem.getSubmitId(),
                wrongItemId,
                null,
                null
        );
        return decideAndApply(
                userId,
                problem.getId(),
                wrongItem.getSubmitId(),
                run.getContext(),
                true,
                "WRONG_BOOK",
                wrongItemId,
                null,
                null,
                wrongItemId,
                run.getPolicyProfile()
        );
    }

    @Transactional
    public AgentDecisionDTO processWrongBookReviewChat(Long userId,
                                                       WrongBookItem wrongItem,
                                                       String message,
                                                       String actionHint,
                                                       String taskType,
                                                       String answer,
                                                       Integer hintLevel) {
        if (wrongItem == null || wrongItem.getProblemId() == null) {
            throw new RuntimeException("Wrong book item not found");
        }

        Problem problem = requireProblem(wrongItem.getProblemId());
        UserProblemInteraction interaction = getOrCreateInteraction(userId, problem.getId());
        AgentContextDTO context = contextBuilder.buildForWrongBookEntry(problem, wrongItem, interaction);
        context.setScene("wrong_book");
        context.setActionHint(firstNonBlank(actionHint, "chat"));
        context.setUserMessage(firstNonBlank(message, answer, ""));
        context.setRequestedFullSolution(isExplicitReferenceRequest(context.getUserMessage(), context.getActionHint()));
        context.setLearningSummary(wrongBookActionSummary(taskType, answer, hintLevel));

        LearningAgentRun run = prepareAgentRun(
                userId,
                context,
                AgentPolicyProfile.WRONG_BOOK_REFLECTION,
                AgentProtocolConstants.TRIGGER_WRONG_BOOK_ENTRY,
                context.getUserMessage(),
                problem.getId(),
                wrongItem.getSubmitId(),
                wrongItem.getId(),
                null,
                null
        );
        if ("similar_problem".equals(context.getActionHint())) {
            run.getContext().setToolResults(wrongBookRecommendationToolResults(userId, wrongItem));
        }
        run.getContext().setScene("wrong_book");
        run.getContext().setActionHint(context.getActionHint());
        run.getContext().setUserMessage(context.getUserMessage());
        run.getContext().setRequestedFullSolution(context.isRequestedFullSolution());
        run.getContext().setLearningSummary(wrongBookActionSummary(taskType, answer, hintLevel));
        return decideAndApply(
                userId,
                problem.getId(),
                wrongItem.getSubmitId(),
                run.getContext(),
                true,
                "WRONG_BOOK",
                wrongItem.getId(),
                null,
                null,
                wrongItem.getId(),
                run.getPolicyProfile()
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
        LearningAgentRun run = prepareAgentRun(
                userId,
                context,
                AgentPolicyProfile.LEARNING_PATH,
                AgentProtocolConstants.TRIGGER_LEARNING_PATH_ENTRY,
                null,
                null,
                null,
                null,
                pathId,
                levelId
        );
        return decideAndApply(
                userId,
                null,
                null,
                run.getContext(),
                false,
                "LEARNING_PATH_LEVEL",
                levelId,
                pathId,
                levelId,
                null,
                run.getPolicyProfile()
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

    @Transactional
    public AgentCoachChatResponseDTO processGlobalGuideChat(Long userId, AgentCoachChatRequestDTO request) {
        AgentCoachChatRequestDTO safeRequest = request == null ? new AgentCoachChatRequestDTO() : request;
        AgentContextDTO context = contextBuilder.buildForGlobalGuideChat(safeRequest);
        LearningAgentRun run = prepareAgentRun(
                userId,
                context,
                AgentPolicyProfile.GLOBAL_GUIDE,
                AgentProtocolConstants.TRIGGER_GLOBAL_GUIDE_CHAT,
                safeRequest.getMessage(),
                safeRequest.getProblemId(),
                safeRequest.getSubmitId(),
                safeRequest.getWrongItemId(),
                safeRequest.getPathId(),
                safeRequest.getLevelId()
        );
        AgentDecisionDTO decision = decideAndApply(
                userId,
                safeRequest.getProblemId(),
                safeRequest.getSubmitId(),
                run.getContext(),
                false,
                "GLOBAL_GUIDE",
                null,
                safeRequest.getPathId(),
                safeRequest.getLevelId(),
                safeRequest.getWrongItemId(),
                run.getPolicyProfile()
        );
        List<AgentCoachActionDTO> actions = globalGuideActions(safeRequest, decision);
        AgentCoachChatResponseDTO response = new AgentCoachChatResponseDTO();
        response.setSessionId(firstNonBlank(safeRequest.getSessionId(), "coach-" + UUID.randomUUID()));
        response.setScene("global_guide");
        response.setFallback(isAgentFallback(decision));
        response.setSource(response.isFallback() ? "LOCAL_FALLBACK" : "AGENT_SERVICE");
        response.setActions(actions);
        response.setReply(appendActionLabels(firstNonBlank(decision.getMainResponse(), decision.getContent(), ""), actions));
        return response;
    }

    private boolean isAgentFallback(AgentDecisionDTO decision) {
        String strategy = decision == null ? "" : firstNonBlank(decision.getSelectedStrategy(), "");
        String errorTag = decision == null ? "" : firstNonBlank(decision.getErrorTag(), "");
        return "AGENT_FAILURE".equals(strategy)
                || "FAILURE".equals(strategy)
                || "MODEL_TIMEOUT".equals(errorTag)
                || "LLM_UNAVAILABLE".equals(errorTag)
                || "MODEL_NOT_FOUND".equals(errorTag)
                || "MODEL_HTTP_ERROR".equals(errorTag)
                || "MODEL_ERROR".equals(errorTag)
                || "INVALID_JSON".equals(errorTag);
    }

    private List<AgentCoachActionDTO> globalGuideActions(AgentCoachChatRequestDTO request, AgentDecisionDTO decision) {
        List<AgentCoachActionDTO> actions = new ArrayList<>();
        String pageType = request == null ? "" : firstNonBlank(request.getPageType(), "").toUpperCase();
        String route = request == null ? "" : firstNonBlank(request.getCurrentRoute(), "");

        if ("PROBLEM".equals(pageType) || route.startsWith("/problem/")) {
            actions.add(action("focus_problem_coach", "题目陪练", null));
        } else if ("WRONG_BOOK_DETAIL".equals(pageType) || route.startsWith("/wrong-book/")) {
            actions.add(action("focus_wrong_book_review", "错题复盘", null));
        } else if ("LEARNING_PATH".equals(pageType)) {
            actions.add(action("route", "题库", "/problems"));
        } else if ("PROFILE_ANALYSIS".equals(pageType)) {
            actions.add(action("route", "学习中心", "/learn"));
            actions.add(action("route", "错题本", "/wrong-book"));
        } else {
            actions.add(action("route", "学习中心", "/learn"));
        }

        if (actions.size() < 2 && shouldOfferAiCoach(request, decision)) {
            actions.add(action("focus_problem_coach", "题目陪练", null));
        }
        return actions;
    }

    private boolean shouldOfferAiCoach(AgentCoachChatRequestDTO request, AgentDecisionDTO decision) {
        String text = ((request == null ? "" : firstNonBlank(request.getMessage(), "")) + " "
                + (decision == null ? "" : firstNonBlank(decision.getMainResponse(), ""))).toLowerCase();
        return text.contains("ai") || text.contains("陪练") || text.contains("题目") || text.contains("提示");
    }

    private AgentCoachActionDTO action(String type, String label, String route) {
        AgentCoachActionDTO action = new AgentCoachActionDTO();
        action.setType(type);
        action.setLabel(label);
        action.setRoute(route);
        return action;
    }

    private String appendActionLabels(String reply, List<AgentCoachActionDTO> actions) {
        String result = firstNonBlank(reply, "我可以先帮你判断当前页面适合做什么，再把深度任务交给对应功能。");
        if (actions == null || actions.isEmpty()) {
            return result;
        }
        List<String> missing = actions.stream()
                .map(AgentCoachActionDTO::getLabel)
                .filter(label -> label != null && !label.isBlank())
                .filter(label -> !result.contains(label))
                .toList();
        if (missing.isEmpty()) {
            return result;
        }
        return result + "\n\n如果你想继续，可以先看 " + String.join(" 或 ", missing) + "。";
    }

    private LearningAgentRun prepareAgentRun(Long userId,
                                             AgentContextDTO context,
                                             AgentPolicyProfile policyProfile,
                                             String eventType,
                                             String userMessage,
                                             Long problemId,
                                             Long submitId,
                                             Long wrongItemId,
                                             Long pathId,
                                             Long levelId) {
        LearningAgentEvent event = new LearningAgentEvent();
        event.setEventType(eventType);
        event.setPolicyProfile(policyProfile);
        event.setUserId(userId);
        event.setUserMessage(userMessage);
        event.setProblemId(problemId);
        event.setSubmitId(submitId);
        event.setWrongItemId(wrongItemId);
        event.setPathId(pathId);
        event.setLevelId(levelId);

        LearningAgentRun run = learningAgentOrchestrator == null ? null : learningAgentOrchestrator.prepare(event, context);
        if (run != null) {
            return run;
        }
        AgentPolicyProfile effectiveProfile = policyProfile == null ? AgentPolicyProfile.PROBLEM_COACH : policyProfile;
        if (context != null) {
            context.setPolicyProfile(effectiveProfile.name());
        }
        return new LearningAgentRun(event, context, effectiveProfile);
    }

    private AgentDecisionDTO decideAndApply(Long userId, Long problemId, Long submitId, AgentContextDTO context) {
        return decideAndApply(userId, problemId, submitId, context, AgentPolicyProfile.PROBLEM_COACH);
    }

    private AgentDecisionDTO decideAndApply(Long userId, Long problemId, Long submitId,
                                            AgentContextDTO context, AgentPolicyProfile policyProfile) {
        return decideAndApply(
                userId,
                problemId,
                submitId,
                context,
                true,
                "PROBLEM",
                problemId,
                null,
                null,
                null,
                policyProfile
        );
    }

    private AgentDecisionDTO decideAndApply(Long userId, Long problemId, Long submitId, AgentContextDTO context,
                                            boolean applyLearningState,
                                            String entryRefType,
                                            Long entryRefId,
                                            Long pathId,
                                            Long levelId,
                                            Long wrongItemId,
                                            AgentPolicyProfile policyProfile) {
        if (decisionContextReducer != null) {
            decisionContextReducer.reduce(context);
        }

        AgentDecisionDTO decision = agentClient.getDecision(context);
        if (decision == null) {
            decision = createLocalFallback(context);
        }
        normalizeDecision(decision);

        AgentExecutionResult result = decisionEnforcer.enforce(context, decision, policyProfile);
        if (!result.isExecuted()) {
            context.setViolation(buildViolation(context, decision, result, policyProfile));
            AgentDecisionDTO retriedDecision = agentClient.getDecision(context);
            if (retriedDecision != null) {
                normalizeDecision(retriedDecision);
                AgentExecutionResult retryResult = decisionEnforcer.enforce(context, retriedDecision, policyProfile);
                decision = retriedDecision;
                result = retryResult;
            }
        }
        decision.setExecuted(result.isExecuted());
        decision.setBlockedReason(result.getBlockedReason());

        if (result.isExecuted() && (!applyLearningState || problemId == null)) {
            recordLearningEvent(userId, problemId, submitId, context, decision, entryRefType, entryRefId, pathId, levelId, wrongItemId, policyProfile);
            return decision;
        }

        if (result.isExecuted() && applyLearningState && problemId != null) {
            executeDecisionState(userId, problemId, submitId, context, decision);
        } else {
            decision.setMainResponse("这次请求被学习约束层拦截：" + result.getBlockedReason());
            decision.setMainResponse(buildUserSafeBlockedResponse(context, policyProfile));
            decision.setContent(decision.getMainResponse());
            decision.setNextSuggestion("先继续尝试一次，或请求一个更轻量的提示。");
            decision.setNextSuggestion("先要一个方向提示、最小反例，或补充一次失败提交后再请求更完整的讲解。");
            decision.setSuggestedNextAction(decision.getNextSuggestion());
        }
        recordLearningEvent(userId, problemId, submitId, context, decision, entryRefType, entryRefId, pathId, levelId, wrongItemId, policyProfile);
        return decision;
    }

    private Map<String, Object> buildViolation(AgentContextDTO context,
                                               AgentDecisionDTO decision,
                                               AgentExecutionResult result,
                                               AgentPolicyProfile policyProfile) {
        Map<String, Object> violation = new LinkedHashMap<>();
        violation.put("blocked_action", decision == null ? null : decision.getActionType());
        violation.put("blocked_reason", result == null ? null : result.getBlockedReason());
        violation.put("allowed_actions", new ArrayList<>(com.programming.agent.AgentPolicyConfig
                .forProfile(policyProfile)
                .getAllowedActions()));
        violation.put("allowed_scopes", allowedScopesFor(context, policyProfile));
        return violation;
    }

    private List<String> allowedScopesFor(AgentContextDTO context, AgentPolicyProfile policyProfile) {
        if (policyProfile == AgentPolicyProfile.WRONG_BOOK_REFLECTION) {
            return List.of("concept_only", "hint_only", "partial_solution", "full_solution");
        }
        if (policyProfile == AgentPolicyProfile.PROBLEM_COACH || policyProfile == null) {
            String evidenceLevel = context == null || context.getFailureEvidenceLevel() == null
                    ? DecisionContextReducer.LEVEL_NONE
                    : context.getFailureEvidenceLevel().trim().toUpperCase();
            boolean explicit = context != null && context.isRequestedFullSolution();
            return switch (evidenceLevel) {
                case DecisionContextReducer.LEVEL_VERIFIED -> explicit
                        ? List.of("concept_only", "hint_only", "partial_solution", "full_solution", "reference_code")
                        : List.of("concept_only", "hint_only", "partial_solution");
                case DecisionContextReducer.LEVEL_STRONG -> List.of("concept_only", "hint_only", "partial_solution");
                default -> List.of("concept_only", "hint_only");
            };
        }
        if (policyProfile == AgentPolicyProfile.GLOBAL_COACH || policyProfile == AgentPolicyProfile.LEARNING_PATH) {
            return List.of("concept_only", "hint_only");
        }
        return List.of("concept_only", "hint_only", "partial_solution");
    }

    private String buildUserSafeBlockedResponse(AgentContextDTO context, AgentPolicyProfile policyProfile) {
        if (policyProfile == AgentPolicyProfile.WRONG_BOOK_REFLECTION) {
            return "这次请求触发了复盘页的参考材料保护，暂时不能直接给参考代码。可以先说明你想对照哪一段，我会先帮你定位错误原因和修正方向。";
        }
        String evidenceLevel = context == null || context.getFailureEvidenceLevel() == null
                ? DecisionContextReducer.LEVEL_NONE
                : context.getFailureEvidenceLevel().trim().toUpperCase();
        if (DecisionContextReducer.LEVEL_NONE.equals(evidenceLevel) || DecisionContextReducer.LEVEL_WEAK.equals(evidenceLevel)) {
            return "这次请求触发了学习保护，当前还不能直接给完整解法。我先给你一个方向提示：先把输入、预期输出和你当前代码的实际输出对齐，优先检查边界条件和状态更新顺序。";
        }
        return "这次请求触发了学习保护，暂时不能继续展开到完整答案。可以先让我定位错误点、给一个最小反例，或在一次失败提交后再请求更完整的对照说明。";
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
        recordLearningEvent(
                userId,
                problemId,
                submitId,
                context,
                decision,
                "PROBLEM",
                problemId,
                null,
                null,
                null,
                AgentPolicyProfile.PROBLEM_COACH
        );
    }

    private void recordLearningEvent(Long userId, Long problemId, Long submitId,
                                     AgentContextDTO context, AgentDecisionDTO decision,
                                     String entryRefType, Long entryRefId,
                                     Long pathId, Long levelId, Long wrongItemId,
                                     AgentPolicyProfile policyProfile) {
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
        log.setPolicyProfile(policyProfile == null ? AgentPolicyProfile.PROBLEM_COACH.name() : policyProfile.name());
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
            return "近期有过早请求完整答案的情况，建议先用提示或复盘把关键思路补齐。";
        }
        return "近期以思路引导和学习建议为主，可以继续观察哪些建议最能帮助你通过题目。";
    }

    private boolean isExplicitReferenceRequest(String message, String actionHint) {
        String text = ((message == null ? "" : message) + " " + (actionHint == null ? "" : actionHint)).toLowerCase();
        return text.contains("full solution")
                || text.contains("reference code")
                || text.contains("complete code")
                || text.contains("give me code")
                || text.contains("show code")
                || text.contains("solution");
    }

    private Map<String, Object> wrongBookActionSummary(String taskType, String answer, Integer hintLevel) {
        Map<String, Object> summary = new LinkedHashMap<>();
        if (taskType != null && !taskType.isBlank()) {
            summary.put("task_type", taskType);
        }
        if (answer != null && !answer.isBlank()) {
            summary.put("task_answer", answer);
        }
        if (hintLevel != null) {
            summary.put("hint_level", hintLevel);
        }
        return summary;
    }

    private List<Map<String, Object>> wrongBookRecommendationToolResults(Long userId, WrongBookItem wrongItem) {
        if (wrongItem == null || wrongItem.getId() == null) {
            return List.of();
        }
        List<Problem> recommendations = wrongBookService.getRecommendedProblems(userId, wrongItem.getId(), 5);
        List<Map<String, Object>> problems = new ArrayList<>();
        if (recommendations != null) {
            for (Problem problem : recommendations) {
                if (problem == null || problem.getId() == null) {
                    continue;
                }
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("problem_id", problem.getId());
                row.put("title", problem.getTitle());
                row.put("difficulty", problem.getDifficulty());
                row.put("route", "/problem/" + problem.getId());
                problems.add(row);
            }
        }
        Map<String, Object> toolResult = new LinkedHashMap<>();
        toolResult.put("tool_name", "wrong_book_recommendations");
        toolResult.put("success", true);
        toolResult.put("problems", problems);
        return List.of(toolResult);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
