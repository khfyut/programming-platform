import request from '@/utils/request'

export function getReferenceSolution(problemId, language) {
  return request(`/api/reference-solution/${problemId}`, {
    params: { language }
  })
}

export function getHint(problemId, hintLevel = 1, language) {
  return request(`/api/reference-solution/${problemId}/hint`, {
    params: { hintLevel, language }
  })
}

export function getAvailableLanguages(problemId) {
  return request(`/api/reference-solution/${problemId}/languages`)
}
