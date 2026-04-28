export const normalizeSessionList = (data) => {
  const list = Array.isArray(data) ? data : data?.list || data?.records || data?.items || []
  return list.map((item) => ({
    ...item,
    id: item.id ?? item.sessionId,
    sessionId: item.sessionId ?? item.id,
    topic: item.topic || item.title || item.firstQuestion || '新对话'
  }))
}

export const normalizeMessageList = (data) => {
  const list = Array.isArray(data) ? data : data?.list || data?.messages || data?.records || []
  return list.map((item) => ({
    role: item.role === 'assistant' ? 'assistant' : 'user',
    content: item.content || item.message || item.response || '',
    time: item.time || item.createTime || item.updateTime || new Date().toISOString()
  }))
}

export const getSessionId = (session) => session?.sessionId || session?.id || null

export const getSessionKey = (session) => getSessionId(session) || Math.random()

export const getSessionTitle = (session) => session?.topic || session?.title || session?.firstQuestion || '新对话'

export const formatSessionTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  if (Number.isNaN(date.getTime())) return ''

  const diff = Date.now() - date.getTime()
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)} 天前`
  return date.toLocaleDateString('zh-CN')
}

export const shouldShowAiSessionSidebar = (route) => {
  const meta = route?.meta || {}
  return route?.path === '/ai' && !meta.hideHeader && !meta.hideResourceSidebar
}
