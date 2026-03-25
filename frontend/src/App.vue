<template>
  <div class="app-shell">
    <div class="app-shell__gradient"></div>
    <div class="app-shell__grid"></div>
    <div class="app-shell__glow app-shell__glow--left"></div>
    <div class="app-shell__glow app-shell__glow--right"></div>
    <router-view />
  </div>
</template>

<script setup>
</script>

<style>
.app-shell {
  position: relative;
  min-height: 100vh;
  overflow-x: clip;
}

.app-shell__gradient,
.app-shell__grid,
.app-shell__glow {
  pointer-events: none;
  position: fixed;
  inset: 0;
}

.app-shell__gradient {
  background:
    radial-gradient(circle at 18% 20%, rgba(0, 255, 255, 0.12), transparent 28%),
    radial-gradient(circle at 82% 14%, rgba(0, 102, 255, 0.16), transparent 30%),
    radial-gradient(circle at 60% 72%, rgba(58, 130, 246, 0.08), transparent 35%);
  z-index: 0;
}

.app-shell__grid {
  background-image:
    linear-gradient(rgba(120, 160, 255, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(120, 160, 255, 0.08) 1px, transparent 1px);
  background-size: 32px 32px;
  mask-image: linear-gradient(to bottom, rgba(0, 0, 0, 0.8), transparent 92%);
  z-index: 0;
}

.app-shell__glow {
  filter: blur(80px);
  opacity: 0.4;
  z-index: 0;
}

.app-shell__glow--left {
  inset: 12% auto auto -10%;
  width: 22rem;
  height: 22rem;
  background: rgba(0, 255, 255, 0.18);
}

.app-shell__glow--right {
  inset: auto -8% 12% auto;
  width: 24rem;
  height: 24rem;
  background: rgba(0, 102, 255, 0.18);
}
</style>
