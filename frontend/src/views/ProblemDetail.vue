﻿﻿﻿﻿﻿﻿﻿﻿﻿<template>
  <div class="problem-detail-page">
    <!-- 椤堕儴瀵艰埅鏍?-->
    <div class="problem-header">
      <div class="header-left">
        <!-- 闈㈠寘灞戝鑸?-->
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
        <!-- 鐢ㄦ埛淇℃伅涓嬫媺鑿滃崟 -->
        <el-dropdown @command="handleUserCommand" trigger="click" class="user-dropdown" popper-class="user-dropdown-panel">
          <div class="user-info">
            <el-avatar :size="32" :icon="UserFilled" class="user-avatar" />
            <span class="username">{{ userStore.userInfo?.username }}</span>
            <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <div class="user-menu-panel">
              <!-- 鐢ㄦ埛淇℃伅澶撮儴 -->
              <div class="user-menu-header">
                <el-avatar :size="48" :icon="UserFilled" class="user-menu-avatar" />
                <div class="user-menu-info">
                  <div class="user-menu-name">{{ userStore.userInfo?.username }}</div>
                  <div class="user-menu-role">{{ userStore.userInfo?.role === 1 ? '管理员' : '普通用户' }}</div>
                </div>
              </div>
              
              <!-- 鏁版嵁缁熻鍗＄墖 -->
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
              
              <!-- 鑿滃崟椤?-->
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

    <!-- 涓讳綋鍐呭鍖?-->
    <div class="problem-content-wrapper">
      <!-- 宸︿晶棰樼洰鎻忚堪 -->
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
          <!-- 棰樼洰鎻忚堪 -->
          <ProblemDescriptionPanel
            v-if="activeTab === 'description'"
            :problem="problem"
            :hints="hints"
          />

          <!-- 棰樿В -->
          <div v-if="activeTab === 'solution'" class="solution-content">
            <ReferenceSolution 
              :problem-id="parseInt(route.params.id)" 
              :can-view="canViewSolution"
              :preferred-language="language"
              @view-solution="handleViewSolution"
            />
          </div>

          <!-- 鎻愪氦璁板綍 -->
          <ProblemSubmissionHistory
            v-if="activeTab === 'submissions'"
            :loading="submissionsLoading"
            :submissions="submissionList"
            @refresh="fetchProblemSubmissions"
            @select="openSubmissionDetail"
          />
        </div>
      </div>

      <!-- 鎷栨嫿鍒嗛殧鏉?-->
      <div class="resizer" @mousedown="startResize"></div>

      <!-- 鍙充晶浠ｇ爜缂栬緫鍖?-->
      <div class="right-panel" :style="{ width: (100 - leftPanelWidth) + '%' }">
        <div class="workspace-main">
          <!-- 浠ｇ爜缂栬緫鍣ㄥご閮?-->
          <div class="editor-header">
            <div class="editor-tabs">
              <div class="editor-tab active">
                <el-icon><Document /></el-icon>
                <span>{{ currentFileName }}</span>
              </div>
            </div>
            <div class="editor-actions">
              <el-select v-model="language" size="small" @change="handleLanguageChange" class="language-select">
                <el-option
                  v-for="item in languageOptions"
                  :key="item.code"
                  :label="item.label"
                  :value="item.code"
                >
                  <span class="lang-option">
                    <span class="lang-icon" :class="item.code">{{ item.label.slice(0, 1) }}</span>
                    {{ item.label }}
                  </span>
                </el-option>
              </el-select>
              <el-button text size="small" @click="coachVisible = !coachVisible" class="coach-btn">
                陪练
              </el-button>
              <el-button text size="small" @click="resetCode" class="reset-btn">
                <el-icon><Refresh /></el-icon>
              </el-button>
            </div>
          </div>

          <!-- 浠ｇ爜缂栬緫鍣?-->
          <div class="editor-container">
            <MonacoEditor
              v-model="code"
              :language="language"
              height="100%"
              @change="handleCodeChange"
              @ai-action="handleAIAction"
            />
          </div>

          <!-- 娴嬭瘯鐢ㄤ緥鍖哄煙 -->
          <ProblemExecutionPanel
            v-model:active-tab="activeTestcaseTab"
            v-model:selected-test-case="selectedTestCase"
            v-model:test-cases="testCases"
            :tabs="testcaseTabs"
            :running="running"
            :result="result"
            @run="runCode"
          />
        </div>

        <ProblemCoachSidebar
          ref="coachSidebarRef"
          v-model:visible="coachVisible"
          :problem-id="route.params.id"
          :problem-title="problem?.title"
          :code="code"
          :language="language"
          @apply-draft="handleApplyDraft"
        />
      </div>
    </div>

    <ProblemSubmissionDetailDialog
      v-model="detailDialogVisible"
      :loading="detailLoading"
      :submission="currentSubmission"
      :problem-title="problem?.title"
    />

    <!-- 璇█璁剧疆瀵硅瘽妗?-->
    <el-dialog v-model="languageDialogVisible" title="设置常用编程语言" width="400px">
      <el-select v-model="selectedLanguage" placeholder="请选择语言" style="width: 100%">
        <el-option
          v-for="item in runtimeLanguageCatalog"
          :key="item.code"
          :label="item.label"
          :value="item.code"
        />
      </el-select>
      <template #footer>
        <el-button @click="languageDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateLanguage">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { defineAsyncComponent, nextTick, ref, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import ProblemDescriptionPanel from '@/components/problem/ProblemDescriptionPanel.vue'
import ProblemCoachSidebar from '@/components/problem/ProblemCoachSidebar.vue'
import ProblemExecutionPanel from '@/components/problem/ProblemExecutionPanel.vue'
import ProblemSubmissionHistory from '@/components/problem/ProblemSubmissionHistory.vue'
import ProblemSubmissionDetailDialog from '@/components/problem/ProblemSubmissionDetailDialog.vue'
import { useProblemDetailData } from '@/composables/useProblemDetailData'
import { runtimeLanguageCatalog } from '@/utils/runtimeLanguage'
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
  CircleCheck,
  UserFilled,
  SwitchButton,
  User,
  TrendCharts,
  Timer,
  Monitor
} from '@element-plus/icons-vue'

const MonacoEditor = defineAsyncComponent(() => import('@/components/MonacoEditor.vue'))
const ReferenceSolution = defineAsyncComponent(() => import('@/components/ReferenceSolution.vue'))

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 鐢ㄦ埛鑿滃崟鐩稿叧
const languageDialogVisible = ref(false)
const selectedLanguage = ref(userStore.userInfo?.language || 'java')
const {
  loading,
  submitting,
  running,
  problem,
  code,
  language,
  result,
  submissionsLoading,
  submissionList,
  detailDialogVisible,
  currentSubmission,
  detailLoading,
  canViewSolution,
  hints,
  userStats,
  tabs,
  activeTab,
  testcaseTabs,
  activeTestcaseTab,
  testCases,
  selectedTestCase,
  languageOptions,
  currentFileName,
  fetchProblemSubmissions,
  handleLanguageChange,
  resetCode,
  runCode: baseRunCode,
  submitCode: baseSubmitCode,
  openSubmissionDetail
} = useProblemDetailData(route, userStore)

// 鐢ㄦ埛缁熻鏁版嵁
// 鑾峰彇鐢ㄦ埛缁熻鏁版嵁
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
    handleLanguageChange()
  } else {
    ElMessage.error('语言设置失败')
  }
}

// 鐘舵€?
const leftPanelWidth = ref(45)
const isResizing = ref(false)
const coachVisible = ref(false)
const coachSidebarRef = ref(null)

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

// AI鍔╂墜
const runCode = async () => {
  const runState = await baseRunCode()
  if (runState?.failed) {
    coachVisible.value = true
    await nextTick()
    await coachSidebarRef.value?.triggerFailure(runState)
  }
  return runState
}

const submitCode = async () => {
  const submitState = await baseSubmitCode()
  if (submitState?.failed) {
    coachVisible.value = true
    await nextTick()
    await coachSidebarRef.value?.triggerFailure(submitState)
  }
  return submitState
}

const handleAIAction = async (data) => {
  coachVisible.value = true
  await nextTick()
  await coachSidebarRef.value?.openWithPrompt({
    prompt: data.prompt,
    selectedCode: data.code
  })
}

const handleCodeChange = () => {}

const handleViewSolution = () => {}

const handleApplyDraft = (draftCode) => {
  if (!draftCode) {
    return
  }
  code.value = draftCode
  ElMessage.success('已用参考修正版覆盖当前代码')
}

onBeforeUnmount(() => {
  document.removeEventListener('mousemove', resize)
  document.removeEventListener('mouseup', stopResize)
})
</script>

<style scoped>
.problem-detail-page {
  --problem-page-bg: #f3f6fb;
  --problem-surface: #ffffff;
  --problem-subtle: #f8fafc;
  --problem-border: #dbe3ef;
  --problem-border-strong: #bfd1e6;
  --problem-text: #1f2937;
  --problem-text-secondary: #64748b;
  --problem-primary: #1890ff;
  --problem-primary-soft: rgba(24, 144, 255, 0.08);
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--problem-page-bg);
}

/* 椤堕儴瀵艰埅鏍?*/
.problem-header {
  min-height: 60px;
  background: rgba(255, 255, 255, 0.92);
  border-bottom: 1px solid var(--problem-border);
  backdrop-filter: blur(14px);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 0 24px;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  min-width: 0;
}

/* 闈㈠寘灞戝鑸?*/
.breadcrumb {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 14px;
}

.breadcrumb-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--problem-text-secondary);
  cursor: pointer;
  transition: color 0.2s;
}

.breadcrumb-item:hover {
  color: var(--problem-primary);
}

.breadcrumb-item.current {
  max-width: min(42vw, 360px);
  overflow: hidden;
  color: var(--problem-text);
  cursor: default;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.breadcrumb-item.current:hover {
  color: var(--problem-text);
}

.breadcrumb-separator {
  font-size: 12px;
  color: #94a3b8;
}

.header-right {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 12px;
}

.action-btn {
  font-weight: 500;
  padding: 8px 20px;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.08);
}

.run-btn {
  background: #52c41a;
  border-color: #52c41a;
}

.run-btn:hover {
  background: #73d13d;
  border-color: #73d13d;
}

/* 鐢ㄦ埛涓嬫媺鑿滃崟 */
.user-dropdown {
  margin-left: 8px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  max-width: 220px;
  padding: 4px 10px;
  border: 1px solid transparent;
  border-radius: 10px;
  transition:
    background-color 0.2s,
    border-color 0.2s;
}

.user-info:hover {
  background-color: var(--problem-primary-soft);
  border-color: rgba(24, 144, 255, 0.12);
}

.user-avatar {
  background: linear-gradient(135deg, #1890ff 0%, #69c0ff 100%);
}

.username {
  font-size: 14px;
  max-width: 120px;
  overflow: hidden;
  color: var(--problem-text);
  font-weight: 500;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dropdown-icon {
  font-size: 12px;
  color: #94a3b8;
}

/* 鐢ㄦ埛涓嬫媺闈㈡澘 */
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

/* 缁熻鍗＄墖缃戞牸 */
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

/* 鑿滃崟鍒楄〃 */
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

/* 涓讳綋鍐呭鍖?*/
.problem-content-wrapper {
  flex: 1;
  display: flex;
  min-height: 0;
  overflow: hidden;
}

/* 宸︿晶闈㈡澘 */
.left-panel {
  background: var(--problem-surface);
  border-right: 1px solid var(--problem-border);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-tabs {
  display: flex;
  border-bottom: 1px solid var(--problem-border);
  background: var(--problem-subtle);
}

.tab-item {
  padding: 12px 20px;
  font-size: 14px;
  color: var(--problem-text-secondary);
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}

.tab-item:hover {
  color: var(--problem-text);
}

.tab-item.active {
  color: var(--problem-primary);
  border-bottom-color: var(--problem-primary);
  background: var(--problem-surface);
}

.panel-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

/* 鎷栨嫿鍒嗛殧鏉?*/
.resizer {
  width: 6px;
  background: var(--problem-border);
  cursor: col-resize;
  transition: background 0.2s;
  flex-shrink: 0;
}

.resizer:hover {
  background: var(--problem-primary);
}

/* 鍙充晶闈㈡澘 */
.right-panel {
  background: var(--problem-surface);
  display: flex;
  min-width: 0;
  overflow: hidden;
}

.workspace-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 缂栬緫鍣ㄥご閮?*/
.editor-header {
  height: 48px;
  background: var(--problem-subtle);
  border-bottom: 1px solid var(--problem-border);
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
  color: var(--problem-text);
  background: var(--problem-surface);
  border: 1px solid var(--problem-border);
  border-bottom: none;
  border-radius: 4px 4px 0 0;
}

.editor-tab.active {
  background: var(--problem-surface);
  border-top: 2px solid var(--problem-primary);
}

.editor-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.coach-btn {
  font-weight: 600;
  color: var(--problem-primary);
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

.lang-icon.cpp {
  background: #00599c;
}

.lang-icon.javascript {
  background: #f7df1e;
  color: #111827;
}

.lang-icon.typescript {
  background: #3178c6;
}

.lang-icon.go {
  background: #00add8;
}

/* 缂栬緫鍣ㄥ鍣?*/
.editor-container {
  flex: 1;
  overflow: hidden;
}

/* 婊氬姩鏉℃牱寮?*/
.panel-content::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.panel-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.panel-content::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 鍝嶅簲寮?*/
@media (max-width: 768px) {
  .problem-header {
    padding: 12px 16px;
  }
  
  .problem-title {
    font-size: 16px;
  }

  .panel-content {
    padding: 18px 16px;
  }
}

@media (max-width: 1100px) {
  .problem-detail-page {
    height: auto;
    min-height: 100vh;
  }

  .problem-header {
    align-items: flex-start;
    padding-top: 14px;
    padding-bottom: 14px;
  }

  .problem-content-wrapper {
    flex-direction: column;
    overflow: visible;
  }

  .left-panel,
  .right-panel {
    width: 100% !important;
  }

  .left-panel {
    min-height: 44vh;
    border-right: none;
    border-bottom: 1px solid var(--problem-border);
  }

  .right-panel {
    flex-direction: column;
    min-height: 56vh;
  }

  .resizer {
    display: none;
  }
}

@media (max-width: 640px) {
  .breadcrumb-item.current {
    max-width: 100%;
  }

  .header-right {
    width: 100%;
    justify-content: stretch;
  }

  .action-btn {
    flex: 1 1 0;
  }

  .user-dropdown {
    width: 100%;
    margin-left: 0;
  }

  .user-info {
    max-width: none;
    width: 100%;
    justify-content: space-between;
  }

  .panel-tabs,
  .editor-header {
    overflow-x: auto;
  }

  .tab-item {
    white-space: nowrap;
  }
}
</style>

