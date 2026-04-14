<template>
  <div
    class="submissions-content"
    :class="{ 'has-submissions': submissions.length > 0 }"
    v-loading="loading"
  >
    <div class="submissions-toolbar">
      <span class="submissions-title">当前题目最近提交</span>
      <el-button text size="small" @click="$emit('refresh')">刷新</el-button>
    </div>

    <div v-if="submissions.length > 0" class="submission-list">
      <div
        v-for="item in submissions"
        :key="item.id"
        class="submission-row"
        @click="$emit('select', item)"
      >
        <div class="submission-row-main">
          <span :class="['submission-status', item.result === 0 ? 'success' : 'error']">
            {{ getSubmissionResultText(item.result) }}
          </span>
          <span class="submission-language">{{ formatLanguage(item.language) }}</span>
        </div>
        <div class="submission-row-meta">
          <span>{{ item.timeCost || 0 }} ms</span>
          <span>{{ item.memoryCost || 0 }} KB</span>
          <span>{{ formatSubmissionDateTime(item.submitTime || item.createTime) }}</span>
        </div>
      </div>
    </div>

    <el-empty description="暂无提交记录" />
  </div>
</template>

<script setup>
import { formatSubmissionDateTime, getSubmissionResultText } from '@/utils/submission'
import { getRuntimeLanguageLabel } from '@/utils/runtimeLanguage'

const formatLanguage = (language) => getRuntimeLanguageLabel(language)

defineProps({
  loading: {
    type: Boolean,
    default: false
  },
  submissions: {
    type: Array,
    default: () => []
  }
})

defineEmits(['refresh', 'select'])
</script>

<style scoped>
.submissions-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.submissions-content.has-submissions :deep(.el-empty) {
  display: none;
}

.submissions-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.submissions-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.submission-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.submission-row {
  padding: 12px 14px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  background: #fff;
  cursor: pointer;
  transition: all 0.2s ease;
}

.submission-row:hover {
  border-color: #cbd5e1;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.06);
}

.submission-row-main {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.submission-status {
  font-size: 13px;
  font-weight: 600;
}

.submission-status.success {
  color: #16a34a;
}

.submission-status.error {
  color: #dc2626;
}

.submission-language {
  font-size: 12px;
  color: #64748b;
  background: #f8fafc;
  border-radius: 999px;
  padding: 2px 8px;
}

.submission-row-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 12px;
  color: #64748b;
}
</style>
