# 仓库文档说明

当前仓库已经按“正式资产 / 过程资产 / 测试资产”重新整理，建议后续统一遵守下面这套结构。

## 目录职责

- `frontend/`
  - 前端正式代码
- `System/`
  - 后端正式代码
- `db/`
  - 正式数据库补丁、种子数据、历史 SQL 归档
- `scripts/`
  - 可复用脚本
- `scripts/archive/`
  - 一次性或历史脚本归档
- `tests/manual/`
  - 手工联调或一次性验证脚本
- `loadtest/jmeter/`
  - 压测脚本与 JMeter 配置
- `loadtest/results/`
  - 压测摘要结果
- `docs/design/`
  - 设计方案、结构规划、流程图
- `docs/reports/`
  - 阶段报告、测试报告、汇报材料
- `docs/testing/`
  - 测试截图和测试说明
- `docs/thesis/`
  - 论文相关文档
- `docs/archive/`
  - 历史文档、遗留方案、系统级过程材料

## 后续新增文件的规则

- 新增正式 SQL：
  - 放到 `db/seed/` 或后续新增的 `db/migration/`
- 一次性修复 SQL：
  - 先放 `db/seed/`，确认长期有效后保留；否则放 `db/archive/legacy/`
- 测试脚本：
  - 放 `tests/manual/` 或后续新增的 `tests/e2e/`
- 截图、回归证据：
  - 放 `docs/testing/screenshots/`
- 设计讨论稿：
  - 放 `docs/design/`
- 论文材料：
  - 放 `docs/thesis/`
- 不要再往仓库根目录新增临时脚本、临时 SQL、临时 Markdown

## 一个完整项目建议保留的核心文件

- `README.md`
- `.gitignore`
- `.env.example`
- `frontend/`
- `System/`
- `db/`
- `scripts/`
- `tests/`
- `loadtest/`
- `docs/`

## 已整理掉的典型噪音

- 根目录空脚本和空报告
- 浏览器自动生成的 `chrome-profile`
- 本地 Maven 缓存 `.m2/`
- 本地工具计划目录 `.trae/`
- 大量原始压测 HTML/JTL 产物

如果后续还要继续收缩仓库，优先看 `docs/archive/`、`db/archive/`、`tests/manual/` 这三个目录。
