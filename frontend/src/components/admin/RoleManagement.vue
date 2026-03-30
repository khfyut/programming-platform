<template>
  <el-card class="management-card" shadow="never">
    <template #header>
      <div class="card-header">
        <span class="card-title">角色权限管理</span>
        <el-button type="primary" class="add-btn" @click="openAddDialog">
          <el-icon><Plus /></el-icon>
          添加角色
        </el-button>
      </div>
    </template>

    <el-table
      :data="roles"
      stripe
      row-key="id"
      v-loading="loading"
      max-height="500"
      class="admin-table"
      table-layout="fixed"
      empty-text="暂无角色数据"
      :default-sort="{ prop: 'id', order: 'ascending' }"
    >
      <el-table-column prop="id" label="ID" width="80" sortable />
      <el-table-column prop="name" label="角色名称" min-width="160" sortable show-overflow-tooltip />
      <el-table-column prop="description" label="描述" min-width="220" show-overflow-tooltip />
      <el-table-column label="权限数" width="100" sortable>
        <template #default="{ row }">
          <el-tag size="small" effect="plain">
            {{ row.permissions?.length || 0 }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link class="action-btn" @click="openEditDialog(row)">编辑</el-button>
          <el-button type="success" link class="action-btn" @click="openPermissionDialog(row)">权限</el-button>
          <el-button type="danger" link class="action-btn" @click="$emit('delete-role', row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="roleDialogVisible" :title="roleDialogTitle" width="500px">
    <el-form :model="roleForm" label-width="80px">
      <el-form-item label="角色名称">
        <el-input v-model="roleForm.name" maxlength="30" show-word-limit placeholder="请输入角色名称" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input
          v-model="roleForm.description"
          type="textarea"
          :rows="4"
          maxlength="200"
          show-word-limit
          placeholder="请输入角色描述"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="closeRoleDialog">取消</el-button>
      <el-button type="primary" @click="submitRole">确定</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="permissionDialogVisible" title="分配权限" width="600px">
    <div class="permission-header">
      <span class="permission-title">{{ currentRoleName || '当前角色' }}</span>
      <span class="permission-count">已选 {{ selectedPermissions.length }} 项</span>
    </div>
    <el-scrollbar max-height="320px">
      <el-checkbox-group v-model="selectedPermissions" class="permission-group">
        <el-checkbox v-for="perm in permissions" :key="perm.id" :label="perm.id" class="permission-checkbox">
          <div class="permission-item">
            <span class="permission-name">{{ perm.name }}</span>
            <span class="permission-code">{{ perm.code || `ID: ${perm.id}` }}</span>
          </div>
        </el-checkbox>
      </el-checkbox-group>
    </el-scrollbar>
    <el-empty v-if="permissions.length === 0" description="暂无可分配权限" />
    <template #footer>
      <el-button @click="closePermissionDialog">取消</el-button>
      <el-button type="primary" @click="submitPermissions">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const props = defineProps({
  roles: {
    type: Array,
    default: () => []
  },
  permissions: {
    type: Array,
    default: () => []
  },
  selectedPermissionIds: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['save-role', 'delete-role', 'load-permissions', 'save-permissions'])

const roleDialogVisible = ref(false)
const roleDialogTitle = ref('添加角色')
const permissionDialogVisible = ref(false)
const currentRoleId = ref(null)
const currentRoleName = ref('')
const selectedPermissions = ref([])
const roleForm = reactive({
  id: null,
  name: '',
  description: ''
})

watch(
  () => props.selectedPermissionIds,
  (value) => {
    if (permissionDialogVisible.value) {
      selectedPermissions.value = [...value]
    }
  },
  { deep: true }
)

const resetRoleForm = () => {
  roleForm.id = null
  roleForm.name = ''
  roleForm.description = ''
}

const openAddDialog = () => {
  roleDialogTitle.value = '添加角色'
  resetRoleForm()
  roleDialogVisible.value = true
}

const openEditDialog = (role) => {
  roleDialogTitle.value = '编辑角色'
  roleForm.id = role.id
  roleForm.name = role.name || ''
  roleForm.description = role.description || ''
  roleDialogVisible.value = true
}

const closeRoleDialog = () => {
  roleDialogVisible.value = false
}

const submitRole = () => {
  if (!roleForm.name.trim()) {
    ElMessage.warning('请输入角色名称')
    return
  }

  emit('save-role', {
    form: {
      id: roleForm.id,
      name: roleForm.name.trim(),
      description: roleForm.description?.trim() || ''
    },
    done: closeRoleDialog
  })
}

const openPermissionDialog = (role) => {
  currentRoleId.value = role.id
  currentRoleName.value = role.name || ''
  selectedPermissions.value = (role.permissions || []).map((item) => item.id)
  permissionDialogVisible.value = true
  emit('load-permissions', role)
}

const closePermissionDialog = () => {
  permissionDialogVisible.value = false
  currentRoleId.value = null
  currentRoleName.value = ''
  selectedPermissions.value = []
}

const submitPermissions = () => {
  emit('save-permissions', {
    roleId: currentRoleId.value,
    permissionIds: selectedPermissions.value,
    done: closePermissionDialog
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

.permission-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  color: var(--text-secondary);
  font-size: 13px;
}

.permission-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.permission-group {
  display: grid;
  gap: 10px;
}

.permission-checkbox {
  display: block;
  margin-left: 0 !important;
}

.permission-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  width: 100%;
}

.permission-name {
  color: var(--text-primary);
}

.permission-code {
  color: var(--text-tertiary);
  font-size: 12px;
}

:deep(.permission-checkbox .el-checkbox__label) {
  display: block;
  width: 100%;
  font-size: 14px;
}
</style>
