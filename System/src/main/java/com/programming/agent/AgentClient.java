package com.programming.agent;

import com.alibaba.fastjson2.JSON;
import com.programming.agent.dto.AgentContextDTO;
import com.programming.agent.dto.AgentDecisionDTO;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class AgentClient {

    private static final String AGENT_SERVICE_URL = "http://localhost:8000/decision";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client;

    public AgentClient() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(30))
                .build();
    }

    public AgentDecisionDTO getDecision(AgentContextDTO context) {
        try {
            String jsonBody = JSON.toJSONString(context);
            System.out.println("调用Agent服务，请求: " + jsonBody);

            RequestBody body = RequestBody.create(jsonBody, JSON_MEDIA_TYPE);
            Request request = new Request.Builder()
                    .url(AGENT_SERVICE_URL)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    System.err.println("Agent服务返回错误: " + response.code());
                    return createFallbackDecision(context);
                }

                String responseBody = response.body().string();
                System.out.println("Agent服务响应: " + responseBody);
                AgentDecisionDTO decision = JSON.parseObject(responseBody, AgentDecisionDTO.class);
                normalizeV2Compatibility(decision);
                return decision;
            }
        } catch (Exception e) {
            System.err.println("调用Agent服务失败: " + e.getMessage());
            return createFallbackDecision(context);
        }
    }

    private void normalizeV2Compatibility(AgentDecisionDTO decision) {
        if (decision == null) {
            return;
        }
        if ((decision.getContent() == null || decision.getContent().isBlank()) && decision.getMainResponse() != null) {
            decision.setContent(decision.getMainResponse());
        }
        if ((decision.getMainResponse() == null || decision.getMainResponse().isBlank()) && decision.getContent() != null) {
            decision.setMainResponse(decision.getContent());
        }
        if ((decision.getSuggestedNextAction() == null || decision.getSuggestedNextAction().isBlank())
                && decision.getNextSuggestion() != null) {
            decision.setSuggestedNextAction(decision.getNextSuggestion());
        }
        if ((decision.getNextSuggestion() == null || decision.getNextSuggestion().isBlank())
                && decision.getSuggestedNextAction() != null) {
            decision.setNextSuggestion(decision.getSuggestedNextAction());
        }
    }

    private AgentDecisionDTO createFallbackDecision(AgentContextDTO context) {
        AgentDecisionDTO fallback = new AgentDecisionDTO();
        fallback.setResponseId(context.getRequestId());
        fallback.setTimestamp(LocalDateTime.now());
        fallback.setDecisionSummary("Agent服务不可用，使用本地兜底策略");
        fallback.setSelectedStrategy("FALLBACK");
        fallback.setDecisionReason("无法连接到Python Agent Service");
        fallback.setRecommendedActionId("hint_fallback");
        fallback.setActionType(AgentProtocolConstants.ACTION_HINT);
        fallback.setPedagogicalGoal("GIVE_LIGHT_HINT");
        fallback.setContentType("hint");
        fallback.setMainResponse("Agent 暂时不可用。先用最小样例检查当前代码的边界条件和状态更新顺序，不要一次改很多处。");
        fallback.setNextSuggestion("修正一个最小问题后重新运行，再继续请求陪练。");
        fallback.setContent(fallback.getMainResponse());
        fallback.setSuggestedNextAction(fallback.getNextSuggestion());
        fallback.setConfidence(0.3);
        return fallback;
    }

    public static String generateRequestId() {
        return "req-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
