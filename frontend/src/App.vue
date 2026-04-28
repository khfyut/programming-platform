<template>
  <div class="app-shell">
    <div class="app-shell__gradient" />
    <div class="app-shell__grid" />
    <router-view />
    <GlobalLearningCoach />
  </div>
</template>

<script setup>
import GlobalLearningCoach from '@/components/agent/GlobalLearningCoach.vue'
</script>

<style>
.app-shell {
  position: relative;
  min-height: 100vh;
  overflow-x: clip;
  isolation: isolate;
  background: linear-gradient(180deg, #f8fbff 0%, #f5f7fa 100%);
}

.app-shell__gradient,
.app-shell__grid {
  pointer-events: none;
  position: fixed;
  inset: 0;
}

.app-shell > :not(.app-shell__gradient):not(.app-shell__grid):not(.global-coach) {
  position: relative;
  z-index: 1;
}

.app-shell__gradient {
  background: linear-gradient(180deg, #f8fbff 0%, #f5f7fa 100%);
  z-index: 0;
}

.app-shell__grid {
  background-image:
    linear-gradient(rgba(120, 160, 255, 0.045) 1px, transparent 1px),
    linear-gradient(90deg, rgba(120, 160, 255, 0.045) 1px, transparent 1px);
  background-size: 32px 32px;
  mask-image: linear-gradient(to bottom, rgba(0, 0, 0, 0.42), transparent 92%);
  z-index: 0;
}
</style>
