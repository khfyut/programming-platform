from unittest import TestCase

from app.core.intent import IntentUnderstandingEngine
from app.schemas.context import AgentContext


def intent_context(message: str, **overrides):
    payload = {
        "request_id": "req-intent",
        "timestamp": "2026-04-24T10:00:00",
        "scene": "problem_coach",
        "action_hint": "chat",
        "trigger_source": "PROBLEM_PAGE_CHAT",
        "user_intent": "UNKNOWN",
        "user_message": message,
        "requested_full_solution": False,
        "problem": {
            "problem_id": 1,
            "title": "Two Sum",
            "difficulty": "easy",
            "knowledge_points": ["array", "hashmap"],
            "problem_content": "Return indices of two numbers that add up to target.",
            "language": "java",
        },
        "submission": None,
        "consecutive_failures": 0,
        "has_viewed_solution": False,
        "hint_count": 0,
        "diagnose_count": 0,
        "explain_count": 0,
        "recommend_count": 0,
        "reflect_count": 0,
        "learning_stage": "FIRST_TRY",
        "failure_evidence_level": "NONE",
    }
    payload.update(overrides)
    return AgentContext(**payload)


class IntentUnderstandingEngineTest(TestCase):
    def test_problem_meaning_question_maps_to_explanation_with_high_confidence(self):
        result = IntentUnderstandingEngine().understand(
            intent_context("what does this problem mean?")
        )

        self.assertEqual(result.normalized_intent, "ASK_FOR_EXPLANATION")
        self.assertGreaterEqual(result.confidence, 0.65)
        self.assertEqual(result.requested_scope, "concept_only")

    def test_failure_question_maps_to_error_analysis_when_failure_is_present(self):
        result = IntentUnderstandingEngine().understand(
            intent_context(
                "why is my answer wrong?",
                trigger_source="SUBMISSION_RESULT",
                failure_evidence_level="VERIFIED",
                consecutive_failures=1,
                submission={
                    "submit_id": 10,
                    "status": "WA",
                    "error_message": "Wrong Answer",
                    "code_content": "class Solution {}",
                },
            )
        )

        self.assertEqual(result.normalized_intent, "ASK_FOR_ERROR_ANALYSIS")
        self.assertGreaterEqual(result.confidence, 0.65)
        self.assertEqual(result.requested_scope, "partial_solution")

    def test_ambiguous_help_request_returns_low_confidence_clarification(self):
        result = IntentUnderstandingEngine().understand(intent_context("I do not know"))

        self.assertEqual(result.normalized_intent, "UNKNOWN")
        self.assertLess(result.confidence, 0.65)
        self.assertIn("explain", result.clarification_question.lower())
