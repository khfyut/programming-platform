# 编程学习平台

本项目是一个前后端分离的在线编程学习平台，包含题库、提交与评测、学习路径、知识图谱、社区、个人中心、管理后台等模块。

## 项目结构

- `System`：Spring Boot 后端
- `frontend`：Vue 3 + Vite 前端

## 运行端口

- 前端开发端口：`3000`
- 后端服务端口：`8080`

## 本地启动（宿主机）

### 1. 后端

进入 `System` 后启动 Spring Boot。后端配置使用环境变量，建议先复制一份环境变量模板：

```bash
cp .env.example .env
```

然后根据你的环境设置变量（数据库、Redis、Docker Host、AI Key）。

### 2. 前端

```bash
cd frontend
npm install
npm run dev
```

访问：`http://localhost:3000`

## 前端可用脚本

在 `frontend` 目录：

```bash
npm run dev
npm run build
npm run preview
npm run lint
```

## 关键配置项

后端配置文件：`System/src/main/resources/application.yml`

关键环境变量：

- `DB_HOST` / `DB_PORT` / `DB_NAME` / `DB_USER` / `DB_PASSWORD`
- `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` / `REDIS_DATABASE`
- `PROGRAMMING_JWT_SECRET` / `PROGRAMMING_JWT_EXPIRE`
- `PROGRAMMING_DOCKER_HOST` / `PROGRAMMING_DOCKER_TIMEOUT` / `PROGRAMMING_DOCKER_MEMORY`
- `PROGRAMMING_AI_API_KEY` / `PROGRAMMING_AI_API_URL` / `PROGRAMMING_AI_PROMPT`
- `SERVER_PORT`

## 说明

- 项目根目录的 `.env.example` 仅用于示例，不会自动生效。
- 建议在运行环境中显式注入环境变量，避免将真实密钥写入仓库。
