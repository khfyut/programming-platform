<template>
  <el-card class="management-card" shadow="never">
    <template #header>
      <div class="card-header">
        <span class="card-title">知识点管理</span>
        <el-button type="primary" class="add-btn" @click="openAddDialog">
          <el-icon><Plus /></el-icon>
          添加知识点
        </el-button>
      </div>
    </template>

    <el-table
      :data="knowledgeList"
      stripe
      row-key="id"
      v-loading="loading"
      max-height="500"
      class="admin-table"
      table-layout="fixed"
      empty-text="暂无知识点数据"
      :default-sort="{ prop: 'id', order: 'ascending' }"
    >
      <el-table-column prop="id" label="ID" width="80" sortable />
      <el-table-column prop="name" label="知识点名称" min-width="180" sortable show-overflow-tooltip />
      <el-table-column prop="parentId" label="父级ID" width="100" sortable>
        <template #default="{ row }">
          {{ row.parentId ?? '--' }}
        </template>
      </el-table-column>
      <el-table-column prop="level" label="层级" width="90" sortable>
        <template #default="{ row }">
          <el-tag size="small" effect="plain">L{{ row.level || 1 }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="220" show-overflow-tooltip />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link class="action-btn" @click="openEditDialog(row)">编辑</el-button>
          <el-button type="danger" link class="action-btn" @click="$emit('delete-knowledge', row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="knowledgeDialogVisible" :title="knowledgeDialogTitle" width="520px">
    <el-form :model="knowledgeForm" label-width="90px">
      <el-form-item label="知识点名称">
        <el-input v-model="knowledgeForm.name" maxlength="50" show-word-limit placeholder="请输入知识点名称" />
      </el-form-item>
      <el-form-item label="父级ID">
        <el-input v-model="knowledgeForm.parentId" placeholder="可选，不填表示顶级知识点" />
      </el-form-item>
      <el-form-item label="层级">
        <el-input-number v-model="knowledgeForm.level" :min="1" :max="10" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input
          v-model="knowledgeForm.description"
          type="textarea"
          :rows="4"
          maxlength="200"
          show-word-limit
          placeholder="请输入知识点描述"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="closeDialog">取消</el-button>
      <el-button type="primary" @click="submitKnowledge">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

defineProps({
  knowledgeList: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['save-knowledge', 'delete-knowledge'])

const knowledgeDialogVisible = ref(false)
const knowledgeDialogTitle = ref('添加知识点')
const knowledgeForm = reactive({
  id: null,
  name: '',
  parentId: '',
  level: 1,
  description: ''
})

const resetKnowledgeForm = () => {
  knowledgeForm.id = null
  knowledgeForm.name = ''
  knowledgeForm.parentId = ''
  knowledgeForm.level = 1
  knowledgeForm.description = ''
}

const openAddDialog = () => {
  knowledgeDialogTitle.value = '添加知识点'
  resetKnowledgeForm()
  knowledgeDialogVisible.value = true
}

const openEditDialog = (knowledge) => {
  knowledgeDialogTitle.value = '编辑知识点'
  knowledgeForm.id = knowledge.id
  knowledgeForm.name = knowledge.name || ''
  knowledgeForm.parentId = knowledge.parentId ?? ''
  knowledgeForm.level = knowledge.level || 1
  knowledgeForm.description = knowledge.description || ''
  knowledgeDialogVisible.value = true
}

const closeDialog = () => {
  knowledgeDialogVisible.value = false
}

const normalizeParentId = (value) => {
  if (value === '' || value === null || value === undefined) {
    return null
  }

  const numericValue = Number(value)
  return Number.isNaN(numericValue) ? value : numericValue
}

const submitKnowledge = () => {
  if (!knowledgeForm.name.trim()) {
    ElMessage.warning('请输入知识点名称')
    return
  }

  emit('save-knowledge', {
    form: {
      id: knowledgeForm.id,
      name: knowledgeForm.name.trim(),
      parentId: normalizeParentId(knowledgeForm.parentId),
      level: knowledgeForm.level,
      description: knowledgeForm.description?.trim() || ''
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
