package com.programming.agent;

import com.programming.agent.dto.AgentContextDTO;
import com.programming.agent.dto.AgentDecisionDTO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentClientFallbackTest {

    @Test
    void fallbackDecisionUsesChineseUserFacingCopy() throws Exception {
        AgentClient client = new AgentClient();
        Method method = AgentClient.class.getDeclaredMethod(
                "createFallbackDecision",
                AgentContextDTO.class,
                String.class,
                String.class
        );
        method.setAccessible(true);

        AgentContextDTO context = new AgentContextDTO();
        context.setRequestId("req-test");
        AgentDecisionDTO decision = (AgentDecisionDTO) method.invoke(client, context, "MODEL_TIMEOUT", "timeout");

        assertTrue(decision.getMainResponse().contains("AI 服务"));
        assertTrue(decision.getNextSuggestion().contains("重新发送"));
        assertFalse(decision.getMainResponse().contains("Agent service is temporarily unavailable"));
        assertFalse(decision.getNextSuggestion().contains("Try asking"));
    }
}
