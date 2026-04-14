import request from '@/utils/request'

export const getRuntimeLanguages = () => {
  return request('/api/runtime/languages')
}
