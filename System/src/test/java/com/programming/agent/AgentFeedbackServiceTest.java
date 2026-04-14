package com.programming.agent;

import com.programming.agent.dto.AgentFeedbackRequestDTO;
import com.programming.entity.AgentFeedbackLog;
import com.programming.entity.LearningEventLog;
import com.programming.mapper.AgentFeedbackLogMapper;
import com.programming.mapper.LearningEventLogMapper;
import com.programming.service.AgentFeedbackService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AgentFeedbackServiceTest {

    @Test
    void acceptsKnownFeedbackTypeAndWritesLog() {
        InMemoryLearningEventLogMapper learningEvents = new InMemoryLearningEventLogMapper();
        InMemoryAgentFeedbackLogMapper feedbackLogs = new InMemoryAgentFeedbackLogMapper();
        learningEvents.events.add(event(1L, 10L, 20L, "req-1", "HINT"));
        AgentFeedbackService service = new AgentFeedbackService(learningEvents, feedbackLogs);

        AgentFeedbackLog log = service.recordFeedback(1L, request("req-1", 10L, 20L, "HINT", "accepted"));

        assertEquals("accepted", log.getFeedbackType());
        assertEquals("req-1", log.getRequestId());
        assertEquals(1, feedbackLogs.logs.size());
        assertEquals("accepted", feedbackLogs.logs.get(0).getFeedbackType());
    }

    @Test
    void acceptsAllKnownFeedbackTypes() {
        List<String> feedbackTypes = List.of(
                "accepted",
                "ignored",
                "asked_followup",
                "revealed_solution",
                "applied_draft",
                "solved_after_action"
        );

        for (String feedbackType : feedbackTypes) {
            InMemoryLearningEventLogMapper learningEvents = new InMemoryLearningEventLogMapper();
            InMemoryAgentFeedbackLogMapper feedbackLogs = new InMemoryAgentFeedbackLogMapper();
            learningEvents.events.add(event(1L, 10L, 20L, "req-" + feedbackType, "HINT"));
            AgentFeedbackService service = new AgentFeedbackService(learningEvents, feedbackLogs);

            AgentFeedbackLog log = service.recordFeedback(
                    1L,
                    request("req-" + feedbackType, 10L, 20L, "HINT", feedbackType)
            );

            assertEquals(feedbackType, log.getFeedbackType());
            assertEquals(1, feedbackLogs.logs.size());
        }
    }

    @Test
    void rejectsInvalidFeedbackType() {
        InMemoryLearningEventLogMapper learningEvents = new InMemoryLearningEventLogMapper();
        InMemoryAgentFeedbackLogMapper feedbackLogs = new InMemoryAgentFeedbackLogMapper();
        learningEvents.events.add(event(1L, 10L, 20L, "req-1", "HINT"));
        AgentFeedbackService service = new AgentFeedbackService(learningEvents, feedbackLogs);

        RuntimeException error = assertThrows(
                RuntimeException.class,
                () -> service.recordFeedback(1L, request("req-1", 10L, 20L, "HINT", "bad_feedback"))
        );

        assertEquals("Invalid feedback_type", error.getMessage());
        assertEquals(0, feedbackLogs.logs.size());
    }

    @Test
    void rejectsCrossUserRequestId() {
        InMemoryLearningEventLogMapper learningEvents = new InMemoryLearningEventLogMapper();
        InMemoryAgentFeedbackLogMapper feedbackLogs = new InMemoryAgentFeedbackLogMapper();
        learningEvents.events.add(event(2L, 10L, 20L, "req-1", "HINT"));
        AgentFeedbackService service = new AgentFeedbackService(learningEvents, feedbackLogs);

        RuntimeException error = assertThrows(
                RuntimeException.class,
                () -> service.recordFeedback(1L, request("req-1", 10L, 20L, "HINT", "accepted"))
        );

        assertEquals("Agent event not found", error.getMessage());
        assertEquals(0, feedbackLogs.logs.size());
    }

    @Test
    void rejectsCrossProblemFeedback() {
        InMemoryLearningEventLogMapper learningEvents = new InMemoryLearningEventLogMapper();
        InMemoryAgentFeedbackLogMapper feedbackLogs = new InMemoryAgentFeedbackLogMapper();
        learningEvents.events.add(event(1L, 10L, 20L, "req-1", "HINT"));
        AgentFeedbackService service = new AgentFeedbackService(learningEvents, feedbackLogs);

        RuntimeException error = assertThrows(
                RuntimeException.class,
                () -> service.recordFeedback(1L, request("req-1", 11L, 20L, "HINT", "accepted"))
        );

        assertEquals("Feedback problem_id does not match Agent event", error.getMessage());
        assertEquals(0, feedbackLogs.logs.size());
    }

    @Test
    void rejectsCrossSubmitFeedback() {
        InMemoryLearningEventLogMapper learningEvents = new InMemoryLearningEventLogMapper();
        InMemoryAgentFeedbackLogMapper feedbackLogs = new InMemoryAgentFeedbackLogMapper();
        learningEvents.events.add(event(1L, 10L, 20L, "req-1", "HINT"));
        AgentFeedbackService service = new AgentFeedbackService(learningEvents, feedbackLogs);

        RuntimeException error = assertThrows(
                RuntimeException.class,
                () -> service.recordFeedback(1L, request("req-1", 10L, 21L, "HINT", "accepted"))
        );

        assertEquals("Feedback submit_id does not match Agent event", error.getMessage());
        assertEquals(0, feedbackLogs.logs.size());
    }

    @Test
    void acceptsLearningPathFeedbackWithoutProblemIdAndPreservesScope() {
        InMemoryLearningEventLogMapper learningEvents = new InMemoryLearningEventLogMapper();
        InMemoryAgentFeedbackLogMapper feedbackLogs = new InMemoryAgentFeedbackLogMapper();
        LearningEventLog event = event(1L, null, null, "req-path", "RECOMMEND");
        event.setEntryRefType("LEARNING_PATH_LEVEL");
        event.setEntryRefId(5L);
        event.setPathId(4L);
        event.setLevelId(5L);
        learningEvents.events.add(event);
        AgentFeedbackService service = new AgentFeedbackService(learningEvents, feedbackLogs);

        AgentFeedbackRequestDTO request = request("req-path", null, null, "RECOMMEND", "accepted");
        request.setEntryRefType("LEARNING_PATH_LEVEL");
        request.setEntryRefId(5L);
        request.setPathId(4L);
        request.setLevelId(5L);

        AgentFeedbackLog log = service.recordFeedback(1L, request);

        assertEquals("LEARNING_PATH_LEVEL", log.getEntryRefType());
        assertEquals(5L, log.getEntryRefId());
        assertEquals(4L, log.getPathId());
        assertEquals(5L, log.getLevelId());
        assertEquals(1, feedbackLogs.logs.size());
    }

    @Test
    void rejectsLearningPathFeedbackScopeMismatch() {
        InMemoryLearningEventLogMapper learningEvents = new InMemoryLearningEventLogMapper();
        InMemoryAgentFeedbackLogMapper feedbackLogs = new InMemoryAgentFeedbackLogMapper();
        LearningEventLog event = event(1L, null, null, "req-path", "RECOMMEND");
        event.setEntryRefType("LEARNING_PATH_LEVEL");
        event.setEntryRefId(5L);
        event.setPathId(4L);
        event.setLevelId(5L);
        learningEvents.events.add(event);
        AgentFeedbackService service = new AgentFeedbackService(learningEvents, feedbackLogs);

        AgentFeedbackRequestDTO request = request("req-path", null, null, "RECOMMEND", "accepted");
        request.setEntryRefType("LEARNING_PATH_LEVEL");
        request.setEntryRefId(5L);
        request.setPathId(4L);
        request.setLevelId(6L);

        RuntimeException error = assertThrows(
                RuntimeException.class,
                () -> service.recordFeedback(1L, request)
        );

        assertEquals("Feedback level_id does not match Agent event", error.getMessage());
        assertEquals(0, feedbackLogs.logs.size());
    }

    @Test
    void feedbackServiceDoesNotDependOnLearningStagePersistence() {
        boolean hasInteractionMapper = Arrays.stream(AgentFeedbackService.class.getDeclaredFields())
                .anyMatch(field -> field.getType().getName().contains("UserProblemInteractionMapper"));

        assertFalse(hasInteractionMapper);
    }

    private AgentFeedbackRequestDTO request(String requestId, Long problemId, Long submitId,
                                            String actionType, String feedbackType) {
        AgentFeedbackRequestDTO request = new AgentFeedbackRequestDTO();
        request.setRequestId(requestId);
        request.setProblemId(problemId);
        request.setSubmitId(submitId);
        request.setActionType(actionType);
        request.setFeedbackType(feedbackType);
        request.setMetadata(Map.of("source", "test"));
        return request;
    }

    private LearningEventLog event(Long userId, Long problemId, Long submitId, String requestId, String actionType) {
        LearningEventLog event = new LearningEventLog();
        event.setUserId(userId);
        event.setProblemId(problemId);
        event.setSubmitId(submitId);
        event.setRequestId(requestId);
        event.setActionType(actionType);
        event.setCreateTime(LocalDateTime.now());
        return event;
    }

    private static class InMemoryLearningEventLogMapper implements LearningEventLogMapper {
        private final List<LearningEventLog> events = new ArrayList<>();

        @Override
        public int insert(LearningEventLog log) {
            events.add(log);
            return 1;
        }

        @Override
        public List<LearningEventLog> findByUserAndProblem(Long userId, Long problemId) {
            return events.stream()
                    .filter(event -> event.getUserId().equals(userId) && Objects.equals(event.getProblemId(), problemId))
                    .toList();
        }

        @Override
        public List<LearningEventLog> findBySubmitId(Long submitId) {
            return events.stream()
                    .filter(event -> event.getSubmitId() != null && event.getSubmitId().equals(submitId))
                    .toList();
        }

        @Override
        public LearningEventLog findByRequestIdAndUser(String requestId, Long userId) {
            return events.stream()
                    .filter(event -> event.getRequestId().equals(requestId) && event.getUserId().equals(userId))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<LearningEventLog> findRecentByUser(Long userId, Integer limit) {
            return events.stream()
                    .filter(event -> event.getUserId().equals(userId))
                    .limit(limit == null ? events.size() : limit)
                    .toList();
        }
    }

    private static class InMemoryAgentFeedbackLogMapper implements AgentFeedbackLogMapper {
        private final List<AgentFeedbackLog> logs = new ArrayList<>();

        @Override
        public int insert(AgentFeedbackLog log) {
            logs.add(log);
            return 1;
        }

        @Override
        public List<AgentFeedbackLog> findByRequestId(String requestId) {
            return logs.stream()
                    .filter(log -> log.getRequestId().equals(requestId))
                    .toList();
        }

        @Override
        public List<AgentFeedbackLog> findRecentByUser(Long userId, Integer limit) {
            return logs.stream()
                    .filter(log -> log.getUserId().equals(userId))
                    .limit(limit == null ? logs.size() : limit)
                    .toList();
        }
    }
}
