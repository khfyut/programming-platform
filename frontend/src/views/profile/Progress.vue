<template>
  <div class="progress-page">
    <div class="section-card">
      <div class="section-header">
        <h3 class="section-title">正在学习的路径</h3>
        <router-link to="/dashboard/learn" class="view-all-link">
          查看全部
          <el-icon><ArrowRight /></el-icon>
        </router-link>
      </div>

      <div class="paths-list">
        <div v-for="path in activePaths" :key="path.id" class="path-item">
          <div class="path-info">
            <div class="path-header">
              <div class="path-badges">
                <el-tag v-if="path.language" :type="getLanguageType(path.language)" size="small">
                  {{ getLanguageLabel(path.language) }}
                </el-tag>
                <el-tag v-if="path.direction" type="info" size="small">
                  {{ getDirectionLabel(path.direction) }}
                </el-tag>
              </div>
              <span class="path-progress-text">{{ path.progress || 0 }}%</span>
            </div>
            <h4 class="path-name">{{ path.name }}</h4>
            <p class="path-description">{{ path.description || '继续沿着当前路径完成学习进度。' }}</p>
          </div>

          <div class="path-progress">
            <el-progress
              :percentage="path.progress || 0"
              :stroke-width="10"
              :color="getProgressColor(path.progress)"
              :show-text="false"
            />
            <div class="progress-meta">
              <span>当前进度 {{ path.progress || 0 }}%</span>
              <span>{{ getDirectionLabel(path.direction) || '学习中' }}</span>
            </div>
          </div>

          <div class="path-actions">
            <el-button type="primary" @click="continuePath(path.id)">
              <el-icon><VideoPlay /></el-icon>
              继续学习
            </el-button>
            <el-button link @click="viewPathDetail(path.id)">查看详情</el-button>
          </div>
        </div>

        <el-empty v-if="activePaths.length === 0" description="暂无进行中的学习路径">
          <el-button type="primary" @click="goToLearn">开始学习</el-button>
        </el-empty>
      </div>
    </div>

    <div class="section-card">
      <div class="section-header">
        <h3 class="section-title">知识点掌握度</h3>
        <el-button link @click="viewKnowledgeGraph">
          <el-icon><Share /></el-icon>
          知识图谱
        </el-button>
      </div>

      <div class="knowledge-list" v-if="knowledgeItems.length > 0">
        <div v-for="item in knowledgeItems" :key="item.knowledgeId" class="knowledge-item">
          <div class="knowledge-header">
            <div class="knowledge-info">
              <span class="knowledge-name">{{ item.knowledgeName }}</span>
              <el-tag :type="getMasteryTagType(item.masteryPercent)" size="small">
                {{ getMasteryLabel(item.masteryPercent) }}
              </el-tag>
            </div>
            <span class="knowledge-percent">{{ item.masteryPercent }}%</span>
          </div>

          <el-progress
            :percentage="item.masteryPercent"
            :stroke-width="8"
            :color="getMasteryColor(item.masteryPercent)"
            :show-text="false"
          />

          <div class="knowledge-stats">
            <span class="stat-item">
              <el-icon><Document /></el-icon>
              练习 {{ item.practiceCount }} 次
            </span>
            <span class="stat-item">
              <el-icon><CircleCheck /></el-icon>
              正确 {{ item.correctCount }} 次
            </span>
            <span class="stat-item">
              <el-icon><TrendCharts /></el-icon>
              正确率 {{ item.accuracy }}%
            </span>
          </div>
        </div>
      </div>

      <el-empty v-else description="暂无知识点掌握数据" />
    </div>

    <div class="section-card" v-if="weakPointItems.length > 0">
      <div class="section-header">
        <h3 class="section-title">
          <el-icon><Warning /></el-icon>
          薄弱知识点
        </h3>
        <span class="subtitle">建议优先补强</span>
      </div>

      <div class="weak-points-list">
        <div
          v-for="point in weakPointItems"
          :key="point.knowledgeId"
          class="weak-point-item"
          @click="practiceWeakPoint(point)"
        >
          <div class="weak-point-info">
            <span class="weak-point-name">{{ point.knowledgeName }}</span>
            <span class="weak-point-level">掌握度 {{ point.masteryPercent }}%</span>
          </div>
          <el-button type="primary" size="small" link>
            去练习
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  ArrowRight,
  VideoPlay,
  Share,
  Document,
  CircleCheck,
  TrendCharts,
  Warning
} from '@element-plus/icons-vue'
import { getActivePaths, getKnowledgeMastery, getWeakKnowledgePoints } from '@/api/learn'
import { getNodeDetail } from '@/api/knowledgeGraph'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const userId = computed(() => userStore.userInfo?.id)

const activePaths = ref([])
const knowledgeMastery = ref([])
const weakPoints = ref([])

const clampPercentage = (value) => Math.min(100, Math.max(0, Math.round(Number(value) || 0)))

const normalizeMasteryPercent = (item) => {
  if (item.score !== undefined && item.score !== null) {
    return clampPercentage(item.score)
  }

  if (item.masteryLevel !== undefined && item.masteryLevel !== null) {
    return clampPercentage((Number(item.masteryLevel) / 3) * 100)
  }

  return 0
}

const hydrateKnowledgeNames = async (items) => {
  const missingIds = [
    ...new Set(
      items
        .filter((item) => !item.knowledgePoint?.name && !item.knowledgeName && item.knowledgeId)
        .map((item) => item.knowledgeId)
    )
  ]

  if (missingIds.length === 0) {
    return items
  }

  const results = await Promise.allSettled(missingIds.map((id) => getNodeDetail(id)))
  const nameMap = {}

  results.forEach((result, index) => {
    if (result.status === 'fulfilled' && result.value?.code === 200) {
      nameMap[missingIds[index]] = result.value.data?.name
    }
  })

  return items.map((item) => ({
    ...item,
    knowledgeName: item.knowledgePoint?.name || item.knowledgeName || nameMap[item.knowledgeId] || `知识点 ${item.knowledgeId}`
  }))
}

const knowledgeItems = computed(() =>
  knowledgeMastery.value
    .map((item) => {
      const practiceCount = Number(item.practiceCount) || 0
      const correctCount = Number(item.correctCount) || 0

      return {
        ...item,
        knowledgeName: item.knowledgePoint?.name || item.knowledgeName || `知识点 ${item.knowledgeId}`,
        masteryPercent: normalizeMasteryPercent(item),
        practiceCount,
        correctCount,
        accuracy: practiceCount > 0 ? clampPercentage((correctCount / practiceCount) * 100) : 0
      }
    })
    .sort((a, b) => b.masteryPercent - a.masteryPercent)
)

const weakPointItems = computed(() =>
  weakPoints.value
    .map((item) => ({
      ...item,
      knowledgeId: item.knowledgeId || item.id,
      knowledgeName: item.knowledgeName || item.name || `知识点 ${item.knowledgeId || item.id}`,
      masteryPercent: normalizeMasteryPercent(item)
    }))
    .sort((a, b) => a.masteryPercent - b.masteryPercent)
)

const fetchData = async () => {
  if (!userId.value) {
    return
  }

  try {
    const [pathsRes, masteryRes, weakRes] = await Promise.all([
      getActivePaths(userId.value),
      getKnowledgeMastery(),
      getWeakKnowledgePoints()
    ])

    activePaths.value = pathsRes?.code === 200 ? pathsRes.data || [] : []
    knowledgeMastery.value = masteryRes?.code === 200
      ? await hydrateKnowledgeNames(masteryRes.data || [])
      : []
    weakPoints.value = weakRes?.code === 200 ? weakRes.data || [] : []
  } catch (error) {
    console.error('获取学习进度数据失败:', error)
  }
}

const getLanguageType = (language) => {
  const typeMap = {
    JAVA: 'danger',
    PYTHON: 'success',
    CPP: 'primary',
    JAVASCRIPT: 'warning'
  }

  return typeMap[language] || 'info'
}

const getLanguageLabel = (language) => {
  const labelMap = {
    JAVA: 'Java',
    PYTHON: 'Python',
    CPP: 'C++',
    JAVASCRIPT: 'JavaScript'
  }

  return labelMap[language] || language || ''
}

const getDirectionLabel = (direction) => {
  const labelMap = {
    FRONTEND: '前端',
    BACKEND: '后端',
    ALGORITHM: '算法',
    FULLSTACK: '全栈'
  }

  return labelMap[direction] || direction || ''
}

const getProgressColor = (progress) => {
  if (progress >= 80) return '#67c23a'
  if (progress >= 50) return '#e6a23c'
  return '#409eff'
}

const getMasteryTagType = (masteryPercent) => {
  if (masteryPercent >= 80) return 'success'
  if (masteryPercent >= 60) return 'warning'
  return 'danger'
}

const getMasteryLabel = (masteryPercent) => {
  if (masteryPercent >= 80) return '熟练'
  if (masteryPercent >= 60) return '良好'
  if (masteryPercent >= 40) return '一般'
  return '薄弱'
}

const getMasteryColor = (masteryPercent) => {
  if (masteryPercent >= 80) return '#67c23a'
  if (masteryPercent >= 60) return '#e6a23c'
  return '#f56c6c'
}

const continuePath = (pathId) => {
  router.push(`/dashboard/learn/path/${pathId}`)
}

const viewPathDetail = (pathId) => {
  router.push(`/dashboard/learn/path/${pathId}`)
}

const goToLearn = () => {
  router.push('/dashboard/learn')
}

const viewKnowledgeGraph = () => {
  router.push('/dashboard/learn/knowledge-graph')
}

const practiceWeakPoint = (point) => {
  if (point.knowledgeName) {
    router.push(`/dashboard/problems?knowledge=${encodeURIComponent(point.knowledgeName)}`)
    return
  }

  router.push('/dashboard/problems')
}

watch(
  userId,
  (id) => {
    if (id) {
      fetchData()
    }
  },
  { immediate: true }
)
</script>

<style scoped>
.progress-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.section-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.view-all-link {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #409eff;
  text-decoration: none;
  font-size: 14px;
}

.subtitle {
  font-size: 13px;
  color: #909399;
}

.paths-list,
.knowledge-list,
.weak-points-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.path-item,
.knowledge-item {
  padding: 20px;
  border-radius: 10px;
  background: #f5f7fa;
}

.path-header,
.knowledge-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.path-badges,
.knowledge-info {
  display: flex;
  gap: 8px;
  align-items: center;
}

.path-name,
.knowledge-name {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.path-description {
  margin: 0 0 16px;
  color: #606266;
  line-height: 1.6;
}

.path-progress-text,
.knowledge-percent {
  font-size: 18px;
  font-weight: 600;
  color: #409eff;
}

.progress-meta,
.knowledge-stats {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-top: 10px;
  font-size: 12px;
  color: #909399;
  flex-wrap: wrap;
}

.path-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
}

.weak-point-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-radius: 10px;
  background: #fdf6ec;
  cursor: pointer;
  transition: background 0.2s ease;
}

.weak-point-item:hover {
  background: #f5dab1;
}

.weak-point-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.weak-point-name {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
}

.weak-point-level {
  font-size: 13px;
  color: #e6a23c;
}

@media (max-width: 768px) {
  .path-actions {
    flex-direction: column;
  }
}
</style>
