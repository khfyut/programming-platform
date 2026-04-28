import assert from 'node:assert/strict'
import test from 'node:test'

import {
  createReflectionTaskPayload,
  createWrongBookAgentActionPayload,
  getWrongBookReflectionRoute,
  normalizeWrongBookAgentActions,
  normalizeWrongBookAgentState,
  shouldFocusWrongBookReflection
} from './wrongBookAgent.js'

test('builds the wrong book reflection route', () => {
  assert.equal(getWrongBookReflectionRoute(88), '/wrong-book/88?coach=reflection')
  assert.equal(getWrongBookReflectionRoute('90'), '/wrong-book/90?coach=reflection')
})

test('detects reflection focus query', () => {
  assert.equal(shouldFocusWrongBookReflection({ coach: 'reflection' }), true)
  assert.equal(shouldFocusWrongBookReflection({ coach: 'other' }), false)
  assert.equal(shouldFocusWrongBookReflection({}), false)
})

test('normalizes wrong book agent state from session snapshot', () => {
  const state = normalizeWrongBookAgentState({
    contextSnapshot: {
      currentJudgement: {
        errorType: '逻辑错误',
        stage: '思路正确，细节错误',
        mainIssue: '边界条件'
      },
      learningState: {
        attemptCount: 2,
        hintLevel: 3,
        errorPattern: 'BOUNDARY',
        currentStage: 'REVIEW_READY',
        tasks: {
          summary: { submitted: true, answer: '边界没处理' }
        }
      }
    }
  })

  assert.equal(state.currentJudgement.errorType, '逻辑错误')
  assert.equal(state.learningState.attemptCount, 2)
  assert.equal(state.learningState.hintLevel, 3)
  assert.equal(state.learningState.currentStage, 'REVIEW_READY')
  assert.equal(state.learningState.tasks.summary.submitted, true)
  assert.equal(state.learningState.tasks.counterexample.submitted, false)
})

test('falls back to wrong item fields when snapshot is missing', () => {
  const state = normalizeWrongBookAgentState(
    { contextSnapshot: { weakPoints: ['边界处理'], nextSuggestion: '写一个最小反例' } },
    {
      errorMessage: 'Wrong answer',
      knowledgePoints: '数组,边界'
    }
  )

  assert.equal(state.currentJudgement.errorType, '逻辑错误')
  assert.equal(state.currentJudgement.stage, '思路正确，细节错误')
  assert.equal(state.currentJudgement.mainIssue, '数组,边界')
  assert.equal(state.learningState.hintLevel, 1)
  assert.equal(state.learningState.errorPattern, 'WRONG_ANSWER')
})

test('normalizes default coach actions and builds action payloads', () => {
  const actions = normalizeWrongBookAgentActions({})
  assert.deepEqual(
    actions.map((action) => action.type),
    ['reflection_task_start', 'minimal_counterexample', 'hint', 'similar_problem']
  )

  assert.deepEqual(
    createWrongBookAgentActionPayload({
      wrongItemId: 88,
      sessionId: 'session-1',
      actionType: 'hint',
      hintLevel: 4
    }),
    {
      wrongItemId: 88,
      sessionId: 'session-1',
      actionType: 'hint',
      hintLevel: 4,
      message: '提示我关键点'
    }
  )
})

test('builds reflection task submit payloads', () => {
  assert.deepEqual(
    createReflectionTaskPayload({
      wrongItemId: 88,
      sessionId: 'session-1',
      taskType: 'fix_idea',
      answer: '先判断再更新状态'
    }),
    {
      wrongItemId: 88,
      sessionId: 'session-1',
      actionType: 'reflection_task_submit',
      taskType: 'fix_idea',
      answer: '先判断再更新状态',
      message: '提交复盘任务：先判断再更新状态'
    }
  )
})
