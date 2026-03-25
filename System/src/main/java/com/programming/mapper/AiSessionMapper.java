package com.programming.mapper;

import com.programming.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AiSessionMapper {
    // 会话相关
    AiSession selectSessionById(Long id);
    AiSession selectSessionBySessionId(String sessionId);
    List<AiSession> selectSessionsByUserId(Long userId);
    void insertSession(AiSession session);
    void updateSession(AiSession session);
    void deleteSession(Long id);
    
    // 消息相关
    List<AiMessage> selectMessagesBySessionId(String sessionId);
    void insertMessage(AiMessage message);
    void deleteMessagesBySessionId(String sessionId);
    
    // 收藏相关
    List<AiCollection> selectCollectionsByUserId(Long userId);
    AiCollection selectCollectionById(Long id);
    void insertCollection(AiCollection collection);
    void deleteCollection(Long id);
}