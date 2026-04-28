import assert from 'node:assert/strict'
import test from 'node:test'
import { parseSseBuffer, splitForTypewriter } from './sseStream.js'

test('parseSseBuffer parses complete events and preserves partial tail', () => {
  const events = []
  const partial = parseSseBuffer(
    'event: status\ndata: {"text":"preparing"}\n\nevent: delta\ndata: {"text":"He"}\n\nevent: delta\ndata: {"text"',
    (event) => events.push(event)
  )

  assert.deepEqual(events, [
    { event: 'status', data: { text: 'preparing' } },
    { event: 'delta', data: { text: 'He' } }
  ])
  assert.equal(partial, 'event: delta\ndata: {"text"')
})

test('parseSseBuffer supports multi-line data payloads', () => {
  const events = []
  const partial = parseSseBuffer(
    'event: delta\ndata: {"text":"line1\\n"\ndata: "line2"}\n\n',
    (event) => events.push(event)
  )

  assert.equal(partial, '')
  assert.deepEqual(events, [
    { event: 'delta', data: '{"text":"line1\\n"\n"line2"}' }
  ])
})

test('splitForTypewriter keeps unicode characters intact', () => {
  assert.deepEqual(splitForTypewriter('你A🙂'), ['你', 'A', '🙂'])
})
