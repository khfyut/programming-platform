package com.programming.service.problemagent;

import com.programming.entity.Problem;
import com.programming.vo.problemagent.CoachRecommendationCardVO;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProblemCoachRecommendationServiceTest {

    private final ProblemCoachRecommendationService recommendationService = new ProblemCoachRecommendationService();

    @Test
    void buildsMixedRecommendationsFromProblemLevelAndKnowledgeInputs() {
        Problem currentProblem = new Problem();
        currentProblem.setId(1L);
        currentProblem.setTitle("Two Sum");

        Problem candidateProblem = new Problem();
        candidateProblem.setId(2L);
        candidateProblem.setTitle("Valid Parentheses");
        candidateProblem.setDifficulty(1);
        candidateProblem.setKnowledgePoints("stack");

        ProblemCoachContext context = new ProblemCoachContext();
        context.setCurrentProblem(currentProblem);
        context.setRecommendedProblems(List.of(candidateProblem));
        context.setRecommendedLevels(List.of(Map.of(
                "pathId", 7L,
                "pathName", "Stack Path",
                "description", "Practice stack basics",
                "language", "java"
        )));
        context.setWeakKnowledgePoints(List.of(Map.of(
                "knowledgeId", 9L,
                "name", "Hash Table",
                "score", 35
        )));

        List<CoachRecommendationCardVO> cards = recommendationService.buildRecommendations(context, 3);

        assertEquals(3, cards.size());
        Set<String> types = cards.stream()
                .map(CoachRecommendationCardVO::getType)
                .collect(Collectors.toSet());
        assertTrue(types.contains("problem"));
        assertTrue(types.contains("level"));
        assertTrue(types.contains("knowledge_point"));
    }

    @Test
    void excludesCurrentProblemAndRespectsLimit() {
        Problem currentProblem = new Problem();
        currentProblem.setId(1L);
        currentProblem.setTitle("Current");

        Problem duplicateProblem = new Problem();
        duplicateProblem.setId(1L);
        duplicateProblem.setTitle("Current Again");

        Problem candidateProblem = new Problem();
        candidateProblem.setId(2L);
        candidateProblem.setTitle("Next");

        ProblemCoachContext context = new ProblemCoachContext();
        context.setCurrentProblem(currentProblem);
        context.setRecommendedProblems(List.of(duplicateProblem, candidateProblem));
        context.setRecommendedLevels(List.of(
                Map.of("pathId", 7L, "pathName", "Path A"),
                Map.of("pathId", 8L, "pathName", "Path B")
        ));
        context.setWeakKnowledgePoints(List.of(
                Map.of("knowledgeId", 9L, "name", "Hash Table"),
                Map.of("knowledgeId", 10L, "name", "Binary Search")
        ));

        List<CoachRecommendationCardVO> cards = recommendationService.buildRecommendations(context, 3);

        assertEquals(3, cards.size());
        assertFalse(cards.stream().anyMatch(card -> "1".equals(card.getTargetId())));
    }
}
