import assert from 'node:assert/strict'
import test from 'node:test'

import { shouldShowResourceSidebar } from './layoutVisibility.js'

test('hides the resource sidebar for routes that opt out', () => {
  assert.equal(shouldShowResourceSidebar({ meta: { hideResourceSidebar: true } }), false)
})

test('keeps the resource sidebar on normal app routes', () => {
  assert.equal(shouldShowResourceSidebar({ meta: {} }), true)
})

test('hides the resource sidebar when the header is hidden', () => {
  assert.equal(shouldShowResourceSidebar({ meta: { hideHeader: true } }), false)
})
