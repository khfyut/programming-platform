<template>
  <div class="submissions-page">
    <section class="panel-card">
      <div class="panel-header">
        <div>
          <div class="section-kicker">Submission Timeline</div>
          <h2>提交记录</h2>
        </div>
        <el-button plain @click="goToWrongBook">去错题本</el-button>
      </div>

      <div class="toolbar">
        <el-select v-model="resultFilter" placeholder="结果筛选" clearable class="toolbar-select">
          <el-option label="通过" :value="0" />
          <el-option label="答案错误" :value="1" />
          <el-option label="运行错误" :value="2" />
          <el-option label="超时" :value="3" />
          <el-option label="内存超限" :value="4" />
        </el-select>
        <el-button @click="fetchSubmissions">刷新</el-button>
      </div>

      <div class="summary-strip" v-if="submissions.length > 0">
        <div class="summary-pill">
          <span>当前页提交</span>
          <strong>{{ submissions.length }}</strong>
        </div>
        <div class="summary-pill success">
          <span>通过</span>
          <strong>{{ acceptedCount }}</strong>
        </div>
        <div class="summary-pill warning">
          <span>待复盘</span>
          <strong>{{ retryCount }}</strong>
        </div>
      </div>

      <div v-if="filteredSubmissions.length > 0" class="submission-list">
        <div v-for="item in filteredSubmissions" :key="item.id" class="submission-item">
          <div class="submission-main">
            <div class="submission-top">
              <button class="problem-link" @click="openProblem(item.problemId)">{{ item.problemTitle }}</button>
              <el-tag size="small" :type="getResultType(item.result)">{{ getResultText(item.result) }}</el-tag>
            </div>
            <div class="submission-meta">
              <span>{{ (item.language || '').toUpperCase() || 'N/A' }}</span>
              <span>{{ item.timeCost || 0 }} ms</span>
              <span>{{ item.memoryCost || 0 }} KB</span>
              <span>{{ formatRelativeTime(item.submitTime) }}</span>
            </div>
          </div>
          <div class="submission-actions">
            <el-button link type="primary" @click="viewDetail(item.id)">查看详情</el-button>
          </div>
        </div>
      </div>
      <div v-else class="empty-box">当前筛选条件下暂无提交记录</div>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchSubmissions"
          @current-change="fetchSubmissions"
        />
      </div>
    </section>

    <el-dialog v-model="detailVisible" title="提交详情" width="760px" @closed="clearRequestedDetail">
      <div v-loading="detailLoading">
        <template v-if="currentDetail">
          <div class="detail-grid">
            <div class="detail-item">
              <span class="detail-label">题目</span>
              <span>{{ currentDetail.problemTitle }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">结果</span>
              <span>{{ getResultText(currentDetail.result) }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">语言</span>
              <span>{{ (currentDetail.language || '').toUpperCase() }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">提交时间</span>
              <span>{{ formatDateTime(currentDetail.submitTime) }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">执行时间</span>
              <span>{{ currentDetail.timeCost || 0 }} ms</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">内存</span>
              <span>{{ currentDetail.memoryCost || 0 }} KB</span>
            </div>
          </div>
          <div class="detail-block">
            <div class="detail-title">代码</div>
            <pre class="code-block">{{ currentDetail.code }}</pre>
          </div>
          <div v-if="currentDetail.error" class="detail-block">
            <div class="detail-title">错误信息</div>
            <pre class="code-block error">{{ currentDetail.error }}</pre>
          </div>
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMySubmissions, getSubmitDetail } from '@/api/submit'

const route = useRoute()
const router = useRouter()

const resultFilter = ref(null)
const submissions = ref([])
const detailVisible = ref(false)
const detailLoading = ref(false)
const currentDetail = ref(null)
const autoOpenedSubmitId = ref(null)

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const filteredSubmissions = computed(() => {
  if (resultFilter.value === null || resultFilter.value === undefined) {
    return submissions.value
  }
  return submissions.value.filter((item) => Number(item.result) === Number(resultFilter.value))
})

const acceptedCount = computed(() =>
  submissions.value.filter((item) => Number(item.result) === 0).length
)

const retryCount = computed(() =>
  submissions.value.filter((item) => Number(item.result) !== 0).length
)

const formatRelativeTime = (time) => {
  if (!time) return '未知'
  const date = new Date(time)
  if (Number.isNaN(date.getTime())) return '未知'

  const diff = Date.now() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes} 分钟前`
  if (hours < 24) return `${hours} 小时前`
  return `${days} 天前`
}

const formatDateTime = (time) => {
  if (!time) return '未知'
  const date = new Date(time)
  if (Number.isNaN(date.getTime())) return '未知'
  return date.toLocaleString('zh-CN')
}

const getResultText = (result) => {
  const texts = {
    0: '通过',
    1: '答案错误',
    2: '运行错误',
    3: '超时',
    4: '内存超限'
  }
  return texts[result] || '未知'
}

const getResultType = (result) => {
  const types = {
    0: 'success',
    1: 'danger',
    2: 'danger',
    3: 'warning',
    4: 'warning'
  }
  return types[result] || 'info'
}

const fetchSubmissions = async () => {
  try {
    const res = await getMySubmissions({
      page: pagination.page,
      size: pagination.size
    })

    if (res?.code === 200) {
      submissions.value = (res.data?.list || []).map((item) => ({
        ...item,
        problemTitle: item.problemTitle || item.title || item.problemName || `题目 ${item.problemId}`,
        submitTime: item.submitTime || item.createTime || item.createdAt
      }))
      pagination.total = res.data?.total || 0
      tryOpenRequestedDetail()
      return
    }

    submissions.value = []
    pagination.total = 0
  } catch (error) {
    console.error('获取提交记录失败:', error)
    ElMessage.error('提交记录加载失败')
    submissions.value = []
    pagination.total = 0
  }
}

const tryOpenRequestedDetail = async () => {
  const requestedId = route.query.submitId

  if (!requestedId) {
    autoOpenedSubmitId.value = null
    return
  }

  if (String(autoOpenedSubmitId.value) === String(requestedId)) {
    return
  }

  autoOpenedSubmitId.value = requestedId
  await viewDetail(requestedId)
}

const viewDetail = async (submitId) => {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await getSubmitDetail(submitId)
    if (res?.code === 200) {
      currentDetail.value = {
        ...res.data,
        problemTitle: res.data?.problemTitle || res.data?.title || res.data?.problemName || `提交 ${submitId}`,
        submitTime: res.data?.submitTime || res.data?.createTime || res.data?.createdAt
      }
      return
    }
    throw new Error(res?.msg || '详情加载失败')
  } catch (error) {
    ElMessage.error(error.message || '详情加载失败')
    currentDetail.value = null
  } finally {
    detailLoading.value = false
  }
}

const openProblem = (problemId) => {
  if (!problemId) return
  router.push(`/problem/${problemId}`)
}

const goToWrongBook = () => {
  router.push('/wrong-book')
}

const clearRequestedDetail = () => {
  if (!route.query.submitId) {
    return
  }

  autoOpenedSubmitId.value = null
  const query = { ...route.query }
  delete query.submitId
  router.replace({ query })
}

watch(() => [pagination.page, pagination.size], fetchSubmissions, { immediate: true })
watch(() => route.query.submitId, tryOpenRequestedDetail, { immediate: true })
</script>

<style scoped>
.submissions-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.panel-card {
  border: 1px solid var(--border-color);
  border-radius: 22px;
  background: var(--bg-card);
  box-shadow: var(--shadow-md);
  padding: 22px;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.section-kicker {
  margin-bottom: 8px;
  color: var(--brand-accent);
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.panel-header h2 {
  margin: 0;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin: 18px 0;
  flex-wrap: wrap;
}

.toolbar-select {
  width: 180px;
}

.summary-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 18px;
}

.summary-pill {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-height: 42px;
  padding: 0 14px;
  border: 1px solid var(--border-light);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.03);
  color: var(--text-secondary);
}

.summary-pill strong {
  color: var(--text-primary);
}

.summary-pill.success {
  border-color: rgba(34, 197, 94, 0.26);
  background: rgba(34, 197, 94, 0.12);
}

.summary-pill.warning {
  border-color: rgba(245, 158, 11, 0.26);
  background: rgba(245, 158, 11, 0.12);
}

.submission-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.submission-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px;
  border: 1px solid var(--border-light);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.03);
  transition: transform var(--transition-fast), border-color var(--transition-fast), background var(--transition-fast);
}

.submission-item:hover {
  transform: translateY(-1px);
  border-color: var(--border-strong);
  background: rgba(255, 255, 255, 0.05);
}

.submission-main {
  min-width: 0;
  flex: 1;
}

.submission-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.problem-link {
  padding: 0;
  border: none;
  background: transparent;
  color: var(--text-primary);
  font-weight: 700;
  cursor: pointer;
}

.problem-link:hover {
  color: var(--brand-primary);
}

.submission-meta {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 8px;
  color: var(--text-secondary);
  font-size: 13px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.empty-box {
  padding: 20px;
  border: 1px dashed var(--border-color);
  border-radius: 16px;
  text-align: center;
  color: var(--text-secondary);
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 20px;
  margin-bottom: 18px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 12px 0;
}

.detail-label,
.detail-title {
  color: var(--text-tertiary);
}

.detail-block + .detail-block {
  margin-top: 16px;
}

.code-block {
  margin: 10px 0 0;
  padding: 14px;
  border: 1px solid var(--border-light);
  border-radius: 14px;
  background: rgba(0, 0, 0, 0.18);
  white-space: pre-wrap;
  word-break: break-word;
}

.code-block.error {
  color: #fecaca;
}

@media (max-width: 720px) {
  .submission-item {
    align-items: flex-start;
    flex-direction: column;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
