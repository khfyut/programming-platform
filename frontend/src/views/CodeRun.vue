<template>
  <div class="leetcode-code-run">
    <div class="code-run-container">
      <div class="editor-section">
        <div class="editor-header">
          <div class="header-left">
            <el-select v-model="language" size="small" @change="handleLanguageChange" class="language-select">
              <el-option
                v-for="item in runtimeLanguages"
                :key="item.code"
                :label="item.label"
                :value="item.code"
              />
            </el-select>
            <el-button
              type="info"
              size="small"
              @click="resetCode"
              text
            >
              重置
            </el-button>
          </div>
          <div class="header-right">
            <el-button
              type="success"
              :loading="running"
              @click="runCode"
              class="run-button"
              size="default"
            >
              <el-icon><VideoPlay /></el-icon>
              运行
            </el-button>
          </div>
        </div>

        <div class="editor-container">
          <MonacoEditor
            v-model="code"
            :language="getRuntimeMonacoLanguage(language)"
            height="calc(100vh - 280px)"
            @change="handleCodeChange"
            @ai-action="handleAIAction"
          />
        </div>
        <AIDialog
          v-model="aiDialogVisible"
          :initial-prompt="aiInitialPrompt"
          :initial-code="aiInitialCode"
        />
      </div>

      <div class="output-section">
        <el-tabs v-model="activeTab" class="output-tabs">
          <el-tab-pane label="输入" name="input">
            <div class="tab-content">
              <el-input
                v-model="inputData"
                type="textarea"
                :rows="12"
                placeholder="输入测试数据..."
                class="code-input"
              />
              <div class="input-actions">
                <el-button
                  type="primary"
                  size="small"
                  @click="runCode"
                  :loading="running"
                >
                  运行
                </el-button>
                <el-button
                  size="small"
                  @click="clearInput"
                >
                  清除
                </el-button>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="输出" name="output">
            <div class="tab-content">
              <div v-if="!hasResult" class="no-output">
                <el-empty description="暂无输出结果" />
              </div>
              <div v-else class="output-display">
                <div class="output-status" :class="outputStatusClass">
                  <el-icon v-if="outputStatusClass === 'success'"><CircleCheck /></el-icon>
                  <el-icon v-else><CircleClose /></el-icon>
                  <span>{{ outputStatusText }}</span>
                </div>
                <pre class="output-content">{{ outputText }}</pre>
                <div v-if="executionTime !== null || executionMemory !== null" class="execution-info">
                  <span v-if="executionTime !== null">执行时间: {{ executionTime }}ms</span>
                  <span v-if="executionMemory !== null">内存: {{ executionMemory }}KB</span>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, defineAsyncComponent, onMounted, ref } from 'vue'
import { runCode as runCodeApi } from '@/api/code'
import { getRuntimeLanguages } from '@/api/runtime'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { VideoPlay, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import AIDialog from '@/components/AIDialog.vue'
import {
  getRuntimeDefaultStarterCode,
  getRuntimeMonacoLanguage,
  normalizeRuntimeLanguage
} from '@/utils/runtimeLanguage'

const MonacoEditor = defineAsyncComponent(() => import('@/components/MonacoEditor.vue'))

const userStore = useUserStore()

const runtimeLanguages = ref([])
const language = ref(userStore.userInfo?.language || 'java')
const code = ref('')
const inputData = ref('')
const executionResult = ref(null)
const running = ref(false)
const activeTab = ref('input')
const aiDialogVisible = ref(false)
const aiInitialPrompt = ref('')
const aiInitialCode = ref('')

const hasResult = computed(() => executionResult.value !== null)
const executionTime = computed(() => executionResult.value?.timeCost ?? null)
const executionMemory = computed(() => executionResult.value?.memoryCost ?? null)
const outputStatusClass = computed(() => (executionResult.value?.status === 'SUCCESS' ? 'success' : 'error'))
const outputStatusText = computed(() => {
  const statusTextMap = {
    SUCCESS: '运行成功',
    COMPILE_ERROR: '编译错误',
    RUNTIME_ERROR: '运行错误',
    TIMEOUT: '执行超时',
    UNSUPPORTED_LANGUAGE: '语言不可用',
    EXECUTION_FAILED: '执行失败',
    EXECUTION_EXCEPTION: '执行异常'
  }
  return statusTextMap[executionResult.value?.status] || '执行失败'
})
const outputText = computed(() => {
  if (!executionResult.value) {
    return ''
  }
  return (
    executionResult.value.output ||
    executionResult.value.compileOutput ||
    executionResult.value.errorMessage ||
    '无输出'
  )
})

const initializeCode = () => {
  code.value = getRuntimeDefaultStarterCode(language.value)
}

const loadRuntimeCatalog = async () => {
  const res = await getRuntimeLanguages()
  if (res.code !== 200) {
    throw new Error(res.msg || '获取语言列表失败')
  }
  runtimeLanguages.value = (res.data || []).filter((item) => item.enabled !== false)
  const supportedCodes = runtimeLanguages.value.map((item) => item.code)
  if (!supportedCodes.includes(normalizeRuntimeLanguage(language.value))) {
    language.value = supportedCodes[0] || 'java'
  }
  initializeCode()
}

const handleLanguageChange = () => {
  initializeCode()
}

const handleCodeChange = () => {}

const resetCode = () => {
  initializeCode()
  ElMessage.success('代码已重置')
}

const clearInput = () => {
  inputData.value = ''
  ElMessage.success('输入已清除')
}

const handleAIAction = (data) => {
  aiInitialPrompt.value = data.prompt
  aiInitialCode.value = data.code
  aiDialogVisible.value = true
}

const runCode = async () => {
  if (!code.value.trim()) {
    ElMessage.warning('请输入代码')
    return
  }

  running.value = true
  executionResult.value = null
  activeTab.value = 'output'

  try {
    const res = await runCodeApi({
      code: code.value,
      language: language.value,
      input: inputData.value
    })

    if (res.code === 200) {
      executionResult.value = res.data || null
      ElMessage.success(res.data?.status === 'SUCCESS' ? '运行成功' : '执行完成')
    } else {
      executionResult.value = {
        status: 'EXECUTION_FAILED',
        errorMessage: res.msg || '运行失败',
        output: '',
        compileOutput: '',
        timeCost: 0,
        memoryCost: 0
      }
      ElMessage.error(res.msg || '运行失败')
    }
  } catch (error) {
    executionResult.value = {
      status: 'EXECUTION_EXCEPTION',
      errorMessage: error.message || '运行失败，请重试',
      output: '',
      compileOutput: '',
      timeCost: 0,
      memoryCost: 0
    }
    ElMessage.error('运行失败，请重试')
  } finally {
    running.value = false
  }
}

onMounted(async () => {
  try {
    await loadRuntimeCatalog()
  } catch (error) {
    ElMessage.error(error.message || '获取语言列表失败')
    initializeCode()
  }
})
</script>

<style scoped>
.leetcode-code-run {
  min-height: 100vh;
  background: var(--leetcode-bg-secondary, #F7F8FA);
  padding: 24px;
}

.code-run-container {
  max-width: 1400px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  height: calc(100vh - 120px);
}

.editor-section {
  display: flex;
  flex-direction: column;
  background: var(--leetcode-bg, #FFFFFF);
  border-radius: 8px;
  border: 1px solid var(--leetcode-border, #E5E7EB);
  overflow: hidden;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--leetcode-border, #E5E7EB);
  background: var(--leetcode-bg, #FFFFFF);
}

.header-left,
.header-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.language-select {
  width: 160px;
}

:deep(.language-select .el-input__wrapper) {
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border-radius: 4px;
  border: 1px solid var(--leetcode-border, #E5E7EB);
  padding: 4px 8px;
}

.run-button {
  background: var(--leetcode-success, #00C853);
  border: none;
  border-radius: 4px;
  font-weight: 500;
  padding: 8px 20px;
  transition: all 0.2s ease;
}

.run-button:hover {
  background: #00A845;
}

.editor-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--leetcode-bg, #FFFFFF);
}

.output-section {
  display: flex;
  flex-direction: column;
  background: var(--leetcode-bg, #FFFFFF);
  border-radius: 8px;
  border: 1px solid var(--leetcode-border, #E5E7EB);
  overflow: hidden;
}

.output-tabs {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

:deep(.output-tabs .el-tabs__header) {
  margin: 0;
  padding: 0 16px;
  background: var(--leetcode-bg, #FFFFFF);
  border-bottom: 1px solid var(--leetcode-border, #E5E7EB);
}

:deep(.output-tabs .el-tabs__content) {
  flex: 1;
  overflow: hidden;
}

:deep(.output-tabs .el-tab-pane) {
  height: 100%;
}

.tab-content {
  height: 100%;
  padding: 20px;
  box-sizing: border-box;
}

.input-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.no-output,
.output-display {
  height: 100%;
}

.output-status {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  border-radius: 999px;
  margin-bottom: 16px;
}

.output-status.success {
  background: rgba(34, 197, 94, 0.12);
  color: #15803d;
}

.output-status.error {
  background: rgba(239, 68, 68, 0.12);
  color: #b91c1c;
}

.output-content {
  margin: 0;
  min-height: 240px;
  padding: 16px;
  border-radius: 8px;
  background: #0f172a;
  color: #e2e8f0;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-word;
}

.execution-info {
  display: flex;
  gap: 16px;
  margin-top: 12px;
  color: #64748b;
  font-size: 13px;
}

@media (max-width: 1024px) {
  .code-run-container {
    grid-template-columns: 1fr;
    height: auto;
  }

  .editor-container {
    min-height: 420px;
  }

  .output-section {
    min-height: 360px;
  }
}
</style>
