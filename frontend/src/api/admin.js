import request from '@/utils/request'

export const getUserList = (params) => {
  const query = params ? new URLSearchParams(params).toString() : ''
  return request(`/api/admin/user/list${query ? '?' + query : ''}`)
}

export const updateUserStatus = (data) => {
  return request('/api/admin/user/status', {
    method: 'POST',
    data: data
  })
}

export const updateUserRole = (data) => {
  return request('/api/admin/user/role', {
    method: 'POST',
    data: data
  })
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
    data: formData
  })
}

export const importFromCsv = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request('/api/admin/problem/import-csv', {
    method: 'POST',
    data: formData
  })
}

export const getAllSubmissions = (params) => {
  const query = params ? new URLSearchParams(params).toString() : ''
  return request(`/api/admin/submit/list${query ? '?' + query : ''}`)
}

export const getStatistics = () => {
  return request('/api/admin/statistics')
}

export const getRoleList = () => {
  return request('/api/admin/role/list')
}

export const getRoleDetail = (id) => {
  return request(`/api/admin/role/${id}`)
}

export const addRole = (data) => {
  return request('/api/admin/role/add', {
    method: 'POST',
    data: data
  })
}

export const updateRole = (data) => {
  return request('/api/admin/role/update', {
    method: 'POST',
    data: data
  })
}

export const deleteRole = (id) => {
  return request(`/api/admin/role/delete/${id}`, {
    method: 'POST'
  })
}

export const getPermissionList = () => {
  return request('/api/admin/permission/list')
}

export const assignRolePermissions = (data) => {
  return request('/api/admin/role/permission', {
    method: 'POST',
    data: data
  })
}

export const getClassList = () => {
  return request('/api/admin/class/list')
}

export const addClass = (data) => {
  return request('/api/admin/class/add', {
    method: 'POST',
    data: data
  })
}

export const updateClass = (data) => {
  return request('/api/admin/class/update', {
    method: 'POST',
    data: data
  })
}

export const deleteClass = (id) => {
  return request(`/api/admin/class/delete/${id}`, {
    method: 'POST'
  })
}

export const getClassUsers = (classId) => {
  return request(`/api/admin/class/${classId}/users`)
}

export const addUserToClass = (data) => {
  return request('/api/admin/class/user/add', {
    method: 'POST',
    data: data
  })
}

export const removeUserFromClass = (data) => {
  return request('/api/admin/class/user/remove', {
    method: 'POST',
    data: data
  })
}

export const getKnowledgeList = () => {
  return request('/api/admin/knowledge/list')
}

export const addKnowledge = (data) => {
  return request('/api/admin/knowledge/add', {
    method: 'POST',
    data: data
  })
}

export const updateKnowledge = (data) => {
  return request('/api/admin/knowledge/update', {
    method: 'POST',
    data: data
  })
}

export const deleteKnowledge = (id) => {
  return request(`/api/admin/knowledge/delete/${id}`, {
    method: 'POST'
  })
}

export const getPathList = () => {
  return request('/api/admin/path/list')
}

export const addPath = (data) => {
  return request('/api/admin/path/add', {
    method: 'POST',
    data: data
  })
}

export const updatePath = (data) => {
  return request('/api/admin/path/update', {
    method: 'POST',
    data: data
  })
}

export const deletePath = (id) => {
  return request(`/api/admin/path/delete/${id}`, {
    method: 'POST'
  })
}

export const getAuditLogs = (params) => {
  const query = params ? new URLSearchParams(params).toString() : ''
  return request(`/api/admin/audit/list${query ? '?' + query : ''}`)
}

export const getSystemStatus = () => {
  return request('/api/admin/system/status')
}

export const getSystemMetrics = () => {
  return request('/api/admin/system/metrics')
}

export const getSandboxStatus = () => {
  return request('/api/admin/system/sandbox')
}

export const getSystemLogs = (params) => {
  const query = params ? new URLSearchParams(params).toString() : ''
  return request(`/api/admin/system/logs${query ? '?' + query : ''}`)
}

export const getDashboard = () => {
  return request('/api/admin/dashboard')
}

export const getHomeStatistics = () => {
  return request('/api/admin/home/statistics')
}
