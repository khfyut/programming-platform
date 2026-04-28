import assert from 'node:assert/strict'
import test from 'node:test'
import { createTypewriterController } from './typewriter.js'

test('typewriter appends one unicode character on each tick', async () => {
  const timers = []
  const controller = createTypewriterController({
    setIntervalFn(callback) {
      timers.push(callback)
      return callback
    },
    clearIntervalFn() {}
  })
  const message = { content: '' }

  controller.enqueue(message, '你A🙂')

  assert.equal(message.content, '')
  timers[0]()
  assert.equal(message.content, '你')
  timers[0]()
  assert.equal(message.content, '你A')
  timers[0]()
  assert.equal(message.content, '你A🙂')
})

test('waitForIdle resolves after queued text has been fully displayed', async () => {
  const timers = []
  const controller = createTypewriterController({
    setIntervalFn(callback) {
      timers.push(callback)
      return callback
    },
    clearIntervalFn() {}
  })
  const message = { content: '' }
  let resolved = false

  controller.enqueue(message, 'OK')
  const idle = controller.waitForIdle().then(() => {
    resolved = true
  })

  timers[0]()
  await Promise.resolve()
  assert.equal(resolved, false)

  timers[0]()
  timers[0]()
  await idle
  assert.equal(resolved, true)
})
