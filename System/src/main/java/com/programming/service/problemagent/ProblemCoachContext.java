package com.programming.service.problemagent;

import com.programming.entity.AiSession;
import com.programming.entity.Problem;
import com.programming.entity.Submit;
import com.programming.entity.WrongBookItem;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class ProblemCoachContext {
    private Long userId;
    private Problem currentProblem;
    private AiSession session;
    private Submit latestSubmit;
    private List<Submit> recentSubmissions = new ArrayList<>();
    private List<WrongBookItem> relatedWrongItems = new ArrayList<>();
    private List<Problem> recommendedProblems = new ArrayList<>();
    private List<Map<String, Object>> recommendedLevels = new ArrayList<>();
    private List<Map<String, Object>> weakKnowledgePoints = new ArrayList<>();
    private String currentCode;
    private String language;
    private String triggerType;
    private Integer latestResultCode;
    private String latestErrorMessage;
    private String executionOutput;
    private boolean hasFailure;
    private boolean aiAvailable;
}
