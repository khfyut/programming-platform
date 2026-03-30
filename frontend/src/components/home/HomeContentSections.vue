<template>
  <section class="features">
    <div class="container">
      <div class="section-header">
        <div>
          <h2 class="section-title">核心功能</h2>
          <p class="section-subtitle">从学习路径到提交反馈，把真正会用到的主流程放在一起。</p>
        </div>
      </div>

      <div class="features-grid">
        <article v-for="feature in features" :key="feature.title" class="feature-card">
          <div class="feature-icon" v-html="feature.icon"></div>
          <div class="feature-badge">{{ feature.badge }}</div>
          <h3 class="feature-title">{{ feature.title }}</h3>
          <p class="feature-description">{{ feature.description }}</p>
          <router-link :to="feature.to" class="feature-link">
            {{ feature.linkText }} <span class="arrow">-&gt;</span>
          </router-link>
        </article>
      </div>
    </div>
  </section>

  <section class="ai-section">
    <div class="container">
      <div class="ai-content">
        <div class="ai-info">
          <div class="ai-badge">
            <span class="ai-icon">AI</span>
            <span>智能助手</span>
          </div>
          <h2 class="ai-title">让 AI 参与到你的练题闭环里</h2>
          <p class="ai-description">
            不只是对话问答，还可以围绕代码、提交结果和错题记录，帮你更快定位问题和决定下一步。
          </p>

          <div class="ai-features">
            <div v-for="item in aiFeatures" :key="item" class="ai-feature">
              <div class="feature-check">OK</div>
              <span>{{ item }}</span>
            </div>
          </div>

          <router-link to="/ai" class="btn btn-primary btn-large">
            <span class="btn-text">体验 AI 助手</span>
            <span class="btn-icon">-&gt;</span>
          </router-link>
        </div>

        <div class="ai-visual">
          <div class="ai-chat">
            <div class="chat-message user">
              <div class="message-avatar">U</div>
              <div class="message-content">
                <div class="message-text">这次提交为什么超时了？</div>
              </div>
            </div>
            <div class="chat-message ai">
              <div class="message-avatar">AI</div>
              <div class="message-content">
                <div class="message-text">可以先从这几个方向排查：</div>
                <div class="message-suggestions">
                  <div class="suggestion-item">1. 看看循环是否有重复扫描</div>
                  <div class="suggestion-item">2. 判断是否缺少更合适的数据结构</div>
                  <div class="suggestion-item">3. 结合失败用例定位最慢路径</div>
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
        <div>
          <h2 class="section-title">热门学习路径</h2>
          <p class="section-subtitle">不想盲练时，可以先沿着路径把知识点和题目串起来。</p>
        </div>
        <router-link to="/learn" class="view-all">查看全部</router-link>
      </div>

      <div class="paths-grid">
        <article v-for="path in learningPaths" :key="path.title" class="path-card">
          <div class="path-header">
            <div class="path-icon" :class="path.type" v-html="path.icon"></div>
            <div class="path-info">
              <h3 class="path-title">{{ path.title }}</h3>
              <span class="path-level">{{ path.level }}</span>
            </div>
          </div>
          <p class="path-description">{{ path.description }}</p>
          <div class="path-meta">
            <span class="meta-item">{{ path.duration }}</span>
            <span class="meta-item">{{ path.units }}</span>
          </div>
          <router-link :to="path.to" class="path-link">进入路径</router-link>
        </article>
      </div>
    </div>
  </section>

  <section class="problems-section">
    <div class="container">
      <div class="section-header">
        <div>
          <h2 class="section-title">精选题目</h2>
          <p class="section-subtitle">先从这些覆盖面高、反馈明确的题目开始更容易进入状态。</p>
        </div>
        <router-link to="/problems" class="view-all">查看全部</router-link>
      </div>

      <div class="problems-grid">
        <article v-for="problem in featuredProblems" :key="problem.id" class="problem-card">
          <div class="problem-header">
            <span class="problem-id">#{{ problem.id }}</span>
            <span class="problem-difficulty" :class="problem.difficultyClass">{{ problem.difficulty }}</span>
          </div>
          <h3 class="problem-title">
            <router-link :to="problem.to">{{ problem.title }}</router-link>
          </h3>
          <p class="problem-description">{{ problem.description }}</p>
          <div class="problem-tags">
            <span v-for="tag in problem.tags" :key="tag" class="tag">{{ tag }}</span>
          </div>
        </article>
      </div>
    </div>
  </section>

  <section class="cta-section">
    <div class="container">
      <div class="cta-content">
        <h2 class="cta-title">准备好开始今天的学习了吗？</h2>
        <p class="cta-subtitle">如果你已经登录，直接进入学习中心；如果还没开始，就先从注册和题库体验进入。</p>
        <div class="cta-actions">
          <router-link :to="primaryCta.to" class="btn btn-primary btn-large">
            <span class="btn-text">{{ primaryCta.label }}</span>
            <span class="btn-icon">-&gt;</span>
          </router-link>
          <router-link :to="secondaryCta.to" class="btn btn-outline btn-large">
            <span class="btn-text">{{ secondaryCta.label }}</span>
            <span class="btn-icon">-&gt;</span>
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
            <span class="footer-logo-icon">&lt;/&gt;</span>
          </div>
          <p class="footer-description">专注编程学习，把路径、练题、反馈和复盘放进同一个学习工作台。</p>
        </div>

        <div class="footer-section">
          <h3 class="footer-title">学习入口</h3>
          <ul class="footer-links">
            <li><router-link to="/learn">学习中心</router-link></li>
            <li><router-link to="/problems">题库练习</router-link></li>
            <li><router-link to="/wrong-book">错题本</router-link></li>
            <li><router-link to="/submissions">提交记录</router-link></li>
          </ul>
        </div>

        <div class="footer-section">
          <h3 class="footer-title">辅助功能</h3>
          <ul class="footer-links">
            <li><router-link to="/ai">AI 助手</router-link></li>
            <li><router-link to="/community">学习社区</router-link></li>
            <li><router-link to="/profile">个人主页</router-link></li>
            <li><router-link to="/learn/knowledge-graph">知识图谱</router-link></li>
          </ul>
        </div>

        <div class="footer-section">
          <h3 class="footer-title">开始使用</h3>
          <ul class="footer-links">
            <li><router-link to="/register">注册账号</router-link></li>
            <li><router-link to="/login">登录系统</router-link></li>
            <li><router-link to="/learn/assessment">能力测评</router-link></li>
            <li><router-link to="/problem/1">在线练习示例</router-link></li>
          </ul>
        </div>
      </div>

      <div class="footer-bottom">
        <p class="copyright">© 2026 CodeMaster. All rights reserved.</p>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const isLoggedIn = computed(() => Boolean(userStore.token))

const primaryCta = computed(() => {
  if (isLoggedIn.value) {
    return {
      label: '进入学习中心',
      to: '/learn'
    }
  }

  return {
    label: '免费注册',
    to: '/register'
  }
})

const secondaryCta = computed(() => {
  if (isLoggedIn.value) {
    return {
      label: '继续练题',
      to: '/problems'
    }
  }

  return {
    label: '先浏览题库',
    to: '/problems'
  }
})

const features = [
  {
    title: '题库练习',
    badge: '高频入口',
    description: '覆盖不同难度和知识点的题目，支持在线作答、运行与提交反馈。',
    to: '/problems',
    linkText: '开始练题',
    icon: '<svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg>'
  },
  {
    title: '学习路径',
    badge: '推荐起点',
    description: '把知识点组织成循序渐进的任务，适合从“知道学什么”开始推进。',
    to: '/learn',
    linkText: '查看路径',
    icon: '<svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>'
  },
  {
    title: '学习社区',
    badge: '交流讨论',
    description: '与其他学习者交流思路、经验和问题，把独自练习变成持续互动。',
    to: '/community',
    linkText: '进入社区',
    icon: '<svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>'
  },
  {
    title: '在线做题',
    badge: '即时反馈',
    description: '从题目详情页直接编写、运行和提交代码，缩短从思路到验证的距离。',
    to: '/problem/1',
    linkText: '打开示例题',
    icon: '<svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="3" width="20" height="14" rx="2" ry="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/></svg>'
  }
]

const aiFeatures = ['智能代码提示', '结果复盘建议', '错题排查思路', '学习路径辅助']

const learningPaths = [
  {
    title: 'Java 编程基础',
    level: '基础',
    description: '从语法到面向对象，先把 Java 的核心表达和常见写法打牢。',
    duration: '适合入门阶段',
    units: '包含分层任务',
    to: '/learn/path/1',
    type: 'java',
    icon: '<svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="2" width="20" height="20" rx="2" ry="2"/><path d="M12 18h.01"/><path d="M7 9h10"/><path d="M7 13h10"/></svg>'
  },
  {
    title: 'Python 编程入门',
    level: '入门',
    description: '围绕语法、函数和常见题型，快速建立 Python 的练习手感。',
    duration: '适合快速起步',
    units: '覆盖基础能力',
    to: '/learn/path/2',
    type: 'python',
    icon: '<svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="2" width="20" height="20" rx="2" ry="2"/><path d="M12 18h.01"/><path d="M7 9h10"/><path d="M7 13h10"/></svg>'
  },
  {
    title: '算法与数据结构',
    level: '进阶',
    description: '围绕数组、链表、哈希和常见算法模型，强化解题能力。',
    duration: '适合系统复习',
    units: '连接路径与题库',
    to: '/learn/path/3',
    type: 'algorithm',
    icon: '<svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 20V10"/><path d="M12 20V4"/><path d="M6 20v-6"/></svg>'
  },
  {
    title: '后端开发实践',
    level: '高级',
    description: '把语言基础延伸到接口、服务与工程化，让练习更贴近真实开发。',
    duration: '适合项目阶段',
    units: '偏实战导向',
    to: '/learn/path/4',
    type: 'backend',
    icon: '<svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="2" width="20" height="8" rx="2" ry="2"/><rect x="2" y="14" width="20" height="8" rx="2" ry="2"/><line x1="6" y1="6" x2="6.01" y2="6"/><line x1="6" y1="18" x2="6.01" y2="18"/></svg>'
  }
]

const featuredProblems = [
  {
    id: 1,
    title: '两数之和',
    difficulty: '简单',
    difficultyClass: 'easy',
    description: '适合快速进入状态，也方便熟悉在线运行和提交流程。',
    tags: ['数组', '哈希表'],
    to: '/problem/1'
  },
  {
    id: 11,
    title: '冒泡排序',
    difficulty: '中等',
    difficultyClass: 'medium',
    description: '用经典排序题把循环、边界和复杂度意识一起带起来。',
    tags: ['排序', '基础算法'],
    to: '/problem/11'
  },
  {
    id: 13,
    title: '链表反转',
    difficulty: '中等',
    difficultyClass: 'medium',
    description: '适合练习指针重连和过程推演，也是常见面试高频题。',
    tags: ['链表', '数据结构'],
    to: '/problem/13'
  },
  {
    id: 14,
    title: '斐波那契数列',
    difficulty: '简单',
    difficultyClass: 'easy',
    description: '便于比较递归和动态规划思路，适合做复盘型练习。',
    tags: ['动态规划', '递归'],
    to: '/problem/14'
  }
]
</script>

<style scoped>
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.features,
.ai-section,
.paths-section,
.problems-section,
.cta-section {
  padding: 96px 0;
}

.section-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 36px;
}

.section-title {
  margin: 0;
  font-size: clamp(28px, 3vw, 40px);
  color: var(--text-primary);
}

.section-subtitle {
  margin: 12px 0 0;
  color: var(--text-secondary);
  line-height: 1.8;
}

.features-grid,
.paths-grid,
.problems-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 20px;
}

.feature-card,
.path-card,
.problem-card {
  padding: 24px;
  border: 1px solid var(--border-color);
  border-radius: 24px;
  background: var(--bg-card);
  box-shadow: var(--shadow-md);
  transition: transform var(--transition-base), border-color var(--transition-base);
}

.feature-card:hover,
.path-card:hover,
.problem-card:hover {
  transform: translateY(-4px);
  border-color: rgba(59, 130, 246, 0.28);
}

.feature-icon {
  display: grid;
  place-items: center;
  width: 72px;
  height: 72px;
  margin-bottom: 16px;
  border-radius: 20px;
  color: var(--primary-color);
  background: rgba(59, 130, 246, 0.1);
}

.feature-badge {
  display: inline-flex;
  margin-bottom: 12px;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(16, 185, 129, 0.12);
  color: #059669;
  font-size: 12px;
  font-weight: 700;
}

.feature-title,
.path-title,
.problem-title {
  margin: 0 0 12px;
  color: var(--text-primary);
}

.feature-description,
.path-description,
.problem-description,
.ai-description,
.footer-description {
  color: var(--text-secondary);
  line-height: 1.8;
}

.feature-link,
.view-all,
.path-link,
.problem-title a,
.footer-links a,
.btn {
  text-decoration: none;
}

.feature-link,
.view-all,
.path-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-top: 18px;
  color: var(--primary-color);
  font-weight: 700;
}

.ai-content {
  display: grid;
  grid-template-columns: 1fr 0.9fr;
  gap: 36px;
  align-items: center;
}

.ai-badge {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(139, 92, 246, 0.12);
  color: #7c3aed;
  font-weight: 700;
}

.ai-icon,
.message-avatar,
.feature-check {
  display: grid;
  place-items: center;
  border-radius: 12px;
  font-weight: 800;
}

.ai-icon {
  width: 36px;
  height: 36px;
  background: var(--gradient-ai);
  color: white;
}

.ai-title,
.cta-title {
  margin: 0 0 18px;
  font-size: clamp(30px, 3vw, 44px);
  color: var(--text-primary);
}

.ai-features {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin: 28px 0 32px;
}

.ai-feature {
  display: flex;
  align-items: center;
  gap: 12px;
}

.feature-check {
  width: 28px;
  height: 28px;
  background: rgba(16, 185, 129, 0.16);
  color: #059669;
  font-size: 11px;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 8px;
  border: none;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-base);
}

.btn-primary {
  background: var(--gradient-primary);
  color: white;
  box-shadow: 0 4px 6px rgba(59, 130, 246, 0.3);
}

.btn-outline {
  background-color: transparent;
  border: 1px solid var(--primary-color);
  color: var(--primary-color);
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

.ai-chat {
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 28px;
  border: 1px solid rgba(139, 92, 246, 0.18);
  border-radius: 28px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.05), transparent 40%),
    var(--bg-card);
  box-shadow: var(--shadow-lg);
}

.chat-message {
  display: flex;
  gap: 14px;
  align-items: flex-start;
}

.chat-message.user {
  justify-content: flex-end;
}

.chat-message.user .message-content {
  background: rgba(59, 130, 246, 0.12);
}

.chat-message.ai .message-content {
  background: rgba(139, 92, 246, 0.1);
}

.message-avatar {
  width: 40px;
  height: 40px;
  background: var(--gradient-primary);
  color: white;
}

.message-content {
  max-width: 80%;
  padding: 14px 16px;
  border-radius: 18px;
}

.message-text {
  color: var(--text-primary);
  line-height: 1.7;
}

.message-suggestions {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.suggestion-item {
  color: var(--text-secondary);
  font-size: 14px;
}

.path-header,
.problem-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.path-header {
  justify-content: flex-start;
}

.path-icon {
  display: grid;
  place-items: center;
  width: 54px;
  height: 54px;
  border-radius: 16px;
  color: white;
}

.path-icon.java { background: linear-gradient(135deg, #f97316, #fb7185); }
.path-icon.python { background: linear-gradient(135deg, #0ea5e9, #2563eb); }
.path-icon.algorithm { background: linear-gradient(135deg, #7c3aed, #8b5cf6); }
.path-icon.backend { background: linear-gradient(135deg, #10b981, #14b8a6); }

.path-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.path-level {
  color: var(--text-tertiary);
  font-size: 13px;
}

.path-meta,
.problem-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 18px;
}

.meta-item,
.tag {
  padding: 6px 10px;
  border-radius: 999px;
  background: var(--bg-tertiary);
  color: var(--text-secondary);
  font-size: 12px;
}

.problem-id {
  color: var(--text-tertiary);
  font-size: 13px;
}

.problem-difficulty {
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.problem-difficulty.easy {
  background: rgba(16, 185, 129, 0.14);
  color: #059669;
}

.problem-difficulty.medium {
  background: rgba(245, 158, 11, 0.14);
  color: #d97706;
}

.cta-content {
  padding: 40px;
  border: 1px solid rgba(59, 130, 246, 0.16);
  border-radius: 30px;
  background:
    radial-gradient(circle at top right, rgba(59, 130, 246, 0.18), transparent 30%),
    radial-gradient(circle at bottom left, rgba(139, 92, 246, 0.16), transparent 25%),
    var(--bg-card);
  text-align: center;
}

.cta-subtitle {
  margin: 0 auto 28px;
  max-width: 640px;
  color: var(--text-secondary);
  line-height: 1.8;
}

.cta-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  flex-wrap: wrap;
}

.footer {
  padding: 48px 0 28px;
  border-top: 1px solid var(--border-color);
}

.footer-content {
  display: grid;
  grid-template-columns: 1.3fr repeat(3, 1fr);
  gap: 24px;
}

.footer-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
  font-weight: 800;
  color: var(--text-primary);
}

.footer-logo-icon {
  padding: 4px 8px;
  border-radius: 999px;
  background: rgba(59, 130, 246, 0.12);
  color: var(--primary-color);
  font-size: 12px;
}

.footer-title {
  margin: 0 0 12px;
  color: var(--text-primary);
}

.footer-links {
  margin: 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.footer-links a {
  color: var(--text-secondary);
}

.footer-bottom {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--border-color);
  color: var(--text-tertiary);
  text-align: center;
}

@media (max-width: 1080px) {
  .features-grid,
  .paths-grid,
  .problems-grid,
  .footer-content,
  .ai-content {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .footer-content {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .section-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .features-grid,
  .paths-grid,
  .problems-grid,
  .ai-content,
  .footer-content,
  .ai-features {
    grid-template-columns: 1fr;
  }

  .features,
  .ai-section,
  .paths-section,
  .problems-section,
  .cta-section {
    padding: 72px 0;
  }

  .cta-content {
    padding: 28px 22px;
  }
}
</style>
