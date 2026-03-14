<template>
  <div class="admin-page">
    <!-- 管理员安全提示 -->
    <div class="admin-banner">
      <el-icon class="banner-icon"><Warning /></el-icon>
      <div class="banner-content">
        <h3>管理员模式</h3>
        <p>您正在访问管理后台，请谨慎操作。所有操作都会被记录。</p>
      </div>
    </div>
    
    <!-- 系统统计仪表盘 -->
    <div class="dashboard-header">
      <div class="dashboard-title">
        <h2>管理后台</h2>
        <span class="dashboard-subtitle">系统数据总览与管理</span>
      </div>
      <el-button type="primary" link @click="fetchStatistics" class="refresh-btn">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <el-row :gutter="20" class="dashboard-row">
      <el-col :span="6">
        <div class="stat-card" v-loading="statsLoading">
          <div class="stat-card-header">
            <span class="stat-card-title">用户总数</span>
            <el-icon class="stat-card-icon" color="#007AFF"><User /></el-icon>
          </div>
          <div class="stat-card-body">
            <div class="stat-value">{{ statistics.userCount || 0 }}</div>
            <div class="stat-trend">
              <span class="trend-label">注册用户</span>
            </div>
          </div>
          <div class="stat-card-footer">
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: '100%' }"></div>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" v-loading="statsLoading">
          <div class="stat-card-header">
            <span class="stat-card-title">题目总数</span>
            <el-icon class="stat-card-icon" color="#00B42A"><Document /></el-icon>
          </div>
          <div class="stat-card-body">
            <div class="stat-value">{{ statistics.problemCount || 0 }}</div>
            <div class="stat-trend">
              <span class="trend-label">在线题目</span>
            </div>
          </div>
          <div class="stat-card-footer">
            <div class="progress-bar">
              <div class="progress-fill success" :style="{ width: '100%' }"></div>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" v-loading="statsLoading">
          <div class="stat-card-header">
            <span class="stat-card-title">提交总数</span>
            <el-icon class="stat-card-icon" color="#FFB400"><Files /></el-icon>
          </div>
          <div class="stat-card-body">
            <div class="stat-value">{{ statistics.submitCount || 0 }}</div>
            <div class="stat-trend">
              <span class="trend-label">累计提交</span>
            </div>
          </div>
          <div class="stat-card-footer">
            <div class="progress-bar">
              <div class="progress-fill warning" :style="{ width: '100%' }"></div>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" v-loading="statsLoading">
          <div class="stat-card-header">
            <span class="stat-card-title">整体正确率</span>
            <el-icon class="stat-card-icon" color="#EE4D38"><CircleCheck /></el-icon>
          </div>
          <div class="stat-card-body">
            <div class="stat-value">{{ statistics.correctRate || '0%' }}</div>
            <div class="stat-trend">
              <span class="trend-label">系统表现</span>
            </div>
          </div>
          <div class="stat-card-footer">
            <div class="progress-bar">
              <div class="progress-fill error" :style="{ width: (statistics.correctRate ? parseFloat(statistics.correctRate) : 0) + '%' }"></div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 用户管理 -->
    <el-card class="management-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">用户管理</span>
          <el-button type="primary" link @click="fetchUsers">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      <el-table :data="userList" stripe v-loading="usersLoading" max-height="500" class="admin-table" table-layout="fixed">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="150" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 1 ? 'danger' : 'primary'" size="small">
              {{ row.role === 1 ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="language" label="语言" width="100">
          <template #default="{ row }">
            <span class="language-badge">{{ row.language ? row.language.toUpperCase() : '' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
      </el-table>
    </el-card>

    <!-- 题目管理 -->
    <el-card class="management-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">题目管理</span>
          <div class="header-buttons">
            <el-button type="primary" @click="showAddDialog" class="add-btn">
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
                  <el-dropdown-item @click="showImportDialog('excel')">
                    <el-icon><Document /></el-icon>
                    Excel文件
                  </el-dropdown-item>
                  <el-dropdown-item @click="showImportDialog('csv')">
                    <el-icon><Document /></el-icon>
                    CSV文件
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </template>
      <el-table :data="problemList" stripe v-loading="problemsLoading" max-height="500" class="admin-table" table-layout="fixed">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="250" />
        <el-table-column prop="difficulty" label="难度" width="100">
          <template #default="{ row }">
            <span :class="['difficulty-badge', getDifficultyClass(row.difficulty)]">
              {{ getDifficultyText(row.difficulty) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="language" label="语言" width="100">
          <template #default="{ row }">
            <span class="language-badge">{{ row.language ? row.language.toUpperCase() : '' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button type="primary" link @click="showEditDialog(row)" class="action-btn">编辑</el-button>
            <el-button type="danger" link @click="deleteProblem(row.id)" class="action-btn">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 提交记录 -->
    <el-card class="management-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">提交记录</span>
          <el-button type="primary" link @click="fetchAllSubmissions">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      <el-table :data="submissionList" stripe v-loading="submissionsLoading" max-height="500" class="admin-table" table-layout="fixed">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="problemId" label="题目ID" width="100">
          <template #default="{ row }">
            <span class="problem-id-link" @click="goToProblem(row.problemId)">
              #{{ row.problemId }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="language" label="语言" width="100">
          <template #default="{ row }">
            <span class="language-badge">{{ row.language ? row.language.toUpperCase() : '' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="result" label="结果" width="100">
          <template #default="{ row }">
            <span :class="['result-tag', getResultClass(row.result)]">
              {{ getResultText(row.result) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="timeCost" label="耗时" width="100">
          <template #default="{ row }">
            <span class="time-cost">{{ row.timeCost }}ms</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="180" />
      </el-table>
    </el-card>

    <!-- 题目编辑弹窗 -->
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
        <el-button @click="problemDialogVisible = false" class="dialog-btn">取消</el-button>
        <el-button type="primary" @click="saveProblem" class="dialog-btn dialog-btn-primary">确定</el-button>
      </template>
    </el-dialog>

    <!-- 题目导入弹窗 -->
    <el-dialog v-model="importDialogVisible" :title="importDialogTitle" width="500px" class="import-dialog">
      <div class="import-content">
        <p class="import-hint">请选择要导入的文件，支持 Excel (.xlsx) 和 CSV 格式</p>
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
              只能上传 {{ importFileExt }} 文件，且不超过 10MB
            </div>
          </template>
        </el-upload>
        <div v-if="selectedFile" class="selected-file">
          <el-icon><Document /></el-icon>
          <span>{{ selectedFile.name }}</span>
          <el-button type="text" @click="selectedFile = null">
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
      </div>
      <template #footer>
        <el-button @click="importDialogVisible = false" class="dialog-btn">取消</el-button>
        <el-button type="primary" @click="importProblems" :disabled="!selectedFile || importLoading" class="dialog-btn dialog-btn-primary">
          <el-icon v-if="importLoading"><Loading /></el-icon>
          {{ importLoading ? '导入中...' : '开始导入' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Refresh,
  User,
  Document,
  Files,
  CircleCheck,
  Plus,
  Warning,
  Upload,
  Delete,
  Loading,
  ArrowDown
} from '@element-plus/icons-vue'
import {
  getUserList,
  addProblem,
  updateProblem,
  deleteProblem as deleteProblemApi,
  getAllSubmissions,
  getStatistics,
  importFromExcel,
  importFromCsv
} from '@/api/admin'
import { getProblemList } from '@/api/problem'

const router = useRouter()

const statsLoading = ref(false)
const usersLoading = ref(false)
const problemsLoading = ref(false)
const submissionsLoading = ref(false)

const statistics = ref({})
const userList = ref([])
const problemList = ref([])
const submissionList = ref([])

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

// 导入功能相关变量
const importDialogVisible = ref(false)
const importDialogTitle = ref('导入题目')
const importType = ref('excel')
const importFileType = ref('.xlsx')
const importFileExt = ref('Excel')
const selectedFile = ref(null)
const importLoading = ref(false)

const getDifficultyClass = (difficulty) => {
  const classes = { 0: 'difficulty-easy', 1: 'difficulty-medium', 2: 'difficulty-hard' }
  return classes[difficulty ?? 0] || ''
}

const getDifficultyText = (difficulty) => {
  const texts = { 0: '简单', 1: '中等', 2: '困难' }
  return texts[difficulty ?? 0] || '未知'
}

const getResultClass = (result) => {
  const classes = { 0: 'result-ac', 1: 'result-wa', 2: 'result-re', 3: 'result-tle', 4: 'result-mle' }
  return classes[result ?? 0] || 'result-unknown'
}

const getResultText = (result) => {
  const texts = { 0: '通过', 1: '答案错误', 2: '运行错误', 3: '超时', 4: '内存超限' }
  return texts[result ?? 0] || '未知'
}

const fetchStatistics = async () => {
  statsLoading.value = true
  try {
    const res = await getStatistics()
    if (res.code === 200) {
      statistics.value = res.data
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  } finally {
    statsLoading.value = false
  }
}

const fetchUsers = async () => {
  usersLoading.value = true
  try {
    const res = await getUserList({ page: 1, size: 100 })
    if (res.code === 200) {
      userList.value = res.data?.list || []
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
  } finally {
    usersLoading.value = false
  }
}

const fetchProblems = async () => {
  problemsLoading.value = true
  try {
    const res = await getProblemList({ page: 1, size: 100 })
    if (res.code === 200) {
      problemList.value = res.data.list || []
    }
  } catch (error) {
    console.error('获取题目列表失败:', error)
  } finally {
    problemsLoading.value = false
  }
}

const fetchAllSubmissions = async () => {
  submissionsLoading.value = true
  try {
    const res = await getAllSubmissions({ page: 1, size: 100 })
    if (res.code === 200) {
      submissionList.value = res.data?.list || []
    }
  } catch (error) {
    console.error('获取提交记录失败:', error)
  } finally {
    submissionsLoading.value = false
  }
}

const showAddDialog = () => {
  dialogTitle.value = '添加题目'
  problemForm.id = null
  problemForm.title = ''
  problemForm.difficulty = 0
  problemForm.language = 'java'
  problemForm.content = ''
  problemForm.input = ''
  problemForm.output = ''
  problemDialogVisible.value = true
}

const showEditDialog = (problem) => {
  dialogTitle.value = '编辑题目'
  problemForm.id = problem.id
  problemForm.title = problem.title
  problemForm.difficulty = problem.difficulty
  problemForm.language = problem.language
  problemForm.content = problem.content
  problemForm.input = problem.input || ''
  problemForm.output = problem.output || ''
  problemDialogVisible.value = true
}

const saveProblem = async () => {
  try {
    let res
    if (problemForm.id) {
      res = await updateProblem(problemForm)
    } else {
      res = await addProblem(problemForm)
    }

    if (res.code === 200) {
      ElMessage.success(problemForm.id ? '更新成功' : '添加成功')
      problemDialogVisible.value = false
      fetchProblems()
      fetchStatistics()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败，请重试')
  }
}

const deleteProblem = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该题目吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await deleteProblemApi(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchProblems()
      fetchStatistics()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败，请重试')
    }
  }
}

const goToProblem = (id) => {
  router.push(`/problem/${id}`)
}

// 导入功能相关方法
const showImportDialog = (type) => {
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

const handleFileChange = (file) => {
  selectedFile.value = file.raw
}

const beforeUpload = (file) => {
  const fileSize = file.size / 1024 / 1024
  if (fileSize > 10) {
    ElMessage.error('文件大小不能超过 10MB')
    return false
  }
  return false // 阻止自动上传，我们手动处理
}

const importProblems = async () => {
  if (!selectedFile.value) {
    ElMessage.error('请选择要导入的文件')
    return
  }

  importLoading.value = true
  try {
    let res
    if (importType.value === 'excel') {
      res = await importFromExcel(selectedFile.value)
    } else {
      res = await importFromCsv(selectedFile.value)
    }

    if (res.code === 200) {
      ElMessage.success('导入成功')
      importDialogVisible.value = false
      fetchProblems()
      fetchStatistics()
    } else {
      ElMessage.error(res.msg || '导入失败')
    }
  } catch (error) {
    console.error('导入失败:', error)
    ElMessage.error('导入失败，请重试')
  } finally {
    importLoading.value = false
  }
}

onMounted(() => {
  fetchStatistics()
  fetchUsers()
  fetchProblems()
  fetchAllSubmissions()
})
</script>

<style scoped>
.admin-page {
  width: 100%;
  min-height: 100vh;
  background: var(--leetcode-bg-secondary, #F7F8FA);
  padding: 32px;
  box-sizing: border-box;
}

/* 管理员横幅 */
.admin-banner {
  display: flex;
  align-items: center;
  gap: 16px;
  background: linear-gradient(135deg, rgba(238, 77, 56, 0.1) 0%, rgba(238, 77, 56, 0.05) 100%);
  border: 1px solid rgba(238, 77, 56, 0.3);
  border-radius: 12px;
  padding: 16px 24px;
  margin-bottom: 24px;
  animation: bannerPulse 2s ease-in-out infinite;
}

@keyframes bannerPulse {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(238, 77, 56, 0.2);
  }
  50% {
    box-shadow: 0 0 0 8px rgba(238, 77, 56, 0);
  }
}

.banner-icon {
  font-size: 32px;
  color: var(--leetcode-danger, #EE4D38);
  flex-shrink: 0;
}

.banner-content h3 {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--leetcode-danger, #EE4D38);
}

.banner-content p {
  margin: 0;
  font-size: 13px;
  color: var(--leetcode-text-secondary, #6B7280);
}

/* 仪表盘头部 */
.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 0 8px;
}

.dashboard-title h2 {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 4px 0;
}

.dashboard-subtitle {
  font-size: 14px;
  color: var(--text-tertiary);
}

.refresh-btn {
  color: var(--brand-primary);
  font-weight: 500;
  transition: all 0.2s ease;
}

.refresh-btn:hover {
  color: var(--brand-primary-hover);
}

/* 仪表盘行 */
.dashboard-row {
  margin-bottom: 24px;
}

/* 统计卡片 */
.stat-card {
  background: var(--leetcode-bg, #FFFFFF);
  border-radius: 12px;
  padding: 24px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--leetcode-border, #E5E7EB);
  transition: all 0.3s ease;
  height: 100%;
}

.stat-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.stat-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.stat-card-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
}

.stat-card-icon {
  font-size: 24px;
}

.stat-card-body {
  margin-bottom: 16px;
}

.stat-value {
  font-size: 36px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1;
  margin-bottom: 8px;
}

.stat-trend {
  font-size: 13px;
  color: var(--text-tertiary);
}

.trend-label {
  font-weight: 500;
}

.stat-card-footer {
  margin-top: auto;
}

.progress-bar {
  height: 6px;
  background: var(--bg-tertiary);
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: var(--brand-primary);
  border-radius: 3px;
  transition: width 0.6s ease;
}

.progress-fill.success {
  background: var(--success-color);
}

.progress-fill.warning {
  background: var(--warning-color);
}

.progress-fill.error {
  background: var(--error-color);
}

/* 卡片通用样式 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: 0.3px;
}

/* 管理卡片 */
.management-card {
  margin-bottom: 32px;
  border-radius: 12px;
  border: 1px solid var(--leetcode-border, #E5E7EB);
  background: var(--leetcode-bg, #FFFFFF);
}

.management-card:last-of-type {
  margin-bottom: 0;
}

:deep(.management-card .el-card__header) {
  background: var(--leetcode-bg, #FFFFFF);
  border-bottom: 1px solid var(--leetcode-border, #E5E7EB);
}

:deep(.management-card .el-card__body) {
  background: var(--leetcode-bg, #FFFFFF);
}

/* 管理表格 */
.admin-table {
  width: 100%;
}

:deep(.admin-table) {
  width: 100% !important;
}

:deep(.admin-table .el-table__inner-wrapper) {
  width: 100% !important;
}

:deep(.admin-table .el-table__header) {
  background: var(--leetcode-bg-secondary, #F7F8FA);
}

:deep(.admin-table .el-table__header th) {
  background: var(--leetcode-bg-secondary, #F7F8FA);
  color: var(--leetcode-text, #24292F);
  font-weight: 600;
  font-size: 13px;
  border-color: var(--leetcode-border, #E5E7EB);
}

:deep(.admin-table .el-table__body td) {
  color: var(--leetcode-text, #24292F);
  font-size: 14px;
  border-color: var(--leetcode-border, #E5E7EB);
}

:deep(.admin-table .el-table__row:hover) {
  background: var(--leetcode-bg-secondary, #F7F8FA) !important;
}

:deep(.admin-table .el-table__row:hover td) {
  background: var(--leetcode-bg-secondary, #F7F8FA) !important;
}

:deep(.admin-table .el-table__empty-block) {
  background: var(--leetcode-bg, #FFFFFF);
}

:deep(.admin-table .el-table__empty-text) {
  color: var(--leetcode-text-secondary, #6B7280);
}

/* 语言标签 */
.language-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  background: var(--info-light);
  color: var(--info-color);
}

/* 难度标签 */
.difficulty-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.difficulty-easy {
  background: var(--difficulty-easy-bg);
  color: var(--difficulty-easy-color);
}

.difficulty-medium {
  background: var(--difficulty-medium-bg);
  color: var(--difficulty-medium-color);
}

.difficulty-hard {
  background: var(--difficulty-hard-bg);
  color: var(--difficulty-hard-color);
}

/* 结果标签 */
.result-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.result-ac {
  background: var(--difficulty-easy-bg);
  color: var(--difficulty-easy-color);
}

.result-wa {
  background: var(--difficulty-hard-bg);
  color: var(--difficulty-hard-color);
}

.result-re {
  background: var(--difficulty-hard-bg);
  color: var(--difficulty-hard-color);
}

.result-tle {
  background: var(--difficulty-medium-bg);
  color: var(--difficulty-medium-color);
}

.result-mle {
  background: var(--difficulty-medium-bg);
  color: var(--difficulty-medium-color);
}

.result-unknown {
  background: var(--info-light);
  color: var(--info-color);
}

/* 耗时 */
.time-cost {
  font-family: 'Fira Code', 'Monaco', 'Consolas', monospace;
  font-size: 13px;
  color: var(--text-secondary);
}

/* 题目ID链接 */
.problem-id-link {
  color: var(--brand-primary);
  font-weight: 500;
  cursor: pointer;
  transition: color 0.2s ease;
}

.problem-id-link:hover {
  color: var(--brand-primary-hover);
  text-decoration: underline;
}

/* 操作按钮 */
.action-btn {
  font-weight: 500;
  transition: all 0.2s ease;
}

.action-btn:hover {
  transform: translateX(2px);
}

/* 添加按钮 */
.add-btn {
  background: var(--brand-primary);
  border-color: var(--brand-primary);
  border-radius: 6px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.add-btn:hover {
  background: var(--brand-primary-hover);
  border-color: var(--brand-primary-hover);
  transform: translateY(-1px);
}

/* 弹窗 */
.problem-dialog {
  border-radius: 12px;
}

:deep(.problem-dialog) {
  background: var(--bg-card);
}

:deep(.problem-dialog .el-dialog__header) {
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-color);
  background: var(--bg-card);
}

:deep(.problem-dialog .el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

:deep(.problem-dialog .el-dialog__body) {
  padding: 24px;
  background: var(--bg-card);
}

:deep(.problem-dialog .el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid var(--border-color);
  background: var(--bg-card);
}

/* 表单 */
.problem-form {
  max-width: 100%;
}

:deep(.problem-form .el-form-item__label) {
  color: var(--text-secondary);
  font-weight: 500;
}

:deep(.problem-form .el-input__wrapper) {
  background: var(--bg-input);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  transition: all 0.2s ease;
}

:deep(.problem-form .el-input__wrapper:hover) {
  border-color: var(--brand-primary);
}

:deep(.problem-form .el-input__wrapper.is-focus) {
  border-color: var(--brand-primary);
  box-shadow: 0 0 0 3px var(--brand-primary-light);
}

:deep(.problem-form .el-textarea__inner) {
  background: var(--bg-input);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  transition: all 0.2s ease;
}

:deep(.problem-form .el-textarea__inner:hover) {
  border-color: var(--brand-primary);
}

:deep(.problem-form .el-textarea__inner:focus) {
  border-color: var(--brand-primary);
  box-shadow: 0 0 0 3px var(--brand-primary-light);
}

:deep(.problem-form .el-select .el-input__wrapper) {
  background: var(--bg-input);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  transition: all 0.2s ease;
}

:deep(.problem-form .el-select .el-input__wrapper:hover) {
  border-color: var(--brand-primary);
}

:deep(.problem-form .el-select .el-input__wrapper.is-focus) {
  border-color: var(--brand-primary);
  box-shadow: 0 0 0 3px var(--brand-primary-light);
}



/* 弹窗按钮 */
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

/* 导入功能样式 */
.header-buttons {
  display: flex;
  gap: 12px;
}

/* 导入弹窗 */
.import-dialog {
  border-radius: 12px;
}

:deep(.import-dialog) {
  background: var(--bg-card);
}

:deep(.import-dialog .el-dialog__header) {
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-color);
  background: var(--bg-card);
}

:deep(.import-dialog .el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

:deep(.import-dialog .el-dialog__body) {
  padding: 24px;
  background: var(--bg-card);
}

:deep(.import-dialog .el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid var(--border-color);
  background: var(--bg-card);
}

/* 导入内容 */
.import-content {
  max-width: 100%;
}

.import-hint {
  margin-bottom: 20px;
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.5;
}

/* 上传组件 */
:deep(.upload-demo .el-upload) {
  margin-bottom: 20px;
}

:deep(.upload-demo .el-upload__tip) {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: 8px;
}

/* 选中文件 */
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

.selected-file .el-button {
  color: var(--danger-color);
  padding: 0;
  min-width: auto;
}

.selected-file .el-button:hover {
  color: var(--danger-hover);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .admin-page {
    padding: 16px;
  }

  .dashboard-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .dashboard-row {
    flex-direction: column;
  }

  :deep(.admin-table) {
    font-size: 12px;
  }
}
</style>
