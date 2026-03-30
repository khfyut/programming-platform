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
      <el-table-column prop="language" label="语言" width="110" sortable>
        <template #default="{ row }">
          <span class="language-badge">{{ formatLanguage(row.language) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link class="action-btn" @click="openEditDialog(row)">编辑</el-button>
          <el-button type="danger" link class="action-btn" @click="$emit('delete-problem', row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="problemDialogVisible" :title="dialogTitle" width="600px" class="problem-dialog">
    <el-form :model="problemForm" label-width="80px" class="problem-form">
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
      <el-form-item label="语言">
        <el-select v-model="problemForm.language" placeholder="请选择语言">
          <el-option label="Java" value="java" />
          <el-option label="Python" value="python" />
        </el-select>
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="problemForm.content" type="textarea" :rows="6" placeholder="请输入题目描述" />
      </el-form-item>
      <el-form-item label="输入示例">
        <el-input v-model="problemForm.input" placeholder="请输入输入示例" />
      </el-form-item>
      <el-form-item label="输出示例">
        <el-input v-model="problemForm.output" placeholder="请输入输出示例" />
      </el-form-item>
    </el-form>
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
import { reactive, ref } from 'vue'
import { ArrowDown, Delete, Document, Loading, Plus, Upload } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

defineProps({
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
const problemForm = reactive({
  id: null,
  title: '',
  difficulty: 0,
  language: 'java',
  content: '',
  input: '',
  output: ''
})

const importDialogVisible = ref(false)
const importDialogTitle = ref('导入题目')
const importType = ref('excel')
const importFileType = ref('.xlsx')
const importFileExt = ref('Excel')
const selectedFile = ref(null)

const getDifficultyClass = (difficulty) => {
  const classes = { 0: 'difficulty-easy', 1: 'difficulty-medium', 2: 'difficulty-hard' }
  return classes[difficulty ?? 0] || ''
}

const getDifficultyText = (difficulty) => {
  const texts = { 0: '简单', 1: '中等', 2: '困难' }
  return texts[difficulty ?? 0] || '未知'
}

const formatLanguage = (language) => {
  if (!language) return '--'
  return String(language).toUpperCase()
}

const resetProblemForm = () => {
  problemForm.id = null
  problemForm.title = ''
  problemForm.difficulty = 0
  problemForm.language = 'java'
  problemForm.content = ''
  problemForm.input = ''
  problemForm.output = ''
}

const openAddDialog = () => {
  dialogTitle.value = '添加题目'
  resetProblemForm()
  problemDialogVisible.value = true
}

const openEditDialog = (problem) => {
  dialogTitle.value = '编辑题目'
  problemForm.id = problem.id
  problemForm.title = problem.title
  problemForm.difficulty = problem.difficulty
  problemForm.language = problem.language || 'java'
  problemForm.content = problem.content
  problemForm.input = problem.input || ''
  problemForm.output = problem.output || ''
  problemDialogVisible.value = true
}

const closeProblemDialog = () => {
  problemDialogVisible.value = false
}

const submitProblem = () => {
  if (!problemForm.title.trim()) {
    ElMessage.warning('请输入题目标题')
    return
  }

  if (!problemForm.content.trim()) {
    ElMessage.warning('请输入题目描述')
    return
  }

  emit('save-problem', {
    form: {
      id: problemForm.id,
      title: problemForm.title.trim(),
      difficulty: problemForm.difficulty,
      language: problemForm.language,
      content: problemForm.content.trim(),
      input: problemForm.input,
      output: problemForm.output
    },
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

.problem-form,
.import-content {
  max-width: 100%;
}

:deep(.problem-form .el-form-item__label) {
  color: var(--text-secondary);
  font-weight: 500;
}

:deep(.problem-form .el-input__wrapper),
:deep(.problem-form .el-select .el-input__wrapper) {
  background: var(--bg-input);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  transition: all 0.2s ease;
}

:deep(.problem-form .el-input__wrapper:hover),
:deep(.problem-form .el-select .el-input__wrapper:hover),
:deep(.problem-form .el-textarea__inner:hover) {
  border-color: var(--brand-primary);
}

:deep(.problem-form .el-input__wrapper.is-focus),
:deep(.problem-form .el-select .el-input__wrapper.is-focus),
:deep(.problem-form .el-textarea__inner:focus) {
  border-color: var(--brand-primary);
  box-shadow: 0 0 0 3px var(--brand-primary-light);
}

:deep(.problem-form .el-textarea__inner) {
  background: var(--bg-input);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  transition: all 0.2s ease;
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
  color: var(--text-tertiary);
  margin-top: 8px;
}

.selected-file {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: var(--bg-input);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  margin-top: 12px;
}

.selected-file .el-icon {
  color: var(--brand-primary);
  font-size: 18px;
}

.selected-file span {
  flex: 1;
  font-size: 14px;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.selected-file :deep(.el-button) {
  color: var(--danger-color);
  padding: 0;
  min-width: auto;
}

.selected-file :deep(.el-button:hover) {
  color: var(--danger-hover);
}
</style>
