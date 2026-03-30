<template>
  <el-dialog
    v-model="visible"
    title="提交详情"
    width="min(920px, 94vw)"
    top="5vh"
    destroy-on-close
    class="detail-dialog"
  >
    <div v-loading="loading" class="detail-content">
      <div v-if="submission" class="detail-shell">
        <div class="detail-section">
          <h3 class="detail-title">基本信息</h3>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="detail-label">题目</span>
              <span class="detail-value">{{ submission.problemTitle || problemTitle }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">结果</span>
              <span :class="['detail-value', 'result-' + submission.result]">
                {{ getSubmissionResultText(submission.result) }}
              </span>
            </div>
            <div class="detail-item">
              <span class="detail-label">语言</span>
              <span class="detail-value">{{ (submission.language || '-').toUpperCase() }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">提交时间</span>
              <span class="detail-value">
                {{ formatSubmissionDateTime(submission.submitTime || submission.createTime) }}
              </span>
            </div>
            <div class="detail-item">
              <span class="detail-label">执行时间</span>
              <span class="detail-value">{{ submission.timeCost || 0 }} ms</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">内存使用</span>
              <span class="detail-value">{{ submission.memoryCost || 0 }} KB</span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h3 class="detail-title">代码</h3>
          <pre class="code-display">{{ submission.code }}</pre>
        </div>

        <div
          v-if="submission.compileError || submission.runtimeError || submission.errorMessage || submission.error"
          class="detail-section"
        >
          <h3 class="detail-title">错误信息</h3>
          <pre class="error-display">{{ submission.compileError || submission.runtimeError || submission.errorMessage || submission.error }}</pre>
        </div>
      </div>
      <el-empty v-else-if="!loading" description="暂无提交详情" />
    </div>
  </el-dialog>
</template>

<script setup>
import { computed } from 'vue'
import { formatSubmissionDateTime, getSubmissionResultText } from '@/utils/submission'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  loading: {
    type: Boolean,
    default: false
  },
  submission: {
    type: Object,
    default: null
  },
  problemTitle: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})
</script>

<style scoped>
.detail-dialog :deep(.el-dialog) {
  max-width: 920px;
  border-radius: 18px;
}

.detail-content {
  min-height: 120px;
}

.detail-shell {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.detail-section {
  margin-bottom: 0;
}

.detail-title {
  margin: 0 0 12px;
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-label {
  font-size: 12px;
  color: #64748b;
}

.detail-value {
  font-size: 14px;
  color: #111827;
  word-break: break-word;
}

.detail-value.result-0 {
  color: #16a34a;
}

.detail-value.result-1,
.detail-value.result-2,
.detail-value.result-3,
.detail-value.result-4 {
  color: #dc2626;
}

.code-display,
.error-display {
  margin: 0;
  padding: 12px;
  border-radius: 10px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 13px;
  line-height: 1.6;
}

.code-display {
  background: #0f172a;
  color: #e2e8f0;
}

.error-display {
  background: #fef2f2;
  color: #b91c1c;
  border: 1px solid #fecaca;
}

@media (max-width: 768px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
