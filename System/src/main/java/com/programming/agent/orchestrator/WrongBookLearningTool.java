package com.programming.agent.orchestrator;

import com.programming.entity.WrongBookItem;
import com.programming.service.WrongBookService;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class WrongBookLearningTool implements LearningTool {
    private final WrongBookService wrongBookService;

    public WrongBookLearningTool(WrongBookService wrongBookService) {
        this.wrongBookService = wrongBookService;
    }

    @Override
    public boolean supports(LearningAgentEvent event) {
        return event != null && event.getUserId() != null && event.getWrongItemId() != null;
    }

    @Override
    public ToolResult execute(LearningAgentEvent event) {
        if (!supports(event)) {
            return ToolResult.failure("wrong_book", "user_id and wrong_item_id are required");
        }
        WrongBookItem item = wrongBookService.getWrongBookItemById(event.getUserId(), event.getWrongItemId());
        if (item == null) {
            return ToolResult.failure("wrong_book", "wrong item not found");
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("wrong_item_id", item.getId());
        data.put("problem_id", item.getProblemId());
        data.put("submit_id", item.getSubmitId());
        data.put("language", item.getLanguage());
        data.put("error_message", item.getErrorMessage());
        data.put("knowledge_points", item.getKnowledgePoints());
        data.put("review_status", item.getReviewStatus());
        data.put("code", item.getCode());
        return ToolResult.success("wrong_book", data);
    }

    @Override
    public String name() {
        return "wrong_book";
    }
}
