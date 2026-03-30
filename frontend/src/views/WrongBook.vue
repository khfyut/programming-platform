<template>
  <div class="wrong-book-page">
    <div class="wrong-book-container">
      <div class="page-header">
        <h1 class="page-title">错题本</h1>
        <p class="page-subtitle">集中整理做错的题目，按错误原因和知识点安排复习。</p>
      </div>

      <div class="stats-overview">
        <div class="stat-card">
          <div class="stat-icon total">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ totalCount }}</div>
            <div class="stat-label">错题总数</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon pending">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ pendingCount }}</div>
            <div class="stat-label">待复习</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon reviewed">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ reviewedCount }}</div>
            <div class="stat-label">已复习</div>
          </div>
        </div>
      </div>

      <div class="content-layout">
        <div class="main-content">
          <div class="filter-section">
            <div class="filter-left">
              <el-select
                v-model="filters.knowledgePoint"
                placeholder="知识点筛选"
                clearable
                class="filter-select"
                @change="handleFilterChange"
              >
                <el-option v-for="point in knowledgePoints" :key="point" :label="point" :value="point" />
              </el-select>
              <el-select
                v-model="filters.difficulty"
                placeholder="难度筛选"
                clearable
                class="filter-select"
                @change="handleFilterChange"
              >
                <el-option label="简单" :value="0" />
                <el-option label="中等" :value="1" />
                <el-option label="困难" :value="2" />
              </el-select>
              <el-select v-model="sortBy" class="filter-select" @change="handleSortChange">
                <el-option label="最新记录" value="latest" />
                <el-option label="最早记录" value="oldest" />
                <el-option label="优先待复习" value="status" />
              </el-select>
            </div>
            <div class="filter-right">
              <el-button :disabled="!hasActiveFilters" @click="resetFilters">重置筛选</el-button>
              <el-button @click="fetchAllData">刷新</el-button>
              <el-button type="primary" @click="showReviewPlan">
                <el-icon><Calendar /></el-icon>
                复习计划
              </el-button>
            </div>
          </div>

          <div v-loading="loading" class="wrong-list">
            <div
              v-for="item in pagedWrongList"
              :key="item.id"
              class="wrong-item"
              @click="showWrongDetail(item)"
            >
              <div class="wrong-left">
                <div class="wrong-status" :class="getStatusClass(item.reviewStatus)">
                  {{ getStatusText(item.reviewStatus) }}
                </div>
                <div class="wrong-info">
                  <div class="wrong-title">{{ item.problemTitle }}</div>
                  <div class="wrong-meta">
                    <span class="meta-item">
                      <el-icon><Document /></el-icon>
                      {{ item.errorMessage || '未知错误' }}
                    </span>
                    <span class="meta-item">
                      <el-icon><Clock /></el-icon>
                      {{ formatTime(item.createTime) }}
                    </span>
                    <span class="meta-item" v-if="item.knowledgePoints">
                      {{ item.knowledgePoints }}
                    </span>
                  </div>
                </div>
              </div>
              <div class="wrong-right">
                <el-button
                  type="primary"
                  size="small"
                  :disabled="Number(item.reviewStatus) >= 1"
                  @click.stop="reviewItem(item)"
                >
                  {{ Number(item.reviewStatus) >= 1 ? '已复习' : '标记已复习' }}
                </el-button>
                <el-button type="danger" size="small" text @click.stop="deleteItem(item.id)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>

            <el-empty v-if="pagedWrongList.length === 0 && !loading" description="暂无错题记录" />
          </div>

          <div class="pagination-section">
            <el-pagination
              v-model:current-page="pagination.page"
              v-model:page-size="pagination.size"
              :total="pagination.total"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              @size-change="handlePageSizeChange"
            />
          </div>
        </div>

        <div class="side-content">
          <div class="distribution-card">
            <h3 class="card-title">知识点分布</h3>
            <div v-if="knowledgeDistribution.length > 0" class="distribution-chart">
              <div v-for="item in knowledgeDistribution" :key="item.knowledgePoint" class="distribution-item">
                <div class="distribution-label">{{ item.knowledgePoint || '未分类' }}</div>
                <div class="distribution-bar">
                  <div class="bar-fill" :style="{ width: `${(item.count / maxKnowledgeCount) * 100}%` }"></div>
                </div>
                <div class="distribution-count">{{ item.count }}</div>
              </div>
            </div>
            <el-empty v-else description="暂无知识点分布数据" />
          </div>

          <div class="distribution-card">
            <h3 class="card-title">难度分布</h3>
            <div class="difficulty-stats">
              <div class="difficulty-item easy">
                <div class="difficulty-label">简单</div>
                <div class="difficulty-bar">
                  <div class="bar-fill" :style="{ width: `${getDifficultyWidth(0)}%` }"></div>
                </div>
                <div class="difficulty-count">{{ difficultyDistribution.easy }}</div>
              </div>
              <div class="difficulty-item medium">
                <div class="difficulty-label">中等</div>
                <div class="difficulty-bar">
                  <div class="bar-fill" :style="{ width: `${getDifficultyWidth(1)}%` }"></div>
                </div>
                <div class="difficulty-count">{{ difficultyDistribution.medium }}</div>
              </div>
              <div class="difficulty-item hard">
                <div class="difficulty-label">困难</div>
                <div class="difficulty-bar">
                  <div class="bar-fill" :style="{ width: `${getDifficultyWidth(2)}%` }"></div>
                </div>
                <div class="difficulty-count">{{ difficultyDistribution.hard }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog
      v-model="detailVisible"
      title="错题详情"
      width="min(880px, 94vw)"
      top="5vh"
      destroy-on-close
      class="detail-dialog"
    >
      <div v-if="currentWrong" class="wrong-detail" v-loading="detailLoading">
        <div class="detail-section">
          <h4>题目信息</h4>
          <div class="problem-info">
            <div class="problem-title">{{ currentWrong.problem?.title || currentWrong.problemTitle }}</div>
            <div class="problem-content">
              {{ currentWrong.problem?.content || '当前错题记录未附带完整题面，可点击下方按钮前往题目详情。' }}
            </div>
          </div>
        </div>
        <div class="detail-section">
          <h4>错误分析</h4>
          <div class="error-analysis">
            <div class="analysis-item">
              <span class="label">错误信息：</span>
              <span class="value">{{ currentWrong.errorMessage || '未知错误' }}</span>
            </div>
            <div class="analysis-item">
              <span class="label">知识点：</span>
              <span class="value">{{ currentWrong.knowledgePoints || '未标注' }}</span>
            </div>
            <div class="analysis-item">
              <span class="label">最近记录：</span>
              <span class="value">{{ formatDateTime(currentWrong.createTime) }}</span>
            </div>
          </div>
        </div>
        <div class="detail-section">
          <h4>提交代码</h4>
          <pre class="code-block">{{ currentWrong.code || '暂无代码记录' }}</pre>
        </div>
      </div>
      <el-empty v-else-if="!detailLoading" description="暂无错题详情" />
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button v-if="currentWrong?.problemId" @click="openProblem(currentWrong.problemId)">前往题目</el-button>
        <el-button type="primary" :disabled="Number(currentWrong?.reviewStatus) >= 1" @click="reviewCurrentItem">
          {{ Number(currentWrong?.reviewStatus) >= 1 ? '已复习' : '标记已复习' }}
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="planVisible"
      title="复习计划"
      width="min(680px, 94vw)"
      top="8vh"
      destroy-on-close
      class="plan-dialog"
    >
      <div class="review-plan">
        <div class="plan-header">
          <span class="plan-title">待安排复习项</span>
          <span class="plan-count">{{ reviewPlan.length }} 项</span>
        </div>
        <div v-if="reviewPlan.length > 0" class="plan-list">
          <div
            v-for="plan in reviewPlan"
            :key="plan.id"
            class="plan-item"
            :class="{ reviewed: Number(plan.status) === 1 }"
          >
            <div class="plan-info">
              <div class="plan-title">{{ getPlanTitle(plan) }}</div>
              <div class="plan-time">计划时间：{{ formatDateTime(plan.nextReviewTime) }}</div>
            </div>
            <div class="plan-actions">
              <el-button
                v-if="Number(plan.status) !== 1"
                type="primary"
                size="small"
                @click="markReviewed(plan)"
              >
                标记完成
              </el-button>
              <el-tag v-else type="success">已完成</el-tag>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无复习计划" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Calendar, CircleCheck, Clock, Delete, Document } from '@element-plus/icons-vue'
import { getProblemDetail } from '@/api/problem'
import {
  deleteWrongBookItem,
  getDifficultyDistribution,
  getKnowledgeDistribution,
  getReviewPlan,
  getWrongBookDetail,
  getWrongBookList,
  getWrongBookStatistics,
  reviewWrongBook,
  updateReviewPlan
} from '@/api/wrongBook'

const router = useRouter()

const loading = ref(false)
const detailLoading = ref(false)
const wrongList = ref([])
const statistics = ref({})
const knowledgeDistribution = ref([])
const difficultyDistributionRaw = ref([])
const reviewPlan = ref([])
const knowledgePoints = ref([])
const sortBy = ref('latest')

const filters = reactive({
  knowledgePoint: null,
  difficulty: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const detailVisible = ref(false)
const planVisible = ref(false)
const currentWrong = ref(null)

const difficultyDistribution = computed(() => {
  const result = { easy: 0, medium: 0, hard: 0 }
  for (const item of difficultyDistributionRaw.value) {
    const difficulty = Number(item.difficulty)
    const count = Number(item.count || 0)
    if (difficulty === 0) result.easy = count
    if (difficulty === 1) result.medium = count
    if (difficulty === 2) result.hard = count
  }
  return result
})

const totalCount = computed(() => Number(statistics.value.totalCount ?? wrongList.value.length ?? 0))
const pendingCount = computed(() => Number(statistics.value.pendingCount ?? wrongList.value.filter((item) => Number(item.reviewStatus) === 0).length))
const reviewedCount = computed(() => Number(statistics.value.reviewedCount ?? wrongList.value.filter((item) => Number(item.reviewStatus) >= 1).length))

const maxKnowledgeCount = computed(() => {
  if (knowledgeDistribution.value.length === 0) return 1
  return Math.max(...knowledgeDistribution.value.map((item) => Number(item.count || 0)), 1)
})

const hasActiveFilters = computed(() => {
  return Boolean(filters.knowledgePoint || filters.difficulty !== null || sortBy.value !== 'latest')
})

const sortedWrongList = computed(() => {
  const list = [...wrongList.value]

  if (sortBy.value === 'oldest') {
    list.sort((a, b) => new Date(a.createTime || 0).getTime() - new Date(b.createTime || 0).getTime())
  } else if (sortBy.value === 'status') {
    list.sort((a, b) => {
      const statusDiff = Number(a.reviewStatus ?? 0) - Number(b.reviewStatus ?? 0)
      if (statusDiff !== 0) return statusDiff
      return new Date(b.createTime || 0).getTime() - new Date(a.createTime || 0).getTime()
    })
  } else {
    list.sort((a, b) => new Date(b.createTime || 0).getTime() - new Date(a.createTime || 0).getTime())
  }

  return list
})

const pagedWrongList = computed(() => {
  const start = (pagination.page - 1) * pagination.size
  const end = start + pagination.size
  return sortedWrongList.value.slice(start, end)
})

const wrongItemMap = computed(() =>
  Object.fromEntries(wrongList.value.map((item) => [item.id, item]))
)

const normalizeWrongItem = (item = {}) => ({
  ...item,
  problemTitle: item.problem?.title || item.problemTitle || `题目 #${item.problemId}`,
  errorMessage: item.errorMessage || '未知错误',
  reviewStatus: Number(item.reviewStatus ?? 0),
  createTime: item.createTime || item.updateTime || null,
  knowledgePoints: item.knowledgePoints || '',
  language: (item.language || '').toUpperCase()
})

const getStatusText = (status) => {
  const mapping = {
    0: '待复习',
    1: '已复习',
    2: '掌握中'
  }
  return mapping[Number(status)] || '未知'
}

const getStatusClass = (status) => {
  const mapping = {
    0: 'pending',
    1: 'reviewed',
    2: 'mastering'
  }
  return mapping[Number(status)] || 'pending'
}

const formatTime = (time) => {
  if (!time) return '未知'
  const date = new Date(time)
  if (Number.isNaN(date.getTime())) return '未知'
  const diff = Date.now() - date.getTime()

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  return `${Math.floor(diff / 86400000)} 天前`
}

const formatDateTime = (time) => {
  if (!time) return '未设置'
  const date = new Date(time)
  if (Number.isNaN(date.getTime())) return '未设置'
  return date.toLocaleString('zh-CN')
}

const getDifficultyWidth = (difficulty) => {
  const total =
    difficultyDistribution.value.easy +
    difficultyDistribution.value.medium +
    difficultyDistribution.value.hard
  if (total === 0) return 0

  if (difficulty === 0) return (difficultyDistribution.value.easy / total) * 100
  if (difficulty === 1) return (difficultyDistribution.value.medium / total) * 100
  return (difficultyDistribution.value.hard / total) * 100
}

const getPlanTitle = (plan) => {
  const wrongItem = wrongItemMap.value[plan.wrongItemId]
  return wrongItem?.problemTitle || `错题 #${plan.wrongItemId}`
}

const updatePagination = () => {
  pagination.total = wrongList.value.length
  if ((pagination.page - 1) * pagination.size >= pagination.total) {
    pagination.page = 1
  }
}

const fetchWrongBookList = async () => {
  loading.value = true
  try {
    const params = {}
    if (filters.knowledgePoint) {
      params.knowledgePoint = filters.knowledgePoint
    }
    if (filters.difficulty !== null && filters.difficulty !== undefined) {
      params.difficulty = filters.difficulty
    }

    const res = await getWrongBookList(params)
    if (res?.code === 200) {
      wrongList.value = Array.isArray(res.data) ? res.data.map(normalizeWrongItem) : []
      updatePagination()
      return
    }

    wrongList.value = []
    updatePagination()
  } catch (error) {
    console.error('获取错题列表失败:', error)
    wrongList.value = []
    updatePagination()
  } finally {
    loading.value = false
  }
}

const fetchStatistics = async () => {
  try {
    const res = await getWrongBookStatistics()
    if (res?.code === 200) {
      statistics.value = res.data || {}
    }
  } catch (error) {
    console.error('获取错题统计失败:', error)
  }
}

const fetchKnowledgeDistribution = async () => {
  try {
    const res = await getKnowledgeDistribution()
    if (res?.code === 200) {
      knowledgeDistribution.value = Array.isArray(res.data) ? res.data : []
      knowledgePoints.value = knowledgeDistribution.value
        .map((item) => item.knowledgePoint)
        .filter(Boolean)
      return
    }

    knowledgeDistribution.value = []
    knowledgePoints.value = []
  } catch (error) {
    console.error('获取知识点分布失败:', error)
    knowledgeDistribution.value = []
    knowledgePoints.value = []
  }
}

const fetchDifficultyDistribution = async () => {
  try {
    const res = await getDifficultyDistribution()
    if (res?.code === 200) {
      difficultyDistributionRaw.value = Array.isArray(res.data) ? res.data : []
      return
    }
    difficultyDistributionRaw.value = []
  } catch (error) {
    console.error('获取难度分布失败:', error)
    difficultyDistributionRaw.value = []
  }
}

const fetchReviewPlan = async () => {
  try {
    const res = await getReviewPlan()
    if (res?.code === 200) {
      reviewPlan.value = Array.isArray(res.data) ? res.data : []
      return
    }
    reviewPlan.value = []
  } catch (error) {
    console.error('获取复习计划失败:', error)
    reviewPlan.value = []
  }
}

const hydrateProblemDetail = async (item) => {
  if (!item?.problemId) {
    return item
  }

  try {
    const res = await getProblemDetail(item.problemId)
    if (res?.code === 200) {
      return {
        ...item,
        problem: res.data,
        problemTitle: res.data?.title || item.problemTitle
      }
    }
  } catch (error) {
    console.error('获取题目详情失败:', error)
  }

  return item
}

const showWrongDetail = async (item) => {
  detailVisible.value = true
  detailLoading.value = true

  try {
    const res = await getWrongBookDetail(item.id)
    if (res?.code === 200) {
      currentWrong.value = await hydrateProblemDetail(normalizeWrongItem(res.data))
      return
    }
    throw new Error('获取详情失败')
  } catch (error) {
    ElMessage.error(error.message || '获取详情失败')
    currentWrong.value = await hydrateProblemDetail(item)
  } finally {
    detailLoading.value = false
  }
}

const reviewItem = async (item) => {
  if (Number(item.reviewStatus) >= 1) {
    return
  }

  try {
    const res = await reviewWrongBook({
      id: item.id,
      status: 1
    })
    if (res?.code === 200) {
      ElMessage.success('已标记为已复习')
      if (currentWrong.value?.id === item.id) {
        currentWrong.value.reviewStatus = 1
      }
      await fetchAllData()
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const reviewCurrentItem = async () => {
  if (!currentWrong.value) return
  await reviewItem(currentWrong.value)
  detailVisible.value = false
}

const deleteItem = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条错题记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await deleteWrongBookItem(id)
    if (res?.code === 200) {
      ElMessage.success('删除成功')
      if (currentWrong.value?.id === id) {
        detailVisible.value = false
      }
      await fetchAllData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const showReviewPlan = async () => {
  await fetchReviewPlan()
  planVisible.value = true
}

const markReviewed = async (plan) => {
  try {
    const res = await updateReviewPlan({
      id: plan.wrongItemId,
      reviewed: true
    })
    if (res?.code === 200) {
      ElMessage.success('已标记完成')
      await fetchReviewPlan()
      await fetchWrongBookList()
      await fetchStatistics()
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const openProblem = (problemId) => {
  if (!problemId) return
  detailVisible.value = false
  router.push(`/problem/${problemId}`)
}

const handleFilterChange = () => {
  pagination.page = 1
  fetchWrongBookList()
}

const handleSortChange = () => {
  pagination.page = 1
}

const handlePageSizeChange = () => {
  pagination.page = 1
}

const resetFilters = () => {
  filters.knowledgePoint = null
  filters.difficulty = null
  sortBy.value = 'latest'
  pagination.page = 1
  fetchWrongBookList()
}

const fetchAllData = async () => {
  await Promise.all([
    fetchWrongBookList(),
    fetchStatistics(),
    fetchKnowledgeDistribution(),
    fetchDifficultyDistribution()
  ])
}

onMounted(() => {
  fetchAllData()
})
</script>

<style scoped>
.wrong-book-page {
  width: 100%;
  min-height: 100vh;
  background: var(--leetcode-bg-secondary, #f7f8fa);
  padding: 24px;
  box-sizing: border-box;
}

.wrong-book-container {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--leetcode-text, #24292f);
  margin: 0 0 8px;
}

.page-subtitle {
  font-size: 14px;
  color: var(--leetcode-text-secondary, #6b7280);
  margin: 0;
}

.stats-overview {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: var(--leetcode-bg, #fff);
  border-radius: 12px;
  padding: 24px;
  border: 1px solid var(--leetcode-border, #e5e7eb);
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.stat-icon.total {
  background: rgba(0, 102, 255, 0.1);
  color: var(--leetcode-primary, #0066ff);
}

.stat-icon.pending {
  background: rgba(255, 179, 0, 0.1);
  color: var(--leetcode-warning, #ffb300);
}

.stat-icon.reviewed {
  background: rgba(0, 200, 83, 0.1);
  color: var(--leetcode-success, #00c853);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--leetcode-text, #24292f);
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: var(--leetcode-text-secondary, #6b7280);
  font-weight: 500;
}

.content-layout {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
}

.main-content {
  background: var(--leetcode-bg, #fff);
  border-radius: 12px;
  border: 1px solid var(--leetcode-border, #e5e7eb);
  overflow: hidden;
}

.filter-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--leetcode-border, #e5e7eb);
  gap: 12px;
}

.filter-left,
.filter-right {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-select {
  width: 160px;
}

:deep(.filter-select .el-input__wrapper) {
  background: var(--leetcode-bg-secondary, #f7f8fa);
  border-radius: 6px;
}

.wrong-list {
  min-height: 400px;
}

.wrong-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--leetcode-border, #e5e7eb);
  cursor: pointer;
  transition: all 0.2s ease;
}

.wrong-item:hover {
  background: var(--leetcode-bg-secondary, #f7f8fa);
}

.wrong-item:last-child {
  border-bottom: none;
}

.wrong-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  min-width: 0;
}

.wrong-status {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

.wrong-status.pending {
  background: rgba(255, 179, 0, 0.1);
  color: var(--leetcode-warning, #ffb300);
}

.wrong-status.reviewed {
  background: rgba(0, 200, 83, 0.1);
  color: var(--leetcode-success, #00c853);
}

.wrong-status.mastering {
  background: rgba(0, 102, 255, 0.1);
  color: var(--leetcode-primary, #0066ff);
}

.wrong-info {
  flex: 1;
  min-width: 0;
}

.wrong-title {
  font-size: 16px;
  font-weight: 500;
  color: var(--leetcode-text, #24292f);
  margin-bottom: 8px;
}

.wrong-meta {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--leetcode-text-secondary, #6b7280);
}

.wrong-right {
  display: flex;
  gap: 8px;
}

.pagination-section {
  display: flex;
  justify-content: center;
  padding: 20px 24px;
  border-top: 1px solid var(--leetcode-border, #e5e7eb);
}

.side-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.distribution-card {
  background: var(--leetcode-bg, #fff);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid var(--leetcode-border, #e5e7eb);
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--leetcode-text, #24292f);
  margin: 0 0 16px;
}

.distribution-chart,
.difficulty-stats {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.distribution-item,
.difficulty-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.distribution-label {
  width: 96px;
  font-size: 13px;
  color: var(--leetcode-text-secondary, #6b7280);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.distribution-bar,
.difficulty-bar {
  flex: 1;
  height: 8px;
  background: var(--leetcode-bg-secondary, #f7f8fa);
  border-radius: 4px;
  overflow: hidden;
}

.distribution-bar .bar-fill,
.difficulty-item .bar-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s ease;
}

.distribution-bar .bar-fill {
  background: var(--leetcode-primary, #0066ff);
}

.difficulty-item.easy .bar-fill {
  background: var(--leetcode-success, #00c853);
}

.difficulty-item.medium .bar-fill {
  background: var(--leetcode-warning, #ffb300);
}

.difficulty-item.hard .bar-fill {
  background: var(--leetcode-danger, #ee4d2e);
}

.distribution-count,
.difficulty-count {
  width: 30px;
  font-size: 13px;
  font-weight: 600;
  color: var(--leetcode-text, #24292f);
  text-align: right;
}

.difficulty-label {
  width: 40px;
  font-size: 13px;
  color: var(--leetcode-text-secondary, #6b7280);
}

.detail-dialog :deep(.el-dialog__body),
.plan-dialog :deep(.el-dialog__body) {
  max-height: 60vh;
  overflow-y: auto;
}

.detail-dialog :deep(.el-dialog__footer),
.plan-dialog :deep(.el-dialog__footer) {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  flex-wrap: wrap;
}

.wrong-detail {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.detail-section h4 {
  font-size: 16px;
  font-weight: 600;
  color: var(--leetcode-text, #24292f);
  margin: 0 0 12px;
}

.problem-info {
  background: var(--leetcode-bg-secondary, #f7f8fa);
  padding: 16px;
  border-radius: 8px;
}

.problem-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--leetcode-text, #24292f);
  margin-bottom: 12px;
}

.problem-content {
  font-size: 14px;
  color: var(--leetcode-text-secondary, #6b7280);
  line-height: 1.6;
}

.error-analysis {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.analysis-item {
  display: flex;
  gap: 8px;
}

.analysis-item .label {
  font-weight: 500;
  color: var(--leetcode-text, #24292f);
  white-space: nowrap;
}

.analysis-item .value {
  color: var(--leetcode-text-secondary, #6b7280);
}

.code-block {
  background: var(--leetcode-bg-secondary, #f7f8fa);
  padding: 16px;
  border-radius: 8px;
  font-family: 'Fira Code', monospace;
  font-size: 13px;
  line-height: 1.6;
  overflow-x: auto;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.review-plan {
  max-height: 400px;
  overflow-y: auto;
}

.plan-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--leetcode-border, #e5e7eb);
}

.plan-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--leetcode-text, #24292f);
}

.plan-count {
  font-size: 14px;
  color: var(--leetcode-text-secondary, #6b7280);
}

.plan-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.plan-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: var(--leetcode-bg-secondary, #f7f8fa);
  border-radius: 8px;
  border: 1px solid var(--leetcode-border, #e5e7eb);
}

.plan-item.reviewed {
  opacity: 0.6;
}

.plan-info {
  flex: 1;
}

.plan-info .plan-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--leetcode-text, #24292f);
  margin-bottom: 4px;
}

.plan-time {
  font-size: 12px;
  color: var(--leetcode-text-secondary, #6b7280);
}

@media (max-width: 1200px) {
  .content-layout {
    grid-template-columns: 1fr;
  }

  .side-content {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .wrong-book-page {
    padding: 16px;
  }

  .stats-overview {
    grid-template-columns: 1fr;
  }

  .filter-section {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-left,
  .filter-right {
    width: 100%;
  }

  .filter-select {
    flex: 1;
    width: 100%;
  }

  .side-content {
    grid-template-columns: 1fr;
  }

  .wrong-item {
    align-items: flex-start;
    flex-direction: column;
    gap: 12px;
  }

  .analysis-item,
  .plan-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .wrong-right {
    width: 100%;
    justify-content: flex-end;
  }

  .plan-actions {
    width: 100%;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
