<template>
  <div class="leetcode-ai">
    <div class="ai-container">
      <div class="sidebar">
        <div class="sidebar-header">
          <h3>对话历史</h3>
          <el-button type="primary" size="small" @click="createNewSession">
            <el-icon><Plus /></el-icon>
            新对话
          </el-button>
        </div>
        <div class="session-list" v-loading="sessionsLoading">
          <div
            v-for="session in sessions"
            :key="session.id"
            :class="['session-item', { active: currentSessionId === session.sessionId }]"
            @click="switchSession(session.sessionId)"
          >
            <div class="session-info">
              <div class="session-title">{{ session.topic || '新对话' }}</div>
              <div class="session-time">{{ formatSessionTime(session.updateTime) }}</div>
            </div>
            <el-button
              type="danger"
              size="small"
              text
              @click.stop="deleteSession(session)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
          <el-empty v-if="sessions.length === 0 && !sessionsLoading" description="暂无对话记录" />
        </div>
      </div>

      <div class="chat-section">
        <div class="chat-messages" ref="messagesContainer">
          <div v-if="messages.length === 0" class="empty-chat">
            <div class="empty-icon-wrapper">
              <el-icon class="empty-icon"><ChatDotRound /></el-icon>
            </div>
            <h3>AI 编程助手</h3>
            <p>我可以帮助您解答编程问题、调试代码、优化算法等</p>
            <div class="quick-actions">
              <div 
                v-for="mode in expertModes" 
                :key="mode.key"
                :class="['quick-action', { active: currentExpertMode === mode.key }]"
                @click="switchExpertMode(mode.key)"
              >
                <el-icon>
                  <component :is="mode.icon" />
                </el-icon>
                <span>{{ mode.label }}</span>
              </div>
            </div>
          </div>
          
          <div 
            v-for="(message, index) in messages" 
            :key="index"
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
              <div class="message-text" v-html="formatMessage(message.content)"></div>
              <div v-if="message.role === 'assistant'" class="message-actions">
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
          
          <div v-if="loading" class="message-wrapper assistant">
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
              :rows="3"
              placeholder="输入您的问题... (Ctrl + Enter 发送)"
              @keydown.ctrl.enter="sendMessage"
              class="message-input"
            />
            <div class="input-actions">
              <div class="action-left">
                <el-tooltip content="清空对话" placement="top">
                  <el-button text @click="clearMessages">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </el-tooltip>
              </div>
              <div class="action-right">
                <span class="hint">Ctrl + Enter 发送</span>
                <el-button 
                  type="primary" 
                  @click="sendMessage"
                  :loading="loading"
                  :disabled="!inputMessage.trim()"
                >
                  <el-icon><Promotion /></el-icon>
                  发送
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { chatAI, getAIHistory, deleteAIHistory, getSessionMessages } from '@/api/ai'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ChatDotRound, 
  Plus, 
  Delete, 
  DocumentCopy, 
  Refresh,
  TrendCharts,
  Warning,
  Reading,
  User,
  Promotion
} from '@element-plus/icons-vue'

const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const messagesContainer = ref(null)
const sessions = ref([])
const sessionsLoading = ref(false)
const currentSessionId = ref(null)
const currentExpertMode = ref('')
const expertModes = [
  { key: 'algorithm', label: '算法优化', icon: TrendCharts },
  { key: 'debug', label: '代码调试', icon: Warning },
  { key: 'concept', label: '概念解释', icon: Reading },
  { key: 'refactor', label: '代码重构', icon: Refresh }
]

const formatMessage = (content) => {
  if (!content) return ''
  return content
    .replace(/```(\w+)?\n([\s\S]*?)```/g, (match, lang, code) => {
      return `<pre class="code-block"><div class="code-header"><span class="code-lang">${lang || 'code'}</span><button class="copy-btn" onclick="navigator.clipboard.writeText(\`${code.replace(/`/g, '\\`')}\`)">复制</button></div><code>${escapeHtml(code)}</code></pre>`
    })
    .replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>')
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
    .replace(/\*([^*]+)\*/g, '<em>$1</em>')
    .replace(/\n/g, '<br>')
}

const escapeHtml = (text) => {
  const map = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;'
  }
  return text.replace(/[&<>"']/g, m => map[m])
}

const formatSessionTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  return date.toLocaleDateString()
}

const formatMessageTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const switchExpertMode = (mode) => {
  const modeInfo = expertModes.find(m => m.key === mode)
  if (currentExpertMode.value === mode) {
    currentExpertMode.value = ''
    ElMessage.info('已取消专家模式')
  } else {
    currentExpertMode.value = mode
    ElMessage.success(`已切换到${modeInfo.label}专家模式`)
  }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim()) {
    ElMessage.warning('请输入消息')
    return
  }

  const userMessage = {
    role: 'user',
    content: inputMessage.value.trim(),
    time: new Date()
  }

  messages.value.push(userMessage)
  const currentInput = inputMessage.value
  inputMessage.value = ''
  
  await scrollToBottom()

  loading.value = true
  try {
    const res = await chatAI({
      content: currentInput,
      sessionId: currentSessionId.value,
      expertMode: currentExpertMode.value
    })

    if (res.code === 200) {
      const aiMessage = {
        role: 'assistant',
        content: res.data.response || res.data.content || res.data || '抱歉，我无法回答这个问题。',
        time: new Date()
      }
      messages.value.push(aiMessage)
      
      if (res.data.sessionId) {
        if (!currentSessionId.value) {
          currentSessionId.value = res.data.sessionId
        }
        fetchSessions()
      }
    } else {
      ElMessage.error(res.msg || '发送失败')
    }
  } catch (error) {
    console.error('AI请求失败:', error)
    ElMessage.error('发送失败，请重试')
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}

const copyMessage = async (content) => {
  try {
    await navigator.clipboard.writeText(content)
    ElMessage.success('已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败')
  }
}

const regenerate = async (index) => {
  if (index === 0 || messages.value[index - 1].role !== 'user') {
    ElMessage.warning('无法重新生成')
    return
  }

  const userMessage = messages.value[index - 1]
  messages.value = messages.value.slice(0, index)
  inputMessage.value = userMessage.content
  await sendMessage()
}

const clearMessages = async () => {
  try {
    await ElMessageBox.confirm('确定要清空当前对话吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    messages.value = []
    currentSessionId.value = null
    ElMessage.success('对话已清空')
  } catch {
    return
  }
}

const createNewSession = () => {
  messages.value = []
  currentSessionId.value = null
  ElMessage.success('已创建新对话')
}

const switchSession = async (sessionId) => {
  if (currentSessionId.value === sessionId) return
  
  currentSessionId.value = sessionId
  messages.value = []
  
  try {
    const res = await getSessionMessages(sessionId)
    if (res.code === 200 && res.data) {
      messages.value = (res.data || []).map(m => ({
        ...m,
        time: m.time || m.createTime
      }))
      await scrollToBottom()
    }
  } catch (error) {
    console.error('加载对话失败:', error)
    ElMessage.error('加载对话失败')
  }
}

const deleteSession = async (session) => {
  try {
    await ElMessageBox.confirm('确定要删除这个对话吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteAIHistory(session.id)
    sessions.value = sessions.value.filter(s => s.id !== session.id)
    
    if (currentSessionId.value === session.sessionId) {
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
      sessions.value = res.data.list || res.data || []
    }
  } catch (error) {
    console.error('获取对话列表失败:', error)
  } finally {
    sessionsLoading.value = false
  }
}

onMounted(() => {
  fetchSessions()
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
  max-width: 1400px;
  margin: 0 auto;
  background: var(--leetcode-bg, #FFFFFF);
  border-radius: 12px;
  border: 1px solid var(--leetcode-border, #E5E7EB);
  overflow: hidden;
  display: flex;
  height: calc(100vh - 120px);
}

.sidebar {
  width: 280px;
  border-right: 1px solid var(--leetcode-border, #E5E7EB);
  display: flex;
  flex-direction: column;
  background: var(--leetcode-bg-secondary, #F7F8FA);
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid var(--leetcode-border, #E5E7EB);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sidebar-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--leetcode-text, #24292F);
  margin: 0;
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
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-bottom: 8px;
}

.session-item:hover {
  background: var(--leetcode-bg, #FFFFFF);
}

.session-item.active {
  background: var(--leetcode-primary, #0066FF);
  color: white;
}

.session-info {
  flex: 1;
  min-width: 0;
}

.session-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--leetcode-text, #24292F);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-item.active .session-title {
  color: white;
}

.session-time {
  font-size: 12px;
  color: var(--leetcode-text-secondary, #6B7280);
  margin-top: 4px;
}

.session-item.active .session-time {
  color: rgba(255, 255, 255, 0.8);
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
  gap: 24px;
}

.empty-chat {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
  padding: 40px;
}

.empty-icon-wrapper {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0066FF 0%, #66B3FF 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  box-shadow: 0 8px 24px rgba(0, 102, 255, 0.3);
}

.empty-icon {
  font-size: 40px;
  color: white;
}

.empty-chat h3 {
  font-size: 24px;
  font-weight: 700;
  color: var(--leetcode-text, #24292F);
  margin: 0 0 12px 0;
}

.empty-chat p {
  font-size: 14px;
  color: var(--leetcode-text-secondary, #6B7280);
  margin: 0 0 32px 0;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  max-width: 400px;
}

.quick-action {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border: 1px solid var(--leetcode-border, #E5E7EB);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
  color: var(--leetcode-text, #24292F);
  position: relative;
  overflow: hidden;
}

.quick-action:hover {
  background: var(--leetcode-primary, #0066FF);
  border-color: var(--leetcode-primary, #0066FF);
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 102, 255, 0.3);
}

.quick-action.active {
  background: var(--leetcode-primary, #0066FF);
  border-color: var(--leetcode-primary, #0066FF);
  color: white;
  box-shadow: 0 6px 16px rgba(0, 102, 255, 0.3);
  transform: translateY(-2px);
}

.quick-action.active::after {
  content: '';
  position: absolute;
  top: 8px;
  right: 8px;
  width: 8px;
  height: 8px;
  background: white;
  border-radius: 50%;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.7;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.message-wrapper {
  display: flex;
  width: 100%;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-wrapper.user {
  justify-content: flex-end;
}

.message-wrapper.assistant {
  justify-content: flex-start;
}

.message-content {
  max-width: 75%;
  padding: 16px 20px;
  border-radius: 12px;
  position: relative;
}

.message-wrapper.user .message-content {
  background: var(--leetcode-primary, #0066FF);
  color: white;
  border-bottom-right-radius: 4px;
}

.message-wrapper.assistant .message-content {
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border: 1px solid var(--leetcode-border, #E5E7EB);
  color: var(--leetcode-text, #24292F);
  border-bottom-left-radius: 4px;
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

.ai-avatar, .user-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-avatar {
  background: linear-gradient(135deg, #0066FF 0%, #66B3FF 100%);
  color: white;
}

.user-avatar {
  background: var(--leetcode-success, #00C853);
  color: white;
}

.ai-name {
  font-weight: 600;
  color: var(--leetcode-text, #24292F);
}

.message-time {
  font-size: 12px;
  color: var(--leetcode-text-secondary, #6B7280);
}

.message-wrapper.user .message-time {
  color: rgba(255, 255, 255, 0.8);
}

.message-text {
  line-height: 1.7;
  font-size: 14px;
  word-wrap: break-word;
}

.message-text :deep(.code-block) {
  background: var(--leetcode-bg, #FFFFFF);
  border-radius: 8px;
  border: 1px solid var(--leetcode-border, #E5E7EB);
  margin: 12px 0;
  overflow: hidden;
}

.message-text :deep(.code-header) {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border-bottom: 1px solid var(--leetcode-border, #E5E7EB);
}

.message-text :deep(.code-lang) {
  font-size: 12px;
  color: var(--leetcode-text-secondary, #6B7280);
  font-weight: 500;
}

.message-text :deep(.copy-btn) {
  background: none;
  border: none;
  color: var(--leetcode-primary, #0066FF);
  font-size: 12px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.message-text :deep(.copy-btn:hover) {
  background: var(--leetcode-primary, #0066FF);
  color: white;
}

.message-text :deep(.code-block code) {
  display: block;
  padding: 12px;
  font-family: 'Fira Code', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.5;
  color: var(--leetcode-text, #24292F);
  overflow-x: auto;
}

.message-text :deep(.inline-code) {
  background: rgba(0, 0, 0, 0.05);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Fira Code', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
}

.message-wrapper.user .message-text :deep(.inline-code) {
  background: rgba(255, 255, 255, 0.2);
}

.message-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--leetcode-border, #E5E7EB);
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
  background: var(--leetcode-primary, #0066FF);
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
    transform: translateY(-8px);
    opacity: 1;
  }
}

.chat-input {
  padding: 20px 24px;
  border-top: 1px solid var(--leetcode-border, #E5E7EB);
  background: var(--leetcode-bg, #FFFFFF);
}

.input-container {
  max-width: 800px;
  margin: 0 auto;
}

.message-input {
  margin-bottom: 12px;
}

:deep(.message-input .el-textarea__inner) {
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border: 1px solid var(--leetcode-border, #E5E7EB);
  border-radius: 8px;
  padding: 12px 16px;
  font-size: 14px;
  line-height: 1.6;
  resize: none;
}

:deep(.message-input .el-textarea__inner:focus) {
  border-color: var(--leetcode-primary, #0066FF);
  box-shadow: 0 0 0 3px rgba(0, 102, 255, 0.1);
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.action-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.hint {
  font-size: 12px;
  color: var(--leetcode-text-secondary, #6B7280);
}

@media (max-width: 1024px) {
  .sidebar {
    width: 240px;
  }
}

@media (max-width: 768px) {
  .leetcode-ai {
    padding: 16px;
  }

  .ai-container {
    height: calc(100vh - 96px);
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    height: auto;
    max-height: 200px;
    border-right: none;
    border-bottom: 1px solid var(--leetcode-border, #E5E7EB);
  }

  .session-list {
    display: flex;
    overflow-x: auto;
    padding: 12px;
    gap: 8px;
  }

  .session-item {
    flex-shrink: 0;
    min-width: 150px;
  }

  .message-content {
    max-width: 85%;
  }

  .quick-actions {
    grid-template-columns: 1fr;
  }

  .chat-input {
    padding: 16px;
  }
}
</style>
