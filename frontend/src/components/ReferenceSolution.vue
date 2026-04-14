<template>
  <div class="reference-solution">
    <el-button
      type="primary"
      :disabled="!canView"
      :loading="buttonLoading"
      @click="handleViewSolution"
    >
      查看参考答案
    </el-button>

    <el-dialog
      v-model="showDialog"
      title="参考答案"
      width="min(1080px, 92vw)"
      destroy-on-close
      class="solution-dialog"
    >
      <div v-loading="contentLoading" class="solution-content">
        <div class="toolbar">
          <div class="toolbar-copy">
            <strong>{{ getRuntimeLanguageLabel(solution.language || selectedLanguage || 'java') }}</strong>
            <span>支持按语言切换查看不同实现，并结合思路说明理解解法。</span>
          </div>
          <el-select
            v-model="selectedLanguage"
            class="language-select"
            size="small"
            :disabled="availableLanguages.length <= 1 || contentLoading"
            @change="loadSolution"
          >
            <el-option
              v-for="lang in availableLanguages"
              :key="lang"
              :label="getRuntimeLanguageLabel(lang)"
              :value="lang"
            />
          </el-select>
        </div>

        <div class="meta-section">
          <el-tag type="success" effect="plain">时间复杂度：{{ solution.timeComplexity || '--' }}</el-tag>
          <el-tag type="warning" effect="plain">空间复杂度：{{ solution.spaceComplexity || '--' }}</el-tag>
        </div>

        <section class="content-section">
          <h3>解题思路</h3>
          <div class="explanation-content">
            {{ solution.explanation || '暂时还没有补充解题说明。' }}
          </div>
        </section>

        <section class="content-section">
          <div class="code-header">
            <h3>参考代码</h3>
            <el-button
              size="small"
              :loading="copyLoading"
              :disabled="!solution.solutionCode"
              @click="copyCode"
            >
              复制代码
            </el-button>
          </div>
          <div class="editor-shell">
            <MonacoEditor
              v-model="solution.solutionCode"
              :language="getRuntimeMonacoLanguage(solution.language || selectedLanguage)"
              height="400px"
              :read-only="true"
            />
          </div>
        </section>

        <section v-if="hintEntries.length > 0" class="content-section">
          <h3>渐进提示</h3>
          <div class="hints-list">
            <div
              v-for="([level, hint]) in hintEntries"
              :key="level"
              class="hint-item"
            >
              <span class="hint-level">提示 {{ level }}</span>
              <span class="hint-content">{{ hint }}</span>
            </div>
          </div>
        </section>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, defineAsyncComponent, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getAvailableLanguages, getReferenceSolution } from '@/api/referenceSolution'
import { getRuntimeLanguageLabel, getRuntimeMonacoLanguage, normalizeRuntimeLanguage } from '@/utils/runtimeLanguage'

const MonacoEditor = defineAsyncComponent(() => import('@/components/MonacoEditor.vue'))

const props = defineProps({
  problemId: {
    type: Number,
    required: true
  },
  canView: {
    type: Boolean,
    default: false
  },
  preferredLanguage: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['view-solution'])

const showDialog = ref(false)
const buttonLoading = ref(false)
const contentLoading = ref(false)
const copyLoading = ref(false)
const selectedLanguage = ref('')
const availableLanguages = ref([])

const solution = reactive({
  problemId: props.problemId,
  language: '',
  solutionCode: '',
  timeComplexity: '',
  spaceComplexity: '',
  explanation: '',
  hints: {}
})

const hintEntries = computed(() => Object.entries(solution.hints || {}).filter(([, hint]) => Boolean(hint)))

const resetSolution = () => {
  Object.assign(solution, {
    problemId: props.problemId,
    language: '',
    solutionCode: '',
    timeComplexity: '',
    spaceComplexity: '',
    explanation: '',
    hints: {}
  })
}

const pickLanguage = (languages) => {
  const preferred = normalizeRuntimeLanguage(props.preferredLanguage)
  if (preferred && languages.includes(preferred)) {
    return preferred
  }
  return languages[0] || ''
}

const handleViewSolution = async () => {
  if (!props.canView) {
    return
  }

  buttonLoading.value = true
  try {
    const languagesRes = await getAvailableLanguages(props.problemId)
    const languages = languagesRes?.code === 200
      ? (languagesRes.data || []).map(normalizeRuntimeLanguage).filter(Boolean)
      : []

    if (languages.length === 0) {
      ElMessage.error('暂无参考答案')
      return
    }

    availableLanguages.value = languages
    selectedLanguage.value = pickLanguage(languages)
    showDialog.value = true
    emit('view-solution')
    await loadSolution()
  } catch (error) {
    console.error('获取参考答案失败:', error)
    ElMessage.error('获取参考答案失败')
  } finally {
    buttonLoading.value = false
  }
}

const loadSolution = async () => {
  if (!selectedLanguage.value) {
    return
  }

  contentLoading.value = true
  try {
    const res = await getReferenceSolution(props.problemId, selectedLanguage.value)

    if (res?.code !== 200 || !res.data) {
      throw new Error(res?.msg || '获取参考答案失败')
    }

    Object.assign(solution, {
      ...res.data,
      language: normalizeRuntimeLanguage(res.data.language || selectedLanguage.value),
      solutionCode: res.data.solutionCode || '',
      timeComplexity: res.data.timeComplexity || '',
      spaceComplexity: res.data.spaceComplexity || '',
      explanation: res.data.explanation || '',
      hints: res.data.hints || {}
    })
  } catch (error) {
    console.error('获取参考答案失败:', error)
    resetSolution()
    ElMessage.error(error.message || '获取参考答案失败')
  } finally {
    contentLoading.value = false
  }
}

const copyCode = async () => {
  if (!solution.solutionCode) {
    ElMessage.info('当前没有可复制的代码')
    return
  }

  copyLoading.value = true
  try {
    await navigator.clipboard.writeText(solution.solutionCode)
    ElMessage.success('代码已复制到剪贴板')
  } catch (error) {
    console.error('复制代码失败:', error)
    ElMessage.error('复制失败，请手动复制')
  } finally {
    copyLoading.value = false
  }
}

watch(
  () => props.preferredLanguage,
  (nextLanguage) => {
    const normalized = normalizeRuntimeLanguage(nextLanguage)
    if (showDialog.value && normalized && availableLanguages.value.includes(normalized)) {
      selectedLanguage.value = normalized
      loadSolution()
    }
  }
)
</script>

<style scoped>
.reference-solution {
  margin: 10px 0;
}

.solution-dialog :deep(.el-dialog) {
  max-width: 1080px;
  border-radius: 18px;
}

.solution-dialog :deep(.el-dialog__body) {
  padding-top: 16px;
}

.solution-content {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid var(--border-light, #e5e7eb);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.03);
}

.toolbar-copy {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 4px;
}

.toolbar-copy strong {
  color: var(--text-primary, #111827);
  font-size: 15px;
}

.toolbar-copy span {
  color: var(--text-secondary, #6b7280);
  font-size: 13px;
  line-height: 1.7;
}

.language-select {
  width: 180px;
  flex-shrink: 0;
}

.meta-section {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.content-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.content-section h3,
.code-header h3 {
  margin: 0;
  color: var(--text-primary, #111827);
  font-size: 16px;
}

.explanation-content {
  padding: 14px 16px;
  border: 1px solid var(--border-light, #e5e7eb);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.03);
  color: var(--text-secondary, #4b5563);
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
}

.code-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.editor-shell {
  overflow: hidden;
  border: 1px solid var(--border-light, #e5e7eb);
  border-radius: 14px;
}

.hints-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 16px;
  border-radius: 14px;
  background: #f5f7fa;
}

.hint-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  line-height: 1.7;
}

.hint-level {
  flex-shrink: 0;
  color: #409eff;
  font-weight: 700;
}

.hint-content {
  color: #606266;
}

@media (max-width: 768px) {
  .toolbar,
  .code-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .language-select {
    width: 100%;
  }
}
</style>
