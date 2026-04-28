import assert from 'node:assert/strict'
import test from 'node:test'

import { normalizeLearningPathChapters } from './learningPathProgress.js'

test('default-unlocked path marks every incomplete level as enterable', () => {
  const chapters = normalizeLearningPathChapters(
    [
      {
        id: 10,
        levels: [
          { id: 100, name: 'Intro', orderNum: 1 },
          { id: 101, name: 'Branch A', orderNum: 2 },
          { id: 102, name: 'Branch B', orderNum: 3 }
        ]
      },
      {
        id: 11,
        levels: [{ id: 200, name: 'Later branch', orderNum: 1 }]
      }
    ],
    new Set([100]),
    10,
    101
  )

  const statuses = chapters.flatMap((chapter) => chapter.levels.map((level) => level.status))

  assert.deepEqual(statuses, ['completed', 'current', 'available', 'available'])
  assert.equal(statuses.includes('locked'), false)
})
