package com.programming.service.impl;

import com.programming.entity.AiSession;
import com.programming.mapper.AiSessionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiServiceImplDeleteSessionTest {

    private AiServiceImpl aiService;
    private AiSessionMapper aiSessionMapper;

    @BeforeEach
    void setUp() {
        aiService = new AiServiceImpl();
        aiSessionMapper = mock(AiSessionMapper.class);
        ReflectionTestUtils.setField(aiService, "aiSessionMapper", aiSessionMapper);
    }

    @Test
    void deleteSessionAcceptsUuidSessionId() {
        String sessionId = "fbc8edd1-058e-478f-936b-b930e0911e04";
        AiSession session = new AiSession();
        session.setId(42L);
        session.setSessionId(sessionId);
        session.setUserId(7L);
        when(aiSessionMapper.selectSessionBySessionId(sessionId)).thenReturn(session);

        aiService.deleteSession(7L, sessionId);

        verify(aiSessionMapper).deleteMessagesBySessionId(sessionId);
        verify(aiSessionMapper).deleteSession(42L);
    }

    @Test
    void deleteSessionKeepsNumericIdCompatibility() {
        AiSession session = new AiSession();
        session.setId(42L);
        session.setSessionId("fbc8edd1-058e-478f-936b-b930e0911e04");
        session.setUserId(7L);
        when(aiSessionMapper.selectSessionById(42L)).thenReturn(session);

        aiService.deleteSession(7L, "42");

        verify(aiSessionMapper).deleteMessagesBySessionId("fbc8edd1-058e-478f-936b-b930e0911e04");
        verify(aiSessionMapper).deleteSession(42L);
    }
}
