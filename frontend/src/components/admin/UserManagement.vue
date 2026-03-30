<template>
  <el-card class="management-card" shadow="never">
    <template #header>
      <div class="card-header">
        <span class="card-title">用户管理</span>
        <el-button type="primary" link @click="$emit('refresh')">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </template>

    <el-table
      :data="users"
      stripe
      row-key="id"
      v-loading="loading"
      max-height="500"
      class="admin-table"
      table-layout="fixed"
      empty-text="暂无用户数据"
      :default-sort="{ prop: 'id', order: 'descending' }"
    >
      <el-table-column prop="id" label="ID" width="80" sortable />
      <el-table-column prop="username" label="用户名" min-width="160" sortable show-overflow-tooltip />
      <el-table-column prop="role" label="角色" width="110" sortable>
        <template #default="{ row }">
          <el-tag :type="row.role === 1 ? 'danger' : 'info'" size="small">
            {{ row.role === 1 ? '管理员' : '普通用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100" sortable>
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'warning'" size="small" effect="plain">
            {{ row.status === 1 ? '正常' : '已禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="language" label="语言" width="110" sortable>
        <template #default="{ row }">
          <span class="language-badge">{{ formatLanguage(row.language) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="180" sortable show-overflow-tooltip />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link class="action-btn" @click="openRoleDialog(row)">修改角色</el-button>
          <el-button type="warning" link class="action-btn" @click="$emit('toggle-status', row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog
    v-model="roleDialogVisible"
    title="修改用户角色"
    width="min(420px, 92vw)"
    destroy-on-close
    class="role-dialog"
  >
    <el-form label-width="80px" class="role-form">
      <el-form-item label="用户名">
        <el-input :value="selectedUser?.username" disabled />
      </el-form-item>
      <el-form-item label="角色">
        <el-select v-model="selectedRole" placeholder="请选择角色" class="role-select">
          <el-option label="普通用户" :value="0" />
          <el-option label="管理员" :value="1" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="closeRoleDialog">取消</el-button>
      <el-button type="primary" @click="submitRoleUpdate">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'

defineProps({
  users: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['refresh', 'toggle-status', 'update-role'])

const roleDialogVisible = ref(false)
const selectedUser = ref(null)
const selectedRole = ref(0)

const formatLanguage = (language) => {
  if (!language) return '--'
  return String(language).toUpperCase()
}

const openRoleDialog = (user) => {
  selectedUser.value = user
  selectedRole.value = user.role
  roleDialogVisible.value = true
}

const closeRoleDialog = () => {
  roleDialogVisible.value = false
  selectedUser.value = null
  selectedRole.value = 0
}

const submitRoleUpdate = () => {
  if (!selectedUser.value) {
    return
  }

  emit('update-role', {
    userId: selectedUser.value.id,
    roleId: selectedRole.value,
    done: closeRoleDialog
  })
}
</script>

<style scoped>
.role-form {
  padding-top: 4px;
}

.role-select {
  width: 100%;
}

:deep(.role-dialog .el-dialog__footer) {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  :deep(.role-dialog .el-form-item__label) {
    width: 100%;
    justify-content: flex-start;
    margin-bottom: 6px;
  }

  :deep(.role-dialog .el-form-item__content) {
    margin-left: 0 !important;
  }
}
</style>
