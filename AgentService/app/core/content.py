from dataclasses import dataclass, field
from typing import Optional

from app.capabilities.diagnose import DiagnosisEngine
from app.core.knowledge import KnowledgeBundle, KnowledgeRetriever
from app.schemas.context import AgentContext


CONTENT_TYPE_BY_ACTION = {
    "GUIDE_IDEA": "guidance",
    "HINT": "hint",
    "DIAGNOSE": "diagnosis",
    "EXPLAIN": "explanation",
    "RECOMMEND": "recommendation",
    "REFLECT": "reflection",
    "REVEAL_ANSWER": "solution",
    "CLARIFY_INTENT": "clarification",
}

GOAL_BY_ACTION = {
    "GUIDE_IDEA": "GUIDE_INITIAL_THINKING",
    "HINT": "GIVE_LIGHT_HINT",
    "DIAGNOSE": "DIAGNOSE_ERROR_CAUSE",
    "EXPLAIN": "EXPLAIN_CONCEPT",
    "RECOMMEND": "RECOMMEND_REMEDIATION",
    "REFLECT": "REVIEW_AFTER_SOLUTION",
    "REVEAL_ANSWER": "REVIEW_AFTER_SOLUTION",
    "CLARIFY_INTENT": "CLARIFY_USER_INTENT",
}


@dataclass
class GeneratedContent:
    main_response: str
    next_suggestion: Optional[str]
    error_tag: Optional[str] = None
    weak_points: list[str] = field(default_factory=list)
    confidence: float = 0.65
    used_knowledge_refs: list[str] = field(default_factory=list)


class PedagogicalGoalBuilder:
    def build(self, action_type: str) -> str:
        return GOAL_BY_ACTION[action_type]


class ContentGenerator:
    def __init__(self):
        self.diagnosis_engine = DiagnosisEngine()
        self.knowledge_retriever = KnowledgeRetriever()

    def generate(self, context: AgentContext, action_type: str, intent: str) -> GeneratedContent:
        goal = GOAL_BY_ACTION[action_type]
        knowledge = self.knowledge_retriever.retrieve(context, action_type, goal)

        if action_type == "DIAGNOSE":
            result = self.diagnosis_engine.diagnose(context)
            return GeneratedContent(
                main_response=self._diagnosis(result.content, knowledge),
                next_suggestion=result.suggested_next_action or self._diagnosis_next_suggestion(knowledge),
                error_tag=result.error_tag,
                weak_points=result.weak_points,
                confidence=0.72,
                used_knowledge_refs=self._knowledge_refs(context, knowledge),
            )

        if action_type == "GUIDE_IDEA":
            return GeneratedContent(
                main_response=self._guidance(context, knowledge),
                next_suggestion=self._guidance_next_suggestion(context),
                used_knowledge_refs=self._knowledge_refs(context, knowledge),
            )

        if action_type == "HINT":
            return GeneratedContent(
                main_response=self._hint(context, knowledge),
                next_suggestion="先只改这一处，再用最小样例重新运行一次。",
                error_tag=self._fallback_error_tag(context),
                weak_points=self._weak_points(context),
                used_knowledge_refs=self._knowledge_refs(context, knowledge),
            )

        if action_type == "EXPLAIN":
            return GeneratedContent(
                main_response=self._explanation(context, intent, knowledge),
                next_suggestion="回到当前代码，把这个知识点对应到一个具体分支或变量上。",
                error_tag=self._fallback_error_tag(context),
                weak_points=self._weak_points(context),
                used_knowledge_refs=self._knowledge_refs(context, knowledge),
            )

        if action_type == "RECOMMEND":
            return GeneratedContent(
                main_response=self._recommendation(context, knowledge),
                next_suggestion="优先选择同知识点、难度相近的练习，再回到当前路径或错题复盘确认是否掌握。",
                weak_points=self._weak_points(context),
                used_knowledge_refs=self._knowledge_refs(context, knowledge),
            )

        if action_type == "REFLECT":
            return GeneratedContent(
                main_response=self._reflection(context, knowledge),
                next_suggestion="用自己的话写下本题的关键判断，再做一道同类题验证是否真的掌握。",
                weak_points=self._weak_points(context),
                used_knowledge_refs=self._knowledge_refs(context, knowledge),
            )

        return GeneratedContent(
            main_response=self._solution(context, knowledge),
            next_suggestion="看完后不要直接背代码，重点记录触发条件、修正思路和边界用例。",
            weak_points=self._weak_points(context),
            used_knowledge_refs=self._knowledge_refs(context, knowledge),
        )

    def _guidance(self, context: AgentContext, knowledge: KnowledgeBundle) -> str:
        if self._is_two_sum(context):
            base = (
                "思路：这题可以从“补数 + 哈希表”入手。遍历数组时，当前数 x 需要的目标是 "
                "target - x；用哈希表记录已经见过的数字和下标，就能在看到 x 时快速判断补数是否已出现。"
            )
            sample = self._card_text(knowledge, "problem", "sample_explanation")
            if sample:
                return f"{base} 结合样例先确认哪两个数配对，再想清楚“先查再存”的顺序。这里不需要直接写完整代码。"
            return base + " 先想清楚“先查再存”的顺序，这能避免同一个元素被使用两次。"

        points = "、".join(context.problem.knowledge_points[:2]) or "当前知识点"
        content = self._card_text(knowledge, "problem", "content")
        hint = self._card_text(knowledge, "problem", "hints")
        sample = self._card_text(knowledge, "problem", "sample_explanation")
        input_part = f"输入：{self._shorten(content)}" if content else "输入：先标出题目给了哪些字段、参数和约束。"
        state_part = f"状态：{self._shorten(hint, 120)}" if hint else f"状态：围绕 {points} 维护必要变量。"
        sample_part = f"样例：{self._shorten(sample, 100)}" if sample else "样例：先手推一个最小输入，确认状态如何变化。"
        return (
            "思路：先把题目拆成输入、状态更新和输出三件事。"
            f" {input_part} {state_part} 步骤：读入数据，按题意更新状态，最后组织输出。"
            f" {sample_part} 常见坑：先确认状态变量的初值、更新时机和输出格式。"
        )

    def _guidance_next_suggestion(self, context: AgentContext) -> str:
        if self._is_two_sum(context):
            return "先用一个小样例手推：每看到一个数，就想它需要的补数是否已经出现过。"
        return "先按“输入 -> 状态变量 -> 更新规则 -> 输出格式”写四行伪代码，再补具体实现。"

    def _hint(self, context: AgentContext, knowledge: KnowledgeBundle) -> str:
        hint = self._card_text(knowledge, "problem", "hints")
        if hint:
            return f"只给一步提示：{self._first_sentence(hint)}"
        if self._is_two_sum(context):
            return "只给一步提示：遍历到当前数之前，先查 `target - 当前数` 是否已经在哈希表里，再把当前数放进去。"
        error = context.submission.error_message if context.submission else None
        if error:
            template = self._card_text(knowledge, "error", "template:error")
            if template:
                return f"只先看一个点：{template}"
            return f"只先看一个点：围绕当前错误 `{error}` 找到第一个不符合预期的输入，不要一次改很多地方。"
        return "只给一步提示：先找最小样例，确认你的状态更新顺序是否和题目要求一致。"

    def _diagnosis(self, diagnosis: str, knowledge: KnowledgeBundle) -> str:
        template = self._card_text(knowledge, "error", "template:error")
        if template and diagnosis and template not in diagnosis:
            return f"{diagnosis} 建议对照这个方向排查：{template}"
        return diagnosis

    def _diagnosis_next_suggestion(self, knowledge: KnowledgeBundle) -> str:
        template = self._card_text(knowledge, "error", "template:error")
        if template:
            return f"先按这个方向验证一个最小失败用例：{template}"
        return "先定位第一个失败用例，再决定是修边界、复杂度还是状态更新顺序。"

    def _explanation(self, context: AgentContext, intent: str, knowledge: KnowledgeBundle) -> str:
        points = context.problem.knowledge_points or ["基础逻辑"]
        teaching = self._card_text(knowledge, "teaching", "template:teaching")
        if self._is_two_sum(context):
            explanation = (
                "两数之和的核心不是双重循环，而是把“找另一个数”变成 O(1) 查询。"
                "哈希表保存已经遍历过的数字及下标；关键顺序是先查补数，再存当前数，避免同一个元素被用两次。"
            )
            return f"{explanation} {teaching}" if teaching and teaching not in explanation else explanation
        if teaching:
            return f"这轮建议重点理解 {points[0]}：{teaching} 再把它对应到当前题目的状态更新和边界条件上。"
        return f"这轮建议重点理解 {points[0]}：先明确它保存的状态，再看每一步什么时候读、什么时候写。"

    def _recommendation(self, context: AgentContext, knowledge: KnowledgeBundle) -> str:
        weak_points = "、".join(self._weak_points(context))
        teaching = self._card_text(knowledge, "teaching", "template:teaching")
        if context.trigger_source == "LEARNING_PATH_ENTRY":
            return (
                f"推荐理由：当前节点适合围绕 {weak_points} 做一次小步练习。"
                "建议练习：先选同知识点、难度相近的题，重点验证状态更新顺序和边界条件。"
                f"复习点：{teaching or '把题目中的输入、状态、输出条件逐一对应到代码变量上。'}"
            )
        return (
            f"推荐理由：下一步应围绕当前薄弱点 {weak_points} 做一道同模式练习。"
            "建议练习：不要马上跳到更难题，先用相近题确认思路是否稳定。"
            "复习点：对照刚才的错误位置，总结触发条件和修正策略。"
        )

    def _reflection(self, context: AgentContext, knowledge: KnowledgeBundle) -> str:
        weak_points = "、".join(self._weak_points(context))
        error_tag = context.last_error_tag or self._fallback_error_tag(context) or "LOGIC_ERROR"
        template = self._card_text(knowledge, "error", "template:error")
        return (
            f"复盘重点：这次的错误更像是 {error_tag}，需要回看失败用例和关键状态更新。"
            f"相关知识点：{weak_points}。"
            f"下次避免策略：{template or '先手推最小样例，再确认判断和更新顺序是否和题意一致。'}"
        )

    def _solution(self, context: AgentContext, knowledge: KnowledgeBundle) -> str:
        if self._is_two_sum(context):
            return (
                "完整思路：遍历数组，用哈希表保存已经出现过的数字到下标的映射。"
                "对每个 nums[i]，先计算 need = target - nums[i]，如果 need 已在表中，返回它的下标和 i；"
                "否则再把 nums[i] 和 i 放入表。时间复杂度 O(n)，空间复杂度 O(n)。"
                "参考实现方向：`Map<Integer, Integer> seen = new HashMap<>();`，循环中先 `containsKey(need)`，再 `put(nums[i], i)`。"
            )
        return (
            "完整解法应先明确状态定义、转移或更新顺序，再处理边界输入。"
            "如果需要参考代码，建议先把当前代码和失败用例贴出来，我会基于你的写法给出可对照的实现。"
        )

    def _weak_points(self, context: AgentContext) -> list[str]:
        if context.weak_points:
            return context.weak_points[:3]
        if context.problem.knowledge_points:
            return context.problem.knowledge_points[:3]
        return ["基础逻辑"]

    def _fallback_error_tag(self, context: AgentContext) -> Optional[str]:
        status = (context.submission.status if context.submission else "") or ""
        error = (context.submission.error_message if context.submission else "") or ""
        if "TLE" in status or "超时" in error:
            return "TIME_COMPLEXITY"
        if "RE" in status or "越界" in error:
            return "ARRAY_BOUNDARY"
        if "CE" in status or "编译" in error:
            return "COMPILE_ERROR"
        if "WA" in status or "答案" in error:
            return "LOGIC_ERROR"
        return None

    def _knowledge_refs(self, context: AgentContext, knowledge: KnowledgeBundle) -> list[str]:
        refs = [f"problem:{context.problem.problem_id}"]
        refs.extend(f"knowledge:{point}" for point in context.problem.knowledge_points[:3])
        refs.extend(ref for ref in knowledge.used_knowledge_refs if ref not in refs)
        return refs

    def _is_two_sum(self, context: AgentContext) -> bool:
        title = context.problem.title.lower()
        return "两数之和" in context.problem.title or "two sum" in title

    def _card_text(self, knowledge: KnowledgeBundle, card_type: str, source_contains: str) -> Optional[str]:
        for card in knowledge.cards:
            if card.card_type == card_type and source_contains in card.source:
                return card.text
        return None

    def _first_sentence(self, text: str) -> str:
        for separator in ["。", "；", "\n"]:
            if separator in text:
                return text.split(separator, 1)[0].strip() + "。"
        return self._shorten(text)

    def _shorten(self, text: str, limit: int = 80) -> str:
        text = " ".join(text.split())
        return text if len(text) <= limit else text[:limit].rstrip() + "..."
