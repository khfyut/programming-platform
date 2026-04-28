package com.programming.service;

import com.alibaba.fastjson2.JSON;
import com.programming.agent.dto.AgentCoachDecideRequestDTO;
import com.programming.agent.dto.AgentCoachDecisionDTO;
import com.programming.agent.dto.AgentCoachEventRequestDTO;
import com.programming.agent.dto.AgentCoachNudgeDTO;
import com.programming.agent.dto.AgentCoachStateDTO;
import com.programming.entity.AgentCoachNudge;
import com.programming.entity.UserProblemInteraction;
import com.programming.entity.WrongBookItem;
import com.programming.mapper.AgentCoachNudgeMapper;
import com.programming.mapper.UserProblemInteractionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AgentCoachService {
    private static final int DWELL_SECONDS_THRESHOLD = 90;
    private static final int COOLDOWN_MINUTES = 30;
    private static final int ACTIVE_TTL_MINUTES = 20;
    private static final int SNOOZE_MINUTES = 30;

    private final AgentCoachNudgeMapper coachNudgeMapper;
    private final UserProblemInteractionMapper interactionMapper;
    private final WrongBookService wrongBookService;

    @Autowired
    public AgentCoachService(AgentCoachNudgeMapper coachNudgeMapper,
                             UserProblemInteractionMapper interactionMapper,
                             WrongBookService wrongBookService) {
        this.coachNudgeMapper = coachNudgeMapper;
        this.interactionMapper = interactionMapper;
        this.wrongBookService = wrongBookService;
    }

    public AgentCoachService(AgentCoachNudgeMapper coachNudgeMapper,
                             UserProblemInteractionMapper interactionMapper) {
        this(coachNudgeMapper, interactionMapper, null);
    }

    public AgentCoachStateDTO getState(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        List<AgentCoachNudgeDTO> active = coachNudgeMapper.findActiveByUser(userId, now, 5)
                .stream()
                .map(this::toDto)
                .toList();

        AgentCoachStateDTO state = new AgentCoachStateDTO();
        state.setDisplayState(active.isEmpty() ? "collapsed" : "nudging");
        state.setUnreadCount(active.size());
        state.setActiveNudges(active);
        state.setActiveNudge(active.isEmpty() ? null : active.get(0));
        state.setRecentSummary(Map.of("active_nudges", active.size()));
        return state;
    }

    public AgentCoachDecisionDTO decide(Long userId, AgentCoachDecideRequestDTO request) {
        LocalDateTime now = LocalDateTime.now();
        AgentCoachDecisionDTO result = new AgentCoachDecisionDTO();

        List<AgentCoachNudge> active = coachNudgeMapper.findActiveByUser(userId, now, 1);
        if (!active.isEmpty()) {
            result.setCreated(false);
            result.setReason("ACTIVE_NUDGE_EXISTS");
            result.setNudge(toDto(active.get(0)));
            return result;
        }

        NudgePlan plan = buildPlan(userId, request);
        if (plan == null) {
            result.setCreated(false);
            result.setReason("NO_HIGH_CONFIDENCE_TRIGGER");
            return result;
        }

        boolean inCooldown = !coachNudgeMapper.findRecentSimilar(
                userId,
                plan.triggerSource,
                plan.actionType,
                now.minusMinutes(COOLDOWN_MINUTES)
        ).isEmpty();
        if (inCooldown) {
            result.setCreated(false);
            result.setReason("COOLDOWN");
            return result;
        }

        AgentCoachNudge nudge = new AgentCoachNudge();
        nudge.setUserId(userId);
        nudge.setRequestId("coach-" + UUID.randomUUID());
        nudge.setTriggerSource(plan.triggerSource);
        nudge.setActionType(plan.actionType);
        nudge.setPedagogicalGoal(plan.pedagogicalGoal);
        nudge.setTitle(plan.title);
        nudge.setSummary(plan.summary);
        nudge.setTargetRoute(plan.targetRoute);
        nudge.setPriority(plan.priority);
        nudge.setStatus("ACTIVE");
        nudge.setMetadataJson(toJson(request.getMetadata()));
        nudge.setCreatedAt(now);
        nudge.setUpdatedAt(now);
        nudge.setExpiresAt(now.plusMinutes(ACTIVE_TTL_MINUTES));
        coachNudgeMapper.insert(nudge);

        result.setCreated(true);
        result.setReason("CREATED");
        result.setNudge(toDto(nudge));
        return result;
    }

    public AgentCoachNudgeDTO recordEvent(Long userId, AgentCoachEventRequestDTO request) {
        AgentCoachNudge nudge = coachNudgeMapper.findByIdAndUser(request.getNudgeId(), userId);
        if (nudge == null) {
            throw new RuntimeException("Coach nudge not found");
        }

        LocalDateTime now = LocalDateTime.now();
        String status = toStatus(request.getEventType(), request.getFeedbackType());
        LocalDateTime expiresAt = "SNOOZED".equals(status)
                ? now.plusMinutes(SNOOZE_MINUTES)
                : nudge.getExpiresAt();

        coachNudgeMapper.updateStatus(nudge.getId(), userId, status, now, expiresAt);
        nudge.setStatus(status);
        nudge.setUpdatedAt(now);
        nudge.setExpiresAt(expiresAt);
        return toDto(nudge);
    }

    private NudgePlan buildPlan(Long userId, AgentCoachDecideRequestDTO request) {
        String trigger = safeUpper(request.getTriggerSource());
        String pageType = safeUpper(request.getPageType());

        if ("PAGE_ENTRY".equals(trigger)) {
            if ("PROBLEM_LIST".equals(pageType)) {
                return new NudgePlan(
                        "PAGE_ENTRY",
                        "RECOMMEND",
                        "RECOMMEND_REMEDIATION",
                        "可以先选一题开始",
                        "如果暂时不知道做哪题，我可以先帮你按当前学习阶段选择一道练习题。",
                        targetRoute(request),
                        35
                );
            }
            if ("LEARN".equals(pageType) || "LEARNING_PATH".equals(pageType)) {
                return new NudgePlan(
                        "PAGE_ENTRY",
                        "RECOMMEND",
                        "RECOMMEND_REMEDIATION",
                        "先确定今天的学习入口",
                        "我可以帮你判断是继续当前路径，还是先补一个薄弱知识点。",
                        targetRoute(request),
                        35
                );
            }
            if ("PROFILE_ANALYSIS".equals(pageType)) {
                return new NudgePlan(
                        "PAGE_ENTRY",
                        "RECOMMEND",
                        "RECOMMEND_REMEDIATION",
                        "把分析变成下一步练习",
                        "可以根据最近的薄弱点，回到学习中心或错题本安排一个具体任务。",
                        targetRoute(request),
                        35
                );
            }
        }

        if ("PROBLEM_PAGE_DWELL".equals(trigger)) {
            int dwellSeconds = request.getDwellSeconds() == null ? 0 : request.getDwellSeconds();
            if (request.getProblemId() == null || dwellSeconds < DWELL_SECONDS_THRESHOLD) {
                return null;
            }
            return new NudgePlan(
                    "PROBLEM_PAGE_DWELL",
                    "GUIDE_IDEA",
                    "GUIDE_INITIAL_THINKING",
                    "还卡在这里吗？",
                    "我可以先帮你梳理思路，不直接给答案。",
                    targetRoute(request),
                    40
            );
        }

        if ("SUBMIT_FAILED".equals(trigger) || "RUN_FAILED".equals(trigger) || "CONSECUTIVE_FAILED".equals(trigger)) {
            String action = chooseFailureAction(userId, request.getProblemId());
            return new NudgePlan(
                    trigger,
                    action,
                    goalFor(action),
                    "需要我一起定位吗？",
                    "我可以根据这次失败先给提示，必要时再进入错误诊断。",
                    targetRoute(request),
                    "DIAGNOSE".equals(action) ? 80 : 60
            );
        }

        if ("ACCEPTED".equals(trigger) || "AC".equals(trigger)) {
            return new NudgePlan(
                    "ACCEPTED",
                    "REFLECT",
                    "REVIEW_AFTER_SOLUTION",
                    "可以做一次短复盘",
                    "把这题的关键判断记下来，后面遇到同类题会更稳。",
                    targetRoute(request),
                    50
            );
        }

        if ("WRONG_BOOK_DETAIL_ENTRY".equals(trigger) || "WRONG_BOOK_DETAIL".equals(pageType)) {
            Long wrongItemId = resolveWrongItemId(userId, request);
            if (wrongItemId == null) {
                return null;
            }
            return new NudgePlan(
                    "WRONG_BOOK_DETAIL_ENTRY",
                    "REFLECT",
                    "REVIEW_AFTER_SOLUTION",
                    "先做一次错题复盘",
                    "我可以帮你把这道错题的错误原因、薄弱点和下次避免策略整理清楚。",
                    wrongBookTargetRoute(wrongItemId),
                    65
            );
        }

        if ("WRONG_BOOK_ENTRY".equals(trigger) || "WRONG_BOOK".equals(pageType)) {
            Long wrongItemId = resolveWrongItemId(userId, request);
            return new NudgePlan(
                    trigger.isBlank() ? "WRONG_BOOK_ENTRY" : trigger,
                    "REFLECT",
                    "REVIEW_AFTER_SOLUTION",
                    "先做一次错题复盘",
                    "我可以帮你把错题的错误原因和下次避免策略整理成复盘。",
                    wrongItemId == null ? targetRoute(request) : wrongBookTargetRoute(wrongItemId),
                    55
            );
        }

        if ("VIEWED_SOLUTION".equals(trigger)) {
            return new NudgePlan(
                    trigger,
                    "REFLECT",
                    "REVIEW_AFTER_SOLUTION",
                    "先别急着跳过",
                    "我可以帮你把错误原因和下次避免策略整理成复盘。",
                    targetRoute(request),
                    55
            );
        }

        if ("LEARNING_PATH_ENTRY".equals(trigger) || "LEARNING_PATH".equals(pageType)) {
            return new NudgePlan(
                    trigger.isBlank() ? "LEARNING_PATH_ENTRY" : trigger,
                    "RECOMMEND",
                    "RECOMMEND_REMEDIATION",
                    "下一步可以更聚焦",
                    "我可以根据当前节点给你一个更合适的练习顺序。",
                    targetRoute(request),
                    45
            );
        }

        return null;
    }

    private String chooseFailureAction(Long userId, Long problemId) {
        if (problemId == null) {
            return "HINT";
        }
        UserProblemInteraction interaction = interactionMapper.findByUserAndProblem(userId, problemId);
        if (interaction == null) {
            return "HINT";
        }
        int failures = value(interaction.getConsecutiveFailures());
        int hints = value(interaction.getHintCount());
        int diagnoses = value(interaction.getDiagnoseCount());
        String stage = interaction.getLearningStage() == null ? "" : interaction.getLearningStage();
        if (failures >= 3 || diagnoses >= 1 || "DIAGNOSED".equals(stage)) {
            return "EXPLAIN";
        }
        if (failures >= 2 || hints >= 1 || "HINTED".equals(stage)) {
            return "DIAGNOSE";
        }
        return "HINT";
    }

    private String goalFor(String actionType) {
        return switch (actionType) {
            case "GUIDE_IDEA" -> "GUIDE_INITIAL_THINKING";
            case "HINT" -> "GIVE_LIGHT_HINT";
            case "DIAGNOSE" -> "DIAGNOSE_ERROR_CAUSE";
            case "EXPLAIN" -> "EXPLAIN_CONCEPT";
            case "RECOMMEND" -> "RECOMMEND_REMEDIATION";
            case "REFLECT", "REVEAL_ANSWER" -> "REVIEW_AFTER_SOLUTION";
            default -> "GIVE_LIGHT_HINT";
        };
    }

    private String toStatus(String eventType, String feedbackType) {
        String event = safeUpper(eventType);
        String feedback = safeUpper(feedbackType);
        if ("SNOOZED".equals(event) || "LATER".equals(event) || "稍后".equals(feedback)) {
            return "SNOOZED";
        }
        if ("DISMISSED".equals(event) || "CLOSED".equals(event) || "NOT_USEFUL".equals(feedback)) {
            return "DISMISSED";
        }
        if ("COMPLETED".equals(event) || "USEFUL".equals(feedback)) {
            return "COMPLETED";
        }
        if ("EXPIRED".equals(event)) {
            return "EXPIRED";
        }
        if ("OPENED".equals(event)) {
            return "ACTIVE";
        }
        return "CLICKED";
    }

    private AgentCoachNudgeDTO toDto(AgentCoachNudge nudge) {
        AgentCoachNudgeDTO dto = new AgentCoachNudgeDTO();
        dto.setId(nudge.getId());
        dto.setRequestId(nudge.getRequestId());
        dto.setTriggerSource(nudge.getTriggerSource());
        dto.setActionType(nudge.getActionType());
        dto.setPedagogicalGoal(nudge.getPedagogicalGoal());
        dto.setTitle(nudge.getTitle());
        dto.setSummary(nudge.getSummary());
        dto.setTargetRoute(nudge.getTargetRoute());
        dto.setPriority(nudge.getPriority());
        dto.setStatus(nudge.getStatus());
        dto.setCreatedAt(nudge.getCreatedAt());
        dto.setUpdatedAt(nudge.getUpdatedAt());
        dto.setExpiresAt(nudge.getExpiresAt());
        return dto;
    }

    private String targetRoute(AgentCoachDecideRequestDTO request) {
        if (request.getWrongItemId() != null) {
            return wrongBookTargetRoute(request.getWrongItemId());
        }
        if (request.getCurrentRoute() != null && !request.getCurrentRoute().isBlank()) {
            return request.getCurrentRoute();
        }
        if (request.getProblemId() != null) {
            return "/problem/" + request.getProblemId();
        }
        if (request.getPathId() != null) {
            return "/learn";
        }
        return "/";
    }

    private String toJson(Map<String, Object> metadata) {
        return metadata == null || metadata.isEmpty() ? "{}" : JSON.toJSONString(metadata);
    }

    private Long resolveWrongItemId(Long userId, AgentCoachDecideRequestDTO request) {
        if (request.getWrongItemId() != null) {
            return request.getWrongItemId();
        }
        if (wrongBookService == null) {
            return null;
        }
        try {
            List<WrongBookItem> items = wrongBookService.getWrongBookItems(userId, null, null);
            if (items == null || items.isEmpty()) {
                return null;
            }
            return items.stream()
                    .filter(item -> item != null && item.getId() != null)
                    .filter(item -> item.getReviewStatus() == null || item.getReviewStatus() == 0)
                    .findFirst()
                    .or(() -> items.stream()
                            .filter(item -> item != null && item.getId() != null)
                            .findFirst())
                    .map(WrongBookItem::getId)
                    .orElse(null);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String wrongBookTargetRoute(Long wrongItemId) {
        return "/wrong-book/" + wrongItemId + "?coach=reflection";
    }

    private String safeUpper(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private int value(Integer value) {
        return value == null ? 0 : value;
    }

    private record NudgePlan(String triggerSource,
                             String actionType,
                             String pedagogicalGoal,
                             String title,
                             String summary,
                             String targetRoute,
                             int priority) {
    }
}
