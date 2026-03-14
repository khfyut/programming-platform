# 个性化在线编程与答疑系统

一个基于Spring Boot + Docker的在线编程学习与AI答疑平台，支持代码运行、题目练习、提交判题、AI问答等功能。

## 目录

- [项目介绍](#项目介绍)
- [功能特性](#功能特性)
- [技术栈](#技术栈)
- [快速开始](#快速开始)
- [项目结构](#项目结构)
- [接口文档](#接口文档)
- [配置说明](#配置说明)
- [测试说明](#测试说明)
- [常见问题](#常见问题)
- [贡献指南](#贡献指南)

## 项目介绍

本项目是一个**个性化在线编程与答疑系统**，旨在为编程学习者提供一个完整的在线编程学习环境。系统集成了代码运行、题目练习、提交判题、AI答疑等功能，帮助用户提升编程能力。

### 核心功能

- 在线代码运行（支持Java、Python）
- 题库管理（支持增删改查）
- 提交判题（自动判题，详细错误反馈）
- AI智能答疑（集成阿里云通义千问）
- 学习记录追踪（个性化推荐）
- 用户权限管理（普通用户/管理员）
- 文件批量导入（Excel、CSV）

### 适用场景

- 编程教学
- 在线练习
- 代码评测
- 编程学习

## 功能特性

### 用户模块

- 用户注册（默认普通用户角色）
- 用户登录（JWT认证）
- 获取用户信息
- 修改常用编程语言

### 代码运行模块

- 在线运行代码
- 支持Java、Python
- Docker容器隔离
- 详细错误反馈

### 题库模块

- 分页查询题目列表
- 获取题目详情
- 按难度和语言筛选
- 管理员管理题目

### 提交判题模块

- 提交代码判题
- 自动判题（AC/WA/RE/RTE/TLE）
- 获取我的提交记录
- 详细错误反馈

### AI答疑模块

- AI编程问题提问
- 获取问答历史记录
- 智能代码建议

### 学习记录模块

- 获取学习统计
- 个性化题目推荐
- 学习进度追踪

### 管理员模块

- 用户列表管理
- 题目管理（增删改查）
- 批量导入题目（JSON/Excel/CSV）
- 查看所有提交记录
- 系统统计数据

### 高级特性

- 题目验重（避免重复添加）
- 批量导入自动跳过重复题目
- 详细的日志反馈（编译错误、运行时错误、运行失败）
- 事务一致性保证

## 技术栈

### 后端技术

- **Spring Boot 3.2.0** - Web框架
- **MyBatis 3.0.3** - ORM框架
- **MySQL 8.0** - 数据库
- **Docker Java API** - 容器管理
- **JWT** - 用户认证
- **OkHttp** - HTTP客户端
- **Apache POI** - Excel解析
- **OpenCSV** - CSV解析
- **Lombok** - 简化代码
- **FastJSON2** - JSON处理

### 前端技术

- 支持任何现代前端框架
- RESTful API接口
- JWT Token认证

### 基础设施

- **Docker** - 代码运行环境
- **MySQL** - 数据存储
- **Redis** - 缓存（可选）

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Docker（用于代码运行）

### 安装步骤

1. 克隆项目
```bash
git clone https://github.com/your-username/programming-system.git
cd programming-system
```

2. 修改数据库配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/programming_system
    username: root
    password: your_password
```

3. 修改Docker配置
```yaml
programming:
  docker:
    host: tcp://localhost:2375
    timeout: 2000
    memory: 134217728
```

4. 修改AI配置
```yaml
programming:
  ai:
    api-key: your_api_key
    api-url: https://dashscope.aliyuncs.com/compatible-mode/v1
```

5. 启动项目
```bash
mvn spring-boot:run
```

6. 访问系统
```
http://localhost:8080
```

### 初始化数据

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS programming_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE programming_system;

-- 创建管理员账号（密码：admin123）
INSERT INTO `user` (`username`, `password`, `role`, `language`) 
VALUES ('admin', '$2a$10$8H9w5f8G7s6D5a4s3d2f1g0h9j8k7l6m5n4b3v2c1x9z8y7u6t5r4e3w2q1a0s9d8f7g6h5j4k3l2', 1, 'java');

-- 创建测试题目
INSERT INTO `problem` (`title`, `content`, `input`, `output`, `difficulty`, `language`) 
VALUES ('两数之和', '给定两个整数a和b，计算它们的和并输出。', '1 2', '3', 0, 'java');
```

## 项目结构

```
programming-system/
├── src/main/java/com/programming/
│   ├── controller/          # 控制器层
│   │   ├── AiController.java
│   │   ├── AdminController.java
│   │   ├── CodeController.java
│   │   ├── FileController.java
│   │   ├── LearnController.java
│   │   ├── ProblemController.java
│   │   ├── SubmitController.java
│   │   └── UserController.java
│   ├── service/            # 服务层
│   │   ├── impl/
│   │   │   ├── AiServiceImpl.java
│   │   │   ├── AdminServiceImpl.java
│   │   │   ├── CodeSandboxServiceImpl.java
│   │   │   ├── LearnServiceImpl.java
│   │   │   ├── ProblemServiceImpl.java
│   │   │   ├── SubmitServiceImpl.java
│   │   │   └── UserServiceImpl.java
│   │   ├── AiService.java
│   │   ├── AdminService.java
│   │   ├── CodeSandboxService.java
│   │   ├── LearnService.java
│   │   ├── ProblemService.java
│   │   ├── SubmitService.java
│   │   └── UserService.java
│   ├── mapper/             # 数据访问层
│   │   ├── AiMapper.xml
│   │   ├── AdminWebMvcConfig.java
│   │   ├── AdminInterceptor.java
│   │   ├── LearnRecordMapper.xml
│   │   ├── ProblemMapper.xml
│   │   ├── QuestionMapper.xml
│   │   ├── SubmitMapper.xml
│   │   ├── UserMapper.xml
│   │   ├── AiMapper.java
│   │   ├── LearnRecordMapper.java
│   │   ├── ProblemMapper.java
│   │   ├── QuestionMapper.java
│   │   ├── SubmitMapper.java
│   │   └── UserMapper.java
│   ├── entity/             # 实体类
│   │   ├── Ai.java
│   │   ├── AdminInterceptor.java
│   │   ├── LearnRecord.java
│   │   ├── Problem.java
│   │   ├── Question.java
│   │   ├── Submit.java
│   │   └── User.java
│   ├── util/               # 工具类
│   │   ├── DockerUtil.java
│   │   ├── ExcelUtil.java
│   │   ├── JwtUtil.java
│   │   └── ResultUtil.java
│   └── vo/                 # 视图对象
│       ├── AiAskVO.java
│       ├── BatchImportProblemsVO.java
│       ├── LoginVO.java
│       ├── RegisterVO.java
│       ├── SubmitResultVO.java
│       └── SubmitVO.java
├── src/main/resources/
│   ├── mybatis/            # MyBatis映射文件
│   │   ├── AiMapper.xml
│   │   ├── LearnRecordMapper.xml
│   │   ├── ProblemMapper.xml
│   │   ├── QuestionMapper.xml
│   │   ├── SubmitMapper.xml
│   │   └── UserMapper.xml
│   └── application.yml     # 配置文件
├── pom.xml                 # Maven配置文件
├── api-doc.md             # API接口文档
├── TEST.md                 # 测试文档
├── IMPORT_TEMPLATE.md       # 导入模板文档
└── README.md               # 项目说明文档
```

## 接口文档

### 基础信息

- 基础URL：`http://localhost:8080/api`
- 认证方式：JWT Token
- 请求格式：JSON / multipart/form-data
- 响应格式：JSON

### 用户模块

#### 用户注册
- 接口：`POST /api/user/register`
- 说明：注册新用户，默认为普通用户角色
- 参数：username, password

#### 用户登录
- 接口：`POST /api/user/login`
- 说明：用户登录，返回JWT Token
- 参数：username, password
- 返回：token, userId, username, role, language

#### 获取用户信息
- 接口：`GET /api/user/info`
- 说明：获取当前登录用户信息
- 需要：Authorization Header

#### 修改编程语言
- 接口：`POST /api/user/update/language`
- 说明：修改用户常用编程语言
- 参数：language (java/python)
- 需要：Authorization Header

### 代码运行模块

#### 在线运行代码
- 接口：`POST /api/code/run`
- 说明：在线运行代码
- 参数：code, language, input
- 返回：运行结果

### 题库模块

#### 获取题目列表
- 接口：`GET /api/problem/list`
- 说明：分页获取题目列表
- 参数：page, size, difficulty, language
- 返回：total, list

#### 获取题目详情
- 接口：`GET /api/problem/detail/{id}`
- 说明：获取题目详细信息
- 需要：Authorization Header

### 提交判题模块

#### 提交代码判题
- 接口：`POST /api/submit/commit`
- 说明：提交代码进行判题
- 参数：problemId, code, language
- 返回：result, timeCost, memoryCost, output, compileError, runtimeError, errorMessage

#### 获取我的提交记录
- 接口：`GET /api/submit/my`
- 说明：获取当前用户的提交记录
- 参数：page, size, problemId
- 需要：Authorization Header

### AI答疑模块

#### AI编程问题提问
- 接口：`POST /api/ai/ask`
- 说明：向AI提问编程问题
- 参数：content, code
- 需要：Authorization Header
- 返回：AI回答

#### 获取问答历史
- 接口：`GET /api/ai/history`
- 说明：获取用户的问答历史记录
- 参数：page, size
- 需要：Authorization Header

### 学习记录模块

#### 获取学习统计
- 接口：`GET /api/learn/my`
- 说明：获取用户学习统计数据
- 需要：Authorization Header
- 返回：problemCount, correctCount, correctRate, lastLearnTime

#### 个性化题目推荐
- 接口：`GET /api/learn/recommend`
- 说明：根据用户学习记录推荐题目
- 需要：Authorization Header

### 管理员模块

#### 用户列表
- 接口：`GET /api/admin/user/list`
- 说明：获取所有用户列表
- 需要：管理员Token + Authorization Header

#### 新增题目
- 接口：`POST /api/admin/problem/add`
- 说明：添加单个题目
- 需要：管理员Token + Authorization Header

#### 新增题目（带验重）
- 接口：`POST /api/admin/problem/add-with-check`
- 说明：添加题目时检查是否重复
- 需要：管理员Token + Authorization Header

#### 修改题目
- 接口：`POST /api/admin/problem/update`
- 说明：修改题目信息
- 需要：管理员Token + Authorization Header

#### 删除题目
- 接口：`POST /api/admin/problem/delete/{id}`
- 说明：删除指定题目
- 需要：管理员Token + Authorization Header

#### 批量导入题目（JSON）
- 接口：`POST /api/admin/problem/batch-import`
- 说明：批量导入题目（JSON格式）
- 需要：管理员Token + Authorization Header
- 自动跳过重复题目

#### Excel文件导入
- 接口：`POST /api/admin/problem/import-excel`
- 说明：从Excel文件导入题目
- 需要：管理员Token + Authorization Header
- 文件格式：.xlsx
- 文件大小限制：10MB

#### CSV文件导入
- 接口：`POST /api/admin/problem/import-csv`
- 说明：从CSV文件导入题目
- 需要：管理员Token + Authorization Header
- 文件格式：.csv
- 文件大小限制：10MB

#### 查看所有提交记录
- 接口：`GET /api/admin/submit/list`
- 说明：查看所有用户的提交记录
- 需要：管理员Token + Authorization Header

#### 系统统计数据
- 接口：`GET /api/admin/statistics`
- 说明：获取系统整体统计数据
- 需要：管理员Token + Authorization Header

## 配置说明

### 数据库配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/programming_system
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### Docker配置

```yaml
programming:
  docker:
    host: tcp://localhost:2375
    timeout: 2000
    memory: 134217728
```

### AI配置

```yaml
programming:
  ai:
    api-key: your_api_key
    api-url: https://dashscope.aliyuncs.com/compatible-mode/v1
    prompt: 你是专业的编程助教，只回答编程相关问题，给出清晰的思路和示例代码，不要直接给出完整答案，帮助用户理解原理。
```

### 文件上传配置

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB
```

## 测试说明

### 测试工具

- Postman
- Apifox
- curl

### 测试流程

1. 启动项目
```bash
mvn spring-boot:run
```

2. 注册用户
```bash
curl -X POST http://localhost:8080/api/user/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456"}'
```

3. 登录获取Token
```bash
TOKEN=$(curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456"}' \
  | jq -r '.data.token')
```

4. 运行代码
```bash
curl -X POST http://localhost:8080/api/code/run \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"code":"print(1+2)","language":"python","input":""}'
```

5. 提交题目
```bash
curl -X POST http://localhost:8080/api/submit/commit \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"problemId":1,"code":"print(1+2)","language":"python"}'
```

6. AI提问
```bash
curl -X POST http://localhost:8080/api/ai/ask \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"content":"Python循环为什么死循环？","code":"for i in range(10):print(i)"}'
```

### 测试文档

详细的测试说明请参考：
- [TEST.md](TEST.md) - 完整的接口测试文档
- [api-doc.md](api-doc.md) - API接口文档

## 常见问题

### 编译错误

**问题**：项目编译失败

**解决方案**：
```bash
mvn clean compile
```

### 数据库连接失败

**问题**：无法连接到MySQL数据库

**解决方案**：
1. 检查MySQL服务是否启动
2. 检查数据库配置是否正确
3. 检查数据库用户名和密码
4. 检查数据库是否存在

### Docker连接失败

**问题**：无法连接到Docker

**解决方案**：
1. 检查Docker服务是否启动
2. 检查Docker API端口是否正确
3. 检查Docker API是否启用
4. 检查防火墙设置

### AI API调用失败

**问题**：AI问答失败

**解决方案**：
1. 检查API Key是否正确
2. 检查网络连接
3. 检查API URL是否正确
4. 检查API额度是否充足

### 文件上传失败

**问题**：文件上传失败

**解决方案**：
1. 检查文件大小是否超过10MB
2. 检查文件格式是否正确
3. 检查文件内容是否符合要求

### 代码运行超时

**问题**：代码运行超时

**解决方案**：
1. 检查代码是否有死循环
2. 增加超时时间配置
3. 优化算法复杂度

### 判题结果说明

| 结果码 | 含义 | 说明 |
|:----:|:----:|:----:|
| 0 | AC | 答案正确 |
| 1 | WA | 答案错误 |
| 2 | RE | 编译错误 |
| 3 | RTE | 运行时错误 |
| 4 | TLE | 运行失败 |

### 日志级别说明

| 日志级别 | 用途 | 示例 |
|:----:|:----:|:----:|
| log.error() | 错误信息 | 编译错误、运行时错误、运行失败 |
| log.warn() | 警告信息 | 答案错误 |
| log.info() | 正常信息 | 判题通过、代码运行成功 |

## 贡献指南

欢迎贡献代码、文档、Bug报告！

### 贡献流程

1. Fork本项目
2. 创建特性分支
3. 提交代码
4. 创建Pull Request

### 代码规范

- 遵循现有代码风格
- 添加必要的注释
- 编写单元测试
- 更新相关文档

### 提交规范

- 清晰描述问题或功能
- 提供复现步骤
- 附上相关日志

## 许可证

本项目采用MIT许可证。

## 联系方式

如有问题或建议，欢迎通过以下方式联系：

- 提交Issue
- 发送邮件
- 查看文档

## 致谢

感谢所有贡献者的支持！

---

**相关文档**：
- [API接口文档](api-doc.md)
- [测试文档](TEST.md)
- [导入模板文档](IMPORT_TEMPLATE.md)