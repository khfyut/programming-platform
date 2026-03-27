<template>
  <div class="collections-page">
    <section class="summary-grid">
      <article class="summary-card">
        <div class="summary-glow"></div>
        <div class="summary-label">收藏题目</div>
        <div class="summary-value">{{ favoriteProblems.length }}</div>
        <div class="summary-hint">保留值得反复练习的题目</div>
      </article>
      <article class="summary-card">
        <div class="summary-glow"></div>
        <div class="summary-label">收藏帖子</div>
        <div class="summary-value">{{ favoritePosts.length }}</div>
        <div class="summary-hint">沉淀对你有帮助的讨论</div>
      </article>
      <article class="summary-card">
        <div class="summary-glow"></div>
        <div class="summary-label">我的帖子</div>
        <div class="summary-value">{{ myPosts.length }}</div>
        <div class="summary-hint">记录你的经验和复盘</div>
      </article>
      <article class="summary-card">
        <div class="summary-glow"></div>
        <div class="summary-label">当前排名</div>
        <div class="summary-value">{{ rankLabel }}</div>
        <div class="summary-hint">来自当前排行榜接口</div>
      </article>
    </section>

    <section class="content-grid">
      <div class="panel-card achievements-panel">
        <div class="panel-header">
          <div>
            <div class="section-kicker">Achievements</div>
            <h3>成就与扩展</h3>
          </div>
          <el-button text @click="goCommunity">进入社区</el-button>
        </div>

        <div v-if="achievements.length > 0" class="achievement-list">
          <div v-for="item in achievements" :key="item.id" class="achievement-item">
            <div class="achievement-icon">
              <el-icon><Trophy /></el-icon>
            </div>
            <div class="achievement-copy">
              <strong>{{ item.title }}</strong>
              <span>{{ item.description }}</span>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无成就数据" />
      </div>

      <div class="panel-card favorites-panel">
        <div class="panel-header">
          <div>
            <div class="section-kicker">Favorites</div>
            <h3>收藏题目</h3>
          </div>
          <el-button text @click="goProblems">去题库</el-button>
        </div>

        <div v-if="favoriteProblems.length > 0" class="list-block">
          <button
            v-for="item in favoriteProblems"
            :key="`problem-${item.targetId}`"
            class="list-item"
            @click="openProblem(item.targetId)"
          >
            <div class="list-main">
              <strong>{{ item.targetName || `题目 ${item.targetId}` }}</strong>
              <span>{{ formatRelativeTime(item.createTime) }}</span>
            </div>
            <el-button text type="danger" @click.stop="removeFavoriteItem(item.targetId, 'PROBLEM')">
              取消收藏
            </el-button>
          </button>
        </div>
        <el-empty v-else description="暂无收藏题目" />
      </div>

      <div class="panel-card favorites-panel">
        <div class="panel-header">
          <div>
            <div class="section-kicker">Saved Posts</div>
            <h3>收藏帖子</h3>
          </div>
          <el-button text @click="goCommunity">去社区</el-button>
        </div>

        <div v-if="favoritePosts.length > 0" class="list-block">
          <button
            v-for="item in favoritePosts"
            :key="`post-${item.targetId}`"
            class="list-item"
            @click="openPost(item.targetId)"
          >
            <div class="list-main">
              <strong>{{ item.targetName || `帖子 ${item.targetId}` }}</strong>
              <span>{{ formatRelativeTime(item.createTime) }}</span>
            </div>
            <el-button text type="danger" @click.stop="removeFavoriteItem(item.targetId, 'POST')">
              取消收藏
            </el-button>
          </button>
        </div>
        <el-empty v-else description="暂无收藏帖子" />
      </div>

      <div class="panel-card posts-panel">
        <div class="panel-header">
          <div>
            <div class="section-kicker">My Posts</div>
            <h3>我的帖子</h3>
          </div>
          <el-button text @click="goCommunity">去发布</el-button>
        </div>

        <div v-if="myPosts.length > 0" class="list-block">
          <button
            v-for="post in myPosts"
            :key="post.id"
            class="list-item"
            @click="openPost(post.id)"
          >
            <div class="list-main">
              <strong>{{ post.title || `帖子 ${post.id}` }}</strong>
              <span>{{ post.summary || post.content || '暂无摘要' }}</span>
            </div>
            <div class="item-meta">{{ formatRelativeTime(post.createTime) }}</div>
          </button>
        </div>
        <el-empty v-else description="你还没有发布帖子" />
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Trophy } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getMyPosts } from '@/api/community'
import {
  getFavorites,
  getMyRank,
  getUserAchievements,
  removeFavorite
} from '@/api/userProfile'

const router = useRouter()
const userStore = useUserStore()
const userId = computed(() => userStore.userInfo?.id)

const favoriteProblems = ref([])
const favoritePosts = ref([])
const myPosts = ref([])
const achievements = ref([])
const myRank = ref(null)

const rankLabel = computed(() => {
  if (!myRank.value) {
    return '--'
  }

  return myRank.value.rank || myRank.value.solvedRank || myRank.value.passRateRank || '--'
})

const formatRelativeTime = (value) => {
  if (!value) {
    return '暂无时间'
  }

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return '暂无时间'
  }

  const diff = Date.now() - date.getTime()
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour

  if (diff < hour) {
    return `${Math.max(1, Math.floor(diff / minute))} 分钟前`
  }

  if (diff < day) {
    return `${Math.floor(diff / hour)} 小时前`
  }

  return `${Math.floor(diff / day)} 天前`
}

const normalizeAchievement = (item, index) => ({
  id: item.id || item.achievementId || `${index}`,
  title: item.title || item.name || '学习成就',
  description: item.description || item.condition || '继续练习会逐步解锁更多成就'
})

const fetchFavorites = async () => {
  if (!userId.value) {
    return
  }

  const [problemRes, postRes] = await Promise.all([
    getFavorites(userId.value, 'PROBLEM'),
    getFavorites(userId.value, 'POST')
  ])

  favoriteProblems.value = problemRes?.code === 200 ? problemRes.data || [] : []
  favoritePosts.value = postRes?.code === 200 ? postRes.data || [] : []
}

const fetchMyPosts = async () => {
  const res = await getMyPosts()
  myPosts.value = res?.code === 200 ? res.data || [] : []
}

const fetchAchievements = async () => {
  if (!userId.value) {
    return
  }

  const res = await getUserAchievements(userId.value)
  achievements.value = res?.code === 200 ? (res.data || []).map(normalizeAchievement) : []
}

const fetchRank = async () => {
  const res = await getMyRank()
  myRank.value = res?.code === 200 ? res.data || null : null
}

const loadData = async () => {
  try {
    await Promise.all([fetchFavorites(), fetchMyPosts(), fetchAchievements(), fetchRank()])
  } catch (error) {
    console.error('加载收藏与扩展失败:', error)
    ElMessage.error('加载收藏与扩展失败')
  }
}

const removeFavoriteItem = async (targetId, targetType) => {
  try {
    await ElMessageBox.confirm('确定取消收藏吗？', '提示', { type: 'warning' })
    const res = await removeFavorite({ targetId, targetType })

    if (res?.code !== 200) {
      throw new Error(res?.msg || '取消收藏失败')
    }

    ElMessage.success('已取消收藏')
    await fetchFavorites()
  } catch (error) {
    if (error === 'cancel' || error?.message === 'cancel') {
      return
    }

    ElMessage.error(error.message || '取消收藏失败')
  }
}

const openProblem = (id) => {
  router.push(`/problem/${id}`)
}

const openPost = (id) => {
  router.push(`/community/post/${id}`)
}

const goProblems = () => {
  router.push('/problems')
}

const goCommunity = () => {
  router.push('/community')
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.collections-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.summary-card,
.panel-card {
  position: relative;
  overflow: hidden;
  border: 1px solid var(--border-color);
  border-radius: 18px;
  background: var(--bg-card);
  box-shadow: var(--shadow-sm);
}

.summary-card {
  padding: 18px 20px;
}

.summary-glow {
  position: absolute;
  top: -30px;
  right: -20px;
  width: 90px;
  height: 90px;
  border-radius: 999px;
  background: rgba(0, 209, 255, 0.12);
  filter: blur(10px);
}

.summary-label,
.section-kicker {
  color: var(--text-secondary);
  font-size: 13px;
}

.summary-value {
  margin-top: 8px;
  color: var(--text-primary);
  font-size: 28px;
  font-weight: 700;
}

.summary-hint {
  margin-top: 6px;
  color: var(--text-tertiary);
  font-size: 13px;
}

.content-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.panel-card {
  padding: 20px;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.panel-header h3 {
  margin: 4px 0 0;
  color: var(--text-primary);
  font-size: 20px;
}

.achievement-list,
.list-block {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.achievement-item,
.list-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 14px 16px;
  border: 1px solid var(--border-light);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.02);
}

.list-item {
  width: 100%;
  text-align: left;
  cursor: pointer;
  transition: transform var(--transition-fast), border-color var(--transition-fast), background var(--transition-fast);
}

.list-item:hover {
  transform: translateY(-1px);
  border-color: var(--border-strong);
  background: rgba(255, 255, 255, 0.04);
}

.achievements-panel {
  background:
    radial-gradient(circle at top left, rgba(245, 158, 11, 0.12), transparent 28%),
    var(--bg-card);
}

.achievement-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 46px;
  height: 46px;
  border-radius: 14px;
  color: var(--warning-color);
  background: var(--warning-light);
  font-size: 22px;
}

.achievement-copy,
.list-main {
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
  gap: 4px;
}

.achievement-copy strong,
.list-main strong {
  color: var(--text-primary);
  font-size: 15px;
}

.achievement-copy span,
.list-main span,
.item-meta {
  color: var(--text-secondary);
  font-size: 13px;
}

@media (max-width: 960px) {
  .summary-grid,
  .content-grid {
    grid-template-columns: 1fr;
  }
}
</style>
