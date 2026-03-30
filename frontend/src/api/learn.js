import request from '@/utils/request'

export const getMyLearnStats = () => {
  return request('/api/learn/my')
}

export const getRecommendations = () => {
  return request('/api/learn/recommend')
}

export const getAssessment = (params) => {
  const query = params ? new URLSearchParams(params).toString() : ''
  return request(`/api/learn/assessment${query ? `?${query}` : ''}`)
}

export const commitAssessment = (data) => {
  return request('/api/learn/assessment/commit', {
    method: 'POST',
    data
  })
}

export const getLearningPath = (pathId) => {
  const query = pathId ? `?pathId=${pathId}` : ''
  return request(`/api/learn/path${query}`)
}

export const getPathProgress = (pathId) => {
  const query = pathId ? `?pathId=${pathId}` : ''
  return request(`/api/learn/path/progress${query}`)
}

export const getChapterDetail = (chapterId) => {
  return request(`/api/learn/path/chapter/${chapterId}`)
}

export const unlockLevel = (levelId) => {
  return request('/api/learn/path/level/unlock', {
    method: 'POST',
    data: { levelId }
  })
}

export const completeLevel = (levelId) => {
  return request('/api/learn/path/level/complete', {
    method: 'POST',
    data: { levelId }
  })
}

export const getAvailablePaths = (params) => {
  const query = params ? new URLSearchParams(params).toString() : ''
  return request(`/api/learn/paths${query ? `?${query}` : ''}`)
}

export const getPathDetail = (pathId) => {
  return request(`/api/learn/path/detail/${pathId}`)
}

export const getKnowledgeGraph = () => {
  return request('/api/learn/knowledge/graph')
}

export const getKnowledgeMastery = () => {
  return request('/api/learn/knowledge/mastery')
}

export const getWeeklyReport = () => {
  return request('/api/learn/report/weekly')
}

export const getMonthlyReport = (params) => {
  const query = params ? new URLSearchParams(params).toString() : ''
  return request(`/api/learn/report/monthly${query ? `?${query}` : ''}`)
}

export const getWeakKnowledgePoints = () => {
  return request('/api/learn/weakness')
}

export const getKnowledgeDistribution = () => {
  return request('/api/learn/knowledge/distribution')
}

export const getErrorFrequency = (params) => {
  const query = params ? new URLSearchParams(params).toString() : ''
  return request(`/api/learn/errors/frequency${query ? `?${query}` : ''}`)
}

export const getLevelProblems = (levelId) => {
  return request(`/api/learn/level/${levelId}/problems`)
}

export const bindProblemToLevel = (levelId, problemId, orderNum) => {
  const query = orderNum ? `?orderNum=${orderNum}` : ''
  return request(`/api/learn/level/${levelId}/problem/${problemId}${query}`, {
    method: 'POST'
  })
}

export const unbindProblemFromLevel = (levelId, problemId) => {
  return request(`/api/learn/level/${levelId}/problem/${problemId}`, {
    method: 'DELETE'
  })
}

export const batchBindProblemsToLevel = (levelId, problemIds) => {
  return request(`/api/learn/level/${levelId}/problems/batch`, {
    method: 'POST',
    data: { problemIds }
  })
}

export const updateLevelProblems = (levelId, problemIds) => {
  return request(`/api/learn/level/${levelId}/problems`, {
    method: 'PUT',
    data: { problemIds }
  })
}

export const getLevelResources = (levelId) => {
  const timestamp = Date.now()
  return request(`/api/learn/level/${levelId}/resources?timestamp=${timestamp}`)
}

export const getActivePaths = (userId) => {
  const query = userId ? `?userId=${userId}` : ''
  return request(`/api/learn/paths/active${query}`)
}

export const getDifficultyStats = (userId) => {
  const query = userId ? `?userId=${userId}` : ''
  return request(`/api/learn/difficulty/stats${query}`)
}
