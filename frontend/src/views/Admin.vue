<template>
  <div class="admin-page">
    <AdminDashboardOverview
      :loading="statsLoading"
      :statistics="statistics"
      @refresh="fetchStatistics"
    />

    <el-tabs v-model="activeTab" class="admin-tabs">
      <el-tab-pane label="用户管理" name="users">
        <UserManagement
          :users="userList"
          :loading="usersLoading"
          @refresh="fetchUsers"
          @toggle-status="toggleUserStatus"
          @update-role="updateUserRoleHandler"
        />
      </el-tab-pane>

      <el-tab-pane label="题目管理" name="problems">
        <ProblemManagement
          :problems="problemList"
          :loading="problemsLoading"
          :import-loading="importLoading"
          @save-problem="saveProblem"
          @delete-problem="deleteProblem"
          @import-problems="importProblems"
        />
      </el-tab-pane>

      <el-tab-pane label="提交记录" name="submissions">
        <SubmissionsManagement
          :submissions="submissionList"
          :loading="submissionsLoading"
          @refresh="fetchAllSubmissions"
          @open-problem="goToProblem"
        />
      </el-tab-pane>

      <el-tab-pane label="角色权限" name="roles">
        <RoleManagement
          :roles="roleList"
          :permissions="permissionList"
          :selected-permission-ids="selectedPermissionIds"
          :loading="rolesLoading"
          @save-role="saveRole"
          @delete-role="deleteRoleItem"
          @load-permissions="loadPermissions"
          @save-permissions="savePermissions"
        />
      </el-tab-pane>

      <el-tab-pane label="知识点管理" name="knowledge">
        <KnowledgeManagement
          :knowledge-list="knowledgeList"
          :loading="knowledgeLoading"
          @save-knowledge="saveKnowledge"
          @delete-knowledge="deleteKnowledgeItem"
        />
      </el-tab-pane>

      <el-tab-pane label="学习路径" name="paths">
        <LearningPathManagement
          :paths="pathList"
          :loading="pathsLoading"
          @save-path="savePath"
          @delete-path="deletePathItem"
        />
      </el-tab-pane>
    </el-tabs>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import AdminDashboardOverview from '@/components/admin/AdminDashboardOverview.vue'
import KnowledgeManagement from '@/components/admin/KnowledgeManagement.vue'
import LearningPathManagement from '@/components/admin/LearningPathManagement.vue'
import ProblemManagement from '@/components/admin/ProblemManagement.vue'
import RoleManagement from '@/components/admin/RoleManagement.vue'
import SubmissionsManagement from '@/components/admin/SubmissionsManagement.vue'
import UserManagement from '@/components/admin/UserManagement.vue'
import {
  getUserList,
  updateUserStatus,
  updateUserRole,
  addProblem,
  updateProblem,
  deleteProblem as deleteProblemApi,
  getAllSubmissions,
  getStatistics,
  importFromExcel,
  importFromCsv,
  getRoleList,
  getRoleDetail,
  addRole,
  updateRole,
  deleteRole,
  getPermissionList,
  assignRolePermissions,
  getKnowledgeList,
  addKnowledge,
  updateKnowledge,
  deleteKnowledge,
  getPathList,
  addPath,
  updatePath,
  deletePath
} from '@/api/admin'
import { getProblemList } from '@/api/problem'

const router = useRouter()

const activeTab = ref('users')
const statsLoading = ref(false)
const usersLoading = ref(false)
const problemsLoading = ref(false)
const submissionsLoading = ref(false)
const rolesLoading = ref(false)
const knowledgeLoading = ref(false)
const pathsLoading = ref(false)

const statistics = ref({})
const userList = ref([])
const problemList = ref([])
const submissionList = ref([])
const roleList = ref([])
const knowledgeList = ref([])
const pathList = ref([])
const permissionList = ref([])
const selectedPermissionIds = ref([])

const importLoading = ref(false)

const fetchStatistics = async () => {
  statsLoading.value = true
  try {
    const res = await getStatistics()
    if (res.code === 200) {
      statistics.value = res.data
    }
  } catch (error) {
    console.error('鑾峰彇缁熻鏁版嵁澶辫触:', error)
  } finally {
    statsLoading.value = false
  }
}

const fetchUsers = async () => {
  usersLoading.value = true
  try {
    const res = await getUserList({ page: 1, size: 100 })
    if (res.code === 200) {
      userList.value = res.data?.list || []
    }
  } catch (error) {
    console.error('鑾峰彇鐢ㄦ埛鍒楄〃澶辫触:', error)
  } finally {
    usersLoading.value = false
  }
}

const fetchProblems = async () => {
  problemsLoading.value = true
  try {
    const res = await getProblemList({ page: 1, size: 100 })
    if (res.code === 200) {
      problemList.value = res.data.list || []
    }
  } catch (error) {
    console.error('鑾峰彇棰樼洰鍒楄〃澶辫触:', error)
  } finally {
    problemsLoading.value = false
  }
}

const fetchAllSubmissions = async () => {
  submissionsLoading.value = true
  try {
    const res = await getAllSubmissions({ page: 1, size: 100 })
    if (res.code === 200) {
      submissionList.value = res.data?.list || []
    }
  } catch (error) {
    console.error('鑾峰彇鎻愪氦璁板綍澶辫触:', error)
  } finally {
    submissionsLoading.value = false
  }
}

const saveProblem = async ({ form, done } = {}) => {
  try {
    let res
    if (form?.id) {
      res = await updateProblem(form)
    } else {
      res = await addProblem(form)
    }

    if (res.code === 200) {
      ElMessage.success(form?.id ? '更新成功' : '添加成功')
      done?.()
      fetchProblems()
      fetchStatistics()
    } else {
      ElMessage.error(res.msg || '鎿嶄綔澶辫触')
    }
  } catch (error) {
    ElMessage.error('鎿嶄綔澶辫触锛岃閲嶈瘯')
  }
}

const deleteProblem = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该题目吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await deleteProblemApi(id)
    if (res.code === 200) {
      ElMessage.success('鍒犻櫎鎴愬姛')
      fetchProblems()
      fetchStatistics()
    } else {
      ElMessage.error(res.msg || '鍒犻櫎澶辫触')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('鍒犻櫎澶辫触锛岃閲嶈瘯')
    }
  }
}

const goToProblem = (id) => {
  router.push(`/problem/${id}`)
}

const importProblems = async ({ type, file, done } = {}) => {
  importLoading.value = true
  try {
    let res
    if (type === 'excel') {
      res = await importFromExcel(file)
    } else {
      res = await importFromCsv(file)
    }

    if (res.code === 200) {
      ElMessage.success('导入成功')
      done?.()
      fetchProblems()
      fetchStatistics()
    } else {
      ElMessage.error(res.msg || '瀵煎叆澶辫触')
    }
  } catch (error) {
    console.error('瀵煎叆澶辫触:', error)
    ElMessage.error('瀵煎叆澶辫触锛岃閲嶈瘯')
  } finally {
    importLoading.value = false
  }
}

const toggleUserStatus = async (user) => {
  try {
    const newStatus = user.status === 1 ? 0 : 1
    const res = await updateUserStatus({ userId: user.id, status: newStatus })
    if (res.code === 200) {
      ElMessage.success(newStatus === 1 ? '用户已启用' : '用户已禁用')
      fetchUsers()
    } else {
      ElMessage.error(res.msg || '鎿嶄綔澶辫触')
    }
  } catch (error) {
    ElMessage.error('鎿嶄綔澶辫触锛岃閲嶈瘯')
  }
}

const updateUserRoleHandler = async ({ userId, roleId, done } = {}) => {
  try {
    const res = await updateUserRole({ userId, roleId })
    if (res.code === 200) {
      ElMessage.success('角色修改成功')
      done?.()
      fetchUsers()
    } else {
      ElMessage.error(res.msg || '鎿嶄綔澶辫触')
    }
  } catch (error) {
    ElMessage.error('鎿嶄綔澶辫触锛岃閲嶈瘯')
  }
}

const fetchRoles = async () => {
  rolesLoading.value = true
  try {
    const res = await getRoleList()
    if (res.code === 200) {
      roleList.value = res.data || []
    }
  } catch (error) {
    console.error('鑾峰彇瑙掕壊鍒楄〃澶辫触:', error)
  } finally {
    rolesLoading.value = false
  }
}

const saveRole = async ({ form, done } = {}) => {
  try {
    let res
    if (form?.id) {
      res = await updateRole(form)
    } else {
      res = await addRole(form)
    }

    if (res.code === 200) {
      ElMessage.success(form?.id ? '更新成功' : '添加成功')
      done?.()
      fetchRoles()
    } else {
      ElMessage.error(res.msg || '鎿嶄綔澶辫触')
    }
  } catch (error) {
    ElMessage.error('鎿嶄綔澶辫触锛岃閲嶈瘯')
  }
}

const deleteRoleItem = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该角色吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await deleteRole(id)
    if (res.code === 200) {
      ElMessage.success('鍒犻櫎鎴愬姛')
      fetchRoles()
    } else {
      ElMessage.error(res.msg || '鍒犻櫎澶辫触')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('鍒犻櫎澶辫触锛岃閲嶈瘯')
    }
  }
}

const loadPermissions = async (role) => {
  try {
    const [permissionRes, roleRes] = await Promise.all([
      getPermissionList(),
      role?.id ? getRoleDetail(role.id) : Promise.resolve(null)
    ])

    if (permissionRes?.code === 200) {
      permissionList.value = permissionRes.data || []
    }

    if (roleRes?.code === 200) {
      selectedPermissionIds.value = (roleRes.data?.permissions || []).map((item) => item.id)
    } else {
      selectedPermissionIds.value = (role?.permissions || []).map((item) => item.id)
    }
  } catch (error) {
    console.error('鑾峰彇鏉冮檺鍒楄〃澶辫触:', error)
    selectedPermissionIds.value = []
  }
}

const savePermissions = async ({ roleId, permissionIds, done } = {}) => {
  try {
    const res = await assignRolePermissions({
      roleId,
      permissionIds
    })
    if (res.code === 200) {
      ElMessage.success('权限分配成功')
      selectedPermissionIds.value = permissionIds || []
      done?.()
      fetchRoles()
    } else {
      ElMessage.error(res.msg || '鎿嶄綔澶辫触')
    }
  } catch (error) {
    ElMessage.error('鎿嶄綔澶辫触锛岃閲嶈瘯')
  }
}

const fetchKnowledge = async () => {
  knowledgeLoading.value = true
  try {
    const res = await getKnowledgeList()
    if (res.code === 200) {
      knowledgeList.value = res.data || []
    }
  } catch (error) {
    console.error('获取知识点列表失败:', error)
  } finally {
    knowledgeLoading.value = false
  }
}

const saveKnowledge = async ({ form, done } = {}) => {
  try {
    let res
    if (form?.id) {
      res = await updateKnowledge(form)
    } else {
      res = await addKnowledge(form)
    }

    if (res.code === 200) {
      ElMessage.success(form?.id ? '更新成功' : '添加成功')
      done?.()
      fetchKnowledge()
    } else {
      ElMessage.error(res.msg || '鎿嶄綔澶辫触')
    }
  } catch (error) {
    ElMessage.error('鎿嶄綔澶辫触锛岃閲嶈瘯')
  }
}

const deleteKnowledgeItem = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该知识点吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await deleteKnowledge(id)
    if (res.code === 200) {
      ElMessage.success('鍒犻櫎鎴愬姛')
      fetchKnowledge()
    } else {
      ElMessage.error(res.msg || '鍒犻櫎澶辫触')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('鍒犻櫎澶辫触锛岃閲嶈瘯')
    }
  }
}

const fetchPaths = async () => {
  pathsLoading.value = true
  try {
    const res = await getPathList()
    if (res.code === 200) {
      pathList.value = res.data || []
    }
  } catch (error) {
    console.error('鑾峰彇瀛︿範璺緞鍒楄〃澶辫触:', error)
  } finally {
    pathsLoading.value = false
  }
}

const savePath = async ({ form, done } = {}) => {
  try {
    let res
    if (form?.id) {
      res = await updatePath(form)
    } else {
      res = await addPath(form)
    }

    if (res.code === 200) {
      ElMessage.success(form?.id ? '更新成功' : '添加成功')
      done?.()
      fetchPaths()
    } else {
      ElMessage.error(res.msg || '鎿嶄綔澶辫触')
    }
  } catch (error) {
    ElMessage.error('鎿嶄綔澶辫触锛岃閲嶈瘯')
  }
}

const deletePathItem = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该学习路径吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await deletePath(id)
    if (res.code === 200) {
      ElMessage.success('鍒犻櫎鎴愬姛')
      fetchPaths()
    } else {
      ElMessage.error(res.msg || '鍒犻櫎澶辫触')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('鍒犻櫎澶辫触锛岃閲嶈瘯')
    }
  }
}

onMounted(() => {
  fetchStatistics()
  fetchUsers()
  fetchProblems()
  fetchAllSubmissions()
  fetchRoles()
  fetchKnowledge()
  fetchPaths()
})
</script>

<style scoped>
.admin-page {
  width: 100%;
  min-height: 100vh;
  background: var(--leetcode-bg-secondary, #F7F8FA);
  padding: 32px;
  box-sizing: border-box;
}

/* Tabs 鏍峰紡 */
.admin-tabs {
  margin-top: 24px;
}

:deep(.admin-tabs .el-tabs__header) {
  margin-bottom: 24px;
  border-bottom: 1px solid var(--leetcode-border, #E5E7EB);
}

:deep(.admin-tabs .el-tabs__nav-wrap::after) {
  display: none;
}

:deep(.admin-tabs .el-tabs__item) {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
  padding: 0 20px;
  height: 44px;
  line-height: 44px;
  transition: all 0.2s ease;
}

:deep(.admin-tabs .el-tabs__item:hover) {
  color: var(--brand-primary);
}

:deep(.admin-tabs .el-tabs__item.is-active) {
  color: var(--brand-primary);
  font-weight: 600;
}

:deep(.admin-tabs .el-tabs__active-bar) {
  background-color: var(--brand-primary);
  height: 3px;
  border-radius: 2px;
}

@media (max-width: 768px) {
  .admin-page {
    padding: 16px;
  }
}

</style>

