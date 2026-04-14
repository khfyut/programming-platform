<template>
  <el-card class="quality-card" shadow="never">
    <template #header>
      <div class="card-header">
        <div>
          <span class="card-title">内容质量与 Agent 闭环</span>
          <p class="card-subtitle">Java-first 范围内检查题目质量、路径绑定和知识点一致性。</p>
        </div>
        <el-button type="primary" :loading="loading" @click="$emit('refresh')">刷新</el-button>
      </div>
    </template>

    <el-row :gutter="16" class="summary-row">
      <el-col :xs="24" :sm="12" :lg="6">
        <el-statistic title="Agent-ready 题目" :value="summary.agentReadyProblems || 0" />
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-statistic title="Java Scope 题目" :value="summary.javaAgentScopeProblems || 0" />
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-statistic title="路径绑定问题" :value="summary.pathBindingIssues || 0" />
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-statistic title="知识点问题" :value="summary.tagIssues || 0" />
      </el-col>
    </el-row>

    <el-tabs v-model="activeInnerTab" class="quality-tabs">
      <el-tab-pane label="题目质量" name="problems">
        <el-table :data="problemRows" v-loading="loading" stripe max-height="520" empty-text="暂无质量数据">
          <el-table-column prop="problemId" label="ID" width="80" />
          <el-table-column prop="title" label="题目" min-width="220" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="row.status === 'PUBLISHED' ? 'success' : 'info'">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="testCaseCount" label="用例" width="80" />
          <el-table-column prop="hiddenCaseCount" label="隐藏" width="80" />
          <el-table-column label="Java" width="120">
            <template #default="{ row }">
              <el-tag :type="row.javaActive && row.javaReferenceSolution ? 'success' : 'warning'">
                {{ row.javaActive && row.javaReferenceSolution ? 'ready' : 'missing' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="Agent-ready" width="130">
            <template #default="{ row }">
              <el-tag :type="row.agentReady ? 'success' : 'danger'">{{ row.agentReady ? 'true' : 'false' }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="路径绑定" name="bindings">
        <el-table :data="pathBindingRows" v-loading="loading" stripe max-height="520" empty-text="暂无绑定数据">
          <el-table-column prop="pathName" label="路径" min-width="180" show-overflow-tooltip />
          <el-table-column prop="levelName" label="关卡" min-width="180" show-overflow-tooltip />
          <el-table-column prop="problemTitle" label="绑定题目" min-width="220" show-overflow-tooltip />
          <el-table-column prop="levelKnowledgePoints" label="关卡知识点" min-width="180" show-overflow-tooltip />
          <el-table-column prop="problemKnowledgePoints" label="题目知识点" min-width="180" show-overflow-tooltip />
          <el-table-column label="语义交集" width="120">
            <template #default="{ row }">
              <el-tag :type="row.semanticMatched ? 'success' : 'danger'">
                {{ row.semanticMatched ? '匹配' : '不匹配' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="匹配点" min-width="160">
            <template #default="{ row }">
              {{ (row.matchedKnowledgePoints || []).join(', ') || '-' }}
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="知识点" name="tags">
        <el-table :data="tagRows" v-loading="loading" stripe max-height="520" empty-text="暂无知识点数据">
          <el-table-column prop="problemId" label="ID" width="80" />
          <el-table-column prop="title" label="题目" min-width="220" show-overflow-tooltip />
          <el-table-column prop="knowledgePoints" label="知识点" min-width="240" show-overflow-tooltip />
          <el-table-column label="是否标准化" width="140">
            <template #default="{ row }">
              <el-tag :type="row.knownKnowledgePoints ? 'success' : 'warning'">
                {{ row.knownKnowledgePoints ? '是' : '需补充' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="未知知识点" min-width="200">
            <template #default="{ row }">
              {{ (row.unknownKnowledgePoints || []).join(', ') || '-' }}
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script setup>
import { ref } from 'vue'

defineProps({
  summary: {
    type: Object,
    default: () => ({})
  },
  problemRows: {
    type: Array,
    default: () => []
  },
  pathBindingRows: {
    type: Array,
    default: () => []
  },
  tagRows: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

defineEmits(['refresh'])

const activeInnerTab = ref('problems')
</script>

<style scoped>
.quality-card {
  border-radius: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary, #1f2937);
}

.card-subtitle {
  margin: 6px 0 0;
  color: var(--text-secondary, #6b7280);
  font-size: 13px;
}

.summary-row {
  margin-bottom: 24px;
}

.quality-tabs {
  margin-top: 8px;
}
</style>
