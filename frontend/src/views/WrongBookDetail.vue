<template>
  <div class="wrong-detail-page">
    <div class="wrong-detail-shell">
      <div class="topbar">
        <el-button @click="goBack">返回错题本</el-button>
        <div class="topbar-actions">
          <el-button v-if="wrongItem?.problemId" @click="openProblem">前往题目</el-button>
          <el-button
            type="primary"
            :disabled="Number(wrongItem?.reviewStatus) >= 1"
            :loading="reviewing"
            @click="markReviewed"
          >
            {{ Number(wrongItem?.reviewStatus) >= 1 ? '已复习' : '标记已复习' }}
          </el-button>
        </div>
      </div>

      <div v-if="loading" class="page-state">正在加载错题详情...</div>
      <el-empty v-else-if="!wrongItem" description="未找到这条错题记录" />

      <div v-else class="detail-layout">
        <main class="detail-main">
          <section class="section-panel">
            <div class="section-head">
              <span class="status-pill" :class="statusClass">{{ statusText }}</span>
              <span>{{ formatDateTime(wrongItem.createTime) }}</span>
            </div>
            <h1>{{ problemTitle }}</h1>
            <p class="problem-content">{{ problemContent }}</p>
          </section>

          <section class="section-panel">
            <h2>错误信息</h2>
            <dl class="info-list">
              <div>
                <dt>错误类型</dt>
                <dd>{{ wrongItem.errorMessage || '未记录' }}</dd>
              </div>
              <div>
                <dt>知识点</dt>
                <dd>{{ wrongItem.knowledgePoints || '未标注' }}</dd>
              </div>
              <div>
                <dt>语言</dt>
                <dd>{{ wrongItem.language || '未记录' }}</dd>
              </div>
              <div>
                <dt>提交编号</dt>
                <dd>{{ wrongItem.submitId || '未记录' }}</dd>
              </div>
            </dl>
          </section>

          <section class="section-panel">
            <h2>错误代码</h2>
            <pre class="code-block">{{ wrongItem.code || '暂无代码记录' }}</pre>
          </section>

          <WrongBookReflectionTasks
            ref="reviewTasksRef"
            :wrong-item="wrongItem"
            :session="agentSession"
            @session-updated="handleAgentSessionUpdated"
          />
        </main>

        <aside ref="agentSectionRef" class="agent-column">
          <WrongBookReviewAgent
            ref="agentRef"
            :wrong-item="wrongItem"
            @session-updated="handleAgentSessionUpdated"
            @focus-review-task="handleFocusReviewTask"
          />
        </aside>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import WrongBookReviewAgent from '@/components/agent/WrongBookReviewAgent.vue'
import WrongBookReflectionTasks from '@/components/agent/WrongBookReflectionTasks.vue'
import { getProblemDetail } from '@/api/problem'
import { getWrongBookDetail, reviewWrongBook } from '@/api/wrongBook'
import { shouldFocusWrongBookReflection } from '@/utils/wrongBookAgent'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const reviewing = ref(false)
const wrongItem = ref(null)
const problem = ref(null)
const agentSession = ref(null)
const agentRef = ref(null)
const agentSectionRef = ref(null)
const reviewTasksRef = ref(null)

const wrongItemId = computed(() => Number(route.params.id))

const problemTitle = computed(() => {
  return problem.value?.title || wrongItem.value?.problem?.title || `题目 #${wrongItem.value?.problemId}`
})

const problemContent = computed(() => {
  return problem.value?.content || wrongItem.value?.problem?.content || '当前错题记录没有附带完整题面，可以进入题目页查看。'
})

const statusText = computed(() => {
  const status = Number(wrongItem.value?.reviewStatus ?? 0)
  if (status >= 2) return '掌握中'
  if (status === 1) return '已复习'
  return '待复习'
})

const statusClass = computed(() => {
  const status = Number(wrongItem.value?.reviewStatus ?? 0)
  if (status >= 2) return 'mastering'
  if (status === 1) return 'reviewed'
  return 'pending'
})

const normalizeWrongItem = (item = {}) => ({
  ...item,
  reviewStatus: Number(item.reviewStatus ?? 0),
  language: item.language || '',
  createTime: item.createTime || item.updateTime || null
})

const loadProblem = async (problemId) => {
  if (!problemId) return
  try {
    const res = await getProblemDetail(problemId)
    if (res?.code === 200) {
      problem.value = res.data || null
    }
  } catch (error) {
    console.warn('Load problem detail failed:', error)
  }
}

const loadDetail = async () => {
  if (!Number.isFinite(wrongItemId.value) || wrongItemId.value <= 0) {
    wrongItem.value = null
    return
  }
  let shouldFocusAfterLoad = false
  loading.value = true
  problem.value = null
  agentSession.value = null
  try {
    const res = await getWrongBookDetail(wrongItemId.value)
    if (res?.code === 200 && res.data) {
      wrongItem.value = normalizeWrongItem(res.data)
      await loadProblem(wrongItem.value.problemId)
      shouldFocusAfterLoad = shouldFocusWrongBookReflection(route.query)
      return
    }
    wrongItem.value = null
    ElMessage.error(res?.msg || '加载错题详情失败')
  } catch (error) {
    console.warn('Load wrong book detail failed:', error)
    wrongItem.value = null
    ElMessage.error('加载错题详情失败')
  } finally {
    loading.value = false
    if (shouldFocusAfterLoad) {
      await focusAgent()
    }
  }
}

const formatDateTime = (time) => {
  if (!time) return '未记录'
  const date = new Date(time)
  if (Number.isNaN(date.getTime())) return '未记录'
  return date.toLocaleString('zh-CN')
}

const goBack = () => {
  router.push('/wrong-book')
}

const openProblem = () => {
  if (wrongItem.value?.problemId) {
    router.push(`/problem/${wrongItem.value.problemId}`)
  }
}

const markReviewed = async () => {
  if (!wrongItem.value || Number(wrongItem.value.reviewStatus) >= 1) return
  reviewing.value = true
  try {
    const res = await reviewWrongBook({
      id: wrongItem.value.id,
      status: 1
    })
    if (res?.code === 200) {
      wrongItem.value.reviewStatus = 1
      ElMessage.success('已标记为已复习')
      return
    }
    ElMessage.error(res?.msg || '标记失败')
  } catch (error) {
    console.warn('Review wrong book item failed:', error)
    ElMessage.error('标记失败')
  } finally {
    reviewing.value = false
  }
}

const focusAgent = async () => {
  await nextTick()
  agentSectionRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  agentRef.value?.focus?.()
}

const handleAgentSessionUpdated = (session) => {
  agentSession.value = session || null
  agentRef.value?.replaceSession?.(agentSession.value)
}

const handleFocusReviewTask = async (taskKey) => {
  await nextTick()
  reviewTasksRef.value?.focusTask?.(taskKey)
}

const focusFromRoute = async () => {
  if (shouldFocusWrongBookReflection(route.query)) {
    await focusAgent()
  }
}

const handleCoachFocus = () => {
  focusAgent()
}

watch(
  () => route.params.id,
  () => loadDetail()
)

watch(
  () => route.query.coach,
  () => focusFromRoute()
)

onMounted(() => {
  loadDetail()
  window.addEventListener('learning-agent-focus-wrong-book-review', handleCoachFocus)
})

onBeforeUnmount(() => {
  window.removeEventListener('learning-agent-focus-wrong-book-review', handleCoachFocus)
})
</script>

<style scoped>
.wrong-detail-page {
  width: 100%;
  min-height: 100vh;
  padding: 24px;
  box-sizing: border-box;
  background: #f7f8fa;
}

.wrong-detail-shell {
  max-width: 1400px;
  margin: 0 auto;
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 18px;
}

.topbar-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}

.page-state {
  padding: 24px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #ffffff;
  color: #526072;
}

.detail-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(360px, 440px);
  gap: 20px;
  align-items: start;
  min-height: 0;
}

.detail-main,
.agent-column {
  min-width: 0;
}

.detail-main {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-panel {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #ffffff;
  padding: 18px;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: #64748b;
  font-size: 13px;
}

.section-panel h1,
.section-panel h2 {
  color: #172033;
  letter-spacing: 0;
}

.section-panel h1 {
  margin: 14px 0 10px;
  font-size: 28px;
}

.section-panel h2 {
  margin: 0 0 14px;
  font-size: 18px;
}

.problem-content {
  margin: 0;
  color: #526072;
  line-height: 1.8;
  white-space: pre-wrap;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  border-radius: 8px;
  padding: 4px 10px;
  font-size: 12px;
  font-weight: 700;
}

.status-pill.pending {
  background: #fff7ed;
  color: #c2410c;
}

.status-pill.reviewed {
  background: #ecfdf5;
  color: #047857;
}

.status-pill.mastering {
  background: #eef6ff;
  color: #2563eb;
}

.info-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin: 0;
}

.info-list div {
  min-width: 0;
}

.info-list dt {
  color: #64748b;
  font-size: 12px;
  font-weight: 700;
}

.info-list dd {
  margin: 5px 0 0;
  color: #243044;
  line-height: 1.6;
  word-break: break-word;
}

.code-block {
  max-height: 460px;
  margin: 0;
  overflow: auto;
  border-radius: 8px;
  background: #111827;
  color: #f8fafc;
  padding: 16px;
  font-family: "Fira Code", Consolas, monospace;
  font-size: 13px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}

.agent-column {
  position: sticky;
  top: 88px;
  align-self: start;
  min-height: 0;
}

@media (max-width: 1100px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }

  .agent-column {
    position: static;
  }
}

@media (max-width: 640px) {
  .wrong-detail-page {
    padding: 16px;
  }

  .topbar,
  .topbar-actions {
    align-items: stretch;
    flex-direction: column;
  }

  .info-list {
    grid-template-columns: 1fr;
  }

  .section-panel h1 {
    font-size: 24px;
  }
}
</style>
