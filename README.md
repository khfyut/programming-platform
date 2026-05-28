# 在线编程学习与 AI 辅导平台

一个前后端分离的在线编程学习平台，支持代码在线运行、自动判题和 AI 学习辅导。

**在线体验 / 截图：**（部署后补充链接）

## 功能概览

- **代码沙箱** — Docker 容器池隔离运行 Java / Python 代码，限制内存与超时，支持 AC / WA / RE / RTE / TLE 五种判题结果
- **AI 辅导** — 接入大模型 API，Prompt 分级教学策略，AI 按"引导思路 → 提示 → 诊断 → 讲解"递进辅导，而不是直接给答案
- **题库与判题** — 支持题目 CRUD、批量导入（Excel / CSV / JSON）、自动验重，提交代码后自动判题并返回详细反馈
- **学习追踪** — 学习路径推荐、错题本复盘、学习统计分析
- **管理后台** — 用户管理、题目管理、系统统计

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 17, SpringBoot 3.2, MyBatis, MySQL, Docker Java API |
| 前端 | Vue 3, Vite, ElementPlus, Monaco Editor |
| AI | 大模型 API（DeepSeek / 通义千问 / Ollama），Python AgentService |
| 测试 | JMeter 压测, pytest, 76 项接口测试用例 |

## 项目结构

```
frontend/          Vue 3 + Vite 前端
System/            SpringBoot + MyBatis 后端
AgentService/      Python AI Agent 决策服务
db/                数据库建表、补丁、种子数据
tests/             接口测试脚本与报告
loadtest/          JMeter 压测配置与结果
docs/              架构设计、API 文档、测试报告
scripts/           可复用脚本
```

## 快速启动

### 环境要求

- JDK 17+, Maven 3.6+
- Node.js 18+, npm
- MySQL 8.0+, Docker
- Python 3.10+（AI 服务）

### 后端

```bash
cd System
# 修改 src/main/resources/application.yml 中的数据库连接信息
mvn spring-boot:run
```

### 前端

```bash
cd frontend
npm install
npm run dev
```

### AI 服务（可选）

```bash
cd AgentService
pip install -r requirements.txt
cp .env.example .env   # 编辑 .env 配置 LLM API Key
python app/main.py
```

## 测试与压测

```bash
# 接口测试
cd tests
python api_test_runner.py

# JMeter 压测（需要 JMeter 5.x）
# 压测配置在 loadtest/jmeter/ 目录下
# 10 并发 × 10 轮，核心 API 平均响应 7.2ms，P95 18ms
```

## 文档

- [后端接口文档](./System/api-doc.md)
- [后端项目说明](./System/README.md)
- [前端项目说明](./frontend/README.md)
- [Learning Agent 架构设计](./docs/learning-agent-architecture.md)
- [测试报告](./tests/test_results_report.md)

## License

MIT
