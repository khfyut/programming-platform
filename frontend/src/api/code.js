import request from '@/utils/request'

export const runCode = (data) => {
  return request('/api/code/run', {
    method: 'POST',
    data: data
  })
}
