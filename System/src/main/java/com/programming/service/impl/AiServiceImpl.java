package com.programming.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.programming.entity.*;
import com.programming.mapper.QuestionMapper;
import com.programming.mapper.AiSessionMapper;
import com.programming.mapper.KnowledgeMasteryMapper;
import com.programming.service.AiService;
import com.programming.vo.AiAskVO;
import com.programming.util.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
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

    @Value("${programming.ai.api-key}")
    private String apiKey;

    @Value("${programming.ai.api-url}")
    private String apiUrl;

    @Value("${programming.ai.prompt}")
    private String prompt;

    private final OkHttpClient client = new OkHttpClient();

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
            
            questionMapper.insert(questionRecord);
            
            return ResultUtil.success(answer);
        } catch (Exception e) {
            log.error("AI问答失败", e);
            return ResultUtil.error("AI问答失败：" + e.getMessage());
        }
    }

    private String callAiApi(String question, String code) {
        try {
            log.info("调用AI API - 问题: {}, 代码: {}", question, code);
            
            String fullPrompt = prompt + "\n\n用户问题：" + question;
            if (code != null && !code.isEmpty()) {
                fullPrompt += "\n\n用户代码：\n" + code;
            }
            
            log.info("完整提示词: {}", fullPrompt);
            
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "qwen-turbo");
            
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", fullPrompt);
            
            requestBody.put("messages", new Object[]{message});
            requestBody.put("temperature", 0.7);
            
            Request request = new Request.Builder()
                    .url(apiUrl + "/chat/completions")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody.toJSONString(), MediaType.parse("application/json")))
                    .build();
            
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("AI API调用失败，状态码: {}, 响应: {}", response.code(), response.body().string());
                    throw new RuntimeException("AI API调用失败: " + response.code());
                }
                
                String responseBody = response.body().string();
                log.info("AI API响应: {}", responseBody);
                
                JSONObject jsonResponse = JSON.parseObject(responseBody);
                String answer = jsonResponse.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");
                
                return answer;
            }
        } catch (Exception e) {
            log.error("调用AI API异常", e);
            throw new RuntimeException("调用AI API异常: " + e.getMessage());
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
            String code = params.get("code") != null ? params.get("code").toString() : null;

            // 检查会话是否存在，不存在则创建新会话
            AiSession existingSession = null;
            if (sessionId != null && !sessionId.isEmpty()) {
                existingSession = aiSessionMapper.selectSessionBySessionId(sessionId);
                if (existingSession != null && (existingSession.getUserId() == null || !existingSession.getUserId().equals(userId))) {
                    throw new RuntimeException("Session not found");
                }
            }
            
            if (existingSession == null) {
                // 会话不存在，创建新会话
                sessionId = UUID.randomUUID().toString();
                AiSession session = new AiSession();
                session.setSessionId(sessionId);
                session.setUserId(userId);
                String topic = content.length() > 30 ? content.substring(0, 30) + "..." : content;
                session.setTopic(topic);
                session.setCreateTime(LocalDateTime.now());
                session.setUpdateTime(LocalDateTime.now());
                aiSessionMapper.insertSession(session);
            } else {
                // 更新会话的更新时间
                existingSession.setUpdateTime(LocalDateTime.now());
                aiSessionMapper.updateSession(existingSession);
            }

            // 构建消息历史
            List<AiMessage> history = aiSessionMapper.selectMessagesBySessionId(sessionId);
            List<JSONObject> messages = new ArrayList<>();

            // 添加历史消息
            for (AiMessage msg : history) {
                JSONObject message = new JSONObject();
                message.put("role", msg.getRole());
                message.put("content", msg.getContent());
                messages.add(message);
            }

            // 添加新消息
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            String fullContent = content;
            if (code != null && !code.isEmpty()) {
                fullContent += "\n\n代码：\n" + code;
            }
            userMessage.put("content", fullContent);
            messages.add(userMessage);

            // 调用AI API
            String response = callChatApi(messages);

            // 保存消息记录
            AiMessage userMsgRecord = new AiMessage();
            userMsgRecord.setSessionId(sessionId);
            userMsgRecord.setRole("user");
            userMsgRecord.setContent(fullContent);
            userMsgRecord.setCreateTime(LocalDateTime.now());
            aiSessionMapper.insertMessage(userMsgRecord);

            AiMessage assistantMsgRecord = new AiMessage();
            assistantMsgRecord.setSessionId(sessionId);
            assistantMsgRecord.setRole("assistant");
            assistantMsgRecord.setContent(response);
            assistantMsgRecord.setCreateTime(LocalDateTime.now());
            aiSessionMapper.insertMessage(assistantMsgRecord);

            Map<String, Object> result = new HashMap<>();
            result.put("sessionId", sessionId);
            result.put("response", response);

            return ResultUtil.success(result);
        } catch (Exception e) {
            log.error("AI多轮对话失败", e);
            return ResultUtil.error("AI多轮对话失败：" + e.getMessage());
        }
    }

    @Override
    public List<AiMessage> getChatHistory(Long userId, String sessionId) {
        AiSession session = requireOwnedSessionBySessionId(userId, sessionId);
        return aiSessionMapper.selectMessagesBySessionId(session.getSessionId());
    }

    @Override
    public ResultUtil optimizeCode(String code, String language) {
        try {
            String prompt = "请优化以下" + language + "代码，使其更高效、更可读：\n" + code;
            String response = callAiApi(prompt, null);
            return ResultUtil.success(response);
        } catch (Exception e) {
            log.error("代码优化失败", e);
            return ResultUtil.error("代码优化失败：" + e.getMessage());
        }
    }

    @Override
    public ResultUtil explainKnowledge(String knowledgePoint) {
        try {
            String prompt = "请详细讲解以下编程知识点：" + knowledgePoint + "，包括基本概念、使用方法、常见问题和最佳实践。";
            String response = callAiApi(prompt, null);
            return ResultUtil.success(response);
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
        
        String prompt = buildExplainPrompt(code, language);
        
        CompletableFuture.runAsync(() -> {
            try {
                String response = callAiApi(prompt, null);
                // 模拟流式响应
                String[] chunks = response.split("\\. ");
                for (String chunk : chunks) {
                    emitter.send(chunk + ". ");
                    Thread.sleep(500);
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
            String prompt = buildDiagnosePrompt(code, errorMessage, language);
            String response = callAiApi(prompt, null);
            return ResultUtil.success(response);
        } catch (Exception e) {
            log.error("错误诊断失败", e);
            return ResultUtil.error("错误诊断失败：" + e.getMessage());
        }
    }

    @Override
    public ResultUtil suggestOptimization(String code, String language) {
        try {
            String prompt = buildOptimizationPrompt(code, language);
            String response = callAiApi(prompt, null);
            return ResultUtil.success(response);
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
                    weakPointsStr.append(point.getName()).append(" (掌握度: " + mastery.getScore() + "%), ");
                }
            }
            
            String prompt = "基于用户的薄弱知识点：" + weakPointsStr.toString() + "，请提供个性化的学习建议，包括推荐学习资源、练习方法和学习路径。";
            String response = callAiApi(prompt, null);
            return ResultUtil.success(response);
        } catch (Exception e) {
            log.error("学习建议失败", e);
            return ResultUtil.error("学习建议失败：" + e.getMessage());
        }
    }

    private String buildExplainPrompt(String code, String language) {
        return String.format(
            "请详细解释以下%s代码的逻辑和算法思路:\n\n```%s\n%s\n```\n\n" +
            "请从以下几个方面进行解释:\n" +
            "1. 代码整体思路\n" +
            "2. 关键算法和数据结构\n" +
            "3. 时间复杂度和空间复杂度分析\n" +
            "4. 代码优化建议",
            language, language.toLowerCase(), code
        );
    }

    private String buildDiagnosePrompt(String code, String errorMessage, String language) {
        return String.format(
            "以下%s代码运行时出现错误:\n\n```%s\n%s\n```\n\n" +
            "错误信息:\n%s\n\n" +
            "请分析错误原因并提供修复方案",
            language, language.toLowerCase(), code, errorMessage
        );
    }

    private String buildOptimizationPrompt(String code, String language) {
        return String.format(
            "请优化以下%s代码，从性能、可读性、安全性等方面提供改进建议:\n\n```%s\n%s\n```\n\n" +
            "请提供具体的优化方案和优化后的代码",
            language, language.toLowerCase(), code
        );
    }

    private String callChatApi(List<JSONObject> messages) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "qwen-turbo");
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);

            Request request = new Request.Builder()
                    .url(apiUrl + "/chat/completions")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody.toJSONString(), MediaType.parse("application/json")))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("AI API调用失败，状态码: {}, 响应: {}", response.code(), response.body().string());
                    throw new RuntimeException("AI API调用失败: " + response.code());
                }

                String responseBody = response.body().string();
                log.info("AI API响应: {}", responseBody);

                JSONObject jsonResponse = JSON.parseObject(responseBody);
                String answer = jsonResponse.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");

                return answer;
            }
        } catch (Exception e) {
            log.error("调用AI API异常", e);
            throw new RuntimeException("调用AI API异常: " + e.getMessage());
        }
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

    @Autowired
    private KnowledgeMasteryMapper knowledgeMasteryMapper;

    @Autowired
    private AiSessionMapper aiSessionMapper;
}
