<template>
  <div v-loading="analysisLoading" class="analysis-page">
    <section class="analysis-grid">
      <div class="panel-card weak-panel">
        <div class="panel-header">
          <div>
            <div class="section-kicker">Weak Points</div>
            <h2>薄弱知识点</h2>
          </div>
          <router-link to="/wrong-book" class="panel-link">去错题本</router-link>
        </div>

        <div v-if="weakPointItems.length > 0" class="weak-list">
          <div v-for="(item, index) in weakPointItems" :key="item.knowledgeId" class="weak-item">
            <div class="weak-rank">TOP {{ index + 1 }}</div>
            <div class="weak-main">
              <div class="weak-name">{{ item.knowledgeName }}</div>
              <div class="weak-desc">掌握度 {{ item.masteryPercent }}%，建议优先复习</div>
            </div>
            <el-progress :percentage="item.masteryPercent" :show-text="false" :stroke-width="8" status="exception" />
          </div>
        </div>
        <div v-else class="empty-box">暂无薄弱知识点数据，继续做题后这里会逐步形成分析。</div>
      </div>

      <div class="panel-card">
        <div class="panel-header">
          <div>
            <div class="section-kicker">Difficulty Mix</div>
            <h2>难度分布</h2>
          </div>
        </div>

        <div class="difficulty-list">
          <div v-for="item in difficultyItems" :key="item.key" class="difficulty-item">
            <div class="difficulty-head">
              <span>{{ item.label }}</span>
              <span>{{ item.value }}</span>
            </div>
            <el-progress :percentage="item.percent" :show-text="false" :stroke-width="10" :color="item.color" />
          </div>
        </div>
      </div>
    </section>

    <section class="panel-card mastery-panel">
      <div class="panel-header">
        <div>
          <div class="section-kicker">Knowledge Mastery</div>
          <h2>知识掌握度</h2>
        </div>
      </div>

      <div v-if="knowledgeItems.length > 0" class="mastery-list">
        <div v-for="item in knowledgeItems" :key="item.knowledgeId" class="mastery-item">
          <div class="mastery-main">
            <div>
              <div class="mastery-name">{{ item.knowledgeName }}</div>
              <div class="mastery-meta">
                练习 {{ item.practiceCount }} 次，正确 {{ item.correctCount }} 次，正确率 {{ item.accuracy }}%
              </div>
            </div>
            <el-tag :type="getMasteryTagType(item.masteryPercent)" size="small">
              {{ getMasteryLabel(item.masteryPercent) }}
            </el-tag>
          </div>
          <el-progress
            :percentage="item.masteryPercent"
            :show-text="false"
            :stroke-width="8"
            :color="getMasteryColor(item.masteryPercent)"
          />
        </div>
      </div>
      <div v-else class="empty-box">暂无知识掌握度数据，先完成几道题后再回来查看。</div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { getDifficultyStats, getKnowledgeMastery, getWeakKnowledgePoints } from '@/api/learn'
import { getNodeDetail } from '@/api/knowledgeGraph'

const userStore = useUserStore()
const analysisLoading = ref(false)
const userId = computed(() => userStore.userInfo?.id)

const difficultyStats = ref({
  easy: 0,
  medium: 0,
  hard: 0
})
const knowledgeMastery = ref([])
const weakPoints = ref([])

const clampPercentage = (value) => Math.min(100, Math.max(0, Math.round(Number(value) || 0)))

const hydrateKnowledgeNames = async (items) => {
  const missingIds = [
    ...new Set(
      items
        .filter((item) => !item.knowledgePoint?.name && !item.knowledgeName && (item.knowledgeId || item.id))
        .map((item) => item.knowledgeId || item.id)
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
    knowledgeName:
      item.knowledgePoint?.name ||
      item.knowledgeName ||
      item.name ||
      nameMap[item.knowledgeId || item.id] ||
      `知识点 ${item.knowledgeId || item.id}`
  }))
}

const normalizeMasteryPercent = (item) => {
  if (item.score !== undefined && item.score !== null) {
    return clampPercentage(item.score)
  }
  if (item.masteryLevel !== undefined && item.masteryLevel !== null) {
    return clampPercentage((Number(item.masteryLevel) / 3) * 100)
  }
  return 0
}

const knowledgeItems = computed(() =>
  knowledgeMastery.value
    .map((item) => {
      const practiceCount = Number(item.practiceCount) || 0
      const correctCount = Number(item.correctCount) || 0

      return {
        ...item,
        knowledgeId: item.knowledgeId || item.id,
        knowledgeName:
          item.knowledgePoint?.name ||
          item.knowledgeName ||
          item.name ||
          `知识点 ${item.knowledgeId || item.id}`,
        masteryPercent: normalizeMasteryPercent(item),
        practiceCount,
        correctCount,
        accuracy: practiceCount > 0 ? clampPercentage((correctCount / practiceCount) * 100) : 0
      }
    })
    .sort((a, b) => a.masteryPercent - b.masteryPercent)
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

const difficultyItems = computed(() => {
  const raw = difficultyStats.value?.difficultyStats || difficultyStats.value || {}
  const easy = Number(raw.easy || raw.EASY || 0)
  const medium = Number(raw.medium || raw.MEDIUM || 0)
  const hard = Number(raw.hard || raw.HARD || 0)
  const total = easy + medium + hard || 1

  return [
    { key: 'easy', label: '简单', value: easy, percent: clampPercentage((easy / total) * 100), color: '#22c55e' },
    { key: 'medium', label: '中等', value: medium, percent: clampPercentage((medium / total) * 100), color: '#f59e0b' },
    { key: 'hard', label: '困难', value: hard, percent: clampPercentage((hard / total) * 100), color: '#f43f5e' }
  ]
})

const getMasteryLabel = (value) => {
  if (value >= 80) return '熟练'
  if (value >= 50) return '掌握中'
  return '待加强'
}

const getMasteryTagType = (value) => {
  if (value >= 80) return 'success'
  if (value >= 50) return 'warning'
  return 'danger'
}

const getMasteryColor = (value) => {
  if (value >= 80) return '#22c55e'
  if (value >= 50) return '#f59e0b'
  return '#f43f5e'
}

const ensureUserReady = async () => {
  if (userStore.userInfo?.id) {
    return userStore.userInfo.id
  }

  if (!userStore.token) {
    return null
  }

  try {
    await userStore.fetchUserInfo()
    return userStore.userInfo?.id || null
  } catch (error) {
    console.error('加载当前用户信息失败:', error)
    return null
  }
}

const fetchData = async () => {
  const currentUserId = await ensureUserReady()
  if (!currentUserId) {
    return
  }

  analysisLoading.value = true

  try {
    const [difficultyRes, masteryRes, weakRes] = await Promise.allSettled([
      getDifficultyStats(currentUserId),
      getKnowledgeMastery(),
      getWeakKnowledgePoints()
    ])

    difficultyStats.value =
      difficultyRes.status === 'fulfilled' && difficultyRes.value?.code === 200
        ? difficultyRes.value.data || {}
        : {}

    knowledgeMastery.value =
      masteryRes.status === 'fulfilled' && masteryRes.value?.code === 200
        ? await hydrateKnowledgeNames(masteryRes.value.data || [])
        : []

    weakPoints.value =
      weakRes.status === 'fulfilled' && weakRes.value?.code === 200
        ? await hydrateKnowledgeNames(weakRes.value.data || [])
        : []
  } catch (error) {
    console.error('获取学习分析数据失败:', error)
  } finally {
    analysisLoading.value = false
  }
}

watch(
  userId,
  (value) => {
    if (value) {
      fetchData()
    }
  },
  { immediate: true }
)

watch(
  () => userStore.token,
  (token) => {
    if (token && !userStore.userInfo?.id) {
      fetchData()
    }
  }
)

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.analysis-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.analysis-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(300px, 0.95fr);
  gap: 20px;
}

.panel-card {
  position: relative;
  overflow: hidden;
  border: 1px solid var(--border-color);
  border-radius: 22px;
  background: var(--bg-card);
  box-shadow: var(--shadow-md);
  padding: 22px;
}

.weak-panel {
  background:
    radial-gradient(circle at top left, rgba(244, 63, 94, 0.12), transparent 30%),
    var(--bg-card);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.section-kicker {
  margin-bottom: 8px;
  color: var(--brand-accent);
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.panel-header h2 {
  margin: 0;
}

.panel-link {
  color: var(--warning-color);
  font-weight: 700;
}

.weak-list,
.mastery-list,
.difficulty-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.weak-item,
.mastery-item {
  position: relative;
  padding: 16px;
  border: 1px solid var(--border-light);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.03);
}

.weak-item {
  padding-top: 20px;
}

.weak-rank {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 56px;
  margin-bottom: 12px;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(244, 63, 94, 0.14);
  color: #ff9aae;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.weak-name,
.mastery-name {
  color: var(--text-primary);
  font-weight: 700;
}

.weak-desc,
.mastery-meta {
  margin: 6px 0 12px;
  color: var(--text-secondary);
  line-height: 1.7;
}

.difficulty-item {
  padding: 16px;
  border: 1px solid var(--border-light);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.03);
  transition:
    transform var(--transition-fast),
    border-color var(--transition-fast),
    background var(--transition-fast);
}

.difficulty-item:hover,
.mastery-item:hover,
.weak-item:hover {
  transform: translateY(-1px);
  border-color: var(--border-strong);
  background: rgba(255, 255, 255, 0.05);
}

.difficulty-head,
.mastery-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.empty-box {
  padding: 20px;
  border: 1px dashed var(--border-color);
  border-radius: 16px;
  text-align: center;
  color: var(--text-secondary);
}

@media (max-width: 980px) {
  .analysis-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .difficulty-head,
  .mastery-main {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
