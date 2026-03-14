<template>
  <div class="leetcode-ai">
    <div class="ai-container">
      <div class="chat-section">
        <div class="chat-messages" ref="messagesContainer">
          <div v-if="messages.length === 0" class="empty-chat">
            <el-icon class="empty-icon"><ChatDotRound /></el-icon>
            <h3>AI 助手</h3>
            <p>有什么可以帮助您的吗?</p>
          </div>
          
          <div 
            v-for="(message, index) in messages" 
            :key="index"
            :class="['message-wrapper', message.role]"
          >
            <div class="message-content">
              <div v-if="message.role === 'assistant'" class="message-header">
                <el-icon class="ai-icon"><ChatDotRound /></el-icon>
                <span class="ai-name">AI 助手</span>
              </div>
              <div class="message-text" v-html="formatMessage(message.content)"></div>
            </div>
          </div>
          
          <div v-if="loading" class="message-wrapper assistant">
            <div class="message-content">
              <div class="message-header">
                <el-icon class="ai-icon"><ChatDotRound /></el-icon>
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
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="3"
            placeholder="输入您的问题..."
            @keydown.ctrl.enter="sendMessage"
            class="message-input"
          />
          <div class="input-actions">
            <span class="hint">Ctrl + Enter 发送</span>
            <el-button 
              type="primary" 
              @click="sendMessage"
              :loading="loading"
              :disabled="!inputMessage.trim()"
            >
              发送
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { askAI } from '@/api/ai'
import { ElMessage } from 'element-plus'
import { ChatDotRound } from '@element-plus/icons-vue'

const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const messagesContainer = ref(null)

const formatMessage = (content) => {
  if (!content) return ''
  return content
    .replace(/```(\w+)?\n([\s\S]*?)```/g, '<pre class="code-block"><code>$2</code></pre>')
    .replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>')
    .replace(/\n/g, '<br>')
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim()) {
    ElMessage.warning('请输入消息')
    return
  }

  const userMessage = {
    role: 'user',
    content: inputMessage.value.trim()
  }

  messages.value.push(userMessage)
  const currentInput = inputMessage.value
  inputMessage.value = ''
  
  await scrollToBottom()

  loading.value = true
  try {
    const res = await askAI({
      content: currentInput,
      history: messages.value.slice(0, -1)
    })

    if (res.code === 200) {
      const aiMessage = {
        role: 'assistant',
        content: res.data || '抱歉,我无法回答这个问题。'
      }
      messages.value.push(aiMessage)
    } else {
      ElMessage.error(res.msg || '发送失败')
    }
  } catch (error) {
    ElMessage.error('发送失败,请重试')
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}

onMounted(() => {
  scrollToBottom()
})
</script>

<style scoped>
.leetcode-ai {
  min-height: 100vh;
  background: var(--leetcode-bg-secondary, #F7F8FA);
  padding: 24px;
}

.ai-container {
  max-width: 1000px;
  margin: 0 auto;
  background: var(--leetcode-bg, #FFFFFF);
  border-radius: 8px;
  border: 1px solid var(--leetcode-border, #E5E7EB);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px);
}

.chat-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.empty-chat {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--leetcode-text-secondary, #6B7280);
  text-align: center;
}

.empty-icon {
  font-size: 64px;
  color: var(--leetcode-primary, #0066FF);
  margin-bottom: 16px;
}

.empty-chat h3 {
  font-size: 20px;
  font-weight: 600;
  color: var(--leetcode-text, #24292F);
  margin: 0 0 8px 0;
}

.empty-chat p {
  font-size: 14px;
  margin: 0;
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
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 8px;
  position: relative;
}

.message-wrapper.user .message-content {
  background: var(--leetcode-primary, #0066FF);
  color: white;
  border-bottom-right-radius: 2px;
}

.message-wrapper.assistant .message-content {
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border: 1px solid var(--leetcode-border, #E5E7EB);
  color: var(--leetcode-text, #24292F);
  border-bottom-left-radius: 2px;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 13px;
  font-weight: 600;
  color: var(--leetcode-text-secondary, #6B7280);
}

.ai-icon {
  color: var(--leetcode-primary, #0066FF);
  font-size: 16px;
}

.message-text {
  line-height: 1.6;
  font-size: 14px;
  word-wrap: break-word;
}

.message-text :deep(.code-block) {
  background: var(--leetcode-bg, #FFFFFF);
  padding: 12px;
  border-radius: 4px;
  border: 1px solid var(--leetcode-border, #E5E7EB);
  margin: 8px 0;
  overflow-x: auto;
}

.message-text :deep(.code-block code) {
  font-family: 'Fira Code', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.5;
  color: var(--leetcode-text, #24292F);
}

.message-text :deep(.inline-code) {
  background: rgba(0, 0, 0, 0.05);
  padding: 2px 6px;
  border-radius: 3px;
  font-family: 'Fira Code', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  align-items: center;
  padding: 8px 0;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background: var(--leetcode-text-secondary, #6B7280);
  border-radius: 50%;
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
    transform: translateY(-4px);
    opacity: 1;
  }
}

.chat-input {
  padding: 16px 24px;
  border-top: 1px solid var(--leetcode-border, #E5E7EB);
  background: var(--leetcode-bg, #FFFFFF);
}

.message-input {
  margin-bottom: 12px;
}

:deep(.message-input .el-textarea__inner) {
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border: 1px solid var(--leetcode-border, #E5E7EB);
  border-radius: 6px;
  padding: 12px;
  font-size: 14px;
  line-height: 1.5;
}

:deep(.message-input .el-textarea__inner:focus) {
  border-color: var(--leetcode-primary, #0066FF);
  box-shadow: 0 0 0 2px rgba(0, 102, 255, 0.1);
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.hint {
  font-size: 12px;
  color: var(--leetcode-text-secondary, #6B7280);
}

@media (max-width: 768px) {
  .leetcode-ai {
    padding: 16px;
  }

  .ai-container {
    height: calc(100vh - 100px);
  }

  .chat-messages {
    padding: 16px;
  }

  .message-content {
    max-width: 85%;
  }

  .chat-input {
    padding: 12px 16px;
  }
}
</style>
