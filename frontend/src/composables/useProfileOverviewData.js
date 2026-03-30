import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getActivePaths, getMyLearnStats } from '@/api/learn'
import { getMySubmissions } from '@/api/submit'
import { getStudyStats, getUserProfile } from '@/api/userProfile'

const DEFAULT_PROFILE = {
  username: '',
  avatarUrl: '',
  bio: '',
  githubUrl: '',
  blogUrl: '',
  isAdmin: false
}

const DEFAULT_STATS = {
  totalSolved: 0,
  totalSubmissions: 0,
  passRate: 0,
  streak: 0
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

const normalizeProfile = (data = {}, fallbackUser = {}) => ({
  username: data.username || fallbackUser.username || '',
  avatarUrl: data.avatarUrl || fallbackUser.avatarUrl || '',
  bio: data.bio || '',
  githubUrl: data.githubUrl || '',
  blogUrl: data.blogUrl || '',
  isAdmin: Boolean(data.isAdmin ?? data.admin ?? fallbackUser.role === 1)
})

const normalizeStats = (data = {}, currentStats = DEFAULT_STATS) => {
  const stats = data?.stats || data || {}
  return {
    totalSolved: Number(stats.totalSolved ?? stats.solved ?? currentStats.totalSolved ?? 0),
    totalSubmissions: Number(
      stats.totalSubmissions ?? stats.submitted ?? stats.submissionCount ?? currentStats.totalSubmissions ?? 0
    ),
    passRate: Number(stats.passRate ?? stats.accuracy ?? currentStats.passRate ?? 0),
    streak: Number(stats.streak ?? currentStats.streak ?? 0)
  }
}

export const formatRelativeTime = (time) => {
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

export const getLearningPathLanguageLabel = (language) => {
  const labels = {
    JAVA: 'Java',
    PYTHON: 'Python',
    CPP: 'C++',
    JAVASCRIPT: 'JavaScript',
    java: 'Java',
    python: 'Python',
    all: '通用'
  }
  return labels[language] || language || '通用'
}

export const getLearningPathDirectionLabel = (direction) => {
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

export const getSubmissionResultText = (result) => {
  const texts = {
    0: '通过',
    1: '答案错误',
    2: '运行错误',
    3: '超时',
    4: '内存超限'
  }
  return texts[result] || '未知'
}

export const getSubmissionResultType = (result) => {
  const types = {
    0: 'success',
    1: 'danger',
    2: 'danger',
    3: 'warning',
    4: 'warning'
  }
  return types[result] || 'info'
}

export const useProfileOverviewData = () => {
  const userStore = useUserStore()
  const overviewLoading = ref(false)
  const profile = ref({ ...DEFAULT_PROFILE })
  const studyStats = ref({ ...DEFAULT_STATS })
  const activePaths = ref([])
  const recentSubmissions = ref([])

  const userId = computed(() => userStore.userInfo?.id)
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
        hint: '根据提交记录实时计算'
      },
      {
        label: '连续学习',
        value: `${streak} 天`,
        hint: '优先使用后端统计，缺失时按最近记录推算'
      }
    ]
  })

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
      profile.value = normalizeProfile(userStore.userInfo || {}, userStore.userInfo || {})

      const [profileRes, statsRes, myLearnStatsRes, pathsRes, submissionsRes] = await Promise.allSettled([
        getUserProfile(currentUserId),
        getStudyStats(currentUserId),
        getMyLearnStats(),
        getActivePaths(currentUserId),
        getMySubmissions({ page: 1, size: 5 })
      ])

      let successCount = 0

      if (profileRes.status === 'fulfilled' && profileRes.value?.code === 200) {
        profile.value = normalizeProfile(profileRes.value.data, userStore.userInfo || {})
        successCount += 1
      }

      if (statsRes.status === 'fulfilled' && statsRes.value?.code === 200) {
        studyStats.value = normalizeStats(statsRes.value.data, studyStats.value)
        successCount += 1
      } else if (myLearnStatsRes.status === 'fulfilled' && myLearnStatsRes.value?.code === 200) {
        studyStats.value = normalizeStats(myLearnStatsRes.value.data, studyStats.value)
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
        ElMessage.error('个人总览数据加载失败')
      }
    } catch (error) {
      console.error('获取总览数据失败:', error)
      ElMessage.error('个人总览数据加载失败')
    } finally {
      overviewLoading.value = false
    }
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

  return {
    currentPath,
    currentPathProgress,
    formatRelativeTime,
    getDirectionLabel: getLearningPathDirectionLabel,
    getLanguageLabel: getLearningPathLanguageLabel,
    getResultText: getSubmissionResultText,
    getResultType: getSubmissionResultType,
    overviewLoading,
    profile,
    recentSubmissions,
    refreshOverview: fetchData,
    statCards
  }
}
