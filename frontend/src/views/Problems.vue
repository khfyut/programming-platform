<template>
  <div class="leetcode-problems-page">
    <div class="problems-container">
      <div class="knowledge-categories">
        <div class="category-list">
          <span
            v-for="tag in visibleKnowledgeTags"
            :key="tag.name"
            class="knowledge-tag"
            :class="{ active: selectedKnowledge === tag.name }"
            @click="selectKnowledge(tag.name)"
          >
            {{ tag.name }}
            <span class="tag-num">{{ tag.count }}</span>
          </span>
          <span
            v-if="knowledgeTags.length > 12"
            class="knowledge-tag expand"
            :class="{ expanded: showAllKnowledge }"
            @click="toggleKnowledgeExpand"
          >
            {{ showAllKnowledge ? '收起' : '展开' }}
            <el-icon class="expand-icon"><ArrowDown /></el-icon>
          </span>
        </div>
      </div>

      <div class="language-categories">
        <div class="lang-filter-list">
          <button
            class="lang-filter-btn"
            :class="{ active: selectedLang === 'all' }"
            @click="selectLang('all')"
          >
            <el-icon class="lang-icon"><Menu /></el-icon>
            <span>全部题目</span>
          </button>
          <button
            v-for="lang in languageTypes"
            :key="lang.value"
            class="lang-filter-btn"
            :class="{ active: selectedLang === lang.value }"
            @click="selectLang(lang.value)"
          >
            <el-icon class="lang-icon" :style="{ color: lang.color }">
              <component :is="resolveLanguageIcon(lang.icon)" />
            </el-icon>
            <span>{{ lang.label }}</span>
          </button>
        </div>
      </div>

      <div class="search-filter-bar">
        <div class="search-box">
          <el-icon class="search-icon"><Search /></el-icon>
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索题目..."
            @keyup.enter="handleSearch"
          />
        </div>
        <div class="filter-actions">
          <button class="action-btn" :class="{ active: sortMode !== 'default' }" @click="toggleSort">
            <el-icon><Sort /></el-icon>
            <span class="action-text">{{ sortLabel }}</span>
          </button>
          <button class="action-btn" :disabled="!hasActiveFilters" @click="clearFilters">
            <el-icon><Close /></el-icon>
            <span class="action-text">清空筛选</span>
          </button>
        </div>
        <div class="solved-stats">
          <el-icon class="stats-icon"><CircleCheck /></el-icon>
          <span class="stats-text">{{ solvedCount }}/{{ pagination.total }} 已解决</span>
          <button class="random-btn" :disabled="problemList.length === 0" @click="goToRandomProblem">
            <el-icon><Refresh /></el-icon>
          </button>
        </div>
      </div>

      <div class="problems-list-container" v-loading="loading">
        <div class="problems-list">
          <div
            v-for="(item, index) in problemList"
            :key="item.id"
            class="problem-item"
            :class="{
              solved: item.isSolved,
              locked: item.isLocked
            }"
            :style="{ animationDelay: `${index * 40}ms` }"
            @click="goToProblem(item.id)"
          >
            <div class="problem-main">
              <div class="status-wrapper">
                <el-icon v-if="item.isSolved" class="status-icon solved-icon"><Select /></el-icon>
                <el-icon v-else-if="item.isAttempted" class="status-icon attempted-icon"><CircleCheck /></el-icon>
                <el-icon v-else class="status-icon default-icon"><Calendar /></el-icon>
              </div>
              <div class="problem-content">
                <span class="problem-id">{{ item.id }}.</span>
                <span class="problem-title-text">{{ item.title }}</span>
              </div>
            </div>
            <div class="problem-meta">
              <span class="pass-rate">{{ Number(item.passRate || 0) }}%</span>
              <span :class="['difficulty-text', getDifficultyClass(item.difficulty)]">
                {{ getDifficultyText(item.difficulty) }}
              </span>
              <div class="action-icons">
                <el-icon v-if="item.isLocked" class="lock-icon"><Lock /></el-icon>
                <button
                  class="favorite-btn"
                  :class="{ active: item.isFavorited }"
                  @click.stop="toggleFavorite(item)"
                >
                  <el-icon><StarFilled v-if="item.isFavorited" /><Star v-else /></el-icon>
                </button>
              </div>
            </div>
          </div>
        </div>

        <el-empty v-if="problemList.length === 0 && !loading" description="暂无题目" class="empty-state" />
      </div>

      <div class="pagination-section">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchProblems"
          @current-change="fetchProblems"
          class="pagination"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'
import { useProblemStore } from '@/stores/problem'
import {
  Search,
  Sort,
  Close,
  CircleCheck,
  Select,
  Lock,
  Star,
  StarFilled,
  ArrowDown,
  DataLine,
  Cpu,
  Grid,
  Calendar,
  Menu,
  Coin,
  Document,
  Refresh,
  Monitor
} from '@element-plus/icons-vue'

const router = useRouter()
const problemStore = useProblemStore()

const {
  loading,
  problemList,
  knowledgeTags,
  languageTypes,
  searchKeyword,
  selectedKnowledge,
  selectedLang,
  solvedCount,
  showAllKnowledge,
  sortMode,
  pagination,
  visibleKnowledgeTags,
  sortLabel,
  hasActiveFilters
} = storeToRefs(problemStore)

const {
  initialize,
  fetchProblems,
  selectKnowledge,
  selectLang,
  toggleKnowledgeExpand,
  toggleSort,
  handleSearch,
  clearFilters,
  toggleFavorite
} = problemStore

const iconMap = {
  DataLine,
  Cpu,
  Grid,
  Coin,
  Document,
  Monitor
}

const getDifficultyClass = (difficulty) => {
  const classes = {
    0: 'difficulty-easy',
    1: 'difficulty-medium',
    2: 'difficulty-hard',
    3: 'difficulty-hard'
  }
  return classes[difficulty] || ''
}

const getDifficultyText = (difficulty) => {
  const texts = {
    0: '简单',
    1: '中等',
    2: '困难',
    3: '困难'
  }
  return texts[difficulty] || '未知'
}

const resolveLanguageIcon = (icon) => {
  if (!icon) {
    return Document
  }

  if (typeof icon === 'string') {
    return iconMap[icon] || Document
  }

  return icon
}

const goToRandomProblem = () => {
  if (problemList.value.length === 0) {
    return
  }

  const randomIndex = Math.floor(Math.random() * problemList.value.length)
  goToProblem(problemList.value[randomIndex].id)
}

const goToProblem = (id) => {
  router.push(`/problem/${id}`)
}

onMounted(() => {
  initialize()
})
</script>

<style scoped>
.leetcode-problems-page {
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(180deg, #fafbfc 0%, #f5f7fa 100%);
  padding: 24px 32px;
  box-sizing: border-box;
  font-family: var(--font-primary, 'Segoe UI', sans-serif);
  transition: all 0.3s ease;
}

.problems-container {
  max-width: 1200px;
  margin: 0 auto;
}

.knowledge-categories {
  margin-bottom: 12px;
}

.category-list {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 16px;
  align-items: center;
}

.knowledge-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #6b7280;
  cursor: pointer;
  transition: color 0.2s ease;
  padding: 4px 0;
}

.knowledge-tag:hover {
  color: #1f2937;
}

.knowledge-tag.active {
  color: #1f2937;
  font-weight: 500;
}

.tag-num {
  color: #9ca3af;
  font-size: 12px;
}

.knowledge-tag.expand {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #6b7280;
  font-weight: 500;
}

.knowledge-tag.expand.expanded .expand-icon {
  transform: rotate(180deg);
}

.expand-icon {
  font-size: 12px;
  transition: transform 0.2s ease;
}

.language-categories {
  margin-bottom: 16px;
}

.lang-filter-list {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.lang-filter-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s ease;
}

.lang-filter-btn:hover {
  background: #f9fafb;
  border-color: #d1d5db;
}

.lang-filter-btn.active {
  background: #1f2937;
  border-color: #1f2937;
  color: #ffffff;
}

.lang-filter-btn.active .lang-icon {
  color: #ffffff !important;
}

.lang-icon {
  font-size: 16px;
}

.search-filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.search-box {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.search-box:focus-within {
  background: #ffffff;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.search-icon {
  color: #9ca3af;
  font-size: 16px;
}

.search-box input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 14px;
  color: #1f2937;
  outline: none;
}

.search-box input::placeholder {
  color: #9ca3af;
}

.filter-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-height: 36px;
  padding: 0 12px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-btn:hover {
  background: #f3f4f6;
  border-color: #d1d5db;
  color: #374151;
}

.action-btn.active {
  background: #eff6ff;
  border-color: #bfdbfe;
  color: #1d4ed8;
}

.action-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.action-btn:disabled:hover {
  background: #f9fafb;
  border-color: #e5e7eb;
  color: #6b7280;
}

.action-text {
  font-size: 13px;
  font-weight: 500;
}

.solved-stats {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 8px;
}

.stats-icon {
  color: #10b981;
  font-size: 16px;
}

.stats-text {
  font-size: 13px;
  font-weight: 500;
  color: #065f46;
}

.random-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  background: #ffffff;
  border: 1px solid #10b981;
  border-radius: 6px;
  color: #10b981;
  cursor: pointer;
  transition: all 0.2s ease;
}

.random-btn:hover {
  background: #10b981;
  color: #ffffff;
}

.random-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
  background: #ffffff;
  color: #10b981;
}

.problems-list-container {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.problems-list {
  min-height: 400px;
}

.problem-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  border-bottom: 1px solid #f3f4f6;
  cursor: pointer;
  transition: background-color 0.15s ease;
  animation: fadeIn 0.3s ease-out backwards;
}

.problem-item:hover {
  background-color: #f9fafb;
}

.problem-item.solved {
  background-color: #f0fdf4;
}

.problem-item.solved:hover {
  background-color: #dcfce7;
}

.problem-item.solved .problem-title-text {
  color: #059669;
}

.problem-item:last-child {
  border-bottom: none;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(5px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.problem-main {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.status-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.status-icon {
  font-size: 16px;
}

.solved-icon {
  color: #10b981;
}

.attempted-icon {
  color: #f59e0b;
}

.default-icon {
  color: #3b82f6;
}

.problem-content {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
  min-width: 0;
}

.problem-id {
  font-size: 14px;
  font-weight: 500;
  color: #6b7280;
  flex-shrink: 0;
}

.problem-title-text {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.problem-item:hover .problem-title-text {
  color: #3b82f6;
}

.problem-meta {
  display: flex;
  align-items: center;
  gap: 24px;
  flex-shrink: 0;
}

.pass-rate {
  font-size: 13px;
  font-weight: 500;
  color: #6b7280;
  min-width: 45px;
  text-align: right;
}

.difficulty-text {
  font-size: 13px;
  font-weight: 500;
  min-width: 32px;
  text-align: center;
}

.difficulty-easy {
  color: #10b981;
}

.difficulty-medium {
  color: #f59e0b;
}

.difficulty-hard {
  color: #ef4444;
}

.action-icons {
  display: flex;
  align-items: center;
  gap: 12px;
}

.lock-icon {
  font-size: 14px;
  color: #9ca3af;
}

.favorite-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  background: transparent;
  border: none;
  color: #d1d5db;
  cursor: pointer;
  transition: all 0.2s ease;
  padding: 0;
}

.favorite-btn:hover {
  color: #f59e0b;
  transform: scale(1.15);
}

.favorite-btn.active {
  color: #f59e0b;
}

.empty-state {
  padding: 80px 20px;
}

.pagination-section {
  display: flex;
  justify-content: center;
  padding: 24px 0;
}

:deep(.pagination) {
  display: flex;
  align-items: center;
  gap: 6px;
}

:deep(.pagination button) {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  color: #374151;
  border-radius: 8px;
  min-width: 36px;
  height: 36px;
  font-weight: 500;
  transition: all 0.2s ease;
}

:deep(.pagination button:hover:not(:disabled)) {
  border-color: #3b82f6;
  color: #3b82f6;
  background: #eff6ff;
}

:deep(.pagination button.is-active) {
  background: #1f2937;
  border-color: #1f2937;
  color: #ffffff;
}

:deep(.pagination button:disabled) {
  opacity: 0.5;
  cursor: not-allowed;
}

:deep(.pagination .el-pager li) {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  color: #374151;
  border-radius: 8px;
  min-width: 36px;
  height: 36px;
  font-weight: 500;
  transition: all 0.2s ease;
}

:deep(.pagination .el-pager li:hover) {
  border-color: #3b82f6;
  color: #3b82f6;
  background: #eff6ff;
}

:deep(.pagination .el-pager li.is-active) {
  background: #1f2937;
  border-color: #1f2937;
  color: #ffffff;
}

:deep(.pagination__total) {
  color: #6b7280;
  font-size: 14px;
  font-weight: 500;
}

:deep(.pagination__sizes .el-select .el-input__wrapper) {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

@media (max-width: 768px) {
  .leetcode-problems-page {
    padding: 16px;
  }

  .category-list {
    overflow-x: auto;
    flex-wrap: nowrap;
    padding-bottom: 8px;
  }

  .search-filter-bar {
    flex-wrap: wrap;
  }

  .filter-actions,
  .action-btn {
    width: 100%;
  }

  .solved-stats {
    width: 100%;
    justify-content: space-between;
  }

  .problem-item {
    padding: 12px 16px;
  }

  .problem-meta {
    flex-wrap: wrap;
    gap: 16px;
  }

  .pass-rate {
    min-width: 40px;
  }

  .difficulty-text {
    min-width: 28px;
  }
}
</style>
