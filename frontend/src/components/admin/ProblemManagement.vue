<template>
  <el-card class="management-card" shadow="never">
    <template #header>
      <div class="card-header">
        <span class="card-title">题目管理</span>
        <div class="header-buttons">
          <el-button type="primary" @click="openAddDialog" class="add-btn">
            <el-icon><Plus /></el-icon>
            添加题目
          </el-button>
          <el-dropdown>
            <el-button type="success">
              <el-icon><Upload /></el-icon>
              导入题目
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="openImportDialog('excel')">
                  <el-icon><Document /></el-icon>
                  Excel 文件
                </el-dropdown-item>
                <el-dropdown-item @click="openImportDialog('csv')">
                  <el-icon><Document /></el-icon>
                  CSV 文件
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </template>

    <el-table
      :data="problems"
      stripe
      row-key="id"
      v-loading="loading"
      max-height="500"
      class="admin-table"
      table-layout="fixed"
      empty-text="暂无题目数据"
      :default-sort="{ prop: 'id', order: 'descending' }"
    >
      <el-table-column prop="id" label="ID" width="80" sortable />
      <el-table-column prop="title" label="标题" min-width="260" sortable show-overflow-tooltip />
      <el-table-column prop="difficulty" label="难度" width="100" sortable>
        <template #default="{ row }">
          <span :class="['difficulty-badge', getDifficultyClass(row.difficulty)]">
            {{ getDifficultyText(row.difficulty) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="language" label="默认语言" width="130" sortable>
        <template #default="{ row }">
          <span class="language-badge">{{ formatLanguage(row.language) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link class="action-btn" @click="openEditDialog(row)">编辑</el-button>
          <el-button type="danger" link class="action-btn" @click="$emit('delete-problem', row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog
    v-model="problemDialogVisible"
    :title="dialogTitle"
    width="1000px"
    class="problem-dialog"
    destroy-on-close
  >
    <div v-loading="dialogLoading" class="problem-dialog-body">
      <el-tabs v-model="activeEditorTab" class="problem-editor-tabs">
        <el-tab-pane label="基础信息" name="basic">
          <el-form :model="problemForm" label-width="100px" class="problem-form">
            <el-form-item label="标题">
              <el-input v-model="problemForm.title" placeholder="请输入题目标题" />
            </el-form-item>
            <el-form-item label="难度">
              <el-select v-model="problemForm.difficulty" placeholder="请选择难度">
                <el-option label="简单" :value="0" />
                <el-option label="中等" :value="1" />
                <el-option label="困难" :value="2" />
              </el-select>
            </el-form-item>
            <el-form-item label="题目描述">
              <el-input v-model="problemForm.content" type="textarea" :rows="8" placeholder="请输入题目描述" />
            </el-form-item>
            <el-form-item label="输入说明">
              <el-input v-model="problemForm.input" type="textarea" :rows="3" placeholder="请输入输入说明" />
            </el-form-item>
            <el-form-item label="输出说明">
              <el-input v-model="problemForm.output" type="textarea" :rows="3" placeholder="请输入输出说明" />
            </el-form-item>
            <el-form-item label="示例说明">
              <el-input
                v-model="problemForm.sampleExplanation"
                type="textarea"
                :rows="3"
                placeholder="请输入样例解释"
              />
            </el-form-item>
            <el-form-item label="时间限制(ms)">
              <el-input-number v-model="problemForm.timeLimit" :min="1" :step="100" />
            </el-form-item>
            <el-form-item label="内存限制(KB)">
              <el-input-number v-model="problemForm.memoryLimit" :min="1" :step="1024" />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="语言配置" name="languages">
          <div class="language-config-panel">
            <el-form label-width="100px">
              <el-form-item label="启用语言">
                <el-checkbox-group v-model="enabledLanguageCodes" @change="syncLanguagePayloads">
                  <el-checkbox
                    v-for="item in runtimeLanguages"
                    :key="item.code"
                    :label="item.code"
                  >
                    {{ item.label }}
                  </el-checkbox>
                </el-checkbox-group>
              </el-form-item>
              <el-form-item label="默认语言">
                <el-select
                  v-model="problemForm.defaultLanguage"
                  placeholder="请选择默认语言"
                  :disabled="enabledLanguageCodes.length === 0"
                  @change="syncDefaultLanguage"
                >
                  <el-option
                    v-for="item in enabledLanguageOptions"
                    :key="item.code"
                    :label="item.label"
                    :value="item.code"
                  />
                </el-select>
              </el-form-item>
            </el-form>

            <el-empty
              v-if="enabledLanguageCodes.length === 0"
              description="请至少启用一种语言"
              class="config-empty"
            />

            <div
              v-for="item in orderedSupportedLanguages"
              :key="item.languageCode"
              class="language-card"
            >
              <div class="language-card-header">
                <div>
                  <strong>{{ formatLanguage(item.languageCode) }}</strong>
                  <span v-if="problemForm.defaultLanguage === item.languageCode" class="default-flag">默认语言</span>
                </div>
                <span class="filename-hint">{{ item.starterFilename }}</span>
              </div>
              <el-form label-width="110px">
                <el-form-item label="起始文件名">
                  <el-input v-model="item.starterFilename" placeholder="请输入起始文件名" />
                </el-form-item>
                <el-form-item label="Starter Code">
                  <el-input
                    v-model="item.starterCode"
                    type="textarea"
                    :rows="8"
                    placeholder="请输入该语言的起始代码"
                  />
                </el-form-item>
              </el-form>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="参考答案" name="solutions">
          <el-empty
            v-if="enabledLanguageCodes.length === 0"
            description="请先在语言配置中启用语言"
            class="config-empty"
          />

          <div
            v-for="item in orderedReferenceSolutions"
            :key="item.language"
            class="language-card"
          >
            <div class="language-card-header">
              <div>
                <strong>{{ formatLanguage(item.language) }}</strong>
                <span v-if="problemForm.defaultLanguage === item.language" class="default-flag">默认语言</span>
              </div>
            </div>
            <el-form label-width="110px">
              <el-form-item label="参考答案代码">
                <el-input
                  v-model="item.solutionCode"
                  type="textarea"
                  :rows="10"
                  placeholder="请输入该语言的参考答案代码"
                />
              </el-form-item>
              <el-form-item label="时间复杂度">
                <el-input v-model="item.timeComplexity" placeholder="例如 O(n)" />
              </el-form-item>
              <el-form-item label="空间复杂度">
                <el-input v-model="item.spaceComplexity" placeholder="例如 O(1)" />
              </el-form-item>
              <el-form-item label="解题说明">
                <el-input
                  v-model="item.explanation"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入解题说明"
                />
              </el-form-item>
              <el-form-item label="提示 1">
                <el-input v-model="item.hintLevel1" placeholder="请输入提示 1" />
              </el-form-item>
              <el-form-item label="提示 2">
                <el-input v-model="item.hintLevel2" placeholder="请输入提示 2" />
              </el-form-item>
              <el-form-item label="提示 3">
                <el-input v-model="item.hintLevel3" placeholder="请输入提示 3" />
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
    <template #footer>
      <el-button @click="closeProblemDialog" class="dialog-btn">取消</el-button>
      <el-button type="primary" @click="submitProblem" class="dialog-btn dialog-btn-primary">确定</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="importDialogVisible" :title="importDialogTitle" width="500px" class="import-dialog">
    <div class="import-content">
      <p class="import-hint">请选择要导入的文件，支持 Excel (.xlsx) 和 CSV 格式。</p>
      <el-upload
        class="upload-demo"
        :action="''"
        :auto-upload="false"
        :on-change="handleFileChange"
        :before-upload="beforeUpload"
        :accept="importFileType"
      >
        <el-button type="primary">
          <el-icon><Upload /></el-icon>
          选择文件
        </el-button>
        <template #tip>
          <div class="el-upload__tip">
            仅支持 {{ importFileExt }} 文件，大小不超过 10MB
          </div>
        </template>
      </el-upload>

      <div v-if="selectedFile" class="selected-file">
        <el-icon><Document /></el-icon>
        <span>{{ selectedFile.name }}</span>
        <el-button text @click="selectedFile = null">
          <el-icon><Delete /></el-icon>
        </el-button>
      </div>
    </div>
    <template #footer>
      <el-button @click="closeImportDialog" class="dialog-btn">取消</el-button>
      <el-button
        type="primary"
        class="dialog-btn dialog-btn-primary"
        :disabled="!selectedFile || importLoading"
        @click="submitImport"
      >
        <el-icon v-if="importLoading"><Loading /></el-icon>
        {{ importLoading ? '导入中...' : '开始导入' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ArrowDown, Delete, Document, Loading, Plus, Upload } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getProblemLanguages, getProblemReferenceSolutions } from '@/api/admin'
import { getProblemDetail } from '@/api/problem'
import { getRuntimeLanguages } from '@/api/runtime'
import {
  getRuntimeDefaultFileName,
  getRuntimeDefaultStarterCode,
  getRuntimeLanguageDefinition,
  getRuntimeLanguageLabel,
  normalizeRuntimeLanguage
} from '@/utils/runtimeLanguage'

const props = defineProps({
  problems: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  importLoading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['save-problem', 'delete-problem', 'import-problems'])

const problemDialogVisible = ref(false)
const dialogTitle = ref('添加题目')
const dialogLoading = ref(false)
const activeEditorTab = ref('basic')
const runtimeLanguages = ref([])
const enabledLanguageCodes = ref([])
const problemForm = reactive({
  id: null,
  title: '',
  difficulty: 0,
  content: '',
  input: '',
  output: '',
  sampleExplanation: '',
  timeLimit: 1000,
  memoryLimit: 262144,
  defaultLanguage: 'java',
  supportedLanguages: [],
  referenceSolutions: []
})

const importDialogVisible = ref(false)
const importDialogTitle = ref('导入题目')
const importType = ref('excel')
const importFileType = ref('.xlsx')
const importFileExt = ref('Excel')
const selectedFile = ref(null)

const runtimeLanguageMap = computed(() =>
  runtimeLanguages.value.reduce((acc, item) => {
    acc[item.code] = item
    return acc
  }, {})
)

const enabledLanguageOptions = computed(() =>
  enabledLanguageCodes.value
    .map((code) => runtimeLanguageMap.value[code] || getRuntimeLanguageDefinition(code))
    .filter(Boolean)
)

const orderedSupportedLanguages = computed(() => {
  const order = enabledLanguageCodes.value
  return [...problemForm.supportedLanguages].sort(
    (a, b) => order.indexOf(a.languageCode) - order.indexOf(b.languageCode)
  )
})

const orderedReferenceSolutions = computed(() => {
  const order = enabledLanguageCodes.value
  return [...problemForm.referenceSolutions].sort(
    (a, b) => order.indexOf(a.language) - order.indexOf(b.language)
  )
})

const getDifficultyClass = (difficulty) => {
  const classes = { 0: 'difficulty-easy', 1: 'difficulty-medium', 2: 'difficulty-hard' }
  return classes[difficulty ?? 0] || ''
}

const getDifficultyText = (difficulty) => {
  const texts = { 0: '简单', 1: '中等', 2: '困难' }
  return texts[difficulty ?? 0] || '未知'
}

const formatLanguage = (language) => getRuntimeLanguageLabel(language)

const createLanguageConfig = (languageCode, overrides = {}) => ({
  languageCode,
  isDefault: problemForm.defaultLanguage === languageCode,
  starterCode: overrides.starterCode ?? getRuntimeDefaultStarterCode(languageCode),
  starterFilename: overrides.starterFilename ?? getRuntimeDefaultFileName(languageCode),
  status: overrides.status || 'ACTIVE',
  sortOrder: overrides.sortOrder ?? enabledLanguageCodes.value.indexOf(languageCode)
})

const parseHints = (rawHints) => {
  if (!rawHints) {
    return {}
  }
  if (typeof rawHints === 'object') {
    return rawHints
  }
  try {
    return JSON.parse(rawHints)
  } catch {
    return {}
  }
}

const createReferenceSolution = (language, overrides = {}) => {
  const hintMap = parseHints(overrides.hints)
  return {
    language,
    solutionCode: overrides.solutionCode || '',
    timeComplexity: overrides.timeComplexity || '',
    spaceComplexity: overrides.spaceComplexity || '',
    explanation: overrides.explanation || '',
    hintLevel1: hintMap['1'] || '',
    hintLevel2: hintMap['2'] || '',
    hintLevel3: hintMap['3'] || ''
  }
}

const resetProblemForm = () => {
  problemForm.id = null
  problemForm.title = ''
  problemForm.difficulty = 0
  problemForm.content = ''
  problemForm.input = ''
  problemForm.output = ''
  problemForm.sampleExplanation = ''
  problemForm.timeLimit = 1000
  problemForm.memoryLimit = 262144
  problemForm.defaultLanguage = runtimeLanguages.value[0]?.code || 'java'
  problemForm.supportedLanguages = [createLanguageConfig(problemForm.defaultLanguage)]
  problemForm.referenceSolutions = [createReferenceSolution(problemForm.defaultLanguage)]
  enabledLanguageCodes.value = [problemForm.defaultLanguage]
}

const syncDefaultLanguage = () => {
  if (!enabledLanguageCodes.value.includes(problemForm.defaultLanguage)) {
    problemForm.defaultLanguage = enabledLanguageCodes.value[0] || ''
  }
  problemForm.supportedLanguages = problemForm.supportedLanguages.map((item, index) => ({
    ...item,
    isDefault: item.languageCode === problemForm.defaultLanguage,
    sortOrder: index
  }))
}

const syncLanguagePayloads = () => {
  const normalizedCodes = enabledLanguageCodes.value.map(normalizeRuntimeLanguage)

  problemForm.supportedLanguages = normalizedCodes.map((code, index) => {
    const existing = problemForm.supportedLanguages.find((item) => item.languageCode === code)
    return {
      ...(existing || createLanguageConfig(code)),
      languageCode: code,
      isDefault: code === problemForm.defaultLanguage,
      sortOrder: index,
      status: 'ACTIVE',
      starterFilename: existing?.starterFilename || getRuntimeDefaultFileName(code),
      starterCode: existing?.starterCode || getRuntimeDefaultStarterCode(code)
    }
  })

  problemForm.referenceSolutions = normalizedCodes.map((code) => {
    const existing = problemForm.referenceSolutions.find((item) => item.language === code)
    return existing || createReferenceSolution(code)
  })

  if (!normalizedCodes.includes(problemForm.defaultLanguage)) {
    problemForm.defaultLanguage = normalizedCodes[0] || ''
  }

  syncDefaultLanguage()
}

const loadRuntimeLanguages = async () => {
  const res = await getRuntimeLanguages()
  if (res.code !== 200) {
    throw new Error(res.msg || '获取语言列表失败')
  }
  runtimeLanguages.value = (res.data || []).filter((item) => item.enabled !== false)
}

const openAddDialog = () => {
  dialogTitle.value = '添加题目'
  activeEditorTab.value = 'basic'
  resetProblemForm()
  problemDialogVisible.value = true
}

const hydrateForm = (problem, languages, solutions) => {
  problemForm.id = problem.id
  problemForm.title = problem.title || ''
  problemForm.difficulty = problem.difficulty ?? 0
  problemForm.content = problem.content || ''
  problemForm.input = problem.input || ''
  problemForm.output = problem.output || ''
  problemForm.sampleExplanation = problem.sampleExplanation || ''
  problemForm.timeLimit = problem.timeLimit || 1000
  problemForm.memoryLimit = problem.memoryLimit || 262144

  const normalizedLanguages = (languages || []).map((item) => ({
    ...item,
    languageCode: normalizeRuntimeLanguage(item.languageCode),
    starterCode: item.starterCode || getRuntimeDefaultStarterCode(item.languageCode),
    starterFilename: item.starterFilename || getRuntimeDefaultFileName(item.languageCode),
    status: item.status || 'ACTIVE',
    sortOrder: item.sortOrder ?? 0
  }))

  enabledLanguageCodes.value = normalizedLanguages.map((item) => item.languageCode)
  problemForm.defaultLanguage =
    normalizedLanguages.find((item) => Number(item.isDefault) === 1)?.languageCode ||
    normalizeRuntimeLanguage(problem.language) ||
    enabledLanguageCodes.value[0] ||
    runtimeLanguages.value[0]?.code ||
    'java'

  problemForm.supportedLanguages = normalizedLanguages.length
    ? normalizedLanguages
    : [createLanguageConfig(problemForm.defaultLanguage)]

  problemForm.referenceSolutions = (solutions || []).map((item) =>
    createReferenceSolution(normalizeRuntimeLanguage(item.language), item)
  )

  syncLanguagePayloads()
}

const openEditDialog = async (problem) => {
  dialogTitle.value = '编辑题目'
  activeEditorTab.value = 'basic'
  dialogLoading.value = true
  problemDialogVisible.value = true

  try {
    const [detailRes, languagesRes, solutionsRes] = await Promise.all([
      getProblemDetail(problem.id),
      getProblemLanguages(problem.id),
      getProblemReferenceSolutions(problem.id)
    ])

    if (detailRes.code !== 200) {
      throw new Error(detailRes.msg || '获取题目详情失败')
    }

    hydrateForm(
      detailRes.data || problem,
      languagesRes.code === 200 ? languagesRes.data || [] : [],
      solutionsRes.code === 200 ? solutionsRes.data || [] : []
    )
  } catch (error) {
    ElMessage.error(error.message || '加载题目配置失败')
    problemDialogVisible.value = false
  } finally {
    dialogLoading.value = false
  }
}

const closeProblemDialog = () => {
  problemDialogVisible.value = false
}

const buildHintsPayload = (item) => {
  const hints = {}
  if (item.hintLevel1?.trim()) hints['1'] = item.hintLevel1.trim()
  if (item.hintLevel2?.trim()) hints['2'] = item.hintLevel2.trim()
  if (item.hintLevel3?.trim()) hints['3'] = item.hintLevel3.trim()
  return hints
}

const submitProblem = () => {
  if (!problemForm.title.trim()) {
    ElMessage.warning('请输入题目标题')
    activeEditorTab.value = 'basic'
    return
  }

  if (!problemForm.content.trim()) {
    ElMessage.warning('请输入题目描述')
    activeEditorTab.value = 'basic'
    return
  }

  if (enabledLanguageCodes.value.length === 0) {
    ElMessage.warning('请至少启用一种语言')
    activeEditorTab.value = 'languages'
    return
  }

  if (!enabledLanguageCodes.value.includes(problemForm.defaultLanguage)) {
    ElMessage.warning('默认语言必须包含在启用语言中')
    activeEditorTab.value = 'languages'
    return
  }

  const missingSolutions = orderedReferenceSolutions.value.filter((item) => !item.solutionCode?.trim())
  if (missingSolutions.length > 0) {
    ElMessage.warning(`请补全 ${formatLanguage(missingSolutions[0].language)} 的参考答案`)
    activeEditorTab.value = 'solutions'
    return
  }

  const payload = {
    id: problemForm.id,
    title: problemForm.title.trim(),
    difficulty: problemForm.difficulty,
    language: problemForm.defaultLanguage,
    defaultLanguage: problemForm.defaultLanguage,
    content: problemForm.content.trim(),
    input: problemForm.input,
    output: problemForm.output,
    sampleExplanation: problemForm.sampleExplanation,
    timeLimit: problemForm.timeLimit,
    memoryLimit: problemForm.memoryLimit,
    supportedLanguages: orderedSupportedLanguages.value.map((item, index) => ({
      languageCode: item.languageCode,
      isDefault: item.languageCode === problemForm.defaultLanguage,
      starterCode: item.starterCode,
      starterFilename: item.starterFilename,
      status: 'ACTIVE',
      sortOrder: index
    })),
    referenceSolutions: orderedReferenceSolutions.value.map((item) => ({
      language: item.language,
      solutionCode: item.solutionCode,
      timeComplexity: item.timeComplexity,
      spaceComplexity: item.spaceComplexity,
      explanation: item.explanation,
      hints: buildHintsPayload(item)
    }))
  }

  emit('save-problem', {
    payload,
    done: closeProblemDialog
  })
}

const openImportDialog = (type) => {
  importType.value = type
  if (type === 'excel') {
    importFileType.value = '.xlsx'
    importFileExt.value = 'Excel'
  } else {
    importFileType.value = '.csv'
    importFileExt.value = 'CSV'
  }
  importDialogTitle.value = `导入${importFileExt.value}题目`
  selectedFile.value = null
  importDialogVisible.value = true
}

const closeImportDialog = () => {
  importDialogVisible.value = false
}

const handleFileChange = (file) => {
  selectedFile.value = file.raw
}

const beforeUpload = (file) => {
  const fileSize = file.size / 1024 / 1024
  if (fileSize > 10) {
    ElMessage.error('文件大小不能超过 10MB')
    return false
  }
  return false
}

const submitImport = () => {
  if (!selectedFile.value) {
    ElMessage.error('请选择要导入的文件')
    return
  }

  emit('import-problems', {
    type: importType.value,
    file: selectedFile.value,
    done: closeImportDialog
  })
}

onMounted(async () => {
  try {
    await loadRuntimeLanguages()
    resetProblemForm()
  } catch (error) {
    ElMessage.error(error.message || '获取语言列表失败')
  }
})
</script>

<style scoped>
.header-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.problem-dialog,
.import-dialog {
  border-radius: 12px;
}

:deep(.problem-dialog),
:deep(.import-dialog) {
  background: var(--bg-card);
}

:deep(.problem-dialog .el-dialog__header),
:deep(.import-dialog .el-dialog__header) {
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-color);
  background: var(--bg-card);
}

:deep(.problem-dialog .el-dialog__title),
:deep(.import-dialog .el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

:deep(.problem-dialog .el-dialog__body),
:deep(.import-dialog .el-dialog__body) {
  padding: 24px;
  background: var(--bg-card);
}

:deep(.problem-dialog .el-dialog__footer),
:deep(.import-dialog .el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid var(--border-color);
  background: var(--bg-card);
}

.problem-dialog-body {
  min-height: 420px;
}

.problem-form,
.import-content {
  max-width: 100%;
}

.language-config-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.language-card {
  border: 1px solid var(--border-color);
  border-radius: 10px;
  padding: 16px;
  background: var(--bg-input);
}

.language-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  gap: 12px;
}

.default-flag {
  margin-left: 8px;
  color: var(--brand-primary);
  font-size: 12px;
  font-weight: 600;
}

.filename-hint {
  color: var(--text-secondary);
  font-size: 12px;
}

.config-empty {
  margin-top: 20px;
}

:deep(.problem-form .el-form-item__label) {
  color: var(--text-secondary);
  font-weight: 500;
}

:deep(.problem-form .el-input__wrapper),
:deep(.problem-form .el-select .el-input__wrapper),
:deep(.problem-form .el-input-number .el-input__wrapper),
:deep(.language-card .el-input__wrapper),
:deep(.language-card .el-textarea__inner) {
  background: var(--bg-input);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  transition: all 0.2s ease;
}

:deep(.problem-form .el-input__wrapper:hover),
:deep(.problem-form .el-select .el-input__wrapper:hover),
:deep(.problem-form .el-textarea__inner:hover),
:deep(.language-card .el-input__wrapper:hover),
:deep(.language-card .el-textarea__inner:hover) {
  border-color: var(--brand-primary);
}

:deep(.problem-form .el-input__wrapper.is-focus),
:deep(.problem-form .el-select .el-input__wrapper.is-focus),
:deep(.problem-form .el-textarea__inner:focus),
:deep(.language-card .el-input__wrapper.is-focus),
:deep(.language-card .el-textarea__inner:focus) {
  border-color: var(--brand-primary);
  box-shadow: 0 0 0 3px var(--brand-primary-light);
}

.dialog-btn {
  border-radius: 6px;
  font-weight: 500;
  padding: 10px 24px;
  transition: all 0.2s ease;
}

.dialog-btn:hover {
  transform: translateY(-1px);
}

.dialog-btn-primary {
  background: var(--brand-primary);
  border-color: var(--brand-primary);
}

.dialog-btn-primary:hover {
  background: var(--brand-primary-hover);
  border-color: var(--brand-primary-hover);
}

.import-hint {
  margin-bottom: 20px;
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.5;
}

:deep(.upload-demo .el-upload) {
  margin-bottom: 20px;
}

:deep(.upload-demo .el-upload__tip) {
  font-size: 12px;
  color: var(--text-secondary);
}

.selected-file {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  color: var(--text-primary);
}

.language-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  background: var(--brand-primary-light);
  color: var(--brand-primary);
}

@media (max-width: 900px) {
  :deep(.problem-dialog) {
    width: 96vw !important;
  }
}
</style>
