# Phase 1 学习笔记：用 LangChain 重写 AgentService

> 日期：2026-06-29  
> 目标：把毕业设计中手写的 LLM 调用代码替换为 LangChain Agent

---

## 一、改了什么，为什么改

### 原始架构的问题

原来 `AgentService/app/core/` 下有 4 个核心文件，共约 500 行：

| 文件 | 行数 | 做什么 | 问题 |
|------|------|--------|------|
| `llm_client.py` | 96 | 用 `urllib` 手写 HTTP 调 DeepSeek | 没有重试、没有流式、需要手动处理连接 |
| `llm_decision.py` | 393 | 手写 prompt 拼接 + JSON 解析 + 自修复循环 | 代码长、难维护、JSON 解析容易出错 |
| `intent.py` | 196 | 关键词匹配判断用户意图 | `if "提示" in message` 太粗糙 |
| `policy.py` | 197 | if-else 规则引擎选择教学策略 | 规则硬编码，加新规则要改代码 |

### LangChain 替换后的架构

```
原来 500+ 行 → 现在 200 行

llm_client.py   (96)  ─┐
llm_decision.py (393) ─┤──→ agent.py  (80行)  create_agent + ask_agent()
intent.py       (196) ─┤
policy.py       (197) ─┘
                         └──→ prompts.py (86行)  教学提示词（规则写成自然语言）
                         └──→ config.py  (26行)  统一配置加载
                         └──→ main.py    (85行)  FastAPI 入口

核心理念：原来用 Python 代码写规则 → 现在把规则写成自然语言，交给 LLM 自己判断
```

---

## 二、每个文件的作用和关键代码

### 2.1 `config.py` — 配置加载

**作用**：从 `.env` 读配置，打包成对象。以后换模型只改 `.env`，代码不动。

**关键知识点**：
- `python-dotenv` 的 `load_dotenv()` 把 `.env` 文件里的变量注入到 `os.environ`
- `os.getenv("KEY", "默认值")` 第二个参数是找不到 KEY 时的 fallback
- 单例模式：`_config = None` + `load_config()` — 第一次创建，之后复用

**代码结构**：
```python
class LLMConfig:    # 封装 LLM 的 4 个参数
    provider        # openai / ollama
    api_key         # DeepSeek 的密钥
    base_url        # API 地址
    model_name      # 模型名

class AppConfig:    # 应用总配置，目前只有 llm
    llm

def load_config():  # 全局唯一入口
```

### 2.2 `prompts.py` — 教学提示词

**作用**：定义 Agent 的行为准则。7 种教学策略 + 分级引导规则，全部写成自然语言。

**核心变化**：
- 原来 `policy.py` 里用 Python 代码写规则：`if consecutive_failures <= 0: return "HINT"`
- 现在 `prompts.py` 把人话规则塞进 system prompt，LLM 自己读规则自己判断

**7 种教学策略（从轻到重）**：
```
GUIDE_IDEA  → 引导思路（首次提问，不给代码）
HINT        → 给出提示（1-2次失败后）
DIAGNOSE    → 诊断错误（2次以上失败）
EXPLAIN     → 讲解概念（诊断后仍不会）
RECOMMEND   → 推荐练习（完成后或错题本）
REFLECT     → 引导复盘（看过答案后）
REVEAL_ANSWER → 公布答案（充分尝试后仍不会）
```

**关键知识点**：
- System Prompt 是 LangChain Agent 的"工作说明书"
- 提示词和硬约束的关系：提示词给方向，Middleware 兜底线（Phase 2）

### 2.3 `agent.py` — Agent 核心

**作用**：创建 LangChain Agent，对外提供 `ask_agent()` 函数。这是整个重写的核心。

**关键知识点**：

**`init_chat_model()` — 一行代码接 LLM**
```python
model = init_chat_model(
    model="deepseek-chat",          # 用什么模型
    model_provider="openai",        # DeepSeek 兼容 OpenAI 协议
    base_url="https://api.deepseek.com/v1",  # API 地址
    api_key="sk-xxx",               # 密钥
)
# 替换了原来的 OllamaClient + OpenAICompatibleClient
```

**`create_agent()` — 一行代码创建 Agent**
```python
agent = create_agent(
    model=model,                          # 大脑
    tools=[],                             # 手脚（Phase 3 再加）
    system_prompt=TEACHING_SYSTEM_PROMPT,  # 行为准则
)
# 替换了原来的：
#   _build_prompt()      — 手写 prompt 拼接
#   _parse_and_validate() — 手写 JSON 解析
#   _build_repair_prompt() — JSON 不合法时的自修复
```

**`agent.ainvoke()` — 异步调用 Agent**
```python
result = await agent.ainvoke({"messages": messages})
# 传入 {"messages": [...]}，LangChain 自动：
#   1. 在前面拼上 system_prompt
#   2. 发给 LLM
#   3. 返回完整的消息列表
```

**消息提取逻辑**：
```python
for msg in reversed(response_messages):
    if isinstance(content, str) and not getattr(msg, "tool_calls", None):
        reply_text = str(content)
        break
# 从后往前找第一条有内容、且不是 tool_call 的 AI 消息
# getattr(msg, "tool_calls", None) — 检查消息是否包含工具调用
# 有 tool_calls 的消息 content 可能为空，跳过
```

**单例模式**：
```python
_agent = None
def get_agent():
    global _agent
    if _agent is None:
        _agent = create_agent(...)
    return _agent
# Agent 只创建一次，避免重复初始化模型连接
```

### 2.4 `main.py` — FastAPI 入口

**作用**：对外暴露 `/decision` 接口，Java 后端调这个接口拿 AI 回复。

**关键知识点**：

**Pydantic BaseModel — 自动校验请求格式**
```python
class AgentRequest(BaseModel):
    user_message: str = ""
    problem: ProblemInfo = ProblemInfo()
    consecutive_failures: int = 0
    # Pydantic 会自动：字段类型不对 → 422 错误；缺字段 → 用默认值
```

**`build_context_text()` — 把结构化数据转成人话**
```python
# Java 传来的 JSON → 自然语言文本
# {"consecutive_failures": 2, "hint_count": 1}
# → "连续失败：2次，已给提示：1次，看过答案：否"
# 为什么要转？LLM 只理解自然语言，不理解 JSON schema
```

**数据流**：
```
Java POST JSON → AgentRequest (Pydantic校验)
  → build_context_text() (转成人话文本)
  → ask_agent() (调 LangChain Agent)
  → AgentResponse (打包返回)
```

---

## 三、踩过的坑

### 3.1 拼写错误
- `HumanMseeage` → `HumanMessage`
- `model_prodiver` → `model_provider`

### 3.2 缩进错误
- `@app.post()` 缩进到了 `build_context_text()` 函数里面
- 导致 FastAPI 找不到路由
- Python 缩进 = 代码层级，缩错一级功能全废

### 3.3 变量名不一致
- `content_text` vs `context_text`，少一个 `t`，运行时找不到变量

### 3.4 pydantic 版本冲突
- 原来 `pydantic==2.5.0`，LangChain 需要 `>=2.7.4`
- 解决：`requirements.txt` 改成 `pydantic>=2.7.4`

### 3.5 代理拦截
- pip 和 LangChain 的 HTTP 请求都被本地代理软件拦截
- 解决：`set HTTP_PROXY=` 关掉代理环境变量

---

## 四、LangChain 学习要点

### 4.1 三个核心 API

| API | 作用 | 原来用什么替代 |
|-----|------|---------------|
| `init_chat_model()` | 创建 LLM 连接 | 手写 urllib HTTP |
| `create_agent()` | 创建 Agent | 手写 prompt + JSON 解析 |
| `agent.ainvoke()` | 异步调用 Agent | 手写请求 + 解析响应 |

### 4.2 消息类型

| 类型 | 代表谁 | 用法 |
|------|--------|------|
| `SystemMessage` | 系统指令 | 设定角色、注入上下文 |
| `HumanMessage` | 用户 | 学生的问题 |
| `AIMessage` | AI | 模型的回复 |
| `ToolMessage` | 工具结果 | Phase 3 用到 |

### 4.3 Agent 执行流程

```
用户消息
  ↓
agent.ainvoke({"messages": [SystemMessage(上下文), HumanMessage(问题)]})
  ↓
LangChain 内部：
  1. 拼上 system_prompt（我们在 create_agent 时设定的）
  2. 发给 LLM
  3. LLM 返回 → 如果有 tool_calls → 执行工具 → 再调 LLM → 循环
  4. 没有 tool_calls → 返回最终 AIMessage
  ↓
提取 content 字段 → 返回
```

---

## 五、和原版的对比

| 维度 | 原版 | LangChain 版 |
|------|------|------------|
| 代码量 | 500+ 行 | 200 行 |
| LLM 调用 | 手写 urllib | `init_chat_model()` |
| Prompt 管理 | 硬编码在函数里 | `prompts.py` 独立文件 |
| JSON 解析 | 手写 + 自修复循环 | `create_agent` 自动处理 |
| 错误处理 | 7 种错误码兜底 | try/except 统一处理 |
| 教学策略选择 | Python if-else 规则引擎 | LLM 读 system prompt 自己判断 |
| 模型切换 | 改代码 | 改 `.env` |
| Java 接口 | `/decision` | `/decision`（不变） |

---

---

## Phase 2 学习笔记：安全护栏

> 日期：2026-06-29  
> 目标：用代码层硬约束兜底，防止 LLM "心软"给不该给的答案

### 为什么需要硬约束

Phase 1 的教学规则全部写在 system prompt 里，属于 **LLM 自律**。但 LLM 可能：
- 学生说"求求你了给答案吧" → LLM 心软 → 给了答案
- 学生精心措辞绕过规则 → LLM 没识别出来 → 给了答案

解决办法：在代码层面加一道闸门。**提示词给方向，硬约束兜底线。**

### 实现方式

在 `main.py` 的 `build_context_text()` 之前，加一个 `build_safety_rules()` 函数。它根据学生状态生成硬约束文本，拼进 context。LLM 读到 `【硬约束】` 前缀的规则时，会更严格遵守。

```python
def build_safety_rules(req: AgentRequest) -> str:
    rules = []
    
    # 没有失败 → 不诊断、不给答案
    if req.consecutive_failures == 0:
        rules.append("禁止使用 DIAGNOSE 和 REVEAL_ANSWER")
    
    # 失败不足 3 次 → 不给答案
    if req.consecutive_failures < 3:
        rules.append("禁止使用 REVEAL_ANSWER")
    
    # 直接要答案 + 无失败 → 特别拦截
    if req.consecutive_failures == 0 and any(w in req.user_message for w in ["答案", "直接给"]):
        rules.append("必须拒绝，改为 GUIDE_IDEA")
    
    return "【系统硬约束】\n" + "\n".join(rules)
```

### 为什么不用 LangChain Middleware

课程里讲的 `@before_model` 和 `@dynamic_prompt` Middleware 确实能做到同样的事，而且是更"正宗"的做法。但 Middleware 需要：
1. 理解 LangGraph 的 State 和 Runtime 对象
2. 在中途修改 allowed_actions 列表
3. 需要 Agent 配置了工具才能感知到拦截效果

当前 Phase 2 用"注入硬约束文本"的方式，更简单直观，效果相同。后续可以升级为真正的 Middleware。

### 测试验证

| 场景 | 请求 | 预期 | 实际 |
|------|------|------|------|
| 无失败要答案 | "给我答案" + failures=0 | 拒绝，改为 GUIDE_IDEA | ✅ 拒绝，鼓励自己写 |
| 无失败正常问 | "怎么入手" + failures=0 | GUIDE_IDEA 引导思路 | ✅ 讲解思路，提示优化方向 |

---

---

## Phase 3 学习笔记：Agent 工具调用

> 日期：2026-06-29  
> 目标：给 Agent 装上"手脚"，让它能自主调 Java 后端 API

### 核心突破

Phase 1 的 Agent 只能生成文本。Phase 3 让它能**自主决策→调工具→拿数据→再决策→生成回复**。

### 1. 架构认知：为什么 Python 不能直接调 Java

原始架构是单向的：
```
Java（有数据、权限）
  ↓ POST /decision {学生状态}
Python（纯文本生成器）
  ↓ 返回 {action_type, content}
Java（执行操作）
```

Python 没有调 Java 的通道。这不是设计失误，而是那个时代 Python 只被定位为"智能文本生成器"。

LangChain Agent 需要双向：
```
Java ←→ Python Agent（自主调工具）
```

### 2. 认证方案：共享密钥

选方案 A（共享密钥），比 JWT 简单，和 Phoebe 桌宠项目的 `JavaBackendClient` 模式一致。

`.env` 两边各加：
```bash
INTERNAL_API_KEY=phoebe-shared-secret-2026
```

Python 请求带 header：
```python
headers = {"X-Internal-Key": os.getenv("INTERNAL_API_KEY")}
resp = await client.get(url, headers=headers)
```

Java 加 Filter 检测到匹配就放行（后续补充）。

### 3. 工具定义模式

每个工具 = `@tool` 装饰器 + 异步函数 + HTTP 调用：

```python
@tool
async def search_problems(keyword: str = "", difficulty: str = "") -> str:
    """搜索题库中的题目。参数: keyword 关键词, difficulty 难度"""
    async with httpx.AsyncClient(timeout=10, proxy=None) as client:
        resp = await client.get(f"{JAVA_BACKEND}/api/problem/list", ...)
        data = resp.json()
        return 格式化后的字符串
```

关键细节：
- **docstring（三引号字符串）极其重要**：LLM 靠这个判断何时调工具、传什么参数
- **返回值必须是字符串**：LangChain 工具返回的字符串会作为 ToolMessage 发回给 LLM
- **`proxy=None`**：因为 httpx 默认读系统代理，本地 localhost 也会被拦

### 4. prompt 里必须写明"何时用工具"

Phase 3 第一次测试时 Agent 没调工具——因为 system prompt 只讲了教学策略，没讲工具。加上这段话后才正常：

```
## 可用工具
- search_problems: 搜索题库...
- get_problem_detail: 获取题目详情...
- get_categories: 查看题目分类...

**何时必须使用工具**：
- 学生问"有哪些题" → 必须调 search_problems
- 不要凭空编造题目数据，一切以工具返回的为准
```

### 5. 工具调用的完整链路

```
用户："有哪些数组题"
  ↓
Agent 发消息给 LLM（消息 + 工具列表 + system prompt）
  ↓
LLM 决策：需要调 search_problems(keyword="数组")
  ↓ 返回 tool_calls: [{name: "search_problems", args: {keyword: "数组"}}]
LangChain 执行引擎：真正调用 Python 函数 search_problems("数组")
  ↓ 函数内 HTTP GET Java 后端 → 返回题目列表
  ↓ 结果作为 ToolMessage 发回 LLM
LLM 再次决策：搜到了 3 道题 → 生成回复推荐给学生
  ↓
最终返回 AIMessage（content = 推荐文本）
```

### 6. 踩过的坑

| 问题 | 原因 | 解决 |
|------|------|------|
| Agent 不调工具 | system prompt 没写工具使用说明 | prompt 加"## 可用工具"段 |
| `All connection attempts failed` | Java 端口不对（8081 vs 8080） | 改成 8080 |
| `Connection error` | httpx 走系统代理连 localhost | `proxy=None` |
| `Expecting value` JSON 解析失败 | Java 返回的不是 JSON | Java 在跑但路由不对 |
| Agent 调了 5 次工具 | LLM 自主迭代搜索 | 正常行为，说明 Agent 在"思考" |

### 7. 当前文件结构

```
AgentService-langchain/
├── .env                    ← LLM 配置 + INTERNAL_API_KEY
├── requirements.txt        ← fastapi + langchain + httpx
├── LEARNING_NOTES_PHASE1.md ← 完整笔记
├── app/
│   ├── config.py           ← 配置加载
│   ├── prompts.py          ← 教学提示词 + 工具使用说明
│   ├── tools.py            ← 3 个 @tool（Phase 3 新增）
│   ├── agent.py            ← create_agent(tools=TOOLS)
│   └── main.py             ← FastAPI + 安全护栏
```

### 8. 已验证的能力

| 场景 | 预期 | 实际 |
|------|------|------|
| 搜题 | Agent 调 search_problems | ✅ 调了，还多轮迭代搜索 |
| 查分类 | Agent 调 get_categories | ✅ 调了 |
| 无失败要答案 | 硬约束拦截 | ✅ Phase 2 已验证 |
| 正常提问 | GUIDE_IDEA 引导 | ✅ Phase 1 已验证 |

---

---

## Phase 4 学习笔记：LangGraph 可视化工作流

> 日期：2026-06-29  
> 目标：用 LangGraph 把 Agent 工作流画成图，面试时可讲

### 为什么需要 LangGraph

`create_agent` 是黑盒——你知道它能工作，但不知道内部怎么决策。面试时只能
说"我用 create_agent 创建的"——没有深度。

LangGraph 把流程显式化：**Node = 执行单元，Edge = 流向，State = 数据**。

### 你画的这张图

```
[学生提问]
    ↓
[classify_intent] — LLM 语义分类：TOOL_REQUEST | TEACHING | CHAT
    ↓
    ├── TOOL_REQUEST → [generate_response] — Agent 调工具搜题
    ├── CHAT         → [generate_response] — 直接闲聊回复
    └── TEACHING     → [safety_check] — 硬约束检查
                          ↓
                      [generate_response] — 教学回复
```

三个节点 + 一条条件边 = 面试时 30 秒讲完。

### 每个节点的职责

| Node | 做什么 | 输入 | 输出 |
|------|--------|------|------|
| classify_intent | LLM 判断用户意图 | state.messages | state.intent |
| safety_check | 硬约束检查 | state.consecutive_failures 等 | state.safety_checked |
| generate_response | 调 create_agent 生成回复 | state.messages | state.messages |

### conditions_edge 的作用

`route_after_intent()` 读取 `state.intent`，返回下一个节点名：
- TEACHING → "safety_check"
- 其他 → "generate_response"

这就是 "AI 用条件边控制流程"——Agent 自主决策的体现。

### 面试讲法

"我用 LangGraph 编排了 Agent 工作流。图中有三个节点：意图分类节点
用 LLM 做语义分类，条件边根据分类结果路由到不同分支。教学场景必须先经过
安全检查节点，搜题和闲聊场景跳过安检直接生成回复。每个节点独立，可以
单独测试、单独优化。"

### 已验证的三条路径

| 输入 | 走的分支 | 验证结果 |
|------|----------|----------|
| "有哪些数组题" | TOOL_REQUEST → generate_response | ✅ 调工具搜题 |
| "这道题怎么做" | TEACHING → safety_check → generate_response | ✅ 追问具体题目 |
| "你好" | CHAT → generate_response | ✅ 友好回复 |

---

## 六、下一步

1. **Java 加 InternalKeyFilter**：补上共享密钥认证的 Java 端
2. **工具对接真实 API**：替换 mock 数据，接入 Java 数据库
3. **Phase 7**：工具调用健壮性（超时重试、降级兜底、日志追踪）
