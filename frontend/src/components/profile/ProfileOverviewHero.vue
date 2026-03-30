<template>
  <section class="hero-grid">
    <div class="hero-card profile-card">
      <div class="profile-main">
        <div class="avatar-shell">
          <el-avatar :size="88" :src="profile.avatarUrl">
            <el-icon><User /></el-icon>
          </el-avatar>
          <span class="avatar-status"></span>
        </div>

        <div class="profile-copy">
          <div class="profile-row">
            <h1>{{ profile.username || '用户' }}</h1>
            <el-tag :type="profile.isAdmin ? 'danger' : 'primary'" size="small">
              {{ profile.isAdmin ? '管理员' : '学习者' }}
            </el-tag>
          </div>

          <p class="profile-bio">
            {{ profile.bio || '继续积累题目、路径和提交记录，让这里成为你的学习控制台。' }}
          </p>

          <div class="profile-meta">
            <span class="meta-pill">{{ profile.isAdmin ? '管理权限已开启' : '学习模式进行中' }}</span>
            <span class="meta-pill muted">{{ currentPath ? '当前有正在推进的学习路径' : '可以从学习中心开始新的路径' }}</span>
          </div>

          <div v-if="profile.githubUrl || profile.blogUrl" class="profile-links">
            <a v-if="profile.githubUrl" :href="profile.githubUrl" target="_blank" rel="noreferrer">GitHub</a>
            <a v-if="profile.blogUrl" :href="profile.blogUrl" target="_blank" rel="noreferrer">博客</a>
          </div>
        </div>
      </div>

      <div class="hero-actions">
        <el-button type="primary" @click="emit('continue-learning')">继续学习</el-button>
        <el-button plain @click="emit('open-learn-center')">学习中心</el-button>
        <el-button plain @click="emit('open-settings')">资料设置</el-button>
        <el-button plain @click="emit('open-wrong-book')">错题本</el-button>
      </div>
    </div>

    <div class="hero-card current-path-card">
      <div class="section-kicker">Current Path</div>
      <template v-if="currentPath">
        <h2>{{ currentPath.name }}</h2>
        <p class="path-desc">
          {{ currentPath.description || '继续沿着当前路径完成今天的学习任务。' }}
        </p>

        <div class="path-meta">
          <span>{{ getLanguageLabel(currentPath.language) }}</span>
          <span>{{ getDirectionLabel(currentPath.direction) }}</span>
        </div>

        <div class="path-progress-line">
          <div class="path-progress-head">
            <span>当前进度</span>
            <span>{{ currentPathProgress }}%</span>
          </div>
          <el-progress :percentage="currentPathProgress" :show-text="false" :stroke-width="10" />
          <p class="path-note">{{ currentPathProgress >= 100 ? '这条路径已经完成，可以开始下一条。' : '下次回来会从这里继续，不会丢失节奏。' }}</p>
        </div>
      </template>

      <template v-else>
        <h2>还没有正在进行的学习路径</h2>
        <p class="path-desc">
          先从学习中心选择一条路径，之后这里会显示你的当前进度和下一步入口。
        </p>
      </template>
    </div>
  </section>
</template>

<script setup>
import { User } from '@element-plus/icons-vue'

defineProps({
  profile: {
    type: Object,
    default: null
  },
  currentPath: {
    type: Object,
    default: null
  },
  currentPathProgress: {
    type: Number,
    default: 0
  },
  getLanguageLabel: {
    type: Function,
    required: true
  },
  getDirectionLabel: {
    type: Function,
    required: true
  }
})

const emit = defineEmits([
  'open-settings',
  'open-wrong-book',
  'continue-learning',
  'open-learn-center'
])
</script>

<style scoped>
.hero-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(320px, 0.95fr);
  gap: 20px;
}

.hero-card {
  position: relative;
  overflow: hidden;
  padding: 22px;
  border: 1px solid var(--border-color);
  border-radius: 22px;
  background: var(--bg-card);
  box-shadow: var(--shadow-md);
}

.profile-card {
  display: flex;
  flex-direction: column;
  gap: 18px;
  background:
    radial-gradient(circle at top left, rgba(0, 209, 255, 0.08), transparent 32%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.03), transparent 28%),
    var(--bg-card);
}

.profile-main {
  display: flex;
  gap: 18px;
  align-items: flex-start;
}

.avatar-shell {
  position: relative;
  flex-shrink: 0;
}

.avatar-shell :deep(.el-avatar) {
  border: 3px solid rgba(0, 209, 255, 0.18);
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.14);
}

.avatar-status {
  position: absolute;
  right: 2px;
  bottom: 4px;
  width: 14px;
  height: 14px;
  border: 3px solid var(--bg-card);
  border-radius: 50%;
  background: #22c55e;
}

.profile-copy {
  min-width: 0;
}

.profile-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.profile-row h1,
.current-path-card h2 {
  margin: 0;
}

.profile-row h1 {
  font-size: 28px;
  line-height: 1.1;
}

.profile-bio,
.path-desc {
  color: var(--text-secondary);
  line-height: 1.7;
}

.profile-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 14px;
}

.meta-pill {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  border: 1px solid rgba(0, 209, 255, 0.2);
  border-radius: 999px;
  background: rgba(0, 209, 255, 0.08);
  color: var(--text-primary);
  font-size: 12px;
}

.meta-pill.muted {
  border-color: var(--border-light);
  background: rgba(255, 255, 255, 0.03);
  color: var(--text-secondary);
}

.profile-links {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 12px;
}

.profile-links a {
  color: var(--brand-primary);
  font-weight: 600;
  text-decoration: none;
}

.profile-links a:hover {
  color: var(--brand-primary-hover);
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.current-path-card {
  border-color: rgba(0, 209, 255, 0.28);
  background:
    radial-gradient(circle at top right, rgba(0, 209, 255, 0.14), transparent 32%),
    linear-gradient(180deg, rgba(9, 25, 46, 0.96), rgba(6, 17, 32, 0.92));
  box-shadow: var(--shadow-neon), var(--shadow-md);
}

.section-kicker {
  margin-bottom: 8px;
  color: var(--brand-accent);
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.path-meta {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin: 14px 0;
}

.path-meta span {
  padding: 5px 10px;
  border: 1px solid var(--border-light);
  border-radius: 999px;
  color: var(--text-secondary);
  font-size: 12px;
  background: rgba(255, 255, 255, 0.03);
}

.path-progress-line {
  margin-top: 18px;
}

.path-progress-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.path-note {
  margin: 10px 0 0;
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.6;
}

@media (max-width: 1200px) {
  .hero-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .profile-main {
    flex-direction: column;
  }

  .hero-actions :deep(.el-button) {
    flex: 1 1 100%;
  }
}
</style>
