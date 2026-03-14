<template>
  <div class="leetcode-code-run">
    <div class="code-run-container">
      <div class="editor-section">
        <div class="editor-header">
          <div class="header-left">
            <el-select v-model="language" size="small" @change="handleLanguageChange" class="language-select">
              <el-option label="Java" value="java" />
              <el-option label="Python" value="python" />
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
            :language="language" 
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
              <div v-if="!output" class="no-output">
                <el-empty description="暂无输出结果" />
              </div>
              <div v-else class="output-display">
                <div class="output-status" :class="outputStatusClass">
                  <el-icon v-if="outputStatusClass === 'success'"><CircleCheck /></el-icon>
                  <el-icon v-else><CircleClose /></el-icon>
                  <span>{{ outputStatusText }}</span>
                </div>
                <pre class="output-content">{{ output }}</pre>
                <div v-if="executionTime" class="execution-info">
                  <span>执行时间: {{ executionTime }}ms</span>
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
import { ref, onMounted } from 'vue'
import { runCode as runCodeApi } from '@/api/code'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { VideoPlay, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import MonacoEditor from '@/components/MonacoEditor.vue'
import AIDialog from '@/components/AIDialog.vue'

const userStore = useUserStore()

const language = ref(userStore.userInfo?.language || 'java')
const code = ref('')
const inputData = ref('')
const output = ref('')
const running = ref(false)
const activeTab = ref('input')
const executionTime = ref(null)
const outputStatusClass = ref('')
const outputStatusText = ref('')
const aiDialogVisible = ref(false)
const aiInitialPrompt = ref('')
const aiInitialCode = ref('')

const codeTemplates = {
  java: `public class Main {
    public static void main(String[] args) {
        System.out.println("Hello World");
    }
}`,
  python: `print("Hello World")`
}

const initializeCode = () => {
  code.value = codeTemplates[language.value] || ''
}

const handleLanguageChange = () => {
  if (!code.value.trim() || code.value === codeTemplates[Object.keys(codeTemplates).find(k => k !== language.value)]) {
    initializeCode()
  }
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
  output.value = ''
  executionTime.value = null
  outputStatusClass.value = ''

  try {
    const res = await runCodeApi({
      code: code.value,
      language: language.value,
      input: inputData.value
    })

    if (res.code === 200) {
      output.value = res.data || '无输出'
      outputStatusClass.value = 'success'
      outputStatusText.value = '运行成功'
      ElMessage.success('运行成功')
    } else {
      output.value = res.msg || '运行失败'
      outputStatusClass.value = 'error'
      outputStatusText.value = '运行失败'
      ElMessage.error(res.msg || '运行失败')
    }
  } catch (error) {
    output.value = error.message || '运行失败,请重试'
    outputStatusClass.value = 'error'
    outputStatusText.value = '运行失败'
    ElMessage.error('运行失败,请重试')
  } finally {
    running.value = false
  }
}

onMounted(() => {
  initializeCode()
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

.header-left {
  display: flex;
  gap: 12px;
  align-items: center;
}

.header-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.language-select {
  width: 140px;
}

:deep(.language-select .el-input__wrapper) {
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border-radius: 4px;
  border: 1px solid var(--leetcode-border, #E5E7EB);
  padding: 4px 8px;
}

:deep(.language-select .el-input__wrapper:hover) {
  border-color: var(--leetcode-primary, #0066FF);
}

:deep(.language-select .el-input__wrapper.is-focus) {
  border-color: var(--leetcode-primary, #0066FF);
  box-shadow: 0 0 0 2px rgba(0, 102, 255, 0.1);
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

:deep(.output-tabs .el-tabs__nav-wrap::after) {
  display: none;
}

:deep(.output-tabs .el-tabs__item) {
  height: 44px;
  line-height: 44px;
  font-size: 14px;
  font-weight: 500;
  color: var(--leetcode-text-secondary, #6B7280);
  padding: 0 16px;
  transition: all 0.2s ease;
}

:deep(.output-tabs .el-tabs__item:hover) {
  color: var(--leetcode-primary, #0066FF);
}

:deep(.output-tabs .el-tabs__item.is-active) {
  color: var(--leetcode-primary, #0066FF);
  font-weight: 600;
}

:deep(.output-tabs .el-tabs__active-bar) {
  background: var(--leetcode-primary, #0066FF);
  height: 2px;
}

:deep(.output-tabs .el-tabs__content) {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.tab-content {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.code-input {
  flex: 1;
  margin-bottom: 16px;
}

:deep(.code-input .el-textarea__inner) {
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border: 1px solid var(--leetcode-border, #E5E7EB);
  border-radius: 4px;
  font-family: 'Fira Code', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.5;
  padding: 12px;
}

:deep(.code-input .el-textarea__inner:focus) {
  border-color: var(--leetcode-primary, #0066FF);
  box-shadow: 0 0 0 2px rgba(0, 102, 255, 0.1);
}

.input-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.no-output {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 200px;
}

.output-display {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.output-status {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 600;
}

.output-status.success {
  background: rgba(0, 200, 83, 0.1);
  color: var(--leetcode-success, #00C853);
}

.output-status.error {
  background: rgba(238, 77, 56, 0.1);
  color: var(--leetcode-danger, #EE4D2E);
}

.output-content {
  background: var(--leetcode-bg-secondary, #F7F8FA);
  padding: 12px;
  border-radius: 4px;
  border: 1px solid var(--leetcode-border, #E5E7EB);
  font-family: 'Fira Code', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.5;
  color: var(--leetcode-text, #24292F);
  white-space: pre-wrap;
  word-wrap: break-word;
  margin: 0;
  flex: 1;
  overflow-y: auto;
}

.execution-info {
  font-size: 13px;
  color: var(--leetcode-text-secondary, #6B7280);
  padding: 8px 12px;
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border-radius: 4px;
}

@media (max-width: 1200px) {
  .code-run-container {
    grid-template-columns: 1fr;
    height: auto;
  }

  .editor-section {
    height: 500px;
  }

  .output-section {
    height: 400px;
  }
}

@media (max-width: 768px) {
  .leetcode-code-run {
    padding: 16px;
  }

  .editor-section {
    height: 400px;
  }

  .output-section {
    height: 350px;
  }

  .editor-header {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }

  .header-left,
  .header-right {
    justify-content: space-between;
  }

  .language-select {
    width: 100%;
  }

  .run-button {
    width: 100%;
  }

  .input-actions {
    flex-direction: column;
  }

  .input-actions .el-button {
    width: 100%;
  }
}
</style>
