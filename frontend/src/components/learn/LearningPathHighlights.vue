<template>
  <div class="highlights">
    <section v-if="inProgressPaths.length > 0" class="section-container">
      <h2 class="section-title">进行中路径</h2>
      <div class="progress-cards">
        <div
          v-for="path in inProgressPaths"
          :key="path.id"
          class="progress-card"
          @click="emit('open-path', path.id)"
        >
          <div class="progress-icon" :style="{ background: path.gradient }">
            <el-icon :size="32"><component :is="path.icon" /></el-icon>
          </div>
          <div class="progress-info">
            <h3 class="progress-name">{{ path.name }}</h3>
            <div class="progress-bar-wrapper">
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: `${path.progress || 0}%` }"></div>
              </div>
              <div class="progress-row">
                <span class="progress-text">完成进度</span>
                <span class="progress-count">{{ path.completedLevels || 0 }} / {{ path.totalLevels || 0 }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="section-container">
      <div class="section-title-row">
        <h2 class="section-title">精选路径</h2>
        <span class="section-hint">适合快速开始，或切换新的学习方向</span>
      </div>

      <div v-if="featuredPaths.length > 0" class="featured-grid">
        <div
          v-for="path in featuredPaths"
          :key="path.id"
          class="featured-card"
          :style="{ background: path.gradient }"
          @click="emit('open-path', path.id)"
        >
          <div class="featured-content">
            <h3 class="featured-name">{{ path.name }}</h3>
            <p class="featured-desc">{{ path.description || '从这里开始下一段训练路径。' }}</p>
          </div>
          <div class="featured-icon">
            <el-icon :size="48"><component :is="path.icon" /></el-icon>
          </div>
        </div>
      </div>
      <div v-else class="featured-empty">
        <el-empty description="暂无精选路径" />
      </div>
    </section>
  </div>
</template>

<script setup>
defineProps({
  inProgressPaths: {
    type: Array,
    default: () => []
  },
  featuredPaths: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['open-path'])
</script>

<style scoped>
.section-container {
  margin-bottom: 32px;
}

.section-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.section-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--leetcode-text, #24292f);
}

.section-hint {
  font-size: 13px;
  color: var(--leetcode-text-secondary, #6b7280);
}

.progress-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.progress-card {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: var(--leetcode-bg, #ffffff);
  border: 1px solid var(--leetcode-border, #e5e7eb);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.progress-card:hover {
  border-color: var(--leetcode-primary, #0066ff);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 102, 255, 0.1);
}

.progress-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.progress-info {
  flex: 1;
}

.progress-name {
  margin: 0 0 12px;
  font-size: 16px;
  font-weight: 600;
  color: var(--leetcode-text, #24292f);
}

.progress-bar-wrapper {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.progress-bar {
  height: 6px;
  background: var(--leetcode-bg-secondary, #f7f8fa);
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: var(--leetcode-primary, #0066ff);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.progress-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.progress-text {
  font-size: 12px;
  color: var(--leetcode-text-secondary, #6b7280);
}

.progress-count {
  font-size: 12px;
  font-weight: 500;
  color: var(--leetcode-primary, #0066ff);
}

.featured-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.featured-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 24px;
  border-radius: 12px;
  color: #fff;
  cursor: pointer;
  transition: all 0.2s ease;
}

.featured-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.featured-content {
  flex: 1;
}

.featured-name {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 600;
}

.featured-desc {
  margin: 0;
  font-size: 14px;
  line-height: 1.5;
  opacity: 0.9;
}

.featured-icon {
  opacity: 0.8;
}

.featured-empty {
  padding: 40px 0;
  background: var(--leetcode-bg, #ffffff);
  border: 1px solid var(--leetcode-border, #e5e7eb);
  border-radius: 12px;
}

@media (max-width: 768px) {
  .section-title-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .progress-cards,
  .featured-grid {
    grid-template-columns: 1fr;
  }
}
</style>
