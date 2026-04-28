import assert from 'node:assert/strict'
import test from 'node:test'
import {
  createGlobalCoachReply,
  createGlobalCoachUnavailableReply,
  getGlobalCoachGuide,
  normalizeGlobalCoachChatResponse,
  shouldShowGlobalLearningCoach,
  shouldShowGlobalLearningCoachNudge
} from './globalLearningCoach.js'

test('global learning coach only appears on learning task routes', () => {
  assert.equal(shouldShowGlobalLearningCoach({ name: 'Learn' }, 'token'), true)
  assert.equal(shouldShowGlobalLearningCoach({ name: 'Problems' }, 'token'), true)
  assert.equal(shouldShowGlobalLearningCoach({ name: 'ProblemDetail' }, 'token'), true)
  assert.equal(shouldShowGlobalLearningCoach({ name: 'LearningPath' }, 'token'), true)
  assert.equal(shouldShowGlobalLearningCoach({ name: 'LevelDetail' }, 'token'), true)
  assert.equal(shouldShowGlobalLearningCoach({ name: 'WrongBook' }, 'token'), true)
  assert.equal(shouldShowGlobalLearningCoach({ name: 'WrongBookDetail' }, 'token'), true)
  assert.equal(shouldShowGlobalLearningCoach({ name: 'ProfileAnalysis' }, 'token'), true)

  assert.equal(shouldShowGlobalLearningCoach({ name: 'AI' }, 'token'), false)
  assert.equal(shouldShowGlobalLearningCoach({ name: 'UserProfile' }, 'token'), false)
  assert.equal(shouldShowGlobalLearningCoach({ name: 'Admin' }, 'token'), false)
  assert.equal(shouldShowGlobalLearningCoach({ name: 'ProblemDetail' }, ''), false)
})

test('global learning coach returns page guide context for learning routes', () => {
  const problemGuide = getGlobalCoachGuide({ name: 'ProblemDetail', path: '/problem/56' })
  assert.equal(problemGuide.scene, 'problem_coach')
  assert.equal(problemGuide.pageTitle, '\u9898\u76ee\u9875')
  assert.equal(problemGuide.primaryAction.type, 'focus_problem_coach')

  const wrongBookGuide = getGlobalCoachGuide({ name: 'WrongBookDetail', path: '/wrong-book/7' })
  assert.equal(wrongBookGuide.scene, 'wrong_book')
  assert.equal(wrongBookGuide.primaryAction.type, 'focus_wrong_book_review')

  const aiGuide = getGlobalCoachGuide({ name: 'AI', path: '/ai' })
  assert.equal(aiGuide, null)
})

test('global learning coach creates local guide replies with inline actions', () => {
  const systemReply = createGlobalCoachReply('\u8fd9\u4e2a\u7cfb\u7edf\u600e\u4e48\u7528', {
    name: 'Learn',
    path: '/learn'
  })
  assert.match(systemReply.text, /\u5b66\u4e60\u8def\u5f84/)
  assert.deepEqual(systemReply.actions.map((action) => action.label), [
    '\u5b66\u4e60\u4e2d\u5fc3',
    'AI \u966a\u7ec3'
  ])

  const pageReply = createGlobalCoachReply('\u5f53\u524d\u9875\u9762\u600e\u4e48\u7528', {
    name: 'ProblemDetail',
    path: '/problem/56'
  })
  assert.match(pageReply.text, /\u9898\u76ee/)
  assert.equal(pageReply.actions[0].type, 'focus_problem_coach')
})

test('ai coach guide intent is not swallowed by generic usage questions', () => {
  const reply = createGlobalCoachReply('AI \u966a\u7ec3\u600e\u4e48\u7528', {
    name: 'Learn',
    path: '/learn'
  })

  assert.match(reply.text, /AI \u966a\u7ec3\u4e0d\u662f/)
  assert.equal(reply.actions[0].type, 'focus_problem_coach')
})

test('problem coach follow-up is delegated instead of duplicated in the floating window', () => {
  const reply = createGlobalCoachReply('\u7ed9\u4e00\u70b9\u601d\u8def', {
    name: 'ProblemDetail',
    path: '/problem/56'
  })

  assert.match(reply.text, /\u9898\u76ee\u966a\u7ec3/)
  assert.equal(reply.actions[0].type, 'focus_problem_coach')
})

test('normalizes backend global coach chat response for inline action rendering', () => {
  const reply = normalizeGlobalCoachChatResponse({
    sessionId: 's1',
    reply: '我建议先去 学习中心，然后了解 AI 陪练。',
    scene: 'global_guide',
    source: 'AGENT_SERVICE',
    fallback: false,
    actions: [
      { type: 'route', label: '学习中心', route: '/learn' },
      { type: 'explain_ai_coach', label: 'AI 陪练' }
    ]
  })

  assert.equal(reply.sessionId, 's1')
  assert.equal(reply.text, '我建议先去 学习中心，然后了解 AI 陪练。')
  assert.equal(reply.source, 'AGENT_SERVICE')
  assert.equal(reply.fallback, false)
  assert.deepEqual(reply.actions.map((action) => action.label), ['学习中心', 'AI 陪练'])
})

test('global coach unavailable reply is transparent about missing llm', () => {
  const reply = createGlobalCoachUnavailableReply({
    name: 'WrongBookDetail',
    path: '/wrong-book/8'
  })

  assert.match(reply.text, /\u5927\u6a21\u578b\u670d\u52a1/)
  assert.match(reply.text, /\u4e0d\u4f1a\u628a\u672c\u5730\u6a21\u677f\u5f53\u6210 AI \u56de\u7b54/)
  assert.equal(reply.actions[0].type, 'focus_wrong_book_review')
})

test('coach nudge card is more restricted than the floating entry', () => {
  const nudge = { id: 1 }

  assert.equal(shouldShowGlobalLearningCoachNudge({ name: 'ProblemDetail', path: '/problem/56' }, nudge, false), true)
  assert.equal(shouldShowGlobalLearningCoachNudge({ name: 'LearningPath', path: '/learn/path/1' }, nudge, false), true)
  assert.equal(shouldShowGlobalLearningCoachNudge({ name: 'LevelDetail', path: '/learn/path/1/level/2' }, nudge, false), true)

  assert.equal(shouldShowGlobalLearningCoachNudge({ name: 'WrongBookDetail', path: '/wrong-book/7' }, nudge, false), false)
  assert.equal(shouldShowGlobalLearningCoachNudge({ name: 'WrongBook' }, nudge, false), false)
  assert.equal(shouldShowGlobalLearningCoachNudge({ name: 'AI' }, nudge, false), false)
  assert.equal(shouldShowGlobalLearningCoachNudge({ name: 'ProblemDetail' }, null, false), false)
  assert.equal(shouldShowGlobalLearningCoachNudge({ name: 'ProblemDetail' }, nudge, true), false)
})

test('coach nudge card only appears on its target route', () => {
  const wrongBookNudge = { id: 1, targetRoute: '/wrong-book/7?coach=reflection' }
  const problemNudge = { id: 2, targetRoute: '/problem/56?tab=coach' }

  assert.equal(
    shouldShowGlobalLearningCoachNudge({ name: 'ProblemDetail', path: '/problem/56' }, wrongBookNudge, false),
    false
  )
  assert.equal(
    shouldShowGlobalLearningCoachNudge({ name: 'ProblemDetail', path: '/problem/56' }, problemNudge, false),
    true
  )
  assert.equal(
    shouldShowGlobalLearningCoachNudge({ name: 'LearningPath', path: '/learn/path/1/level/2' }, {
      id: 3,
      targetRoute: '/learn/path/1'
    }, false),
    true
  )
})
