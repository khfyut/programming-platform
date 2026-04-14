package com.programming.agent;

import com.programming.agent.dto.AgentContextDTO;
import com.programming.agent.dto.AgentDecisionDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentDecisionEnforcerTest {

    private final AgentDecisionEnforcer enforcer = new AgentDecisionEnforcer();

    @Test
    void blocksRevealAnswerWhenThereIsNoTrustedFailure() {
        AgentContextDTO context = new AgentContextDTO();
        context.setConsecutiveFailures(0);
        context.setHasViewedSolution(false);
        context.setRequestedFullSolution(true);

        AgentDecisionDTO decision = new AgentDecisionDTO();
        decision.setActionType("REVEAL_ANSWER");
        decision.setContentType("solution");

        AgentExecutionResult result = enforcer.enforce(context, decision);

        assertFalse(result.isExecuted());
        assertTrue(result.getBlockedReason().contains("REVEAL_ANSWER"));
    }

    @Test
    void blocksMismatchedActionAndContentType() {
        AgentContextDTO context = new AgentContextDTO();
        context.setConsecutiveFailures(2);

        AgentDecisionDTO decision = new AgentDecisionDTO();
        decision.setActionType("DIAGNOSE");
        decision.setContentType("recommendation");

        AgentExecutionResult result = enforcer.enforce(context, decision);

        assertFalse(result.isExecuted());
        assertTrue(result.getBlockedReason().contains("content_type"));
    }

    @Test
    void allowsGuidanceBeforeSubmission() {
        AgentContextDTO context = new AgentContextDTO();
        context.setConsecutiveFailures(0);
        context.setTriggerSource("PROBLEM_PAGE_CHAT");
        context.setUserIntent("ASK_PROBLEM_SOLVING_IDEA");

        AgentDecisionDTO decision = new AgentDecisionDTO();
        decision.setActionType("GUIDE_IDEA");
        decision.setContentType("guidance");

        AgentExecutionResult result = enforcer.enforce(context, decision);

        assertTrue(result.isExecuted());
    }
}
