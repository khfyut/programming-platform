package com.programming.service.ai;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Component
public class AiLlmClient {
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client;

    public AiLlmClient() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(120))
                .writeTimeout(Duration.ofSeconds(30))
                .build();
    }

    public String chat(AiLlmConfig config, List<JSONObject> messages, double temperature) throws IOException {
        String provider = config.normalizedProvider();
        if ("openai-compatible".equals(provider)) {
            return chatOpenAiCompatible(config, messages, temperature);
        }
        if ("ollama".equals(provider)) {
            return chatOllama(config, messages, temperature);
        }
        throw new IllegalArgumentException("Unsupported AI provider: " + config.provider());
    }

    public String streamChat(AiLlmConfig config,
                             List<JSONObject> messages,
                             double temperature,
                             Consumer<String> onDelta) throws IOException {
        String provider = config.normalizedProvider();
        if ("openai-compatible".equals(provider)) {
            return streamOpenAiCompatible(config, messages, temperature, onDelta);
        }
        if ("ollama".equals(provider)) {
            return streamOllama(config, messages, temperature, onDelta);
        }
        throw new IllegalArgumentException("Unsupported AI provider: " + config.provider());
    }

    private String chatOllama(AiLlmConfig config, List<JSONObject> messages, double temperature) throws IOException {
        String baseUrl = trimTrailingSlash(config.ollamaUrl() == null || config.ollamaUrl().isBlank()
                ? "http://127.0.0.1:11434"
                : config.ollamaUrl());

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", config.normalizedModel());
        requestBody.put("messages", new JSONArray(messages));
        requestBody.put("stream", false);

        JSONObject options = new JSONObject();
        options.put("temperature", temperature);
        requestBody.put("options", options);

        Request request = new Request.Builder()
                .url(baseUrl + "/api/chat")
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toJSONString(), JSON_MEDIA_TYPE))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String body = response.body() == null ? "" : response.body().string();
            if (!response.isSuccessful()) {
                log.warn("Ollama call failed, status={}, body={}", response.code(), body);
                throw new IOException("Ollama call failed: " + response.code());
            }

            JSONObject json = JSON.parseObject(body);
            JSONObject message = json.getJSONObject("message");
            if (message != null && message.getString("content") != null) {
                return message.getString("content");
            }
            String responseText = json.getString("response");
            return responseText == null ? "" : responseText;
        }
    }

    private String chatOpenAiCompatible(AiLlmConfig config, List<JSONObject> messages, double temperature) throws IOException {
        if (config.apiKey() == null || config.apiKey().isBlank()) {
            throw new IOException("OpenAI-compatible API key is not configured");
        }
        String apiUrl = trimTrailingSlash(config.apiUrl() == null || config.apiUrl().isBlank()
                ? "https://dashscope.aliyuncs.com/compatible-mode/v1"
                : config.apiUrl());

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", config.normalizedModel());
        requestBody.put("messages", new JSONArray(messages));
        requestBody.put("temperature", temperature);

        Request request = new Request.Builder()
                .url(apiUrl + "/chat/completions")
                .addHeader("Authorization", "Bearer " + config.apiKey())
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toJSONString(), JSON_MEDIA_TYPE))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String body = response.body() == null ? "" : response.body().string();
            if (!response.isSuccessful()) {
                log.warn("OpenAI-compatible call failed, status={}, body={}", response.code(), body);
                throw new IOException("OpenAI-compatible call failed: " + response.code());
            }

            JSONObject json = JSON.parseObject(body);
            return json.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
        }
    }

    private String streamOllama(AiLlmConfig config,
                                List<JSONObject> messages,
                                double temperature,
                                Consumer<String> onDelta) throws IOException {
        String baseUrl = trimTrailingSlash(config.ollamaUrl() == null || config.ollamaUrl().isBlank()
                ? "http://127.0.0.1:11434"
                : config.ollamaUrl());

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", config.normalizedModel());
        requestBody.put("messages", new JSONArray(messages));
        requestBody.put("stream", true);

        JSONObject options = new JSONObject();
        options.put("temperature", temperature);
        requestBody.put("options", options);

        Request request = new Request.Builder()
                .url(baseUrl + "/api/chat")
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toJSONString(), JSON_MEDIA_TYPE))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                String body = response.body() == null ? "" : response.body().string();
                log.warn("Ollama stream call failed, status={}, body={}", response.code(), body);
                throw new IOException("Ollama stream call failed: " + response.code());
            }

            StringBuilder full = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    response.body().byteStream(),
                    StandardCharsets.UTF_8
            ))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) {
                        continue;
                    }
                    JSONObject json = JSON.parseObject(line);
                    JSONObject message = json.getJSONObject("message");
                    String delta = message == null ? json.getString("response") : message.getString("content");
                    if (delta != null && !delta.isEmpty()) {
                        full.append(delta);
                        if (onDelta != null) {
                            onDelta.accept(delta);
                        }
                    }
                    if (Boolean.TRUE.equals(json.getBoolean("done"))) {
                        break;
                    }
                }
            }
            return full.toString();
        }
    }

    private String streamOpenAiCompatible(AiLlmConfig config,
                                          List<JSONObject> messages,
                                          double temperature,
                                          Consumer<String> onDelta) throws IOException {
        if (config.apiKey() == null || config.apiKey().isBlank()) {
            throw new IOException("OpenAI-compatible API key is not configured");
        }
        String apiUrl = trimTrailingSlash(config.apiUrl() == null || config.apiUrl().isBlank()
                ? "https://dashscope.aliyuncs.com/compatible-mode/v1"
                : config.apiUrl());

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", config.normalizedModel());
        requestBody.put("messages", new JSONArray(messages));
        requestBody.put("temperature", temperature);
        requestBody.put("stream", true);

        Request request = new Request.Builder()
                .url(apiUrl + "/chat/completions")
                .addHeader("Authorization", "Bearer " + config.apiKey())
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toJSONString(), JSON_MEDIA_TYPE))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                String body = response.body() == null ? "" : response.body().string();
                log.warn("OpenAI-compatible stream call failed, status={}, body={}", response.code(), body);
                throw new IOException("OpenAI-compatible stream call failed: " + response.code());
            }

            StringBuilder full = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    response.body().byteStream(),
                    StandardCharsets.UTF_8
            ))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("data:")) {
                        continue;
                    }
                    String data = line.substring("data:".length()).trim();
                    if ("[DONE]".equals(data)) {
                        break;
                    }
                    JSONObject json = JSON.parseObject(data);
                    JSONArray choices = json.getJSONArray("choices");
                    if (choices == null || choices.isEmpty()) {
                        continue;
                    }
                    JSONObject deltaObject = choices.getJSONObject(0).getJSONObject("delta");
                    String delta = deltaObject == null ? null : deltaObject.getString("content");
                    if (delta != null && !delta.isEmpty()) {
                        full.append(delta);
                        if (onDelta != null) {
                            onDelta.accept(delta);
                        }
                    }
                }
            }
            return full.toString();
        }
    }

    private String trimTrailingSlash(String value) {
        String trimmed = value.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
