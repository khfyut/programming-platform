<template>
  <div class="content-page">
    <el-tabs v-model="activeTab" class="content-tabs">
      <el-tab-pane label="收藏的题目" name="problems">
        <div class="content-list">
          <div
            v-for="item in favoriteProblems"
            :key="item.targetId"
            class="content-item"
            @click="goToProblem(item.targetId)"
          >
            <div class="content-main">
              <div class="content-header">
                <el-tag type="success" size="small">题目</el-tag>
                <span class="content-time">{{ formatTime(item.createTime) }}</span>
              </div>
              <h4 class="content-title">{{ item.targetName || `题目 ${item.targetId}` }}</h4>
            </div>
            <div class="content-actions">
              <el-button type="primary" size="small" @click.stop="goToProblem(item.targetId)">
                开始做题
              </el-button>
              <el-button type="danger" link size="small" @click.stop="removeFavorite(item.targetId, 'PROBLEM')">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
          <el-empty v-if="favoriteProblems.length === 0" description="暂无收藏的题目" />
        </div>
      </el-tab-pane>

      <el-tab-pane label="收藏的帖子" name="posts">
        <div class="content-list">
          <div
            v-for="item in favoritePosts"
            :key="item.targetId"
            class="content-item"
            @click="goToPost(item.targetId)"
          >
            <div class="content-main">
              <div class="content-header">
                <el-tag type="warning" size="small">帖子</el-tag>
                <span class="content-time">{{ formatTime(item.createTime) }}</span>
              </div>
              <h4 class="content-title">{{ item.targetName || `帖子 ${item.targetId}` }}</h4>
            </div>
            <div class="content-actions">
              <el-button type="danger" link size="small" @click.stop="removeFavorite(item.targetId, 'POST')">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
          <el-empty v-if="favoritePosts.length === 0" description="暂无收藏的帖子" />
        </div>
      </el-tab-pane>

      <el-tab-pane label="我发布的帖子" name="my-posts">
        <div class="content-list">
          <div
            v-for="post in myPosts"
            :key="post.id"
            class="content-item"
            @click="goToPost(post.id)"
          >
            <div class="content-main">
              <div class="content-header">
                <el-tag type="primary" size="small">我的发布</el-tag>
                <span class="content-time">{{ formatTime(post.createTime) }}</span>
              </div>
              <h4 class="content-title">{{ post.title }}</h4>
              <p class="content-description">{{ post.content || post.summary || '暂无内容摘要' }}</p>
            </div>
            <div class="content-actions">
              <el-button type="primary" link size="small" @click.stop="editPost(post.id)">
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button type="danger" link size="small" @click.stop="deletePost(post.id)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
          <el-empty v-if="myPosts.length === 0" description="暂无发布的帖子">
            <el-button type="primary" @click="goToCreatePost">发布帖子</el-button>
          </el-empty>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Edit } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { deletePost as deletePostApi, getMyPosts } from '@/api/community'
import { getProblemDetail } from '@/api/problem'
import { getFavorites, removeFavorite as removeFavoriteApi } from '@/api/userProfile'
import { formatDistanceToNow } from '@/utils/format'

const router = useRouter()
const userStore = useUserStore()
const userId = computed(() => userStore.userInfo?.id)

const activeTab = ref('problems')
const favoriteProblems = ref([])
const favoritePosts = ref([])
const myPosts = ref([])

const formatTime = (time) => {
  if (!time) {
    return ''
  }

  return formatDistanceToNow(new Date(time))
}

const hydrateFavoriteProblems = async (items) => {
  const missingIds = [
    ...new Set(items.filter((item) => !item.targetName && item.targetId).map((item) => item.targetId))
  ]

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

  return items.map((item) => ({
    ...item,
    targetName: item.targetName || titleMap[item.targetId] || `题目 ${item.targetId}`
  }))
}

const fetchFavorites = async () => {
  if (!userId.value) {
    return
  }

  try {
    const [problemRes, postRes] = await Promise.all([
      getFavorites(userId.value, 'PROBLEM'),
      getFavorites(userId.value, 'POST')
    ])

    favoriteProblems.value = problemRes?.code === 200
      ? await hydrateFavoriteProblems(problemRes.data || [])
      : []
    favoritePosts.value = postRes?.code === 200 ? postRes.data || [] : []
  } catch (error) {
    console.error('获取收藏内容失败:', error)
  }
}

const fetchMyPosts = async () => {
  try {
    const res = await getMyPosts()
    myPosts.value = res?.code === 200 ? res.data || [] : []
  } catch (error) {
    console.error('获取我的帖子失败:', error)
  }
}

const goToProblem = (id) => {
  router.push(`/dashboard/problem/${id}`)
}

const goToPost = (id) => {
  router.push(`/dashboard/community/post/${id}`)
}

const goToCreatePost = () => {
  router.push('/dashboard/community?action=create')
}

const editPost = (id) => {
  router.push(`/dashboard/community?edit=${id}`)
}

const removeFavorite = async (targetId, targetType) => {
  try {
    await ElMessageBox.confirm('确定要取消收藏吗？', '提示', { type: 'warning' })
    const res = await removeFavoriteApi(targetId, targetType)

    if (res?.code !== 200) {
      throw new Error(res?.msg || '取消收藏失败')
    }

    ElMessage.success('已取消收藏')
    await fetchFavorites()
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const deletePost = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这篇帖子吗？', '提示', { type: 'warning' })
    const res = await deletePostApi(id)

    if (res?.code !== 200) {
      throw new Error(res?.msg || '删除失败')
    }

    ElMessage.success('删除成功')
    await fetchMyPosts()
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

watch(
  [userId, activeTab],
  ([id, tab]) => {
    if (!id) {
      return
    }

    if (tab === 'my-posts') {
      fetchMyPosts()
      return
    }

    fetchFavorites()
  },
  { immediate: true }
)
</script>

<style scoped>
.content-page {
  min-height: 500px;
  padding: 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.content-tabs :deep(.el-tabs__header) {
  margin-bottom: 24px;
}

.content-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.content-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s ease;
}

.content-item:hover {
  background: #ecf5ff;
}

.content-main {
  flex: 1;
  min-width: 0;
}

.content-header {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 12px;
}

.content-time {
  font-size: 12px;
  color: #909399;
}

.content-title {
  margin: 0 0 8px;
  font-size: 16px;
  color: #303133;
}

.content-description {
  margin: 0;
  color: #606266;
  line-height: 1.6;
}

.content-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-left: 16px;
}

@media (max-width: 768px) {
  .content-item {
    flex-direction: column;
  }

  .content-actions {
    flex-direction: row;
    width: 100%;
    margin-top: 12px;
    margin-left: 0;
    justify-content: flex-end;
  }
}
</style>
