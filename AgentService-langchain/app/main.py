from fastapi import FastAPI
from pydantic import BaseModel
from app.agent import ask_agent
from app.rag import search_knowledge, import_knowledge

app = FastAPI(title="Learning Agent Service (LangChain)")

# ── 请求模型 ────────────────────────────────────
# 这些类定义了 Java 后端发来的 JSON 格式
# Pydantic BaseModel 会自动校验：字段类型不对、缺少必填字段 → 返回 422 错误
class ProblemInfo(BaseModel):
    """题目信息 —— Java 传来的题目数据"""
    title: str = ""          # 题目标题，如 "两数之和"
    description: str = ""    # 题目描述
    difficulty: str = ""     # 难度：easy / medium / hard


class SubmissionInfo(BaseModel):
    """提交记录 —— 学生最近一次提交的代码和结果"""
    status: str = ""         # 判题结果：AC(通过) / WA(答案错) / RE(运行错) / TLE(超时)
    code: str = ""           # 学生提交的代码
    error_message: str = ""  # 错误信息


class AgentRequest(BaseModel):
    """Java 后端发来的完整请求"""
    request_id: str = ""                           # 请求 ID，用于日志追踪
    user_message: str = ""                          # 学生说的话
    problem: ProblemInfo = ProblemInfo()             # 题目信息
    submission: SubmissionInfo | None = None          # 提交记录（可能为空）
    consecutive_failures: int = 0                    # 连续失败次数
    hint_count: int = 0                              # 已给提示次数
    has_viewed_solution: bool = False                # 是否看过答案
    trigger_source: str = "CHAT"                     # 触发来源


class AgentResponse(BaseModel):
    """返回给 Java 的完整 AgentDecision 格式"""
    # 核心字段（Java 强制校验）
    response_id: str = ""
    action_type: str = ""            # GUIDE_IDEA / HINT / DIAGNOSE 等
    content_type: str = ""           # guidance / hint / diagnosis 等
    main_response: str = ""          # 给学生看的回复
    next_suggestion: str = ""        # 下一步建议
    answer_scope: str = ""           # concept_only / hint_only 等
    selected_strategy: str = ""      # 同 action_type
    decision_summary: str = ""       # 一句话总结
    decision_reason: str = ""        # 为什么做这个决策

    # 辅助字段
    pedagogical_goal: str = ""
    confidence: float = 0.8
    error_tag: str | None = None
    weak_points: list[str] = []

def build_safety_rules(req: AgentRequest) -> str:
    """
    根据学生状态生成硬约束文本。

    这些规则是代码层兜底，不是 LLM 自律。
    即便 LLM "心软"，系统层面也能拦截不当策略。
    """
    rules = []

    # 规则 1：没有失败记录 → 不诊断、不给答案
    if req.consecutive_failures == 0:
        rules.append("- 【硬约束】学生没有失败记录，禁止使用 DIAGNOSE 和 REVEAL_ANSWER 策略")

    # 规则 2：失败不足 3 次 → 不给答案
    if req.consecutive_failures < 3:
        rules.append("- 【硬约束】学生失败不足 3 次，禁止使用 REVEAL_ANSWER 策略")

    # 规则 3：没有失败记录 + 学生直接要答案 → 特别提醒
    if req.consecutive_failures == 0 and any(
        w in req.user_message for w in ["答案", "直接给", "帮我写", "参考代码"]
    ):
        rules.append("- 【硬约束】学生直接要答案但无失败记录，必须拒绝，改为 GUIDE_IDEA")

    # 规则 4：看过答案 → 优先复盘
    if req.has_viewed_solution:
        rules.append("- 【硬约束】学生已看过答案，优先使用 REFLECT 策略引导复盘")

    # 规则 5：纯闲聊不触发教学策略
    if any(w in req.user_message for w in ["你好", "谢谢", "再见"]):
        rules.append("- 这是闲聊，简短友好回复即可")

    if not rules:
        return ""

    return "【系统硬约束 — 必须严格遵守，不得违反】\n" + "\n".join(rules)

#工具函数：把结构化数据转成 LLM 能读的文本
def build_context_text(req: AgentRequest) -> str:
    parts = []
    #题目信息
    if req.problem.title:
        parts.append(f"【当前题目】{req.problem.title}")
        if req.problem.description:
            parts.append(f"题目描述：{req.problem.description[:500]}")
        if req.problem.difficulty:
            parts.append(f"难度：{req.problem.difficulty}")
    
    #提交记录
    if req.submission:
        parts.append(f"【最近提交】状态：{req.submission.status}")
        if req.submission.error_message:
            parts.append(f"错误信息：{req.submission.error_message[:300]}")
        if req.submission.code:
            parts.append(f"学生代码：\n'''\n{req.submission.code[:1000]}\n'''")
    # ── 学习状态（这是 LLM 选择教学策略的核心依据） ──
    parts.append(
        f"【学习状态】"
        f"连续失败：{req.consecutive_failures}次，"
        f"已给提示：{req.hint_count}次，"
        f"看过答案：{'是' if req.has_viewed_solution else '否'}，"
        f"触发来源：{req.trigger_source}"
    )
    return "\n".join(parts) + "\n\n" + build_safety_rules(req)

# ── 结构化决策推断 ─────────────────────────────

# action_type → content_type 映射（Java AgentProtocolConstants）
ACTION_TO_CONTENT = {
    "GUIDE_IDEA": "guidance",
    "HINT": "hint",
    "DIAGNOSE": "diagnosis",
    "EXPLAIN": "explanation",
    "RECOMMEND": "recommendation",
    "REFLECT": "reflection",
    "REVEAL_ANSWER": "solution",
    "CLARIFY_INTENT": "clarification",
}

# action_type → answer_scope 映射
ACTION_TO_SCOPE = {
    "GUIDE_IDEA": "concept_only",
    "HINT": "hint_only",
    "DIAGNOSE": "partial_solution",
    "EXPLAIN": "concept_only",
    "RECOMMEND": "concept_only",
    "REFLECT": "partial_solution",
    "REVEAL_ANSWER": "full_solution",
    "CLARIFY_INTENT": "concept_only",
}


# LLM 判断意图的单例模型（轻量调用，不重复创建）
_decision_model = None


def _get_decision_model():
    global _decision_model
    if _decision_model is None:
        from langchain.chat_models import init_chat_model
        cfg = load_config()
        _decision_model = init_chat_model(
            model=cfg.llm.model_name,
            model_provider="openai",
            base_url=cfg.llm.base_url,
            api_key=cfg.llm.api_key,
        )
    return _decision_model


async def infer_action_llm(req: AgentRequest) -> str:
    """
    用 LLM 语义判断教学策略。

    比关键词匹配更准确——"教教我这题"能识别为 GUIDE_IDEA，
    "为什么 n/2==0 不对"能识别为 DIAGNOSE。
    """
    msg = req.user_message
    failures = req.consecutive_failures

    # 硬规则：有代码+错误 → 直接诊断，不等 LLM
    if req.submission and req.submission.status in ("WA", "RE", "CE") and req.submission.code:
        return "DIAGNOSE"

    prompt = (
        "根据学生状态和问题，选择最合适的教学策略。只回复一个策略名。\n\n"
        "可选策略：\n"
        "- GUIDE_IDEA: 学生首次提问，引导思路不給答案\n"
        "- HINT: 学生有过尝试，给关键提示\n"
        "- DIAGNOSE: 学生提交了错误代码，诊断问题\n"
        "- EXPLAIN: 学生概念不清，讲解知识点\n"
        "- RECOMMEND: 推荐练习，找题\n"
        "- REFLECT: 学生看过答案，引导复盘\n"
        "- REVEAL_ANSWER: 学生充分尝试后，给完整答案\n"
        "- EXPLAIN: 闲聊/打招呼\n\n"
        f"学生消息：{msg[:200]}\n"
        f"连续失败：{failures}次\n"
        f"看过答案：{'是' if req.has_viewed_solution else '否'}\n"
        f"题目：{req.problem.title if req.problem.title else '未指定'}\n\n"
        "策略："
    )

    try:
        model = _get_decision_model()
        result = await model.ainvoke(prompt)
        action = str(result.content).strip().upper()
        valid = {"GUIDE_IDEA", "HINT", "DIAGNOSE", "EXPLAIN", "RECOMMEND", "REFLECT", "REVEAL_ANSWER"}
        if action in valid:
            return action
    except Exception:
        pass

    # LLM 失败 → 回退到关键词规则
    return infer_action(req)


def infer_action(req: AgentRequest) -> str:
    """
    关键词规则版判断——作为 LLM 版的 fallback。
    规则和 Java 端的 StrategySelector 保持一致。
    """
    msg = req.user_message
    failures = req.consecutive_failures

    # 闲聊 → EXPLAIN
    if any(w in msg for w in ["你好", "谢谢", "再见", "hello", "hi"]):
        return "EXPLAIN"

    # 要答案 → 根据失败次数决定
    if any(w in msg for w in ["答案", "直接给", "参考代码", "solution"]):
        if failures >= 3:
            return "REVEAL_ANSWER"
        elif failures >= 2:
            return "DIAGNOSE"
        elif failures >= 1:
            return "HINT"
        else:
            return "GUIDE_IDEA"  # 0次失败要答案 → 引导思路

    # 概念提问 → EXPLAIN
    if any(w in msg for w in ["什么是", "怎么用", "原理", "解释", "概念"]):
        return "EXPLAIN"

    # 搜题请求 → RECOMMEND
    if any(w in msg for w in ["有哪些", "搜一下", "推荐", "找题"]):
        return "RECOMMEND"

    # 看过答案 → REFLECT
    if req.has_viewed_solution:
        return "REFLECT"

    # 有提交记录 + 有错误 → 优先诊断
    if req.submission and req.submission.status in ("WA", "RE", "CE"):
        return "DIAGNOSE"

    # 根据失败次数递进
    if failures == 0:
        return "GUIDE_IDEA"      # 首次提问 → 引导思路
    elif failures <= 2:
        return "HINT"            # 1-2次 → 给提示
    elif failures <= 3:
        return "DIAGNOSE"        # 3次 → 诊断错误
    else:
        return "EXPLAIN"         # 4次以上 → 讲解概念

@app.post("/decision", response_model=AgentResponse)
async def make_decision(req: AgentRequest):
    # ── Phase 6：代码层安全拦截 ──────────────
    # 不满足条件的学生直接拒绝，根本不发给 Agent
    # 这是真闸门，不是"求 LLM 自律"

    wants_answer = any(
        w in req.user_message for w in ["答案", "直接给", "帮我写", "参考代码", "solution"]
    )

    # 拦截 1：没失败 + 要答案 → 直接拒绝
    if req.consecutive_failures == 0 and wants_answer:
        return AgentResponse(
            main_response="我理解你想直接看答案，但学习编程最重要的是自己动手。"
                          "这道题你不妨先试着写一写，哪怕是暴力解法也行。"
                          "写完提交后，我会帮你分析代码、给出针对性的提示。💪",
            action_type="GUIDE_IDEA",
            content_type="guidance",
            answer_scope="concept_only",
            selected_strategy="GUIDE_IDEA",
            decision_summary="拦截：0次失败要答案",
            decision_reason="consecutive_failures=0, wants_answer=true",
        )

    # 拦截 2：失败 < 3 次 + 要答案 → 拒绝
    if req.consecutive_failures < 3 and wants_answer:
        return AgentResponse(
            main_response=f"你已经尝试了 {req.consecutive_failures} 次，但还不到直接看答案的时候。"
                          "建议你再想想，或者把代码发给我，我帮你诊断具体问题。",
            action_type="GUIDE_IDEA",
            content_type="guidance",
            answer_scope="concept_only",
            selected_strategy="GUIDE_IDEA",
            decision_summary=f"拦截：{req.consecutive_failures}次失败要答案",
            decision_reason=f"consecutive_failures={req.consecutive_failures}, wants_answer=true",
        )

    # ── 通过拦截 → 正常流程 ──────────────────
    knowledge_text = search_knowledge(req.user_message)
    if knowledge_text:
        context_text = knowledge_text + "\n\n" + build_context_text(req)
    else:
        context_text = build_context_text(req)

    result = await ask_agent(
        user_message=req.user_message or "请根据学生状态给出教学建议",
        context_text=context_text,
    )
# ── 推断结构化决策标签 ──
    action_type = await infer_action_llm(req)
    content_type = ACTION_TO_CONTENT.get(action_type, "explanation")
    scope = ACTION_TO_SCOPE.get(action_type, "concept_only")

    return AgentResponse(
        response_id=req.request_id or f"req-{id(req)}",
        action_type=action_type,
        content_type=content_type,
        main_response=result["content"],
        next_suggestion="如果还有疑问，可以继续问我。",
        answer_scope=scope,
        selected_strategy=action_type,
        decision_summary=f"动作: {action_type}, 失败次数: {req.consecutive_failures}",
        decision_reason=_build_decision_reason(req, action_type),
        confidence=0.8,
    )

def _build_decision_reason(req: AgentRequest, action_type: str) -> str:
    return (
        f"consecutive_failures={req.consecutive_failures}, "
        f"hint_count={req.hint_count}, "
        f"has_viewed_solution={req.has_viewed_solution}, "
        f"user_message={req.user_message[:50]!r}, "
        f"action={action_type}"
    )

@app.get("/health")
async def health():
    return {"status": "ok", "service": "learning-agent-service-langchain"}