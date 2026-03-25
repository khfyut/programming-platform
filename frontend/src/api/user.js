import request from '@/utils/request'

export const login = (data) => {
  return request('/api/user/login', {
    method: 'POST',
    data: data
  })
}

export const register = (data) => {
  return request('/api/user/register', {
    method: 'POST',
    data: data
  })
}

export const getUserInfo = () => {
  return request('/api/user/info')
}

export const updateUserInfo = (data) => {
  return request('/api/user/update', {
    method: 'POST',
    data: data
  })
}

export const changePassword = (data) => {
  return request('/api/user/change-password', {
    method: 'POST',
    data: data
  })
}

export const updateLanguage = (language) => {
  return request('/api/user/update/language', {
    method: 'POST',
    data: { language }
  })
}
