package com.programming.agent;

import com.programming.agent.dto.AgentContextDTO;
import com.programming.agent.dto.AgentDecisionDTO;
import com.programming.entity.LearningEventLog;
import com.programming.entity.LearningPath;
import com.programming.entity.PathLevel;
import com.programming.entity.Problem;
import com.programming.entity.UserProblemInteraction;
import com.programming.entity.WrongBookItem;
import com.programming.mapper.LearningEventLogMapper;
import com.programming.mapper.ProblemMapper;
import com.programming.mapper.SubmitMapper;
import com.programming.mapper.UserProblemInteractionMapper;
import com.programming.service.AgentService;
import com.programming.service.LearningService;
import com.programming.service.WrongBookService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentEntryServiceTest {

    @Mock
    private AgentClient agentClient;
    @Mock
    private AgentContextBuilder contextBuilder;
    @Mock
    private AgentDecisionEnforcer decisionEnforcer;
    @Mock
    private SubmitMapper submitMapper;
    @Mock
    private ProblemMapper problemMapper;
    @Mock
    private LearningEventLogMapper learningEventLogMapper;
    @Mock
    private UserProblemInteractionMapper userProblemInteractionMapper;
    @Mock
    private WrongBookService wrongBookService;
    @Mock
    private LearningService learningService;

    @InjectMocks
    private AgentService agentService;

    @Test
    void wrongBookEntryReturnsReflectionDecisionAndRecordsWrongItemScope() {
        Long userId = 7L;
        Long wrongItemId = 31L;
        WrongBookItem item = wrongItem(wrongItemId, 100L, 900L);
        Problem problem = problem(100L);
        UserProblemInteraction interaction = interaction(userId, 100L);
        AgentContextDTO context = context("req-wrong", "WRONG_BOOK_ENTRY", "ASK_FOR_ERROR_ANALYSIS");
        AgentDecisionDTO decision = decision("req-wrong", "REFLECT", "reflection", "REVIEW_AFTER_SOLUTION");

        when(wrongBookService.getWrongBookItemById(userId, wrongItemId)).thenReturn(item);
        when(problemMapper.findById(100L)).thenReturn(problem);
        when(userProblemInteractionMapper.findByUserAndProblem(userId, 100L)).thenReturn(interaction);
        when(contextBuilder.buildForWrongBookEntry(problem, item, interaction)).thenReturn(context);
        when(agentClient.getDecision(context)).thenReturn(decision);
        when(decisionEnforcer.enforce(context, decision)).thenReturn(AgentExecutionResult.executed());

        AgentDecisionDTO result = agentService.processWrongBookReflection(userId, wrongItemId);

        assertEquals("REFLECT", result.getActionType());
        assertEquals("reflection", result.getContentType());
        assertEquals("REVIEW_AFTER_SOLUTION", result.getPedagogicalGoal());
        verify(userProblemInteractionMapper).incrementReflectCount(userId, 100L);
        ArgumentCaptor<LearningEventLog> logCaptor = ArgumentCaptor.forClass(LearningEventLog.class);
        verify(learningEventLogMapper).insert(logCaptor.capture());
        assertEquals("WRONG_BOOK", logCaptor.getValue().getEntryRefType());
        assertEquals(wrongItemId, logCaptor.getValue().getEntryRefId());
        assertEquals(wrongItemId, logCaptor.getValue().getWrongItemId());
    }

    @Test
    void learningPathEntryReturnsRecommendationAndDoesNotTouchProblemInteractionState() {
        Long userId = 7L;
        Long pathId = 4L;
        Long levelId = 5L;
        LearningPath path = new LearningPath();
        path.setId(pathId);
        path.setName("Java algorithm path");
        PathLevel level = new PathLevel();
        level.setId(levelId);
        level.setChapterId(99L);
        level.setName("hashmap warmup");
        level.setKnowledgePoints("array,hashmap");
        AgentContextDTO context = context("req-path", "LEARNING_PATH_ENTRY", "ASK_FOR_NEXT_STEP");
        AgentDecisionDTO decision = decision("req-path", "RECOMMEND", "recommendation", "RECOMMEND_REMEDIATION");

        when(learningService.getPathDetail(pathId)).thenReturn(path);
        when(learningService.getLevelDetail(levelId)).thenReturn(level);
        when(contextBuilder.buildForLearningPathEntry(path, level, new ArrayList<>(), null)).thenReturn(context);
        when(agentClient.getDecision(context)).thenReturn(decision);
        when(decisionEnforcer.enforce(context, decision)).thenReturn(AgentExecutionResult.executed());

        AgentDecisionDTO result = agentService.processLearningPathRecommendation(userId, pathId, levelId);

        assertEquals("RECOMMEND", result.getActionType());
        assertEquals("recommendation", result.getContentType());
        assertEquals("RECOMMEND_REMEDIATION", result.getPedagogicalGoal());
        verify(userProblemInteractionMapper, never()).incrementRecommendCount(any(), any());
        ArgumentCaptor<LearningEventLog> logCaptor = ArgumentCaptor.forClass(LearningEventLog.class);
        verify(learningEventLogMapper).insert(logCaptor.capture());
        assertEquals("LEARNING_PATH_LEVEL", logCaptor.getValue().getEntryRefType());
        assertEquals(levelId, logCaptor.getValue().getEntryRefId());
        assertEquals(pathId, logCaptor.getValue().getPathId());
        assertEquals(levelId, logCaptor.getValue().getLevelId());
        assertEquals(null, logCaptor.getValue().getProblemId());
    }

    @Test
    void wrongBookEntryRejectsCrossUserItemThroughWrongBookService() {
        when(wrongBookService.getWrongBookItemById(7L, 31L)).thenThrow(new RuntimeException("Wrong book item not found"));

        assertThrows(RuntimeException.class, () -> agentService.processWrongBookReflection(7L, 31L));

        verify(agentClient, never()).getDecision(any());
    }

    @Test
    void reportSummaryGroupsRecentAgentActionsAndWeakPointsFromJavaState() {
        Long userId = 7L;
        LearningEventLog hint = event(userId, "HINT", true);
        LearningEventLog reveal = event(userId, "REVEAL_ANSWER", true);
        UserProblemInteraction weak = interaction(userId, 100L);
        weak.setWeakPoints("[\"hashmap\",\"boundary\"]");

        when(learningEventLogMapper.findRecentByUser(userId, 30)).thenReturn(List.of(hint, reveal));
        when(userProblemInteractionMapper.findRecentByUser(userId, 20)).thenReturn(List.of(weak));

        Map<String, Object> summary = agentService.getAgentReportSummary(userId);

        Map<?, ?> actionCounts = (Map<?, ?>) summary.get("action_counts");
        assertEquals(1L, actionCounts.get("HINT"));
        assertEquals(1L, actionCounts.get("REVEAL_ANSWER"));
        assertEquals(1L, summary.get("revealed_solution_count"));
        assertEquals(List.of("hashmap", "boundary"), summary.get("weak_points"));
    }

    private WrongBookItem wrongItem(Long id, Long problemId, Long submitId) {
        WrongBookItem item = new WrongBookItem();
        item.setId(id);
        item.setProblemId(problemId);
        item.setSubmitId(submitId);
        item.setCode("class Solution {}");
        item.setLanguage("java");
        item.setErrorMessage("WA");
        item.setKnowledgePoints("array,hashmap");
        return item;
    }

    private Problem problem(Long id) {
        Problem problem = new Problem();
        problem.setId(id);
        problem.setTitle("two sum");
        problem.setKnowledgePoints("array,hashmap");
        problem.setLanguage("java");
        return problem;
    }

    private UserProblemInteraction interaction(Long userId, Long problemId) {
        UserProblemInteraction interaction = new UserProblemInteraction();
        interaction.setUserId(userId);
        interaction.setProblemId(problemId);
        interaction.setHintCount(0);
        interaction.setDiagnoseCount(0);
        interaction.setExplainCount(0);
        interaction.setRecommendCount(0);
        interaction.setReflectCount(0);
        interaction.setHasViewedAnswer(true);
        interaction.setConsecutiveFailures(1);
        interaction.setLearningStage("EXPLAINED");
        return interaction;
    }

    private AgentContextDTO context(String requestId, String triggerSource, String intent) {
        AgentContextDTO context = new AgentContextDTO();
        context.setRequestId(requestId);
        context.setTriggerSource(triggerSource);
        context.setUserIntent(intent);
        context.setRequestedFullSolution(false);
        context.setConsecutiveFailures(1);
        return context;
    }

    private AgentDecisionDTO decision(String requestId, String actionType, String contentType, String goal) {
        AgentDecisionDTO decision = new AgentDecisionDTO();
        decision.setResponseId(requestId);
        decision.setActionType(actionType);
        decision.setContentType(contentType);
        decision.setPedagogicalGoal(goal);
        decision.setMainResponse("agent response");
        decision.setContent("agent response");
        return decision;
    }

    private LearningEventLog event(Long userId, String actionType, boolean executed) {
        LearningEventLog event = new LearningEventLog();
        event.setUserId(userId);
        event.setRequestId("req-" + actionType);
        event.setActionType(actionType);
        event.setContentType("hint");
        event.setExecuted(executed);
        return event;
    }
}
