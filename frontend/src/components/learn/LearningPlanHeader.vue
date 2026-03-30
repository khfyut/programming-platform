<template>
  <div class="page-header">
    <div class="header-left">
      <h1 class="page-title">学习计划</h1>
      <p class="page-subtitle">从精选路径开始，逐步推进自己的编程训练节奏。</p>
    </div>
    <div class="header-right">
      <el-button class="my-plan-btn" @click="emit('open-plan')">
        我的学习计划
        <el-icon><ArrowRight /></el-icon>
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ArrowRight } from '@element-plus/icons-vue'

const emit = defineEmits(['open-plan'])
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
  margin-bottom: 32px;
}

.page-title {
  margin: 0 0 8px;
  font-size: 28px;
  font-weight: 700;
  color: var(--leetcode-text, #24292f);
}

.page-subtitle {
  margin: 0;
  font-size: 14px;
  color: var(--leetcode-text-secondary, #6b7280);
}

.my-plan-btn {
  display: flex;
  align-items: center;
  gap: 8px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
}
</style>
