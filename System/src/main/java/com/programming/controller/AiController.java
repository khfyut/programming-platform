package com.programming.controller;

import com.programming.service.AiService;
import com.programming.util.ResultUtil;
import com.programming.vo.AiAskVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    @PostMapping("/ask")
    public ResultUtil askQuestion(@RequestBody AiAskVO aiAskVO, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            aiAskVO.setUserId(userId);
            return aiService.askQuestion(aiAskVO);
        } catch (Exception e) {
            return ResultUtil.error("AI 问答失败：" + e.getMessage());
        }
    }

    @PostMapping("/chat")
    public ResultUtil chat(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            params.put("userId", userId);
            return aiService.chat(params);
        } catch (Exception e) {
            return ResultUtil.error("AI 对话失败：" + e.getMessage());
        }
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            params.put("userId", userId);
            return aiService.chatStream(params);
        } catch (Exception e) {
            SseEmitter emitter = new SseEmitter();
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data(Map.of("message", "AI 对话失败：" + e.getMessage())));
            } catch (Exception ignored) {
                // Nothing else to do if the stream cannot be opened.
            }
            emitter.completeWithError(e);
            return emitter;
        }
    }

    @GetMapping("/history")
    public ResultUtil<Map<String, Object>> getHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Map<String, Object> result = aiService.getHistory(userId, page, size);
            return ResultUtil.success(result);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @DeleteMapping("/history/{sessionKey}")
    public ResultUtil deleteHistory(@PathVariable String sessionKey, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            aiService.deleteSession(userId, sessionKey);
            return ResultUtil.success("删除成功");
        } catch (Exception e) {
            return ResultUtil.error("删除失败：" + e.getMessage());
        }
    }

    @GetMapping("/session/{sessionId}")
    public ResultUtil getSessionMessages(@PathVariable String sessionId, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            var messages = aiService.getChatHistory(userId, sessionId);
            return ResultUtil.success(messages);
        } catch (Exception e) {
            return ResultUtil.error("获取消息失败：" + e.getMessage());
        }
    }

    @GetMapping("/explain-code")
    public SseEmitter explainCode(
            @RequestParam String code,
            @RequestParam String language,
            HttpServletRequest request) {
        try {
            return aiService.explainCode(code, language);
        } catch (Exception e) {
            SseEmitter emitter = new SseEmitter();
            emitter.completeWithError(e);
            return emitter;
        }
    }

    @PostMapping("/diagnose-error")
    public ResultUtil diagnoseError(
            @RequestParam String code,
            @RequestParam String errorMessage,
            @RequestParam String language,
            HttpServletRequest request) {
        try {
            return aiService.diagnoseError(code, errorMessage, language);
        } catch (Exception e) {
            return ResultUtil.error("错误诊断失败：" + e.getMessage());
        }
    }

    @PostMapping("/suggest-optimization")
    public ResultUtil suggestOptimization(
            @RequestParam String code,
            @RequestParam String language,
            HttpServletRequest request) {
        try {
            return aiService.suggestOptimization(code, language);
        } catch (Exception e) {
            return ResultUtil.error("优化建议失败：" + e.getMessage());
        }
    }

    @GetMapping("/learning-suggestions")
    public ResultUtil getLearningSuggestions(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            return aiService.getLearningSuggestions(userId);
        } catch (Exception e) {
            return ResultUtil.error("获取学习建议失败：" + e.getMessage());
        }
    }
}
