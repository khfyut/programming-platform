package com.programming.service.impl;

import com.programming.entity.AiMessage;
import com.programming.mapper.AiSessionMapper;
import com.programming.mapper.KnowledgeMasteryMapper;
import com.programming.mapper.QuestionMapper;
import com.programming.util.ResultUtil;
import com.programming.vo.AiAskVO;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiServiceImplFallbackTest {

    private AiServiceImpl aiService;
    private QuestionMapper questionMapper;

    @BeforeEach
    void setUp() {
        aiService = new AiServiceImpl();
        ReflectionTestUtils.setField(aiService, "apiKey", "");
        ReflectionTestUtils.setField(aiService, "apiUrl", "http://127.0.0.1:1");
        ReflectionTestUtils.setField(aiService, "prompt", "You are a programming tutor.");

        AiSessionMapper aiSessionMapper = mock(AiSessionMapper.class);
        when(aiSessionMapper.selectMessagesBySessionId(anyString())).thenReturn(List.<AiMessage>of());
        ReflectionTestUtils.setField(aiService, "aiSessionMapper", aiSessionMapper);
        questionMapper = mock(QuestionMapper.class);
        ReflectionTestUtils.setField(aiService, "questionMapper", questionMapper);
        ReflectionTestUtils.setField(aiService, "knowledgeMasteryMapper", mock(KnowledgeMasteryMapper.class));
    }

    @Test
    void chatUsesOllamaWhenDashScopeApiKeyIsMissing() throws Exception {
        AtomicInteger requests = new AtomicInteger();
        HttpServer server = ollamaServer(requests, "来自 Ollama 的真实回答");
        server.start();
        try {
            ReflectionTestUtils.setField(aiService, "ollamaUrl", "http://127.0.0.1:" + server.getAddress().getPort());
            ReflectionTestUtils.setField(aiService, "aiProvider", "ollama");
            ReflectionTestUtils.setField(aiService, "aiModel", "gemma4:e2b");

            ResultUtil result = aiService.chat(Map.of(
                    "userId", 1L,
                    "content", "什么是 Java 循环？",
                    "code", ""
            ));

            assertEquals(200, result.getCode());
            Map<?, ?> data = assertInstanceOf(Map.class, result.getData());
            assertEquals("来自 Ollama 的真实回答", data.get("response"));
            assertEquals(1, requests.get());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void askQuestionUsesOllamaAndSavesQuestionWhenDashScopeApiKeyIsMissing() throws Exception {
        AtomicInteger requests = new AtomicInteger();
        HttpServer server = ollamaServer(requests, "Ollama 单轮回答");
        server.start();
        try {
            ReflectionTestUtils.setField(aiService, "ollamaUrl", "http://127.0.0.1:" + server.getAddress().getPort());
            ReflectionTestUtils.setField(aiService, "aiProvider", "ollama");
            ReflectionTestUtils.setField(aiService, "aiModel", "gemma4:e2b");

            AiAskVO request = new AiAskVO();
            request.setUserId(1L);
            request.setContent("How do I debug a loop?");
            request.setCode("for(;;){}");

            ResultUtil result = aiService.askQuestion(request);

            assertEquals(200, result.getCode());
            assertEquals("Ollama 单轮回答", result.getData());
            assertEquals(1, requests.get());
            verify(questionMapper).insert(org.mockito.ArgumentMatchers.argThat(question ->
                    "How do I debug a loop?".equals(question.getContent())
                            && "Ollama 单轮回答".equals(question.getAnswer())
            ));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void chatReturnsLocalFallbackWhenOllamaIsUnavailable() {
        ReflectionTestUtils.setField(aiService, "ollamaUrl", "http://127.0.0.1:1");
        ReflectionTestUtils.setField(aiService, "aiProvider", "ollama");
        ReflectionTestUtils.setField(aiService, "aiModel", "gemma4:e2b");

        ResultUtil result = aiService.chat(Map.of(
                "userId", 1L,
                "content", "What is a loop?",
                "code", ""
        ));

        assertEquals(200, result.getCode());
        Map<?, ?> data = assertInstanceOf(Map.class, result.getData());
        String response = String.valueOf(data.get("response"));
        assertFalse(response.isBlank());
        assertTrue(response.contains("AI 服务暂时不可用"));
    }

    @Test
    void askQuestionReturnsLocalFallbackWhenApiKeyIsMissing() {
        ReflectionTestUtils.setField(aiService, "ollamaUrl", "http://127.0.0.1:1");
        ReflectionTestUtils.setField(aiService, "aiProvider", "ollama");
        ReflectionTestUtils.setField(aiService, "aiModel", "gemma4:e2b");

        AiAskVO request = new AiAskVO();
        request.setUserId(1L);
        request.setContent("How do I debug a loop?");
        request.setCode("for(;;){}");

        ResultUtil result = aiService.askQuestion(request);

        assertEquals(200, result.getCode());
        String answer = String.valueOf(result.getData());
        assertFalse(answer.isBlank());
        assertTrue(answer.contains("AI 服务暂时不可用"));
    }

    private HttpServer ollamaServer(AtomicInteger requests, String answer) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/chat", exchange -> {
            requests.incrementAndGet();
            String response = "{\"message\":{\"role\":\"assistant\",\"content\":\"" + answer + "\"}}";
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream body = exchange.getResponseBody()) {
                body.write(bytes);
            }
        });
        return server;
    }
}
