package com.programming.service.ai;

import com.alibaba.fastjson2.JSONObject;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiLlmClientStreamTest {

    @Test
    void streamChatEmitsOllamaDeltasAndReturnsFullResponse() throws Exception {
        AtomicReference<String> requestBody = new AtomicReference<>("");
        HttpServer server = ollamaStreamServer(requestBody);
        server.start();
        try {
            AiLlmClient client = new AiLlmClient();
            List<JSONObject> messages = List.of(message("user", "hello"));
            List<String> deltas = new ArrayList<>();

            String full = client.streamChat(
                    new AiLlmConfig("ollama", "test-model", "http://127.0.0.1:" + server.getAddress().getPort(), "", ""),
                    messages,
                    0.3,
                    deltas::add
            );

            assertTrue(requestBody.get().contains("\"stream\":true"));
            assertEquals(List.of("He", "llo"), deltas);
            assertEquals("Hello", full);
        } finally {
            server.stop(0);
        }
    }

    private JSONObject message(String role, String content) {
        JSONObject json = new JSONObject();
        json.put("role", role);
        json.put("content", content);
        return json;
    }

    private HttpServer ollamaStreamServer(AtomicReference<String> requestBody) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/chat", exchange -> {
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            String response = """
                    {"message":{"role":"assistant","content":"He"},"done":false}
                    {"message":{"role":"assistant","content":"llo"},"done":false}
                    {"done":true}
                    """;
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/x-ndjson; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream body = exchange.getResponseBody()) {
                body.write(bytes);
            }
        });
        return server;
    }
}
