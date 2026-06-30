<template>
  <div v-if="visible" class="global-coach" :class="{ expanded }">
    <section v-if="showNudgeCard" class="coach-nudge" aria-live="polite">
      <div class="coach-nudge__label">学习助手</div>
      <h3>{{ activeNudge.title }}</h3>
      <p>{{ activeNudge.summary }}</p>
      <div class="coach-nudge__actions">
        <button type="button" @click="openPanel">打开</button>
        <button type="button" class="ghost" @click="snoozeNudge">稍后</button>
      </div>
    </section>

    <section v-if="expanded" class="coach-panel" aria-label="AI 学习助手">
      <header class="coach-panel__header">
        <div>
          <h2>AI 学习助手</h2>
          <p>{{ conversationSubtitle }}</p>
        </div>
        <div class="coach-panel__window-actions">
          <button type="button" class="icon-button" aria-label="收起学习助手" @click="closePanel">-</button>
          <button type="button" class="icon-button" aria-label="关闭学习助手" @click="closePanel">×</button>
        </div>
      </header>

      <div ref="messageListRef" class="coach-panel__content" :class="{ welcome: isWelcomeMode }">
        <template v-if="isWelcomeMode">
          <div class="coach-avatar">AI</div>
          <h3>Hi，我是学习助手</h3>
          <p class="welcome-copy">可以帮你了解系统，也可以陪你做题和复盘</p>

          <div v-if="activeNudge" class="inline-nudge">
            <span>当前建议</span>
            <strong>{{ activeNudge.title }}</strong>
            <p>{{ activeNudge.summary }}</p>
          </div>

          <div class="starter-chips">
            <button
              v-for="chip in starterChips"
              :key="chip.label"
              type="button"
              @click="submitLocalMessage(chip.prompt)"
            >
              {{ chip.label }}
            </button>
          </div>

          <button type="button" class="guide-link" @click="submitLocalMessage('这个系统怎么用')">
            查看新手导览 →
          </button>
        </template>

        <template v-else>
          <div
            v-for="(message, index) in messages"
            :key="`${message.role}-${index}`"
            class="message-row"
            :class="message.role"
          >
            <div v-if="message.role === 'assistant'" class="message-avatar">AI</div>
            <div class="message-bubble">
              <template v-for="(part, partIndex) in messageParts(message)" :key="partIndex">
                <button
                  v-if="part.action"
                  type="button"
                  class="inline-action"
                  @click="handleInlineAction(part.action)"
                >
                  {{ part.text }}
                </button>
                <span v-else v-html="renderMarkdown(part.text)"></span>
              </template>
            </div>
          </div>

          <div v-if="activeNudge" class="conversation-nudge">
            <span>当前建议</span>
            <strong>{{ activeNudge.title }}</strong>
            <p>{{ activeNudge.summary }}</p>
            <div class="conversation-nudge__actions">
              <button type="button" @click="followNudge">进入建议位置</button>
              <button type="button" class="ghost" @click="snoozeNudge">稍后</button>
            </div>
          </div>

          <div v-if="loading" class="message-row assistant">
            <div class="message-avatar">AI</div>
            <div class="message-bubble muted">{{ loadingStep }}</div>
          </div>
        </template>
      </div>

      <form class="coach-composer" @submit.prevent="submitComposer">
        <textarea
          v-model="input"
          rows="2"
          placeholder="有问题，尽管问"
          @keydown.ctrl.enter.prevent="submitComposer"
        />
        <div class="composer-footer">
          <button type="button" class="scope-button" @click="submitLocalMessage('当前页面怎么用')">
            学习助手 · {{ guide?.shortTitle || '当前页面' }}
          </button>
          <button type="submit" class="send-button" :disabled="!input.trim() || loading" aria-label="发送">
            ↑
          </button>
        </div>
      </form>
    </section>

    <button
      v-if="!expanded"
      type="button"
      class="coach-fab"
      :class="{ hasNudge: !!activeNudge }"
      aria-label="打开 AI 学习助手"
      @click="openPanel"
    >
      AI
      <span v-if="unreadCount > 0" class="coach-fab__dot">{{ unreadCount }}</span>
    </button>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  chatAgentCoach,
  decideAgentCoach,
  getAgentCoachState,
  recordAgentCoachEvent
} from '@/api/problemAgent'
import {
  createGlobalCoachUnavailableReply,
  getGlobalCoachGuide,
  normalizeGlobalCoachChatResponse,
  shouldShowGlobalLearningCoach,
  shouldShowGlobalLearningCoachNudge
} from '@/utils/globalLearningCoach'
import { getWrongBookReflectionRoute } from '@/utils/wrongBookAgent'
import { renderMarkdown } from '@/utils/markdown'

const ONBOARDING_SEEN_KEY = 'globalLearningCoach.onboardingSeen'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const expanded = ref(false)
const activeNudge = ref(null)
const unreadCount = ref(0)
const dwellTimer = ref(null)
const lastActivityAt = ref(Date.now())
const deciding = ref(false)
const loading = ref(false)
const loadingStep = ref('正在理解你的问题意图...')
const input = ref('')
const messages = ref([])
const sessionId = ref('')
const messageListRef = ref(null)

const visible = computed(() => shouldShowGlobalLearningCoach(route, userStore.token))
const guide = computed(() => getGlobalCoachGuide(route))
const isWelcomeMode = computed(() => messages.value.length === 0)
const conversationSubtitle = computed(() => {
  if (isWelcomeMode.value) {
    return localStorage.getItem(ONBOARDING_SEEN_KEY) ? (guide.value?.shortTitle || '当前页面') : '系统导览'
  }
  return guide.value?.shortTitle || '轻量问答'
})

const starterChips = computed(() => [
  { label: '了解系统', prompt: '这个系统怎么用' },
  { label: '当前页怎么用', prompt: '当前页面怎么用' },
  { label: '开始练习', prompt: '我想开始练习' },
  { label: 'AI 陪练', prompt: 'AI 陪练怎么用' }
])

const showNudgeCard = computed(() => {
  return shouldShowGlobalLearningCoachNudge(route, activeNudge.value, expanded.value)
})

const routeProblemId = computed(() => {
  if (route.name !== 'ProblemDetail') {
    return null
  }
  const id = Number(route.params.id || route.params.problemId)
  return Number.isFinite(id) && id > 0 ? id : null
})

const routeWrongItemId = computed(() => {
  if (route.name !== 'WrongBookDetail') {
    return null
  }
  const id = Number(route.params.id)
  return Number.isFinite(id) && id > 0 ? id : null
})

const scrollToBottom = async () => {
  await nextTick()
  const node = messageListRef.value
  if (node) {
    node.scrollTop = node.scrollHeight
  }
}

const loadState = async () => {
  if (!visible.value) {
    activeNudge.value = null
    unreadCount.value = 0
    return
  }
  const res = await getAgentCoachState()
  if (res.code !== 200) {
    return
  }
  activeNudge.value = res.data?.activeNudge || null
  unreadCount.value = res.data?.unreadCount || 0
}

const decide = async (payload) => {
  if (!visible.value || deciding.value) {
    return
  }
  deciding.value = true
  try {
    const res = await decideAgentCoach({
      currentRoute: route.fullPath,
      pageType: pageType(),
      ...payload
    })
    if (res.code === 200) {
      if (res.data?.nudge) {
        activeNudge.value = res.data.nudge
        unreadCount.value = activeNudge.value?.status === 'ACTIVE' ? 1 : 0
      } else if (res.data?.created === false && res.data?.reason === 'COOLDOWN') {
        activeNudge.value = null
        unreadCount.value = 0
      }
    }
  } finally {
    deciding.value = false
  }
}

const pageType = () => {
  if (route.name === 'Problems') return 'PROBLEM_LIST'
  if (route.name === 'ProblemDetail') return 'PROBLEM'
  if (route.name === 'Learn') return 'LEARN'
  if (route.name === 'WrongBook') return 'WRONG_BOOK'
  if (route.name === 'WrongBookDetail') return 'WRONG_BOOK_DETAIL'
  if (['LearningPath', 'LevelDetail', 'Learn'].includes(route.name)) return 'LEARNING_PATH'
  if (route.name === 'ProfileAnalysis') return 'PROFILE_ANALYSIS'
  return 'GENERAL'
}

const scheduleDwellCheck = () => {
  clearDwellTimer()
  if (!visible.value || route.name !== 'ProblemDetail') {
    return
  }
  dwellTimer.value = window.setTimeout(() => {
    const inactiveSeconds = Math.floor((Date.now() - lastActivityAt.value) / 1000)
    if (!activeNudge.value && inactiveSeconds >= 90) {
      decide({
        triggerSource: 'PROBLEM_PAGE_DWELL',
        problemId: routeProblemId.value,
        dwellSeconds: inactiveSeconds
      })
    } else {
      scheduleDwellCheck()
    }
  }, 90000)
}

const clearDwellTimer = () => {
  if (dwellTimer.value) {
    window.clearTimeout(dwellTimer.value)
    dwellTimer.value = null
  }
}

const markActivity = () => {
  lastActivityAt.value = Date.now()
  scheduleDwellCheck()
}

const handleLearningEvent = (event) => {
  markActivity()
  const detail = event.detail || {}
  const triggerSource = {
    run_failed: 'RUN_FAILED',
    submit_failed: 'SUBMIT_FAILED',
    accepted: 'ACCEPTED',
    viewed_solution: 'VIEWED_SOLUTION'
  }[detail.eventType] || detail.triggerSource

  if (!triggerSource) {
    return
  }
  decide({
    triggerSource,
    problemId: detail.problemId || routeProblemId.value,
    submitId: detail.submitId,
    metadata: detail
  })
}

const requestRouteContextNudge = async () => {
  if (['Problems', 'Learn', 'ProfileAnalysis'].includes(route.name)) {
    await decide({ triggerSource: 'PAGE_ENTRY' })
  }
  if (route.name === 'WrongBook') {
    await decide({ triggerSource: 'WRONG_BOOK_ENTRY' })
  }
  if (route.name === 'WrongBookDetail') {
    await decide({
      triggerSource: 'WRONG_BOOK_DETAIL_ENTRY',
      wrongItemId: routeWrongItemId.value,
      metadata: {
        targetRoute: getWrongBookReflectionRoute(routeWrongItemId.value)
      }
    })
  }
  if (['LearningPath', 'LevelDetail'].includes(route.name)) {
    await decide({
      triggerSource: 'LEARNING_PATH_ENTRY',
      pathId: route.params.pathId || route.params.id,
      levelId: route.params.levelId
    })
  }
}

const openPanel = async () => {
  expanded.value = true
  unreadCount.value = 0
  localStorage.setItem(ONBOARDING_SEEN_KEY, '1')
  if (activeNudge.value?.id) {
    await recordAgentCoachEvent({
      nudgeId: activeNudge.value.id,
      eventType: 'OPENED',
      currentRoute: route.fullPath
    })
  }
}

const closePanel = () => {
  expanded.value = false
}

const submitComposer = () => {
  const message = input.value.trim()
  if (!message || loading.value) {
    return
  }
  input.value = ''
  submitLocalMessage(message)
}

const submitLocalMessage = async (message) => {
  const trimmed = String(message || '').trim()
  if (!trimmed) {
    return
  }
  loading.value = true
  loadingStep.value = '正在理解你的问题意图...'
  messages.value.push({
    role: 'user',
    content: trimmed,
    actions: []
  })
  await scrollToBottom()
  try {
    loadingStep.value = '正在结合当前页面...'
    const res = await chatAgentCoach({
      sessionId: sessionId.value || undefined,
      message: trimmed,
      currentRoute: route.fullPath,
      pageType: pageType(),
      problemId: routeProblemId.value,
      wrongItemId: routeWrongItemId.value,
      pathId: route.params.pathId || route.params.id,
      levelId: route.params.levelId,
      metadata: {
        routeName: route.name
      }
    })
    if (res.code === 200 && res.data) {
      const reply = normalizeGlobalCoachChatResponse(res.data)
      sessionId.value = reply.sessionId || sessionId.value
      if (reply.fallback || reply.source === 'LOCAL_FALLBACK') {
        appendModelUnavailable()
      } else {
        messages.value.push({
          role: 'assistant',
          content: reply.text,
          actions: reply.actions || [],
          source: reply.source,
          fallback: reply.fallback
        })
      }
    } else {
      appendModelUnavailable()
    }
  } catch (error) {
    console.error('调用全局学习助手失败:', error)
    appendModelUnavailable()
  } finally {
    loading.value = false
  }
  await scrollToBottom()
}

const appendModelUnavailable = () => {
  const reply = createGlobalCoachUnavailableReply(route)
  messages.value.push({
    role: 'assistant',
    content: reply.text,
    actions: reply.actions || [],
    source: 'LOCAL_FALLBACK',
    fallback: true
  })
}

const messageParts = (message) => {
  if (message.role !== 'assistant' || !message.actions?.length) {
    return [{ text: message.content }]
  }

  const actions = message.actions
    .map((action) => ({
      action,
      label: action.label,
      index: message.content.indexOf(action.label)
    }))
    .filter((item) => item.index >= 0)
    .sort((a, b) => a.index - b.index)

  if (!actions.length) {
    return [{ text: message.content }]
  }

  const parts = []
  let cursor = 0
  actions.forEach((item) => {
    if (item.index < cursor) {
      return
    }
    if (item.index > cursor) {
      parts.push({ text: message.content.slice(cursor, item.index) })
    }
    parts.push({ text: item.label, action: item.action })
    cursor = item.index + item.label.length
  })
  if (cursor < message.content.length) {
    parts.push({ text: message.content.slice(cursor) })
  }
  return parts
}

const handleInlineAction = async (action) => {
  if (!action) {
    return
  }
  if (action.type === 'route' && action.route) {
    await router.push(action.route)
    expanded.value = false
    return
  }
  if (action.type === 'focus_problem_coach') {
    if (route.name === 'ProblemDetail') {
      window.dispatchEvent(new CustomEvent('learning-agent-focus-problem-coach', {
        detail: { source: 'global-coach-inline-action' }
      }))
      expanded.value = false
    } else {
      await router.push('/problems')
      expanded.value = false
    }
    return
  }
  if (action.type === 'focus_wrong_book_review') {
    if (route.name === 'WrongBookDetail') {
      window.dispatchEvent(new CustomEvent('learning-agent-focus-wrong-book-review', {
        detail: { source: 'global-coach-inline-action' }
      }))
      expanded.value = false
    } else {
      await router.push('/wrong-book')
      expanded.value = false
    }
    return
  }
  if (action.type === 'explain_ai_coach') {
    await submitLocalMessage('AI 陪练怎么用')
  }
}

const followNudge = async () => {
  if (!activeNudge.value) {
    return
  }
  const nudge = activeNudge.value
  await recordAgentCoachEvent({
    nudgeId: nudge.id,
    eventType: 'CLICKED',
    currentRoute: route.fullPath
  })
  if (nudge.targetRoute && nudge.targetRoute !== route.fullPath) {
    await router.push(nudge.targetRoute)
  } else if (route.name === 'ProblemDetail') {
    window.dispatchEvent(new CustomEvent('learning-agent-focus-problem-coach', {
      detail: { nudge }
    }))
  } else if (route.name === 'WrongBookDetail') {
    window.dispatchEvent(new CustomEvent('learning-agent-focus-wrong-book-review', {
      detail: { nudge }
    }))
  }
  expanded.value = false
}

const snoozeNudge = async () => {
  if (!activeNudge.value) {
    return
  }
  await recordAgentCoachEvent({
    nudgeId: activeNudge.value.id,
    eventType: 'SNOOZED',
    currentRoute: route.fullPath
  })
  activeNudge.value = null
  unreadCount.value = 0
  expanded.value = false
}

watch(
  () => route.fullPath,
  async () => {
    messages.value = []
    sessionId.value = ''
    markActivity()
    await loadState()
    await requestRouteContextNudge()
  }
)

watch(visible, () => {
  markActivity()
  loadState()
})

onMounted(() => {
  loadState().then(requestRouteContextNudge)
  scheduleDwellCheck()
  window.addEventListener('keydown', markActivity, { passive: true })
  window.addEventListener('click', markActivity, { passive: true })
  window.addEventListener('learning-agent-event', handleLearningEvent)
})

onBeforeUnmount(() => {
  clearDwellTimer()
  window.removeEventListener('keydown', markActivity)
  window.removeEventListener('click', markActivity)
  window.removeEventListener('learning-agent-event', handleLearningEvent)
})
</script>

<style scoped>
.global-coach {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 2400;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
  color: #172033;
}

.coach-fab {
  position: relative;
  width: 56px;
  height: 56px;
  border: 1px solid #1f2937;
  border-radius: 50%;
  background: #1f2937;
  color: #ffffff;
  font-weight: 800;
  letter-spacing: 0;
  cursor: pointer;
  box-shadow: 0 14px 30px rgba(15, 23, 42, 0.24);
}

.coach-fab.hasNudge {
  background: #2563eb;
  border-color: #2563eb;
}

.coach-fab__dot {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 20px;
  height: 20px;
  padding: 0 5px;
  border: 2px solid #ffffff;
  border-radius: 10px;
  background: #ef4444;
  color: #ffffff;
  font-size: 11px;
  line-height: 16px;
}

.coach-nudge,
.coach-panel {
  width: min(360px, calc(100vw - 96px));
  border: 1px solid #d9e2ef;
  border-radius: 16px;
  background: #ffffff;
  box-shadow: 0 18px 44px rgba(15, 23, 42, 0.16);
}

.coach-nudge {
  padding: 16px;
  border-radius: 8px;
}

.coach-nudge__label {
  color: #2563eb;
  font-size: 12px;
  font-weight: 700;
}

.coach-nudge h3 {
  margin: 6px 0;
  color: #172033;
  font-size: 17px;
  letter-spacing: 0;
}

.coach-nudge p {
  margin: 0;
  color: #526072;
  font-size: 14px;
  line-height: 1.6;
}

.coach-nudge__actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 14px;
}

.coach-nudge button,
.conversation-nudge button {
  border: 1px solid #2563eb;
  border-radius: 8px;
  background: #2563eb;
  color: #ffffff;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  padding: 8px 12px;
}

.coach-nudge button.ghost,
.conversation-nudge button.ghost {
  border-color: #d9e2ef;
  background: #ffffff;
  color: #374151;
}

.coach-panel {
  height: min(680px, calc(100vh - 48px));
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.coach-panel__header {
  flex: 0 0 auto;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 18px 20px 12px;
  border-bottom: 1px solid #edf2f7;
}

.coach-panel__header h2 {
  margin: 0;
  color: #172033;
  font-size: 17px;
  letter-spacing: 0;
}

.coach-panel__header p {
  margin: 5px 0 0;
  color: #64748b;
  font-size: 12px;
}

.coach-panel__window-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.icon-button {
  width: 24px;
  height: 24px;
  padding: 0;
  border: 0;
  border-radius: 6px;
  background: transparent;
  color: #64748b;
  font-size: 18px;
  line-height: 1;
  cursor: pointer;
}

.coach-panel__content {
  flex: 1 1 auto;
  min-height: 0;
  overflow-y: auto;
  padding: 18px 20px;
}

.coach-panel__content.welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 72px;
  text-align: center;
}

.coach-avatar {
  width: 58px;
  height: 58px;
  border-radius: 50%;
  background: #eef6ff;
  color: #2563eb;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
}

.coach-panel__content h3 {
  margin: 18px 0 0;
  color: #111827;
  font-size: 22px;
  letter-spacing: 0;
}

.welcome-copy {
  margin: 8px 0 0;
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
}

.starter-chips {
  width: 100%;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
  margin-top: 26px;
}

.starter-chips button,
.guide-link,
.scope-button {
  border: 1px solid #d9e2ef;
  border-radius: 999px;
  background: #f8fafc;
  color: #374151;
  font-size: 12px;
  cursor: pointer;
  padding: 7px 10px;
}

.guide-link {
  margin-top: auto;
  background: #ffffff;
  color: #64748b;
}

.inline-nudge,
.conversation-nudge {
  width: 100%;
  margin-top: 22px;
  border: 1px solid #dbeafe;
  border-radius: 12px;
  background: #f8fbff;
  padding: 12px;
  text-align: left;
}

.inline-nudge span,
.conversation-nudge span {
  display: block;
  margin-bottom: 4px;
  color: #2563eb;
  font-size: 12px;
  font-weight: 700;
}

.inline-nudge strong,
.conversation-nudge strong {
  color: #172033;
  font-size: 14px;
}

.inline-nudge p,
.conversation-nudge p {
  margin: 6px 0 0;
  color: #526072;
  font-size: 13px;
  line-height: 1.6;
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 9px;
  margin-bottom: 14px;
}

.message-row.user {
  justify-content: flex-end;
}

.message-avatar {
  width: 28px;
  height: 28px;
  flex: 0 0 auto;
  border-radius: 50%;
  background: #eef6ff;
  color: #2563eb;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 800;
}

.message-bubble {
  max-width: 78%;
  border: 1px solid #e5e7eb;
  border-radius: 14px 14px 14px 4px;
  background: #f8fafc;
  color: #172033;
  padding: 11px 12px;
  font-size: 13px;
  line-height: 1.68;
  text-align: left;
  white-space: pre-wrap;
  word-break: break-word;
}

.message-row.user .message-bubble {
  border-color: #2563eb;
  border-radius: 14px 14px 4px 14px;
  background: #2563eb;
  color: #ffffff;
}

.message-bubble.muted {
  color: #64748b;
}

.inline-action {
  border: 0;
  background: transparent;
  color: #2563eb;
  cursor: pointer;
  font: inherit;
  font-weight: 700;
  padding: 0;
}

.inline-action:hover {
  text-decoration: underline;
}

.conversation-nudge {
  margin: 8px 0 16px 37px;
  width: auto;
  max-width: 78%;
}

.conversation-nudge__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.coach-composer {
  flex: 0 0 auto;
  margin: 0 20px 18px;
  border: 2px solid #111827;
  border-radius: 10px;
  padding: 10px 12px;
  background: #ffffff;
}

.coach-composer textarea {
  width: 100%;
  min-height: 42px;
  border: 0;
  resize: none;
  outline: none;
  color: #172033;
  font: inherit;
  font-size: 14px;
  line-height: 1.5;
}

.composer-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.scope-button {
  border: 0;
  background: transparent;
  color: #2563eb;
  padding: 4px 0;
}

.send-button {
  width: 26px;
  height: 26px;
  border: 0;
  border-radius: 50%;
  background: #dbeafe;
  color: #2563eb;
  font-weight: 800;
  cursor: pointer;
}

.send-button:disabled {
  background: #e5e7eb;
  color: #ffffff;
  cursor: not-allowed;
}

@media (max-width: 640px) {
  .global-coach {
    right: 14px;
    bottom: 14px;
  }

  .coach-nudge,
  .coach-panel {
    width: min(360px, calc(100vw - 28px));
  }

  .coach-panel {
    height: min(680px, calc(100vh - 28px));
  }

  .coach-panel__content.welcome {
    padding-top: 48px;
  }

  .coach-fab {
    width: 52px;
    height: 52px;
  }
}
</style>
