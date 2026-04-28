package com.programming.agent.orchestrator;

import com.programming.agent.AgentPolicyConfig;
import com.programming.agent.AgentPolicyProfile;
import com.programming.agent.dto.AgentContextDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class PromptLayerBuilder {

    public Map<String, Object> build(LearningAgentEvent event,
                                     AgentContextDTO context,
                                     AgentPolicyProfile profile,
                                     List<Map<String, Object>> toolResults,
                                     Map<String, Object> learningSummary) {
        AgentPolicyConfig policy = AgentPolicyConfig.forProfile(profile);
        Map<String, Object> layers = new LinkedHashMap<>();
        layers.put("role_layer", roleLayer());
        layers.put("user_profile_layer", userProfileLayer(learningSummary));
        layers.put("entry_context_layer", entryContextLayer(event, context));
        layers.put("runtime_state_layer", runtimeStateLayer(context, toolResults, learningSummary));
        layers.put("output_policy_layer", outputPolicyLayer(policy, profile));
        return layers;
    }

    private Map<String, Object> roleLayer() {
        Map<String, Object> layer = new LinkedHashMap<>();
        layer.put("role", "programming_learning_coach");
        layer.put("control_principle", "system_controls_boundaries_model_generates_content");
        layer.put("default_language", "zh-CN");
        return layer;
    }

    private Map<String, Object> userProfileLayer(Map<String, Object> learningSummary) {
        Map<String, Object> layer = new LinkedHashMap<>();
        layer.put("preferred_language", "Java");
        layer.put("weak_points", learningSummary == null ? List.of() : learningSummary.get("weak_points"));
        layer.put("help_style", "hint_before_answer_unless_reflection_profile");
        return layer;
    }

    private Map<String, Object> entryContextLayer(LearningAgentEvent event, AgentContextDTO context) {
        Map<String, Object> layer = new LinkedHashMap<>();
        layer.put("event_type", event == null ? null : event.getEventType());
        layer.put("problem_id", event == null ? null : event.getProblemId());
        layer.put("submit_id", event == null ? null : event.getSubmitId());
        layer.put("wrong_item_id", event == null ? null : event.getWrongItemId());
        layer.put("path_id", event == null ? null : event.getPathId());
        layer.put("level_id", event == null ? null : event.getLevelId());
        layer.put("trigger_source", context == null ? null : context.getTriggerSource());
        layer.put("user_intent", context == null ? null : context.getUserIntent());
        layer.put("user_message", event == null ? null : event.getUserMessage());
        return layer;
    }

    private Map<String, Object> runtimeStateLayer(AgentContextDTO context,
                                                  List<Map<String, Object>> toolResults,
                                                  Map<String, Object> learningSummary) {
        Map<String, Object> layer = new LinkedHashMap<>();
        layer.put("consecutive_failures", context == null ? 0 : context.getConsecutiveFailures());
        layer.put("hint_count", context == null ? 0 : context.getHintCount());
        layer.put("diagnose_count", context == null ? 0 : context.getDiagnoseCount());
        layer.put("learning_stage", context == null ? null : context.getLearningStage());
        layer.put("submission", context == null ? null : context.getSubmission());
        layer.put("tool_results", toolResults == null ? List.of() : toolResults);
        layer.put("learning_summary", learningSummary == null ? Map.of() : learningSummary);
        return layer;
    }

    private Map<String, Object> outputPolicyLayer(AgentPolicyConfig policy, AgentPolicyProfile profile) {
        Map<String, Object> layer = new LinkedHashMap<>();
        layer.put("policy_profile", profile == null ? AgentPolicyProfile.PROBLEM_COACH.name() : profile.name());
        layer.put("content_freedom", policy.getContentFreedom());
        layer.put("allowed_actions", new ArrayList<>(policy.getAllowedActions()));
        layer.put("reveal_requires_trusted_failure", policy.isRevealRequiresTrustedFailure());
        layer.put("reveal_requires_explicit_request", policy.isRevealRequiresExplicitRequest());
        layer.put("force_hint_ladder", policy.isForceHintLadder());
        layer.put("allow_similar_recommendations", policy.isAllowSimilarRecommendations());
        layer.put("allow_divergent_discussion", policy.isAllowDivergentDiscussion());
        return layer;
    }
}
