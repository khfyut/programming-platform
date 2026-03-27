<template>
  <div v-loading="overviewLoading" class="overview-page">
    <section class="hero-grid">
      <div class="hero-card profile-card">
        <div class="profile-main">
          <el-avatar :size="88" :src="profile.avatarUrl">
            <el-icon><User /></el-icon>
          </el-avatar>
          <div class="profile-copy">
            <div class="profile-row">
              <h1>{{ profile.username || userStore.userInfo?.username || '用户' }}</h1>
              <el-tag :type="profile.isAdmin ? 'danger' : 'primary'" size="small">
                {{ profile.isAdmin ? '管理员' : '学习者' }}
              </el-tag>
            </div>
            <p class="profile-bio">
              {{ profile.bio || '继续积累题目、路径和提交记录，让这里成为你的学习控制台。' }}
            </p>
            <div v-if="profile.githubUrl || profile.blogUrl" class="profile-links">
              <a v-if="profile.githubUrl" :href="profile.githubUrl" target="_blank" rel="noreferrer">GitHub</a>
              <a v-if="profile.blogUrl" :href="profile.blogUrl" target="_blank" rel="noreferrer">博客</a>
            </div>
          </div>
        </div>
        <div class="hero-actions">
          <el-button type="primary" @click="goToSettings">编辑资料</el-button>
          <el-button plain @click="goToWrongBook">进入错题本</el-button>
        </div>
      </div>

      <div class="hero-card current-path-card">
        <div class="section-kicker">Current Path</div>
        <template v-if="currentPath">
          <h2>{{ currentPath.name }}</h2>
          <p class="path-desc">
            {{ currentPath.description || '继续沿着当前路径完成今天的学习任务。' }}
          </p>
          <div class="path-meta">
            <span>{{ getLanguageLabel(currentPath.language) }}</span>
            <span>{{ getDirectionLabel(currentPath.direction) }}</span>
          </div>
          <div class="path-progress-line">
            <div class="path-progress-head">
              <span>当前进度</span>
              <span>{{ currentPathProgress }}%</span>
            </div>
            <el-progress :percentage="currentPathProgress" :show-text="false" :stroke-width="10" />
          </div>
          <div class="path-actions">
            <el-button type="primary" @click="continueLearning">继续学习</el-button>
            <el-button plain @click="goToLearnCenter">查看全部路径</el-button>
          </div>
        </template>
        <template v-else>
          <h2>还没有正在进行的学习路径</h2>
          <p class="path-desc">
            先从学习中心选择一条路径，之后这里会显示你的当前进度和下一步入口。
          </p>
          <div class="path-actions">
            <el-button type="primary" @click="goToLearnCenter">去选择学习路径</el-button>
          </div>
        </template>
      </div>
    </section>

    <section class="stats-grid">
      <article v-for="item in statCards" :key="item.label" class="stat-card">
        <div class="stat-accent"></div>
        <div class="stat-label">{{ item.label }}</div>
        <div class="stat-value">{{ item.value }}</div>
        <div class="stat-hint">{{ item.hint }}</div>
      </article>
    </section>

    <section class="dashboard-grid">
      <div class="panel-card action-panel">
        <div class="panel-header">
          <div>
            <div class="section-kicker">Quick Actions</div>
            <h3>下一步做什么</h3>
          </div>
        </div>
        <div class="action-list">
          <button class="action-item" @click="continueLearning">
            <div>
              <strong>继续学习</strong>
              <span>{{ currentPath?.name || '进入学习中心继续当前计划' }}</span>
            </div>
            <el-icon><ArrowRight /></el-icon>
          </button>
          <button class="action-item" @click="goToWrongBook">
            <div>
              <strong>查看错题</strong>
              <span>回看高频错误，优先补足薄弱点</span>
            </div>
            <el-icon><ArrowRight /></el-icon>
          </button>
          <button class="action-item" @click="goToSubmissions">
            <div>
              <strong>复盘提交</strong>
              <span>快速查看最近结果和运行表现</span>
            </div>
            <el-icon><ArrowRight /></el-icon>
          </button>
        </div>
      </div>

      <div class="panel-card submissions-panel">
        <div class="panel-header">
          <div>
            <div class="section-kicker">Recent Submissions</div>
            <h3>最近提交</h3>
          </div>
          <router-link :to="{ name: 'ProfileSubmissions' }" class="panel-link">查看全部</router-link>
        </div>

        <div v-if="recentSubmissions.length > 0" class="submission-list">
          <div
            v-for="item in recentSubmissions"
            :key="item.id"
            class="submission-item"
            @click="openProblem(item.problemId)"
          >
            <div class="submission-main">
              <div class="submission-top">
                <span class="submission-title">{{ item.problemTitle }}</span>
                <el-tag size="small" :type="getResultType(item.result)">
                  {{ getResultText(item.result) }}
                </el-tag>
              </div>
              <div class="submission-meta">
                <span>{{ item.language || 'N/A' }}</span>
                <span>{{ item.timeCost || 0 }} ms</span>
                <span>{{ formatRelativeTime(item.submitTime || item.createTime) }}</span>
              </div>
            </div>
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
        <div v-else class="empty-box">暂无提交记录</div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowRight, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getActivePaths, getMyLearnStats } from '@/api/learn'
import { getMySubmissions } from '@/api/submit'
import { getStudyStats, getUserProfile } from '@/api/userProfile'

const router = useRouter()
const userStore = useUserStore()
const overviewLoading = ref(false)

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
  streak: 0
})

const activePaths = ref([])
const recentSubmissions = ref([])

const currentPath = computed(() => activePaths.value[0] || null)
const currentPathProgress = computed(() => {
  const progress = Number(
    currentPath.value?.progress ??
      currentPath.value?.progressRate ??
      currentPath.value?.completionRate ??
      0
  )
  return Math.max(0, Math.min(100, Math.round(progress)))
})

const statCards = computed(() => {
  const rawPassRate = Number(studyStats.value.passRate || 0)
  const passRate = rawPassRate <= 1 ? Math.round(rawPassRate * 100) : Math.round(rawPassRate)
  const streak = Number(studyStats.value.streak) || calculateAcceptedStreak(recentSubmissions.value)

  return [
    {
      label: '已解决题目',
      value: studyStats.value.totalSolved || 0,
      hint: '累计通过的题目数量'
    },
    {
      label: '累计提交',
      value: studyStats.value.totalSubmissions || 0,
      hint: '包含历史全部提交'
    },
    {
      label: '通过率',
      value: `${passRate}%`,
      hint: '按提交记录实时计算'
    },
    {
      label: '连续学习',
      value: `${streak} 天`,
      hint: '优先读取后端统计，缺失时按最近记录推算'
    }
  ]
})

const normalizeProfile = (data = {}) => ({
  username: data.username || userStore.userInfo?.username || '',
  avatarUrl: data.avatarUrl || userStore.userInfo?.avatarUrl || '',
  bio: data.bio || '',
  githubUrl: data.githubUrl || '',
  blogUrl: data.blogUrl || '',
  isAdmin: Boolean(data.isAdmin ?? data.admin ?? userStore.userInfo?.role === 1)
})

const applyLearnStats = (data = {}) => {
  const stats = data?.stats || data || {}
  const rawPassRate = Number(stats.passRate ?? stats.accuracy ?? 0)

  studyStats.value = {
    totalSolved: Number(stats.totalSolved ?? stats.solved ?? studyStats.value.totalSolved ?? 0),
    totalSubmissions: Number(
      stats.totalSubmissions ?? stats.submitted ?? stats.submissionCount ?? studyStats.value.totalSubmissions ?? 0
    ),
    passRate: rawPassRate,
    streak: Number(stats.streak ?? studyStats.value.streak ?? 0)
  }
}

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
      .filter((item) => Number(item.result) === 0 && (item.submitTime || item.createTime))
      .map((item) => formatDay(item.submitTime || item.createTime))
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

const formatRelativeTime = (time) => {
  if (!time) return '刚刚'
  const date = new Date(time)
  if (Number.isNaN(date.getTime())) return '刚刚'

  const diff = Date.now() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes} 分钟前`
  if (hours < 24) return `${hours} 小时前`
  return `${days} 天前`
}

const getLanguageLabel = (language) => {
  const labels = {
    JAVA: 'Java',
    PYTHON: 'Python',
    CPP: 'C++',
    JAVASCRIPT: 'JavaScript',
    java: 'Java',
    python: 'Python',
    通用: '通用'
  }
  return labels[language] || language || '通用'
}

const getDirectionLabel = (direction) => {
  const labels = {
    language: '语言基础',
    algorithm: '算法与数据结构',
    backend: '后端开发',
    frontend: '前端开发',
    database: '数据库',
    system: '系统设计'
  }
  return labels[direction] || direction || '学习路径'
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

const ensureUserReady = async () => {
  if (userStore.userInfo?.id) {
    return userStore.userInfo.id
  }

  if (!userStore.token) {
    return null
  }

  try {
    await userStore.fetchUserInfo()
    return userStore.userInfo?.id || null
  } catch (error) {
    console.error('加载当前用户信息失败:', error)
    return null
  }
}

const fetchData = async () => {
  const currentUserId = await ensureUserReady()
  if (!currentUserId) {
    return
  }

  overviewLoading.value = true

  try {
    profile.value = normalizeProfile(userStore.userInfo || {})

    const [profileRes, statsRes, myLearnStatsRes, pathsRes, submissionsRes] = await Promise.allSettled([
      getUserProfile(currentUserId),
      getStudyStats(currentUserId),
      getMyLearnStats(),
      getActivePaths(currentUserId),
      getMySubmissions({ page: 1, size: 5 })
    ])

    let successCount = 0

    if (profileRes.status === 'fulfilled' && profileRes.value?.code === 200) {
      profile.value = normalizeProfile(profileRes.value.data)
      successCount += 1
    }

    if (statsRes.status === 'fulfilled' && statsRes.value?.code === 200) {
      applyLearnStats(statsRes.value.data)
      successCount += 1
    } else if (myLearnStatsRes.status === 'fulfilled' && myLearnStatsRes.value?.code === 200) {
      applyLearnStats(myLearnStatsRes.value.data)
      successCount += 1
    }

    if (pathsRes.status === 'fulfilled' && pathsRes.value?.code === 200) {
      activePaths.value = Array.isArray(pathsRes.value.data) ? pathsRes.value.data : []
      successCount += 1
    } else {
      activePaths.value = []
    }

    if (submissionsRes.status === 'fulfilled' && submissionsRes.value?.code === 200) {
      recentSubmissions.value = (submissionsRes.value.data?.list || []).map((item) => ({
        ...item,
        problemTitle: item.problemTitle || item.title || item.problemName || `题目 ${item.problemId}`,
        language: (item.language || '').toUpperCase(),
        submitTime: item.submitTime || item.createTime
      }))
      successCount += 1
    } else {
      recentSubmissions.value = []
    }

    if (successCount === 0) {
      ElMessage.error('个人主页总览数据加载失败')
    }
  } catch (error) {
    console.error('获取总览数据失败:', error)
    ElMessage.error('个人主页总览数据加载失败')
  } finally {
    overviewLoading.value = false
  }
}

const continueLearning = () => {
  if (currentPath.value?.id) {
    router.push(`/learn/path/${currentPath.value.id}`)
    return
  }
  router.push('/learn')
}

const goToLearnCenter = () => {
  router.push('/learn')
}

const goToWrongBook = () => {
  router.push('/wrong-book')
}

const goToSettings = () => {
  router.push({ name: 'ProfileSettings' })
}

const goToSubmissions = () => {
  router.push({ name: 'ProfileSubmissions' })
}

const openProblem = (problemId) => {
  if (!problemId) {
    return
  }
  router.push(`/problem/${problemId}`)
}

watch(
  userId,
  (value) => {
    if (value) {
      fetchData()
    }
  },
  { immediate: true }
)

watch(
  () => userStore.token,
  (token) => {
    if (token && !userStore.userInfo?.id) {
      fetchData()
    }
  }
)

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.overview-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.hero-grid,
.dashboard-grid,
.stats-grid {
  display: grid;
  gap: 20px;
}

.hero-grid {
  grid-template-columns: minmax(0, 1.1fr) minmax(320px, 0.9fr);
}

.hero-card,
.panel-card,
.stat-card {
  position: relative;
  overflow: hidden;
  border: 1px solid var(--border-color);
  border-radius: 22px;
  background: var(--bg-card);
  box-shadow: var(--shadow-md);
}

.hero-card,
.panel-card {
  padding: 22px;
}

.profile-card {
  display: flex;
  flex-direction: column;
  gap: 18px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.03), transparent 28%),
    var(--bg-card);
}

.profile-main {
  display: flex;
  gap: 18px;
  align-items: flex-start;
}

.profile-copy {
  min-width: 0;
}

.profile-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.profile-row h1,
.panel-card h3,
.current-path-card h2 {
  margin: 0;
}

.profile-row h1 {
  font-size: 28px;
  line-height: 1.1;
}

.current-path-card {
  border-color: rgba(0, 209, 255, 0.28);
  background:
    radial-gradient(circle at top right, rgba(0, 209, 255, 0.14), transparent 32%),
    linear-gradient(180deg, rgba(9, 25, 46, 0.96), rgba(6, 17, 32, 0.92));
  box-shadow: var(--shadow-neon), var(--shadow-md);
}

.profile-bio,
.path-desc,
.stat-hint,
.submission-meta {
  color: var(--text-secondary);
  line-height: 1.7;
}

.profile-links {
  display: flex;
  gap: 12px;
  margin-top: 12px;
}

.hero-actions,
.path-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.section-kicker {
  margin-bottom: 8px;
  color: var(--brand-accent);
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.path-meta {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin: 14px 0;
}

.path-meta span {
  padding: 5px 10px;
  border: 1px solid var(--border-light);
  border-radius: 999px;
  color: var(--text-secondary);
  font-size: 12px;
  background: rgba(255, 255, 255, 0.03);
}

.path-progress-line {
  margin: 18px 0;
}

.path-progress-head,
.panel-header,
.submission-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.stats-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.stat-card {
  padding: 18px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.02), transparent 50%),
    var(--bg-card);
}

.stat-accent {
  width: 42px;
  height: 4px;
  border-radius: 999px;
  background: var(--gradient-brand);
}

.stat-label {
  margin-top: 12px;
  color: var(--text-tertiary);
  font-size: 13px;
}

.stat-value {
  margin: 10px 0 8px;
  font-family: var(--font-display);
  font-size: 32px;
  color: var(--text-primary);
  text-shadow: 0 0 24px rgba(0, 209, 255, 0.12);
}

.dashboard-grid {
  grid-template-columns: minmax(280px, 0.9fr) minmax(0, 1.1fr);
}

.action-list,
.submission-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 16px;
}

.action-item,
.submission-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  width: 100%;
  padding: 16px;
  border: 1px solid var(--border-light);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.03);
  color: var(--text-primary);
  text-align: left;
  cursor: pointer;
  transition:
    transform var(--transition-fast),
    border-color var(--transition-fast),
    background var(--transition-fast);
}

.action-item:first-child {
  border-color: rgba(0, 209, 255, 0.3);
  background: linear-gradient(135deg, rgba(0, 209, 255, 0.14), rgba(0, 102, 255, 0.08));
}

.action-item:hover,
.submission-item:hover {
  transform: translateY(-1px);
  border-color: var(--border-strong);
  background: rgba(255, 255, 255, 0.05);
}

.action-item strong,
.submission-title {
  display: block;
  margin-bottom: 4px;
}

.action-item span,
.submission-meta span {
  font-size: 13px;
}

.panel-link {
  color: var(--brand-primary);
  font-weight: 700;
}

.panel-link:hover {
  color: var(--brand-primary-hover);
}

.empty-box {
  margin-top: 16px;
  padding: 18px;
  border: 1px dashed var(--border-color);
  border-radius: 16px;
  text-align: center;
  color: var(--text-secondary);
}

@media (max-width: 1200px) {
  .hero-grid,
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .profile-main {
    flex-direction: column;
  }
}

@media (max-width: 560px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
