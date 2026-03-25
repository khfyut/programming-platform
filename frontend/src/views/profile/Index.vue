<template>
  <div class="profile-page">
    <div class="profile-container">
      <!-- 左侧导航 -->
      <aside class="profile-sidebar">
        <div class="sidebar-header">
          <el-avatar :size="48" :src="userStore.userInfo?.avatarUrl">
            <el-icon><User /></el-icon>
          </el-avatar>
          <span class="username">{{ userStore.userInfo?.username || '用户' }}</span>
        </div>
        
        <nav class="sidebar-nav">
          <router-link
            v-for="item in menuItems"
            :key="item.name"
            :to="{ name: item.name }"
            class="nav-item"
            :class="{ active: currentRoute === item.name }"
          >
            <el-icon :size="18">
              <component :is="item.icon" />
            </el-icon>
            <span>{{ item.label }}</span>
          </router-link>
        </nav>
      </aside>

      <!-- 右侧内容区 -->
      <main class="profile-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  User,
  DataLine,
  Reading,
  Collection,
  Setting
} from '@element-plus/icons-vue'

const route = useRoute()
const userStore = useUserStore()

const currentRoute = computed(() => route.name)

const menuItems = [
  { name: 'ProfileOverview', label: '概览', icon: 'User' },
  { name: 'ProfileStatistics', label: '学习数据', icon: 'DataLine' },
  { name: 'ProfileProgress', label: '学习进度', icon: 'Reading' },
  { name: 'ProfileContent', label: '我的内容', icon: 'Collection' },
  { name: 'ProfileSettings', label: '设置', icon: 'Setting' }
]
</script>

<style scoped>
.profile-page {
  min-height: calc(100vh - 60px);
  background: #f5f7fa;
  padding: 24px;
}

.profile-container {
  display: flex;
  max-width: 1400px;
  margin: 0 auto;
  gap: 24px;
  min-height: calc(100vh - 108px);
}

/* 左侧导航 */
.profile-sidebar {
  width: 240px;
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  height: fit-content;
  position: sticky;
  top: 24px;
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 24px;
  border-bottom: 1px solid #e4e7ed;
  margin-bottom: 16px;
}

.username {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 8px;
  color: #606266;
  text-decoration: none;
  transition: all 0.3s ease;
  cursor: pointer;
}

.nav-item:hover {
  background: #f5f7fa;
  color: #409eff;
}

.nav-item.active {
  background: #ecf5ff;
  color: #409eff;
  font-weight: 500;
}

/* 右侧内容区 */
.profile-content {
  flex: 1;
  min-width: 0;
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .profile-container {
    flex-direction: column;
  }
  
  .profile-sidebar {
    width: 100%;
    position: static;
  }
  
  .sidebar-nav {
    flex-direction: row;
    flex-wrap: wrap;
  }
  
  .nav-item span {
    display: none;
  }
}
</style>
