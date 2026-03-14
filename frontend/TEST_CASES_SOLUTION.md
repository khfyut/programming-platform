# 测试用例功能实现方案

## 问题分析
当前系统缺少测试用例（Test Cases）功能，导致用户无法：
1. 看到题目的示例输入输出
2. 了解题目的约束条件（时间限制、内存限制）
3. 查看详细的判题结果（每个测试用例的通过情况）

## 解决方案

### 1. 后端数据结构改造

#### 1.1 题目表（problem）增加字段
```sql
ALTER TABLE problem ADD COLUMN test_cases TEXT COMMENT '测试用例JSON';
ALTER TABLE problem ADD COLUMN constraints TEXT COMMENT '约束条件JSON';
```

#### 1.2 测试用例数据结构
```json
{
  "testCases": [
    {
      "id": 1,
      "input": "5 3",
      "output": "8",
      "isExample": true,
      "description": "基本测试"
    },
    {
      "id": 2,
      "input": "10 20",
      "output": "30",
      "isExample": false,
      "description": "边界测试"
    }
  ],
  "constraints": {
    "timeLimit": 1000,
    "memoryLimit": 256
  }
}
```

#### 1.3 判题结果数据结构
```json
{
  "result": 0,
  "timeCost": 156,
  "memoryCost": 128,
  "testCases": [
    {
      "id": 1,
      "passed": true,
      "timeCost": 50,
      "memoryCost": 64,
      "input": "5 3",
      "expectedOutput": "8",
      "actualOutput": "8"
    },
    {
      "id": 2,
      "passed": false,
      "timeCost": 106,
      "memoryCost": 128,
      "input": "10 20",
      "expectedOutput": "30",
      "actualOutput": "25",
      "error": "答案错误"
    }
  ],
  "passedCount": 1,
  "failedCount": 1,
  "totalCount": 2
}
```

### 2. 后端API接口

#### 2.1 获取题目测试用例
```
GET /api/problem/testCases/{id}
```

**响应示例：**
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "input": "5 3",
      "output": "8",
      "isExample": true
    }
  ]
}
```

#### 2.2 提交代码（修改现有接口）
```
POST /api/submit/commit
```

**请求体：**
```json
{
  "problemId": 1,
  "code": "public class Main {...}",
  "language": "java"
}
```

**响应体（增强）：**
```json
{
  "code": 200,
  "data": {
    "result": 1,
    "timeCost": 156,
    "memoryCost": 128,
    "testCases": [...],
    "passedCount": 1,
    "failedCount": 1,
    "totalCount": 2
  }
}
```

### 3. 前端功能实现

#### 3.1 题目详情页增强
✅ 已完成的功能：
- 显示示例测试用例（输入/输出）
- 显示约束条件（时间限制、内存限制）
- 显示详细的判题结果

#### 3.2 测试用例展示
**位置：** 题目描述下方

**功能：**
- 示例测试用例：显示输入和预期输出
- 每个测试用例有独立的卡片
- 输入用蓝色左边框标识
- 输出用绿色左边框标识

#### 3.3 判题结果展示
**位置：** 提交代码后右侧卡片

**功能：**
- 总体判题结果（通过/失败）
- 每个测试用例的详细结果
- 通过的测试用例：绿色边框 + 通过标签
- 失败的测试用例：红色边框 + 失败标签 + 详细对比
  - 输入
  - 预期输出
  - 实际输出
  - 错误信息（如果有）
- 统计信息：通过数/总数、失败数

### 4. 判题逻辑

#### 4.1 判题流程
```
1. 接收用户提交的代码
2. 根据语言选择对应的编译器/解释器
3. 对每个测试用例：
   a. 提供输入数据
   b. 运行代码
   c. 捕获输出
   d. 比较预期输出和实际输出
   e. 记录运行时间和内存消耗
4. 汇总所有测试用例的结果
5. 返回详细的判题结果
```

#### 4.2 判题结果类型
- **0: 全部通过** - 所有测试用例都通过
- **1: 答案错误** - 至少一个测试用例输出不匹配
- **2: 运行错误** - 代码编译或运行时错误
- **3: 超时** - 运行时间超过限制
- **4: 内存超限** - 内存消耗超过限制

### 5. 数据库设计

#### 5.1 测试用例表（可选）
如果需要独立管理测试用例，可以创建单独的表：

```sql
CREATE TABLE test_case (
  id INT PRIMARY KEY AUTO_INCREMENT,
  problem_id INT NOT NULL,
  input TEXT NOT NULL,
  output TEXT NOT NULL,
  is_example TINYINT(1) DEFAULT 0,
  description VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (problem_id) REFERENCES problem(id)
);
```

#### 5.2 提交记录表增强
```sql
ALTER TABLE submission ADD COLUMN test_results TEXT COMMENT '测试用例结果JSON';
ALTER TABLE submission ADD COLUMN passed_count INT DEFAULT 0 COMMENT '通过的测试用例数';
ALTER TABLE submission ADD COLUMN failed_count INT DEFAULT 0 COMMENT '失败的测试用例数';
```

### 6. 安全考虑

#### 6.1 测试用例保护
- 示例测试用例：公开显示，帮助用户理解题目
- 隐藏测试用例：不显示，用于判题
- 判题时使用所有测试用例（包括隐藏的）

#### 6.2 代码执行安全
- 使用沙箱环境执行用户代码
- 限制系统资源（CPU、内存、磁盘）
- 防止恶意代码执行
- 超时机制防止无限循环

### 7. 性能优化

#### 7.1 判题性能
- 并行执行多个测试用例
- 缓存编译结果
- 使用高效的输出比较算法

#### 7.2 前端性能
- 懒加载测试用例详情
- 虚拟滚动处理大量测试用例
- 防抖提交请求

## 实现优先级

### 高优先级（必须实现）
1. ✅ 前端测试用例展示UI
2. ⏳ 后端测试用例数据结构
3. ⏳ 后端判题逻辑增强
4. ⏳ API接口完善

### 中优先级（建议实现）
1. ⏳ 测试用例管理后台
2. ⏳ 提交记录详情页
3. ⏳ 测试用例批量导入

### 低优先级（可选实现）
1. ⏳ 自定义测试用例运行
2. ⏳ 测试用例分享功能
3. ⏳ 测试用例讨论区

## 技术栈建议

### 后端
- **判题引擎**：Docker沙箱 + 多语言支持
- **消息队列**：RabbitMQ/Kafka（异步判题）
- **缓存**：Redis（缓存编译结果）

### 前端
- **代码高亮**：Monaco Editor（已集成）
- **UI组件**：Element Plus（已使用）
- **状态管理**：Pinia（已使用）

## 测试建议

### 功能测试
1. 提交正确代码，所有测试用例通过
2. 提交错误代码，显示失败详情
3. 提交超时代码，显示超时错误
4. 提交内存超限代码，显示内存错误

### 性能测试
1. 测试大量测试用例的判题速度
2. 测试并发提交的处理能力
3. 测试前端渲染性能

### 安全测试
1. 测试恶意代码的隔离
2. 测试资源限制的有效性
3. 测试SQL注入等攻击

## 总结

测试用例功能是在线编程平台的核心功能，需要从前端展示、后端判题、数据存储等多个方面进行系统性的实现。建议按照优先级逐步实现，先保证核心功能可用，再逐步完善辅助功能。

前端UI部分已经完成，现在需要配合后端实现完整的测试用例功能。
