import request from '@/utils/request'

export const askAI = (data) => {
  return request('/api/ai/ask', {
    method: 'POST',
    data: data
  })
}

export const getAIHistory = () => {
  return request('/api/ai/history')
}
