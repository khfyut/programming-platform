import json
import os
from datetime import datetime
from time import monotonic
from typing import Any

from app.core.content import CONTENT_TYPE_BY_ACTION, GOAL_BY_ACTION
from app.core.intent import INTENT_CONFIDENCE_THRESHOLD, IntentUnderstanding, IntentUnderstandingEngine
from app.core.llm_client import OllamaClient
from app.schemas.context import AgentContext
from app.schemas.decision import AgentDecision, CandidateAction


ALLOWED_ACTIONS = set(CONTENT_TYPE_BY_ACTION.keys())
ANSWER_SCOPE_ORDER = {
    "concept_only": 0,
    "hint_only": 1,
    "partial_solution": 2,
    "full_solution": 3,
    "reference_code": 4,
}


class DecisionValidationError(Exception):
    pass


class LlmDecisionEngine:
    REQUEST_TIMEOUT_SECONDS = int(os.getenv("AGENT_REQUEST_TIMEOUT_SECONDS", "110"))
    LLM_TIMEOUT_SECONDS = int(os.getenv("AGENT_LLM_TIMEOUT_SECONDS", "100"))

    def __init__(self, client: OllamaClient | None = None):
        self.client = client or OllamaClient()
        self.intent_engine = IntentUnderstandingEngine()

    def decide(self, context: AgentContext) -> AgentDecision:
        started_at = monotonic()
        intent = self.intent_engine.understand(context)
        context.intent_hypothesis = {
            "normalized_intent": intent.normalized_intent,
            "confidence": intent.confidence,
            "requested_scope": intent.requested_scope,
            "intent_reason": intent.intent_reason,
            "clarification_question": intent.clarification_question,
        }
        if intent.confidence < INTENT_CONFIDENCE_THRESHOLD:
            return self._clarification_decision(context, intent)

        prompt = self._build_prompt(context, intent)
        raw = self.client.generate(
            prompt,
            temperature=0.25,
            timeout_seconds=self._remaining_timeout(started_at),
        )
        if raw is None or not str(raw).strip():
            return self._failure_decision(context, self._client_error_tag("MODEL_TIMEOUT"), intent)

        try:
            payload = self._parse_and_validate(raw, self._candidate_actions(context))
            return self._to_decision(context, payload, "initial", intent)
        except DecisionValidationError as first_error:
            if self._remaining_timeout(started_at) <= 0:
                return self._failure_decision(context, "INVALID_JSON", intent)
            repair_prompt = self._build_repair_prompt(context, raw, str(first_error))
            repaired = self.client.generate(
                repair_prompt,
                temperature=0.1,
                timeout_seconds=self._remaining_timeout(started_at),
            )
            if repaired is None or not str(repaired).strip():
                return self._failure_decision(context, self._client_error_tag("INVALID_JSON"), intent)
            try:
                payload = self._parse_and_validate(repaired, self._candidate_actions(context))
                return self._to_decision(context, payload, "repaired", intent)
            except DecisionValidationError:
                return self._failure_decision(context, "INVALID_JSON", intent)

    def _remaining_timeout(self, started_at: float) -> int:
        remaining = self.REQUEST_TIMEOUT_SECONDS - (monotonic() - started_at)
        if remaining <= 0:
            return 0
        return min(self.LLM_TIMEOUT_SECONDS, max(1, int(remaining)))

    def _client_error_tag(self, fallback: str) -> str:
        tag = getattr(self.client, "last_error_tag", None)
        return tag or fallback

    def _build_prompt(self, context: AgentContext, intent: IntentUnderstanding) -> str:
        prompt_payload = {
            "request_id": context.request_id,
            "scene": context.scene,
            "action_hint": context.action_hint,
            "trigger_source": context.trigger_source,
            "user_message": context.user_message,
            "policy_profile": context.policy_profile,
            "requested_full_solution": context.requested_full_solution,
            "allowed_actions": self._candidate_actions(context),
            "answer_scope_order": list(ANSWER_SCOPE_ORDER.keys()),
            "failure_evidence_level": context.failure_evidence_level,
            "failure_signals": context.failure_signals.model_dump(),
            "violation": context.violation.model_dump() if context.violation else None,
            "conversation_history": [
                item.model_dump() for item in context.conversation_history[-6:]
            ],
            "problem": context.problem.model_dump(),
            "submission": context.submission.model_dump() if context.submission else None,
            "reduced_context": context.reduced_context.model_dump(),
            "tool_results": context.tool_results,
            "learning_summary": context.learning_summary,
            "intent_understanding": context.intent_hypothesis,
        }

        return (
            "你是编程学习系统的单一 Agent 决策内核。规则层只负责安全裁决，"
            "你负责根据上下文选择教学策略并生成回复。\n"
            "必须只输出一个合法 JSON 对象，不能输出 Markdown 代码围栏或额外解释。\n"
            "JSON schema:\n"
            "{\n"
            '  "decision": {\n'
            '    "action_type": "GUIDE_IDEA|HINT|DIAGNOSE|EXPLAIN|RECOMMEND|REFLECT|REVEAL_ANSWER|CLARIFY_INTENT",\n'
            '    "user_intent": "string",\n'
            '    "teaching_goal": "string",\n'
            '    "requested_scope": "concept_only|hint_only|partial_solution|full_solution|reference_code",\n'
            '    "answer_scope": "concept_only|hint_only|partial_solution|full_solution|reference_code",\n'
            '    "risk_level": "LOW|MEDIUM|HIGH",\n'
            '    "confidence": 0.0,\n'
            '    "used_tool_signals": ["string"]\n'
            "  },\n"
            '  "content_plan": {"key_points": ["string"], "quality_flags": ["string"]},\n'
            '  "final_response": {\n'
            '    "main_response": "150-300字中文回复，关键技术术语可用英文",\n'
            '    "next_suggestion": "string",\n'
            '    "weak_points": ["string"],\n'
            '    "error_tag": null\n'
            "  }\n"
            "}\n"
            "global_guide 场景只能做系统导览、当前页面说明和下一步分流；禁止完整题解、完整代码、替用户完成复杂复盘。\n"
            "不要编造题目、推荐题、链接或不存在的失败记录；真实数据只能来自输入上下文。\n"
            "输入上下文:\n"
            f"{json.dumps(prompt_payload, ensure_ascii=False, indent=2)}"
        )

    def _build_repair_prompt(self, context: AgentContext, raw: str, reason: str) -> str:
        return (
            "上一次 Agent 输出不是合法的决策 JSON。请只返回修复后的 JSON 对象，"
            "不要解释，不要使用 Markdown 代码围栏。\n"
            f"错误原因: {reason}\n"
            f"request_id: {context.request_id}\n"
            f"允许 action_type: {sorted(ALLOWED_ACTIONS)}\n"
            f"允许 answer_scope: {list(ANSWER_SCOPE_ORDER.keys())}\n"
            f"原始输出:\n{raw}"
        )

    def _parse_and_validate(self, raw: str, allowed_actions: list[str] | None = None) -> dict[str, Any]:
        payload = self._extract_json(raw)
        decision = payload.get("decision")
        final_response = payload.get("final_response")
        if not isinstance(decision, dict):
            raise DecisionValidationError("decision object is required")
        if not isinstance(final_response, dict):
            raise DecisionValidationError("final_response object is required")

        action_type = str(decision.get("action_type") or "").strip()
        if action_type not in ALLOWED_ACTIONS:
            raise DecisionValidationError(f"unsupported action_type: {action_type}")
        if allowed_actions and action_type not in allowed_actions:
            raise DecisionValidationError(f"action_type not allowed by context policy: {action_type}")

        answer_scope = str(decision.get("answer_scope") or "").strip()
        requested_scope = str(decision.get("requested_scope") or answer_scope).strip()
        if answer_scope not in ANSWER_SCOPE_ORDER:
            raise DecisionValidationError(f"unsupported answer_scope: {answer_scope}")
        if requested_scope not in ANSWER_SCOPE_ORDER:
            raise DecisionValidationError(f"unsupported requested_scope: {requested_scope}")

        main_response = str(final_response.get("main_response") or "").strip()
        if not main_response:
            raise DecisionValidationError("main_response is required")

        return payload

    def _extract_json(self, raw: str) -> dict[str, Any]:
        text = str(raw).strip()
        if text.startswith("```"):
            text = text.strip("`")
            if text.lower().startswith("json"):
                text = text[4:].strip()
        start = text.find("{")
        end = text.rfind("}")
        if start < 0 or end < start:
            raise DecisionValidationError("json object not found")
        try:
            return json.loads(text[start:end + 1])
        except json.JSONDecodeError as error:
            raise DecisionValidationError(f"invalid json: {error}") from error

    def _to_decision(self, context: AgentContext, payload: dict[str, Any], stage: str, intent: IntentUnderstanding) -> AgentDecision:
        decision = payload["decision"]
        content_plan = payload.get("content_plan") if isinstance(payload.get("content_plan"), dict) else {}
        final_response = payload["final_response"]
        alignment_check = payload.get("alignment_check") if isinstance(payload.get("alignment_check"), dict) else {}
        action_type = decision["action_type"]
        main_response = str(final_response.get("main_response") or "").strip()
        next_suggestion = final_response.get("next_suggestion")
        weak_points = final_response.get("weak_points")
        used_tool_signals = decision.get("used_tool_signals")
        quality_flags = content_plan.get("quality_flags")

        return AgentDecision(
            response_id=context.request_id,
            timestamp=datetime.now(),
            decision_summary=f"动作: {action_type}, scope: {decision.get('answer_scope')}",
            selected_strategy=action_type,
            decision_reason=self._decision_reason(context, decision, stage, intent),
            applied_constraints=[],
            blocked_actions=[],
            candidate_actions=self._candidate_action_views(context, action_type, main_response),
            recommended_action_id=f"{action_type.lower()}_001",
            action_type=action_type,
            pedagogical_goal=GOAL_BY_ACTION.get(action_type, "GUIDE_INITIAL_THINKING"),
            content_type=CONTENT_TYPE_BY_ACTION[action_type],
            main_response=main_response,
            next_suggestion=str(next_suggestion).strip() if next_suggestion else None,
            error_tag=final_response.get("error_tag"),
            weak_points=weak_points if isinstance(weak_points, list) else [],
            confidence=float(decision.get("confidence") or 0.65),
            used_knowledge_refs=[],
            user_intent=str(decision.get("user_intent") or "UNKNOWN"),
            teaching_goal=str(decision.get("teaching_goal") or "clarify_next_step"),
            requested_scope=str(decision.get("requested_scope") or decision.get("answer_scope")),
            answer_scope=str(decision.get("answer_scope")),
            risk_level=str(decision.get("risk_level") or "LOW"),
            used_tool_signals=used_tool_signals if isinstance(used_tool_signals, list) else [],
            content_plan=content_plan,
            quality_flags=quality_flags if isinstance(quality_flags, list) else [],
            normalized_intent=intent.normalized_intent,
            intent_confidence=intent.confidence,
            intent_reason=intent.intent_reason,
            clarification_question=intent.clarification_question,
            answered_user_question=bool(alignment_check.get("answered_user_question", True)),
            alignment_reason=str(alignment_check.get("alignment_reason") or "directly addresses the normalized user intent"),
            decision_stage=stage,
            content=main_response,
            suggested_next_action=str(next_suggestion).strip() if next_suggestion else None,
        )

    def _clarification_decision(self, context: AgentContext, intent: IntentUnderstanding) -> AgentDecision:
        question = intent.clarification_question or (
            "I am not sure what you want me to do first: explain the problem, give a hint, "
            "diagnose an error, review code, or recommend the next step?"
        )
        return AgentDecision(
            response_id=context.request_id,
            timestamp=datetime.now(),
            decision_summary="clarify user intent",
            selected_strategy="CLARIFY_INTENT",
            decision_reason=intent.intent_reason,
            applied_constraints=["LOW_INTENT_CONFIDENCE"],
            blocked_actions=[],
            candidate_actions=[
                CandidateAction(
                    action_id="clarify_intent_001",
                    action_type="CLARIFY_INTENT",
                    title="CLARIFY_INTENT",
                    description="Ask a short clarification before answering.",
                    priority="high",
                    content=question,
                    required_conditions=[],
                )
            ],
            recommended_action_id="clarify_intent_001",
            action_type="CLARIFY_INTENT",
            pedagogical_goal="CLARIFY_USER_INTENT",
            content_type="clarification",
            main_response=question,
            next_suggestion="Reply with the direction you want: explanation, hint, diagnosis, code review, answer, or next step.",
            error_tag=None,
            weak_points=[],
            confidence=intent.confidence,
            used_knowledge_refs=[],
            user_intent="UNKNOWN",
            teaching_goal="clarify_user_intent",
            requested_scope=intent.requested_scope,
            answer_scope="concept_only",
            risk_level="LOW",
            used_tool_signals=[],
            content_plan={"key_points": ["clarify intent"], "quality_flags": ["LOW_INTENT_CONFIDENCE"]},
            quality_flags=["LOW_INTENT_CONFIDENCE"],
            normalized_intent=intent.normalized_intent,
            intent_confidence=intent.confidence,
            intent_reason=intent.intent_reason,
            clarification_question=question,
            answered_user_question=False,
            alignment_reason="intent confidence is too low, so the agent asks a clarification instead of guessing",
            decision_stage="clarify_intent",
            content=question,
            suggested_next_action="Reply with the direction you want: explanation, hint, diagnosis, code review, answer, or next step.",
        )

    def _failure_decision(self, context: AgentContext, failure_code: str, intent: IntentUnderstanding | None = None) -> AgentDecision:
        messages = {
            "MODEL_TIMEOUT": "模型响应超时，暂时无法完成这次 Agent 决策。你可以缩小问题范围，或补充当前代码和具体错误后重试。",
            "LLM_UNAVAILABLE": "大模型服务暂时不可用，当前 Python AgentService 没有连上 Ollama。请确认 Ollama 已启动，模型已安装，并且 AGENT_OLLAMA_URL 配置正确。",
            "MODEL_NOT_FOUND": "当前配置的模型不存在。请确认 AGENT_OLLAMA_MODEL 对应的 Ollama 模型已经安装，或修改模型配置后重试。",
            "MODEL_HTTP_ERROR": "大模型服务返回了异常状态，暂时无法完成这次 Agent 决策。请检查 Ollama 服务日志和模型配置。",
            "MODEL_ERROR": "大模型调用发生异常，暂时无法完成这次 Agent 决策。请检查 AgentService 日志中的模型错误详情。",
            "INVALID_JSON": "暂时无法生成稳定的结构化决策。你可以换一种更具体的问法，例如只问题意、提示、错误原因或相似题推荐。",
            "SAFETY_BLOCKED": "当前请求被安全策略拦截，暂不能直接给完整答案。可以先要一个提示或错误定位。",
            "TOOL_MISSING": "当前缺少必要的题目或推荐数据，无法可靠回答。请刷新页面或重新进入题目后再试。",
            "CONTEXT_INSUFFICIENT": "当前上下文不足以做出可靠判断。请补充题目、代码或失败现象中的一个。",
        }
        main_response = messages.get(failure_code, messages["CONTEXT_INSUFFICIENT"])
        normalized_intent = intent.normalized_intent if intent else "UNKNOWN"
        intent_confidence = intent.confidence if intent else 0.0
        intent_reason = intent.intent_reason if intent else ""
        return AgentDecision(
            response_id=context.request_id,
            timestamp=datetime.now(),
            decision_summary=f"Agent failure: {failure_code}",
            selected_strategy="FAILURE",
            decision_reason=f"failure_code={failure_code}, scene={context.scene}, action_hint={context.action_hint}",
            applied_constraints=[],
            blocked_actions=[],
            candidate_actions=[],
            recommended_action_id="failure_001",
            action_type="EXPLAIN",
            pedagogical_goal="EXPLAIN_CONCEPT",
            content_type="explanation",
            main_response=main_response,
            next_suggestion="缩小问题范围后重试，例如只问“题目要求是什么”或“给我一个关键提示”。",
            error_tag=failure_code,
            weak_points=[],
            confidence=0.0,
            used_knowledge_refs=[],
            user_intent="UNKNOWN",
            teaching_goal="recover_from_failure",
            requested_scope="concept_only",
            answer_scope="concept_only",
            risk_level="LOW",
            used_tool_signals=[],
            content_plan={"key_points": [], "quality_flags": [failure_code]},
            quality_flags=[failure_code],
            normalized_intent=normalized_intent,
            intent_confidence=intent_confidence,
            intent_reason=intent_reason,
            clarification_question=intent.clarification_question if intent else None,
            answered_user_question=False,
            alignment_reason=f"decision failed before a reliable answer could be generated: {failure_code}",
            decision_stage="failed",
            content=main_response,
            suggested_next_action="缩小问题范围后重试，例如只问“题目要求是什么”或“给我一个关键提示”。",
        )

    def _candidate_actions(self, context: AgentContext) -> list[str]:
        actions = [action for action in context.candidate_actions if action in ALLOWED_ACTIONS]
        return actions if actions else sorted(ALLOWED_ACTIONS)

    def _candidate_action_views(self, context: AgentContext, selected: str, content: str) -> list[CandidateAction]:
        actions = self._candidate_actions(context)
        ordered = [selected] + [action for action in actions if action != selected]
        result: list[CandidateAction] = []
        for index, action_type in enumerate(ordered[:4]):
            result.append(
                CandidateAction(
                    action_id=f"{action_type.lower()}_001",
                    action_type=action_type,
                    title=action_type,
                    description="LLM 结构化决策候选动作",
                    priority="high" if index == 0 else "medium",
                    content=content if index == 0 else "可作为后续教学动作。",
                    required_conditions=[],
                )
            )
        return result

    def _decision_reason(self, context: AgentContext, decision: dict[str, Any], stage: str, intent: IntentUnderstanding) -> str:
        return (
            f"scene={context.scene}, action_hint={context.action_hint}, "
            f"stage={stage}, user_intent={decision.get('user_intent')}, "
            f"normalized_intent={intent.normalized_intent}, intent_confidence={intent.confidence}, "
            f"answer_scope={decision.get('answer_scope')}, "
            f"failure_evidence_level={context.failure_evidence_level}"
        )
