package com.programming.controller;

import com.programming.service.wrongbookagent.WrongBookAgentChatRequestVO;
import com.programming.service.wrongbookagent.WrongBookAgentService;
import com.programming.util.ResultUtil;
import com.programming.vo.problemagent.ProblemAgentSessionVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/wrong-book-agent")
public class WrongBookAgentController {
    private final WrongBookAgentService wrongBookAgentService;

    public WrongBookAgentController(WrongBookAgentService wrongBookAgentService) {
        this.wrongBookAgentService = wrongBookAgentService;
    }

    @GetMapping("/wrong-item/{wrongItemId}/latest")
    public ResultUtil<ProblemAgentSessionVO> getLatest(@PathVariable Long wrongItemId,
                                                       HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            return ResultUtil.success(wrongBookAgentService.getLatestSession(userId, wrongItemId));
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/wrong-item/{wrongItemId}/reflect")
    public ResultUtil<ProblemAgentSessionVO> reflect(@PathVariable Long wrongItemId,
                                                     HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            return ResultUtil.success(wrongBookAgentService.reflect(userId, wrongItemId));
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/chat")
    public ResultUtil<ProblemAgentSessionVO> chat(@RequestBody WrongBookAgentChatRequestVO body,
                                                  HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            return ResultUtil.success(wrongBookAgentService.chat(userId, body));
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/session/{sessionId}")
    public ResultUtil<ProblemAgentSessionVO> getSession(@PathVariable String sessionId,
                                                        HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            return ResultUtil.success(wrongBookAgentService.getSession(userId, sessionId));
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }
}
