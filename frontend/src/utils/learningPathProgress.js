export const normalizeProgress = (raw) => {
  if (!raw || typeof raw !== 'object') {
    return {
      completedLevels: 0,
      totalLevels: 0,
      progress: 0,
      currentChapterId: null,
      currentLevelId: null,
      completedLevelIds: []
    }
  }

  return {
    completedLevels: Number(raw.completedLevels || 0),
    totalLevels: Number(raw.totalLevels || 0),
    progress: Number(raw.progress || 0),
    currentChapterId: raw.currentChapterId ? Number(raw.currentChapterId) : null,
    currentLevelId: raw.currentLevelId ? Number(raw.currentLevelId) : null,
    completedLevelIds: Array.isArray(raw.completedLevelIds) ? raw.completedLevelIds.map((id) => Number(id)) : []
  }
}

export const toCompletedLevelSet = (raw) => {
  const set = new Set()
  const normalized = normalizeProgress(raw)
  normalized.completedLevelIds
    .filter((id) => !Number.isNaN(id))
    .forEach((id) => set.add(id))
  return set
}

export const normalizeLearningPathChapters = (rawChapters, completedSet, _currentChapterId, currentLevelId) => {
  let fallbackCurrentAssigned = false

  return (rawChapters || []).map((chapter, chapterIndex) => {
    const levels = (chapter.levels || []).map((level, index) => {
      const levelId = Number(level.id)
      const problemCount = level.problemCount ?? (level.problemIds ? level.problemIds.split(',').filter(Boolean).length : 0)

      let status = 'available'
      if (completedSet.has(levelId)) {
        status = 'completed'
      } else if (currentLevelId && currentLevelId === levelId) {
        status = 'current'
        fallbackCurrentAssigned = true
      } else if (!currentLevelId && !fallbackCurrentAssigned) {
        status = 'current'
        fallbackCurrentAssigned = true
      }

      return {
        ...level,
        order: level.orderNum ?? level.order ?? index + 1,
        problemCount,
        status
      }
    })

    return {
      ...chapter,
      order: chapter.orderNum ?? chapter.order ?? chapterIndex + 1,
      levels
    }
  })
}
