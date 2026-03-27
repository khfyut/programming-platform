<template>
  <div class="profile-workspace">
    <aside class="profile-sidebar">
      <div class="sidebar-user">
        <el-avatar :size="60" :src="userStore.userInfo?.avatarUrl">
          <el-icon><User /></el-icon>
        </el-avatar>
        <div class="sidebar-user-info">
          <div class="sidebar-username">{{ userStore.userInfo?.username || '用户' }}</div>
          <div class="sidebar-subtitle">学习控制台</div>
        </div>
      </div>

      <nav class="sidebar-nav">
        <router-link
          v-for="item in menuItems"
          :key="item.name"
          :to="{ name: item.name }"
          class="nav-item"
          :class="{ active: currentRoute === item.name }"
        >
          <el-icon class="nav-icon">
            <component :is="item.icon" />
          </el-icon>
          <span>{{ item.label }}</span>
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <router-link to="/wrong-book" class="footer-shortcut">
          <el-icon><Warning /></el-icon>
          <span>错题本快捷入口</span>
        </router-link>
      </div>
    </aside>

    <main class="profile-main">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  User,
  DataLine,
  Document,
  Collection,
  Setting,
  Warning
} from '@element-plus/icons-vue'

const route = useRoute()
const userStore = useUserStore()

const currentRoute = computed(() => route.name)

const menuItems = [
  { name: 'UserProfile', label: '总览', icon: User },
  { name: 'ProfileAnalysis', label: '学习分析', icon: DataLine },
  { name: 'ProfileSubmissions', label: '提交记录', icon: Document },
  { name: 'ProfileCollections', label: '收藏与扩展', icon: Collection },
  { name: 'ProfileSettings', label: '资料设置', icon: Setting }
]
</script>

<style scoped>
.profile-workspace {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 20px;
  min-height: calc(100vh - 108px);
}

.profile-sidebar,
.profile-main {
  border: 1px solid var(--border-color);
  border-radius: 22px;
  background: var(--bg-card);
  box-shadow: var(--shadow-md);
}

.profile-sidebar {
  position: sticky;
  top: 24px;
  display: flex;
  flex-direction: column;
  gap: 22px;
  height: fit-content;
  padding: 22px;
  overflow: hidden;
  background:
    radial-gradient(circle at top left, rgba(0, 209, 255, 0.12), transparent 34%),
    var(--bg-card);
}

.profile-sidebar::before {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.03), transparent 30%);
}

.sidebar-user {
  position: relative;
  display: flex;
  align-items: center;
  gap: 14px;
  padding-bottom: 18px;
  border-bottom: 1px solid var(--border-light);
}

.sidebar-user-info {
  min-width: 0;
}

.sidebar-username {
  color: var(--text-primary);
  font-weight: 700;
  font-size: 18px;
}

.sidebar-subtitle {
  margin-top: 4px;
  color: var(--text-secondary);
  font-size: 13px;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.nav-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 46px;
  padding: 0 14px;
  border: 1px solid transparent;
  border-radius: 14px;
  color: var(--text-secondary);
  transition: background var(--transition-fast), border-color var(--transition-fast), color var(--transition-fast), transform var(--transition-fast);
}

.nav-item:hover {
  transform: translateX(2px);
  border-color: var(--border-color);
  background: rgba(255, 255, 255, 0.03);
  color: var(--text-primary);
}

.nav-item.active {
  border-color: var(--border-strong);
  background: var(--sidebar-active);
  color: var(--sidebar-text-active);
  box-shadow: inset 0 0 0 1px rgba(124, 247, 255, 0.12);
}

.nav-item.active::before {
  content: '';
  position: absolute;
  left: 10px;
  top: 10px;
  bottom: 10px;
  width: 3px;
  border-radius: 999px;
  background: var(--gradient-brand);
}

.nav-icon {
  font-size: 18px;
}

.sidebar-footer {
  margin-top: auto;
  padding-top: 16px;
  border-top: 1px solid var(--border-light);
}

.footer-shortcut {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border: 1px dashed var(--border-color);
  border-radius: 14px;
  color: var(--warning-color);
  background: var(--warning-light);
  transition: transform var(--transition-fast), border-color var(--transition-fast), background var(--transition-fast);
}

.footer-shortcut:hover {
  transform: translateY(-1px);
  border-color: rgba(245, 158, 11, 0.35);
  background: rgba(245, 158, 11, 0.18);
}

.profile-main {
  min-width: 0;
  padding: 24px;
  background:
    radial-gradient(circle at top right, rgba(0, 209, 255, 0.06), transparent 24%),
    var(--bg-card);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.18s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 960px) {
  .profile-workspace {
    grid-template-columns: 1fr;
  }

  .profile-sidebar {
    position: static;
  }

  .sidebar-nav {
    flex-direction: row;
    flex-wrap: wrap;
  }

  .nav-item {
    flex: 1 1 calc(50% - 8px);
  }
}

@media (max-width: 640px) {
  .profile-main,
  .profile-sidebar {
    padding: 16px;
  }

  .nav-item {
    flex-basis: 100%;
  }
}
</style>
