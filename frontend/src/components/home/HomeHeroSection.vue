<template>
  <div class="background-animation"></div>

  <nav class="navbar">
    <div class="container">
      <div class="navbar-brand">
        <router-link to="/" class="logo">
          <span class="logo-text">CodeMaster</span>
          <span class="logo-icon">&lt;/&gt;</span>
        </router-link>
      </div>

      <div class="navbar-nav">
        <router-link to="/problems" class="nav-link">题库练习</router-link>
        <router-link to="/learn" class="nav-link">学习路径</router-link>
        <router-link to="/community" class="nav-link">社区交流</router-link>
        <router-link to="/ai" class="nav-link ai-link">AI 助手</router-link>
      </div>

      <div class="navbar-actions">
        <button type="button" @click="themeStore.toggleTheme()" class="theme-toggle">
          <span v-if="!themeStore.isDark">浅色</span>
          <span v-else>深色</span>
        </button>
        <template v-if="!isLoggedIn">
          <router-link to="/login" class="btn btn-outline">登录</router-link>
          <router-link to="/register" class="btn btn-primary">注册</router-link>
        </template>
        <template v-else>
          <router-link to="/learn" class="btn btn-outline">学习中心</router-link>
          <router-link to="/profile" class="btn btn-primary">个人主页</router-link>
        </template>
      </div>
    </div>
  </nav>

  <section class="hero">
    <div class="hero-background">
      <div class="grid-lines"></div>
      <div class="floating-elements">
        <div class="floating-code code-1">function learn() {</div>
        <div class="floating-code code-2">return betterPractice;</div>
        <div class="floating-code code-3">const progress = track();</div>
      </div>
    </div>

    <div class="container hero-layout">
      <div class="hero-content">
        <div class="hero-badge">
          <span class="badge-dot"></span>
          <span>AI 编程学习</span>
        </div>

        <h1 class="hero-title">
          <span class="title-line">练题、路径、反馈</span>
          <span class="title-highlight">一站完成</span>
          <span class="title-line">下一步更清楚</span>
        </h1>

        <p class="hero-subtitle">
          按路径学习，在线练题，提交后获得反馈和 AI 建议。
        </p>

        <div class="hero-actions">
          <router-link :to="primaryAction.to" class="btn btn-primary btn-large">
            <span class="btn-text">{{ primaryAction.label }}</span>
            <span class="btn-icon">-&gt;</span>
          </router-link>
          <router-link :to="secondaryAction.to" class="btn btn-outline btn-large">
            <span class="btn-text">{{ secondaryAction.label }}</span>
            <span class="btn-icon">-&gt;</span>
          </router-link>
        </div>

        <div class="hero-highlights">
          <span class="highlight-pill">路径学习</span>
          <span class="highlight-pill">提交反馈</span>
          <span class="highlight-pill">AI 复盘</span>
        </div>

        <div class="hero-stats">
          <div class="stat-item">
            <div class="stat-number">题库</div>
            <div class="stat-label">练习与解析</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-number">路径</div>
            <div class="stat-label">串联知识点</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-number">反馈</div>
            <div class="stat-label">定位薄弱点</div>
          </div>
        </div>
      </div>

      <div class="hero-visual">
        <div class="code-editor">
          <div class="editor-header">
            <div class="editor-dots">
              <span class="dot red"></span>
              <span class="dot yellow"></span>
              <span class="dot green"></span>
            </div>
            <div class="editor-title">learning-session.ts</div>
          </div>

          <div class="editor-content">
            <div class="code-line"><span class="keyword">const</span> <span class="class-name">session</span> = {</div>
            <div class="code-line">  <span class="type">path</span>: <span class="string">'算法入门'</span>,</div>
            <div class="code-line">  <span class="type">focus</span>: <span class="string">'数组哈希'</span>,</div>
            <div class="code-line">  <span class="type">next</span>: <span class="string">'提交后复盘'</span></div>
            <div class="code-line">}</div>
          </div>

          <div class="signal-grid">
            <div class="signal-card">
              <span class="signal-label">当前任务</span>
              <strong>完成一题</strong>
            </div>
            <div class="signal-card">
              <span class="signal-label">AI 建议</span>
              <strong>先看失败点</strong>
            </div>
          </div>

          <div class="ai-assistant">
            <div class="ai-icon">AI</div>
            <div class="ai-message">
              <div class="ai-message-title">建议已生成</div>
              <div class="ai-message-text">根据提交结果安排下一步</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { useUserStore } from '@/stores/user'

const themeStore = useThemeStore()
const userStore = useUserStore()

const isLoggedIn = computed(() => Boolean(userStore.token))

const primaryAction = computed(() => {
  if (isLoggedIn.value) {
    return {
      label: '开始学习',
      to: '/learn'
    }
  }

  return {
    label: '开始学习',
    to: '/register'
  }
})

const secondaryAction = computed(() => {
  if (isLoggedIn.value) {
    return {
      label: '去刷题',
      to: '/problems'
    }
  }

  return {
    label: '去刷题',
    to: '/problems'
  }
})
</script>

<style scoped>
.background-animation {
  position: fixed;
  inset: 0;
  background:
    radial-gradient(circle at 20% 50%, rgba(59, 130, 246, 0.05) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(139, 92, 246, 0.05) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

.container {
  position: relative;
  z-index: 1;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background-color: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--border-color);
  transition: all var(--transition-base);
}

.navbar .container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 70px;
  gap: 16px;
}

.navbar-brand .logo {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--primary-color);
  text-decoration: none;
  font-size: 1.5rem;
  font-weight: 700;
}

.logo-icon {
  font-size: 1rem;
  padding: 4px 8px;
  border-radius: 999px;
  background: rgba(59, 130, 246, 0.12);
}

.navbar-nav {
  display: flex;
  gap: 30px;
}

.nav-link {
  position: relative;
  padding: 8px 0;
  color: var(--text-primary);
  text-decoration: none;
  font-weight: 500;
  transition: all var(--transition-base);
}

.nav-link:hover {
  color: var(--primary-color);
}

.nav-link::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: 0;
  width: 0;
  height: 2px;
  background: var(--gradient-primary);
  transition: width var(--transition-base);
}

.nav-link:hover::after {
  width: 100%;
}

.ai-link {
  background: var(--gradient-ai);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.navbar-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.theme-toggle {
  padding: 8px 10px;
  border: none;
  border-radius: 8px;
  background: none;
  color: var(--text-primary);
  cursor: pointer;
  transition: all var(--transition-base);
}

.theme-toggle:hover {
  background-color: var(--bg-tertiary);
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 10px;
  border: none;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  cursor: pointer;
  transition: all var(--transition-base);
}

.btn-primary {
  background: var(--gradient-primary);
  color: white;
  box-shadow: 0 4px 6px rgba(59, 130, 246, 0.3);
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(59, 130, 246, 0.4);
}

.btn-outline {
  background-color: transparent;
  border: 1px solid var(--primary-color);
  color: var(--primary-color);
}

.btn-outline:hover {
  background-color: var(--primary-color);
  color: white;
}

.btn-large {
  padding: 14px 28px;
  font-size: 16px;
}

.btn:hover .btn-icon {
  transform: translateX(4px);
}

.btn-icon {
  transition: transform var(--transition-base);
}

.hero {
  position: relative;
  overflow: hidden;
  padding: 120px 0 100px;
}

.hero-background {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.hero-layout {
  display: grid;
  grid-template-columns: 1.05fr 0.95fr;
  gap: 64px;
  align-items: center;
}

.grid-lines {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(59, 130, 246, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(59, 130, 246, 0.08) 1px, transparent 1px);
  background-size: 50px 50px;
  mask-image: radial-gradient(circle at center, black 40%, transparent 80%);
}

.floating-elements {
  position: absolute;
  inset: 0;
}

.floating-code {
  position: absolute;
  padding: 8px 14px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.55);
  color: #dbeafe;
  font-family: Consolas, Monaco, monospace;
  font-size: 13px;
  backdrop-filter: blur(12px);
}

.code-1 { top: 18%; right: 12%; }
.code-2 { top: 58%; right: 6%; }
.code-3 { top: 76%; left: 8%; }

.hero-content {
  position: relative;
  z-index: 1;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 24px;
  padding: 10px 16px;
  border: 1px solid rgba(59, 130, 246, 0.15);
  border-radius: 999px;
  background: rgba(59, 130, 246, 0.08);
  color: var(--primary-color);
  font-weight: 600;
}

.badge-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--accent-color);
}

.hero-title {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin: 0 0 20px;
  line-height: 1.05;
  font-size: clamp(40px, 7vw, 72px);
  letter-spacing: -0.04em;
}

.title-line {
  color: var(--text-primary);
}

.title-highlight {
  color: transparent;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  background-clip: text;
}

.hero-subtitle {
  max-width: 620px;
  margin: 0 0 32px;
  color: var(--text-secondary);
  line-height: 1.8;
  font-size: 18px;
}

.hero-actions {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.hero-highlights {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 36px;
}

.highlight-pill {
  padding: 6px 12px;
  border: 1px solid rgba(59, 130, 246, 0.16);
  border-radius: 999px;
  background: rgba(59, 130, 246, 0.08);
  color: var(--text-secondary);
  font-size: 13px;
}

.hero-stats {
  display: flex;
  align-items: stretch;
  gap: 22px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-width: 180px;
}

.stat-number {
  font-size: 28px;
  font-weight: 800;
  color: var(--text-primary);
}

.stat-label {
  color: var(--text-tertiary);
  font-size: 14px;
  line-height: 1.6;
}

.stat-divider {
  width: 1px;
  background: var(--border-color);
}

.hero-visual {
  position: relative;
}

.code-editor {
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(59, 130, 246, 0.16);
  border-radius: 24px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.06), transparent 35%),
    rgba(15, 23, 42, 0.94);
  box-shadow: 0 24px 80px rgba(15, 23, 42, 0.28);
}

.editor-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 18px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.editor-dots {
  display: flex;
  gap: 8px;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.dot.red { background: #ef4444; }
.dot.yellow { background: #f59e0b; }
.dot.green { background: #10b981; }

.editor-title {
  color: #cbd5e1;
  font-size: 13px;
}

.editor-content {
  padding: 24px 22px 18px;
  color: #e2e8f0;
  font-family: Consolas, Monaco, monospace;
  font-size: 14px;
  line-height: 1.9;
}

.code-line {
  white-space: nowrap;
}

.keyword { color: #c084fc; }
.class-name { color: #38bdf8; }
.type { color: #22c55e; }
.string { color: #fda4af; }

.signal-grid {
  display: grid;
  gap: 12px;
  padding: 0 22px 84px;
}

.signal-card {
  padding: 14px 16px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.04);
}

.signal-label {
  display: block;
  margin-bottom: 6px;
  color: #94a3b8;
  font-size: 12px;
  letter-spacing: 0.08em;
}

.signal-card strong {
  color: #f8fafc;
  line-height: 1.6;
}

.ai-assistant {
  position: absolute;
  right: 18px;
  bottom: 18px;
  display: flex;
  align-items: flex-start;
  gap: 10px;
  max-width: 280px;
  padding: 12px 14px;
  border-radius: 16px;
  background: rgba(59, 130, 246, 0.16);
  backdrop-filter: blur(12px);
}

.ai-icon {
  display: grid;
  place-items: center;
  width: 36px;
  height: 36px;
  border-radius: 12px;
  background: var(--gradient-primary);
  color: white;
  font-size: 12px;
  font-weight: 800;
}

.ai-message-title {
  color: #f8fafc;
  font-size: 13px;
  font-weight: 700;
}

.ai-message-text {
  margin-top: 4px;
  color: #dbeafe;
  font-size: 12px;
  line-height: 1.6;
}

@media (max-width: 1080px) {
  .hero-layout {
    grid-template-columns: 1fr;
  }

  .hero {
    padding-top: 80px;
  }
}

@media (max-width: 820px) {
  .navbar .container {
    height: auto;
    padding-top: 16px;
    padding-bottom: 16px;
    flex-wrap: wrap;
    gap: 16px;
  }

  .navbar-nav {
    order: 3;
    width: 100%;
    justify-content: space-between;
    gap: 16px;
  }

  .hero-actions,
  .hero-highlights,
  .hero-stats {
    flex-wrap: wrap;
  }

  .stat-divider {
    display: none;
  }
}

@media (max-width: 560px) {
  .navbar-nav {
    flex-wrap: wrap;
  }

  .navbar-actions {
    width: 100%;
    justify-content: flex-start;
    flex-wrap: wrap;
  }

  .hero-title {
    font-size: 38px;
  }

  .hero-actions .btn {
    width: 100%;
    justify-content: center;
  }

  .ai-assistant {
    left: 18px;
    right: 18px;
    max-width: none;
  }
}
</style>
