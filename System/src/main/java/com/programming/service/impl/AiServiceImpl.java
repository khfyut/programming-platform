package com.programming.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.programming.entity.AiCollection;
import com.programming.entity.AiMessage;
import com.programming.entity.AiSession;
import com.programming.entity.KnowledgePoint;
import com.programming.entity.Question;
import com.programming.entity.UserKnowledgeMastery;
import com.programming.mapper.AiSessionMapper;
import com.programming.mapper.KnowledgeMasteryMapper;
import com.programming.mapper.QuestionMapper;
import com.programming.service.AiService;
import com.programming.service.ai.AiLlmClient;
import com.programming.service.ai.AiLlmConfig;
import com.programming.util.ResultUtil;
import com.programming.vo.AiAskVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class AiServiceImpl implements AiService {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private KnowledgeMasteryMapper knowledgeMasteryMapper;

    @Autowired
    private AiSessionMapper aiSessionMapper;

    @Autowired(required = false)
    private AiLlmClient aiLlmClient;

    @Value("${programming.ai.provider:ollama}")
    private String aiProvider;

    @Value("${programming.ai.model:gemma4:e2b}")
    private String aiModel;

    @Value("${programming.ai.ollama-url:http://127.0.0.1:11434}")
    private String ollamaUrl;

    @Value("${programming.ai.api-key:}")
    private String apiKey;

    @Value("${programming.ai.api-url:https://dashscope.aliyuncs.com/compatible-mode/v1}")
    private String apiUrl;

    @Value("${programming.ai.prompt:你是专业的编程助教，只回答编程学习相关问题。回答要清晰、有步骤，优先解释思路，再给必要示例。}")
    private String prompt;

    @Override
    public ResultUtil askQuestion(AiAskVO aiAskVO) {
        try {
            String question = aiAskVO.getContent();
            String code = aiAskVO.getCode();
            String answer = callAiApi(question, code);

            Question questionRecord = new Question();
            questionRecord.setUserId(aiAskVO.getUserId());
            questionRecord.setContent(question);
            questionRecord.setAnswer(answer);
            questionRecord.setCode(code);
            questionRecord.setCreateTime(LocalDateTime.now());
            questionMapper.insert(questionRecord);

            return ResultUtil.success(answer);
        } catch (Exception e) {
            log.error("AI 问答失败", e);
            return ResultUtil.error("AI 问答失败：" + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getHistory(Long userId, int page, int size) {
        List<AiSession> sessions = aiSessionMapper.selectSessionsByUserId(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("total", sessions.size());
        result.put("list", sessions);
        return result;
    }

    @Override
    public ResultUtil chat(Map<String, Object> params) {
        try {
            Long userId = Long.valueOf(params.get("userId").toString());
            String sessionId = (String) params.get("sessionId");
            String content = (String) params.get("content");
            String code = params.get("code") == null ? null : params.get("code").toString();

            AiSession existingSession = null;
            if (sessionId != null && !sessionId.isBlank()) {
                existingSession = aiSessionMapper.selectSessionBySessionId(sessionId);
                if (existingSession != null && (existingSession.getUserId() == null || !existingSession.getUserId().equals(userId))) {
                    throw new RuntimeException("Session not found");
                }
            }

            if (existingSession == null) {
                sessionId = UUID.randomUUID().toString();
                AiSession session = new AiSession();
                session.setSessionId(sessionId);
                session.setUserId(userId);
                session.setTopic(buildTopic(content));
                session.setSessionType("general");
                session.setStatus(1);
                session.setMetadataJson("{}");
                session.setCreateTime(LocalDateTime.now());
                session.setUpdateTime(LocalDateTime.now());
                aiSessionMapper.insertSession(session);
            } else {
                existingSession.setUpdateTime(LocalDateTime.now());
                if (existingSession.getSessionType() == null || existingSession.getSessionType().isBlank()) {
                    existingSession.setSessionType("general");
                }
                aiSessionMapper.updateSession(existingSession);
            }

            List<AiMessage> history = aiSessionMapper.selectMessagesBySessionId(sessionId);
            List<JSONObject> messages = new ArrayList<>();
            addSystemMessage(messages);

            for (AiMessage msg : history) {
                JSONObject message = new JSONObject();
                message.put("role", msg.getRole());
                message.put("content", msg.getContent());
                messages.add(message);
            }

            String fullContent = appendCode(content, code);
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", fullContent);
            messages.add(userMessage);

            String response = callChatApi(messages);
            persistChatMessages(sessionId, fullContent, response);

            Map<String, Object> result = new HashMap<>();
            result.put("sessionId", sessionId);
            result.put("response", response);
            return ResultUtil.success(result);
        } catch (Exception e) {
            log.error("AI 多轮对话失败", e);
            return ResultUtil.error("AI 多轮对话失败：" + e.getMessage());
        }
    }

    @Override
    public SseEmitter chatStream(Map<String, Object> params) {
        SseEmitter emitter = new SseEmitter(60000L);

        CompletableFuture.runAsync(() -> {
            try {
                sendStreamEvent(emitter, "status", Map.of("text", "已收到问题，正在准备回答"));

                Long userId = Long.valueOf(params.get("userId").toString());
                String sessionId = (String) params.get("sessionId");
                String content = (String) params.get("content");
                String code = params.get("code") == null ? null : params.get("code").toString();

                AiSession existingSession = null;
                if (sessionId != null && !sessionId.isBlank()) {
                    existingSession = aiSessionMapper.selectSessionBySessionId(sessionId);
                    if (existingSession != null && (existingSession.getUserId() == null || !existingSession.getUserId().equals(userId))) {
                        throw new RuntimeException("Session not found");
                    }
                }

                sendStreamEvent(emitter, "status", Map.of("text", "正在整理历史对话和上下文"));
                if (existingSession == null) {
                    sessionId = UUID.randomUUID().toString();
                    AiSession session = new AiSession();
                    session.setSessionId(sessionId);
                    session.setUserId(userId);
                    session.setTopic(buildTopic(content));
                    session.setSessionType("general");
                    session.setStatus(1);
                    session.setMetadataJson("{}");
                    session.setCreateTime(LocalDateTime.now());
                    session.setUpdateTime(LocalDateTime.now());
                    aiSessionMapper.insertSession(session);
                } else {
                    existingSession.setUpdateTime(LocalDateTime.now());
                    if (existingSession.getSessionType() == null || existingSession.getSessionType().isBlank()) {
                        existingSession.setSessionType("general");
                    }
                    aiSessionMapper.updateSession(existingSession);
                }

                sendStreamEvent(emitter, "meta", Map.of("sessionId", sessionId));
                List<AiMessage> history = aiSessionMapper.selectMessagesBySessionId(sessionId);
                List<JSONObject> messages = new ArrayList<>();
                addSystemMessage(messages);

                for (AiMessage msg : history) {
                    JSONObject message = new JSONObject();
                    message.put("role", msg.getRole());
                    message.put("content", msg.getContent());
                    messages.add(message);
                }

                String fullContent = appendCode(content, code);
                JSONObject userMessage = new JSONObject();
                userMessage.put("role", "user");
                userMessage.put("content", fullContent);
                messages.add(userMessage);

                sendStreamEvent(emitter, "status", Map.of("text", "正在生成回答"));
                String response;
                try {
                    response = llmClient().streamChat(currentLlmConfig(), messages, 0.7, delta -> {
                        if (delta != null && !delta.isEmpty()) {
                            sendStreamEvent(emitter, "delta", Map.of("text", delta));
                        }
                    });
                    if (response == null || response.isBlank()) {
                        response = buildLocalFallbackAnswer(extractLastUserMessage(messages), null);
                        emitFallbackText(emitter, response);
                    }
                } catch (Exception llmError) {
                    log.warn("AI LLM stream failed, using local fallback. provider={}, model={}, error={}",
                            aiProvider, aiModel, llmError.getMessage());
                    response = buildLocalFallbackAnswer(extractLastUserMessage(messages), null);
                    emitFallbackText(emitter, response);
                }

                persistChatMessages(sessionId, fullContent, response);
                sendStreamEvent(emitter, "done", Map.of("sessionId", sessionId));
                emitter.complete();
            } catch (Exception e) {
                log.error("AI stream chat failed", e);
                try {
                    sendStreamEvent(emitter, "error", Map.of("message", "AI 响应失败，请稍后重试"));
                } finally {
                    emitter.completeWithError(e);
                }
            }
        });

        return emitter;
    }

    @Override
    public List<AiMessage> getChatHistory(Long userId, String sessionId) {
        AiSession session = requireOwnedSessionBySessionId(userId, sessionId);
        return aiSessionMapper.selectMessagesBySessionId(session.getSessionId());
    }

    @Override
    public ResultUtil optimizeCode(String code, String language) {
        try {
            String requestPrompt = "请优化以下 " + language + " 代码，使它更高效、更可读，并指出主要改动：\n\n" + code;
            return ResultUtil.success(callAiApi(requestPrompt, null));
        } catch (Exception e) {
            log.error("代码优化失败", e);
            return ResultUtil.error("代码优化失败：" + e.getMessage());
        }
    }

    @Override
    public ResultUtil explainKnowledge(String knowledgePoint) {
        try {
            String requestPrompt = "请讲解以下编程知识点：" + knowledgePoint + "。包括概念、使用场景、常见错误和一个简短示例。";
            return ResultUtil.success(callAiApi(requestPrompt, null));
        } catch (Exception e) {
            log.error("知识点讲解失败", e);
            return ResultUtil.error("知识点讲解失败：" + e.getMessage());
        }
    }

    @Override
    public void addCollection(Long userId, String sessionId, String content) {
        AiCollection collection = new AiCollection();
        collection.setUserId(userId);
        collection.setSessionId(sessionId);
        collection.setContent(content);
        collection.setCreateTime(LocalDateTime.now());
        aiSessionMapper.insertCollection(collection);
    }

    @Override
    public List<AiCollection> getCollections(Long userId) {
        return aiSessionMapper.selectCollectionsByUserId(userId);
    }

    @Override
    public void removeCollection(Long id) {
        aiSessionMapper.deleteCollection(id);
    }

    @Override
    public List<AiSession> getSessions(Long userId) {
        return aiSessionMapper.selectSessionsByUserId(userId);
    }

    @Override
    public void deleteSession(Long userId, Long id) {
        AiSession session = requireOwnedSessionById(userId, id);
        aiSessionMapper.deleteMessagesBySessionId(session.getSessionId());
        aiSessionMapper.deleteSession(id);
    }

    @Override
    public SseEmitter explainCode(String code, String language) {
        SseEmitter emitter = new SseEmitter(60000L);
        String requestPrompt = buildExplainPrompt(code, language);

        CompletableFuture.runAsync(() -> {
            try {
                String response = callAiApi(requestPrompt, null);
                for (String chunk : splitForStreaming(response)) {
                    emitter.send(chunk);
                    Thread.sleep(120);
                }
                emitter.complete();
            } catch (Exception e) {
                log.error("代码解释失败", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    @Override
    public ResultUtil diagnoseError(String code, String errorMessage, String language) {
        try {
            return ResultUtil.success(callAiApi(buildDiagnosePrompt(code, errorMessage, language), null));
        } catch (Exception e) {
            log.error("错误诊断失败", e);
            return ResultUtil.error("错误诊断失败：" + e.getMessage());
        }
    }

    @Override
    public ResultUtil suggestOptimization(String code, String language) {
        try {
            return ResultUtil.success(callAiApi(buildOptimizationPrompt(code, language), null));
        } catch (Exception e) {
            log.error("优化建议失败", e);
            return ResultUtil.error("优化建议失败：" + e.getMessage());
        }
    }

    @Override
    public ResultUtil getLearningSuggestions(Long userId) {
        try {
            List<UserKnowledgeMastery> weakPoints = knowledgeMasteryMapper.selectUserWeakKnowledgePoints(userId, 2);
            StringBuilder weakPointsStr = new StringBuilder();
            for (UserKnowledgeMastery mastery : weakPoints) {
                KnowledgePoint point = knowledgeMasteryMapper.selectKnowledgePointById(mastery.getKnowledgeId());
                if (point != null) {
                    weakPointsStr.append(point.getName()).append("，掌握度 ").append(mastery.getScore()).append("%；");
                }
            }

            String requestPrompt = "基于用户当前薄弱知识点：" + weakPointsStr + "，请给出个性化学习建议，包括复习顺序、练习方法和下一步目标。";
            return ResultUtil.success(callAiApi(requestPrompt, null));
        } catch (Exception e) {
            log.error("学习建议失败", e);
            return ResultUtil.error("学习建议失败：" + e.getMessage());
        }
    }

    private String callAiApi(String question, String code) {
        List<JSONObject> messages = new ArrayList<>();
        addSystemMessage(messages);

        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", appendCode(question, code));
        messages.add(userMessage);

        return callChatApi(messages);
    }

    private void sendStreamEvent(SseEmitter emitter, String eventName, Map<String, Object> data) {
        try {
            emitter.send(SseEmitter.event().name(eventName).data(data));
        } catch (Exception e) {
            throw new RuntimeException("Failed to send stream event", e);
        }
    }

    private void emitFallbackText(SseEmitter emitter, String response) {
        for (String chunk : splitForStreaming(response)) {
            sendStreamEvent(emitter, "delta", Map.of("text", chunk));
        }
    }

    private String callChatApi(List<JSONObject> messages) {
        try {
            String answer = llmClient().chat(currentLlmConfig(), messages, 0.7);
            if (answer == null || answer.isBlank()) {
                return buildLocalFallbackAnswer(extractLastUserMessage(messages), null);
            }
            return answer;
        } catch (Exception e) {
            log.warn("AI LLM 调用失败，使用本地兜底。provider={}, model={}, error={}",
                    aiProvider, aiModel, e.getMessage());
            return buildLocalFallbackAnswer(extractLastUserMessage(messages), null);
        }
    }

    private AiLlmClient llmClient() {
        return aiLlmClient == null ? new AiLlmClient() : aiLlmClient;
    }

    private AiLlmConfig currentLlmConfig() {
        return new AiLlmConfig(aiProvider, aiModel, ollamaUrl, apiUrl, apiKey);
    }

    private void addSystemMessage(List<JSONObject> messages) {
        JSONObject system = new JSONObject();
        system.put("role", "system");
        system.put("content", prompt);
        messages.add(system);
    }

    private String appendCode(String content, String code) {
        String fullContent = content == null ? "" : content;
        if (code != null && !code.isBlank()) {
            fullContent += "\n\n代码：\n" + code;
        }
        return fullContent;
    }

    private void persistChatMessages(String sessionId, String fullContent, String response) {
        AiMessage userMsgRecord = new AiMessage();
        userMsgRecord.setSessionId(sessionId);
        userMsgRecord.setRole("user");
        userMsgRecord.setContent(fullContent);
        userMsgRecord.setMessageKind("chat");
        userMsgRecord.setCreateTime(LocalDateTime.now());
        aiSessionMapper.insertMessage(userMsgRecord);

        AiMessage assistantMsgRecord = new AiMessage();
        assistantMsgRecord.setSessionId(sessionId);
        assistantMsgRecord.setRole("assistant");
        assistantMsgRecord.setContent(response);
        assistantMsgRecord.setMessageKind("chat");
        assistantMsgRecord.setCreateTime(LocalDateTime.now());
        aiSessionMapper.insertMessage(assistantMsgRecord);
    }

    private String extractLastUserMessage(List<JSONObject> messages) {
        if (messages == null || messages.isEmpty()) {
            return "";
        }
        for (int i = messages.size() - 1; i >= 0; i--) {
            JSONObject message = messages.get(i);
            if ("user".equals(message.getString("role"))) {
                return message.getString("content");
            }
        }
        return "";
    }

    private String buildLocalFallbackAnswer(String question, String code) {
        StringBuilder answer = new StringBuilder();
        answer.append("AI 服务暂时不可用，当前使用本地兜底建议。");
        answer.append("建议先把问题拆成输入、状态变化、输出三步来检查。");
        if (question != null && !question.isBlank()) {
            answer.append("针对你的问题：").append(question).append("，先确认边界条件，再用一个最小样例手动走一遍。");
        }
        if (code != null && !code.isBlank()) {
            answer.append("针对当前代码，优先检查循环终止条件、变量更新顺序和输出格式。");
        }
        answer.append("如果需要真实模型回答，请确认 Ollama 已启动且模型可用。");
        return answer.toString();
    }

    private String buildExplainPrompt(String code, String language) {
        return String.format("""
                请详细解释以下 %s 代码的逻辑和算法思路：

                ```%s
                %s
                ```

                请从代码整体思路、关键算法和数据结构、时间复杂度、空间复杂度、可改进点五个方面说明。
                """, language, safeLanguage(language), code);
    }

    private String buildDiagnosePrompt(String code, String errorMessage, String language) {
        return String.format("""
                以下 %s 代码运行时出现错误：

                ```%s
                %s
                ```

                错误信息：
                %s

                请分析错误原因，并给出可执行的修复步骤。
                """, language, safeLanguage(language), code, errorMessage);
    }

    private String buildOptimizationPrompt(String code, String language) {
        return String.format("""
                请优化以下 %s 代码，从性能、可读性、安全性和边界处理方面给出改进建议：

                ```%s
                %s
                ```

                请说明为什么这样优化，并给出必要的代码片段。
                """, language, safeLanguage(language), code);
    }

    private List<String> splitForStreaming(String response) {
        if (response == null || response.isBlank()) {
            return List.of("");
        }
        String[] parts = response.split("(?<=[。！？.!?])");
        List<String> chunks = new ArrayList<>();
        for (String part : parts) {
            if (!part.isBlank()) {
                chunks.add(part);
            }
        }
        return chunks.isEmpty() ? List.of(response) : chunks;
    }

    private String safeLanguage(String language) {
        return language == null || language.isBlank() ? "text" : language.toLowerCase();
    }

    private String buildTopic(String content) {
        if (content == null || content.isBlank()) {
            return "AI 答疑";
        }
        return content.length() > 30 ? content.substring(0, 30) + "..." : content;
    }

    private AiSession requireOwnedSessionById(Long userId, Long id) {
        AiSession session = aiSessionMapper.selectSessionById(id);
        if (session == null || session.getUserId() == null || !session.getUserId().equals(userId)) {
            throw new RuntimeException("Session not found");
        }
        return session;
    }

    private AiSession requireOwnedSessionBySessionId(Long userId, String sessionId) {
        AiSession session = aiSessionMapper.selectSessionBySessionId(sessionId);
        if (session == null || session.getUserId() == null || !session.getUserId().equals(userId)) {
            throw new RuntimeException("Session not found");
        }
        return session;
    }
}
