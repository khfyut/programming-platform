<template>
  <el-dialog
    v-model="visible"
    width="min(960px, 94vw)"
    :close-on-click-modal="false"
    class="ai-dialog-pro"
    destroy-on-close
    :show-close="true"
  >
    <template #header>
      <div class="dialog-header">
        <div class="header-left">
          <div class="ai-avatar">
            <el-icon><ChatDotRound /></el-icon>
          </div>
          <div class="header-info">
            <h3 class="dialog-title">AI 编程助手</h3>
            <span class="dialog-status" :class="loading ? 'thinking' : 'ready'">
              {{ loading ? '正在思考…' : '在线' }}
            </span>
          </div>
        </div>
      </div>
    </template>

    <div class="ai-dialog-content">
      <div ref="messagesContainer" class="chat-messages">
        <div
          v-for="(message, index) in messages"
          :key="index"
          :class="['message-wrapper', message.role]"
        >
          <div v-if="message.role === 'assistant'" class="avatar-wrapper">
            <div class="ai-avatar-small">
              <el-icon><ChatDotRound /></el-icon>
            </div>
          </div>

          <div class="message-content">
            <div v-if="message.role === 'user'" class="user-avatar">
              <div class="user-avatar-inner">
                <el-icon><User /></el-icon>
              </div>
            </div>
            <div class="message-bubble">
              <div class="message-text" v-html="formatMessage(message.content)"></div>
            </div>
          </div>
        </div>

        <div v-if="loading && !typingMessage" class="message-wrapper assistant">
          <div class="avatar-wrapper">
            <div class="ai-avatar-small">
              <el-icon><ChatDotRound /></el-icon>
            </div>
          </div>
          <div class="message-content">
            <div class="message-bubble">
              <div class="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        </div>

        <div v-if="typingMessage" class="message-wrapper assistant">
          <div class="avatar-wrapper">
            <div class="ai-avatar-small">
              <el-icon><ChatDotRound /></el-icon>
            </div>
          </div>
          <div class="message-content">
            <div class="message-bubble">
              <div class="message-text" v-html="formatMessage(typingMessage.displayContent)"></div>
              <span class="typing-cursor"></span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { askAI } from '@/api/ai'
import { ElMessage } from 'element-plus'
import { ChatDotRound, User } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  initialPrompt: {
    type: String,
    default: ''
  },
  initialCode: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue'])

const visible = ref(false)
const messages = ref([])
const loading = ref(false)
const typingMessage = ref(null)
const typingTimer = ref(null)
const messagesContainer = ref(null)

watch(
  () => props.modelValue,
  (newVal) => {
    visible.value = newVal
    if (newVal && props.initialPrompt && props.initialCode) {
      nextTick(() => {
        sendInitialMessage()
      })
    }
  }
)

watch(visible, (newVal) => {
  emit('update:modelValue', newVal)
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
    .replace(/^### (.+)$/gm, '<h4 class="message-heading">$1</h4>')
    .replace(/^## (.+)$/gm, '<h3 class="message-heading">$1</h3>')
    .replace(/^# (.+)$/gm, '<h2 class="message-heading">$1</h2>')
    .replace(/\n/g, '<br>')

  codeBlocks.forEach((block, index) => {
    const token = `__CODE_BLOCK_${index}__`
    processed = processed.replace(
      token,
      `
      <div class="code-block-wrapper">
        <div class="code-block-header"><span class="code-lang">${block.lang}</span></div>
        <pre class="code-block"><code>${block.code}</code></pre>
      </div>
      `
    )
  })

  return processed
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const stopTyping = () => {
  if (typingTimer.value) {
    clearTimeout(typingTimer.value)
    typingTimer.value = null
  }
}

const startTyping = (fullContent) => {
  return new Promise((resolve) => {
    const messageId = Date.now()
    typingMessage.value = {
      id: messageId,
      fullContent,
      displayContent: '',
      currentIndex: 0
    }

    const typeNext = () => {
      if (!typingMessage.value || typingMessage.value.id !== messageId) {
        return
      }

      if (typingMessage.value.currentIndex < typingMessage.value.fullContent.length) {
        typingMessage.value.displayContent += typingMessage.value.fullContent[typingMessage.value.currentIndex]
        typingMessage.value.currentIndex += 1
        scrollToBottom()
        typingTimer.value = setTimeout(typeNext, 18)
      } else {
        resolve()
      }
    }

    typeNext()
  })
}

const sendInitialMessage = async () => {
  stopTyping()
  typingMessage.value = null

  const userMessage = {
    role: 'user',
    content: `${props.initialPrompt}\n\n\`\`\`\n${props.initialCode}\n\`\`\``
  }

  messages.value = [userMessage]
  await scrollToBottom()

  loading.value = true
  try {
    const res = await askAI({
      content: userMessage.content,
      history: []
    })

    if (res.code === 200) {
      loading.value = false
      const fullContent = res.data || '抱歉，我暂时无法回答这个问题。'
      await startTyping(fullContent)

      if (typingMessage.value) {
        messages.value.push({
          role: 'assistant',
          content: typingMessage.value.fullContent
        })
        typingMessage.value = null
      }
    } else {
      ElMessage.error(res.msg || '发送失败')
    }
  } catch (error) {
    console.error('AI 请求失败:', error)
    ElMessage.error('发送失败，请重试')
  } finally {
    loading.value = false
    stopTyping()
    await scrollToBottom()
  }
}

onBeforeUnmount(() => {
  stopTyping()
})
</script>

<style scoped>
.ai-dialog-pro :deep(.el-dialog) {
  width: min(960px, 94vw);
  max-width: 960px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
}

.ai-dialog-pro :deep(.el-dialog__header) {
  padding: 0;
  margin: 0;
  border-bottom: 1px solid var(--leetcode-border, #e5e7eb);
}

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.ai-avatar {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.3) 0%, rgba(255, 255, 255, 0.1) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-avatar .el-icon {
  font-size: 24px;
  color: white;
}

.header-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.dialog-title {
  font-size: 18px;
  font-weight: 700;
  color: white;
  margin: 0;
}

.dialog-status {
  font-size: 13px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
}

.dialog-status.ready {
  color: rgba(255, 255, 255, 0.85);
}

.dialog-status.thinking {
  color: rgba(255, 255, 255, 0.95);
  animation: pulse 1.5s ease-in-out infinite;
}

.dialog-status::before {
  content: '';
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: currentColor;
}

.dialog-status.ready::before {
  background: #00c853;
}

.dialog-status.thinking::before {
  background: #ffb300;
}

.ai-dialog-pro :deep(.el-dialog__body) {
  padding: 0;
  max-height: min(78vh, 920px);
  overflow: hidden;
}

.ai-dialog-content {
  height: min(600px, 72vh);
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #fafbff 0%, #ffffff 100%);
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 28px 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.message-wrapper {
  display: flex;
  width: 100%;
  gap: 12px;
}

.message-wrapper.user {
  flex-direction: row-reverse;
}

.avatar-wrapper,
.user-avatar {
  flex-shrink: 0;
  padding-top: 4px;
}

.ai-avatar-small,
.user-avatar-inner {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-avatar-small {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.user-avatar-inner {
  background: linear-gradient(135deg, #00c853 0%, #00a845 100%);
  color: white;
}

.message-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  max-width: 80%;
}

.message-wrapper.user .message-content {
  align-items: flex-end;
}

.message-wrapper.assistant .message-content {
  align-items: flex-start;
}

.message-bubble {
  padding: 16px 20px;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.message-wrapper.user .message-bubble {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-bottom-right-radius: 4px;
}

.message-wrapper.assistant .message-bubble {
  background: white;
  color: var(--leetcode-text, #24292f);
  border: 1px solid var(--leetcode-border, #e5e7eb);
  border-bottom-left-radius: 4px;
}

.message-text {
  line-height: 1.7;
  font-size: 14px;
  word-break: break-word;
}

.message-text :deep(.code-block-wrapper) {
  margin: 16px 0;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.message-text :deep(.code-block-header) {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  padding: 10px 16px;
}

.message-text :deep(.code-lang) {
  color: rgba(255, 255, 255, 0.7);
  font-size: 12px;
  font-weight: 600;
}

.message-text :deep(.code-block) {
  background: #0d1117;
  padding: 16px;
  margin: 0;
  overflow-x: auto;
}

.message-text :deep(.code-block code) {
  font-family: 'Fira Code', 'Monaco', 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.7;
  color: #c9d1d9;
}

.message-text :deep(.inline-code) {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  padding: 3px 8px;
  border-radius: 4px;
  font-family: 'Fira Code', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
}

.message-wrapper.user .message-text :deep(.inline-code) {
  background: rgba(255, 255, 255, 0.15);
  color: white;
}

.message-text :deep(.message-heading) {
  margin: 16px 0 10px;
  font-weight: 700;
}

.typing-indicator {
  display: flex;
  gap: 6px;
  align-items: center;
  padding: 12px 0;
}

.typing-indicator span {
  width: 10px;
  height: 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  animation: typing 1.4s ease-in-out infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

.typing-cursor {
  display: inline-block;
  width: 3px;
  height: 18px;
  background: #667eea;
  margin-left: 2px;
  vertical-align: middle;
  animation: cursorBlink 1s infinite;
  border-radius: 2px;
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

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.6;
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

@media (max-width: 768px) {
  .dialog-header {
    padding: 16px 18px;
  }

  .chat-messages {
    padding: 20px 16px;
    gap: 18px;
  }

  .message-content {
    max-width: 100%;
  }

  .message-bubble {
    max-width: 100%;
  }
}
</style>
