from types import SimpleNamespace
from unittest import TestCase
from unittest.mock import patch

from app.main import make_rule_decision as make_decision
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
        "explain_count": 0,
        "recommend_count": 0,
        "reflect_count": 0,
        "last_action_type": None,
        "learning_stage": "FIRST_TRY",
    }
    payload.update(overrides)
    return payload


class DecisionEndpointTest(TestCase):
    def post_decision(self, payload):
        decision = make_decision(AgentContext(**payload))
        return decision.model_dump(mode="json")

    def test_problem_idea_message_without_user_intent_routes_to_guide_idea(self):
        data = self.post_decision(
            base_context(
                user_intent="UNKNOWN",
                user_message="这道题的解题思路是什么",
            )
        )

        self.assertEqual(data["action_type"], "GUIDE_IDEA")
        self.assertEqual(data["content_type"], "guidance")
        self.assertEqual(data["pedagogical_goal"], "GUIDE_INITIAL_THINKING")
        self.assertIn("哈希表", data["main_response"])
        self.assertNotIn("public class", data["main_response"])

    def test_problem_idea_request_overrides_generic_explanation_intent(self):
        data = self.post_decision(
            base_context(
                user_intent="ASK_FOR_EXPLANATION",
                user_message="请讲一下这道题的解题思路，先给方向，不要给完整代码",
            )
        )

        self.assertEqual(data["action_type"], "GUIDE_IDEA")
        self.assertEqual(data["content_type"], "guidance")
        self.assertEqual(data["pedagogical_goal"], "GUIDE_INITIAL_THINKING")
        self.assertIn("思路", data["main_response"])
        self.assertNotIn("class Solution", data["main_response"])

    def test_first_submission_failure_returns_light_hint(self):
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

    def test_repeated_submission_failure_with_hint_history_returns_diagnose(self):
        diagnosis = SimpleNamespace(
            content="这次更像是哈希表补数判断顺序导致的逻辑错误。",
            error_tag="LOGIC_ERROR",
            weak_points=["哈希表", "补数判断"],
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
        self.assertEqual(data["weak_points"], ["哈希表", "补数判断"])

    def test_repeated_agent_hint_without_new_submission_escalates_to_diagnose(self):
        diagnosis = SimpleNamespace(
            content="你已经看过提示，下一步需要定位失败发生在哪个状态更新点。",
            error_tag="LOGIC_ERROR",
            weak_points=["状态更新"],
            suggested_next_action="先找第一个失败样例，再对照每一步哈希表内容。",
        )

        with patch("app.core.content.DiagnosisEngine.diagnose", return_value=diagnosis):
            data = self.post_decision(
                base_context(
                    trigger_source="SUBMISSION_RESULT",
                    user_intent="ASK_FOR_ERROR_ANALYSIS",
                    submission={
                        "submit_id": 103,
                        "status": "WA",
                        "error_message": "WA",
                        "is_first_attempt": False,
                        "code_content": "class Solution {}",
                    },
                    consecutive_failures=1,
                    hint_count=2,
                    last_action_type="HINT",
                    learning_stage="HINTED",
                )
            )

        self.assertEqual(data["action_type"], "DIAGNOSE")
        self.assertEqual(data["content_type"], "diagnosis")

    def test_repeated_failure_after_diagnosis_escalates_to_explain(self):
        data = self.post_decision(
            base_context(
                trigger_source="SUBMISSION_RESULT",
                user_intent="ASK_FOR_ERROR_ANALYSIS",
                submission={
                    "submit_id": 102,
                    "status": "WA",
                    "error_message": "WA",
                    "is_first_attempt": False,
                    "code_content": "class Solution {}",
                },
                consecutive_failures=3,
                hint_count=1,
                diagnose_count=1,
                learning_stage="DIAGNOSED",
            )
        )

        self.assertEqual(data["action_type"], "EXPLAIN")
        self.assertEqual(data["content_type"], "explanation")
        self.assertEqual(data["pedagogical_goal"], "EXPLAIN_CONCEPT")
        self.assertIn("哈希表", data["main_response"])

    def test_viewed_solution_routes_to_reflection(self):
        data = self.post_decision(
            base_context(
                trigger_source="PROBLEM_PAGE_CHAT",
                user_intent="ASK_FOR_EXPLANATION",
                has_viewed_solution=True,
                reflect_count=0,
                last_error_tag="LOGIC_ERROR",
            )
        )

        self.assertEqual(data["action_type"], "REFLECT")
        self.assertEqual(data["content_type"], "reflection")
        self.assertEqual(data["pedagogical_goal"], "REVIEW_AFTER_SOLUTION")
        self.assertIn("复盘", data["main_response"])

    def test_learning_path_entry_routes_to_recommend(self):
        data = self.post_decision(
            base_context(
                trigger_source="LEARNING_PATH_ENTRY",
                user_intent="ASK_FOR_NEXT_STEP",
                user_message=None,
            )
        )

        self.assertEqual(data["action_type"], "RECOMMEND")
        self.assertEqual(data["content_type"], "recommendation")
        self.assertEqual(data["pedagogical_goal"], "RECOMMEND_REMEDIATION")

    def test_full_solution_request_without_trusted_failure_is_blocked(self):
        data = self.post_decision(
            base_context(
                user_intent="ASK_FOR_FULL_SOLUTION",
                user_message="直接给我完整答案",
                requested_full_solution=True,
                consecutive_failures=0,
            )
        )

        self.assertNotEqual(data["action_type"], "REVEAL_ANSWER")
        self.assertIn("REVEAL_ANSWER", data["blocked_actions"])
        self.assertIn("完整答案", " ".join(data["applied_constraints"]))
