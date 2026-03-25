<template>
  <div class="community-page">
    <div class="community-container">
      <div class="page-header">
        <div class="header-left">
          <h1 class="page-title">学习社区</h1>
          <p class="page-subtitle">与其他学习者一起交流、分享和成长</p>
        </div>
        <div class="header-right">
          <el-button type="primary" @click="showCreateDialog">
            <el-icon><Edit /></el-icon>
            发布帖子
          </el-button>
        </div>
      </div>

      <div class="content-layout">
        <div class="main-content">
          <div class="filter-section">
            <el-radio-group v-model="postType" size="small" @change="handleTypeChange">
              <el-radio-button value="">全部</el-radio-button>
              <el-radio-button value="discussion">讨论</el-radio-button>
              <el-radio-button value="question">问答</el-radio-button>
              <el-radio-button value="share">分享</el-radio-button>
            </el-radio-group>
          </div>

          <div class="posts-list" v-loading="loading">
            <div 
              v-for="post in posts" 
              :key="post.id"
              class="post-card"
              @click="goToPostDetail(post.id)"
            >
              <div class="post-header">
                <div class="post-author">
                  <el-avatar :size="40" :icon="UserFilled" />
                  <div class="author-info">
                    <span class="author-name">{{ post.username || '匿名用户' }}</span>
                    <span class="post-time">{{ formatTime(post.createTime) }}</span>
                  </div>
                </div>
                <el-tag :type="getPostTypeTag(post.type)" size="small">
                  {{ getPostTypeText(post.type) }}
                </el-tag>
              </div>

              <div class="post-content">
                <h3 class="post-title">{{ post.title }}</h3>
                <p class="post-summary">{{ post.content }}</p>
              </div>

              <div class="post-footer">
                <div class="post-stats">
                  <span class="stat-item">
                    <el-icon><View /></el-icon>
                    {{ post.viewCount || 0 }}
                  </span>
                  <span class="stat-item">
                    <el-icon><ChatDotRound /></el-icon>
                    {{ post.commentCount || 0 }}
                  </span>
                  <span class="stat-item">
                    <el-icon><Star /></el-icon>
                    {{ post.likeCount || 0 }}
                  </span>
                </div>
              </div>
            </div>

            <div v-if="posts.length === 0 && !loading" class="empty-posts">
              <el-empty description="暂无帖子" />
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

        <div class="sidebar">
          <div class="sidebar-card">
            <div class="card-header">
              <h3 class="card-title">社区统计</h3>
            </div>
            <div class="card-content">
              <div class="stat-row">
                <span class="stat-label">总帖子数</span>
                <span class="stat-value">{{ statistics.totalPosts || 0 }}</span>
              </div>
              <div class="stat-row">
                <span class="stat-label">总评论数</span>
                <span class="stat-value">{{ statistics.totalComments || 0 }}</span>
              </div>
              <div class="stat-row">
                <span class="stat-label">活跃用户</span>
                <span class="stat-value">{{ statistics.activeUsers || 0 }}</span>
              </div>
              <div class="stat-row">
                <span class="stat-label">今日帖子</span>
                <span class="stat-value">{{ statistics.todayPosts || 0 }}</span>
              </div>
            </div>
          </div>

          <div class="sidebar-card">
            <div class="card-header">
              <h3 class="card-title">热门话题</h3>
            </div>
            <div class="card-content">
              <div class="topic-list">
                <div 
                  v-for="(topic, index) in hotTopics" 
                  :key="index"
                  class="topic-item"
                >
                  <span class="topic-rank" :class="'rank-' + (index + 1)">{{ index + 1 }}</span>
                  <span class="topic-name">{{ topic.name }}</span>
                  <span class="topic-count">{{ topic.count }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog
      v-model="createDialogVisible"
      title="发布帖子"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="newPost" label-width="80px">
        <el-form-item label="类型">
          <el-select v-model="newPost.type" placeholder="请选择帖子类型">
            <el-option label="讨论" value="discussion" />
            <el-option label="问答" value="question" />
            <el-option label="分享" value="share" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="newPost.title" placeholder="请输入标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="内容">
          <el-input
            v-model="newPost.content"
            type="textarea"
            :rows="8"
            placeholder="请输入内容"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreatePost" :loading="submitting">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Edit,
  UserFilled,
  View,
  ChatDotRound,
  Star
} from '@element-plus/icons-vue'
import {
  getPosts,
  createPost,
  getCommunityStatistics,
  getHotTopics
} from '@/api/community'

const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const posts = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const postType = ref('')
const createDialogVisible = ref(false)

const newPost = ref({
  type: 'discussion',
  title: '',
  content: ''
})

const statistics = ref({
  totalPosts: 0,
  totalComments: 0,
  activeUsers: 0
})

const hotTopics = ref([])

const getPostTypeTag = (type) => {
  const types = {
    'discussion': 'primary',
    'question': 'warning',
    'share': 'success'
  }
  return types[type] || 'info'
}

const getPostTypeText = (type) => {
  const texts = {
    'discussion': '讨论',
    'question': '问答',
    'share': '分享'
  }
  return texts[type] || '其他'
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
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  return date.toLocaleDateString()
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
    const res = await getPosts(params)
    if (res.code === 200) {
      posts.value = res.data || []
      total.value = res.data?.length || 0
    }
  } catch (error) {
    console.error('获取帖子列表失败:', error)
    posts.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const generateMockPosts = () => {
  posts.value = [
    {
      id: 1,
      title: '如何高效学习动态规划？',
      content: '最近在学习动态规划，感觉有点吃力，有没有什么好的学习方法推荐？',
      type: 'question',
      username: '学习者A',
      createTime: new Date(Date.now() - 3600000),
      viewCount: 156,
      commentCount: 23,
      likeCount: 45
    },
    {
      id: 2,
      title: '分享我的Java学习笔记',
      content: '整理了一份Java基础学习笔记，包含了变量、数据类型、面向对象等核心知识点...',
      type: 'share',
      username: '编程爱好者',
      createTime: new Date(Date.now() - 7200000),
      viewCount: 342,
      commentCount: 56,
      likeCount: 128
    },
    {
      id: 3,
      title: '算法学习路线讨论',
      content: '大家都是怎么学习算法的？有没有推荐的学习路线和资源？',
      type: 'discussion',
      username: '算法小白',
      createTime: new Date(Date.now() - 86400000),
      viewCount: 289,
      commentCount: 78,
      likeCount: 96
    }
  ]
  total.value = posts.value.length
}

const fetchStatistics = async () => {
  try {
    const res = await getCommunityStatistics()
    if (res.code === 200) {
      statistics.value = res.data || {
        totalPosts: 0,
        totalComments: 0,
        activeUsers: 0
      }
    }
  } catch (error) {
    console.error('获取社区统计失败:', error)
    statistics.value = {
      totalPosts: 0,
      totalComments: 0,
      activeUsers: 0,
      todayPosts: 0
    }
  }
}

const fetchHotTopics = async () => {
  try {
    const res = await getHotTopics()
    if (res.code === 200) {
      hotTopics.value = res.data || []
    }
  } catch (error) {
    console.error('获取热点话题失败:', error)
    hotTopics.value = []
  }
}

const showCreateDialog = () => {
  newPost.value = {
    type: 'discussion',
    title: '',
    content: ''
  }
  createDialogVisible.value = true
}

const handleCreatePost = async () => {
  if (!newPost.value.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  if (!newPost.value.content.trim()) {
    ElMessage.warning('请输入内容')
    return
  }

  submitting.value = true
  try {
    const res = await createPost(newPost.value)
    if (res.code === 200) {
      ElMessage.success('发布成功')
      createDialogVisible.value = false
      fetchPosts()
    } else {
      ElMessage.error(res.msg || '发布失败')
    }
  } catch (error) {
    console.error('发布帖子失败:', error)
    ElMessage.error('发布失败，请重试')
  } finally {
    submitting.value = false
  }
}

const handleTypeChange = () => {
  currentPage.value = 1
  fetchPosts()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchPosts()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchPosts()
}

const goToPostDetail = (postId) => {
  router.push(`/community/post/${postId}`)
}

onMounted(() => {
  fetchPosts()
  fetchStatistics()
  fetchHotTopics()
})
</script>

<style scoped>
.community-page {
  min-height: 100vh;
  background: var(--leetcode-bg-secondary, #F7F8FA);
  padding: 24px;
}

.community-container {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
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

.content-layout {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 24px;
}

.main-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.filter-section {
  background: var(--leetcode-bg, #FFFFFF);
  border-radius: 12px;
  padding: 16px 20px;
  border: 1px solid var(--leetcode-border, #E5E7EB);
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-card {
  background: var(--leetcode-bg, #FFFFFF);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid var(--leetcode-border, #E5E7EB);
  cursor: pointer;
  transition: all 0.2s ease;
}

.post-card:hover {
  border-color: var(--leetcode-primary, #0066FF);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 102, 255, 0.1);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.post-author {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.author-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--leetcode-text, #24292F);
}

.post-time {
  font-size: 12px;
  color: var(--leetcode-text-secondary, #6B7280);
}

.post-content {
  margin-bottom: 16px;
}

.post-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--leetcode-text, #24292F);
  margin: 0 0 8px 0;
}

.post-summary {
  font-size: 14px;
  color: var(--leetcode-text-secondary, #6B7280);
  margin: 0;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.post-stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--leetcode-text-secondary, #6B7280);
}

.sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.sidebar-card {
  background: var(--leetcode-bg, #FFFFFF);
  border-radius: 12px;
  border: 1px solid var(--leetcode-border, #E5E7EB);
  overflow: hidden;
}

.card-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--leetcode-border, #E5E7EB);
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--leetcode-text, #24292F);
  margin: 0;
}

.card-content {
  padding: 16px 20px;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.stat-label {
  font-size: 14px;
  color: var(--leetcode-text-secondary, #6B7280);
}

.stat-value {
  font-size: 16px;
  font-weight: 600;
  color: var(--leetcode-text, #24292F);
}

.topic-list {
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
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: var(--leetcode-bg-secondary, #F7F8FA);
  color: var(--leetcode-text-secondary, #6B7280);
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.topic-rank.rank-1 {
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  color: white;
}

.topic-rank.rank-2 {
  background: linear-gradient(135deg, #C0C0C0 0%, #A9A9A9 100%);
  color: white;
}

.topic-rank.rank-3 {
  background: linear-gradient(135deg, #CD7F32 0%, #B8860B 100%);
  color: white;
}

.topic-name {
  flex: 1;
  font-size: 14px;
  color: var(--leetcode-text, #24292F);
}

.topic-count {
  font-size: 13px;
  color: var(--leetcode-text-secondary, #6B7280);
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.empty-posts {
  padding: 60px 20px;
}

@media (max-width: 1024px) {
  .content-layout {
    grid-template-columns: 1fr;
  }

  .sidebar {
    order: -1;
  }
}

@media (max-width: 768px) {
  .community-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    gap: 16px;
  }

  .page-title {
    font-size: 24px;
  }
}
</style>
