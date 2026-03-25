<template>
  <div class="learning-plan-page">
    <div class="learn-container">
      <!-- 页面头部 -->
      <div class="page-header">
        <div class="header-left">
          <h1 class="page-title">学习计划</h1>
        </div>
        <div class="header-right">
          <el-button class="my-plan-btn" @click="goToMyPlan">
            我的学习计划
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>

      <!-- 进行中 -->
      <section class="section-container" v-if="inProgressPaths.length > 0">
        <h2 class="section-title">进行中</h2>
        <div class="progress-cards">
          <div 
            v-for="path in inProgressPaths" 
            :key="path.id"
            class="progress-card"
            @click="goToPath(path.id)"
          >
            <div class="progress-icon" :style="{ background: path.gradient }">
              <el-icon :size="32"><component :is="path.icon" /></el-icon>
            </div>
            <div class="progress-info">
              <h3 class="progress-name">{{ path.name }}</h3>
              <div class="progress-bar-wrapper">
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: path.progress + '%' }"></div>
                </div>
                <span class="progress-text">完成进度</span>
                <span class="progress-count">{{ path.completed }} / {{ path.total }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 精选 -->
      <section class="section-container">
        <h2 class="section-title">精选</h2>
        <div class="featured-grid">
          <div 
            v-for="path in featuredPaths" 
            :key="path.id"
            class="featured-card"
            :style="{ background: path.gradient }"
            @click="goToPath(path.id)"
          >
            <div class="featured-content">
              <h3 class="featured-name">{{ path.name }}</h3>
              <p class="featured-desc">{{ path.description }}</p>
            </div>
            <div class="featured-icon">
              <el-icon :size="48"><component :is="path.icon" /></el-icon>
            </div>
          </div>
        </div>
      </section>

      <!-- 所有学习路径 -->
      <section class="section-container">
        <div class="section-header-with-filter">
          <h2 class="section-title">全部路径</h2>
          <div class="filter-controls">
            <el-select v-model="languageFilter" size="small" placeholder="语言" @change="handleFilterChange">
              <el-option label="全部" value="all" />
              <el-option label="Java" value="java" />
              <el-option label="Python" value="python" />
            </el-select>
            <el-select v-model="directionFilter" size="small" placeholder="方向" @change="handleFilterChange">
              <el-option label="全部" value="all" />
              <el-option label="语言基础" value="language" />
              <el-option label="算法与数据结构" value="algorithm" />
              <el-option label="后端开发" value="backend" />
            </el-select>
          </div>
        </div>
        
        <div v-if="learningPaths.length > 0" class="paths-grid">
          <div 
            v-for="path in paginatedLearningPaths" 
            :key="path.id" 
            class="path-card"
            @click="goToPath(path.id)"
          >
            <div class="path-icon-wrapper" :style="{ background: path.gradient }">
              <el-icon :size="28"><component :is="path.icon" /></el-icon>
            </div>
            <div class="path-content">
              <div class="path-header">
                <span class="path-language-tag" :class="path.language">{{ getLanguageLabel(path.language) }}</span>
                <span class="path-direction-tag">{{ getDirectionLabel(path.direction) }}</span>
              </div>
              <h3 class="path-name">{{ path.name }}</h3>
              <p class="path-description">{{ path.description }}</p>
              <div class="path-meta">
                <span class="problem-count">{{ path.problemCount || 0 }} 题</span>
                <div v-if="path.progress > 0" class="path-progress">
                  <div class="mini-progress-bar">
                    <div class="mini-progress-fill" :style="{ width: path.progress + '%' }"></div>
                  </div>
                  <span class="mini-progress-text">{{ path.progress }}%</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div v-else class="empty-state">
          <el-empty description="暂无学习路径" />
        </div>

        <div class="pagination-container" v-if="learningPaths.length > 0">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[8, 12, 16, 20]"
            layout="total, sizes, prev, pager, next"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, markRaw } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowRight,
  Coffee,
  DataLine,
  Document,
  Cpu,
  Connection,
  Collection,
  Grid,
  Histogram,
  Menu,
  OfficeBuilding,
  Opportunity,
  Platform,
  Reading,
  School,
  TrendCharts
} from '@element-plus/icons-vue'
import { getAvailablePaths, getPathProgress } from '@/api/learn'

const router = useRouter()

// 数据
const inProgressPaths = ref([])
const featuredPaths = ref([])
const learningPaths = ref([])
const currentPage = ref(1)
const pageSize = ref(8)
const total = ref(0)
const languageFilter = ref('all')
const directionFilter = ref('all')

// 计算属性
const paginatedLearningPaths = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return learningPaths.value.slice(start, end)
})

// 获取图标组件
const getIconComponent = (iconName) => {
  const iconMap = {
    'Coffee': markRaw(Coffee),
    'DataLine': markRaw(DataLine),
    'Document': markRaw(Document),
    'Cpu': markRaw(Cpu),
    'Connection': markRaw(Connection),
    'Collection': markRaw(Collection),
    'Grid': markRaw(Grid),
    'Histogram': markRaw(Histogram),
    'Menu': markRaw(Menu),
    'OfficeBuilding': markRaw(OfficeBuilding),
    'Opportunity': markRaw(Opportunity),
    'Platform': markRaw(Platform),
    'Reading': markRaw(Reading),
    'School': markRaw(School),
    'TrendCharts': markRaw(TrendCharts),
    'ArrowRight': markRaw(ArrowRight)
  }
  return iconMap[iconName] || markRaw(Document)
}

// 获取语言标签
const getLanguageLabel = (language) => {
  const labels = {
    'java': 'Java',
    'python': 'Python',
    'cpp': 'C++',
    'all': '通用'
  }
  return labels[language] || language
}

// 获取方向标签
const getDirectionLabel = (direction) => {
  const labels = {
    'language': '语言基础',
    'algorithm': '算法与数据结构',
    'backend': '后端开发',
    'frontend': '前端开发',
    'database': '数据库',
    'system': '系统设计'
  }
  return labels[direction] || direction
}

// 获取进行中的路径
const fetchInProgressPaths = async () => {
  try {
    const res = await getPathProgress()
    if (res.code === 200 && res.data) {
      const paths = Array.isArray(res.data) ? res.data : (res.data.paths || [])
      inProgressPaths.value = paths.map(path => ({
        ...path,
        icon: getIconComponent(path.icon || 'Document'),
        gradient: path.gradient || 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
      }))
    }
  } catch (error) {
    console.error('获取进行中的路径失败:', error)
    inProgressPaths.value = []
  }
}

// 获取精选路径
const fetchFeaturedPaths = async () => {
  try {
    const res = await getAvailablePaths({ featured: true })
    if (res.code === 200 && res.data) {
      const paths = Array.isArray(res.data) ? res.data : (res.data.paths || [])
      featuredPaths.value = paths.map(path => ({
        ...path,
        icon: getIconComponent(path.icon || 'Document'),
        gradient: path.gradient || 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
      }))
    }
  } catch (error) {
    console.error('获取精选路径失败:', error)
    featuredPaths.value = []
  }
}

// 获取所有学习路径
const fetchLearningPaths = async () => {
  try {
    const params = {}
    if (languageFilter.value !== 'all') {
      params.language = languageFilter.value
    }
    if (directionFilter.value !== 'all') {
      params.direction = directionFilter.value
    }
    const res = await getAvailablePaths(params)
    if (res.code === 200 && res.data) {
      const paths = Array.isArray(res.data) ? res.data : (res.data.paths || [])
      learningPaths.value = paths.map(path => ({
        ...path,
        icon: getIconComponent(path.icon || 'Document'),
        gradient: path.gradient || 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
      }))
      total.value = learningPaths.value.length
    }
  } catch (error) {
    console.error('获取学习路径失败:', error)
    learningPaths.value = []
    total.value = 0
  }
}

// 处理筛选变化
const handleFilterChange = () => {
  currentPage.value = 1
  fetchLearningPaths()
}

// 处理分页
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (page) => {
  currentPage.value = page
}

// 跳转到学习路径详情
const goToPath = (pathId) => {
  router.push(`/learn/path/${pathId}`)
}

// 跳转到我的学习计划
const goToMyPlan = () => {
  router.push('/learn/assessment')
}

// 初始化
onMounted(() => {
  fetchInProgressPaths()
  fetchFeaturedPaths()
  fetchLearningPaths()
})
</script>

<style scoped>
.learning-plan-page {
  min-height: 100vh;
  background: var(--leetcode-bg-secondary, #F7F8FA);
  padding: 24px;
}

.learn-container {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--leetcode-text, #24292F);
  margin: 0;
}

.my-plan-btn {
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-container {
  margin-bottom: 32px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--leetcode-text, #24292F);
  margin: 0 0 16px 0;
}

.section-header-with-filter {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.filter-controls {
  display: flex;
  gap: 12px;
}

/* 进行中卡片 */
.progress-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.progress-card {
  background: var(--leetcode-bg, #FFFFFF);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid var(--leetcode-border, #E5E7EB);
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  gap: 16px;
}

.progress-card:hover {
  border-color: var(--leetcode-primary, #0066FF);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 102, 255, 0.1);
}

.progress-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.progress-info {
  flex: 1;
}

.progress-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--leetcode-text, #24292F);
  margin: 0 0 12px 0;
}

.progress-bar-wrapper {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.progress-bar {
  height: 6px;
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: var(--leetcode-primary, #0066FF);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 12px;
  color: var(--leetcode-text-secondary, #6B7280);
}

.progress-count {
  font-size: 12px;
  color: var(--leetcode-primary, #0066FF);
  font-weight: 500;
}

/* 精选卡片 */
.featured-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.featured-card {
  border-radius: 12px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: white;
}

.featured-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.featured-content {
  flex: 1;
}

.featured-name {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.featured-desc {
  font-size: 14px;
  opacity: 0.9;
  margin: 0;
}

.featured-icon {
  opacity: 0.8;
}

/* 路径网格 */
.paths-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.path-card {
  background: var(--leetcode-bg, #FFFFFF);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid var(--leetcode-border, #E5E7EB);
  cursor: pointer;
  transition: all 0.2s ease;
}

.path-card:hover {
  border-color: var(--leetcode-primary, #0066FF);
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
  margin-bottom: 8px;
}

.path-language-tag {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
}

.path-language-tag.java {
  background: #FFF3E0;
  color: #F57C00;
}

.path-language-tag.python {
  background: #E3F2FD;
  color: #1976D2;
}

.path-language-tag.cpp {
  background: #E8EAF6;
  color: #3F51B5;
}

.path-direction-tag {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  background: var(--leetcode-bg-secondary, #F7F8FA);
  color: var(--leetcode-text-secondary, #6B7280);
}

.path-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--leetcode-text, #24292F);
  margin: 0 0 8px 0;
}

.path-description {
  font-size: 13px;
  color: var(--leetcode-text-secondary, #6B7280);
  margin: 0 0 12px 0;
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
}

.problem-count {
  font-size: 13px;
  color: var(--leetcode-text-secondary, #6B7280);
}

.path-progress {
  display: flex;
  align-items: center;
  gap: 8px;
}

.mini-progress-bar {
  width: 60px;
  height: 4px;
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border-radius: 2px;
  overflow: hidden;
}

.mini-progress-fill {
  height: 100%;
  background: var(--leetcode-success, #00C853);
  border-radius: 2px;
}

.mini-progress-text {
  font-size: 12px;
  color: var(--leetcode-success, #00C853);
  font-weight: 500;
}

/* 空状态 */
.empty-state {
  padding: 60px 20px;
}

/* 分页 */
.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .learning-plan-page {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .section-header-with-filter {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .filter-controls {
    width: 100%;
  }
  
  .progress-cards,
  .featured-grid,
  .paths-grid {
    grid-template-columns: 1fr;
  }
}
</style>