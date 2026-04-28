from unittest import TestCase
from unittest.mock import patch

from app.core.llm_decision import LlmDecisionEngine
from app.main import make_decision
from app.schemas.context import AgentContext


def context_payload(**overrides):
    payload = {
        "request_id": "req-v2",
        "timestamp": "2026-04-20T10:00:00",
        "scene": "problem_coach",
        "action_hint": "ask_followup",
        "trigger_source": "PROBLEM_PAGE_CHAT",
        "user_intent": "UNKNOWN",
        "user_message": "这道题目是什么",
        "requested_full_solution": False,
        "problem": {
            "problem_id": 1,
            "title": "两数之和",
            "difficulty": "easy",
            "knowledge_points": ["array", "hashmap"],
            "problem_content": "给定整数数组 nums 和目标值 target，请返回两个数的下标。",
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
        "conversation_history": [
            {"role": "user", "content": "帮我看看这题", "kind": "chat"},
            {"role": "assistant", "content": "先读题意。", "kind": "chat"},
        ],
        "reduced_context": {
            "required_context": {
                "scene": "problem_coach",
                "latest_user_message": "这道题目是什么",
            },
            "conditional_context": {
                "problem_title": "两数之和",
            },
            "derived_summary": {
                "problem_summary": "从数组中找两个数，使它们和为 target，并返回下标。",
            },
            "selected_signals": ["PROBLEM_CONTENT_PRESENT"],
            "dropped_signals": ["CODE_OMITTED_NOT_DEBUG_REQUEST"],
        },
        "failure_evidence_level": "NONE",
        "failure_signals": {
            "context": [],
            "strong": [],
            "weak": [],
        },
    }
    payload.update(overrides)
    return payload


def llm_json(action_type="EXPLAIN", answer_scope="concept_only"):
    return f"""
{{
  "decision": {{
    "action_type": "{action_type}",
    "user_intent": "ASK_PROBLEM_STATEMENT",
    "teaching_goal": "clarify_problem",
    "requested_scope": "concept_only",
    "answer_scope": "{answer_scope}",
    "risk_level": "LOW",
    "confidence": 0.82,
    "used_tool_signals": ["PROBLEM_CONTENT_PRESENT"]
  }},
  "content_plan": {{
    "key_points": ["说明题目输入输出", "不展开完整代码"],
    "quality_flags": []
  }},
  "final_response": {{
    "main_response": "这道题是两数之和：给定数组 nums 和目标 target，找出两个下标，使对应两个数相加等于 target。",
    "next_suggestion": "你可以先用一个小样例手推需要保存哪些状态。",
    "weak_points": ["题意建模"],
    "error_tag": null
  }}
}}
"""


class LlmDecisionV2Test(TestCase):
    def decide(self, payload):
        return make_decision(AgentContext(**payload)).model_dump(mode="json")

    def test_unavailable_llm_is_reported_separately_from_timeout(self):
        class UnavailableClient:
            last_error_tag = "LLM_UNAVAILABLE"

            def generate(self, *args, **kwargs):
                return None

        decision = LlmDecisionEngine(client=UnavailableClient()).decide(AgentContext(**context_payload()))
        data = decision.model_dump(mode="json")

        self.assertEqual(data["error_tag"], "LLM_UNAVAILABLE")
        self.assertIn("Ollama", data["main_response"])

    @patch("app.core.llm_decision.OllamaClient.generate")
    def test_low_confidence_intent_short_circuits_to_clarification(self, generate):
        data = self.decide(context_payload(user_message="I do not know", user_intent="UNKNOWN"))

        self.assertEqual(generate.call_count, 0)
        self.assertEqual(data["action_type"], "CLARIFY_INTENT")
        self.assertEqual(data["content_type"], "clarification")
        self.assertEqual(data["normalized_intent"], "UNKNOWN")
        self.assertLess(data["intent_confidence"], 0.65)
        self.assertFalse(data["answered_user_question"])

    @patch("app.core.llm_decision.OllamaClient.generate")
    def test_valid_schema_v2_maps_to_compatible_decision_fields(self, generate):
        generate.return_value = llm_json()

        data = self.decide(context_payload())

        self.assertEqual(data["action_type"], "EXPLAIN")
        self.assertEqual(data["content_type"], "explanation")
        self.assertEqual(data["user_intent"], "ASK_PROBLEM_STATEMENT")
        self.assertEqual(data["normalized_intent"], "ASK_FOR_EXPLANATION")
        self.assertGreaterEqual(data["intent_confidence"], 0.65)
        self.assertTrue(data["answered_user_question"])
        self.assertIn("directly", data["alignment_reason"])
        self.assertEqual(data["teaching_goal"], "clarify_problem")
        self.assertEqual(data["answer_scope"], "concept_only")
        self.assertEqual(data["risk_level"], "LOW")
        self.assertEqual(data["used_tool_signals"], ["PROBLEM_CONTENT_PRESENT"])
        self.assertIn("两数之和", data["main_response"])
        self.assertIn("小样例", data["next_suggestion"])

    @patch("app.core.llm_decision.OllamaClient.generate")
    def test_invalid_json_triggers_one_repair_call(self, generate):
        generate.side_effect = [
            "我不是 JSON",
            llm_json(action_type="HINT", answer_scope="hint_only"),
        ]

        data = self.decide(context_payload(action_hint="hint", user_message="给我一点提示"))

        self.assertEqual(generate.call_count, 2)
        self.assertEqual(data["action_type"], "HINT")
        self.assertEqual(data["answer_scope"], "hint_only")

    @patch("app.core.llm_decision.OllamaClient.generate")
    def test_invalid_action_after_repair_returns_invalid_json_failure(self, generate):
        generate.side_effect = [
            llm_json(action_type="SOLVE_IT", answer_scope="full_solution"),
            llm_json(action_type="SOLVE_IT", answer_scope="full_solution"),
        ]

        data = self.decide(context_payload(user_message="直接帮我做完"))

        self.assertEqual(generate.call_count, 2)
        self.assertEqual(data["error_tag"], "INVALID_JSON")
        self.assertEqual(data["action_type"], "EXPLAIN")
        self.assertEqual(data["answer_scope"], "concept_only")
        self.assertIn("暂时无法生成稳定的结构化决策", data["main_response"])

    @patch("app.core.llm_decision.OllamaClient.generate")
    def test_prompt_receives_scene_action_hint_reduced_context_and_failure_signals(self, generate):
        generate.return_value = llm_json(action_type="RECOMMEND", answer_scope="hint_only")

        self.decide(
            context_payload(
                scene="wrong_book",
                action_hint="similar_problem",
                failure_evidence_level="VERIFIED",
                failure_signals={"context": ["WRONG_BOOK_RECORD"], "strong": [], "weak": []},
            )
        )

        prompt = generate.call_args.args[0]
        self.assertIn('"scene": "wrong_book"', prompt)
        self.assertIn('"action_hint": "similar_problem"', prompt)
        self.assertIn('"failure_evidence_level": "VERIFIED"', prompt)
        self.assertIn("WRONG_BOOK_RECORD", prompt)
        self.assertIn("derived_summary", prompt)

    @patch("app.core.llm_decision.OllamaClient.generate")
    def test_global_guide_uses_llm_for_system_guide_without_deep_solution(self, generate):
        generate.return_value = llm_json(action_type="EXPLAIN", answer_scope="concept_only")

        data = self.decide(
            context_payload(
                scene="global_guide",
                policy_profile="GLOBAL_GUIDE",
                trigger_source="GLOBAL_GUIDE_CHAT",
                user_message="这个系统怎么用",
                candidate_actions=["EXPLAIN", "RECOMMEND", "CLARIFY_INTENT"],
            )
        )

        self.assertEqual(data["action_type"], "EXPLAIN")
        self.assertEqual(data["content_type"], "explanation")
        self.assertEqual(data["answer_scope"], "concept_only")
        prompt = generate.call_args.args[0]
        self.assertIn('"scene": "global_guide"', prompt)
        self.assertIn('"policy_profile": "GLOBAL_GUIDE"', prompt)

    @patch("app.core.llm_decision.OllamaClient.generate")
    def test_global_guide_rejects_reveal_answer_even_if_model_requests_it(self, generate):
        generate.side_effect = [
            llm_json(action_type="REVEAL_ANSWER", answer_scope="full_solution"),
            llm_json(action_type="REVEAL_ANSWER", answer_scope="full_solution"),
        ]

        data = self.decide(
            context_payload(
                scene="global_guide",
                policy_profile="GLOBAL_GUIDE",
                trigger_source="GLOBAL_GUIDE_CHAT",
                user_message="直接给我答案",
                requested_full_solution=True,
                candidate_actions=["EXPLAIN", "RECOMMEND", "CLARIFY_INTENT"],
            )
        )

        self.assertNotEqual(data["action_type"], "REVEAL_ANSWER")
        self.assertEqual(data["answer_scope"], "concept_only")
