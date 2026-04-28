package com.programming.agent.orchestrator;

import com.programming.entity.LearningEventLog;
import com.programming.entity.UserProblemInteraction;
import com.programming.mapper.LearningEventLogMapper;
import com.programming.mapper.UserProblemInteractionMapper;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class LearningStateTool implements LearningTool {
    private final UserProblemInteractionMapper interactionMapper;
    private final LearningEventLogMapper learningEventLogMapper;

    public LearningStateTool(UserProblemInteractionMapper interactionMapper,
                             LearningEventLogMapper learningEventLogMapper) {
        this.interactionMapper = interactionMapper;
        this.learningEventLogMapper = learningEventLogMapper;
    }

    @Override
    public boolean supports(LearningAgentEvent event) {
        return event != null && event.getUserId() != null;
    }

    @Override
    public ToolResult execute(LearningAgentEvent event) {
        if (!supports(event)) {
            return ToolResult.failure("learning_state", "user_id is required");
        }
        Map<String, Object> data = new LinkedHashMap<>();
        UserProblemInteraction interaction = event.getProblemId() == null
                ? null
                : interactionMapper.findByUserAndProblem(event.getUserId(), event.getProblemId());
        if (interaction != null) {
            data.put("learning_stage", interaction.getLearningStage());
            data.put("hint_count", value(interaction.getHintCount()));
            data.put("diagnose_count", value(interaction.getDiagnoseCount()));
            data.put("explain_count", value(interaction.getExplainCount()));
            data.put("reflect_count", value(interaction.getReflectCount()));
            data.put("consecutive_failures", value(interaction.getConsecutiveFailures()));
            data.put("last_action_type", interaction.getLastActionType());
            data.put("last_error_tag", interaction.getErrorTag());
            data.put("weak_points", interaction.getWeakPoints());
            data.put("has_viewed_answer", Boolean.TRUE.equals(interaction.getHasViewedAnswer()));
        }
        List<LearningEventLog> recentEvents = learningEventLogMapper.findRecentByUser(event.getUserId(), 5);
        data.put("recent_actions", recentEvents == null
                ? List.of()
                : recentEvents.stream().map(LearningEventLog::getActionType).filter(action -> action != null && !action.isBlank()).toList());
        return ToolResult.success("learning_state", data);
    }

    private int value(Integer value) {
        return value == null ? 0 : value;
    }

    @Override
    public String name() {
        return "learning_state";
    }
}
