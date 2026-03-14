# 个性化在线编程与答疑系统 - 前端

基于 Vue3 + Monaco Editor + Element Plus 开发的在线编程学习平台前端。

## 技术栈

- **Vue 3** - 渐进式 JavaScript 框架
- **Vite** - 新一代前端构建工具
- **Vue Router** - Vue.js 官方路由
- **Pinia** - Vue 状态管理库
- **Element Plus** - 基于 Vue 3 的组件库
- **Monaco Editor** - 微软开发的代码编辑器
- **Axios** - HTTP 客户端

## 项目结构

```
frontend/
├── src/
│   ├── api/              # API 接口
│   │   ├── admin.js
│   │   ├── ai.js
│   │   ├── code.js
│   │   ├── learn.js
│   │   ├── problem.js
│   │   ├── submit.js
│   │   └── user.js
│   ├── components/       # 公共组件
│   │   └── MonacoEditor.vue
│   ├── layout/          # 布局组件
│   │   └── MainLayout.vue
│   ├── router/          # 路由配置
│   │   └── index.js
│   ├── stores/          # 状态管理
│   │   └── user.js
│   ├── utils/           # 工具函数
│   │   └── request.js
│   ├── views/           # 页面组件
│   │   ├── Admin.vue
│   │   ├── AI.vue
│   │   ├── CodeRun.vue
│   │   ├── Learn.vue
│   │   ├── Login.vue
│   │   ├── ProblemDetail.vue
│   │   ├── Problems.vue
│   │   ├── Register.vue
│   │   └── Submissions.vue
│   ├── App.vue
│   └── main.js
├── index.html
├── package.json
└── vite.config.js
```

## 功能模块

### 1. 用户认证
- 用户注册
- 用户登录
- 用户信息管理
- 语言偏好设置

### 2. 在线代码运行
- 集成 Monaco Editor 代码编辑器
- 支持 Java 和 Python 语言
- 实时代码运行
- 输入输出展示

### 3. 题库系统
- 题目列表展示
- 难度和语言筛选
- 题目详情查看
- 分页加载

### 4. 提交判题
- 代码提交判题
- 判题结果展示
- 提交记录查询
- 代码详情查看

### 5. AI 答疑
- 编程问题提问
- 代码片段分析
- 问答历史记录

### 6. 学习记录
- 学习数据统计
- 个性化题目推荐
- 学习进度追踪

### 7. 管理后台
- 系统数据统计
- 用户管理
- 题目管理（增删改查）
- 提交记录查看

## 快速开始

### 安装依赖

```bash
cd frontend
npm install
```

### 启动开发服务器

```bash
npm run dev
```

前端服务将在 `http://localhost:3000` 启动

### 构建生产版本

```bash
npm run build
```

### 预览生产版本

```bash
npm run preview
```

## 配置说明

### API 代理配置

在 `vite.config.js` 中配置了 API 代理：

```javascript
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

确保后端服务运行在 `http://localhost:8080`

### 后端接口文档

详见后端项目的 `api-doc.md` 文件

## 界面预览

- 登录/注册页面：简洁美观的渐变背景设计
- 主界面：左侧导航栏 + 右侧内容区
- 题库页面：表格展示 + 筛选功能
- 代码编辑器：深色主题 Monaco Editor
- AI 答疑：聊天式交互界面
- 学习统计：卡片式数据展示
- 管理后台：数据可视化 + 表格管理

## 注意事项

1. 确保后端服务已启动
2. 首次使用需要注册账号
3. 管理员功能需要管理员权限
4. Monaco Editor 首次加载可能较慢

## 开发建议

1. 使用 Vue DevTools 进行调试
2. 遵循 Vue 3 Composition API 最佳实践
3. 保持组件职责单一
4. 合理使用 Pinia 进行状态管理
5. 注意 API 错误处理

## 浏览器支持

- Chrome (推荐)
- Firefox
- Edge
- Safari
