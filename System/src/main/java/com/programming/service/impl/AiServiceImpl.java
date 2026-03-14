package com.programming.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.programming.entity.Question;
import com.programming.mapper.QuestionMapper;
import com.programming.service.AiService;
import com.programming.vo.AiAskVO;
import com.programming.util.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        int offset = (page - 1) * size;
        List<Question> list = questionMapper.findByUserId(userId, offset, size);
        int total = questionMapper.countByUserId(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", list);

        return result;
    }
}