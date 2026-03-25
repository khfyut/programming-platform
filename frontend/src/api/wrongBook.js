import request from '@/utils/request'

export const getWrongBookList = (params) => {
  const query = params ? new URLSearchParams(params).toString() : ''
  return request(`/api/wrong-book/list${query ? '?' + query : ''}`)
}

export const getWrongBookDetail = (id) => {
  return request(`/api/wrong-book/detail/${id}`)
}

export const reviewWrongBook = (data) => {
  return request('/api/wrong-book/review', {
    method: 'POST',
    data: data
  })
}

export const getReviewPlan = () => {
  return request('/api/wrong-book/review/plan')
}

export const getPendingReviews = () => {
  return request('/api/wrong-book/review/pending')
}

export const updateReviewPlan = (data) => {
  return request('/api/wrong-book/review/update', {
    method: 'POST',
    data: data
  })
}

export const getRecommendedProblems = (params) => {
  const query = params ? new URLSearchParams(params).toString() : ''
  return request(`/api/wrong-book/recommend${query ? '?' + query : ''}`)
}

export const getWrongBookStatistics = () => {
  return request('/api/wrong-book/statistics')
}

export const getKnowledgeDistribution = () => {
  return request('/api/wrong-book/distribution/knowledge')
}

export const getDifficultyDistribution = () => {
  return request('/api/wrong-book/distribution/difficulty')
}

export const deleteWrongBookItem = (id) => {
  return request(`/api/wrong-book/delete/${id}`, {
    method: 'POST'
  })
}
