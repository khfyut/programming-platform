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

export const updateLanguage = (language) => {
  return request('/api/user/update/language', {
    method: 'POST',
    data: { language }
  })
}
