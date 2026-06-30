# 在线编程学习与 AI 教学平台

一个前后端分离的在线编程学习平台，核心亮点是基于 **LangChain + LangGraph** 实现分级 AI 教学策略，通过 **ChromaDB RAG** 增强知识检索，集成 **LangSmith** 全链路可观测。

## 🎯 核心亮点

**AI 教学引擎 — 不是"判对错"，而是"教你怎么做对"**

学生提交代码判错后，AI 根据失败次数递进教学：首次失败引导思路(GUIDE_IDEA) → 再次失败给提示(HINT) → 多次失败诊断代码(DIAGNOSE) → 概念不清时讲解(EXPLAIN)。代码层安全拦截确保不足 3 次失败不会给答案。

| 策略 | 触发条件 | 行为 |
|------|----------|------|
| GUIDE_IDEA | 首次提问 | 引导解题方向，不给代码 |
| HINT | 1-2次失败 | 给出关键线索 |
| DIAGNOSE | 有 WA/RE + 代码 | 分析具体哪行有问题 |
| EXPLAIN | 概念不清 | 讲解知识点原理 |
| REVEAL_ANSWER | ≥3次+已诊断 | 给出完整解答 |
| RECOMMEND | 搜题/完成 | 推荐相似题目 |
| REFLECT | 看过答案 | 引导复盘总结 |

## 🏗 系统架构

```
┌─────────────────────────────────────────┐
│          Vue 3 + Vite 前端              │
│  AI对话页 / 题目陪练 / 错题复盘 / 全局助手 │
└────────────────┬────────────────────────┘
                 │ HTTP
┌────────────────▼────────────────────────┐
│      Java Spring Boot :8080             │
│  ┌───────────────────────────────────┐  │
│  │  AgentService → AgentClient       │  │
│  │  AgentDecisionEnforcer (校验策略)  │  │
│  └───────────────┬───────────────────┘  │
│  JudgeService / QuestionService / ...   │
└──────────────────┼──────────────────────┘
                   │ POST /decision
┌──────────────────▼──────────────────────┐
│    Python FastAPI :8766 (LangChain版)   │
│  ┌───────────────────────────────────┐  │
│  │  ① 代码层安全拦截 (Phase 6)       │  │
│  │  ② RAG 知识检索 (ChromaDB)        │  │
│  │  ③ LLM 策略推断 (infer_action_llm) │  │
│  │  ④ Agent 生成回复 (create_agent)  │  │
│  │  ⑤ 组装 AgentDecision 返回 Java   │  │
│  └───────────────────────────────────┘  │
│  ┌──────────┐ ┌────────┐ ┌───────────┐  │
│  │ LangGraph│ │ChromaDB│ │ LangSmith │   │
│  │ 工作流编排│ │ 知识库 │ │ 全链路追踪│   │
│  └──────────┘ └────────┘ └───────────┘  │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│         DeepSeek API (OpenAI 兼容)       │
└─────────────────────────────────────────┘
```

## 📊 LangGraph 工作流

```
[学生提问] → classify_intent (LLM语义分类)
                ├── TOOL_REQUEST → generate_response (调工具搜题)
                ├── CHAT         → generate_response (直接回复)
                └── TEACHING     → safety_check → generate_response
```

## 🔧 技术栈

| 层级 | 技术 |
|------|------|
| **前端** | Vue 3, Vite, ElementPlus, Monaco Editor, marked(markdown渲染) |
| **后端** | Java 17, SpringBoot 3.2, MyBatis, Docker Java API, Spring Security + JWT |
| **AI Agent** | LangChain 1.3, LangGraph, ChromaDB, sentence-transformers |
| **LLM** | DeepSeek Chat (OpenAI 兼容协议) |
| **可观测性** | LangSmith 全链路 trace |
| **数据库** | MySQL 8.0 |
| **判题** | Docker 容器池隔离执行 |
| **测试** | JMeter 压测(P95 18ms), pytest 76用例, 10项 Agent 评估用例 |

## 📁 项目结构

```
├── frontend/                     Vue 3 前端
│   └── src/
│       ├── components/agent/     AI Agent 组件
│       ├── views/AI.vue          AI 对话页
│       └── utils/markdown.js     Markdown 渲染
├── System/                       Java SpringBoot 后端
│   └── src/main/java/com/programming/
│       ├── agent/                Agent 调度与安全策略
│       ├── security/             JWT + InternalApiFilter
│       ├── controller/           REST API
│       └── service/              业务逻辑
├── AgentService/                 旧版 Python (手写 LLM 调用)
├── AgentService-langchain/       新版 LangChain 重构 ⭐
│   ├── app/
│   │   ├── main.py               FastAPI + 安全拦截 + RAG
│   │   ├── agent.py              create_agent 封装
│   │   ├── graph.py              LangGraph 工作流
│   │   ├── tools.py              Agent 工具(搜题/详情/分类)
│   │   ├── rag.py                ChromaDB 知识库
│   │   └── prompts.py            教学提示词(7种策略)
│   ├── ARCHITECTURE.md           完整架构文档
│   └── test_eval.py              10项 Agent 评估用例
├── db/                           数据库脚本
└── tests/                        接口测试
```

## 🚀 快速启动

### 环境要求

- JDK 17+, Maven 3.6+
- Node.js 18+, npm
- MySQL 8.0+, Docker
- Python 3.10+

### 启动步骤

**1. 数据库**
```bash
# 确保 MySQL 在运行，导入 db/ 下的建表脚本
```

**2. Java 后端**
```bash
cd System
mvn spring-boot:run
# 或: start-backend-8080.cmd
```

**3. Python AI 服务（LangChain 版）**
```bash
cd AgentService-langchain
pip install -r requirements.txt --break-system-packages
python -c "from app.rag import import_knowledge; import_knowledge()"
python -m uvicorn app.main:app --reload --host 127.0.0.1 --port 8766
```

**4. 前端**
```bash
cd frontend
npm install
npm run dev
```

### 测试

```bash
# Agent 评估（10 用例）
cd AgentService-langchain
python test_eval.py

# LangGraph 工作流测试
python test_graph.py

# 安全拦截测试
python test_phase6.py
```

## 🔑 关键设计决策

- **Python LLM 判断 + 关键词 fallback**：`infer_action_llm()` 用 LLM 语义判断教学策略，LLM 失败时自动回退关键词规则，保障可用性
- **代码层硬约束**：连续失败不足 3 次要答案时，请求根本不发给 LLM，直接返回拒绝
- **Java ↔ Python 共享密钥认证**：`InternalApiFilter` 检测 `X-Internal-Key` header 匹配后跳过 JWT 校验
- **LangSmith 可观测性**：每次 Agent 调用的 prompt、推理过程、工具调用全链路可视化，不靠猜调试

## 📝 License

MIT
