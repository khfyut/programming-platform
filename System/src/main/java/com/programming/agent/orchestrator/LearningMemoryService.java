package com.programming.agent.orchestrator;

import com.programming.agent.dto.AgentContextDTO;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class LearningMemoryService {

    public Map<String, Object> summarize(LearningAgentEvent event, AgentContextDTO context) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("event_type", event == null ? null : event.getEventType());
        summary.put("problem_id", event == null ? null : event.getProblemId());
        summary.put("submit_id", event == null ? null : event.getSubmitId());
        summary.put("wrong_item_id", event == null ? null : event.getWrongItemId());
        summary.put("learning_stage", context == null ? null : context.getLearningStage());
        summary.put("consecutive_failures", context == null ? 0 : context.getConsecutiveFailures());
        summary.put("hint_count", context == null ? 0 : context.getHintCount());
        summary.put("diagnose_count", context == null ? 0 : context.getDiagnoseCount());
        summary.put("explain_count", context == null ? 0 : context.getExplainCount());
        summary.put("reflect_count", context == null ? 0 : context.getReflectCount());
        summary.put("last_action_type", context == null ? null : context.getLastActionType());
        summary.put("last_error_tag", context == null ? null : context.getLastErrorTag());
        summary.put("weak_points", context == null ? null : context.getWeakPoints());
        summary.put("has_viewed_solution", context != null && context.isHasViewedSolution());
        summary.put("near_answer_leak_boundary", isNearAnswerLeakBoundary(context));
        return summary;
    }

    private boolean isNearAnswerLeakBoundary(AgentContextDTO context) {
        if (context == null) {
            return false;
        }
        return context.isRequestedFullSolution()
                || context.isHasViewedSolution()
                || context.getHintCount() >= 2
                || context.getConsecutiveFailures() >= 3;
    }
}
