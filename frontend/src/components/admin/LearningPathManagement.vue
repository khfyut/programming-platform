<template>
  <el-card class="management-card" shadow="never">
    <template #header>
      <div class="card-header">
        <span class="card-title">学习路径管理</span>
        <el-button type="primary" class="add-btn" @click="openAddDialog">
          <el-icon><Plus /></el-icon>
          添加路径
        </el-button>
      </div>
    </template>

    <el-table
      :data="paths"
      stripe
      row-key="id"
      v-loading="loading"
      max-height="500"
      class="admin-table"
      table-layout="fixed"
      empty-text="暂无学习路径数据"
      :default-sort="{ prop: 'id', order: 'ascending' }"
    >
      <el-table-column prop="id" label="ID" width="80" sortable />
      <el-table-column prop="name" label="路径名称" min-width="180" sortable show-overflow-tooltip />
      <el-table-column prop="direction" label="方向" width="130" sortable>
        <template #default="{ row }">
          <el-tag size="small" effect="plain">{{ getDirectionText(row.direction) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="language" label="语言" width="110" sortable>
        <template #default="{ row }">
          <span class="language-badge">{{ formatLanguage(row.language) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="220" show-overflow-tooltip />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link class="action-btn" @click="openEditDialog(row)">编辑</el-button>
          <el-button type="danger" link class="action-btn" @click="$emit('delete-path', row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="pathDialogVisible" :title="pathDialogTitle" width="520px">
    <el-form :model="pathForm" label-width="90px">
      <el-form-item label="路径名称">
        <el-input v-model="pathForm.name" maxlength="60" show-word-limit placeholder="请输入学习路径名称" />
      </el-form-item>
      <el-form-item label="方向">
        <el-select v-model="pathForm.direction" placeholder="请选择方向">
          <el-option label="算法" value="algorithm" />
          <el-option label="数据结构" value="data-structure" />
          <el-option label="前端开发" value="frontend" />
          <el-option label="后端开发" value="backend" />
          <el-option label="数据库" value="database" />
          <el-option label="系统设计" value="system" />
        </el-select>
      </el-form-item>
      <el-form-item label="语言">
        <el-select v-model="pathForm.language" placeholder="请选择语言">
          <el-option label="Java" value="java" />
          <el-option label="Python" value="python" />
          <el-option label="C++" value="cpp" />
          <el-option label="通用" value="all" />
        </el-select>
      </el-form-item>
      <el-form-item label="描述">
        <el-input
          v-model="pathForm.description"
          type="textarea"
          :rows="4"
          maxlength="200"
          show-word-limit
          placeholder="请输入学习路径描述"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="closeDialog">取消</el-button>
      <el-button type="primary" @click="submitPath">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

defineProps({
  paths: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['save-path', 'delete-path'])

const pathDialogVisible = ref(false)
const pathDialogTitle = ref('添加学习路径')
const pathForm = reactive({
  id: null,
  name: '',
  direction: '',
  language: 'java',
  description: ''
})

const getDirectionText = (direction) => {
  const texts = {
    algorithm: '算法',
    'data-structure': '数据结构',
    frontend: '前端开发',
    backend: '后端开发',
    database: '数据库',
    system: '系统设计'
  }
  return texts[direction] || direction || '--'
}

const formatLanguage = (language) => {
  if (!language) return '--'
  if (language === 'all') return '通用'
  return String(language).toUpperCase()
}

const resetPathForm = () => {
  pathForm.id = null
  pathForm.name = ''
  pathForm.direction = ''
  pathForm.language = 'java'
  pathForm.description = ''
}

const openAddDialog = () => {
  pathDialogTitle.value = '添加学习路径'
  resetPathForm()
  pathDialogVisible.value = true
}

const openEditDialog = (path) => {
  pathDialogTitle.value = '编辑学习路径'
  pathForm.id = path.id
  pathForm.name = path.name || ''
  pathForm.direction = path.direction || ''
  pathForm.language = path.language || 'java'
  pathForm.description = path.description || ''
  pathDialogVisible.value = true
}

const closeDialog = () => {
  pathDialogVisible.value = false
}

const submitPath = () => {
  if (!pathForm.name.trim()) {
    ElMessage.warning('请输入学习路径名称')
    return
  }

  if (!pathForm.direction) {
    ElMessage.warning('请选择方向')
    return
  }

  emit('save-path', {
    form: {
      id: pathForm.id,
      name: pathForm.name.trim(),
      direction: pathForm.direction,
      language: pathForm.language,
      description: pathForm.description?.trim() || ''
    },
    done: closeDialog
  })
}
</script>

<style scoped>
.action-btn {
  font-weight: 500;
  transition: all 0.2s ease;
}

.action-btn:hover {
  transform: translateX(2px);
}

.add-btn {
  background: var(--brand-primary);
  border-color: var(--brand-primary);
  border-radius: 6px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.add-btn:hover {
  background: var(--brand-primary-hover);
  border-color: var(--brand-primary-hover);
  transform: translateY(-1px);
}
</style>
