package com.programming.service;

import com.programming.vo.ReferenceSolutionVO;

public interface ReferenceSolutionService {

    PermissionCheckResult checkViewPermission(Long userId, Long problemId);

    ReferenceSolutionVO getSolution(Long problemId, String language);

    void recordViewBehavior(Long userId, Long problemId);

    String getHint(Long problemId, Integer hintLevel, String language);

    String[] getAvailableLanguages(Long problemId);

    class PermissionCheckResult {
        private final boolean canView;
        private final String message;

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
