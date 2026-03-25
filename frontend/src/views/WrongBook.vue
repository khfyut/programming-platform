<template>
  <div class="wrong-book-page">
    <div class="wrong-book-container">
      <div class="page-header">
        <h1 class="page-title">错题本</h1>
        <p class="page-subtitle">智能错题管理，科学复习计划</p>
      </div>

      <div class="stats-overview">
        <div class="stat-card">
          <div class="stat-icon total">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.totalCount || 0 }}</div>
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
                @change="fetchWrongBookList"
              >
                <el-option
                  v-for="point in knowledgePoints"
                  :key="point"
                  :label="point"
                  :value="point"
                />
              </el-select>
              <el-select
                v-model="filters.difficulty"
                placeholder="难度筛选"
                clearable
                class="filter-select"
                @change="fetchWrongBookList"
              >
                <el-option label="简单" :value="0" />
                <el-option label="中等" :value="1" />
                <el-option label="困难" :value="2" />
              </el-select>
            </div>
            <div class="filter-right">
              <el-button type="primary" @click="showReviewPlan">
                <el-icon><Calendar /></el-icon>
                复习计划
              </el-button>
            </div>
          </div>

          <div class="wrong-list" v-loading="loading">
            <div
              v-for="item in wrongList"
              :key="item.id"
              class="wrong-item"
              @click="showWrongDetail(item)"
            >
              <div class="wrong-left">
                <div class="wrong-status" :class="item.reviewStatus">
                  {{ getStatusText(item.reviewStatus) }}
                </div>
                <div class="wrong-info">
                  <div class="wrong-title">{{ item.problemTitle }}</div>
                  <div class="wrong-meta">
                    <span class="meta-item">
                      <el-icon><Document /></el-icon>
                      {{ item.errorType || '未知错误' }}
                    </span>
                    <span class="meta-item">
                      <el-icon><Clock /></el-icon>
                      {{ formatTime(item.submitTime) }}
                    </span>
                  </div>
                </div>
              </div>
              <div class="wrong-right">
                <el-button
                  type="primary"
                  size="small"
                  @click.stop="reviewItem(item)"
                >
                  复习
                </el-button>
                <el-button
                  type="danger"
                  size="small"
                  text
                  @click.stop="deleteItem(item.id)"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>

            <el-empty v-if="wrongList.length === 0 && !loading" description="暂无错题记录" />
          </div>

          <div class="pagination-section">
            <el-pagination
              v-model:current-page="pagination.page"
              v-model:page-size="pagination.size"
              :total="pagination.total"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              @size-change="fetchWrongBookList"
              @current-change="fetchWrongBookList"
            />
          </div>
        </div>

        <div class="side-content">
          <div class="distribution-card">
            <h3 class="card-title">知识点分布</h3>
            <div class="distribution-chart">
              <div
                v-for="item in knowledgeDistribution"
                :key="item.knowledgePoint"
                class="distribution-item"
              >
                <div class="distribution-label">{{ item.knowledgePoint }}</div>
                <div class="distribution-bar">
                  <div
                    class="bar-fill"
                    :style="{ width: (item.count / maxKnowledgeCount * 100) + '%' }"
                  ></div>
                </div>
                <div class="distribution-count">{{ item.count }}</div>
              </div>
            </div>
          </div>

          <div class="distribution-card">
            <h3 class="card-title">难度分布</h3>
            <div class="difficulty-stats">
              <div class="difficulty-item easy">
                <div class="difficulty-label">简单</div>
                <div class="difficulty-bar">
                  <div
                    class="bar-fill"
                    :style="{ width: getDifficultyWidth(0) + '%' }"
                  ></div>
                </div>
                <div class="difficulty-count">{{ difficultyDistribution.easy || 0 }}</div>
              </div>
              <div class="difficulty-item medium">
                <div class="difficulty-label">中等</div>
                <div class="difficulty-bar">
                  <div
                    class="bar-fill"
                    :style="{ width: getDifficultyWidth(1) + '%' }"
                  ></div>
                </div>
                <div class="difficulty-count">{{ difficultyDistribution.medium || 0 }}</div>
              </div>
              <div class="difficulty-item hard">
                <div class="difficulty-label">困难</div>
                <div class="difficulty-bar">
                  <div
                    class="bar-fill"
                    :style="{ width: getDifficultyWidth(2) + '%' }"
                  ></div>
                </div>
                <div class="difficulty-count">{{ difficultyDistribution.hard || 0 }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog
      v-model="detailVisible"
      title="错题详情"
      width="800px"
      class="detail-dialog"
    >
      <div v-if="currentWrong" class="wrong-detail">
        <div class="detail-section">
          <h4>题目信息</h4>
          <div class="problem-info">
            <div class="problem-title">{{ currentWrong.problem?.title }}</div>
            <div class="problem-content">{{ currentWrong.problem?.content }}</div>
          </div>
        </div>
        <div class="detail-section">
          <h4>错误分析</h4>
          <div class="error-analysis">
            <div class="analysis-item">
              <span class="label">错误类型：</span>
              <span class="value">{{ currentWrong.errorType }}</span>
            </div>
            <div class="analysis-item">
              <span class="label">错误信息：</span>
              <span class="value">{{ currentWrong.errorMessage }}</span>
            </div>
            <div class="analysis-item">
              <span class="label">分析建议：</span>
              <span class="value">{{ currentWrong.analysis }}</span>
            </div>
          </div>
        </div>
        <div class="detail-section">
          <h4>提交代码</h4>
          <pre class="code-block">{{ currentWrong.submission?.code }}</pre>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" @click="reviewCurrentItem">标记已复习</el-button>
        <el-button type="success" @click="getRecommendations">推荐练习</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="planVisible"
      title="复习计划"
      width="600px"
      class="plan-dialog"
    >
      <div class="review-plan">
        <div class="plan-header">
          <span class="plan-title">待复习题目</span>
          <span class="plan-count">{{ reviewPlan.length }} 项</span>
        </div>
        <div class="plan-list">
          <div
            v-for="plan in reviewPlan"
            :key="plan.id"
            class="plan-item"
            :class="{ reviewed: plan.reviewed }"
          >
            <div class="plan-info">
              <div class="plan-title">{{ plan.problemTitle }}</div>
              <div class="plan-time">
                计划时间：{{ formatDateTime(plan.scheduledTime) }}
              </div>
            </div>
            <div class="plan-actions">
              <el-button
                v-if="!plan.reviewed"
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
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Document,
  Clock,
  CircleCheck,
  Calendar,
  Delete
} from '@element-plus/icons-vue'
import {
  getWrongBookList,
  getWrongBookDetail,
  reviewWrongBook,
  getReviewPlan,
  getPendingReviews,
  updateReviewPlan,
  getWrongBookStatistics,
  getKnowledgeDistribution,
  getDifficultyDistribution,
  deleteWrongBookItem
} from '@/api/wrongBook'

const router = useRouter()

const loading = ref(false)
const wrongList = ref([])
const statistics = ref({})
const knowledgeDistribution = ref([])
const difficultyDistribution = ref({})
const reviewPlan = ref([])
const knowledgePoints = ref([])

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

const pendingCount = computed(() => {
  return wrongList.value.filter(item => item.reviewStatus === 'pending').length
})

const reviewedCount = computed(() => {
  return wrongList.value.filter(item => item.reviewStatus === 'reviewed').length
})

const maxKnowledgeCount = computed(() => {
  if (knowledgeDistribution.value.length === 0) return 1
  return Math.max(...knowledgeDistribution.value.map(item => item.count))
})

const getStatusText = (status) => {
  const texts = {
    pending: '待复习',
    reviewed: '已复习',
    mastering: '掌握中'
  }
  return texts[status] || '未知'
}

const formatTime = (time) => {
  if (!time) return '未知'
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return `${Math.floor(diff / 86400000)}天前`
}

const formatDateTime = (time) => {
  if (!time) return '未设置'
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

const getDifficultyWidth = (difficulty) => {
  const total = (difficultyDistribution.value.easy || 0) +
                (difficultyDistribution.value.medium || 0) +
                (difficultyDistribution.value.hard || 0)
  if (total === 0) return 0
  
  const count = difficulty === 0 ? difficultyDistribution.value.easy :
                difficulty === 1 ? difficultyDistribution.value.medium :
                difficultyDistribution.value.hard
  return (count / total) * 100
}

const fetchWrongBookList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size
    }
    
    if (filters.knowledgePoint) {
      params.knowledgePoint = filters.knowledgePoint
    }
    if (filters.difficulty !== null) {
      params.difficulty = filters.difficulty
    }
    
    const res = await getWrongBookList(params)
    if (res.code === 200) {
      wrongList.value = res.data || []
      pagination.total = res.data?.total || 0
    }
  } catch (error) {
    console.error('获取错题列表失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchStatistics = async () => {
  try {
    const res = await getWrongBookStatistics()
    if (res.code === 200) {
      statistics.value = res.data || {}
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

const fetchKnowledgeDistribution = async () => {
  try {
    const res = await getKnowledgeDistribution()
    if (res.code === 200) {
      knowledgeDistribution.value = res.data?.data || []
      knowledgePoints.value = knowledgeDistribution.value.map(item => item.knowledgePoint)
    }
  } catch (error) {
    console.error('获取知识点分布失败:', error)
  }
}

const fetchDifficultyDistribution = async () => {
  try {
    const res = await getDifficultyDistribution()
    if (res.code === 200) {
      difficultyDistribution.value = res.data?.data || {}
    }
  } catch (error) {
    console.error('获取难度分布失败:', error)
  }
}

const fetchReviewPlan = async () => {
  try {
    const res = await getReviewPlan()
    if (res.code === 200) {
      reviewPlan.value = res.data || []
    }
  } catch (error) {
    console.error('获取复习计划失败:', error)
  }
}

const showWrongDetail = async (item) => {
  try {
    const res = await getWrongBookDetail(item.id)
    if (res.code === 200) {
      currentWrong.value = res.data
      detailVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取详情失败')
  }
}

const reviewItem = async (item) => {
  try {
    const res = await reviewWrongBook({
      id: item.id,
      status: 'reviewed'
    })
    if (res.code === 200) {
      ElMessage.success('已标记为已复习')
      fetchWrongBookList()
      fetchStatistics()
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
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchWrongBookList()
      fetchStatistics()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const showReviewPlan = () => {
  fetchReviewPlan()
  planVisible.value = true
}

const markReviewed = async (plan) => {
  try {
    const res = await updateReviewPlan({
      id: plan.id,
      reviewed: true
    })
    if (res.code === 200) {
      ElMessage.success('已标记完成')
      fetchReviewPlan()
      fetchWrongBookList()
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const getRecommendations = () => {
  ElMessage.info('正在为您推荐相关练习题...')
}

onMounted(() => {
  fetchWrongBookList()
  fetchStatistics()
  fetchKnowledgeDistribution()
  fetchDifficultyDistribution()
})
</script>

<style scoped>
.wrong-book-page {
  width: 100%;
  min-height: 100vh;
  background: var(--leetcode-bg-secondary, #F7F8FA);
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
  color: var(--leetcode-text, #24292F);
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 14px;
  color: var(--leetcode-text-secondary, #6B7280);
  margin: 0;
}

.stats-overview {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: var(--leetcode-bg, #FFFFFF);
  border-radius: 12px;
  padding: 24px;
  border: 1px solid var(--leetcode-border, #E5E7EB);
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
  color: var(--leetcode-primary, #0066FF);
}

.stat-icon.pending {
  background: rgba(255, 179, 0, 0.1);
  color: var(--leetcode-warning, #FFB300);
}

.stat-icon.reviewed {
  background: rgba(0, 200, 83, 0.1);
  color: var(--leetcode-success, #00C853);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--leetcode-text, #24292F);
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: var(--leetcode-text-secondary, #6B7280);
  font-weight: 500;
}

.content-layout {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
}

.main-content {
  background: var(--leetcode-bg, #FFFFFF);
  border-radius: 12px;
  border: 1px solid var(--leetcode-border, #E5E7EB);
  overflow: hidden;
}

.filter-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--leetcode-border, #E5E7EB);
}

.filter-left {
  display: flex;
  gap: 12px;
}

.filter-select {
  width: 140px;
}

:deep(.filter-select .el-input__wrapper) {
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border: 1px solid var(--leetcode-border, #E5E7EB);
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
  border-bottom: 1px solid var(--leetcode-border, #E5E7EB);
  cursor: pointer;
  transition: all 0.2s ease;
}

.wrong-item:hover {
  background: var(--leetcode-bg-secondary, #F7F8FA);
}

.wrong-item:last-child {
  border-bottom: none;
}

.wrong-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
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
  color: var(--leetcode-warning, #FFB300);
}

.wrong-status.reviewed {
  background: rgba(0, 200, 83, 0.1);
  color: var(--leetcode-success, #00C853);
}

.wrong-status.mastering {
  background: rgba(0, 102, 255, 0.1);
  color: var(--leetcode-primary, #0066FF);
}

.wrong-info {
  flex: 1;
}

.wrong-title {
  font-size: 16px;
  font-weight: 500;
  color: var(--leetcode-text, #24292F);
  margin-bottom: 8px;
}

.wrong-meta {
  display: flex;
  gap: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--leetcode-text-secondary, #6B7280);
}

.wrong-right {
  display: flex;
  gap: 8px;
}

.pagination-section {
  display: flex;
  justify-content: center;
  padding: 20px 24px;
  border-top: 1px solid var(--leetcode-border, #E5E7EB);
}

.side-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.distribution-card {
  background: var(--leetcode-bg, #FFFFFF);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid var(--leetcode-border, #E5E7EB);
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--leetcode-text, #24292F);
  margin: 0 0 16px 0;
}

.distribution-chart {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.distribution-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.distribution-label {
  width: 80px;
  font-size: 13px;
  color: var(--leetcode-text-secondary, #6B7280);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.distribution-bar {
  flex: 1;
  height: 8px;
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border-radius: 4px;
  overflow: hidden;
}

.distribution-bar .bar-fill {
  height: 100%;
  background: var(--leetcode-primary, #0066FF);
  border-radius: 4px;
  transition: width 0.3s ease;
}

.distribution-count {
  width: 30px;
  font-size: 13px;
  font-weight: 600;
  color: var(--leetcode-text, #24292F);
  text-align: right;
}

.difficulty-stats {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.difficulty-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.difficulty-label {
  width: 40px;
  font-size: 13px;
  color: var(--leetcode-text-secondary, #6B7280);
}

.difficulty-bar {
  flex: 1;
  height: 8px;
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border-radius: 4px;
  overflow: hidden;
}

.difficulty-item.easy .bar-fill {
  background: var(--leetcode-success, #00C853);
}

.difficulty-item.medium .bar-fill {
  background: var(--leetcode-warning, #FFB300);
}

.difficulty-item.hard .bar-fill {
  background: var(--leetcode-danger, #EE4D2E);
}

.difficulty-count {
  width: 30px;
  font-size: 13px;
  font-weight: 600;
  color: var(--leetcode-text, #24292F);
  text-align: right;
}

.detail-dialog :deep(.el-dialog__body) {
  max-height: 60vh;
  overflow-y: auto;
}

.wrong-detail {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.detail-section h4 {
  font-size: 16px;
  font-weight: 600;
  color: var(--leetcode-text, #24292F);
  margin: 0 0 12px 0;
}

.problem-info {
  background: var(--leetcode-bg-secondary, #F7F8FA);
  padding: 16px;
  border-radius: 8px;
}

.problem-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--leetcode-text, #24292F);
  margin-bottom: 12px;
}

.problem-content {
  font-size: 14px;
  color: var(--leetcode-text-secondary, #6B7280);
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
  color: var(--leetcode-text, #24292F);
  white-space: nowrap;
}

.analysis-item .value {
  color: var(--leetcode-text-secondary, #6B7280);
}

.code-block {
  background: var(--leetcode-bg-secondary, #F7F8FA);
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
  border-bottom: 1px solid var(--leetcode-border, #E5E7EB);
}

.plan-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--leetcode-text, #24292F);
}

.plan-count {
  font-size: 14px;
  color: var(--leetcode-text-secondary, #6B7280);
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
  padding: 16px;
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border-radius: 8px;
  border: 1px solid var(--leetcode-border, #E5E7EB);
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
  color: var(--leetcode-text, #24292F);
  margin-bottom: 4px;
}

.plan-time {
  font-size: 12px;
  color: var(--leetcode-text-secondary, #6B7280);
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
    gap: 12px;
  }
  
  .filter-left {
    width: 100%;
  }
  
  .filter-select {
    flex: 1;
  }
  
  .side-content {
    grid-template-columns: 1fr;
  }
}
</style>
