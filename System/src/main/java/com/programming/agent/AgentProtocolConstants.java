package com.programming.agent;

import java.util.Map;
import java.util.Set;

public final class AgentProtocolConstants {
    private AgentProtocolConstants() {}

    public static final String TRIGGER_PROBLEM_PAGE_CHAT = "PROBLEM_PAGE_CHAT";
    public static final String TRIGGER_RUN_RESULT = "RUN_RESULT";
    public static final String TRIGGER_SUBMISSION_RESULT = "SUBMISSION_RESULT";
    public static final String TRIGGER_WRONG_BOOK_ENTRY = "WRONG_BOOK_ENTRY";
    public static final String TRIGGER_LEARNING_PATH_ENTRY = "LEARNING_PATH_ENTRY";
    public static final String TRIGGER_MANUAL_HELP_REQUEST = "MANUAL_HELP_REQUEST";

    public static final String INTENT_ASK_PROBLEM_SOLVING_IDEA = "ASK_PROBLEM_SOLVING_IDEA";
    public static final String INTENT_ASK_FOR_HINT = "ASK_FOR_HINT";
    public static final String INTENT_ASK_FOR_EXPLANATION = "ASK_FOR_EXPLANATION";
    public static final String INTENT_ASK_FOR_FULL_SOLUTION = "ASK_FOR_FULL_SOLUTION";
    public static final String INTENT_ASK_FOR_CODE_REVIEW = "ASK_FOR_CODE_REVIEW";
    public static final String INTENT_ASK_FOR_ERROR_ANALYSIS = "ASK_FOR_ERROR_ANALYSIS";
    public static final String INTENT_ASK_FOR_NEXT_STEP = "ASK_FOR_NEXT_STEP";
    public static final String INTENT_GENERAL_CHAT = "GENERAL_CHAT";
    public static final String INTENT_UNKNOWN = "UNKNOWN";

    public static final String ACTION_GUIDE_IDEA = "GUIDE_IDEA";
    public static final String ACTION_HINT = "HINT";
    public static final String ACTION_DIAGNOSE = "DIAGNOSE";
    public static final String ACTION_EXPLAIN = "EXPLAIN";
    public static final String ACTION_RECOMMEND = "RECOMMEND";
    public static final String ACTION_REFLECT = "REFLECT";
    public static final String ACTION_REVEAL_ANSWER = "REVEAL_ANSWER";

    public static final Map<String, String> CONTENT_TYPE_BY_ACTION = Map.of(
            ACTION_GUIDE_IDEA, "guidance",
            ACTION_HINT, "hint",
            ACTION_DIAGNOSE, "diagnosis",
            ACTION_EXPLAIN, "explanation",
            ACTION_RECOMMEND, "recommendation",
            ACTION_REFLECT, "reflection",
            ACTION_REVEAL_ANSWER, "solution"
    );

    public static final Map<String, String> GOAL_BY_ACTION = Map.of(
            ACTION_GUIDE_IDEA, "GUIDE_INITIAL_THINKING",
            ACTION_HINT, "GIVE_LIGHT_HINT",
            ACTION_DIAGNOSE, "DIAGNOSE_ERROR_CAUSE",
            ACTION_EXPLAIN, "EXPLAIN_CONCEPT",
            ACTION_RECOMMEND, "RECOMMEND_REMEDIATION",
            ACTION_REFLECT, "REVIEW_AFTER_SOLUTION",
            ACTION_REVEAL_ANSWER, "REVIEW_AFTER_SOLUTION"
    );

    public static final Set<String> LEARNING_ACTIONS = Set.of(
            ACTION_GUIDE_IDEA,
            ACTION_HINT,
            ACTION_DIAGNOSE,
            ACTION_EXPLAIN,
            ACTION_RECOMMEND,
            ACTION_REFLECT,
            ACTION_REVEAL_ANSWER
    );
}
