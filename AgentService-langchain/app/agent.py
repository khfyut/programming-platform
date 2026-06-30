# 导入LangChain组件
# init_chat_model: 一行代码接入 DeepSeek
from langchain.chat_models import init_chat_model

#create_agent:一行代码创建 Agent
from langchain.agents import create_agent

#HumanMseeage/SystemMessage:构造发给LLM的消息
from langchain.messages import HumanMessage, SystemMessage

from app.config import load_config
from app.prompts import TEACHING_SYSTEM_PROMPT

from app.tools import TOOLS

#Agent单例
_agent = None

def get_agent():
    global _agent
    if _agent is None:
        config = load_config()
        model = init_chat_model(
            model = config.llm.model_name,
            model_provider = "openai",
            base_url = config.llm.base_url,
            api_key = config.llm.api_key,
        )

        _agent = create_agent(
            model = model,
            tools = TOOLS,
            system_prompt = TEACHING_SYSTEM_PROMPT,
        )

    return _agent

# 对外接口
async def ask_agent(user_message: str, context_text: str = "") -> dict:
    try:
        agent = get_agent()
        # 构造消息列表
        messages = []
        if context_text:
            messages.append(
                SystemMessage(
                    content = (
                        "以下是学生的当前状态，"
                        "请严格依据这些信息，按照教学策略规则选择回复方式：\n\n"
                        f"{context_text}"
                    )
                )
            )
        messages.append(HumanMessage(content = user_message))

        # 调用Agent
        # ainvoke() 是异步调用，会等待 LLM 回复完整生成
        # 传入 {"messages": messages}，LangChain 会自动：
        #   1. 在前面拼上 system_prompt
        #   2. 发给 LLM
        #   3. 拿到回复
        result = await agent.ainvoke({"messages":messages})

        # 提取回复文本
        # result["messages"] 是对话的完整消息列表
        # 最后一条 AIMessage 就是 LLM 的回复
        response_messages = result.get("messages", [])
        reply_text = ""

        # 遍历消息列表
        for msg in reversed(response_messages):
            content = getattr(msg, "content", "")
            if content and isinstance(content, str) and content.strip():
                if not getattr(msg, "tool_calls", None):
                    reply_text = str(content)
                    break
        # 收集所有工具调用信息
        tool_calls_made = []
        for msg in response_messages:
            tc = getattr(msg, "tool_calls", None)
            if tc:
                for t in tc:
                    tool_calls_made.append({
                        "name": t.get("name", ""),
                        "args": t.get("args", {}),
                    })            
        return {
            "success": True,
            "content": reply_text or "抱歉，我没有生成有效的回复。",
            "tool_calls": tool_calls_made,
            "error": None,
        }
    except Exception as e:
        # ── 错误处理 ──────────────────────────
        # 原来 llm_decision.py 有 7 种错误兜底，
        # 这里简化为一句话，详细的错误信息存到 error 字段
        return{
            "success": False,
            "content": "AI服务暂时不可用，请稍后再试。",
            "error": str(e),
        }