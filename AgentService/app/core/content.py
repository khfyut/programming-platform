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
}

GOAL_BY_ACTION = {
    "GUIDE_IDEA": "GUIDE_INITIAL_THINKING",
    "HINT": "GIVE_LIGHT_HINT",
    "DIAGNOSE": "DIAGNOSE_ERROR_CAUSE",
    "EXPLAIN": "EXPLAIN_CONCEPT",
    "RECOMMEND": "RECOMMEND_REMEDIATION",
    "REFLECT": "REVIEW_AFTER_SOLUTION",
    "REVEAL_ANSWER": "REVIEW_AFTER_SOLUTION",
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
                next_suggestion="先用一个小样例手推：每看到一个数，就想它需要的补数是否已经出现过。",
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
            main_response="已经满足查看完整答案的条件。请先对照你自己的代码看关键差异，再复盘为什么原实现没有覆盖这个情况。",
            next_suggestion="看完后先不要直接记代码，重点记录触发条件和修正思路。",
            weak_points=self._weak_points(context),
            used_knowledge_refs=self._knowledge_refs(context, knowledge),
        )

    def _guidance(self, context: AgentContext, knowledge: KnowledgeBundle) -> str:
        if self._is_two_sum(context):
            base = "这题可以从“补数 + 哈希表”入手：遍历数组时，当前数 x 需要的目标是 target - x；用哈希表记录已经见过的数字，就能在看到 x 时快速判断补数是否已经出现。"
            sample = self._card_text(knowledge, "problem", "sample_explanation")
            if sample:
                return f"{base} 结合样例解释看，先确认哪两个数配对，再想清楚“先查再存”的顺序，不需要直接写完整代码。"
            return base + "先想清楚“先查再存”的顺序，不需要直接写完整代码。"
        points = "、".join(context.problem.knowledge_points[:2]) or "当前知识点"
        content = self._card_text(knowledge, "problem", "content")
        prefix = "先把题目拆成状态和选择：当前要维护什么信息、每一步如何更新、什么时候可以确定答案。"
        if content:
            return f"{prefix} 这道题的核心输入输出是：{self._shorten(content)} 结合 {points}，先手推一个最小样例，再写代码会更稳。"
        return f"{prefix} 结合 {points}，先手推一个最小样例，再写代码会更稳。"

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
            explanation = "两数之和的核心不是双重循环，而是把“找另一个数”变成 O(1) 查询：哈希表保存已经遍历过的数字及下标。关键顺序是先查补数，再存当前数，这样不会把同一个元素用两次。"
            return f"{explanation} {teaching}" if teaching and teaching not in explanation else explanation
        if teaching:
            return f"这轮建议重点理解 {points[0]}：{teaching} 再把它对应到当前题目的状态更新和边界条件上。"
        return f"这轮建议重点理解 {points[0]}：先明确它保存的状态，再看每一步什么时候读、什么时候写。错误通常来自状态更新顺序或边界条件没有和题意对齐。"

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
            f"推荐理由：下一步应围绕当前薄弱点 {weak_points} 做一题同模式练习。"
            "建议练习：不要马上跳到更难题，先用相近题确认思路是否稳定。"
            "复习点：对照刚才的错误位置，总结触发条件和修正策略。"
        )

    def _reflection(self, context: AgentContext, knowledge: KnowledgeBundle) -> str:
        weak_points = "、".join(self._weak_points(context))
        error_tag = context.last_error_tag or self._fallback_error_tag(context) or "LOGIC_ERROR"
        template = self._card_text(knowledge, "error", "template:error")
        return (
            f"错误原因：这次更像是 {error_tag}，需要回看失败用例和关键状态更新。"
            f"相关知识点：{weak_points}。"
            f"下次避免策略：{template or '先手推最小样例，再确认先判断、后更新的顺序是否和题意一致。'}"
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
