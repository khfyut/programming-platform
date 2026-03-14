# 个性化在线编程与答疑系统 - 接口测试文档

**项目名称**：个性化在线编程与答疑系统

**基础地址**：`http://localhost:8080/api`

**文档版本**：v1.0

**测试工具推荐**：Postman、Apifox、curl

***

## 测试前准备

### 1. 启动项目

```bash
mvn spring-boot:run
```

### 2. 数据库初始化

确保MySQL数据库已创建并初始化数据：

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS programming_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE programming_system;

-- 初始化管理员账号（密码：admin123）
INSERT INTO `user` (`username`, `password`, `role`) VALUES ('admin', '$2a$10$8H9w5f8G7s6D5a4s3d2f1g0h9j8k7l6m5n4b3v2c1x9z8y7u6t5r4e3w2q1a0s9d8f7g6h5j4k3l2', 1);

-- 初始化测试题目
INSERT INTO `problem` (`title`, `content`, `input`, `output`, `difficulty`) 
VALUES ('两数之和', '给定两个整数a和b，计算它们的和并输出。', '1 2', '3', 0);
```

### 3. Docker环境准备

确保Docker服务已启动并可访问：

```bash
# Windows
docker -H tcp://192.168.59.133:2375 ps

# Linux/Mac
docker ps
```

***

## 1. 用户模块测试

### 1.1 用户注册

**接口地址**：`POST /user/register`

**请求示例**：

```json
{
  "username": "testuser",
  "password": "123456"
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

- ✅ 正常注册：新用户名
- ❌ 重复注册：已存在的用户名

***

### 1.2 用户登录

**接口地址**：`POST /user/login`

**请求示例**：

```json
{
  "username": "testuser",
  "password": "123456"
}
```

**预期响应**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJpZCI6MSwidXNlcm5hbWUiOiJ0ZXN0dXNlciIsImV4cCI6MTcxMjU0NjAwMH0.YzJmNjY4ZTQyZmQ2MjU5MjU5ZjZjMmYxZTc5NjY0ZjQxYzI1ZjM"
  }
}
```

**测试用例**：

- ✅ 正确的用户名和密码
- ❌ 错误的用户名或密码

***

### 1.3 获取当前用户信息

**接口地址**：`GET /user/info`

**请求头**：

```
Authorization: Bearer {token}
```

**预期响应**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "id": 1,
    "username": "testuser",
    "role": 0,
    "language": "java",
    "createTime": "2026-03-12T10:00:00",
    "updateTime": "2026-03-12T10:00:00"
  }
}
```

**测试用例**：

- ✅ 有效token
- ❌ 无效token
- ❌ 缺少token

***

### 1.4 修改常用编程语言

**接口地址**：`POST /user/update/language`

**请求头**：

```
Authorization: Bearer {token}
```

**请求示例**：

```json
{
  "language": "python"
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

- ✅ 修改为java
- ✅ 修改为python

***

## 2. 在线代码运行模块测试

### 2.1 在线运行代码

**接口地址**：`POST /code/run`

**请求示例**：

```json
{
  "code": "public class Main{public static void main(String[] args){System.out.println(1+2);}}",
  "language": "java",
  "input": ""
}
```

**预期响应**：

```json
{
  "code": 200,
  "msg": "运行完成",
  "data": "3"
}
```

**测试用例**：

- ✅ Java代码：输出3
- ✅ Python代码：输出Hello World
- ❌ 不支持的语言

**Python示例**：

```json
{
  "code": "print(1+2)",
  "language": "python",
  "input": ""
}
```

***

## 3. 题库模块测试

### 3.1 分页获取题目列表

**接口地址**：`GET /problem/list`

**请求参数**：

```
page: 1
size: 10
difficulty: 0（可选）
language: java（可选）
```

**预期响应**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "total": 1,
    "list": [
      {
        "id": 1,
        "title": "两数之和",
        "content": "给定两个整数a和b，计算它们的和并输出。",
        "input": "1 2",
        "output": "3",
        "difficulty": 0,
        "language": "java",
        "createTime": "2026-03-12T10:00:00",
        "updateTime": "2026-03-12T10:00:00"
      }
    ]
  }
}
```

**测试用例**：

- ✅ 获取第1页
- ✅ 按难度筛选：difficulty=0
- ✅ 按语言筛选：language=java

***

### 3.2 获取题目详情

**接口地址**：`GET /problem/detail/{id}`

**预期响应**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "id": 1,
    "title": "两数之和",
    "content": "给定两个整数a和b，计算它们的和并输出。",
    "input": "1 2",
    "output": "3",
    "difficulty": 0,
    "language": "java",
    "createTime": "2026-03-12T10:00:00",
    "updateTime": "2026-03-12T10:00:00"
  }
}
```

**测试用例**：

- ✅ 存在的题目ID
- ❌ 不存在的题目ID

***

## 4. 提交判题模块测试

### 4.1 提交代码判题

**接口地址**：`POST /submit/commit`

**请求头**：

```
Authorization: Bearer {token}
```

**请求示例**：

```json
{
  "problemId": 1,
  "code": "public class Main{public static void main(String[] args){int a=1,b=2;System.out.println(a+b);}}",
  "language": "java"
}
```

**预期响应**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "result": 0,
    "timeCost": 150,
    "memoryCost": 0
  }
}
```

**测试用例**：

- ✅ 正确答案：result=0
- ❌ 错误答案：result=1
- ❌ 不存在的题目ID

**结果码说明**：

- 0: AC（通过）
- 1: WA（答案错误）
- 2: RE（运行错误）
- 3: TLE（超时）
- 4: MLE（内存超限）

***

### 4.2 获取我的提交记录

**接口地址**：`GET /submit/my`

**请求头**：

```
Authorization: Bearer {token}
```

**请求参数**：

```
page: 1
size: 10
problemId: 1（可选）
```

**预期响应**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "total": 1,
    "list": [
      {
        "id": 1,
        "userId": 1,
        "problemId": 1,
        "code": "public class Main{...}",
        "language": "java",
        "result": 0,
        "timeCost": 150,
        "memoryCost": 0,
        "createTime": "2026-03-12T10:00:00"
      }
    ]
  }
}
```

**测试用例**：

- ✅ 获取所有提交
- ✅ 按题目筛选：problemId=1

***

## 5. AI 答疑模块测试

### 5.1 AI 编程问题提问

**接口地址**：`POST /ai/ask`

**请求头**：

```
Authorization: Bearer {token}
```

**请求示例**：

```json
{
  "content": "Java循环为什么死循环？",
  "code": "for(int i=0;i>=0;){}"
}
```

**预期响应**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": "你的循环条件永远成立，导致无法退出循环。建议修改循环条件，例如：for(int i=0;i<10;i++) {...}"
}
```

**测试用例**：

- ✅ 纯文本问题
- ✅ 带代码的问题
- ❌ 空内容

***

### 5.2 获取我的问答历史

**接口地址**：`GET /ai/history`

**请求头**：

```
Authorization: Bearer {token}
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
    "total": 1,
    "list": [
      {
        "id": 1,
        "userId": 1,
        "content": "Java循环为什么死循环？",
        "answer": "你的循环条件永远成立...",
        "code": "for(int i=0;i>=0;){}",
        "createTime": "2026-03-12T10:00:00"
      }
    ]
  }
}
```

**测试用例**：

- ✅ 获取历史记录
- ✅ 分页查询

***

## 6. 学习记录模块测试

### 6.1 获取我的学习统计

**接口地址**：`GET /learn/my`

**请求头**：

```
Authorization: Bearer {token}
```

**预期响应**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "problemCount": 15,
    "correctCount": 10,
    "correctRate": "66.67%",
    "lastLearnTime": "2026-03-12T15:30:00"
  }
}
```

**测试用例**：

- ✅ 有学习记录的用户
- ✅ 无学习记录的用户

***

### 6.2 个性化题目推荐

**接口地址**：`GET /learn/recommend`

**请求头**：

```
Authorization: Bearer {token}
```

**预期响应**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "id": 2,
      "title": "题目2",
      "content": "题目描述",
      "difficulty": 0,
      "language": "java"
    }
  ]
}
```

**测试用例**：

- ✅ 基于历史推荐
- ✅ 新用户推荐

***

## 7. 管理员模块测试

> 注意：管理员接口需要用户角色为1（管理员）

### 7.1 用户列表

**接口地址**：`GET /admin/user/list`

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

***

### 7.2 题目管理

#### 7.2.1 新增题目

**接口地址**：`POST /admin/problem/add`

**请求头**：

```
Authorization: Bearer {admin_token}
```

**请求示例**：

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

#### 7.2.2 修改题目

**接口地址**：`POST /admin/problem/update`

**请求头**：

```
Authorization: Bearer {admin_token}
```

**请求示例**：

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

***

### 7.3 查看所有提交记录

**接口地址**：`GET /admin/submit/list`

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

***

### 7.4 系统统计数据

**接口地址**：`GET /admin/statistics`

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

***

## 7. 管理员权限测试

### 7.1 测试普通用户权限

```bash
# 注册普通用户
curl -X POST http://localhost:8080/api/user/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456"}'

# 登录获取token
TOKEN=$(curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456"}' \
  | jq -r '.data.token')

# 尝试访问管理员接口（应该返回403）
curl -X GET http://localhost:8080/api/admin/user/list \
  -H "Authorization: Bearer $TOKEN"
```

**预期响应**：

```json
{
  "code": 403,
  "msg": "无权限访问",
  "data": null
}
```

### 7.2 测试管理员权限

```bash
# 使用管理员账号登录
ADMIN_TOKEN=$(curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' \
  | jq -r '.data.token')

# 访问管理员接口（应该成功）
curl -X GET http://localhost:8080/api/admin/user/list \
  -H "Authorization: Bearer $ADMIN_TOKEN"
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

### 7.3 测试管理员功能

#### 7.3.1 添加单个题目

```bash
# 添加题目（无验重）
curl -X POST http://localhost:8080/api/admin/problem/add \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"新题目","content":"题目描述","input":"1","output":"2","difficulty":1,"language":"java"}'
```

**预期响应**：
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

#### 7.3.1.1 添加题目（带验重）

```bash
# 添加题目（带验重）
curl -X POST http://localhost:8080/api/admin/problem/add-with-check \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"新题目","content":"题目描述","input":"1","output":"2","difficulty":1,"language":"java"}'
```

**预期响应**：
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

**错误响应**（题目已存在）：
```json
{
  "code": 500,
  "msg": "题目已存在：新题目",
  "data": null
}
```

#### 7.3.2 批量导入题目

```bash
# 批量导入题目（JSON格式）
curl -X POST http://localhost:8080/api/admin/problem/batch-import \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
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
  }'
```

**预期响应**：
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

**批量导入时跳过重复题目**：
```bash
# 批量导入包含重复题目
curl -X POST http://localhost:8080/api/admin/problem/batch-import \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
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
        "title": "三数之和",
        "content": "给定三个整数a、b、c，计算它们的和并输出。",
        "input": "1 2 3",
        "output": "6",
        "difficulty": 1,
        "language": "java"
      }
    ]
  }'
```

**预期响应**：
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

**说明**：
- 重复题目会被跳过，不会重复添加
- 重复题目会在后端日志中记录警告信息
- 只有新题目会被成功添加

#### 7.3.3 Excel文件导入题目

```bash
# 导入Excel文件
curl -X POST http://localhost:8080/api/admin/problem/import-excel \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -F "file=@/path/to/problems.xlsx"
```

**预期响应**：
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

#### 7.3.4 CSV文件导入题目

```bash
# 导入CSV文件
curl -X POST http://localhost:8080/api/admin/problem/import-csv \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -F "file=@/path/to/problems.csv"
```

**预期响应**：
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

#### 7.3.5 查看系统统计

```bash
# 查看系统统计
curl -X GET http://localhost:8080/api/admin/statistics \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

**预期响应**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "userCount": 2,
    "problemCount": 4,
    "submitCount": 1,
    "correctCount": 0,
    "correctRate": "0.00%"
  }
}
```

### 7.4 权限验证总结

|     测试场景    |  使用Token  |   预期结果   |      说明     |
| :---------: | :-------: | :------: | :---------: |
| 普通用户访问管理员接口 | 普通用户token | 403（无权限） |   ✅ 权限控制正常  |
|  管理员访问管理员接口 |  管理员token |  200（成功） |   ✅ 权限控制正常  |
|  管理员访问管理员接口 |  无效token  | 401（未登录） | ✅ token验证正常 |

***

## 8. 通用状态码说明

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

***

## 9. 测试脚本示例

### 9.1 使用curl测试

```bash
# 1. 注册用户
curl -X POST http://localhost:8080/api/user/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456"}'

# 2. 登录获取token
TOKEN=$(curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456"}' \
  | jq -r '.data.token')

# 3. 获取用户信息
curl -X GET http://localhost:8080/api/user/info \
  -H "Authorization: Bearer $TOKEN"

# 4. 运行代码
curl -X POST http://localhost:8080/api/code/run \
  -H "Content-Type: application/json" \
  -d '{"code":"print(1+2)","language":"python","input":""}'

# 5. 提交题目
curl -X POST http://localhost:8080/api/submit/commit \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"problemId":1,"code":"public class Main{...}","language":"java"}'
```

### 9.2 Postman测试步骤

1. **创建Collection**：新建"编程系统测试"Collection
2. **设置环境变量**：
   - `base_url`: `http://localhost:8080/api`
   - `token`: 登录后获取的token
3. **按顺序测试**：
   - 用户注册 → 用户登录 → 获取用户信息 → 修改语言
   - 运行代码 → 获取题目列表 → 提交题目
   - AI提问 → 获取AI历史
   - 获取学习统计 → 获取推荐题目
   - 管理员功能（需要管理员账号）

***

## 10. 常见问题排查

### 10.1 编译错误

```bash
# 清理并重新编译
mvn clean compile

# 如果Lombok相关错误，检查pom.xml配置
# 确保maven-compiler-plugin配置了annotationProcessorPaths
```

### 10.2 数据库连接失败

```bash
# 检查MySQL服务状态
mysql -h 192.168.59.133 -u root -p

# 检查数据库是否存在
SHOW DATABASES;
```

### 10.3 Docker连接失败

```bash
# 检查Docker服务
docker -H tcp://192.168.59.133:2375 ps

# 检查Docker API是否可访问
curl http://192.168.59.133:2375/version
```

### 10.4 AI API调用失败

```bash
# 检查API Key是否正确
# 检查网络连接
curl https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions \
  -H "Authorization: Bearer YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"model":"qwen-turbo","messages":[{"role":"user","content":"test"}]}'
```

***

## 11. 性能测试建议

### 11.1 并发测试

使用JMeter或Apache Bench进行压力测试：

```bash
# Apache Bench
ab -n 1000 -c 10 http://localhost:8080/api/problem/list
```

### 11.2 响应时间监控

- 代码运行接口：建议响应时间 < 5秒
- AI问答接口：建议响应时间 < 10秒
- 数据查询接口：建议响应时间 < 500ms

***

**测试完成后**：

- ✅ 所有接口返回正确的状态码
- ✅ 数据库记录正确保存
- ✅ Docker容器正常创建和销毁
- ✅ AI API正常调用
- ✅ 学习记录正确更新
- ✅ 管理员权限验证正常

