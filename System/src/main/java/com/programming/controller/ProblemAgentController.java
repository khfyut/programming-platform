package com.programming.controller;

import com.programming.service.problemagent.ProblemCoachService;
import com.programming.util.ResultUtil;
import com.programming.vo.problemagent.ProblemAgentChatRequestVO;
import com.programming.vo.problemagent.ProblemAgentResponseVO;
import com.programming.vo.problemagent.ProblemAgentSessionVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/problem-agent")
public class ProblemAgentController {

    @Autowired
    private ProblemCoachService problemCoachService;

    @PostMapping("/chat")
    public ResultUtil<ProblemAgentResponseVO> chat(@RequestBody ProblemAgentChatRequestVO request,
                                                   HttpServletRequest httpServletRequest) {
        try {
            Long userId = (Long) httpServletRequest.getAttribute("userId");
            return ResultUtil.success(problemCoachService.chat(userId, request));
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/session/{sessionId}")
    public ResultUtil<ProblemAgentSessionVO> getSession(@PathVariable String sessionId,
                                                        HttpServletRequest httpServletRequest) {
        try {
            Long userId = (Long) httpServletRequest.getAttribute("userId");
            return ResultUtil.success(problemCoachService.getSession(userId, sessionId));
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/problem/{problemId}/latest")
    public ResultUtil<ProblemAgentSessionVO> getLatestSession(@PathVariable Long problemId,
                                                              HttpServletRequest httpServletRequest) {
        try {
            Long userId = (Long) httpServletRequest.getAttribute("userId");
            return ResultUtil.success(problemCoachService.getLatestSession(userId, problemId));
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }
}
