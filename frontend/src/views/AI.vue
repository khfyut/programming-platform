<template>
  <div class="ai-page">
    <div class="ai-shell">
      <aside class="sidebar">
        <div class="sidebar-header">
          <div>
            <p class="sidebar-kicker">AI Workspace</p>
            <h2>对话历史</h2>
          </div>
          <el-button type="primary" size="small" @click="createNewSession">
            <el-icon><Plus /></el-icon>
            新对话
          </el-button>
        </div>

        <div class="mode-strip">
          <button
            v-for="mode in expertModes"
            :key="mode.key"
            type="button"
            :class="['mode-pill', { active: currentExpertMode === mode.key }]"
            @click="switchExpertMode(mode.key)"
          >
            <el-icon><component :is="mode.icon" /></el-icon>
            <span>{{ mode.label }}</span>
          </button>
        </div>

        <div class="session-list" v-loading="sessionsLoading">
          <div
            v-for="session in sessions"
            :key="getSessionKey(session)"
            :class="['session-item', { active: currentSessionId === getSessionId(session) }]"
            role="button"
            tabindex="0"
            @click="switchSession(getSessionId(session))"
            @keydown.enter.prevent="switchSession(getSessionId(session))"
            @keydown.space.prevent="switchSession(getSessionId(session))"
          >
            <div class="session-info">
              <div class="session-title">{{ getSessionTitle(session) }}</div>
              <div class="session-time">{{ formatSessionTime(session.updateTime || session.createTime) }}</div>
            </div>
            <el-button type="danger" size="small" text @click.stop="deleteSession(session)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>

          <el-empty v-if="sessions.length === 0 && !sessionsLoading" description="暂无对话记录">
            <template #description>
              <p class="empty-side-text">开始第一轮提问后，会话会自动沉淀在这里。</p>
            </template>
          </el-empty>
        </div>
      </aside>

      <section class="chat-section">
        <div class="chat-header">
          <div>
            <p class="chat-kicker">Assistant</p>
            <h1>AI 编程助手</h1>
            <p class="chat-subtitle">
              你可以围绕题目、代码、提交结果和概念理解继续追问，不用每次都重新描述上下文。
            </p>
          </div>
          <div class="chat-header-right">
            <span v-if="currentModeLabel" class="mode-badge">当前模式：{{ currentModeLabel }}</span>
            <div class="mode-strip inline-mode-strip">
              <button
                v-for="mode in expertModes"
                :key="mode.key"
                type="button"
                :class="['mode-pill', { active: currentExpertMode === mode.key }]"
                @click="switchExpertMode(mode.key)"
              >
                <el-icon><component :is="mode.icon" /></el-icon>
                <span>{{ mode.label }}</span>
              </button>
            </div>
            <el-button type="primary" plain @click="createNewSession">
              <el-icon><Plus /></el-icon>
              新对话
            </el-button>
            <el-button text @click="clearMessages" :disabled="messages.length === 0 && !currentSessionId">
              <el-icon><Delete /></el-icon>
              清空当前对话
            </el-button>
          </div>
        </div>

        <div ref="messagesContainer" class="chat-messages">
          <div v-if="messages.length === 0" class="empty-chat">
            <div class="empty-icon-wrapper">
              <el-icon class="empty-icon"><ChatDotRound /></el-icon>
            </div>
            <h3>先抛出一个具体问题，会更高效</h3>
            <p>比如直接贴报错、卡住的思路、复杂度疑问，或者你已经尝试过的方案。</p>

            <div class="quick-prompts">
              <button
                v-for="prompt in quickPrompts"
                :key="prompt"
                type="button"
                class="prompt-card"
                @click="applyPrompt(prompt)"
              >
                {{ prompt }}
              </button>
            </div>
          </div>

          <div
            v-for="(message, index) in messages"
            :key="`${message.role}-${index}-${message.time || ''}`"
            :class="['message-wrapper', message.role]"
          >
            <div class="message-content">
              <div v-if="message.role === 'assistant'" class="message-header">
                <div class="ai-avatar">
                  <el-icon><ChatDotRound /></el-icon>
                </div>
                <span class="ai-name">AI 助手</span>
                <span class="message-time">{{ formatMessageTime(message.time) }}</span>
              </div>
              <div v-else class="message-header user-header">
                <span class="message-time">{{ formatMessageTime(message.time) }}</span>
                <div class="user-avatar">
                  <el-icon><User /></el-icon>
                </div>
              </div>

              <div
                v-if="message.role === 'assistant' && message.streaming && !message.content"
                class="message-status"
              >
                {{ streamStatus || '正在连接 AI...' }}
              </div>
              <div class="message-text" v-html="formatMessage(message.content)"></div>
              <span
                v-if="message.role === 'assistant' && message.streaming && message.content"
                class="stream-cursor"
              ></span>

              <div v-if="message.role === 'assistant' && !message.streaming" class="message-actions">
                <el-button size="small" text @click="copyMessage(message.content)">
                  <el-icon><DocumentCopy /></el-icon>
                  复制
                </el-button>
                <el-button size="small" text @click="regenerate(index)">
                  <el-icon><Refresh /></el-icon>
                  重新生成
                </el-button>
              </div>
            </div>
          </div>

          <div v-if="loading && !hasStreamingMessage" class="message-wrapper assistant">
            <div class="message-content">
              <div class="message-header">
                <div class="ai-avatar">
                  <el-icon><ChatDotRound /></el-icon>
                </div>
                <span class="ai-name">AI 助手</span>
              </div>
              <div class="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        </div>

        <div class="chat-input">
          <div class="input-container">
            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="4"
              placeholder="输入你的问题，例如：为什么这次提交超时了？或者这段代码哪里还可以继续优化？"
              @keydown.ctrl.enter="sendMessage"
              class="message-input"
            />

            <div class="input-actions">
              <div class="action-left">
                <span class="hint">Ctrl + Enter 发送</span>
              </div>
              <div class="action-right">
                <el-button text @click="applyPrompt('请帮我分析这段代码为什么没有通过。')">
                  插入示例问题
                </el-button>
                <el-button
                  type="primary"
                  @click="sendMessage"
                  :loading="loading"
                  :disabled="!inputMessage.trim() || loading"
                >
                  <el-icon><Promotion /></el-icon>
                  发送
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { deleteAIHistory, getAIHistory, getSessionMessages, streamAIChat } from '@/api/ai'
import { createTypewriterController } from '@/utils/typewriter'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ChatDotRound,
  Delete,
  DocumentCopy,
  Plus,
  Promotion,
  Reading,
  Refresh,
  TrendCharts,
  User,
  Warning
} from '@element-plus/icons-vue'

const messages = ref([])
const route = useRoute()
const router = useRouter()
const inputMessage = ref('')
const loading = ref(false)
const messagesContainer = ref(null)
const sessions = ref([])
const sessionsLoading = ref(false)
const currentSessionId = ref(null)
const currentExpertMode = ref('')
const streamStatus = ref('')
const activeStreamController = ref(null)

const expertModes = [
  { key: 'algorithm', label: '算法优化', icon: TrendCharts },
  { key: 'debug', label: '代码调试', icon: Warning },
  { key: 'concept', label: '概念解释', icon: Reading },
  { key: 'refactor', label: '代码重构', icon: Refresh }
]

const quickPrompts = [
  '请帮我分析这段代码为什么没有通过。',
  '这道题更适合用什么数据结构？',
  '帮我解释一下这个算法的时间复杂度。',
  '如果我要重构这段代码，应该先改哪里？'
]

const currentModeLabel = computed(() => {
  return expertModes.find((mode) => mode.key === currentExpertMode.value)?.label || ''
})

const hasStreamingMessage = computed(() => {
  return messages.value.some((message) => message.role === 'assistant' && message.streaming)
})

const escapeHtml = (text) => {
  const map = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;'
  }
  return String(text || '').replace(/[&<>"']/g, (char) => map[char])
}

const formatMessage = (content) => {
  if (!content) return ''

  const codeBlocks = []
  let processed = String(content).replace(/```(\w+)?\n?([\s\S]*?)```/g, (_, lang, code) => {
    const token = `__CODE_BLOCK_${codeBlocks.length}__`
    codeBlocks.push({
      lang: escapeHtml(lang || 'code'),
      code: escapeHtml(code || '')
    })
    return token
  })

  processed = escapeHtml(processed)
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
    .replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>')
    .replace(/\n/g, '<br>')

  codeBlocks.forEach((block, index) => {
    const token = `__CODE_BLOCK_${index}__`
    const html = `
      <div class="code-block-wrapper">
        <div class="code-header"><span class="code-lang">${block.lang}</span></div>
        <pre class="code-block"><code>${block.code}</code></pre>
      </div>
    `
    processed = processed.replace(token, html)
  })

  return processed
}

const normalizeMessageList = (data) => {
  const list = Array.isArray(data) ? data : data?.list || data?.messages || data?.records || []
  return list.map((item) => ({
    role: item.role === 'assistant' ? 'assistant' : 'user',
    content: item.content || item.message || item.response || '',
    time: item.time || item.createTime || item.updateTime || new Date().toISOString()
  }))
}

const normalizeSessionList = (data) => {
  const list = Array.isArray(data) ? data : data?.list || data?.records || data?.items || []
  return list.map((item) => ({
    ...item,
    id: item.id ?? item.sessionId,
    sessionId: item.sessionId ?? item.id,
    topic: item.topic || item.title || item.firstQuestion || '新对话'
  }))
}

const getSessionKey = (session) => session?.sessionId || session?.id || Math.random()
const getSessionId = (session) => session?.sessionId || session?.id || null
const getSessionTitle = (session) => session?.topic || session?.title || '新对话'

const formatSessionTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  if (Number.isNaN(date.getTime())) return ''

  const diff = Date.now() - date.getTime()
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)} 天前`
  return date.toLocaleDateString('zh-CN')
}

const formatMessageTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  if (Number.isNaN(date.getTime())) return ''
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const typewriter = createTypewriterController({
  onTick: scrollToBottom
})

const enqueueAssistantText = (targetMessage, text) => {
  typewriter.enqueue(targetMessage, text)
}

const waitForTypewriter = () => {
  return typewriter.waitForIdle()
}

const stopTypewriter = (options) => {
  typewriter.stop(options)
}

const applyPrompt = (prompt) => {
  inputMessage.value = prompt
}

const syncRouteSessionId = (sessionId) => {
  if (!sessionId || String(route.query.sessionId || '') === String(sessionId)) {
    return
  }
  router.replace({ path: '/ai', query: { sessionId } })
}

const switchExpertMode = (mode) => {
  if (currentExpertMode.value === mode) {
    currentExpertMode.value = ''
    ElMessage.info('已取消专家模式')
    return
  }

  currentExpertMode.value = mode
  const target = expertModes.find((item) => item.key === mode)
  ElMessage.success(`已切换到 ${target?.label || '专家'} 模式`)
}

const sendMessage = async () => {
  const currentInput = inputMessage.value.trim()
  if (!currentInput) {
    ElMessage.warning('请输入消息')
    return
  }

  stopTypewriter()
  messages.value.push({
    role: 'user',
    content: currentInput,
    time: new Date().toISOString()
  })

  const assistantMessage = {
    role: 'assistant',
    content: '',
    time: new Date().toISOString(),
    streaming: true
  }
  messages.value.push(assistantMessage)

  inputMessage.value = ''
  await scrollToBottom()

  loading.value = true
  streamStatus.value = '正在连接 AI...'
  activeStreamController.value?.abort?.()
  const controller = new AbortController()
  activeStreamController.value = controller
  let streamError = ''
  try {
    await streamAIChat({
      content: currentInput,
      sessionId: currentSessionId.value,
      expertMode: currentExpertMode.value
    }, {
      signal: controller.signal,
      onStatus(data) {
        streamStatus.value = data?.text || 'AI 正在工作...'
      },
      onMeta(data) {
        if (data?.sessionId) {
          currentSessionId.value = data.sessionId
          syncRouteSessionId(data.sessionId)
        }
      },
      onDelta(data) {
        streamStatus.value = ''
        enqueueAssistantText(assistantMessage, data?.text || data || '')
      },
      onError(data) {
        streamError = data?.message || 'AI 响应失败，请稍后重试'
      },
      onDone(data) {
        if (data?.sessionId) {
          currentSessionId.value = data.sessionId
          syncRouteSessionId(data.sessionId)
        }
      }
    })

    if (streamError) {
      throw new Error(streamError)
    }
    await waitForTypewriter()
    assistantMessage.streaming = false
    if (!assistantMessage.content) {
      assistantMessage.content = '抱歉，我暂时无法回答这个问题。'
    }
    window.dispatchEvent(new CustomEvent('ai-sessions-refresh'))
  } catch (error) {
    console.error('AI 请求失败:', error)
    stopTypewriter()
    assistantMessage.streaming = false
    if (!assistantMessage.content) {
      assistantMessage.content = error?.message || '发送失败，请重试。'
    }
    ElMessage.error(error?.message || '发送失败，请重试')
  } finally {
    if (activeStreamController.value === controller) {
      activeStreamController.value = null
    }
    loading.value = false
    streamStatus.value = ''
    await scrollToBottom()
  }
}

const copyMessage = async (content) => {
  try {
    await navigator.clipboard.writeText(content || '')
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败')
  }
}

const regenerate = async (index) => {
  if (index === 0 || messages.value[index - 1]?.role !== 'user') {
    ElMessage.warning('当前消息无法重新生成')
    return
  }

  const previousUserMessage = messages.value[index - 1]
  messages.value = messages.value.slice(0, index)
  inputMessage.value = previousUserMessage.content
  await sendMessage()
}

const clearMessages = async () => {
  if (messages.value.length === 0 && !currentSessionId.value) {
    return
  }

  try {
    await ElMessageBox.confirm(
      '确定要开始一段新的对话吗？当前界面的上下文会被清空。',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    stopTypewriter()
    messages.value = []
    currentSessionId.value = null
    ElMessage.success('已清空当前对话')
  } catch {
    return
  }
}

const createNewSession = () => {
  stopTypewriter()
  messages.value = []
  currentSessionId.value = null
  inputMessage.value = ''
  if (route.query.sessionId) {
    router.replace('/ai')
  }
  ElMessage.success('已创建新对话')
}

const switchSession = async (sessionId) => {
  if (!sessionId || String(currentSessionId.value || '') === String(sessionId)) return

  stopTypewriter()
  currentSessionId.value = sessionId
  messages.value = []

  try {
    const res = await getSessionMessages(sessionId)
    if (res.code === 200) {
      messages.value = normalizeMessageList(res.data)
      await scrollToBottom()
    } else {
      ElMessage.error(res.msg || '加载对话失败')
    }
  } catch (error) {
    console.error('加载对话失败:', error)
    ElMessage.error('加载对话失败')
  }
}

watch(
  () => route.query.sessionId,
  async (sessionId) => {
    if (!sessionId) {
      stopTypewriter()
      currentSessionId.value = null
      messages.value = []
      return
    }
    await switchSession(sessionId)
  },
  { immediate: true }
)

const deleteSession = async (session) => {
  const targetId = session?.id || session?.sessionId
  if (!targetId) return

  try {
    await ElMessageBox.confirm('确定要删除这段对话吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await deleteAIHistory(targetId)
    if (res?.code !== 200) {
      ElMessage.error(res?.msg || '删除失败')
      return
    }

    sessions.value = sessions.value.filter((item) => getSessionKey(item) !== getSessionKey(session))

    if (currentSessionId.value === getSessionId(session)) {
      currentSessionId.value = null
      messages.value = []
    }

    ElMessage.success('对话已删除')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const fetchSessions = async () => {
  sessionsLoading.value = true
  try {
    const res = await getAIHistory({ page: 1, size: 20 })
    if (res.code === 200) {
      sessions.value = normalizeSessionList(res.data)
    } else {
      sessions.value = []
    }
  } catch (error) {
    console.error('获取对话列表失败:', error)
    sessions.value = []
  } finally {
    sessionsLoading.value = false
  }
}

onMounted(async () => {
  await scrollToBottom()
})

onBeforeUnmount(() => {
  activeStreamController.value?.abort?.()
  stopTypewriter()
})
</script>

<style scoped>
.ai-page {
  min-height: 100vh;
  padding: 24px;
  background:
    radial-gradient(circle at top left, rgba(0, 102, 255, 0.06), transparent 22%),
    var(--leetcode-bg-secondary, #f7f8fa);
}

.ai-shell {
  max-width: 1400px;
  height: calc(100vh - 120px);
  margin: 0 auto;
  display: flex;
  overflow: hidden;
  border: 1px solid var(--leetcode-border, #e5e7eb);
  border-radius: 24px;
  background: var(--leetcode-bg, #ffffff);
  box-shadow: 0 24px 70px rgba(15, 23, 42, 0.06);
}

.sidebar {
  display: none;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  padding: 20px;
  border-bottom: 1px solid var(--leetcode-border, #e5e7eb);
}

.sidebar-kicker,
.chat-kicker {
  margin: 0 0 8px;
  color: #1668dc;
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.sidebar-header h2,
.chat-header h1 {
  margin: 0;
  font-size: 20px;
  color: var(--leetcode-text, #24292f);
}

.mode-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 16px 20px;
  border-bottom: 1px solid var(--leetcode-border, #e5e7eb);
}

.mode-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  border: 1px solid var(--leetcode-border, #e5e7eb);
  border-radius: 999px;
  background: var(--leetcode-bg, #ffffff);
  color: var(--leetcode-text-secondary, #6b7280);
  cursor: pointer;
  transition: all 0.2s ease;
}

.mode-pill.active,
.mode-pill:hover {
  border-color: #1668dc;
  background: rgba(22, 104, 220, 0.08);
  color: #1668dc;
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.session-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  margin-bottom: 8px;
  padding: 12px;
  border: 1px solid transparent;
  border-radius: 14px;
  background: transparent;
  text-align: left;
  cursor: pointer;
  transition: all 0.2s ease;
}

.session-item:hover {
  background: var(--leetcode-bg, #ffffff);
  border-color: rgba(22, 104, 220, 0.08);
}

.session-item:focus-visible {
  outline: 2px solid rgba(22, 104, 220, 0.28);
  outline-offset: 2px;
}

.session-item.active {
  background: linear-gradient(135deg, rgba(22, 104, 220, 0.14), rgba(0, 102, 255, 0.08));
  border-color: rgba(22, 104, 220, 0.16);
}

.session-info {
  min-width: 0;
  flex: 1;
}

.session-title {
  color: var(--leetcode-text, #24292f);
  font-size: 14px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-time {
  margin-top: 4px;
  color: var(--leetcode-text-secondary, #6b7280);
  font-size: 12px;
}

.empty-side-text {
  margin: 0;
  line-height: 1.7;
}

.chat-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 22px 24px;
  border-bottom: 1px solid var(--leetcode-border, #e5e7eb);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(255, 255, 255, 0.94));
}

.chat-subtitle {
  max-width: 680px;
  margin: 8px 0 0;
  color: var(--leetcode-text-secondary, #6b7280);
  font-size: 14px;
  line-height: 1.7;
}

.chat-header-right {
  display: flex;
  align-items: flex-start;
  justify-content: flex-end;
  gap: 12px;
  flex-wrap: wrap;
  max-width: 680px;
}

.inline-mode-strip {
  justify-content: flex-end;
  padding: 0;
  border-bottom: 0;
}

.mode-badge {
  display: inline-flex;
  align-items: center;
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(22, 104, 220, 0.08);
  color: #1668dc;
  font-size: 12px;
  font-weight: 600;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.empty-chat {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 40px;
  text-align: center;
}

.empty-icon-wrapper {
  width: 84px;
  height: 84px;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 28px;
  background: linear-gradient(135deg, #0066ff 0%, #66b3ff 100%);
  box-shadow: 0 18px 40px rgba(0, 102, 255, 0.22);
}

.empty-icon {
  color: white;
  font-size: 38px;
}

.empty-chat h3 {
  margin: 0;
  color: var(--leetcode-text, #24292f);
  font-size: 24px;
}

.empty-chat p {
  max-width: 520px;
  margin: 12px 0 0;
  color: var(--leetcode-text-secondary, #6b7280);
  line-height: 1.8;
}

.quick-prompts {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  width: min(720px, 100%);
  margin-top: 28px;
}

.prompt-card {
  padding: 16px;
  border: 1px solid var(--leetcode-border, #e5e7eb);
  border-radius: 16px;
  background: var(--leetcode-bg, #ffffff);
  color: var(--leetcode-text, #24292f);
  line-height: 1.7;
  text-align: left;
  cursor: pointer;
  transition: all 0.2s ease;
}

.prompt-card:hover {
  border-color: #1668dc;
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(22, 104, 220, 0.08);
}

.message-wrapper {
  display: flex;
  width: 100%;
}

.message-wrapper.user {
  justify-content: flex-end;
}

.message-wrapper.assistant {
  justify-content: flex-start;
}

.message-content {
  max-width: min(820px, 82%);
  padding: 16px 20px;
  border-radius: 18px;
}

.message-wrapper.user .message-content {
  background: linear-gradient(135deg, #0066ff, #3b82f6);
  color: white;
  border-bottom-right-radius: 6px;
}

.message-wrapper.assistant .message-content {
  border: 1px solid var(--leetcode-border, #e5e7eb);
  background: var(--leetcode-bg-secondary, #f7f8fa);
  color: var(--leetcode-text, #24292f);
  border-bottom-left-radius: 6px;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 13px;
}

.user-header {
  justify-content: flex-end;
}

.ai-avatar,
.user-avatar {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.ai-avatar {
  background: linear-gradient(135deg, #0066ff 0%, #66b3ff 100%);
  color: white;
}

.user-avatar {
  background: #16a34a;
  color: white;
}

.ai-name {
  color: var(--leetcode-text, #24292f);
  font-weight: 600;
}

.message-time {
  color: rgba(107, 114, 128, 0.92);
  font-size: 12px;
}

.message-wrapper.user .message-time {
  color: rgba(255, 255, 255, 0.84);
}

.message-text {
  font-size: 14px;
  line-height: 1.8;
  word-break: break-word;
}

.message-status {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: var(--leetcode-text-secondary, #6b7280);
  font-size: 13px;
  line-height: 1.6;
}

.message-status::before {
  content: '';
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #1668dc;
  animation: typing 1.2s infinite;
}

.stream-cursor {
  display: inline-block;
  width: 2px;
  height: 16px;
  margin-left: 2px;
  border-radius: 2px;
  background: #1668dc;
  vertical-align: middle;
  animation: cursorBlink 1s infinite;
}

.message-text :deep(.code-block-wrapper) {
  margin: 12px 0;
  overflow: hidden;
  border: 1px solid var(--leetcode-border, #e5e7eb);
  border-radius: 12px;
  background: white;
}

.message-text :deep(.code-header) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  border-bottom: 1px solid var(--leetcode-border, #e5e7eb);
  background: var(--leetcode-bg-secondary, #f7f8fa);
}

.message-text :deep(.code-lang) {
  color: var(--leetcode-text-secondary, #6b7280);
  font-size: 12px;
  font-weight: 600;
}

.message-text :deep(.code-block) {
  margin: 0;
  overflow-x: auto;
  padding: 12px;
  background: #0f172a;
}

.message-text :deep(.code-block code) {
  display: block;
  color: #e2e8f0;
  font-family: 'Fira Code', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
}

.message-text :deep(.inline-code) {
  padding: 2px 6px;
  border-radius: 4px;
  background: rgba(0, 0, 0, 0.06);
  font-family: 'Fira Code', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
}

.message-wrapper.user .message-text :deep(.inline-code) {
  background: rgba(255, 255, 255, 0.18);
}

.message-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--leetcode-border, #e5e7eb);
}

.typing-indicator {
  display: flex;
  gap: 6px;
  align-items: center;
  padding: 8px 0;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #1668dc;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }

  30% {
    transform: translateY(-8px);
    opacity: 1;
  }
}

@keyframes cursorBlink {
  0%, 50% {
    opacity: 1;
  }

  51%, 100% {
    opacity: 0;
  }
}

.chat-input {
  padding: 20px 24px;
  border-top: 1px solid var(--leetcode-border, #e5e7eb);
  background: var(--leetcode-bg, #ffffff);
}

.input-container {
  max-width: 960px;
  margin: 0 auto;
}

.message-input {
  margin-bottom: 12px;
}

:deep(.message-input .el-textarea__inner) {
  min-height: 108px;
  padding: 14px 16px;
  border-radius: 14px;
  border: 1px solid var(--leetcode-border, #e5e7eb);
  background: var(--leetcode-bg-secondary, #f7f8fa);
  line-height: 1.7;
  resize: none;
}

:deep(.message-input .el-textarea__inner:focus) {
  border-color: #1668dc;
  box-shadow: 0 0 0 3px rgba(22, 104, 220, 0.1);
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.action-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.hint {
  color: var(--leetcode-text-secondary, #6b7280);
  font-size: 12px;
}

@media (max-width: 1100px) {
  .ai-shell {
    height: auto;
    min-height: calc(100vh - 120px);
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid var(--leetcode-border, #e5e7eb);
  }

  .session-list {
    max-height: 240px;
  }
}

@media (max-width: 768px) {
  .ai-page {
    padding: 16px;
  }

  .chat-header,
  .input-actions {
    flex-direction: column;
    align-items: flex-start;
  }

  .quick-prompts {
    grid-template-columns: 1fr;
  }

  .message-content {
    max-width: 100%;
  }
}
</style>
