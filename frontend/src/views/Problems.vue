<template>
  <div class="leetcode-problems-page">
    <div class="problems-container">
      <!-- 知识点分类栏 -->
      <div class="knowledge-categories">
        <div class="category-list">
          <span 
            v-for="tag in knowledgeTags" 
            :key="tag.name"
            class="knowledge-tag"
            :class="{ active: selectedKnowledge === tag.name }"
            @click="selectKnowledge(tag.name)"
          >
            {{ tag.name }}
            <span class="tag-num">{{ tag.count }}</span>
          </span>
          <span class="knowledge-tag expand" @click="showMoreKnowledge">
            展开
            <el-icon class="expand-icon"><ArrowDown /></el-icon>
          </span>
        </div>
      </div>

      <!-- 语言/类型分类栏 -->
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
            <el-icon class="lang-icon" :style="{ color: lang.color }"><component :is="lang.icon" /></el-icon>
            <span>{{ lang.label }}</span>
          </button>
        </div>
      </div>

      <!-- 搜索和筛选栏 -->
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
          <button class="action-btn" @click="toggleSort">
            <el-icon><Sort /></el-icon>
          </button>
          <button class="action-btn" @click="showFilterPanel">
            <el-icon><Filter /></el-icon>
          </button>
        </div>
        <div class="solved-stats">
          <el-icon class="stats-icon"><CircleCheck /></el-icon>
          <span class="stats-text">{{ solvedCount }}/{{ pagination.total }} 已解答</span>
          <button class="random-btn" @click="goToRandomProblem">
            <el-icon><Refresh /></el-icon>
          </button>
        </div>
      </div>

      <!-- 题目列表 -->
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
              <span class="pass-rate">{{ item.passRate || 0 }}%</span>
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

      <!-- 分页 -->
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
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getProblemList, getLanguages } from '@/api/problem'
import { getKnowledgePointStats } from '@/api/knowledge'
import { 
  Search, 
  Sort, 
  Filter, 
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
const route = useRoute()

const loading = ref(false)
const problemList = ref([])
const searchKeyword = ref('')
const selectedKnowledge = ref('')
const selectedLang = ref('all')
const solvedCount = ref(0)

// 知识点分类数据（从API获取）
const knowledgeTags = ref([])

// 语言/类型分类数据（从API获取）
const languageTypes = ref([])

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

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

const getStatusClass = (item) => {
  if (item.isSolved) return 'status-solved'
  if (item.isAttempted) return 'status-attempted'
  if (item.isLocked) return 'status-locked'
  return 'status-default'
}

const getKnowledgePoints = (knowledgePoints) => {
  if (!knowledgePoints) return []
  return knowledgePoints.split(',').filter(tag => tag.trim())
}

const selectKnowledge = (knowledge) => {
  // 切换知识点筛选时，清除语言筛选
  selectedKnowledge.value = selectedKnowledge.value === knowledge ? '' : knowledge
  selectedLang.value = 'all'
  pagination.page = 1
  fetchProblems()
}

const selectLang = (lang) => {
  // 切换语言筛选时，清除知识点筛选
  selectedLang.value = lang
  selectedKnowledge.value = ''
  pagination.page = 1
  fetchProblems()
}

const showMoreKnowledge = () => {
  // 展开更多知识点分类
}

const toggleSort = () => {
  // 切换排序
}

const showFilterPanel = () => {
  // 显示筛选面板
}

const handleSearch = () => {
  pagination.page = 1
  fetchProblems()
}

const toggleFavorite = (item) => {
  item.isFavorited = !item.isFavorited
}

const goToRandomProblem = () => {
  if (problemList.value.length > 0) {
    const randomIndex = Math.floor(Math.random() * problemList.value.length)
    goToProblem(problemList.value[randomIndex].id)
  }
}

const goToProblem = (id) => {
  router.push(`/problem/${id}`)
}

const fetchProblems = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size
    }

    if (searchKeyword.value && searchKeyword.value.trim()) {
      params.keyword = searchKeyword.value.trim()
    }

    if (selectedLang.value && selectedLang.value !== 'all') {
      params.language = selectedLang.value
    }

    if (selectedKnowledge.value && selectedKnowledge.value.trim()) {
      params.knowledge = selectedKnowledge.value.trim()
    }

    const res = await getProblemList(params)
    if (res.code === 200) {
      // 使用后端返回的真实数据
      problemList.value = res.data.list.map((item) => ({
        ...item,
        isLocked: false,
        isPremium: false
      }))
      pagination.total = res.data.total
      solvedCount.value = res.data.solvedCount || 0
    }
  } catch (error) {
    console.error('获取题目列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取知识点分类数据
const fetchKnowledgeTags = async () => {
  try {
    const res = await getKnowledgePointStats()
    if (res.code === 200 && res.data) {
      // 从API返回的数据中提取知识点及其题目数量
      knowledgeTags.value = res.data
        .filter(item => item.problemCount > 0) // 只显示有题目的知识点
        .map(item => ({
          name: item.name,
          count: item.problemCount
        }))
        .slice(0, 12) // 只显示前12个
    }
  } catch (error) {
    console.error('获取知识点分类失败:', error)
    // 使用默认数据作为后备
    knowledgeTags.value = [
      { name: '数组', count: 2306 },
      { name: '字符串', count: 938 },
      { name: '哈希表', count: 870 },
      { name: '数学', count: 721 },
      { name: '动态规划', count: 718 },
      { name: '排序', count: 549 },
      { name: '贪心', count: 467 },
      { name: '深度优先搜索', count: 395 },
      { name: '二分查找', count: 361 },
      { name: '位运算', count: 310 },
    ]
  }
}

// 获取语言分类数据
const fetchLanguageTypes = async () => {
  try {
    const res = await getLanguages()
    if (res.code === 200) {
      languageTypes.value = res.data
    }
  } catch (error) {
    console.error('获取语言分类失败:', error)
    // 使用默认数据作为后备
    languageTypes.value = [
      { value: 'algorithm', label: '算法', icon: 'Cpu', color: '#F59E0B' },
      { value: 'database', label: '数据库', icon: 'DataLine', color: '#3B82F6' },
      { value: 'java', label: 'Java', icon: 'Document', color: '#EA2D2E' },
      { value: 'python', label: 'Python', icon: 'Coin', color: '#3776AB' },
    ]
  }
}

onMounted(() => {
  fetchProblems()
  fetchKnowledgeTags()
  fetchLanguageTypes()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

.leetcode-problems-page {
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(180deg, #FAFBFC 0%, #F5F7FA 100%);
  padding: 24px 32px;
  box-sizing: border-box;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  transition: all 0.3s ease;
}

.problems-container {
  max-width: 1200px;
  margin: 0 auto;
}

/* 知识点分类栏 - 力扣风格 */
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
  color: #6B7280;
  cursor: pointer;
  transition: color 0.2s ease;
  padding: 4px 0;
}

.knowledge-tag:hover {
  color: #1F2937;
}

.knowledge-tag.active {
  color: #1F2937;
  font-weight: 500;
}

.tag-num {
  color: #9CA3AF;
  font-size: 12px;
}

.knowledge-tag.expand {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #6B7280;
  font-weight: 500;
}

.expand-icon {
  font-size: 12px;
}

/* 语言/类型分类栏 */
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
  background: #FFFFFF;
  border: 1px solid #E5E7EB;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s ease;
}

.lang-filter-btn:hover {
  background: #F9FAFB;
  border-color: #D1D5DB;
}

.lang-filter-btn.active {
  background: #1F2937;
  border-color: #1F2937;
  color: #FFFFFF;
}

.lang-filter-btn.active .lang-icon {
  color: #FFFFFF !important;
}

.lang-icon {
  font-size: 16px;
}

/* 搜索和筛选栏 */
.search-filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: #FFFFFF;
  border: 1px solid #E5E7EB;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.search-box {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  background: #F9FAFB;
  border: 1px solid #E5E7EB;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.search-box:focus-within {
  background: #FFFFFF;
  border-color: #3B82F6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.search-icon {
  color: #9CA3AF;
  font-size: 16px;
}

.search-box input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 14px;
  color: #1F2937;
  outline: none;
}

.search-box input::placeholder {
  color: #9CA3AF;
}

.filter-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background: #F9FAFB;
  border: 1px solid #E5E7EB;
  border-radius: 8px;
  color: #6B7280;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-btn:hover {
  background: #F3F4F6;
  border-color: #D1D5DB;
  color: #374151;
}

.solved-stats {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #F0FDF4;
  border: 1px solid #BBF7D0;
  border-radius: 8px;
}

.stats-icon {
  color: #10B981;
  font-size: 16px;
}

.stats-text {
  font-size: 13px;
  font-weight: 500;
  color: #065F46;
}

.random-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  background: #FFFFFF;
  border: 1px solid #10B981;
  border-radius: 6px;
  color: #10B981;
  cursor: pointer;
  transition: all 0.2s ease;
}

.random-btn:hover {
  background: #10B981;
  color: #FFFFFF;
}

/* 题目列表 - 力扣风格 */
.problems-list-container {
  background: #FFFFFF;
  border: 1px solid #E5E7EB;
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
  border-bottom: 1px solid #F3F4F6;
  cursor: pointer;
  transition: background-color 0.15s ease;
  animation: fadeIn 0.3s ease-out backwards;
}

.problem-item:hover {
  background-color: #F9FAFB;
}

.problem-item.solved {
  background-color: #F0FDF4;
}

.problem-item.solved:hover {
  background-color: #DCFCE7;
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

/* 题目主要内容区 */
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
  color: #10B981;
}

.attempted-icon {
  color: #F59E0B;
}

.default-icon {
  color: #3B82F6;
}

.problem-content {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
  min-width: 0;
}

.problem-id {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  font-size: 14px;
  font-weight: 500;
  color: #6B7280;
  flex-shrink: 0;
}

.problem-title-text {
  font-size: 14px;
  font-weight: 500;
  color: #1F2937;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.problem-item:hover .problem-title-text {
  color: #3B82F6;
}

/* 题目元信息区 */
.problem-meta {
  display: flex;
  align-items: center;
  gap: 24px;
  flex-shrink: 0;
}

.pass-rate {
  font-size: 13px;
  font-weight: 500;
  color: #6B7280;
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
  color: #10B981;
}

.difficulty-medium {
  color: #F59E0B;
}

.difficulty-hard {
  color: #EF4444;
}

/* 操作图标区 */
.action-icons {
  display: flex;
  align-items: center;
  gap: 12px;
}

.lock-icon {
  font-size: 14px;
  color: #9CA3AF;
}

.favorite-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  background: transparent;
  border: none;
  color: #D1D5DB;
  cursor: pointer;
  transition: all 0.2s ease;
  padding: 0;
}

.favorite-btn:hover {
  color: #F59E0B;
  transform: scale(1.15);
}

.favorite-btn.active {
  color: #F59E0B;
}

/* 空状态 */
.empty-state {
  padding: 80px 20px;
}

/* 分页 */
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
  background: #FFFFFF;
  border: 1px solid #E5E7EB;
  color: #374151;
  border-radius: 8px;
  min-width: 36px;
  height: 36px;
  font-weight: 500;
  transition: all 0.2s ease;
}

:deep(.pagination button:hover:not(:disabled)) {
  border-color: #3B82F6;
  color: #3B82F6;
  background: #EFF6FF;
}

:deep(.pagination button.is-active) {
  background: #1F2937;
  border-color: #1F2937;
  color: #FFFFFF;
}

:deep(.pagination button:disabled) {
  opacity: 0.5;
  cursor: not-allowed;
}

:deep(.pagination .el-pager li) {
  background: #FFFFFF;
  border: 1px solid #E5E7EB;
  color: #374151;
  border-radius: 8px;
  min-width: 36px;
  height: 36px;
  font-weight: 500;
  transition: all 0.2s ease;
}

:deep(.pagination .el-pager li:hover) {
  border-color: #3B82F6;
  color: #3B82F6;
  background: #EFF6FF;
}

:deep(.pagination .el-pager li.is-active) {
  background: #1F2937;
  border-color: #1F2937;
  color: #FFFFFF;
}

:deep(.pagination__total) {
  color: #6B7280;
  font-size: 14px;
  font-weight: 500;
}

:deep(.pagination__sizes .el-select .el-input__wrapper) {
  background: #FFFFFF;
  border: 1px solid #E5E7EB;
  border-radius: 8px;
}

/* 响应式 */
@media (max-width: 768px) {
  .leetcode-problems-page {
    padding: 16px;
  }

  .category-tags {
    overflow-x: auto;
    flex-wrap: nowrap;
    padding-bottom: 8px;
  }

  .search-filter-bar {
    flex-wrap: wrap;
  }

  .solved-stats {
    width: 100%;
    justify-content: space-between;
  }

  .problem-item {
    padding: 12px 16px;
  }

  .problem-meta {
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
