<template>
  <div class="leetcode-problem-detail">
    <div class="layout-container">
      <div class="left-panel" :style="{ width: leftPanelWidth + '%' }">
        <div class="problem-panel">
          <div class="problem-header">
            <h2 class="problem-title">{{ problem?.title }}</h2>
            <el-tag :type="getDifficultyType(problem?.difficulty)" size="small" class="difficulty-tag">
              {{ getDifficultyText(problem?.difficulty) }}
            </el-tag>
          </div>
          
          <el-tabs v-model="activeTab" class="problem-tabs">
            <el-tab-pane label="题目描述" name="description">
              <div v-loading="loading" class="problem-content">
                <div class="content-section">
                  <div class="content-text">{{ problem?.content }}</div>
                </div>

                <div class="content-section" v-if="problem?.testCases !== undefined">
                  <h3 class="section-title">示例测试用例</h3>
                  
                  <div v-if="problem.testCases.length === 0" class="empty-test-cases">
                    <el-empty description="暂无示例测试用例" />
                  </div>
                  
                  <div v-else class="test-cases-container">
                    <div 
                      v-for="(testCase, index) in problem.testCases" 
                      :key="index"
                      class="test-case-item"
                    >
                      <div class="test-case-header">
                        <span class="test-case-title">示例 {{ index + 1 }}</span>
                      </div>
                      <div class="test-case-body">
                        <div class="test-case-section">
                          <div class="test-case-label">输入</div>
                          <pre class="test-case-content input-content">{{ testCase.input }}</pre>
                        </div>
                        <div class="test-case-section">
                          <div class="test-case-label">输出</div>
                          <pre class="test-case-content output-content">{{ testCase.output }}</pre>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <div class="content-section" v-if="problem?.constraints">
                  <h3 class="section-title">约束条件</h3>
                  <div class="constraints-content">
                    <div class="constraint-item">
                      <span>时间限制: {{ problem.constraints.timeLimit }}ms</span>
                    </div>
                    <div class="constraint-item">
                      <span>内存限制: {{ problem.constraints.memoryLimit }}KB</span>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>
            
            <el-tab-pane label="提示" name="hints">
              <div class="hints-content">
                <el-empty description="暂无提示信息" />
              </div>
            </el-tab-pane>
            
            <el-tab-pane label="讨论区" name="discussion">
              <div class="discussion-content">
                <el-empty description="讨论区功能开发中" />
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>

      <div 
        class="resizer-left"
        @mousedown="startResizeLeft"
        @dblclick="resetLeftPanel"
      ></div>

      <div class="center-panel" :style="{ width: centerPanelWidth + '%' }">
        <div class="editor-panel">
          <div class="editor-header">
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
          
          <div class="editor-container">
            <MonacoEditor 
              v-model="code" 
              :language="language" 
              height="calc(100vh - 200px)"
              @change="handleCodeChange"
              @ai-action="handleAIAction"
            />
          </div>
          
          <div class="editor-footer">
            <el-button 
              type="success" 
              :loading="runningExampleTest" 
              @click="runExampleTest" 
              class="run-button"
              size="default"
            >
              运行
            </el-button>
            <el-button 
              type="primary" 
              :loading="submitting" 
              @click="submitCode" 
              class="submit-button"
              size="default"
            >
              提交
            </el-button>
          </div>
        </div>
      </div>

      <div 
        class="resizer-right"
        @mousedown="startResizeRight"
        @dblclick="resetRightPanel"
      ></div>

      <div class="right-panel" :style="{ width: rightPanelWidth + '%' }">
        <div class="result-panel">
          <div class="result-header">
            <span>运行结果</span>
            <el-button 
              v-if="result"
              type="text" 
              size="small"
              @click="clearResult"
            >
              清除
            </el-button>
          </div>
          
          <div class="result-content">
            <div v-if="!result" class="no-result">
              <el-empty description="暂无运行结果" />
            </div>
            
            <div v-else class="result-display">
              <div class="result-status" :class="getResultStatusClass(result.result)">
                <span class="status-text">{{ getResultText(result.result) }}</span>
              </div>
              
              <div class="result-metrics">
                <div class="metric-item">
                  <span>{{ result.timeCost }}ms</span>
                </div>
                <div class="metric-item">
                  <span>{{ result.memoryCost }}KB</span>
                </div>
              </div>

              <div class="pass-rate-section">
                <div class="pass-rate-header">
                  <span class="pass-rate-label">通过率</span>
                  <span class="pass-rate-value">{{ result.passedCount }} / {{ result.totalCount }}</span>
                </div>
                <el-progress 
                  :percentage="getPassRate(result)" 
                  :color="getProgressColor(result)"
                  :stroke-width="8"
                  :show-text="false"
                />
              </div>

              <div class="test-results-section" v-if="result.testCaseResults && result.testCaseResults.length > 0">
                <div class="section-header">
                  <h4 class="section-title">测试用例详情</h4>
                  <el-button 
                    type="text" 
                    size="small"
                    @click="toggleTestCases"
                  >
                    {{ showAllTestCases ? '收起' : '展开' }}
                  </el-button>
                </div>
                
                <div class="test-results-list" v-show="showAllTestCases">
                  <div 
                    v-for="(testCase, index) in result.testCaseResults" 
                    :key="index"
                    class="test-result-item"
                    :class="testCase.result === 0 ? 'passed' : 'failed'"
                  >
                    <div class="test-result-header">
                      <div class="test-result-info">
                        <span class="test-result-name">测试用例 {{ index + 1 }}</span>
                        <el-tag 
                          :type="testCase.result === 0 ? 'success' : 'danger'" 
                          size="small"
                        >
                          {{ testCase.result === 0 ? '通过' : '失败' }}
                        </el-tag>
                      </div>
                      <div class="test-result-metrics">
                        <span class="metric-item">{{ testCase.timeCost }}ms</span>
                        <span class="metric-item">{{ testCase.memoryCost }}KB</span>
                      </div>
                    </div>
                    
                    <div class="test-result-body" v-if="testCase.result !== 0">
                      <div class="test-result-section">
                        <div class="test-result-label">输入</div>
                        <pre class="test-result-content">{{ testCase.input }}</pre>
                      </div>
                      <div class="test-result-section">
                        <div class="test-result-label">预期输出</div>
                        <pre class="test-result-content expected">{{ testCase.expectedOutput }}</pre>
                      </div>
                      <div class="test-result-section">
                        <div class="test-result-label">实际输出</div>
                        <pre class="test-result-content actual">{{ testCase.actualOutput || '无输出' }}</pre>
                      </div>
                      <div class="test-result-section" v-if="testCase.errorMessage">
                        <div class="test-result-label">错误信息</div>
                        <pre class="test-result-content error">{{ testCase.errorMessage }}</pre>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="test-case-panel">
          <div class="test-case-header">
            <span>测试用例</span>
            <el-button 
              type="text" 
              size="small"
              @click="toggleTestCasePanel"
            >
              {{ testCasePanelCollapsed ? '展开' : '收起' }}
            </el-button>
          </div>
          
          <div class="test-case-panel-content" v-show="!testCasePanelCollapsed">
            <el-form label-position="top">
              <el-form-item label="自定义输入">
                <el-input
                  v-model="customTestCase.input"
                  type="textarea"
                  :rows="4"
                  placeholder="输入测试数据..."
                  class="test-input"
                />
              </el-form-item>
              <el-form-item label="预期输出">
                <el-input
                  v-model="customTestCase.output"
                  type="textarea"
                  :rows="3"
                  placeholder="输入预期结果..."
                  class="test-input"
                />
              </el-form-item>
              <div class="test-actions">
                <el-button 
                  type="success" 
                  size="small"
                  @click="runCustomTest"
                  :loading="runningCustomTest"
                >
                  运行自定义测试
                </el-button>
                <el-button 
                  type="primary" 
                  size="small"
                  @click="runExampleTest"
                  :loading="runningExampleTest"
                >
                  运行示例测试
                </el-button>
              </div>
            </el-form>
          </div>
        </div>
      </div>
    </div>
    <AIDialog 
      v-model="aiDialogVisible"
      :initial-prompt="aiInitialPrompt"
      :initial-code="aiInitialCode"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRoute } from 'vue-router'
import { getProblemDetail, getSampleTestCases } from '@/api/problem'
import { submitCode as submitCodeApi } from '@/api/submit'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import MonacoEditor from '@/components/MonacoEditor.vue'
import AIDialog from '@/components/AIDialog.vue'

const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const problem = ref(null)
const language = ref(userStore.userInfo?.language || 'java')
const code = ref('')
const result = ref(null)
const activeTab = ref('description')
const leftPanelWidth = ref(30)
const centerPanelWidth = ref(45)
const rightPanelWidth = ref(25)
const isResizingLeft = ref(false)
const isResizingRight = ref(false)
const testCasePanelCollapsed = ref(false)
const customTestCase = ref({ input: '', output: '' })
const runningCustomTest = ref(false)
const runningExampleTest = ref(false)
const showAllTestCases = ref(false)
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

const startResizeLeft = (e) => {
  isResizingLeft.value = true
  document.addEventListener('mousemove', resizeLeft)
  document.addEventListener('mouseup', stopResizeLeft)
}

const resizeLeft = (e) => {
  if (!isResizingLeft.value) return
  const container = document.querySelector('.layout-container')
  const containerRect = container.getBoundingClientRect()
  const newWidth = ((e.clientX - containerRect.left) / containerRect.width) * 100
  
  if (newWidth >= 20 && newWidth <= 50) {
    leftPanelWidth.value = newWidth
    centerPanelWidth.value = 100 - newWidth - rightPanelWidth.value
  }
}

const stopResizeLeft = () => {
  isResizingLeft.value = false
  document.removeEventListener('mousemove', resizeLeft)
  document.removeEventListener('mouseup', stopResizeLeft)
}

const resetLeftPanel = () => {
  leftPanelWidth.value = 30
  centerPanelWidth.value = 45
  rightPanelWidth.value = 25
}

const startResizeRight = (e) => {
  isResizingRight.value = true
  document.addEventListener('mousemove', resizeRight)
  document.addEventListener('mouseup', stopResizeRight)
}

const resizeRight = (e) => {
  if (!isResizingRight.value) return
  const container = document.querySelector('.layout-container')
  const containerRect = container.getBoundingClientRect()
  const newWidth = ((containerRect.right - e.clientX) / containerRect.width) * 100
  
  if (newWidth >= 15 && newWidth <= 40) {
    rightPanelWidth.value = newWidth
    centerPanelWidth.value = 100 - leftPanelWidth.value - newWidth
  }
}

const stopResizeRight = () => {
  isResizingRight.value = false
  document.removeEventListener('mousemove', resizeRight)
  document.removeEventListener('mouseup', stopResizeRight)
}

const resetRightPanel = () => {
  leftPanelWidth.value = 30
  centerPanelWidth.value = 45
  rightPanelWidth.value = 25
}

const toggleTestCasePanel = () => {
  testCasePanelCollapsed.value = !testCasePanelCollapsed.value
}

const toggleTestCases = () => {
  showAllTestCases.value = !showAllTestCases.value
}

const clearResult = () => {
  result.value = null
}

const handleAIAction = (data) => {
  aiInitialPrompt.value = data.prompt
  aiInitialCode.value = data.code
  aiDialogVisible.value = true
}

const runCustomTest = async () => {
  if (!customTestCase.value.input.trim()) {
    ElMessage.warning('请输入测试数据')
    return
  }
  
  runningCustomTest.value = true
  try {
    const res = await submitCodeApi({
      problemId: route.params.id,
      code: code.value,
      language: language.value
    })
    
    if (res.code === 200) {
      result.value = res.data
      ElMessage.success('测试完成')
    } else {
      ElMessage.error(res.msg || '测试失败')
    }
  } catch (error) {
    ElMessage.error('测试失败,请重试')
  } finally {
    runningCustomTest.value = false
  }
}

const runExampleTest = async () => {
  runningExampleTest.value = true
  try {
    const res = await submitCodeApi({
      problemId: route.params.id,
      code: code.value,
      language: language.value
    })
    
    if (res.code === 200) {
      result.value = res.data
      ElMessage.success('测试完成')
    } else {
      ElMessage.error(res.msg || '测试失败')
    }
  } catch (error) {
    ElMessage.error('测试失败,请重试')
  } finally {
    runningExampleTest.value = false
  }
}

const getDifficultyType = (difficulty) => {
  const types = { 0: 'success', 1: 'warning', 2: 'danger' }
  return types[difficulty] || 'info'
}

const getDifficultyText = (difficulty) => {
  const texts = { 0: '简单', 1: '中等', 2: '困难' }
  return texts[difficulty] || '未知'
}

const getResultText = (resultType) => {
  const texts = { 0: '通过', 1: '答案错误', 2: '运行错误', 3: '超时', 4: '内存超限' }
  return texts[resultType] || '未知错误'
}

const getResultStatusClass = (resultType) => {
  return resultType === 0 ? 'success' : 'failed'
}

const getPassRate = (result) => {
  if (!result.totalCount || result.totalCount === 0) return 0
  return Math.round((result.passedCount / result.totalCount) * 100)
}

const getProgressColor = (result) => {
  const rate = getPassRate(result)
  if (rate === 100) return '#00C853'
  if (rate >= 50) return '#FFB300'
  return '#EE4D2E'
}

const fetchProblemDetail = async () => {
  loading.value = true
  try {
    const res = await getProblemDetail(route.params.id)
    if (res.code === 200) {
      problem.value = res.data
      initializeCode()
      fetchTestCases()
    }
  } catch (error) {
    console.error('获取题目详情失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchTestCases = async () => {
  try {
    const res = await getSampleTestCases(route.params.id)
    if (res.code === 200 && Array.isArray(res.data)) {
      problem.value.testCases = res.data
    } else {
      ElMessage.warning('测试用例数据格式错误')
      problem.value.testCases = []
    }
  } catch (error) {
    console.error('获取测试用例失败:', error)
    ElMessage.error('获取测试用例失败')
    problem.value.testCases = []
  }
}

const submitCode = async () => {
  if (!code.value.trim()) {
    ElMessage.warning('请输入代码')
    return
  }

  submitting.value = true
  try {
    const res = await submitCodeApi({
      problemId: route.params.id,
      code: code.value,
      language: language.value
    })

    if (res.code === 200) {
      result.value = res.data
      ElMessage.success('提交成功')
    } else {
      ElMessage.error(res.msg || '提交失败')
    }
  } catch (error) {
    ElMessage.error('提交失败,请重试')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchProblemDetail()
})

onBeforeUnmount(() => {
  document.removeEventListener('mousemove', resizeLeft)
  document.removeEventListener('mouseup', stopResizeLeft)
  document.removeEventListener('mousemove', resizeRight)
  document.removeEventListener('mouseup', stopResizeRight)
})
</script>

<style scoped>
.leetcode-problem-detail {
  height: 100vh;
  overflow: hidden;
  background: var(--leetcode-bg-secondary, #F7F8FA);
}

.layout-container {
  display: flex;
  height: 100%;
  width: 100%;
  position: relative;
}

.left-panel {
  display: flex;
  flex-direction: column;
  min-width: 300px;
  max-width: 50%;
  transition: width 0.1s ease;
  background: var(--leetcode-bg, #FFFFFF);
  border-right: 1px solid var(--leetcode-border, #E5E7EB);
}

.problem-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--leetcode-bg, #FFFFFF);
}

.problem-header {
  padding: 20px;
  border-bottom: 1px solid var(--leetcode-border, #E5E7EB);
}

.problem-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--leetcode-text, #24292F);
  margin: 0 0 12px 0;
}

.difficulty-tag {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
}

.problem-tabs {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

:deep(.problem-tabs .el-tabs__header) {
  margin: 0;
  padding: 0 20px;
  background: var(--leetcode-bg, #FFFFFF);
  border-bottom: 1px solid var(--leetcode-border, #E5E7EB);
}

:deep(.problem-tabs .el-tabs__nav-wrap::after) {
  display: none;
}

:deep(.problem-tabs .el-tabs__item) {
  height: 44px;
  line-height: 44px;
  font-size: 14px;
  font-weight: 500;
  color: var(--leetcode-text-secondary, #6B7280);
  padding: 0 16px;
  transition: all 0.2s ease;
}

:deep(.problem-tabs .el-tabs__item:hover) {
  color: var(--leetcode-primary, #0066FF);
}

:deep(.problem-tabs .el-tabs__item.is-active) {
  color: var(--leetcode-primary, #0066FF);
  font-weight: 600;
}

:deep(.problem-tabs .el-tabs__active-bar) {
  background: var(--leetcode-primary, #0066FF);
  height: 2px;
}

:deep(.problem-tabs .el-tabs__content) {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.problem-content {
  min-height: 200px;
}

.content-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--leetcode-text, #24292F);
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--leetcode-border, #E5E7EB);
}

.content-text {
  line-height: 1.7;
  color: var(--leetcode-text-secondary, #6B7280);
  white-space: pre-wrap;
  font-size: 14px;
}

.test-cases-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.test-case-item {
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border: 1px solid var(--leetcode-border, #E5E7EB);
  border-radius: 6px;
  overflow: hidden;
}

.test-case-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  background: var(--leetcode-bg, #FFFFFF);
  border-bottom: 1px solid var(--leetcode-border, #E5E7EB);
}

.test-case-title {
  font-weight: 600;
  color: var(--leetcode-text, #24292F);
  font-size: 13px;
}

.test-case-body {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.test-case-section {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.test-case-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--leetcode-text-secondary, #6B7280);
}

.test-case-content {
  background: var(--leetcode-bg, #FFFFFF);
  padding: 10px;
  border-radius: 4px;
  border: 1px solid var(--leetcode-border, #E5E7EB);
  font-family: 'Fira Code', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.5;
  color: var(--leetcode-text, #24292F);
  white-space: pre-wrap;
  word-wrap: break-word;
  margin: 0;
}

.input-content {
  border-left: 3px solid var(--leetcode-primary, #0066FF);
}

.output-content {
  border-left: 3px solid var(--leetcode-success, #00C853);
}

.empty-test-cases {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border: 1px dashed var(--leetcode-border, #E5E7EB);
  border-radius: 6px;
}

.constraints-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.constraint-item {
  color: var(--leetcode-text-secondary, #6B7280);
  font-size: 13px;
}

.center-panel {
  display: flex;
  flex-direction: column;
  min-width: 300px;
  max-width: 60%;
  transition: width 0.1s ease;
  background: var(--leetcode-bg, #FFFFFF);
  border-right: 1px solid var(--leetcode-border, #E5E7EB);
}

.editor-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--leetcode-bg, #FFFFFF);
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--leetcode-border, #E5E7EB);
  background: var(--leetcode-bg, #FFFFFF);
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

.editor-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--leetcode-bg, #FFFFFF);
}

.editor-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 12px 16px;
  border-top: 1px solid var(--leetcode-border, #E5E7EB);
  background: var(--leetcode-bg, #FFFFFF);
  gap: 12px;
}

.run-button {
  background: var(--leetcode-success, #00C853);
  border: none;
  border-radius: 4px;
  font-weight: 500;
  padding: 8px 24px;
  transition: all 0.2s ease;
}

.run-button:hover {
  background: #00A845;
}

.submit-button {
  background: var(--leetcode-primary, #0066FF);
  border: none;
  border-radius: 4px;
  font-weight: 500;
  padding: 8px 24px;
  transition: all 0.2s ease;
}

.submit-button:hover {
  background: #0052CC;
}

.resizer-left,
.resizer-right {
  width: 4px;
  background: transparent;
  cursor: col-resize;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s ease;
  position: relative;
  z-index: 10;
}

.resizer-left:hover,
.resizer-right:hover {
  background: var(--leetcode-primary, #0066FF);
}

.right-panel {
  display: flex;
  flex-direction: column;
  min-width: 250px;
  max-width: 40%;
  transition: width 0.1s ease;
  background: var(--leetcode-bg, #FFFFFF);
}

.result-panel {
  flex: 0 0 auto;
  display: flex;
  flex-direction: column;
  background: var(--leetcode-bg, #FFFFFF);
  border-bottom: 1px solid var(--leetcode-border, #E5E7EB);
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--leetcode-border, #E5E7EB);
  background: var(--leetcode-bg, #FFFFFF);
  font-size: 14px;
  font-weight: 600;
  color: var(--leetcode-text, #24292F);
}

.result-content {
  padding: 16px;
  max-height: 400px;
  overflow-y: auto;
}

.no-result {
  padding: 40px 20px;
}

.result-display {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.result-status {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 12px;
}

.result-status.success {
  background: rgba(0, 200, 83, 0.1);
  color: var(--leetcode-success, #00C853);
}

.result-status.failed {
  background: rgba(238, 77, 56, 0.1);
  color: var(--leetcode-danger, #EE4D2E);
}

.result-metrics {
  display: flex;
  gap: 16px;
}

.metric-item {
  color: var(--leetcode-text-secondary, #6B7280);
  font-size: 13px;
}

.pass-rate-section {
  padding: 16px;
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border-radius: 6px;
  border: 1px solid var(--leetcode-border, #E5E7EB);
}

.pass-rate-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.pass-rate-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--leetcode-text, #24292F);
}

.pass-rate-value {
  font-size: 14px;
  font-weight: 700;
  color: var(--leetcode-primary, #0066FF);
}

.test-results-section {
  padding: 16px;
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border-radius: 6px;
  border: 1px solid var(--leetcode-border, #E5E7EB);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--leetcode-text, #24292F);
  margin: 0;
}

.test-results-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.test-result-item {
  background: var(--leetcode-bg, #FFFFFF);
  border: 1px solid var(--leetcode-border, #E5E7EB);
  border-radius: 4px;
  overflow: hidden;
}

.test-result-item.passed {
  border-color: var(--leetcode-success, #00C853);
  background: rgba(0, 200, 83, 0.05);
}

.test-result-item.failed {
  border-color: var(--leetcode-danger, #EE4D2E);
  background: rgba(238, 77, 56, 0.05);
}

.test-result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border-bottom: 1px solid var(--leetcode-border, #E5E7EB);
}

.test-result-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.test-result-name {
  font-weight: 600;
  color: var(--leetcode-text, #24292F);
  font-size: 13px;
}

.test-result-metrics {
  display: flex;
  gap: 12px;
  align-items: center;
}

.test-result-body {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.test-result-section {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.test-result-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--leetcode-text-secondary, #6B7280);
}

.test-result-content {
  background: var(--leetcode-bg-secondary, #F7F8FA);
  padding: 10px;
  border-radius: 4px;
  border: 1px solid var(--leetcode-border, #E5E7EB);
  font-family: 'Fira Code', 'Monaco', 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.5;
  color: var(--leetcode-text, #24292F);
  white-space: pre-wrap;
  word-wrap: break-word;
  margin: 0;
}

.test-result-content.expected {
  border-left: 3px solid var(--leetcode-success, #00C853);
}

.test-result-content.actual {
  border-left: 3px solid var(--leetcode-warning, #FFB300);
}

.test-result-content.error {
  border-left: 3px solid var(--leetcode-danger, #EE4D2E);
  color: var(--leetcode-danger, #EE4D2E);
}

.test-case-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--leetcode-bg, #FFFFFF);
}

.test-case-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--leetcode-border, #E5E7EB);
  background: var(--leetcode-bg, #FFFFFF);
  font-size: 14px;
  font-weight: 600;
  color: var(--leetcode-text, #24292F);
}

.test-case-panel-content {
  padding: 16px;
  overflow-y: auto;
  flex: 1;
}

.test-input {
  width: 100%;
}

:deep(.test-input .el-textarea__inner) {
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border: 1px solid var(--leetcode-border, #E5E7EB);
  border-radius: 4px;
  font-family: 'Fira Code', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.5;
  padding: 10px;
}

:deep(.test-input .el-textarea__inner:focus) {
  border-color: var(--leetcode-primary, #0066FF);
  box-shadow: 0 0 0 2px rgba(0, 102, 255, 0.1);
}

.test-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

:deep(.el-tag--success) {
  background: rgba(0, 200, 83, 0.1);
  border-color: var(--leetcode-success, #00C853);
  color: var(--leetcode-success, #00C853);
}

:deep(.el-tag--warning) {
  background: rgba(255, 179, 0, 0.1);
  border-color: var(--leetcode-warning, #FFB300);
  color: var(--leetcode-warning, #FFB300);
}

:deep(.el-tag--danger) {
  background: rgba(238, 77, 56, 0.1);
  border-color: var(--leetcode-danger, #EE4D2E);
  color: var(--leetcode-danger, #EE4D2E);
}

:deep(.el-tag--info) {
  background: rgba(134, 144, 156, 0.1);
  border-color: rgba(134, 144, 156, 0.2);
  color: var(--leetcode-text-secondary, #6B7280);
}

.hints-content,
.discussion-content {
  padding: 40px 20px;
  min-height: 200px;
}

@media (max-width: 1200px) {
  .left-panel {
    min-width: 250px;
  }
  
  .center-panel {
    min-width: 250px;
  }
  
  .right-panel {
    min-width: 200px;
  }
}

@media (max-width: 768px) {
  .layout-container {
    flex-direction: column;
  }
  
  .left-panel,
  .center-panel,
  .right-panel {
    width: 100% !important;
    min-width: 100%;
    max-width: 100%;
  }
  
  .resizer-left,
  .resizer-right {
    display: none;
  }
  
  .editor-footer {
    flex-direction: column;
    gap: 12px;
  }
  
  .run-button,
  .submit-button {
    width: 100%;
  }
}
</style>
