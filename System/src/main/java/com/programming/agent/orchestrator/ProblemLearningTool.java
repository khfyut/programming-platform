package com.programming.agent.orchestrator;

import com.programming.entity.Problem;
import com.programming.mapper.ProblemMapper;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ProblemLearningTool implements LearningTool {
    private final ProblemMapper problemMapper;

    public ProblemLearningTool(ProblemMapper problemMapper) {
        this.problemMapper = problemMapper;
    }

    @Override
    public boolean supports(LearningAgentEvent event) {
        return event != null && event.getProblemId() != null && event.getProblemId() > 0;
    }

    @Override
    public ToolResult execute(LearningAgentEvent event) {
        if (!supports(event)) {
            return ToolResult.failure("problem", "problem_id is required");
        }
        Problem problem = problemMapper.findById(event.getProblemId());
        if (problem == null) {
            return ToolResult.failure("problem", "problem not found");
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("problem_id", problem.getId());
        data.put("title", problem.getTitle());
        data.put("difficulty", problem.getDifficulty());
        data.put("language", problem.getLanguage());
        data.put("tags", problem.getTags());
        data.put("knowledge_points", problem.getKnowledgePoints());
        data.put("content", problem.getContent());
        data.put("hints", problem.getHints());
        data.put("sample_explanation", problem.getSampleExplanation());
        return ToolResult.success("problem", data);
    }

    @Override
    public String name() {
        return "problem";
    }
}
