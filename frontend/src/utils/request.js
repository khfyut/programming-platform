import { ElMessageBox } from 'element-plus'

let router = null
let userStore = null

const handleTokenExpired = () => {
  return ElMessageBox.confirm('登录已过期，请重新登录', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    if (userStore) {
      userStore.clearToken()
    }
    if (router) {
      router.push('/login')
    }
  }).catch(() => {
    return Promise.reject(new Error('用户取消'))
  })
}

export const setRequestContext = (r, u) => {
  router = r
  userStore = u
}

const request = (url, options = {}) => {
  const token = localStorage.getItem('token')
  console.log('========== 请求开始 ==========')
  console.log('请求 URL:', url)
  console.log('Token 原始值:', token)
  console.log('Token 类型:', typeof token)
  console.log('Token 长度:', token ? token.length : 0)
  console.log('Token 前10个字符:', token ? token.substring(0, 10) : 'N/A')
  
  const headers = {
    'Content-Type': 'application/json'
  }

  if (token) {
    headers['Authorization'] = `Bearer ${token}`
    console.log('设置 Authorization header:', headers['Authorization'])
  }

  if (options.headers) {
    console.log('合并自定义 headers:', options.headers)
    Object.assign(headers, options.headers)
  }

  const fetchOptions = {
    ...options,
    headers: headers
  }

  if (fetchOptions.data) {
    // 如果是FormData对象，直接使用，不进行JSON转换
    if (fetchOptions.data instanceof FormData) {
      fetchOptions.body = fetchOptions.data
      // 删除默认的Content-Type，让浏览器自动设置
      delete headers['Content-Type']
    } else {
      fetchOptions.body = JSON.stringify(fetchOptions.data)
    }
    delete fetchOptions.data
  }

  console.log('最终 headers:', headers)
  console.log('Authorization header:', headers.Authorization)
  console.log('Authorization header 类型:', typeof headers.Authorization)
  console.log('完整请求选项:', fetchOptions)
  console.log('========== 请求结束 ==========')

  return fetch(url, fetchOptions)
    .then(res => {
      if (res.status === 401) {
        return handleTokenExpired()
      }
      return res.json()
    })
    .then(data => {
      if (data && data.code === 401) {
        return handleTokenExpired()
      }
      return data
    })
}

export default request
