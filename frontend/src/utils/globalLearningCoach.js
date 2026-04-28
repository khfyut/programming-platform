const coachRouteNames = new Set([
  'Learn',
  'Problems',
  'ProblemDetail',
  'LearningPath',
  'LevelDetail',
  'WrongBook',
  'WrongBookDetail',
  'ProfileAnalysis'
])

const nudgeCardRouteNames = new Set([
  'ProblemDetail',
  'LearningPath',
  'LevelDetail'
])

const pathOnly = (value) => {
  return String(value || '').split('?')[0].split('#')[0]
}

const routeAction = (label, route) => ({
  type: 'route',
  label,
  route
})

const focusAction = (label, type) => ({
  type,
  label
})

const GUIDE_BY_ROUTE = {
  Learn: {
    scene: 'learning_path',
    pageTitle: '\u5b66\u4e60\u4e2d\u5fc3',
    shortTitle: '\u5b66\u4e60\u4e2d\u5fc3',
    pageHelp: '\u8fd9\u91cc\u7528\u6765\u9009\u62e9\u5b66\u4e60\u8def\u5f84\u548c\u67e5\u770b\u5f53\u524d\u8fdb\u5ea6\u3002\u4f60\u53ef\u4ee5\u5148\u9009\u4e00\u6761\u8def\u5f84\uff0c\u518d\u6309\u5173\u5361\u7ec3\u4e60\u9898\u76ee\u3002',
    primaryAction: routeAction('\u5b66\u4e60\u8def\u5f84', '/learn'),
    secondaryAction: routeAction('\u9898\u5e93', '/problems')
  },
  Problems: {
    scene: 'problem_list',
    pageTitle: '\u9898\u5e93',
    shortTitle: '\u9898\u5e93',
    pageHelp: '\u8fd9\u91cc\u7528\u6765\u6309\u96be\u5ea6\u3001\u77e5\u8bc6\u70b9\u548c\u72b6\u6001\u627e\u9898\u3002\u5982\u679c\u4e0d\u77e5\u9053\u4ece\u54ea\u9898\u5f00\u59cb\uff0c\u5148\u9009\u4e00\u9053\u548c\u5f53\u524d\u5b66\u4e60\u8def\u5f84\u76f8\u5173\u7684\u9898\u3002',
    primaryAction: routeAction('\u5b66\u4e60\u4e2d\u5fc3', '/learn'),
    secondaryAction: routeAction('\u9519\u9898\u672c', '/wrong-book')
  },
  ProblemDetail: {
    scene: 'problem_coach',
    pageTitle: '\u9898\u76ee\u9875',
    shortTitle: '\u9898\u76ee\u966a\u7ec3',
    pageHelp: '\u8fd9\u91cc\u662f\u505a\u9898\u9875\u3002\u5148\u8bfb\u9898\u610f\u548c\u6837\u4f8b\uff0c\u518d\u8fd0\u884c\u4ee3\u7801\u6216\u63d0\u4ea4\u3002\u5982\u679c\u5361\u4f4f\uff0c\u6211\u53ef\u4ee5\u628a\u95ee\u9898\u4ea4\u7ed9\u9898\u76ee\u966a\u7ec3\uff0c\u5b83\u4f1a\u6309\u63d0\u793a\u3001\u8bca\u65ad\u548c\u4ee3\u7801\u68c0\u67e5\u6765\u5e2e\u4f60\u3002',
    primaryAction: focusAction('\u9898\u76ee\u966a\u7ec3', 'focus_problem_coach'),
    secondaryAction: routeAction('\u9519\u9898\u672c', '/wrong-book')
  },
  LearningPath: {
    scene: 'learning_path',
    pageTitle: '\u5b66\u4e60\u8def\u5f84',
    shortTitle: '\u5b66\u4e60\u8def\u5f84',
    pageHelp: '\u8fd9\u91cc\u5c55\u793a\u4e00\u6761\u5b66\u4e60\u8def\u5f84\u7684\u77e5\u8bc6\u7ed3\u6784\u3002\u5efa\u8bae\u6309\u5173\u5361\u987a\u5e8f\u63a8\u8fdb\uff0c\u5b8c\u6210\u4e00\u4e2a\u9636\u6bb5\u540e\u518d\u8fdb\u5165\u4e0b\u4e00\u4e2a\u5173\u5361\u3002',
    primaryAction: routeAction('\u9898\u5e93', '/problems'),
    secondaryAction: routeAction('\u9519\u9898\u672c', '/wrong-book')
  },
  LevelDetail: {
    scene: 'learning_path',
    pageTitle: '\u5173\u5361\u8be6\u60c5',
    shortTitle: '\u5173\u5361\u5b66\u4e60',
    pageHelp: '\u8fd9\u91cc\u662f\u67d0\u4e2a\u5173\u5361\u7684\u7ec3\u4e60\u5165\u53e3\u3002\u5148\u770b\u672c\u5173\u77e5\u8bc6\u70b9\uff0c\u518d\u5b8c\u6210\u63a8\u8350\u9898\u76ee\u3002',
    primaryAction: routeAction('\u9898\u5e93', '/problems'),
    secondaryAction: routeAction('\u5b66\u4e60\u4e2d\u5fc3', '/learn')
  },
  WrongBook: {
    scene: 'wrong_book',
    pageTitle: '\u9519\u9898\u672c',
    shortTitle: '\u9519\u9898\u590d\u76d8',
    pageHelp: '\u8fd9\u91cc\u6536\u96c6\u4f60\u505a\u9519\u6216\u63d0\u4ea4\u5931\u8d25\u7684\u9898\u3002\u4f60\u53ef\u4ee5\u4ece\u6700\u8fd1\u7684\u9519\u9898\u5f00\u59cb\uff0c\u5148\u590d\u76d8\u9519\u56e0\uff0c\u518d\u505a\u76f8\u4f3c\u9898\u3002',
    primaryAction: routeAction('\u9519\u9898\u672c', '/wrong-book'),
    secondaryAction: routeAction('\u9898\u5e93', '/problems')
  },
  WrongBookDetail: {
    scene: 'wrong_book',
    pageTitle: '\u9519\u9898\u8be6\u60c5',
    shortTitle: '\u9519\u9898\u590d\u76d8',
    pageHelp: '\u8fd9\u91cc\u7528\u6765\u590d\u76d8\u67d0\u9053\u9519\u9898\u3002\u5efa\u8bae\u5148\u8bf4\u6e05\u695a\u5f53\u65f6\u4e3a\u4ec0\u4e48\u9519\uff0c\u518d\u627e\u4e00\u4e2a\u6700\u5c0f\u53cd\u4f8b\u9a8c\u8bc1\u4fee\u6b63\u601d\u8def\u3002',
    primaryAction: focusAction('\u9519\u9898\u590d\u76d8', 'focus_wrong_book_review'),
    secondaryAction: routeAction('\u9898\u5e93', '/problems')
  },
  ProfileAnalysis: {
    scene: 'learning_report',
    pageTitle: '\u5b66\u4e60\u5206\u6790',
    shortTitle: '\u5b66\u4e60\u5206\u6790',
    pageHelp: '\u8fd9\u91cc\u7528\u6765\u770b\u4f60\u7684\u7ec3\u4e60\u8d8b\u52bf\u548c\u8584\u5f31\u70b9\u3002\u5982\u679c\u60f3\u884c\u52a8\uff0c\u53ef\u4ee5\u56de\u5230\u5b66\u4e60\u4e2d\u5fc3\u6216\u9519\u9898\u672c\u9009\u4e00\u4e2a\u5177\u4f53\u4efb\u52a1\u3002',
    primaryAction: routeAction('\u5b66\u4e60\u4e2d\u5fc3', '/learn'),
    secondaryAction: routeAction('\u9519\u9898\u672c', '/wrong-book')
  }
}

const SYSTEM_REPLY = {
  text: '\u8fd9\u4e2a\u7cfb\u7edf\u7684\u4e3b\u7ebf\u662f\uff1a\u5148\u5728\u5b66\u4e60\u8def\u5f84\u91cc\u9009\u62e9\u9636\u6bb5\uff0c\u518d\u8fdb\u5165\u9898\u76ee\u7ec3\u4e60\uff0c\u9047\u5230\u4e0d\u4f1a\u6216\u63d0\u4ea4\u5931\u8d25\u65f6\u7528 AI \u966a\u7ec3\u83b7\u5f97\u63d0\u793a\uff0c\u6700\u540e\u5230\u9519\u9898\u672c\u505a\u590d\u76d8\u3002\u6211\u5efa\u8bae\u4f60\u5148\u53bb \u5b66\u4e60\u4e2d\u5fc3 \u770b\u4e00\u6761\u8def\u5f84\uff1b\u5982\u679c\u60f3\u5148\u4e86\u89e3\u6211\u600e\u4e48\u5e2e\u4f60\uff0c\u4e5f\u53ef\u4ee5\u95ee\u6211 AI \u966a\u7ec3\u3002',
  actions: [
    routeAction('\u5b66\u4e60\u4e2d\u5fc3', '/learn'),
    focusAction('AI \u966a\u7ec3', 'explain_ai_coach')
  ]
}

const AI_COACH_REPLY = {
  text: 'AI \u966a\u7ec3\u4e0d\u662f\u76f4\u63a5\u4ee3\u5199\u7b54\u6848\uff0c\u800c\u662f\u6839\u636e\u4f60\u7684\u9875\u9762\u548c\u5b66\u4e60\u72b6\u6001\u7ed9\u63d0\u793a\u3001\u8bca\u65ad\u9519\u56e0\u6216\u5f15\u5bfc\u590d\u76d8\u3002\u5728\u9898\u76ee\u9875\u6211\u53ef\u4ee5\u628a\u95ee\u9898\u4ea4\u7ed9 \u9898\u76ee\u966a\u7ec3\uff1b\u5728\u9519\u9898\u9875\u53ef\u4ee5\u5f15\u5bfc\u4f60\u505a \u9519\u9898\u590d\u76d8\u3002',
  actions: [
    focusAction('\u9898\u76ee\u966a\u7ec3', 'focus_problem_coach'),
    focusAction('\u9519\u9898\u590d\u76d8', 'focus_wrong_book_review')
  ]
}

const normalizeMessage = (value) => String(value || '').trim().toLowerCase()

const includesAny = (value, words) => words.some((word) => value.includes(word))

const isNudgeTargetingRoute = (route, activeNudge) => {
  const targetPath = pathOnly(activeNudge?.targetRoute)
  if (!targetPath) {
    return true
  }
  const currentPath = pathOnly(route?.path || route?.fullPath)
  if (!currentPath) {
    return false
  }
  return (
    targetPath === currentPath ||
    targetPath.startsWith(`${currentPath}/`) ||
    currentPath.startsWith(`${targetPath}/`)
  )
}

export const shouldShowGlobalLearningCoach = (route, token) => {
  return Boolean(token) && coachRouteNames.has(route?.name)
}

export const getGlobalCoachGuide = (route = {}) => {
  return GUIDE_BY_ROUTE[route?.name] || null
}

export const createGlobalCoachReply = (message, route = {}) => {
  const guide = getGlobalCoachGuide(route)
  const normalized = normalizeMessage(message)

  if (includesAny(normalized, ['\u5f53\u524d\u9875', '\u9875\u9762', '\u8fd9\u91cc', 'page'])) {
    if (!guide) {
      return SYSTEM_REPLY
    }
    return {
      text: `${guide.pageHelp}\u5982\u679c\u4f60\u60f3\u7ee7\u7eed\uff0c\u6211\u5efa\u8bae\u5148\u770b ${guide.primaryAction.label}\u3002`,
      actions: [guide.primaryAction, guide.secondaryAction].filter(Boolean)
    }
  }

  if (includesAny(normalized, ['ai \u966a\u7ec3', '\u966a\u7ec3', 'agent', '\u80fd\u5e2e\u6211\u4ec0\u4e48'])) {
    return AI_COACH_REPLY
  }

  if (includesAny(normalized, ['\u7cfb\u7edf', '\u600e\u4e48\u7528', '\u600e\u4e48\u5f00\u59cb', '\u7b2c\u4e00\u6b21', '\u5f00\u59cb\u7ec3\u4e60', '\u5f00\u59cb\u505a\u9898', 'start', 'begin'])) {
    return SYSTEM_REPLY
  }

  if (guide?.scene === 'problem_coach' && includesAny(normalized, ['\u601d\u8def', '\u63d0\u793a', '\u6837\u4f8b', '\u4ee3\u7801', '\u4e3a\u4ec0\u4e48\u9519', '\u9519\u4e86'])) {
    return {
      text: '\u8fd9\u7c7b\u95ee\u9898\u9700\u8981\u7ed3\u5408\u5f53\u524d\u9898\u76ee\u3001\u4ee3\u7801\u548c\u8fd0\u884c\u7ed3\u679c\u3002\u6211\u5efa\u8bae\u4ea4\u7ed9 \u9898\u76ee\u966a\u7ec3 \u5904\u7406\uff0c\u5b83\u4f1a\u6309\u63d0\u793a\u6216\u8bca\u65ad\u7684\u7c92\u5ea6\u5e2e\u4f60\uff0c\u4e0d\u4f1a\u76f4\u63a5\u8df3\u5230\u5b8c\u6574\u7b54\u6848\u3002',
      actions: [guide.primaryAction]
    }
  }

  if (guide?.scene === 'wrong_book' && includesAny(normalized, ['\u590d\u76d8', '\u9519\u9898', '\u53cd\u4f8b', '\u9519\u56e0'])) {
    return {
      text: '\u8fd9\u7c7b\u95ee\u9898\u66f4\u9002\u5408\u5728 \u9519\u9898\u590d\u76d8 \u91cc\u505a\uff0c\u56e0\u4e3a\u5b83\u4f1a\u5e26\u4e0a\u539f\u63d0\u4ea4\u3001\u9519\u8bef\u4fe1\u606f\u548c\u590d\u76d8\u4efb\u52a1\u3002',
      actions: [guide.primaryAction]
    }
  }

  if (guide) {
    return {
      text: `${guide.pageHelp}\u5982\u679c\u4f60\u4e0d\u786e\u5b9a\u4e0b\u4e00\u6b65\uff0c\u6211\u5efa\u8bae\u5148\u8bd5\u8bd5 ${guide.primaryAction.label}\u3002`,
      actions: [guide.primaryAction, guide.secondaryAction].filter(Boolean)
    }
  }

  return SYSTEM_REPLY
}

export const createGlobalCoachUnavailableReply = (route = {}) => {
  const guide = getGlobalCoachGuide(route)
  const actions = guide?.primaryAction
    ? [guide.primaryAction]
    : [routeAction('\u5b66\u4e60\u4e2d\u5fc3', '/learn')]
  const actionLabel = actions[0]?.label || '\u5b66\u4e60\u4e2d\u5fc3'
  return {
    text: `\u8fd9\u6b21\u6ca1\u6709\u8fde\u4e0a\u5927\u6a21\u578b\u670d\u52a1\uff0c\u6240\u4ee5\u6211\u4e0d\u4f1a\u628a\u672c\u5730\u6a21\u677f\u5f53\u6210 AI \u56de\u7b54\u7ed9\u4f60\u3002\u8bf7\u7a0d\u540e\u91cd\u8bd5\uff0c\u6216\u5148\u786e\u8ba4 Java \u540e\u7aef\u548c Python AgentService \u90fd\u5728\u8fd0\u884c\u3002\u5982\u679c\u8981\u7ee7\u7eed\u5f53\u524d\u4efb\u52a1\uff0c\u53ef\u4ee5\u5148\u6253\u5f00 ${actionLabel}\u3002`,
    actions
  }
}

export const normalizeGlobalCoachChatResponse = (payload = {}) => {
  const actions = Array.isArray(payload.actions)
    ? payload.actions
        .map((action) => ({
          type: action.type || action.actionType || '',
          label: action.label || action.title || '',
          route: action.route || action.targetRoute || '',
          eventName: action.eventName || action.event_name || '',
          prompt: action.prompt || ''
        }))
        .filter((action) => action.type && action.label)
    : []

  return {
    sessionId: payload.sessionId || payload.session_id || '',
    text: payload.reply || payload.text || payload.content || '',
    actions,
    scene: payload.scene || 'global_guide',
    source: payload.source || '',
    fallback: Boolean(payload.fallback)
  }
}

export const shouldShowGlobalLearningCoachNudge = (route, activeNudge, expanded) => {
  return (
    Boolean(activeNudge) &&
    !expanded &&
    nudgeCardRouteNames.has(route?.name) &&
    isNudgeTargetingRoute(route, activeNudge)
  )
}
