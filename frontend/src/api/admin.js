import request from '@/utils/request'

export const getUserList = (params) => {
  const query = params ? new URLSearchParams(params).toString() : ''
  return request(`/api/admin/user/list${query ? '?' + query : ''}`)
}

export const addProblem = (data) => {
  return request('/api/admin/problem/add', {
    method: 'POST',
    data: data
  })
}

export const addProblemWithCheck = (data) => {
  return request('/api/admin/problem/add-with-check', {
    method: 'POST',
    data: data
  })
}

export const updateProblem = (data) => {
  return request('/api/admin/problem/update', {
    method: 'POST',
    data: data
  })
}

export const deleteProblem = (id) => {
  return request(`/api/admin/problem/delete/${id}`, {
    method: 'POST'
  })
}

export const batchImportProblems = (data) => {
  return request('/api/admin/problem/batch-import', {
    method: 'POST',
    data: data
  })
}

export const importFromExcel = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request('/api/admin/problem/import-excel', {
    method: 'POST',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export const importFromCsv = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request('/api/admin/problem/import-csv', {
    method: 'POST',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export const getAllSubmissions = (params) => {
  const query = params ? new URLSearchParams(params).toString() : ''
  return request(`/api/admin/submit/list${query ? '?' + query : ''}`)
}

export const getStatistics = () => {
  return request('/api/admin/statistics')
}
