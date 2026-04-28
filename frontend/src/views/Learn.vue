<template>
  <div class="learning-plan-page">
    <div class="learn-container">
      <div class="page-shell">
        <div class="shell-accent"></div>
        <div class="shell-copy">
          <p class="shell-kicker">Learning Center</p>
          <p class="shell-text">
            从路径、进度和题库入口一起推进，比单纯刷题更容易保持连续性。
          </p>
        </div>
      </div>

      <LearningPlanHeader @open-plan="goToMyPlan" />

      <section class="agent-next-step" :class="{ loading: agentLoading }">
        <div class="agent-copy">
          <span class="agent-kicker">Agent 下一步建议</span>
          <strong>{{ agentAdviceTitle }}</strong>
          <p>{{ agentAdviceCopy }}</p>
        </div>
        <div class="agent-meta">
          <span v-if="agentWeakPoints.length">关联薄弱点：{{ agentWeakPoints.slice(0, 3).join('、') }}</span>
          <span v-else>完成更多提交后，推荐会更具体。</span>
          <el-button size="small" @click="goToAnalysis">查看学习分析</el-button>
        </div>
      </section>

      <LearningPathHighlights
        :in-progress-paths="inProgressPaths"
        :featured-paths="featuredPaths"
        @open-path="goToPath"
      />

      <LearningPathCatalog
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        v-model:language-filter="languageFilter"
        v-model:direction-filter="directionFilter"
        :learning-paths="learningPaths"
        :paginated-learning-paths="paginatedLearningPaths"
        :total="total"
        :get-language-label="getLanguageLabel"
        :get-direction-label="getDirectionLabel"
        @filter-change="handleFilterChange"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        @open-path="goToPath"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useLearnPageData } from '@/composables/useLearnPageData'
import { getAgentReportSummary } from '@/api/problemAgent'
import LearningPlanHeader from '@/components/learn/LearningPlanHeader.vue'
import LearningPathHighlights from '@/components/learn/LearningPathHighlights.vue'
import LearningPathCatalog from '@/components/learn/LearningPathCatalog.vue'

const router = useRouter()
const agentLoading = ref(false)
const agentSummary = ref(null)

const {
  currentPage,
  directionFilter,
  featuredPaths,
  getDirectionLabel,
  getLanguageLabel,
  handleCurrentChange,
  handleFilterChange,
  handleSizeChange,
  inProgressPaths,
  languageFilter,
  learningPaths,
  pageSize,
  paginatedLearningPaths,
  total
} = useLearnPageData()

const actionLabels = {
  GUIDE_IDEA: '思路引导',
  HINT: '轻提示',
  DIAGNOSE: '错误诊断',
  EXPLAIN: '知识讲解',
  RECOMMEND: '路径推荐',
  REFLECT: '错题复盘',
  REVEAL_ANSWER: '参考修正版'
}

const agentActionItems = computed(() => {
  const counts = agentSummary.value?.action_counts || agentSummary.value?.actionCounts || {}
  return Object.entries(counts)
    .map(([action, count]) => ({
      action,
      label: actionLabels[action] || '学习建议',
      count: Number(count || 0)
    }))
    .sort((a, b) => b.count - a.count)
})

const agentWeakPoints = computed(() => {
  const points = agentSummary.value?.weak_points || agentSummary.value?.weakPoints || []
  return Array.isArray(points) ? points.filter(Boolean) : []
})

const agentAdviceTitle = computed(() => {
  if (agentWeakPoints.value.length) {
    return `优先补强 ${agentWeakPoints.value[0]}`
  }
  if (agentActionItems.value.length) {
    return `最近更常需要${agentActionItems.value[0].label}`
  }
  return '先完成几次提交，Agent 会生成个性化推荐'
})

const agentAdviceCopy = computed(() => {
  if (agentWeakPoints.value.length) {
    return '建议从同知识点、难度相近的路径节点开始，先把薄弱点练稳定。'
  }
  if (agentActionItems.value.length) {
    return '继续沿当前路径推进，遇到卡点时优先追问思路或失败原因。'
  }
  return '完成题目、提交失败或打开错题复盘后，这里会根据真实学习记录给出下一步。'
})

const loadAgentSummary = async () => {
  agentLoading.value = true
  try {
    const res = await getAgentReportSummary()
    agentSummary.value = res?.code === 200 ? res.data || null : null
  } catch (error) {
    console.warn('加载 Agent 学习建议失败:', error)
    agentSummary.value = null
  } finally {
    agentLoading.value = false
  }
}

const goToPath = (pathId) => {
  router.push(`/learn/path/${pathId}`)
}

const goToMyPlan = () => {
  router.push('/learn/assessment')
}

const goToAnalysis = () => {
  router.push('/profile/analysis')
}

onMounted(loadAgentSummary)
</script>

<style scoped>
.learning-plan-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(0, 102, 255, 0.05), transparent 22%),
    var(--leetcode-bg-secondary, #f7f8fa);
  padding: 24px;
}

.learn-container {
  max-width: 1200px;
  margin: 0 auto;
}

.page-shell {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 20px;
  margin-bottom: 20px;
  border: 1px solid rgba(0, 102, 255, 0.12);
  border-radius: 18px;
  background:
    linear-gradient(135deg, rgba(0, 102, 255, 0.08), rgba(255, 255, 255, 0.92)),
    var(--leetcode-bg, #ffffff);
}

.agent-next-step {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 24px;
  padding: 14px 0;
  border-top: 1px solid var(--leetcode-border, #e5e7eb);
  border-bottom: 1px solid var(--leetcode-border, #e5e7eb);
}

.agent-next-step.loading {
  opacity: 0.72;
}

.agent-copy {
  min-width: 0;
}

.agent-kicker {
  display: block;
  margin-bottom: 4px;
  color: #1668dc;
  font-size: 12px;
  font-weight: 700;
}

.agent-copy strong {
  display: block;
  color: var(--leetcode-text, #24292f);
  font-size: 16px;
}

.agent-copy p {
  margin: 6px 0 0;
  color: var(--leetcode-text-secondary, #6b7280);
  font-size: 13px;
  line-height: 1.6;
}

.agent-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 0 0 auto;
  color: var(--leetcode-text-secondary, #6b7280);
  font-size: 13px;
}

.shell-accent {
  width: 10px;
  align-self: stretch;
  border-radius: 999px;
  background: linear-gradient(180deg, #0066ff, #66b3ff);
}

.shell-copy {
  min-width: 0;
}

.shell-kicker {
  margin: 0 0 6px;
  color: #1668dc;
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.shell-text {
  margin: 0;
  color: var(--leetcode-text-secondary, #6b7280);
  line-height: 1.7;
}

@media (max-width: 768px) {
  .learning-plan-page {
    padding: 16px;
  }

  .page-shell {
    align-items: flex-start;
  }

  .agent-next-step,
  .agent-meta {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
