# 个性化在线编程与学习平台

这是一个前后端分离的在线编程学习平台项目，当前仓库已经整理为“正式代码 + 数据补丁 + 测试资产 + 文档资产”四个主块，适合继续开发、联调和毕业设计交付。

## 核心目录

- `frontend/`
  - Vue 3 + Vite 前端
- `System/`
  - Spring Boot + MyBatis 后端
- `db/`
  - 数据库补丁、种子数据、历史 SQL 归档
- `scripts/`
  - 可复用脚本
- `tests/`
  - 手工联调脚本与后续测试入口
- `loadtest/`
  - JMeter 压测配置与结果摘要
- `docs/`
  - 设计、报告、测试截图、论文材料、历史归档

## 推荐阅读顺序

1. [docs/README.md](/D:/Desktop/毕业设计/Code/docs/README.md)
2. [System/README.md](/D:/Desktop/毕业设计/Code/System/README.md)
3. [frontend/README.md](/D:/Desktop/毕业设计/Code/frontend/README.md)

## 当前保留的正式数据库补丁

- [20260327_learning_experience_patch.sql](/D:/Desktop/毕业设计/Code/db/seed/20260327_learning_experience_patch.sql)
- [20260327_problem_detail_and_solution_patch.sql](/D:/Desktop/毕业设计/Code/db/seed/20260327_problem_detail_and_solution_patch.sql)
- [20260327_user_knowledge_mastery_backfill.sql](/D:/Desktop/毕业设计/Code/db/seed/20260327_user_knowledge_mastery_backfill.sql)

## 仓库约定

- 正式代码只放在 `frontend/` 和 `System/`
- 正式数据补丁只放在 `db/`
- 临时或过程材料不要再直接放仓库根目录
- 测试截图和压测摘要统一归档到 `docs/` 与 `loadtest/`

## 说明

仓库中仍可能存在少量历史文件需要后续继续收敛，但根目录已经只保留项目入口级内容，适合继续作为主开发目录使用。
