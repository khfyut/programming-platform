package com.programming.controller;

import com.programming.agent.dto.AgentDecisionDTO;
import com.programming.agent.dto.AgentFeedbackRequestDTO;
import com.programming.service.AgentFeedbackService;
import com.programming.service.AgentService;
import com.programming.util.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    @Autowired
    private AgentService agentService;

    @Autowired
    private AgentFeedbackService agentFeedbackService;

    @PostMapping("/advice/{submitId}")
    public ResultUtil getLearningAdvice(@RequestAttribute("userId") Long userId,
                                        @PathVariable Long submitId) {
        try {
            AgentDecisionDTO decision = agentService.processSubmission(userId, submitId);
            return ResultUtil.success(decision);
        } catch (Exception e) {
            return ResultUtil.error("获取学习建议失败: " + e.getMessage());
        }
    }

    @PostMapping("/feedback")
    public ResultUtil recordFeedback(@RequestAttribute("userId") Long userId,
                                     @RequestBody AgentFeedbackRequestDTO request) {
        try {
            return ResultUtil.success(agentFeedbackService.recordFeedback(userId, request));
        } catch (Exception e) {
            return ResultUtil.error("记录Agent反馈失败: " + e.getMessage());
        }
    }

    @PostMapping("/wrong-book/{wrongItemId}/reflect")
    public ResultUtil reflectWrongBookItem(@RequestAttribute("userId") Long userId,
                                           @PathVariable Long wrongItemId) {
        try {
            return ResultUtil.success(agentService.processWrongBookReflection(userId, wrongItemId));
        } catch (Exception e) {
            return ResultUtil.error("生成错题复盘失败: " + e.getMessage());
        }
    }

    @PostMapping("/learning-path/{pathId}/level/{levelId}/recommend")
    public ResultUtil recommendLearningPathNextStep(@RequestAttribute("userId") Long userId,
                                                    @PathVariable Long pathId,
                                                    @PathVariable Long levelId) {
        try {
            return ResultUtil.success(agentService.processLearningPathRecommendation(userId, pathId, levelId));
        } catch (Exception e) {
            return ResultUtil.error("生成学习路径推荐失败: " + e.getMessage());
        }
    }

    @GetMapping("/report/summary")
    public ResultUtil getAgentReportSummary(@RequestAttribute("userId") Long userId) {
        try {
            return ResultUtil.success(agentService.getAgentReportSummary(userId));
        } catch (Exception e) {
            return ResultUtil.error("获取Agent学习摘要失败: " + e.getMessage());
        }
    }
}
