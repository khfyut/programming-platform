import { ElMessageBox } from 'element-plus'

let router = null
let userStore = null

const DEFAULT_JSON_ERROR = { code: 500, msg: '服务端返回了无效响应', data: null }
const DEFAULT_UNAUTHORIZED = { code: 401, msg: '未登录', data: null }
const CANCELLED_UNAUTHORIZED = { code: 401, msg: '用户取消重新登录', data: null }

const appendQuery = (url, params) => {
  if (!params || typeof params !== 'object') {
    return url
  }

  const searchParams = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') {
      return
    }

    if (Array.isArray(value)) {
      value.forEach((item) => {
        if (item !== undefined && item !== null && item !== '') {
          searchParams.append(key, item)
        }
      })
      return
    }

    searchParams.append(key, value)
  })

  const query = searchParams.toString()
  if (!query) {
    return url
  }

  return `${url}${url.includes('?') ? '&' : '?'}${query}`
}

const normalizeRequest = (input, options = {}) => {
  if (typeof input === 'string') {
    return {
      url: appendQuery(input, options.params),
      options: { ...options, params: undefined }
    }
  }

  if (input && typeof input === 'object' && typeof input.url === 'string') {
    const mergedOptions = {
      ...input,
      ...options,
      headers: {
        ...(input.headers || {}),
        ...(options.headers || {})
      }
    }

    return {
      url: appendQuery(input.url, mergedOptions.params),
      options: { ...mergedOptions, url: undefined, params: undefined }
    }
  }

  throw new Error('Invalid request config')
}

const parseJson = (response) => response.json().catch(() => DEFAULT_JSON_ERROR)

const handleTokenExpired = () => {
  return ElMessageBox.confirm('登录已过期，请重新登录', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      if (userStore) {
        userStore.clearToken()
      }
      if (router) {
        router.push('/login')
      }
    })
    .catch(() => Promise.reject(new Error('cancelled')))
}

export const setRequestContext = (r, u) => {
  router = r
  userStore = u
}

const request = (input, options = {}) => {
  const { url, options: normalizedOptions } = normalizeRequest(input, options)
  const token = localStorage.getItem('token')

  const headers = {
    'Content-Type': 'application/json',
    ...(normalizedOptions.headers || {})
  }

  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  const fetchOptions = {
    ...normalizedOptions,
    method: (normalizedOptions.method || 'GET').toUpperCase(),
    headers
  }

  delete fetchOptions.url
  delete fetchOptions.params

  if (fetchOptions.data !== undefined) {
    if (fetchOptions.data instanceof FormData) {
      fetchOptions.body = fetchOptions.data
      delete fetchOptions.headers['Content-Type']
    } else {
      fetchOptions.body = JSON.stringify(fetchOptions.data)
    }
    delete fetchOptions.data
  }

  const runFetch = () => fetch(url, fetchOptions)

  return runFetch()
    .then((response) => {
      if (response.status === 401) {
        return handleTokenExpired()
          .then(() => runFetch().then(parseJson).catch(() => DEFAULT_UNAUTHORIZED))
          .catch(() => CANCELLED_UNAUTHORIZED)
      }

      return parseJson(response)
    })
    .then((data) => {
      if (data && data.code === 401) {
        return handleTokenExpired()
          .then(() => runFetch().then(parseJson).catch(() => DEFAULT_UNAUTHORIZED))
          .catch(() => CANCELLED_UNAUTHORIZED)
      }
      return data
    })
}

export default request
