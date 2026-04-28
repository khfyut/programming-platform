package com.programming.agent;

import com.programming.agent.dto.AgentContextDTO;
import com.programming.agent.dto.AgentDecisionDTO;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Component
public class AgentDecisionEnforcer {
    private static final Map<String, Integer> ANSWER_SCOPE_RANK = Map.of(
            "concept_only", 1,
            "hint_only", 2,
            "partial_solution", 3,
            "full_solution", 4,
            "reference_code", 5
    );

    public AgentExecutionResult enforce(AgentContextDTO context, AgentDecisionDTO decision) {
        return enforce(context, decision, AgentPolicyProfile.PROBLEM_COACH);
    }

    public AgentExecutionResult enforce(AgentContextDTO context, AgentDecisionDTO decision, AgentPolicyProfile policyProfile) {
        if (decision == null) {
            return AgentExecutionResult.blocked("decision is null");
        }

        AgentPolicyProfile effectiveProfile = policyProfile == null ? AgentPolicyProfile.PROBLEM_COACH : policyProfile;
        AgentPolicyConfig policy = AgentPolicyConfig.forProfile(effectiveProfile);
        String actionType = decision.getActionType();
        if (actionType == null || actionType.isBlank()) {
            return AgentExecutionResult.blocked("action_type is required");
        }

        if (!AgentProtocolConstants.LEARNING_ACTIONS.contains(actionType)) {
            return AgentExecutionResult.blocked("unsupported action_type: " + actionType);
        }

        if (!policy.isActionAllowed(actionType)) {
            return AgentExecutionResult.blocked(actionType + " is not allowed for policy_profile " + effectiveProfile.name());
        }

        String expectedContentType = AgentProtocolConstants.CONTENT_TYPE_BY_ACTION.get(actionType);
        if (decision.getContentType() != null && !Objects.equals(expectedContentType, decision.getContentType())) {
            return AgentExecutionResult.blocked("content_type mismatch for " + actionType + ": expected " + expectedContentType);
        }

        AgentExecutionResult scopeResult = enforceAnswerScope(context, decision, effectiveProfile, policy);
        if (!scopeResult.isExecuted()) {
            return scopeResult;
        }

        return AgentExecutionResult.executed();
    }

    private AgentExecutionResult enforceAnswerScope(AgentContextDTO context,
                                                    AgentDecisionDTO decision,
                                                    AgentPolicyProfile profile,
                                                    AgentPolicyConfig policy) {
        String scene = effectiveScene(context, profile);
        String answerScope = normalizeScope(decision.getAnswerScope(), decision.getActionType());
        String requestedScope = normalizeScope(decision.getRequestedScope(), null);
        String evidenceLevel = effectiveEvidenceLevel(context);
        boolean explicitlyRequested = explicitFullAnswerRequest(context, requestedScope);

        if ("wrong_book".equals(scene)) {
            if ("reference_code".equals(answerScope) && !explicitlyRequested) {
                return AgentExecutionResult.blocked("reference_code requires explicit request in wrong_book scene");
            }
            return AgentExecutionResult.executed();
        }

        if (!"problem_coach".equals(scene)) {
            return enforceLegacyRevealRule(context, decision, policy);
        }

        String maxAllowedScope = switch (evidenceLevel) {
            case DecisionContextReducer.LEVEL_VERIFIED -> explicitlyRequested ? "reference_code" : "partial_solution";
            case DecisionContextReducer.LEVEL_STRONG -> "partial_solution";
            case DecisionContextReducer.LEVEL_WEAK, DecisionContextReducer.LEVEL_NONE -> "hint_only";
            default -> "hint_only";
        };

        if (rank(answerScope) > rank(maxAllowedScope)) {
            return AgentExecutionResult.blocked(
                    decision.getActionType() + " answer_scope " + answerScope + " exceeds allowed_scope " + maxAllowedScope
                            + " for scene problem_coach and failure_evidence_level " + evidenceLevel
            );
        }

        if (rank(answerScope) >= rank("full_solution") && !explicitlyRequested) {
            return AgentExecutionResult.blocked("full_solution requires explicit request");
        }
        return enforceLegacyRevealRule(context, decision, policy);
    }

    private AgentExecutionResult enforceLegacyRevealRule(AgentContextDTO context,
                                                         AgentDecisionDTO decision,
                                                         AgentPolicyConfig policy) {
        if (!AgentProtocolConstants.ACTION_REVEAL_ANSWER.equals(decision.getActionType())) {
            return AgentExecutionResult.executed();
        }
        boolean hasTrustedFailure = context != null
                && (context.getConsecutiveFailures() > 0
                || DecisionContextReducer.LEVEL_VERIFIED.equalsIgnoreCase(context.getFailureEvidenceLevel())
                || DecisionContextReducer.LEVEL_STRONG.equalsIgnoreCase(context.getFailureEvidenceLevel()));
        boolean explicitlyRequested = context != null && context.isRequestedFullSolution();
        if (policy.isRevealRequiresTrustedFailure() && !hasTrustedFailure) {
            return AgentExecutionResult.blocked("REVEAL_ANSWER requires trusted failure and explicit request");
        }
        if (policy.isRevealRequiresExplicitRequest() && !explicitlyRequested) {
            return AgentExecutionResult.blocked("REVEAL_ANSWER requires explicit request");
        }
        return AgentExecutionResult.executed();
    }

    private String effectiveScene(AgentContextDTO context, AgentPolicyProfile profile) {
        if (context != null && context.getScene() != null && !context.getScene().isBlank()) {
            return context.getScene().trim();
        }
        if (profile == AgentPolicyProfile.WRONG_BOOK_REFLECTION) {
            return "wrong_book";
        }
        return "problem_coach";
    }

    private String effectiveEvidenceLevel(AgentContextDTO context) {
        if (context != null && context.getFailureEvidenceLevel() != null && !context.getFailureEvidenceLevel().isBlank()) {
            return context.getFailureEvidenceLevel().trim().toUpperCase();
        }
        if (context != null && context.getConsecutiveFailures() > 0) {
            return DecisionContextReducer.LEVEL_VERIFIED;
        }
        return DecisionContextReducer.LEVEL_NONE;
    }

    private boolean explicitFullAnswerRequest(AgentContextDTO context, String requestedScope) {
        if (context != null && context.isRequestedFullSolution()) {
            return true;
        }
        if (context == null) {
            return false;
        }
        String actionHint = context.getActionHint() == null ? "" : context.getActionHint().toLowerCase();
        String message = context.getUserMessage() == null ? "" : context.getUserMessage().toLowerCase();
        boolean explicitSignal = actionHint.contains("full_solution")
                || actionHint.contains("reference_code")
                || message.contains("full solution")
                || message.contains("reference code")
                || message.contains("complete code")
                || message.contains("give me code")
                || message.contains("show code");
        return explicitSignal;
    }

    private String normalizeScope(String value, String actionType) {
        if (value != null && ANSWER_SCOPE_RANK.containsKey(value.trim())) {
            return value.trim();
        }
        if (AgentProtocolConstants.ACTION_REVEAL_ANSWER.equals(actionType)) {
            return "full_solution";
        }
        if (AgentProtocolConstants.ACTION_HINT.equals(actionType)
                || AgentProtocolConstants.ACTION_GUIDE_IDEA.equals(actionType)) {
            return "hint_only";
        }
        if (AgentProtocolConstants.ACTION_DIAGNOSE.equals(actionType)) {
            return "partial_solution";
        }
        return "concept_only";
    }

    private int rank(String scope) {
        return ANSWER_SCOPE_RANK.getOrDefault(scope, ANSWER_SCOPE_RANK.get("concept_only"));
    }
}
