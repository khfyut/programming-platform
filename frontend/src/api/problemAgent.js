import request from '@/utils/request'

export const chatProblemAgent = (data) => {
  return request('/api/ai/problem-agent/chat', {
    method: 'POST',
    data
  })
}

export const getProblemAgentSession = (sessionId) => {
  return request(`/api/ai/problem-agent/session/${sessionId}`)
}

export const getLatestProblemAgentSession = (problemId) => {
  return request(`/api/ai/problem-agent/problem/${problemId}/latest`)
}

export const sendAgentFeedback = (data) => {
  return request('/api/agent/feedback', {
    method: 'POST',
    data
  })
}

export const reflectWrongBookItem = (wrongItemId) => {
  return request(`/api/agent/wrong-book/${wrongItemId}/reflect`, {
    method: 'POST'
  })
}

export const recommendLearningPathLevel = (pathId, levelId) => {
  return request(`/api/agent/learning-path/${pathId}/level/${levelId}/recommend`, {
    method: 'POST'
  })
}

export const getAgentReportSummary = () => {
  return request('/api/agent/report/summary')
}

export const getAgentCoachState = () => {
  return request('/api/agent/coach/state')
}

export const decideAgentCoach = (data) => {
  return request('/api/agent/coach/decide', {
    method: 'POST',
    data
  })
}

export const chatAgentCoach = (data) => {
  return request('/api/agent/coach/chat', {
    method: 'POST',
    data
  })
}

export const recordAgentCoachEvent = (data) => {
  return request('/api/agent/coach/event', {
    method: 'POST',
    data
  })
}
