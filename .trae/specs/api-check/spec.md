# 前后端接口核对 - 产品需求文档

## Overview
- **Summary**: 核对前端 API 调用与后端接口文档的对应状况，识别缺失接口、路径不匹配和额外接口
- **Purpose**: 确保前后端接口一致性，为后续功能开发和接口修复提供依据
- **Target Users**: 开发团队、项目维护人员

## Goals
- 完整核对用户模块、代码运行模块、题库模块、提交判题模块、AI 答疑模块、学习记录模块和管理员模块的所有接口
- 识别前端缺失的后端已定义接口
- 识别前端存在但后端未定义的额外接口
- 识别路径不匹配的接口
- 生成接口修复计划

## Non-Goals (Out of Scope)
- 不实现任何新功能
- 不修复接口调用问题（仅核对）
- 不修改后端接口定义
- 不涉及接口内部逻辑验证

## Background & Context
- 后端接口文档已完整定义在 `d:\Desktop\毕业设计\Code\System\api-doc.md`
- 前端 API 文件位于 `d:\Desktop\毕业设计\Code\frontend\src\api\` 目录下
- 项目采用 Vue.js 前端 + Spring Boot 后端架构

## Functional Requirements
- **FR-1**: 核对用户模块所有接口
- **FR-2**: 核对在线代码运行模块所有接口
- **FR-3**: 核对题库模块所有接口
- **FR-4**: 核对提交判题模块所有接口
- **FR-5**: 核对 AI 答疑模块所有接口
- **FR-6**: 核对学习记录模块所有接口
- **FR-7**: 核对管理员模块所有接口

## Non-Functional Requirements
- **NFR-1**: 核对结果必须清晰、准确、完整
- **NFR-2**: 必须以结构化方式呈现核对结果
- **NFR-3**: 必须标识每个接口的状态（✅ 正常、❌ 缺失、⚠️ 不匹配）

## Constraints
- **Technical**: 仅基于现有代码和文档进行核对
- **Business**: 不进行任何代码修改
- **Dependencies**: 依赖后端接口文档和前端 API 代码

## Assumptions
- 后端接口文档是最新且准确的
- 前端 API 文件代表当前所有接口调用
- 接口路径和方法是判断是否匹配的主要标准

## Acceptance Criteria

### AC-1: 用户模块接口核对完整
- **Given**: 用户模块后端接口文档已存在，前端 user.js 文件已存在
- **When**: 执行用户模块接口核对
- **Then**: 所有后端定义的用户模块接口都被核对，结果清晰呈现
- **Verification**: `programmatic`

### AC-2: 题库模块接口核对完整
- **Given**: 题库模块后端接口文档已存在，前端 problem.js 文件已存在
- **When**: 执行题库模块接口核对
- **Then**: 所有后端定义的题库模块接口都被核对，结果清晰呈现
- **Verification**: `programmatic`

### AC-3: 管理员模块接口核对完整
- **Given**: 管理员模块后端接口文档已存在，前端 admin.js 文件已存在
- **When**: 执行管理员模块接口核对
- **Then**: 所有后端定义的管理员模块接口都被核对，结果清晰呈现
- **Verification**: `programmatic`

### AC-4: 所有模块核对结果汇总
- **Given**: 所有模块接口核对已完成
- **When**: 生成核对报告
- **Then**: 汇总结果包含：缺失接口列表、额外接口列表、路径不匹配接口列表
- **Verification**: `programmatic`

## Open Questions
- 无
