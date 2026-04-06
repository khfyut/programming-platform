package com.programming.controller;

import com.programming.service.WrongBookService;
import com.programming.util.ResultUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/wrong-book")
public class WrongBookController {

    @Autowired
    private WrongBookService wrongBookService;

    // 获取错题列表
    @GetMapping("/list")
    public ResultUtil getWrongBookList(HttpServletRequest request, 
                                      @RequestParam(required = false) String knowledgePoint, 
                                      @RequestParam(required = false) Integer difficulty) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            return ResultUtil.success(wrongBookService.getWrongBookItems(userId, knowledgePoint, difficulty));
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    // 获取错题详情
    @GetMapping("/detail/{id}")
    public ResultUtil getWrongBookDetail(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            return ResultUtil.success(wrongBookService.getWrongBookItemById(userId, id));
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    // 标记错题复习状态
    @PostMapping("/review")
    public ResultUtil markReviewStatus(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Long id = Long.valueOf(params.get("id").toString());
            Integer status = Integer.valueOf(params.get("status").toString());
            wrongBookService.updateWrongBookItemStatus(userId, id, status);
            return ResultUtil.success(null);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    // 获取复习计划
    @GetMapping("/review/plan")
    public ResultUtil getReviewPlan(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            return ResultUtil.success(wrongBookService.getReviewPlans(userId));
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    // 获取待复习的错题
    @GetMapping("/review/pending")
    public ResultUtil getPendingReview(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            return ResultUtil.success(wrongBookService.getPendingReviewPlans(userId));
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    // 更新复习计划
    @PostMapping("/review/update")
    public ResultUtil updateReviewPlan(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        try {
            Long id = Long.valueOf(params.get("id").toString());
            boolean reviewed = Boolean.valueOf(params.get("reviewed").toString());
            wrongBookService.updateReviewPlan(id, reviewed);
            return ResultUtil.success(null);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    // 获取推荐题目
    @GetMapping("/recommend")
    public ResultUtil getRecommendedProblems(HttpServletRequest request, 
                                           @RequestParam Long wrongItemId, 
                                           @RequestParam Integer limit) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            return ResultUtil.success(wrongBookService.getRecommendedProblems(userId, wrongItemId, limit));
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    // 获取错题统计信息
    @GetMapping("/statistics")
    public ResultUtil getWrongBookStatistics(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            return ResultUtil.success(wrongBookService.getWrongBookStatistics(userId));
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    // 获取按知识点分布的错题
    @GetMapping("/distribution/knowledge")
    public ResultUtil getWrongDistributionByKnowledge(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            return ResultUtil.success(wrongBookService.getWrongDistributionByKnowledgePoint(userId));
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    // 获取按难度分布的错题
    @GetMapping("/distribution/difficulty")
    public ResultUtil getWrongDistributionByDifficulty(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            return ResultUtil.success(wrongBookService.getWrongDistributionByDifficulty(userId));
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    // 删除错题
    @PostMapping("/delete/{id}")
    public ResultUtil deleteWrongItem(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            wrongBookService.removeWrongBookItem(userId, id);
            return ResultUtil.success(null);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }
}
