# 测试用例清理与生成计划

## 任务分解与优先级

### [x] 任务1: 分析当前数据库状态
- **优先级**: P0
- **Depends On**: None
- **Description**:
  - 分析problem表中的重复题目
  - 分析test_case表中的现有测试用例
  - 确定重复题目的判断标准
- **Success Criteria**:
  - 识别出所有重复题目
  - 了解当前测试用例的分布情况
- **Test Requirements**:
  - `programmatic` TR-1.1: 查询并列出所有重复题目
  - `programmatic` TR-1.2: 统计现有测试用例数量

### [x] 任务2: 清空测试用例表
- **优先级**: P1
- **Depends On**: 任务1
- **Description**:
  - 执行SQL语句清空test_case表
  - 验证表已清空
- **Success Criteria**:
  - test_case表中的所有数据被删除
- **Test Requirements**:
  - `programmatic` TR-2.1: 执行DELETE语句
  - `programmatic` TR-2.2: 验证表计数为0

### [x] 任务3: 删除重复题目
- **优先级**: P1
- **Depends On**: 任务1
- **Description**:
  - 基于分析结果，删除problem表中的重复题目
  - 保留每个重复组中的一个题目
- **Success Criteria**:
  - 所有重复题目被删除
  - 只保留唯一的题目
- **Test Requirements**:
  - `programmatic` TR-3.1: 执行DELETE语句删除重复题目
  - `programmatic` TR-3.2: 验证problem表中无重复题目

### [x] 任务4: 为所有题目生成测试用例
- **优先级**: P2
- **Depends On**: 任务2, 任务3
- **Description**:
  - 为problem表中的每个题目生成对应的测试用例
  - 确保测试用例覆盖正常情况和边界情况
  - 生成SQL文件用于插入测试用例
- **Success Criteria**:
  - 每个题目都有对应的测试用例
  - 测试用例覆盖各种输入情况
- **Test Requirements**:
  - `programmatic` TR-4.1: 生成包含所有题目测试用例的SQL文件
  - `human-judgement` TR-4.2: 检查测试用例的合理性和覆盖率

### [x] 任务5: 导入测试用例并验证
- **优先级**: P2
- **Depends On**: 任务4
- **Description**:
  - 执行生成的SQL文件导入测试用例
  - 验证所有测试用例已正确导入
  - 验证每个题目都有对应的测试用例
- **Success Criteria**:
  - 所有测试用例成功导入
  - 每个题目都有至少一个测试用例
- **Test Requirements**:
  - `programmatic` TR-5.1: 执行SQL文件导入测试用例
  - `programmatic` TR-5.2: 验证每个题目都有对应的测试用例

## 执行步骤

1. 首先执行任务1，分析当前数据库状态
2. 执行任务2，清空测试用例表
3. 执行任务3，删除重复题目
4. 执行任务4，为所有题目生成测试用例
5. 执行任务5，导入测试用例并验证

## 注意事项

- 在删除重复题目时，需要谨慎选择保留哪个版本
- 生成测试用例时，要确保覆盖各种输入情况
- 执行SQL操作前，建议先备份数据库
- 验证步骤要确保数据的一致性和完整性