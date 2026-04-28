package com.programming.service.wrongbookagent;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.programming.agent.dto.AgentDecisionDTO;
import com.programming.entity.AiMessage;
import com.programming.entity.AiSession;
import com.programming.entity.Problem;
import com.programming.entity.WrongBookItem;
import com.programming.mapper.AiSessionMapper;
import com.programming.mapper.UserProblemInteractionMapper;
import com.programming.service.AgentService;
import com.programming.service.WrongBookService;
import com.programming.vo.problemagent.CoachActionVO;
import com.programming.vo.problemagent.ProblemAgentSessionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class WrongBookAgentService {
    private static final String SESSION_TYPE = "wrong_book_review";
    private static final String REFLECTION_KIND = "wrong_book_reflection";
    private static final String CHAT_KIND = "wrong_book_chat";
    private static final String ACTION_MINIMAL_COUNTEREXAMPLE = "minimal_counterexample";
    private static final String ACTION_HINT = "hint";
    private static final String ACTION_SIMILAR_PROBLEM = "similar_problem";
    private static final String ACTION_REFLECTION_TASK_SUBMIT = "reflection_task_submit";
    private static final String TASK_SUMMARY = "summary";
    private static final String TASK_COUNTEREXAMPLE = "counterexample";
    private static final String TASK_FIX_IDEA = "fix_idea";
    private static final String STAGE_REFLECTING = "REFLECTING";
    private static final String STAGE_REVIEW_READY = "REVIEW_READY";

    private final AiSessionMapper aiSessionMapper;
    private final WrongBookService wrongBookService;
    private final AgentService agentService;

    @Autowired(required = false)
    private UserProblemInteractionMapper userProblemInteractionMapper;

    public WrongBookAgentService(AiSessionMapper aiSessionMapper,
                                 WrongBookService wrongBookService,
                                 AgentService agentService) {
        this.aiSessionMapper = aiSessionMapper;
        this.wrongBookService = wrongBookService;
        this.agentService = agentService;
    }

    public ProblemAgentSessionVO getLatestSession(Long userId, Long wrongItemId) {
        WrongBookItem item = requireWrongItem(userId, wrongItemId);
        AiSession session = aiSessionMapper.selectLatestWrongBookReviewSession(userId, item.getId());
        return session == null ? null : toSessionVO(session);
    }

    public ProblemAgentSessionVO getSession(Long userId, String sessionId) {
        AiSession session = requireOwnedWrongBookSession(userId, sessionId, null);
        return toSessionVO(session);
    }

    @Transactional
    public ProblemAgentSessionVO reflect(Long userId, Long wrongItemId) {
        WrongBookItem item = requireWrongItem(userId, wrongItemId);
        AiSession existing = aiSessionMapper.selectLatestWrongBookReviewSession(userId, item.getId());
        if (existing != null && hasReflection(existing.getSessionId())) {
            return toSessionVO(existing);
        }

        AiSession session = existing == null ? createSession(userId, item) : existing;
        AgentDecisionDTO decision = null;
        try {
            decision = agentService.processWrongBookReflection(userId, item.getId());
        } catch (Exception ignored) {
            // Keep the review page usable if the unified Agent service is temporarily unavailable.
        }
        insertAssistantMessage(session, item, reflectionText(decision), REFLECTION_KIND);
        Map<String, Object> snapshot = buildSnapshot(item, decision);
        appendAgentDecisionTrace(snapshot, "reflect", decision);
        session.setMetadataJson(JSON.toJSONString(snapshot));
        session.setUpdateTime(LocalDateTime.now());
        aiSessionMapper.updateSession(session);
        return toSessionVO(session);
    }

    private void handleCoachAction(Long userId, AiSession session, WrongBookItem item, WrongBookAgentChatRequestVO request) {
        String actionType = request.getActionType() == null ? "" : request.getActionType().trim();
        Map<String, Object> snapshot = normalizeSnapshot(parseMetadata(session.getMetadataJson()));
        incrementAttempt(snapshot);

        String userMessage = actionUserMessage(request);
        String assistantReply;
        switch (actionType) {
            case ACTION_MINIMAL_COUNTEREXAMPLE -> assistantReply = agentReplyOrFallback(
                    userId,
                    item,
                    request,
                    userMessage,
                    buildMinimalCounterexampleReply(item)
            );
            case ACTION_HINT -> {
                int hintLevel = clampHintLevel(request.getHintLevel());
                learningState(snapshot).put("hintLevel", hintLevel);
                updateHintInteraction(userId, item);
                assistantReply = agentReplyOrFallback(
                        userId,
                        item,
                        request,
                        userMessage,
                        buildHintReply(item, hintLevel)
                );
            }
            case ACTION_SIMILAR_PROBLEM -> assistantReply = buildRecommendationReply(userId, item);
            case ACTION_REFLECTION_TASK_SUBMIT -> {
                String fallback = handleReflectionTask(userId, item, snapshot, request);
                assistantReply = agentReplyOrFallback(userId, item, request, userMessage, fallback);
            }
            default -> assistantReply = agentReplyOrFallback(
                    userId,
                    item,
                    request,
                    userMessage,
                    buildChatReply(userId, blankToDefault(request.getMessage(), actionType), item)
            );
        }

        session.setMetadataJson(JSON.toJSONString(snapshot));
        appendAgentDecisionTrace(snapshot, actionType.isBlank() ? "chat" : actionType, null);
        session.setMetadataJson(JSON.toJSONString(snapshot));
        insertUserMessage(session, item, userMessage, CHAT_KIND);
        insertAssistantMessage(session, item, assistantReply, CHAT_KIND);
        session.setUpdateTime(LocalDateTime.now());
        aiSessionMapper.updateSession(session);
    }

    private void updateHintInteraction(Long userId, WrongBookItem item) {
        if (userProblemInteractionMapper == null || item == null || item.getProblemId() == null) {
            return;
        }
        userProblemInteractionMapper.incrementHintCount(userId, item.getProblemId());
    }

    private void updateReviewReadyInteraction(Long userId, WrongBookItem item, Map<String, Object> snapshot) {
        if (userProblemInteractionMapper == null || item == null || item.getProblemId() == null) {
            return;
        }
        Object weakPoints = snapshot.get("weakPoints");
        String weakPointsJson = weakPoints == null ? null : JSON.toJSONString(weakPoints);
        userProblemInteractionMapper.updateLearningState(
                userId,
                item.getProblemId(),
                stringValue(learningState(snapshot).get("errorPattern")),
                weakPointsJson,
                STAGE_REVIEW_READY
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> learningState(Map<String, Object> snapshot) {
        Object raw = snapshot.get("learningState");
        if (raw instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        Map<String, Object> state = defaultLearningState(snapshot);
        snapshot.put("learningState", state);
        return state;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> taskState(Map<String, Object> state) {
        Object raw = state.get("tasks");
        if (raw instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        Map<String, Object> tasks = defaultTasks();
        state.put("tasks", tasks);
        return tasks;
    }

    private String handleReflectionTask(Long userId, WrongBookItem item, Map<String, Object> snapshot, WrongBookAgentChatRequestVO request) {
        String taskKey = normalizeTaskKey(request.getTaskType());
        String answer = request.getAnswer() == null ? "" : request.getAnswer().trim();
        if (taskKey == null) {
            throw new RuntimeException("taskType is required");
        }
        if (answer.isBlank()) {
            throw new RuntimeException("answer is required");
        }

        Map<String, Object> state = learningState(snapshot);
        Map<String, Object> tasks = taskState(state);
        tasks.put(taskKey, task(true, answer));
        state.put("currentStage", allTasksSubmitted(tasks) ? STAGE_REVIEW_READY : STAGE_REFLECTING);

        if (STAGE_REVIEW_READY.equals(state.get("currentStage"))) {
            updateReviewReadyInteraction(userId, item, snapshot);
            return "这三项复盘任务已经完成。现在可以回到上方点击“标记已复习”，或者继续在聊天区追问细节。";
        }
        return "已记录这一步复盘。继续完成剩下的任务，把错误原因、最小反例和修正思路串起来。";
    }

    private String actionUserMessage(WrongBookAgentChatRequestVO request) {
        if (request.getMessage() != null && !request.getMessage().isBlank()) {
            return request.getMessage().trim();
        }
        String actionType = request.getActionType() == null ? "" : request.getActionType().trim();
        if (ACTION_REFLECTION_TASK_SUBMIT.equals(actionType)) {
            return "提交复盘任务：" + blankToDefault(request.getAnswer(), "");
        }
        if (ACTION_MINIMAL_COUNTEREXAMPLE.equals(actionType)) {
            return "给我一个最小反例";
        }
        if (ACTION_HINT.equals(actionType)) {
            return "提示我关键点";
        }
        if (ACTION_SIMILAR_PROBLEM.equals(actionType)) {
            return "再来一道类似题";
        }
        return "继续帮我复盘";
    }

    private void incrementAttempt(Map<String, Object> snapshot) {
        Map<String, Object> state = learningState(snapshot);
        state.put("attemptCount", intValue(state.get("attemptCount"), 0) + 1);
    }

    private int clampHintLevel(Integer hintLevel) {
        if (hintLevel == null) {
            return 1;
        }
        return Math.max(1, Math.min(4, hintLevel));
    }

    @Transactional
    public ProblemAgentSessionVO chat(Long userId, WrongBookAgentChatRequestVO request) {
        if (request == null || request.getWrongItemId() == null) {
            throw new RuntimeException("wrongItemId is required");
        }
        boolean hasAction = request.getActionType() != null && !request.getActionType().isBlank();
        boolean hasMessage = request.getMessage() != null && !request.getMessage().isBlank();
        boolean hasAnswer = request.getAnswer() != null && !request.getAnswer().isBlank();
        if (!hasAction && !hasMessage && !hasAnswer) {
            throw new RuntimeException("message is required");
        }

        WrongBookItem item = requireWrongItem(userId, request.getWrongItemId());
        AiSession session = request.getSessionId() == null || request.getSessionId().isBlank()
                ? aiSessionMapper.selectLatestWrongBookReviewSession(userId, item.getId())
                : requireOwnedWrongBookSession(userId, request.getSessionId(), item.getId());
        if (session == null) {
            session = createSession(userId, item);
        }

        if (hasAction) {
            handleCoachAction(userId, session, item, request);
            return toSessionVO(session);
        }

        Map<String, Object> snapshot = normalizeSnapshot(parseMetadata(session.getMetadataJson()));
        appendAgentDecisionTrace(snapshot, "chat", null);
        session.setMetadataJson(JSON.toJSONString(snapshot));
        insertUserMessage(session, item, request.getMessage(), CHAT_KIND);
        String assistantReply = isRecommendationIntent(request.getMessage())
                ? buildRecommendationReply(userId, item)
                : agentReplyOrFallback(
                        userId,
                        item,
                        request,
                        request.getMessage(),
                        buildChatReply(userId, request.getMessage(), item)
                );
        insertAssistantMessage(session, item, assistantReply, CHAT_KIND);
        session.setUpdateTime(LocalDateTime.now());
        aiSessionMapper.updateSession(session);
        return toSessionVO(session);
    }

    private String agentReplyOrFallback(Long userId,
                                        WrongBookItem item,
                                        WrongBookAgentChatRequestVO request,
                                        String userMessage,
                                        String fallback) {
        try {
            AgentDecisionDTO decision = agentService.processWrongBookReviewChat(
                    userId,
                    item,
                    userMessage,
                    actionHintFor(request),
                    request == null ? null : request.getTaskType(),
                    request == null ? null : request.getAnswer(),
                    request == null ? null : request.getHintLevel()
            );
            if (decision != null) {
                if (isAgentFailureDecision(decision)) {
                    return fallback;
                }
                String reply = reflectionText(decision);
                if (reply != null && !reply.isBlank()) {
                    return reply;
                }
            }
        } catch (Exception ignored) {
            // Keep the review page usable if the unified Agent service is temporarily unavailable.
        }
        return fallback;
    }

    private String actionHintFor(WrongBookAgentChatRequestVO request) {
        if (request == null || request.getActionType() == null || request.getActionType().isBlank()) {
            return "chat";
        }
        return request.getActionType().trim();
    }

    private WrongBookItem requireWrongItem(Long userId, Long wrongItemId) {
        WrongBookItem item = wrongBookService.getWrongBookItemById(userId, wrongItemId);
        if (item == null || item.getId() == null) {
            throw new RuntimeException("Wrong book item not found");
        }
        return item;
    }

    private AiSession requireOwnedWrongBookSession(Long userId, String sessionId, Long wrongItemId) {
        AiSession session = aiSessionMapper.selectSessionBySessionId(sessionId);
        boolean wrongType = session == null || !SESSION_TYPE.equals(session.getSessionType());
        boolean wrongUser = session == null || session.getUserId() == null || !session.getUserId().equals(userId);
        boolean wrongItem = wrongItemId != null && !wrongItemId.equals(session.getRelatedWrongItemId());
        if (wrongType || wrongUser || wrongItem) {
            throw new RuntimeException("Wrong book review session not found");
        }
        return session;
    }

    private AiSession createSession(Long userId, WrongBookItem item) {
        AiSession session = new AiSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setUserId(userId);
        session.setTopic("错题复盘 - 题目 #" + item.getProblemId());
        session.setStatus(1);
        session.setSessionType(SESSION_TYPE);
        session.setRelatedProblemId(item.getProblemId());
        session.setRelatedWrongItemId(item.getId());
        session.setLastSubmitId(item.getSubmitId());
        session.setMetadataJson(JSON.toJSONString(buildSnapshot(item, null)));
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        aiSessionMapper.insertSession(session);
        return session;
    }

    private boolean hasReflection(String sessionId) {
        return aiSessionMapper.selectMessagesBySessionId(sessionId).stream()
                .anyMatch(message -> REFLECTION_KIND.equals(message.getMessageKind()));
    }

    private void insertUserMessage(AiSession session, WrongBookItem item, String content, String kind) {
        AiMessage message = baseMessage(session, item, kind);
        message.setRole("user");
        message.setContent(content);
        aiSessionMapper.insertMessage(message);
    }

    private void insertAssistantMessage(AiSession session, WrongBookItem item, String content, String kind) {
        AiMessage message = baseMessage(session, item, kind);
        message.setRole("assistant");
        message.setContent(content);
        aiSessionMapper.insertMessage(message);
    }

    private AiMessage baseMessage(AiSession session, WrongBookItem item, String kind) {
        AiMessage message = new AiMessage();
        message.setSessionId(session.getSessionId());
        message.setRelatedProblemId(item.getProblemId());
        message.setRelatedSubmitId(item.getSubmitId());
        message.setMessageKind(kind);
        message.setCreateTime(LocalDateTime.now());
        return message;
    }

    private String reflectionText(AgentDecisionDTO decision) {
        if (decision == null || isAgentFailureDecision(decision)) {
            return "我暂时无法生成复盘。你可以先从错误信息、失败用例和边界条件三处重新检查。";
        }
        if (decision.getMainResponse() != null && !decision.getMainResponse().isBlank()) {
            return decision.getMainResponse();
        }
        if (decision.getContent() != null && !decision.getContent().isBlank()) {
            return decision.getContent();
        }
        return "这道错题建议先复盘错误原因，再写出下次避免同类错误的检查点。";
    }

    private boolean isAgentFailureDecision(AgentDecisionDTO decision) {
        if (decision == null) {
            return true;
        }
        String strategy = firstNonBlank(decision.getSelectedStrategy(), "");
        String errorTag = firstNonBlank(decision.getErrorTag(), "");
        return "AGENT_FAILURE".equals(strategy)
                || "FAILURE".equals(strategy)
                || "LOCAL_FALLBACK".equals(strategy)
                || "MODEL_TIMEOUT".equals(errorTag)
                || "LLM_UNAVAILABLE".equals(errorTag)
                || "MODEL_NOT_FOUND".equals(errorTag)
                || "MODEL_HTTP_ERROR".equals(errorTag)
                || "MODEL_ERROR".equals(errorTag)
                || "INVALID_JSON".equals(errorTag)
                || "TOOL_MISSING".equals(errorTag)
                || "CONTEXT_INSUFFICIENT".equals(errorTag);
    }

    private String buildChatReply(Long userId, String userMessage, WrongBookItem item) {
        if (isRecommendationIntent(userMessage)) {
            return buildRecommendationReply(userId, item);
        }
        if (isCodeIntent(userMessage)) {
            return buildReferenceCodeReply(item);
        }
        if (isComparisonIntent(userMessage)) {
            return buildComparisonReply(item);
        }
        if (isCompleteIdeaIntent(userMessage)) {
            return buildExpandedReviewReply(item);
        }
        StringBuilder builder = new StringBuilder();
        builder.append("我们就围绕这条错题继续看。\n\n");
        builder.append("你的问题是：").append(userMessage.trim()).append("\n\n");
        builder.append("结合当前记录，错误信息是「")
                .append(blankToDefault(item.getErrorMessage(), "未记录"))
                .append("」，关联知识点是「")
                .append(blankToDefault(item.getKnowledgePoints(), "未标注"))
                .append("」。建议你先回答两个检查点：\n");
        builder.append("1. 这段代码在哪个输入上和预期不一致？\n");
        builder.append("2. 这个不一致来自条件判断、边界处理，还是数据结构选择？\n\n");
        builder.append("你可以把失败用例或你怀疑的代码片段发给我，我再继续帮你定位。");
        return builder.toString();
    }

    private String blankToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private boolean isRecommendationIntent(String userMessage) {
        if (userMessage == null) {
            return false;
        }
        String text = userMessage.trim().toLowerCase();
        if (text.contains("推荐")
                || text.contains("相似")
                || text.contains("类似")
                || text.contains("巩固")
                || text.contains("练习")
                || text.contains("下一题")
                || text.contains("刷题")) {
            return true;
        }
        return text.contains("推荐")
                || text.contains("相似")
                || text.contains("类似")
                || text.contains("巩固")
                || text.contains("练习")
                || text.contains("下一题")
                || text.contains("刷题");
    }

    private boolean isCompleteIdeaIntent(String userMessage) {
        if (userMessage == null) {
            return false;
        }
        String text = userMessage.trim().toLowerCase();
        return text.contains("完整思路")
                || text.contains("解题思路")
                || text.contains("讲清楚")
                || text.contains("详细讲")
                || text.contains("怎么做")
                || text.contains("思路是什么");
    }

    private boolean isCodeIntent(String userMessage) {
        if (userMessage == null) {
            return false;
        }
        String text = userMessage.trim().toLowerCase();
        return text.contains("参考代码")
                || text.contains("完整代码")
                || text.contains("代码实现")
                || text.contains("给我代码")
                || text.contains("写代码")
                || text.contains("solution");
    }

    private boolean isComparisonIntent(String userMessage) {
        if (userMessage == null) {
            return false;
        }
        String text = userMessage.trim().toLowerCase();
        return text.contains("别的解法")
                || text.contains("其他解法")
                || text.contains("另一种")
                || text.contains("不同解法")
                || text.contains("比较")
                || text.contains("优化");
    }

    private String buildExpandedReviewReply(WrongBookItem item) {
        StringBuilder builder = new StringBuilder();
        builder.append("完整思路可以按「问题建模 -> 数据结构 -> 执行顺序 -> 边界检查」来复盘。\n\n");
        builder.append("1. 问题建模：先明确输入里哪些信息需要被快速查询，哪些信息只需要顺序遍历。\n");
        builder.append("2. 数据结构：如果知识点涉及「")
                .append(blankToDefault(item.getKnowledgePoints(), "数组、哈希表或边界处理"))
                .append("」，优先考虑用映射、集合或双指针减少重复扫描。\n");
        builder.append("3. 执行顺序：每处理一个元素时，先判断它和已有状态能否构成答案，再更新状态，避免把当前元素错误地和自己匹配。\n");
        builder.append("4. 错误复盘：这次记录的错误信息是「")
                .append(blankToDefault(item.getErrorMessage(), "未记录"))
                .append("」，重点检查边界输入、重复元素、空结果和索引返回顺序。\n\n");
        builder.append("复杂度上，理想目标通常是把暴力的 O(n^2) 降到 O(n) 或 O(n log n)，空间换时间是否值得取决于题目约束。");
        return builder.toString();
    }

    private String buildReferenceCodeReply(WrongBookItem item) {
        StringBuilder builder = new StringBuilder();
        builder.append("可以。错题复盘阶段可以看参考代码，但建议你把它当成对照材料，而不是直接记答案。\n\n");
        builder.append("参考代码的组织方式可以是：\n\n");
        builder.append("```java\n");
        builder.append("class Solution {\n");
        builder.append("    // 根据题目约束补全方法签名，例如 twoSum(int[] nums, int target)\n");
        builder.append("    // 核心写法：遍历输入 -> 查询互补状态 -> 更新状态 -> 返回结果\n");
        builder.append("}\n");
        builder.append("```\n\n");
        builder.append("你这次提交的代码片段是：\n\n");
        builder.append("```java\n");
        builder.append(blankToDefault(item.getCode(), "// 当前错题没有保存提交代码"));
        builder.append("\n```\n\n");
        builder.append("对照时重点看三点：方法签名是否匹配、状态更新顺序是否正确、边界输入是否覆盖。");
        return builder.toString();
    }

    private String buildComparisonReply(WrongBookItem item) {
        StringBuilder builder = new StringBuilder();
        builder.append("可以，错题复盘里适合比较不同解法，这能帮你建立迁移能力。\n\n");
        builder.append("一种解法是直观暴力：枚举所有候选关系，优点是容易写，缺点是复杂度高。\n");
        builder.append("另一种解法是利用数据结构保存已处理状态，比如哈希表、集合、栈或队列，把重复查找变成快速查询。\n");
        builder.append("还有一种方向是排序、双指针或前缀信息，适合题目允许重排或需要区间关系时使用。\n\n");
        builder.append("比较时不要只看代码长短，重点看：时间复杂度、空间复杂度、是否保持原索引、是否容易处理重复值和边界。");
        builder.append(" 这条错题关联知识点是「")
                .append(blankToDefault(item.getKnowledgePoints(), "未标注"))
                .append("」，优先把它归到对应模式里复盘。");
        return builder.toString();
    }

    private String buildMinimalCounterexampleReply(WrongBookItem item) {
        String issue = blankToDefault(item.getKnowledgePoints(), "边界条件");
        return "先用最小反例验证这类错误：只保留能触发问题的最少输入。"
                + "\n\n建议你从这三个方向里选一个写下来："
                + "\n1. 空输入或只有一个元素。"
                + "\n2. 正好卡在边界值上的输入。"
                + "\n3. 会让变量更新顺序暴露问题的输入。"
                + "\n\n这条错题关联的是「" + issue + "」，先把失败用例写进左侧复盘任务。";
    }

    private String buildHintReply(WrongBookItem item, int hintLevel) {
        String issue = blankToDefault(item.getKnowledgePoints(), "当前错误点");
        return switch (hintLevel) {
            case 1 -> "先给你一个方向提示：先不要改代码，先定位是哪一个边界或状态变化让输出偏离预期。关注「" + issue + "」。";
            case 2 -> "关键点提示：检查判断条件和状态更新的先后顺序。很多错题不是思路错，而是把当前元素过早写入了状态。";
            case 3 -> "思路提示：用一个最小失败用例手动走一遍，每一步记录变量变化，再对照预期输出找第一次分叉的位置。";
            default -> "伪代码提示：先读取当前输入 -> 判断是否已经满足目标 -> 再更新状态 -> 最后处理返回值。把你的代码按这个顺序对照一遍。";
        };
    }

    private String buildRecommendationReply(Long userId, WrongBookItem item) {
        List<Problem> problems = wrongBookService.getRecommendedProblems(userId, item.getId(), 5);
        if (problems == null || problems.isEmpty()) {
            return "可以。当前还没有找到可直接跳转的相似题，建议先按这条错题的知识点做一组专项练习："
                    + blankToDefault(item.getKnowledgePoints(), "相关基础知识")
                    + "。做完后回到这里复盘是否还会出现同类错误。";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("可以。结合这条错题，我推荐你先做这几道相似题来巩固：\n\n");
        int index = 1;
        for (Problem problem : problems) {
            if (problem == null || problem.getId() == null) {
                continue;
            }
            builder.append(index++)
                    .append(". ")
                    .append(blankToDefault(problem.getTitle(), "题目 #" + problem.getId()))
                    .append("（")
                    .append(difficultyLabel(problem.getDifficulty()))
                    .append("）")
                    .append("：/problem/")
                    .append(problem.getId())
                    .append("\n");
        }
        builder.append("\n建议顺序：先独立做第一题，再回来对照这条错题的错误原因，看是否还会在同一个知识点上重复失误。");
        return builder.toString();
    }

    private String difficultyLabel(Integer difficulty) {
        if (difficulty == null) {
            return "难度未标注";
        }
        return switch (difficulty) {
            case 0 -> "简单";
            case 1 -> "中等";
            case 2 -> "困难";
            default -> "难度 " + difficulty;
        };
    }

    private Map<String, Object> buildSnapshot(WrongBookItem item, AgentDecisionDTO decision) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("wrongItemId", item.getId());
        snapshot.put("problemId", item.getProblemId());
        snapshot.put("submitId", item.getSubmitId());
        snapshot.put("language", item.getLanguage());
        snapshot.put("errorMessage", item.getErrorMessage());
        snapshot.put("knowledgePoints", item.getKnowledgePoints());
        if (decision != null) {
            snapshot.put("requestId", decision.getResponseId());
            snapshot.put("actionType", decision.getActionType());
            snapshot.put("errorTag", decision.getErrorTag());
            snapshot.put("pedagogicalGoal", decision.getPedagogicalGoal());
            snapshot.put("contentType", decision.getContentType());
            snapshot.put("nextSuggestion", decision.getNextSuggestion());
            snapshot.put("weakPoints", decision.getWeakPoints());
        }
        snapshot.put("currentJudgement", buildCurrentJudgement(snapshot));
        snapshot.put("learningState", defaultLearningState(snapshot));
        return normalizeSnapshot(snapshot);
    }

    @SuppressWarnings("unchecked")
    private void appendAgentDecisionTrace(Map<String, Object> snapshot, String actionHint, AgentDecisionDTO decision) {
        if (snapshot == null) {
            return;
        }
        List<Map<String, Object>> traces = new ArrayList<>();
        Object raw = snapshot.get("agentDecisionTraces");
        if (raw instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    traces.add(new LinkedHashMap<>((Map<String, Object>) map));
                }
            }
        }

        Map<String, Object> trace = new LinkedHashMap<>();
        trace.put("decision_stage", "final");
        trace.put("selected_signals", decision == null || decision.getUsedToolSignals() == null ? List.of() : decision.getUsedToolSignals());
        trace.put("dropped_signals", List.of());
        trace.put("user_intent_inferred", blankToDefault(actionHint, "chat"));
        trace.put("teaching_goal_inferred", firstNonBlank(
                decision == null ? null : decision.getTeachingGoal(),
                decision == null ? null : decision.getPedagogicalGoal(),
                "wrong_book_review"
        ));
        trace.put("risk_level_inferred", firstNonBlank(decision == null ? null : decision.getRiskLevel(), "normal"));
        trace.put("enforcer_result", decision != null && Boolean.FALSE.equals(decision.getExecuted()) ? "block" : "pass");
        trace.put("latency_breakdown_ms", Map.of());
        trace.put("quality_flags", decision == null || decision.getQualityFlags() == null ? List.of() : decision.getQualityFlags());

        traces.add(trace);
        while (traces.size() > 3) {
            traces.remove(0);
        }
        snapshot.put("agentDecisionTraces", traces);
    }

    private ProblemAgentSessionVO toSessionVO(AiSession session) {
        List<AiMessage> messages = aiSessionMapper.selectMessagesBySessionId(session.getSessionId());
        ProblemAgentSessionVO response = new ProblemAgentSessionVO();
        response.setSessionId(session.getSessionId());
        response.setMessages(normalizeMessagesForDisplay(messages));
        response.setSummary("这是一条错题复盘会话，可以继续追问错误原因、边界条件和下次避免策略。");
        response.setContextSnapshot(normalizeSnapshot(parseMetadata(session.getMetadataJson())));
        response.setCanRevealFullSolution(false);
        response.setActions(defaultActions());
        return response;
    }

    private List<AiMessage> normalizeMessagesForDisplay(List<AiMessage> messages) {
        if (messages == null) {
            return new ArrayList<>();
        }
        return messages.stream()
                .peek(message -> message.setContent(normalizeAgentFailureCopy(message.getContent())))
                .toList();
    }

    private String normalizeAgentFailureCopy(String content) {
        if (content == null || content.isBlank()) {
            return content;
        }
        if (content.contains("Agent service is temporarily unavailable")
                || content.contains("Python Agent service unavailable")
                || content.contains("MODEL_TIMEOUT")
                || content.contains("LLM_UNAVAILABLE")
                || content.contains("大模型服务暂时不可用")
                || content.contains("模型响应超时")
                || content.contains("Agent 决策")) {
            return "AI 服务暂时没有返回稳定结果。你可以重新发送一次，或者把问题缩小到一个具体动作：要提示、要定位错误、要最小失败用例，或要类似题推荐。\n\n建议补充代码片段、报错信息和你已经尝试过的思路。";
        }
        return content;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> normalizeSnapshot(Map<String, Object> source) {
        Map<String, Object> snapshot = source == null ? new LinkedHashMap<>() : new LinkedHashMap<>(source);

        Object currentJudgement = snapshot.get("currentJudgement");
        if (!(currentJudgement instanceof Map<?, ?>)) {
            snapshot.put("currentJudgement", buildCurrentJudgement(snapshot));
        }

        Object learningState = snapshot.get("learningState");
        Map<String, Object> state = learningState instanceof Map<?, ?>
                ? new LinkedHashMap<>((Map<String, Object>) learningState)
                : defaultLearningState(snapshot);
        normalizeLearningState(state, snapshot);
        snapshot.put("learningState", state);
        return snapshot;
    }

    private void normalizeLearningState(Map<String, Object> state, Map<String, Object> snapshot) {
        state.put("attemptCount", intValue(state.get("attemptCount"), 0));
        state.put("hintLevel", clampHintLevel(intValue(state.get("hintLevel"), 1)));
        state.put("errorPattern", blankToDefault(stringValue(state.get("errorPattern")), inferErrorPattern(snapshot)));
        state.put("currentStage", blankToDefault(stringValue(state.get("currentStage")), STAGE_REFLECTING));

        Map<String, Object> tasks = taskState(state);
        Map<String, Object> normalizedTasks = defaultTasks();
        mergeTask(normalizedTasks, tasks, "summary");
        mergeTask(normalizedTasks, tasks, "counterexample");
        mergeTask(normalizedTasks, tasks, "fixIdea");
        state.put("tasks", normalizedTasks);
        if (allTasksSubmitted(normalizedTasks)) {
            state.put("currentStage", STAGE_REVIEW_READY);
        }
    }

    @SuppressWarnings("unchecked")
    private void mergeTask(Map<String, Object> normalizedTasks, Map<String, Object> sourceTasks, String key) {
        Object raw = sourceTasks.get(key);
        if (!(raw instanceof Map<?, ?> map)) {
            return;
        }
        Map<String, Object> source = (Map<String, Object>) map;
        normalizedTasks.put(key, task(Boolean.TRUE.equals(source.get("submitted")), stringValue(source.get("answer"))));
    }

    private Map<String, Object> buildCurrentJudgement(Map<String, Object> snapshot) {
        Map<String, Object> judgement = new LinkedHashMap<>();
        judgement.put("errorType", inferErrorType(snapshot));
        judgement.put("stage", "思路正确，细节错误");
        judgement.put("mainIssue", inferMainIssue(snapshot));
        return judgement;
    }

    private Map<String, Object> defaultLearningState(Map<String, Object> snapshot) {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("attemptCount", 0);
        state.put("hintLevel", 1);
        state.put("errorPattern", inferErrorPattern(snapshot));
        state.put("currentStage", STAGE_REFLECTING);
        state.put("tasks", defaultTasks());
        return state;
    }

    private Map<String, Object> defaultTasks() {
        Map<String, Object> tasks = new LinkedHashMap<>();
        tasks.put("summary", task(false, ""));
        tasks.put("counterexample", task(false, ""));
        tasks.put("fixIdea", task(false, ""));
        return tasks;
    }

    private Map<String, Object> task(boolean submitted, String answer) {
        Map<String, Object> task = new LinkedHashMap<>();
        task.put("submitted", submitted);
        task.put("answer", answer == null ? "" : answer);
        return task;
    }

    @SuppressWarnings("unchecked")
    private boolean allTasksSubmitted(Map<String, Object> tasks) {
        for (String key : List.of("summary", "counterexample", "fixIdea")) {
            Object raw = tasks.get(key);
            if (!(raw instanceof Map<?, ?> map) || !Boolean.TRUE.equals(((Map<String, Object>) map).get("submitted"))) {
                return false;
            }
        }
        return true;
    }

    private String normalizeTaskKey(String taskType) {
        if (taskType == null || taskType.isBlank()) {
            return null;
        }
        return switch (taskType.trim()) {
            case TASK_SUMMARY -> "summary";
            case TASK_COUNTEREXAMPLE -> "counterexample";
            case TASK_FIX_IDEA, "fixIdea" -> "fixIdea";
            default -> null;
        };
    }

    private int intValue(Object value, int fallback) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String inferErrorType(Map<String, Object> snapshot) {
        String errorMessage = stringValue(snapshot.get("errorMessage")).toLowerCase();
        String errorPattern = inferErrorPattern(snapshot);
        if (errorMessage.contains("compile") || errorMessage.contains("syntax")) {
            return "编译错误";
        }
        if (errorMessage.contains("runtime") || errorMessage.contains("exception")) {
            return "运行错误";
        }
        if (errorMessage.contains("time") || errorPattern.contains("TLE")) {
            return "复杂度问题";
        }
        return "逻辑错误";
    }

    private String inferMainIssue(Map<String, Object> snapshot) {
        String knowledgePoints = stringValue(snapshot.get("knowledgePoints"));
        if (!knowledgePoints.isBlank()) {
            return knowledgePoints;
        }
        Object weakPoints = snapshot.get("weakPoints");
        if (weakPoints instanceof List<?> list && !list.isEmpty()) {
            return String.join(" + ", list.stream().map(String::valueOf).toList());
        }
        String nextSuggestion = stringValue(snapshot.get("nextSuggestion"));
        return nextSuggestion.isBlank() ? "边界条件 + 状态更新顺序" : nextSuggestion;
    }

    private String inferErrorPattern(Map<String, Object> snapshot) {
        String errorTag = stringValue(snapshot.get("errorTag"));
        if (!errorTag.isBlank()) {
            return errorTag;
        }
        String errorMessage = stringValue(snapshot.get("errorMessage")).toLowerCase();
        if (errorMessage.contains("wrong answer")) {
            return "WRONG_ANSWER";
        }
        if (errorMessage.contains("time")) {
            return "TLE";
        }
        if (errorMessage.contains("runtime") || errorMessage.contains("exception")) {
            return "RUNTIME_ERROR";
        }
        if (errorMessage.contains("compile") || errorMessage.contains("syntax")) {
            return "COMPILE_ERROR";
        }
        return "UNKNOWN";
    }

    private List<CoachActionVO> defaultActions() {
        return List.of(
                action("reflection_task_start", "我先自己总结问题", "先完成当前复盘任务的第一项。"),
                action(ACTION_MINIMAL_COUNTEREXAMPLE, "给我一个最小反例", "请给我一个最小失败用例。"),
                action(ACTION_HINT, "提示我关键点", "请给我一个关键提示。"),
                action(ACTION_SIMILAR_PROBLEM, "再来一道类似题", "推荐一道类似题。")
        );
    }

    private Map<String, Object> parseMetadata(String metadataJson) {
        if (metadataJson == null || metadataJson.isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            JSONObject json = JSON.parseObject(metadataJson);
            return new LinkedHashMap<>(json);
        } catch (Exception ignored) {
            return new LinkedHashMap<>();
        }
    }

    private CoachActionVO action(String type, String label, String prompt) {
        CoachActionVO action = new CoachActionVO();
        action.setType(type);
        action.setLabel(label);
        action.setPrompt(prompt);
        return action;
    }
}
