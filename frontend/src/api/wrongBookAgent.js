import request from '@/utils/request'

export const getLatestWrongBookAgentSession = (wrongItemId) => {
  return request(`/api/ai/wrong-book-agent/wrong-item/${wrongItemId}/latest`)
}

export const reflectWrongBookReview = (wrongItemId) => {
  return request(`/api/ai/wrong-book-agent/wrong-item/${wrongItemId}/reflect`, {
    method: 'POST'
  })
}

export const chatWrongBookAgent = (data) => {
  return request('/api/ai/wrong-book-agent/chat', {
    method: 'POST',
    data
  })
}

export const getWrongBookAgentSession = (sessionId) => {
  return request(`/api/ai/wrong-book-agent/session/${sessionId}`)
}
