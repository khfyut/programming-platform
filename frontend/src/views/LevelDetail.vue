
<template>
  <div class="level-detail-page">
    <div class="detail-container">
      <div class="page-header">
        <div class="header-left">
          <el-button @click="goBack" text>
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <div class="level-info" v-if="currentLevel">
            <h1 class="page-title">{{ currentLevel.name }}</h1>
            <p class="page-subtitle">{{ currentLevel.description || '学习相关知识和技能' }}</p>
          </div>
        </div>
      </div>

      <el-tabs v-model="activeTab" class="content-tabs">
        <el-tab-pane label="学习资源" name="resources">
          <div class="resources-section" v-loading="loadingResources">
            <div v-if="resources.length > 0" class="resources-list">
              <div
                v-for="resource in resources"
                :key="resource.id"
                class="resource-card"
                @click="openResource(resource)"
              >
                <div class="resource-icon">
                  <el-icon v-if="resource.type === 'tutorial'"><Reading /></el-icon>
                  <el-icon v-else-if="resource.type === 'example'"><Document /></el-icon>
                  <el-icon v-else-if="resource.type === 'explanation'"><ChatDotRound /></el-icon>
                  <el-icon v-else><Notebook /></el-icon>
                </div>
                <div class="resource-info">
                  <h4 class="resource-name">{{ resource.name }}</h4>
                  <p class="resource-desc">{{ resource.description }}</p>
                </div>
                <div class="resource-type-tag">
                  {{ getResourceTypeText(resource.type) }}
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无学习资源" />
          </div>
        </el-tab-pane>

        <el-tab-pane label="练习题目" name="problems">
          <div class="problems-section" v-loading="loadingProblems">
            <div v-if="problems.length > 0" class="problems-list">
              <div
                v-for="problem in problems"
                :key="problem.id"
                class="problem-item"
                @click="goToProblem(problem.id)"
              >
                <div class="problem-info">
                  <span class="problem-id">{{ problem.id }}.</span>
                  <div class="problem-title-container">
                    <h4 class="problem-title">{{ problem.title }}</h4>
                    <div class="problem-tags">
                      <el-tag :type="getDifficultyType(problem.difficulty)" size="small">
                        {{ getDifficultyText(problem.difficulty) }}
                      </el-tag>
                      <el-tag 
                        v-for="(tag, index) in getKnowledgePoints(problem.knowledgePoints)" 
                        :key="index" 
                        size="small" 
                        type="info"
                      >
                        {{ tag }}
                      </el-tag>
                    </div>
                  </div>
                </div>
                <el-icon class="problem-arrow"><ArrowRight /></el-icon>
              </div>
            </div>
            <el-empty v-else description="暂无练习题" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog
      v-model="resourceDialogVisible"
      :title="currentResource?.name"
      width="800px"
      class="resource-dialog"
    >
      <div class="resource-content" v-if="currentResource">
        <div class="markdown-body" v-html="renderedContent"></div>
      </div>
      <template #footer>
        <el-button @click="resourceDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft,
  ArrowRight,
  Reading,
  Document,
  ChatDotRound,
  Notebook
} from '@element-plus/icons-vue'
import {
  getPathDetail,
  getLevelResources,
  getLevelProblems
} from '@/api/learn'

const route = useRoute()
const router = useRouter()

const activeTab = ref('resources')
const loadingResources = ref(false)
const loadingProblems = ref(false)
const resources = ref([])
const problems = ref([])
const currentLevel = ref(null)
const currentResource = ref(null)
const resourceDialogVisible = ref(false)

const levelId = computed(() => route.params.levelId)
const pathId = computed(() => route.params.pathId)

const renderMarkdown = (content) => {
  if (!content) return ''
  
  let html = content
    .replace(/^# (.*$)/gim, '<h1>$1</h1>')
    .replace(/^## (.*$)/gim, '<h2>$1</h2>')
    .replace(/^### (.*$)/gim, '<h3>$1</h3>')
    .replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>')
    .replace(/\n\n/g, '</p><p>')
    .replace(/^\*(.*$)/gim, '<li>$1</li>')
    .replace(/^-(.*$)/gim, '<li>$1</li>')
    .replace(/^\d+\. (.*$)/gim, '<li>$1</li>')
  
  // 处理列表
  html = html.replace(/(<li>.*?<\/li>)/gs, '<ul>$1</ul>')
  
  return `<p>${html}</p>`
}

const renderedContent = computed(() => {
  if (currentResource.value && currentResource.value.url) {
    console.log('渲染内容长度:', currentResource.value.url.length)
    const result = renderMarkdown(currentResource.value.url)
    console.log('渲染结果长度:', result.length)
    console.log('渲染结果前100字符:', result.substring(0, 100))
    return result
  }
  return ''
})

const getResourceTypeText = (type) => {
  const types = {
    tutorial: '教程',
    example: '示例',
    explanation: '知识点',
    document: '文档',
    video: '视频'
  }
  return types[type] || type
}

const getDifficultyType = (difficulty) => {
  const types = { 0: 'success', 1: 'warning', 2: 'danger', 3: 'danger' }
  return types[difficulty] || 'info'
}

const getDifficultyText = (difficulty) => {
  const texts = { 0: '简单', 1: '中等', 2: '困难', 3: '困难' }
  return texts[difficulty] || '未知'
}

const getKnowledgePoints = (knowledgePoints) => {
  if (!knowledgePoints) return []
  return knowledgePoints.split(',').filter(tag => tag.trim())
}

const fetchLevelData = async () => {
  if (!pathId.value || !levelId.value) {
    ElMessage.error('缺少必要参数')
    return
  }

  try {
    const res = await getPathDetail(pathId.value)
    if (res.code === 200 && res.data) {
      const chapters = res.data.chapters || []
      for (const chapter of chapters) {
        if (chapter.levels) {
          const level = chapter.levels.find(l => l.id === parseInt(levelId.value))
          if (level) {
            currentLevel.value = level
            break
          }
        }
      }
    }
  } catch (error) {
    console.error('获取关卡信息失败:', error)
  }
}

const fetchResources = async () => {
  loadingResources.value = true
  try {
    const res = await getLevelResources(levelId.value)
    if (res.code === 200) {
      resources.value = res.data || []
      console.log('获取到的学习资源:', resources.value)
    }
  } catch (error) {
    console.error('获取学习资源失败:', error)
    ElMessage.error('获取学习资源失败')
  } finally {
    loadingResources.value = false
  }
}

const fetchProblems = async () => {
  loadingProblems.value = true
  try {
    const res = await getLevelProblems(levelId.value)
    if (res.code === 200) {
      problems.value = res.data || []
    }
  } catch (error) {
    console.error('获取练习题失败:', error)
    ElMessage.error('获取练习题失败')
  } finally {
    loadingProblems.value = false
  }
}

const openResource = (resource) => {
  console.log('打开资源:', resource)
  console.log('资源URL:', resource.url.substring(0, 100))
  currentResource.value = resource
  resourceDialogVisible.value = true
  console.log('当前资源:', currentResource.value)
}

const goToProblem = (problemId) => {
  router.push({ name: 'ProblemDetail', params: { id: problemId } })
}

const goBack = () => {
  if (pathId.value) {
    router.push({ name: 'LearningPath', params: { id: pathId.value } })
  } else {
    router.push({ name: 'Learn' })
  }
}

onMounted(() => {
  fetchLevelData()
  fetchResources()
  fetchProblems()
})
</script>

<style scoped>
.level-detail-page {
  width: 100%;
  min-height: 100vh;
  background: var(--leetcode-bg-secondary, #F7F8FA);
  padding: 24px;
  box-sizing: border-box;
}

.detail-container {
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.level-info {
  flex: 1;
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

.content-tabs {
  background: var(--leetcode-bg, #FFFFFF);
  border-radius: 12px;
  padding: 0 24px 24px;
  border: 1px solid var(--leetcode-border, #E5E7EB);
}

.content-tabs :deep(.el-tabs__header) {
  margin: 0 -24px 24px;
  padding: 0 24px;
}

.resources-section,
.problems-section {
  min-height: 300px;
}

.resources-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.resource-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;
}

.resource-card:hover {
  background: #FFFFFF;
  border-color: var(--leetcode-primary, #0066FF);
}

.resource-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  background: var(--leetcode-primary, #0066FF);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.resource-info {
  flex: 1;
}

.resource-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--leetcode-text, #24292F);
  margin: 0 0 4px 0;
}

.resource-desc {
  font-size: 13px;
  color: var(--leetcode-text-secondary, #6B7280);
  margin: 0;
}

.resource-type-tag {
  padding: 4px 12px;
  background: var(--leetcode-bg, #FFFFFF);
  border: 1px solid var(--leetcode-border, #E5E7EB);
  border-radius: 12px;
  font-size: 12px;
  color: var(--leetcode-text-secondary, #6B7280);
  flex-shrink: 0;
}

.problems-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.problem-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;
}

.problem-item:hover {
  background: #FFFFFF;
  border-color: var(--leetcode-primary, #0066FF);
}

.problem-info {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.problem-id {
  font-size: 14px;
  color: var(--leetcode-text-secondary, #6B7280);
  font-weight: 500;
  margin-top: 2px;
}

.problem-title-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}

.problem-title {
  font-size: 15px;
  font-weight: 500;
  color: var(--leetcode-text, #24292F);
  margin: 0;
}

.problem-tags {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.problem-arrow {
  color: var(--leetcode-text-secondary, #6B7280);
  font-size: 18px;
}

.resource-dialog :deep(.el-dialog__body) {
  max-height: 600px;
  overflow-y: auto;
}

.resource-content {
  padding: 0 10px;
}

.markdown-body {
  line-height: 1.8;
  color: var(--leetcode-text, #24292F);
}

.markdown-body h1 {
  font-size: 24px;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--leetcode-border, #E5E7EB);
}

.markdown-body h2 {
  font-size: 20px;
  margin-top: 24px;
  margin-bottom: 12px;
}

.markdown-body h3 {
  font-size: 18px;
  margin-top: 20px;
  margin-bottom: 10px;
}

.markdown-body p {
  margin-bottom: 12px;
}

.markdown-body ul,
.markdown-body ol {
  padding-left: 24px;
  margin-bottom: 12px;
}

.markdown-body li {
  margin-bottom: 6px;
}

.markdown-body code {
  background: var(--leetcode-bg-secondary, #F7F8FA);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 14px;
}

.markdown-body pre {
  background: #1E1E1E;
  color: #D4D4D4;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin-bottom: 16px;
}

.markdown-body pre code {
  background: transparent;
  padding: 0;
  color: inherit;
}

@media (max-width: 768px) {
  .level-detail-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    gap: 16px;
  }

  .resource-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .resource-icon {
    width: 40px;
    height: 40px;
    font-size: 20px;
  }

  .problem-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .problem-arrow {
    display: none;
  }
}
</style>
