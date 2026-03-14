# 前后端接口核对 - 实施计划

## [x] Task 1: 核对用户模块接口
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 核对用户注册接口 POST /user/register
  - 核对用户登录接口 POST /user/login
  - 核对获取用户信息接口 GET /user/info
  - 核对修改编程语言接口 POST /user/update/language
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `programmatic` TR-1.1: 确认所有4个用户接口路径和方法匹配
- **Notes**: 用户模块接口全部匹配 ✅

## [x] Task 2: 核对在线代码运行模块接口
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 核对在线运行代码接口 POST /code/run
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `programmatic` TR-2.1: 确认代码运行接口路径和方法匹配
- **Notes**: 代码运行模块接口匹配 ✅

## [x] Task 3: 核对题库模块接口
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 核对分页获取题目列表接口 GET /problem/list
  - 核对获取题目详情接口 GET /problem/detail/{id}
  - 核对按标签查询题目接口 GET /problem/tag/{tag}
  - 核对按难度查询题目接口 GET /problem/difficulty/{difficulty}
  - 核对获取样例测试用例接口 GET /problem/{id}/test-cases/sample
  - 核对获取所有测试用例接口 GET /problem/{id}/test-cases/all
- **Acceptance Criteria Addressed**: AC-2
- **Test Requirements**:
  - `programmatic` TR-3.1: 识别缺失的4个题库接口
  - `programmatic` TR-3.2: 识别路径不匹配的测试用例接口
- **Notes**: 题库模块有4个接口缺失，1个接口路径不匹配 ❌⚠️

## [x] Task 4: 核对提交判题模块接口
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 核对提交代码判题接口 POST /submit/commit
  - 核对获取我的提交记录接口 GET /submit/my
  - 识别前端存在但后端未定义的接口
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `programmatic` TR-4.1: 确认2个核心接口匹配
  - `programmatic` TR-4.2: 识别3个额外接口
- **Notes**: 核心接口匹配，但有3个额外接口 ⚠️

## [x] Task 5: 核对 AI 答疑模块接口
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 核对 AI 提问接口 POST /ai/ask
  - 核对获取问答历史接口 GET /ai/history
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `programmatic` TR-5.1: 确认2个 AI 接口匹配
- **Notes**: AI 模块接口全部匹配 ✅

## [x] Task 6: 核对学习记录模块接口
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 核对获取学习统计接口 GET /learn/my
  - 核对个性化推荐接口 GET /learn/recommend
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `programmatic` TR-6.1: 确认2个学习记录接口匹配
- **Notes**: 学习记录模块接口全部匹配 ✅

## [x] Task 7: 核对管理员模块接口
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 核对用户列表接口 GET /admin/user/list
  - 核对新增题目接口 POST /admin/problem/add
  - 核对新增题目（带验重）接口 POST /admin/problem/add-with-check
  - 核对修改题目接口 POST /admin/problem/update
  - 核对删除题目接口 POST /admin/problem/delete/{id}
  - 核对批量导入接口 POST /admin/problem/batch-import
  - 核对 Excel 导入接口 POST /admin/problem/import-excel
  - 核对 CSV 导入接口 POST /admin/problem/import-csv
  - 核对查看所有提交记录接口 GET /admin/submit/list
  - 核对系统统计数据接口 GET /admin/statistics
- **Acceptance Criteria Addressed**: AC-3
- **Test Requirements**:
  - `programmatic` TR-7.1: 确认5个管理员接口匹配
  - `programmatic` TR-7.2: 识别缺失的4个管理员接口
- **Notes**: 管理员模块有4个接口缺失 ❌

## [x] Task 8: 生成核对汇总报告
- **Priority**: P0
- **Depends On**: Task 1, Task 2, Task 3, Task 4, Task 5, Task 6, Task 7
- **Description**: 
  - 汇总所有模块的接口核对结果
  - 列出缺失接口、额外接口、路径不匹配接口
- **Acceptance Criteria Addressed**: AC-4
- **Test Requirements**:
  - `programmatic` TR-8.1: 生成完整的接口核对报告
- **Notes**: 汇总报告已生成 ✅
