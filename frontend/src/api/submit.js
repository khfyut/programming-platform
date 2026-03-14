import request from '@/utils/request'

export const submitCode = (data) => {
  return request('/api/submit/commit', {
    method: 'POST',
    data: data
  })
}

export const getMySubmissions = (params) => {
  const query = new URLSearchParams(params).toString()
  return request(`/api/submit/my?${query}`)
}

export const getSubmitDetail = (submitId) => {
  return request(`/api/submit/detail/${submitId}`)
}
