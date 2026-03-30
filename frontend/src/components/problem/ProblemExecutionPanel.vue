<template>
  <div class="testcase-section">
    <div class="testcase-header">
      <div class="testcase-tabs">
        <div
          v-for="tab in tabs"
          :key="tab.key"
          class="testcase-tab"
          :class="{ active: activeTab === tab.key }"
          @click="emit('update:activeTab', tab.key)"
        >
          {{ tab.label }}
        </div>
      </div>
      <div class="testcase-actions">
        <el-button type="success" size="small" :loading="running" @click="emit('run')">
          <el-icon><VideoPlay /></el-icon>
          运行
        </el-button>
      </div>
    </div>

    <div class="testcase-content">
      <div v-if="activeTab === 'case'" class="testcase-panel">
        <div class="testcase-list">
          <div
            v-for="(tc, index) in testCases"
            :key="index"
            class="testcase-item"
            :class="{ active: selectedTestCase === index }"
            @click="emit('update:selectedTestCase', index)"
          >
            <span class="testcase-name">Case {{ index + 1 }}</span>
            <el-icon v-if="tc.result === 'passed'" class="testcase-status passed"><CircleCheck /></el-icon>
            <el-icon v-else-if="tc.result === 'failed'" class="testcase-status failed"><CircleClose /></el-icon>
          </div>
          <div class="testcase-item add-btn" @click="handleAddTestCase">
            <el-icon><Plus /></el-icon>
          </div>
        </div>

        <div v-if="currentTestCase" class="testcase-detail">
          <div class="input-section">
            <div class="section-label">
              <span>输入：</span>
            </div>
            <el-input
              :model-value="currentTestCase.input"
              type="textarea"
              :rows="3"
              class="testcase-input"
              @update:model-value="handleInputChange"
            />
          </div>
          <div v-if="currentTestCase.output" class="output-section">
            <div class="section-label">
              <span>预期输出：</span>
            </div>
            <pre class="expected-output">{{ currentTestCase.output }}</pre>
          </div>
        </div>
      </div>

      <div v-if="activeTab === 'result'" class="result-panel">
        <div v-if="!result" class="no-result">
          <div class="empty-state">
            <el-icon class="empty-icon"><VideoPlay /></el-icon>
            <p>点击“运行”查看结果</p>
          </div>
        </div>

        <div v-else class="result-display">
          <div class="result-summary" :class="result.result === 0 ? 'success' : 'error'">
            <el-icon v-if="result.result === 0" class="result-icon"><CircleCheck /></el-icon>
            <el-icon v-else class="result-icon"><CircleClose /></el-icon>
            <span class="result-text">{{ getSubmissionResultText(result.result) }}</span>
            <span v-if="result.timeCost" class="result-time">{{ result.timeCost }} ms</span>
            <span v-if="result.memoryCost" class="result-memory">{{ result.memoryCost }} KB</span>
          </div>

          <div v-if="result.testCaseResults?.length > 0" class="result-details">
            <div
              v-for="(tc, index) in result.testCaseResults"
              :key="index"
              class="result-item"
              :class="tc.result === 0 ? 'passed' : 'failed'"
            >
              <div class="result-item-header">
                <span class="result-item-name">测试用例 {{ index + 1 }}</span>
                <el-tag :type="tc.result === 0 ? 'success' : 'danger'" size="small">
                  {{ tc.result === 0 ? '通过' : '失败' }}
                </el-tag>
              </div>
              <div v-if="tc.result !== 0" class="result-item-body">
                <div class="result-row">
                  <span class="result-label">输入：</span>
                  <pre>{{ tc.input }}</pre>
                </div>
                <div class="result-row">
                  <span class="result-label">预期输出：</span>
                  <pre>{{ tc.expectedOutput }}</pre>
                </div>
                <div class="result-row">
                  <span class="result-label">实际输出：</span>
                  <pre>{{ tc.actualOutput || '无输出' }}</pre>
                </div>
                <div v-if="tc.errorMessage" class="result-row">
                  <span class="result-label">错误：</span>
                  <pre class="error-message">{{ tc.errorMessage }}</pre>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { CircleCheck, CircleClose, Plus, VideoPlay } from '@element-plus/icons-vue'
import { getSubmissionResultText } from '@/utils/submission'

const props = defineProps({
  tabs: {
    type: Array,
    default: () => []
  },
  activeTab: {
    type: String,
    default: 'case'
  },
  running: {
    type: Boolean,
    default: false
  },
  testCases: {
    type: Array,
    default: () => []
  },
  selectedTestCase: {
    type: Number,
    default: 0
  },
  result: {
    type: Object,
    default: null
  }
})

const emit = defineEmits([
  'run',
  'update:activeTab',
  'update:selectedTestCase',
  'update:testCases'
])

const currentTestCase = computed(() => {
  if (props.selectedTestCase === null || props.selectedTestCase === undefined) {
    return null
  }
  return props.testCases[props.selectedTestCase] || null
})

const handleAddTestCase = () => {
  const nextTestCases = [
    ...props.testCases,
    { input: '', output: '', result: null }
  ]
  emit('update:testCases', nextTestCases)
  emit('update:selectedTestCase', nextTestCases.length - 1)
}

const handleInputChange = (value) => {
  if (props.selectedTestCase === null || props.selectedTestCase === undefined) {
    return
  }

  const nextTestCases = props.testCases.map((testCase, index) => {
    if (index !== props.selectedTestCase) {
      return testCase
    }

    return {
      ...testCase,
      input: value
    }
  })

  emit('update:testCases', nextTestCases)
}
</script>

<style scoped>
.testcase-section {
  height: 200px;
  border-top: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.testcase-header {
  height: 40px;
  background: #fafafa;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
}

.testcase-tabs {
  display: flex;
  gap: 4px;
}

.testcase-tab {
  padding: 8px 16px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}

.testcase-tab:hover {
  color: #333;
}

.testcase-tab.active {
  color: #1890ff;
  border-bottom-color: #1890ff;
}

.testcase-content {
  flex: 1;
  overflow: hidden;
}

.testcase-panel {
  height: 100%;
  display: flex;
}

.testcase-list {
  width: 100px;
  border-right: 1px solid #e8e8e8;
  padding: 8px;
  overflow-y: auto;
}

.testcase-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  margin-bottom: 4px;
  transition: all 0.2s;
}

.testcase-item:hover {
  background: #f0f0f0;
}

.testcase-item.active {
  background: #e6f7ff;
  color: #1890ff;
}

.testcase-item.add-btn {
  justify-content: center;
  color: #999;
  border: 1px dashed #d9d9d9;
}

.testcase-item.add-btn:hover {
  color: #1890ff;
  border-color: #1890ff;
}

.testcase-name {
  font-size: 13px;
}

.testcase-status {
  font-size: 14px;
}

.testcase-status.passed {
  color: #52c41a;
}

.testcase-status.failed {
  color: #ff4d4f;
}

.testcase-detail {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

.input-section,
.output-section {
  margin-bottom: 16px;
}

.section-label {
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
}

.testcase-input :deep(.el-textarea__inner) {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 13px;
}

.expected-output {
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  border-radius: 4px;
  padding: 12px;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 13px;
  color: #333;
  margin: 0;
}

.result-panel {
  height: 100%;
  overflow-y: auto;
  padding: 16px;
}

.no-result {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-state {
  text-align: center;
  color: #999;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 14px;
}

.result-display {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.result-summary {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 8px;
}

.result-summary.success {
  background: #f6ffed;
  border: 1px solid #b7eb8f;
}

.result-summary.error {
  background: #fff2f0;
  border: 1px solid #ffccc7;
}

.result-icon {
  font-size: 24px;
}

.result-summary.success .result-icon {
  color: #52c41a;
}

.result-summary.error .result-icon {
  color: #ff4d4f;
}

.result-text {
  font-size: 16px;
  font-weight: 600;
}

.result-summary.success .result-text {
  color: #52c41a;
}

.result-summary.error .result-text {
  color: #ff4d4f;
}

.result-time,
.result-memory {
  font-size: 13px;
  color: #666;
  margin-left: auto;
}

.result-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.result-item {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
}

.result-item.passed {
  border-color: #b7eb8f;
}

.result-item.failed {
  border-color: #ffccc7;
}

.result-item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #fafafa;
  border-bottom: 1px solid #e8e8e8;
}

.result-item-name {
  font-size: 14px;
  font-weight: 500;
}

.result-item-body {
  padding: 16px;
}

.result-row {
  margin-bottom: 12px;
}

.result-row:last-child {
  margin-bottom: 0;
}

.result-label {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
  display: block;
}

.result-row pre {
  background: #f5f5f5;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  padding: 12px;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 13px;
  color: #333;
  margin: 0;
  overflow-x: auto;
}

.error-message {
  color: #ff4d4f !important;
  background: #fff2f0 !important;
  border-color: #ffccc7 !important;
}

.testcase-list::-webkit-scrollbar,
.testcase-detail::-webkit-scrollbar,
.result-panel::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.testcase-list::-webkit-scrollbar-thumb,
.testcase-detail::-webkit-scrollbar-thumb,
.result-panel::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.testcase-list::-webkit-scrollbar-thumb:hover,
.testcase-detail::-webkit-scrollbar-thumb:hover,
.result-panel::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
