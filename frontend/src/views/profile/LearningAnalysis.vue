<template>
  <div v-loading="analysisLoading" class="analysis-page">
    <section class="summary-grid">
      <article class="summary-card">
        <div class="summary-label">掌握知识点</div>
        <div class="summary-value">{{ knowledgeItems.length }}</div>
        <div class="summary-hint">已纳入分析的知识点数量</div>
      </article>
      <article class="summary-card warning">
        <div class="summary-label">薄弱点</div>
        <div class="summary-value">{{ weakPointItems.length }}</div>
        <div class="summary-hint">{{ weakestPointLabel }}</div>
      </article>
      <article class="summary-card success">
        <div class="summary-label">累计练习</div>
        <div class="summary-value">{{ totalPracticeCount }}</div>
        <div class="summary-hint">来自知识掌握度统计</div>
      </article>
      <article class="summary-card accent">
        <div class="summary-label">整体通过率</div>
        <div class="summary-value">{{ overallAccuracy }}%</div>
        <div class="summary-hint">按知识点练习数据估算</div>
      </article>
    </section>

    <section class="panel-card agent-summary-panel">
      <div class="panel-header">
        <div>
          <div class="section-kicker">Learning Agent</div>
          <h2>近期学习洞察</h2>
        </div>
        <span class="panel-caption">看答案 {{ agentRevealCount }} 次</span>
      </div>

      <div v-if="agentSummary" class="agent-summary-grid">
        <div class="agent-summary-block">
          <div class="agent-block-title">最近需要的帮助</div>
          <div v-if="agentActionItems.length" class="agent-action-list">
            <div v-for="item in agentActionItems" :key="item.action" class="agent-action-item">
              <span>{{ item.label }}</span>
              <strong>{{ item.count }}</strong>
            </div>
          </div>
          <div v-else class="empty-copy">近期还没有 Agent 动作记录。</div>
        </div>

        <div class="agent-summary-block">
          <div class="agent-block-title">近期薄弱点</div>
          <div v-if="agentWeakPoints.length" class="agent-tag-list">
            <span v-for="point in agentWeakPoints" :key="point">{{ point }}</span>
          </div>
          <div v-else class="empty-copy">近期没有新的薄弱点标签。</div>
        </div>

        <div class="agent-summary-block">
          <div class="agent-block-title">反馈提示</div>
          <div class="agent-feedback-copy">
            {{ agentSummary.feedback_hint || agentSummary.feedbackHint || '继续观察 Agent 建议是否被采纳。' }}
          </div>
        </div>

        <div class="agent-summary-block">
          <div class="agent-block-title">最近学习入口</div>
          <div v-if="agentRecentEvents.length" class="agent-event-list">
            <div
              v-for="(event, index) in agentRecentEvents"
              :key="event.request_id || event.requestId || index"
              class="agent-event-item"
            >
              <span>{{ getAgentActionLabel(event.action_type || event.actionType) }}</span>
              <small>{{ getAgentTriggerLabel(event.trigger_source || event.triggerSource) }}</small>
            </div>
          </div>
          <div v-else class="empty-copy">暂无最新事件。</div>
        </div>
      </div>
      <div v-else class="empty-box compact">
        <div>暂无 Agent 学习摘要。</div>
        <div class="empty-copy">完成题目陪练、错题复盘或学习路径推荐后，这里会出现近期动作和薄弱点。</div>
      </div>
    </section>

    <section class="analysis-grid">
      <div class="panel-card weak-panel">
        <div class="panel-header">
          <div>
            <div class="section-kicker">Weak Points</div>
            <h2>薄弱知识点</h2>
          </div>
          <div class="panel-actions">
            <el-button text @click="refreshAnalysis">刷新</el-button>
            <router-link to="/wrong-book" class="panel-link">去错题本</router-link>
          </div>
        </div>

        <div v-if="weakPointItems.length > 0" class="weak-list">
          <div v-for="(item, index) in weakPointItems" :key="item.knowledgeId" class="weak-item">
            <div class="weak-rank">TOP {{ index + 1 }}</div>
            <div class="weak-main">
              <div class="weak-name">{{ item.knowledgeName }}</div>
              <div class="weak-desc">掌握度 {{ item.masteryPercent }}%，建议优先安排复习和专项练习。</div>
            </div>
            <el-progress :percentage="item.masteryPercent" :show-text="false" :stroke-width="8" status="exception" />
          </div>
        </div>
        <div v-else class="empty-box">
          <div>暂时还没有识别出明显的薄弱点。</div>
          <div class="empty-copy">继续刷题后，这里会逐渐形成更有针对性的学习建议。</div>
          <div class="empty-actions">
            <el-button type="primary" @click="goToProblems">去做题</el-button>
            <el-button @click="goToWrongBook">查看错题本</el-button>
          </div>
        </div>
      </div>

      <div class="panel-card">
        <div class="panel-header">
          <div>
            <div class="section-kicker">Difficulty Mix</div>
            <h2>难度分布</h2>
          </div>
          <span class="panel-caption">共 {{ difficultyTotal }} 道题</span>
        </div>

        <div v-if="difficultyTotal > 0" class="difficulty-list">
          <div v-for="item in difficultyItems" :key="item.key" class="difficulty-item">
            <div class="difficulty-head">
              <span>{{ item.label }}</span>
              <span>{{ item.value }}</span>
            </div>
            <el-progress :percentage="item.percent" :show-text="false" :stroke-width="10" :color="item.color" />
          </div>
        </div>
        <div v-else class="empty-box compact">
          <div>还没有足够的题目记录来生成难度分布。</div>
          <div class="empty-copy">先完成几次提交，分析页会更快变得有参考价值。</div>
        </div>
      </div>
    </section>

    <section class="panel-card mastery-panel">
      <div class="panel-header">
        <div>
          <div class="section-kicker">Knowledge Mastery</div>
          <h2>知识掌握度</h2>
        </div>
        <span class="panel-caption">按掌握度从低到高排序</span>
      </div>

      <div v-if="knowledgeItems.length > 0" class="mastery-list">
        <div v-for="item in knowledgeItems" :key="item.knowledgeId" class="mastery-item">
          <div class="mastery-main">
            <div>
              <div class="mastery-name">{{ item.knowledgeName }}</div>
              <div class="mastery-meta">
                练习 {{ item.practiceCount }} 次，答对 {{ item.correctCount }} 次，正确率 {{ item.accuracy }}%
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
      <div v-else class="empty-box">
        <div>暂无知识掌握度数据。</div>
        <div class="empty-copy">先完成几道题，再回来看看哪些知识点已经稳定、哪些还需要补强。</div>
        <div class="empty-actions">
          <el-button type="primary" @click="goToLearn">去学习中心</el-button>
          <el-button @click="goToProblems">去题库</el-button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getDifficultyStats, getKnowledgeMastery, getWeakKnowledgePoints } from '@/api/learn'
import { getNodeDetail } from '@/api/knowledgeGraph'
import { getAgentReportSummary } from '@/api/problemAgent'

const router = useRouter()
const userStore = useUserStore()

const analysisLoading = ref(false)
const userId = computed(() => userStore.userInfo?.id)
const loadedUserId = ref(null)

const difficultyStats = ref({
  easy: 0,
  medium: 0,
  hard: 0
})
const knowledgeMastery = ref([])
const weakPoints = ref([])
const agentSummary = ref(null)

const clampPercentage = (value) => Math.min(100, Math.max(0, Math.round(Number(value) || 0)))

const normalizeList = (data) => {
  if (Array.isArray(data)) {
    return data
  }

  return data?.list || data?.records || data?.content || data?.items || []
}

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
    const rawLevel = Number(item.masteryLevel)
    const normalized = rawLevel > 1 ? (rawLevel / 3) * 100 : rawLevel * 100
    return clampPercentage(normalized)
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
      knowledgeName:
        item.knowledgePoint?.name || item.knowledgeName || item.name || `知识点 ${item.knowledgeId || item.id}`,
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

const difficultyTotal = computed(() =>
  difficultyItems.value.reduce((total, item) => total + Number(item.value || 0), 0)
)

const totalPracticeCount = computed(() =>
  knowledgeItems.value.reduce((total, item) => total + Number(item.practiceCount || 0), 0)
)

const overallAccuracy = computed(() => {
  const totalCorrect = knowledgeItems.value.reduce((total, item) => total + Number(item.correctCount || 0), 0)
  return totalPracticeCount.value > 0 ? clampPercentage((totalCorrect / totalPracticeCount.value) * 100) : 0
})

const weakestPointLabel = computed(() => {
  if (!weakPointItems.value.length) {
    return '暂时还没有需要重点关注的知识点'
  }

  const weakest = weakPointItems.value[0]
  return `${weakest.knowledgeName} 当前掌握度 ${weakest.masteryPercent}%`
})

const agentActionItems = computed(() => {
  const counts = agentSummary.value?.action_counts || agentSummary.value?.actionCounts || {}
  return Object.entries(counts)
    .map(([action, count]) => ({
      action,
      label: getAgentActionLabel(action),
      count: Number(count || 0)
    }))
    .sort((a, b) => b.count - a.count)
})

const agentWeakPoints = computed(() => {
  const points = agentSummary.value?.weak_points || agentSummary.value?.weakPoints || []
  return Array.isArray(points) ? points : []
})

const agentRecentEvents = computed(() => {
  const events = agentSummary.value?.recent_events || agentSummary.value?.recentEvents || []
  return Array.isArray(events) ? events.slice(0, 5) : []
})

const agentRevealCount = computed(() =>
  Number(agentSummary.value?.revealed_solution_count || agentSummary.value?.revealedSolutionCount || 0)
)

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

const getAgentActionLabel = (action) => {
  const labels = {
    GUIDE_IDEA: '思路引导',
    HINT: '轻提示',
    DIAGNOSE: '错误诊断',
    EXPLAIN: '知识讲解',
    RECOMMEND: '路径推荐',
    REFLECT: '错题复盘',
    REVEAL_ANSWER: '参考修正版'
  }
  return labels[action] || '学习建议'
}

const getAgentTriggerLabel = (trigger) => {
  const labels = {
    PROBLEM_PAGE_CHAT: '题目页陪练',
    RUN_RESULT: '运行后分析',
    SUBMISSION_RESULT: '提交后分析',
    WRONG_BOOK_ENTRY: '错题本复盘',
    LEARNING_PATH_ENTRY: '学习路径推荐',
    MANUAL_HELP_REQUEST: '主动求助'
  }
  return labels[trigger] || '学习记录'
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

const fetchData = async ({ force = false } = {}) => {
  const currentUserId = await ensureUserReady()
  if (!currentUserId) {
    return
  }

  if (analysisLoading.value) {
    return
  }

  if (!force && loadedUserId.value === currentUserId && knowledgeMastery.value.length > 0) {
    return
  }

  analysisLoading.value = true

  try {
    const [difficultyRes, masteryRes, weakRes, agentRes] = await Promise.allSettled([
      getDifficultyStats(currentUserId),
      getKnowledgeMastery(),
      getWeakKnowledgePoints(),
      getAgentReportSummary()
    ])

    difficultyStats.value =
      difficultyRes.status === 'fulfilled' && difficultyRes.value?.code === 200
        ? difficultyRes.value.data || {}
        : {}

    const masteryList =
      masteryRes.status === 'fulfilled' && masteryRes.value?.code === 200
        ? normalizeList(masteryRes.value.data)
        : []

    const weakList =
      weakRes.status === 'fulfilled' && weakRes.value?.code === 200
        ? normalizeList(weakRes.value.data)
        : []

    knowledgeMastery.value = await hydrateKnowledgeNames(masteryList)
    weakPoints.value = await hydrateKnowledgeNames(weakList)
    agentSummary.value =
      agentRes.status === 'fulfilled' && agentRes.value?.code === 200
        ? agentRes.value.data || null
        : null
    loadedUserId.value = currentUserId
  } catch (error) {
    console.error('获取学习分析数据失败:', error)
    difficultyStats.value = {}
    knowledgeMastery.value = []
    weakPoints.value = []
    agentSummary.value = null
  } finally {
    analysisLoading.value = false
  }
}

const refreshAnalysis = () => {
  fetchData({ force: true })
}

const goToWrongBook = () => {
  router.push('/wrong-book')
}

const goToProblems = () => {
  router.push('/problems')
}

const goToLearn = () => {
  router.push('/learn')
}

watch(
  userId,
  (value, oldValue) => {
    if (value && value !== oldValue) {
      fetchData({ force: true })
    }
  }
)

onMounted(() => {
  fetchData({ force: true })
})
</script>

<style scoped>
.analysis-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.summary-card,
.panel-card {
  position: relative;
  overflow: hidden;
  border: 1px solid var(--border-color);
  border-radius: 22px;
  background: var(--bg-card);
  box-shadow: var(--shadow-md);
}

.summary-card {
  padding: 18px 20px;
  background:
    radial-gradient(circle at top right, rgba(0, 209, 255, 0.08), transparent 32%),
    var(--bg-card);
}

.summary-card.warning {
  background:
    radial-gradient(circle at top right, rgba(244, 63, 94, 0.12), transparent 32%),
    var(--bg-card);
}

.summary-card.success {
  background:
    radial-gradient(circle at top right, rgba(34, 197, 94, 0.12), transparent 32%),
    var(--bg-card);
}

.summary-card.accent {
  background:
    radial-gradient(circle at top right, rgba(14, 165, 233, 0.14), transparent 32%),
    var(--bg-card);
}

.summary-label {
  color: var(--text-secondary);
  font-size: 13px;
}

.summary-value {
  margin: 10px 0 6px;
  color: var(--text-primary);
  font-size: 32px;
  font-weight: 700;
}

.summary-hint {
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.7;
}

.analysis-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(300px, 0.95fr);
  gap: 20px;
}

.panel-card {
  padding: 22px;
}

.weak-panel {
  background:
    radial-gradient(circle at top left, rgba(244, 63, 94, 0.12), transparent 30%),
    var(--bg-card);
}

.mastery-panel {
  background:
    radial-gradient(circle at top right, rgba(0, 209, 255, 0.08), transparent 28%),
    var(--bg-card);
}

.agent-summary-panel {
  border-radius: 8px;
}

.agent-summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.agent-summary-block {
  min-width: 0;
  padding: 14px;
  border: 1px solid var(--border-light);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.03);
}

.agent-block-title {
  margin-bottom: 12px;
  color: var(--text-primary);
  font-weight: 700;
}

.agent-action-list,
.agent-event-list,
.agent-tag-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.agent-action-item,
.agent-event-item {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  color: var(--text-secondary);
  line-height: 1.5;
}

.agent-action-item strong {
  color: var(--text-primary);
}

.agent-tag-list {
  align-items: flex-start;
}

.agent-tag-list span {
  max-width: 100%;
  padding: 4px 9px;
  border-radius: 999px;
  background: rgba(14, 165, 233, 0.12);
  color: var(--brand-accent);
  word-break: break-word;
}

.agent-feedback-copy {
  color: var(--text-secondary);
  line-height: 1.7;
}

.agent-event-item small {
  color: var(--text-muted);
}

.panel-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.panel-actions {
  display: flex;
  align-items: center;
  gap: 12px;
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

.panel-caption {
  color: var(--text-secondary);
  font-size: 13px;
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
.mastery-item,
.difficulty-item {
  position: relative;
  border: 1px solid var(--border-light);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.03);
  transition:
    transform var(--transition-fast),
    border-color var(--transition-fast),
    background var(--transition-fast);
}

.weak-item,
.mastery-item {
  padding: 16px;
}

.weak-item {
  padding-top: 20px;
}

.difficulty-item {
  padding: 16px;
}

.difficulty-item:hover,
.mastery-item:hover,
.weak-item:hover {
  transform: translateY(-1px);
  border-color: var(--border-strong);
  background: rgba(255, 255, 255, 0.05);
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

.difficulty-head,
.mastery-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.empty-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 24px 20px;
  border: 1px dashed var(--border-color);
  border-radius: 16px;
  text-align: center;
  color: var(--text-secondary);
}

.empty-box.compact {
  padding: 28px 20px;
}

.empty-copy {
  max-width: 420px;
  line-height: 1.7;
}

.empty-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
}

@media (max-width: 1180px) {
  .summary-grid,
  .agent-summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .analysis-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .summary-grid,
  .agent-summary-grid {
    grid-template-columns: 1fr;
  }

  .panel-header,
  .difficulty-head,
  .mastery-main,
  .panel-actions {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
