# 个性化在线编程与答疑系统 API 文档

## 1. 概述

本文档描述了个性化在线编程与答疑系统的所有API接口，包括用户管理、代码运行、题目管理、提交管理、AI答疑、学习管理、错题本管理、管理员管理等模块。

## 2. 基础信息

- API基础路径: `/api`
- 响应格式: JSON
- 认证方式: JWT Token (在请求头中添加 `Authorization: Bearer {token}`)
- 统一响应结构:
  ```json
  {
    "code": 200,
    "msg": "成功",
    "data": {}
  }
  ```

## 3. API模块

### 3.1 用户模块 (`/api/user`)

| 接口 | 方法 | 功能描述 | 请求参数 | 响应数据 |
|------|------|----------|----------|----------|
| `/login` | POST | 用户登录 | `username`, `password` | `{token, user}` |
| `/register` | POST | 用户注册 | `username`, `password`, `email` | `{user}` |
| `/info` | GET | 获取用户信息 | N/A | `{user}` |
| `/update` | POST | 更新用户信息 | `username`, `email` | `{user}` |
| `/change-password` | POST | 修改密码 | `oldPassword`, `newPassword` | `{success: true}` |

### 3.2 代码运行模块 (`/api/code`)

| 接口 | 方法 | 功能描述 | 请求参数 | 响应数据 |
|------|------|----------|----------|----------|
| `/run` | POST | 运行代码 | `language`, `code`, `input` | `{output, error, time, memory}` |
| `/test` | POST | 测试代码 | `language`, `code`, `testCases` | `{results: [{input, expected, actual, passed}]}` |

### 3.3 题目模块 (`/api/problem`)

| 接口 | 方法 | 功能描述 | 请求参数 | 响应数据 |
|------|------|----------|----------|----------|
| `/list` | GET | 获取题目列表 | `page`, `size`, `difficulty`, `category` | `{total, list: [{id, title, difficulty, category}]}` |
| `/detail/{id}` | GET | 获取题目详情 | N/A | `{id, title, description, input, output, examples, difficulty, category}` |
| `/random` | GET | 获取随机题目 | `difficulty` | `{id, title, description, input, output, examples, difficulty, category}` |

### 3.4 提交模块 (`/api/submit`)

| 接口 | 方法 | 功能描述 | 请求参数 | 响应数据 |
|------|------|----------|----------|----------|
| `/submit` | POST | 提交代码 | `problemId`, `language`, `code` | `{id, status, score, executionTime, memoryUsage}` |
| `/history` | GET | 获取提交历史 | `page`, `size`, `problemId` | `{total, list: [{id, problemId, language, code, status, score, submitTime}]}` |
| `/detail/{id}` | GET | 获取提交详情 | N/A | `{id, problemId, language, code, status, score, executionTime, memoryUsage, output, error}` |

### 3.5 AI模块 (`/api/ai`)

| 接口 | 方法 | 功能描述 | 请求参数 | 响应数据 |
|------|------|----------|----------|----------|
| `/ask` | POST | 向AI提问 | `question`, `code`, `language`, `sessionId` | `{answer, sessionId}` |
| `/history` | GET | 获取AI对话历史 | `page`, `size` | `{total, list: [{id, question, answer, sessionId, createTime}]}` |

### 3.6 学习模块 (`/api/learn`)

| 接口 | 方法 | 功能描述 | 请求参数 | 响应数据 |
|------|------|----------|----------|----------|
| `/my` | GET | 获取学习统计 | N/A | `{solvedCount, submissionCount, accuracy, ranking}` |
| `/recommend` | GET | 获取推荐题目 | N/A | `[{id, title, difficulty, reason}]` |
| `/assessment` | GET | 获取能力测评题目 | `language`, `direction`, `limit` | `[{id, question, options}]` |
| `/assessment/commit` | POST | 提交能力测评 | `language`, `direction`, `answers` | `{score, level, suggestions}` |
| `/path` | GET | 获取学习路径进度 | `pathId` | `{path, chapters: [{id, name, levels: [{id, name, status}]}]}` |
| `/path/chapter/{chapterId}` | GET | 获取章节详情 | N/A | `{id, name, description, levels}` |
| `/path/level/unlock` | POST | 解锁关卡 | `levelId` | `{success: true}` |
| `/path/level/complete` | POST | 完成关卡 | `levelId` | `{success: true}` |
| `/paths` | GET | 获取可用学习路径 | `language`, `direction` | `[{id, name, description, difficulty}]` |
| `/path/detail/{pathId}` | GET | 获取路径详情 | N/A | `{id, name, description, chapters}` |
| `/knowledge/graph` | GET | 获取知识图谱 | N/A | `{nodes, edges}` |
| `/knowledge/mastery` | GET | 获取知识掌握度 | N/A | `[{knowledgePoint, masteryLevel, lastPracticeTime}]` |
| `/report/weekly` | GET | 获取周报 | N/A | `{solvedCount, submissionCount, accuracy, weakPoints, improvements}` |
| `/report/monthly` | GET | 获取月报 | `year`, `month` | `{solvedCount, submissionCount, accuracy, weakPoints, improvements, trends}` |
| `/weakness` | GET | 获取薄弱知识点 | N/A | `[{knowledgePoint, masteryLevel, recommendedProblems}]` |
| `/knowledge/distribution` | GET | 获取知识掌握度分布 | N/A | `{data: [{level, count}]}` |
| `/errors/frequency` | GET | 获取高频错误 | `limit` | `[{errorType, count, examples}]` |

### 3.7 错题本模块 (`/api/wrong-book`)

| 接口 | 方法 | 功能描述 | 请求参数 | 响应数据 |
|------|------|----------|----------|----------|
| `/list` | GET | 获取错题列表 | `knowledgePoint`, `difficulty` | `[{id, problemId, problemTitle, submissionId, errorType, submitTime, reviewStatus}]` |
| `/detail/{id}` | GET | 获取错题详情 | N/A | `{id, problem, submission, errorType, analysis, reviewStatus}` |
| `/review` | POST | 标记错题复习状态 | `id`, `status` | `{success: true}` |
| `/review/plan` | GET | 获取复习计划 | N/A | `[{id, wrongItemId, scheduledTime, reviewed}]` |
| `/review/pending` | GET | 获取待复习的错题 | N/A | `[{id, wrongItemId, problemTitle, scheduledTime}]` |
| `/review/update` | POST | 更新复习计划 | `id`, `reviewed` | `{success: true}` |
| `/recommend` | GET | 获取推荐题目 | `wrongItemId`, `limit` | `[{id, title, difficulty, relevance}]` |
| `/statistics` | GET | 获取错题统计信息 | N/A | `{totalCount, byDifficulty: [{difficulty, count}], byKnowledgePoint: [{knowledgePoint, count}]}` |
| `/distribution/knowledge` | GET | 获取按知识点分布的错题 | N/A | `{data: [{knowledgePoint, count}]}` |
| `/distribution/difficulty` | GET | 获取按难度分布的错题 | N/A | `{data: [{difficulty, count}]}` |
| `/delete/{id}` | POST | 删除错题 | N/A | `{success: true}` |

### 3.8 管理员模块 (`/api/admin`)

| 接口 | 方法 | 功能描述 | 请求参数 | 响应数据 |
|------|------|----------|----------|----------|
| `/user/list` | GET | 获取用户列表 | `page`, `size` | `{total, list: [{id, username, email, role, createTime}]}` |
| `/user/status` | POST | 更新用户状态 | `userId`, `status` | `{success: true}` |
| `/user/role` | POST | 更新用户角色 | `userId`, `roleId` | `{success: true}` |
| `/problem/add` | POST | 添加题目 | `title`, `description`, `input`, `output`, `examples`, `difficulty`, `category` | `{success: true}` |
| `/problem/add-with-check` | POST | 添加题目并检查 | `title`, `description`, `input`, `output`, `examples`, `difficulty`, `category` | `{success: true, warnings: []}` |
| `/problem/update` | POST | 更新题目 | `id`, `title`, `description`, `input`, `output`, `examples`, `difficulty`, `category` | `{success: true}` |
| `/problem/delete/{id}` | POST | 删除题目 | N/A | `{success: true}` |
| `/submit/list` | GET | 获取提交列表 | `page`, `size` | `{total, list: [{id, userId, username, problemId, problemTitle, language, status, score, submitTime}]}` |
| `/statistics` | GET | 获取系统统计 | N/A | `{userCount, problemCount, submissionCount, aiQueryCount, averageAccuracy}` |
| `/problem/batch-import` | POST | 批量导入题目 | `problems: [{title, description, input, output, examples, difficulty, category}]` | `{success: true, imported: 0, failed: 0}` |
| `/role/list` | GET | 获取角色列表 | N/A | `[{id, name, description, permissions}]` |
| `/role/{id}` | GET | 获取角色详情 | N/A | `{id, name, description, permissions}` |
| `/role/add` | POST | 添加角色 | `name`, `description` | `{success: true}` |
| `/role/update` | POST | 更新角色 | `id`, `name`, `description` | `{success: true}` |
| `/role/delete/{id}` | POST | 删除角色 | N/A | `{success: true}` |
| `/permission/list` | GET | 获取权限列表 | N/A | `[{id, name, code, description}]` |
| `/role/permission` | POST | 分配角色权限 | `roleId`, `permissionIds` | `{success: true}` |
| `/class/list` | GET | 获取班级列表 | N/A | `[{id, name, description, createTime}]` |
| `/class/add` | POST | 添加班级 | `name`, `description` | `{success: true}` |
| `/class/update` | POST | 更新班级 | `id`, `name`, `description` | `{success: true}` |
| `/class/delete/{id}` | POST | 删除班级 | N/A | `{success: true}` |
| `/class/{classId}/users` | GET | 获取班级用户 | N/A | `[{id, username, email, role}]` |
| `/class/user/add` | POST | 添加用户到班级 | `userId`, `classId`, `role` | `{success: true}` |
| `/class/user/remove` | POST | 移除班级用户 | `userId`, `classId` | `{success: true}` |
| `/knowledge/list` | GET | 获取知识点列表 | N/A | `[{id, name, parentId, level, description}]` |
| `/knowledge/add` | POST | 添加知识点 | `name`, `parentId`, `level`, `description` | `{success: true}` |
| `/knowledge/update` | POST | 更新知识点 | `id`, `name`, `parentId`, `level`, `description` | `{success: true}` |
| `/knowledge/delete/{id}` | POST | 删除知识点 | N/A | `{success: true}` |
| `/path/list` | GET | 获取学习路径列表 | N/A | `[{id, name, direction, language, description}]` |
| `/path/add` | POST | 添加学习路径 | `name`, `direction`, `language`, `description` | `{success: true}` |
| `/path/update` | POST | 更新学习路径 | `id`, `name`, `direction`, `language`, `description` | `{success: true}` |
| `/path/delete/{id}` | POST | 删除学习路径 | N/A | `{success: true}` |
| `/audit/list` | GET | 获取审计日志 | `page`, `size` | `{total, list: [{id, userId, operation, resourceType, resourceId, detail, ipAddress, createTime}]}` |
| `/system/status` | GET | 获取系统状态 | N/A | `{metricName, metricValue, metricUnit, createTime}` |
| `/system/metrics` | GET | 获取API指标 | N/A | `{metrics: [{apiPath, method, responseTime, statusCode}]}` |
| `/system/sandbox` | GET | 获取沙箱状态 | N/A | `{statuses: [{containerId, status, cpuUsage, memoryUsage}]}` |
| `/system/logs` | GET | 获取系统日志 | `page`, `size` | `[{id, level, category, message, stackTrace, createTime}]` |
| `/dashboard` | GET | 获取仪表盘数据 | N/A | `{userCount, problemCount, submitCount, activeUsers}` |

## 4. 数据结构

### 4.1 统一响应结构

```json
{
  "code": 200, // 状态码
  "msg": "成功", // 消息
  "data": {} // 数据
}
```

### 4.2 常见数据结构

#### 4.2.1 用户信息

```json
{
  "id": 1,
  "username": "user1",
  "email": "user1@example.com",
  "role": "user",
  "createTime": "2024-01-01T00:00:00"
}
```

#### 4.2.2 题目信息

```json
{
  "id": 1,
  "title": "两数之和",
  "description": "给定一个整数数组...",
  "input": "整数数组和目标值",
  "output": "返回两个数的索引",
  "examples": [
    {
      "input": "[2,7,11,15], 9",
      "output": "[0,1]"
    }
  ],
  "difficulty": "easy",
  "category": "数组"
}
```

#### 4.2.3 提交信息

```json
{
  "id": 1,
  "problemId": 1,
  "language": "java",
  "code": "class Solution { ... }",
  "status": "accepted",
  "score": 100,
  "executionTime": 10,
  "memoryUsage": 1024,
  "submitTime": "2024-01-01T00:00:00"
}
```

#### 4.2.4 学习路径信息

```json
{
  "id": 1,
  "name": "Java基础入门",
  "description": "Java语言基础学习路径",
  "difficulty": "beginner",
  "chapters": [
    {
      "id": 1,
      "name": "Java语法基础",
      "levels": [
        {
          "id": 1,
          "name": "变量与数据类型",
          "status": "completed"
        }
      ]
    }
  ]
}
```

#### 4.2.5 错题信息

```json
{
  "id": 1,
  "problemId": 1,
  "problemTitle": "两数之和",
  "submissionId": 1,
  "errorType": "逻辑错误",
  "submitTime": "2024-01-01T00:00:00",
  "reviewStatus": "pending"
}
```

## 5. 错误码

| 错误码 | 描述 |
|--------|------|
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 6. 示例请求

### 6.1 登录请求

```bash
POST /api/user/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}
```

### 6.2 运行代码请求

```bash
POST /api/code/run
Content-Type: application/json

{
  "language": "java",
  "code": "public class Main { public static void main(String[] args) { System.out.println(\"Hello World\"); } }",
  "input": ""
}
```

### 6.3 提交题目请求

```bash
POST /api/submit/submit
Content-Type: application/json

{
  "problemId": 1,
  "language": "java",
  "code": "class Solution { public int[] twoSum(int[] nums, int target) { ... } }"
}
```

### 6.4 向AI提问请求

```bash
POST /api/ai/ask
Content-Type: application/json

{
  "question": "如何实现快速排序算法？",
  "code": "",
  "language": "java",
  "sessionId": ""
}
```

## 7. 安全注意事项

1. 所有API请求都需要携带有效的JWT Token
2. 敏感操作需要进行权限验证
3. 输入参数需要进行严格的验证和过滤
4. 防止SQL注入和XSS攻击
5. 对敏感数据进行加密存储

## 8. 版本管理

| 版本 | 日期 | 变更内容 |
|------|------|----------|
| v1.0 | 2024-01-01 | 初始版本 |
| v1.1 | 2024-01-15 | 新增学习路径和知识掌握度功能 |
| v1.2 | 2024-01-30 | 新增错题本功能 |
| v1.3 | 2024-02-15 | 新增AI多轮对话功能 |

---

# 新增功能接口文档

## 1. 新增功能概述

本次新增的P0级功能包括：

1. **个性化学习路径** - 基于用户能力和学习目标的自适应学习路径
2. **知识掌握度分析** - 实时追踪用户对各个知识点的掌握程度
3. **错题本与复习系统** - 自动收集错题并生成智能复习计划
4. **AI智能辅导** - 支持多轮对话的AI编程助手
5. **管理员权限管理** - 完善的后台管理功能

## 2. 新增API接口

### 2.1 学习路径相关接口 (`/api/learn`)

| 接口 | 方法 | 功能描述 | 请求参数 | 响应数据 |
|------|------|----------|----------|----------|
| `/assessment` | GET | 获取能力测评题目 | `language`, `direction`, `limit` | `[{id, question, options}]` |
| `/assessment/commit` | POST | 提交能力测评 | `language`, `direction`, `answers` | `{score, level, suggestions}` |
| `/path` | GET | 获取学习路径进度 | `pathId` | `{path, chapters: [{id, name, levels: [{id, name, status}]}]}` |
| `/path/chapter/{chapterId}` | GET | 获取章节详情 | N/A | `{id, name, description, levels}` |
| `/path/level/unlock` | POST | 解锁关卡 | `levelId` | `{success: true}` |
| `/path/level/complete` | POST | 完成关卡 | `levelId` | `{success: true}` |
| `/paths` | GET | 获取可用学习路径 | `language`, `direction` | `[{id, name, description, difficulty}]` |
| `/path/detail/{pathId}` | GET | 获取路径详情 | N/A | `{id, name, description, chapters}` |

### 2.2 知识掌握度相关接口 (`/api/learn`)

| 接口 | 方法 | 功能描述 | 请求参数 | 响应数据 |
|------|------|----------|----------|----------|
| `/knowledge/graph` | GET | 获取知识图谱 | N/A | `{nodes, edges}` |
| `/knowledge/mastery` | GET | 获取知识掌握度 | N/A | `[{knowledgePoint, masteryLevel, lastPracticeTime}]` |
| `/report/weekly` | GET | 获取周报 | N/A | `{solvedCount, submissionCount, accuracy, weakPoints, improvements}` |
| `/report/monthly` | GET | 获取月报 | `year`, `month` | `{solvedCount, submissionCount, accuracy, weakPoints, improvements, trends}` |
| `/weakness` | GET | 获取薄弱知识点 | N/A | `[{knowledgePoint, masteryLevel, recommendedProblems}]` |
| `/knowledge/distribution` | GET | 获取知识掌握度分布 | N/A | `{data: [{level, count}]}` |
| `/errors/frequency` | GET | 获取高频错误 | `limit` | `[{errorType, count, examples}]` |

### 2.3 错题本相关接口 (`/api/wrong-book`)

| 接口 | 方法 | 功能描述 | 请求参数 | 响应数据 |
|------|------|----------|----------|----------|
| `/list` | GET | 获取错题列表 | `knowledgePoint`, `difficulty` | `[{id, problemId, problemTitle, submissionId, errorType, submitTime, reviewStatus}]` |
| `/detail/{id}` | GET | 获取错题详情 | N/A | `{id, problem, submission, errorType, analysis, reviewStatus}` |
| `/review` | POST | 标记错题复习状态 | `id`, `status` | `{success: true}` |
| `/review/plan` | GET | 获取复习计划 | N/A | `[{id, wrongItemId, scheduledTime, reviewed}]` |
| `/review/pending` | GET | 获取待复习的错题 | N/A | `[{id, wrongItemId, problemTitle, scheduledTime}]` |
| `/review/update` | POST | 更新复习计划 | `id`, `reviewed` | `{success: true}` |
| `/recommend` | GET | 获取推荐题目 | `wrongItemId`, `limit` | `[{id, title, difficulty, relevance}]` |
| `/statistics` | GET | 获取错题统计信息 | N/A | `{totalCount, byDifficulty: [{difficulty, count}], byKnowledgePoint: [{knowledgePoint, count}]}` |
| `/distribution/knowledge` | GET | 获取按知识点分布的错题 | N/A | `{data: [{knowledgePoint, count}]}` |
| `/distribution/difficulty` | GET | 获取按难度分布的错题 | N/A | `{data: [{difficulty, count}]}` |
| `/delete/{id}` | POST | 删除错题 | N/A | `{success: true}` |

### 2.4 AI智能辅导相关接口 (`/api/ai`)

| 接口 | 方法 | 功能描述 | 请求参数 | 响应数据 |
|------|------|----------|----------|----------|
| `/ask` | POST | 向AI提问（支持多轮对话） | `question`, `code`, `language`, `sessionId` | `{answer, sessionId}` |
| `/history` | GET | 获取AI对话历史 | `page`, `size` | `{total, list: [{id, question, answer, sessionId, createTime}]}` |

## 3. 新增数据结构

### 3.1 学习路径数据结构

#### 3.1.1 学习路径

```json
{
  "id": 1,
  "name": "Java基础入门",
  "description": "Java语言基础学习路径",
  "language": "java",
  "direction": "backend",
  "difficulty": "beginner",
  "totalChapters": 5,
  "totalLevels": 20,
  "createdAt": "2024-01-01T00:00:00"
}
```

#### 3.1.2 路径章节

```json
{
  "id": 1,
  "pathId": 1,
  "name": "Java语法基础",
  "description": "Java语言的基本语法",
  "order": 1,
  "totalLevels": 4
}
```

#### 3.1.3 路径关卡

```json
{
  "id": 1,
  "chapterId": 1,
  "name": "变量与数据类型",
  "description": "学习Java的变量定义和数据类型",
  "order": 1,
  "difficulty": "easy",
  "requiredPoints": 0
}
```

#### 3.1.4 用户路径进度

```json
{
  "id": 1,
  "userId": 1,
  "pathId": 1,
  "currentChapterId": 2,
  "currentLevelId": 5,
  "completedChapters": 1,
  "completedLevels": 4,
  "progress": 20,
  "status": "in_progress",
  "startedAt": "2024-01-01T00:00:00",
  "lastUpdatedAt": "2024-01-15T00:00:00"
}
```

### 3.2 知识掌握度数据结构

#### 3.2.1 知识点

```json
{
  "id": 1,
  "name": "变量定义",
  "description": "Java变量的定义和使用",
  "parentId": null,
  "level": 1,
  "importance": 5
}
```

#### 3.2.2 用户知识掌握度

```json
{
  "id": 1,
  "userId": 1,
  "knowledgePointId": 1,
  "masteryLevel": 0.85,
  "practiceCount": 10,
  "correctCount": 8,
  "lastPracticeTime": "2024-01-15T00:00:00"
}
```

### 3.3 错题本数据结构

#### 3.3.1 错题本

```json
{
  "id": 1,
  "userId": 1,
  "totalItems": 10,
  "createdAt": "2024-01-01T00:00:00",
  "lastUpdatedAt": "2024-01-15T00:00:00"
}
```

#### 3.3.2 错题项

```json
{
  "id": 1,
  "wrongBookId": 1,
  "problemId": 1,
  "submissionId": 1,
  "errorType": "逻辑错误",
  "errorMessage": "数组越界",
  "analysis": "需要检查数组索引是否越界",
  "reviewStatus": "pending",
  "addedAt": "2024-01-15T00:00:00",
  "lastReviewedAt": null
}
```

#### 3.3.3 复习计划

```json
{
  "id": 1,
  "userId": 1,
  "wrongBookItemId": 1,
  "scheduledTime": "2024-01-16T00:00:00",
  "reviewed": false,
  "createdAt": "2024-01-15T00:00:00"
}
```

### 3.4 AI会话数据结构

#### 3.4.1 AI会话

```json
{
  "id": 1,
  "userId": 1,
  "sessionId": "session_123456",
  "title": "快速排序算法",
  "totalMessages": 5,
  "createdAt": "2024-01-15T00:00:00",
  "lastMessageAt": "2024-01-15T00:10:00"
}
```

#### 3.4.2 AI消息

```json
{
  "id": 1,
  "sessionId": "session_123456",
  "role": "user",
  "content": "如何实现快速排序算法？",
  "timestamp": "2024-01-15T00:00:00"
}
```

## 4. 新增功能的业务流程

### 4.1 个性化学习路径流程

1. 用户进行能力测评
2. 系统根据测评结果推荐合适的学习路径
3. 用户选择学习路径并开始学习
4. 系统记录用户的学习进度
5. 用户完成关卡后，系统自动解锁下一关
6. 系统定期生成学习报告，分析用户的学习情况

### 4.2 知识掌握度分析流程

1. 用户提交代码后，系统分析其对知识点的掌握程度
2. 系统更新用户的知识掌握度数据
3. 系统生成知识图谱，展示知识点之间的关系
4. 系统定期分析用户的薄弱知识点，并推荐相关题目
5. 系统生成周报和月报，展示用户的学习成果和改进方向

### 4.3 错题本与复习系统流程

1. 用户提交错误代码后，系统自动将题目加入错题本
2. 系统分析错误原因，生成错误类型和分析
3. 系统根据艾宾浩斯遗忘曲线生成复习计划
4. 用户按照复习计划进行复习
5. 系统跟踪复习效果，调整复习计划
6. 用户可以查看错题统计和分布情况

### 4.4 AI智能辅导流程

1. 用户向AI提问编程相关问题
2. 系统创建AI会话，记录对话历史
3. AI生成回答并返回给用户
4. 用户可以继续在同一会话中提问，AI会根据上下文提供更准确的回答
5. 系统保存对话历史，用户可以随时查看

## 5. 测试建议

### 5.1 功能测试

1. **学习路径测试**
   - 测试能力测评接口，确保能正确生成测评题目
   - 测试学习路径获取接口，确保能返回正确的路径信息
   - 测试关卡解锁和完成接口，确保能正确更新进度

2. **知识掌握度测试**
   - 测试知识掌握度获取接口，确保能返回正确的掌握度数据
   - 测试学习报告接口，确保能生成正确的周报和月报
   - 测试薄弱知识点分析接口，确保能准确识别用户的薄弱环节

3. **错题本测试**
   - 测试错题添加功能，确保提交错误代码后能自动加入错题本
   - 测试错题列表和详情接口，确保能返回正确的错题信息
   - 测试复习计划功能，确保能生成合理的复习计划
   - 测试复习状态更新接口，确保能正确更新复习状态

4. **AI智能辅导测试**
   - 测试AI提问接口，确保能正确处理用户问题
   - 测试多轮对话功能，确保AI能根据上下文提供连贯的回答
   - 测试对话历史接口，确保能正确保存和返回对话历史

### 5.2 性能测试

1. 测试API响应时间，确保在正常负载下响应时间不超过1秒
2. 测试并发处理能力，确保系统能同时处理多个用户的请求
3. 测试数据存储性能，确保数据读写操作高效

### 5.3 安全测试

1. 测试API认证和授权，确保未授权用户无法访问受保护的接口
2. 测试输入验证，确保系统能正确处理各种输入情况
3. 测试SQL注入和XSS攻击防护，确保系统安全

## 6. 部署注意事项

1. **数据库部署**
   - 执行数据库迁移脚本 `V1__add_p0_tables.sql`
   - 确保数据库连接配置正确

2. **后端部署**
   - 编译打包Spring Boot应用
   - 配置JWT密钥和其他环境变量
   - 部署应用到服务器

3. **前端部署**
   - 构建Vue3应用
   - 配置API基础路径
   - 部署到Web服务器

4. **监控部署**
   - 配置系统监控工具
   - 设置告警机制
   - 定期备份数据

## 7. 未来扩展建议

1. **扩展学习路径类型**
   - 添加更多编程语言和方向的学习路径
   - 支持自定义学习路径

2. **增强AI功能**
   - 集成更多AI模型
   - 支持代码自动生成和优化

3. **添加社交功能**
   - 支持用户之间的学习交流
   - 添加学习小组和竞赛功能

4. **增强数据分析**
   - 添加更多维度的学习数据分析
   - 生成个性化的学习建议

5. **支持移动应用**
   - 开发iOS和Android应用
   - 实现跨平台同步

---

本文档详细描述了个性化在线编程与答疑系统的API接口，包括现有功能和新增的P0级功能。通过这些接口，系统可以实现个性化学习路径、知识掌握度分析、错题本管理、AI智能辅导等核心功能，为用户提供更加智能、高效的编程学习体验。