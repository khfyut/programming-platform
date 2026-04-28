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
    void wrongBookReflectionAllowsExplicitRevealAnswerWithoutSubmissionFailure() {
        AgentContextDTO context = new AgentContextDTO();
        context.setConsecutiveFailures(0);
        context.setTriggerSource("WRONG_BOOK_ENTRY");
        context.setRequestedFullSolution(true);

        AgentDecisionDTO decision = new AgentDecisionDTO();
        decision.setActionType("REVEAL_ANSWER");
        decision.setContentType("solution");

        AgentExecutionResult result = enforcer.enforce(context, decision, AgentPolicyProfile.WRONG_BOOK_REFLECTION);

        assertTrue(result.isExecuted());
    }

    @Test
    void globalCoachBlocksDeepAnswerActions() {
        AgentContextDTO context = new AgentContextDTO();
        context.setRequestedFullSolution(true);

        AgentDecisionDTO decision = new AgentDecisionDTO();
        decision.setActionType("REVEAL_ANSWER");
        decision.setContentType("solution");

        AgentExecutionResult result = enforcer.enforce(context, decision, AgentPolicyProfile.GLOBAL_COACH);

        assertFalse(result.isExecuted());
        assertTrue(result.getBlockedReason().contains("not allowed"));
    }

    @Test
    void globalGuideAllowsOnlyLightGuideActions() {
        AgentContextDTO context = new AgentContextDTO();
        context.setScene("global_guide");
        context.setRequestedFullSolution(false);

        AgentDecisionDTO explain = new AgentDecisionDTO();
        explain.setActionType("EXPLAIN");
        explain.setContentType("explanation");
        explain.setAnswerScope("concept_only");

        AgentDecisionDTO recommend = new AgentDecisionDTO();
        recommend.setActionType("RECOMMEND");
        recommend.setContentType("recommendation");
        recommend.setAnswerScope("concept_only");

        AgentDecisionDTO reveal = new AgentDecisionDTO();
        reveal.setActionType("REVEAL_ANSWER");
        reveal.setContentType("solution");
        reveal.setAnswerScope("full_solution");

        assertTrue(enforcer.enforce(context, explain, AgentPolicyProfile.GLOBAL_GUIDE).isExecuted());
        assertTrue(enforcer.enforce(context, recommend, AgentPolicyProfile.GLOBAL_GUIDE).isExecuted());
        assertFalse(enforcer.enforce(context, reveal, AgentPolicyProfile.GLOBAL_GUIDE).isExecuted());
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

    @Test
    void allowsClarifyIntentWithoutLearningStateRequirements() {
        AgentContextDTO context = new AgentContextDTO();
        context.setScene("problem_coach");
        context.setConsecutiveFailures(0);

        AgentDecisionDTO decision = new AgentDecisionDTO();
        decision.setActionType("CLARIFY_INTENT");
        decision.setContentType("clarification");
        decision.setAnswerScope("concept_only");

        AgentExecutionResult result = enforcer.enforce(context, decision);

        assertTrue(result.isExecuted());
    }

    @Test
    void problemCoachWithoutTrustedFailureAllowsOnlyHintScope() {
        AgentContextDTO context = new AgentContextDTO();
        context.setScene("problem_coach");
        context.setFailureEvidenceLevel("WEAK");
        context.setRequestedFullSolution(true);

        AgentDecisionDTO decision = new AgentDecisionDTO();
        decision.setActionType("EXPLAIN");
        decision.setContentType("explanation");
        decision.setRequestedScope("full_solution");
        decision.setAnswerScope("partial_solution");

        AgentExecutionResult result = enforcer.enforce(context, decision, AgentPolicyProfile.PROBLEM_COACH);

        assertFalse(result.isExecuted());
        assertTrue(result.getBlockedReason().contains("answer_scope"));
    }

    @Test
    void problemCoachWithStrongFailureAllowsPartialButNotFullSolution() {
        AgentContextDTO context = new AgentContextDTO();
        context.setScene("problem_coach");
        context.setFailureEvidenceLevel("STRONG");
        context.setRequestedFullSolution(true);

        AgentDecisionDTO partial = new AgentDecisionDTO();
        partial.setActionType("EXPLAIN");
        partial.setContentType("explanation");
        partial.setAnswerScope("partial_solution");

        AgentDecisionDTO full = new AgentDecisionDTO();
        full.setActionType("REVEAL_ANSWER");
        full.setContentType("solution");
        full.setRequestedScope("full_solution");
        full.setAnswerScope("full_solution");

        assertTrue(enforcer.enforce(context, partial, AgentPolicyProfile.PROBLEM_COACH).isExecuted());
        assertFalse(enforcer.enforce(context, full, AgentPolicyProfile.PROBLEM_COACH).isExecuted());
    }

    @Test
    void problemCoachAllowsFullSolutionOnlyForVerifiedExplicitRequest() {
        AgentContextDTO context = new AgentContextDTO();
        context.setScene("problem_coach");
        context.setFailureEvidenceLevel("VERIFIED");
        context.setRequestedFullSolution(true);

        AgentDecisionDTO decision = new AgentDecisionDTO();
        decision.setActionType("REVEAL_ANSWER");
        decision.setContentType("solution");
        decision.setRequestedScope("full_solution");
        decision.setAnswerScope("full_solution");

        AgentExecutionResult result = enforcer.enforce(context, decision, AgentPolicyProfile.PROBLEM_COACH);

        assertTrue(result.isExecuted());
    }

    @Test
    void wrongBookRequiresExplicitRequestForReferenceCodeScope() {
        AgentContextDTO context = new AgentContextDTO();
        context.setScene("wrong_book");
        context.setFailureEvidenceLevel("VERIFIED");
        context.setRequestedFullSolution(false);

        AgentDecisionDTO decision = new AgentDecisionDTO();
        decision.setActionType("REVEAL_ANSWER");
        decision.setContentType("solution");
        decision.setRequestedScope("hint_only");
        decision.setAnswerScope("reference_code");

        AgentExecutionResult blocked = enforcer.enforce(context, decision, AgentPolicyProfile.WRONG_BOOK_REFLECTION);
        context.setRequestedFullSolution(true);
        decision.setRequestedScope("reference_code");
        AgentExecutionResult allowed = enforcer.enforce(context, decision, AgentPolicyProfile.WRONG_BOOK_REFLECTION);

        assertFalse(blocked.isExecuted());
        assertTrue(blocked.getBlockedReason().contains("reference_code"));
        assertTrue(allowed.isExecuted());
    }

    @Test
    void llmRequestedScopeAloneDoesNotAuthorizeWrongBookReferenceCode() {
        AgentContextDTO context = new AgentContextDTO();
        context.setScene("wrong_book");
        context.setFailureEvidenceLevel("VERIFIED");
        context.setUserMessage("please help me review the mistake");

        AgentDecisionDTO decision = new AgentDecisionDTO();
        decision.setActionType("REVEAL_ANSWER");
        decision.setContentType("solution");
        decision.setRequestedScope("reference_code");
        decision.setAnswerScope("reference_code");

        AgentExecutionResult result = enforcer.enforce(context, decision, AgentPolicyProfile.WRONG_BOOK_REFLECTION);

        assertFalse(result.isExecuted());
        assertTrue(result.getBlockedReason().contains("reference_code"));
    }
}
