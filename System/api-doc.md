# 个性化在线编程与答疑系统 - 后端接口文档

**项目名称**：个性化在线编程与答疑系统

**适用版本**：一周最小可用版

**基础地址**：`http://localhost:8080/api`

**文档版本**：v1.0

**通用规范**

- 除登录 / 注册外，所有接口请求头需携带：`Authorization: Bearer {token}`
- 统一返回格式：`{"code":200/500, "msg":"提示信息", "data":返回数据}`
- 时间格式：`yyyy-MM-dd HH:mm:ss`

***

## 目录

1. 用户模块接口
2. 在线代码运行接口
3. 题库模块接口
4. 提交判题接口
5. AI 答疑接口
6. 学习记录接口
7. 管理员模块接口
8. 状态码与枚举说明

***

# 1. 用户模块（User）

## 1.1 用户注册

- **接口地址**：`POST /user/register`
- **请求方式**：POST
- **请求参数**

```JSON
{
  "username": "testuser",
  "password": "123456"
}
```

- **返回结果**

```json
{
  "code": 200,
  "msg": "注册成功",
  "data": null
}
```

## 1.2 用户登录

- **接口地址**：`POST /user/login`
- **请求方式**：POST
- **请求参数**

```json
{
  "username": "testuser",
  "password": "123456"
}
```

- **返回结果**

```json
{
  "code": 200,
  "msg": "登录成功",
  "data": "eyJhbGciOiJIUzI1NiJ9..."
}
```

## 1.3 获取当前用户信息

- **接口地址**：`GET /user/info`
- **请求方式**：GET
- **请求头**：Authorization
- **返回结果**

```json
{
  "code": 200,
  "msg": "获取成功",
  "data": {
    "id": 1,
    "username": "testuser",
    "role": 0,
    "language": "java",
    "createTime": "2026-03-12 10:00:00"
  }
}
```

## 1.4 修改常用编程语言

- **接口地址**：`POST /user/update/language`
- **请求方式**：POST
- **请求参数**

```json
{
  "language": "java"
}
```

- **返回结果**：统一成功 / 失败

***

# 2. 在线代码运行模块（Code）

## 2.1 在线运行代码

- **接口地址**：`POST /code/run`
- **请求方式**：POST
- **请求参数**

```json
{
  "code": "public class Main{public static void main(String[] args){System.out.println(1+2);}}",
  "language": "java",
  "input": ""
}
```

- **返回结果**

```json
{
  "code": 200,
  "msg": "运行完成",
  "data": "3"
}
```

***

# 3. 题库模块（Problem）

## 3.1 分页获取题目列表

- **接口地址**：`GET /problem/list`
- **请求方式**：GET
- **请求参数**

```plantext
page: 1
size: 10
difficulty: 0/1/2（可选）
language: java/python（可选）
```

- **返回结果**

```json
{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "total": 100,
    "list": [
      {
        "id": 1,
        "title": "两数之和",
        "content": "给定a和b，求和",
        "difficulty": 0,
        "language": "java"
      }
    ]
  }
}
```

## 3.2 获取题目详情

- **接口地址**：`GET /problem/detail/{id}`
- **请求方式**：GET
- **路径参数**：id = 题目 ID
- **返回结果**：题目完整信息（包含新增字段）

**题目字段说明**：
- `id`: 题目ID
- `title`: 题目标题
- `content`: 题目描述
- `difficulty`: 难度（0-简单，1-中等，2-困难）
- `language`: 编程语言（java/python）
- `input`: 示例输入
- `output`: 示例输出
- `timeLimit`: 时间限制（毫秒），默认1000ms
- `memoryLimit`: 内存限制（MB），默认256MB
- `tags`: 题目标签，逗号分隔（如：动态规划,贪心算法,数组）
- `knowledgePoints`: 知识点，逗号分隔（如：动态规划,状态转移,边界处理）
- `hints`: 提示信息，JSON格式（如：[{"step":1,"content":"考虑使用动态规划"}]）
- `sampleExplanation`: 示例解释
- `createTime`: 创建时间
- `updateTime`: 更新时间
- `testCases`: 测试用例列表

**返回示例**：
```json
{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "id": 1,
    "title": "两数之和",
    "content": "给定a和b，求和",
    "difficulty": 0,
    "language": "java",
    "timeLimit": 1000,
    "memoryLimit": 256,
    "tags": "数组,基础算法",
    "knowledgePoints": "基础运算,输入输出",
    "hints": "[{\"step\":1,\"content\":\"考虑使用加法运算\"}]",
    "sampleExplanation": "对于输入1和2，输出应该是它们的和3",
    "input": "1 2",
    "output": "3",
    "createTime": "2026-03-12 10:00:00",
    "updateTime": "2026-03-12 10:00:00"
  }
}
```

***

## 3.3 按标签查询题目

- **接口地址**：`GET /problem/tag/{tag}`
- **请求方式**：GET
- **路径参数**：tag = 题目标签
- **返回结果**：匹配该标签的题目列表

**请求示例**：
```
GET /problem/tag/动态规划
```

**返回示例**：
```json
{
  "code": 200,
  "msg": "查询成功",
  "data": [
    {
      "id": 1,
      "title": "两数之和",
      "tags": "动态规划,贪心算法"
    }
  ]
}
```

## 3.4 按难度查询题目

- **接口地址**：`GET /problem/difficulty/{difficulty}`
- **请求方式**：GET
- **路径参数**：difficulty = 难度（0/1/2）
- **返回结果**：匹配该难度的题目列表

**请求示例**：
```
GET /problem/difficulty/0
```

**返回示例**：
```json
{
  "code": 200,
  "msg": "查询成功",
  "data": [
    {
      "id": 1,
      "title": "两数之和",
      "difficulty": 0
    }
  ]
}
```

## 3.5 获取题目样例测试用例

- **接口地址**：`GET /problem/{id}/test-cases/sample`
- **请求方式**：GET
- **路径参数**：id = 题目 ID
- **返回结果**：该题目的样例测试用例列表（is_sample=true）

**说明**：样例测试用例用于在题目详情页展示给用户参考

**请求示例**：
```
GET /problem/1/test-cases/sample
```

**返回示例**：
```json
{
  "code": 200,
  "msg": "查询成功",
  "data": [
    {
      "id": 1,
      "problemId": 1,
      "input": "1 2",
      "output": "3",
      "isSample": true,
      "sortOrder": 1,
      "createTime": "2026-03-12 10:00:00",
      "updateTime": "2026-03-12 10:00:00"
    }
  ]
}
```

## 3.6 获取题目所有测试用例（管理员）

- **接口地址**：`GET /problem/{id}/test-cases/all`
- **请求方式**：GET
- **路径参数**：id = 题目 ID
- **返回结果**：该题目的所有测试用例（包括样例和正式用例）

**说明**：此接口需要管理员权限，用于题目管理

**请求示例**：
```
GET /problem/1/test-cases/all
```

**返回示例**：
```json
{
  "code": 200,
  "msg": "查询成功",
  "data": [
    {
      "id": 1,
      "problemId": 1,
      "input": "1 2",
      "output": "3",
      "isSample": true,
      "sortOrder": 1,
      "createTime": "2026-03-12 10:00:00",
      "updateTime": "2026-03-12 10:00:00"
    },
    {
      "id": 2,
      "problemId": 1,
      "input": "100 200",
      "output": "300",
      "isSample": false,
      "sortOrder": 2,
      "createTime": "2026-03-12 10:00:00",
      "updateTime": "2026-03-12 10:00:00"
    }
  ]
}
```

## 3.7 测试用例字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 测试用例ID |
| problemId | Long | 题目ID |
| input | String | 输入数据 |
| output | String | 预期输出数据 |
| isSample | Boolean | 是否为样例用例（true=样例，false=正式） |
| sortOrder | Integer | 排序顺序 |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |

***

# 4. 提交判题模块（Submit）

## 4.1 提交代码判题

- **接口地址**：`POST /submit/commit`
- **请求方式**：POST
- **请求头**：Authorization
- **请求参数**

```json
{
  "problemId": 1,
  "code": "public class Main{...}",
  "language": "java"
}
```

- **返回结果**

```json
{
  "code": 200,
  "msg": "判题完成",
  "data": {
    "result": 0,
    "timeCost": 45,
    "memoryCost": 1024,
    "output": "3",
    "compileError": null,
    "runtimeError": null,
    "errorMessage": null,
    "testCaseResults": [
      {
        "testCaseId": 1,
        "result": 0,
        "timeCost": 20,
        "memoryCost": 512,
        "actualOutput": "3",
        "errorMessage": null
      },
      {
        "testCaseId": 2,
        "result": 0,
        "timeCost": 25,
        "memoryCost": 512,
        "actualOutput": "300",
        "errorMessage": null
      }
    ],
    "passedCount": 5,
    "totalCount": 5
  }
}
```

**结果码说明**：

- 0: AC（答案正确）
- 1: WA（答案错误）
- 2: RE（编译错误）
- 3: RTE（运行时错误）
- 4: TLE（超时）

**返回字段说明**：

| 字段 | 类型 | 说明 |
|------|------|------|
| result | Integer | 最终结果（0-AC，1-WA，2-RE，3-RTE，4-TLE） |
| timeCost | Integer | 最大耗时（毫秒） |
| memoryCost | Integer | 最大内存消耗（KB） |
| output | String | 第一个测试用例的输出 |
| compileError | String | 编译错误信息（result=2时） |
| runtimeError | String | 运行时错误信息（result=3时） |
| errorMessage | String | 其他错误信息（result=4时） |
| testCaseResults | Array | 每个测试用例的详细结果 |
| passedCount | Integer | 通过的测试用例数 |
| totalCount | Integer | 总测试用例数 |

**测试用例结果字段说明**（TestCaseResultVO）：

| 字段 | 类型 | 说明 |
|------|------|------|
| testCaseId | Long | 测试用例ID |
| result | Integer | 该测试用例的结果 |
| timeCost | Integer | 耗时（毫秒） |
| memoryCost | Integer | 内存消耗（KB） |
| actualOutput | String | 实际输出 |
| errorMessage | String | 错误信息 |

**判题逻辑说明**：

1. 系统会运行所有测试用例（包括样例和正式用例）
2. 如果编译错误（result=2），所有测试用例都标记为编译错误
3. 如果任何测试用例出现运行错误（result=3）或超时（result=4），最终结果设为该错误
4. 如果答案不匹配（result=1），最终结果设为答案错误
5. 只有所有测试用例都通过（result=0），最终结果才是AC
6. timeCost 和 memoryCost 取所有测试用例中的最大值
7. passedCount 统计 result=0 的测试用例数量

## 4.2 获取我的提交记录

- **接口地址**：`GET /submit/my`
- **请求方式**：GET
- **请求头**：Authorization
- **请求参数**：
  - problemId (可选): 题目ID，筛选特定题目的提交
  - page: 页码，默认1
  - size: 每页数量，默认10

**返回结果**：提交历史列表（包含题目信息）

**返回示例**：
```json
{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "total": 10,
    "list": [
      {
        "id": 1,
        "userId": 1,
        "problemId": 1,
        "code": "public class Main{...}",
        "language": "java",
        "result": 0,
        "timeCost": 45,
        "memoryCost": 1024,
        "createTime": "2026-03-12 10:00:00",
        "submitTime": "2026-03-12 10:00:00",
        "problemTitle": "两数之和",
        "difficulty": "EASY",
        "tags": ["数组", "基础算法"],
        "testCaseResults": [...]
      }
    ]
  }
}
```

## 4.3 获取提交详情

- **接口地址**：`GET /submit/detail/{submitId}`
- **请求方式**：GET
- **请求头**：Authorization
- **路径参数**：submitId = 提交记录ID
- **返回结果**：提交详情（包含完整的测试用例结果）

**请求示例**：
```
GET /submit/detail/1
```

**返回示例**：
```json
{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "id": 1,
    "userId": 1,
    "problemId": 1,
    "code": "public class Main{...}",
    "language": "java",
    "result": 0,
    "timeCost": 45,
    "memoryCost": 1024,
    "createTime": "2026-03-12 10:00:00",
    "submitTime": "2026-03-12 10:00:00",
    "problemTitle": "两数之和",
    "difficulty": "EASY",
    "tags": ["数组", "基础算法"],
    "testCaseResults": [
      {
        "id": 1,
        "submitId": 1,
        "testCaseId": 1,
        "result": 0,
        "timeCost": 20,
        "memoryCost": 512,
        "actualOutput": "3",
        "errorMessage": null,
        "createTime": "2026-03-12 10:00:00"
      }
    ]
  }
}
```

**提交详情字段说明**（SubmitWithProblemVO）：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 提交记录ID |
| userId | Long | 用户ID |
| problemId | Long | 题目ID |
| code | String | 提交的代码 |
| language | String | 编程语言 |
| result | Integer | 判题结果 |
| timeCost | Integer | 最大耗时（毫秒） |
| memoryCost | Integer | 最大内存消耗（KB） |
| createTime | LocalDateTime | 创建时间 |
| submitTime | LocalDateTime | 提交时间 |
| problemTitle | String | 题目标题 |
| difficulty | String | 难度（EASY/MEDIUM/HARD） |
| tags | Array | 题目标签列表 |
| testCaseResults | Array | 测试用例结果列表 |

***

# 5. AI 答疑模块（AI）

## 5.1 AI 编程问题提问

- **接口地址**：`POST /ai/ask`
- **请求方式**：POST
- **请求参数**

```json
{
  "content": "Java循环为什么死循环？",
  "code": "for(int i=0;i>=0;){}"
}
```

- **返回结果**

```json
{
  "code": 200,
  "msg": "回答成功",
  "data": "你的循环条件永远成立，导致无法退出..."
}
```

## 5.2 获取我的问答历史

- **接口地址**：`GET /ai/history`
- **请求方式**：GET
- **返回结果**：历史问题 + AI 回答列表

***

# 6. 学习记录模块（Learn）

## 6.1 获取我的学习统计

- **接口地址**：`GET /learn/my`
- **请求方式**：GET
- **返回结果**

```json
{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "problemCount": 15,
    "correctCount": 10,
    "correctRate": "66.67%",
    "lastLearnTime": "2026-03-12 15:30:00"
  }
}
```

## 6.2 个性化题目推荐

- **接口地址**：`GET /learn/recommend`
- **请求方式**：GET
- **返回结果**：推荐题目列表

***

# 7. 管理员模块（Admin）

> 仅角色 = 1（管理员）可访问，需要token验证 + role验证

## 7.1 用户列表

- **接口地址**：`GET /admin/user/list`

**请求头**：

```
Authorization: Bearer {admin_token}
```

**请求参数**：

```
page: 1
size: 10
```

**预期响应**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "total": 2,
    "list": [
      {
        "id": 1,
        "username": "admin",
        "role": 1,
        "language": "java"
      }
    ]
  }
}
```

**测试用例**：

- ✅ 管理员token访问
- ❌ 普通用户token访问（返回403）
- ❌ 无效token访问（返回401）

## 7.2 题目管理

> 需要管理员权限（role=1）

### 7.2.1 新增题目

**接口地址**：`POST /admin/problem/add`

**请求头**：

```
Authorization: Bearer {admin_token}
```

**请求参数**：

```json
{
  "title": "三数之和",
  "content": "给定三个整数a、b、c，计算它们的和并输出。",
  "input": "1 2 3",
  "output": "6",
  "difficulty": 1,
  "language": "java"
}
```

**预期响应**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

**测试用例**：

- ✅ 管理员token访问
- ❌ 普通用户token访问（返回403）
- ❌ 无效token访问（返回401）
- ❌ 题目已存在（返回500）

#### 7.2.1.1 新增题目（带验重）

**接口地址**：`POST /admin/problem/add-with-check`

**请求头**：

```
Authorization: Bearer {admin_token}
```

**请求参数**：

```json
{
  "title": "三数之和",
  "content": "给定三个整数a、b、c，计算它们的和并输出。",
  "input": "1 2 3",
  "output": "6",
  "difficulty": 1,
  "language": "java"
}
```

**预期响应**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

**错误响应**：

```json
{
  "code": 500,
  "msg": "题目已存在：三数之和",
  "data": null
}
```

**测试用例**：

- ✅ 管理员token访问
- ✅ 新题目添加成功
- ❌ 题目已存在时返回错误
- ❌ 普通用户token访问（返回403）

**说明**：

- 添加题目时会检查题目是否已存在
- 如果题目已存在，返回错误提示
- 批量导入时会自动跳过重复题目

### 7.2.2 修改题目

**接口地址**：`POST /admin/problem/update`

**请求头**：

```
Authorization: Bearer {admin_token}
```

**请求参数**：

```json
{
  "id": 1,
  "title": "两数之和（修改）",
  "content": "给定两个整数a和b，计算它们的和并输出。",
  "input": "1 2",
  "output": "3",
  "difficulty": 0,
  "language": "java"
}
```

**预期响应**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

#### 7.2.3 删除题目

**接口地址**：`POST /admin/problem/delete/{id}`

**请求头**：

```
Authorization: Bearer {admin_token}
```

**预期响应**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

#### 7.2.4 批量导入题目

**接口地址**：`POST /admin/problem/batch-import`

**请求头**：

```
Authorization: Bearer {admin_token}
```

**请求参数**：

```json
{
  "problems": [
    {
      "title": "三数之和",
      "content": "给定三个整数a、b、c，计算它们的和并输出。",
      "input": "1 2 3",
      "output": "6",
      "difficulty": 1,
      "language": "java"
    },
    {
      "title": "四数之和",
      "content": "给定四个整数a、b、c、d，计算它们的和并输出。",
      "input": "1 2 3 4",
      "output": "10",
      "difficulty": 2,
      "language": "java"
    }
  ]
}
```

**预期响应**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

**测试用例**：

- ✅ 管理员token访问
- ❌ 普通用户token访问（返回403）
- ❌ 无效token访问（返回401）
- ✅ 批量导入多个题目（事务一致性）

**说明**：

- 批量导入使用事务保证数据一致性
- 适合一次性导入大量题目
- 如果导入失败，所有题目都不会被保存

#### 7.2.5 文件导入题目

**Excel导入接口**

- **接口地址**：`POST /admin/problem/import-excel`
- **请求头**：

```
Authorization: Bearer {admin_token}
Content-Type: multipart/form-data
```

- **请求参数**：

```
file: Excel文件（.xlsx格式）
```

**预期响应**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

**CSV导入接口**

- **接口地址**：`POST /admin/problem/import-csv`
- **请求头**：

```
Authorization: Bearer {admin_token}
Content-Type: multipart/form-data
```

- **请求参数**：

```
file: CSV文件（.csv格式）
```

**预期响应**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

**测试用例**：

- ✅ 管理员token访问
- ❌ 普通用户token访问（返回403）
- ✅ Excel文件导入成功
- ✅ CSV文件导入成功
- ✅ 批量导入时跳过重复题目
- ❌ 文件格式错误（返回500）
- ❌ 文件过大（返回500）

**说明**：

- 批量导入使用事务保证数据一致性
- 适合一次性导入大量题目
- 如果导入失败，所有题目都不会被保存
- **批量导入时会自动跳过重复题目**，只添加新题目
- 重复题目会在日志中记录警告信息

**文件格式要求**：

Excel格式（.xlsx）：

```
| 标题 | 内容 | 输入示例 | 输出示例 | 难度 | 语言 |
|:----:|:----:|:----:|:----:|:----:|
| 三数之和 | 给定三个整数a、b、c，计算它们的和并输出。 | 1 2 3 | 6 | 1 | java |
```

CSV格式（.csv）：

```csv
标题,内容,输入示例,输出示例,难度,语言
三数之和,给定三个整数a、b、c，计算它们的和并输出。,1 2 3,6,1,java
```

**注意事项**：

- 文件大小限制：10MB
- Excel第一行为表头，从第二行开始是数据
- CSV第一行为表头，使用逗号分隔字段
- 所有字段都是必填的

## 7.3 查看所有提交记录

- **接口地址**：`GET /admin/submit/list`

**请求头**：

```
Authorization: Bearer {admin_token}
```

**请求参数**：

```
page: 1
size: 10
```

**预期响应**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "total": 10,
    "list": [
      {
        "id": 1,
        "userId": 1,
        "problemId": 1,
        "result": 0,
        "timeCost": 150,
        "createTime": "2026-03-12T10:00:00"
      }
    ]
  }
}
```

**测试用例**：

- ✅ 管理员token访问
- ❌ 普通用户token访问（返回403）

## 7.4 系统统计数据

- **接口地址**：`GET /admin/statistics`

**请求头**：

```
Authorization: Bearer {admin_token}
```

**预期响应**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "userCount": 10,
    "problemCount": 20,
    "submitCount": 100,
    "correctCount": 70,
    "correctRate": "70.00%"
  }
}
```

**测试用例**：

- ✅ 管理员token访问
- ❌ 普通用户token访问（返回403）

***

# 8. 状态码与枚举说明

## 8.1 通用状态码

|  状态码 |       含义       |
| :--: | :------------: |
|  200 |      操作成功      |
|  401 | 未登录 / Token 无效 |
|  403 |      无权限访问     |
|  500 |      服务器异常     |
| 1001 |     用户名已存在     |
| 1002 |    用户名或密码错误    |
| 2001 |     代码运行超时     |
| 2002 |    不支持的编程语言    |

## 8.2 枚举值

- **用户角色**：0 = 普通用户，1 = 管理员
- **题目难度**：0 = 简单，1 = 中等，2 = 困难
- **判题结果**：0=AC，1=WA，2=RE，3=TLE，4=MLE
- **支持语言**：java、python

