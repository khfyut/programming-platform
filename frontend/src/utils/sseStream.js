export const splitForTypewriter = (text) => Array.from(String(text || ''))

const parseEventData = (rawData) => {
  if (!rawData) {
    return {}
  }
  try {
    return JSON.parse(rawData)
  } catch {
    return rawData
  }
}

const parseEventBlock = (block) => {
  const lines = block.split('\n')
  let event = 'message'
  const dataLines = []

  lines.forEach((line) => {
    if (line.startsWith('event:')) {
      event = line.slice('event:'.length).trim() || 'message'
    } else if (line.startsWith('data:')) {
      dataLines.push(line.slice('data:'.length).trimStart())
    }
  })

  return {
    event,
    data: parseEventData(dataLines.join('\n'))
  }
}

export const parseSseBuffer = (buffer, onEvent) => {
  const normalized = String(buffer || '').replace(/\r\n/g, '\n')
  const parts = normalized.split('\n\n')
  const partial = parts.pop() || ''

  parts.forEach((part) => {
    const block = part.trimEnd()
    if (!block) {
      return
    }
    onEvent(parseEventBlock(block))
  })

  return partial
}

export const streamSsePost = async (url, payload, handlers = {}) => {
  const token = localStorage.getItem('token')
  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    body: JSON.stringify(payload || {}),
    signal: handlers.signal
  })

  if (!response.ok || !response.body) {
    throw new Error(`Stream request failed: ${response.status}`)
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''
  let completed = false

  const dispatch = (event) => {
    handlers.onEvent?.(event)
    if (event.event === 'status') handlers.onStatus?.(event.data)
    if (event.event === 'meta') handlers.onMeta?.(event.data)
    if (event.event === 'delta') handlers.onDelta?.(event.data)
    if (event.event === 'done') {
      completed = true
      handlers.onDone?.(event.data)
    }
    if (event.event === 'error') handlers.onError?.(event.data)
  }

  try {
    while (true) {
      const { done, value } = await reader.read()
      if (done) {
        break
      }
      buffer += decoder.decode(value, { stream: true })
      buffer = parseSseBuffer(buffer, dispatch)
      if (completed) {
        await reader.cancel().catch(() => {})
        return
      }
    }
  } catch (error) {
    if (!completed) {
      throw error
    }
    return
  }

  buffer += decoder.decode()
  if (buffer.trim()) {
    parseSseBuffer(`${buffer}\n\n`, dispatch)
  }
}
