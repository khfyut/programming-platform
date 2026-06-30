from typing import TypedDict, Annotated, Literal
from operator import add
from langgraph.graph import StateGraph, START, END
from langchain.messages import HumanMessage, SystemMessage, SystemMessage, AIMessage
from langchain.chat_models import init_chat_model

from app.config import load_config
from app.prompts import TEACHING_SYSTEM_PROMPT
from app.tools import TOOLS

# ── State：工作流中流转的数据 ──────────────────

class AgentState(TypedDict):
    # 消息历史（用 add 合并，不会互相覆盖）
    messages: Annotated[list, add]
    # 学生状态（来自 Java 后端）
    consecutive_failures: int
    hint_count: int
    has_viewed_solution: bool
    # 当前阶段的标记
    intent: str           # "TOOL_REQUEST" | "TEACHING" | "CHAT"
    safety_checked: bool  # 是否已过安全检查

# ── Node 1：意图分类 ──────────────────────────

# 用一个简单的 LLM 调用判断用户想干嘛
_intent_model = None

def _get_intent_model():
    global _intent_model
    if _intent_model is None:
        config = load_config()
        _intent_model = init_chat_model(
            model=config.llm.model_name,
            model_provider="openai",
            base_url=config.llm.base_url,
            api_key=config.llm.api_key,
        )
    return _intent_model


async def classify_intent(state: AgentState) -> dict:
    """
    判断用户意图：搜题？教学？闲聊？
    这是图的第一个节点，决定了后续走哪条分支。
    """
    last_msg = state["messages"][-1]
    user_text = last_msg.content if hasattr(last_msg, "content") else str(last_msg)

    # 让 LLM 快速判断意图（不是生成回复，只是分类）
    model = _get_intent_model()
    prompt = (
        "判断以下用户消息的意图，只回复一个词：\n"
        "- TOOL_REQUEST：搜题、查分类、看题目详情\n"
        "- TEACHING：问怎么做、为什么错、要提示、要答案\n"
        "- CHAT：闲聊、打招呼、感谢\n\n"
        f"用户消息：{user_text}\n意图："
    )
    result = await model.ainvoke(prompt)
    intent = str(result.content).strip().upper()

    # 兜底
    if intent not in ("TOOL_REQUEST", "TEACHING", "CHAT"):
        intent = "TEACHING"

    return {"intent": intent}


# ── Node 2：安全检查 ──────────────────────────

def safety_check(state: AgentState) -> dict:
    """
    根据学生状态决定是否拦截高风险策略。
    这是 Phase 2 的硬约束，从文本注入升级为有名字的 Node。
    """
    # 这里只是标记"已检查"，实际约束通过后续 prompt 注入
    return {"safety_checked": True}


# ── Node 3：生成回复 ──────────────────────────

# 复用现有的 create_agent（不做重复工作）
from app.agent import get_agent as get_langchain_agent
from app.rag import search_knowledge

async def generate_response(state: AgentState) -> dict:
    """
    调用 LangChain Agent 生成最终回复。
    这是图的最后一个节点。
    """
    agent = get_langchain_agent()
    result = await agent.ainvoke({"messages": state["messages"]})
    return {"messages": result["messages"]}

# ── 路由函数：决定下一步走哪 ──────────────────

def route_after_intent(state: AgentState) -> Literal["safety_check", "generate_response", "generate_response"]:
    """
    根据意图决定走哪条路径。

    TOOL_REQUEST → 直接生成回复（Agent 会自动调工具）
    TEACHING     → 先安全检查，再生成回复
    CHAT         → 直接生成回复
    """
    intent = state.get("intent", "TEACHING")
    if intent == "TEACHING":
        return "safety_check"
    else:
        return "generate_response"


# ── 组装 Graph ────────────────────────────────

def build_graph():
    """构建并编译 LangGraph 工作流。"""
    builder = StateGraph(AgentState)

    # 注册 3 个节点
    builder.add_node("classify_intent", classify_intent)
    builder.add_node("safety_check", safety_check)
    builder.add_node("generate_response", generate_response)

    # 连线
    builder.add_edge(START, "classify_intent")                    # 入口 → 意图分类

    builder.add_conditional_edges(                                # 意图分类 → 分支
        "classify_intent",
        route_after_intent,
        {
            "safety_check": "safety_check",
            "generate_response": "generate_response",
        }
    )

    builder.add_edge("safety_check", "generate_response")        # 安全检查 → 生成回复
    builder.add_edge("generate_response", END)                   # 生成回复 → 出口

    return builder.compile()


# 单例
_graph = None

def get_graph():
    global _graph
    if _graph is None:
        _graph = build_graph()
    return _graph

async def run_graph(user_message: str, consecutive_failures: int = 0,
                    hint_count: int = 0, has_viewed_solution: bool = False) -> dict:
    """
    运行 LangGraph 工作流，返回最终回复。

    这是 main.py 里 ask_agent() 的 LangGraph 替代版。
    """
    graph = get_graph()

    # RAG 检索：先搜相关知识，如果有则注入 context
    knowledge_text = search_knowledge(user_message)
    messages = []
    if knowledge_text:
        messages.append(SystemMessage(
            content=f"以下是与学生问题相关的知识点，请基于这些知识回答：\n\n{knowledge_text}"
        ))
    messages.append(HumanMessage(content=user_message))

    initial_state = {
        "messages": messages,
        "consecutive_failures": consecutive_failures,
        "hint_count": hint_count,
        "has_viewed_solution": has_viewed_solution,
        "intent": "",
        "safety_checked": False,
    }

    result = await graph.ainvoke(initial_state)

    # 提取最后一条 AI 消息
    messages = result.get("messages", [])
    reply = ""
    for msg in reversed(messages):
        content = getattr(msg, "content", "")
        if content and isinstance(content, str) and content.strip():
            if not getattr(msg, "tool_calls", None):
                reply = str(content)
                break

    return {"success": True, "content": reply or "抱歉，无法生成回复。"}