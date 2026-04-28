import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import test from 'node:test'

const mainLayoutSource = readFileSync(new URL('../layout/MainLayout.vue', import.meta.url), 'utf8')
const problemsSource = readFileSync(new URL('../views/Problems.vue', import.meta.url), 'utf8')
const problemStoreSource = readFileSync(new URL('../stores/problem.js', import.meta.url), 'utf8')

test('removes low-value problem search entry points', () => {
  assert.equal(mainLayoutSource.includes('placeholder="搜索题目..."'), false)
  assert.equal(mainLayoutSource.includes('const searchKeyword'), false)
  assert.equal(mainLayoutSource.includes('const handleSearch'), false)

  assert.equal(problemsSource.includes('placeholder="搜索题目..."'), false)
  assert.equal(problemsSource.includes('searchKeyword'), false)
  assert.equal(problemsSource.includes('handleSearch'), false)

  assert.equal(problemStoreSource.includes('searchKeyword'), false)
  assert.equal(problemStoreSource.includes('params.keyword'), false)
  assert.equal(problemStoreSource.includes('handleSearch'), false)
})
