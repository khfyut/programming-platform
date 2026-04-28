const genericSteps = [
  '正在接收你的问题',
  '正在整理当前上下文',
  '正在生成可执行的下一步建议'
]

const problemBaseSteps = [
  '正在读取题目上下文',
  '正在检查你的代码和提问',
  '正在判断该给提示、诊断还是解释',
  '正在生成陪练建议'
]

const problemRunFailureSteps = [
  '正在读取题目上下文',
  '正在检查运行结果和错误信息',
  '正在定位可能的失败输入',
  '正在生成下一步调试建议'
]

const problemSubmitFailureSteps = [
  '正在读取题目上下文',
  '正在检查最近一次提交记录',
  '正在对照错误信息和薄弱知识点',
  '正在生成针对性陪练建议'
]

const wrongBookBaseSteps = [
  '正在读取错题记录',
  '正在整理错误信息和代码片段',
  '正在更新学习状态',
  '正在生成复盘建议'
]

const wrongBookTaskSteps = [
  '正在保存复盘任务',
  '正在更新学习状态',
  '正在判断是否可以进入复习完成阶段',
  '正在生成下一步复盘反馈'
]

const intentUnderstandingSteps = [
  '\u6b63\u5728\u7406\u89e3\u4f60\u7684\u95ee\u9898\u610f\u56fe',
  '\u6b63\u5728\u786e\u8ba4\u662f\u5426\u9700\u8981\u6f84\u6e05'
]

const withIntentUnderstanding = (steps) => {
  if (steps.some((step) => step.includes('\u610f\u56fe'))) {
    return steps
  }
  return [steps[0], ...intentUnderstandingSteps, ...steps.slice(1)]
}

export const getAgentWorkingSteps = ({ scene, triggerType, actionType } = {}) => {
  if (scene === 'problem_coach') {
    if (triggerType === 'run_failed') {
      return withIntentUnderstanding(problemRunFailureSteps)
    }
    if (triggerType === 'submit_failed') {
      return withIntentUnderstanding(problemSubmitFailureSteps)
    }
    return withIntentUnderstanding(problemBaseSteps)
  }

  if (scene === 'wrong_book') {
    if (actionType === 'reflection_task_submit') {
      return wrongBookTaskSteps
    }
    return wrongBookBaseSteps
  }

  return withIntentUnderstanding(genericSteps)
}
