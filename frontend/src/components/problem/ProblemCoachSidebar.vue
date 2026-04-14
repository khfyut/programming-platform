<template>
  <div class="problem-coach-shell" :class="{ open: panelVisible }">
    <div class="coach-rail">
      <button class="coach-toggle" type="button" @click="togglePanel">
        <el-icon><ChatDotRound /></el-icon>
        <span>{{ panelVisible ? '收起陪练' : '陪练 Agent' }}</span>
      </button>
    </div>

    <aside v-if="panelVisible" class="coach-sidebar">
      <div class="coach-header">
        <div>
          <div class="coach-title">题目陪练</div>
          <div class="coach-subtitle">{{ problemTitle || '围绕当前题持续追问' }}</div>
        </div>
        <el-tag size="small" :type="canRevealFullSolution ? 'success' : 'info'">
          {{ canRevealFullSolution ? '可索要修正版' : '提示模式' }}
        </el-tag>
      </div>

      <div v-if="summary" class="coach-summary">
        <div class="section-title">当前诊断</div>
        <p>{{ summary }}</p>
      </div>

      <div v-if="decisionMeta.length" class="coach-decision-meta">
        <el-tag v-for="item in decisionMeta" :key="item.label" size="small" effect="plain">
          {{ item.label }}：{{ item.value }}
        </el-tag>
      </div>

      <div v-if="nextSuggestion || weakPoints.length" class="coach-learning-notes">
        <p v-if="nextSuggestion">{{ nextSuggestion }}</p>
        <div v-if="weakPoints.length" class="weak-tags">
          <el-tag v-for="point in weakPoints" :key="point" size="small" type="warning">
            {{ point }}
          </el-tag>
        </div>
      </div>

      <div class="coach-actions">
        <div class="section-title">快捷操作</div>
        <div class="action-grid">
          <el-button
            v-for="action in actions"
            :key="`${action.type}-${action.label}`"
            size="small"
            plain
            class="action-btn"
            @click="handleAction(action)"
          >
            {{ action.label }}
          </el-button>
        </div>
      </div>

      <div v-if="recommendations.length" class="coach-recommendations">
        <div class="section-title">下一步推荐</div>
        <button
          v-for="card in recommendations"
          :key="`${card.type}-${card.targetId}`"
          type="button"
          class="recommend-card"
          @click="openRecommendation(card)"
        >
          <div class="recommend-badge">{{ cardTypeLabel(card.type) }}</div>
          <div class="recommend-title">{{ card.title }}</div>
          <div class="recommend-description">{{ card.description }}</div>
          <div class="recommend-reason">{{ card.reason }}</div>
        </button>
      </div>

      <div class="coach-thread">
        <div class="section-title">对话线程</div>
        <div ref="messageListRef" class="message-list" v-loading="loading">
          <div v-if="!messages.length" class="thread-empty">
            失败后会自动给出摘要，也可以现在直接追问思路、报错和下一步练习。
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
        </div>
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
          <span class="composer-tip">`Ctrl+Enter` 发送</span>
          <el-button type="primary" :loading="sending" @click="submitMessage">
            发送
          </el-button>
        </div>
      </div>
    </aside>
  </div>
</template>

<script setup>
import { computed, nextTick, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatDotRound } from '@element-plus/icons-vue'
import { chatProblemAgent, getLatestProblemAgentSession, sendAgentFeedback } from '@/api/problemAgent'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
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

const emit = defineEmits(['update:visible', 'apply-draft'])

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
const pedagogicalGoal = ref('')
const contentType = ref('')
const nextSuggestion = ref('')
const errorTag = ref('')
const weakPoints = ref([])
const requestId = ref('')
const latestSubmitId = ref(null)

const numericProblemId = computed(() => {
  const value = Number(props.problemId)
  return Number.isFinite(value) && value > 0 ? value : null
})

const panelVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

const resetConversation = () => {
  sessionId.value = ''
  messages.value = []
  summary.value = ''
  actions.value = []
  recommendations.value = []
  draftCode.value = ''
  canRevealFullSolution.value = false
  actionType.value = ''
  pedagogicalGoal.value = ''
  contentType.value = ''
  nextSuggestion.value = ''
  errorTag.value = ''
  weakPoints.value = []
  requestId.value = ''
  latestSubmitId.value = null
  input.value = ''
}

const scrollToBottom = async () => {
  await nextTick()
  const node = messageListRef.value
  if (node) {
    node.scrollTop = node.scrollHeight
  }
}

const readCoachMetadata = (payload = {}) => {
  const snapshot =
    payload.contextSnapshot && typeof payload.contextSnapshot === 'object' && !Array.isArray(payload.contextSnapshot)
      ? payload.contextSnapshot
      : {}

  return {
    actionType: payload.actionType || snapshot.actionType || '',
    pedagogicalGoal: payload.pedagogicalGoal || snapshot.pedagogicalGoal || '',
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
  pedagogicalGoal.value = metadata.pedagogicalGoal
  contentType.value = metadata.contentType
  nextSuggestion.value = metadata.nextSuggestion
  errorTag.value = metadata.errorTag
  weakPoints.value = metadata.weakPoints
  requestId.value = metadata.requestId
  latestSubmitId.value = metadata.latestSubmitId
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
  pedagogicalGoal.value = metadata.pedagogicalGoal
  contentType.value = metadata.contentType
  nextSuggestion.value = metadata.nextSuggestion
  errorTag.value = metadata.errorTag
  weakPoints.value = metadata.weakPoints
  requestId.value = metadata.requestId
  latestSubmitId.value = metadata.latestSubmitId || requestPayload?.submitId || null

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
    console.error('记录Agent反馈失败:', error)
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
    return
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
  try {
    const res = await chatProblemAgent(requestPayload)
    if (res.code === 200 && res.data) {
      await applyChatPayload(res.data, requestPayload)
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

const togglePanel = async () => {
  panelVisible.value = !panelVisible.value
  if (panelVisible.value && !messages.value.length) {
    await loadLatestSession()
  }
}

const handleAction = async (action) => {
  if (action.type === 'apply_draft') {
    if (!draftCode.value) {
      ElMessage.warning('当前没有可覆盖的参考修正版')
      return
    }
    await sendFeedback('applied_draft', { source: 'quick_action', label: action.label })
    emit('apply-draft', draftCode.value)
    return
  }

  if (action.type === 'reveal_solution') {
    await sendFeedback('revealed_solution', { source: 'quick_action', label: action.label })
    await sendAgentRequest({
      triggerType: 'manual',
      requestFullSolution: true,
      message: action.prompt || '请给我参考修正版代码。'
    })
    return
  }

  if (action.type === 'open_recommendation' && action.route) {
    await sendFeedback('accepted', { source: 'recommendation_action', label: action.label, route: action.route })
    router.push(action.route)
    return
  }

  await sendFeedback('asked_followup', { source: 'quick_action', label: action.label })
  await sendAgentRequest({
    triggerType: 'manual',
    message: action.prompt || action.label
  })
}

const openRecommendation = (card) => {
  if (!card?.route) {
    return
  }
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

const decisionMeta = computed(() =>
  [
    { label: '动作', value: actionType.value },
    { label: '目标', value: pedagogicalGoal.value },
    { label: '类型', value: contentType.value },
    { label: '错误', value: errorTag.value }
  ].filter((item) => item.value)
)

const openWithPrompt = async ({ prompt, selectedCode } = {}) => {
  panelVisible.value = true
  await sendAgentRequest({
    triggerType: 'manual',
    message: prompt || '请帮我分析当前选中的代码。',
    code: selectedCode || props.code
  })
}

const triggerFailure = async (payload = {}) => {
  panelVisible.value = true
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

defineExpose({
  openWithPrompt,
  triggerFailure,
  reloadSession: loadLatestSession
})
</script>

<style scoped>
.problem-coach-shell {
  width: 64px;
  height: 100%;
  display: flex;
  flex-shrink: 0;
  border-left: 1px solid #dbe3ef;
  background: linear-gradient(180deg, #f8fbff 0%, #eef5ff 100%);
  transition: width 0.25s ease;
}

.problem-coach-shell.open {
  width: 360px;
}

.coach-rail {
  width: 64px;
  border-right: 1px solid rgba(148, 163, 184, 0.18);
  display: flex;
  justify-content: center;
  padding: 16px 0;
}

.coach-toggle {
  width: 44px;
  min-height: 180px;
  border: none;
  border-radius: 18px;
  background: linear-gradient(180deg, #0f4c81 0%, #1d74c8 100%);
  color: #fff;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 16px 8px;
  box-shadow: 0 18px 32px rgba(15, 76, 129, 0.18);
}

.coach-toggle span {
  writing-mode: vertical-rl;
  font-size: 13px;
  letter-spacing: 1px;
}

.coach-sidebar {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 18px 18px 16px 16px;
  overflow: hidden;
}

.coach-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.coach-title {
  font-size: 17px;
  font-weight: 700;
  color: #102a43;
}

.coach-subtitle {
  margin-top: 4px;
  font-size: 12px;
  line-height: 1.5;
  color: #5f7389;
}

.coach-summary,
.coach-decision-meta,
.coach-learning-notes,
.coach-actions,
.coach-recommendations,
.coach-thread,
.coach-composer {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.coach-summary {
  padding: 14px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(15, 76, 129, 0.1);
}

.coach-summary p {
  margin: 0;
  font-size: 13px;
  line-height: 1.65;
  color: #243b53;
}

.coach-decision-meta,
.weak-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.coach-learning-notes {
  padding: 12px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(15, 76, 129, 0.1);
}

.coach-learning-notes p {
  margin: 0;
  font-size: 13px;
  line-height: 1.6;
  color: #243b53;
}

.section-title {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #52667a;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.action-btn {
  margin: 0;
  white-space: normal;
}

.coach-recommendations {
  max-height: 220px;
  overflow-y: auto;
  padding-right: 4px;
}

.recommend-card {
  width: 100%;
  border: 1px solid rgba(15, 76, 129, 0.08);
  border-radius: 16px;
  background: #fff;
  padding: 12px;
  text-align: left;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 8px;
  transition:
    transform 0.2s ease,
    border-color 0.2s ease,
    box-shadow 0.2s ease;
}

.recommend-card:hover {
  transform: translateY(-1px);
  border-color: rgba(29, 116, 200, 0.28);
  box-shadow: 0 12px 24px rgba(15, 76, 129, 0.08);
}

.recommend-badge {
  width: fit-content;
  padding: 4px 8px;
  border-radius: 999px;
  background: rgba(29, 116, 200, 0.08);
  color: #1d74c8;
  font-size: 11px;
  font-weight: 700;
}

.recommend-title {
  font-size: 14px;
  font-weight: 700;
  color: #102a43;
}

.recommend-description,
.recommend-reason {
  font-size: 12px;
  line-height: 1.6;
  color: #52667a;
}

.coach-thread {
  flex: 1;
  min-height: 0;
}

.message-list {
  flex: 1;
  min-height: 180px;
  overflow-y: auto;
  padding-right: 4px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.thread-empty {
  border: 1px dashed rgba(95, 115, 137, 0.32);
  border-radius: 16px;
  padding: 14px;
  font-size: 13px;
  line-height: 1.6;
  color: #5f7389;
  background: rgba(255, 255, 255, 0.72);
}

.message-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 12px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(15, 76, 129, 0.08);
}

.message-item.user {
  background: rgba(29, 116, 200, 0.08);
}

.message-role {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #52667a;
}

.message-content {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 13px;
  line-height: 1.65;
  color: #102a43;
  font-family: inherit;
}

.composer-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.composer-tip {
  font-size: 12px;
  color: #7b8794;
}

@media (max-width: 1280px) {
  .problem-coach-shell {
    width: 100%;
    height: auto;
    border-left: none;
    border-top: 1px solid #dbe3ef;
  }

  .problem-coach-shell.open {
    width: 100%;
  }

  .coach-rail {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid rgba(148, 163, 184, 0.18);
    padding: 12px;
  }

  .coach-toggle {
    width: 100%;
    min-height: auto;
    flex-direction: row;
  }

  .coach-toggle span {
    writing-mode: initial;
  }
}
</style>
