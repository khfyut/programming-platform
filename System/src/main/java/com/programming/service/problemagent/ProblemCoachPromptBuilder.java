package com.programming.service.problemagent;

import com.programming.entity.Problem;
import com.programming.entity.Submit;
import com.programming.entity.WrongBookItem;
import com.programming.vo.problemagent.ProblemAgentChatRequestVO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ProblemCoachPromptBuilder {

    public String buildSystemPrompt(ProblemCoachContext context, ProblemAgentChatRequestVO request, boolean revealFullSolution) {
        StringBuilder builder = new StringBuilder();
        builder.append("You are a programming problem coach inside an online judge.\n");
        builder.append("Help the learner improve with concrete diagnosis and next steps.\n");
        builder.append("Rules:\n");
        builder.append("- Default mode: diagnose mistakes, give hints, point out weak concepts, and suggest next practice.\n");
        if (revealFullSolution) {
            builder.append("- The learner has already failed at least once and explicitly requested a full corrected reference solution.\n");
            builder.append("- Return one complete corrected solution in a fenced code block first, then a concise explanation.\n");
        } else {
            builder.append("- Do not provide a full finished solution or a full replacement code block.\n");
            builder.append("- Small local snippets are allowed if they directly explain the fix.\n");
        }
        builder.append("- Keep the response practical and concise.\n\n");

        Problem problem = context.getCurrentProblem();
        if (problem != null) {
            builder.append("Problem title: ").append(safe(problem.getTitle())).append("\n");
            builder.append("Difficulty: ").append(problem.getDifficulty() == null ? "unknown" : problem.getDifficulty()).append("\n");
            builder.append("Knowledge points: ").append(safe(problem.getKnowledgePoints())).append("\n");
            builder.append("Tags: ").append(safe(problem.getTags())).append("\n");
            builder.append("Problem statement:\n").append(truncate(problem.getContent(), 2200)).append("\n\n");
        }

        builder.append("Current state:\n");
        builder.append("Language: ").append(safe(context.getLanguage())).append("\n");
        builder.append("Trigger: ").append(safe(context.getTriggerType())).append("\n");
        builder.append("Latest result code: ").append(context.getLatestResultCode() == null ? "unknown" : context.getLatestResultCode()).append("\n");
        builder.append("Latest error: ").append(safe(context.getLatestErrorMessage())).append("\n");
        builder.append("Execution output: ").append(safe(context.getExecutionOutput())).append("\n");

        Submit latestSubmit = context.getLatestSubmit();
        if (latestSubmit != null) {
            builder.append("Latest submit id: ").append(latestSubmit.getId()).append("\n");
            builder.append("Latest submit result: ").append(latestSubmit.getResult()).append("\n");
        }

        builder.append("Weak knowledge points: ")
                .append(joinMapNames(context.getWeakKnowledgePoints(), "name", "knowledgeName"))
                .append("\n");
        builder.append("Related wrong-book themes: ")
                .append(context.getRelatedWrongItems().stream()
                        .map(WrongBookItem::getKnowledgePoints)
                        .filter(value -> value != null && !value.isBlank())
                        .collect(Collectors.joining("; ")))
                .append("\n");
        builder.append("Suggested modules: ")
                .append(joinMapNames(context.getRecommendedLevels(), "pathName", "levelName"))
                .append("\n");
        builder.append("Candidate problems: ")
                .append(context.getRecommendedProblems().stream()
                        .map(Problem::getTitle)
                        .filter(value -> value != null && !value.isBlank())
                        .collect(Collectors.joining(", ")))
                .append("\n\n");

        builder.append("Current code:\n```").append(languageFence(context.getLanguage())).append("\n")
                .append(truncate(context.getCurrentCode(), 6000))
                .append("\n```\n");

        if (request != null && request.getMessage() != null && !request.getMessage().isBlank()) {
            builder.append("\nLearner request: ").append(request.getMessage()).append("\n");
        }
        return builder.toString();
    }

    public String buildUserPrompt(ProblemCoachContext context, ProblemAgentChatRequestVO request, boolean revealFullSolution) {
        if (request != null && request.getMessage() != null && !request.getMessage().isBlank()) {
            return request.getMessage();
        }
        if (revealFullSolution) {
            return "Please give me a complete corrected reference solution for this problem in my current language, and explain the key fix.";
        }
        if ("submit_failed".equalsIgnoreCase(context.getTriggerType())) {
            return "Please explain why this submission failed, what to fix next, and what I should practice after that.";
        }
        if ("run_failed".equalsIgnoreCase(context.getTriggerType())) {
            return "Please explain why this run failed and give me the smallest next debugging step.";
        }
        return "Please coach me on this problem without revealing the full answer.";
    }

    private String joinMapNames(List<Map<String, Object>> items, String primaryKey, String fallbackKey) {
        return items.stream()
                .map(item -> {
                    Object value = item.get(primaryKey);
                    if (value == null) {
                        value = item.get(fallbackKey);
                    }
                    return value == null ? null : String.valueOf(value);
                })
                .filter(value -> value != null && !value.isBlank())
                .limit(5)
                .collect(Collectors.joining(", "));
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "none" : value;
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return "";
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "\n...[truncated]";
    }

    private String languageFence(String language) {
        return language == null || language.isBlank() ? "text" : language.toLowerCase();
    }
}
