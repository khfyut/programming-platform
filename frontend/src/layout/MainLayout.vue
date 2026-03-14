<template>
  <div class="leetcode-layout">
    <el-header class="leetcode-header">
      <div class="header-container">
        <div class="header-left">
          <div class="logo" @click="router.push('/')">
            <div class="logo-icon">
              <el-icon :size="24"><Edit /></el-icon>
              <div class="logo-dot"></div>
            </div>
            <span class="logo-text">CodeMaster</span>
          </div>
          <nav class="nav-menu">
            <router-link to="/" class="nav-item" :class="{ active: route.path === '/' }">
              首页
            </router-link>
            <router-link to="/problems" class="nav-item" :class="{ active: route.path === '/problems' || route.path.startsWith('/problem') }">
              题库
            </router-link>
            <router-link to="/code-run" class="nav-item" :class="{ active: route.path === '/code-run' }">
              在线运行
            </router-link>
            <router-link to="/ai" class="nav-item" :class="{ active: route.path === '/ai' }">
              AI答疑
            </router-link>
            <router-link to="/learn" class="nav-item" :class="{ active: route.path === '/learn' }">
              学习记录
            </router-link>
            <router-link to="/submissions" class="nav-item" :class="{ active: route.path === '/submissions' }">
              提交记录
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
          <el-dropdown @command="handleCommand" trigger="click">
            <div class="user-info">
              <div class="user-avatar-wrapper">
                <el-avatar :size="36" :icon="UserFilled" />
                <div class="online-indicator"></div>
              </div>
              <span class="username">{{ userStore.userInfo?.username }}</span>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="language">
                  <el-icon><Setting /></el-icon>
                  设置语言
                </el-dropdown-item>
                <el-dropdown-item v-if="userStore.userInfo?.role === 1" command="admin">
                  <el-icon><Setting /></el-icon>
                  管理后台
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>
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
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Edit,
  Search,
  Setting,
  UserFilled,
  SwitchButton,
  ArrowDown,
  Sunny,
  Moon
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()

const isDark = computed(() => themeStore.isDark)
const searchKeyword = ref('')
const languageDialogVisible = ref(false)
const selectedLanguage = ref(userStore.userInfo?.language || 'java')

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
  gap: 40px;
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

/* 深色模式适配 */
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

.user-info:hover .dropdown-icon {
  transform: rotate(180deg);
  color: var(--leetcode-primary, #0066FF);
}

.leetcode-main {
  margin-top: 60px;
  padding: 0;
  background: var(--bg-primary);
  min-height: calc(100vh - 60px);
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
}
</style>
