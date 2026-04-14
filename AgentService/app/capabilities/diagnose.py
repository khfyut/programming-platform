# 诊断能力模块（分析代码错误）
from app.core.llm_client import OllamaClient
from app.schemas.context import AgentContext
from typing import Dict, List, Optional


class DiagnosisResult:
    """
    结构化诊断结果
    """
    def __init__(self):
        self.content: str = ""  # 展示给用户的文本内容
        self.error_tag: Optional[str] = None  # 错误标签
        self.weak_points: List[str] = []  # 薄弱知识点
        self.suggested_next_action: Optional[str] = None  # 建议的下一步


class DiagnosisEngine:
    """
    诊断引擎：调用LLM分析错误原因，返回结构化结果
    """

    def __init__(self):
        self.llm = OllamaClient()

    def diagnose(self, context: AgentContext) -> DiagnosisResult:
        """
        分析用户错误，返回结构化诊断结果
        """
        result = DiagnosisResult()

        # 构建提示词
        prompt = self._build_prompt(context)

        # 调用LLM
        response = self.llm.generate(prompt, temperature=0.5)

        # 如果LLM失败，使用默认诊断
        if response is None:
            response = self._fallback_diagnosis(context)

        # 设置内容
        result.content = response

        # 解析结构化数据（基于错误类型和上下文）
        self._parse_structured_data(context, result)

        return result

    def _build_prompt(self, context: AgentContext) -> str:
        """
        构建诊断提示词
        """
        return f"""你是一位编程教学助手。请分析学生的错误并给出诊断。

【题目信息】
- 题目：{context.problem.title}
- 难度：{context.problem.difficulty}
- 知识点：{', '.join(context.problem.knowledge_points)}

【提交信息】
- 状态：{context.submission.status}
- 错误信息：{context.submission.error_message or '无'}
- 失败次数：{context.consecutive_failures}

【任务】
请分析：
1. 最可能的错误原因是什么？
2. 学生当前的知识薄弱点在哪里？
3. 应该给出什么建议？

用中文回答，简洁明了（100字以内）。"""

    def _parse_structured_data(self, context: AgentContext, result: DiagnosisResult):
        """
        从上下文解析结构化诊断数据

        基于错误类型和题目信息，生成：
        - error_tag: 错误标签
        - weak_points: 薄弱知识点
        - suggested_next_action: 建议的下一步动作
        """
        error_msg = context.submission.error_message or ""
        status = context.submission.status
        knowledge_points = context.problem.knowledge_points

        # 根据错误类型和错误信息推断error_tag
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
            # 根据知识点推断薄弱点
            if "array" in knowledge_points or "数组" in str(knowledge_points):
                result.weak_points = ["数组操作", "边界处理"]
            elif "hashmap" in knowledge_points or "哈希" in str(knowledge_points):
                result.weak_points = ["哈希表使用", "键值映射"]
            else:
                result.weak_points = knowledge_points[:2] if knowledge_points else ["基础逻辑"]
            result.suggested_next_action = "HINT"
        else:
            result.error_tag = "UNKNOWN"
            result.weak_points = knowledge_points[:2] if knowledge_points else ["基础知识"]
            result.suggested_next_action = "HINT"

    def _fallback_diagnosis(self, context: AgentContext) -> str:
        """
        LLM失败时的默认诊断
        """
        if context.submission.status == "WA":
            return "答案错误。请检查代码逻辑，特别关注边界条件和特殊情况。"
        elif context.submission.status == "RE":
            return "运行时错误。请检查数组越界、空指针等常见错误。"
        elif context.submission.status == "TLE":
            return "超时。请考虑优化算法复杂度。"
        else:
            return "请仔细检查代码，找出问题所在。"