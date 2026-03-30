<template>
  <section class="dashboard-grid">
    <div class="panel-card action-panel">
      <div class="panel-header">
        <div>
          <div class="section-kicker">Quick Actions</div>
          <h3>下一步做什么</h3>
        </div>
      </div>
      <div class="action-list">
        <button class="action-item" @click="emit('continue-learning')">
          <div>
            <strong>继续学习</strong>
            <span>{{ currentPath?.name || '进入学习中心继续当前计划' }}</span>
          </div>
          <el-icon><ArrowRight /></el-icon>
        </button>
        <button class="action-item" @click="emit('open-wrong-book')">
          <div>
            <strong>查看错题</strong>
            <span>回看高频错误，优先补足薄弱点</span>
          </div>
          <el-icon><ArrowRight /></el-icon>
        </button>
        <button class="action-item" @click="emit('open-submissions')">
          <div>
            <strong>复盘提交</strong>
            <span>快速查看最近结果和运行表现</span>
          </div>
          <el-icon><ArrowRight /></el-icon>
        </button>
      </div>
    </div>

    <div class="panel-card submissions-panel">
      <div class="panel-header">
        <div>
          <div class="section-kicker">Recent Submissions</div>
          <h3>最近提交</h3>
        </div>
        <button class="panel-link-button" @click="emit('open-submissions')">查看全部</button>
      </div>

      <div v-if="recentSubmissions.length > 0" class="submission-list">
        <button
          v-for="item in recentSubmissions"
          :key="item.id"
          class="submission-item"
          type="button"
          @click="emit('open-submissions', item.id)"
        >
          <div class="submission-main">
            <div class="submission-top">
              <span class="submission-title">{{ item.problemTitle || `题目 ${item.problemId || item.id}` }}</span>
              <el-tag size="small" :type="getResultType(item.result)">
                {{ getResultText(item.result) }}
              </el-tag>
            </div>
            <div class="submission-meta">
              <span>{{ item.language || 'N/A' }}</span>
              <span>{{ item.timeCost || 0 }} ms</span>
              <span>{{ formatRelativeTime(item.submitTime || item.createTime) }}</span>
            </div>
          </div>
          <div class="submission-actions">
            <button
              v-if="item.problemId"
              type="button"
              class="submission-link-button"
              @click.stop="emit('open-problem', item.problemId)"
            >
              查看题目
            </button>
            <el-icon><ArrowRight /></el-icon>
          </div>
        </button>
      </div>
      <div v-else class="empty-box">
        <div>暂无提交记录</div>
        <button class="panel-link-button empty-action" @click="emit('continue-learning')">去练几道题</button>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ArrowRight } from '@element-plus/icons-vue'

defineProps({
  currentPath: {
    type: Object,
    default: null
  },
  formatRelativeTime: {
    type: Function,
    required: true
  },
  getResultText: {
    type: Function,
    required: true
  },
  getResultType: {
    type: Function,
    required: true
  },
  recentSubmissions: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits([
  'continue-learning',
  'open-problem',
  'open-submissions',
  'open-wrong-book'
])
</script>

<style scoped>
.dashboard-grid {
  display: grid;
  grid-template-columns: minmax(280px, 0.9fr) minmax(0, 1.1fr);
  gap: 20px;
}

.panel-card {
  position: relative;
  overflow: hidden;
  padding: 22px;
  border: 1px solid var(--border-color);
  border-radius: 22px;
  background: var(--bg-card);
  box-shadow: var(--shadow-md);
}

.section-kicker {
  margin-bottom: 8px;
  color: var(--brand-accent);
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.panel-header,
.submission-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.panel-card h3 {
  margin: 0;
}

.action-list,
.submission-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 16px;
}

.action-item,
.submission-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  width: 100%;
  padding: 16px;
  border: 1px solid var(--border-light);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.03);
  color: var(--text-primary);
  text-align: left;
  cursor: pointer;
  transition:
    transform var(--transition-fast),
    border-color var(--transition-fast),
    background var(--transition-fast);
}

.action-item:first-child {
  border-color: rgba(0, 209, 255, 0.3);
  background: linear-gradient(135deg, rgba(0, 209, 255, 0.14), rgba(0, 102, 255, 0.08));
}

.action-item:hover,
.submission-item:hover {
  transform: translateY(-1px);
  border-color: var(--border-strong);
  background: rgba(255, 255, 255, 0.05);
}

.action-item strong,
.submission-title {
  display: block;
  margin-bottom: 4px;
}

.action-item span,
.submission-meta span {
  font-size: 13px;
}

.submission-meta {
  color: var(--text-secondary);
  line-height: 1.7;
}

.submission-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--text-secondary);
}

.submission-link-button {
  border: none;
  background: transparent;
  color: var(--brand-primary);
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}

.submission-link-button:hover {
  color: var(--brand-primary-hover);
}

.panel-link-button {
  padding: 0;
  border: none;
  background: transparent;
  color: var(--brand-primary);
  font-weight: 700;
  cursor: pointer;
}

.panel-link-button:hover {
  color: var(--brand-primary-hover);
}

.empty-box {
  margin-top: 16px;
  padding: 18px;
  border: 1px dashed var(--border-color);
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  text-align: center;
  color: var(--text-secondary);
}

.empty-action {
  font-size: 13px;
}

@media (max-width: 1200px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}
</style>
