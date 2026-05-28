from typing import List, Optional

import os

from app.core.llm_client import OllamaClient, OpenAICompatibleClient
from app.schemas.context import AgentContext


def _create_llm_client():
    if os.getenv("AGENT_LLM_PROVIDER", "").lower() == "openai":
        return OpenAICompatibleClient()
    return OllamaClient()


class DiagnosisResult:
    """结构化诊断结果。"""

    def __init__(self):
        self.content: str = ""
        self.error_tag: Optional[str] = None
        self.weak_points: List[str] = []
        self.suggested_next_action: Optional[str] = None


class DiagnosisEngine:
    """诊断引擎：调用 LLM 分析错误原因，并返回结构化结果。"""

    def __init__(self):
        self.llm = _create_llm_client()

    def diagnose(self, context: AgentContext) -> DiagnosisResult:
        result = DiagnosisResult()
        prompt = self._build_prompt(context)
        response = self.llm.generate(prompt, temperature=0.5)

        if response is None:
            response = self._fallback_diagnosis(context)

        result.content = response
        self._parse_structured_data(context, result)
        return result

    def _build_prompt(self, context: AgentContext) -> str:
        submission = context.submission
        status = submission.status if submission else "NONE"
        error_message = submission.error_message if submission and submission.error_message else "无"

        return f"""你是一位编程学习教练。请分析学生的错误并给出诊断。

【题目信息】
- 题目：{context.problem.title}
- 难度：{context.problem.difficulty}
- 知识点：{', '.join(context.problem.knowledge_points)}

【提交信息】
- 状态：{status}
- 错误信息：{error_message}
- 连续失败次数：{context.consecutive_failures}

【任务】
请分析：
1. 最可能的错误原因是什么？
2. 学生当前的薄弱点在哪里？
3. 下一步应该怎么修正？

用中文回答，简洁明确，控制在 100 字以内。"""

    def _parse_structured_data(self, context: AgentContext, result: DiagnosisResult):
        submission = context.submission
        error_msg = submission.error_message if submission and submission.error_message else ""
        status = submission.status if submission else ""
        knowledge_points = context.problem.knowledge_points
        points_text = str(knowledge_points).lower()

        if "TLE" in error_msg or status == "TLE":
            result.error_tag = "TIME_COMPLEXITY"
            result.weak_points = ["算法复杂度分析", "时间优化"]
            result.suggested_next_action = "EXPLAIN_COMPLEXITY"
        elif "MLE" in error_msg or status == "MLE":
            result.error_tag = "MEMORY_MANAGEMENT"
            result.weak_points = ["空间复杂度", "内存管理"]
            result.suggested_next_action = "EXPLAIN_MEMORY"
        elif "RE" in error_msg or status == "RE" or "越界" in error_msg:
            result.error_tag = "ARRAY_BOUNDARY"
            result.weak_points = ["数组索引", "边界检查"]
            result.suggested_next_action = "EXPLAIN_BOUNDARY"
        elif "空指针" in error_msg or "NullPointer" in error_msg:
            result.error_tag = "NULL_POINTER"
            result.weak_points = ["空值检查", "指针安全"]
            result.suggested_next_action = "EXPLAIN_NULL_SAFETY"
        elif "WA" in error_msg or status == "WA":
            result.error_tag = "LOGIC_ERROR"
            if "array" in points_text or "数组" in points_text:
                result.weak_points = ["数组操作", "边界处理"]
            elif "hashmap" in points_text or "哈希" in points_text:
                result.weak_points = ["哈希表使用", "键值映射"]
            else:
                result.weak_points = knowledge_points[:2] if knowledge_points else ["基础逻辑"]
            result.suggested_next_action = "HINT"
        else:
            result.error_tag = "UNKNOWN"
            result.weak_points = knowledge_points[:2] if knowledge_points else ["基础知识"]
            result.suggested_next_action = "HINT"

    def _fallback_diagnosis(self, context: AgentContext) -> str:
        submission = context.submission
        status = submission.status if submission else "NONE"
        if status == "WA":
            return "答案错误。请检查代码逻辑，特别关注边界条件和特殊情况。"
        if status == "RE":
            return "运行时错误。请检查数组越界、空指针等常见错误。"
        if status == "TLE":
            return "运行超时。请重新评估算法复杂度，减少不必要的重复遍历。"
        return "请先定位最小失败用例，再判断问题属于逻辑、边界、复杂度还是语法。"
