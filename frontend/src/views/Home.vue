<template>
  <div class="home" :class="{ 'dark-mode': themeStore.isDark }">
    <HomeHeroSection />
    <HomeContentSections />
  </div>
</template>

<script setup>
import HomeContentSections from '@/components/home/HomeContentSections.vue'
import HomeHeroSection from '@/components/home/HomeHeroSection.vue'
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()
</script>

<style scoped>
.home {
  --primary-color: #3B82F6;
  --primary-hover: #2563EB;
  --primary-light: #60A5FA;
  --secondary-color: #8B5CF6;
  --accent-color: #10B981;
  --bg-primary: #F8FAFC;
  --bg-secondary: #FFFFFF;
  --bg-tertiary: #F1F5F9;
  --bg-card: #FFFFFF;
  --text-primary: #1E293B;
  --text-secondary: #475569;
  --text-tertiary: #64748B;
  --border-color: #E2E8F0;
  --border-light: #F1F5F9;
  --shadow-sm: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  --shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  --shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  --shadow-xl: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
  --gradient-primary: linear-gradient(135deg, #3B82F6 0%, #8B5CF6 100%);
  --gradient-tech: linear-gradient(135deg, #1E3A8A 0%, #7C3AED 100%);
  --gradient-ai: linear-gradient(135deg, #8B5CF6 0%, #EC4899 100%);
  --transition-fast: 0.15s ease;
  --transition-base: 0.3s ease;
  --transition-slow: 0.5s ease;
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
  background-color: var(--bg-primary);
  color: var(--text-primary);
  font-family: 'Inter', 'PingFang SC', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.home.dark-mode {
  --bg-primary: #0F172A;
  --bg-secondary: #1E293B;
  --bg-tertiary: #334155;
  --bg-card: #1E293B;
  --text-primary: #F1F5F9;
  --text-secondary: #E2E8F0;
  --text-tertiary: #94A3B8;
  --border-color: #334155;
  --border-light: #1E293B;
  --shadow-sm: 0 1px 2px 0 rgba(0, 0, 0, 0.3);
  --shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.4);
  --shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.4);
  --shadow-xl: 0 20px 25px -5px rgba(0, 0, 0, 0.5);
}
</style>
