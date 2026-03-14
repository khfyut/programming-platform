# 编程平台项目

一个类似于LeetCode的在线编程平台，包含后端Java服务和前端Vue界面，支持代码提交、测试用例执行、AI辅助编程等功能。

## 项目结构

```
├── System/           # 后端Java项目
│   ├── src/          # 源代码
│   ├── target/       # 构建输出
│   ├── tools/        # 工具脚本
│   └── pom.xml       # Maven配置
├── frontend/         # 前端Vue项目
│   ├── src/          # 源代码
│   ├── package.json  # NPM配置
│   └── vite.config.js # Vite配置
├── skills/           # 技能模块
└── README.md         # 项目说明
```

## 功能特性

### 后端功能
- 用户认证与授权
- 题目管理
- 代码提交与执行
- 测试用例管理
- 学习记录统计
- AI辅助编程
- 管理员功能

### 前端功能
- 响应式界面设计
- 代码编辑器（Monaco Editor）
- 测试用例执行
- 提交记录管理
- 学习统计分析
- 深色/浅色主题切换
- AI辅助功能

## 技术栈

### 后端
- Java 17
- Spring Boot 3.2
- MyBatis
- MySQL
- Docker（代码执行沙箱）

### 前端
- Vue 3
- Element Plus
- Monaco Editor
- Vite

## 快速开始

### 后端启动
1. 确保MySQL数据库已启动
2. 执行`init_sample_data.sql`初始化数据库
3. 运行`mvn clean package`构建项目
4. 运行`java -jar target/programming-system-1.0.0.jar`启动服务

### 前端启动
1. 执行`npm install`安装依赖
2. 执行`npm run dev`启动开发服务器
3. 访问`http://localhost:3002`

## 项目配置

### 数据库配置
修改`System/src/main/resources/application.yml`中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/programming_platform
    username: root
    password: password
```

### 前端API配置
修改`frontend/src/utils/request.js`中的API基础URL：

```javascript
const service = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000
})
```

## 分支管理策略

- `master`：主分支，保持稳定可部署状态
- `develop`：开发分支，集成新功能
- `feature/*`：功能分支，开发特定功能
- `bugfix/*`：修复分支，修复bug
- `release/*`：发布分支，准备发布

## 贡献指南

1. Fork本仓库
2. 创建特性分支（`git checkout -b feature/amazing-feature`）
3. 提交更改（`git commit -m 'Add some amazing feature'`）
4. 推送到分支（`git push origin feature/amazing-feature`）
5. 打开Pull Request

## 许可证

MIT License

## 联系方式

如有问题或建议，请通过以下方式联系：
- Gitee：https://gitee.com/feng-hong-ran-qiu/programming-platform

## 项目状态

🚀 开发中
