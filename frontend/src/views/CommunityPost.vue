<template>
  <div class="post-detail-page">
    <div class="post-container">
      <div class="page-header">
        <el-button @click="goBack" text>
          <el-icon><ArrowLeft /></el-icon>
          返回社区
        </el-button>
        <el-button text @click="refreshPage" :loading="loading || commentsLoading">
          <el-icon><RefreshRight /></el-icon>
          刷新
        </el-button>
      </div>

      <div v-if="post" class="post-content-section">
        <article class="post-card">
          <div class="post-header">
            <div class="post-author">
              <el-avatar :size="48" :icon="UserFilled" />
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
              <el-tag :type="getVisibilityTag(post.visibility)" size="small" effect="plain">
                {{ getVisibilityText(post.visibility) }}
              </el-tag>
            </div>
          </div>

          <h1 class="post-title">{{ post.title || '未命名帖子' }}</h1>

          <div v-if="post.content" class="post-body markdown-body" v-html="renderMarkdown(post.content)"></div>
          <div v-else class="post-body">帖子暂时还没有正文内容。</div>

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

          <div class="post-footer">
            <div class="post-stats">
              <span class="stat-item">
                <el-icon><View /></el-icon>
                {{ Number(post.viewCount || 0) }} 浏览
              </span>
              <span class="stat-item">
                <el-icon><ChatDotRound /></el-icon>
                {{ Number(commentCount) }} 评论
              </span>
              <span class="stat-item">
                <el-icon><Star /></el-icon>
                {{ Number(post.likeCount || 0) }} 点赞
              </span>
            </div>
            <div class="post-actions">
              <el-button v-if="isOwner" plain @click="goToEdit">
                编辑
              </el-button>
              <el-button
                :icon="Star"
                @click="handleLike"
                :type="liked ? 'primary' : 'default'"
              >
                {{ liked ? '已点赞' : '点赞' }}
              </el-button>
            </div>
          </div>
        </article>

        <section class="comments-section">
          <div class="section-header">
            <div>
              <h2 class="section-title">评论区</h2>
              <p class="section-subtitle">如果你有更具体的做法、补充信息或反例，现在就可以接着讨论。</p>
            </div>
            <span class="comment-count">{{ commentCount }} 条评论</span>
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
            <article
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
              <div class="comment-content">{{ comment.content || '这条评论暂时没有内容。' }}</div>
              <div class="comment-footer">
                <el-button text size="small" @click="handleLikeComment(comment.id)">
                  <el-icon><Star /></el-icon>
                  <span>{{ Number(comment.likeCount || 0) }}</span>
                </el-button>
              </div>
            </article>

            <div v-if="comments.length === 0 && !commentsLoading" class="empty-comments">
              <el-empty description="还没有评论">
                <template #description>
                  <p class="empty-text">如果你也在关注这个问题，可以先留下你的思路或补充信息。</p>
                </template>
                <el-button type="primary" @click="focusCommentInput">写第一条评论</el-button>
              </el-empty>
            </div>
          </div>
        </section>
      </div>

      <div v-else class="loading-container">
        <el-empty description="帖子不存在或加载失败">
          <el-button type="primary" @click="goBack">返回社区</el-button>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { renderMarkdown } from '@/utils/markdown'
import {
  ArrowLeft,
  UserFilled,
  View,
  ChatDotRound,
  Star,
  RefreshRight
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
const userStore = useUserStore()

const post = ref(null)
const comments = ref([])
const newComment = ref('')
const liked = ref(false)
const loading = ref(false)
const commentsLoading = ref(false)
const submitting = ref(false)

const commentCount = computed(() => {
  const value = Number(post.value?.commentCount)
  if (Number.isFinite(value) && value > 0) {
    return value
  }
  return comments.value.length
})

const isOwner = computed(() => String(post.value?.userId || '') === String(userStore.userInfo?.id || ''))

const normalizePost = (data = {}) => ({
  ...data,
  id: data.id ?? route.params.id,
  title: data.title || '',
  content: data.content || '',
  username: data.username || data.authorName || '',
  type: data.type || 'note',
  visibility: data.visibility || 'public',
  tags: data.tags || '',
  createTime: data.createTime || data.updateTime || '',
  viewCount: Number(data.viewCount ?? data.views ?? 0),
  commentCount: Number(data.commentCount ?? data.comments ?? 0),
  likeCount: Number(data.likeCount ?? data.likes ?? 0)
})

const normalizeComments = (data) => {
  const list = Array.isArray(data) ? data : data?.list || data?.records || []
  return list.map((comment, index) => ({
    ...comment,
    id: comment.id ?? `${route.params.id}-${index}`,
    username: comment.username || comment.authorName || '',
    content: comment.content || '',
    createTime: comment.createTime || comment.updateTime || '',
    likeCount: Number(comment.likeCount || 0)
  }))
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

const formatTime = (time) => {
  if (!time) return '未知'
  const date = new Date(time)
  if (Number.isNaN(date.getTime())) return '未知'

  const diff = Date.now() - date.getTime()

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)} 天前`

  return date.toLocaleDateString('zh-CN')
}

const fetchPost = async () => {
  loading.value = true
  try {
    const res = await getPostById(route.params.id)
    if (res.code === 200 && res.data) {
      post.value = normalizePost(res.data)
      return
    }

    post.value = null
  } catch (error) {
    console.error('获取帖子详情失败:', error)
    post.value = null
  } finally {
    loading.value = false
  }
}

const fetchComments = async () => {
  commentsLoading.value = true
  try {
    const res = await getCommentsByPostId(route.params.id)
    if (res.code === 200) {
      comments.value = normalizeComments(res.data)
      return
    }

    comments.value = []
  } catch (error) {
    console.error('获取评论失败:', error)
    comments.value = []
  } finally {
    commentsLoading.value = false
  }
}

const refreshPage = async () => {
  await Promise.all([fetchPost(), fetchComments()])
}

const handleLike = async () => {
  if (!post.value?.id) return

  try {
    await likePost(post.value.id)
    liked.value = !liked.value
    post.value.likeCount = liked.value
      ? Number(post.value.likeCount || 0) + 1
      : Math.max(0, Number(post.value.likeCount || 0) - 1)
    ElMessage.success(liked.value ? '点赞成功' : '已取消点赞')
  } catch (error) {
    console.error('点赞失败:', error)
    ElMessage.error('点赞失败，请稍后重试')
  }
}

const handleAddComment = async () => {
  if (!post.value?.id) return

  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  submitting.value = true
  try {
    const res = await createComment(post.value.id, {
      content: newComment.value.trim()
    })

    if (res.code === 200) {
      ElMessage.success('评论发布成功')
      newComment.value = ''
      await fetchComments()
      post.value.commentCount = comments.value.length
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
  if (!commentId) return

  try {
    await likeComment(commentId)
    const target = comments.value.find((comment) => comment.id === commentId)
    if (target) {
      target.likeCount = Number(target.likeCount || 0) + 1
    }
    ElMessage.success('评论点赞成功')
  } catch (error) {
    console.error('评论点赞失败:', error)
    ElMessage.error('评论点赞失败，请稍后重试')
  }
}

const focusCommentInput = async () => {
  await nextTick()
  const textarea = document.querySelector('.comment-form textarea')
  textarea?.focus()
}

const goBack = () => {
  router.push('/community')
}

const goToEdit = () => {
  if (!post.value?.id) return
  router.push(`/community/write/${post.value.id}`)
}

onMounted(() => {
  refreshPage()
})
</script>

<style scoped>
.post-detail-page {
  min-height: 100vh;
  padding: 24px;
  background:
    radial-gradient(circle at top left, rgba(0, 102, 255, 0.05), transparent 22%),
    var(--leetcode-bg-secondary, #f7f8fa);
}

.post-container {
  max-width: 920px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.post-content-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.post-card,
.comments-section {
  border: 1px solid var(--leetcode-border, #e5e7eb);
  border-radius: 22px;
  background: var(--leetcode-bg, #ffffff);
  box-shadow: 0 18px 44px rgba(15, 23, 42, 0.04);
}

.post-card {
  padding: 26px;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 20px;
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
}

.author-name {
  color: var(--leetcode-text, #24292f);
  font-size: 15px;
  font-weight: 600;
}

.author-meta {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  color: var(--leetcode-text-secondary, #6b7280);
  font-size: 12px;
}

.post-title {
  margin: 0 0 20px;
  color: var(--leetcode-text, #24292f);
  font-size: 28px;
  line-height: 1.4;
}

.post-body {
  margin-bottom: 22px;
  color: var(--leetcode-text, #24292f);
  font-size: 15px;
  line-height: 1.9;
  white-space: pre-wrap;
  word-break: break-word;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3) {
  margin: 1.1em 0 0.55em;
  line-height: 1.35;
}

.markdown-body {
  white-space: normal;
}

.markdown-body :deep(p),
.markdown-body :deep(ul),
.markdown-body :deep(ol),
.markdown-body :deep(blockquote),
.markdown-body :deep(pre) {
  margin: 0 0 1em;
}

.markdown-body :deep(pre) {
  overflow: auto;
  padding: 16px;
  border-radius: 14px;
  background: #0f172a;
  color: #e5e7eb;
  white-space: pre;
}

.markdown-body :deep(code) {
  font-family: var(--font-code, 'Fira Code', Consolas, monospace);
}

.markdown-body :deep(:not(pre) > code) {
  padding: 2px 6px;
  border-radius: 6px;
  background: rgba(22, 104, 220, 0.08);
  color: #1668dc;
}

.markdown-body :deep(blockquote) {
  padding: 10px 14px;
  border-left: 4px solid #1668dc;
  border-radius: 10px;
  background: rgba(22, 104, 220, 0.05);
  color: var(--leetcode-text-secondary, #6b7280);
}

.markdown-body :deep(a) {
  color: #1668dc;
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: -8px 0 22px;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding-top: 20px;
  border-top: 1px solid var(--leetcode-border, #e5e7eb);
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
  color: var(--leetcode-text-secondary, #6b7280);
  font-size: 14px;
}

.comments-section {
  padding: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 20px;
}

.section-title {
  margin: 0;
  color: var(--leetcode-text, #24292f);
  font-size: 20px;
}

.section-subtitle {
  margin: 8px 0 0;
  color: var(--leetcode-text-secondary, #6b7280);
  font-size: 13px;
  line-height: 1.7;
}

.comment-count {
  color: #1668dc;
  font-size: 13px;
  font-weight: 600;
}

.comment-form {
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--leetcode-border, #e5e7eb);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-item {
  padding: 16px;
  border: 1px solid rgba(0, 102, 255, 0.08);
  border-radius: 16px;
  background: rgba(0, 102, 255, 0.03);
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
  gap: 4px;
}

.comment-author {
  color: var(--leetcode-text, #24292f);
  font-size: 14px;
  font-weight: 600;
}

.comment-time {
  color: var(--leetcode-text-secondary, #6b7280);
  font-size: 12px;
}

.comment-content {
  color: var(--leetcode-text, #24292f);
  font-size: 14px;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
}

.comment-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

.empty-comments {
  padding: 32px 12px;
}

.empty-text {
  margin: 0;
  line-height: 1.7;
}

.loading-container {
  min-height: 360px;
  display: flex;
  align-items: center;
  justify-content: center;
}

@media (max-width: 768px) {
  .post-detail-page {
    padding: 16px;
  }

  .page-header,
  .post-header,
  .post-footer,
  .section-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .post-card,
  .comments-section {
    padding: 20px;
  }

  .post-title {
    font-size: 22px;
  }
}
</style>
