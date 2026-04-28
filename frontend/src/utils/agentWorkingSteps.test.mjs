import assert from 'node:assert/strict'
import test from 'node:test'
import { getAgentWorkingSteps } from './agentWorkingSteps.js'

test('problem coach run failure steps explain visible work', () => {
  const steps = getAgentWorkingSteps({
    scene: 'problem_coach',
    triggerType: 'run_failed'
  })

  assert.ok(steps.length >= 4)
  assert.ok(steps.some((step) => step.includes('\u610f\u56fe')))
  assert.ok(steps.some((step) => step.includes('题目上下文')))
  assert.ok(steps.some((step) => step.includes('运行结果')))
  assert.ok(steps.some((step) => step.includes('生成')))
})

test('wrong book task submit steps mention reflection state', () => {
  const steps = getAgentWorkingSteps({
    scene: 'wrong_book',
    actionType: 'reflection_task_submit'
  })

  assert.ok(steps.length >= 4)
  assert.ok(steps.some((step) => step.includes('复盘任务')))
  assert.ok(steps.some((step) => step.includes('学习状态')))
})

test('unknown agent scene still has generic working steps', () => {
  const steps = getAgentWorkingSteps({})

  assert.ok(steps.length >= 3)
  assert.equal(steps[0], '正在接收你的问题')
})
