package com.programming.handler;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class JudgeProgressHandler extends TextWebSocketHandler {
    
    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private static final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        log.info("WebSocket连接建立: {}", session.getId());
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String payload = message.getPayload();
            Map<String, Object> data = JSON.parseObject(payload, Map.class);
            if (data.containsKey("userId")) {
                Long userId = ((Number) data.get("userId")).longValue();
                associateUser(userId, session);
                log.info("收到用户ID关联请求: {}", userId);
            }
        } catch (Exception e) {
            log.error("处理WebSocket消息失败", e);
        }
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        // 移除用户会话映射
        userSessions.entrySet().removeIf(entry -> entry.getValue().getId().equals(session.getId()));
        log.info("WebSocket连接关闭: {}", session.getId());
    }
    
    /**
     * 关联用户ID和WebSocket会话
     */
    public void associateUser(Long userId, WebSocketSession session) {
        userSessions.put(userId.toString(), session);
        log.info("用户 {} 关联WebSocket会话: {}", userId, session.getId());
    }
    
    /**
     * 发送判题进度
     */
    public void sendProgress(Long userId, Object progress) {
        WebSocketSession session = userSessions.get(userId.toString());
        if (session != null && session.isOpen()) {
            try {
                String message = JSON.toJSONString(progress);
                session.sendMessage(new TextMessage(message));
                log.info("发送判题进度给用户 {}: {}", userId, message);
            } catch (IOException e) {
                log.error("发送判题进度失败", e);
            }
        }
    }
    
    /**
     * 发送判题完成结果
     */
    public void sendComplete(Long userId, Object result) {
        WebSocketSession session = userSessions.get(userId.toString());
        if (session != null && session.isOpen()) {
            try {
                Map<String, Object> completeMessage = Map.of(
                    "type", "complete",
                    "result", result
                );
                String message = JSON.toJSONString(completeMessage);
                session.sendMessage(new TextMessage(message));
                log.info("发送判题完成结果给用户 {}: {}", userId, message);
            } catch (IOException e) {
                log.error("发送判题完成结果失败", e);
            }
        }
    }
    
    /**
     * 发送错误信息
     */
    public void sendError(Long userId, String errorMessage) {
        WebSocketSession session = userSessions.get(userId.toString());
        if (session != null && session.isOpen()) {
            try {
                Map<String, Object> errorMessageObj = Map.of(
                    "type", "error",
                    "message", errorMessage
                );
                String message = JSON.toJSONString(errorMessageObj);
                session.sendMessage(new TextMessage(message));
                log.info("发送错误信息给用户 {}: {}", userId, message);
            } catch (IOException e) {
                log.error("发送错误信息失败", e);
            }
        }
    }
}
