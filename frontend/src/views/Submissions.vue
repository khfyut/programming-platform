<template>
  <div class="leetcode-submissions">
    <div class="submissions-container">
      <div class="page-header">
        <h1 class="page-title">提交记录</h1>
        <p class="page-subtitle">查看您的所有提交历史</p>
      </div>

      <div class="filter-section">
        <div class="filter-left">
          <el-select
            v-model="filters.result"
            placeholder="结果"
            clearable
            class="filter-select"
            @change="fetchSubmissions"
          >
            <el-option label="通过" :value="0" />
            <el-option label="答案错误" :value="1" />
            <el-option label="运行错误" :value="2" />
            <el-option label="超时" :value="3" />
            <el-option label="内存超限" :value="4" />
          </el-select>
          <el-select
            v-model="filters.language"
            placeholder="语言"
            clearable
            class="filter-select"
            @change="fetchSubmissions"
          >
            <el-option label="Java" value="java" />
            <el-option label="Python" value="python" />
          </el-select>
        </div>
        <div class="filter-right">
          <el-button @click="resetFilters" class="reset-btn">
            <el-icon><RefreshLeft /></el-icon>
            重置
          </el-button>
        </div>
      </div>

      <div class="submissions-list" v-loading="loading">
        <div
          v-for="(submission, index) in submissionList"
          :key="submission.id"
          class="submission-card"
          :style="{ animationDelay: `${index * 50}ms` }"
          @click="viewDetail(submission)"
        >
          <div class="submission-left">
            <div class="submission-info">
              <div class="submission-header">
                <div class="submission-title">{{ submission.problemTitle }}</div>
                <span v-if="submission.difficulty" :class="['difficulty-badge', getDifficultyClass(submission.difficulty)]">
                  {{ getDifficultyText(submission.difficulty) }}
                </span>
              </div>
              <div class="submission-meta">
                <span :class="['result-badge', getResultClass(submission.result)]">
                  {{ getResultText(submission.result) }}
                </span>
                <span class="language-badge">{{ submission.language.toUpperCase() }}</span>
                <span class="time-badge">{{ submission.timeCost }}ms</span>
                <span class="memory-badge">{{ submission.memoryCost }}KB</span>
              </div>
              <div v-if="submission.tags && submission.tags.length > 0" class="submission-tags">
                <span v-for="tag in submission.tags" :key="tag" class="tag-badge">{{ tag }}</span>
              </div>
            </div>
          </div>
          <div class="submission-right">
            <div class="submission-time">{{ formatTime(submission.submitTime) }}</div>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>

        <el-empty v-if="submissionList.length === 0 && !loading" description="暂无提交记录" class="empty-state" />
      </div>

      <div class="pagination-section">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchSubmissions"
          @current-change="fetchSubmissions"
          class="pagination"
        />
      </div>
    </div>

    <el-dialog v-model="detailDialogVisible" title="提交详情" width="800px" class="detail-dialog">
      <div v-loading="detailLoading" class="detail-content">
        <div v-if="currentSubmission">
        <div class="detail-section">
          <h3 class="detail-title">基本信息</h3>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="detail-label">题目</span>
              <span class="detail-value">{{ currentSubmission.problemTitle }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">困难度</span>
              <span v-if="currentSubmission.difficulty" :class="['detail-value', 'difficulty-' + currentSubmission.difficulty.toLowerCase()]">
                {{ getDifficultyText(currentSubmission.difficulty) }}
              </span>
              <span v-else class="detail-value">未知</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">结果</span>
              <span :class="['detail-value', 'result-' + currentSubmission.result]">
                {{ getResultText(currentSubmission.result) }}
              </span>
            </div>
            <div class="detail-item">
              <span class="detail-label">语言</span>
              <span class="detail-value">{{ currentSubmission.language.toUpperCase() }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">提交时间</span>
              <span class="detail-value">{{ formatDateTime(currentSubmission.submitTime) }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">执行时间</span>
              <span class="detail-value">{{ currentSubmission.timeCost }}ms</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">内存使用</span>
              <span class="detail-value">{{ currentSubmission.memoryCost }}KB</span>
            </div>
          </div>
        </div>
        <div v-if="currentSubmission.tags && currentSubmission.tags.length > 0" class="detail-section">
          <h3 class="detail-title">标签</h3>
          <div class="detail-tags">
            <span v-for="tag in currentSubmission.tags" :key="tag" class="detail-tag-badge">{{ tag }}</span>
          </div>
        </div>

        <div class="detail-section">
          <h3 class="detail-title">代码</h3>
          <pre class="code-display">{{ currentSubmission.code }}</pre>
        </div>

        <div v-if="currentSubmission.error" class="detail-section">
          <h3 class="detail-title">错误信息</h3>
          <pre class="error-display">{{ currentSubmission.error }}</pre>
        </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getMySubmissions, getSubmitDetail } from '@/api/submit'
import { RefreshLeft, ArrowRight } from '@element-plus/icons-vue'

const loading = ref(false)
const submissionList = ref([])
const detailDialogVisible = ref(false)
const currentSubmission = ref(null)
const detailLoading = ref(false)

const filters = reactive({
  result: null,
  language: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const getResultClass = (result) => {
  const classes = { 0: 'success', 1: 'error', 2: 'error', 3: 'warning', 4: 'warning' }
  return classes[result] || 'info'
}

const getResultText = (result) => {
  const texts = { 0: '通过', 1: '答案错误', 2: '运行错误', 3: '超时', 4: '内存超限' }
  return texts[result] || '未知'
}

const getDifficultyClass = (difficulty) => {
  const classes = { 'EASY': 'easy', 'MEDIUM': 'medium', 'HARD': 'hard' }
  return classes[difficulty] || 'info'
}

const getDifficultyText = (difficulty) => {
  const texts = { 'EASY': '简单', 'MEDIUM': '中等', 'HARD': '困难' }
  return texts[difficulty] || '未知'
}

const formatTime = (time) => {
  if (!time) return '未知'
  const date = new Date(time)
  if (isNaN(date.getTime())) return '未知'
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return `${Math.floor(diff / 86400000)}天前`
}

const formatDateTime = (time) => {
  if (!time) return '未知'
  const date = new Date(time)
  if (isNaN(date.getTime())) return '未知'
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const fetchSubmissions = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size
    }

    if (filters.result !== null && filters.result !== undefined) {
      params.result = filters.result
    }

    if (filters.language !== null && filters.language !== undefined) {
      params.language = filters.language
    }

    const res = await getMySubmissions(params)
    if (res.code === 200) {
      let list = res.data.list || []
      submissionList.value = list.map(item => ({
        ...item,
        problemTitle: item.problemTitle || item.title || item.problemName,
        difficulty: item.difficulty,
        tags: item.tags || item.tagList || []
      }))
      pagination.total = res.data.total
    }
  } catch (error) {
    console.error('获取提交记录失败:', error)
  } finally {
    loading.value = false
  }
}

const viewDetail = async (submission) => {
  detailLoading.value = true
  currentSubmission.value = null
  detailDialogVisible.value = true
  try {
    const res = await getSubmitDetail(submission.id)
    if (res.code === 200) {
      let data = res.data
      if (data) {
        data.problemTitle = data.problemTitle || data.title || data.problemName || submission.problemTitle
        data.difficulty = data.difficulty || submission.difficulty
        data.submitTime = data.submitTime || data.createTime || data.createdAt || submission.submitTime
      }
      currentSubmission.value = data
    } else {
      currentSubmission.value = submission
    }
  } catch (error) {
    console.error('获取提交详情失败:', error)
    currentSubmission.value = submission
  } finally {
    detailLoading.value = false
  }
}

const resetFilters = () => {
  filters.result = null
  filters.language = null
  pagination.page = 1
  fetchSubmissions()
}

onMounted(() => {
  fetchSubmissions()
})
</script>

<style scoped>
.leetcode-submissions {
  width: 100%;
  min-height: 100vh;
  background: var(--leetcode-bg-secondary, #F7F8FA);
  padding: 24px;
  box-sizing: border-box;
}

.submissions-container {
  max-width: 1200px;
  margin: 0 auto;
  background: var(--leetcode-bg, #FFFFFF);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid var(--leetcode-border, #E5E7EB);
  overflow: hidden;
  animation: fadeInUp 0.3s ease-out;
}

.page-header {
  padding: 32px 32px 24px;
  border-bottom: 1px solid var(--leetcode-border, #E5E7EB);
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--leetcode-text, #24292F);
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 14px;
  color: var(--leetcode-text-secondary, #6B7280);
  margin: 0;
}

.filter-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 32px;
  border-bottom: 1px solid var(--leetcode-border, #E5E7EB);
  background: var(--leetcode-bg, #FFFFFF);
  flex-wrap: wrap;
  gap: 16px;
}

.filter-left {
  display: flex;
  gap: 12px;
  align-items: center;
}

.filter-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.filter-select {
  width: 140px;
}

:deep(.filter-select .el-input__wrapper) {
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border: 1px solid var(--leetcode-border, #E5E7EB);
  border-radius: 6px;
  padding: 6px 12px;
  transition: all 0.2s ease;
  box-shadow: none;
}

:deep(.filter-select .el-input__wrapper:hover) {
  border-color: var(--leetcode-primary, #0066FF);
  background: #FFFFFF;
}

:deep(.filter-select .el-input__wrapper.is-focus) {
  border-color: var(--leetcode-primary, #0066FF);
  background: #FFFFFF;
  box-shadow: 0 0 0 3px rgba(0, 102, 255, 0.1);
}

.reset-btn {
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border: 1px solid var(--leetcode-border, #E5E7EB);
  color: var(--leetcode-text-secondary, #6B7280);
  border-radius: 6px;
  padding: 8px 16px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.reset-btn:hover {
  background: var(--leetcode-primary, #0066FF);
  border-color: var(--leetcode-primary, #0066FF);
  color: white;
}

.submissions-list {
  min-height: 400px;
  position: relative;
}

.submission-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 32px;
  border-bottom: 1px solid var(--leetcode-border, #E5E7EB);
  cursor: pointer;
  transition: all 0.2s ease;
  animation: fadeInUp 0.3s ease-out backwards;
}

.submission-card:hover {
  background: var(--leetcode-bg-secondary, #F7F8FA);
  transform: translateX(4px);
}

.submission-card:last-child {
  border-bottom: none;
}

.submission-left {
  flex: 1;
}

.submission-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.submission-header {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.submission-title {
  font-size: 16px;
  font-weight: 500;
  color: var(--leetcode-text, #24292F);
}

.difficulty-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

.difficulty-badge.easy {
  background: rgba(0, 200, 83, 0.1);
  color: var(--leetcode-success, #00C853);
}

.difficulty-badge.medium {
  background: rgba(255, 179, 0, 0.1);
  color: var(--leetcode-warning, #FFB300);
}

.difficulty-badge.hard {
  background: rgba(238, 77, 56, 0.1);
  color: var(--leetcode-danger, #EE4D2E);
}

.submission-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.tag-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
  background: rgba(0, 102, 255, 0.08);
  color: var(--leetcode-primary, #0066FF);
  border: 1px solid rgba(0, 102, 255, 0.15);
}

.submission-meta {
  display: flex;
  gap: 8px;
  align-items: center;
}

.result-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

.result-badge.success {
  background: rgba(0, 200, 83, 0.1);
  color: var(--leetcode-success, #00C853);
}

.result-badge.error {
  background: rgba(238, 77, 56, 0.1);
  color: var(--leetcode-danger, #EE4D2E);
}

.result-badge.warning {
  background: rgba(255, 179, 0, 0.1);
  color: var(--leetcode-warning, #FFB300);
}

.result-badge.info {
  background: rgba(134, 144, 156, 0.1);
  color: var(--leetcode-text-secondary, #6B7280);
}

.language-badge,
.time-badge,
.memory-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  background: var(--leetcode-bg-secondary, #F7F8FA);
  color: var(--leetcode-text-secondary, #6B7280);
}

.submission-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.submission-time {
  font-size: 13px;
  color: var(--leetcode-text-secondary, #6B7280);
}

.arrow-icon {
  color: var(--leetcode-text-secondary, #6B7280);
  font-size: 16px;
  transition: transform 0.2s ease;
}

.submission-card:hover .arrow-icon {
  transform: translateX(4px);
  color: var(--leetcode-primary, #0066FF);
}

.empty-state {
  padding: 80px 20px;
}

.pagination-section {
  display: flex;
  justify-content: center;
  padding: 20px 32px;
  border-top: 1px solid var(--leetcode-border, #E5E7EB);
  background: var(--leetcode-bg, #FFFFFF);
}

:deep(.pagination) {
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.pagination button) {
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border: 1px solid var(--leetcode-border, #E5E7EB);
  color: var(--leetcode-text, #24292F);
  border-radius: 6px;
  min-width: 32px;
  height: 32px;
  transition: all 0.2s ease;
}

:deep(.pagination button:hover) {
  border-color: var(--leetcode-primary, #0066FF);
  color: var(--leetcode-primary, #0066FF);
  background: #FFFFFF;
}

:deep(.pagination button.is-active) {
  background: var(--leetcode-primary, #0066FF);
  border-color: var(--leetcode-primary, #0066FF);
  color: white;
}

:deep(.pagination .el-pager li) {
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border: 1px solid var(--leetcode-border, #E5E7EB);
  color: var(--leetcode-text, #24292F);
  border-radius: 6px;
  min-width: 32px;
  height: 32px;
  transition: all 0.2s ease;
}

:deep(.pagination .el-pager li:hover) {
  border-color: var(--leetcode-primary, #0066FF);
  color: var(--leetcode-primary, #0066FF);
  background: #FFFFFF;
}

:deep(.pagination .el-pager li.is-active) {
  background: var(--leetcode-primary, #0066FF);
  border-color: var(--leetcode-primary, #0066FF);
  color: white;
}

:deep(.pagination__total) {
  color: var(--leetcode-text-secondary, #6B7280);
  font-size: 14px;
  font-weight: 500;
}

:deep(.pagination__sizes .el-select .el-input__wrapper) {
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border: 1px solid var(--leetcode-border, #E5E7EB);
  border-radius: 6px;
}

:deep(.pagination__jump) {
  color: var(--leetcode-text-secondary, #6B7280);
  font-size: 14px;
  font-weight: 500;
}

:deep(.pagination__jump .el-input__wrapper) {
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border: 1px solid var(--leetcode-border, #E5E7EB);
  border-radius: 6px;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.detail-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--leetcode-text, #24292F);
  margin: 0;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--leetcode-border, #E5E7EB);
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-label {
  font-size: 12px;
  color: var(--leetcode-text-secondary, #6B7280);
  font-weight: 500;
}

.detail-value {
  font-size: 14px;
  color: var(--leetcode-text, #24292F);
  font-weight: 500;
}

.detail-value.result-0 {
  color: var(--leetcode-success, #00C853);
}

.detail-value.result-1,
.detail-value.result-2 {
  color: var(--leetcode-danger, #EE4D2E);
}

.detail-value.result-3,
.detail-value.result-4 {
  color: var(--leetcode-warning, #FFB300);
}

.detail-value.difficulty-easy {
  color: var(--leetcode-success, #00C853);
}

.detail-value.difficulty-medium {
  color: var(--leetcode-warning, #FFB300);
}

.detail-value.difficulty-hard {
  color: var(--leetcode-danger, #EE4D2E);
}

.detail-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.detail-tag-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 500;
  background: rgba(0, 102, 255, 0.08);
  color: var(--leetcode-primary, #0066FF);
  border: 1px solid rgba(0, 102, 255, 0.2);
}

.code-display {
  background: var(--leetcode-bg-secondary, #F7F8FA);
  padding: 16px;
  border-radius: 6px;
  border: 1px solid var(--leetcode-border, #E5E7EB);
  font-family: 'Fira Code', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.5;
  color: var(--leetcode-text, #24292F);
  white-space: pre-wrap;
  word-wrap: break-word;
  margin: 0;
  max-height: 400px;
  overflow-y: auto;
}

.error-display {
  background: rgba(238, 77, 56, 0.05);
  padding: 16px;
  border-radius: 6px;
  border: 1px solid var(--leetcode-danger, #EE4D2E);
  font-family: 'Fira Code', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.5;
  color: var(--leetcode-danger, #EE4D2E);
  white-space: pre-wrap;
  word-wrap: break-word;
  margin: 0;
}

:deep(.detail-dialog) {
  background: var(--leetcode-bg, #FFFFFF);
  border: 1px solid var(--leetcode-border, #E5E7EB);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  border-radius: 12px;
}

:deep(.detail-dialog .el-dialog__header) {
  border-bottom: 1px solid var(--leetcode-border, #E5E7EB);
  padding: 20px 24px;
}

:deep(.detail-dialog .el-dialog__title) {
  color: var(--leetcode-text, #24292F);
  font-size: 16px;
  font-weight: 600;
}

:deep(.detail-dialog .el-dialog__body) {
  color: var(--leetcode-text, #24292F);
  padding: 24px;
}

:deep(.detail-dialog .el-dialog__footer) {
  border-top: 1px solid var(--leetcode-border, #E5E7EB);
  padding: 16px 24px;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 768px) {
  .leetcode-submissions {
    padding: 16px;
  }

  .page-header {
    padding: 24px 20px 20px;
  }

  .page-title {
    font-size: 24px;
  }

  .filter-section {
    flex-direction: column;
    align-items: stretch;
    padding: 16px 20px;
  }

  .filter-left,
  .filter-right {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-select {
    width: 100%;
  }

  .submission-card {
    padding: 16px 20px;
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .submission-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .submission-meta {
    flex-wrap: wrap;
  }

  .submission-right {
    width: 100%;
    justify-content: space-between;
  }

  .pagination-section {
    padding: 16px 20px;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
