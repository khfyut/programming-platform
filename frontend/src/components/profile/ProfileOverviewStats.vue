<template>
  <section class="stats-grid">
    <article v-for="item in statCards" :key="item.label" class="stat-card">
      <div class="stat-accent"></div>
      <div class="stat-label">{{ item.label }}</div>
      <div class="stat-value">{{ item.value }}</div>
      <div class="stat-hint">{{ item.hint }}</div>
    </article>
  </section>
</template>

<script setup>
defineProps({
  statCards: {
    type: Array,
    default: () => []
  }
})
</script>

<style scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 20px;
}

.stat-card {
  position: relative;
  overflow: hidden;
  padding: 18px;
  border: 1px solid var(--border-color);
  border-radius: 22px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.02), transparent 50%),
    var(--bg-card);
  box-shadow: var(--shadow-md);
}

.stat-accent {
  width: 42px;
  height: 4px;
  border-radius: 999px;
  background: var(--gradient-brand);
}

.stat-label {
  margin-top: 12px;
  color: var(--text-tertiary);
  font-size: 13px;
}

.stat-value {
  margin: 10px 0 8px;
  font-family: var(--font-display);
  font-size: 32px;
  color: var(--text-primary);
  text-shadow: 0 0 24px rgba(0, 209, 255, 0.12);
}

.stat-hint {
  color: var(--text-secondary);
  line-height: 1.7;
}

@media (max-width: 760px) {
  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
