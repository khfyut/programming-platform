package com.programming.agent;

import com.alibaba.fastjson2.JSON;
import com.programming.agent.dto.AgentContextDTO;
import com.programming.agent.dto.ProblemContextDTO;
import com.programming.agent.dto.SubmissionContextDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class DecisionContextReducer {
    public static final String LEVEL_NONE = "NONE";
    public static final String LEVEL_WEAK = "WEAK";
    public static final String LEVEL_STRONG = "STRONG";
    public static final String LEVEL_VERIFIED = "VERIFIED";

    private static final int NORMAL_HARD_LIMIT = 2500;
    private static final int HEAVY_HARD_LIMIT = 4000;

    public AgentContextDTO reduce(AgentContextDTO context) {
        if (context == null) {
            return null;
        }

        String scene = firstNonBlank(context.getScene(), inferScene(context));
        String actionHint = firstNonBlank(context.getActionHint(), inferActionHint(context));
        context.setScene(scene);
        context.setActionHint(actionHint);

        Map<String, Object> failureSignals = buildFailureSignals(context, scene);
        String evidenceLevel = failureEvidenceLevel(failureSignals);
        context.setFailureSignals(failureSignals);
        context.setFailureEvidenceLevel(evidenceLevel);

        boolean heavy = isHeavyContext(context, scene, actionHint);
        Map<String, Object> reduced = buildReducedContext(context, scene, actionHint, failureSignals, evidenceLevel, heavy);
        context.setReducedContext(trimReducedContext(reduced, heavy ? HEAVY_HARD_LIMIT : NORMAL_HARD_LIMIT));
        return context;
    }

    private Map<String, Object> buildReducedContext(AgentContextDTO context,
                                                    String scene,
                                                    String actionHint,
                                                    Map<String, Object> failureSignals,
                                                    String evidenceLevel,
                                                    boolean heavy) {
        List<String> selectedSignals = flattenSignalLists(failureSignals);
        List<String> droppedSignals = new ArrayList<>();
        ProblemContextDTO problem = context.getProblem();
        SubmissionContextDTO submission = context.getSubmission();

        Map<String, Object> required = new LinkedHashMap<>();
        required.put("scene", scene);
        required.put("action_hint", actionHint);
        required.put("latest_user_message", truncate(context.getUserMessage(), 280));
        required.put("problem_title", problem == null ? "" : truncate(problem.getTitle(), 120));
        required.put("problem_summary", problem == null ? "" : truncate(problem.getProblemContent(), heavy ? 1000 : 650));

        Map<String, Object> conditional = new LinkedHashMap<>();
        if (problem != null) {
            conditional.put("difficulty", problem.getDifficulty());
            conditional.put("knowledge_points", problem.getKnowledgePoints());
        }
        if (submission != null) {
            conditional.put("submission_status", submission.getStatus());
            conditional.put("latest_error", truncate(submission.getErrorMessage(), heavy ? 700 : 360));
            conditional.put("execution_output", truncate(submission.getExecutionOutput(), heavy ? 700 : 280));
            if (shouldIncludeCode(context, actionHint, heavy)) {
                conditional.put("code_excerpt", truncate(submission.getCodeContent(), heavy ? 1300 : 700));
            } else if (hasText(submission.getCodeContent())) {
                droppedSignals.add("CODE_OMITTED_NOT_DEBUG_REQUEST");
            }
        }
        if (shouldIncludeRecommendations(context, actionHint)) {
            conditional.put("tool_results", compactToolResults(context.getToolResults(), 5));
        } else if (context.getToolResults() != null && !context.getToolResults().isEmpty()) {
            droppedSignals.add("TOOL_RESULTS_OMITTED_NOT_RECOMMEND_REQUEST");
        }

        Map<String, Object> derived = new LinkedHashMap<>();
        derived.put("failure_evidence_level", evidenceLevel);
        derived.put("failure_signals", failureSignals);
        derived.put("learning_stage", context.getLearningStage());
        derived.put("weak_points", context.getWeakPoints());
        derived.put("requested_full_solution", context.isRequestedFullSolution());

        Map<String, Object> reduced = new LinkedHashMap<>();
        reduced.put("required_context", required);
        reduced.put("conditional_context", conditional);
        reduced.put("derived_summary", derived);
        reduced.put("selected_signals", selectedSignals);
        reduced.put("dropped_signals", droppedSignals);
        return reduced;
    }

    private Map<String, Object> trimReducedContext(Map<String, Object> reduced, int hardLimit) {
        if (JSON.toJSONString(reduced).length() <= hardLimit) {
            return reduced;
        }
        trimSectionText(reduced, "conditional_context", "code_excerpt", 500);
        trimSectionText(reduced, "conditional_context", "latest_error", 240);
        trimSectionText(reduced, "conditional_context", "execution_output", 200);
        trimSectionText(reduced, "required_context", "problem_summary", 420);
        trimSectionText(reduced, "required_context", "latest_user_message", 180);
        if (JSON.toJSONString(reduced).length() <= hardLimit) {
            return reduced;
        }
        Object dropped = reduced.get("dropped_signals");
        if (dropped instanceof List<?> list) {
            List<Object> copy = new ArrayList<>(list);
            copy.add("CONTEXT_TRUNCATED_TO_BUDGET");
            reduced.put("dropped_signals", copy);
        }
        trimSectionText(reduced, "conditional_context", "code_excerpt", 220);
        trimSectionText(reduced, "required_context", "problem_summary", 260);
        return reduced;
    }

    @SuppressWarnings("unchecked")
    private void trimSectionText(Map<String, Object> reduced, String sectionName, String key, int maxChars) {
        Object raw = reduced.get(sectionName);
        if (!(raw instanceof Map<?, ?> section)) {
            return;
        }
        Object value = section.get(key);
        if (value instanceof String text && text.length() > maxChars) {
            ((Map<String, Object>) section).put(key, truncate(text, maxChars));
        }
    }

    private Map<String, Object> buildFailureSignals(AgentContextDTO context, String scene) {
        List<String> contextSignals = new ArrayList<>();
        List<String> strongSignals = new ArrayList<>();
        List<String> weakSignals = new ArrayList<>();

        SubmissionContextDTO submission = context.getSubmission();
        String trigger = normalize(context.getTriggerSource());
        String status = submission == null ? "" : normalize(submission.getStatus());
        String error = submission == null ? "" : normalize(submission.getErrorMessage());

        if ("wrong_book".equals(scene) || AgentProtocolConstants.TRIGGER_WRONG_BOOK_ENTRY.equalsIgnoreCase(context.getTriggerSource())) {
            contextSignals.add("WRONG_BOOK_RECORD");
        }
        if (AgentProtocolConstants.TRIGGER_SUBMISSION_RESULT.equalsIgnoreCase(context.getTriggerSource())
                && submission != null
                && submission.getSubmitId() > 0
                && isFailureStatus(status)) {
            contextSignals.add("SESSION_LINKED_FAILED_SUBMIT");
        }

        if (AgentProtocolConstants.TRIGGER_SUBMISSION_RESULT.equalsIgnoreCase(context.getTriggerSource()) && isFailureStatus(status)) {
            strongSignals.add("SUBMIT_FAILED");
        }
        if (AgentProtocolConstants.TRIGGER_RUN_RESULT.equalsIgnoreCase(context.getTriggerSource())
                && (isFailureStatus(status) || hasText(error))) {
            strongSignals.add("RUN_FAILED_WITH_ERROR");
        }
        if (containsAny(error, "failed test", "test case", "case failed")
                || containsAny(normalize(submission == null ? null : submission.getExecutionOutput()), "failed test", "test case", "case failed")) {
            strongSignals.add("FAILED_TEST_CASE");
        }

        if (hasText(submission == null ? null : submission.getCodeContent())) {
            weakSignals.add("USER_CODE_PRESENT");
        }
        if (hasText(context.getUserMessage()) || hasText(error)) {
            weakSignals.add("USER_ERROR_DESCRIPTION");
        }
        if (containsAny(normalize(context.getUserMessage()), "tried", "submitted", "ran", "failed", "error")) {
            weakSignals.add("USER_CLAIMS_TRIED");
        }

        Map<String, Object> signals = new LinkedHashMap<>();
        signals.put("context_signals", contextSignals);
        signals.put("strong_evidence_signals", strongSignals);
        signals.put("weak_attempt_signals", weakSignals);
        return signals;
    }

    private String failureEvidenceLevel(Map<String, Object> signals) {
        if (!signalList(signals, "context_signals").isEmpty()) {
            return LEVEL_VERIFIED;
        }
        if (!signalList(signals, "strong_evidence_signals").isEmpty()) {
            return LEVEL_STRONG;
        }
        if (!signalList(signals, "weak_attempt_signals").isEmpty()) {
            return LEVEL_WEAK;
        }
        return LEVEL_NONE;
    }

    private List<String> flattenSignalLists(Map<String, Object> signals) {
        List<String> result = new ArrayList<>();
        result.addAll(signalList(signals, "context_signals"));
        result.addAll(signalList(signals, "strong_evidence_signals"));
        result.addAll(signalList(signals, "weak_attempt_signals"));
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<String> signalList(Map<String, Object> signals, String key) {
        Object raw = signals == null ? null : signals.get(key);
        if (raw instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    private boolean isHeavyContext(AgentContextDTO context, String scene, String actionHint) {
        String intent = normalize(context.getUserIntent());
        String hint = normalize(actionHint);
        return "wrong_book".equals(scene)
                || containsAny(hint, "diagnose", "reflection_task_submit")
                || containsAny(intent, "error", "code_review")
                || context.isRequestedFullSolution();
    }

    private boolean shouldIncludeCode(AgentContextDTO context, String actionHint, boolean heavy) {
        String intent = normalize(context.getUserIntent());
        String message = normalize(context.getUserMessage());
        String hint = normalize(actionHint);
        return heavy
                || containsAny(hint, "diagnose", "reflection_task_submit")
                || containsAny(intent, "error", "code_review", "full_solution")
                || containsAny(message, "code", "bug", "runtime", "error", "wrong answer", "compile");
    }

    private boolean shouldIncludeRecommendations(AgentContextDTO context, String actionHint) {
        String hint = normalize(actionHint);
        String message = normalize(context.getUserMessage());
        return containsAny(hint, "similar_problem", "recommend")
                || containsAny(message, "recommend", "similar", "practice");
    }

    private List<Map<String, Object>> compactToolResults(List<Map<String, Object>> toolResults, int limit) {
        if (toolResults == null || toolResults.isEmpty()) {
            return List.of();
        }
        List<Map<String, Object>> compact = new ArrayList<>();
        for (Map<String, Object> item : toolResults) {
            if (item == null) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : item.entrySet()) {
                if (row.size() >= 6) {
                    break;
                }
                row.put(entry.getKey(), entry.getValue());
            }
            compact.add(row);
            if (compact.size() >= limit) {
                break;
            }
        }
        return compact;
    }

    private String inferScene(AgentContextDTO context) {
        if (context == null) {
            return "problem_coach";
        }
        if (AgentPolicyProfile.WRONG_BOOK_REFLECTION.name().equalsIgnoreCase(context.getPolicyProfile())
                || AgentProtocolConstants.TRIGGER_WRONG_BOOK_ENTRY.equalsIgnoreCase(context.getTriggerSource())) {
            return "wrong_book";
        }
        return "problem_coach";
    }

    private String inferActionHint(AgentContextDTO context) {
        if (context == null || context.getUserIntent() == null) {
            return "hint";
        }
        return switch (context.getUserIntent()) {
            case AgentProtocolConstants.INTENT_ASK_FOR_HINT -> "hint";
            case AgentProtocolConstants.INTENT_ASK_FOR_ERROR_ANALYSIS -> "diagnose";
            case AgentProtocolConstants.INTENT_ASK_FOR_NEXT_STEP -> "recommend";
            case AgentProtocolConstants.INTENT_ASK_FOR_FULL_SOLUTION -> "full_solution";
            default -> "chat";
        };
    }

    private boolean isFailureStatus(String status) {
        if (status == null || status.isBlank()) {
            return false;
        }
        return !"0".equals(status)
                && !"AC".equalsIgnoreCase(status)
                && !"NONE".equalsIgnoreCase(status);
    }

    private String truncate(String value, int maxChars) {
        if (value == null) {
            return "";
        }
        if (value.length() <= maxChars) {
            return value;
        }
        if (maxChars <= 3) {
            return value.substring(0, maxChars);
        }
        return value.substring(0, maxChars - 3) + "...";
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (hasText(value)) {
                return value;
            }
        }
        return "";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private boolean containsAny(String value, String... candidates) {
        if (value == null || value.isBlank()) {
            return false;
        }
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank() && value.contains(candidate)) {
                return true;
            }
        }
        return false;
    }
}
