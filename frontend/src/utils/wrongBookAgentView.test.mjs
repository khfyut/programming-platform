import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import test from 'node:test'

const componentSource = readFileSync(
  new URL('../components/agent/WrongBookReviewAgent.vue', import.meta.url),
  'utf8'
)
const utilitySource = readFileSync(new URL('./wrongBookAgent.js', import.meta.url), 'utf8')

test('wrong book coach panel hides explicit hint level controls', () => {
  assert.equal(componentSource.includes('class="hint-panel"'), false)
  assert.equal(componentSource.includes('hintLevels'), false)
  assert.equal(componentSource.includes('requestHint'), false)
  assert.equal(componentSource.includes('提示等级'), false)
  assert.equal(componentSource.includes('L1 方向提示'), false)
  assert.equal(componentSource.includes('action-grid'), true)
  assert.equal(utilitySource.includes('提示我关键点'), true)
})
