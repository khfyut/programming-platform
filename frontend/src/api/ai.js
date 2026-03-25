import request from '@/utils/request'

export const askAI = (data) => {
  return request('/api/ai/ask', {
    method: 'POST',
    data: data
  })
}

export const chatAI = (data) => {
  return request('/api/ai/chat', {
    method: 'POST',
    data: data
  })
}

export const getAIHistory = (params) => {
  const query = params ? new URLSearchParams(params).toString() : ''
  return request(`/api/ai/history${query ? '?' + query : ''}`)
}

export const deleteAIHistory = (sessionId) => {
  return request(`/api/ai/history/${sessionId}`, {
    method: 'DELETE'
  })
}

export const getSessionMessages = (sessionId) => {
  return request(`/api/ai/session/${sessionId}`)
}
