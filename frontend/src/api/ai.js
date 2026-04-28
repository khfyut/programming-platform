import request from '@/utils/request'
import { streamSsePost } from '@/utils/sseStream'

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

export const streamAIChat = (data, handlers = {}) => {
  return streamSsePost('/api/ai/chat/stream', data, handlers)
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
