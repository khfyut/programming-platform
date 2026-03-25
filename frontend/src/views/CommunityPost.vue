<template>
  <div class="post-detail-page">
    <div class="post-container">
      <div class="page-header">
        <div class="header-left">
          <el-button @click="goBack" text>
            <el-icon><ArrowLeft /></el-icon>
            返回社区
          </el-button>
        </div>
      </div>

      <div v-if="post" class="post-content-section">
        <div class="post-card">
          <div class="post-header">
            <div class="post-author">
              <el-avatar :size="48" :icon="UserFilled" />
              <div class="author-info">
                <span class="author-name">{{ post.username || '匿名用户' }}</span>
                <span class="post-time">{{ formatTime(post.createTime) }}</span>
              </div>
            </div>
            <el-tag :type="getPostTypeTag(post.type)" size="small">
              {{ getPostTypeText(post.type) }}
            </el-tag>
          </div>

          <h1 class="post-title">{{ post.title }}</h1>

          <div class="post-body">{{ post.content }}</div>

          <div class="post-footer">
            <div class="post-stats">
              <span class="stat-item">
                <el-icon><View /></el-icon>
                {{ post.viewCount || 0 }} 浏览
              </span>
              <span class="stat-item">
                <el-icon><ChatDotRound /></el-icon>
                {{ post.commentCount || 0 }} 评论
              </span>
            </div>
            <div class="post-actions">
              <el-button 
                :icon="Star" 
                @click="handleLike" 
                :type="liked ? 'primary' : 'default'"
              >
                {{ post.likeCount || 0 }}
              </el-button>
            </div>
          </div>
        </div>

        <div class="comments-section">
          <div class="section-header">
            <h2 class="section-title">评论 ({{ comments.length }})</h2>
          </div>

          <div class="comment-form">
            <el-input
              v-model="newComment"
              type="textarea"
              :rows="3"
              placeholder="写下你的评论..."
              maxlength="500"
              show-word-limit
            />
            <div class="form-actions">
              <el-button type="primary" @click="handleAddComment" :loading="submitting">
                发布评论
              </el-button>
            </div>
          </div>

          <div class="comments-list" v-loading="commentsLoading">
            <div 
              v-for="comment in comments" 
              :key="comment.id"
              class="comment-item"
            >
              <div class="comment-header">
                <el-avatar :size="36" :icon="UserFilled" />
                <div class="comment-author-info">
                  <span class="comment-author">{{ comment.username || '匿名用户' }}</span>
                  <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
                </div>
              </div>
              <div class="comment-content">{{ comment.content }}</div>
              <div class="comment-footer">
                <el-button text size="small" @click="handleLikeComment(comment.id)">
                  <el-icon><Star /></el-icon>
                  <span>{{ comment.likeCount || 0 }}</span>
                </el-button>
              </div>
            </div>

            <div v-if="comments.length === 0 && !commentsLoading" class="empty-comments">
              <el-empty description="暂无评论，快来抢沙发吧！" />
            </div>
          </div>
        </div>
      </div>

      <div v-else class="loading-container">
        <el-empty description="加载中..." />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft,
  UserFilled,
  View,
  ChatDotRound,
  Star
} from '@element-plus/icons-vue'
import {
  getPostById,
  getCommentsByPostId,
  createComment,
  likePost,
  likeComment
} from '@/api/community'

const route = useRoute()
const router = useRouter()

const post = ref(null)
const comments = ref([])
const newComment = ref('')
const liked = ref(false)
const loading = ref(false)
const commentsLoading = ref(false)
const submitting = ref(false)

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

const fetchPost = async () => {
  loading.value = true
  try {
    const res = await getPostById(route.params.id)
    if (res.code === 200) {
      post.value = res.data
    }
  } catch (error) {
    console.error('获取帖子详情失败:', error)
    generateMockPost()
  } finally {
    loading.value = false
  }
}

const generateMockPost = () => {
  post.value = {
    id: route.params.id,
    title: '如何高效学习动态规划？',
    content: '最近在学习动态规划，感觉有点吃力，有没有什么好的学习方法推荐？\n\n我目前的学习进度：\n1. 已经掌握了基本的递归思想\n2. 会做一些简单的DP题目\n3. 但遇到中等难度的题目就有点卡壳了\n\n希望大家能分享一些学习经验和资源！',
    type: 'question',
    username: '学习者A',
    createTime: new Date(Date.now() - 3600000),
    viewCount: 156,
    commentCount: 23,
    likeCount: 45
  }
}

const fetchComments = async () => {
  commentsLoading.value = true
  try {
    const res = await getCommentsByPostId(route.params.id)
    if (res.code === 200) {
      comments.value = res.data || []
    }
  } catch (error) {
    console.error('获取评论失败:', error)
    generateMockComments()
  } finally {
    commentsLoading.value = false
  }
}

const generateMockComments = () => {
  comments.value = [
    {
      id: 1,
      content: '我推荐先从基础开始，先理解状态转移方程的意义，而不是死记硬背！',
      username: '算法达人',
      createTime: new Date(Date.now() - 1800000),
      likeCount: 12
    },
    {
      id: 2,
      content: '可以试试LeetCode上的动态规划专题，按照tag为dp的题目从简单到困难刷一遍，进步很快的。',
      username: '刷题狂人',
      createTime: new Date(Date.now() - 3600000),
      likeCount: 8
    }
  ]
}

const handleLike = async () => {
  try {
    await likePost(post.value.id)
    liked.value = !liked.value
    if (liked.value) {
      post.value.likeCount = (post.value.likeCount || 0) + 1
    } else {
      post.value.likeCount = Math.max(0, (post.value.likeCount || 0) - 1)
    }
    ElMessage.success(liked.value ? '点赞成功' : '取消点赞')
  } catch (error) {
    console.error('点赞失败:', error)
  }
}

const handleAddComment = async () => {
  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  submitting.value = true
  try {
    const res = await createComment(post.value.id, {
      content: newComment.value
    })
    if (res.code === 200) {
      ElMessage.success('评论发布成功')
      newComment.value = ''
      fetchComments()
    } else {
      ElMessage.error(res.msg || '评论发布失败')
    }
  } catch (error) {
    console.error('发布评论失败:', error)
    ElMessage.error('评论发布失败，请重试')
  } finally {
    submitting.value = false
  }
}

const handleLikeComment = async (commentId) => {
  try {
    await likeComment(commentId)
    const comment = comments.value.find(c => c.id === commentId)
    if (comment) {
      comment.likeCount = (comment.likeCount || 0) + 1
    }
    ElMessage.success('点赞成功')
  } catch (error) {
    console.error('点赞失败:', error)
  }
}

const goBack = () => {
  router.push('/community')
}

onMounted(() => {
  fetchPost()
  fetchComments()
})
</script>

<style scoped>
.post-detail-page {
  min-height: 100vh;
  background: var(--leetcode-bg-secondary, #F7F8FA);
  padding: 24px;
}

.post-container {
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.post-content-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.post-card {
  background: var(--leetcode-bg, #FFFFFF);
  border-radius: 12px;
  padding: 24px;
  border: 1px solid var(--leetcode-border, #E5E7EB);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
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
  font-size: 15px;
  font-weight: 500;
  color: var(--leetcode-text, #24292F);
}

.post-time {
  font-size: 13px;
  color: var(--leetcode-text-secondary, #6B7280);
}

.post-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--leetcode-text, #24292F);
  margin: 0 0 20px 0;
  line-height: 1.4;
}

.post-body {
  font-size: 15px;
  color: var(--leetcode-text, #24292F);
  line-height: 1.8;
  white-space: pre-wrap;
  margin-bottom: 20px;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20px;
  border-top: 1px solid var(--leetcode-border, #E5E7EB);
}

.post-stats {
  display: flex;
  gap: 24px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--leetcode-text-secondary, #6B7280);
}

.post-actions {
  display: flex;
  gap: 12px;
}

.comments-section {
  background: var(--leetcode-bg, #FFFFFF);
  border-radius: 12px;
  padding: 24px;
  border: 1px solid var(--leetcode-border, #E5E7EB);
}

.section-header {
  margin-bottom: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--leetcode-text, #24292F);
  margin: 0;
}

.comment-form {
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--leetcode-border, #E5E7EB);
}

.form-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  padding: 16px;
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border-radius: 8px;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.comment-author-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.comment-author {
  font-size: 14px;
  font-weight: 500;
  color: var(--leetcode-text, #24292F);
}

.comment-time {
  font-size: 12px;
  color: var(--leetcode-text-secondary, #6B7280);
}

.comment-content {
  font-size: 14px;
  color: var(--leetcode-text, #24292F);
  line-height: 1.6;
  margin-bottom: 12px;
}

.comment-footer {
  display: flex;
  align-items: center;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.empty-comments {
  padding: 40px 20px;
}

@media (max-width: 768px) {
  .post-detail-page {
    padding: 16px;
  }

  .post-title {
    font-size: 20px;
  }

  .post-card {
    padding: 20px;
  }

  .comments-section {
    padding: 20px;
  }
}
</style>
