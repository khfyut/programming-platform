<template>
  <div class="learning-path-page">
    <div class="path-container">
      <div class="page-header">
        <div class="header-left">
          <el-button text @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <div class="path-info" v-if="pathDetail">
            <h1 class="page-title">{{ pathDetail.name }}</h1>
            <p class="page-subtitle">{{ pathDetail.description }}</p>
          </div>
        </div>
        <div class="header-right" v-if="pathDetail">
          <el-tag :type="getDifficultyType(pathDetail.difficulty)">
            {{ getDifficultyText(pathDetail.difficulty) }}
          </el-tag>
        </div>
      </div>

      <div class="progress-section" v-if="pathDetail">
        <div class="progress-stats">
          <div class="stat-item">
            <span class="stat-value">{{ progress.completedLevels }}</span>
            <span class="stat-label">已完成</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-value">{{ progress.totalLevels }}</span>
            <span class="stat-label">总关卡</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-value">{{ Math.round(progress.progress) }}%</span>
            <span class="stat-label">完成度</span>
          </div>
        </div>
        <el-progress :percentage="progress.progress" :stroke-width="12" :show-text="false" />
      </div>

      <div class="chapters-section" v-loading="loading">
        <template v-if="chapters.length > 0">
          <div v-for="chapter in chapters" :key="chapter.id" class="chapter-card">
            <div class="chapter-header" @click="toggleChapter(chapter.id)">
              <div class="chapter-left">
                <div class="chapter-icon" :class="{ completed: isChapterCompleted(chapter) }">
                  <el-icon v-if="isChapterCompleted(chapter)"><Check /></el-icon>
                  <span v-else>{{ chapter.order }}</span>
                </div>
                <div class="chapter-info">
                  <h3 class="chapter-name">{{ chapter.name }}</h3>
                  <p class="chapter-desc">{{ chapter.description }}</p>
                </div>
              </div>
              <div class="chapter-right">
                <span class="level-count">{{ getCompletedCount(chapter) }}/{{ chapter.levels.length }} 关</span>
                <el-icon class="expand-icon" :class="{ expanded: expandedChapters.includes(chapter.id) }">
                  <ArrowDown />
                </el-icon>
              </div>
            </div>

            <transition name="expand">
              <div class="chapter-levels" v-if="expandedChapters.includes(chapter.id)">
                <div
                  v-for="level in chapter.levels"
                  :key="level.id"
                  class="level-item"
                  :class="level.status"
                >
                  <div class="level-main">
                    <div class="level-icon">
                      <el-icon v-if="level.status === 'completed'"><Check /></el-icon>
                      <el-icon v-else-if="level.status === 'locked'"><Lock /></el-icon>
                      <span v-else>{{ level.order }}</span>
                    </div>
                    <div class="level-info">
                      <h4 class="level-name">{{ level.name }}</h4>
                      <div class="level-meta">
                        <span class="meta-item">
                          <el-icon><Document /></el-icon>
                          {{ level.problemCount }} 题
                        </span>
                        <span class="meta-item" v-if="level.estimatedTime">
                          <el-icon><Clock /></el-icon>
                          {{ level.estimatedTime }}
                        </span>
                      </div>
                    </div>
                  </div>

                  <div class="level-actions">
                    <el-button v-if="level.status === 'locked'" disabled size="small">
                      未解锁
                    </el-button>
                    <el-button v-else-if="level.status === 'completed'" size="small" @click.stop="goToLevel(level)">
                      复习
                    </el-button>
                    <el-button v-else-if="level.status === 'current'" type="primary" size="small" @click.stop="startLevel(level)">
                      开始学习
                    </el-button>
                    <el-button v-else size="small" @click.stop="goToLevel(level)">
                      继续
                    </el-button>
                  </div>
                </div>
              </div>
            </transition>
          </div>
        </template>

        <el-empty v-else-if="!loading" description="暂无学习路径内容" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown, ArrowLeft, Check, Clock, Document, Lock } from '@element-plus/icons-vue'
import { getPathDetail, getPathProgress, unlockLevel } from '@/api/learn'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const pathDetail = ref(null)
const chapters = ref([])
const expandedChapters = ref([])
const progress = ref({
  completedLevels: 0,
  totalLevels: 0,
  progress: 0
})

const pathId = computed(() => route.params.id)

const resetPathData = (showError = false) => {
  pathDetail.value = null
  chapters.value = []
  expandedChapters.value = []
  progress.value = {
    completedLevels: 0,
    totalLevels: 0,
    progress: 0
  }

  if (showError) {
    ElMessage.error('获取学习路径失败，请稍后重试')
  } else {
    ElMessage.warning('学习路径暂无可展示数据')
  }
}

const normalizeProgress = (raw) => {
  if (!raw || typeof raw !== 'object') {
    return { completedLevels: 0, totalLevels: 0, progress: 0 }
  }

  return {
    completedLevels: Number(raw.completedLevels || 0),
    totalLevels: Number(raw.totalLevels || 0),
    progress: Number(raw.progress || 0)
  }
}

const toCompletedLevelSet = (raw) => {
  const completedLevelSet = new Set()

  if (!raw || typeof raw !== 'object') {
    return completedLevelSet
  }

  if (Array.isArray(raw.completedLevelIds)) {
    raw.completedLevelIds.forEach((id) => completedLevelSet.add(Number(id)))
    return completedLevelSet
  }

  if (typeof raw.completedLevels === 'string' && raw.completedLevels.includes(',')) {
    raw.completedLevels
      .split(',')
      .map((id) => Number(id))
      .filter((id) => !Number.isNaN(id))
      .forEach((id) => completedLevelSet.add(id))
  }

  return completedLevelSet
}

const normalizeChapters = (rawChapters, completedSet) => {
  return (rawChapters || []).map((chapter) => {
    const levels = (chapter.levels || []).map((level, index, allLevels) => {
      const currentLevelId = Number(level.id)
      const previousLevelId = Number(allLevels[index - 1]?.id)

      let status = level.status
      if (!status) {
        if (completedSet.has(currentLevelId)) {
          status = 'completed'
        } else if (index === 0 || completedSet.has(previousLevelId)) {
          status = 'current'
        } else {
          status = 'locked'
        }
      }

      return {
        ...level,
        order: level.orderNum ?? level.order ?? index + 1,
        problemCount: level.problemCount ?? (level.problemIds ? level.problemIds.split(',').filter(Boolean).length : 0),
        status
      }
    })

    return {
      ...chapter,
      order: chapter.orderNum ?? chapter.order ?? 1,
      levels
    }
  })
}

const fetchPathDetail = async () => {
  loading.value = true
  try {
    const [pathRes, progressRes] = await Promise.all([
      getPathDetail(pathId.value),
      getPathProgress(pathId.value)
    ])

    if (pathRes?.code !== 200 || !pathRes.data) {
      resetPathData(false)
      return
    }

    pathDetail.value = pathRes.data
    progress.value = normalizeProgress(progressRes?.data)
    const completedSet = toCompletedLevelSet(progressRes?.data)
    chapters.value = normalizeChapters(pathRes.data.chapters, completedSet)

    if (chapters.value.length > 0) {
      expandedChapters.value = [chapters.value[0].id]
    }
  } catch (error) {
    console.error('获取学习路径失败:', error)
    resetPathData(true)
  } finally {
    loading.value = false
  }
}

const getDifficultyType = (difficulty) => {
  const map = {
    beginner: 'success',
    intermediate: 'warning',
    advanced: 'danger'
  }
  return map[difficulty] || 'info'
}

const getDifficultyText = (difficulty) => {
  const map = {
    beginner: '初级',
    intermediate: '中级',
    advanced: '高级'
  }
  return map[difficulty] || '未知'
}

const isChapterCompleted = (chapter) => chapter.levels.length > 0 && chapter.levels.every((level) => level.status === 'completed')

const getCompletedCount = (chapter) => chapter.levels.filter((level) => level.status === 'completed').length

const toggleChapter = (chapterId) => {
  const index = expandedChapters.value.indexOf(chapterId)
  if (index > -1) {
    expandedChapters.value.splice(index, 1)
  } else {
    expandedChapters.value.push(chapterId)
  }
}

const goToLevel = (level) => {
  if (level.status === 'locked') {
    ElMessage.warning('该关卡尚未解锁')
    return
  }
  router.push(`/learn/path/${pathId.value}/level/${level.id}`)
}

const startLevel = async (level) => {
  if (level.status === 'locked') {
    try {
      const res = await unlockLevel(level.id)
      if (res?.code !== 200) {
        ElMessage.error(res?.msg || '解锁失败')
        return
      }
      level.status = 'current'
      ElMessage.success('关卡已解锁')
    } catch (error) {
      console.error('解锁失败:', error)
      ElMessage.error('解锁失败')
      return
    }
  }

  goToLevel(level)
}

const goBack = () => {
  router.push('/learn')
}

onMounted(() => {
  fetchPathDetail()
})
</script>

<style scoped>
.learning-path-page {
  width: 100%;
  min-height: 100vh;
  padding: 24px;
  box-sizing: border-box;
  background: var(--leetcode-bg-secondary, #f7f8fa);
}

.path-container {
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.page-title {
  margin: 0 0 8px;
  font-size: 28px;
  color: var(--leetcode-text, #24292f);
}

.page-subtitle {
  margin: 0;
  font-size: 14px;
  color: var(--leetcode-text-secondary, #6b7280);
}

.progress-section {
  margin-bottom: 20px;
  padding: 20px;
  border-radius: 12px;
  border: 1px solid var(--leetcode-border, #e5e7eb);
  background: var(--leetcode-bg, #fff);
}

.progress-stats {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 16px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
}

.stat-label {
  margin-top: 4px;
  font-size: 12px;
  color: var(--leetcode-text-secondary, #6b7280);
}

.stat-divider {
  width: 1px;
  height: 36px;
  background: var(--leetcode-border, #e5e7eb);
}

.chapters-section {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.chapter-card {
  border-radius: 12px;
  border: 1px solid var(--leetcode-border, #e5e7eb);
  background: var(--leetcode-bg, #fff);
  overflow: hidden;
}

.chapter-header {
  padding: 18px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
}

.chapter-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.chapter-icon {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid var(--leetcode-border, #e5e7eb);
  color: var(--leetcode-text-secondary, #6b7280);
}

.chapter-icon.completed {
  background: #00c853;
  border-color: #00c853;
  color: #fff;
}

.chapter-name {
  margin: 0 0 4px;
  font-size: 16px;
}

.chapter-desc {
  margin: 0;
  font-size: 13px;
  color: var(--leetcode-text-secondary, #6b7280);
}

.chapter-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.level-count {
  font-size: 13px;
  color: var(--leetcode-text-secondary, #6b7280);
}

.expand-icon {
  transition: transform 0.2s ease;
}

.expand-icon.expanded {
  transform: rotate(180deg);
}

.chapter-levels {
  padding: 0 20px 20px;
}

.level-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-top: 10px;
  padding: 12px 14px;
  border: 1px solid var(--leetcode-border, #e5e7eb);
  border-radius: 10px;
  background: var(--leetcode-bg-secondary, #f7f8fa);
}

.level-item.locked {
  opacity: 0.65;
}

.level-main {
  display: flex;
  align-items: center;
  gap: 12px;
}

.level-icon {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--leetcode-border, #e5e7eb);
  background: #fff;
  color: var(--leetcode-text-secondary, #6b7280);
}

.level-item.completed .level-icon {
  background: #00c853;
  border-color: #00c853;
  color: #fff;
}

.level-item.current .level-icon {
  background: #0066ff;
  border-color: #0066ff;
  color: #fff;
}

.level-name {
  margin: 0 0 4px;
  font-size: 14px;
}

.level-meta {
  display: flex;
  gap: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--leetcode-text-secondary, #6b7280);
}

.expand-enter-active,
.expand-leave-active {
  transition: all 0.2s ease;
  overflow: hidden;
}

.expand-enter-from,
.expand-leave-to {
  opacity: 0;
  max-height: 0;
}

.expand-enter-to,
.expand-leave-from {
  opacity: 1;
  max-height: 1200px;
}

@media (max-width: 768px) {
  .learning-path-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    gap: 12px;
  }

  .header-left {
    width: 100%;
  }

  .progress-stats {
    gap: 12px;
    flex-wrap: wrap;
  }

  .stat-divider {
    display: none;
  }

  .level-item {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
