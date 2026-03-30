<template>
  <div class="description-content">
    <div v-if="summaryItems.length > 0" class="content-block">
      <div class="summary-grid">
        <div
          v-for="item in summaryItems"
          :key="item.label"
          class="summary-card"
        >
          <span class="summary-label">{{ item.label }}</span>
          <span class="summary-value">{{ item.value }}</span>
        </div>
      </div>
    </div>

    <div v-if="knowledgePoints.length > 0" class="content-block">
      <h3 class="block-title">知识点</h3>
      <div class="tag-list">
        <span
          v-for="tag in knowledgePoints"
          :key="tag"
          class="tag-chip"
        >
          {{ tag }}
        </span>
      </div>
    </div>

    <div class="content-block">
      <div class="problem-description">{{ problem?.content }}</div>
    </div>

    <div v-if="problem?.testCases?.length > 0" class="content-block">
      <h3 class="block-title">示例</h3>
      <div class="examples-list">
        <div
          v-for="(testCase, index) in problem.testCases"
          :key="index"
          class="example-item"
        >
          <div class="example-header">
            <span class="example-index">示例 {{ index + 1 }}</span>
          </div>
          <div class="example-body">
            <div class="example-row">
              <span class="example-label">输入：</span>
              <pre class="example-code">{{ testCase.input }}</pre>
            </div>
            <div class="example-row">
              <span class="example-label">输出：</span>
              <pre class="example-code">{{ testCase.output }}</pre>
            </div>
            <div v-if="testCase.explanation" class="example-row">
              <span class="example-label">解释：</span>
              <span class="example-text">{{ testCase.explanation }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="hints.length > 0" class="content-block">
      <h3 class="block-title">提示</h3>
      <div class="hints-list">
        <div
          v-for="(hint, index) in hints"
          :key="index"
          class="hint-item"
        >
          <span class="hint-num">{{ index + 1 }}.</span>
          <span class="hint-text">{{ hint }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  problem: {
    type: Object,
    default: null
  },
  hints: {
    type: Array,
    default: () => []
  }
})

const difficultyMap = {
  1: '简单',
  2: '中等',
  3: '困难',
  easy: '简单',
  medium: '中等',
  hard: '困难',
  EASY: '简单',
  MEDIUM: '中等',
  HARD: '困难'
}

const normalizeList = (value) => {
  if (!value) return []
  if (Array.isArray(value)) {
    return value
      .map((item) => {
        if (typeof item === 'string') return item
        return item?.name || item?.title || item?.label || ''
      })
      .filter(Boolean)
  }

  if (typeof value === 'string') {
    return value
      .split(/[、,，/]/)
      .map((item) => item.trim())
      .filter(Boolean)
  }

  return []
}

const difficultyText = computed(() => {
  const value =
    props.problem?.difficultyText ||
    props.problem?.difficultyLabel ||
    props.problem?.difficulty

  return difficultyMap[value] || value || ''
})

const knowledgePoints = computed(() => {
  const sources = [
    props.problem?.knowledgePoints,
    props.problem?.knowledgePointNames,
    props.problem?.tags
  ]

  const merged = sources.flatMap((source) => normalizeList(source))
  return [...new Set(merged)]
})

const summaryItems = computed(() => {
  const items = [
    {
      label: '难度',
      value: difficultyText.value
    },
    {
      label: '时间限制',
      value: props.problem?.timeLimit ? `${props.problem.timeLimit} ms` : ''
    },
    {
      label: '内存限制',
      value: props.problem?.memoryLimit ? `${props.problem.memoryLimit} MB` : ''
    },
    {
      label: '示例数',
      value: Array.isArray(props.problem?.testCases) ? String(props.problem.testCases.length) : ''
    }
  ]

  return items.filter((item) => item.value)
})
</script>

<style scoped>
.description-content {
  color: var(--problem-text, #1f2937);
}

.content-block {
  margin-bottom: 24px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 12px;
}

.summary-card {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 14px 16px;
  border: 1px solid var(--problem-border, #dbe3ef);
  border-radius: 14px;
  background:
    linear-gradient(180deg, rgba(24, 144, 255, 0.04), transparent 75%),
    var(--problem-surface, #ffffff);
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.04);
}

.summary-label {
  font-size: 12px;
  color: var(--problem-text-secondary, #64748b);
  letter-spacing: 0.04em;
}

.summary-value {
  font-size: 16px;
  font-weight: 600;
  color: var(--problem-text, #1f2937);
}

.block-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--problem-text, #1f2937);
  margin: 0 0 16px;
}

.problem-description {
  font-size: 14px;
  line-height: 1.8;
  color: var(--problem-text, #334155);
  white-space: pre-wrap;
  word-break: break-word;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-chip {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  border: 1px solid rgba(24, 144, 255, 0.16);
  border-radius: 999px;
  background: rgba(24, 144, 255, 0.08);
  color: #1668dc;
  font-size: 13px;
  font-weight: 500;
}

.examples-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.example-item {
  background: var(--problem-subtle, #f8fafc);
  border: 1px solid var(--problem-border, #dbe3ef);
  border-radius: 14px;
  overflow: hidden;
}

.example-header {
  padding: 12px 16px;
  background: rgba(148, 163, 184, 0.12);
  border-bottom: 1px solid var(--problem-border, #dbe3ef);
}

.example-index {
  font-size: 14px;
  font-weight: 500;
  color: var(--problem-text, #1f2937);
}

.example-body {
  padding: 16px;
}

.example-row {
  margin-bottom: 12px;
}

.example-row:last-child {
  margin-bottom: 0;
}

.example-label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: var(--problem-text-secondary, #64748b);
  margin-bottom: 4px;
}

.example-code {
  background: var(--problem-surface, #ffffff);
  border: 1px solid var(--problem-border, #dbe3ef);
  border-radius: 10px;
  padding: 12px;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 13px;
  color: var(--problem-text, #1f2937);
  margin: 0;
  overflow-x: auto;
  white-space: pre-wrap;
  word-break: break-word;
}

.example-text {
  font-size: 14px;
  color: var(--problem-text-secondary, #64748b);
  line-height: 1.6;
}

.hints-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hint-item {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  border-radius: 12px;
}

.hint-num {
  font-weight: 600;
  color: #52c41a;
  flex-shrink: 0;
}

.hint-text {
  font-size: 14px;
  color: var(--problem-text, #1f2937);
  line-height: 1.6;
}

@media (max-width: 768px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 520px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
