<template>
  <Teleport to="body">
    <Transition name="context-menu">
      <div 
        v-if="visible" 
        class="context-menu-overlay"
        @click="handleOverlayClick"
      >
        <div 
          class="context-menu"
          :style="{ left: position.x + 'px', top: position.y + 'px' }"
          @click.stop
        >
          <div class="menu-header">
            <el-icon class="menu-header-icon"><ChatDotRound /></el-icon>
            <span class="menu-header-text">AI 助手</span>
          </div>
          
          <div class="menu-divider"></div>
          
          <div 
            v-for="item in menuItems" 
            :key="item.id"
            class="menu-item"
            @click="handleMenuClick(item)"
          >
            <el-icon class="menu-item-icon">
              <component :is="item.icon" />
            </el-icon>
            <span class="menu-item-text">{{ item.label }}</span>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { ChatDotRound, Document, RefreshLeft, Warning, Edit, Star } from '@element-plus/icons-vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  position: {
    type: Object,
    default: () => ({ x: 0, y: 0 })
  }
})

const emit = defineEmits(['close', 'select'])

const menuItems = [
  { id: 'explain', label: '解释代码', icon: Document, prompt: '请详细解释以下代码的功能和逻辑：' },
  { id: 'optimize', label: '优化代码', icon: RefreshLeft, prompt: '请优化以下代码，提高性能和可读性：' },
  { id: 'find-bugs', label: '查找问题', icon: Warning, prompt: '请分析以下代码，找出潜在的问题和错误：' },
  { id: 'add-comments', label: '添加注释', icon: Edit, prompt: '请为以下代码添加详细的中文注释：' },
  { id: 'suggest', label: '代码建议', icon: Star, prompt: '请为以下代码提供改进建议和最佳实践：' }
]

const handleMenuClick = (item) => {
  emit('select', item)
  emit('close')
}

const handleOverlayClick = () => {
  emit('close')
}

const handleKeyDown = (e) => {
  if (e.key === 'Escape' && props.visible) {
    emit('close')
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleKeyDown)
})

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleKeyDown)
})
</script>

<style scoped>
.context-menu-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9999;
}

.context-menu {
  position: fixed;
  min-width: 220px;
  background: var(--leetcode-bg, #FFFFFF);
  border-radius: 12px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15), 0 8px 16px rgba(0, 0, 0, 0.1);
  border: 1px solid var(--leetcode-border, #E5E7EB);
  padding: 8px;
  z-index: 10000;
  backdrop-filter: blur(10px);
}

.menu-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  margin-bottom: 4px;
}

.menu-header-icon {
  color: var(--leetcode-primary, #0066FF);
  font-size: 18px;
}

.menu-header-text {
  font-size: 14px;
  font-weight: 600;
  color: var(--leetcode-text, #24292F);
}

.menu-divider {
  height: 1px;
  background: var(--leetcode-border, #E5E7EB);
  margin: 4px 0;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: var(--leetcode-text, #24292F);
}

.menu-item:hover {
  background: var(--leetcode-primary, #0066FF);
  color: white;
  transform: translateX(4px);
}

.menu-item-icon {
  font-size: 16px;
  flex-shrink: 0;
}

.menu-item-text {
  font-size: 14px;
  font-weight: 500;
}

.context-menu-enter-active,
.context-menu-leave-active {
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.context-menu-enter-from,
.context-menu-leave-to {
  opacity: 0;
  transform: scale(0.95);
}

.context-menu-enter-to,
.context-menu-leave-from {
  opacity: 1;
  transform: scale(1);
}
</style>
