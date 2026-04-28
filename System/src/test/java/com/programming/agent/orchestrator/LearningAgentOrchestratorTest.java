package com.programming.agent.orchestrator;

import com.programming.agent.AgentPolicyProfile;
import com.programming.agent.AgentProtocolConstants;
import com.programming.agent.dto.AgentContextDTO;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LearningAgentOrchestratorTest {

    @Test
    void preparesWrongBookEventWithPolicyToolsPromptLayersAndLearningSummary() {
        LearningToolRegistry registry = new LearningToolRegistry(List.of(
                event -> ToolResult.success("wrong_book", Map.of("wrong_item_id", event.getWrongItemId()))
        ));
        LearningAgentOrchestrator orchestrator = new LearningAgentOrchestrator(
                registry,
                new PromptLayerBuilder(),
                new LearningMemoryService()
        );
        LearningAgentEvent event = new LearningAgentEvent();
        event.setEventType(AgentProtocolConstants.TRIGGER_WRONG_BOOK_ENTRY);
        event.setPolicyProfile(AgentPolicyProfile.WRONG_BOOK_REFLECTION);
        event.setUserId(17L);
        event.setWrongItemId(88L);
        event.setProblemId(3L);
        event.setUserMessage("这题完整思路是什么");

        AgentContextDTO context = new AgentContextDTO();
        context.setTriggerSource(AgentProtocolConstants.TRIGGER_WRONG_BOOK_ENTRY);
        context.setUserIntent(AgentProtocolConstants.INTENT_ASK_FOR_ERROR_ANALYSIS);
        context.setConsecutiveFailures(1);
        context.setHintCount(2);
        context.setWeakPoints(List.of("boundary"));
        context.setLearningStage("EXPLAINED");

        LearningAgentRun run = orchestrator.prepare(event, context);

        assertSame(context, run.getContext());
        assertEquals(AgentPolicyProfile.WRONG_BOOK_REFLECTION, run.getPolicyProfile());
        assertEquals("WRONG_BOOK_REFLECTION", context.getPolicyProfile());
        assertEquals(1, context.getToolResults().size());
        assertEquals("wrong_book", context.getToolResults().get(0).get("tool_name"));
        assertTrue(context.getPromptLayers().containsKey("role_layer"));
        assertTrue(context.getPromptLayers().containsKey("user_profile_layer"));
        assertTrue(context.getPromptLayers().containsKey("entry_context_layer"));
        assertTrue(context.getPromptLayers().containsKey("runtime_state_layer"));
        assertTrue(context.getPromptLayers().containsKey("output_policy_layer"));
        assertEquals("OPEN_REFLECTION", ((Map<?, ?>) context.getPromptLayers().get("output_policy_layer")).get("content_freedom"));
        assertEquals(List.of("boundary"), context.getLearningSummary().get("weak_points"));
        assertTrue(context.getCandidateActions().contains(AgentProtocolConstants.ACTION_REVEAL_ANSWER));
    }

    @Test
    void globalCoachEventDoesNotExposeDeepAnswerActions() {
        LearningAgentOrchestrator orchestrator = new LearningAgentOrchestrator(
                new LearningToolRegistry(List.of()),
                new PromptLayerBuilder(),
                new LearningMemoryService()
        );
        LearningAgentEvent event = new LearningAgentEvent();
        event.setEventType("COACH_NUDGE");
        event.setPolicyProfile(AgentPolicyProfile.GLOBAL_COACH);
        event.setUserId(17L);

        AgentContextDTO context = new AgentContextDTO();

        orchestrator.prepare(event, context);

        assertEquals("GLOBAL_COACH", context.getPolicyProfile());
        assertFalse(context.getCandidateActions().contains(AgentProtocolConstants.ACTION_REVEAL_ANSWER));
        assertEquals("LIGHT_NUDGE", ((Map<?, ?>) context.getPromptLayers().get("output_policy_layer")).get("content_freedom"));
    }
}
