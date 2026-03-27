<template>
  <div class="problem-detail-page">
    <!-- 顶部导航栏 -->
    <div class="problem-header">
      <div class="header-left">
        <!-- 面包屑导航 -->
        <div class="breadcrumb">
          <span class="breadcrumb-item" @click="$router.push('/problems')">
            <el-icon><Collection /></el-icon>
            题库
          </span>
          <el-icon class="breadcrumb-separator"><ArrowRight /></el-icon>
          <span class="breadcrumb-item" @click="$router.push('/problems')">
            算法
          </span>
          <el-icon class="breadcrumb-separator"><ArrowRight /></el-icon>
          <span class="breadcrumb-item current">{{ problem?.title || '题目详情' }}</span>
        </div>
      </div>
      <div class="header-right">
        <el-button type="success" :loading="running" @click="runCode" class="action-btn run-btn">
          <el-icon><VideoPlay /></el-icon>
          运行
        </el-button>
        <el-button type="primary" :loading="submitting" @click="submitCode" class="action-btn submit-btn">
          <el-icon><Promotion /></el-icon>
          提交
        </el-button>
        <!-- 用户信息下拉菜单 -->
        <el-dropdown @command="handleUserCommand" trigger="click" class="user-dropdown" popper-class="user-dropdown-panel">
          <div class="user-info">
            <el-avatar :size="32" :icon="UserFilled" class="user-avatar" />
            <span class="username">{{ userStore.userInfo?.username }}</span>
            <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <div class="user-menu-panel">
              <!-- 用户信息头部 -->
              <div class="user-menu-header">
                <el-avatar :size="48" :icon="UserFilled" class="user-menu-avatar" />
                <div class="user-menu-info">
                  <div class="user-menu-name">{{ userStore.userInfo?.username }}</div>
                  <div class="user-menu-role">{{ userStore.userInfo?.role === 1 ? '管理员' : '普通用户' }}</div>
                </div>
              </div>
              
              <!-- 数据统计卡片 -->
              <div class="user-stats-grid">
                <div class="stat-card" @click="goToProfile('solved')">
                  <div class="stat-icon solved">
                    <el-icon><CircleCheck /></el-icon>
                  </div>
                  <div class="stat-value">{{ userStats.solved }}</div>
                  <div class="stat-label">已解决</div>
                </div>
                <div class="stat-card" @click="goToProfile('submissions')">
                  <div class="stat-icon submissions">
                    <el-icon><Document /></el-icon>
                  </div>
                  <div class="stat-value">{{ userStats.submissions }}</div>
                  <div class="stat-label">提交次数</div>
                </div>
                <div class="stat-card" @click="goToProfile('passRate')">
                  <div class="stat-icon pass-rate">
                    <el-icon><TrendCharts /></el-icon>
                  </div>
                  <div class="stat-value">{{ userStats.passRate }}%</div>
                  <div class="stat-label">通过率</div>
                </div>
                <div class="stat-card" @click="goToProfile('streak')">
                  <div class="stat-icon streak">
                    <el-icon><Timer /></el-icon>
                  </div>
                  <div class="stat-value">{{ userStats.streak }}</div>
                  <div class="stat-label">连续天数</div>
                </div>
              </div>
              
              <!-- 菜单项 -->
              <div class="user-menu-list">
                <div class="menu-item" @click="handleUserCommand('profile')">
                  <el-icon><User /></el-icon>
                  <span>个人主页</span>
                </div>
                <div class="menu-item" @click="handleUserCommand('language')">
                  <el-icon><Setting /></el-icon>
                  <span>设置语言</span>
                </div>
                <div v-if="userStore.userInfo?.role === 1" class="menu-item" @click="handleUserCommand('admin')">
                  <el-icon><Monitor /></el-icon>
                  <span>管理后台</span>
                </div>
                <div class="menu-divider"></div>
                <div class="menu-item logout" @click="handleUserCommand('logout')">
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出登录</span>
                </div>
              </div>
            </div>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 主体内容区 -->
    <div class="problem-content-wrapper">
      <!-- 左侧题目描述 -->
      <div class="left-panel" :style="{ width: leftPanelWidth + '%' }">
        <div class="panel-tabs">
          <div 
            v-for="tab in tabs" 
            :key="tab.key"
            class="tab-item"
            :class="{ active: activeTab === tab.key }"
            @click="activeTab = tab.key"
          >
            {{ tab.label }}
          </div>
        </div>
        
        <div class="panel-content" v-loading="loading">
          <!-- 题目描述 -->
          <div v-if="activeTab === 'description'" class="description-content">
            <div class="content-block">
              <div class="problem-description">{{ problem?.content }}</div>
            </div>
            
            <!-- 示例 -->
            <div v-if="problem?.testCases?.length > 0" class="content-block">
              <h3 class="block-title">示例</h3>
              <div class="examples-list">
                <div 
                  v-for="(testCase, index) in problem.testCases" 
                  :key="index"
                  class="example-item"
                >
                  <div class="example-header">
                    <span class="example-index">示例 {{ index + 1 }}</span>
                  </div>
                  <div class="example-body">
                    <div class="example-row">
                      <span class="example-label">输入：</span>
                      <pre class="example-code">{{ testCase.input }}</pre>
                    </div>
                    <div class="example-row">
                      <span class="example-label">输出：</span>
                      <pre class="example-code">{{ testCase.output }}</pre>
                    </div>
                    <div v-if="testCase.explanation" class="example-row">
                      <span class="example-label">解释：</span>
                      <span class="example-text">{{ testCase.explanation }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 提示 -->
            <div v-if="hints.length > 0" class="content-block">
              <h3 class="block-title">提示</h3>
              <div class="hints-list">
                <div 
                  v-for="(hint, index) in hints" 
                  :key="index"
                  class="hint-item"
                >
                  <span class="hint-num">{{ index + 1 }}.</span>
                  <span class="hint-text">{{ hint }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 题解 -->
          <div v-if="activeTab === 'solution'" class="solution-content">
            <ReferenceSolution 
              :problem-id="parseInt(route.params.id)" 
              :can-view="canViewSolution"
              @view-solution="handleViewSolution"
            />
          </div>

          <!-- 提交记录 -->
          <div
            v-if="activeTab === 'submissions'"
            class="submissions-content"
            :class="{ 'has-submissions': submissionList.length > 0 }"
            v-loading="submissionsLoading"
          >
            <div class="submissions-toolbar">
              <span class="submissions-title">当前题目最近提交</span>
              <el-button text size="small" @click="fetchProblemSubmissions">刷新</el-button>
            </div>
            <div v-if="submissionList.length > 0" class="submission-list">
              <div
                v-for="item in submissionList"
                :key="item.id"
                class="submission-row"
                @click="openSubmissionDetail(item)"
              >
                <div class="submission-row-main">
                  <span :class="['submission-status', item.result === 0 ? 'success' : 'error']">
                    {{ getResultText(item.result) }}
                  </span>
                  <span class="submission-language">{{ (item.language || '-').toUpperCase() }}</span>
                </div>
                <div class="submission-row-meta">
                  <span>{{ item.timeCost || 0 }} ms</span>
                  <span>{{ item.memoryCost || 0 }} KB</span>
                  <span>{{ formatDateTime(item.submitTime || item.createTime) }}</span>
                </div>
              </div>
            </div>
            <el-empty description="暂无提交记录" />
          </div>
        </div>
      </div>

      <!-- 拖拽分隔条 -->
      <div class="resizer" @mousedown="startResize"></div>

      <!-- 右侧代码编辑区 -->
      <div class="right-panel" :style="{ width: (100 - leftPanelWidth) + '%' }">
        <!-- 代码编辑器头部 -->
        <div class="editor-header">
          <div class="editor-tabs">
            <div class="editor-tab active">
              <el-icon><Document /></el-icon>
              <span>Solution.{{ language === 'java' ? 'java' : 'py' }}</span>
            </div>
          </div>
          <div class="editor-actions">
            <el-select v-model="language" size="small" @change="handleLanguageChange" class="language-select">
              <el-option label="Java" value="java">
                <span class="lang-option">
                  <span class="lang-icon java">J</span>
                  Java
                </span>
              </el-option>
              <el-option label="Python" value="python">
                <span class="lang-option">
                  <span class="lang-icon python">P</span>
                  Python
                </span>
              </el-option>
            </el-select>
            <el-button text size="small" @click="resetCode" class="reset-btn">
              <el-icon><Refresh /></el-icon>
            </el-button>
          </div>
        </div>

        <!-- 代码编辑器 -->
        <div class="editor-container">
          <MonacoEditor 
            v-model="code" 
            :language="language" 
            height="100%"
            @change="handleCodeChange"
            @ai-action="handleAIAction"
          />
        </div>

        <!-- 测试用例区域 -->
        <div class="testcase-section">
          <div class="testcase-header">
            <div class="testcase-tabs">
              <div 
                v-for="tab in testcaseTabs" 
                :key="tab.key"
                class="testcase-tab"
                :class="{ active: activeTestcaseTab === tab.key }"
                @click="activeTestcaseTab = tab.key"
              >
                {{ tab.label }}
              </div>
            </div>
            <div class="testcase-actions">
              <el-button type="success" size="small" :loading="running" @click="runCode">
                <el-icon><VideoPlay /></el-icon>
                运行
              </el-button>
            </div>
          </div>
          
          <div class="testcase-content">
            <!-- 测试用例 -->
            <div v-if="activeTestcaseTab === 'case'" class="testcase-panel">
              <div class="testcase-list">
                <div 
                  v-for="(tc, index) in testCases" 
                  :key="index"
                  class="testcase-item"
                  :class="{ active: selectedTestCase === index }"
                  @click="selectTestCase(index)"
                >
                  <span class="testcase-name">Case {{ index + 1 }}</span>
                  <el-icon v-if="tc.result === 'passed'" class="testcase-status passed"><CircleCheck /></el-icon>
                  <el-icon v-else-if="tc.result === 'failed'" class="testcase-status failed"><CircleClose /></el-icon>
                </div>
                <div class="testcase-item add-btn" @click="addTestCase">
                  <el-icon><Plus /></el-icon>
                </div>
              </div>
              <div class="testcase-detail" v-if="selectedTestCase !== null && testCases[selectedTestCase]">
                <div class="input-section">
                  <div class="section-label">
                    <span>输入：</span>
                  </div>
                  <el-input
                    v-model="testCases[selectedTestCase].input"
                    type="textarea"
                    :rows="3"
                    class="testcase-input"
                  />
                </div>
                <div class="output-section" v-if="testCases[selectedTestCase].output">
                  <div class="section-label">
                    <span>预期输出：</span>
                  </div>
                  <pre class="expected-output">{{ testCases[selectedTestCase].output }}</pre>
                </div>
              </div>
            </div>

            <!-- 测试结果 -->
            <div v-if="activeTestcaseTab === 'result'" class="result-panel">
              <div v-if="!result" class="no-result">
                <div class="empty-state">
                  <el-icon class="empty-icon"><VideoPlay /></el-icon>
                  <p>点击"运行"查看结果</p>
                </div>
              </div>
              <div v-else class="result-display">
                <div class="result-summary" :class="result.result === 0 ? 'success' : 'error'">
                  <el-icon v-if="result.result === 0" class="result-icon"><CircleCheck /></el-icon>
                  <el-icon v-else class="result-icon"><CircleClose /></el-icon>
                  <span class="result-text">{{ getResultText(result.result) }}</span>
                  <span class="result-time" v-if="result.timeCost">{{ result.timeCost }} ms</span>
                  <span class="result-memory" v-if="result.memoryCost">{{ result.memoryCost }} KB</span>
                </div>
                
                <div class="result-details" v-if="result.testCaseResults?.length > 0">
                  <div 
                    v-for="(tc, index) in result.testCaseResults" 
                    :key="index"
                    class="result-item"
                    :class="tc.result === 0 ? 'passed' : 'failed'"
                  >
                    <div class="result-item-header">
                      <span class="result-item-name">测试用例 {{ index + 1 }}</span>
                      <el-tag :type="tc.result === 0 ? 'success' : 'danger'" size="small">
                        {{ tc.result === 0 ? '通过' : '失败' }}
                      </el-tag>
                    </div>
                    <div class="result-item-body" v-if="tc.result !== 0">
                      <div class="result-row">
                        <span class="result-label">输入：</span>
                        <pre>{{ tc.input }}</pre>
                      </div>
                      <div class="result-row">
                        <span class="result-label">预期输出：</span>
                        <pre>{{ tc.expectedOutput }}</pre>
                      </div>
                      <div class="result-row">
                        <span class="result-label">实际输出：</span>
                        <pre>{{ tc.actualOutput || '无输出' }}</pre>
                      </div>
                      <div class="result-row" v-if="tc.errorMessage">
                        <span class="result-label">错误：</span>
                        <pre class="error-message">{{ tc.errorMessage }}</pre>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- AI助手对话框 -->
    <el-dialog v-model="detailDialogVisible" title="提交详情" width="800px" class="detail-dialog">
      <div v-loading="detailLoading" class="detail-content">
        <div v-if="currentSubmission">
          <div class="detail-section">
            <h3 class="detail-title">基本信息</h3>
            <div class="detail-grid">
              <div class="detail-item">
                <span class="detail-label">题目</span>
                <span class="detail-value">{{ currentSubmission.problemTitle || problem?.title }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">结果</span>
                <span :class="['detail-value', 'result-' + currentSubmission.result]">
                  {{ getResultText(currentSubmission.result) }}
                </span>
              </div>
              <div class="detail-item">
                <span class="detail-label">语言</span>
                <span class="detail-value">{{ (currentSubmission.language || '-').toUpperCase() }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">提交时间</span>
                <span class="detail-value">{{ formatDateTime(currentSubmission.submitTime || currentSubmission.createTime) }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">执行时间</span>
                <span class="detail-value">{{ currentSubmission.timeCost || 0 }} ms</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">内存使用</span>
                <span class="detail-value">{{ currentSubmission.memoryCost || 0 }} KB</span>
              </div>
            </div>
          </div>

          <div class="detail-section">
            <h3 class="detail-title">代码</h3>
            <pre class="code-display">{{ currentSubmission.code }}</pre>
          </div>

          <div
            v-if="currentSubmission.compileError || currentSubmission.runtimeError || currentSubmission.errorMessage || currentSubmission.error"
            class="detail-section"
          >
            <h3 class="detail-title">错误信息</h3>
            <pre class="error-display">{{ currentSubmission.compileError || currentSubmission.runtimeError || currentSubmission.errorMessage || currentSubmission.error }}</pre>
          </div>
        </div>
      </div>
    </el-dialog>

    <AIDialog 
      v-model="aiDialogVisible"
      :initial-prompt="aiInitialPrompt"
      :initial-code="aiInitialCode"
    />

    <!-- 语言设置对话框 -->
    <el-dialog v-model="languageDialogVisible" title="设置常用编程语言" width="400px">
      <el-select v-model="selectedLanguage" placeholder="请选择语言" style="width: 100%">
        <el-option label="Java" value="java" />
        <el-option label="Python" value="python" />
      </el-select>
      <template #footer>
        <el-button @click="languageDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateLanguage">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProblemDetail, getSampleTestCases } from '@/api/problem'
import { submitCode as submitCodeApi, getMySubmissions, getSubmitDetail } from '@/api/submit'
import { getHint } from '@/api/referenceSolution'
import { getStudyStats } from '@/api/userProfile'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import MonacoEditor from '@/components/MonacoEditor.vue'
import AIDialog from '@/components/AIDialog.vue'
import ReferenceSolution from '@/components/ReferenceSolution.vue'
import {
  ArrowLeft,
  ArrowRight,
  ArrowDown,
  Collection,
  Promotion,
  Check,
  Document,
  Refresh,
  Setting,
  VideoPlay,
  Plus,
  CircleCheck,
  CircleClose,
  UserFilled,
  SwitchButton,
  User,
  TrendCharts,
  Timer,
  Monitor
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 用户菜单相关
const languageDialogVisible = ref(false)
const selectedLanguage = ref(userStore.userInfo?.language || 'java')

// 用户统计数据
const userStats = ref({
  solved: 0,
  submissions: 0,
  passRate: 0,
  streak: 0
})

// 获取用户统计数据
const fetchUserStats = async () => {
  try {
    // 这里可以调用API获取真实数据
    // 暂时使用模拟数据
    const userId = userStore.userInfo?.id
    if (!userId) return

    const res = await getStudyStats(userId)
    const stats = res?.data || {}
    const passRate = Number(stats.passRate || 0)

    userStats.value = {
      solved: stats.totalSolved || 0,
      submissions: stats.totalSubmissions || 0,
      passRate: Math.round(passRate * 100),
      streak: stats.streak || 0
    }
  } catch (error) {
    console.error('获取用户统计失败:', error)
  }
}

const goToProfile = (tab) => {
  const routeByTab = {
    submissions: { name: 'ProfileSubmissions' },
    passRate: { name: 'ProfileAnalysis' },
    solved: { name: 'UserProfile' },
    streak: { name: 'UserProfile' }
  }
  router.push(routeByTab[tab] || { name: 'UserProfile' })
}

const handleUserCommand = async (command) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      userStore.clearToken()
      ElMessage.success('已退出登录')
      router.push('/login')
    } catch {
      return
    }
  } else if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'language') {
    selectedLanguage.value = userStore.userInfo?.language || 'java'
    languageDialogVisible.value = true
  } else if (command === 'admin') {
    router.push('/admin')
  }
}

const handleUpdateLanguage = async () => {
  const success = await userStore.updateLanguage(selectedLanguage.value)
  if (success) {
    ElMessage.success('语言设置成功')
    languageDialogVisible.value = false
    language.value = selectedLanguage.value
  } else {
    ElMessage.error('语言设置失败')
  }
}

// 状态
const loading = ref(false)
const submitting = ref(false)
const running = ref(false)
const problem = ref(null)
const code = ref('')
const language = ref(userStore.userInfo?.language || 'java')
const result = ref(null)
const submissionsLoading = ref(false)
const submissionList = ref([])
const detailDialogVisible = ref(false)
const currentSubmission = ref(null)
const detailLoading = ref(false)
const leftPanelWidth = ref(45)
const isResizing = ref(false)
const canViewSolution = ref(false)
const hints = ref([])
const aiDialogVisible = ref(false)
const aiInitialPrompt = ref('')
const aiInitialCode = ref('')

// 标签页
const tabs = [
  { key: 'description', label: '题目描述' },
  { key: 'solution', label: '题解' },
  { key: 'submissions', label: '提交记录' }
]
const activeTab = ref('description')

// 测试用例标签
const testcaseTabs = [
  { key: 'case', label: '测试用例' },
  { key: 'result', label: '测试结果' }
]
const activeTestcaseTab = ref('case')

// 测试用例
const testCases = ref([])
const selectedTestCase = ref(0)

// 代码模板
const codeTemplates = {
  java: `class Solution {
    public int[] twoSum(int[] nums, int target) {
        
    }
}`,
  python: `class Solution:
    def twoSum(self, nums: List[int], target: int) -> List[int]:
        `
}

// 初始化代码
const initializeCode = () => {
  // 根据题目ID生成对应的代码模板
  const problemId = route.params.id
  let template = codeTemplates[language.value] || ''
  
  // 可以根据题目ID生成不同的模板
  if (problemId == 1) {
    // 两数之和
    if (language.value === 'java') {
      template = `class Solution {
    public int[] twoSum(int[] nums, int target) {
        
    }
}`
    } else {
      template = `class Solution:
    def twoSum(self, nums: List[int], target: int) -> List[int]:
        `
    }
  } else if (problemId == 2) {
    // 求最大值
    if (language.value === 'java') {
      template = `class Solution {
    public int findMax(int[] nums) {
        
    }
}`
    } else {
      template = `class Solution:
    def findMax(self, nums: List[int]) -> int:
        `
    }
  } else if (problemId == 3) {
    // 求绝对值
    if (language.value === 'java') {
      template = `class Solution {
    public int abs(int x) {
        
    }
}`
    } else {
      template = `class Solution:
    def abs(self, x: int) -> int:
        `
    }
  }
  
  code.value = template
}

// 获取题目详情
const fetchProblemDetail = async () => {
  loading.value = true
  try {
    const res = await getProblemDetail(route.params.id)
    if (res.code === 200) {
      problem.value = res.data
      initializeCode()
      fetchTestCases()
      fetchHints()
    }
  } catch (error) {
    console.error('获取题目详情失败:', error)
    ElMessage.error('获取题目详情失败')
  } finally {
    loading.value = false
  }
}

// 获取测试用例
const fetchTestCases = async () => {
  try {
    const res = await getSampleTestCases(route.params.id)
    if (res.code === 200 && Array.isArray(res.data)) {
      testCases.value = res.data.map(tc => ({
        input: tc.input,
        output: tc.output,
        result: null
      }))
      if (testCases.value.length > 0) {
        selectedTestCase.value = 0
      }
    }
  } catch (error) {
    console.error('获取测试用例失败:', error)
  }
}

// 获取提示
const fetchProblemSubmissions = async () => {
  submissionsLoading.value = true
  try {
    const res = await getMySubmissions({
      problemId: route.params.id,
      page: 1,
      size: 10
    })

    if (res.code === 200) {
      submissionList.value = (res.data?.list || []).map((item) => ({
        ...item,
        submitTime: item.submitTime || item.createTime || item.createdAt
      }))
    }
  } catch (error) {
    console.error('获取当前题目提交记录失败:', error)
  } finally {
    submissionsLoading.value = false
  }
}

const fetchHints = async () => {
  try {
    const hintsArray = []
    for (let i = 1; i <= 3; i++) {
      const res = await getHint(route.params.id, i)
      if (res.code === 200 && res.data) {
        hintsArray.push(res.data)
      }
    }
    hints.value = hintsArray
  } catch (error) {
    console.error('获取提示失败:', error)
  }
}

// 语言切换
const handleLanguageChange = () => {
  initializeCode()
}

// 重置代码
const resetCode = () => {
  initializeCode()
  ElMessage.success('代码已重置')
}

// 运行代码
const runCode = async () => {
  running.value = true
  activeTestcaseTab.value = 'result'
  try {
    const res = await submitCodeApi({
      problemId: route.params.id,
      code: code.value,
      language: language.value
    })
    
    if (res.code === 200) {
      result.value = res.data
      ElMessage.success('运行完成')
    } else {
      ElMessage.error(res.msg || '运行失败')
    }
  } catch (error) {
    ElMessage.error('运行失败，请重试')
  } finally {
    running.value = false
  }
}

// 提交代码
const submitCode = async () => {
  submitting.value = true
  try {
    const res = await submitCodeApi({
      problemId: route.params.id,
      code: code.value,
      language: language.value
    })
    
    if (res.code === 200) {
      result.value = res.data
      activeTestcaseTab.value = 'result'
      ElMessage.success('提交成功')
      canViewSolution.value = true
      await Promise.all([fetchProblemSubmissions(), fetchUserStats()])
    } else {
      ElMessage.error(res.msg || '提交失败')
    }
  } catch (error) {
    ElMessage.error('提交失败，请重试')
  } finally {
    submitting.value = false
  }
}

// 选择测试用例
const selectTestCase = (index) => {
  selectedTestCase.value = index
}

// 添加测试用例
const addTestCase = () => {
  testCases.value.push({ input: '', output: '', result: null })
  selectedTestCase.value = testCases.value.length - 1
}

// 拖拽调整面板宽度
const startResize = (e) => {
  isResizing.value = true
  document.addEventListener('mousemove', resize)
  document.addEventListener('mouseup', stopResize)
}

const resize = (e) => {
  if (!isResizing.value) return
  const container = document.querySelector('.problem-content-wrapper')
  const containerRect = container.getBoundingClientRect()
  const newWidth = ((e.clientX - containerRect.left) / containerRect.width) * 100
  
  if (newWidth >= 30 && newWidth <= 70) {
    leftPanelWidth.value = newWidth
  }
}

const stopResize = () => {
  isResizing.value = false
  document.removeEventListener('mousemove', resize)
  document.removeEventListener('mouseup', stopResize)
}

// AI助手
const handleAIAction = (data) => {
  aiInitialPrompt.value = data.prompt
  aiInitialCode.value = data.code
  aiDialogVisible.value = true
}

const handleCodeChange = () => {}

const handleViewSolution = () => {}

// 工具函数
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

const getResultText = (resultType) => {
  const texts = { 0: '通过', 1: '解答错误', 2: '运行错误', 3: '超时', 4: '内存超限' }
  return texts[resultType] || '未知错误'
}

const formatDateTime = (time) => {
  if (!time) return '未知时间'
  const date = new Date(time)
  if (Number.isNaN(date.getTime())) return '未知时间'
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const openSubmissionDetail = async (submission) => {
  const submitId = Number(submission?.id || submission)
  if (!submitId) return

  detailLoading.value = true
  currentSubmission.value = null
  detailDialogVisible.value = true

  try {
    const res = await getSubmitDetail(submitId)
    if (res.code === 200) {
      const data = res.data || {}
      data.problemTitle = data.problemTitle || data.title || problem.value?.title
      data.submitTime = data.submitTime || data.createTime || data.createdAt || submission?.submitTime
      currentSubmission.value = data
    } else {
      currentSubmission.value = typeof submission === 'object' ? submission : null
    }
  } catch (error) {
    console.error('获取提交详情失败:', error)
    currentSubmission.value = typeof submission === 'object' ? submission : null
  } finally {
    detailLoading.value = false
  }
}

onMounted(() => {
  fetchProblemDetail()
  fetchUserStats()
  fetchProblemSubmissions()
})

watch(activeTab, (tab) => {
  if (tab === 'submissions') {
    fetchProblemSubmissions()
  }
})

watch(
  () => route.params.id,
  () => {
    fetchProblemDetail()
    fetchProblemSubmissions()
  }
)

onBeforeUnmount(() => {
  document.removeEventListener('mousemove', resize)
  document.removeEventListener('mouseup', stopResize)
})
</script>

<style scoped>
.problem-detail-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

/* 顶部导航栏 */
.problem-header {
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
}

/* 面包屑导航 */
.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.breadcrumb-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #666;
  cursor: pointer;
  transition: color 0.2s;
}

.breadcrumb-item:hover {
  color: #1890ff;
}

.breadcrumb-item.current {
  color: #333;
  cursor: default;
}

.breadcrumb-item.current:hover {
  color: #333;
}

.breadcrumb-separator {
  font-size: 12px;
  color: #999;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.action-btn {
  font-weight: 500;
  padding: 8px 20px;
}

.run-btn {
  background: #52c41a;
  border-color: #52c41a;
}

.run-btn:hover {
  background: #73d13d;
  border-color: #73d13d;
}

/* 用户下拉菜单 */
.user-dropdown {
  margin-left: 8px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background-color 0.2s;
}

.user-info:hover {
  background-color: #f5f5f5;
}

.user-avatar {
  background: linear-gradient(135deg, #1890ff 0%, #69c0ff 100%);
}

.username {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.dropdown-icon {
  font-size: 12px;
  color: #999;
}

/* 用户下拉面板 */
:deep(.user-dropdown-panel) {
  padding: 0 !important;
  border-radius: 12px !important;
  overflow: hidden;
}

.user-menu-panel {
  width: 280px;
  background: #fff;
}

.user-menu-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-bottom: 1px solid #e2e8f0;
}

.user-menu-avatar {
  background: linear-gradient(135deg, #1890ff 0%, #69c0ff 100%);
}

.user-menu-info {
  flex: 1;
}

.user-menu-name {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.user-menu-role {
  font-size: 12px;
  color: #64748b;
  margin-top: 2px;
}

/* 统计卡片网格 */
.user-stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  padding: 12px 16px;
  border-bottom: 1px solid #e2e8f0;
}

.stat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 4px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.stat-card:hover {
  background: #f8fafc;
}

.stat-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  margin-bottom: 6px;
}

.stat-icon.solved {
  background: #dcfce7;
  color: #16a34a;
}

.stat-icon.submissions {
  background: #dbeafe;
  color: #2563eb;
}

.stat-icon.pass-rate {
  background: #fef3c7;
  color: #d97706;
}

.stat-icon.streak {
  background: #fce7f3;
  color: #db2777;
}

.stat-value {
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
}

.stat-label {
  font-size: 11px;
  color: #64748b;
  margin-top: 2px;
}

/* 菜单列表 */
.user-menu-list {
  padding: 8px 0;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
  color: #334155;
}

.menu-item:hover {
  background: #f8fafc;
  color: #1890ff;
}

.menu-item .el-icon {
  font-size: 16px;
  color: #64748b;
}

.menu-item:hover .el-icon {
  color: #1890ff;
}

.menu-item.logout {
  color: #ef4444;
}

.menu-item.logout:hover {
  background: #fef2f2;
}

.menu-item.logout .el-icon {
  color: #ef4444;
}

.menu-divider {
  height: 1px;
  background: #e2e8f0;
  margin: 8px 16px;
}

/* 主体内容区 */
.problem-content-wrapper {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* 左侧面板 */
.left-panel {
  background: #fff;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-tabs {
  display: flex;
  border-bottom: 1px solid #e8e8e8;
  background: #fafafa;
}

.tab-item {
  padding: 12px 20px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}

.tab-item:hover {
  color: #333;
}

.tab-item.active {
  color: #1890ff;
  border-bottom-color: #1890ff;
  background: #fff;
}

.panel-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.submissions-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.submissions-content.has-submissions :deep(.el-empty) {
  display: none;
}

.submissions-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.submissions-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.submission-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.submission-row {
  padding: 12px 14px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  background: #fff;
  cursor: pointer;
  transition: all 0.2s ease;
}

.submission-row:hover {
  border-color: #cbd5e1;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.06);
}

.submission-row-main {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.submission-status {
  font-size: 13px;
  font-weight: 600;
}

.submission-status.success {
  color: #16a34a;
}

.submission-status.error {
  color: #dc2626;
}

.submission-language {
  font-size: 12px;
  color: #64748b;
  background: #f8fafc;
  border-radius: 999px;
  padding: 2px 8px;
}

.submission-row-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 12px;
  color: #64748b;
}

.detail-content {
  min-height: 120px;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-title {
  margin: 0 0 12px;
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-label {
  font-size: 12px;
  color: #64748b;
}

.detail-value {
  font-size: 14px;
  color: #111827;
  word-break: break-word;
}

.detail-value.result-0 {
  color: #16a34a;
}

.detail-value.result-1,
.detail-value.result-2,
.detail-value.result-3,
.detail-value.result-4 {
  color: #dc2626;
}

.code-display,
.error-display {
  margin: 0;
  padding: 12px;
  border-radius: 10px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 13px;
  line-height: 1.6;
}

.code-display {
  background: #0f172a;
  color: #e2e8f0;
}

.error-display {
  background: #fef2f2;
  color: #b91c1c;
  border: 1px solid #fecaca;
}

.content-block {
  margin-bottom: 24px;
}

.block-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 16px 0;
}

.problem-description {
  font-size: 14px;
  line-height: 1.8;
  color: #333;
}

/* 示例样式 */
.examples-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.example-item {
  background: #fafafa;
  border-radius: 8px;
  overflow: hidden;
}

.example-header {
  padding: 12px 16px;
  background: #f0f0f0;
  border-bottom: 1px solid #e8e8e8;
}

.example-index {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.example-body {
  padding: 16px;
}

.example-row {
  margin-bottom: 12px;
}

.example-row:last-child {
  margin-bottom: 0;
}

.example-label {
  font-size: 13px;
  font-weight: 500;
  color: #666;
  margin-bottom: 4px;
}

.example-code {
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  padding: 12px;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 13px;
  color: #333;
  margin: 0;
  overflow-x: auto;
}

.example-text {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

/* 提示样式 */
.hints-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hint-item {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  border-radius: 8px;
}

.hint-num {
  font-weight: 600;
  color: #52c41a;
  flex-shrink: 0;
}

.hint-text {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
}

/* 拖拽分隔条 */
.resizer {
  width: 6px;
  background: #e8e8e8;
  cursor: col-resize;
  transition: background 0.2s;
  flex-shrink: 0;
}

.resizer:hover {
  background: #1890ff;
}

/* 右侧面板 */
.right-panel {
  background: #fff;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 编辑器头部 */
.editor-header {
  height: 48px;
  background: #fafafa;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  flex-shrink: 0;
}

.editor-tabs {
  display: flex;
}

.editor-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  font-size: 13px;
  color: #333;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-bottom: none;
  border-radius: 4px 4px 0 0;
}

.editor-tab.active {
  background: #fff;
  border-top: 2px solid #1890ff;
}

.editor-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.language-select {
  width: 120px;
}

.lang-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.lang-icon {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
}

.lang-icon.java {
  background: #f89820;
}

.lang-icon.python {
  background: #3776ab;
}

/* 编辑器容器 */
.editor-container {
  flex: 1;
  overflow: hidden;
}

/* 测试用例区域 */
.testcase-section {
  height: 200px;
  border-top: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.testcase-header {
  height: 40px;
  background: #fafafa;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
}

.testcase-tabs {
  display: flex;
  gap: 4px;
}

.testcase-tab {
  padding: 8px 16px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}

.testcase-tab:hover {
  color: #333;
}

.testcase-tab.active {
  color: #1890ff;
  border-bottom-color: #1890ff;
}

.testcase-content {
  flex: 1;
  overflow: hidden;
}

/* 测试用例面板 */
.testcase-panel {
  height: 100%;
  display: flex;
}

.testcase-list {
  width: 100px;
  border-right: 1px solid #e8e8e8;
  padding: 8px;
  overflow-y: auto;
}

.testcase-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  margin-bottom: 4px;
  transition: all 0.2s;
}

.testcase-item:hover {
  background: #f0f0f0;
}

.testcase-item.active {
  background: #e6f7ff;
  color: #1890ff;
}

.testcase-item.add-btn {
  justify-content: center;
  color: #999;
  border: 1px dashed #d9d9d9;
}

.testcase-item.add-btn:hover {
  color: #1890ff;
  border-color: #1890ff;
}

.testcase-name {
  font-size: 13px;
}

.testcase-status {
  font-size: 14px;
}

.testcase-status.passed {
  color: #52c41a;
}

.testcase-status.failed {
  color: #ff4d4f;
}

.testcase-detail {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

.input-section,
.output-section {
  margin-bottom: 16px;
}

.section-label {
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
}

.testcase-input :deep(.el-textarea__inner) {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 13px;
}

.expected-output {
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  border-radius: 4px;
  padding: 12px;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 13px;
  color: #333;
  margin: 0;
}

/* 结果面板 */
.result-panel {
  height: 100%;
  overflow-y: auto;
  padding: 16px;
}

.no-result {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-state {
  text-align: center;
  color: #999;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 14px;
}

.result-display {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.result-summary {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 8px;
}

.result-summary.success {
  background: #f6ffed;
  border: 1px solid #b7eb8f;
}

.result-summary.error {
  background: #fff2f0;
  border: 1px solid #ffccc7;
}

.result-icon {
  font-size: 24px;
}

.result-summary.success .result-icon {
  color: #52c41a;
}

.result-summary.error .result-icon {
  color: #ff4d4f;
}

.result-text {
  font-size: 16px;
  font-weight: 600;
}

.result-summary.success .result-text {
  color: #52c41a;
}

.result-summary.error .result-text {
  color: #ff4d4f;
}

.result-time,
.result-memory {
  font-size: 13px;
  color: #666;
  margin-left: auto;
}

.result-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.result-item {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
}

.result-item.passed {
  border-color: #b7eb8f;
}

.result-item.failed {
  border-color: #ffccc7;
}

.result-item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #fafafa;
  border-bottom: 1px solid #e8e8e8;
}

.result-item-name {
  font-size: 14px;
  font-weight: 500;
}

.result-item-body {
  padding: 16px;
}

.result-row {
  margin-bottom: 12px;
}

.result-row:last-child {
  margin-bottom: 0;
}

.result-label {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
  display: block;
}

.result-row pre {
  background: #f5f5f5;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  padding: 12px;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 13px;
  color: #333;
  margin: 0;
  overflow-x: auto;
}

.error-message {
  color: #ff4d4f !important;
  background: #fff2f0 !important;
  border-color: #ffccc7 !important;
}

/* 滚动条样式 */
.panel-content::-webkit-scrollbar,
.testcase-list::-webkit-scrollbar,
.testcase-detail::-webkit-scrollbar,
.result-panel::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.panel-content::-webkit-scrollbar-thumb,
.testcase-list::-webkit-scrollbar-thumb,
.testcase-detail::-webkit-scrollbar-thumb,
.result-panel::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.panel-content::-webkit-scrollbar-thumb:hover,
.testcase-list::-webkit-scrollbar-thumb:hover,
.testcase-detail::-webkit-scrollbar-thumb:hover,
.result-panel::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 响应式 */
@media (max-width: 768px) {
  .problem-header {
    padding: 0 16px;
  }
  
  .problem-title {
    font-size: 16px;
  }
  
  .left-panel {
    width: 100% !important;
  }
  
  .right-panel {
    display: none;
  }
  
  .resizer {
    display: none;
  }
}
</style>
