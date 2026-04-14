# 学习笔记 05：AgentDecision设计与完善

## 1. 核心概念
**本质**：AgentDecision的演进与工程化设计

**从简单到复杂的过程**：
- 第一步：基础字段（content, content_type）
- 第二步：候选动作（candidate_actions, recommended_action_id）
- 第三步：决策解释（decision_reason, constraints, blocked_actions）
- 第四步：状态更新建议（proposed_updates with confidence）

**知识类型**：🟢 Agent系统特有知识

## 2. 设计动机
**为什么要这么复杂的AgentDecision？**

| 阶段 | 问题 | 解决 |
|------|------|------|
| 简单返回 | Java不知道Agent为什么做这个决策 | 添加decision_reason |
| 候选动作 | Java不知道哪些动作被禁掉了 | 添加blocked_actions |
| 状态更新 | 推断状态直接落库会污染可信状态 | 添加confidence分层 |

**关键设计原则**：
- **透明性**：Java需要知道Agent为什么做这个决策
- **可控性**：Java需要知道哪些动作被约束层禁掉了
- **分层性**：推断状态和可信状态要区分

**反例**：只返回一个content字符串
- Java不知道Agent的决策依据
- 无法做审核和fallback
- 无法追踪决策质量

## 3. 关键结构

```python
AgentDecision (决策结果)
├── 元信息
│   ├── response_id
│   └── timestamp
├── 决策摘要
│   ├── decision_summary
│   └── selected_strategy
├── 决策解释（关键）
│   ├── decision_reason      # 为什么做这个决策
│   ├── applied_constraints  # 应用了哪些约束
│   └── blocked_actions      # 哪些动作被禁掉了
├── 候选动作
│   ├── candidate_actions[]
│   └── recommended_action_id
├── 生成内容
│   ├── content
│   └── content_type
└── 状态更新建议（关键）
    └── proposed_updates[]
        ├── update_type
        ├── value
        ├── confidence      # 置信度分层
        └── reason
```

## 4. 调用链路

```
Decision Policy做出决策
    ↓
组装AgentDecision
    ↓
HTTP返回给Java
    ↓
Java解析并审核
    ↓
根据confidence决定是否采纳状态更新
    ↓
执行动作并落库
```

## 5. 常见错误

| 错误 | 原因 | 解决 |
|------|------|------|
| 推断状态直接落库 | 没有confidence分层 | 高confidence才落库，低confidence只记录 |
| Java无法审核 | 缺少decision_reason | 必须提供决策依据 |
| 动作越界 | 没有blocked_actions | 明确告诉Java哪些动作被约束层禁掉了 |

**最大误区**：以为AgentDecision只是"返回给前端展示的内容"
- 实际上它是"Java审核和执行的依据"
- 需要包含足够的信息供Java做决策

## 6. 可复用模板

**决策结果模型模板**：
```python
class DecisionResult(BaseModel):
    # 1. 元信息
    request_id: str
    timestamp: datetime

    # 2. 决策解释（供审核）
    decision_reason: str
    applied_constraints: List[str] = []
    blocked_actions: List[str] = []

    # 3. 候选动作
    candidate_actions: List[CandidateAction]
    recommended_action_id: str

    # 4. 状态更新建议（分层）
    proposed_updates: List[StateUpdate] = []

class StateUpdate(BaseModel):
    update_type: str
    value: str
    confidence: float  # 分层关键
    reason: str
```

## 7. 我的理解检查

1. **为什么需要blocked_actions字段？Java自己不能判断吗？**
2. **confidence字段的作用是什么？Java怎么使用它？**
3. **decision_reason和decision_summary有什么区别？**

## 8. 判断标准

**"什么时候该分层返回？"**
> 当主系统需要审核AI决策，而不是直接执行时。

**"confidence怎么定？"**
> 规则匹配→0.9+，统计推断→0.6-0.8，LLM推测→0.3-0.6

**【面试高频点】**
**Q：AI系统的输出应该如何设计才能被主系统安全地集成？**
**A**：AI系统应该返回结构化的决策结果，包含：1）决策解释（为什么做这个决策）；2）候选动作列表（供选择）；3）置信度分层（高置信度建议可直接采纳，低置信度需人工审核）；4）被约束的动作（明确边界）。主系统保留最终执行权，AI只提供建议。
