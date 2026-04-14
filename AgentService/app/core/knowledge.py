from dataclasses import dataclass, field

from app.schemas.context import AgentContext


@dataclass
class KnowledgeCard:
    source: str
    text: str
    card_type: str


@dataclass
class KnowledgeBundle:
    problem_cards: list[KnowledgeCard] = field(default_factory=list)
    error_cards: list[KnowledgeCard] = field(default_factory=list)
    teaching_cards: list[KnowledgeCard] = field(default_factory=list)
    used_knowledge_refs: list[str] = field(default_factory=list)

    @property
    def cards(self) -> list[KnowledgeCard]:
        return self.problem_cards + self.error_cards + self.teaching_cards


class KnowledgeRetriever:
    def retrieve(self, context: AgentContext, action_type: str, pedagogical_goal: str) -> KnowledgeBundle:
        bundle = KnowledgeBundle()
        self._add_problem_cards(context, bundle, action_type)
        self._add_error_cards(context, bundle, action_type)
        self._add_teaching_cards(context, bundle, action_type, pedagogical_goal)
        return bundle

    def _add_problem_cards(self, context: AgentContext, bundle: KnowledgeBundle, action_type: str) -> None:
        problem = context.problem
        problem_id = problem.problem_id

        if problem.problem_content.strip() and action_type in {"GUIDE_IDEA", "EXPLAIN"}:
            self._add_card(bundle.problem_cards, bundle, f"problem:{problem_id}:content", problem.problem_content, "problem")

        if problem.hints.strip() and action_type in {"HINT", "GUIDE_IDEA"}:
            self._add_card(bundle.problem_cards, bundle, f"problem:{problem_id}:hints", problem.hints, "problem")

        if problem.sample_explanation.strip() and action_type in {"GUIDE_IDEA", "EXPLAIN", "DIAGNOSE"}:
            self._add_card(
                bundle.problem_cards,
                bundle,
                f"problem:{problem_id}:sample_explanation",
                problem.sample_explanation,
                "problem",
            )

        if problem.tags.strip():
            self._add_card(bundle.problem_cards, bundle, f"problem:{problem_id}:tags", problem.tags, "problem")

    def _add_error_cards(self, context: AgentContext, bundle: KnowledgeBundle, action_type: str) -> None:
        if action_type not in {"HINT", "DIAGNOSE"}:
            return

        status = ((context.submission.status if context.submission else "") or "").upper()
        error_message = ((context.submission.error_message if context.submission else "") or "").upper()
        error_key = self._error_key(status, error_message)
        templates = {
            "WA": "答案错误通常先检查状态更新顺序、边界条件和样例外的反例。",
            "RE": "运行时错误优先检查数组越界、空指针和输入为空的情况。",
            "TLE": "超时通常需要重新评估时间复杂度，避免不必要的嵌套遍历。",
            "MLE": "内存超限通常需要检查是否保存了过多中间状态。",
            "CE": "编译错误优先处理语法、类型和方法签名问题。",
            "UNKNOWN": "先定位最小失败输入，再判断是逻辑、边界、复杂度还是语法问题。",
        }
        self._add_card(bundle.error_cards, bundle, f"template:error:{error_key}", templates[error_key], "error")

    def _add_teaching_cards(self, context: AgentContext, bundle: KnowledgeBundle, action_type: str, pedagogical_goal: str) -> None:
        if action_type not in {"GUIDE_IDEA", "EXPLAIN", "DIAGNOSE"}:
            return

        normalized_points = " ".join(context.problem.knowledge_points + [context.problem.tags]).lower()
        if "哈希" in normalized_points or "hash" in normalized_points:
            self._add_card(
                bundle.teaching_cards,
                bundle,
                "template:teaching:hashmap",
                "哈希表适合把“是否出现过”或“值到位置”的查询降到接近 O(1)。",
                "teaching",
            )
        if "数组" in normalized_points or "array" in normalized_points:
            self._add_card(
                bundle.teaching_cards,
                bundle,
                "template:teaching:array",
                "数组题要特别关注下标范围、遍历顺序和是否重复使用同一个元素。",
                "teaching",
            )

        if not bundle.teaching_cards and pedagogical_goal == "EXPLAIN_CONCEPT":
            self._add_card(
                bundle.teaching_cards,
                bundle,
                "template:teaching:general",
                "先明确需要维护的状态，再检查每一步读取和写入的时机。",
                "teaching",
            )

    def _error_key(self, status: str, error_message: str) -> str:
        text = f"{status} {error_message}"
        for key in ["WA", "RE", "TLE", "MLE", "CE"]:
            if key in text:
                return key
        if "答案" in text:
            return "WA"
        if "越界" in text or "RUNTIME" in text:
            return "RE"
        if "超时" in text:
            return "TLE"
        if "编译" in text:
            return "CE"
        return "UNKNOWN"

    def _add_card(self, cards: list[KnowledgeCard], bundle: KnowledgeBundle, source: str, text: str, card_type: str) -> None:
        if not text or not text.strip() or source in bundle.used_knowledge_refs:
            return
        cards.append(KnowledgeCard(source=source, text=text.strip(), card_type=card_type))
        bundle.used_knowledge_refs.append(source)
