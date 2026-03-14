<template>
  <div class="leetcode-problems-page">
    <div class="problems-container">
      <div class="page-header">
        <h1 class="page-title">题库</h1>
        <p class="page-subtitle">选择题目开始练习</p>
      </div>

      <div class="filter-section">
        <div class="filter-left">
          <el-select
            v-model="filters.difficulty"
            placeholder="难度"
            clearable
            class="filter-select"
            @change="saveFilters"
          >
            <el-option label="简单" :value="0" />
            <el-option label="中等" :value="1" />
            <el-option label="困难" :value="2" />
          </el-select>
          <el-select
            v-model="filters.language"
            placeholder="语言"
            clearable
            class="filter-select"
            @change="saveFilters"
          >
            <el-option label="Java" value="java" />
            <el-option label="Python" value="python" />
          </el-select>
        </div>
        <div class="filter-right">
          <div class="search-wrapper">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索题目..."
              clearable
              class="search-input"
              @clear="handleSearchClear"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon class="search-icon"><Search /></el-icon>
              </template>
            </el-input>
          </div>
          <el-button @click="resetFilters" class="reset-btn">
            <el-icon><RefreshLeft /></el-icon>
            重置
          </el-button>
        </div>
      </div>

      <div class="problems-list" v-loading="loading">
        <div
          v-for="(item, index) in problemList"
          :key="item.id"
          class="problem-item"
          :style="{ animationDelay: `${index * 50}ms` }"
          @click="goToProblem(item.id)"
        >
          <div class="problem-left">
            <span class="problem-id">{{ item.id }}</span>
            <div class="problem-info">
              <span class="problem-title">{{ item.title }}</span>
              <div class="problem-tags">
                <span :class="['difficulty-badge', getDifficultyClass(item.difficulty)]">
                  {{ getDifficultyText(item.difficulty) }}
                </span>
                <span class="language-badge">{{ item.language.toUpperCase() }}</span>
              </div>
            </div>
          </div>
          <div class="problem-right">
            <el-button type="primary" link class="view-btn">
              <el-icon><Right /></el-icon>
              开始
            </el-button>
          </div>
        </div>

        <el-empty v-if="problemList.length === 0 && !loading" description="暂无题目" class="empty-state" />
      </div>

      <div class="pagination-section">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchProblems"
          @current-change="fetchProblems"
          class="pagination"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getProblemList } from '@/api/problem'
import { RefreshLeft, Search, Right } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const problemList = ref([])
const searchKeyword = ref('')

const filters = reactive({
  difficulty: null,
  language: null
})

const loadFilters = () => {
  const savedFilters = localStorage.getItem('problemFilters')
  if (savedFilters) {
    try {
      const parsed = JSON.parse(savedFilters)
      filters.difficulty = parsed.difficulty
      filters.language = parsed.language
    } catch (e) {
      console.error('解析筛选条件失败:', e)
    }
  }
  
  if (route.query.keyword) {
    searchKeyword.value = route.query.keyword
  }
}

const saveFilters = () => {
  localStorage.setItem('problemFilters', JSON.stringify({
    difficulty: filters.difficulty,
    language: filters.language
  }))
}

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const getDifficultyClass = (difficulty) => {
  const classes = { 0: 'difficulty-easy', 1: 'difficulty-medium', 2: 'difficulty-hard' }
  return classes[difficulty] || ''
}

const getDifficultyText = (difficulty) => {
  const texts = { 0: '简单', 1: '中等', 2: '困难' }
  return texts[difficulty] || '未知'
}

const handleSearch = () => {
  pagination.page = 1
  fetchProblems()
}

const handleSearchClear = () => {
  searchKeyword.value = ''
  pagination.page = 1
  fetchProblems()
}

const fetchProblems = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size
    }

    if (filters.difficulty !== null && filters.difficulty !== undefined) {
      params.difficulty = filters.difficulty
    }

    if (filters.language !== null && filters.language !== undefined) {
      params.language = filters.language
    }

    if (searchKeyword.value && searchKeyword.value.trim()) {
      params.keyword = searchKeyword.value.trim()
    }

    const res = await getProblemList(params)
    if (res.code === 200) {
      problemList.value = res.data.list
      pagination.total = res.data.total
    }
  } catch (error) {
    console.error('获取题目列表失败:', error)
  } finally {
    loading.value = false
  }
}

const goToProblem = (id) => {
  router.push(`/problem/${id}`)
}

const resetFilters = () => {
  filters.difficulty = null
  filters.language = null
  searchKeyword.value = ''
  saveFilters()
  pagination.page = 1
  fetchProblems()
}

watch(filters, () => {
  pagination.page = 1
  saveFilters()
  fetchProblems()
})

onMounted(() => {
  loadFilters()
  fetchProblems()
})
</script>

<style scoped>
.leetcode-problems-page {
  width: 100%;
  min-height: 100vh;
  background: var(--leetcode-bg-secondary, #F7F8FA);
  padding: 24px;
  box-sizing: border-box;
}

.problems-container {
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
  width: 120px;
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

.search-wrapper {
  position: relative;
}

.search-input {
  width: 280px;
}

.search-icon {
  color: var(--leetcode-text-secondary, #6B7280);
  transition: color 0.2s ease;
}

:deep(.search-input .el-input__wrapper) {
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border: 1px solid var(--leetcode-border, #E5E7EB);
  border-radius: 6px;
  padding: 6px 12px;
  transition: all 0.2s ease;
  box-shadow: none;
}

:deep(.search-input .el-input__wrapper:hover) {
  border-color: var(--leetcode-primary, #0066FF);
  background: #FFFFFF;
}

:deep(.search-input .el-input__wrapper.is-focus) {
  border-color: var(--leetcode-primary, #0066FF);
  background: #FFFFFF;
  box-shadow: 0 0 0 3px rgba(0, 102, 255, 0.1);
}

:deep(.search-input .el-input__wrapper.is-focus) .search-icon {
  color: var(--leetcode-primary, #0066FF);
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

.problems-list {
  min-height: 400px;
  position: relative;
}

.problem-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 32px;
  border-bottom: 1px solid var(--leetcode-border, #E5E7EB);
  cursor: pointer;
  transition: all 0.2s ease;
  animation: fadeInUp 0.3s ease-out backwards;
}

.problem-item:hover {
  background: var(--leetcode-bg-secondary, #F7F8FA);
}

.problem-item:last-child {
  border-bottom: none;
}

.problem-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.problem-id {
  font-family: 'Fira Code', 'Monaco', 'Courier New', monospace;
  font-size: 14px;
  font-weight: 600;
  color: var(--leetcode-text-secondary, #6B7280);
  min-width: 60px;
}

.problem-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}

.problem-title {
  font-size: 16px;
  font-weight: 500;
  color: var(--leetcode-primary, #0066FF);
  transition: color 0.2s ease;
}

.problem-item:hover .problem-title {
  color: var(--leetcode-primary, #0066FF);
  text-decoration: underline;
}

.problem-tags {
  display: flex;
  gap: 8px;
  align-items: center;
}

.difficulty-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

.difficulty-easy {
  background: rgba(0, 200, 83, 0.1);
  color: var(--leetcode-success, #00C853);
}

.difficulty-medium {
  background: rgba(255, 179, 0, 0.1);
  color: var(--leetcode-warning, #FFB300);
}

.difficulty-hard {
  background: rgba(238, 77, 56, 0.1);
  color: var(--leetcode-danger, #EE4D2E);
}

.language-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  background: var(--leetcode-bg-secondary, #F7F8FA);
  color: var(--leetcode-text-secondary, #6B7280);
}

.problem-right {
  display: flex;
  align-items: center;
}

.view-btn {
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--leetcode-primary, #0066FF);
  transition: all 0.2s ease;
}

.view-btn:hover {
  transform: translateX(4px);
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
  .leetcode-problems-page {
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

  .filter-select,
  .search-input {
    width: 100%;
  }

  .problem-item {
    padding: 16px 20px;
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .problem-left {
    width: 100%;
    gap: 12px;
  }

  .problem-right {
    width: 100%;
    justify-content: flex-end;
  }

  .pagination-section {
    padding: 16px 20px;
  }
}
</style>
