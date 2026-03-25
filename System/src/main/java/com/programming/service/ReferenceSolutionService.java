package com.programming.service;

import com.programming.vo.ReferenceSolutionVO;
import com.programming.entity.UserBehavior;

import java.util.Map;

public interface ReferenceSolutionService {

    /**
     * 检查用户是否有权限查看参考答案
     */
    PermissionCheckResult checkViewPermission(Long userId, Long problemId);

    /**
     * 获取参考答案
     */
    ReferenceSolutionVO getSolution(Long problemId, String language);

    /**
     * 记录用户查看行为
     */
    void recordViewBehavior(Long userId, Long problemId);

    /**
     * 获取渐进式提示
     */
    String getHint(Long problemId, Integer hintLevel);

    /**
     * 获取题目支持的语言列表
     */
    String[] getAvailableLanguages(Long problemId);

    /**
     * 权限检查结果类
     */
    class PermissionCheckResult {
        private boolean canView;
        private String message;

        public PermissionCheckResult(boolean canView, String message) {
            this.canView = canView;
            this.message = message;
        }

        public boolean isCanView() {
            return canView;
        }

        public String getMessage() {
            return message;
        }
    }
}