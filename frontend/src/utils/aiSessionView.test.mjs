import assert from 'node:assert/strict'
import test from 'node:test'

import {
  getSessionId,
  getSessionTitle,
  normalizeSessionList,
  shouldShowAiSessionSidebar
} from './aiSessionView.js'

test('normalizes mixed AI session response shapes', () => {
  const result = normalizeSessionList({
    records: [
      { id: 1, title: 'First question' },
      { sessionId: 'abc', topic: 'Second question' }
    ]
  })

  assert.equal(result.length, 2)
  assert.equal(result[0].sessionId, 1)
  assert.equal(result[0].topic, 'First question')
  assert.equal(result[1].id, 'abc')
  assert.equal(result[1].topic, 'Second question')
})

test('uses stable id and title fallbacks for sidebar rendering', () => {
  assert.equal(getSessionId({ sessionId: 's-1', id: 9 }), 's-1')
  assert.equal(getSessionId({ id: 9 }), 9)
  assert.equal(getSessionTitle({ firstQuestion: 'Why stack?' }), 'Why stack?')
  assert.equal(getSessionTitle({}), '新对话')
})

test('shows AI session sidebar only on the AI page with resource sidebar visible', () => {
  assert.equal(shouldShowAiSessionSidebar({ path: '/ai', meta: {} }), true)
  assert.equal(shouldShowAiSessionSidebar({ path: '/problems', meta: {} }), false)
  assert.equal(shouldShowAiSessionSidebar({ path: '/ai', meta: { hideResourceSidebar: true } }), false)
  assert.equal(shouldShowAiSessionSidebar({ path: '/ai', meta: { hideHeader: true } }), false)
})
