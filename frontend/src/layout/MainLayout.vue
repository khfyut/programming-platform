<template>
  <div class="leetcode-layout" :class="{ 'hide-header': route.meta.hideHeader }">
    <el-header v-if="!route.meta.hideHeader" class="leetcode-header">
      <div class="header-container">
        <div class="header-left">

          <div class="logo" @click="router.push('/')">
            <div class="logo-icon">
              <el-icon :size="24"><Edit /></el-icon>
              <div class="logo-dot"></div>
            </div>
            <span class="logo-text">CodeMaster</span>
          </div>
          <!-- 移动端汉堡菜单按钮 -->
          <el-button 
            class="mobile-menu-btn" 
            @click="mobileMenuVisible = !mobileMenuVisible"
            :icon="mobileMenuVisible ? Close : Menu"
            circle
          />
          <!-- 桌面端导航菜单 -->
          <nav class="nav-menu">
            <router-link to="/" class="nav-item" :class="{ active: route.path === '/' }">
              首页
            </router-link>
            <router-link to="/problems" class="nav-item" :class="{ active: route.path === '/problems' || route.path.startsWith('/problem') }">
              题库
            </router-link>
            <router-link to="/community" class="nav-item" :class="{ active: route.path.startsWith('/community') }">
              学习社区
            </router-link>
          </nav>
        </div>
        <div class="header-right">
          <div class="search-box">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索题目..."
              clearable
              class="search-input"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon class="search-icon"><Search /></el-icon>
              </template>
            </el-input>
          </div>
          <el-tooltip content="切换主题" placement="bottom">
            <el-button 
              class="theme-toggle-btn"
              @click="toggleTheme"
              :icon="isDark ? Sunny : Moon"
              circle
            />
          </el-tooltip>
          <el-dropdown @command="handleCommand" @visible-change="handleDropdownVisibleChange" trigger="click" popper-class="user-dropdown-panel">
            <div class="user-info-trigger">
              <div class="user-avatar-wrapper">
                <el-avatar :size="36" :icon="UserFilled" />
                <div class="online-indicator"></div>
              </div>
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
                <div class="user-stats-grid" v-loading="statsLoading">
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
                  <div class="menu-item" @click="handleCommand('profile')">
                    <el-icon><User /></el-icon>
                    <span>个人主页</span>
                  </div>
                  <div class="menu-item" @click="handleCommand('language')">
                    <el-icon><Setting /></el-icon>
                    <span>设置语言</span>
                  </div>
                  <div v-if="userStore.userInfo?.role === 1" class="menu-item" @click="handleCommand('admin')">
                    <el-icon><Monitor /></el-icon>
                    <span>管理后台</span>
                  </div>
                  <div class="menu-divider"></div>
                  <div class="menu-item logout" @click="handleCommand('logout')">
                    <el-icon><SwitchButton /></el-icon>
                    <span>退出登录</span>
                  </div>
                </div>
              </div>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>
    
    <!-- 侧边栏 -->
    <el-aside v-if="!route.meta.hideHeader" class="leetcode-sidebar" width="240px">
      <div class="sidebar-content">
        <div class="sidebar-section">
          <h3 class="sidebar-section-title">学习资源</h3>
          <ul class="sidebar-menu">
            <li class="sidebar-menu-item" :class="{ active: route.path === '/ai' }">
              <router-link to="/ai" class="sidebar-menu-link">
                <el-icon class="sidebar-menu-icon"><Cpu /></el-icon>
                <span>AI答疑</span>
              </router-link>
            </li>
            <li class="sidebar-menu-item" :class="{ active: route.path.startsWith('/learn') }">
              <router-link to="/learn" class="sidebar-menu-link">
                <el-icon class="sidebar-menu-icon"><Reading /></el-icon>
                <span>学习中心</span>
              </router-link>
            </li>
            <li class="sidebar-menu-item" :class="{ active: route.path === '/wrong-book' }">
              <router-link to="/wrong-book" class="sidebar-menu-link">
                <el-icon class="sidebar-menu-icon"><Warning /></el-icon>
                <span>错题本</span>
              </router-link>
            </li>
            <li class="sidebar-menu-item" :class="{ active: route.path === '/submissions' }">
              <router-link to="/submissions" class="sidebar-menu-link">
                <el-icon class="sidebar-menu-icon"><Document /></el-icon>
                <span>提交记录</span>
              </router-link>
            </li>
          </ul>
        </div>
        

      </div>
    </el-aside>
    
    <el-main class="leetcode-main">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </el-main>

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

    <!-- 移动端导航菜单 -->
    <el-drawer
      v-model="mobileMenuVisible"
      direction="ltr"
      size="80%"
      class="mobile-menu-drawer"
    >
      <div class="mobile-menu-content">
        <div class="mobile-menu-header">
          <div class="logo" @click="router.push('/')">
            <div class="logo-icon">
              <el-icon :size="20"><Edit /></el-icon>
              <div class="logo-dot"></div>
            </div>
            <span class="logo-text">CodeMaster</span>
          </div>
          <el-button 
            @click="mobileMenuVisible = false"
            :icon="Close"
            circle
            class="close-btn"
          />
        </div>
        <nav class="mobile-nav-menu">
          <router-link to="/" class="mobile-nav-item" :class="{ active: route.path === '/' }" @click="mobileMenuVisible = false">
            首页
          </router-link>
          <router-link to="/problems" class="mobile-nav-item" :class="{ active: route.path === '/problems' || route.path.startsWith('/problem') }" @click="mobileMenuVisible = false">
            题库
          </router-link>
          <router-link to="/ai" class="mobile-nav-item" :class="{ active: route.path === '/ai' }" @click="mobileMenuVisible = false">
            AI答疑
          </router-link>
          <router-link to="/learn" class="mobile-nav-item" :class="{ active: route.path.startsWith('/learn') }" @click="mobileMenuVisible = false">
            学习中心
          </router-link>
          <router-link to="/wrong-book" class="mobile-nav-item" :class="{ active: route.path === '/wrong-book' }" @click="mobileMenuVisible = false">
            错题本
          </router-link>
          <router-link to="/submissions" class="mobile-nav-item" :class="{ active: route.path === '/submissions' }" @click="mobileMenuVisible = false">
            提交记录
          </router-link>
          <router-link to="/community" class="mobile-nav-item" :class="{ active: route.path.startsWith('/community') }" @click="mobileMenuVisible = false">
            学习社区
          </router-link>
          <router-link to="/profile" class="mobile-nav-item" :class="{ active: route.path.startsWith('/profile') }" @click="mobileMenuVisible = false">
            个人主页
          </router-link>
        </nav>
        <div class="mobile-menu-footer">
          <div class="user-info">
            <div class="user-avatar-wrapper">
              <el-avatar :size="40" :icon="UserFilled" />
              <div class="online-indicator"></div>
            </div>
            <div class="user-details">
              <span class="username">{{ userStore.userInfo?.username || '未登录' }}</span>
              <el-button 
                v-if="!userStore.userInfo" 
                type="primary" 
                size="small" 
                class="login-btn"
                @click="router.push('/login'); mobileMenuVisible = false"
              >
                登录
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyLearnStats } from '@/api/learn'
import {
  Edit,
  Search,
  Setting,
  UserFilled,
  SwitchButton,
  Sunny,
  Moon,
  Close,
  Menu,
  User,
  CircleCheck,
  Document,
  TrendCharts,
  Timer,
  Monitor,
  ArrowDown,
  Cpu,
  Reading,
  Warning,
  DataAnalysis,
  Collection
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()

const isDark = computed(() => themeStore.isDark)
const searchKeyword = ref('')
const languageDialogVisible = ref(false)
const selectedLanguage = ref(userStore.userInfo?.language || 'java')
const mobileMenuVisible = ref(false)
const sidebarVisible = ref(true)
const statsLoading = ref(false)

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
    userStats.value = {
      solved: userStore.userInfo?.solvedCount || 0,
      submissions: userStore.userInfo?.submissionCount || 0,
      passRate: userStore.userInfo?.passRate || 0,
      streak: userStore.userInfo?.streak || 0
    }
  } catch (error) {
    console.error('获取用户统计失败:', error)
  }
}

const refreshUserStats = async () => {
  if (!userStore.token) {
    userStats.value = {
      solved: 0,
      submissions: 0,
      passRate: 0,
      streak: 0
    }
    return
  }

  statsLoading.value = true
  try {
    const fallbackPassRate = Number(userStore.userInfo?.passRate || 0)
    const res = await getMyLearnStats()
    const payload = res?.code === 200 ? (res.data?.stats || res.data || {}) : {}
    const rawPassRate = payload.passRate ?? payload.accuracy ?? fallbackPassRate

    userStats.value = {
      solved: payload.solved ?? payload.totalSolved ?? userStore.userInfo?.solvedCount ?? userStore.userInfo?.totalSolved ?? 0,
      submissions: payload.submitted ?? payload.submissionCount ?? payload.totalSubmissions ?? userStore.userInfo?.submissionCount ?? userStore.userInfo?.totalSubmissions ?? 0,
      passRate: Number(rawPassRate) <= 1 ? Math.round(Number(rawPassRate || 0) * 100) : Math.round(Number(rawPassRate || 0)),
      streak: payload.streak ?? userStore.userInfo?.streak ?? 0
    }
  } catch (error) {
    console.error('refresh user stats failed:', error)
    const fallbackPassRate = Number(userStore.userInfo?.passRate || 0)
    userStats.value = {
      solved: userStore.userInfo?.solvedCount ?? userStore.userInfo?.totalSolved ?? 0,
      submissions: userStore.userInfo?.submissionCount ?? userStore.userInfo?.totalSubmissions ?? 0,
      passRate: fallbackPassRate <= 1 ? Math.round(fallbackPassRate * 100) : Math.round(fallbackPassRate),
      streak: userStore.userInfo?.streak ?? 0
    }
  } finally {
    statsLoading.value = false
  }
}

const handleDropdownVisibleChange = (visible) => {
  if (visible) {
    refreshUserStats()
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

const toggleTheme = () => {
  themeStore.toggleTheme()
  ElMessage.success(isDark.value ? '已切换到亮色模式' : '已切换到暗色模式')
}

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({
      path: '/problems',
      query: { keyword: searchKeyword.value.trim() }
    })
  }
}

const handleCommand = async (command) => {
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
  } else {
    ElMessage.error('语言设置失败')
  }
}
watch(
  () => userStore.userInfo?.id,
  (userId) => {
    if (userId) {
      refreshUserStats()
    }
  },
  { immediate: true }
)

onMounted(() => {
  if (userStore.token && !userStore.userInfo) {
    userStore.fetchUserInfo().finally(() => {
      refreshUserStats()
    })
    return
  }
  refreshUserStats()
})
</script>

<style scoped>
.leetcode-layout {
  min-height: 100vh;
  background: var(--bg-primary);
  display: flex;
  flex-direction: column;
}



.leetcode-header {
  height: 60px;
  background: var(--leetcode-bg, #FFFFFF);
  border-bottom: 1px solid var(--leetcode-border, #E5E7EB);
  padding: 0;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.header-container {
  max-width: 1400px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

/* 侧边栏切换按钮 */
.sidebar-toggle-btn {
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border: 1px solid var(--leetcode-border, #E5E7EB);
  color: var(--leetcode-text-secondary, #6B7280);
  transition: all 0.2s ease;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.sidebar-toggle-btn:hover {
  background: var(--leetcode-primary, #0066FF);
  border-color: var(--leetcode-primary, #0066FF);
  color: white;
}

/* 侧边栏样式 */
.leetcode-sidebar {
  background: var(--leetcode-bg, #FFFFFF);
  border-right: 1px solid var(--leetcode-border, #E5E7EB);
  padding: 20px 0;
  position: fixed;
  top: 60px;
  left: 0;
  bottom: 0;
  z-index: 999;
  overflow: hidden;
}

.sidebar-content {
  height: 100%;
  overflow-y: auto;
  padding: 0 16px;
}

.sidebar-section {
  margin-bottom: 32px;
}

.sidebar-section-title {
  font-size: 12px;
  font-weight: 600;
  color: var(--leetcode-text-secondary, #6B7280);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 12px;
  padding: 0 12px;
}

.sidebar-menu {
  list-style: none;
  padding: 0;
  margin: 0;
}

.sidebar-menu-item {
  margin-bottom: 4px;
}

.sidebar-menu-link {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  color: var(--leetcode-text, #24292F);
  text-decoration: none;
  transition: all 0.2s ease;
  font-size: 14px;
  font-weight: 500;
}

.sidebar-menu-link:hover {
  background: var(--leetcode-bg-secondary, #F7F8FA);
  color: var(--leetcode-primary, #0066FF);
}

.sidebar-menu-item.active .sidebar-menu-link {
  background: var(--leetcode-primary, #0066FF);
  color: white;
}

.sidebar-menu-icon {
  font-size: 16px;
  width: 20px;
  text-align: center;
}

/* 主内容区域调整 */
.leetcode-main {
  margin-top: 60px;
  margin-left: 240px;
  padding: 0;
  background: var(--bg-primary);
  min-height: calc(100vh - 60px);
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 8px 12px;
  border-radius: 12px;
  position: relative;
  overflow: hidden;
}

.logo::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(0, 102, 255, 0.1), transparent);
  transition: left 0.6s ease;
}

.logo:hover::before {
  left: 100%;
}

.logo:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 102, 255, 0.15);
}

.logo-icon {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #0066FF 0%, #66B3FF 100%);
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 102, 255, 0.3);
  animation: logoPulse 2s ease-in-out infinite;
}

@keyframes logoPulse {
  0%, 100% {
    box-shadow: 0 4px 12px rgba(0, 102, 255, 0.3);
  }
  50% {
    box-shadow: 0 6px 20px rgba(0, 102, 255, 0.5);
  }
}

.logo-icon .el-icon {
  color: white;
  font-size: 20px;
  position: relative;
  z-index: 1;
}

.logo-dot {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 8px;
  height: 8px;
  background: #00FF88;
  border: 2px solid white;
  border-radius: 50%;
  animation: dotPulse 1.5s ease-in-out infinite;
}

@keyframes dotPulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.8;
  }
}

.logo-text {
  font-size: 22px;
  font-weight: 800;
  color: var(--leetcode-text, #24292F);
  letter-spacing: -0.5px;
  font-family: 'Orbitron', 'PingFang SC', sans-serif;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  background: linear-gradient(135deg, #24292F 0%, #6B7280 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.dark .logo-text {
  background: linear-gradient(135deg, #F1F5F9 0%, #94A3B8 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.dark .logo {
  background: rgba(33, 38, 45, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.dark .logo:hover {
  box-shadow: 0 4px 12px rgba(0, 102, 255, 0.25);
}

.nav-menu {
  display: flex;
  gap: 8px;
}

.nav-item {
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 500;
  color: var(--leetcode-text-secondary, #6B7280);
  text-decoration: none;
  border-radius: 6px;
  transition: all 0.2s ease;
  position: relative;
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-item:hover {
  color: var(--leetcode-primary, #0066FF);
  background: var(--leetcode-bg-secondary, #F7F8FA);
}

.nav-item.active {
  color: var(--leetcode-primary, #0066FF);
  background: var(--leetcode-bg-secondary, #F7F8FA);
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-box {
  position: relative;
}

.search-input {
  width: 240px;
}

.search-icon {
  color: var(--leetcode-text-secondary, #6B7280);
  transition: color 0.2s ease;
}

:deep(.search-input .el-input__wrapper) {
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border: 1px solid var(--leetcode-border, #E5E7EB);
  border-radius: 6px;
  padding: 6px 12px;
  transition: all 0.2s ease;
  box-shadow: none;
}

:deep(.search-input .el-input__wrapper:hover) {
  border-color: var(--leetcode-primary, #0066FF);
  background: #FFFFFF;
}

:deep(.search-input .el-input__wrapper.is-focus) {
  border-color: var(--leetcode-primary, #0066FF);
  background: #FFFFFF;
  box-shadow: 0 0 0 3px rgba(0, 102, 255, 0.1);
}

.theme-toggle-btn {
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border: 1px solid var(--leetcode-border, #E5E7EB);
  color: var(--leetcode-text-secondary, #6B7280);
  transition: all 0.2s ease;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.theme-toggle-btn:hover {
  background: var(--leetcode-primary, #0066FF);
  border-color: var(--leetcode-primary, #0066FF);
  color: white;
  transform: rotate(180deg);
}

.user-info-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: all 0.2s ease;
  background: transparent;
}

.user-info-trigger:hover {
  background: var(--leetcode-bg-secondary, #F7F8FA);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: all 0.2s ease;
  background: transparent;
}

.user-info:hover {
  background: var(--leetcode-bg-secondary, #F7F8FA);
}

.user-avatar-wrapper {
  position: relative;
  display: inline-block;
}

.online-indicator {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 10px;
  height: 10px;
  background: var(--leetcode-success, #00C853);
  border: 2px solid var(--leetcode-bg, #FFFFFF);
  border-radius: 50%;
  box-shadow: 0 0 8px rgba(0, 200, 83, 0.5);
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: var(--leetcode-text, #24292F);
}

.dropdown-icon {
  font-size: 12px;
  color: var(--leetcode-text-secondary, #6B7280);
  transition: transform 0.2s ease;
}

.user-info:hover .dropdown-icon,
.user-info-trigger:hover .dropdown-icon {
  transform: rotate(180deg);
  color: var(--leetcode-primary, #0066FF);
}

/* 用户下拉面板样式 */
:deep(.user-dropdown-panel) {
  padding: 0 !important;
  border-radius: 12px !important;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12) !important;
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

.leetcode-main {
  margin-top: 60px;
  padding: 0;
  background: var(--bg-primary);
  min-height: calc(100vh - 60px);
}

/* 隐藏头部时的样式 */
.leetcode-layout.hide-header .leetcode-main {
  margin-top: 0;
  margin-left: 0;
  min-height: 100vh;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

:deep(.el-dropdown-menu) {
  background: var(--leetcode-bg, #FFFFFF);
  border: 1px solid var(--leetcode-border, #E5E7EB);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  padding: 6px 0;
  min-width: 160px;
}

:deep(.el-dropdown-menu__item) {
  color: var(--leetcode-text, #24292F);
  transition: all 0.2s ease;
  padding: 10px 16px;
  font-size: 14px;
  border-radius: 6px;
  margin: 2px 8px;
}

:deep(.el-dropdown-menu__item:hover) {
  background: var(--leetcode-bg-secondary, #F7F8FA);
  color: var(--leetcode-primary, #0066FF);
}

:deep(.el-dropdown-menu__item.is-divided) {
  border-top: 1px solid var(--leetcode-border, #E5E7EB);
  margin-top: 6px;
  padding-top: 6px;
}

:deep(.el-dropdown-menu__item .el-icon) {
  margin-right: 8px;
  font-size: 16px;
}

:deep(.el-dialog) {
  background: var(--leetcode-bg, #FFFFFF);
  border: 1px solid var(--leetcode-border, #E5E7EB);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  border-radius: 12px;
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid var(--leetcode-border, #E5E7EB);
  padding: 20px 24px;
}

:deep(.el-dialog__title) {
  color: var(--leetcode-text, #24292F);
  font-size: 16px;
  font-weight: 600;
}

:deep(.el-dialog__body) {
  color: var(--leetcode-text, #24292F);
  padding: 24px;
}

:deep(.el-dialog__footer) {
  border-top: 1px solid var(--leetcode-border, #E5E7EB);
  padding: 16px 24px;
}

@media (max-width: 1200px) {
  .header-container {
    padding: 0 20px;
  }

  .header-left {
    gap: 24px;
  }

  .nav-menu {
    gap: 4px;
  }

  .nav-item {
    padding: 8px 12px;
    font-size: 13px;
  }

  .search-input {
    width: 200px;
  }

  .username {
    display: none;
  }
}

@media (max-width: 768px) {
  .header-container {
    padding: 0 16px;
  }

  .nav-menu {
    display: none;
  }

  .mobile-menu-btn {
    display: flex;
  }

  .search-input {
    width: 160px;
  }

  .theme-toggle-btn {
    width: 32px;
    height: 32px;
  }

  .user-info {
    padding: 4px 6px;
  }
  
  /* 移动端隐藏侧边栏 */
  .leetcode-sidebar {
    display: none;
  }
  
  .leetcode-main.sidebar-visible {
    margin-left: 0;
  }
  
  .sidebar-toggle-btn {
    display: none;
  }
}

/* 移动端菜单样式 */
.mobile-menu-btn {
  display: none;
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border: 1px solid var(--leetcode-border, #E5E7EB);
  color: var(--leetcode-text-secondary, #6B7280);
  transition: all 0.2s ease;
  width: 36px;
  height: 36px;
  margin-right: 12px;
}

.mobile-menu-btn:hover {
  background: var(--leetcode-primary, #0066FF);
  border-color: var(--leetcode-primary, #0066FF);
  color: white;
}

.mobile-menu-drawer {
  background: var(--leetcode-bg, #FFFFFF);
  border-right: 1px solid var(--leetcode-border, #E5E7EB);
}

.mobile-menu-content {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.mobile-menu-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid var(--leetcode-border, #E5E7EB);
}

.mobile-menu-header .logo {
  margin: 0;
}

.close-btn {
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border: 1px solid var(--leetcode-border, #E5E7EB);
  color: var(--leetcode-text-secondary, #6B7280);
  transition: all 0.2s ease;
  width: 36px;
  height: 36px;
}

.close-btn:hover {
  background: var(--leetcode-primary, #0066FF);
  border-color: var(--leetcode-primary, #0066FF);
  color: white;
}

.mobile-nav-menu {
  flex: 1;
  padding: 20px 0;
  display: flex;
  flex-direction: column;
}

.mobile-nav-item {
  padding: 16px 20px;
  font-size: 16px;
  font-weight: 500;
  color: var(--leetcode-text, #24292F);
  text-decoration: none;
  transition: all 0.2s ease;
  border-left: 4px solid transparent;
}

.mobile-nav-item:hover {
  color: var(--leetcode-primary, #0066FF);
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border-left-color: var(--leetcode-primary, #0066FF);
}

.mobile-nav-item.active {
  color: var(--leetcode-primary, #0066FF);
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border-left-color: var(--leetcode-primary, #0066FF);
  font-weight: 600;
}

.mobile-menu-footer {
  padding: 20px;
  border-top: 1px solid var(--leetcode-border, #E5E7EB);
  background: var(--leetcode-bg-secondary, #F7F8FA);
}

.mobile-menu-footer .user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  background: transparent;
  padding: 0;
}

.mobile-menu-footer .user-details {
  flex: 1;
}

.mobile-menu-footer .username {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: var(--leetcode-text, #24292F);
  margin-bottom: 8px;
}

.login-btn {
  width: 100%;
}

/* 响应式调整 */
@media (max-width: 480px) {
  .mobile-menu-drawer {
    size: '90%';
  }

  .mobile-nav-item {
    padding: 14px 16px;
    font-size: 15px;
  }

  .search-input {
    width: 120px;
  }
}
</style>
