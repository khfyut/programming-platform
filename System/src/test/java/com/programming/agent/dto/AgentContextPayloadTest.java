package com.programming.agent.dto;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentContextPayloadTest {

    @Test
    void serializesHermesStyleAgentContextExtensionsForPythonService() {
        AgentContextDTO context = new AgentContextDTO();
        context.setRequestId("req-test");
        context.setPolicyProfile("WRONG_BOOK_REFLECTION");
        context.setCandidateActions(List.of("REFLECT", "REVEAL_ANSWER"));
        context.setToolResults(List.of(Map.of("tool_name", "wrong_book", "success", true)));
        context.setLearningSummary(Map.of("learning_stage", "EXPLAINED", "weak_points", List.of("boundary")));
        context.setPromptLayers(Map.of(
                "role_layer", Map.of("role", "learning_coach"),
                "output_policy_layer", Map.of("content_freedom", "OPEN_REFLECTION")
        ));

        String payload = JSON.toJSONString(context);

        assertTrue(payload.contains("\"policy_profile\":\"WRONG_BOOK_REFLECTION\""));
        assertTrue(payload.contains("\"candidate_actions\""));
        assertTrue(payload.contains("\"tool_results\""));
        assertTrue(payload.contains("\"learning_summary\""));
        assertTrue(payload.contains("\"prompt_layers\""));
        assertTrue(payload.contains("\"output_policy_layer\""));
    }
}
