import assert from 'node:assert/strict'
import test from 'node:test'

import { getSafeRedirectPath } from './navigation.js'

test('returns a safe internal redirect path', () => {
  assert.equal(getSafeRedirectPath('/problems'), '/problems')
  assert.equal(getSafeRedirectPath('/problem/1?tab=submissions'), '/problem/1?tab=submissions')
})

test('rejects external and auth-loop redirect paths', () => {
  assert.equal(getSafeRedirectPath('https://example.com'), '/')
  assert.equal(getSafeRedirectPath('http://example.com'), '/')
  assert.equal(getSafeRedirectPath('//example.com'), '/')
  assert.equal(getSafeRedirectPath('/login'), '/')
  assert.equal(getSafeRedirectPath('/register?redirect=/ai'), '/')
  assert.equal(getSafeRedirectPath('problems'), '/')
})
