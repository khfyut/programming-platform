<template>
  <div class="leetcode-learn">
    <div class="learn-container">
      <div class="page-header">
        <h1 class="page-title">学习记录</h1>
        <p class="page-subtitle">查看您的学习进度和成就</p>
      </div>

      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon solved">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.solved }}</div>
            <div class="stat-label">已解决</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon submitted">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.submitted }}</div>
            <div class="stat-label">提交次数</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon accuracy">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.accuracy }}%</div>
            <div class="stat-label">通过率</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon streak">
            <el-icon><Trophy /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.streak }}</div>
            <div class="stat-label">连续天数</div>
          </div>
        </div>
      </div>

      <div class="content-sections">
        <div class="section-card">
          <div class="section-header">
            <h2 class="section-title">难度分布</h2>
          </div>
          <div class="difficulty-stats">
            <div class="difficulty-item easy">
              <div class="difficulty-label">简单</div>
              <div class="difficulty-bar">
                <div class="bar-fill" :style="{ width: getBarWidth(difficultyStats.easy) + '%' }"></div>
              </div>
              <div class="difficulty-count">{{ difficultyStats.easy }}</div>
            </div>
            <div class="difficulty-item medium">
              <div class="difficulty-label">中等</div>
              <div class="difficulty-bar">
                <div class="bar-fill" :style="{ width: getBarWidth(difficultyStats.medium) + '%' }"></div>
              </div>
              <div class="difficulty-count">{{ difficultyStats.medium }}</div>
            </div>
            <div class="difficulty-item hard">
              <div class="difficulty-label">困难</div>
              <div class="difficulty-bar">
                <div class="bar-fill" :style="{ width: getBarWidth(difficultyStats.hard) + '%' }"></div>
              </div>
              <div class="difficulty-count">{{ difficultyStats.hard }}</div>
            </div>
          </div>
        </div>

        <div class="section-card">
          <div class="section-header">
            <h2 class="section-title">最近提交</h2>
            <router-link to="/submissions" class="view-all-link">
              查看全部
              <el-icon><ArrowRight /></el-icon>
            </router-link>
          </div>
          <div class="recent-submissions">
            <div 
              v-for="submission in recentSubmissions" 
              :key="submission.id"
              class="submission-item"
            >
              <div class="submission-info">
                <div class="submission-title">{{ submission.problemTitle }}</div>
                <div class="submission-meta">
                  <span :class="['result-badge', getResultClass(submission.result)]">
                    {{ getResultText(submission.result) }}
                  </span>
                  <span class="submission-time">{{ formatTime(submission.submitTime) }}</span>
                </div>
              </div>
              <el-icon class="arrow-icon"><ArrowRight /></el-icon>
            </div>
            
            <div v-if="recentSubmissions.length === 0" class="empty-submissions">
              <el-empty description="暂无提交记录" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyLearnStats } from '@/api/learn'
import { getMySubmissions } from '@/api/submit'
import { 
  CircleCheck, 
  Document, 
  TrendCharts, 
  Trophy, 
  ArrowRight 
} from '@element-plus/icons-vue'

const stats = ref({
  solved: 0,
  submitted: 0,
  accuracy: 0,
  streak: 0
})

const difficultyStats = ref({
  easy: 0,
  medium: 0,
  hard: 0
})

const recentSubmissions = ref([])

const getResultClass = (result) => {
  const classes = { 0: 'success', 1: 'error', 2: 'error', 3: 'warning', 4: 'warning' }
  return classes[result] || 'info'
}

const getResultText = (result) => {
  const texts = { 0: '通过', 1: '答案错误', 2: '运行错误', 3: '超时', 4: '内存超限' }
  return texts[result] || '未知'
}

const formatTime = (time) => {
  if (!time) return '未知'
  const date = new Date(time)
  if (isNaN(date.getTime())) return '未知'
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return `${Math.floor(diff / 86400000)}天前`
}

const getBarWidth = (count) => {
  const total = difficultyStats.value.easy + difficultyStats.value.medium + difficultyStats.value.hard
  if (total === 0) return 0
  return Math.min((count / total) * 100, 100)
}

const fetchStats = async () => {
  try {
    const res = await getMyLearnStats()
    if (res.code === 200) {
      stats.value = res.data.stats || res.data
      difficultyStats.value = res.data.difficultyStats || { easy: 0, medium: 0, hard: 0 }
    }
  } catch (error) {
    console.error('获取学习统计失败:', error)
  }
}

const fetchRecentSubmissions = async () => {
  try {
    const res = await getMySubmissions({ page: 1, size: 5 })
    if (res.code === 200) {
      recentSubmissions.value = res.data.list || []
    }
  } catch (error) {
    console.error('获取最近提交失败:', error)
  }
}

onMounted(() => {
  fetchStats()
  fetchRecentSubmissions()
})
</script>

<style scoped>
.leetcode-learn {
  min-height: 100vh;
  background: var(--leetcode-bg-secondary, #F7F8FA);
  padding: 24px;
}

.learn-container {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 32px;
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

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-bottom: 32px;
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

.stat-icon.solved {
  background: rgba(0, 200, 83, 0.1);
  color: var(--leetcode-success, #00C853);
}

.stat-icon.submitted {
  background: rgba(0, 102, 255, 0.1);
  color: var(--leetcode-primary, #0066FF);
}

.stat-icon.accuracy {
  background: rgba(255, 179, 0, 0.1);
  color: var(--leetcode-warning, #FFB300);
}

.stat-icon.streak {
  background: rgba(238, 77, 56, 0.1);
  color: var(--leetcode-danger, #EE4D2E);
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

.content-sections {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.section-card {
  background: var(--leetcode-bg, #FFFFFF);
  border-radius: 12px;
  padding: 24px;
  border: 1px solid var(--leetcode-border, #E5E7EB);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--leetcode-text, #24292F);
  margin: 0;
}

.view-all-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--leetcode-primary, #0066FF);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.view-all-link:hover {
  color: var(--leetcode-primary, #0066FF);
  transform: translateX(4px);
}

.difficulty-stats {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.difficulty-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.difficulty-label {
  width: 60px;
  font-size: 14px;
  font-weight: 500;
  color: var(--leetcode-text, #24292F);
}

.difficulty-bar {
  flex: 1;
  height: 8px;
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border-radius: 4px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.5s ease;
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
  width: 40px;
  text-align: right;
  font-size: 14px;
  font-weight: 600;
  color: var(--leetcode-text, #24292F);
}

.recent-submissions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.submission-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border-radius: 6px;
  border: 1px solid var(--leetcode-border, #E5E7EB);
  transition: all 0.2s ease;
  cursor: pointer;
}

.submission-item:hover {
  background: #FFFFFF;
  border-color: var(--leetcode-primary, #0066FF);
  transform: translateX(4px);
}

.submission-info {
  flex: 1;
}

.submission-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--leetcode-text, #24292F);
  margin-bottom: 6px;
}

.submission-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.result-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.result-badge.success {
  background: rgba(0, 200, 83, 0.1);
  color: var(--leetcode-success, #00C853);
}

.result-badge.error {
  background: rgba(238, 77, 56, 0.1);
  color: var(--leetcode-danger, #EE4D2E);
}

.result-badge.warning {
  background: rgba(255, 179, 0, 0.1);
  color: var(--leetcode-warning, #FFB300);
}

.result-badge.info {
  background: rgba(134, 144, 156, 0.1);
  color: var(--leetcode-text-secondary, #6B7280);
}

.submission-time {
  font-size: 12px;
  color: var(--leetcode-text-secondary, #6B7280);
}

.arrow-icon {
  color: var(--leetcode-text-secondary, #6B7280);
  font-size: 16px;
}

.empty-submissions {
  padding: 40px 20px;
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .content-sections {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .leetcode-learn {
    padding: 16px;
  }

  .page-title {
    font-size: 24px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .stat-card {
    padding: 20px;
  }

  .stat-value {
    font-size: 28px;
  }

  .section-card {
    padding: 20px;
  }
}
</style>
