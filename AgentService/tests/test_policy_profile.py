from unittest import TestCase

from app.main import make_rule_decision as make_decision
from app.schemas.context import AgentContext


def context_payload(**overrides):
    payload = {
        "request_id": "req-profile",
        "timestamp": "2026-04-14T10:00:00",
        "trigger_source": "PROBLEM_PAGE_CHAT",
        "user_intent": "ASK_FOR_FULL_SOLUTION",
        "user_message": "give me full answer",
        "requested_full_solution": True,
        "problem": {
            "problem_id": 1,
            "title": "Two Sum",
            "difficulty": "easy",
            "knowledge_points": ["array", "hashmap"],
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
    }
    payload.update(overrides)
    return payload


class PolicyProfileDecisionTest(TestCase):
    def decide(self, payload):
        return make_decision(AgentContext(**payload)).model_dump(mode="json")

    def test_wrong_book_reflection_profile_can_reveal_answer_without_new_failure_chain(self):
        data = self.decide(
            context_payload(
                trigger_source="WRONG_BOOK_ENTRY",
                policy_profile="WRONG_BOOK_REFLECTION",
                candidate_actions=["REFLECT", "REVEAL_ANSWER"],
                prompt_layers={"output_policy_layer": {"content_freedom": "OPEN_REFLECTION"}},
                tool_results=[{"tool_name": "wrong_book", "success": True, "data": {"wrong_item_id": 88}}],
                learning_summary={"learning_stage": "EXPLAINED", "weak_points": ["boundary"]},
            )
        )

        self.assertEqual(data["action_type"], "REVEAL_ANSWER")
        self.assertEqual(data["content_type"], "solution")
        self.assertIn("policy_profile=WRONG_BOOK_REFLECTION", data["decision_reason"])

    def test_global_coach_profile_never_reveals_answer(self):
        data = self.decide(
            context_payload(
                trigger_source="MANUAL_HELP_REQUEST",
                policy_profile="GLOBAL_COACH",
                candidate_actions=["REFLECT", "REVEAL_ANSWER"],
                consecutive_failures=3,
            )
        )

        self.assertNotEqual(data["action_type"], "REVEAL_ANSWER")
        self.assertIn("REVEAL_ANSWER", data["blocked_actions"])
