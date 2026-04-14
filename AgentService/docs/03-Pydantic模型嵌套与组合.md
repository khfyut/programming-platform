# 学习笔记 03：Pydantic模型嵌套与组合

## 1. 核心概念
**本质**：Pydantic模型的组合与嵌套

**核心思想**：复杂模型由简单模型组合而成，类似乐高积木。

**知识类型**：🔵 通用后端知识

## 2. 设计动机
**为什么要拆分多个子模型？**

| 方式 | 问题 | 示例 |
|------|------|------|
| 平铺所有字段 | 字段太多，难以管理 | 50个字段的类 |
| 按领域拆分 | 结构清晰，易于复用 | ProblemContext可在多个地方用 |

**类比**：
- 平铺 = 把所有东西塞进一个行李箱
- 拆分 = 用多个收纳盒分类存放

**在Agent系统中的意义**：
- `ProblemContext` 可以在推荐系统、题目分析等多个场景复用
- 修改题目相关字段时，只改一处
- Java端组装数据时，也是按领域查询数据库，天然对应

## 3. 关键结构

```python
class ProblemContext(BaseModel):
    problem_id: int
    title: str
    # ...

class SubmissionContext(BaseModel):
    submit_id: int
    status: str
    # ...

class AgentContext(BaseModel):
    request_id: str
    problem: ProblemContext      # ← 嵌套子模型
    submission: SubmissionContext # ← 嵌套子模型
```

**嵌套语法**：
- `field: SubModel`：必填嵌套对象
- `field: Optional[SubModel] = None`：可选嵌套对象

## 4. 调用链路

```
Java组装数据
    ↓
{
  "request_id": "123",
  "problem": {
    "problem_id": 1,
    "title": "两数之和"
  },
  "submission": {
    "submit_id": 10,
    "status": "WA"
  }
}
    ↓
Pydantic自动解析为嵌套对象
    ↓
context.problem.title  # "两数之和"
context.submission.status  # "WA"
```

## 5. 常见错误

| 错误 | 原因 | 解决 |
|------|------|------|
| `ValidationError: field required` | 嵌套对象没传 | 提供完整的嵌套结构 |
| `AttributeError` | 用dict方式访问 | 应该用 `.` 访问属性 |
| 循环导入 | 两个模型互相引用 | 用 `from __future__ import annotations` |

**最大误区**：以为嵌套模型会自动创建
```python
# 错误
ctx = AgentContext(request_id="123")  # 缺少problem和submission

# 正确
ctx = AgentContext(
    request_id="123",
    problem=ProblemContext(...),
    submission=SubmissionContext(...)
)
```

## 6. 可复用模板

**嵌套模型模板**：
```python
class Address(BaseModel):
    city: str
    street: str

class Person(BaseModel):
    name: str
    address: Address  # 嵌套

# 使用
person = Person(
    name="张三",
    address=Address(city="北京", street="长安街")
)
```

**从JSON创建**：
```python
json_data = {
    "name": "张三",
    "address": {"city": "北京", "street": "长安街"}
}
person = Person(**json_data)  # 自动解析嵌套
```

## 7. 我的理解检查

1. **如果Java传过来的JSON缺少problem字段，Pydantic会怎么处理？**
2. **如何访问题目标题？是 `ctx.problem.title` 还是 `ctx['problem']['title']`？**
3. **SubmissionContext里的error_message是必填还是可选？怎么判断的？**

## 8. 判断标准

**"什么时候该拆分模型？"**
> 当字段超过10个，或明显属于不同领域时，就应该拆分。

**"嵌套模型怎么访问字段？"**
> 用 `.` 链式访问：`context.problem.title`，不是dict的 `['key']`。

**【面试高频点】**
**Q：Pydantic如何处理嵌套模型？**
**A**：Pydantic支持模型嵌套，可以在一个模型中使用另一个模型作为字段类型。解析时会自动递归验证嵌套结构。访问时用点号链式访问，如`context.problem.title`。这种设计让复杂数据结构的定义和验证变得清晰简洁。
