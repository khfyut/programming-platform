package com.programming.agent;

import com.programming.agent.dto.AgentCoachDecideRequestDTO;
import com.programming.agent.dto.AgentCoachDecisionDTO;
import com.programming.agent.dto.AgentCoachEventRequestDTO;
import com.programming.agent.dto.AgentCoachNudgeDTO;
import com.programming.entity.AgentCoachNudge;
import com.programming.entity.UserProblemInteraction;
import com.programming.mapper.AgentCoachNudgeMapper;
import com.programming.mapper.UserProblemInteractionMapper;
import com.programming.service.AgentCoachService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentCoachServiceTest {

    @Test
    void problemPageDwellCreatesLightGuideIdeaNudge() {
        InMemoryCoachNudgeMapper nudges = new InMemoryCoachNudgeMapper();
        AgentCoachService service = new AgentCoachService(nudges, new InMemoryUserProblemInteractionMapper());

        AgentCoachDecisionDTO decision = service.decide(17L, dwellRequest(1L, 90));

        assertTrue(decision.isCreated());
        AgentCoachNudgeDTO nudge = decision.getNudge();
        assertNotNull(nudge);
        assertEquals("PROBLEM_PAGE_DWELL", nudge.getTriggerSource());
        assertEquals("GUIDE_IDEA", nudge.getActionType());
        assertEquals("GUIDE_INITIAL_THINKING", nudge.getPedagogicalGoal());
        assertEquals("ACTIVE", nudge.getStatus());
        assertEquals("/problem/1", nudge.getTargetRoute());
        assertTrue(nudge.getSummary().contains("梳理思路"));
    }

    @Test
    void activeNudgeSuppressesDuplicateDwellReminder() {
        InMemoryCoachNudgeMapper nudges = new InMemoryCoachNudgeMapper();
        AgentCoachService service = new AgentCoachService(nudges, new InMemoryUserProblemInteractionMapper());

        AgentCoachDecisionDTO first = service.decide(17L, dwellRequest(1L, 90));
        AgentCoachDecisionDTO second = service.decide(17L, dwellRequest(1L, 120));

        assertTrue(first.isCreated());
        assertFalse(second.isCreated());
        assertEquals(first.getNudge().getId(), second.getNudge().getId());
        assertEquals(1, nudges.rows.size());
    }

    @Test
    void submitFailureUsesInteractionStateToEscalateNudgeAction() {
        InMemoryCoachNudgeMapper nudges = new InMemoryCoachNudgeMapper();
        InMemoryUserProblemInteractionMapper interactions = new InMemoryUserProblemInteractionMapper();
        interactions.rows.add(interaction(17L, 1L, 2, 1, 0, "HINTED"));
        AgentCoachService service = new AgentCoachService(nudges, interactions);

        AgentCoachDecideRequestDTO request = new AgentCoachDecideRequestDTO();
        request.setTriggerSource("SUBMIT_FAILED");
        request.setProblemId(1L);
        request.setCurrentRoute("/problem/1");

        AgentCoachDecisionDTO decision = service.decide(17L, request);

        assertTrue(decision.isCreated());
        assertEquals("DIAGNOSE", decision.getNudge().getActionType());
        assertEquals("DIAGNOSE_ERROR_CAUSE", decision.getNudge().getPedagogicalGoal());
    }

    @Test
    void snoozedNudgeBlocksSameReminderUntilCooldownExpires() {
        InMemoryCoachNudgeMapper nudges = new InMemoryCoachNudgeMapper();
        AgentCoachService service = new AgentCoachService(nudges, new InMemoryUserProblemInteractionMapper());

        AgentCoachDecisionDTO first = service.decide(17L, dwellRequest(1L, 90));
        AgentCoachEventRequestDTO event = new AgentCoachEventRequestDTO();
        event.setNudgeId(first.getNudge().getId());
        event.setEventType("SNOOZED");
        AgentCoachNudgeDTO snoozed = service.recordEvent(17L, event);

        AgentCoachDecisionDTO second = service.decide(17L, dwellRequest(1L, 120));

        assertEquals("SNOOZED", snoozed.getStatus());
        assertNotNull(snoozed.getExpiresAt());
        assertFalse(second.isCreated());
        assertNull(second.getNudge());
        assertEquals(1, nudges.rows.size());
    }

    @Test
    void openingPanelDoesNotConsumeActiveNudge() {
        InMemoryCoachNudgeMapper nudges = new InMemoryCoachNudgeMapper();
        AgentCoachService service = new AgentCoachService(nudges, new InMemoryUserProblemInteractionMapper());

        AgentCoachDecisionDTO first = service.decide(17L, dwellRequest(1L, 90));
        AgentCoachEventRequestDTO event = new AgentCoachEventRequestDTO();
        event.setNudgeId(first.getNudge().getId());
        event.setEventType("OPENED");
        AgentCoachNudgeDTO opened = service.recordEvent(17L, event);

        assertEquals("ACTIVE", opened.getStatus());
        assertEquals(1, service.getState(17L).getUnreadCount());
    }

    @Test
    void wrongBookDetailEntryTargetsReflectionDetailPage() {
        InMemoryCoachNudgeMapper nudges = new InMemoryCoachNudgeMapper();
        AgentCoachService service = new AgentCoachService(nudges, new InMemoryUserProblemInteractionMapper());
        AgentCoachDecideRequestDTO request = new AgentCoachDecideRequestDTO();
        request.setTriggerSource("WRONG_BOOK_DETAIL_ENTRY");
        request.setPageType("WRONG_BOOK_DETAIL");
        request.setWrongItemId(88L);
        request.setCurrentRoute("/wrong-book/88");

        AgentCoachDecisionDTO decision = service.decide(17L, request);

        assertTrue(decision.isCreated());
        assertEquals("REFLECT", decision.getNudge().getActionType());
        assertEquals("/wrong-book/88?coach=reflection", decision.getNudge().getTargetRoute());
    }

    @Test
    void pageEntryOnProblemListCreatesLightActiveSuggestion() {
        InMemoryCoachNudgeMapper nudges = new InMemoryCoachNudgeMapper();
        AgentCoachService service = new AgentCoachService(nudges, new InMemoryUserProblemInteractionMapper());
        AgentCoachDecideRequestDTO request = new AgentCoachDecideRequestDTO();
        request.setTriggerSource("PAGE_ENTRY");
        request.setPageType("PROBLEM_LIST");
        request.setCurrentRoute("/problems");

        AgentCoachDecisionDTO decision = service.decide(17L, request);

        assertTrue(decision.isCreated());
        assertEquals("PAGE_ENTRY", decision.getNudge().getTriggerSource());
        assertEquals("RECOMMEND", decision.getNudge().getActionType());
        assertEquals("/problems", decision.getNudge().getTargetRoute());
        assertTrue(decision.getNudge().getSummary().contains("题"));
    }

    private AgentCoachDecideRequestDTO dwellRequest(Long problemId, int dwellSeconds) {
        AgentCoachDecideRequestDTO request = new AgentCoachDecideRequestDTO();
        request.setTriggerSource("PROBLEM_PAGE_DWELL");
        request.setProblemId(problemId);
        request.setCurrentRoute("/problem/" + problemId);
        request.setDwellSeconds(dwellSeconds);
        return request;
    }

    private UserProblemInteraction interaction(Long userId, Long problemId, int failures, int hints,
                                               int diagnoses, String stage) {
        UserProblemInteraction interaction = new UserProblemInteraction();
        interaction.setUserId(userId);
        interaction.setProblemId(problemId);
        interaction.setConsecutiveFailures(failures);
        interaction.setHintCount(hints);
        interaction.setDiagnoseCount(diagnoses);
        interaction.setLearningStage(stage);
        return interaction;
    }

    private static class InMemoryCoachNudgeMapper implements AgentCoachNudgeMapper {
        private final List<AgentCoachNudge> rows = new ArrayList<>();
        private long nextId = 1L;

        @Override
        public int insert(AgentCoachNudge nudge) {
            nudge.setId(nextId++);
            rows.add(nudge);
            return 1;
        }

        @Override
        public AgentCoachNudge findByIdAndUser(Long id, Long userId) {
            return rows.stream()
                    .filter(row -> row.getId().equals(id) && row.getUserId().equals(userId))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<AgentCoachNudge> findActiveByUser(Long userId, LocalDateTime now, Integer limit) {
            return rows.stream()
                    .filter(row -> row.getUserId().equals(userId))
                    .filter(row -> "ACTIVE".equals(row.getStatus()))
                    .filter(row -> row.getExpiresAt() == null || row.getExpiresAt().isAfter(now))
                    .sorted(Comparator.comparing(AgentCoachNudge::getCreatedAt).reversed())
                    .limit(limit == null ? rows.size() : limit)
                    .toList();
        }

        @Override
        public List<AgentCoachNudge> findRecentSimilar(Long userId, String triggerSource, String actionType,
                                                       LocalDateTime after) {
            return rows.stream()
                    .filter(row -> row.getUserId().equals(userId))
                    .filter(row -> Objects.equals(row.getTriggerSource(), triggerSource))
                    .filter(row -> Objects.equals(row.getActionType(), actionType))
                    .filter(row -> row.getCreatedAt().isAfter(after))
                    .toList();
        }

        @Override
        public int updateStatus(Long id, Long userId, String status, LocalDateTime updatedAt,
                                LocalDateTime expiresAt) {
            AgentCoachNudge row = findByIdAndUser(id, userId);
            if (row == null) {
                return 0;
            }
            row.setStatus(status);
            row.setUpdatedAt(updatedAt);
            row.setExpiresAt(expiresAt);
            return 1;
        }
    }

    private static class InMemoryUserProblemInteractionMapper implements UserProblemInteractionMapper {
        private final List<UserProblemInteraction> rows = new ArrayList<>();

        @Override
        public UserProblemInteraction findByUserAndProblem(Long userId, Long problemId) {
            return rows.stream()
                    .filter(row -> row.getUserId().equals(userId) && row.getProblemId().equals(problemId))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public int insert(UserProblemInteraction interaction) {
            rows.add(interaction);
            return 1;
        }

        @Override
        public int update(UserProblemInteraction interaction) {
            return 1;
        }

        @Override
        public int incrementHintCount(Long userId, Long problemId) {
            return 0;
        }

        @Override
        public int incrementDiagnoseCount(Long userId, Long problemId) {
            return 0;
        }

        @Override
        public int incrementExplainCount(Long userId, Long problemId) {
            return 0;
        }

        @Override
        public int incrementRecommendCount(Long userId, Long problemId) {
            return 0;
        }

        @Override
        public int incrementReflectCount(Long userId, Long problemId) {
            return 0;
        }

        @Override
        public int updateConsecutiveFailures(Long userId, Long problemId, Integer failures) {
            return 0;
        }

        @Override
        public int updateLearningState(Long userId, Long problemId, String errorTag, String weakPoints,
                                       String learningStage) {
            return 0;
        }

        @Override
        public int updateActionSemanticState(Long userId, Long problemId, String lastActionType,
                                             String lastGoal, String lastGuidanceType, String learningStage) {
            return 0;
        }

        @Override
        public List<UserProblemInteraction> findRecentByUser(Long userId, Integer limit) {
            return rows.stream()
                    .filter(row -> row.getUserId().equals(userId))
                    .limit(limit == null ? rows.size() : limit)
                    .toList();
        }
    }
}
