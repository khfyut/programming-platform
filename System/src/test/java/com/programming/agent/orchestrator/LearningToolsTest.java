package com.programming.agent.orchestrator;

import com.programming.entity.LearningEventLog;
import com.programming.entity.Problem;
import com.programming.entity.Submit;
import com.programming.entity.UserProblemInteraction;
import com.programming.entity.WrongBookItem;
import com.programming.mapper.LearningEventLogMapper;
import com.programming.mapper.ProblemMapper;
import com.programming.mapper.SubmitMapper;
import com.programming.mapper.UserProblemInteractionMapper;
import com.programming.service.WrongBookService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LearningToolsTest {

    @Test
    void problemToolReadsProblemContext() {
        ProblemMapper mapper = mock(ProblemMapper.class);
        Problem problem = new Problem();
        problem.setId(3L);
        problem.setTitle("Two Sum");
        problem.setKnowledgePoints("array,hashmap");
        when(mapper.findById(3L)).thenReturn(problem);

        ToolResult result = new ProblemLearningTool(mapper).execute(eventWithProblem());

        assertTrue(result.isSuccess());
        assertEquals("problem", result.getToolName());
        assertEquals("Two Sum", result.getData().get("title"));
        assertEquals("array,hashmap", result.getData().get("knowledge_points"));
    }

    @Test
    void submissionToolRejectsCrossUserSubmission() {
        SubmitMapper mapper = mock(SubmitMapper.class);
        Submit submit = new Submit();
        submit.setId(40L);
        submit.setUserId(99L);
        submit.setProblemId(3L);
        when(mapper.findById(40L)).thenReturn(submit);

        LearningAgentEvent event = eventWithProblem();
        event.setSubmitId(40L);

        ToolResult result = new SubmissionLearningTool(mapper).execute(event);

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("not owned"));
    }

    @Test
    void wrongBookToolReadsOwnedWrongItem() {
        WrongBookService service = mock(WrongBookService.class);
        WrongBookItem item = new WrongBookItem();
        item.setId(88L);
        item.setProblemId(3L);
        item.setErrorMessage("WA");
        item.setKnowledgePoints("array,boundary");
        when(service.getWrongBookItemById(17L, 88L)).thenReturn(item);

        LearningAgentEvent event = eventWithProblem();
        event.setWrongItemId(88L);

        ToolResult result = new WrongBookLearningTool(service).execute(event);

        assertTrue(result.isSuccess());
        assertEquals("wrong_book", result.getToolName());
        assertEquals("WA", result.getData().get("error_message"));
    }

    @Test
    void recommendationToolReturnsSimilarProblemsForWrongItem() {
        WrongBookService service = mock(WrongBookService.class);
        Problem similar = new Problem();
        similar.setId(12L);
        similar.setTitle("Two Sum II");
        when(service.getRecommendedProblems(17L, 88L, 5)).thenReturn(List.of(similar));

        LearningAgentEvent event = eventWithProblem();
        event.setWrongItemId(88L);

        ToolResult result = new RecommendationLearningTool(service).execute(event);

        assertTrue(result.isSuccess());
        List<?> recommendations = (List<?>) result.getData().get("recommendations");
        Map<?, ?> first = (Map<?, ?>) recommendations.get(0);
        assertEquals(12L, first.get("problem_id"));
        assertEquals("Two Sum II", first.get("title"));
    }

    @Test
    void learningStateToolSummarizesInteractionAndRecentEvents() {
        UserProblemInteractionMapper interactionMapper = mock(UserProblemInteractionMapper.class);
        LearningEventLogMapper eventMapper = mock(LearningEventLogMapper.class);
        UserProblemInteraction interaction = new UserProblemInteraction();
        interaction.setLearningStage("HINTED");
        interaction.setHintCount(2);
        interaction.setConsecutiveFailures(1);
        LearningEventLog log = new LearningEventLog();
        log.setActionType("HINT");
        when(interactionMapper.findByUserAndProblem(17L, 3L)).thenReturn(interaction);
        when(eventMapper.findRecentByUser(17L, 5)).thenReturn(List.of(log));

        ToolResult result = new LearningStateTool(interactionMapper, eventMapper).execute(eventWithProblem());

        assertTrue(result.isSuccess());
        assertEquals("learning_state", result.getToolName());
        assertEquals("HINTED", result.getData().get("learning_stage"));
        assertEquals(2, result.getData().get("hint_count"));
        assertEquals(List.of("HINT"), result.getData().get("recent_actions"));
    }

    private LearningAgentEvent eventWithProblem() {
        LearningAgentEvent event = new LearningAgentEvent();
        event.setUserId(17L);
        event.setProblemId(3L);
        return event;
    }
}
