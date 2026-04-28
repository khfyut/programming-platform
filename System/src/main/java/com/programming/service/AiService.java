package com.programming.service;

import com.programming.entity.*;
import com.programming.util.ResultUtil;
import com.programming.vo.AiAskVO;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

public interface AiService {
    ResultUtil askQuestion(AiAskVO aiAskVO);
    Map<String, Object> getHistory(Long userId, int page, int size);
    
    // 多轮对话相关
    ResultUtil chat(Map<String, Object> params);
    SseEmitter chatStream(Map<String, Object> params);
    List<AiMessage> getChatHistory(Long userId, String sessionId);
    
    // 代码优化相关
    ResultUtil optimizeCode(String code, String language);
    
    // 知识点讲解相关
    ResultUtil explainKnowledge(String knowledgePoint);
    
    // 收藏相关
    void addCollection(Long userId, String sessionId, String content);
    List<AiCollection> getCollections(Long userId);
    void removeCollection(Long id);
    
    // 会话管理
    List<AiSession> getSessions(Long userId);
    void deleteSession(Long userId, Long id);
    
    // AI能力增强
    SseEmitter explainCode(String code, String language);
    ResultUtil diagnoseError(String code, String errorMessage, String language);
    ResultUtil suggestOptimization(String code, String language);
    ResultUtil getLearningSuggestions(Long userId);
}
