# API导入错误修复计划

## 问题描述
在重构后的页面中,使用了错误的API函数导入名称,导致以下错误:
- `AI.vue`: 导入了不存在的 `sendMessage`,实际应该是 `askAI`
- `Learn.vue`: 导入了不存在的 `getLearnStats` 和 `getRecentSubmissions`,实际应该是 `getMyLearnStats` 和需要新增的API
- `Submissions.vue`: 导入了不存在的 `getSubmissionList`,实际应该是 `getMySubmissions`

## 当前API文件状态

### ai.js
- ✅ `askAI(data)` - 发送AI问题
- ✅ `getAIHistory()` - 获取AI历史

### learn.js
- ✅ `getMyLearnStats()` - 获取学习统计
- ✅ `getRecommendations()` - 获取推荐题目

### submit.js
- ✅ `submitCode(data)` - 提交代码
- ✅ `getMySubmissions(params)` - 获取我的提交
- ✅ `getSubmitDetail(submitId)` - 获取提交详情
- ✅ `runSingleTestCase(data)` - 运行单个测试用例
- ✅ `runWithCustomInput(data)` - 自定义输入运行

## 修复方案

### 1. 修复 AI.vue
**文件**: `d:\Desktop\毕业设计\Code\frontend\src\views\AI.vue`

**修改内容**:
- 将 `import { sendMessage as sendMessageApi } from '@/api/ai'` 改为 `import { askAI } from '@/api/ai'`
- 将函数调用从 `sendMessageApi(...)` 改为 `askAI(...)`

### 2. 修复 Learn.vue
**文件**: `d:\Desktop\毕业设计\Code\frontend\src\views\Learn.vue`

**修改内容**:
- 将 `import { getLearnStats, getRecentSubmissions } from '@/api/learn'` 改为 `import { getMyLearnStats, getRecommendations } from '@/api/learn'`
- 将 `fetchStats()` 中的API调用从 `getLearnStats()` 改为 `getMyLearnStats()`
- 将 `fetchRecentSubmissions()` 中的API调用从 `getRecentSubmissions()` 改为 `getRecommendations()`
- 调整数据结构以匹配API返回格式

### 3. 修复 Submissions.vue
**文件**: `d:\Desktop\毕业设计\Code\frontend\src\views\Submissions.vue`

**修改内容**:
- 将 `import { getSubmissionList } from '@/api/submit'` 改为 `import { getMySubmissions } from '@/api/submit'`
- 将 `fetchSubmissions()` 中的API调用从 `getSubmissionList(params)` 改为 `getMySubmissions(params)`

## 实施步骤

1. 修复 AI.vue 的API导入和调用
2. 修复 Learn.vue 的API导入和调用
3. 修复 Submissions.vue 的API导入和调用
4. 验证所有页面能正常加载

## 预期结果
- AI页面能正常加载和发送消息
- 学习记录页面能正常显示统计数据
- 提交记录页面能正常显示提交列表
- 所有页面不再报错
