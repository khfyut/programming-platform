package com.programming.agent.orchestrator;

import com.programming.entity.Submit;
import com.programming.mapper.SubmitMapper;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class SubmissionLearningTool implements LearningTool {
    private final SubmitMapper submitMapper;

    public SubmissionLearningTool(SubmitMapper submitMapper) {
        this.submitMapper = submitMapper;
    }

    @Override
    public boolean supports(LearningAgentEvent event) {
        return event != null && event.getSubmitId() != null;
    }

    @Override
    public ToolResult execute(LearningAgentEvent event) {
        if (!supports(event)) {
            return ToolResult.failure("submission", "submit_id is required");
        }
        Submit submit = submitMapper.findById(event.getSubmitId());
        if (submit == null) {
            return ToolResult.failure("submission", "submission not found");
        }
        if (event.getUserId() != null && submit.getUserId() != null && !event.getUserId().equals(submit.getUserId())) {
            return ToolResult.failure("submission", "submission not owned by user");
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("submit_id", submit.getId());
        data.put("problem_id", submit.getProblemId());
        data.put("language", submit.getLanguage());
        data.put("result", submit.getResult());
        data.put("time_cost", submit.getTimeCost());
        data.put("memory_cost", submit.getMemoryCost());
        data.put("code", submit.getCode());
        if (submit.getProblemId() != null && event.getUserId() != null) {
            List<Submit> recent = submitMapper.findRecentByUserAndProblem(event.getUserId(), submit.getProblemId(), 5);
            data.put("recent_attempt_count", recent == null ? 0 : recent.size());
        }
        return ToolResult.success("submission", data);
    }

    @Override
    public String name() {
        return "submission";
    }
}
