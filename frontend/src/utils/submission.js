export const getSubmissionResultText = (resultType) => {
  const texts = {
    0: '通过',
    1: '答案错误',
    2: '运行错误',
    3: '超时',
    4: '内存超限'
  }

  return texts[resultType] || '未知错误'
}

export const formatSubmissionDateTime = (time) => {
  if (!time) return '未知时间'

  const date = new Date(time)
  if (Number.isNaN(date.getTime())) return '未知时间'

  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}
