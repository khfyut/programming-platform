<template>
  <div class="user-profile-page">
    <section class="card profile-card">
      <div class="profile-header">
        <div class="avatar-wrap">
          <el-avatar :size="96" :src="profile.avatarUrl">
            <el-icon><User /></el-icon>
          </el-avatar>
          <el-button v-if="isOwner" class="avatar-btn" size="small" circle @click="changeAvatar">
            <el-icon><Camera /></el-icon>
          </el-button>
        </div>

        <div class="profile-info">
          <div class="profile-title">
            <h1>{{ profile.username || '用户' }}</h1>
            <el-tag v-if="profile.isAdmin" type="danger" size="small">管理员</el-tag>
            <el-tag v-else type="primary" size="small">学习者</el-tag>
          </div>
          <p class="bio">{{ profile.bio || '这个人很低调，还没有留下个人简介。' }}</p>
          <div v-if="profile.githubUrl || profile.blogUrl" class="links">
            <a v-if="profile.githubUrl" :href="profile.githubUrl" target="_blank" rel="noreferrer">GitHub</a>
            <a v-if="profile.blogUrl" :href="profile.blogUrl" target="_blank" rel="noreferrer">博客</a>
          </div>
          <el-button v-if="isOwner" type="primary" @click="showEditDialog">
            <el-icon><Edit /></el-icon>
            编辑资料
          </el-button>
        </div>
      </div>
    </section>

    <section class="stats-grid">
      <article v-for="item in statCards" :key="item.key" class="card stat-card">
        <div class="stat-icon" :class="item.key">
          <el-icon><component :is="item.icon" /></el-icon>
        </div>
        <strong class="stat-value">{{ item.value }}</strong>
        <span class="stat-label">{{ item.label }}</span>
        <span class="stat-trend" :class="Number(item.trend) >= 0 ? 'up' : 'down'">
          <el-icon><TrendCharts /></el-icon>
          {{ formatTrend(item.trend, item.unit) }}
        </span>
      </article>
    </section>

    <section class="content-grid">
      <div class="left-column">
        <section class="card section-card">
          <div class="section-header">
            <h3><el-icon><Guide /></el-icon>我的学习路径</h3>
            <router-link to="/learn">查看全部</router-link>
          </div>
          <div v-if="activePaths.length" class="stack">
            <article v-for="path in activePaths" :key="path.id" class="block">
              <div class="inline-tags">
                <span class="pill pill-primary">{{ getLanguageLabel(path.language) }}</span>
                <span class="pill">{{ getDirectionLabel(path.direction) }}</span>
              </div>
              <h4>{{ path.name }}</h4>
              <p>{{ path.description || '继续沿着当前路径完成阶段练习。' }}</p>
              <div class="meta-row">
                <span>学习进度</span>
                <span>{{ path.progress || 0 }}%</span>
              </div>
              <el-progress :percentage="path.progress || 0" :show-text="false" :stroke-width="8" :color="getProgressColor(path.progress || 0)" />
              <div class="actions">
                <el-button type="primary" size="small" @click="continuePath(path.id)">继续学习</el-button>
              </div>
            </article>
          </div>
          <el-empty v-else description="暂无正在进行的学习路径">
            <el-button type="primary" @click="goToLearn">去学习中心看看</el-button>
          </el-empty>
        </section>

        <section class="card section-card">
          <div class="section-header">
            <h3><el-icon><DataAnalysis /></el-icon>知识点掌握度</h3>
            <el-button size="small" @click="viewKnowledgeGraph">知识图谱</el-button>
          </div>
          <div v-if="knowledgeMastery.length" class="stack">
            <article v-for="mastery in knowledgeMastery" :key="mastery.id || mastery.knowledgeId" class="block">
              <div class="meta-row">
                <strong>{{ mastery.knowledgeName }}</strong>
                <span class="pill" :class="getMasteryLevelClass(mastery.masteryLevel)">{{ getMasteryLevelText(mastery.masteryLevel) }}</span>
              </div>
              <el-progress :percentage="Math.round((mastery.masteryLevel || 0) * 100)" :stroke-width="10" :color="getMasteryColor(mastery.masteryLevel || 0)" />
              <div class="minor-text">已练习 {{ mastery.solvedCount || 0 }} 题，已通过 {{ mastery.passedCount || 0 }} 题</div>
            </article>
          </div>
          <el-empty v-else description="暂无知识点掌握数据" />
          <div v-if="weakPoints.length" class="weak-points">
            <div class="sub-title"><el-icon><Warning /></el-icon>当前薄弱项</div>
            <div class="inline-tags">
              <el-tag v-for="point in weakPoints" :key="point.id" type="warning">{{ point.name }}</el-tag>
            </div>
          </div>
        </section>

        <section class="card section-card">
          <div class="section-header">
            <h3><el-icon><DataLine /></el-icon>难度分布</h3>
          </div>
          <div class="stack">
            <div v-for="item in difficultyRows" :key="item.key" class="difficulty-row" :class="item.key">
              <span class="difficulty-name">{{ item.label }}</span>
              <div class="difficulty-bar"><div class="difficulty-fill" :style="{ width: `${getBarWidth(item.value)}%` }"></div></div>
              <span class="difficulty-count">{{ item.value }}</span>
            </div>
          </div>
        </section>
      </div>

      <div class="right-column">
        <section class="card section-card">
          <div class="section-header">
            <h3><el-icon><MagicStick /></el-icon>为你推荐</h3>
            <el-tag type="success" size="small">智能推荐</el-tag>
          </div>
          <div v-if="recommendedProblems.length" class="stack">
            <article v-for="problem in recommendedProblems.slice(0, 3)" :key="problem.id" class="block clickable" @click="goToProblem(problem.id)">
              <div class="inline-tags">
                <el-tag :type="getDifficultyTagType(problem.difficulty)" size="small">{{ getDifficultyText(problem.difficulty) }}</el-tag>
                <el-tag v-if="problem.isRecommended" type="success" size="small">推荐</el-tag>
              </div>
              <h4>{{ problem.title }}</h4>
              <p>{{ problem.description }}</p>
              <div class="minor-text">浏览 {{ problem.viewCount }} · 通过 {{ problem.passedCount }}</div>
              <div class="actions">
                <el-button type="primary" size="small" @click.stop="startProblem(problem.id)">开始做题</el-button>
              </div>
            </article>
          </div>
          <el-empty v-else description="暂时还没有可展示的推荐题目">
            <el-button type="primary" @click="goToLearn">去学习中心看看</el-button>
          </el-empty>
        </section>

        <section class="card section-card">
          <div class="section-header">
            <h3><el-icon><Clock /></el-icon>最近提交</h3>
            <router-link to="/submissions">查看全部</router-link>
          </div>
          <div v-if="recentSubmissions.length" class="stack">
            <button v-for="submission in recentSubmissions" :key="submission.id" type="button" class="block submission-btn" @click="goToSubmission(submission.id)">
              <div class="meta-row">
                <strong>{{ submission.problemTitle }}</strong>
                <el-tag :type="getResultTagType(submission.result)" size="small">{{ getSubmissionResultText(submission.result) }}</el-tag>
              </div>
              <div class="minor-text">{{ formatTime(submission.submitTime) }}</div>
            </button>
          </div>
          <el-empty v-else description="暂无提交记录" />
        </section>

        <section class="card section-card">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="学习动态" name="activities">
              <div v-if="activities.length" class="stack">
                <article v-for="activity in activities" :key="activity.id" class="block">
                  <div class="minor-text">{{ formatActivityText(activity) }}</div>
                  <div class="minor-text">{{ formatTime(activity.createTime) }}</div>
                </article>
              </div>
              <el-empty v-else description="还没有学习动态" />
            </el-tab-pane>

            <el-tab-pane label="成就" name="achievements">
              <div v-if="achievements.length" class="stack">
                <article v-for="achievement in achievements" :key="achievement.achievementId || achievement.id" class="block">
                  <strong>{{ achievement.name || achievement.title || '学习成就' }}</strong>
                  <p>{{ achievement.description || '继续练习会逐步解锁更多成就。' }}</p>
                  <div class="minor-text">获得于 {{ formatTime(achievement.unlockTime || achievement.createTime) }}</div>
                </article>
              </div>
              <el-empty v-else description="还没有解锁成就" />
            </el-tab-pane>

            <el-tab-pane label="学习动态" name="posts">
              <div v-if="posts.length" class="stack">
                <button v-for="post in posts" :key="post.id" type="button" class="block submission-btn" @click="goToPost(post.id)">
                  <strong>{{ post.title }}</strong>
                  <el-tag v-if="isOwner && post.visibility === 'private'" size="small" type="warning" effect="plain">仅自己可见</el-tag>
                  <div class="minor-text">{{ formatTime(post.createTime) }} · {{ post.viewCount }} 浏览 · {{ post.commentCount }} 评论</div>
                </button>
              </div>
              <el-empty v-else description="还没有发布学习动态">
                <el-button type="primary" @click="goToPostCreate">去社区记录</el-button>
              </el-empty>
            </el-tab-pane>

            <el-tab-pane label="收藏" name="favorites">
              <div v-if="favorites.length" class="stack">
                <button
                  v-for="favorite in favorites"
                  :key="`${favorite.targetType}-${favorite.targetId}`"
                  type="button"
                  class="block submission-btn"
                  @click="goToTarget(favorite)"
                >
                  <div class="meta-row">
                    <strong>{{ favorite.targetName }}</strong>
                    <el-tag size="small">{{ favorite.targetType === 'POST' ? '帖子' : '题目' }}</el-tag>
                  </div>
                  <div class="minor-text">{{ formatTime(favorite.createTime) }}</div>
                </button>
              </div>
              <el-empty v-else description="还没有收藏内容" />
            </el-tab-pane>
          </el-tabs>
        </section>
      </div>
    </section>

    <el-dialog v-model="editDialogVisible" title="编辑资料" width="520px">
      <el-form :model="editForm" label-width="84px">
        <el-form-item label="个人简介">
          <el-input v-model="editForm.bio" type="textarea" :rows="3" placeholder="输入个人简介" />
        </el-form-item>
        <el-form-item label="头像 URL">
          <el-input v-model="editForm.avatarUrl" placeholder="输入头像地址" />
        </el-form-item>
        <el-form-item label="GitHub">
          <el-input v-model="editForm.githubUrl" placeholder="输入 GitHub 链接" />
        </el-form-item>
        <el-form-item label="博客">
          <el-input v-model="editForm.blogUrl" placeholder="输入博客链接" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Camera,
  Clock,
  DataAnalysis,
  DataLine,
  Edit,
  Guide,
  MagicStick,
  TrendCharts,
  Trophy,
  User,
  Warning,
  CircleCheck,
  Document,
  Medal
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getMySubmissions } from '@/api/submit'
import { getUserPosts } from '@/api/community'
import { getFavorites, getStudyActivities, getStudyStats, getUserAchievements, getUserProfile, updateProfile } from '@/api/userProfile'
import { getActivePaths, getDifficultyStats, getKnowledgeMastery, getMyLearnStats, getRecommendations } from '@/api/learn'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const userId = ref(route.params.userId || userStore.userInfo?.id || null)
const activeTab = ref('activities')

const profile = ref({ username: '', avatarUrl: '', bio: '', githubUrl: '', blogUrl: '', isAdmin: false })
const stats = ref({ solved: 0, submitted: 0, accuracy: 0, streak: 0, studyHours: 0, ranking: 0, solvedTrend: 0, submittedTrend: 0, accuracyTrend: 0, streakTrend: 0, studyHoursTrend: 0, rankingTrend: 0 })
const activePaths = ref([])
const knowledgeMastery = ref([])
const weakPoints = ref([])
const difficultyStats = ref({ easy: 0, medium: 0, hard: 0 })
const recommendedProblems = ref([])
const recentSubmissions = ref([])
const activities = ref([])
const achievements = ref([])
const posts = ref([])
const favorites = ref([])
const editDialogVisible = ref(false)
const editForm = ref({ bio: '', avatarUrl: '', githubUrl: '', blogUrl: '' })

const isOwner = computed(() => String(userId.value || '') === String(userStore.userInfo?.id || ''))

const normalizeStats = (payload = {}) => {
  const data = payload.stats || payload
  const passRatePercent = data.passRate !== undefined ? Math.round(Number(data.passRate) * 100) : Number(data.accuracy || 0)
  stats.value = {
    solved: data.solved ?? data.totalSolved ?? 0,
    submitted: data.submitted ?? data.totalSubmissions ?? 0,
    accuracy: passRatePercent,
    streak: data.streak ?? 0,
    studyHours: data.studyHours ?? 0,
    ranking: data.ranking ?? payload.ranking ?? profile.value.ranking ?? 0,
    solvedTrend: data.solvedTrend ?? data.weeklySolvedTrend ?? 0,
    submittedTrend: data.submittedTrend ?? data.weeklySubmittedTrend ?? 0,
    accuracyTrend: data.accuracyTrend ?? data.weeklyAccuracyTrend ?? 0,
    streakTrend: data.streakTrend ?? data.weeklyStreakTrend ?? 0,
    studyHoursTrend: data.studyHoursTrend ?? data.weeklyStudyHoursTrend ?? 0,
    rankingTrend: data.rankingTrend ?? data.weeklyRankingTrend ?? 0
  }
}

const resetPrivateSections = () => {
  knowledgeMastery.value = []
  weakPoints.value = []
  recommendedProblems.value = []
  recentSubmissions.value = []
  favorites.value = []
  if (activeTab.value === 'favorites') {
    activeTab.value = 'activities'
  }
}

const statCards = computed(() => [
  { key: 'solved', label: '已解题', value: stats.value.solved, trend: stats.value.solvedTrend, unit: '', icon: CircleCheck },
  { key: 'submitted', label: '提交次数', value: stats.value.submitted, trend: stats.value.submittedTrend, unit: '', icon: Document },
  { key: 'accuracy', label: '通过率', value: `${stats.value.accuracy}%`, trend: stats.value.accuracyTrend, unit: '%', icon: TrendCharts },
  { key: 'streak', label: '连续天数', value: stats.value.streak, trend: stats.value.streakTrend, unit: '', icon: Trophy },
  { key: 'study-time', label: '学习时长', value: `${stats.value.studyHours}h`, trend: stats.value.studyHoursTrend, unit: 'h', icon: Clock },
  { key: 'ranking', label: '排名', value: `#${stats.value.ranking}`, trend: stats.value.rankingTrend, unit: '', icon: Medal }
])

const difficultyRows = computed(() => [
  { key: 'easy', label: '简单', value: difficultyStats.value.easy },
  { key: 'medium', label: '中等', value: difficultyStats.value.medium },
  { key: 'hard', label: '困难', value: difficultyStats.value.hard }
])

const normalizeList = (data) => Array.isArray(data) ? data : (data?.list || data?.records || data?.content || data?.items || [])
const isAcceptedResult = (result) => result === 'ACCEPTED' || result === 'accepted' || Number(result) === 0
const formatTrend = (value, unit = '') => `${Number(value || 0) > 0 ? '+' : ''}${Number(value || 0)}${unit} 本周`
const formatTime = (value) => {
  if (!value) return '暂无时间'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? '暂无时间' : date.toLocaleString('zh-CN')
}

const getLanguageLabel = (language) => ({ java: 'Java', python: 'Python', 'c++': 'C++', javascript: 'JavaScript' }[String(language || '').toLowerCase()] || language || '未指定')
const getDirectionLabel = (direction) => ({ algorithm: '算法', 'data-structure': '数据结构', web: 'Web 开发', ai: '人工智能' }[String(direction || '').toLowerCase()] || direction || '综合')
const getProgressColor = (progress) => progress >= 80 ? '#67c23a' : progress >= 50 ? '#409eff' : '#e6a23c'
const getMasteryLevelClass = (level) => level >= 0.8 ? 'mastery-expert' : level >= 0.6 ? 'mastery-proficient' : level >= 0.4 ? 'mastery-intermediate' : 'mastery-beginner'
const getMasteryLevelText = (level) => level >= 0.8 ? '专家' : level >= 0.6 ? '熟练' : level >= 0.4 ? '中级' : '初级'
const getMasteryColor = (level) => level >= 0.8 ? '#67c23a' : level >= 0.6 ? '#409eff' : level >= 0.4 ? '#e6a23c' : '#f56c6c'
const getBarWidth = (count) => {
  const total = difficultyStats.value.easy + difficultyStats.value.medium + difficultyStats.value.hard
  return total > 0 ? (count / total) * 100 : 0
}
const normalizeDifficultyKey = (difficulty) => {
  const value = String(difficulty ?? '').trim().toLowerCase()
  if (value === '0' || value === 'easy') return 'easy'
  if (value === '1' || value === 'medium' || value === 'normal') return 'medium'
  if (value === '2' || value === 'hard') return 'hard'
  return 'hard'
}
const getDifficultyTagType = (difficulty) => {
  const normalized = normalizeDifficultyKey(difficulty)
  return normalized === 'easy' ? 'success' : normalized === 'medium' ? 'warning' : 'danger'
}
const getDifficultyText = (difficulty) => difficulty === 'easy' ? '简单' : difficulty === 'medium' ? '中等' : '困难'
const getResultTagType = (result) => isAcceptedResult(result) ? 'success' : 'danger'
const getSubmissionResultText = (result) => isAcceptedResult(result) ? '通过' : '未通过'
const formatActivityText = (activity) => {
  if (activity.type === 'SOLVE_PROBLEM') return `完成题目 ${activity.targetName || ''}`
  if (activity.type === 'CREATE_POST') return `发布帖子 ${activity.targetName || ''}`
  return '完成了一条新的学习记录'
}

const loadProfile = async () => {
  try {
    const res = await getUserProfile(userId.value)
    if (res?.code === 200) {
      profile.value = { ...profile.value, ...(res.data || {}) }
      stats.value = { ...stats.value, ranking: Number(res.data?.ranking || 0) }
    }
  } catch (error) {
    console.error('获取个人资料失败:', error)
  }
}

const loadStats = async () => {
  try {
    const res = isOwner.value ? await getMyLearnStats() : await getStudyStats(userId.value)
    if (res?.code === 200) {
      normalizeStats(res.data || {})
    }
  } catch (error) {
    console.error('获取学习统计失败:', error)
  }
}

const loadActivePaths = async () => {
  try {
    const res = await getActivePaths(userId.value)
    if (res?.code === 200) activePaths.value = normalizeList(res.data)
  } catch (error) {
    console.error('获取学习路径失败:', error)
    activePaths.value = []
  }
}

const loadKnowledgeMastery = async () => {
  if (!isOwner.value) {
    knowledgeMastery.value = []
    weakPoints.value = []
    return
  }
  try {
    const res = await getKnowledgeMastery()
    if (res?.code === 200) {
      const list = normalizeList(res.data?.masteryList || res.data)
      const normalized = list.map((item) => {
        const rawLevel = Number(item.masteryLevel ?? 0)
        const masteryLevel = rawLevel > 1 ? Math.min(rawLevel / 3, 1) : Math.max(rawLevel, 0)
        return {
          ...item,
          id: item.knowledgeId ?? item.id,
          knowledgeName: item.knowledgeName || item.knowledgePoint?.name || `知识点 ${item.knowledgeId ?? ''}`.trim(),
          masteryLevel
        }
      })
      knowledgeMastery.value = normalized
      weakPoints.value = normalized.filter((item) => item.masteryLevel < 0.4).map((item) => ({ id: item.id, name: item.knowledgeName }))
    }
  } catch (error) {
    console.error('获取知识点掌握度失败:', error)
    knowledgeMastery.value = []
    weakPoints.value = []
  }
}

const loadDifficulty = async () => {
  try {
    const res = await getDifficultyStats(userId.value)
    if (res?.code === 200) difficultyStats.value = { easy: Number(res.data?.easy || 0), medium: Number(res.data?.medium || 0), hard: Number(res.data?.hard || 0) }
  } catch (error) {
    console.error('获取难度分布失败:', error)
    difficultyStats.value = { easy: 0, medium: 0, hard: 0 }
  }
}

const loadRecommendations = async () => {
  if (!isOwner.value) {
    recommendedProblems.value = []
    return
  }
  try {
    const res = await getRecommendations()
    if (res?.code === 200) {
      const source = res.data?.problems || res.data
      recommendedProblems.value = normalizeList(source).map((item) => ({
        ...item,
        difficulty: normalizeDifficultyKey(item.difficulty),
        title: item.title || '未命名题目',
        description: item.description || item.content || '这道题暂时还没有补充描述。',
        viewCount: Number(item.viewCount || 0),
        passedCount: Number(item.passedCount || item.acceptedCount || 0)
      }))
    }
  } catch (error) {
    console.error('获取推荐题目失败:', error)
    recommendedProblems.value = []
  }
}

const loadRecentSubmissions = async () => {
  if (!isOwner.value) {
    recentSubmissions.value = []
    return
  }
  try {
    const res = await getMySubmissions({ page: 1, size: 5 })
    if (res?.code === 200) {
      recentSubmissions.value = normalizeList(res.data).map((item) => ({
        ...item,
        problemTitle: item.problemTitle || item.title || item.problemName || `题目 ${item.problemId ?? item.id ?? ''}`.trim(),
        submitTime: item.submitTime || item.createTime || item.createdAt || ''
      }))
    }
  } catch (error) {
    console.error('获取最近提交失败:', error)
    recentSubmissions.value = []
  }
}

const loadActivities = async () => {
  try {
    const res = await getStudyActivities(userId.value)
    if (res?.code === 200) activities.value = normalizeList(res.data)
  } catch (error) {
    console.error('获取学习动态失败:', error)
    activities.value = []
  }
}

const loadAchievements = async () => {
  try {
    const res = await getUserAchievements(userId.value)
    if (res?.code === 200) achievements.value = normalizeList(res.data)
  } catch (error) {
    console.error('获取成就失败:', error)
    achievements.value = []
  }
}

const loadPosts = async () => {
  try {
    const res = await getUserPosts({ userId: userId.value, page: 0, size: 10 })
    if (res?.code === 200) {
      posts.value = normalizeList(res.data).map((item) => ({
        ...item,
        title: item.title || '未命名动态',
        createTime: item.createTime || item.updateTime || '',
        visibility: item.visibility || 'public',
        viewCount: Number(item.viewCount || item.views || 0),
        commentCount: Number(item.commentCount || item.comments || 0)
      }))
    }
  } catch (error) {
    console.error('获取帖子失败:', error)
    posts.value = []
  }
}

const loadFavorites = async () => {
  if (!isOwner.value) {
    favorites.value = []
    return
  }
  try {
    const [postRes, problemRes] = await Promise.all([getFavorites(userId.value, 'POST'), getFavorites(userId.value, 'PROBLEM')])
    favorites.value = [
      ...normalizeList(postRes?.data).map((item) => ({ ...item, targetType: item.targetType || 'POST', targetName: item.targetName || item.name || '未命名帖子' })),
      ...normalizeList(problemRes?.data).map((item) => ({ ...item, targetType: item.targetType || 'PROBLEM', targetName: item.targetName || item.name || '未命名题目' }))
    ]
  } catch (error) {
    console.error('获取收藏失败:', error)
    favorites.value = []
  }
}

const loadAll = async () => {
  if (!userId.value) return
  if (!isOwner.value) {
    resetPrivateSections()
  }
  await Promise.all([loadProfile(), loadStats(), loadActivePaths(), loadKnowledgeMastery(), loadDifficulty(), loadRecommendations(), loadRecentSubmissions(), loadActivities(), loadAchievements(), loadPosts(), loadFavorites()])
}

const showEditDialog = () => {
  editForm.value = { bio: profile.value.bio || '', avatarUrl: profile.value.avatarUrl || '', githubUrl: profile.value.githubUrl || '', blogUrl: profile.value.blogUrl || '' }
  editDialogVisible.value = true
}

const submitEdit = async () => {
  try {
    await updateProfile(editForm.value)
    editDialogVisible.value = false
    await loadProfile()
    ElMessage.success('资料已更新')
  } catch (error) {
    console.error('更新资料失败:', error)
    ElMessage.error('更新资料失败')
  }
}

const goToLearn = () => router.push('/learn')
const continuePath = (pathId) => router.push({ name: 'LearningPath', params: { id: pathId } })
const viewKnowledgeGraph = () => router.push({ name: 'KnowledgeGraph' })
const goToProblem = (problemId) => router.push(`/problem/${problemId}`)
const startProblem = (problemId) => router.push({ name: 'ProblemDetail', params: { id: problemId } })
const goToSubmission = (submissionId) => router.push({ name: 'Submissions', query: { submitId: submissionId } })
const goToPost = (postId) => router.push(`/community/post/${postId}`)
const goToPostCreate = () => router.push('/community/write')
const goToTarget = (favorite) => router.push(favorite.targetType === 'POST' ? `/community/post/${favorite.targetId}` : `/problem/${favorite.targetId}`)
const changeAvatar = () => ElMessage.info('头像修改功能开发中')

watch(() => route.params.userId, (value) => {
  userId.value = value || userStore.userInfo?.id || null
  loadAll()
})

onMounted(() => {
  loadAll()
})
</script>

<style scoped>
.user-profile-page { max-width: 1240px; margin: 0 auto; padding: 20px; }
.card { border: 1px solid var(--border-color, #ebeef5); border-radius: 18px; background: var(--bg-card, #fff); box-shadow: 0 12px 30px rgba(15, 23, 42, 0.06); }
.profile-card, .section-card { padding: 22px; }
.profile-card, .stats-grid, .section-card { margin-bottom: 20px; }
.profile-header, .profile-title, .meta-row, .section-header, .actions, .links, .inline-tags { display: flex; }
.profile-header { gap: 24px; align-items: center; }
.avatar-wrap { position: relative; }
.avatar-btn { position: absolute; right: 0; bottom: 0; }
.profile-info { flex: 1; min-width: 0; }
.profile-title { align-items: center; gap: 12px; flex-wrap: wrap; }
.profile-title h1 { margin: 0; font-size: 30px; color: var(--text-primary, #1f2937); }
.bio { margin: 12px 0; line-height: 1.7; color: var(--text-secondary, #606266); }
.links { gap: 12px; flex-wrap: wrap; margin-bottom: 16px; }
.links a, .section-header a { color: var(--primary-color, #409eff); text-decoration: none; }
.stats-grid { display: grid; grid-template-columns: repeat(6, minmax(0, 1fr)); gap: 16px; }
.stat-card { padding: 18px; text-align: center; }
.stat-icon { display: inline-flex; align-items: center; justify-content: center; width: 48px; height: 48px; border-radius: 14px; margin-bottom: 10px; font-size: 22px; }
.stat-icon.solved { color: #16a34a; background: rgba(34, 197, 94, 0.12); }
.stat-icon.submitted { color: #2563eb; background: rgba(59, 130, 246, 0.12); }
.stat-icon.accuracy { color: #d97706; background: rgba(245, 158, 11, 0.14); }
.stat-icon.streak { color: #dc2626; background: rgba(239, 68, 68, 0.12); }
.stat-icon.study-time { color: #475569; background: rgba(148, 163, 184, 0.16); }
.stat-icon.ranking { color: #7c3aed; background: rgba(124, 58, 237, 0.12); }
.stat-value { display: block; font-size: 26px; color: var(--text-primary, #1f2937); }
.stat-label, .minor-text { color: var(--text-secondary, #606266); font-size: 13px; }
.stat-trend { display: inline-flex; align-items: center; gap: 4px; margin-top: 8px; font-size: 13px; }
.stat-trend.up { color: #16a34a; }
.stat-trend.down { color: #ef4444; }
.content-grid { display: grid; grid-template-columns: minmax(0, 2fr) minmax(320px, 1fr); gap: 20px; }
.section-header { align-items: center; justify-content: space-between; gap: 12px; margin-bottom: 16px; }
.section-header h3 { display: inline-flex; align-items: center; gap: 8px; margin: 0; color: var(--text-primary, #1f2937); font-size: 18px; }
.stack { display: flex; flex-direction: column; gap: 14px; }
.block { border: 1px solid var(--border-light, #eef2f7); border-radius: 14px; background: rgba(248, 250, 252, 0.9); padding: 16px; }
.block h4, .block p { margin: 10px 0 0; }
.block p { color: var(--text-secondary, #606266); line-height: 1.6; }
.clickable, .submission-btn { cursor: pointer; text-align: left; width: 100%; }
.submission-btn { background: rgba(248, 250, 252, 0.9); }
.actions { justify-content: flex-end; margin-top: 14px; }
.inline-tags { gap: 8px; flex-wrap: wrap; }
.pill { display: inline-flex; align-items: center; padding: 4px 10px; border-radius: 999px; background: rgba(148, 163, 184, 0.16); color: #475569; font-size: 12px; font-weight: 600; }
.pill-primary { background: rgba(59, 130, 246, 0.12); color: #2563eb; }
.mastery-expert { background: rgba(34, 197, 94, 0.12); color: #15803d; }
.mastery-proficient { background: rgba(59, 130, 246, 0.12); color: #2563eb; }
.mastery-intermediate { background: rgba(245, 158, 11, 0.16); color: #b45309; }
.mastery-beginner { background: rgba(239, 68, 68, 0.12); color: #dc2626; }
.meta-row { justify-content: space-between; gap: 12px; align-items: center; flex-wrap: wrap; }
.weak-points { margin-top: 16px; }
.sub-title { display: inline-flex; align-items: center; gap: 6px; margin-bottom: 10px; color: #d97706; font-size: 14px; }
.difficulty-row { display: grid; grid-template-columns: 48px minmax(0, 1fr) 32px; gap: 12px; align-items: center; }
.difficulty-bar { overflow: hidden; height: 8px; border-radius: 999px; background: #eef2f7; }
.difficulty-fill { height: 100%; border-radius: 999px; }
.difficulty-row.easy .difficulty-fill { background: #22c55e; }
.difficulty-row.medium .difficulty-fill { background: #f59e0b; }
.difficulty-row.hard .difficulty-fill { background: #ef4444; }
@media (max-width: 1100px) {
  .stats-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }
  .content-grid { grid-template-columns: 1fr; }
}
@media (max-width: 768px) {
  .user-profile-page { padding: 16px; }
  .profile-header { flex-direction: column; align-items: flex-start; }
  .stats-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
@media (max-width: 560px) {
  .stats-grid { grid-template-columns: 1fr; }
  .section-header, .meta-row { flex-direction: column; align-items: flex-start; }
}
</style>
