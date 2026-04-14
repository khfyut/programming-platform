<template>
  <div class="community-page">
    <div class="community-container">
      <section class="page-hero">
        <div class="hero-copy">
          <p class="hero-kicker">Community</p>
          <h1 class="page-title">学习社区</h1>
          <p class="page-subtitle">把学习笔记、提问、讨论和经验分享放在一起：想公开分享就发到广场，只想沉淀给自己也可以私密记录。</p>
        </div>
        <div class="hero-actions">
          <el-button plain @click="refreshCommunity" :loading="loading">
            <el-icon><RefreshRight /></el-icon>
            刷新内容
          </el-button>
          <el-button type="primary" @click="showCreateDialog">
            <el-icon><Edit /></el-icon>
            发布学习动态
          </el-button>
        </div>
      </section>

      <section class="summary-grid">
        <article v-for="item in summaryCards" :key="item.label" class="summary-card">
          <span class="summary-label">{{ item.label }}</span>
          <strong class="summary-value">{{ item.value }}</strong>
          <span class="summary-hint">{{ item.hint }}</span>
        </article>
      </section>

      <div class="content-layout">
        <div class="main-content">
          <section class="toolbar-card">
            <div class="toolbar-head">
              <div>
                <h2 class="section-title">{{ feedMode === 'mine' ? '我的动态' : '社区广场' }}</h2>
                <p class="section-subtitle">
                  {{ feedMode === 'mine'
                    ? '这里会展示你发布过的公开动态和仅自己可见笔记。'
                    : '广场只展示公开内容，私密学习笔记不会出现在这里。' }}
                </p>
              </div>
            </div>

            <div class="toolbar-row feed-mode-row">
              <el-radio-group v-model="feedMode" size="small" @change="handleFeedModeChange">
                <el-radio-button value="public">社区广场</el-radio-button>
                <el-radio-button value="mine">我的动态</el-radio-button>
              </el-radio-group>
              <span class="mode-hint">{{ feedMode === 'mine' ? '公开 + 私密' : '仅公开' }}</span>
            </div>

            <div class="toolbar-row">
              <el-radio-group v-model="postType" size="small" @change="handleTypeChange">
                <el-radio-button value="">全部</el-radio-button>
                <el-radio-button value="note">学习笔记</el-radio-button>
                <el-radio-button value="discussion">讨论</el-radio-button>
                <el-radio-button value="question">问答</el-radio-button>
                <el-radio-button value="share">分享</el-radio-button>
              </el-radio-group>
              <el-button text @click="clearFilter" :disabled="!postType">清空筛选</el-button>
            </div>
          </section>

          <div class="posts-list" v-loading="loading">
            <article
              v-for="post in posts"
              :key="post.id"
              class="post-card"
              @click="goToPostDetail(post.id)"
            >
              <div class="post-header">
                <div class="post-author">
                  <el-avatar :size="42" :icon="UserFilled" />
                  <div class="author-info">
                    <span class="author-name">{{ post.username || '匿名用户' }}</span>
                    <div class="author-meta">
                      <span>{{ formatTime(post.createTime) }}</span>
                      <span>{{ getPostTypeText(post.type) }}</span>
                    </div>
                  </div>
                </div>
                <div class="post-badges">
                  <el-tag :type="getPostTypeTag(post.type)" size="small">
                    {{ getPostTypeText(post.type) }}
                  </el-tag>
                  <el-tag v-if="feedMode === 'mine'" :type="getVisibilityTag(post.visibility)" size="small" effect="plain">
                    {{ getVisibilityText(post.visibility) }}
                  </el-tag>
                </div>
              </div>

              <div class="post-content">
                <h3 class="post-title">{{ post.title || '未命名帖子' }}</h3>
                <p class="post-summary">{{ summarizeContent(post.content) }}</p>
                <div v-if="normalizeTags(post.tags).length" class="post-tags">
                  <el-tag
                    v-for="tag in normalizeTags(post.tags)"
                    :key="`${post.id}-${tag}`"
                    size="small"
                    effect="plain"
                  >
                    {{ tag }}
                  </el-tag>
                </div>
              </div>

              <div class="post-footer">
                <div class="post-stats">
                  <span class="stat-item">
                    <el-icon><View /></el-icon>
                    {{ Number(post.viewCount || 0) }}
                  </span>
                  <span class="stat-item">
                    <el-icon><ChatDotRound /></el-icon>
                    {{ Number(post.commentCount || 0) }}
                  </span>
                  <span class="stat-item">
                    <el-icon><Star /></el-icon>
                    {{ Number(post.likeCount || 0) }}
                  </span>
                </div>
                <span class="post-link">查看详情</span>
              </div>
            </article>

            <div v-if="posts.length === 0 && !loading" class="empty-posts">
              <el-empty :description="feedMode === 'mine' ? '你还没有发布学习动态' : '当前筛选下还没有公开动态'">
                <template #description>
                  <p class="empty-text">{{ feedMode === 'mine' ? '可以先记录一条仅自己可见的学习笔记。' : '可以先发布一个问题或经验分享，给社区开个头。' }}</p>
                </template>
                <el-button type="primary" @click="showCreateDialog">发布第一条学习动态</el-button>
              </el-empty>
            </div>
          </div>

          <div class="pagination-container" v-if="total > 0">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 30, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="total"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </div>

        <aside class="sidebar">
          <section class="sidebar-card">
            <div class="card-header">
              <h3 class="card-title">热门话题</h3>
              <span class="card-tip">看看最近大家在讨论什么</span>
            </div>
            <div v-if="hotTopics.length > 0" class="card-content">
              <div class="topic-list">
                <div
                  v-for="(topic, index) in hotTopics"
                  :key="`${topic.name}-${index}`"
                  class="topic-item"
                >
                  <span class="topic-rank" :class="`rank-${index + 1}`">{{ index + 1 }}</span>
                  <span class="topic-name">{{ topic.name }}</span>
                  <span class="topic-count">{{ topic.count }}</span>
                </div>
              </div>
            </div>
            <div v-else class="card-empty">暂无热门话题，等第一波讨论热起来后这里会更有意思。</div>
          </section>

          <section class="sidebar-card guide-card">
            <div class="card-header">
              <h3 class="card-title">发帖建议</h3>
              <span class="card-tip">让交流更容易得到回应</span>
            </div>
            <div class="card-content guide-list">
              <div class="guide-item">提问题时尽量带上题目名、报错或你的思路卡点。</div>
              <div class="guide-item">分享经验时可以补充适用场景，方便别人判断要不要跟进。</div>
              <div class="guide-item">如果是讨论帖，标题尽量具体，后续更容易沉淀有效回复。</div>
            </div>
          </section>
        </aside>
      </div>
    </div>

  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Edit,
  UserFilled,
  View,
  ChatDotRound,
  Star,
  RefreshRight
} from '@element-plus/icons-vue'
import {
  getPosts,
  getUserPosts,
  getCommunityStatistics,
  getHotTopics
} from '@/api/community'
import { markdownToPlainText } from '@/utils/markdown'

const router = useRouter()

const loading = ref(false)
const posts = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const postType = ref('')
const feedMode = ref('public')

const statistics = ref({
  totalPosts: 0,
  totalComments: 0,
  activeUsers: 0,
  todayPosts: 0
})

const hotTopics = ref([])

const summaryCards = computed(() => [
  {
    label: '公开动态数',
    value: Number(statistics.value.totalPosts || 0),
    hint: '社区广场目前沉淀的公开内容'
  },
  {
    label: '总评论数',
    value: Number(statistics.value.totalComments || 0),
    hint: '用来衡量讨论活跃度'
  },
  {
    label: '活跃用户',
    value: Number(statistics.value.activeUsers || 0),
    hint: '最近仍在参与交流的用户'
  },
  {
    label: '今日新动态',
    value: Number(statistics.value.todayPosts || 0),
    hint: '今天社区新增的公开内容'
  }
])

const normalizePostsResponse = (data) => {
  if (Array.isArray(data)) {
    return {
      list: data,
      total: data.length
    }
  }

  const list = data?.list || data?.content || data?.records || data?.rows || []
  const totalCount = Number(
    data?.total ??
      data?.totalElements ??
      data?.count ??
      (Array.isArray(list) ? list.length : 0)
  )

  return {
    list: Array.isArray(list) ? list : [],
    total: totalCount
  }
}

const normalizePost = (data = {}) => ({
  ...data,
  username: data.username || data.authorName || '',
  type: data.type || 'note',
  visibility: data.visibility || 'public',
  tags: data.tags || '',
  viewCount: Number(data.viewCount ?? data.views ?? 0),
  commentCount: Number(data.commentCount ?? data.comments ?? 0),
  likeCount: Number(data.likeCount ?? data.likes ?? 0)
})

const normalizeStatistics = (data = {}) => ({
  totalPosts: Number(data.totalPosts ?? data.postCount ?? 0),
  totalComments: Number(data.totalComments ?? data.commentCount ?? 0),
  activeUsers: Number(data.activeUsers ?? data.userCount ?? 0),
  todayPosts: Number(data.todayPosts ?? data.todayCount ?? 0)
})

const normalizeTopicList = (data) => {
  const list = Array.isArray(data) ? data : data?.list || data?.items || []
  return list
    .map((topic) => ({
      name: topic?.name || topic?.topic || topic?.label || '未命名话题',
      count: Number(topic?.count ?? topic?.postCount ?? topic?.heat ?? 0)
    }))
    .filter((topic) => topic.name)
}

const getPostTypeTag = (type) => {
  const types = {
    note: 'info',
    discussion: 'primary',
    question: 'warning',
    share: 'success'
  }
  return types[type] || 'info'
}

const getPostTypeText = (type) => {
  const texts = {
    note: '学习笔记',
    discussion: '讨论',
    question: '问答',
    share: '分享'
  }
  return texts[type] || '其他'
}

const getVisibilityText = (visibility) => (visibility === 'private' ? '仅自己可见' : '公开')

const getVisibilityTag = (visibility) => (visibility === 'private' ? 'warning' : 'success')

const normalizeTags = (tags) => {
  if (Array.isArray(tags)) {
    return tags.map((tag) => String(tag).trim()).filter(Boolean)
  }
  return String(tags || '')
    .split(/[,，]/)
    .map((tag) => tag.trim())
    .filter(Boolean)
}

const summarizeContent = (content) => {
  const plainText = markdownToPlainText(content)

  if (!plainText) {
    return '这条帖子暂时还没有正文摘要。'
  }

  return plainText.length > 120 ? `${plainText.slice(0, 120)}...` : plainText
}

const formatTime = (time) => {
  if (!time) return '未知'
  const date = new Date(time)
  if (Number.isNaN(date.getTime())) return '未知'

  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)} 天前`
  return date.toLocaleDateString('zh-CN')
}

const fetchPosts = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }

    if (postType.value) {
      params.type = postType.value
    }

    const res = feedMode.value === 'mine'
      ? await getUserPosts(params)
      : await getPosts(params)
    if (res.code === 200) {
      const normalized = normalizePostsResponse(res.data)
      posts.value = normalized.list.map(normalizePost)
      total.value = normalized.total
      return
    }

    posts.value = []
    total.value = 0
  } catch (error) {
    console.error('获取学习动态列表失败:', error)
    posts.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const fetchStatistics = async () => {
  try {
    const res = await getCommunityStatistics()
    if (res.code === 200) {
      statistics.value = normalizeStatistics(res.data)
      return
    }

    statistics.value = normalizeStatistics()
  } catch (error) {
    console.error('获取社区统计失败:', error)
    statistics.value = normalizeStatistics()
  }
}

const fetchHotTopics = async () => {
  try {
    const res = await getHotTopics()
    if (res.code === 200) {
      hotTopics.value = normalizeTopicList(res.data)
      return
    }

    hotTopics.value = []
  } catch (error) {
    console.error('获取热门话题失败:', error)
    hotTopics.value = []
  }
}

const refreshCommunity = async () => {
  await Promise.all([fetchPosts(), fetchStatistics(), fetchHotTopics()])
}

const clearFilter = async () => {
  if (!postType.value) return
  postType.value = ''
  currentPage.value = 1
  await fetchPosts()
}

const showCreateDialog = () => {
  router.push('/community/write')
}

const handleTypeChange = async () => {
  currentPage.value = 1
  await fetchPosts()
}

const handleFeedModeChange = async () => {
  currentPage.value = 1
  await fetchPosts()
}

const handleSizeChange = async (size) => {
  pageSize.value = size
  currentPage.value = 1
  await fetchPosts()
}

const handleCurrentChange = async (page) => {
  currentPage.value = page
  await fetchPosts()
}

const goToPostDetail = (postId) => {
  if (!postId) return
  router.push(`/community/post/${postId}`)
}

onMounted(() => {
  refreshCommunity()
})
</script>

<style scoped>
.community-page {
  min-height: 100vh;
  padding: 24px;
  background:
    radial-gradient(circle at top left, rgba(0, 102, 255, 0.05), transparent 24%),
    var(--leetcode-bg-secondary, #f7f8fa);
}

.community-container {
  max-width: 1200px;
  margin: 0 auto;
}

.page-hero {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  padding: 28px;
  margin-bottom: 24px;
  border: 1px solid rgba(0, 102, 255, 0.12);
  border-radius: 24px;
  background:
    radial-gradient(circle at top right, rgba(0, 102, 255, 0.12), transparent 30%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(255, 255, 255, 0.92));
  box-shadow: 0 18px 50px rgba(15, 23, 42, 0.05);
}

.hero-kicker {
  margin: 0 0 10px;
  color: #1668dc;
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.page-title {
  margin: 0 0 8px;
  font-size: 30px;
  font-weight: 700;
  color: var(--leetcode-text, #24292f);
}

.page-subtitle {
  max-width: 640px;
  margin: 0;
  font-size: 14px;
  line-height: 1.8;
  color: var(--leetcode-text-secondary, #6b7280);
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: flex-start;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.summary-card {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 18px 20px;
  border: 1px solid var(--leetcode-border, #e5e7eb);
  border-radius: 18px;
  background: var(--leetcode-bg, #ffffff);
}

.summary-label {
  color: var(--leetcode-text-secondary, #6b7280);
  font-size: 13px;
}

.summary-value {
  color: var(--leetcode-text, #24292f);
  font-size: 28px;
  line-height: 1.1;
}

.summary-hint {
  color: var(--leetcode-text-secondary, #6b7280);
  font-size: 12px;
  line-height: 1.6;
}

.content-layout {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
}

.main-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.toolbar-card,
.sidebar-card {
  border: 1px solid var(--leetcode-border, #e5e7eb);
  border-radius: 18px;
  background: var(--leetcode-bg, #ffffff);
}

.toolbar-card {
  padding: 20px;
}

.toolbar-head {
  margin-bottom: 16px;
}

.section-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--leetcode-text, #24292f);
}

.section-subtitle {
  margin: 8px 0 0;
  color: var(--leetcode-text-secondary, #6b7280);
  font-size: 13px;
  line-height: 1.7;
}

.toolbar-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.feed-mode-row {
  justify-content: flex-start;
  margin-bottom: 12px;
}

.mode-hint {
  color: var(--leetcode-text-secondary, #6b7280);
  font-size: 12px;
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-card {
  padding: 20px;
  border: 1px solid var(--leetcode-border, #e5e7eb);
  border-radius: 18px;
  background: var(--leetcode-bg, #ffffff);
  cursor: pointer;
  transition: all 0.2s ease;
}

.post-card:hover {
  border-color: var(--leetcode-primary, #0066ff);
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(0, 102, 255, 0.08);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;
}

.post-badges {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  flex-wrap: wrap;
}

.post-author {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.author-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.author-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--leetcode-text, #24292f);
}

.author-meta {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  color: var(--leetcode-text-secondary, #6b7280);
  font-size: 12px;
}

.post-title {
  margin: 0 0 10px;
  font-size: 18px;
  font-weight: 600;
  color: var(--leetcode-text, #24292f);
}

.post-summary {
  margin: 0;
  color: var(--leetcode-text-secondary, #6b7280);
  line-height: 1.8;
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-top: 18px;
}

.post-stats {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.stat-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--leetcode-text-secondary, #6b7280);
}

.post-link {
  color: #1668dc;
  font-size: 13px;
  font-weight: 600;
}

.empty-posts {
  padding: 50px 20px;
  border: 1px dashed var(--leetcode-border, #e5e7eb);
  border-radius: 18px;
  background: var(--leetcode-bg, #ffffff);
}

.empty-text {
  margin: 0;
  line-height: 1.7;
}

.pagination-container {
  display: flex;
  justify-content: center;
  padding: 8px 0 4px;
}

.sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.card-header {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 18px 20px;
  border-bottom: 1px solid var(--leetcode-border, #e5e7eb);
}

.card-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: var(--leetcode-text, #24292f);
}

.card-tip {
  color: var(--leetcode-text-secondary, #6b7280);
  font-size: 12px;
}

.card-content {
  padding: 18px 20px;
}

.card-empty {
  padding: 18px 20px;
  color: var(--leetcode-text-secondary, #6b7280);
  line-height: 1.7;
}

.topic-list,
.guide-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.topic-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.topic-rank {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: var(--leetcode-bg-secondary, #f7f8fa);
  color: var(--leetcode-text-secondary, #6b7280);
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.topic-rank.rank-1 {
  background: linear-gradient(135deg, #ffd700 0%, #ffa500 100%);
  color: white;
}

.topic-rank.rank-2 {
  background: linear-gradient(135deg, #c0c0c0 0%, #a9a9a9 100%);
  color: white;
}

.topic-rank.rank-3 {
  background: linear-gradient(135deg, #cd7f32 0%, #b8860b 100%);
  color: white;
}

.topic-name {
  flex: 1;
  color: var(--leetcode-text, #24292f);
  font-size: 14px;
}

.topic-count {
  color: var(--leetcode-text-secondary, #6b7280);
  font-size: 13px;
}

.guide-card {
  background:
    linear-gradient(180deg, rgba(0, 102, 255, 0.03), transparent 60%),
    var(--leetcode-bg, #ffffff);
}

.guide-item {
  padding: 12px 14px;
  border: 1px solid rgba(0, 102, 255, 0.08);
  border-radius: 14px;
  background: rgba(0, 102, 255, 0.04);
  color: var(--leetcode-text-secondary, #6b7280);
  line-height: 1.7;
}

@media (max-width: 1100px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .content-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .community-page {
    padding: 16px;
  }

  .page-hero,
  .post-header,
  .post-footer {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 560px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
