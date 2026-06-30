<template>
  <section ref="rootRef" class="wrong-book-agent">
    <header class="agent-header">
      <div>
        <p class="eyebrow">Agent 复盘</p>
        <h2>围绕这道错题继续讨论</h2>
      </div>
      <el-button :loading="reflecting" @click="refreshReflection">重新复盘</el-button>
    </header>

    <div v-if="loading" class="agent-state">正在读取错题复盘会话...</div>

    <template v-else>
      <div class="reflection-panel">
        <div class="panel-label">当前判断</div>
        <dl class="judgement-list">
          <div>
            <dt>类型</dt>
            <dd>{{ currentJudgement.errorType }}</dd>
          </div>
          <div>
            <dt>阶段</dt>
            <dd>{{ currentJudgement.stage }}</dd>
          </div>
          <div>
            <dt>主要问题</dt>
            <dd>{{ currentJudgement.mainIssue }}</dd>
          </div>
        </dl>
        <p class="reflection-text" v-html="renderMarkdown(reflectionText)"></p>
        <div v-if="weakPoints.length" class="tag-row">
          <span v-for="point in weakPoints" :key="point">{{ point }}</span>
        </div>
      </div>

      <div class="action-panel">
        <div class="panel-label">下一步你可以做</div>
        <div class="action-grid">
          <el-button
            v-for="action in coachActions"
            :key="action.type"
            plain
            :loading="sendingAction === action.type"
            @click="handleCoachAction(action)"
          >
            {{ action.label }}
          </el-button>
        </div>
        <div v-if="nextSuggestion" class="next-step">下一步：{{ nextSuggestion }}</div>
      </div>

      <div class="chat-panel">
        <div class="panel-label">讨论记录</div>
        <div class="chat-guidance">当前推荐通过“复盘任务”完成学习，聊天用于补充问题和深挖细节。</div>
        <div v-if="visibleChatMessages.length" class="chat-list">
          <div
            v-for="message in visibleChatMessages"
            :key="message.id || `${message.role}-${message.createTime}-${message.content}`"
            class="chat-message"
            :class="[message.role, { working: message.working }]"
          >
            <span>{{ message.role === 'user' ? '我' : 'AI' }}</span>
            <p v-html="renderMarkdown(message.content)"></p>
          </div>
        </div>
        <div v-else class="agent-state compact">
          可以直接问“我为什么错”“这个边界怎么判断”“下次怎么避免”。
        </div>

        <div class="chat-input">
          <el-input
            ref="inputRef"
            v-model="draft"
            type="textarea"
            :rows="3"
            resize="none"
            maxlength="500"
            show-word-limit
            placeholder="针对这道错题继续提问..."
            @keydown.ctrl.enter.prevent="sendMessage"
          />
          <el-button type="primary" :loading="sending" :disabled="!canSend" @click="sendMessage">
            发送
          </el-button>
        </div>
      </div>
    </template>
  </section>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  chatWrongBookAgent,
  getLatestWrongBookAgentSession,
  reflectWrongBookReview
} from '@/api/wrongBookAgent'
import {
  createWrongBookAgentActionPayload,
  normalizeWrongBookAgentActions,
  normalizeWrongBookAgentState
} from '@/utils/wrongBookAgent'
import { getAgentWorkingSteps } from '@/utils/agentWorkingSteps'
import { renderMarkdown } from '@/utils/markdown'

const props = defineProps({
  wrongItem: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['session-updated', 'focus-review-task'])

const rootRef = ref(null)
const inputRef = ref(null)
const session = ref(null)
const loading = ref(false)
const reflecting = ref(false)
const sending = ref(false)
const sendingAction = ref('')
const draft = ref('')
const pendingChatMessages = ref([])
const workingStep = ref('')
const workingTimer = ref(null)

const messages = computed(() => session.value?.messages || [])

const agentState = computed(() => normalizeWrongBookAgentState(session.value || {}, props.wrongItem || {}))
const currentJudgement = computed(() => agentState.value.currentJudgement)
const learningState = computed(() => agentState.value.learningState)
const coachActions = computed(() => normalizeWrongBookAgentActions(session.value || {}))
const nextHintLevel = computed(() => {
  const current = Number(learningState.value.hintLevel || 1)
  if (!Number.isFinite(current)) return 2
  return Math.max(1, Math.min(4, Math.round(current) + 1))
})

const reflectionMessage = computed(() => {
  return messages.value.find((message) => message.messageKind === 'wrong_book_reflection')
})

const reflectionText = computed(() => {
  return reflectionMessage.value?.content || '还没有复盘内容，可以点击“重新复盘”生成。'
})

const chatMessages = computed(() => {
  return messages.value.filter((message) => message.messageKind === 'wrong_book_chat')
})

const visibleChatMessages = computed(() => {
  return [...chatMessages.value, ...pendingChatMessages.value]
})

const weakPoints = computed(() => {
  return agentState.value.weakPoints
})

const nextSuggestion = computed(() => {
  return agentState.value.nextSuggestion
})

const canSend = computed(() => {
  return draft.value.trim().length > 0 && !sending.value
})

const applySession = (value) => {
  pendingChatMessages.value = []
  session.value = value || null
  emit('session-updated', session.value)
}

const replaceSession = (value) => {
  pendingChatMessages.value = []
  session.value = value || null
}

const stopWorkingSteps = () => {
  if (workingTimer.value) {
    window.clearInterval(workingTimer.value)
    workingTimer.value = null
  }
  workingStep.value = ''
  pendingChatMessages.value = pendingChatMessages.value.filter((message) => !message.working)
}

const startWorkingSteps = (actionType = 'chat') => {
  stopWorkingSteps()
  const steps = getAgentWorkingSteps({
    scene: 'wrong_book',
    actionType
  })
  let index = 0
  workingStep.value = steps[index] || '正在复盘这道错题'
  pendingChatMessages.value.push({
    id: `working-${Date.now()}`,
    role: 'assistant',
    content: workingStep.value,
    working: true
  })
  workingTimer.value = window.setInterval(() => {
    index = Math.min(index + 1, steps.length - 1)
    workingStep.value = steps[index]
    const workingMessage = pendingChatMessages.value.find((message) => message.working)
    if (workingMessage) {
      workingMessage.content = workingStep.value
    }
  }, 1800)
}

const loadSession = async () => {
  if (!props.wrongItem?.id) return
  loading.value = true
  try {
    const res = await getLatestWrongBookAgentSession(props.wrongItem.id)
    if (res?.code === 200 && res.data?.sessionId) {
      applySession(res.data)
      return
    }
    await refreshReflection()
  } catch (error) {
    console.warn('Load wrong book agent session failed:', error)
    await refreshReflection()
  } finally {
    loading.value = false
  }
}

const refreshReflection = async () => {
  if (!props.wrongItem?.id) return
  reflecting.value = true
  try {
    const res = await reflectWrongBookReview(props.wrongItem.id)
    if (res?.code === 200 && res.data) {
      applySession(res.data)
      return
    }
    ElMessage.error(res?.msg || '生成错题复盘失败')
  } catch (error) {
    console.warn('Reflect wrong book item failed:', error)
    ElMessage.error('生成错题复盘失败')
  } finally {
    reflecting.value = false
  }
}

const sendMessage = async () => {
  if (!canSend.value || !props.wrongItem?.id) return
  const message = draft.value.trim()
  sending.value = true
  pendingChatMessages.value = [{
    id: `user-${Date.now()}`,
    role: 'user',
    content: message
  }]
  startWorkingSteps('chat')
  try {
    const res = await chatWrongBookAgent({
      wrongItemId: props.wrongItem.id,
      sessionId: session.value?.sessionId,
      message
    })
    if (res?.code === 200 && res.data) {
      applySession(res.data)
      draft.value = ''
      await nextTick()
      inputRef.value?.focus?.()
      return
    }
    ElMessage.error(res?.msg || '发送失败')
  } catch (error) {
    console.warn('Wrong book agent chat failed:', error)
    ElMessage.error('发送失败')
  } finally {
    sending.value = false
    stopWorkingSteps()
  }
}

const runCoachAction = async (payload, loadingKey) => {
  if (!props.wrongItem?.id || sendingAction.value) return
  sendingAction.value = loadingKey
  pendingChatMessages.value = [{
    id: `user-${Date.now()}`,
    role: 'user',
    content: payload.message || '继续帮我复盘'
  }]
  startWorkingSteps(payload.actionType || 'chat')
  try {
    const res = await chatWrongBookAgent(payload)
    if (res?.code === 200 && res.data) {
      applySession(res.data)
      return
    }
    ElMessage.error(res?.msg || '执行建议失败')
  } catch (error) {
    console.warn('Wrong book agent action failed:', error)
    ElMessage.error('执行建议失败')
  } finally {
    sendingAction.value = ''
    stopWorkingSteps()
  }
}

const handleCoachAction = async (action) => {
  if (action.type === 'reflection_task_start') {
    emit('focus-review-task', 'summary')
    return
  }

  await runCoachAction(
    createWrongBookAgentActionPayload({
      wrongItemId: props.wrongItem.id,
      sessionId: session.value?.sessionId,
      actionType: action.type,
      hintLevel: action.type === 'hint' ? nextHintLevel.value : learningState.value.hintLevel,
      message: action.prompt || action.label
    }),
    action.type
  )
}

const focus = async () => {
  await nextTick()
  rootRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  inputRef.value?.focus?.()
}

watch(
  () => props.wrongItem?.id,
  () => {
    session.value = null
    pendingChatMessages.value = []
    draft.value = ''
    loadSession()
  },
  { immediate: true }
)

onBeforeUnmount(() => {
  stopWorkingSteps()
})

defineExpose({ focus, replaceSession })
</script>

<style scoped>
.wrong-book-agent {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
}

.agent-header,
.reflection-panel,
.action-panel,
.chat-panel {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #ffffff;
}

.agent-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px;
}

.eyebrow,
.panel-label {
  margin: 0;
  color: #2563eb;
  font-size: 12px;
  font-weight: 700;
}

.agent-header h2 {
  margin: 4px 0 0;
  color: #172033;
  font-size: 18px;
  letter-spacing: 0;
}

.reflection-panel,
.action-panel,
.chat-panel {
  padding: 16px;
}

.reflection-panel,
.action-panel {
  flex: 0 0 auto;
}

.chat-panel {
  display: flex;
  flex-direction: column;
  min-height: 260px;
  max-height: min(520px, calc(100vh - 140px));
}

.judgement-list {
  display: grid;
  gap: 10px;
  margin: 12px 0 0;
}

.judgement-list div {
  display: grid;
  grid-template-columns: 72px minmax(0, 1fr);
  gap: 10px;
  align-items: start;
}

.judgement-list dt {
  color: #64748b;
  font-size: 12px;
  font-weight: 700;
}

.judgement-list dd {
  margin: 0;
  color: #172033;
  line-height: 1.6;
  word-break: break-word;
}

.reflection-text {
  margin: 10px 0 0;
  color: #243044;
  line-height: 1.7;
  white-space: pre-wrap;
}

.action-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 8px;
  margin-top: 12px;
}

.action-grid :deep(.el-button) {
  justify-content: flex-start;
  margin-left: 0;
  white-space: normal;
}

.chat-guidance {
  margin-top: 10px;
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.tag-row span {
  padding: 4px 8px;
  border-radius: 8px;
  background: #eef6ff;
  color: #2563eb;
  font-size: 12px;
  font-weight: 700;
}

.next-step {
  margin-top: 12px;
  color: #526072;
  font-size: 14px;
  line-height: 1.6;
}

.agent-state {
  padding: 18px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #f8fafc;
  color: #526072;
  line-height: 1.6;
}

.agent-state.compact {
  padding: 12px;
  margin-top: 10px;
}

.chat-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 12px;
  max-height: 280px;
  min-height: 0;
  overflow-y: auto;
  padding-right: 4px;
}

.chat-message {
  display: grid;
  grid-template-columns: 36px 1fr;
  gap: 10px;
  align-items: start;
}

.chat-message span {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: #1f2937;
  color: #ffffff;
  font-size: 12px;
  font-weight: 800;
}

.chat-message.user span {
  background: #2563eb;
}

.chat-message p {
  min-width: 0;
  margin: 0;
  padding: 10px 12px;
  border-radius: 8px;
  background: #f8fafc;
  color: #243044;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}

.chat-message.working p {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #475569;
  background: #f8fbff;
}

.chat-message.working p::before {
  content: '';
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #2563eb;
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

.chat-input {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
  align-items: end;
  margin-top: 14px;
  flex: 0 0 auto;
}

@media (max-width: 640px) {
  .agent-header,
  .chat-input {
    grid-template-columns: 1fr;
    display: grid;
  }

  .agent-header {
    align-items: stretch;
  }
}
</style>
