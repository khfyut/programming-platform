from dataclasses import dataclass

from app.schemas.context import AgentContext


INTENT_CONFIDENCE_THRESHOLD = 0.65


@dataclass
class IntentUnderstanding:
    normalized_intent: str
    confidence: float
    requested_scope: str
    intent_reason: str
    clarification_question: str | None = None


class IntentUnderstandingEngine:
    def understand(self, context: AgentContext) -> IntentUnderstanding:
        message = self._normalize(context.user_message)
        trigger = context.trigger_source

        if context.scene == "global_guide" and self._contains_any(
            message,
            [
                "system",
                "how to use",
                "how do i start",
                "start",
                "guide",
                "这个系统",
                "怎么用",
                "怎么开始",
                "学习助手",
                "了解系统",
                "ai 陪练",
            ],
        ):
            return IntentUnderstanding(
                normalized_intent="ASK_SYSTEM_GUIDE",
                confidence=0.86,
                requested_scope="concept_only",
                intent_reason="global guide question asks how to use the learning system or assistant",
            )

        if context.requested_full_solution:
            return IntentUnderstanding(
                normalized_intent="ASK_FOR_FULL_SOLUTION",
                confidence=0.94,
                requested_scope="full_solution",
                intent_reason="request explicitly asked for full solution",
            )

        if self._contains_any(message, ["hint", "tip", "clue", "next hint", "提示", "关键提示", "给点提示", "一点提示"]):
            return IntentUnderstanding(
                normalized_intent="ASK_FOR_HINT",
                confidence=0.9,
                requested_scope="hint_only",
                intent_reason="message asks for a hint",
            )

        if self._contains_any(
            message,
            [
                "why wrong",
                "why is my",
                "wrong answer",
                "answer wrong",
                "error",
                "bug",
                "runtime",
                "compile",
                "failed",
                "为什么错",
                "哪里错",
                "报错",
                "错误",
                "失败",
            ],
        ):
            return IntentUnderstanding(
                normalized_intent="ASK_FOR_ERROR_ANALYSIS",
                confidence=0.88 if self._has_failure_evidence(context) else 0.74,
                requested_scope="partial_solution" if self._has_failure_evidence(context) else "hint_only",
                intent_reason="message asks about an error or failed attempt",
            )

        if (
            self._contains_any(
                message,
                ["answer", "solution", "complete code", "reference code", "full code", "答案", "完整代码", "参考代码", "直接给", "做完", "帮我做完"],
            )
            and not self._contains_any(message, ["do not give", "don't give", "without answer", "不要给", "先不要给"])
        ):
            return IntentUnderstanding(
                normalized_intent="ASK_FOR_FULL_SOLUTION",
                confidence=0.88,
                requested_scope="full_solution",
                intent_reason="message asks for answer or reference code",
            )

        if self._contains_any(message, ["review my code", "check my code", "code review", "检查代码", "看看代码", "代码检查"]):
            return IntentUnderstanding(
                normalized_intent="ASK_FOR_CODE_REVIEW",
                confidence=0.86,
                requested_scope="partial_solution",
                intent_reason="message asks to inspect code",
            )

        if self._contains_any(
            message,
            ["what does this problem mean", "problem mean", "explain the problem", "what is this asking", "这题是什么意思", "题目是什么意思", "解释题意", "题意"],
        ):
            return IntentUnderstanding(
                normalized_intent="ASK_FOR_EXPLANATION",
                confidence=0.86,
                requested_scope="concept_only",
                intent_reason="message asks to explain the problem statement",
            )

        if self._contains_any(message, ["explain", "concept", "meaning", "what is", "解释", "讲一下", "什么意思", "题目"]):
            return IntentUnderstanding(
                normalized_intent="ASK_FOR_EXPLANATION",
                confidence=0.78,
                requested_scope="concept_only",
                intent_reason="message asks for an explanation",
            )

        if self._contains_any(message, ["idea", "approach", "how to solve", "where to start", "direction", "思路", "怎么做", "从哪开始", "方向"]):
            return IntentUnderstanding(
                normalized_intent="ASK_PROBLEM_SOLVING_IDEA",
                confidence=0.84,
                requested_scope="hint_only",
                intent_reason="message asks for solving direction",
            )

        action_hint = self._normalize(context.action_hint)
        if self._contains_any(message, ["next", "recommend", "similar", "practice", "下一步", "推荐", "类似题", "相似题", "练习"]) or self._contains_any(action_hint, ["similar_problem", "recommend"]):
            return IntentUnderstanding(
                normalized_intent="ASK_FOR_NEXT_STEP",
                confidence=0.84,
                requested_scope="concept_only",
                intent_reason="message asks for a next step",
            )

        if not message and trigger in {"RUN_RESULT", "SUBMISSION_RESULT"}:
            return IntentUnderstanding(
                normalized_intent="ASK_FOR_ERROR_ANALYSIS",
                confidence=0.83,
                requested_scope="partial_solution",
                intent_reason="failure trigger without explicit message defaults to error analysis",
            )

        if not message and trigger == "WRONG_BOOK_ENTRY":
            return IntentUnderstanding(
                normalized_intent="ASK_FOR_ERROR_ANALYSIS",
                confidence=0.78,
                requested_scope="partial_solution",
                intent_reason="wrong book entry defaults to reflection over the mistake",
            )

        if self._contains_any(message, ["hello", "hi", "thanks", "thank you", "你好", "谢谢"]):
            return IntentUnderstanding(
                normalized_intent="GENERAL_CHAT",
                confidence=0.76,
                requested_scope="concept_only",
                intent_reason="message is general chat",
            )

        return IntentUnderstanding(
            normalized_intent="UNKNOWN",
            confidence=0.42,
            requested_scope="concept_only",
            intent_reason="message does not clearly map to a supported learning intent",
            clarification_question=(
                "我还不确定你希望我先做哪件事：解释题意 explain、给提示、诊断错误、检查代码，"
                "还是推荐下一步？你可以直接回复其中一个方向。"
            ),
        )

    def _has_failure_evidence(self, context: AgentContext) -> bool:
        level = (context.failure_evidence_level or "").upper()
        if level in {"WEAK", "STRONG", "VERIFIED"}:
            return True
        if context.consecutive_failures > 0:
            return True
        if context.submission and context.submission.status not in {"", "NONE", "0", "AC"}:
            return True
        return False

    def _normalize(self, message: str | None) -> str:
        return " ".join((message or "").lower().strip().split())

    def _contains_any(self, message: str, keywords: list[str]) -> bool:
        return any(keyword in message for keyword in keywords)
