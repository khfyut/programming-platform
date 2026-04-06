package com.programming.controller;

import com.programming.service.SubmitService;
import com.programming.util.ResultUtil;
import com.programming.vo.SubmitResultVO;
import com.programming.vo.SubmitVO;
import com.programming.vo.SubmitWithProblemVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/submit")
public class SubmitController {

    @Autowired
    private SubmitService submitService;

    @PostMapping("/commit")
    public ResultUtil<SubmitResultVO> commit(@RequestBody SubmitVO submitVO, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            SubmitResultVO result = submitService.commit(
                    userId,
                    submitVO.getProblemId(),
                    submitVO.getCode(),
                    submitVO.getLanguage()
            );
            return ResultUtil.success(result);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/my")
    public ResultUtil<Map<String, Object>> getMySubmits(
            @RequestParam(required = false) Long problemId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Map<String, Object> result = submitService.getMySubmits(userId, problemId, page, size);
            return ResultUtil.success(result);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/detail/{submitId}")
    public ResultUtil<SubmitWithProblemVO> getSubmitDetail(@PathVariable Long submitId, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            SubmitWithProblemVO submit = submitService.getSubmitDetail(userId, submitId);
            return ResultUtil.success(submit);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }
}
