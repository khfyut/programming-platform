import request from '@/utils/request'

export function getReferenceSolution(problemId, language) {
  return request(`/api/reference-solution/${problemId}`, {
    params: { language }
  })
}

export function getHint(problemId, hintLevel = 1) {
  return request(`/api/reference-solution/${problemId}/hint`, {
    params: { hintLevel }
  })
}

export function getAvailableLanguages(problemId) {
  return request(`/api/reference-solution/${problemId}/languages`)
}
