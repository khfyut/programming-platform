package com.programming.service.problemagent;

import com.programming.entity.Problem;
import com.programming.vo.problemagent.CoachRecommendationCardVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ProblemCoachRecommendationService {

    public List<CoachRecommendationCardVO> buildRecommendations(ProblemCoachContext context, int limit) {
        int safeLimit = Math.max(limit, 0);
        List<CoachRecommendationCardVO> cards = new ArrayList<>();
        if (safeLimit == 0 || context == null) {
            return cards;
        }

        List<CoachRecommendationCardVO> problemCards = buildProblemCards(context);
        List<CoachRecommendationCardVO> levelCards = buildLevelCards(context);
        List<CoachRecommendationCardVO> knowledgeCards = buildKnowledgeCards(context);

        addFirst(cards, problemCards, safeLimit);
        addFirst(cards, levelCards, safeLimit);
        addFirst(cards, knowledgeCards, safeLimit);
        fillRemaining(cards, problemCards, safeLimit);
        fillRemaining(cards, levelCards, safeLimit);
        fillRemaining(cards, knowledgeCards, safeLimit);
        return cards;
    }

    private List<CoachRecommendationCardVO> buildProblemCards(ProblemCoachContext context) {
        List<CoachRecommendationCardVO> cards = new ArrayList<>();
        Long currentProblemId = context.getCurrentProblem() == null ? null : context.getCurrentProblem().getId();
        for (Problem problem : context.getRecommendedProblems()) {
            if (problem == null || problem.getId() == null) {
                continue;
            }
            if (currentProblemId != null && currentProblemId.equals(problem.getId())) {
                continue;
            }

            CoachRecommendationCardVO card = new CoachRecommendationCardVO();
            card.setType("problem");
            card.setTargetId(String.valueOf(problem.getId()));
            card.setTitle(problem.getTitle());
            card.setDescription(firstNonBlank(problem.getKnowledgePoints(), problem.getTags(), "Recommended problem"));
            card.setReason("Try a nearby problem to reinforce the same pattern.");
            card.setRoute("/problem/" + problem.getId());
            card.setActionType("open_recommendation");
            cards.add(card);
        }
        return cards;
    }

    private List<CoachRecommendationCardVO> buildLevelCards(ProblemCoachContext context) {
        List<CoachRecommendationCardVO> cards = new ArrayList<>();
        for (Map<String, Object> level : context.getRecommendedLevels()) {
            if (level == null) {
                continue;
            }

            Object pathId = firstNonNull(level.get("levelId"), level.get("pathId"));
            if (pathId == null) {
                continue;
            }

            CoachRecommendationCardVO card = new CoachRecommendationCardVO();
            card.setType("level");
            card.setTargetId(String.valueOf(pathId));
            card.setTitle(String.valueOf(firstNonNull(level.get("levelName"), level.get("pathName"), "Recommended module")));
            card.setDescription(String.valueOf(firstNonNull(level.get("description"), level.get("direction"), "Continue learning path practice")));
            card.setReason("This learning step can fill the gap behind the current mistake.");
            Object routeTarget = firstNonNull(level.get("route"), pathId);
            if (level.containsKey("route")) {
                card.setRoute(String.valueOf(routeTarget));
            } else {
                card.setRoute("/learn/path/" + routeTarget);
            }
            card.setActionType("open_recommendation");
            cards.add(card);
        }
        return cards;
    }

    private List<CoachRecommendationCardVO> buildKnowledgeCards(ProblemCoachContext context) {
        List<CoachRecommendationCardVO> cards = new ArrayList<>();
        for (Map<String, Object> point : context.getWeakKnowledgePoints()) {
            if (point == null) {
                continue;
            }

            Object knowledgeId = firstNonNull(point.get("knowledgeId"), point.get("id"));
            if (knowledgeId == null) {
                continue;
            }

            CoachRecommendationCardVO card = new CoachRecommendationCardVO();
            card.setType("knowledge_point");
            card.setTargetId(String.valueOf(knowledgeId));
            card.setTitle(String.valueOf(firstNonNull(point.get("name"), point.get("knowledgeName"), "Weak knowledge point")));
            card.setDescription("Review this concept before retrying the problem.");
            Object score = point.get("score");
            card.setReason(score == null
                    ? "Your recent history suggests this concept needs reinforcement."
                    : "Current mastery is around " + score + ", so strengthening it should help the next attempt.");
            card.setRoute("/learn/knowledge-graph");
            card.setActionType("open_recommendation");
            cards.add(card);
        }
        return cards;
    }

    private void addFirst(List<CoachRecommendationCardVO> target, List<CoachRecommendationCardVO> source, int limit) {
        if (target.size() >= limit || source.isEmpty()) {
            return;
        }
        target.add(source.get(0));
    }

    private void fillRemaining(List<CoachRecommendationCardVO> target, List<CoachRecommendationCardVO> source, int limit) {
        for (CoachRecommendationCardVO card : source) {
            if (target.size() >= limit) {
                return;
            }
            if (!target.contains(card)) {
                target.add(card);
            }
        }
    }

    private Object firstNonNull(Object... values) {
        for (Object value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}
