package com.programming.service.problemagent;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.programming.agent.dto.AgentDecisionDTO;
import com.programming.entity.AiMessage;
import com.programming.entity.AiSession;
import com.programming.mapper.AiSessionMapper;
import com.programming.service.AgentService;
import com.programming.vo.problemagent.CoachActionVO;
import com.programming.vo.problemagent.CoachRecommendationCardVO;
import com.programming.vo.problemagent.ProblemAgentChatRequestVO;
import com.programming.vo.problemagent.ProblemAgentResponseVO;
import com.programming.vo.problemagent.ProblemAgentSessionVO;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ProblemCoachService {
    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```(?:[a-zA-Z0-9_+\\-]+)?\\s*(.*?)```", Pattern.DOTALL);

    @Autowired
    private ProblemCoachContextAssembler contextAssembler;

    @Autowired
    private ProblemCoachPromptBuilder promptBuilder;

    @Autowired
    private ProblemCoachRecommendationService recommendationService;

    @Autowired
    private ProblemCoachAccessPolicy accessPolicy;

    @Autowired
    private AgentService agentService;

    @Autowired
    private AiSessionMapper aiSessionMapper;

    @Value("${programming.ai.api-key:}")
    private String apiKey;

    @Value("${programming.ai.api-url}")
    private String apiUrl;

    @Value("${programming.ai.prompt}")
    private String defaultPrompt;

    private final OkHttpClient client = new OkHttpClient();

    public ProblemAgentResponseVO chat(Long userId, ProblemAgentChatRequestVO request) {
        ProblemCoachContext context = contextAssembler.assemble(userId, request);
        context.setAiAvailable(isAiAvailable());

        boolean revealFullSolution = accessPolicy.canRevealFullSolution(context.isHasFailure(), request);
        AiSession session = createOrReuseSession(userId, context, request);

        AgentDecisionDTO decision = agentService.processProblemCoach(context, request);
        List<CoachRecommendationCardVO> recommendations = recommendationService.buildRecommendations(context, 3);
        String summary = buildSummary(context, revealFullSolution, decision);
        String reply = responseText(decision);
        boolean revealExecuted = Boolean.TRUE.equals(decision.getExecuted())
                && "REVEAL_ANSWER".equals(decision.getActionType());
        String draftCode = revealExecuted ? extractDraftCode(reply) : null;
        List<CoachActionVO> actions = buildActions(context, revealFullSolution, draftCode);
        Map<String, Object> snapshot = buildContextSnapshot(context, recommendations, draftCode != null, decision);

        persistMessages(session, context, request, reply, revealExecuted);
        session.setLastSubmitId(trustedSubmitId(context));
        session.setMetadataJson(JSON.toJSONString(snapshot));
        session.setUpdateTime(LocalDateTime.now());
        aiSessionMapper.updateSession(session);

        ProblemAgentResponseVO response = new ProblemAgentResponseVO();
        response.setSessionId(session.getSessionId());
        response.setSummary(summary);
        response.setReply(reply);
        response.setRecommendations(recommendations);
        response.setActions(actions);
        response.setDraftCode(draftCode);
        response.setCanRevealFullSolution(context.isHasFailure());
        response.setRequestId(decision.getResponseId());
        response.setActionType(decision.getActionType());
        response.setPedagogicalGoal(decision.getPedagogicalGoal());
        response.setContentType(decision.getContentType());
        response.setNextSuggestion(decision.getNextSuggestion());
        response.setErrorTag(decision.getErrorTag());
        response.setWeakPoints(decision.getWeakPoints() == null ? new ArrayList<>() : decision.getWeakPoints());
        response.setContextSnapshot(snapshot);
        return response;
    }

    public ProblemAgentSessionVO getSession(Long userId, String sessionId) {
        AiSession session = requireOwnedSession(userId, sessionId);
        return toSessionVO(session);
    }

    public ProblemAgentSessionVO getLatestSession(Long userId, Long problemId) {
        AiSession session = aiSessionMapper.selectLatestProblemCoachSession(userId, problemId);
        if (session == null) {
            return null;
        }
        return toSessionVO(session);
    }

    private AiSession createOrReuseSession(Long userId, ProblemCoachContext context, ProblemAgentChatRequestVO request) {
        AiSession existing = context.getSession();
        if (existing != null) {
            if (existing.getUserId() == null || !existing.getUserId().equals(userId)) {
                throw new RuntimeException("Session not found");
            }
            existing.setTopic(buildTopic(context));
            existing.setSessionType("problem_coach");
            existing.setRelatedProblemId(context.getCurrentProblem().getId());
            existing.setLastSubmitId(trustedSubmitId(context));
            existing.setStatus(1);
            return existing;
        }

        AiSession session = new AiSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setUserId(userId);
        session.setTopic(buildTopic(context));
        session.setStatus(1);
        session.setSessionType("problem_coach");
        session.setRelatedProblemId(context.getCurrentProblem().getId());
        session.setLastSubmitId(trustedSubmitId(context));
        session.setMetadataJson("{}");
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        aiSessionMapper.insertSession(session);
        return session;
    }

    private Long trustedSubmitId(ProblemCoachContext context) {
        return context.getLatestSubmit() == null ? null : context.getLatestSubmit().getId();
    }

    private void persistMessages(AiSession session, ProblemCoachContext context, ProblemAgentChatRequestVO request,
                                 String reply, boolean revealFullSolution) {
        AiMessage userMessage = new AiMessage();
        userMessage.setSessionId(session.getSessionId());
        userMessage.setRole("user");
        userMessage.setRelatedProblemId(context.getCurrentProblem().getId());
        userMessage.setRelatedSubmitId(session.getLastSubmitId());
        userMessage.setMessageKind("chat");
        userMessage.setContent(buildPersistedUserMessage(context, request, revealFullSolution));
        userMessage.setCreateTime(LocalDateTime.now());
        aiSessionMapper.insertMessage(userMessage);

        AiMessage assistantMessage = new AiMessage();
        assistantMessage.setSessionId(session.getSessionId());
        assistantMessage.setRole("assistant");
        assistantMessage.setRelatedProblemId(context.getCurrentProblem().getId());
        assistantMessage.setRelatedSubmitId(session.getLastSubmitId());
        assistantMessage.setMessageKind(revealFullSolution ? "solution_reveal" : "summary");
        assistantMessage.setContent(reply);
        assistantMessage.setCreateTime(LocalDateTime.now());
        aiSessionMapper.insertMessage(assistantMessage);
    }

    private String buildPersistedUserMessage(ProblemCoachContext context, ProblemAgentChatRequestVO request, boolean revealFullSolution) {
        if (request.getMessage() != null && !request.getMessage().isBlank()) {
            return request.getMessage();
        }
        if (revealFullSolution) {
            return "请给我参考修正版代码。";
        }
        if ("submit_failed".equalsIgnoreCase(context.getTriggerType())) {
            return "提交失败后自动分析。";
        }
        if ("run_failed".equalsIgnoreCase(context.getTriggerType())) {
            return "运行失败后自动分析。";
        }
        return "请帮我分析这道题。";
    }

    private String buildReply(ProblemCoachContext context, ProblemAgentChatRequestVO request, boolean revealFullSolution) {
        if (!isAiAvailable()) {
            return buildFallbackReply(context, revealFullSolution);
        }

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "qwen-turbo");
            requestBody.put("temperature", revealFullSolution ? 0.2 : 0.4);

            JSONArray messages = new JSONArray();
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", defaultPrompt + "\n\n" + promptBuilder.buildSystemPrompt(context, request, revealFullSolution));
            messages.add(systemMessage);

            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", promptBuilder.buildUserPrompt(context, request, revealFullSolution));
            messages.add(userMessage);
            requestBody.put("messages", messages);

            Request httpRequest = new Request.Builder()
                    .url(apiUrl + "/chat/completions")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody.toJSONString(), MediaType.parse("application/json")))
                    .build();

            try (Response response = client.newCall(httpRequest).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    return buildFallbackReply(context, revealFullSolution);
                }

                String body = response.body().string();
                JSONObject json = JSON.parseObject(body);
                return json.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");
            }
        } catch (Exception ignored) {
            return buildFallbackReply(context, revealFullSolution);
        }
    }

    private String buildFallbackReply(ProblemCoachContext context, boolean revealFullSolution) {
        StringBuilder builder = new StringBuilder();
        builder.append("AI 分析暂时不可用，但本地陪练上下文已经准备好。\n\n");
        if (revealFullSolution) {
            builder.append("你已经满足查看参考修正版的条件，但当前无法生成代码草稿。\n");
            builder.append("先使用摘要和推荐卡片，等 AI 服务恢复后再重试。");
            return builder.toString();
        }
        if ("submit_failed".equalsIgnoreCase(context.getTriggerType())) {
            builder.append("这次提交没有通过。先检查第一个失败用例，再对照分支条件和边界情况。\n");
        } else if ("run_failed".equalsIgnoreCase(context.getTriggerType())) {
            builder.append("这次运行失败。先从报错或输出不匹配处入手，验证一个最小可复现输入。\n");
        } else {
            builder.append("可以追问当前思路、具体 bug 或下一步要复习的概念。\n");
        }
        builder.append("薄弱点和下一步推荐仍然可以继续查看。");
        return builder.toString();
    }

    private String buildSummary(ProblemCoachContext context, boolean revealFullSolution, AgentDecisionDTO decision) {
        if (decision != null && decision.getActionType() != null && decision.getPedagogicalGoal() != null) {
            return decision.getActionType() + " / " + decision.getPedagogicalGoal();
        }
        if (revealFullSolution) {
            return "你已经有失败记录，本次可以在约束允许后查看参考修正版。";
        }
        if ("submit_failed".equalsIgnoreCase(context.getTriggerType())) {
            return "上一次提交没有通过，先定位失败条件，再补上相关薄弱点。";
        }
        if ("run_failed".equalsIgnoreCase(context.getTriggerType())) {
            return "上一次运行失败，先处理当前报错或输出不匹配，再用样例重新运行。";
        }
        if (context.isHasFailure()) {
            return "这道题已有失败记录，可以继续要定向提示，必要时再请求参考修正版。";
        }
        return "陪练已就绪，可以追问思路、代码问题或下一步练习。";
    }

    private List<CoachActionVO> buildActions(ProblemCoachContext context, boolean revealFullSolution, String draftCode) {
        List<CoachActionVO> actions = new ArrayList<>();
        actions.add(action("ask_followup", "解释失败原因", "请先解释这次失败的核心原因。", null, null));
        actions.add(action("ask_followup", "给我下一步提示", "请给我一步一步的下一步提示，但不要直接给完整答案。", null, null));
        if (context.isHasFailure() && !revealFullSolution) {
            actions.add(action("reveal_solution", "给我参考修正版代码", "请给我参考修正版代码。", null, null));
        }
        if (draftCode != null && !draftCode.isBlank()) {
            actions.add(action("apply_draft", "直接覆盖当前代码", null, null, null));
        }
        return actions;
    }

    private CoachActionVO action(String type, String label, String prompt, String route, String targetId) {
        CoachActionVO action = new CoachActionVO();
        action.setType(type);
        action.setLabel(label);
        action.setPrompt(prompt);
        action.setRoute(route);
        action.setTargetId(targetId);
        return action;
    }

    private Map<String, Object> buildContextSnapshot(ProblemCoachContext context,
                                                     List<CoachRecommendationCardVO> recommendations,
                                                     boolean hasDraftCode,
                                                     AgentDecisionDTO decision) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("problemId", context.getCurrentProblem().getId());
        snapshot.put("problemTitle", context.getCurrentProblem().getTitle());
        snapshot.put("language", context.getLanguage());
        snapshot.put("triggerType", context.getTriggerType());
        snapshot.put("hasFailure", context.isHasFailure());
        snapshot.put("latestResultCode", context.getLatestResultCode());
        snapshot.put("latestErrorMessage", context.getLatestErrorMessage());
        snapshot.put("latestSubmitId", context.getLatestSubmit() == null ? null : context.getLatestSubmit().getId());
        snapshot.put("weakPointNames", context.getWeakKnowledgePoints().stream()
                .map(item -> item.get("name") != null ? item.get("name") : item.get("knowledgeName"))
                .toList());
        snapshot.put("recommendationCount", recommendations.size());
        snapshot.put("aiAvailable", context.isAiAvailable());
        snapshot.put("hasDraftCode", hasDraftCode);
        if (decision != null) {
            snapshot.put("requestId", decision.getResponseId());
            snapshot.put("actionType", decision.getActionType());
            snapshot.put("pedagogicalGoal", decision.getPedagogicalGoal());
            snapshot.put("contentType", decision.getContentType());
            snapshot.put("nextSuggestion", decision.getNextSuggestion());
            snapshot.put("errorTag", decision.getErrorTag());
            snapshot.put("weakPoints", decision.getWeakPoints());
        }
        return snapshot;
    }

    private String responseText(AgentDecisionDTO decision) {
        if (decision == null) {
            return "Agent 暂时不可用，请稍后重试。";
        }
        if (decision.getMainResponse() != null && !decision.getMainResponse().isBlank()) {
            return decision.getMainResponse();
        }
        if (decision.getContent() != null && !decision.getContent().isBlank()) {
            return decision.getContent();
        }
        return "Agent 暂时没有生成具体内容，请换一种方式提问。";
    }

    private ProblemAgentSessionVO toSessionVO(AiSession session) {
        List<AiMessage> messages = aiSessionMapper.selectMessagesBySessionId(session.getSessionId());
        ProblemAgentSessionVO response = new ProblemAgentSessionVO();
        response.setSessionId(session.getSessionId());
        response.setMessages(messages);
        response.setContextSnapshot(parseMetadata(session.getMetadataJson()));
        response.setSummary(buildSessionSummary(response.getContextSnapshot()));
        response.setCanRevealFullSolution(Boolean.TRUE.equals(response.getContextSnapshot().get("hasFailure")));
        response.setActions(buildSessionActions(response.getCanRevealFullSolution(), messages));
        response.setRecommendations(new ArrayList<>());

        String latestDraft = messages.stream()
                .filter(message -> "solution_reveal".equals(message.getMessageKind()))
                .reduce((first, second) -> second)
                .map(AiMessage::getContent)
                .map(this::extractDraftCode)
                .orElse(null);
        response.setDraftCode(latestDraft);
        return response;
    }

    private List<CoachActionVO> buildSessionActions(Boolean canRevealFullSolution, List<AiMessage> messages) {
        boolean hasSolutionDraft = messages.stream()
                .anyMatch(message -> "solution_reveal".equals(message.getMessageKind()));
        List<CoachActionVO> actions = new ArrayList<>();
        actions.add(action("ask_followup", "继续追问", "请继续结合我当前的代码给我下一步提示。", null, null));
        if (Boolean.TRUE.equals(canRevealFullSolution) && !hasSolutionDraft) {
            actions.add(action("reveal_solution", "给我参考修正版代码", "请给我参考修正版代码。", null, null));
        }
        if (hasSolutionDraft) {
            actions.add(action("apply_draft", "直接覆盖当前代码", null, null, null));
        }
        return actions;
    }

    private Map<String, Object> parseMetadata(String metadataJson) {
        if (metadataJson == null || metadataJson.isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            return JSON.parseObject(metadataJson);
        } catch (Exception ignored) {
            return new LinkedHashMap<>();
        }
    }

    private String buildSessionSummary(Map<String, Object> snapshot) {
        Object triggerType = snapshot.get("triggerType");
        Object hasFailure = snapshot.get("hasFailure");
        if (Boolean.TRUE.equals(hasFailure) && "submit_failed".equals(triggerType)) {
            return "这是一次提交失败后的陪练会话，可以继续诊断或请求参考修正版。";
        }
        if (Boolean.TRUE.equals(hasFailure) && "run_failed".equals(triggerType)) {
            return "这是一次运行失败后的陪练会话，可以继续调试或请求更明确的提示。";
        }
        return "这是当前题目的最新陪练会话。";
    }

    private AiSession requireOwnedSession(Long userId, String sessionId) {
        AiSession session = aiSessionMapper.selectSessionBySessionId(sessionId);
        if (session == null || session.getUserId() == null || !session.getUserId().equals(userId)) {
            throw new RuntimeException("Session not found");
        }
        return session;
    }

    private String buildTopic(ProblemCoachContext context) {
        String title = context.getCurrentProblem() == null ? "题目陪练" : context.getCurrentProblem().getTitle();
        return (title == null || title.isBlank()) ? "题目陪练" : "题目陪练 - " + title;
    }

    private boolean isAiAvailable() {
        return apiKey != null && !apiKey.isBlank();
    }

    private String extractDraftCode(String reply) {
        if (reply == null || reply.isBlank()) {
            return null;
        }
        Matcher matcher = CODE_BLOCK_PATTERN.matcher(reply);
        if (!matcher.find()) {
            return null;
        }
        String codeBlock = matcher.group(1);
        return codeBlock == null || codeBlock.isBlank() ? null : codeBlock.trim();
    }
}
