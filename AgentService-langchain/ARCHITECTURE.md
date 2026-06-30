# 在线编程学习平台 — 完整架构文档

## 一、系统全景

```
┌─────────────────────────────────────────────────────────────────┐
│                        前端 Vue 3 + Vite                        │
│  ┌──────────┐ ┌──────────────┐ ┌───────────┐ ┌─────────────┐   │
│  │ AI 对话页│ │ 题目陪练侧栏  │ │错题复盘Agent│ │全局学习助手 │   │
│  │  /ai     │ │ProblemCoach  │ │WrongBook   │ │GlobalCoach  │   │
│  └────┬─────┘ └──────┬───────┘ └─────┬─────┘ └──────┬──────┘   │
│       │              │               │              │           │
└───────┼──────────────┼───────────────┼──────────────┼───────────┘
        │              │               │              │
        ▼              ▼               ▼              ▼     HTTP
┌───────────────────────────────────────────────────────────────────┐
│                    Java Spring Boot :8080                          │
│                                                                   │
│  ┌───────────────────┐  ┌──────────────────┐  ┌───────────────┐  │
│  │   AgentController  │  │ ProblemAgentCont │  │ WrongBookCont │  │
│  │   /api/agent/...   │  │ /api/ai/problem  │  │ /api/ai/wrong │  │
│  └────────┬──────────┘  └────────┬─────────┘  └───────┬───────┘  │
│           │                      │                     │          │
│           ▼                      ▼                     ▼          │
│  ┌──────────────────────────────────────────────────────────┐    │
│  │              AgentService.decideAndApply()                │    │
│  │                                                          │    │
│  │  AgentContextBuilder  →  AgentClient.getDecision()       │    │
│  │       ↓                         ↓                        │    │
│  │  构造学生状态              POST /decision (JSON)          │    │
│  │       ↓                         ↓                        │    │
│  │  AgentDecisionEnforcer  ←  AgentDecisionDTO              │    │
│  │  (校验 action_type, 策略合规)                              │    │
│  └──────────────────────────────────────────────────────────┘    │
│                                                                   │
│  ┌────────────┐ ┌──────────┐ ┌────────────┐ ┌────────────────┐  │
│  │ JudgeService│ │QuestionSvc│ │WrongBookSvc│ │LearningPathSvc │  │
│  │ Docker 沙箱 │ │ 题库 CRUD│ │ 错题管理   │ │ 学习路径规划   │  │
│  └──────┬─────┘ └────┬─────┘ └─────┬──────┘ └───────┬────────┘  │
└─────────┼────────────┼────────────┼───────────────┼──────────────┘
          │            │            │               │
          ▼            ▼            ▼               ▼
┌───────────────────────────────────────────────────────────────────┐
│                       MySQL :3306                                  │
│  programming_system: user, question, submission, wrong_book, ...  │
└───────────────────────────────────────────────────────────────────┘

                              │  HTTP POST /decision
                              ▼
┌───────────────────────────────────────────────────────────────────┐
│                 Python FastAPI :8766  (LangChain 重构版)           │
│                                                                   │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                    main.py  ── 请求入口                       │ │
│  │                                                              │ │
│  │  ① 代码层安全拦截（Phase 6）                                  │ │
│  │     └─ failures==0 + "答案" → 秒回拒绝                       │ │
│  │                                                              │ │
│  │  ② RAG 知识检索（Phase 5）                                    │ │
│  │     └─ ChromaDB.search_knowledge(用户问题)                    │ │
│  │                                                              │ │
│  │  ③ 教学策略推断（方案B LLM版）                                │ │
│  │     └─ infer_action_llm() → GUIDE_IDEA/HINT/DIAGNOSE/...    │ │
│  │                                                              │ │
│  │  ④ 调用 Agent 生成回复                                       │ │
│  │     └─ ask_agent() → create_agent → DeepSeek LLM             │ │
│  │                                                              │ │
│  │  ⑤ 组装 AgentDecision 返回 Java                              │ │
│  │     └─ action_type + content_type + answer_scope + ...       │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                   │
│  ┌──────────────┐ ┌──────────────┐ ┌─────────────────────────┐   │
│  │   graph.py   │ │   rag.py     │ │      tools.py           │   │
│  │ LangGraph 编排│ │ ChromaDB 知识│ │  Agent 工具             │   │
│  │              │ │              │ │  search_problems        │   │
│  │ classify_int │ │ ONNX 本地向量│ │  get_problem_detail     │   │
│  │ safety_check │ │ 6条编程知识  │ │  get_categories         │   │
│  │ generate_... │ │              │ │  → HTTP 调 Java API     │   │
│  └──────────────┘ └──────────────┘ └─────────────────────────┘   │
│                                                                   │
│  ┌──────────────────────────────────────────────────────────────┐ │
│  │                     LangSmith 可观测性                        │ │
│  │  每次 Agent 调用的完整 trace: prompt → 推理 → 工具 → 响应     │ │
│  └──────────────────────────────────────────────────────────────┘ │
└───────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌───────────────────────────────────────────────────────────────────┐
│                        DeepSeek API                                │
│                    https://api.deepseek.com/v1                     │
│                      模型: deepseek-chat                           │
└───────────────────────────────────────────────────────────────────┘
```

---

## 二、Agent 教学策略决策流

```
                    Java POST /decision
                           │
                           ▼
┌──────────────────────────────────────────────────────────┐
│              main.py: make_decision()                     │
│                                                          │
│  Step 1: 安全拦截                                        │
│  ┌──────────────────────────────────────────────────┐   │
│  │ failures==0 + "答案" → return GUIDE_IDEA 拒绝    │   │
│  │ failures<3  + "答案" → return GUIDE_IDEA 拒绝    │   │
│  └──────────────────────────────────────────────────┘   │
│                    ↓ (通过拦截)                           │
│  Step 2: RAG 知识检索                                    │
│  ┌──────────────────────────────────────────────────┐   │
│  │ search_knowledge(query) → ChromaDB 相似度检索     │   │
│  │ 如有结果 → 拼入 context                          │   │
│  └──────────────────────────────────────────────────┘   │
│                    ↓                                     │
│  Step 3: LLM 策略推断                                    │
│  ┌──────────────────────────────────────────────────┐   │
│  │ infer_action_llm(req)                            │   │
│  │  └─ 有 WA/RE + 代码 → DIAGNOSE (硬规则)          │   │
│  │  └─ LLM 语义判断 → GUIDE_IDEA/HINT/EXPLAIN/...   │   │
│  │  └─ LLM 失败 → infer_action (关键词 fallback)     │   │
│  └──────────────────────────────────────────────────┘   │
│                    ↓                                     │
│  Step 4: Agent 生成回复                                  │
│  ┌──────────────────────────────────────────────────┐   │
│  │ ask_agent(user_message, context_text)             │   │
│  │  └─ create_agent(model, tools, system_prompt)     │   │
│  │  └─ agent.ainvoke({"messages": [...]})            │   │
│  │  └─ 提取 AIMessage.content                       │   │
│  └──────────────────────────────────────────────────┘   │
│                    ↓                                     │
│  Step 5: 组装响应                                       │
│  ┌──────────────────────────────────────────────────┐   │
│  │ AgentDecision {                                  │   │
│  │   action_type, content_type, main_response,       │   │
│  │   answer_scope, selected_strategy,                │   │
│  │   decision_summary, decision_reason               │   │
│  │ } → 返回 Java                                     │   │
│  └──────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────┘
```

---

## 三、LangGraph 工作流（graph.py）

```
                     [学生提问]
                         │
                         ▼
             ┌───────────────────────┐
             │  classify_intent       │
             │  LLM 语义分类          │
             │  TOOL_REQUEST /        │
             │  TEACHING / CHAT       │
             └───────────┬───────────┘
                         │
                         ▼
              ┌─────────────────────┐
              │ route_after_intent   │  条件边
              └──┬────────┬─────────┘
                 │        │
        ┌────────┘        └────────┐
        ▼                          ▼
┌──────────────┐          ┌──────────────────┐
│ TOOL_REQUEST │          │    TEACHING       │
│ CHAT         │          │                   │
│              │          │        ▼          │
│     │        │          │ ┌──────────────┐  │
│     │        │          │ │ safety_check │  │
│     │        │          │ │ 硬约束检查    │  │
│     │        │          │ └──────┬───────┘  │
│     │        │          │        │          │
└─────┼────────┘          └────────┼──────────┘
      │                            │
      ▼                            ▼
┌─────────────────────────────────────────────┐
│           generate_response                  │
│           调 create_agent 生成回复           │
│           Agent 可自主调 tools 搜索          │
└─────────────────────┬───────────────────────┘
                      │
                      ▼
                   [返回]
```

---

## 四、7 种教学策略递进规则

```
学生状态                        → 策略             → 做什么
───────────────────────────────────────────────────────────
首次提问, failures==0          → GUIDE_IDEA        → 引导思路，不给代码
1-2次失败                       → HINT              → 给关键提示
有 WA/RE + 代码                 → DIAGNOSE          → 诊断具体错误
诊断后仍不会                    → EXPLAIN           → 讲解概念
完成了当前题                    → RECOMMEND         → 推荐相似题
看过答案后提问                  → REFLECT           → 引导复盘
>=3次失败 + 已诊断              → REVEAL_ANSWER     → 给完整解答
直接要答案 + failures<3         → (拦截) GUIDE_IDEA → 拒绝给答案
```

策略决策优先级（infer_action_llm）：
1. 有提交代码 + WA/RE → DIAGNOSE（硬规则，不走 LLM）
2. LLM 语义判断 → 根据消息含义 + 失败次数选择策略
3. LLM 失败 → 关键词规则 fallback（`if "答案" in msg` 等）

---

## 五、前端 4 个 Agent 入口

| 组件 | 路由 | Java 端点 | 用途 |
|------|------|-----------|------|
| AI.vue | `/ai` | `/api/ai/chat/stream` | 通用 AI 对话，SSE 流式，会话历史 |
| ProblemCoachSidebar | 题目详情页 | `/api/ai/problem-agent/chat` | 题目陪练，写代码时求助 |
| WrongBookReviewAgent | 错题详情页 | `/api/ai/wrong-book-agent/...` | 错题分析 + 结构复盘任务 |
| GlobalLearningCoach | App.vue 全局 | `/api/agent/coach/...` | 浮动气泡，跨页面 AI 助手 |

---

## 六、数据流：一次完整的 AI 教学请求

```
用户在前端题目页提交代码
        │
        ▼
Java 判题服务执行代码 → WA（答案错误）
        │
        ▼
AgentService.decideAndApply()
        │
        ├── AgentContextBuilder.buildForSubmission()
        │     构造 AgentContextDTO { code, errorMessage, consecutiveFailures, ... }
        │
        ├── AgentClient.getDecision()
        │     POST http://localhost:8766/decision  → Python AgentService-langchain
        │
        ▼
Python make_decision():
  ① 安全拦截 → 通过（failures>0）
  ② RAG 检索 → 搜到"取余运算"知识点
  ③ infer_action_llm → DIAGNOSE（有代码+WA）
  ④ ask_agent() → LLM: "你第3行用了 n/2，应该是 n%2 取余运算"
  ⑤ 组装 AgentDecision → action_type=DIAGNOSE, content_type=diagnosis
        │
        ▼
Java AgentDecisionEnforcer:
  - 校验 action_type 合法
  - 校验 answer_scope 合规
  - 写 learning_event_log
        │
        ▼
Java 返回给前端 → 前端渲染 AI 回复（v-html + marked）
```

---

## 七、技术栈总览

| 层级 | 技术 | 说明 |
|------|------|------|
| 前端框架 | Vue 3 + Vite + ElementPlus | SPA |
| 前端 AI | marked (markdown 渲染) | v-html 显示 Agent 回复 |
| Java 后端 | Spring Boot 3.2 + MyBatis | REST API |
| Java 安全 | Spring Security + JWT | InternalApiFilter 共享密钥 |
| 判题引擎 | Docker Java API | 隔离沙箱执行代码 |
| 数据库 | MySQL 8 | 用户、题库、提交、错题 |
| Python 后端 | FastAPI + LangChain 1.3 | AI 引擎 |
| Agent 框架 | create_agent + LangGraph | Agent 编排 |
| LLM | DeepSeek Chat (OpenAI 兼容) | 文本生成 |
| 向量数据库 | ChromaDB | RAG 知识库 |
| 向量模型 | text2vec-base-chinese (本地) | 中文语义检索 |
| 可观测性 | LangSmith | 全链路 trace |
| 认证 | X-Internal-Key 共享密钥 | Python ↔ Java |

---

## 八、项目目录结构

```
毕业设计/
├── Code/
│   ├── System/           ← Java Spring Boot 后端
│   ├── frontend/         ← Vue 3 前端
│   ├── AgentService/     ← 旧版 Python（手写 LLM 调用）
│   └── AgentService-langchain/  ← 新版 LangChain 重构
│       ├── app/
│       │   ├── main.py      ← FastAPI 入口 + 安全拦截 + RAG
│       │   ├── agent.py     ← create_agent 封装
│       │   ├── graph.py     ← LangGraph 工作流（面试核心）
│       │   ├── tools.py     ← Agent 工具（搜题/详情/分类）
│       │   ├── rag.py       ← ChromaDB 知识库
│       │   ├── prompts.py   ← 教学提示词（7种策略）
│       │   └── config.py    ← 配置加载
│       ├── data/chroma/     ← ChromaDB 持久化
│       ├── .env             ← LLM密钥 + LangSmith + 共享密钥
│       ├── README.md
│       ├── ARCHITECTURE.md
│       └── test_*.py        ← 测试文件
```
