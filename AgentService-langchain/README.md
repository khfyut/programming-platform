# Learning Agent Service (LangChain 重构版)

在线编程学习平台的 AI 教学引擎。基于 LangChain Agent + ChromaDB RAG + LangGraph 工作流实现分级教学策略。

## 架构

```
Java Spring Boot (:8080)              Python FastAPI (:8766)
        │                                      │
        │  POST /decision  {学生状态}           │
        ├─────────────────────────────────────→│
        │                                      ├── 安全拦截（Phase 6）
        │                                      ├── RAG 检索（Phase 5）
        │                                      ├── LangGraph 工作流（Phase 4）
        │                                      │   ├── classify_intent
        │                                      │   ├── safety_check
        │                                      │   └── generate_response
        │                                      ├── Agent 工具调用（Phase 3）
        │                                      │   ├── search_problems
        │                                      │   ├── get_problem_detail
        │                                      │   └── get_categories
        │                                      └── DeepSeek LLM
        │←─────────────────────────────────────│
        │         返回教学回复                   │
```

## LangGraph 工作流

```
[学生提问]
    ↓
[classify_intent] — LLM 语义分类
    ↓
    ├── TOOL_REQUEST → [generate_response] — 调工具搜题
    ├── CHAT         → [generate_response] — 直接回复
    └── TEACHING     → [safety_check]    — 硬约束检查
                          ↓
                      [generate_response] — 教学回复
```

## 技术栈

| 层级 | 技术 |
|------|------|
| AI Agent | LangChain 1.3 + LangGraph |
| RAG 记忆 | ChromaDB + text2vec-base-chinese（本地） |
| LLM | DeepSeek Chat（OpenAI 兼容） |
| 后端 | Python FastAPI + SSE 流式 |
| 安全 | 代码层硬约束拦截（Phase 6） |

## 快速启动

```bash
cd D:\Desktop\毕业设计\Code\AgentService-langchain

# 安装依赖
pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple --break-system-packages

# 导入知识库（首次运行）
python -c "from app.rag import import_knowledge; import_knowledge()"

# 启动服务
python -m uvicorn app.main:app --reload --host 127.0.0.1 --port 8766
```

## 测试

```bash
# 测试 LangGraph 工作流（三条分支）
python test_graph.py

# 测试安全拦截
python test_phase6.py

# 测试 RAG 检索
python -c "from app.rag import search_knowledge; print(search_knowledge('哈希表怎么用'))"
```

## 项目结构

```
AgentService-langchain/
├── README.md
├── LEARNING_NOTES_PHASE1.md    ← 完整学习笔记（Phase 1-6）
├── requirements.txt
├── .env
├── test_graph.py               ← LangGraph 测试
├── test_phase6.py              ← 安全拦截测试
├── app/
│   ├── config.py               ← LLM 配置
│   ├── prompts.py              ← 教学提示词（7种策略）
│   ├── tools.py                ← Agent 工具（搜题/详情/分类）
│   ├── agent.py                ← create_agent 版
│   ├── graph.py                ← LangGraph 工作流
│   ├── rag.py                  ← ChromaDB 知识库
│   └── main.py                 ← FastAPI + 安全拦截 + RAG
└── data/
    └── chroma/                 ← ChromaDB 持久化数据
```

## 核心特性

### 分级教学策略
7 种递进策略：GUIDE_IDEA → HINT → DIAGNOSE → EXPLAIN → RECOMMEND → REFLECT → REVEAL_ANSWER

### 代码层安全拦截
连续失败不足 3 次的学生索要答案时，请求根本不发给 LLM，直接返回拒绝。

### Agent 工具调用
Agent 可自主调用题库搜索、题目详情、题目分类等后端服务。

### RAG 知识库
6 条编程知识点存入 ChromaDB，Agent 回答前自动检索相关知识，降低幻觉。

## 面试讲法

> "我基于 LangChain + LangGraph 重构了编程学习平台的 AI 引擎。Agent 采用分级教学策略，根据学生失败次数自动选择引导→提示→诊断→讲解的递进方案。安全层面实现了代码层硬约束拦截，不满足条件的学生请求不会发给 LLM。集成了 RAG 知识库，用 ChromaDB + 中文向量模型存储编程知识点，回答前自动检索增强。整个工作流用 LangGraph 编排，三个节点加条件边，可可视化、可单独测试。"
