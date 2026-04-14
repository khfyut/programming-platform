from types import SimpleNamespace
from unittest import TestCase
from unittest.mock import patch

from app.main import make_decision
from app.schemas.context import AgentContext


def base_context(**overrides):
    payload = {
        "request_id": "req-test",
        "timestamp": "2026-04-14T10:00:00",
        "trigger_source": "PROBLEM_PAGE_CHAT",
        "user_intent": "ASK_PROBLEM_SOLVING_IDEA",
        "user_message": "这道题的解题思路是什么",
        "requested_full_solution": False,
        "problem": {
            "problem_id": 1,
            "title": "两数之和",
            "difficulty": "easy",
            "knowledge_points": ["数组", "哈希表"],
            "language": "java",
            "hint_shown_count": 0,
            "diagnosed_count": 0,
            "problem_content": "给定整数数组 nums 和目标值 target，请返回两个数的下标。",
            "hints": "遍历时先查找 target - nums[i] 是否已经出现，再记录当前数字。",
            "sample_explanation": "因为 nums[0] + nums[1] = 9，所以返回 [0,1]。",
            "tags": "数组,哈希表",
        },
        "submission": None,
        "consecutive_failures": 0,
        "has_viewed_solution": False,
        "hint_count": 0,
        "diagnose_count": 0,
        "learning_stage": "FIRST_TRY",
    }
    payload.update(overrides)
    return payload


class DecisionEndpointTest(TestCase):
    def post_decision(self, payload):
        decision = make_decision(AgentContext(**payload))
        return decision.model_dump(mode="json")

    def test_chinese_problem_idea_message_without_user_intent_routes_to_guidance(self):
        data = self.post_decision(
            base_context(
                user_intent="UNKNOWN",
                user_message="这道题的解题思路是什么",
                problem={
                    "problem_id": 1,
                    "title": "两数之和",
                    "difficulty": "easy",
                    "knowledge_points": ["数组", "哈希表"],
                    "language": "java",
                    "hint_shown_count": 0,
                    "diagnosed_count": 0,
                },
            )
        )

        self.assertEqual(data["action_type"], "GUIDE_IDEA")
        self.assertEqual(data["content_type"], "guidance")
        self.assertEqual(data["pedagogical_goal"], "GUIDE_INITIAL_THINKING")
        self.assertNotIn("public class", data["main_response"])

    def test_problem_idea_before_submit_returns_guidance_without_solution(self):
        data = self.post_decision(base_context())

        self.assertEqual(data["action_type"], "GUIDE_IDEA")
        self.assertEqual(data["content_type"], "guidance")
        self.assertEqual(data["pedagogical_goal"], "GUIDE_INITIAL_THINKING")
        self.assertIn("哈希", data["main_response"])
        self.assertNotIn("public class", data["main_response"])

    def test_first_failure_returns_hint_content_type(self):
        data = self.post_decision(
            base_context(
                trigger_source="SUBMISSION_RESULT",
                user_intent="ASK_FOR_ERROR_ANALYSIS",
                user_message="为什么错了",
                submission={
                    "submit_id": 100,
                    "status": "WA",
                    "error_message": "WA",
                    "is_first_attempt": False,
                    "code_content": "class Solution {}",
                },
                consecutive_failures=1,
            )
        )

        self.assertEqual(data["action_type"], "HINT")
        self.assertEqual(data["content_type"], "hint")
        self.assertEqual(data["pedagogical_goal"], "GIVE_LIGHT_HINT")
        self.assertIn("problem:1:hints", data["used_knowledge_refs"])
        self.assertNotIn("public class", data["main_response"])

    def test_diagnose_content_type_matches_action(self):
        diagnosis = SimpleNamespace(
            content="这次更像是哈希表补数判断顺序导致的逻辑错误。",
            error_tag="LOGIC_ERROR",
            weak_points=["哈希表", "补数"],
            suggested_next_action="先检查是否在放入当前数之前查找补数。",
        )

        with patch("app.core.content.DiagnosisEngine.diagnose", return_value=diagnosis):
            data = self.post_decision(
                base_context(
                    trigger_source="SUBMISSION_RESULT",
                    user_intent="ASK_FOR_ERROR_ANALYSIS",
                    submission={
                        "submit_id": 101,
                        "status": "WA",
                        "error_message": "WA",
                        "is_first_attempt": False,
                        "code_content": "class Solution {}",
                    },
                    consecutive_failures=2,
                    hint_count=1,
                    diagnose_count=0,
                    learning_stage="HINTED",
                )
            )

        self.assertEqual(data["action_type"], "DIAGNOSE")
        self.assertEqual(data["content_type"], "diagnosis")
        self.assertEqual(data["pedagogical_goal"], "DIAGNOSE_ERROR_CAUSE")
        self.assertEqual(data["error_tag"], "LOGIC_ERROR")
        self.assertEqual(data["weak_points"], ["哈希表", "补数"])
        self.assertTrue(
            any(ref.startswith("template:error:") or ref.startswith("problem:1:") for ref in data["used_knowledge_refs"])
        )

    def test_explain_uses_teaching_template_refs(self):
        data = self.post_decision(
            base_context(
                trigger_source="PROBLEM_PAGE_CHAT",
                user_intent="ASK_FOR_EXPLANATION",
                user_message="讲解一下哈希表",
                consecutive_failures=0,
            )
        )

        self.assertEqual(data["action_type"], "EXPLAIN")
        self.assertEqual(data["content_type"], "explanation")
        self.assertEqual(data["pedagogical_goal"], "EXPLAIN_CONCEPT")
        self.assertIn("哈希", data["main_response"])
        self.assertIn("template:teaching:hashmap", data["used_knowledge_refs"])
