package com.programming.agent;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public class AgentPolicyConfig {
    private static final Map<AgentPolicyProfile, AgentPolicyConfig> CONFIGS = new EnumMap<>(AgentPolicyProfile.class);

    static {
        CONFIGS.put(
                AgentPolicyProfile.PROBLEM_COACH,
                new AgentPolicyConfig(
                        AgentProtocolConstants.LEARNING_ACTIONS,
                        true,
                        true,
                        true,
                        false,
                        false,
                        "STRICT_TUTORING"
                )
        );
        CONFIGS.put(
                AgentPolicyProfile.WRONG_BOOK_REFLECTION,
                new AgentPolicyConfig(
                        AgentProtocolConstants.LEARNING_ACTIONS,
                        false,
                        true,
                        false,
                        true,
                        true,
                        "OPEN_REFLECTION"
                )
        );
        CONFIGS.put(
                AgentPolicyProfile.GLOBAL_COACH,
                new AgentPolicyConfig(
                        Set.of(
                                AgentProtocolConstants.ACTION_GUIDE_IDEA,
                                AgentProtocolConstants.ACTION_HINT,
                                AgentProtocolConstants.ACTION_RECOMMEND,
                                AgentProtocolConstants.ACTION_REFLECT,
                                AgentProtocolConstants.ACTION_CLARIFY_INTENT
                        ),
                        true,
                        true,
                        false,
                        false,
                        false,
                        "LIGHT_NUDGE"
                )
        );
        CONFIGS.put(
                AgentPolicyProfile.GLOBAL_GUIDE,
                new AgentPolicyConfig(
                        Set.of(
                                AgentProtocolConstants.ACTION_EXPLAIN,
                                AgentProtocolConstants.ACTION_RECOMMEND,
                                AgentProtocolConstants.ACTION_CLARIFY_INTENT
                        ),
                        true,
                        true,
                        false,
                        false,
                        false,
                        "GLOBAL_GUIDE"
                )
        );
        CONFIGS.put(
                AgentPolicyProfile.LEARNING_PATH,
                new AgentPolicyConfig(
                        Set.of(
                                AgentProtocolConstants.ACTION_GUIDE_IDEA,
                                AgentProtocolConstants.ACTION_RECOMMEND,
                                AgentProtocolConstants.ACTION_REFLECT,
                                AgentProtocolConstants.ACTION_CLARIFY_INTENT
                        ),
                        true,
                        true,
                        false,
                        true,
                        false,
                        "PLANNING"
                )
        );
    }

    private final Set<String> allowedActions;
    private final boolean revealRequiresTrustedFailure;
    private final boolean revealRequiresExplicitRequest;
    private final boolean forceHintLadder;
    private final boolean allowSimilarRecommendations;
    private final boolean allowDivergentDiscussion;
    private final String contentFreedom;

    private AgentPolicyConfig(Set<String> allowedActions,
                              boolean revealRequiresTrustedFailure,
                              boolean revealRequiresExplicitRequest,
                              boolean forceHintLadder,
                              boolean allowSimilarRecommendations,
                              boolean allowDivergentDiscussion,
                              String contentFreedom) {
        this.allowedActions = allowedActions;
        this.revealRequiresTrustedFailure = revealRequiresTrustedFailure;
        this.revealRequiresExplicitRequest = revealRequiresExplicitRequest;
        this.forceHintLadder = forceHintLadder;
        this.allowSimilarRecommendations = allowSimilarRecommendations;
        this.allowDivergentDiscussion = allowDivergentDiscussion;
        this.contentFreedom = contentFreedom;
    }

    public static AgentPolicyConfig forProfile(AgentPolicyProfile profile) {
        return CONFIGS.get(profile == null ? AgentPolicyProfile.PROBLEM_COACH : profile);
    }

    public boolean isActionAllowed(String actionType) {
        return allowedActions.contains(actionType);
    }

    public Set<String> getAllowedActions() {
        return allowedActions;
    }

    public boolean isRevealRequiresTrustedFailure() {
        return revealRequiresTrustedFailure;
    }

    public boolean isRevealRequiresExplicitRequest() {
        return revealRequiresExplicitRequest;
    }

    public boolean isForceHintLadder() {
        return forceHintLadder;
    }

    public boolean isAllowSimilarRecommendations() {
        return allowSimilarRecommendations;
    }

    public boolean isAllowDivergentDiscussion() {
        return allowDivergentDiscussion;
    }

    public String getContentFreedom() {
        return contentFreedom;
    }
}
