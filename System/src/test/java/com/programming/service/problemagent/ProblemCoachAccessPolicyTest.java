package com.programming.service.problemagent;

import com.programming.vo.problemagent.ProblemAgentChatRequestVO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProblemCoachAccessPolicyTest {

    private final ProblemCoachAccessPolicy policy = new ProblemCoachAccessPolicy();

    @Test
    void fullSolutionRequiresExplicitRequestAndPriorFailure() {
        ProblemAgentChatRequestVO request = new ProblemAgentChatRequestVO();
        request.setRequestFullSolution(true);

        assertFalse(policy.canRevealFullSolution(false, request));
    }

    @Test
    void fullSolutionAllowedAfterFailureWhenUserExplicitlyRequestsIt() {
        ProblemAgentChatRequestVO request = new ProblemAgentChatRequestVO();
        request.setRequestFullSolution(true);

        assertTrue(policy.canRevealFullSolution(true, request));
    }
}
