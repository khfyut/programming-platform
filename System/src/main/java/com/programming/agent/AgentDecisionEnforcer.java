package com.programming.agent;

import com.programming.agent.dto.AgentContextDTO;
import com.programming.agent.dto.AgentDecisionDTO;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class AgentDecisionEnforcer {

    public AgentExecutionResult enforce(AgentContextDTO context, AgentDecisionDTO decision) {
        if (decision == null) {
            return AgentExecutionResult.blocked("decision is null");
        }

        String actionType = decision.getActionType();
        if (actionType == null || actionType.isBlank()) {
            return AgentExecutionResult.blocked("action_type is required");
        }

        if (!AgentProtocolConstants.LEARNING_ACTIONS.contains(actionType)) {
            return AgentExecutionResult.blocked("unsupported action_type: " + actionType);
        }

        String expectedContentType = AgentProtocolConstants.CONTENT_TYPE_BY_ACTION.get(actionType);
        if (decision.getContentType() != null && !Objects.equals(expectedContentType, decision.getContentType())) {
            return AgentExecutionResult.blocked("content_type mismatch for " + actionType + ": expected " + expectedContentType);
        }

        if (AgentProtocolConstants.ACTION_REVEAL_ANSWER.equals(actionType)) {
            boolean hasTrustedFailure = context != null && context.getConsecutiveFailures() > 0;
            boolean explicitlyRequested = context != null && context.isRequestedFullSolution();
            if (!hasTrustedFailure || !explicitlyRequested) {
                return AgentExecutionResult.blocked("REVEAL_ANSWER requires trusted failure and explicit request");
            }
        }

        return AgentExecutionResult.executed();
    }
}
