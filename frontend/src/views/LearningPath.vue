<template>
  <div class="learning-path-page">
    <div class="path-container">
      <div class="page-header">
        <div class="header-left">
          <el-button text @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <div v-if="pathDetail" class="path-info">
            <h1 class="page-title">{{ pathDetail.name }}</h1>
            <p class="page-subtitle">{{ pathDetail.description || '按章节逐步完成关卡练习。' }}</p>
          </div>
        </div>

        <div v-if="pathDetail" class="header-right">
          <el-tag type="info">{{ getPathMetaText(pathDetail) }}</el-tag>
        </div>
      </div>

      <div v-if="pathDetail" class="progress-section">
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
                  <p class="chapter-desc">{{ chapter.description || '完成本章关卡后可进入下一章节。' }}</p>
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
              <div v-if="expandedChapters.includes(chapter.id)" class="chapter-levels">
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
                      <div class="level-name-row">
                        <h4 class="level-name">{{ level.name }}</h4>
                        <el-tag size="small" :type="getStatusTagType(level.status)">
                          {{ getStatusText(level.status) }}
                        </el-tag>
                      </div>
                      <div class="level-meta">
                        <span class="meta-item">
                          <el-icon><Document /></el-icon>
                          {{ level.problemCount }} 题
                        </span>
                        <span v-if="level.estimatedTime" class="meta-item">
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
                    <el-button
                      v-else-if="level.status === 'current'"
                      type="primary"
                      size="small"
                      @click.stop="startLevel(level)"
                    >
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
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown, ArrowLeft, Check, Clock, Document, Lock } from '@element-plus/icons-vue'
import { getPathDetail, getPathProgress } from '@/api/learn'
import { normalizeLearningPathChapters, normalizeProgress, toCompletedLevelSet } from '@/utils/learningPathProgress'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const pathDetail = ref(null)
const chapters = ref([])
const expandedChapters = ref([])
const progress = ref({
  completedLevels: 0,
  totalLevels: 0,
  progress: 0,
  currentChapterId: null,
  currentLevelId: null,
  completedLevelIds: []
})

const pathId = computed(() => Number(route.params.id))

const resetPathData = (showError = false) => {
  pathDetail.value = null
  chapters.value = []
  expandedChapters.value = []
  progress.value = {
    completedLevels: 0,
    totalLevels: 0,
    progress: 0,
    currentChapterId: null,
    currentLevelId: null,
    completedLevelIds: []
  }

  if (showError) {
    ElMessage.error('获取学习路径失败，请稍后重试')
  }
}

const fetchPathDetail = async () => {
  if (!pathId.value) {
    resetPathData(true)
    return
  }

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

    chapters.value = normalizeLearningPathChapters(
      pathRes.data.chapters,
      completedSet,
      progress.value.currentChapterId,
      progress.value.currentLevelId
    )

    const defaultExpanded = []
    if (progress.value.currentChapterId) {
      defaultExpanded.push(progress.value.currentChapterId)
    }
    if (defaultExpanded.length === 0 && chapters.value.length > 0) {
      defaultExpanded.push(chapters.value[0].id)
    }
    expandedChapters.value = defaultExpanded
  } catch (error) {
    console.error('获取学习路径失败:', error)
    resetPathData(true)
  } finally {
    loading.value = false
  }
}

const getPathMetaText = (path) => {
  const parts = [path.language, path.direction].filter(Boolean)
  return parts.length > 0 ? parts.join(' / ') : '学习路径'
}

const getStatusText = (status) => {
  const map = {
    completed: '已完成',
    current: '当前关卡',
    available: '可进入',
    locked: '未解锁'
  }
  return map[status] || '未解锁'
}

const getStatusTagType = (status) => {
  const map = {
    completed: 'success',
    current: 'primary',
    available: 'warning',
    locked: 'info'
  }
  return map[status] || 'info'
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
    ElMessage.warning('该关卡尚未解锁')
    return
  }

  goToLevel(level)
}

const goBack = () => {
  router.push('/learn')
}

watch(pathId, () => {
  fetchPathDetail()
})

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
  gap: 16px;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.path-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.page-title {
  margin: 0;
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
  gap: 16px;
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

.level-item.current {
  border-color: #0066ff;
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

.level-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.level-name {
  margin: 0;
  font-size: 14px;
}

.level-meta {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
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

  .level-actions {
    width: 100%;
  }
}
</style>
