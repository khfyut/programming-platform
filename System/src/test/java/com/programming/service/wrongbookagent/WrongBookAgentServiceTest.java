package com.programming.service.wrongbookagent;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.programming.agent.dto.AgentDecisionDTO;
import com.programming.entity.AiMessage;
import com.programming.entity.AiSession;
import com.programming.entity.Problem;
import com.programming.entity.WrongBookItem;
import com.programming.mapper.AiSessionMapper;
import com.programming.service.AgentService;
import com.programming.service.WrongBookService;
import com.programming.vo.problemagent.ProblemAgentSessionVO;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WrongBookAgentServiceTest {

    @Test
    void reflectCreatesWrongBookReviewSessionAndReusesIt() {
        InMemoryAiSessionMapper sessions = new InMemoryAiSessionMapper();
        WrongBookService wrongBookService = mock(WrongBookService.class);
        AgentService agentService = mock(AgentService.class);
        WrongBookItem item = wrongItem(88L, 3L, 40L);
        when(wrongBookService.getWrongBookItemById(17L, 88L)).thenReturn(item);
        when(agentService.processWrongBookReflection(17L, 88L)).thenReturn(reflection("先复盘错误原因"));

        WrongBookAgentService service = new WrongBookAgentService(sessions, wrongBookService, agentService);

        ProblemAgentSessionVO first = service.reflect(17L, 88L);
        ProblemAgentSessionVO second = service.reflect(17L, 88L);

        assertEquals(first.getSessionId(), second.getSessionId());
        assertEquals(1, sessions.sessionRows.size());
        assertEquals("wrong_book_review", sessions.sessionRows.get(0).getSessionType());
        assertEquals(88L, sessions.sessionRows.get(0).getRelatedWrongItemId());
        assertEquals(1, sessions.messageRows.stream()
                .filter(message -> "wrong_book_reflection".equals(message.getMessageKind()))
                .count());
        verify(agentService, times(1)).processWrongBookReflection(17L, 88L);
    }

    @Test
    void reflectReturnsLearningStateAndCoachActions() {
        InMemoryAiSessionMapper sessions = new InMemoryAiSessionMapper();
        WrongBookService wrongBookService = mock(WrongBookService.class);
        AgentService agentService = mock(AgentService.class);
        WrongBookItem item = wrongItem(88L, 3L, 40L);
        when(wrongBookService.getWrongBookItemById(17L, 88L)).thenReturn(item);
        when(agentService.processWrongBookReflection(17L, 88L)).thenReturn(reflection("Review the boundary condition"));

        WrongBookAgentService service = new WrongBookAgentService(sessions, wrongBookService, agentService);

        ProblemAgentSessionVO reflected = service.reflect(17L, 88L);

        Map<String, Object> snapshot = reflected.getContextSnapshot();
        assertTrue(snapshot.containsKey("currentJudgement"));
        assertTrue(snapshot.containsKey("learningState"));
        JSONObject learningState = JSON.parseObject(JSON.toJSONString(snapshot.get("learningState")));
        assertEquals(0, learningState.getIntValue("attemptCount"));
        assertEquals(1, learningState.getIntValue("hintLevel"));
        assertEquals("REFLECTING", learningState.getString("currentStage"));
        assertFalse(reflected.getActions().isEmpty());
        assertTrue(reflected.getActions().stream().anyMatch(action -> "minimal_counterexample".equals(action.getType())));
        assertTrue(reflected.getActions().stream().anyMatch(action -> "hint".equals(action.getType())));
        assertTrue(reflected.getActions().stream().anyMatch(action -> "similar_problem".equals(action.getType())));
    }

    @Test
    void hintActionUpdatesHintLevelAttemptCountAndMetadata() {
        InMemoryAiSessionMapper sessions = new InMemoryAiSessionMapper();
        WrongBookService wrongBookService = mock(WrongBookService.class);
        AgentService agentService = mock(AgentService.class);
        WrongBookItem item = wrongItem(88L, 3L, 40L);
        when(wrongBookService.getWrongBookItemById(17L, 88L)).thenReturn(item);
        when(agentService.processWrongBookReflection(17L, 88L)).thenReturn(reflection("Review the boundary condition"));

        WrongBookAgentService service = new WrongBookAgentService(sessions, wrongBookService, agentService);
        ProblemAgentSessionVO reflected = service.reflect(17L, 88L);
        WrongBookAgentChatRequestVO request = new WrongBookAgentChatRequestVO();
        request.setWrongItemId(88L);
        request.setSessionId(reflected.getSessionId());
        request.setActionType("hint");
        request.setHintLevel(4);

        ProblemAgentSessionVO hinted = service.chat(17L, request);

        JSONObject learningState = JSON.parseObject(JSON.toJSONString(hinted.getContextSnapshot().get("learningState")));
        assertEquals(1, learningState.getIntValue("attemptCount"));
        assertEquals(4, learningState.getIntValue("hintLevel"));
        JSONObject stored = JSON.parseObject(sessions.sessionRows.get(0).getMetadataJson());
        JSONObject storedLearningState = stored.getJSONObject("learningState");
        assertEquals(1, storedLearningState.getIntValue("attemptCount"));
        assertEquals(4, storedLearningState.getIntValue("hintLevel"));
        String assistantReply = hinted.getMessages().stream()
                .filter(message -> "assistant".equals(message.getRole()))
                .filter(message -> "wrong_book_chat".equals(message.getMessageKind()))
                .reduce((first, second) -> second)
                .orElseThrow()
                .getContent();
        assertFalse(assistantReply.contains("L4"));
        assertTrue(assistantReply.contains("伪代码"));
    }

    @Test
    void submittingAllReflectionTasksMakesSessionReviewReady() {
        InMemoryAiSessionMapper sessions = new InMemoryAiSessionMapper();
        WrongBookService wrongBookService = mock(WrongBookService.class);
        AgentService agentService = mock(AgentService.class);
        WrongBookItem item = wrongItem(88L, 3L, 40L);
        when(wrongBookService.getWrongBookItemById(17L, 88L)).thenReturn(item);
        when(agentService.processWrongBookReflection(17L, 88L)).thenReturn(reflection("Review the boundary condition"));

        WrongBookAgentService service = new WrongBookAgentService(sessions, wrongBookService, agentService);
        ProblemAgentSessionVO reflected = service.reflect(17L, 88L);

        ProblemAgentSessionVO current = service.chat(17L, taskRequest(reflected.getSessionId(), "summary", "Boundary was missed"));
        current = service.chat(17L, taskRequest(current.getSessionId(), "counterexample", "[] or single item"));
        current = service.chat(17L, taskRequest(current.getSessionId(), "fix_idea", "Check before updating state"));

        JSONObject learningState = JSON.parseObject(JSON.toJSONString(current.getContextSnapshot().get("learningState")));
        assertEquals(3, learningState.getIntValue("attemptCount"));
        assertEquals("REVIEW_READY", learningState.getString("currentStage"));
        JSONObject tasks = learningState.getJSONObject("tasks");
        assertTrue(tasks.getJSONObject("summary").getBooleanValue("submitted"));
        assertTrue(tasks.getJSONObject("counterexample").getBooleanValue("submitted"));
        assertTrue(tasks.getJSONObject("fixIdea").getBooleanValue("submitted"));
    }

    @Test
    void similarProblemActionUsesRecommendationService() {
        InMemoryAiSessionMapper sessions = new InMemoryAiSessionMapper();
        WrongBookService wrongBookService = mock(WrongBookService.class);
        AgentService agentService = mock(AgentService.class);
        WrongBookItem item = wrongItem(88L, 3L, 40L);
        when(wrongBookService.getWrongBookItemById(17L, 88L)).thenReturn(item);
        when(wrongBookService.getRecommendedProblems(17L, 88L, 5)).thenReturn(List.of(problem(12L, "Two Sum II", 1)));

        WrongBookAgentService service = new WrongBookAgentService(sessions, wrongBookService, agentService);
        WrongBookAgentChatRequestVO request = new WrongBookAgentChatRequestVO();
        request.setWrongItemId(88L);
        request.setActionType("similar_problem");

        ProblemAgentSessionVO response = service.chat(17L, request);

        assertTrue(response.getMessages().get(1).getContent().contains("Two Sum II"));
        assertTrue(response.getMessages().get(1).getContent().contains("/problem/12"));
        verify(wrongBookService, times(1)).getRecommendedProblems(17L, 88L, 5);
    }

    @Test
    void similarProblemActionBypassesUnifiedAgentService() {
        InMemoryAiSessionMapper sessions = new InMemoryAiSessionMapper();
        WrongBookService wrongBookService = mock(WrongBookService.class);
        AgentService agentService = mock(AgentService.class);
        WrongBookItem item = wrongItem(88L, 3L, 40L);
        when(wrongBookService.getWrongBookItemById(17L, 88L)).thenReturn(item);
        when(wrongBookService.getRecommendedProblems(17L, 88L, 5)).thenReturn(List.of(problem(12L, "Two Sum II", 1)));

        WrongBookAgentService service = new WrongBookAgentService(sessions, wrongBookService, agentService);
        WrongBookAgentChatRequestVO request = new WrongBookAgentChatRequestVO();
        request.setWrongItemId(88L);
        request.setActionType("similar_problem");

        ProblemAgentSessionVO response = service.chat(17L, request);

        assertTrue(response.getMessages().get(1).getContent().contains("Two Sum II"));
        verify(agentService, never()).processWrongBookReviewChat(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void chatAppendsWrongBookChatMessagesToOwnedSession() {
        InMemoryAiSessionMapper sessions = new InMemoryAiSessionMapper();
        WrongBookService wrongBookService = mock(WrongBookService.class);
        AgentService agentService = mock(AgentService.class);
        WrongBookItem item = wrongItem(88L, 3L, 40L);
        when(wrongBookService.getWrongBookItemById(17L, 88L)).thenReturn(item);
        when(agentService.processWrongBookReflection(17L, 88L)).thenReturn(reflection("先复盘错误原因"));

        WrongBookAgentService service = new WrongBookAgentService(sessions, wrongBookService, agentService);
        ProblemAgentSessionVO reflected = service.reflect(17L, 88L);
        WrongBookAgentChatRequestVO request = new WrongBookAgentChatRequestVO();
        request.setWrongItemId(88L);
        request.setSessionId(reflected.getSessionId());
        request.setMessage("为什么我这里边界错了？");

        ProblemAgentSessionVO chatted = service.chat(17L, request);

        assertEquals(reflected.getSessionId(), chatted.getSessionId());
        assertEquals(3, sessions.messageRows.size());
        assertEquals("wrong_book_chat", sessions.messageRows.get(1).getMessageKind());
        assertEquals("wrong_book_chat", sessions.messageRows.get(2).getMessageKind());
        verify(wrongBookService, times(2)).getWrongBookItemById(eq(17L), eq(88L));
    }

    @Test
    void chatUsesUnifiedAgentDecisionWhenAvailable() {
        InMemoryAiSessionMapper sessions = new InMemoryAiSessionMapper();
        WrongBookService wrongBookService = mock(WrongBookService.class);
        AgentService agentService = mock(AgentService.class);
        WrongBookItem item = wrongItem(88L, 3L, 40L);
        when(wrongBookService.getWrongBookItemById(17L, 88L)).thenReturn(item);
        when(agentService.processWrongBookReviewChat(eq(17L), eq(item), eq("why is this wrong?"), eq("chat"), eq(null), eq(null), eq(null)))
                .thenReturn(reflection("agent decision reply"));

        WrongBookAgentService service = new WrongBookAgentService(sessions, wrongBookService, agentService);
        WrongBookAgentChatRequestVO request = new WrongBookAgentChatRequestVO();
        request.setWrongItemId(88L);
        request.setMessage("why is this wrong?");

        ProblemAgentSessionVO chatted = service.chat(17L, request);

        assertEquals("agent decision reply", chatted.getMessages().get(1).getContent());
        verify(agentService).processWrongBookReviewChat(17L, item, "why is this wrong?", "chat", null, null, null);
    }

    @Test
    void reflectHidesAgentTimeoutBehindReviewFallback() {
        InMemoryAiSessionMapper sessions = new InMemoryAiSessionMapper();
        WrongBookService wrongBookService = mock(WrongBookService.class);
        AgentService agentService = mock(AgentService.class);
        WrongBookItem item = wrongItem(88L, 3L, 40L);
        when(wrongBookService.getWrongBookItemById(17L, 88L)).thenReturn(item);
        when(agentService.processWrongBookReflection(17L, 88L)).thenReturn(timeoutDecision());

        WrongBookAgentService service = new WrongBookAgentService(sessions, wrongBookService, agentService);

        ProblemAgentSessionVO reflected = service.reflect(17L, 88L);

        String assistantReply = reflected.getMessages().get(0).getContent();
        assertFalse(assistantReply.contains("MODEL_TIMEOUT_REPLY"));
        assertTrue(assistantReply.length() > 10);
    }

    @Test
    void chatUsesLocalFallbackWhenUnifiedAgentTimesOut() {
        InMemoryAiSessionMapper sessions = new InMemoryAiSessionMapper();
        WrongBookService wrongBookService = mock(WrongBookService.class);
        AgentService agentService = mock(AgentService.class);
        WrongBookItem item = wrongItem(88L, 3L, 40L);
        when(wrongBookService.getWrongBookItemById(17L, 88L)).thenReturn(item);
        when(agentService.processWrongBookReviewChat(eq(17L), eq(item), eq("why is this wrong?"), eq("chat"), eq(null), eq(null), eq(null)))
                .thenReturn(timeoutDecision());

        WrongBookAgentService service = new WrongBookAgentService(sessions, wrongBookService, agentService);
        WrongBookAgentChatRequestVO request = new WrongBookAgentChatRequestVO();
        request.setWrongItemId(88L);
        request.setMessage("why is this wrong?");

        ProblemAgentSessionVO chatted = service.chat(17L, request);

        String assistantReply = chatted.getMessages().get(1).getContent();
        assertFalse(assistantReply.contains("MODEL_TIMEOUT_REPLY"));
        assertTrue(assistantReply.length() > 10);
        verify(agentService).processWrongBookReviewChat(17L, item, "why is this wrong?", "chat", null, null, null);
    }

    @Test
    void storedAgentFailureMessagesAreNormalizedForDisplay() {
        InMemoryAiSessionMapper sessions = new InMemoryAiSessionMapper();
        WrongBookService wrongBookService = mock(WrongBookService.class);
        AgentService agentService = mock(AgentService.class);
        WrongBookItem item = wrongItem(88L, 3L, 40L);
        when(wrongBookService.getWrongBookItemById(17L, 88L)).thenReturn(item);

        WrongBookAgentService service = new WrongBookAgentService(sessions, wrongBookService, agentService);
        ProblemAgentSessionVO reflected = service.reflect(17L, 88L);
        AiMessage storedFailure = new AiMessage();
        storedFailure.setSessionId(reflected.getSessionId());
        storedFailure.setRole("assistant");
        storedFailure.setContent("Agent service is temporarily unavailable. Please retry, narrow the question, or provide the code/error message again.");
        storedFailure.setMessageKind("wrong_book_chat");
        sessions.insertMessage(storedFailure);

        ProblemAgentSessionVO loaded = service.getSession(17L, reflected.getSessionId());
        String displayed = loaded.getMessages().stream()
                .filter(message -> "wrong_book_chat".equals(message.getMessageKind()))
                .findFirst()
                .orElseThrow()
                .getContent();

        assertTrue(displayed.contains("AI 服务"));
        assertTrue(displayed.contains("重新发送"));
        assertFalse(displayed.contains("Agent service is temporarily unavailable"));
    }

    @Test
    void storedModelTimeoutMessagesAreNormalizedForDisplay() {
        InMemoryAiSessionMapper sessions = new InMemoryAiSessionMapper();
        WrongBookService wrongBookService = mock(WrongBookService.class);
        AgentService agentService = mock(AgentService.class);
        WrongBookItem item = wrongItem(88L, 3L, 40L);
        when(wrongBookService.getWrongBookItemById(17L, 88L)).thenReturn(item);

        WrongBookAgentService service = new WrongBookAgentService(sessions, wrongBookService, agentService);
        ProblemAgentSessionVO reflected = service.reflect(17L, 88L);
        AiMessage storedFailure = new AiMessage();
        storedFailure.setSessionId(reflected.getSessionId());
        storedFailure.setRole("assistant");
        storedFailure.setContent("模型响应超时，暂时无法完成这次 Agent 决策。");
        storedFailure.setMessageKind("wrong_book_chat");
        sessions.insertMessage(storedFailure);

        ProblemAgentSessionVO loaded = service.getSession(17L, reflected.getSessionId());
        String displayed = loaded.getMessages().stream()
                .filter(message -> "wrong_book_chat".equals(message.getMessageKind()))
                .findFirst()
                .orElseThrow()
                .getContent();

        assertFalse(displayed.contains("模型响应超时"));
        assertFalse(displayed.contains("Agent 决策"));
        assertTrue(displayed.length() > 10);
    }

    @Test
    void hintActionUsesUnifiedAgentReplyAndStillStoresLearningState() {
        InMemoryAiSessionMapper sessions = new InMemoryAiSessionMapper();
        WrongBookService wrongBookService = mock(WrongBookService.class);
        AgentService agentService = mock(AgentService.class);
        WrongBookItem item = wrongItem(88L, 3L, 40L);
        when(wrongBookService.getWrongBookItemById(17L, 88L)).thenReturn(item);
        when(agentService.processWrongBookReflection(17L, 88L)).thenReturn(reflection("Review the boundary condition"));
        when(agentService.processWrongBookReviewChat(eq(17L), eq(item), any(), eq("hint"), eq(null), eq(null), eq(3)))
                .thenReturn(reflection("agent hint reply"));

        WrongBookAgentService service = new WrongBookAgentService(sessions, wrongBookService, agentService);
        ProblemAgentSessionVO reflected = service.reflect(17L, 88L);
        WrongBookAgentChatRequestVO request = new WrongBookAgentChatRequestVO();
        request.setWrongItemId(88L);
        request.setSessionId(reflected.getSessionId());
        request.setActionType("hint");
        request.setHintLevel(3);

        ProblemAgentSessionVO hinted = service.chat(17L, request);

        JSONObject learningState = JSON.parseObject(JSON.toJSONString(hinted.getContextSnapshot().get("learningState")));
        assertEquals(1, learningState.getIntValue("attemptCount"));
        assertEquals(3, learningState.getIntValue("hintLevel"));
        assertEquals("agent hint reply", hinted.getMessages().get(2).getContent());
    }

    @Test
    void metadataKeepsLatestThreeAgentDecisionTraces() {
        InMemoryAiSessionMapper sessions = new InMemoryAiSessionMapper();
        WrongBookService wrongBookService = mock(WrongBookService.class);
        AgentService agentService = mock(AgentService.class);
        WrongBookItem item = wrongItem(88L, 3L, 40L);
        when(wrongBookService.getWrongBookItemById(17L, 88L)).thenReturn(item);
        when(agentService.processWrongBookReflection(17L, 88L)).thenReturn(reflection("Review the boundary condition"));
        when(agentService.processWrongBookReviewChat(eq(17L), eq(item), any(), eq("chat"), eq(null), eq(null), eq(null)))
                .thenReturn(reflection("agent chat reply"));

        WrongBookAgentService service = new WrongBookAgentService(sessions, wrongBookService, agentService);
        ProblemAgentSessionVO current = service.reflect(17L, 88L);
        for (int i = 0; i < 4; i++) {
            WrongBookAgentChatRequestVO request = new WrongBookAgentChatRequestVO();
            request.setWrongItemId(88L);
            request.setSessionId(current.getSessionId());
            request.setMessage("question " + i);
            current = service.chat(17L, request);
        }

        JSONObject stored = JSON.parseObject(sessions.sessionRows.get(0).getMetadataJson());
        assertEquals(3, stored.getJSONArray("agentDecisionTraces").size());
        JSONObject lastTrace = stored.getJSONArray("agentDecisionTraces").getJSONObject(2);
        assertEquals("final", lastTrace.getString("decision_stage"));
        assertEquals("chat", lastTrace.getString("user_intent_inferred"));
        assertTrue(lastTrace.containsKey("latency_breakdown_ms"));
        assertTrue(lastTrace.containsKey("quality_flags"));
    }

    @Test
    void recommendationIntentReturnsSimilarProblemsInsteadOfDebugTemplate() {
        InMemoryAiSessionMapper sessions = new InMemoryAiSessionMapper();
        WrongBookService wrongBookService = mock(WrongBookService.class);
        AgentService agentService = mock(AgentService.class);
        WrongBookItem item = wrongItem(88L, 3L, 40L);
        when(wrongBookService.getWrongBookItemById(17L, 88L)).thenReturn(item);
        when(wrongBookService.getRecommendedProblems(17L, 88L, 5)).thenReturn(List.of(
                problem(12L, "两数之和 II", 1),
                problem(18L, "三数之和", 2)
        ));

        WrongBookAgentService service = new WrongBookAgentService(sessions, wrongBookService, agentService);
        WrongBookAgentChatRequestVO request = new WrongBookAgentChatRequestVO();
        request.setWrongItemId(88L);
        request.setMessage("这类相似的题给我推荐来巩固");

        ProblemAgentSessionVO chatted = service.chat(17L, request);

        String reply = chatted.getMessages().get(1).getContent();
        assertTrue(reply.contains("两数之和 II"));
        assertTrue(reply.contains("/problem/12"));
        assertTrue(reply.contains("三数之和"));
        assertTrue(reply.contains("推荐"));
        assertTrue(!reply.contains("失败用例"));
    }

    @Test
    void typedRecommendationIntentBypassesUnifiedAgentService() {
        InMemoryAiSessionMapper sessions = new InMemoryAiSessionMapper();
        WrongBookService wrongBookService = mock(WrongBookService.class);
        AgentService agentService = mock(AgentService.class);
        WrongBookItem item = wrongItem(88L, 3L, 40L);
        when(wrongBookService.getWrongBookItemById(17L, 88L)).thenReturn(item);
        when(wrongBookService.getRecommendedProblems(17L, 88L, 5)).thenReturn(List.of(problem(12L, "Two Sum II", 1)));

        WrongBookAgentService service = new WrongBookAgentService(sessions, wrongBookService, agentService);
        WrongBookAgentChatRequestVO request = new WrongBookAgentChatRequestVO();
        request.setWrongItemId(88L);
        request.setMessage("推荐一道类似题。");

        ProblemAgentSessionVO response = service.chat(17L, request);

        assertTrue(response.getMessages().get(1).getContent().contains("Two Sum II"));
        verify(agentService, never()).processWrongBookReviewChat(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void completeIdeaIntentReturnsExpandedReviewInsteadOfDebugTemplate() {
        InMemoryAiSessionMapper sessions = new InMemoryAiSessionMapper();
        WrongBookService wrongBookService = mock(WrongBookService.class);
        AgentService agentService = mock(AgentService.class);
        WrongBookItem item = wrongItem(88L, 3L, 40L);
        when(wrongBookService.getWrongBookItemById(17L, 88L)).thenReturn(item);

        WrongBookAgentService service = new WrongBookAgentService(sessions, wrongBookService, agentService);
        WrongBookAgentChatRequestVO request = new WrongBookAgentChatRequestVO();
        request.setWrongItemId(88L);
        request.setMessage("这题完整思路是什么");

        ProblemAgentSessionVO chatted = service.chat(17L, request);

        String reply = chatted.getMessages().get(1).getContent();
        assertTrue(reply.contains("完整思路"));
        assertTrue(reply.contains("复杂度"));
        assertTrue(!reply.contains("失败用例"));
        assertTrue(!reply.contains("两个检查点"));
    }

    @Test
    void codeIntentCanReturnReferenceCodeInWrongBookReview() {
        InMemoryAiSessionMapper sessions = new InMemoryAiSessionMapper();
        WrongBookService wrongBookService = mock(WrongBookService.class);
        AgentService agentService = mock(AgentService.class);
        WrongBookItem item = wrongItem(88L, 3L, 40L);
        when(wrongBookService.getWrongBookItemById(17L, 88L)).thenReturn(item);

        WrongBookAgentService service = new WrongBookAgentService(sessions, wrongBookService, agentService);
        WrongBookAgentChatRequestVO request = new WrongBookAgentChatRequestVO();
        request.setWrongItemId(88L);
        request.setMessage("给我参考代码");

        ProblemAgentSessionVO chatted = service.chat(17L, request);

        String reply = chatted.getMessages().get(1).getContent();
        assertTrue(reply.contains("参考代码"));
        assertTrue(reply.contains("class Solution"));
        assertTrue(!reply.contains("失败用例"));
    }

    @Test
    void comparisonIntentAllowsDivergentReviewDiscussion() {
        InMemoryAiSessionMapper sessions = new InMemoryAiSessionMapper();
        WrongBookService wrongBookService = mock(WrongBookService.class);
        AgentService agentService = mock(AgentService.class);
        WrongBookItem item = wrongItem(88L, 3L, 40L);
        when(wrongBookService.getWrongBookItemById(17L, 88L)).thenReturn(item);

        WrongBookAgentService service = new WrongBookAgentService(sessions, wrongBookService, agentService);
        WrongBookAgentChatRequestVO request = new WrongBookAgentChatRequestVO();
        request.setWrongItemId(88L);
        request.setMessage("还有别的解法吗");

        ProblemAgentSessionVO chatted = service.chat(17L, request);

        String reply = chatted.getMessages().get(1).getContent();
        assertTrue(reply.contains("另一种"));
        assertTrue(reply.contains("比较"));
        assertTrue(!reply.contains("失败用例"));
    }

    private WrongBookItem wrongItem(Long wrongItemId, Long problemId, Long submitId) {
        WrongBookItem item = new WrongBookItem();
        item.setId(wrongItemId);
        item.setProblemId(problemId);
        item.setSubmitId(submitId);
        item.setCode("class Solution {}");
        item.setLanguage("JAVA");
        item.setErrorMessage("Wrong answer");
        item.setKnowledgePoints("数组,边界");
        return item;
    }

    private AgentDecisionDTO reflection(String content) {
        AgentDecisionDTO decision = new AgentDecisionDTO();
        decision.setActionType("REFLECT");
        decision.setPedagogicalGoal("REVIEW_AFTER_SOLUTION");
        decision.setContentType("reflection");
        decision.setMainResponse(content);
        decision.setNextSuggestion("重新写出边界条件");
        decision.setWeakPoints(List.of("边界处理"));
        decision.setResponseId("request-1");
        decision.setExecuted(true);
        return decision;
    }

    private AgentDecisionDTO timeoutDecision() {
        AgentDecisionDTO decision = reflection("MODEL_TIMEOUT_REPLY");
        decision.setSelectedStrategy("FAILURE");
        decision.setErrorTag("MODEL_TIMEOUT");
        return decision;
    }

    private WrongBookAgentChatRequestVO taskRequest(String sessionId, String taskType, String answer) {
        WrongBookAgentChatRequestVO request = new WrongBookAgentChatRequestVO();
        request.setWrongItemId(88L);
        request.setSessionId(sessionId);
        request.setActionType("reflection_task_submit");
        request.setTaskType(taskType);
        request.setAnswer(answer);
        return request;
    }

    private Problem problem(Long id, String title, Integer difficulty) {
        Problem problem = new Problem();
        problem.setId(id);
        problem.setTitle(title);
        problem.setDifficulty(difficulty);
        return problem;
    }

    private static class InMemoryAiSessionMapper implements AiSessionMapper {
        private final List<AiSession> sessionRows = new ArrayList<>();
        private final List<AiMessage> messageRows = new ArrayList<>();
        private long nextSessionId = 1L;
        private long nextMessageId = 1L;

        @Override
        public AiSession selectSessionById(Long id) {
            return sessionRows.stream().filter(row -> row.getId().equals(id)).findFirst().orElse(null);
        }

        @Override
        public AiSession selectSessionBySessionId(String sessionId) {
            return sessionRows.stream().filter(row -> row.getSessionId().equals(sessionId)).findFirst().orElse(null);
        }

        @Override
        public List<AiSession> selectSessionsByUserId(Long userId) {
            return sessionRows.stream().filter(row -> row.getUserId().equals(userId)).toList();
        }

        @Override
        public AiSession selectLatestProblemCoachSession(Long userId, Long problemId) {
            return null;
        }

        @Override
        public AiSession selectLatestWrongBookReviewSession(Long userId, Long wrongItemId) {
            return sessionRows.stream()
                    .filter(row -> row.getUserId().equals(userId))
                    .filter(row -> "wrong_book_review".equals(row.getSessionType()))
                    .filter(row -> wrongItemId.equals(row.getRelatedWrongItemId()))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public void insertSession(AiSession session) {
            session.setId(nextSessionId++);
            sessionRows.add(session);
        }

        @Override
        public void updateSession(AiSession session) {
        }

        @Override
        public void deleteSession(Long id) {
        }

        @Override
        public List<AiMessage> selectMessagesBySessionId(String sessionId) {
            return messageRows.stream().filter(row -> row.getSessionId().equals(sessionId)).toList();
        }

        @Override
        public void insertMessage(AiMessage message) {
            message.setId(nextMessageId++);
            messageRows.add(message);
        }

        @Override
        public void deleteMessagesBySessionId(String sessionId) {
        }

        @Override
        public List<com.programming.entity.AiCollection> selectCollectionsByUserId(Long userId) {
            return List.of();
        }

        @Override
        public com.programming.entity.AiCollection selectCollectionById(Long id) {
            return null;
        }

        @Override
        public void insertCollection(com.programming.entity.AiCollection collection) {
        }

        @Override
        public void deleteCollection(Long id) {
        }
    }
}
