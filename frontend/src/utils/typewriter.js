export const splitForTypewriter = (text) => Array.from(String(text || ''))

export const createTypewriterController = ({
  interval = 18,
  setIntervalFn = setInterval,
  clearIntervalFn = clearInterval,
  onTick
} = {}) => {
  const queue = []
  const idleResolvers = []
  let timer = null

  const resolveIdle = () => {
    while (idleResolvers.length) {
      idleResolvers.shift()()
    }
  }

  const stopTimer = () => {
    if (timer) {
      clearIntervalFn(timer)
      timer = null
    }
  }

  const tick = () => {
    const next = queue.shift()
    if (!next) {
      stopTimer()
      resolveIdle()
      return
    }

    next.target.content += next.char
    onTick?.(next.target)

    if (queue.length === 0) {
      stopTimer()
      resolveIdle()
    }
  }

  const start = () => {
    if (!timer && queue.length > 0) {
      timer = setIntervalFn(tick, interval)
    }
  }

  return {
    enqueue(target, text) {
      const chars = splitForTypewriter(text)
      if (!target || chars.length === 0) {
        return
      }

      queue.push(...chars.map((char) => ({ target, char })))
      start()
    },
    waitForIdle() {
      if (!timer && queue.length === 0) {
        return Promise.resolve()
      }

      return new Promise((resolve) => {
        idleResolvers.push(resolve)
      })
    },
    stop({ clearQueue = true } = {}) {
      stopTimer()
      if (clearQueue) {
        queue.length = 0
      }
      resolveIdle()
    }
  }
}
