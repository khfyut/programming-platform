<template>
  <div class="admin-overview">
    <div class="admin-banner">
      <el-icon class="banner-icon"><Warning /></el-icon>
      <div class="banner-content">
        <h3>管理员模式</h3>
        <p>您正在访问管理后台，请谨慎操作。所有关键操作都会被记录。</p>
      </div>
    </div>

    <div class="dashboard-header">
      <div class="dashboard-title">
        <h2>管理后台</h2>
        <span class="dashboard-subtitle">系统数据总览与管理入口</span>
      </div>
      <el-button type="primary" link class="refresh-btn" @click="$emit('refresh')">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <el-row :gutter="20" class="dashboard-row">
      <el-col :span="6">
        <div class="stat-card" v-loading="loading">
          <div class="stat-card-header">
            <span class="stat-card-title">用户总数</span>
            <el-icon class="stat-card-icon" color="#007AFF"><User /></el-icon>
          </div>
          <div class="stat-card-body">
            <div class="stat-value">{{ statistics.userCount || 0 }}</div>
            <div class="stat-trend">
              <span class="trend-label">注册用户</span>
            </div>
          </div>
          <div class="stat-card-footer">
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: '100%' }"></div>
            </div>
          </div>
        </div>
      </el-col>

      <el-col :span="6">
        <div class="stat-card" v-loading="loading">
          <div class="stat-card-header">
            <span class="stat-card-title">题目总数</span>
            <el-icon class="stat-card-icon" color="#00B42A"><Document /></el-icon>
          </div>
          <div class="stat-card-body">
            <div class="stat-value">{{ statistics.problemCount || 0 }}</div>
            <div class="stat-trend">
              <span class="trend-label">在线题目</span>
            </div>
          </div>
          <div class="stat-card-footer">
            <div class="progress-bar">
              <div class="progress-fill success" :style="{ width: '100%' }"></div>
            </div>
          </div>
        </div>
      </el-col>

      <el-col :span="6">
        <div class="stat-card" v-loading="loading">
          <div class="stat-card-header">
            <span class="stat-card-title">提交总数</span>
            <el-icon class="stat-card-icon" color="#FFB400"><Files /></el-icon>
          </div>
          <div class="stat-card-body">
            <div class="stat-value">{{ statistics.submitCount || 0 }}</div>
            <div class="stat-trend">
              <span class="trend-label">累计提交</span>
            </div>
          </div>
          <div class="stat-card-footer">
            <div class="progress-bar">
              <div class="progress-fill warning" :style="{ width: '100%' }"></div>
            </div>
          </div>
        </div>
      </el-col>

      <el-col :span="6">
        <div class="stat-card" v-loading="loading">
          <div class="stat-card-header">
            <span class="stat-card-title">整体正确率</span>
            <el-icon class="stat-card-icon" color="#EE4D38"><CircleCheck /></el-icon>
          </div>
          <div class="stat-card-body">
            <div class="stat-value">{{ statistics.correctRate || '0%' }}</div>
            <div class="stat-trend">
              <span class="trend-label">系统表现</span>
            </div>
          </div>
          <div class="stat-card-footer">
            <div class="progress-bar">
              <div
                class="progress-fill error"
                :style="{ width: (statistics.correctRate ? parseFloat(statistics.correctRate) : 0) + '%' }"
              ></div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { CircleCheck, Document, Files, Refresh, User, Warning } from '@element-plus/icons-vue'

defineProps({
  loading: {
    type: Boolean,
    default: false
  },
  statistics: {
    type: Object,
    default: () => ({})
  }
})

defineEmits(['refresh'])
</script>

<style scoped>
.admin-banner {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 18px 20px;
  margin-bottom: 24px;
  border-radius: 16px;
  background: linear-gradient(135deg, #fff7ed 0%, #fffbeb 100%);
  border: 1px solid #fed7aa;
}

.banner-icon {
  font-size: 24px;
  color: #f97316;
  margin-top: 2px;
}

.banner-content h3 {
  margin: 0 0 6px;
  font-size: 18px;
  color: #9a3412;
}

.banner-content p {
  margin: 0;
  font-size: 14px;
  line-height: 1.7;
  color: #7c2d12;
}

.dashboard-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.dashboard-title h2 {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #111827;
}

.dashboard-subtitle {
  display: inline-block;
  margin-top: 6px;
  font-size: 14px;
  color: #6b7280;
}

.refresh-btn {
  font-weight: 600;
}

.dashboard-row {
  margin-bottom: 24px;
}

.stat-card {
  padding: 20px;
  border-radius: 18px;
  background: #fff;
  border: 1px solid #e5e7eb;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.04);
}

.stat-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.stat-card-title {
  font-size: 14px;
  color: #6b7280;
}

.stat-card-icon {
  font-size: 22px;
}

.stat-card-body {
  margin-bottom: 18px;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  line-height: 1;
  color: #111827;
}

.stat-trend {
  margin-top: 10px;
}

.trend-label {
  font-size: 13px;
  color: #94a3b8;
}

.progress-bar {
  width: 100%;
  height: 8px;
  border-radius: 999px;
  background: #f1f5f9;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #007aff;
  border-radius: inherit;
}

.progress-fill.success {
  background: #00b42a;
}

.progress-fill.warning {
  background: #ffb400;
}

.progress-fill.error {
  background: #ee4d38;
}
</style>
