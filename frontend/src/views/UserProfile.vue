<template>
  <div class="user-profile-page">
    <!-- 个人信息卡片 -->
    <div class="profile-card">
      <div class="profile-header">
        <div class="avatar-section">
          <el-avatar :size="120" :src="profile.avatarUrl">
            <el-icon><User /></el-icon>
          </el-avatar>
          <div class="avatar-actions">
            <el-button size="small" circle @click="changeAvatar">
              <el-icon><Camera /></el-icon>
            </el-button>
          </div>
        </div>
        
        <div class="profile-info">
          <div class="profile-main">
            <h2 class="username">{{ profile.username }}</h2>
            <el-tag v-if="profile.isAdmin" type="danger" size="small">管理员</el-tag>
            <el-tag v-else type="primary" size="small">学习者</el-tag>
          </div>
          
          <p class="bio">{{ profile.bio || '这个人很懒，什么都没留下...' }}</p>
          
          <div class="social-links">
            <a v-if="profile.githubUrl" :href="profile.githubUrl" target="_blank" class="social-link">
              <el-icon><Link /></el-icon> GitHub
            </a>
            <a v-if="profile.blogUrl" :href="profile.blogUrl" target="_blank" class="social-link">
              <el-icon><Link /></el-icon> 博客
            </a>
          </div>
          
          <div class="profile-actions">
            <el-button v-if="isOwner" type="primary" @click="showEditDialog">
              <el-icon><Edit /></el-icon> 编辑资料
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 学习统计卡片 (来自学习中心) -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon solved">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.solved }}</div>
          <div class="stat-label">已解决</div>
          <div class="stat-trend" :class="stats.solvedTrend > 0 ? 'up' : 'down'">
            <el-icon><TrendCharts /></el-icon>
            {{ stats.solvedTrend > 0 ? '+' : '' }}{{ stats.solvedTrend }} 本周
          </div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon submitted">
          <el-icon><Document /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.submitted }}</div>
          <div class="stat-label">提交次数</div>
          <div class="stat-trend" :class="stats.submittedTrend > 0 ? 'up' : 'down'">
            <el-icon><TrendCharts /></el-icon>
            {{ stats.submittedTrend > 0 ? '+' : '' }}{{ stats.submittedTrend }} 本周
          </div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon accuracy">
          <el-icon><TrendCharts /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.accuracy }}%</div>
          <div class="stat-label">通过率</div>
          <div class="stat-trend" :class="stats.accuracyTrend > 0 ? 'up' : 'down'">
            <el-icon><TrendCharts /></el-icon>
            {{ stats.accuracyTrend > 0 ? '+' : '' }}{{ stats.accuracyTrend }}% 本周
          </div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon streak">
          <el-icon><Trophy /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.streak }}</div>
          <div class="stat-label">连续天数</div>
          <div class="stat-trend" :class="stats.streakTrend > 0 ? 'up' : 'down'">
            <el-icon><TrendCharts /></el-icon>
            {{ stats.streakTrend > 0 ? '+' : '' }}{{ stats.streakTrend }} 本周
          </div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon study-time">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.studyHours }}h</div>
          <div class="stat-label">学习时长</div>
          <div class="stat-trend" :class="stats.studyHoursTrend > 0 ? 'up' : 'down'">
            <el-icon><TrendCharts /></el-icon>
            {{ stats.studyHoursTrend > 0 ? '+' : '' }}{{ stats.studyHoursTrend }}h 本周
          </div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon ranking">
          <el-icon><Medal /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">#{{ stats.ranking }}</div>
          <div class="stat-label">排名</div>
          <div class="stat-trend" :class="stats.rankingTrend > 0 ? 'up' : 'down'">
            <el-icon><TrendCharts /></el-icon>
            {{ stats.rankingTrend > 0 ? '+' : '' }}{{ stats.rankingTrend }} 本周
          </div>
        </div>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="content-grid">
      <!-- 左侧列 -->
      <div class="left-column">
        <!-- 学习路径进度 (来自学习中心) -->
        <div class="section-card learning-paths-section">
          <div class="section-header">
            <h3 class="section-title">
              <el-icon><Guide /></el-icon>
              我的学习路径
            </h3>
            <router-link to="/learn" class="view-all-link">
              查看全部
              <el-icon><ArrowRight /></el-icon>
            </router-link>
          </div>
          
          <div class="paths-grid">
            <div 
              v-for="path in activePaths" 
              :key="path.id"
              class="path-card"
            >
              <div class="path-header">
                <div class="path-language-badge" :class="path.language">
                  {{ getLanguageLabel(path.language) }}
                </div>
                <div class="path-direction-badge">
                  {{ getDirectionLabel(path.direction) }}
                </div>
              </div>
              
              <h4 class="path-name">{{ path.name }}</h4>
              <p class="path-description">{{ path.description }}</p>
              
              <div class="path-progress-section">
                <div class="progress-info">
                  <span class="progress-label">学习进度</span>
                  <span class="progress-value">{{ path.progress || 0 }}%</span>
                </div>
                <el-progress 
                  :percentage="path.progress || 0" 
                  :show-text="false" 
                  :stroke-width="8"
                  :color="getProgressColor(path.progress)"
                />
              </div>
              
              <div class="path-actions">
                <el-button type="primary" size="small" @click="continuePath(path.id)">
                  <el-icon><VideoPlay /></el-icon>
                  继续学习
                </el-button>
              </div>
            </div>
          </div>
          
          <div v-if="activePaths.length === 0" class="empty-paths">
            <el-empty description="暂无正在学习的路径">
              <el-button type="primary" @click="goToLearn">
                开始学习
              </el-button>
            </el-empty>
          </div>
        </div>

        <!-- 知识点掌握度 (来自学习中心) -->
        <div class="section-card knowledge-mastery-section">
          <div class="section-header">
            <h3 class="section-title">
              <el-icon><DataAnalysis /></el-icon>
              知识点掌握度
            </h3>
            <el-button size="small" @click="viewKnowledgeGraph">
              <el-icon><Share /></el-icon>
              知识图谱
            </el-button>
          </div>
          
          <div class="mastery-grid">
            <div 
              v-for="mastery in knowledgeMastery" 
              :key="mastery.knowledgeId"
              class="mastery-item"
            >
              <div class="mastery-header">
                <div class="mastery-name">{{ mastery.knowledgeName }}</div>
                <div class="mastery-level" :class="getMasteryLevelClass(mastery.masteryLevel)">
                  {{ getMasteryLevelText(mastery.masteryLevel) }}
                </div>
              </div>
              
              <el-progress 
                :percentage="Math.round(mastery.masteryLevel * 100)" 
                :color="getMasteryColor(mastery.masteryLevel)" 
                :stroke-width="10"
              />
              
              <div class="mastery-stats">
                <span class="mastery-stat">
                  <el-icon><Document /></el-icon>
                  {{ mastery.solvedCount }} 题目
                </span>
                <span class="mastery-stat">
                  <el-icon><CircleCheck /></el-icon>
                  {{ mastery.passedCount }} 通过
                </span>
              </div>
            </div>
          </div>
          
          <div v-if="weakPoints.length > 0" class="weak-points-section">
            <h4 class="weak-points-title">
              <el-icon><Warning /></el-icon>
              薄弱知识点
            </h4>
            <div class="weak-points-list">
              <el-tag 
                v-for="point in weakPoints" 
                :key="point.id"
                type="warning"
                class="weak-point-tag"
              >
                {{ point.name }}
              </el-tag>
            </div>
          </div>
        </div>

        <!-- 难度分布 (来自学习中心) -->
        <div class="section-card difficulty-section">
          <div class="section-header">
            <h3 class="section-title">
              <el-icon><DataLine /></el-icon>
              难度分布
            </h3>
          </div>
          
          <div class="difficulty-stats">
            <div class="difficulty-item easy">
              <div class="difficulty-label">简单</div>
              <div class="difficulty-bar">
                <div 
                  class="bar-fill" 
                  :style="{ width: getBarWidth(difficultyStats.easy) + '%' }"
                ></div>
              </div>
              <div class="difficulty-count">{{ difficultyStats.easy }}</div>
            </div>
            
            <div class="difficulty-item medium">
              <div class="difficulty-label">中等</div>
              <div class="difficulty-bar">
                <div 
                  class="bar-fill" 
                  :style="{ width: getBarWidth(difficultyStats.medium) + '%' }"
                ></div>
              </div>
              <div class="difficulty-count">{{ difficultyStats.medium }}</div>
            </div>
            
            <div class="difficulty-item hard">
              <div class="difficulty-label">困难</div>
              <div class="difficulty-bar">
                <div 
                  class="bar-fill" 
                  :style="{ width: getBarWidth(difficultyStats.hard) + '%' }"
                ></div>
              </div>
              <div class="difficulty-count">{{ difficultyStats.hard }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧列 -->
      <div class="right-column">
        <!-- 个性化推荐 (简化版，来自学习中心) -->
        <div class="section-card recommendations-section">
          <div class="section-header">
            <h3 class="section-title">
              <el-icon><MagicStick /></el-icon>
              为你推荐
            </h3>
            <el-tag type="success" size="small">
              <el-icon><MagicStick /></el-icon>
              智能推荐
            </el-tag>
          </div>
          
          <div class="recommendations-grid">
            <div 
              v-for="problem in recommendedProblems.slice(0, 3)" 
              :key="problem.id"
              class="problem-card"
              @click="goToProblem(problem.id)"
            >
              <div class="problem-header">
                <el-tag :type="getDifficultyTagType(problem.difficulty)" size="small">
                  {{ getDifficultyText(problem.difficulty) }}
                </el-tag>
                <el-tag v-if="problem.isRecommended" type="success" size="small">
                  <el-icon><Star /></el-icon>
                  推荐
                </el-tag>
              </div>
              
              <h4 class="problem-title">{{ problem.title }}</h4>
              <p class="problem-description">{{ problem.description }}</p>
              
              <div class="problem-stats">
                <span class="problem-stat">
                  <el-icon><View /></el-icon>
                  {{ problem.viewCount }}
                </span>
                <span class="problem-stat">
                  <el-icon><CircleCheck /></el-icon>
                  {{ problem.passedCount }}
                </span>
              </div>
              
              <el-button type="primary" size="small" @click.stop="startProblem(problem.id)">
                <el-icon><VideoPlay /></el-icon>
                开始解题
              </el-button>
            </div>
          </div>
        </div>

        <!-- 最近提交 (来自学习中心) -->
        <div class="section-card recent-submissions-section">
          <div class="section-header">
            <h3 class="section-title">
              <el-icon><Clock /></el-icon>
              最近提交
            </h3>
            <router-link to="/submissions" class="view-all-link">
              查看全部
              <el-icon><ArrowRight /></el-icon>
            </router-link>
          </div>
          
          <div class="submissions-list">
            <div 
              v-for="submission in recentSubmissions" 
              :key="submission.id"
              class="submission-item"
              @click="goToSubmission(submission.id)"
            >
              <div class="submission-icon">
                <el-icon 
                  :class="getResultIconClass(submission.result)"
                >
                  <component :is="getResultIcon(submission.result)" />
                </el-icon>
              </div>
              
              <div class="submission-info">
                <div class="submission-title">{{ submission.problemTitle }}</div>
                <div class="submission-meta">
                  <el-tag :type="getResultTagType(submission.result)" size="small">
                    {{ getResultText(submission.result) }}
                  </el-tag>
                  <span class="submission-time">{{ formatTime(submission.submitTime) }}</span>
                </div>
              </div>
              
              <el-icon class="arrow-icon"><ArrowRight /></el-icon>
            </div>
          </div>
          
          <div v-if="recentSubmissions.length === 0" class="empty-submissions">
            <el-empty description="暂无提交记录" />
          </div>
        </div>

        <!-- 标签页 -->
        <div class="section-card tabs-section">
          <el-tabs v-model="activeTab" class="profile-tabs">
            <!-- 学习动态 -->
            <el-tab-pane label="学习动态" name="activities">
              <div class="activities-list">
                <div 
                  v-for="activity in activities" 
                  :key="activity.id"
                  class="activity-item"
                >
                  <div class="activity-icon">
                    <el-icon v-if="activity.type === 'SOLVE_PROBLEM'">
                      <Edit />
                    </el-icon>
                    <el-icon v-else-if="activity.type === 'CREATE_POST'">
                      <ChatDotRound />
                    </el-icon>
                  </div>
                  <div class="activity-content">
                    <div class="activity-text">
                      <span v-if="activity.type === 'SOLVE_PROBLEM'">
                        完成题目 <a :href="`/problem/${activity.targetId}`">{{ activity.targetName }}</a>
                        <el-tag :type="activity.status === 'ACCEPTED' ? 'success' : 'danger'" size="small">
                          {{ activity.status === 'ACCEPTED' ? '通过' : '未通过' }}
                        </el-tag>
                      </span>
                      <span v-else-if="activity.type === 'CREATE_POST'">
                        发布帖子 <a :href="`/community/post/${activity.targetId}`">{{ activity.targetName }}</a>
                      </span>
                    </div>
                    <div class="activity-time">{{ formatTime(activity.createTime) }}</div>
                  </div>
                </div>
              </div>
            </el-tab-pane>
            
            <!-- 成就 -->
            <el-tab-pane label="成就" name="achievements">
              <div class="achievements-grid">
                <div 
                  v-for="achievement in achievements" 
                  :key="achievement.achievementId"
                  class="achievement-item"
                >
                  <div class="achievement-icon">
                    <el-icon :size="50"><Trophy /></el-icon>
                  </div>
                  <div class="achievement-name">{{ achievement.name }}</div>
                  <div class="achievement-desc">{{ achievement.description }}</div>
                  <div class="achievement-time">
                    获得于 {{ formatTime(achievement.unlockTime) }}
                  </div>
                </div>
              </div>
            </el-tab-pane>
            
            <!-- 发布的帖子 -->
            <el-tab-pane label="发布的帖子" name="posts">
              <div class="posts-list">
                <div 
                  v-for="post in posts" 
                  :key="post.id"
                  class="post-item"
                  @click="goToPost(post.id)"
                >
                  <h4 class="post-title">{{ post.title }}</h4>
                  <div class="post-meta">
                    <span>{{ formatTime(post.createTime) }}</span>
                    <span>{{ post.viewCount }} 浏览</span>
                    <span>{{ post.commentCount }} 评论</span>
                  </div>
                </div>
              </div>
            </el-tab-pane>
            
            <!-- 收藏 -->
            <el-tab-pane label="收藏" name="favorites">
              <div class="favorites-list">
                <div 
                  v-for="favorite in favorites" 
                  :key="favorite.targetId"
                  class="favorite-item"
                  @click="goToTarget(favorite)"
                >
                  <div class="favorite-type">
                    <el-tag>{{ favorite.targetType === 'POST' ? '帖子' : '题目' }}</el-tag>
                  </div>
                  <div class="favorite-name">{{ favorite.targetName }}</div>
                  <div class="favorite-time">{{ formatTime(favorite.createTime) }}</div>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </div>

    <!-- 编辑资料对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑资料" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="个人简介">
          <el-input v-model="editForm.bio" type="textarea" rows="3" placeholder="请输入个人简介" />
        </el-form-item>
        <el-form-item label="头像URL">
          <el-input v-model="editForm.avatarUrl" placeholder="请输入头像URL" />
        </el-form-item>
        <el-form-item label="GitHub链接">
          <el-input v-model="editForm.githubUrl" placeholder="请输入GitHub链接" />
        </el-form-item>
        <el-form-item label="博客链接">
          <el-input v-model="editForm.blogUrl" placeholder="请输入博客链接" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEdit">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { 
  getUserProfile, 
  updateProfile, 
  getStudyActivities, 
  getUserAchievements, 
  getFavorites 
} from '@/api/userProfile'
import { 
  getMyLearnStats, 
  getActivePaths,
  getKnowledgeMastery, 
  getRecommendations,
  getWeakKnowledgePoints,
  getDifficultyStats
} from '@/api/learn'
import { getMySubmissions } from '@/api/submit'
import { Edit, ChatDotRound, User, Camera, Link, Guide, ArrowRight, DataAnalysis, Share, Warning, DataLine, MagicStick, Clock, Trophy, CircleCheck, Document, TrendCharts, Medal, View, VideoPlay, Star } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const userId = ref(route.params.userId || userStore.userInfo?.id)
const activeTab = ref('activities')

const profile = ref({
  username: '',
  avatarUrl: '',
  bio: '',
  githubUrl: '',
  blogUrl: '',
  isAdmin: false
})

const stats = ref({
  solved: 0,
  submitted: 0,
  accuracy: 0,
  streak: 0,
  studyHours: 0,
  ranking: 0,
  solvedTrend: 0,
  submittedTrend: 0,
  accuracyTrend: 0,
  streakTrend: 0,
  studyHoursTrend: 0,
  rankingTrend: 0
})

const activePaths = ref([])
const knowledgeMastery = ref([])
const weakPoints = ref([])
const difficultyStats = ref({
  easy: 0,
  medium: 0,
  hard: 0
})

const recommendedProblems = ref([])
const recentSubmissions = ref([])
const activities = ref([])
const achievements = ref([])
const posts = ref([])
const favorites = ref([])

const editDialogVisible = ref(false)
const editForm = ref({
  bio: '',
  avatarUrl: '',
  githubUrl: '',
  blogUrl: ''
})

const isOwner = computed(() => {
  return userId.value === userStore.userId
})

const loadProfile = async () => {
  try {
    const res = await getUserProfile(userId.value)
    if (res && res.code === 200) {
      profile.value = res.data
    }
  } catch (error) {
    console.error('获取个人信息失败:', error)
    // 使用默认数据
    profile.value = {
      username: '用户',
      avatarUrl: '',
      bio: '这个人很懒，什么都没留下...',
      githubUrl: '',
      blogUrl: '',
      isAdmin: false
    }
  }
}

const loadStats = async () => {
  try {
    const res = await getMyLearnStats(userId.value)
    if (res && res.code === 200) {
      const data = res.data || {}
      const statsData = data.stats || data
      const passRatePercent = statsData.passRate !== undefined
        ? Math.round(Number(statsData.passRate) * 100)
        : (statsData.accuracy || 0)
      stats.value = {
        ...statsData,
        solved: statsData.solved ?? statsData.totalSolved ?? 0,
        submitted: statsData.submitted ?? statsData.totalSubmissions ?? 0,
        accuracy: passRatePercent,
        streak: statsData.streak ?? 0,
        studyHours: statsData.studyHours ?? 0,
        ranking: statsData.ranking ?? data.ranking ?? 0,
        solvedTrend: statsData.solvedTrend ?? statsData.weeklySolvedTrend ?? 0,
        submittedTrend: statsData.submittedTrend ?? statsData.weeklySubmittedTrend ?? 0,
        accuracyTrend: statsData.accuracyTrend ?? statsData.weeklyAccuracyTrend ?? 0,
        streakTrend: statsData.streakTrend ?? statsData.weeklyStreakTrend ?? 0,
        studyHoursTrend: statsData.studyHoursTrend ?? statsData.weeklyStudyHoursTrend ?? 0,
        rankingTrend: statsData.rankingTrend ?? statsData.weeklyRankingTrend ?? 0
      }
    }
  } catch (error) {
    console.error('获取学习统计失败:', error)
    // 使用默认数据
    stats.value = {
      solved: 0,
      submitted: 0,
      accuracy: 0,
      streak: 0,
      studyHours: 0,
      ranking: 0,
      solvedTrend: 0,
      submittedTrend: 0,
      accuracyTrend: 0,
      streakTrend: 0,
      studyHoursTrend: 0,
      rankingTrend: 0
    }
  }
}

const loadActivePaths = async () => {
  try {
    const res = await getActivePaths(userId.value)
    if (res && res.code === 200) {
      activePaths.value = res.data
    }
  } catch (error) {
    console.error('获取学习路径失败:', error)
    // 使用默认数据
    activePaths.value = []
  }
}

const loadKnowledgeMastery = async () => {
  try {
    const res = await getKnowledgeMastery(userId.value)
    if (res && res.code === 200) {
      const list = Array.isArray(res.data) ? res.data : (res.data?.masteryList || [])
      const normalizedList = list.map((item) => {
        const rawLevel = Number(item.masteryLevel ?? 0)
        const normalizedLevel = rawLevel > 1 ? Math.min(rawLevel / 3, 1) : Math.max(rawLevel, 0)
        return {
          ...item,
          id: item.knowledgeId ?? item.id,
          knowledgeName: item.knowledgeName || item.knowledgePoint?.name || `知识点 ${item.knowledgeId ?? ''}`.trim(),
          masteryLevel: normalizedLevel
        }
      })

      knowledgeMastery.value = normalizedList
      weakPoints.value = normalizedList
        .filter((item) => item.masteryLevel < 0.4)
        .map((item) => ({ id: item.id, name: item.knowledgeName }))
    }
  } catch (error) {
    console.error('获取知识点掌握度失败:', error)
    // 使用默认数据
    knowledgeMastery.value = []
    weakPoints.value = []
  }
}

const loadDifficultyStats = async () => {
  try {
    const res = await getDifficultyStats(userId.value)
    if (res && res.code === 200) {
      difficultyStats.value = res.data
    }
  } catch (error) {
    console.error('获取难度分布失败:', error)
    // 使用默认数据
    difficultyStats.value = {
      easy: 0,
      medium: 0,
      hard: 0
    }
  }
}

const loadRecommendations = async () => {
  try {
    const res = await getRecommendations(userId.value)
    if (res && res.code === 200) {
      const data = res.data
      recommendedProblems.value = data.problems || []
    }
  } catch (error) {
    console.error('获取推荐题目失败:', error)
    // 使用默认数据
    recommendedProblems.value = []
  }
}

const loadRecentSubmissions = async () => {
  try {
    const res = await getMySubmissions({ page: 1, size: 5 })
    if (res && res.code === 200) {
      const payload = res.data || {}
      recentSubmissions.value = Array.isArray(payload) ? payload : (payload.list || [])
    }
  } catch (error) {
    console.error('获取最近提交失败:', error)
    // 使用默认数据
    recentSubmissions.value = []
  }
}

const loadActivities = async () => {
  try {
    const res = await getStudyActivities(userId.value)
    if (res && res.code === 200) {
      activities.value = res.data
    }
  } catch (error) {
    console.error('获取学习活动失败:', error)
    // 使用默认数据
    activities.value = []
  }
}

const loadAchievements = async () => {
  try {
    const res = await getUserAchievements(userId.value)
    if (res && res.code === 200) {
      achievements.value = res.data
    }
  } catch (error) {
    console.error('获取成就失败:', error)
    // 使用默认数据
    achievements.value = []
  }
}

const loadFavorites = async () => {
  try {
    const res = await getFavorites(userId.value, 'POST')
    if (res && res.code === 200) {
      favorites.value = res.data
    }
  } catch (error) {
    console.error('获取收藏失败:', error)
    // 使用默认数据
    favorites.value = []
  }
}

onMounted(() => {
  loadProfile()
  loadStats()
  loadActivePaths()
  loadKnowledgeMastery()
  loadDifficultyStats()
  loadRecommendations()
  loadRecentSubmissions()
  loadActivities()
  loadAchievements()
  loadFavorites()
})

const showEditDialog = () => {
  editForm.value = {
    bio: profile.value.bio || '',
    avatarUrl: profile.value.avatarUrl || '',
    githubUrl: profile.value.githubUrl || '',
    blogUrl: profile.value.blogUrl || ''
  }
  editDialogVisible.value = true
}

const submitEdit = async () => {
  try {
    await updateProfile(editForm.value)
    editDialogVisible.value = false
    await loadProfile()
    ElMessage.success('更新成功')
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

const goToLearn = () => {
  router.push('/learn')
}

const continuePath = (pathId) => {
  router.push(`/learning-path/${pathId}`)
}

const viewKnowledgeGraph = () => {
  router.push('/knowledge-graph')
}

const goToProblem = (problemId) => {
  router.push(`/problem/${problemId}`)
}

const startProblem = (problemId) => {
  router.push(`/code-run/${problemId}`)
}

const goToSubmission = (submissionId) => {
  router.push(`/submissions/${submissionId}`)
}

const goToPost = (postId) => {
  router.push(`/community/post/${postId}`)
}

const goToTarget = (favorite) => {
  if (favorite.targetType === 'POST') {
    router.push(`/community/post/${favorite.targetId}`)
  } else if (favorite.targetType === 'PROBLEM') {
    router.push(`/problem/${favorite.targetId}`)
  }
}

const getLanguageLabel = (language) => {
  const labels = {
    'java': 'Java',
    'python': 'Python',
    'c++': 'C++',
    'javascript': 'JavaScript'
  }
  return labels[language] || language
}

const getDirectionLabel = (direction) => {
  const labels = {
    'algorithm': '算法',
    'data-structure': '数据结构',
    'web': 'Web开发',
    'ai': '人工智能'
  }
  return labels[direction] || direction
}

const getProgressColor = (progress) => {
  if (progress >= 80) return '#67c23a'
  if (progress >= 50) return '#409eff'
  return '#e6a23c'
}

const getMasteryLevelClass = (level) => {
  if (level >= 0.8) return 'mastery-expert'
  if (level >= 0.6) return 'mastery-proficient'
  if (level >= 0.4) return 'mastery-intermediate'
  return 'mastery-beginner'
}

const getMasteryLevelText = (level) => {
  if (level >= 0.8) return '专家'
  if (level >= 0.6) return '熟练'
  if (level >= 0.4) return '中级'
  return '初级'
}

const getMasteryColor = (level) => {
  if (level >= 0.8) return '#67c23a'
  if (level >= 0.6) return '#409eff'
  if (level >= 0.4) return '#e6a23c'
  return '#f56c6c'
}

const getBarWidth = (count) => {
  const total = difficultyStats.value.easy + difficultyStats.value.medium + difficultyStats.value.hard
  return total > 0 ? (count / total) * 100 : 0
}

const getDifficultyTagType = (difficulty) => {
  if (difficulty === 'easy') return 'success'
  if (difficulty === 'medium') return 'warning'
  return 'danger'
}

const getDifficultyText = (difficulty) => {
  if (difficulty === 'easy') return '简单'
  if (difficulty === 'medium') return '中等'
  return '困难'
}

const getResultIconClass = (result) => {
  return result === 'ACCEPTED' ? 'result-success' : 'result-error'
}

const getResultIcon = (result) => {
  return result === 'ACCEPTED' ? CircleCheck : Document
}

const getResultTagType = (result) => {
  return result === 'ACCEPTED' ? 'success' : 'danger'
}

const getResultText = (result) => {
  return result === 'ACCEPTED' ? '通过' : '未通过'
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

const changeAvatar = () => {
  // 实现头像修改功能
  ElMessage.info('头像修改功能开发中')
}
</script>

<style scoped>
.user-profile-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.profile-card {
  background: #fff;
  border-radius: 8px;
  padding: 30px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 30px;
  margin-bottom: 30px;
}

.avatar-section {
  position: relative;
}

.avatar-actions {
  position: absolute;
  bottom: 0;
  right: 0;
  background: #fff;
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.profile-info {
  flex: 1;
}

.profile-main {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 10px;
}

.username {
  margin: 0;
  font-size: 24px;
  font-weight: bold;
}

.bio {
  color: #606266;
  margin: 0 0 15px 0;
  line-height: 1.5;
}

.social-links {
  display: flex;
  gap: 15px;
  margin-bottom: 15px;
}

.social-link {
  color: #409eff;
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 5px;
}

.social-link:hover {
  text-decoration: underline;
}

.profile-actions {
  display: flex;
  gap: 10px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  text-align: center;
}

.stat-icon {
  font-size: 32px;
  margin-bottom: 10px;
}

.stat-icon.solved {
  color: #67c23a;
}

.stat-icon.submitted {
  color: #409eff;
}

.stat-icon.accuracy {
  color: #e6a23c;
}

.stat-icon.streak {
  color: #f56c6c;
}

.stat-icon.study-time {
  color: #909399;
}

.stat-icon.ranking {
  color: #722ed1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.stat-trend {
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
}

.stat-trend.up {
  color: #67c23a;
}

.stat-trend.down {
  color: #f56c6c;
}

.content-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
}

.section-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  margin: 0;
  font-size: 18px;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 10px;
}

.view-all-link {
  color: #409eff;
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
}

.view-all-link:hover {
  text-decoration: underline;
}

.paths-grid {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.path-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 15px;
}

.path-header {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.path-language-badge {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.path-language-badge.java {
  background: #e6f7ff;
  color: #1890ff;
}

.path-language-badge.python {
  background: #f6ffed;
  color: #52c41a;
}

.path-language-badge.c {
  background: #fff7e6;
  color: #fa8c16;
}

.path-language-badge.javascript {
  background: #fff1f0;
  color: #ff4d4f;
}

.path-direction-badge {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  background: #f0f2f5;
  color: #606266;
}

.path-name {
  margin: 0 0 5px 0;
  font-size: 16px;
  font-weight: bold;
}

.path-description {
  margin: 0 0 15px 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.4;
}

.path-progress-section {
  margin-bottom: 15px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
  font-size: 14px;
}

.progress-label {
  color: #909399;
}

.progress-value {
  font-weight: bold;
  color: #303133;
}

.path-actions {
  display: flex;
  justify-content: flex-end;
}

.empty-paths {
  padding: 40px 0;
  text-align: center;
}

.mastery-grid {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-bottom: 20px;
}

.mastery-item {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 15px;
}

.mastery-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.mastery-name {
  font-weight: bold;
  color: #303133;
}

.mastery-level {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.mastery-level.mastery-expert {
  background: #f6ffed;
  color: #52c41a;
}

.mastery-level.mastery-proficient {
  background: #e6f7ff;
  color: #1890ff;
}

.mastery-level.mastery-intermediate {
  background: #fff7e6;
  color: #fa8c16;
}

.mastery-level.mastery-beginner {
  background: #fff1f0;
  color: #ff4d4f;
}

.mastery-stats {
  display: flex;
  gap: 15px;
  margin-top: 10px;
  font-size: 14px;
  color: #606266;
}

.mastery-stat {
  display: flex;
  align-items: center;
  gap: 5px;
}

.weak-points-section {
  margin-top: 20px;
}

.weak-points-title {
  margin: 0 0 10px 0;
  font-size: 16px;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #f56c6c;
}

.weak-points-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.weak-point-tag {
  margin: 0;
}

.difficulty-stats {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.difficulty-item {
  display: flex;
  align-items: center;
  gap: 15px;
}

.difficulty-label {
  width: 60px;
  font-size: 14px;
  color: #606266;
}

.difficulty-bar {
  flex: 1;
  height: 8px;
  background: #f0f2f5;
  border-radius: 4px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: 4px;
}

.difficulty-item.easy .bar-fill {
  background: #67c23a;
}

.difficulty-item.medium .bar-fill {
  background: #e6a23c;
}

.difficulty-item.hard .bar-fill {
  background: #f56c6c;
}

.difficulty-count {
  width: 40px;
  text-align: right;
  font-weight: bold;
  color: #303133;
}

.recommendations-grid {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.problem-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 15px;
  cursor: pointer;
  transition: all 0.3s;
}

.problem-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px 0 rgba(64, 158, 255, 0.1);
}

.problem-header {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.problem-title {
  margin: 0 0 5px 0;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.problem-description {
  margin: 0 0 15px 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.problem-stats {
  display: flex;
  gap: 15px;
  margin-bottom: 15px;
  font-size: 14px;
  color: #909399;
}

.problem-stat {
  display: flex;
  align-items: center;
  gap: 5px;
}

.submissions-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.submission-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.submission-item:hover {
  background: #ecf5ff;
}

.submission-icon {
  font-size: 24px;
}

.result-success {
  color: #67c23a;
}

.result-error {
  color: #f56c6c;
}

.submission-info {
  flex: 1;
}

.submission-title {
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.submission-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
  color: #909399;
}

.arrow-icon {
  color: #c0c4cc;
}

.empty-submissions {
  padding: 40px 0;
  text-align: center;
}

.profile-tabs {
  margin-top: 10px;
}

.activities-list, .posts-list, .favorites-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.activity-item, .post-item, .favorite-item {
  display: flex;
  align-items: flex-start;
  gap: 15px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.activity-item:hover, .post-item:hover, .favorite-item:hover {
  background: #ecf5ff;
}

.activity-icon {
  font-size: 20px;
  color: #409eff;
  margin-top: 2px;
}

.activity-content {
  flex: 1;
}

.activity-text {
  color: #303133;
  margin-bottom: 5px;
}

.activity-text a {
  color: #409eff;
  text-decoration: none;
}

.activity-text a:hover {
  text-decoration: underline;
}

.activity-time {
  font-size: 12px;
  color: #909399;
}

.achievements-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 15px;
}

.achievement-item {
  text-align: center;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.achievement-icon {
  color: #e6a23c;
  margin-bottom: 10px;
}

.achievement-name {
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.achievement-desc {
  font-size: 12px;
  color: #606266;
  margin-bottom: 10px;
  line-height: 1.4;
}

.achievement-time {
  font-size: 12px;
  color: #909399;
}

.post-title {
  margin: 0 0 5px 0;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.post-meta {
  display: flex;
  gap: 15px;
  font-size: 12px;
  color: #909399;
}

.favorite-type {
  margin-top: 2px;
}

.favorite-name {
  flex: 1;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.favorite-time {
  font-size: 12px;
  color: #909399;
}

@media (max-width: 768px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .achievements-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
