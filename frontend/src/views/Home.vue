<template>
  <div class="home" :class="{ 'dark-mode': themeStore.isDark }">
    <div class="background-animation"></div>
    
    <nav class="navbar">
      <div class="container">
        <div class="navbar-brand">
          <router-link to="/" class="logo">
            <span class="logo-text">CodeMaster</span>
            <span class="logo-icon">⚡</span>
          </router-link>
        </div>
        <div class="navbar-nav">
          <router-link to="/problems" class="nav-link">题目练习</router-link>
          <router-link to="/paths" class="nav-link">学习路径</router-link>
          <router-link to="/community" class="nav-link">社区交流</router-link>
          <router-link to="/ai" class="nav-link ai-link">AI助手</router-link>
        </div>
        <div class="navbar-actions">
          <button @click="themeStore.toggleTheme()" class="theme-toggle">
            <span v-if="!themeStore.isDark">🌙</span>
            <span v-else>☀️</span>
          </button>
          <router-link to="/login" class="btn btn-outline">登录</router-link>
          <router-link to="/register" class="btn btn-primary">注册</router-link>
        </div>
      </div>
    </nav>

    <section class="hero">
      <div class="hero-background">
        <div class="grid-lines"></div>
        <div class="floating-elements">
          <div class="floating-code code-1">function learn() {</div>
          <div class="floating-code code-2">return skills;</div>
          <div class="floating-code code-3">const skills = [];</div>
        </div>
      </div>
      <div class="container">
        <div class="hero-content">
          <div class="hero-badge">
            <span class="badge-dot"></span>
            <span>AI驱动的编程学习平台</span>
          </div>
          <h1 class="hero-title">
            <span class="title-line">开启你的</span>
            <span class="title-highlight">编程之旅</span>
            <span class="title-line">，用AI加速学习</span>
          </h1>
          <p class="hero-subtitle">
            通过系统化的学习路径、丰富的编程题目和AI智能辅导，
            快速提升你的编程技能，让学习更高效
          </p>
          <div class="hero-actions">
            <router-link to="/register" class="btn btn-primary btn-large">
              <span class="btn-text">立即注册</span>
              <span class="btn-icon">→</span>
            </router-link>
            <router-link to="/paths" class="btn btn-outline btn-large">
              <span class="btn-text">开始学习</span>
              <span class="btn-icon">→</span>
            </router-link>
          </div>
          <div class="hero-stats">
            <div class="stat-item">
              <div class="stat-number">10,000+</div>
              <div class="stat-label">注册用户</div>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <div class="stat-number">500+</div>
              <div class="stat-label">编程题目</div>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <div class="stat-number">7</div>
              <div class="stat-label">学习路径</div>
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
              <div class="editor-title">Main.java</div>
            </div>
            <div class="editor-content">
              <div class="code-line line-1"><span class="keyword">public class</span> <span class="class-name">LearningPath</span> {</div>
              <div class="code-line line-2">  <span class="keyword">public static void</span> <span class="method-name">main</span>(<span class="type">String</span>[] args) {</div>
              <div class="code-line line-3">    <span class="type">System</span>.out.<span class="method-name">println</span>(<span class="string">"开始学习编程！"</span>);</div>
              <div class="code-line line-4">    <span class="comment">// AI助手将帮助你快速进步</span></div>
              <div class="code-line line-5">  }</div>
              <div class="code-line line-6">}</div>
            </div>
            <div class="ai-assistant">
              <div class="ai-icon">🤖</div>
              <div class="ai-message">
                <div class="ai-typing">
                  <span class="typing-dot"></span>
                  <span class="typing-dot"></span>
                  <span class="typing-dot"></span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="features">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">核心功能</h2>
          <p class="section-subtitle">全方位的编程学习体验，从入门到精通</p>
        </div>
        <div class="features-grid">
          <div class="feature-card">
            <div class="feature-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                <polyline points="14 2 14 8 20 8"></polyline>
                <line x1="16" y1="13" x2="8" y2="13"></line>
                <line x1="16" y1="17" x2="8" y2="17"></line>
                <polyline points="10 9 9 9 8 9"></polyline>
              </svg>
            </div>
            <h3 class="feature-title">题目练习</h3>
            <p class="feature-description">
              丰富的编程题目库，覆盖不同难度和知识点，支持在线判题和实时反馈
            </p>
            <router-link to="/problems" class="feature-link">
              开始练习 <span class="arrow">→</span>
            </router-link>
          </div>

          <div class="feature-card">
            <div class="feature-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                <polyline points="22 4 12 14.01 9 11.01"></polyline>
              </svg>
            </div>
            <h3 class="feature-title">学习路径</h3>
            <p class="feature-description">
              精心设计的学习路径，从基础到进阶，系统化学习编程知识
            </p>
            <router-link to="/paths" class="feature-link">
              探索路径 <span class="arrow">→</span>
            </router-link>
          </div>

          <div class="feature-card">
            <div class="feature-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
              </svg>
            </div>
            <h3 class="feature-title">社区交流</h3>
            <p class="feature-description">
              与其他学习者交流分享，解决编程问题，共同进步
            </p>
            <router-link to="/community" class="feature-link">
              加入社区 <span class="arrow">→</span>
            </router-link>
          </div>

          <div class="feature-card">
            <div class="feature-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect>
                <line x1="8" y1="21" x2="16" y2="21"></line>
                <line x1="12" y1="17" x2="12" y2="21"></line>
              </svg>
            </div>
            <h3 class="feature-title">代码编辑器</h3>
            <p class="feature-description">
              在线代码编辑器，支持多种编程语言，实时运行和调试代码
            </p>
            <router-link to="/editor" class="feature-link">
              开始编码 <span class="arrow">→</span>
            </router-link>
          </div>
        </div>
      </div>
    </section>

    <section class="ai-section">
      <div class="container">
        <div class="ai-content">
          <div class="ai-info">
            <div class="ai-badge">
              <span class="ai-icon">🤖</span>
              <span>AI智能助手</span>
            </div>
            <h2 class="ai-title">让AI成为你的编程导师</h2>
            <p class="ai-description">
              基于阿里云通义千问大模型，提供智能代码提示、错误诊断、学习建议等功能，
              让你的编程学习之路更加顺畅高效
            </p>
            <div class="ai-features">
              <div class="ai-feature">
                <div class="feature-check">✓</div>
                <span>智能代码提示</span>
              </div>
              <div class="ai-feature">
                <div class="feature-check">✓</div>
                <span>错误诊断分析</span>
              </div>
              <div class="ai-feature">
                <div class="feature-check">✓</div>
                <span>个性化学习建议</span>
              </div>
              <div class="ai-feature">
                <div class="feature-check">✓</div>
                <span>实时答疑解惑</span>
              </div>
            </div>
            <router-link to="/ai" class="btn btn-primary btn-large">
              <span class="btn-text">体验AI助手</span>
              <span class="btn-icon">→</span>
            </router-link>
          </div>
          <div class="ai-visual">
            <div class="ai-chat">
              <div class="chat-message user">
                <div class="message-avatar">👤</div>
                <div class="message-content">
                  <div class="message-text">如何优化这段代码的性能？</div>
                </div>
              </div>
              <div class="chat-message ai">
                <div class="message-avatar">🤖</div>
                <div class="message-content">
                  <div class="message-text">根据代码分析，我建议：</div>
                  <div class="message-suggestions">
                    <div class="suggestion-item">1. 使用更高效的数据结构</div>
                    <div class="suggestion-item">2. 减少不必要的循环嵌套</div>
                    <div class="suggestion-item">3. 考虑使用缓存机制</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="paths-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">热门学习路径</h2>
          <router-link to="/paths" class="view-all">查看全部 →</router-link>
        </div>
        <div class="paths-grid">
          <div class="path-card">
            <div class="path-header">
              <div class="path-icon java">
                <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="2" y="2" width="20" height="20" rx="2" ry="2"></rect>
                  <path d="M12 18h.01"></path>
                  <path d="M7 9h10"></path>
                  <path d="M7 13h10"></path>
                </svg>
              </div>
              <div class="path-info">
                <h3 class="path-title">Java编程基础</h3>
                <span class="path-level">基础</span>
              </div>
            </div>
            <p class="path-description">
              从Java基础语法开始，逐步掌握面向对象编程
            </p>
            <div class="path-meta">
              <span class="meta-item">⏱️ 10小时</span>
              <span class="meta-item">📚 8个关卡</span>
            </div>
            <router-link to="/path/1" class="path-link">
              开始学习
            </router-link>
          </div>

          <div class="path-card">
            <div class="path-header">
              <div class="path-icon python">
                <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="2" y="2" width="20" height="20" rx="2" ry="2"></rect>
                  <path d="M12 18h.01"></path>
                  <path d="M7 9h10"></path>
                  <path d="M7 13h10"></path>
                </svg>
              </div>
              <div class="path-info">
                <h3 class="path-title">Python编程入门</h3>
                <span class="path-level">入门</span>
              </div>
            </div>
            <p class="path-description">
              快速掌握Python基础语法和常用库
            </p>
            <div class="path-meta">
              <span class="meta-item">⏱️ 8小时</span>
              <span class="meta-item">📚 6个关卡</span>
            </div>
            <router-link to="/path/2" class="path-link">
              开始学习
            </router-link>
          </div>

          <div class="path-card">
            <div class="path-header">
              <div class="path-icon algorithm">
                <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M18 20V10"></path>
                  <path d="M12 20V4"></path>
                  <path d="M6 20v-6"></path>
                </svg>
              </div>
              <div class="path-info">
                <h3 class="path-title">算法与数据结构</h3>
                <span class="path-level">进阶</span>
              </div>
            </div>
            <p class="path-description">
              学习常用算法和数据结构，提升编程能力
            </p>
            <div class="path-meta">
              <span class="meta-item">⏱️ 15小时</span>
              <span class="meta-item">📚 10个关卡</span>
            </div>
            <router-link to="/path/3" class="path-link">
              开始学习
            </router-link>
          </div>

          <div class="path-card">
            <div class="path-header">
              <div class="path-icon backend">
                <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="2" y="2" width="20" height="8" rx="2" ry="2"></rect>
                  <rect x="2" y="14" width="20" height="8" rx="2" ry="2"></rect>
                  <line x1="6" y1="6" x2="6.01" y2="6"></line>
                  <line x1="6" y1="18" x2="6.01" y2="18"></line>
                </svg>
              </div>
              <div class="path-info">
                <h3 class="path-title">后端开发实战</h3>
                <span class="path-level">高级</span>
              </div>
            </div>
            <p class="path-description">
              掌握Spring Boot框架，构建企业级应用
            </p>
            <div class="path-meta">
              <span class="meta-item">⏱️ 20小时</span>
              <span class="meta-item">📚 12个关卡</span>
            </div>
            <router-link to="/path/4" class="path-link">
              开始学习
            </router-link>
          </div>
        </div>
      </div>
    </section>

    <section class="problems-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">精选题目</h2>
          <router-link to="/problems" class="view-all">查看全部 →</router-link>
        </div>
        <div class="problems-grid">
          <div class="problem-card">
            <div class="problem-header">
              <span class="problem-id">#1</span>
              <span class="problem-difficulty easy">简单</span>
            </div>
            <h3 class="problem-title">
              <router-link to="/problem/1">两数之和</router-link>
            </h3>
            <p class="problem-description">
              给定一个整数数组和一个目标值，找出和为目标值的两个数
            </p>
            <div class="problem-tags">
              <span class="tag">数组</span>
              <span class="tag">哈希表</span>
            </div>
          </div>

          <div class="problem-card">
            <div class="problem-header">
              <span class="problem-id">#11</span>
              <span class="problem-difficulty medium">中等</span>
            </div>
            <h3 class="problem-title">
              <router-link to="/problem/11">冒泡排序</router-link>
            </h3>
            <p class="problem-description">
              使用冒泡排序算法对数组进行升序排序
            </p>
            <div class="problem-tags">
              <span class="tag">排序</span>
              <span class="tag">算法</span>
            </div>
          </div>

          <div class="problem-card">
            <div class="problem-header">
              <span class="problem-id">#13</span>
              <span class="problem-difficulty medium">中等</span>
            </div>
            <h3 class="problem-title">
              <router-link to="/problem/13">链表反转</router-link>
            </h3>
            <p class="problem-description">
              反转一个单链表
            </p>
            <div class="problem-tags">
              <span class="tag">链表</span>
              <span class="tag">数据结构</span>
            </div>
          </div>

          <div class="problem-card">
            <div class="problem-header">
              <span class="problem-id">#14</span>
              <span class="problem-difficulty easy">简单</span>
            </div>
            <h3 class="problem-title">
              <router-link to="/problem/14">斐波那契数列</router-link>
            </h3>
            <p class="problem-description">
              计算第n个斐波那契数
            </p>
            <div class="problem-tags">
              <span class="tag">动态规划</span>
              <span class="tag">递归</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="cta-section">
      <div class="container">
        <div class="cta-content">
          <h2 class="cta-title">准备好开始你的编程之旅了吗？</h2>
          <p class="cta-subtitle">
            立即注册，开启AI辅助的编程学习体验
          </p>
          <div class="cta-actions">
            <router-link to="/register" class="btn btn-primary btn-large">
              <span class="btn-text">免费注册</span>
              <span class="btn-icon">→</span>
            </router-link>
            <router-link to="/paths" class="btn btn-outline btn-large">
              <span class="btn-text">浏览学习路径</span>
              <span class="btn-icon">→</span>
            </router-link>
          </div>
        </div>
      </div>
    </section>

    <footer class="footer">
      <div class="container">
        <div class="footer-content">
          <div class="footer-section">
            <div class="footer-brand">
              <span class="footer-logo">CodeMaster</span>
              <span class="footer-logo-icon">⚡</span>
            </div>
            <p class="footer-description">
              专注于编程教育，帮助你快速提升编程技能
            </p>
          </div>
          <div class="footer-section">
            <h3 class="footer-title">功能</h3>
            <ul class="footer-links">
              <li><router-link to="/problems">题目练习</router-link></li>
              <li><router-link to="/paths">学习路径</router-link></li>
              <li><router-link to="/community">社区交流</router-link></li>
              <li><router-link to="/ai">AI助手</router-link></li>
            </ul>
          </div>
          <div class="footer-section">
            <h3 class="footer-title">资源</h3>
            <ul class="footer-links">
              <li><a href="#">帮助中心</a></li>
              <li><a href="#">教程文档</a></li>
              <li><a href="#">常见问题</a></li>
              <li><a href="#">联系我们</a></li>
            </ul>
          </div>
          <div class="footer-section">
            <h3 class="footer-title">关于</h3>
            <ul class="footer-links">
              <li><a href="#">关于我们</a></li>
              <li><a href="#">隐私政策</a></li>
              <li><a href="#">服务条款</a></li>
              <li><a href="#">Cookie政策</a></li>
            </ul>
          </div>
        </div>
        <div class="footer-bottom">
          <p class="copyright">
            © 2026 CodeMaster. All rights reserved.
          </p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()
</script>

<style scoped>
:root {
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
}

.dark-mode {
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

.home {
  font-family: 'Inter', 'PingFang SC', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  color: var(--text-primary);
  background-color: var(--bg-primary);
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
}

.background-animation {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle at 20% 50%, rgba(59, 130, 246, 0.05) 0%, transparent 50%),
              radial-gradient(circle at 80% 20%, rgba(139, 92, 246, 0.05) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  position: relative;
  z-index: 1;
}

.navbar {
  background-color: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--border-color);
  position: sticky;
  top: 0;
  z-index: 100;
  transition: all var(--transition-base);
}

.dark-mode .navbar {
  background-color: rgba(15, 23, 42, 0.8);
}

.navbar .container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 70px;
}

.navbar-brand .logo {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--primary-color);
  transition: all var(--transition-base);
}

.logo-icon {
  font-size: 1.8rem;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

.navbar-nav {
  display: flex;
  gap: 30px;
}

.nav-link {
  text-decoration: none;
  color: var(--text-primary);
  font-weight: 500;
  transition: all var(--transition-base);
  position: relative;
  padding: 8px 0;
}

.nav-link:hover {
  color: var(--primary-color);
}

.nav-link::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
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
  gap: 15px;
  align-items: center;
}

.theme-toggle {
  background: none;
  border: none;
  font-size: 1.2rem;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  transition: all var(--transition-base);
  color: var(--text-primary);
}

.theme-toggle:hover {
  background-color: var(--bg-tertiary);
}

.btn {
  padding: 10px 20px;
  border-radius: 8px;
  font-weight: 500;
  text-decoration: none;
  transition: all var(--transition-base);
  cursor: pointer;
  border: none;
  font-size: 14px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
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
  color: var(--primary-color);
  border: 1px solid var(--primary-color);
}

.btn-outline:hover {
  background-color: var(--primary-color);
  color: white;
}

.btn-large {
  padding: 14px 28px;
  font-size: 16px;
}

.btn-text {
  font-weight: 500;
}

.btn-icon {
  transition: transform var(--transition-base);
}

.btn:hover .btn-icon {
  transform: translateX(4px);
}

.hero {
  padding: 120px 0 100px;
  position: relative;
  overflow: hidden;
}

.hero-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.grid-lines {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: 
    linear-gradient(rgba(59, 130, 246, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(59, 130, 246, 0.03) 1px, transparent 1px);
  background-size: 50px 50px;
  animation: gridMove 20s linear infinite;
}

@keyframes gridMove {
  0% {
    transform: translate(0, 0);
  }
  100% {
    transform: translate(50px, 50px);
  }
}

.floating-elements {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.floating-code {
  position: absolute;
  font-family: 'Fira Code', monospace;
  font-size: 14px;
  color: var(--primary-color);
  opacity: 0.15;
  white-space: nowrap;
  animation: float 8s ease-in-out infinite;
}

.code-1 {
  top: 20%;
  left: 10%;
  animation-delay: 0s;
}

.code-2 {
  top: 60%;
  right: 15%;
  animation-delay: 2s;
}

.code-3 {
  bottom: 30%;
  left: 20%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(2deg);
  }
}

.hero .container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  align-items: center;
}

.hero-content {
  max-width: 600px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: rgba(59, 130, 246, 0.1);
  color: var(--primary-color);
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 24px;
  border: 1px solid rgba(59, 130, 246, 0.2);
}

.badge-dot {
  width: 8px;
  height: 8px;
  background: var(--primary-color);
  border-radius: 50%;
  animation: blink 1.5s infinite;
}

@keyframes blink {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.3;
  }
}

.hero-title {
  font-size: 3.5rem;
  font-weight: 800;
  line-height: 1.2;
  margin-bottom: 24px;
  color: var(--text-primary);
}

.title-line {
  display: block;
}

.title-highlight {
  display: block;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 8px 0;
}

.hero-subtitle {
  font-size: 1.125rem;
  color: var(--text-secondary);
  margin-bottom: 32px;
  line-height: 1.6;
}

.hero-actions {
  display: flex;
  gap: 16px;
  margin-bottom: 48px;
}

.hero-stats {
  display: flex;
  align-items: center;
  gap: 24px;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--primary-color);
  margin-bottom: 4px;
}

.stat-label {
  font-size: 0.875rem;
  color: var(--text-tertiary);
}

.stat-divider {
  width: 1px;
  height: 40px;
  background-color: var(--border-color);
}

.hero-visual {
  display: flex;
  justify-content: center;
  align-items: center;
}

.code-editor {
  background: #1E293B;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  position: relative;
  animation: editorFloat 6s ease-in-out infinite;
}

@keyframes editorFloat {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.editor-header {
  background: #334155;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.editor-dots {
  display: flex;
  gap: 8px;
}

.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.dot.red {
  background: #EF4444;
}

.dot.yellow {
  background: #F59E0B;
}

.dot.green {
  background: #10B981;
}

.editor-title {
  color: #94A3B8;
  font-size: 13px;
  font-family: 'Fira Code', monospace;
}

.editor-content {
  padding: 20px;
  font-family: 'Fira Code', monospace;
  font-size: 14px;
  line-height: 1.8;
}

.code-line {
  color: #E2E8F0;
  opacity: 0;
  animation: fadeIn 0.5s ease forwards;
}

.line-1 { animation-delay: 0.1s; }
.line-2 { animation-delay: 0.2s; }
.line-3 { animation-delay: 0.3s; }
.line-4 { animation-delay: 0.4s; }
.line-5 { animation-delay: 0.5s; }
.line-6 { animation-delay: 0.6s; }

@keyframes fadeIn {
  to {
    opacity: 1;
  }
}

.keyword {
  color: #93C5FD;
}

.class-name {
  color: #FCA5A5;
}

.method-name {
  color: #FCD34D;
}

.type {
  color: #86EFAC;
}

.string {
  color: #A7F3D0;
}

.comment {
  color: #64748B;
}

.ai-assistant {
  position: absolute;
  bottom: -20px;
  right: -20px;
  background: var(--gradient-ai);
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 10px 25px rgba(139, 92, 246, 0.3);
  display: flex;
  align-items: center;
  gap: 12px;
  animation: assistantBounce 3s ease-in-out infinite;
}

@keyframes assistantBounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-5px);
  }
}

.ai-icon {
  font-size: 24px;
}

.ai-message {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 12px 16px;
  min-width: 120px;
}

.ai-typing {
  display: flex;
  gap: 4px;
  align-items: center;
  height: 20px;
}

.typing-dot {
  width: 6px;
  height: 6px;
  background: white;
  border-radius: 50%;
  animation: typing 1.4s infinite;
}

.typing-dot:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-8px);
  }
}

.features {
  padding: 100px 0;
  background-color: var(--bg-secondary);
}

.section-header {
  text-align: center;
  margin-bottom: 60px;
}

.section-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 16px;
  color: var(--text-primary);
}

.section-subtitle {
  font-size: 1.125rem;
  color: var(--text-secondary);
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 30px;
}

.feature-card {
  background-color: var(--bg-card);
  border-radius: 16px;
  padding: 32px;
  box-shadow: var(--shadow-md);
  transition: all var(--transition-base);
  border: 1px solid var(--border-color);
  position: relative;
  overflow: hidden;
}

.feature-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: var(--gradient-primary);
  opacity: 0;
  transition: opacity var(--transition-base);
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-xl);
}

.feature-card:hover::before {
  opacity: 1;
}

.feature-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1), rgba(139, 92, 246, 0.1));
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  color: var(--primary-color);
  transition: all var(--transition-base);
}

.feature-card:hover .feature-icon {
  background: var(--gradient-primary);
  color: white;
  transform: scale(1.1);
}

.feature-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 12px;
  color: var(--text-primary);
}

.feature-description {
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 20px;
  font-size: 0.95rem;
}

.feature-link {
  text-decoration: none;
  color: var(--primary-color);
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: all var(--transition-base);
  font-size: 0.95rem;
}

.feature-link:hover {
  color: var(--primary-hover);
}

.arrow {
  transition: transform var(--transition-base);
}

.feature-link:hover .arrow {
  transform: translateX(4px);
}

.ai-section {
  padding: 100px 0;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.05) 0%, rgba(139, 92, 246, 0.05) 100%);
}

.ai-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 80px;
  align-items: center;
}

.ai-info {
  max-width: 500px;
}

.ai-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: var(--gradient-ai);
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 24px;
}

.ai-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 20px;
  color: var(--text-primary);
}

.ai-description {
  font-size: 1.125rem;
  color: var(--text-secondary);
  margin-bottom: 32px;
  line-height: 1.6;
}

.ai-features {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 32px;
}

.ai-feature {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--text-primary);
  font-size: 0.95rem;
}

.feature-check {
  width: 20px;
  height: 20px;
  background: var(--accent-color);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 12px;
  font-weight: 700;
}

.ai-visual {
  display: flex;
  justify-content: center;
}

.ai-chat {
  background: var(--bg-card);
  border-radius: 20px;
  padding: 24px;
  box-shadow: var(--shadow-xl);
  border: 1px solid var(--border-color);
  max-width: 400px;
  width: 100%;
}

.chat-message {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.chat-message:last-child {
  margin-bottom: 0;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.user .message-avatar {
  background: var(--bg-tertiary);
}

.ai .message-avatar {
  background: var(--gradient-ai);
}

.message-content {
  flex: 1;
}

.message-text {
  background: var(--bg-tertiary);
  padding: 12px 16px;
  border-radius: 12px;
  color: var(--text-primary);
  font-size: 0.95rem;
  line-height: 1.5;
}

.ai .message-text {
  background: rgba(139, 92, 246, 0.1);
  border: 1px solid rgba(139, 92, 246, 0.2);
}

.message-suggestions {
  margin-top: 12px;
  padding-left: 16px;
}

.suggestion-item {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin-bottom: 8px;
  line-height: 1.5;
}

.suggestion-item:last-child {
  margin-bottom: 0;
}

.paths-section {
  padding: 100px 0;
  background-color: var(--bg-primary);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
}

.view-all {
  text-decoration: none;
  color: var(--primary-color);
  font-weight: 500;
  transition: all var(--transition-base);
  font-size: 0.95rem;
}

.view-all:hover {
  color: var(--primary-hover);
}

.paths-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
}

.path-card {
  background-color: var(--bg-card);
  border-radius: 16px;
  padding: 24px;
  box-shadow: var(--shadow-md);
  transition: all var(--transition-base);
  border: 1px solid var(--border-color);
}

.path-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-xl);
}

.path-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.path-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.path-icon.java {
  background: linear-gradient(135deg, #007396, #00A9CE);
}

.path-icon.python {
  background: linear-gradient(135deg, #3776AB, #4584B6);
}

.path-icon.algorithm {
  background: linear-gradient(135deg, #F59E0B, #FBBF24);
}

.path-icon.backend {
  background: linear-gradient(135deg, #10B981, #34D399);
}

.path-info {
  flex: 1;
}

.path-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.path-level {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  background: rgba(59, 130, 246, 0.1);
  color: var(--primary-color);
}

.path-description {
  color: var(--text-secondary);
  line-height: 1.5;
  margin-bottom: 16px;
  font-size: 0.95rem;
}

.path-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  font-size: 0.875rem;
  color: var(--text-tertiary);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.path-link {
  display: block;
  text-align: center;
  padding: 12px;
  background: var(--gradient-primary);
  color: white;
  border-radius: 8px;
  text-decoration: none;
  font-weight: 500;
  transition: all var(--transition-base);
  box-shadow: 0 4px 6px rgba(59, 130, 246, 0.3);
}

.path-link:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(59, 130, 246, 0.4);
}

.problems-section {
  padding: 100px 0;
  background-color: var(--bg-secondary);
}

.problems-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
}

.problem-card {
  background-color: var(--bg-card);
  border-radius: 16px;
  padding: 24px;
  box-shadow: var(--shadow-md);
  transition: all var(--transition-base);
  border: 1px solid var(--border-color);
}

.problem-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-xl);
}

.problem-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.problem-id {
  font-weight: 600;
  color: var(--text-tertiary);
  font-size: 0.875rem;
}

.problem-difficulty {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.problem-difficulty.easy {
  background-color: rgba(16, 185, 129, 0.1);
  color: var(--accent-color);
}

.problem-difficulty.medium {
  background-color: rgba(245, 158, 11, 0.1);
  color: var(--warning-color);
}

.problem-difficulty.hard {
  background-color: rgba(239, 68, 68, 0.1);
  color: var(--danger-color);
}

.problem-title a {
  text-decoration: none;
  color: var(--text-primary);
  font-weight: 600;
  font-size: 1.125rem;
  margin-bottom: 12px;
  display: block;
  transition: color var(--transition-base);
}

.problem-title a:hover {
  color: var(--primary-color);
}

.problem-description {
  color: var(--text-secondary);
  font-size: 0.95rem;
  line-height: 1.5;
  margin-bottom: 16px;
}

.problem-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.tag {
  background-color: var(--bg-tertiary);
  color: var(--text-secondary);
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.cta-section {
  padding: 100px 0;
  background: var(--gradient-tech);
  text-align: center;
}

.cta-content {
  max-width: 700px;
  margin: 0 auto;
}

.cta-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: white;
  margin-bottom: 16px;
}

.cta-subtitle {
  font-size: 1.25rem;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 40px;
}

.cta-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.cta-section .btn-primary {
  background: white;
  color: var(--primary-color);
}

.cta-section .btn-outline {
  border-color: white;
  color: white;
}

.cta-section .btn-outline:hover {
  background: white;
  color: var(--primary-color);
}

.footer {
  background-color: #1E293B;
  color: white;
  padding: 60px 0 30px;
}

.footer-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 40px;
  margin-bottom: 40px;
}

.footer-section h3 {
  font-size: 1.125rem;
  font-weight: 600;
  margin-bottom: 20px;
  color: white;
}

.footer-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.footer-logo {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--primary-color);
}

.footer-logo-icon {
  font-size: 1.5rem;
}

.footer-description {
  color: #94A3B8;
  line-height: 1.6;
  font-size: 0.95rem;
}

.footer-links {
  list-style: none;
  padding: 0;
  margin: 0;
}

.footer-links li {
  margin-bottom: 12px;
}

.footer-links a {
  text-decoration: none;
  color: #94A3B8;
  transition: color var(--transition-base);
  font-size: 0.95rem;
}

.footer-links a:hover {
  color: white;
}

.footer-bottom {
  border-top: 1px solid #334155;
  padding-top: 30px;
  text-align: center;
}

.copyright {
  color: #94A3B8;
  font-size: 0.875rem;
}

@media (max-width: 1024px) {
  .hero .container,
  .ai-content {
    grid-template-columns: 1fr;
    gap: 40px;
  }
  
  .hero-content {
    max-width: 100%;
    text-align: center;
  }
  
  .hero-actions {
    justify-content: center;
  }
  
  .hero-stats {
    justify-content: center;
  }
  
  .hero-visual {
    order: -1;
  }
  
  .ai-info {
    max-width: 100%;
  }
  
  .ai-visual {
    order: -1;
  }
}

@media (max-width: 768px) {
  .navbar-nav {
    display: none;
  }
  
  .hero-title {
    font-size: 2.5rem;
  }
  
  .section-title {
    font-size: 2rem;
  }
  
  .ai-title {
    font-size: 2rem;
  }
  
  .cta-title {
    font-size: 2rem;
  }
  
  .hero-stats {
    flex-direction: column;
    gap: 16px;
  }
  
  .stat-divider {
    display: none;
  }
  
  .ai-features {
    grid-template-columns: 1fr;
  }
  
  .cta-actions {
    flex-direction: column;
  }
}

@media (max-width: 480px) {
  .hero-title {
    font-size: 2rem;
  }
  
  .btn-large {
    padding: 12px 24px;
    font-size: 14px;
  }
  
  .feature-card,
  .path-card,
  .problem-card {
    padding: 20px;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>