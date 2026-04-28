package com.programming.agent;

import com.alibaba.fastjson2.JSON;
import com.programming.agent.dto.AgentContextDTO;
import com.programming.agent.dto.ProblemContextDTO;
import com.programming.agent.dto.SubmissionContextDTO;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DecisionContextReducerTest {

    private final DecisionContextReducer reducer = new DecisionContextReducer();

    @Test
    void ordinaryHintContextDropsCodeAndStaysWithinBudget() {
        AgentContextDTO context = baseContext("problem_coach", "hint", "give me a direction");
        context.setTriggerSource(AgentProtocolConstants.TRIGGER_PROBLEM_PAGE_CHAT);
        context.setSubmission(submission("NONE", "", repeated("class Solution {}\n", 400)));

        reducer.reduce(context);

        Map<String, Object> reduced = context.getReducedContext();
        assertNotNull(reduced);
        assertEquals("WEAK", context.getFailureEvidenceLevel());
        assertTrue(context.getFailureSignals().containsKey("weak_attempt_signals"));
        assertTrue(((List<?>) context.getFailureSignals().get("weak_attempt_signals")).contains("USER_CODE_PRESENT"));
        assertTrue(((List<?>) reduced.get("dropped_signals")).contains("CODE_OMITTED_NOT_DEBUG_REQUEST"));
        assertTrue(JSON.toJSONString(reduced).length() <= 2500);
    }

    @Test
    void debugContextKeepsCodeAndProducesStrongFailureEvidence() {
        AgentContextDTO context = baseContext("problem_coach", "diagnose", "why does this runtime error happen?");
        context.setTriggerSource(AgentProtocolConstants.TRIGGER_RUN_RESULT);
        context.setSubmission(submission("RE", "NullPointerException at line 12", "class Solution { void solve() {} }"));

        reducer.reduce(context);

        Map<String, Object> reduced = context.getReducedContext();
        Map<?, ?> conditional = (Map<?, ?>) reduced.get("conditional_context");

        assertEquals("STRONG", context.getFailureEvidenceLevel());
        assertTrue(((List<?>) context.getFailureSignals().get("strong_evidence_signals")).contains("RUN_FAILED_WITH_ERROR"));
        assertTrue(String.valueOf(conditional.get("code_excerpt")).contains("class Solution"));
        assertTrue(((List<?>) reduced.get("selected_signals")).contains("RUN_FAILED_WITH_ERROR"));
    }

    @Test
    void wrongBookContextIsVerifiedAndCarriesWrongBookSignal() {
        AgentContextDTO context = baseContext("wrong_book", "reflection_task_submit", "my summary");
        context.setTriggerSource(AgentProtocolConstants.TRIGGER_WRONG_BOOK_ENTRY);
        context.setSubmission(submission("WA", "Wrong answer", "class Solution {}"));

        reducer.reduce(context);

        assertEquals("VERIFIED", context.getFailureEvidenceLevel());
        assertTrue(((List<?>) context.getFailureSignals().get("context_signals")).contains("WRONG_BOOK_RECORD"));
        assertTrue(((List<?>) context.getReducedContext().get("selected_signals")).contains("WRONG_BOOK_RECORD"));
    }

    private AgentContextDTO baseContext(String scene, String actionHint, String message) {
        AgentContextDTO context = new AgentContextDTO();
        context.setRequestId("req-test");
        context.setScene(scene);
        context.setActionHint(actionHint);
        context.setUserMessage(message);
        context.setUserIntent(AgentProtocolConstants.INTENT_GENERAL_CHAT);
        context.setProblem(problem());
        return context;
    }

    private ProblemContextDTO problem() {
        ProblemContextDTO problem = new ProblemContextDTO();
        problem.setProblemId(1);
        problem.setTitle("Two Sum");
        problem.setDifficulty("easy");
        problem.setKnowledgePoints(List.of("array", "hashmap"));
        problem.setProblemContent(repeated("Find two indices that add to target. ", 120));
        return problem;
    }

    private SubmissionContextDTO submission(String status, String errorMessage, String code) {
        SubmissionContextDTO submission = new SubmissionContextDTO();
        submission.setSubmitId(10);
        submission.setStatus(status);
        submission.setErrorMessage(errorMessage);
        submission.setCodeContent(code);
        return submission;
    }

    private String repeated(String value, int times) {
        return value.repeat(times);
    }
}
