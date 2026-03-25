package com.programming.controller;

import com.programming.service.ReferenceSolutionService;
import com.programming.vo.ReferenceSolutionVO;
import com.programming.util.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reference-solution")
public class ReferenceSolutionController {

    @Autowired
    private ReferenceSolutionService referenceSolutionService;

    /**
     * 获取参考答案
     */
    @GetMapping("/{problemId}")
    public Object getReferenceSolution(
            @PathVariable Long problemId,
            @RequestParam(required = false) String language,
            @RequestAttribute Long userId) {

        // 1. 检查用户是否有权限查看
        ReferenceSolutionService.PermissionCheckResult permission = referenceSolutionService
                .checkViewPermission(userId, problemId);

        if (!permission.isCanView()) {
            return ResultUtil.error(permission.getMessage());
        }

        // 2. 获取参考答案
        ReferenceSolutionVO solution = referenceSolutionService
                .getSolution(problemId, language);

        if (solution == null) {
            return ResultUtil.error("暂无参考答案");
        }

        // 3. 记录查看行为
        referenceSolutionService.recordViewBehavior(userId, problemId);

        return ResultUtil.success(solution);
    }

    /**
     * 获取渐进式提示
     */
    @GetMapping("/{problemId}/hint")
    public Object getHint(
            @PathVariable Long problemId,
            @RequestParam(defaultValue = "1") Integer hintLevel) {

        String hint = referenceSolutionService.getHint(problemId, hintLevel);
        return ResultUtil.success(hint);
    }

    /**
     * 获取可用语言列表
     */
    @GetMapping("/{problemId}/languages")
    public Object getAvailableLanguages(@PathVariable Long problemId) {
        String[] languages = referenceSolutionService.getAvailableLanguages(problemId);
        return ResultUtil.success(languages);
    }
}