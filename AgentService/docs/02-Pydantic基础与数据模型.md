# 学习笔记 02：Pydantic基础与数据模型

## 1. 核心概念
**本质**：Pydantic数据模型定义与验证

**Pydantic是什么？**
- Python的数据验证库
- 通过Python类型注解定义数据结构
- 自动进行类型检查和转换
- 自动生成JSON序列化/反序列化

**知识类型**：🔵 通用后端知识（但Pydantic是Python特有）

## 2. 设计动机
**为什么要用Pydantic而不是普通dict？**

| 特性 | dict | Pydantic |
|------|------|----------|
| 字段提示 | 无 | IDE自动提示 |
| 类型检查 | 运行时出错 | 实例化时自动验证 |
| 文档生成 | 手动写 | 自动生成 |
| JSON转换 | 手动处理 | `.json()` 方法 |

**反例**：用dict接收数据
```python
# 错误示例
data = json.loads(request.body)
user_id = data["user_id"]  # 如果key不存在，KeyError
# 不知道有哪些字段，IDE也无法提示
```

**正例**：用Pydantic
```python
# 正确示例
context = AgentContext(**data)  # 自动验证字段
context.user_id  # IDE自动提示，类型安全
```

**在Agent系统中的意义**：
- Java和Python通过JSON通信，需要严格的schema约定
- Pydantic确保数据格式一致，避免"字段对不上"的问题
- 类型安全 = 减少运行时错误

## 3. 关键结构

```python
from pydantic import BaseModel, Field
from typing import Optional
from datetime import datetime

class AgentContext(BaseModel):  # 继承BaseModel
    request_id: str              # 必填字段
    timestamp: datetime          # Pydantic自动转换类型
    error_message: Optional[str] = None  # 可选字段，默认None
```

**关键语法**：
- `class X(BaseModel)`：定义模型
- `field: type`：必填字段
- `field: Optional[type] = None`：可选字段
- `Field(...)`：添加验证规则和描述（进阶）

## 4. 调用链路

```
Java发送JSON
    ↓
FastAPI接收
    ↓
Pydantic自动解析 + 验证
    ↓
得到AgentContext对象（类型安全）
    ↓
业务逻辑使用
```

**验证时机**：实例化时自动验证
```python
context = AgentContext(
    request_id="123",
    timestamp=datetime.now(),
    user_id="abc"  # ❌ 类型错误！期望int，给了str
)
# 会抛出 ValidationError
```

## 5. 常见错误

| 错误 | 原因 | 解决 |
|------|------|------|
| `NameError: name 'BaseModel' is not defined` | 没导入或没保存 | `from pydantic import BaseModel` + 保存 |
| `ValidationError` | 字段类型不匹配 | 检查传入数据的类型 |
| `Field required` | 必填字段没传 | 提供默认值或改为Optional |
| IDE不提示字段 | 没用Pydantic模型 | 用Pydantic代替dict |

**最大误区**：以为Pydantic只是"定义结构"
- 实际上它还会**自动验证**、**类型转换**、**生成文档**

## 6. 可复用模板

**基础模型模板**：
```python
from pydantic import BaseModel, Field
from typing import Optional
from datetime import datetime

class MyModel(BaseModel):
    # 必填字段
    id: int
    name: str

    # 可选字段
    description: Optional[str] = None

    # 带默认值的字段
    status: str = "active"

    # 带验证的字段
    score: float = Field(ge=0, le=100)  # 0-100之间
```

**使用模板**：
```python
# 创建实例
obj = MyModel(id=1, name="test")

# 从dict创建
data = {"id": 1, "name": "test"}
obj = MyModel(**data)

# 转JSON
json_str = obj.model_dump_json()

# 转dict
data_dict = obj.model_dump()
```

## 7. 我的理解检查

1. **Pydantic的BaseModel和普通Python class有什么区别？**
2. **Optional[str] = None 是什么意思？如果不写=None会怎样？**
3. **如果Java传过来的user_id是字符串"123"，而Python期望是int，Pydantic会怎么处理？**

## 8. 判断标准

**"什么时候该用Pydantic？"**
> 需要定义数据结构、且需要验证和序列化时。API接口的输入输出必用。

**"Optional和默认值的关系？"**
> `Optional[X]` 表示可以是X或None。`= None` 给默认值。两者配合表示"可选且默认为空"。

**【面试高频点】**
**Q：Pydantic有什么优势？**
**A**：Pydantic通过Python类型注解实现数据验证，主要优势：1）类型安全，实例化时自动验证数据类型；2）IDE友好，提供代码提示；3）自动生成API文档；4）便捷的JSON序列化/反序列化。在FastAPI中，Pydantic模型可以直接作为请求体和响应体的schema定义。
