package com.programming.agent;

import com.alibaba.fastjson2.JSON;
import com.programming.agent.dto.AgentContextDTO;
import com.programming.agent.dto.ProblemContextDTO;
import com.programming.agent.dto.SubmissionContextDTO;
import com.programming.entity.LearningPath;
import com.programming.entity.PathLevel;
import com.programming.entity.Problem;
import com.programming.entity.Submit;
import com.programming.entity.UserProblemInteraction;
import com.programming.entity.WrongBookItem;
import com.programming.service.problemagent.ProblemCoachContext;
import com.programming.vo.problemagent.ProblemAgentChatRequestVO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AgentContextBuilder {

    public AgentContextDTO buildForSubmission(Problem problem,
                                              Submit submit,
                                              UserProblemInteraction interaction,
                                              int consecutiveFailures) {
        return build(
                problem,
                submit,
                interaction,
                AgentProtocolConstants.TRIGGER_SUBMISSION_RESULT,
                AgentProtocolConstants.INTENT_ASK_FOR_ERROR_ANALYSIS,
                null,
                false,
                submit == null ? null : submit.getCode(),
                submit == null ? null : submit.getLanguage(),
                submit == null ? null : mapResultToErrorType(submit.getResult()),
                null,
                consecutiveFailures
        );
    }

    public AgentContextDTO buildForProblemCoach(ProblemCoachContext coachContext,
                                                ProblemAgentChatRequestVO request,
                                                UserProblemInteraction interaction,
                                                int consecutiveFailures) {
        Submit submit = coachContext.getLatestSubmit();
        boolean requestedFullSolution = request != null && Boolean.TRUE.equals(request.getRequestFullSolution());
        String triggerSource = mapTriggerSource(coachContext.getTriggerType(), requestedFullSolution);
        String userIntent = detectIntent(triggerSource, request == null ? null : request.getMessage(), requestedFullSolution);
        String status = statusFromCoach(coachContext, submit);

        AgentContextDTO context = build(
                coachContext.getCurrentProblem(),
                submit,
                interaction,
                triggerSource,
                userIntent,
                request == null ? null : request.getMessage(),
                requestedFullSolution,
                request == null ? coachContext.getCurrentCode() : request.getCode(),
                request == null ? coachContext.getLanguage() : request.getLanguage(),
                firstNonBlank(coachContext.getLatestErrorMessage(), status),
                coachContext.getExecutionOutput(),
                consecutiveFailures
        );
        if (context.getSubmission() != null) {
            context.getSubmission().setStatus(status);
        }
        return context;
    }

    public AgentContextDTO buildForWrongBookEntry(Problem problem,
                                                  WrongBookItem wrongItem,
                                                  UserProblemInteraction interaction) {
        Submit submit = null;
        if (wrongItem != null && wrongItem.getSubmitId() != null) {
            submit = new Submit();
            submit.setId(wrongItem.getSubmitId());
            submit.setProblemId(wrongItem.getProblemId());
            submit.setCode(wrongItem.getCode());
            submit.setLanguage(wrongItem.getLanguage());
            submit.setResult(1);
        }

        int failures = Math.max(1, safeInt(interaction == null ? null : interaction.getConsecutiveFailures()));
        return build(
                problem,
                submit,
                interaction,
                AgentProtocolConstants.TRIGGER_WRONG_BOOK_ENTRY,
                AgentProtocolConstants.INTENT_ASK_FOR_ERROR_ANALYSIS,
                null,
                false,
                wrongItem == null ? null : wrongItem.getCode(),
                wrongItem == null ? null : wrongItem.getLanguage(),
                wrongItem == null ? null : wrongItem.getErrorMessage(),
                null,
                failures
        );
    }

    public AgentContextDTO buildForLearningPathEntry(LearningPath path,
                                                     PathLevel level,
                                                     List<Problem> problems,
                                                     UserProblemInteraction interaction) {
        Problem contextProblem = (problems == null || problems.isEmpty()) ? null : problems.get(0);
        if (contextProblem == null) {
            contextProblem = new Problem();
            contextProblem.setId(0L);
            contextProblem.setTitle(level == null ? "learning path" : level.getName());
            contextProblem.setContent(path == null ? null : path.getDescription());
            contextProblem.setKnowledgePoints(level == null ? null : level.getKnowledgePoints());
            contextProblem.setLanguage(path == null ? "java" : path.getLanguage());
        }

        AgentContextDTO context = build(
                contextProblem,
                null,
                interaction,
                AgentProtocolConstants.TRIGGER_LEARNING_PATH_ENTRY,
                AgentProtocolConstants.INTENT_ASK_FOR_NEXT_STEP,
                null,
                false,
                null,
                contextProblem.getLanguage(),
                null,
                null,
                0
        );
        if (level != null) {
            context.getProblem().setTitle(firstNonBlank(level.getName(), context.getProblem().getTitle()));
            context.getProblem().setProblemContent(firstNonBlank(level.getUnlockCondition(), context.getProblem().getProblemContent()));
        }
        return context;
    }

    private AgentContextDTO build(Problem problem,
                                  Submit submit,
                                  UserProblemInteraction interaction,
                                  String triggerSource,
                                  String userIntent,
                                  String userMessage,
                                  boolean requestedFullSolution,
                                  String code,
                                  String language,
                                  String errorMessage,
                                  String executionOutput,
                                  int consecutiveFailures) {
        AgentContextDTO context = new AgentContextDTO();
        context.setRequestId(AgentClient.generateRequestId());
        context.setTimestamp(LocalDateTime.now());
        context.setTriggerSource(triggerSource);
        context.setUserIntent(userIntent);
        context.setUserMessage(userMessage);
        context.setRequestedFullSolution(requestedFullSolution);
        context.setConsecutiveFailures(Math.max(consecutiveFailures, 0));
        context.setHasViewedSolution(Boolean.TRUE.equals(interaction == null ? null : interaction.getHasViewedAnswer()));
        context.setHintCount(safeInt(interaction == null ? null : interaction.getHintCount()));
        context.setDiagnoseCount(safeInt(interaction == null ? null : interaction.getDiagnoseCount()));
        context.setExplainCount(safeInt(interaction == null ? null : interaction.getExplainCount()));
        context.setRecommendCount(safeInt(interaction == null ? null : interaction.getRecommendCount()));
        context.setReflectCount(safeInt(interaction == null ? null : interaction.getReflectCount()));
        context.setLastActionType(interaction == null ? null : interaction.getLastActionType());
        context.setLastGoal(interaction == null ? null : interaction.getLastGoal());
        context.setLastGuidanceType(interaction == null ? null : interaction.getLastGuidanceType());
        context.setLastErrorTag(interaction == null ? null : interaction.getErrorTag());
        context.setWeakPoints(parseWeakPoints(interaction == null ? null : interaction.getWeakPoints()));
        context.setLearningStage(firstNonBlank(interaction == null ? null : interaction.getLearningStage(), "FIRST_TRY"));

        ProblemContextDTO problemDTO = new ProblemContextDTO();
        problemDTO.setProblemId(problem == null || problem.getId() == null ? 0 : problem.getId().intValue());
        problemDTO.setTitle(problem == null ? "" : problem.getTitle());
        problemDTO.setDifficulty(problem == null || problem.getDifficulty() == null ? "unknown" : String.valueOf(problem.getDifficulty()));
        problemDTO.setKnowledgePoints(parseKnowledgePoints(problem == null ? null : problem.getKnowledgePoints()));
        problemDTO.setProblemContent(problem == null ? null : problem.getContent());
        problemDTO.setHints(problem == null ? null : problem.getHints());
        problemDTO.setSampleExplanation(problem == null ? null : problem.getSampleExplanation());
        problemDTO.setTags(problem == null ? null : problem.getTags());
        problemDTO.setLanguage(firstNonBlank(language, problem == null ? null : problem.getLanguage(), "java"));
        problemDTO.setHintShownCount(context.getHintCount());
        problemDTO.setDiagnosedCount(context.getDiagnoseCount());
        context.setProblem(problemDTO);

        SubmissionContextDTO submissionDTO = new SubmissionContextDTO();
        submissionDTO.setSubmitId(submit == null || submit.getId() == null ? 0 : submit.getId().intValue());
        submissionDTO.setStatus(submit == null ? "NONE" : String.valueOf(submit.getResult()));
        submissionDTO.setErrorMessage(errorMessage);
        submissionDTO.setFirstAttempt(context.getConsecutiveFailures() <= 1);
        submissionDTO.setCodeContent(code);
        submissionDTO.setExecutionOutput(executionOutput);
        context.setSubmission(submissionDTO);

        return context;
    }

    private String mapTriggerSource(String triggerType, boolean requestedFullSolution) {
        if (requestedFullSolution) {
            return AgentProtocolConstants.TRIGGER_MANUAL_HELP_REQUEST;
        }
        if ("run_failed".equalsIgnoreCase(triggerType)) {
            return AgentProtocolConstants.TRIGGER_RUN_RESULT;
        }
        if ("submit_failed".equalsIgnoreCase(triggerType)) {
            return AgentProtocolConstants.TRIGGER_SUBMISSION_RESULT;
        }
        return AgentProtocolConstants.TRIGGER_PROBLEM_PAGE_CHAT;
    }

    private String detectIntent(String triggerSource, String message, boolean requestedFullSolution) {
        if (requestedFullSolution) {
            return AgentProtocolConstants.INTENT_ASK_FOR_FULL_SOLUTION;
        }
        String normalized = message == null ? "" : message.toLowerCase();
        if (normalized.contains("答案") || normalized.contains("完整代码") || normalized.contains("参考代码")) {
            return AgentProtocolConstants.INTENT_ASK_FOR_FULL_SOLUTION;
        }
        if (normalized.contains("思路") || normalized.contains("怎么做") || normalized.contains("解法")) {
            return AgentProtocolConstants.INTENT_ASK_PROBLEM_SOLVING_IDEA;
        }
        if (normalized.contains("提示")) {
            return AgentProtocolConstants.INTENT_ASK_FOR_HINT;
        }
        if (normalized.contains("讲解") || normalized.contains("解释") || normalized.contains("概念")) {
            return AgentProtocolConstants.INTENT_ASK_FOR_EXPLANATION;
        }
        if (normalized.contains("检查") || normalized.contains("review")) {
            return AgentProtocolConstants.INTENT_ASK_FOR_CODE_REVIEW;
        }
        if (normalized.contains("为什么错") || normalized.contains("报错") || normalized.contains("错误")) {
            return AgentProtocolConstants.INTENT_ASK_FOR_ERROR_ANALYSIS;
        }
        if (normalized.contains("下一步") || normalized.contains("接下来")) {
            return AgentProtocolConstants.INTENT_ASK_FOR_NEXT_STEP;
        }
        if (AgentProtocolConstants.TRIGGER_RUN_RESULT.equals(triggerSource)
                || AgentProtocolConstants.TRIGGER_SUBMISSION_RESULT.equals(triggerSource)) {
            return AgentProtocolConstants.INTENT_ASK_FOR_ERROR_ANALYSIS;
        }
        return normalized.isBlank() ? AgentProtocolConstants.INTENT_UNKNOWN : AgentProtocolConstants.INTENT_GENERAL_CHAT;
    }

    private String statusFromCoach(ProblemCoachContext context, Submit submit) {
        if (context.getLatestResultCode() != null) {
            return mapResultToErrorType(context.getLatestResultCode());
        }
        if (submit != null) {
            return mapResultToErrorType(submit.getResult());
        }
        if ("run_failed".equalsIgnoreCase(context.getTriggerType())) {
            return "RUN_FAILED";
        }
        return "NONE";
    }

    public String mapResultToErrorType(Integer result) {
        if (result == null) return "UNKNOWN";
        return switch (result) {
            case 0 -> "AC";
            case 1 -> "WA";
            case 2 -> "TLE";
            case 3 -> "MLE";
            case 4 -> "RE";
            case 5 -> "CE";
            default -> "UNKNOWN";
        };
    }

    private List<String> parseKnowledgePoints(String value) {
        if (value == null || value.trim().isEmpty()) {
            return Collections.singletonList("general");
        }
        String trimmed = value.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            try {
                return JSON.parseArray(trimmed, String.class);
            } catch (Exception ignored) {
                // Fall through to comma splitting.
            }
        }
        return Arrays.stream(trimmed.split("[,，]"))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .collect(Collectors.toList());
    }

    private List<String> parseWeakPoints(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return JSON.parseArray(value, String.class);
        } catch (Exception ignored) {
            return parseKnowledgePoints(value);
        }
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
