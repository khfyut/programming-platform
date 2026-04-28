import request from '@/utils/request'

export const getProblemList = (params) => {
  const query = new URLSearchParams(params).toString()
  return request(`/api/problem/list?${query}`)
}

export const getProblemDetail = (id) => {
  return request(`/api/problem/detail/${id}`)
}

export const getProblemsByTag = (tag) => {
  return request(`/api/problem/tag/${tag}`)
}

export const getSampleTestCases = (id) => {
  return request(`/api/problem/${id}/test-cases/sample`)
}

export const getProblemSupportedLanguages = (id) => {
  return request(`/api/problem/${id}/supported-languages`)
}

export const getAllTestCases = (id) => {
  return request(`/api/problem/${id}/test-cases/all`)
}

export const getLanguages = () => {
  return request('/api/problem/languages')
}

export const getProblemCategories = () => {
  return request('/api/problem/categories')
}
