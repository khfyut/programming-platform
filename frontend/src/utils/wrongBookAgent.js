export const getWrongBookReflectionRoute = (wrongItemId) => {
  const id = String(wrongItemId || '').trim()
  return id ? `/wrong-book/${encodeURIComponent(id)}?coach=reflection` : '/wrong-book'
}

export const shouldFocusWrongBookReflection = (query = {}) => {
  return query.coach === 'reflection'
}

const DEFAULT_TASKS = {
  summary: { submitted: false, answer: '' },
  counterexample: { submitted: false, answer: '' },
  fixIdea: { submitted: false, answer: '' }
}

export const REVIEW_TASKS = [
  {
    key: 'summary',
    taskType: 'summary',
    title: '用一句话说明这段代码为什么错',
    placeholder: '例如：边界条件没有覆盖长度为 1 的输入'
  },
  {
    key: 'counterexample',
    taskType: 'counterexample',
    title: '找一个最小失败用例',
    placeholder: '例如：nums = [1], target = 2'
  },
  {
    key: 'fixIdea',
    taskType: 'fix_idea',
    title: '修正你的思路，不用写代码',
    placeholder: '例如：先判断当前值，再更新状态'
  }
]

const DEFAULT_ACTIONS = [
  {
    type: 'reflection_task_start',
    label: '我先自己总结问题',
    prompt: '先完成当前复盘任务的第一项。'
  },
  {
    type: 'minimal_counterexample',
    label: '给我一个最小反例',
    prompt: '请给我一个最小失败用例。'
  },
  {
    type: 'hint',
    label: '提示我关键点',
    prompt: '请给我一个关键提示。'
  },
  {
    type: 'similar_problem',
    label: '再来一道类似题',
    prompt: '推荐一道类似题。'
  }
]

const ACTION_LABELS = {
  minimal_counterexample: '给我一个最小反例',
  hint: '提示我关键点',
  similar_problem: '再来一道类似题',
  reflection_task_submit: '提交复盘任务'
}

const toObject = (value) => {
  return value && typeof value === 'object' && !Array.isArray(value) ? value : {}
}

const toTask = (value) => {
  const task = toObject(value)
  return {
    submitted: Boolean(task.submitted),
    answer: String(task.answer || '')
  }
}

const inferErrorType = (wrongItem = {}) => {
  const message = String(wrongItem.errorMessage || '').toLowerCase()
  if (message.includes('compile') || message.includes('syntax')) return '编译错误'
  if (message.includes('runtime') || message.includes('exception')) return '运行错误'
  if (message.includes('time')) return '复杂度问题'
  return '逻辑错误'
}

const inferErrorPattern = (wrongItem = {}) => {
  const message = String(wrongItem.errorMessage || '').toLowerCase()
  if (message.includes('wrong answer')) return 'WRONG_ANSWER'
  if (message.includes('time')) return 'TLE'
  if (message.includes('runtime') || message.includes('exception')) return 'RUNTIME_ERROR'
  if (message.includes('compile') || message.includes('syntax')) return 'COMPILE_ERROR'
  return 'UNKNOWN'
}

const clampHintLevel = (value) => {
  const numberValue = Number(value || 1)
  if (!Number.isFinite(numberValue)) return 1
  return Math.max(1, Math.min(4, Math.round(numberValue)))
}

export const normalizeWrongBookAgentState = (session = {}, wrongItem = {}) => {
  const snapshot = toObject(session.contextSnapshot)
  const judgement = toObject(snapshot.currentJudgement)
  const learningState = toObject(snapshot.learningState)
  const tasks = toObject(learningState.tasks)
  const weakPoints = Array.isArray(snapshot.weakPoints) ? snapshot.weakPoints.filter(Boolean) : []

  return {
    currentJudgement: {
      errorType: judgement.errorType || inferErrorType(wrongItem),
      stage: judgement.stage || '思路正确，细节错误',
      mainIssue: judgement.mainIssue || wrongItem.knowledgePoints || weakPoints.join(' + ') || snapshot.nextSuggestion || '边界条件 + 状态更新顺序'
    },
    learningState: {
      attemptCount: Number(learningState.attemptCount || 0),
      hintLevel: clampHintLevel(learningState.hintLevel),
      errorPattern: learningState.errorPattern || inferErrorPattern(wrongItem),
      currentStage: learningState.currentStage || 'REFLECTING',
      tasks: {
        summary: toTask(tasks.summary || DEFAULT_TASKS.summary),
        counterexample: toTask(tasks.counterexample || DEFAULT_TASKS.counterexample),
        fixIdea: toTask(tasks.fixIdea || DEFAULT_TASKS.fixIdea)
      }
    },
    weakPoints,
    nextSuggestion: snapshot.nextSuggestion || ''
  }
}

export const normalizeWrongBookAgentActions = (session = {}) => {
  const actions = Array.isArray(session.actions) ? session.actions : []
  const normalized = actions
    .map((action) => ({
      type: action.type || action.actionType || '',
      label: action.label || action.title || '',
      prompt: action.prompt || ''
    }))
    .filter((action) => action.type && action.label)

  return normalized.length ? normalized : DEFAULT_ACTIONS
}

export const createWrongBookAgentActionPayload = ({
  wrongItemId,
  sessionId,
  actionType,
  hintLevel,
  message
}) => {
  const payload = {
    wrongItemId,
    sessionId,
    actionType,
    message: message || ACTION_LABELS[actionType] || '继续帮我复盘'
  }
  if (actionType === 'hint') {
    payload.hintLevel = clampHintLevel(hintLevel)
  }
  return payload
}

export const createReflectionTaskPayload = ({
  wrongItemId,
  sessionId,
  taskType,
  answer
}) => {
  const normalizedAnswer = String(answer || '').trim()
  return {
    wrongItemId,
    sessionId,
    actionType: 'reflection_task_submit',
    taskType,
    answer: normalizedAnswer,
    message: `提交复盘任务：${normalizedAnswer}`
  }
}
