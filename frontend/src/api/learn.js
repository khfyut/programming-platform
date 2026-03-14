import request from '@/utils/request'

export const getMyLearnStats = () => {
  return request('/api/learn/my')
}

export const getRecommendations = () => {
  return request('/api/learn/recommend')
}
