<template>
  <section class="problem-coach-panel">
    <header class="coach-header">
      <div class="coach-heading">
        <div class="coach-title-row">
          <el-icon><ChatDotRound /></el-icon>
          <h2>题目陪练</h2>
        </div>
        <p>{{ problemTitle || '围绕当前题目继续追问思路、报错和下一步。' }}</p>
      </div>
      <div class="coach-status">当前：{{ statusLabel }}</div>
    </header>

    <div class="coach-thread">
      <div ref="messageListRef" class="message-list" v-loading="loading">
        <div v-if="!messages.length && summary" class="message-item assistant latest-summary">
          <div class="message-role">陪练</div>
          <pre class="message-content">{{ summary }}</pre>
        </div>
        <div v-else-if="!messages.length" class="thread-empty">
          可以直接问解题思路、下一步提示、报错原因，也可以先运行或提交，让陪练根据结果分析。
        </div>

        <div
          v-for="(message, index) in messages"
          :key="`${message.role}-${index}-${message.createTime || ''}`"
          class="message-item"
          :class="message.role"
        >
          <div class="message-role">{{ message.role === 'assistant' ? '陪练' : '我' }}</div>
          <pre class="message-content">{{ message.content }}</pre>
        </div>

        <div v-if="sending" class="message-item assistant working-message">
          <div class="message-role">陪练</div>
          <div class="working-status">
            <span></span>
            {{ workingStep || '正在分析当前问题' }}
          </div>
        </div>
      </div>
    </div>

    <div v-if="visibleActions.length" class="coach-actions">
      <el-button
        v-for="action in visibleActions"
        :key="`${action.type || action.actionType}-${action.label || action.title}`"
        size="small"
        plain
        class="action-btn"
        @click="handleAction(action)"
      >
        {{ action.label || action.title || '继续追问' }}
      </el-button>
    </div>

    <div class="coach-composer">
      <el-input
        v-model="input"
        type="textarea"
        :rows="3"
        resize="none"
        placeholder="继续追问，例如：为什么这个边界条件会错？"
        @keydown.ctrl.enter.prevent="submitMessage"
      />
      <div class="composer-actions">
        <span class="composer-tip">Ctrl+Enter 发送</span>
        <el-button type="primary" :loading="sending" @click="submitMessage">
          发送
        </el-button>
      </div>
    </div>

    <el-collapse v-if="hasLearningHints" v-model="learningHintPanels" class="coach-hints">
      <el-collapse-item title="学习提示" name="hints">
        <div class="hint-stack">
          <p v-if="nextSuggestion" class="hint-text">{{ nextSuggestion }}</p>
          <p v-if="friendlyErrorTag" class="hint-text">错误方向：{{ friendlyErrorTag }}</p>

          <div v-if="weakPoints.length" class="weak-tags">
            <span v-for="point in weakPoints" :key="point" class="weak-tag">
              {{ point }}
            </span>
          </div>

          <div v-if="recommendations.length" class="coach-recommendations">
            <button
              v-for="card in recommendations"
              :key="`${card.type}-${card.targetId}`"
              type="button"
              class="recommend-row"
              @click="openRecommendation(card)"
            >
              <span class="recommend-badge">{{ cardTypeLabel(card.type) }}</span>
              <span class="recommend-main">
                <strong>{{ card.title }}</strong>
                <span>{{ card.description || card.reason }}</span>
              </span>
            </button>
          </div>
        </div>
      </el-collapse-item>
    </el-collapse>
  </section>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatDotRound } from '@element-plus/icons-vue'
import { chatProblemAgent, getLatestProblemAgentSession, sendAgentFeedback } from '@/api/problemAgent'
import { getAgentWorkingSteps } from '@/utils/agentWorkingSteps'

const props = defineProps({
  problemId: {
    type: [Number, String],
    default: null
  },
  problemTitle: {
    type: String,
    default: ''
  },
  code: {
    type: String,
    default: ''
  },
  language: {
    type: String,
    default: 'java'
  }
})

const emit = defineEmits(['apply-draft'])

const router = useRouter()
const messageListRef = ref(null)
const loading = ref(false)
const sending = ref(false)
const sessionId = ref('')
const input = ref('')
const messages = ref([])
const summary = ref('')
const actions = ref([])
const recommendations = ref([])
const draftCode = ref('')
const canRevealFullSolution = ref(false)
const actionType = ref('')
const contentType = ref('')
const nextSuggestion = ref('')
const errorTag = ref('')
const weakPoints = ref([])
const requestId = ref('')
const latestSubmitId = ref(null)
const learningHintPanels = ref([])
const workingStep = ref('')
const workingTimer = ref(null)

const numericProblemId = computed(() => {
  const value = Number(props.problemId)
  return Number.isFinite(value) && value > 0 ? value : null
})

const statusLabel = computed(() => {
  if (sending.value) return '正在分析'
  if (loading.value) return '加载会话'

  if (actionType.value === 'CLARIFY_INTENT' || contentType.value === 'clarification') {
    return '\u786e\u8ba4\u610f\u56fe'
  }

  const actionLabels = {
    GUIDE_IDEA: '思路引导',
    HINT: '轻提示',
    DIAGNOSE: '错误诊断',
    EXPLAIN: '知识讲解',
    RECOMMEND: '下一步建议',
    REFLECT: '复盘',
    REVEAL_ANSWER: '参考修正版'
  }

  const contentLabels = {
    guidance: '思路引导',
    hint: '轻提示',
    diagnosis: '错误诊断',
    explanation: '知识讲解',
    recommendation: '下一步建议',
    reflection: '复盘',
    solution: '参考修正版'
  }

  return actionLabels[actionType.value] || contentLabels[contentType.value] || '随时提问'
})

const friendlyErrorTag = computed(() => {
  const normalized = String(errorTag.value || '')
    .trim()
    .toUpperCase()
    .replace(/[\s-]+/g, '_')

  const labels = {
    WA: '输出不匹配',
    WRONG_ANSWER: '输出不匹配',
    CE: '编译错误',
    COMPILE_ERROR: '编译错误',
    RE: '运行错误',
    RTE: '运行错误',
    RUNTIME_ERROR: '运行错误',
    TLE: '超时或复杂度问题',
    TIME_LIMIT_EXCEEDED: '超时或复杂度问题',
    OUTPUT_FORMAT: '输出格式',
    INPUT_PARSING: '输入解析',
    BOUNDARY: '边界条件',
    NULL_POINTER: '空值处理',
    TYPE_ERROR: '类型处理'
  }

  return labels[normalized] || ''
})

const visibleActions = computed(() => {
  const seen = new Set()
  return actions.value
    .filter((action) => {
      const type = action?.type || action?.actionType || ''
      if (type === 'reveal_solution' && !canRevealFullSolution.value) {
        return false
      }
      if (type === 'apply_draft' && !draftCode.value) {
        return false
      }
      const key = `${type}-${action?.label || action?.title || ''}`
      if (seen.has(key)) {
        return false
      }
      seen.add(key)
      return true
    })
    .slice(0, 3)
})

const hasLearningHints = computed(() =>
  Boolean(
    nextSuggestion.value ||
      friendlyErrorTag.value ||
      weakPoints.value.length ||
      recommendations.value.length
  )
)

const resetConversation = () => {
  sessionId.value = ''
  messages.value = []
  summary.value = ''
  actions.value = []
  recommendations.value = []
  draftCode.value = ''
  canRevealFullSolution.value = false
  actionType.value = ''
  contentType.value = ''
  nextSuggestion.value = ''
  errorTag.value = ''
  weakPoints.value = []
  requestId.value = ''
  latestSubmitId.value = null
  input.value = ''
  learningHintPanels.value = []
  stopWorkingSteps()
}

const scrollToBottom = async () => {
  await nextTick()
  const node = messageListRef.value
  if (node) {
    node.scrollTop = node.scrollHeight
  }
}

const stopWorkingSteps = () => {
  if (workingTimer.value) {
    window.clearInterval(workingTimer.value)
    workingTimer.value = null
  }
  workingStep.value = ''
}

const startWorkingSteps = (payload = {}) => {
  stopWorkingSteps()
  const steps = getAgentWorkingSteps({
    scene: 'problem_coach',
    triggerType: payload.triggerType
  })
  let index = 0
  workingStep.value = steps[index] || '正在分析当前问题'
  workingTimer.value = window.setInterval(() => {
    index = Math.min(index + 1, steps.length - 1)
    workingStep.value = steps[index]
  }, 1800)
}

const readCoachMetadata = (payload = {}) => {
  const snapshot =
    payload.contextSnapshot && typeof payload.contextSnapshot === 'object' && !Array.isArray(payload.contextSnapshot)
      ? payload.contextSnapshot
      : {}

  return {
    actionType: payload.actionType || snapshot.actionType || '',
    contentType: payload.contentType || snapshot.contentType || '',
    nextSuggestion: payload.nextSuggestion || snapshot.nextSuggestion || '',
    errorTag: payload.errorTag || snapshot.errorTag || '',
    requestId: payload.requestId || snapshot.requestId || '',
    latestSubmitId: payload.submitId || snapshot.latestSubmitId || null,
    weakPoints: Array.isArray(payload.weakPoints)
      ? payload.weakPoints
      : Array.isArray(snapshot.weakPoints)
        ? snapshot.weakPoints
        : []
  }
}

const applySessionPayload = async (payload) => {
  if (!payload) {
    return
  }

  sessionId.value = payload.sessionId || ''
  messages.value = Array.isArray(payload.messages) ? payload.messages : []
  summary.value = payload.summary || ''
  actions.value = Array.isArray(payload.actions) ? payload.actions : []
  recommendations.value = Array.isArray(payload.recommendations) ? payload.recommendations : []
  draftCode.value = payload.draftCode || ''
  canRevealFullSolution.value = Boolean(payload.canRevealFullSolution)
  const metadata = readCoachMetadata(payload)
  actionType.value = metadata.actionType
  contentType.value = metadata.contentType
  nextSuggestion.value = metadata.nextSuggestion
  errorTag.value = metadata.errorTag
  weakPoints.value = metadata.weakPoints
  requestId.value = metadata.requestId
  latestSubmitId.value = metadata.latestSubmitId
  learningHintPanels.value = []
  await scrollToBottom()
}

const createDisplayUserMessage = (payload) => {
  if (payload.message) {
    return payload.message
  }
  if (payload.requestFullSolution) {
    return '请给我参考修正版代码。'
  }
  if (payload.triggerType === 'submit_failed') {
    return '提交失败后自动分析'
  }
  if (payload.triggerType === 'run_failed') {
    return '运行失败后自动分析'
  }
  return '继续帮我分析这道题。'
}

const applyChatPayload = async (payload, requestPayload) => {
  sessionId.value = payload.sessionId || sessionId.value
  summary.value = payload.summary || ''
  actions.value = Array.isArray(payload.actions) ? payload.actions : []
  recommendations.value = Array.isArray(payload.recommendations) ? payload.recommendations : []
  draftCode.value = payload.draftCode || ''
  canRevealFullSolution.value = Boolean(payload.canRevealFullSolution)
  const metadata = readCoachMetadata(payload)
  actionType.value = metadata.actionType
  contentType.value = metadata.contentType
  nextSuggestion.value = metadata.nextSuggestion
  errorTag.value = metadata.errorTag
  weakPoints.value = metadata.weakPoints
  requestId.value = metadata.requestId
  latestSubmitId.value = metadata.latestSubmitId || requestPayload?.submitId || null
  learningHintPanels.value = []

  if (requestPayload) {
    messages.value.push({
      role: 'user',
      content: createDisplayUserMessage(requestPayload)
    })
  }
  if (payload.reply) {
    messages.value.push({
      role: 'assistant',
      content: payload.reply
    })
  }
  await scrollToBottom()
}

const sendFeedback = async (feedbackType, metadata = {}) => {
  if (!requestId.value || !numericProblemId.value || !actionType.value) {
    return
  }

  try {
    await sendAgentFeedback({
      request_id: requestId.value,
      problem_id: numericProblemId.value,
      submit_id: latestSubmitId.value || undefined,
      action_type: actionType.value,
      feedback_type: feedbackType,
      metadata
    })
  } catch (error) {
    console.error('记录 Agent 反馈失败:', error)
  }
}

const loadLatestSession = async () => {
  if (!numericProblemId.value) {
    resetConversation()
    return
  }

  loading.value = true
  try {
    const res = await getLatestProblemAgentSession(numericProblemId.value)
    if (res.code === 200 && res.data) {
      await applySessionPayload(res.data)
    } else {
      resetConversation()
    }
  } catch (error) {
    console.error('加载题目陪练会话失败:', error)
    resetConversation()
  } finally {
    loading.value = false
  }
}

const sendAgentRequest = async (payload = {}) => {
  if (!numericProblemId.value || sending.value) {
    return null
  }

  const requestPayload = {
    sessionId: sessionId.value || undefined,
    problemId: numericProblemId.value,
    code: payload.code ?? props.code,
    language: payload.language ?? props.language,
    triggerType: payload.triggerType || 'manual',
    requestFullSolution: Boolean(payload.requestFullSolution),
    message: payload.message || '',
    submitId: payload.submitId,
    latestResultCode: payload.latestResultCode,
    errorMessage: payload.errorMessage,
    executionOutput: payload.executionOutput
  }

  sending.value = true
  messages.value.push({
    role: 'user',
    content: createDisplayUserMessage(requestPayload)
  })
  startWorkingSteps(requestPayload)
  try {
    const res = await chatProblemAgent(requestPayload)
    if (res.code === 200 && res.data) {
      await applyChatPayload(res.data, null)
      return res.data
    }
    ElMessage.error(res.msg || '题目陪练暂时不可用')
    return null
  } catch (error) {
    console.error('调用题目陪练失败:', error)
    ElMessage.error('题目陪练暂时不可用')
    return null
  } finally {
    sending.value = false
    stopWorkingSteps()
  }
}

const submitMessage = async () => {
  const message = input.value.trim()
  if (!message) {
    return
  }
  input.value = ''
  await sendFeedback('asked_followup', { source: 'composer', messageLength: message.length })
  await sendAgentRequest({ message, triggerType: 'manual' })
}

const handleAction = async (action) => {
  const type = action.type || action.actionType || ''

  if (type === 'apply_draft') {
    if (!draftCode.value) {
      ElMessage.warning('当前没有可覆盖的参考修正版')
      return
    }
    await sendFeedback('applied_draft', { source: 'quick_action', label: action.label })
    emit('apply-draft', draftCode.value)
    return
  }

  if (type === 'reveal_solution') {
    await sendFeedback('revealed_solution', { source: 'quick_action', label: action.label })
    await sendAgentRequest({
      triggerType: 'manual',
      requestFullSolution: true,
      message: action.prompt || '请给我参考修正版代码。'
    })
    return
  }

  if (type === 'open_recommendation' && action.route) {
    await sendFeedback('accepted', { source: 'recommendation_action', label: action.label, route: action.route })
    router.push(action.route)
    return
  }

  await sendFeedback('asked_followup', { source: 'quick_action', label: action.label })
  await sendAgentRequest({
    triggerType: 'manual',
    message: action.prompt || action.label || action.title || '继续帮我分析'
  })
}

const openRecommendation = async (card) => {
  if (!card?.route) {
    return
  }
  await sendFeedback('accepted', {
    source: 'recommendation_card',
    type: card.type,
    targetId: card.targetId
  })
  router.push(card.route)
}

const cardTypeLabel = (type) => {
  if (type === 'problem') {
    return '下一题'
  }
  if (type === 'level') {
    return '学习路径'
  }
  if (type === 'knowledge_point') {
    return '薄弱点'
  }
  return '推荐'
}

const openWithPrompt = async ({ prompt, selectedCode } = {}) => {
  await sendAgentRequest({
    triggerType: 'manual',
    message: prompt || '请帮我分析当前选中的代码。',
    code: selectedCode || props.code
  })
}

const triggerFailure = async (payload = {}) => {
  await sendAgentRequest({
    triggerType: payload.triggerType || 'run_failed',
    latestResultCode: payload.latestResultCode,
    errorMessage: payload.errorMessage,
    executionOutput: payload.executionOutput,
    submitId: payload.submitId,
    code: payload.code || props.code,
    language: payload.language || props.language
  })
}

watch(
  () => props.problemId,
  async () => {
    resetConversation()
    if (numericProblemId.value) {
      await loadLatestSession()
    }
  },
  { immediate: true }
)

onBeforeUnmount(() => {
  stopWorkingSteps()
})

defineExpose({
  openWithPrompt,
  triggerFailure,
  reloadSession: loadLatestSession
})
</script>

<style scoped>
.problem-coach-panel {
  height: 100%;
  min-height: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
  color: var(--problem-text, #1f2937);
}

.coach-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--problem-border, #dbe3ef);
}

.coach-heading {
  min-width: 0;
}

.coach-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.coach-title-row .el-icon {
  color: var(--problem-primary, #1890ff);
}

.coach-title-row h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--problem-text, #1f2937);
}

.coach-heading p {
  margin: 6px 0 0;
  font-size: 13px;
  line-height: 1.6;
  color: var(--problem-text-secondary, #64748b);
}

.coach-status {
  flex: 0 0 auto;
  padding: 4px 10px;
  border: 1px solid rgba(24, 144, 255, 0.24);
  border-radius: 8px;
  background: rgba(24, 144, 255, 0.06);
  color: #0f5da8;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

.coach-thread {
  flex: 1 1 auto;
  min-height: 0;
}

.message-list {
  height: 100%;
  min-height: 0;
  overflow-y: auto;
  padding-right: 4px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.thread-empty {
  border: 1px dashed #cbd5e1;
  border-radius: 8px;
  padding: 14px;
  font-size: 13px;
  line-height: 1.7;
  color: var(--problem-text-secondary, #64748b);
  background: #f8fafc;
}

.message-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 12px 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
}

.message-item.user {
  border-color: rgba(24, 144, 255, 0.22);
  background: rgba(24, 144, 255, 0.06);
}

.latest-summary {
  background: #fff;
}

.message-role {
  font-size: 12px;
  font-weight: 700;
  color: var(--problem-text-secondary, #64748b);
}

.message-content {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 14px;
  line-height: 1.75;
  color: #1f2937;
  font-family: inherit;
}

.working-message {
  border-color: rgba(24, 144, 255, 0.18);
  background: #f8fbff;
}

.working-status {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #475569;
  font-size: 13px;
  line-height: 1.6;
}

.working-status span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #1890ff;
  animation: workingPulse 1.2s infinite;
}

@keyframes workingPulse {
  0%, 100% {
    opacity: 0.4;
    transform: scale(0.9);
  }

  50% {
    opacity: 1;
    transform: scale(1.08);
  }
}

.coach-actions {
  display: flex;
  flex: 0 0 auto;
  flex-wrap: wrap;
  gap: 8px;
}

.action-btn {
  margin: 0;
  border-radius: 8px;
}

.coach-composer {
  position: sticky;
  bottom: 0;
  z-index: 2;
  order: 5;
  flex: 0 0 auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: auto;
  padding-top: 12px;
  background: var(--problem-surface, #fff);
}

.composer-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.composer-tip {
  font-size: 12px;
  color: #94a3b8;
}

.coach-hints {
  order: 4;
  flex: 0 0 auto;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.coach-hints :deep(.el-collapse-item__header) {
  height: 40px;
  padding: 0 12px;
  border-bottom: 0;
  font-weight: 600;
  color: #334155;
  background: #f8fafc;
}

.coach-hints :deep(.el-collapse-item__wrap) {
  border-bottom: 0;
}

.coach-hints :deep(.el-collapse-item__content) {
  padding: 12px;
}

.hint-stack {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.hint-text {
  margin: 0;
  font-size: 13px;
  line-height: 1.65;
  color: #334155;
}

.weak-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.weak-tag {
  padding: 3px 8px;
  border: 1px solid #fed7aa;
  border-radius: 6px;
  background: #fff7ed;
  color: #c2410c;
  font-size: 12px;
}

.coach-recommendations {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.recommend-row {
  width: 100%;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  padding: 10px 12px;
  text-align: left;
  cursor: pointer;
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.recommend-row:hover {
  border-color: #bfdbfe;
  background: #f8fbff;
}

.recommend-badge {
  flex: 0 0 auto;
  padding: 3px 7px;
  border-radius: 6px;
  background: rgba(24, 144, 255, 0.08);
  color: #0f5da8;
  font-size: 11px;
  font-weight: 700;
}

.recommend-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.recommend-main strong {
  font-size: 13px;
  color: #1f2937;
}

.recommend-main span {
  font-size: 12px;
  line-height: 1.5;
  color: #64748b;
}

@media (max-width: 640px) {
  .coach-header {
    flex-direction: column;
  }

  .coach-status {
    white-space: normal;
  }

  .message-list {
    min-height: 0;
  }

  .composer-actions {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
