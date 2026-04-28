package com.programming.agent.orchestrator;

import com.programming.entity.Problem;
import com.programming.service.WrongBookService;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class RecommendationLearningTool implements LearningTool {
    private static final int DEFAULT_LIMIT = 5;
    private final WrongBookService wrongBookService;

    public RecommendationLearningTool(WrongBookService wrongBookService) {
        this.wrongBookService = wrongBookService;
    }

    @Override
    public boolean supports(LearningAgentEvent event) {
        return event != null && event.getUserId() != null && event.getWrongItemId() != null;
    }

    @Override
    public ToolResult execute(LearningAgentEvent event) {
        if (!supports(event)) {
            return ToolResult.failure("recommendation", "user_id and wrong_item_id are required");
        }
        List<Problem> problems = wrongBookService.getRecommendedProblems(event.getUserId(), event.getWrongItemId(), DEFAULT_LIMIT);
        List<Map<String, Object>> recommendations = problems == null
                ? List.of()
                : problems.stream().filter(problem -> problem != null && problem.getId() != null).map(this::toMap).toList();
        return ToolResult.success("recommendation", Map.of("recommendations", recommendations));
    }

    private Map<String, Object> toMap(Problem problem) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("problem_id", problem.getId());
        data.put("title", problem.getTitle());
        data.put("difficulty", problem.getDifficulty());
        data.put("knowledge_points", problem.getKnowledgePoints());
        data.put("target_route", "/problem/" + problem.getId());
        return data;
    }

    @Override
    public String name() {
        return "recommendation";
    }
}
