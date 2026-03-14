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

export const getProblemsByDifficulty = (difficulty) => {
  return request(`/api/problem/difficulty/${difficulty}`)
}

export const getSampleTestCases = (id) => {
  return request(`/api/problem/${id}/test-cases/sample`)
}

export const getAllTestCases = (id) => {
  return request(`/api/problem/${id}/test-cases/all`)
}
