<template>
  <div class="level-detail-page">
    <div class="detail-container">
      <div class="page-header">
        <div class="header-left">
          <el-button text @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            返回路径
          </el-button>
          <div v-if="currentLevel" class="level-info">
            <h1 class="page-title">{{ currentLevel.name }}</h1>
            <p class="page-subtitle">
              {{ chapterName || '当前关卡' }} · {{ levelSummary }}
            </p>
          </div>
        </div>
        <el-tag v-if="statusText" :type="statusTagType" size="large">{{ statusText }}</el-tag>
      </div>

      <div class="summary-card" v-if="currentLevel">
        <div class="summary-grid">
          <div class="summary-item">
            <span class="summary-label">关卡序号</span>
            <span class="summary-value">第 {{ currentLevel.orderNum || currentLevel.order || '-' }} 关</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">题目数量</span>
            <span class="summary-value">{{ problems.length }} 题</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">学习资源</span>
            <span class="summary-value">{{ resources.length }} 个</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">知识点</span>
            <span class="summary-value">{{ knowledgePointSummary }}</span>
          </div>
        </div>

        <div class="summary-actions">
          <el-button @click="goBack">返回路径</el-button>
          <el-button
            v-if="canCompleteLevel"
            type="primary"
            :loading="completing"
            @click="handleCompleteLevel"
          >
            标记本关完成
          </el-button>
        </div>
      </div>

      <div class="agent-recommendation-card">
        <div class="agent-recommendation-head">
          <div>
            <div class="agent-kicker">Learning Agent</div>
            <h2>Agent 推荐</h2>
          </div>
          <span v-if="agentRecommendation" class="agent-badge">
            {{ agentRecommendation.actionType || 'RECOMMEND' }}
          </span>
        </div>
        <div v-if="agentRecommendationLoading" class="agent-muted">正在生成关卡建议...</div>
        <div v-else-if="agentRecommendation">
          <p class="agent-main">{{ agentRecommendation.mainResponse }}</p>
          <div v-if="agentRecommendation.weakPoints.length" class="agent-tags">
            <span v-for="point in agentRecommendation.weakPoints" :key="point">{{ point }}</span>
          </div>
          <div v-if="agentRecommendation.nextSuggestion" class="agent-next">
            下一步：{{ agentRecommendation.nextSuggestion }}
          </div>
        </div>
        <div v-else class="agent-muted">暂无推荐，先完成当前资源和练习题。</div>
      </div>

      <el-tabs v-model="activeTab" class="content-tabs">
        <el-tab-pane label="学习资源" name="resources">
          <div class="resources-section" v-loading="loadingResources">
            <div v-if="resources.length > 0" class="resources-list">
              <div
                v-for="resource in resources"
                :key="resource.id"
                class="resource-card"
                @click="openResource(resource)"
              >
                <div class="resource-icon">
                  <el-icon v-if="resource.type === 'tutorial'"><Reading /></el-icon>
                  <el-icon v-else-if="resource.type === 'example'"><Document /></el-icon>
                  <el-icon v-else-if="resource.type === 'explanation'"><ChatDotRound /></el-icon>
                  <el-icon v-else><Notebook /></el-icon>
                </div>
                <div class="resource-info">
                  <h4 class="resource-name">{{ resource.name }}</h4>
                  <p class="resource-desc">{{ resource.description || '点击查看资源详情。' }}</p>
                </div>
                <div class="resource-type-tag">
                  {{ getResourceTypeText(resource.type) }}
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无学习资源" />
          </div>
        </el-tab-pane>

        <el-tab-pane label="练习题目" name="problems">
          <div class="problems-section" v-loading="loadingProblems">
            <div v-if="problems.length > 0" class="problems-list">
              <div
                v-for="problem in problems"
                :key="problem.id"
                class="problem-item"
                @click="goToProblem(problem.id)"
              >
                <div class="problem-info">
                  <span class="problem-id">{{ problem.id }}.</span>
                  <div class="problem-title-container">
                    <h4 class="problem-title">{{ problem.title }}</h4>
                    <div class="problem-tags">
                      <el-tag :type="getDifficultyType(problem.difficulty)" size="small">
                        {{ getDifficultyText(problem.difficulty) }}
                      </el-tag>
                      <el-tag
                        v-for="(tag, index) in getKnowledgePoints(problem.knowledgePoints)"
                        :key="index"
                        size="small"
                        type="info"
                      >
                        {{ tag }}
                      </el-tag>
                    </div>
                  </div>
                </div>
                <el-icon class="problem-arrow"><ArrowRight /></el-icon>
              </div>
            </div>
            <el-empty v-else description="暂无练习题目" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog
      v-model="resourceDialogVisible"
      :title="currentResource?.name || '资源详情'"
      width="800px"
      class="resource-dialog"
    >
      <div v-if="currentResource" class="resource-content">
        <div class="markdown-body" v-html="renderedContent"></div>
      </div>
      <template #footer>
        <el-button @click="resourceDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft,
  ArrowRight,
  ChatDotRound,
  Document,
  Notebook,
  Reading
} from '@element-plus/icons-vue'
import {
  completeLevel,
  getLevelProblems,
  getLevelResources,
  getPathDetail,
  getPathProgress
} from '@/api/learn'
import { recommendLearningPathLevel } from '@/api/problemAgent'

const route = useRoute()
const router = useRouter()

const activeTab = ref('resources')
const loadingResources = ref(false)
const loadingProblems = ref(false)
const completing = ref(false)
const resources = ref([])
const problems = ref([])
const currentLevel = ref(null)
const chapterName = ref('')
const currentResource = ref(null)
const resourceDialogVisible = ref(false)
const agentRecommendationLoading = ref(false)
const agentRecommendation = ref(null)
const progress = ref({
  currentLevelId: null,
  completedLevelIds: []
})

const levelId = computed(() => Number(route.params.levelId))
const pathId = computed(() => Number(route.params.pathId))

const renderMarkdown = (content) => {
  if (!content) return ''

  let html = String(content)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/^### (.*$)/gim, '<h3>$1</h3>')
    .replace(/^## (.*$)/gim, '<h2>$1</h2>')
    .replace(/^# (.*$)/gim, '<h1>$1</h1>')
    .replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/\n/g, '<br />')

  return `<div>${html}</div>`
}

const renderedContent = computed(() => renderMarkdown(currentResource.value?.url || currentResource.value?.content || ''))

const completedLevelSet = computed(() => {
  const set = new Set()
  const ids = Array.isArray(progress.value.completedLevelIds) ? progress.value.completedLevelIds : []
  ids.map((id) => Number(id)).filter((id) => !Number.isNaN(id)).forEach((id) => set.add(id))
  return set
})

const isCompleted = computed(() => completedLevelSet.value.has(levelId.value))
const isCurrentLevel = computed(() => Number(progress.value.currentLevelId) === levelId.value)
const canCompleteLevel = computed(() => !!currentLevel.value && !isCompleted.value && isCurrentLevel.value)

const statusText = computed(() => {
  if (isCompleted.value) return '已完成'
  if (isCurrentLevel.value) return '当前关卡'
  return '学习中'
})

const statusTagType = computed(() => {
  if (isCompleted.value) return 'success'
  if (isCurrentLevel.value) return 'primary'
  return 'info'
})

const levelSummary = computed(() => {
  const parts = []
  if (isCompleted.value) {
    parts.push('已完成')
  } else if (isCurrentLevel.value) {
    parts.push('当前正在学习')
  } else {
    parts.push('可查看内容')
  }
  parts.push(`${resources.value.length} 个资源`)
  parts.push(`${problems.value.length} 道题`)
  return parts.join(' · ')
})

const knowledgePointSummary = computed(() => {
  const points = getKnowledgePoints(currentLevel.value?.knowledgePoints)
  return points.length > 0 ? points.join('、') : '未配置'
})

const getResourceTypeText = (type) => {
  const map = {
    tutorial: '教程',
    example: '示例',
    explanation: '知识点',
    document: '文档',
    video: '视频'
  }
  return map[type] || '资源'
}

const getDifficultyType = (difficulty) => {
  const map = { 0: 'success', 1: 'warning', 2: 'danger', 3: 'danger' }
  return map[difficulty] || 'info'
}

const getDifficultyText = (difficulty) => {
  const map = { 0: '简单', 1: '中等', 2: '困难', 3: '困难' }
  return map[difficulty] || '未知'
}

const getKnowledgePoints = (knowledgePoints) => {
  if (!knowledgePoints) return []
  return String(knowledgePoints)
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}

const normalizeAgentDecision = (decision = {}) => ({
  actionType: decision.action_type || decision.actionType || '',
  contentType: decision.content_type || decision.contentType || '',
  mainResponse: decision.main_response || decision.mainResponse || decision.content || '',
  nextSuggestion: decision.next_suggestion || decision.nextSuggestion || decision.suggested_next_action || '',
  weakPoints: Array.isArray(decision.weak_points || decision.weakPoints)
    ? decision.weak_points || decision.weakPoints
    : []
})

const fetchAgentRecommendation = async () => {
  if (!pathId.value || !levelId.value) return
  agentRecommendationLoading.value = true
  try {
    const res = await recommendLearningPathLevel(pathId.value, levelId.value)
    if (res?.code === 200 && res.data) {
      agentRecommendation.value = normalizeAgentDecision(res.data)
      return
    }
    agentRecommendation.value = null
  } catch (error) {
    console.warn('Agent learning path recommendation failed:', error)
    agentRecommendation.value = null
  } finally {
    agentRecommendationLoading.value = false
  }
}

const fetchLevelData = async () => {
  if (!pathId.value || !levelId.value) {
    ElMessage.error('缺少必要参数')
    return
  }

  try {
    const [pathRes, progressRes] = await Promise.all([
      getPathDetail(pathId.value),
      getPathProgress(pathId.value)
    ])

    if (progressRes?.code === 200 && progressRes.data) {
      progress.value = {
        currentLevelId: progressRes.data.currentLevelId ? Number(progressRes.data.currentLevelId) : null,
        completedLevelIds: Array.isArray(progressRes.data.completedLevelIds) ? progressRes.data.completedLevelIds : []
      }
    } else {
      progress.value = { currentLevelId: null, completedLevelIds: [] }
    }

    if (pathRes?.code === 200 && pathRes.data) {
      const chapters = pathRes.data.chapters || []
      let foundLevel = null
      let foundChapterName = ''

      for (const chapter of chapters) {
        const targetLevel = (chapter.levels || []).find((level) => Number(level.id) === levelId.value)
        if (targetLevel) {
          foundLevel = targetLevel
          foundChapterName = chapter.name || ''
          break
        }
      }

      currentLevel.value = foundLevel
      chapterName.value = foundChapterName

      if (!foundLevel) {
        ElMessage.warning('未找到对应关卡')
      }
    }
  } catch (error) {
    console.error('获取关卡信息失败:', error)
    ElMessage.error('获取关卡信息失败')
  }
}

const fetchResources = async () => {
  loadingResources.value = true
  try {
    const res = await getLevelResources(levelId.value)
    if (res?.code === 200) {
      resources.value = Array.isArray(res.data) ? res.data : []
    } else {
      resources.value = []
    }
  } catch (error) {
    console.error('获取学习资源失败:', error)
    ElMessage.error('获取学习资源失败')
  } finally {
    loadingResources.value = false
  }
}

const fetchProblems = async () => {
  loadingProblems.value = true
  try {
    const res = await getLevelProblems(levelId.value)
    if (res?.code === 200) {
      problems.value = Array.isArray(res.data) ? res.data : []
    } else {
      problems.value = []
    }
  } catch (error) {
    console.error('获取练习题失败:', error)
    ElMessage.error('获取练习题失败')
  } finally {
    loadingProblems.value = false
  }
}

const openResource = (resource) => {
  currentResource.value = resource
  resourceDialogVisible.value = true
}

const goToProblem = (problemId) => {
  router.push({ name: 'ProblemDetail', params: { id: problemId } })
}

const handleCompleteLevel = async () => {
  if (!canCompleteLevel.value) {
    ElMessage.warning('当前关卡还不能标记完成')
    return
  }

  completing.value = true
  try {
    const res = await completeLevel(levelId.value)
    if (res?.code !== 200 || res?.data !== true) {
      ElMessage.error(res?.msg || '标记完成失败')
      return
    }

    ElMessage.success('已完成当前关卡')
    router.push({ name: 'LearningPath', params: { id: pathId.value } })
  } catch (error) {
    console.error('标记关卡完成失败:', error)
    ElMessage.error('标记完成失败')
  } finally {
    completing.value = false
  }
}

const goBack = () => {
  if (pathId.value) {
    router.push({ name: 'LearningPath', params: { id: pathId.value } })
  } else {
    router.push({ name: 'Learn' })
  }
}

const loadPage = async () => {
  agentRecommendation.value = null
  agentRecommendationLoading.value = false
  await Promise.all([fetchLevelData(), fetchResources(), fetchProblems()])
  fetchAgentRecommendation()
}

watch(
  () => [pathId.value, levelId.value],
  () => {
    loadPage()
  }
)

onMounted(() => {
  loadPage()
})
</script>

<style scoped>
.level-detail-page {
  width: 100%;
  min-height: 100vh;
  background: var(--leetcode-bg-secondary, #f7f8fa);
  padding: 24px;
  box-sizing: border-box;
}

.detail-container {
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.level-info {
  flex: 1;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--leetcode-text, #24292f);
  margin: 0 0 8px;
}

.page-subtitle {
  font-size: 14px;
  color: var(--leetcode-text-secondary, #6b7280);
  margin: 0;
}

.summary-card {
  margin-bottom: 20px;
  padding: 20px;
  border-radius: 12px;
  border: 1px solid var(--leetcode-border, #e5e7eb);
  background: var(--leetcode-bg, #fff);
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.summary-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.summary-label {
  font-size: 12px;
  color: var(--leetcode-text-secondary, #6b7280);
}

.summary-value {
  font-size: 15px;
  font-weight: 600;
  color: var(--leetcode-text, #24292f);
}

.summary-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}

.agent-recommendation-card {
  margin-bottom: 20px;
  padding: 18px;
  border: 1px solid var(--leetcode-border, #e5e7eb);
  border-radius: 8px;
  background: var(--leetcode-bg, #fff);
}

.agent-recommendation-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.agent-kicker {
  margin-bottom: 4px;
  color: var(--leetcode-text-secondary, #6b7280);
  font-size: 12px;
  font-weight: 700;
}

.agent-recommendation-head h2 {
  margin: 0;
  font-size: 18px;
  color: var(--leetcode-text, #24292f);
}

.agent-badge,
.agent-tags span {
  align-self: flex-start;
  padding: 3px 8px;
  border-radius: 999px;
  background: #eef6ff;
  color: #2563eb;
  font-size: 12px;
  font-weight: 700;
}

.agent-main {
  margin: 0 0 12px;
  color: var(--leetcode-text, #24292f);
  line-height: 1.7;
  white-space: pre-wrap;
}

.agent-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.agent-next,
.agent-muted {
  color: var(--leetcode-text-secondary, #6b7280);
  line-height: 1.6;
}

.content-tabs {
  background: var(--leetcode-bg, #fff);
  border-radius: 12px;
  padding: 0 24px 24px;
  border: 1px solid var(--leetcode-border, #e5e7eb);
}

.content-tabs :deep(.el-tabs__header) {
  margin: 0 -24px 24px;
  padding: 0 24px;
}

.resources-section,
.problems-section {
  min-height: 300px;
}

.resources-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.resource-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: var(--leetcode-bg-secondary, #f7f8fa);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;
}

.resource-card:hover {
  background: #fff;
  border-color: var(--leetcode-primary, #0066ff);
}

.resource-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  background: var(--leetcode-primary, #0066ff);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.resource-info {
  flex: 1;
}

.resource-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--leetcode-text, #24292f);
  margin: 0 0 4px;
}

.resource-desc {
  font-size: 13px;
  color: var(--leetcode-text-secondary, #6b7280);
  margin: 0;
}

.resource-type-tag {
  padding: 4px 12px;
  background: var(--leetcode-bg, #fff);
  border: 1px solid var(--leetcode-border, #e5e7eb);
  border-radius: 12px;
  font-size: 12px;
  color: var(--leetcode-text-secondary, #6b7280);
  flex-shrink: 0;
}

.problems-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.problem-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: var(--leetcode-bg-secondary, #f7f8fa);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;
}

.problem-item:hover {
  background: #fff;
  border-color: var(--leetcode-primary, #0066ff);
}

.problem-info {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  flex: 1;
}

.problem-id {
  font-size: 14px;
  color: var(--leetcode-text-secondary, #6b7280);
  font-weight: 500;
  margin-top: 2px;
}

.problem-title-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}

.problem-title {
  font-size: 15px;
  font-weight: 500;
  color: var(--leetcode-text, #24292f);
  margin: 0;
}

.problem-tags {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.problem-arrow {
  color: var(--leetcode-text-secondary, #6b7280);
  font-size: 18px;
}

.resource-dialog :deep(.el-dialog__body) {
  max-height: 600px;
  overflow-y: auto;
}

.resource-content {
  padding: 0 10px;
}

.markdown-body {
  line-height: 1.8;
  color: var(--leetcode-text, #24292f);
}

.markdown-body :deep(h1) {
  font-size: 24px;
  margin-bottom: 16px;
}

.markdown-body :deep(h2) {
  font-size: 20px;
  margin: 20px 0 12px;
}

.markdown-body :deep(h3) {
  font-size: 18px;
  margin: 18px 0 10px;
}

.markdown-body :deep(pre) {
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
}

@media (max-width: 768px) {
  .level-detail-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
  }

  .summary-grid {
    grid-template-columns: 1fr 1fr;
  }

  .resource-card,
  .problem-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .summary-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .problem-arrow {
    display: none;
  }
}
</style>
