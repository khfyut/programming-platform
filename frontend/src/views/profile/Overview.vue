<template>
  <div class="overview-page">
    <div class="profile-card">
      <div class="profile-main">
        <div class="avatar-section">
          <el-avatar :size="80" :src="profile.avatarUrl">
            <el-icon :size="40"><User /></el-icon>
          </el-avatar>
        </div>

        <div class="profile-info">
          <div class="profile-header">
            <h2 class="username">{{ profile.username || '用户' }}</h2>
            <el-tag :type="profile.isAdmin ? 'danger' : 'primary'" size="small">
              {{ profile.isAdmin ? '管理员' : '学习者' }}
            </el-tag>
          </div>

          <p class="bio">{{ profile.bio || '还没有填写个人简介。' }}</p>

          <div class="social-links" v-if="profile.githubUrl || profile.blogUrl">
            <a v-if="profile.githubUrl" :href="profile.githubUrl" target="_blank" rel="noreferrer" class="social-link">
              <el-icon><Link /></el-icon>
              <span>GitHub</span>
            </a>
            <a v-if="profile.blogUrl" :href="profile.blogUrl" target="_blank" rel="noreferrer" class="social-link">
              <el-icon><Link /></el-icon>
              <span>博客</span>
            </a>
          </div>
        </div>

        <div class="profile-actions">
          <el-button type="primary" @click="showEditDialog">
            <el-icon><Edit /></el-icon>
            编辑资料
          </el-button>
        </div>
      </div>
    </div>

    <div class="stats-grid">
      <div v-for="stat in statsList" :key="stat.key" class="stat-card">
        <div class="stat-value">{{ stat.value }}</div>
        <div class="stat-label">{{ stat.label }}</div>
        <div class="stat-trend" :class="stat.trend > 0 ? 'up' : stat.trend < 0 ? 'down' : ''">
          <template v-if="stat.trend !== null">
            <el-icon v-if="stat.trend !== 0"><TrendCharts /></el-icon>
            <span>{{ formatTrend(stat.trend, stat.unit) }}</span>
          </template>
          <span v-else>根据提交记录实时计算</span>
        </div>
      </div>
    </div>

    <div class="quick-actions">
      <h3 class="section-title">快捷操作</h3>
      <div class="actions-grid">
        <div class="action-card" @click="continueLearning">
          <div class="action-icon primary">
            <el-icon><VideoPlay /></el-icon>
          </div>
          <div class="action-content">
            <h4>继续学习</h4>
            <p>{{ activePaths[0]?.name || '进入学习中心继续进度' }}</p>
          </div>
          <el-icon class="action-arrow"><ArrowRight /></el-icon>
        </div>

        <div class="action-card" @click="goToDailyChallenge">
          <div class="action-icon success">
            <el-icon><Calendar /></el-icon>
          </div>
          <div class="action-content">
            <h4>每日挑战</h4>
            <p>继续完成今天的练习</p>
          </div>
          <el-icon class="action-arrow"><ArrowRight /></el-icon>
        </div>

        <div class="action-card" @click="goToRecommendations">
          <div class="action-icon warning">
            <el-icon><Star /></el-icon>
          </div>
          <div class="action-content">
            <h4>推荐题目</h4>
            <p>查看与你当前进度相关的题目</p>
          </div>
          <el-icon class="action-arrow"><ArrowRight /></el-icon>
        </div>
      </div>
    </div>

    <div class="recent-activities">
      <h3 class="section-title">最近活动</h3>
      <div class="activities-list">
        <div
          v-for="(activity, index) in recentActivities"
          :key="`${activity.type}-${activity.targetId}-${activity.createTime || index}`"
          class="activity-item"
        >
          <div class="activity-icon" :class="activity.type">
            <el-icon v-if="activity.type === 'SOLVE_PROBLEM'"><CircleCheck /></el-icon>
            <el-icon v-else-if="activity.type === 'CREATE_POST'"><ChatDotRound /></el-icon>
            <el-icon v-else><VideoPlay /></el-icon>
          </div>
          <div class="activity-content">
            <p class="activity-text">{{ formatActivityText(activity) }}</p>
            <span class="activity-time">{{ formatTime(activity.createTime) }}</span>
          </div>
        </div>

        <el-empty v-if="recentActivities.length === 0" description="暂无活动记录" />
      </div>
    </div>

    <el-dialog v-model="editDialogVisible" title="编辑资料" width="500px" destroy-on-close>
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="头像链接">
          <el-input v-model="editForm.avatarUrl" placeholder="https://example.com/avatar.png" />
        </el-form-item>
        <el-form-item label="个人简介">
          <el-input
            v-model="editForm.bio"
            type="textarea"
            :rows="3"
            maxlength="100"
            show-word-limit
            placeholder="介绍一下自己吧"
          />
        </el-form-item>
        <el-form-item label="GitHub">
          <el-input v-model="editForm.githubUrl" placeholder="https://github.com/username" />
        </el-form-item>
        <el-form-item label="博客链接">
          <el-input v-model="editForm.blogUrl" placeholder="https://yourblog.com" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import {
  User,
  Edit,
  Link,
  TrendCharts,
  VideoPlay,
  ArrowRight,
  Calendar,
  Star,
  CircleCheck,
  ChatDotRound
} from '@element-plus/icons-vue'
import { getProblemDetail } from '@/api/problem'
import { getActivePaths } from '@/api/learn'
import { getMySubmissions } from '@/api/submit'
import { getStudyActivities, getStudyStats, getUserProfile, updateProfile } from '@/api/userProfile'
import { formatDistanceToNow } from '@/utils/format'

const router = useRouter()
const userStore = useUserStore()

const userId = computed(() => userStore.userInfo?.id)

const profile = ref({
  username: '',
  avatarUrl: '',
  bio: '',
  githubUrl: '',
  blogUrl: '',
  isAdmin: false
})

const studyStats = ref({
  totalSolved: 0,
  totalSubmissions: 0,
  passRate: 0,
  studyHours: 0,
  solvedTrend: 0,
  submittedTrend: 0,
  accuracyTrend: 0,
  studyHoursTrend: 0
})

const submissionList = ref([])
const activePaths = ref([])
const recentActivities = ref([])

const editDialogVisible = ref(false)
const saving = ref(false)
const editForm = ref({
  username: '',
  avatarUrl: '',
  bio: '',
  githubUrl: '',
  blogUrl: ''
})

const normalizeProfile = (data = {}) => ({
  username: data.username || '',
  avatarUrl: data.avatarUrl || '',
  bio: data.bio || '',
  githubUrl: data.githubUrl || '',
  blogUrl: data.blogUrl || '',
  isAdmin: Boolean(data.isAdmin ?? data.admin)
})

const formatDay = (date) => {
  const target = new Date(date)
  const year = target.getFullYear()
  const month = String(target.getMonth() + 1).padStart(2, '0')
  const day = String(target.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const calculateAcceptedStreak = (submissions) => {
  const acceptedDays = new Set(
    (submissions || [])
      .filter((item) => Number(item.result) === 0 && item.createTime)
      .map((item) => formatDay(item.createTime))
  )

  let streak = 0
  const current = new Date()
  current.setHours(0, 0, 0, 0)

  while (acceptedDays.has(formatDay(current))) {
    streak += 1
    current.setDate(current.getDate() - 1)
  }

  return streak
}

const statsList = computed(() => {
  const passRate = Math.round((studyStats.value.passRate || 0) * 100)

  return [
    {
      key: 'solved',
      value: studyStats.value.totalSolved || 0,
      label: '已解决题目',
      trend: studyStats.value.solvedTrend || 0,
      unit: ''
    },
    {
      key: 'accuracy',
      value: `${passRate}%`,
      label: '通过率',
      trend: Math.round(studyStats.value.accuracyTrend || 0),
      unit: '%'
    },
    {
      key: 'streak',
      value: calculateAcceptedStreak(submissionList.value),
      label: '连续通过天数',
      trend: null,
      unit: ''
    },
    {
      key: 'studyHours',
      value: `${studyStats.value.studyHours || 0}h`,
      label: '累计学习时长',
      trend: studyStats.value.studyHoursTrend || 0,
      unit: 'h'
    }
  ]
})

const hydrateProblemTitles = async (items, matcher) => {
  const targetItems = items.filter(matcher)
  const missingIds = [...new Set(targetItems.map((item) => item.targetId).filter(Boolean))]

  if (missingIds.length === 0) {
    return items
  }

  const results = await Promise.allSettled(missingIds.map((id) => getProblemDetail(id)))
  const titleMap = {}

  results.forEach((result, index) => {
    if (result.status === 'fulfilled' && result.value?.code === 200) {
      titleMap[missingIds[index]] = result.value.data?.title
    }
  })

  return items.map((item) => {
    if (!matcher(item)) {
      return item
    }

    return {
      ...item,
      targetName: titleMap[item.targetId] || item.targetName || `题目 ${item.targetId}`
    }
  })
}

const fetchData = async () => {
  if (!userId.value) {
    return
  }

  try {
    const [profileRes, statsRes, pathsRes, activitiesRes, submissionsRes] = await Promise.all([
      getUserProfile(userId.value),
      getStudyStats(userId.value),
      getActivePaths(userId.value),
      getStudyActivities(userId.value, 0, 5),
      getMySubmissions({ page: 1, size: 365 })
    ])

    if (profileRes?.code === 200) {
      profile.value = normalizeProfile(profileRes.data)
    }

    if (statsRes?.code === 200) {
      studyStats.value = {
        ...studyStats.value,
        ...statsRes.data
      }
    }

    if (pathsRes?.code === 200) {
      activePaths.value = pathsRes.data || []
    }

    if (submissionsRes?.code === 200) {
      submissionList.value = submissionsRes.data?.list || []
    }

    if (activitiesRes?.code === 200) {
      recentActivities.value = await hydrateProblemTitles(
        activitiesRes.data || [],
        (item) => item.type === 'SOLVE_PROBLEM' && (!item.targetName || /^Problem\b/i.test(item.targetName))
      )
    } else {
      recentActivities.value = []
    }
  } catch (error) {
    console.error('获取个人主页数据失败:', error)
  }
}

const showEditDialog = () => {
  editForm.value = {
    username: profile.value.username,
    avatarUrl: profile.value.avatarUrl,
    bio: profile.value.bio,
    githubUrl: profile.value.githubUrl,
    blogUrl: profile.value.blogUrl
  }
  editDialogVisible.value = true
}

const submitEdit = async () => {
  saving.value = true
  try {
    await updateProfile(editForm.value)
    Object.assign(profile.value, normalizeProfile(editForm.value))
    editDialogVisible.value = false
    ElMessage.success('资料更新成功')
  } catch (error) {
    ElMessage.error(error.message || '资料更新失败')
  } finally {
    saving.value = false
  }
}

const continueLearning = () => {
  if (activePaths.value.length > 0) {
    router.push(`/dashboard/learn/path/${activePaths.value[0].id}`)
    return
  }

  router.push('/dashboard/learn')
}

const goToDailyChallenge = () => {
  router.push('/dashboard/problems?type=daily')
}

const goToRecommendations = () => {
  router.push('/dashboard/problems?type=recommended')
}

const formatTime = (time) => {
  if (!time) {
    return ''
  }

  return formatDistanceToNow(new Date(time))
}

const formatTrend = (value, unit) => {
  if (value === null) {
    return ''
  }

  if (value === 0) {
    return '较上一周期持平'
  }

  return `${value > 0 ? '+' : ''}${value}${unit}`
}

const formatActivityText = (activity) => {
  const targetName = activity.targetName || `内容 ${activity.targetId}`
  const status = Number(activity.status)

  if (activity.type === 'SOLVE_PROBLEM') {
    if (status === 0) {
      return `通过了题目《${targetName}》`
    }

    return `提交了题目《${targetName}》`
  }

  if (activity.type === 'CREATE_POST') {
    return `发布了帖子《${targetName}》`
  }

  return `完成了《${targetName}》相关学习`
}

watch(
  userId,
  (id) => {
    if (id) {
      fetchData()
    }
  },
  { immediate: true }
)
</script>

<style scoped>
.overview-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.profile-card,
.quick-actions,
.recent-activities {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.profile-main {
  display: flex;
  align-items: flex-start;
  gap: 24px;
}

.avatar-section {
  flex-shrink: 0;
}

.profile-info {
  flex: 1;
  min-width: 0;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.username {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.bio {
  margin: 0 0 16px;
  color: #606266;
  line-height: 1.7;
}

.social-links {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.social-link {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #409eff;
  text-decoration: none;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  text-align: center;
}

.stat-value {
  font-size: 30px;
  font-weight: 600;
  color: #303133;
}

.stat-label {
  margin-top: 8px;
  color: #909399;
  font-size: 14px;
}

.stat-trend {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

.stat-trend.up {
  color: #67c23a;
}

.stat-trend.down {
  color: #f56c6c;
}

.section-title {
  margin: 0 0 20px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.action-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: 10px;
  background: #f5f7fa;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-card:hover {
  background: #ecf5ff;
  transform: translateX(4px);
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.action-icon.primary {
  background: #ecf5ff;
  color: #409eff;
}

.action-icon.success {
  background: #f0f9eb;
  color: #67c23a;
}

.action-icon.warning {
  background: #fdf6ec;
  color: #e6a23c;
}

.action-content {
  flex: 1;
  min-width: 0;
}

.action-content h4 {
  margin: 0 0 4px;
  font-size: 16px;
  color: #303133;
}

.action-content p {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.action-arrow {
  color: #c0c4cc;
  font-size: 20px;
}

.activities-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.activity-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  transition: background 0.2s ease;
}

.activity-item:hover {
  background: #f5f7fa;
}

.activity-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.activity-icon.SOLVE_PROBLEM {
  background: #f0f9eb;
  color: #67c23a;
}

.activity-icon.CREATE_POST {
  background: #ecf5ff;
  color: #409eff;
}

.activity-content {
  min-width: 0;
}

.activity-text {
  margin: 0 0 4px;
  color: #303133;
  line-height: 1.6;
}

.activity-time {
  font-size: 12px;
  color: #909399;
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .actions-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .profile-main {
    flex-direction: column;
  }

  .profile-actions {
    width: 100%;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
