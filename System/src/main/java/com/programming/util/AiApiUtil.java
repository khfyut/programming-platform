package com.programming.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class AiApiUtil {

    @Value("${programming.ai.api-key}")
    private String apiKey;

    @Value("${programming.ai.api-url}")
    private String apiUrl;

    @Value("${programming.ai.prompt}")
    private String systemPrompt;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    public String getAiAnswer(String question, String code) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-3.5-turbo");

            List<Map<String, String>> messages = new ArrayList<>();

            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);
            messages.add(systemMessage);

            Map<String, String> userMessage = new HashMap<>();
            String content = question;
            if (code != null && !code.isEmpty()) {
                content += "\n\n关联代码：\n```" + getLanguageFromCode(code) + "\n" + code + "\n```";
            }
            userMessage.put("role", "user");
            userMessage.put("content", content);
            messages.add(userMessage);

            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(
                            JSON.toJSONString(requestBody),
                            MediaType.parse("application/json; charset=utf-8")
                    ))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return "AI接口调用失败：" + response.code() + " " + response.message();
                }

                String responseBody = response.body().string();
                JSONObject json = JSON.parseObject(responseBody);

                if (json.containsKey("choices")) {
                    return json.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");
                }

                return "AI返回格式异常：" + responseBody;
            }
        } catch (Exception e) {
            return "AI调用异常：" + e.getMessage();
        }
    }

    private String getLanguageFromCode(String code) {
        if (code.contains("public class") || code.contains("System.out.println")) {
            return "java";
        } else if (code.contains("def ") || code.contains("print(")) {
            return "python";
        } else if (code.contains("function ") || code.contains("console.log")) {
            return "javascript";
        }
        return "text";
    }

}
