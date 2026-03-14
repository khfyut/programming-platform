package com.programming.controller;

import com.programming.service.AiService;
import com.programming.util.ResultUtil;
import com.programming.vo.AiAskVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
            return ResultUtil.error("AI问答失败：" + e.getMessage());
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
}
