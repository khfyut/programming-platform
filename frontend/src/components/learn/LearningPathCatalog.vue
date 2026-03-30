<template>
  <section class="section-container">
    <div class="section-header-with-filter">
      <div>
        <h2 class="section-title">全部路径</h2>
        <p class="section-subtitle">按方向和语言筛选适合你的学习路线。</p>
      </div>
      <div class="filter-controls">
        <el-select
          v-model="languageFilterModel"
          size="small"
          placeholder="语言"
          @change="emit('filter-change')"
        >
          <el-option label="全部" value="all" />
          <el-option label="Java" value="java" />
          <el-option label="Python" value="python" />
          <el-option label="C++" value="cpp" />
        </el-select>
        <el-select
          v-model="directionFilterModel"
          size="small"
          placeholder="方向"
          @change="emit('filter-change')"
        >
          <el-option label="全部" value="all" />
          <el-option label="语言基础" value="language" />
          <el-option label="算法与数据结构" value="algorithm" />
          <el-option label="前端开发" value="frontend" />
          <el-option label="后端开发" value="backend" />
          <el-option label="数据库" value="database" />
          <el-option label="系统设计" value="system" />
        </el-select>
        <el-button class="clear-btn" :disabled="!hasFilters" @click="clearFilters">清空</el-button>
      </div>
    </div>

    <div v-if="learningPaths.length > 0" class="paths-grid">
      <div
        v-for="path in paginatedLearningPaths"
        :key="path.id"
        class="path-card"
        @click="emit('open-path', path.id)"
      >
        <div class="path-icon-wrapper" :style="{ background: path.gradient }">
          <el-icon :size="28"><component :is="path.icon" /></el-icon>
        </div>
        <div class="path-content">
          <div class="path-header">
            <span class="path-language-tag" :class="path.language">
              {{ getLanguageLabel(path.language) }}
            </span>
            <span class="path-direction-tag">
              {{ getDirectionLabel(path.direction) }}
            </span>
          </div>
          <h3 class="path-name">{{ path.name }}</h3>
          <p class="path-description">{{ path.description || '暂无路径描述' }}</p>
          <div class="path-meta">
            <span class="problem-count">{{ path.problemCount || 0 }} 题</span>
            <div v-if="path.progress > 0" class="path-progress">
              <div class="mini-progress-bar">
                <div class="mini-progress-fill" :style="{ width: `${path.progress}%` }"></div>
              </div>
              <span class="mini-progress-text">{{ path.progress }}%</span>
            </div>
            <span v-else class="path-status">未开始</span>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <el-empty description="暂无学习路径" />
    </div>

    <div v-if="learningPaths.length > 0" class="pagination-container">
      <el-pagination
        v-model:current-page="currentPageModel"
        v-model:page-size="pageSizeModel"
        :page-sizes="[8, 12, 16, 20]"
        layout="total, sizes, prev, pager, next"
        :total="total"
        @size-change="emit('size-change', $event)"
        @current-change="emit('current-change', $event)"
      />
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  learningPaths: {
    type: Array,
    default: () => []
  },
  paginatedLearningPaths: {
    type: Array,
    default: () => []
  },
  total: {
    type: Number,
    default: 0
  },
  currentPage: {
    type: Number,
    default: 1
  },
  pageSize: {
    type: Number,
    default: 8
  },
  languageFilter: {
    type: String,
    default: 'all'
  },
  directionFilter: {
    type: String,
    default: 'all'
  },
  getLanguageLabel: {
    type: Function,
    required: true
  },
  getDirectionLabel: {
    type: Function,
    required: true
  }
})

const emit = defineEmits([
  'update:currentPage',
  'update:pageSize',
  'update:languageFilter',
  'update:directionFilter',
  'filter-change',
  'size-change',
  'current-change',
  'open-path'
])

const currentPageModel = computed({
  get: () => props.currentPage,
  set: (value) => emit('update:currentPage', value)
})

const pageSizeModel = computed({
  get: () => props.pageSize,
  set: (value) => emit('update:pageSize', value)
})

const languageFilterModel = computed({
  get: () => props.languageFilter,
  set: (value) => emit('update:languageFilter', value)
})

const directionFilterModel = computed({
  get: () => props.directionFilter,
  set: (value) => emit('update:directionFilter', value)
})

const hasFilters = computed(() => props.languageFilter !== 'all' || props.directionFilter !== 'all')

const clearFilters = () => {
  emit('update:languageFilter', 'all')
  emit('update:directionFilter', 'all')
  emit('filter-change')
}
</script>

<style scoped>
.section-container {
  margin-bottom: 32px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--leetcode-text, #24292f);
  margin: 0 0 8px;
}

.section-subtitle {
  margin: 0;
  font-size: 13px;
  color: var(--leetcode-text-secondary, #6b7280);
}

.section-header-with-filter {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 16px;
}

.filter-controls {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.clear-btn {
  min-width: 72px;
}

.paths-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.path-card {
  background: var(--leetcode-bg, #ffffff);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid var(--leetcode-border, #e5e7eb);
  cursor: pointer;
  transition: all 0.2s ease;
}

.path-card:hover {
  border-color: var(--leetcode-primary, #0066ff);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 102, 255, 0.1);
}

.path-icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-bottom: 12px;
}

.path-header {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}

.path-language-tag {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
}

.path-language-tag.java {
  background: #fff3e0;
  color: #f57c00;
}

.path-language-tag.python {
  background: #e3f2fd;
  color: #1976d2;
}

.path-language-tag.cpp {
  background: #e8eaf6;
  color: #3f51b5;
}

.path-language-tag.all {
  background: #eef2ff;
  color: #4338ca;
}

.path-direction-tag {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  background: var(--leetcode-bg-secondary, #f7f8fa);
  color: var(--leetcode-text-secondary, #6b7280);
}

.path-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--leetcode-text, #24292f);
  margin: 0 0 8px;
}

.path-description {
  font-size: 13px;
  color: var(--leetcode-text-secondary, #6b7280);
  margin: 0 0 12px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.path-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.problem-count {
  font-size: 13px;
  color: var(--leetcode-text-secondary, #6b7280);
}

.path-progress {
  display: flex;
  align-items: center;
  gap: 8px;
}

.mini-progress-bar {
  width: 60px;
  height: 4px;
  background: var(--leetcode-bg-secondary, #f7f8fa);
  border-radius: 2px;
  overflow: hidden;
}

.mini-progress-fill {
  height: 100%;
  background: var(--leetcode-success, #00c853);
  border-radius: 2px;
}

.mini-progress-text,
.path-status {
  font-size: 12px;
  font-weight: 500;
  color: var(--leetcode-success, #00c853);
}

.path-status {
  color: var(--leetcode-text-secondary, #6b7280);
}

.empty-state {
  padding: 60px 20px;
}

.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .section-header-with-filter {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-controls {
    width: 100%;
  }

  .filter-controls > * {
    flex: 1;
  }

  .paths-grid {
    grid-template-columns: 1fr;
  }
}
</style>
