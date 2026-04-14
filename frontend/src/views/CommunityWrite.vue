<template>
  <div class="write-page">
    <div class="write-container">
      <section class="write-hero">
        <div>
          <p class="hero-kicker">Markdown Studio</p>
          <h1 class="page-title">{{ isEditMode ? '编辑学习动态' : '写一条学习动态' }}</h1>
          <p class="page-subtitle">用 Markdown 记录学习过程、代码片段和复盘结论。公开分享或仅自己可见，都在这里完成。</p>
        </div>
        <div class="hero-actions">
          <el-button plain @click="goBack">返回社区</el-button>
          <el-button plain @click="triggerMarkdownImport" :disabled="saving">
            <el-icon><Upload /></el-icon>
            导入 .md
          </el-button>
          <el-button type="primary" @click="handleSave" :loading="saving">
            {{ isEditMode ? '保存修改' : '发布动态' }}
          </el-button>
          <input
            ref="fileInputRef"
            class="hidden-file-input"
            type="file"
            accept=".md,.markdown,text/markdown,text/plain"
            @change="handleMarkdownFileChange"
          />
        </div>
      </section>

      <section v-if="draftRestored" class="draft-banner">
        <span>已恢复上次未发布的本地草稿。</span>
        <el-button text type="primary" @click="clearDraftAndReset">清空草稿</el-button>
      </section>

      <section class="meta-card">
        <el-form label-position="top" class="meta-form">
          <el-form-item label="标题">
            <el-input v-model="form.title" maxlength="100" show-word-limit placeholder="例如：HashMap 学习笔记与踩坑复盘" />
          </el-form-item>
          <div class="meta-grid">
            <el-form-item label="类型">
              <el-select v-model="form.type" class="full-width">
                <el-option label="学习笔记" value="note" />
                <el-option label="讨论" value="discussion" />
                <el-option label="问答" value="question" />
                <el-option label="分享" value="share" />
              </el-select>
            </el-form-item>
            <el-form-item label="可见性">
              <el-radio-group v-model="form.visibility">
                <el-radio label="public">公开分享</el-radio>
                <el-radio label="private">仅自己可见</el-radio>
              </el-radio-group>
            </el-form-item>
          </div>
          <el-form-item label="标签">
            <el-input v-model="form.tags" maxlength="500" placeholder="可选，用逗号分隔，如 Java, 集合, 复盘" />
          </el-form-item>
        </el-form>
      </section>

      <section class="editor-shell">
        <div class="editor-toolbar">
          <div>
            <strong>正文</strong>
            <span class="toolbar-hint">{{ contentSizeLabel }} / 60KB</span>
          </div>
          <el-radio-group v-model="mobilePane" size="small" class="mobile-pane-switch">
            <el-radio-button value="edit">编辑</el-radio-button>
            <el-radio-button value="preview">预览</el-radio-button>
          </el-radio-group>
        </div>

        <div class="editor-grid">
          <div class="editor-panel" :class="{ 'mobile-hidden': mobilePane !== 'edit' }">
            <el-input
              v-model="form.content"
              type="textarea"
              :rows="22"
              resize="none"
              placeholder="# 今天学到了什么&#10;&#10;- 记录概念&#10;- 粘贴代码块&#10;- 写下下一步计划"
              @input="handleContentInput"
            />
          </div>
          <article class="preview-panel markdown-body" :class="{ 'mobile-hidden': mobilePane !== 'preview' }">
            <div v-if="form.content.trim()" v-html="renderedContent"></div>
            <div v-else class="preview-empty">预览会实时显示在这里。可以导入 `.md` 文件，或直接开始书写。</div>
          </article>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import { createPost, getPostById, updatePost } from '@/api/community'
import { useUserStore } from '@/stores/user'
import { extractMarkdownTitle, renderMarkdown, titleFromMarkdownFileName } from '@/utils/markdown'

const DRAFT_KEY = 'community-write-single-draft'
const MAX_CONTENT_BYTES = 60 * 1024

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const fileInputRef = ref(null)
const saving = ref(false)
const loading = ref(false)
const draftRestored = ref(false)
const mobilePane = ref('edit')
const form = ref(createEmptyForm())

const isEditMode = computed(() => Boolean(route.params.id))
const contentByteSize = computed(() => new Blob([form.value.content || '']).size)
const contentSizeLabel = computed(() => `${Math.ceil(contentByteSize.value / 1024)}KB`)
const renderedContent = computed(() => renderMarkdown(form.value.content))

function createEmptyForm() {
  return {
    title: '',
    type: 'note',
    visibility: 'public',
    tags: '',
    content: ''
  }
}

const normalizePost = (post = {}) => ({
  title: post.title || '',
  type: post.type || 'note',
  visibility: post.visibility || 'public',
  tags: post.tags || '',
  content: post.content || ''
})

const handleContentInput = () => {
  if (contentByteSize.value > MAX_CONTENT_BYTES) {
    ElMessage.warning('当前版本支持 60KB 以内 Markdown 内容，请压缩后再发布。')
  }
}

const validateForm = () => {
  if (!form.value.title.trim()) {
    ElMessage.warning('请输入标题')
    return false
  }
  if (!form.value.content.trim()) {
    ElMessage.warning('请输入正文内容')
    return false
  }
  if (contentByteSize.value > MAX_CONTENT_BYTES) {
    ElMessage.warning('正文超过 60KB，当前数据库字段无法可靠保存，请精简内容。')
    return false
  }
  return true
}

const buildPayload = () => ({
  ...form.value,
  title: form.value.title.trim(),
  content: form.value.content.trim(),
  tags: form.value.tags.trim()
})

const handleSave = async () => {
  if (!validateForm()) return

  saving.value = true
  try {
    const payload = buildPayload()
    const res = isEditMode.value
      ? await updatePost(route.params.id, payload)
      : await createPost(payload)

    if (res.code !== 200) {
      ElMessage.error(res.msg || '保存失败')
      return
    }

    if (!isEditMode.value) {
      clearDraft()
    }

    const postId = isEditMode.value ? route.params.id : (res.data?.id || res.data)
    ElMessage.success(isEditMode.value ? '保存成功' : '发布成功')
    router.push(postId ? `/community/post/${postId}` : '/community')
  } catch (error) {
    console.error('保存学习动态失败:', error)
    ElMessage.error('保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

const triggerMarkdownImport = () => {
  fileInputRef.value?.click()
}

const handleMarkdownFileChange = async (event) => {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return

  const isMarkdownFile = /\.(md|markdown)$/i.test(file.name) ||
    ['text/markdown', 'text/plain', ''].includes(file.type)
  if (!isMarkdownFile) {
    ElMessage.warning('请选择 .md 或 .markdown 文件')
    return
  }
  if (file.size > MAX_CONTENT_BYTES) {
    ElMessage.warning('当前版本支持导入 60KB 以内的 Markdown 文件')
    return
  }

  if (form.value.content.trim()) {
    try {
      await ElMessageBox.confirm('导入后会替换当前正文内容，是否继续？', '导入 Markdown', {
        confirmButtonText: '替换正文',
        cancelButtonText: '取消',
        type: 'warning'
      })
    } catch {
      return
    }
  }

  try {
    const content = await readFileAsText(file)
    form.value.content = content
    const title = extractMarkdownTitle(content) || titleFromMarkdownFileName(file.name)
    if (title) {
      form.value.title = title
    }
    mobilePane.value = 'preview'
    ElMessage.success('Markdown 文件已导入')
  } catch (error) {
    console.error('读取 Markdown 文件失败:', error)
    ElMessage.error('读取文件失败，请确认文件编码为 UTF-8 文本')
  }
}

const readFileAsText = (file) => new Promise((resolve, reject) => {
  const reader = new FileReader()
  reader.onload = () => resolve(String(reader.result || ''))
  reader.onerror = () => reject(reader.error || new Error('read file failed'))
  reader.readAsText(file, 'utf-8')
})

const restoreDraft = () => {
  if (isEditMode.value) return
  const rawDraft = localStorage.getItem(DRAFT_KEY)
  if (!rawDraft) return

  try {
    const draft = JSON.parse(rawDraft)
    form.value = {
      ...createEmptyForm(),
      ...draft
    }
    draftRestored.value = Boolean(form.value.title || form.value.content || form.value.tags)
  } catch {
    localStorage.removeItem(DRAFT_KEY)
  }
}

const saveDraft = () => {
  if (isEditMode.value || loading.value) return
  const hasDraft = form.value.title || form.value.content || form.value.tags || form.value.visibility !== 'public'
  if (!hasDraft) {
    clearDraft()
    return
  }
  localStorage.setItem(DRAFT_KEY, JSON.stringify(form.value))
}

const clearDraft = () => {
  localStorage.removeItem(DRAFT_KEY)
  draftRestored.value = false
}

const clearDraftAndReset = () => {
  clearDraft()
  form.value = createEmptyForm()
}

const loadPostForEdit = async () => {
  if (!isEditMode.value) return

  loading.value = true
  try {
    const res = await getPostById(route.params.id)
    if (res.code !== 200 || !res.data) {
      ElMessage.error(res.msg || '动态不存在或无权访问')
      router.replace('/community')
      return
    }

    const ownerId = Number(res.data.userId)
    const currentUserId = Number(userStore.userInfo?.id)
    if (ownerId && currentUserId && ownerId !== currentUserId) {
      ElMessage.error('只能编辑自己的学习动态')
      router.replace(`/community/post/${route.params.id}`)
      return
    }

    form.value = normalizePost(res.data)
  } catch (error) {
    console.error('加载学习动态失败:', error)
    ElMessage.error('加载失败，请稍后重试')
    router.replace('/community')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push(isEditMode.value ? `/community/post/${route.params.id}` : '/community')
}

watch(form, saveDraft, { deep: true })

onMounted(async () => {
  if (!userStore.userInfo) {
    await userStore.fetchUserInfo()
  }
  restoreDraft()
  await loadPostForEdit()
})
</script>

<style scoped>
.write-page {
  min-height: 100vh;
  padding: 24px;
  background:
    radial-gradient(circle at top left, rgba(22, 104, 220, 0.08), transparent 24%),
    var(--leetcode-bg-secondary, #f7f8fa);
}

.write-container {
  max-width: 1220px;
  margin: 0 auto;
}

.write-hero,
.meta-card,
.editor-shell,
.draft-banner {
  border: 1px solid var(--leetcode-border, #e5e7eb);
  border-radius: 22px;
  background: var(--leetcode-bg, #fff);
  box-shadow: 0 18px 44px rgba(15, 23, 42, 0.04);
}

.write-hero {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  padding: 28px;
  margin-bottom: 18px;
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
  color: var(--leetcode-text, #24292f);
  font-size: 30px;
}

.page-subtitle {
  max-width: 680px;
  margin: 0;
  color: var(--leetcode-text-secondary, #6b7280);
  font-size: 14px;
  line-height: 1.8;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  justify-content: flex-end;
  gap: 12px;
}

.hidden-file-input {
  display: none;
}

.draft-banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 12px 18px;
  margin-bottom: 18px;
  color: #1668dc;
  background: rgba(22, 104, 220, 0.06);
}

.meta-card {
  padding: 22px;
  margin-bottom: 18px;
}

.meta-grid {
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 18px;
}

.full-width {
  width: 100%;
}

.editor-shell {
  overflow: hidden;
}

.editor-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  border-bottom: 1px solid var(--leetcode-border, #e5e7eb);
}

.toolbar-hint {
  margin-left: 10px;
  color: var(--leetcode-text-secondary, #6b7280);
  font-size: 12px;
}

.mobile-pane-switch {
  display: none;
}

.editor-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  min-height: 620px;
}

.editor-panel {
  border-right: 1px solid var(--leetcode-border, #e5e7eb);
}

.editor-panel :deep(.el-textarea),
.editor-panel :deep(.el-textarea__inner) {
  height: 100%;
}

.editor-panel :deep(.el-textarea__inner) {
  min-height: 620px !important;
  border: 0;
  border-radius: 0;
  padding: 22px;
  font-family: var(--font-code, 'Fira Code', Consolas, monospace);
  font-size: 14px;
  line-height: 1.8;
  box-shadow: none;
}

.preview-panel {
  min-height: 620px;
  padding: 22px;
  overflow: auto;
}

.preview-empty {
  display: flex;
  min-height: 300px;
  align-items: center;
  justify-content: center;
  color: var(--leetcode-text-secondary, #6b7280);
  text-align: center;
  line-height: 1.8;
}

.markdown-body {
  color: var(--leetcode-text, #24292f);
  line-height: 1.85;
  word-break: break-word;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3) {
  margin: 1.1em 0 0.55em;
  line-height: 1.35;
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

@media (max-width: 900px) {
  .write-page {
    padding: 16px;
  }

  .write-hero,
  .draft-banner {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-actions {
    justify-content: flex-start;
  }

  .meta-grid,
  .editor-grid {
    grid-template-columns: 1fr;
  }

  .mobile-pane-switch {
    display: inline-flex;
  }

  .editor-panel {
    border-right: 0;
  }

  .mobile-hidden {
    display: none;
  }
}
</style>
