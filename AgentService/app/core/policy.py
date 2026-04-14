from app.schemas.context import AgentContext


ALL_ACTIONS = [
    "GUIDE_IDEA",
    "HINT",
    "DIAGNOSE",
    "EXPLAIN",
    "RECOMMEND",
    "REFLECT",
    "REVEAL_ANSWER",
]


class IntentRouter:
    def detect(self, context: AgentContext) -> str:
        if context.requested_full_solution:
            return "ASK_FOR_FULL_SOLUTION"

        if context.user_intent and context.user_intent != "UNKNOWN":
            return context.user_intent

        message = self._normalize_message(context.user_message)
        if not message:
            if context.trigger_source in {"RUN_RESULT", "SUBMISSION_RESULT"}:
                return "ASK_FOR_ERROR_ANALYSIS"
            return "UNKNOWN"

        if self._contains_any(message, ["完整代码", "全部代码", "标准答案", "参考代码", "解答", "solution"]):
            return "ASK_FOR_FULL_SOLUTION"
        if self._contains_any(message, ["解题思路", "思路", "怎么做", "怎么想", "idea"]):
            return "ASK_PROBLEM_SOLVING_IDEA"
        if self._contains_any(message, ["提示", "一丢丢", "一点点", "hint"]):
            return "ASK_FOR_HINT"
        if self._contains_any(message, ["为什么错", "报错", "错误", "wrong", "wa", "运行", "异常"]):
            return "ASK_FOR_ERROR_ANALYSIS"
        if self._contains_any(message, ["代码检查", "检查代码", "review"]):
            return "ASK_FOR_CODE_REVIEW"
        if self._contains_any(message, ["讲解", "解释", "概念", "为什么"]):
            return "ASK_FOR_EXPLANATION"
        if self._contains_any(message, ["下一步", "接下来", "next"]):
            return "ASK_FOR_NEXT_STEP"
        if self._contains_any(message, ["你好", "谢谢", "hello", "hi"]):
            return "GENERAL_CHAT"
        return "UNKNOWN"

    def _normalize_message(self, message: str | None) -> str:
        if not message:
            return ""
        normalized = message.lower().strip()
        return "".join(
            ch
            for ch in normalized
            if ch.isalnum() or "\u4e00" <= ch <= "\u9fff" or ch == "_"
        )

    def _contains_any(self, message: str, keywords: list[str]) -> bool:
        return any(keyword.lower() in message for keyword in keywords)


class ConstraintFilter:
    def check(self, context: AgentContext, intent: str) -> tuple[list[str], list[str], list[str]]:
        available = ALL_ACTIONS.copy()
        blocked: list[str] = []
        constraints: list[str] = []

        if context.has_viewed_solution:
            for action in ["GUIDE_IDEA", "HINT", "REVEAL_ANSWER"]:
                if action in available:
                    available.remove(action)
                    blocked.append(action)
            constraints.append("看过答案后优先复盘或推荐，不再继续递进提示")

        if context.consecutive_failures <= 0 and intent == "ASK_FOR_FULL_SOLUTION":
            if "REVEAL_ANSWER" in available:
                available.remove("REVEAL_ANSWER")
                blocked.append("REVEAL_ANSWER")
            constraints.append("没有可信失败记录时不能直接给完整答案")

        if context.consecutive_failures < 2:
            if "DIAGNOSE" in available:
                available.remove("DIAGNOSE")
                blocked.append("DIAGNOSE")
            constraints.append("失败次数不够时优先轻提示")

        return available, blocked, constraints


class StrategySelector:
    def select(self, context: AgentContext, intent: str, available_actions: list[str]) -> str:
        if context.trigger_source == "WRONG_BOOK_ENTRY" and "REFLECT" in available_actions:
            return "REFLECT"

        if context.trigger_source == "LEARNING_PATH_ENTRY" and "RECOMMEND" in available_actions:
            return "RECOMMEND"

        if context.has_viewed_solution and "REFLECT" in available_actions:
            return "REFLECT"

        if intent == "ASK_FOR_FULL_SOLUTION":
            return "REVEAL_ANSWER" if "REVEAL_ANSWER" in available_actions else "HINT"

        if (
            context.trigger_source == "PROBLEM_PAGE_CHAT"
            and intent == "ASK_PROBLEM_SOLVING_IDEA"
            and context.consecutive_failures <= 0
            and "GUIDE_IDEA" in available_actions
        ):
            return "GUIDE_IDEA"

        if context.trigger_source in {"RUN_RESULT", "SUBMISSION_RESULT"}:
            if context.consecutive_failures >= 3 and "EXPLAIN" in available_actions:
                return "EXPLAIN"
            if context.consecutive_failures >= 2 and "DIAGNOSE" in available_actions:
                return "DIAGNOSE"
            return "HINT"

        if intent == "ASK_FOR_HINT":
            return "HINT"
        if intent in {"ASK_FOR_EXPLANATION", "ASK_FOR_CODE_REVIEW"}:
            return "EXPLAIN"
        if intent == "ASK_FOR_NEXT_STEP":
            return "RECOMMEND"
        if intent == "GENERAL_CHAT":
            return "EXPLAIN"
        return "HINT"
