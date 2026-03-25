import request from '@/utils/request'

export function getUserProfile(userId) {
  return request(`/api/user/profile/${userId}`)
}

export function updateProfile(data) {
  return request('/api/user/profile', {
    method: 'PUT',
    data
  })
}

export function getStudyStats(userId) {
  return request(`/api/user/profile/${userId}/stats`)
}

export function getStudyActivities(userId, page = 0, size = 20) {
  return request(`/api/user/profile/${userId}/activities`, {
    params: { page, size }
  })
}

export function getUserAchievements(userId) {
  return request(`/api/user/profile/${userId}/achievements`)
}

export function addFavorite(data) {
  return request('/api/user/profile/favorite', {
    method: 'POST',
    data
  })
}

export function removeFavorite(data) {
  return request('/api/user/profile/favorite', {
    method: 'DELETE',
    data
  })
}

export function getFavorites(userId, targetType, page = 0, size = 10) {
  return request(`/api/user/profile/${userId}/favorites`, {
    params: { targetType, page, size }
  })
}

export function getSolvedRanking(period = 'week', page = 0, size = 50) {
  return request('/api/ranking/solved', {
    params: { period, page, size }
  })
}

export function getPassRateRanking(page = 0, size = 50) {
  return request('/api/ranking/pass-rate', {
    params: { page, size }
  })
}

export function getStudyHoursRanking(period = 'week', page = 0, size = 50) {
  return request('/api/ranking/study-hours', {
    params: { period, page, size }
  })
}

export function getMyRank() {
  return request('/api/ranking/my-rank')
}

export function changePassword(data) {
  return request('/api/user/password', {
    method: 'PUT',
    data
  })
}
