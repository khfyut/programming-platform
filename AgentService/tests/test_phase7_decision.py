from unittest import TestCase

from app.main import make_rule_decision as make_decision
from app.schemas.context import AgentContext


def phase7_context(**overrides):
    payload = {
        "request_id": "req-phase7",
        "timestamp": "2026-04-14T10:00:00",
        "trigger_source": "WRONG_BOOK_ENTRY",
        "user_intent": "UNKNOWN",
        "user_message": None,
        "requested_full_solution": False,
        "problem": {
            "problem_id": 1,
            "title": "Two Sum",
            "difficulty": "easy",
            "knowledge_points": ["array", "hashmap"],
            "language": "java",
            "problem_content": "Find two numbers that add up to target.",
            "hints": "Check the complement before storing the current number.",
            "sample_explanation": "nums[0] + nums[1] = target.",
            "tags": "array,hashmap",
        },
        "submission": {
            "submit_id": 10,
            "status": "WA",
            "error_message": "Wrong Answer",
            "is_first_attempt": False,
            "code_content": "class Solution {}",
        },
        "consecutive_failures": 1,
        "has_viewed_solution": True,
        "hint_count": 1,
        "diagnose_count": 1,
        "explain_count": 1,
        "recommend_count": 0,
        "reflect_count": 0,
        "last_error_tag": "LOGIC_ERROR",
        "weak_points": ["hashmap", "complement"],
        "learning_stage": "EXPLAINED",
    }
    payload.update(overrides)
    return payload


class Phase7DecisionTest(TestCase):
    def post_decision(self, payload):
        return make_decision(AgentContext(**payload)).model_dump(mode="json")

    def test_wrong_book_entry_routes_to_reflection(self):
        data = self.post_decision(phase7_context())

        self.assertEqual(data["action_type"], "REFLECT")
        self.assertEqual(data["content_type"], "reflection")
        self.assertEqual(data["pedagogical_goal"], "REVIEW_AFTER_SOLUTION")
        self.assertIn("错误", data["main_response"])
        self.assertIn("下次", data["main_response"])

    def test_learning_path_entry_routes_to_recommendation(self):
        data = self.post_decision(
            phase7_context(
                trigger_source="LEARNING_PATH_ENTRY",
                user_intent="UNKNOWN",
                submission=None,
                consecutive_failures=0,
                has_viewed_solution=False,
                recommend_count=0,
                weak_points=["array", "hashmap"],
                learning_stage="FIRST_TRY",
            )
        )

        self.assertEqual(data["action_type"], "RECOMMEND")
        self.assertEqual(data["content_type"], "recommendation")
        self.assertEqual(data["pedagogical_goal"], "RECOMMEND_REMEDIATION")
        self.assertIn("推荐", data["main_response"])
        self.assertIn("复习", data["main_response"])
