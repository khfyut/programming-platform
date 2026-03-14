package com.programming.controller;

import com.programming.service.LearnService;
import com.programming.util.ResultUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/learn")
public class LearnController {

    @Autowired
    private LearnService learnService;

    @GetMapping("/my")
    public ResultUtil<Map<String, Object>> getMyStatistics(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Map<String, Object> result = learnService.getStatistics(userId);
            return ResultUtil.success(result);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/recommend")
    public ResultUtil getRecommend(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            return ResultUtil.success(learnService.getRecommend(userId));
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }
}