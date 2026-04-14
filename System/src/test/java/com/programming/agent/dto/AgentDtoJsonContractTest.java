package com.programming.agent.dto;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentDtoJsonContractTest {

    @Test
    void serializesAgentContextWithPythonSnakeCaseFieldNames() {
        AgentContextDTO context = new AgentContextDTO();
        context.setRequestId("req-test");
        context.setTimestamp(LocalDateTime.of(2026, 4, 13, 13, 3, 38));
        context.setConsecutiveFailures(2);
        context.setHasViewedSolution(false);
        context.setTriggerSource("SUBMISSION_RESULT");
        context.setUserIntent("ASK_FOR_ERROR_ANALYSIS");
        context.setUserMessage("why is this wrong?");
        context.setRequestedFullSolution(false);
        context.setHintCount(1);
        context.setDiagnoseCount(0);
        context.setExplainCount(0);
        context.setRecommendCount(0);
        context.setReflectCount(0);
        context.setLearningStage("HINTED");

        ProblemContextDTO problem = new ProblemContextDTO();
        problem.setProblemId(1);
        problem.setTitle("two sum");
        problem.setDifficulty("easy");
        problem.setKnowledgePoints(List.of("array", "hashmap"));
        problem.setProblemContent("given an array, return two indices");
        problem.setHints("use a hashmap");
        problem.setSampleExplanation("nums[0] + nums[1] = target");
        problem.setTags("array,hashmap");
        context.setProblem(problem);

        SubmissionContextDTO submission = new SubmissionContextDTO();
        submission.setSubmitId(100);
        submission.setStatus("WA");
        submission.setErrorMessage("array index out of bounds");
        submission.setFirstAttempt(false);
        context.setSubmission(submission);

        String json = JSON.toJSONString(context);

        assertTrue(json.contains("\"request_id\""));
        assertTrue(json.contains("\"consecutive_failures\""));
        assertTrue(json.contains("\"has_viewed_solution\""));
        assertTrue(json.contains("\"trigger_source\""));
        assertTrue(json.contains("\"user_intent\""));
        assertTrue(json.contains("\"user_message\""));
        assertTrue(json.contains("\"requested_full_solution\""));
        assertTrue(json.contains("\"hint_count\""));
        assertTrue(json.contains("\"diagnose_count\""));
        assertTrue(json.contains("\"explain_count\""));
        assertTrue(json.contains("\"recommend_count\""));
        assertTrue(json.contains("\"reflect_count\""));
        assertTrue(json.contains("\"learning_stage\""));
        assertTrue(json.contains("\"problem_id\""));
        assertTrue(json.contains("\"knowledge_points\""));
        assertTrue(json.contains("\"problem_content\""));
        assertTrue(json.contains("\"sample_explanation\""));
        assertTrue(json.contains("\"submit_id\""));
        assertTrue(json.contains("\"error_message\""));
        assertTrue(json.contains("\"is_first_attempt\""));
        assertFalse(json.contains("\"requestId\""));
        assertFalse(json.contains("\"consecutiveFailures\""));
        assertFalse(json.contains("\"triggerSource\""));
        assertFalse(json.contains("\"problemId\""));
    }

    @Test
    void parsesAgentDecisionFromPythonSnakeCaseFieldNames() {
        String response = """
                {
                  "response_id": "req-test",
                  "timestamp": "2026-04-13T13:03:38.209386",
                  "decision_summary": "strategy: ERROR_DIAGNOSIS",
                  "selected_strategy": "ERROR_DIAGNOSIS",
                  "decision_reason": "two consecutive failures",
                  "applied_constraints": ["no full solution on first failure"],
                  "blocked_actions": [],
                  "candidate_actions": [
                    {
                      "action_id": "diagnose_001",
                      "action_type": "DIAGNOSE",
                      "title": "diagnose",
                      "description": "analyze the error pattern",
                      "priority": "high",
                      "content": "check array boundaries",
                      "required_conditions": []
                    }
                  ],
                  "recommended_action_id": "diagnose_001",
                  "action_type": "DIAGNOSE",
                  "pedagogical_goal": "DIAGNOSE_ERROR_CAUSE",
                  "main_response": "check array boundaries",
                  "next_suggestion": "locate the first failing case",
                  "error_tag": "LOGIC_ERROR",
                  "weak_points": ["array", "hashmap"],
                  "confidence": 0.8,
                  "used_knowledge_refs": ["problem:1"],
                  "executed": true,
                  "blocked_reason": null,
                  "content": "check array boundaries",
                  "content_type": "diagnosis",
                  "proposed_updates": []
                }
                """;

        AgentDecisionDTO decision = JSON.parseObject(response, AgentDecisionDTO.class);

        assertEquals("req-test", decision.getResponseId());
        assertEquals("strategy: ERROR_DIAGNOSIS", decision.getDecisionSummary());
        assertEquals("ERROR_DIAGNOSIS", decision.getSelectedStrategy());
        assertEquals("two consecutive failures", decision.getDecisionReason());
        assertEquals(List.of("no full solution on first failure"), decision.getAppliedConstraints());
        assertEquals("diagnose_001", decision.getRecommendedActionId());
        assertEquals("DIAGNOSE", decision.getActionType());
        assertEquals("DIAGNOSE_ERROR_CAUSE", decision.getPedagogicalGoal());
        assertEquals("check array boundaries", decision.getMainResponse());
        assertEquals("locate the first failing case", decision.getNextSuggestion());
        assertEquals("LOGIC_ERROR", decision.getErrorTag());
        assertEquals(List.of("array", "hashmap"), decision.getWeakPoints());
        assertEquals(0.8, decision.getConfidence(), 0.0001);
        assertEquals(List.of("problem:1"), decision.getUsedKnowledgeRefs());
        assertEquals(true, decision.getExecuted());
        assertEquals("diagnosis", decision.getContentType());
        assertEquals(1, decision.getCandidateActions().size());
        assertEquals("diagnose_001", decision.getCandidateActions().get(0).getActionId());
        assertEquals("DIAGNOSE", decision.getCandidateActions().get(0).getActionType());
    }

    @Test
    void serializesAgentFeedbackScopeWithSnakeCaseFieldNames() throws Exception {
        AgentFeedbackRequestDTO request = new AgentFeedbackRequestDTO();
        request.setRequestId("req-path");
        request.setEntryRefType("LEARNING_PATH_LEVEL");
        request.setEntryRefId(5L);
        request.setPathId(4L);
        request.setLevelId(5L);
        request.setActionType("RECOMMEND");
        request.setFeedbackType("accepted");
        request.setMetadata(Map.of("source", "test"));

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(request);

        assertTrue(json.contains("\"request_id\""));
        assertTrue(json.contains("\"entry_ref_type\""));
        assertTrue(json.contains("\"entry_ref_id\""));
        assertTrue(json.contains("\"path_id\""));
        assertTrue(json.contains("\"level_id\""));
        assertFalse(json.contains("\"entryRefType\""));

        AgentFeedbackRequestDTO parsed = mapper.readValue(json, AgentFeedbackRequestDTO.class);
        assertEquals("req-path", parsed.getRequestId());
        assertEquals("LEARNING_PATH_LEVEL", parsed.getEntryRefType());
        assertEquals(5L, parsed.getEntryRefId());
        assertEquals(4L, parsed.getPathId());
        assertEquals(5L, parsed.getLevelId());
    }
}
