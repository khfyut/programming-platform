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

import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class AgentClient {

    private static final String AGENT_SERVICE_URL = "http://localhost:8766/decision";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static final int CONNECT_TIMEOUT_SECONDS = intSetting("AGENT_CLIENT_CONNECT_TIMEOUT_SECONDS", 5);
    private static final int READ_TIMEOUT_SECONDS = intSetting("AGENT_CLIENT_READ_TIMEOUT_SECONDS", 130);
    private static final int WRITE_TIMEOUT_SECONDS = intSetting("AGENT_CLIENT_WRITE_TIMEOUT_SECONDS", 30);

    private final OkHttpClient client;

    public AgentClient() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT_SECONDS))
                .readTimeout(Duration.ofSeconds(READ_TIMEOUT_SECONDS))
                .writeTimeout(Duration.ofSeconds(WRITE_TIMEOUT_SECONDS))
                .build();
    }

    public AgentDecisionDTO getDecision(AgentContextDTO context) {
        try {
            String jsonBody = JSON.toJSONString(context);
            System.out.println("Calling Agent service: requestId=" + safe(context == null ? null : context.getRequestId())
                    + ", scene=" + safe(context == null ? null : context.getScene())
                    + ", actionHint=" + safe(context == null ? null : context.getActionHint())
                    + ", failureEvidenceLevel=" + safe(context == null ? null : context.getFailureEvidenceLevel()));

            RequestBody body = RequestBody.create(jsonBody, JSON_MEDIA_TYPE);
            Request request = new Request.Builder()
                    .url(AGENT_SERVICE_URL)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    System.err.println("Agent service returned error: " + response.code());
                    return createFallbackDecision(context, "TOOL_MISSING", "Python Agent service returned HTTP " + response.code());
                }

                String responseBody = response.body().string();
                AgentDecisionDTO decision = JSON.parseObject(responseBody, AgentDecisionDTO.class);
                normalizeV2Compatibility(decision);
                System.out.println("Agent service response: requestId=" + safe(decision == null ? null : decision.getResponseId())
                        + ", actionType=" + safe(decision == null ? null : decision.getActionType())
                        + ", answerScope=" + safe(decision == null ? null : decision.getAnswerScope())
                        + ", errorTag=" + safe(decision == null ? null : decision.getErrorTag()));
                return decision;
            }
        } catch (SocketTimeoutException e) {
            System.err.println("Agent service call failed: " + e.getMessage());
            return createFallbackDecision(context, "AGENT_SERVICE_TIMEOUT", e.getMessage());
        } catch (Exception e) {
            System.err.println("Agent service call failed: " + e.getMessage());
            return createFallbackDecision(context, "AGENT_SERVICE_UNAVAILABLE", e.getMessage());
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

    private AgentDecisionDTO createFallbackDecision(AgentContextDTO context, String errorTag, String detail) {
        AgentDecisionDTO fallback = new AgentDecisionDTO();
        fallback.setResponseId(context == null ? generateRequestId() : context.getRequestId());
        fallback.setTimestamp(LocalDateTime.now());
        fallback.setDecisionSummary("Agent service unavailable");
        fallback.setSelectedStrategy("AGENT_FAILURE");
        fallback.setDecisionReason(detail == null ? "Python Agent service unavailable" : detail);
        fallback.setRecommendedActionId("agent_failure_notice");
        fallback.setActionType(AgentProtocolConstants.ACTION_EXPLAIN);
        fallback.setPedagogicalGoal("EXPLAIN_AGENT_FAILURE");
        fallback.setContentType("explanation");
        fallback.setErrorTag(errorTag);
        fallback.setAnswerScope("concept_only");
        fallback.setRiskLevel("high");
        fallback.setMainResponse("AI 服务暂时没有返回稳定结果。你可以重新发送一次，或者把问题缩小到一个具体动作：要提示、要定位错误、要最小失败用例，或要类似题推荐。");
        fallback.setNextSuggestion("建议重新发送当前问题，或补充代码片段、报错信息和你已经尝试过的思路。");
        fallback.setContent(fallback.getMainResponse());
        fallback.setSuggestedNextAction(fallback.getNextSuggestion());
        fallback.setConfidence(0.1);
        return fallback;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    public static String generateRequestId() {
        return "req-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private static int intSetting(String key, int defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            value = System.getProperty(key);
        }
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Math.max(1, Integer.parseInt(value.trim()));
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }
}
