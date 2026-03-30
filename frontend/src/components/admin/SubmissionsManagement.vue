<template>
  <el-card class="management-card" shadow="never">
    <template #header>
      <div class="card-header">
        <span class="card-title">提交记录</span>
        <el-button type="primary" link @click="$emit('refresh')">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </template>

    <el-table
      :data="submissions"
      stripe
      row-key="id"
      v-loading="loading"
      max-height="500"
      class="admin-table"
      table-layout="fixed"
      empty-text="暂无提交记录"
      :default-sort="{ prop: 'id', order: 'descending' }"
    >
      <el-table-column prop="id" label="ID" width="80" sortable />
      <el-table-column prop="userId" label="用户ID" width="100" sortable />
      <el-table-column prop="problemId" label="题目ID" width="110" sortable>
        <template #default="{ row }">
          <span
            class="problem-id-link"
            tabindex="0"
            role="button"
            @click="$emit('open-problem', row.problemId)"
            @keyup.enter="$emit('open-problem', row.problemId)"
          >
            #{{ row.problemId }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="language" label="语言" width="110" sortable>
        <template #default="{ row }">
          <span class="language-badge">{{ formatLanguage(row.language) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="result" label="结果" width="120" sortable>
        <template #default="{ row }">
          <span :class="['result-tag', getResultClass(row.result)]">
            {{ getResultText(row.result) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="timeCost" label="耗时" width="110" sortable>
        <template #default="{ row }">
          <span class="time-cost">{{ formatMetric(row.timeCost, 'ms') }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="提交时间" min-width="180" sortable show-overflow-tooltip />
    </el-table>
  </el-card>
</template>

<script setup>
import { Refresh } from '@element-plus/icons-vue'

defineProps({
  submissions: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

defineEmits(['refresh', 'open-problem'])

const getResultClass = (result) => {
  const classes = { 0: 'result-ac', 1: 'result-wa', 2: 'result-re', 3: 'result-tle', 4: 'result-mle' }
  return classes[result ?? 0] || 'result-unknown'
}

const getResultText = (result) => {
  const texts = { 0: '通过', 1: '答案错误', 2: '运行错误', 3: '超时', 4: '内存超限' }
  return texts[result ?? 0] || '未知'
}

const formatLanguage = (language) => {
  if (!language) return '--'
  return String(language).toUpperCase()
}

const formatMetric = (value, unit) => {
  if (value === null || value === undefined || value === '') {
    return '--'
  }
  return `${value}${unit}`
}
</script>

<style scoped>
.result-tag {
  display: inline-block;
  min-width: 72px;
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  text-align: center;
}

.result-ac {
  background: var(--difficulty-easy-bg);
  color: var(--difficulty-easy-color);
}

.result-wa,
.result-re {
  background: var(--difficulty-hard-bg);
  color: var(--difficulty-hard-color);
}

.result-tle,
.result-mle {
  background: var(--difficulty-medium-bg);
  color: var(--difficulty-medium-color);
}

.result-unknown {
  background: var(--info-light);
  color: var(--info-color);
}

.time-cost {
  font-family: 'Fira Code', 'Monaco', 'Consolas', monospace;
  font-size: 13px;
  color: var(--text-secondary);
}

.problem-id-link {
  color: var(--brand-primary);
  font-weight: 500;
  cursor: pointer;
  transition: color 0.2s ease;
  outline: none;
}

.problem-id-link:hover,
.problem-id-link:focus-visible {
  color: var(--brand-primary-hover);
  text-decoration: underline;
}
</style>
