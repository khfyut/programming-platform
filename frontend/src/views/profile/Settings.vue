<template>
  <div class="settings-page">
    <div class="settings-section">
      <h3 class="section-title">账号设置</h3>
      <div class="settings-list">
        <div class="setting-item">
          <div class="setting-info">
            <span class="setting-label">用户名</span>
            <span class="setting-value">{{ userStore.userInfo?.username || '未设置' }}</span>
          </div>
          <el-button link type="primary" @click="showUsernameDialog">修改</el-button>
        </div>

        <div class="setting-item">
          <div class="setting-info">
            <span class="setting-label">密码</span>
            <span class="setting-value">已启用登录密码保护</span>
          </div>
          <el-button link type="primary" @click="showPasswordDialog">修改密码</el-button>
        </div>
      </div>
    </div>

    <div class="settings-section">
      <h3 class="section-title">外观设置</h3>
      <div class="settings-list">
        <div class="setting-item">
          <div class="setting-info">
            <span class="setting-label">深色模式</span>
            <span class="setting-desc">切换当前页面的明暗主题</span>
          </div>
          <el-switch v-model="isDarkMode" @change="toggleTheme" />
        </div>
      </div>
    </div>

    <div class="settings-section danger">
      <h3 class="section-title">危险操作</h3>
      <div class="settings-list">
        <div class="setting-item">
          <div class="setting-info">
            <span class="setting-label">退出登录</span>
            <span class="setting-desc">退出当前账号并返回首页</span>
          </div>
          <el-button type="danger" plain @click="handleLogout">退出登录</el-button>
        </div>
      </div>
    </div>

    <el-dialog v-model="usernameDialogVisible" title="修改用户名" width="400px">
      <el-form ref="usernameFormRef" :model="usernameForm" :rules="usernameRules">
        <el-form-item label="新用户名" prop="username">
          <el-input v-model="usernameForm.username" placeholder="请输入新用户名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="usernameDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitUsername">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="420px">
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules">
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitPassword">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { changePassword, updateProfile } from '@/api/userProfile'

const router = useRouter()
const userStore = useUserStore()

const isDarkMode = ref(false)
const usernameDialogVisible = ref(false)
const passwordDialogVisible = ref(false)
const submitting = ref(false)

const usernameForm = ref({
  username: ''
})

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const usernameFormRef = ref(null)
const passwordFormRef = ref(null)

const usernameRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度需在 3 到 20 个字符之间', trigger: 'blur' }
  ]
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
          return
        }

        callback()
      },
      trigger: 'blur'
    }
  ]
}

const toggleTheme = () => {
  document.documentElement.classList.toggle('dark', isDarkMode.value)
  localStorage.setItem('theme', isDarkMode.value ? 'dark' : 'light')
  ElMessage.success(isDarkMode.value ? '已切换到深色模式' : '已切换到浅色模式')
}

const showUsernameDialog = () => {
  usernameForm.value.username = userStore.userInfo?.username || ''
  usernameDialogVisible.value = true
}

const showPasswordDialog = () => {
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  passwordDialogVisible.value = true
}

const submitUsername = async () => {
  const isValid = await usernameFormRef.value?.validate().catch(() => false)
  if (!isValid) {
    return
  }

  submitting.value = true
  try {
    await updateProfile({ username: usernameForm.value.username })

    if (userStore.userInfo) {
      userStore.userInfo.username = usernameForm.value.username
    }

    usernameDialogVisible.value = false
    ElMessage.success('用户名修改成功')
  } catch (error) {
    ElMessage.error(error.message || '用户名修改失败')
  } finally {
    submitting.value = false
  }
}

const submitPassword = async () => {
  const isValid = await passwordFormRef.value?.validate().catch(() => false)
  if (!isValid) {
    return
  }

  submitting.value = true
  try {
    await changePassword({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })

    passwordDialogVisible.value = false
    ElMessage.success('密码修改成功')
  } catch (error) {
    ElMessage.error(error.message || '密码修改失败')
  } finally {
    submitting.value = false
  }
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
    userStore.logout()
    router.push('/')
    ElMessage.success('已退出登录')
  } catch {
    return
  }
}

onMounted(() => {
  isDarkMode.value = document.documentElement.classList.contains('dark')
})
</script>

<style scoped>
.settings-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.settings-section {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.settings-section.danger {
  border: 1px solid #fde2e2;
}

.settings-section.danger .section-title {
  color: #f56c6c;
}

.section-title {
  margin: 0 0 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.settings-list {
  display: flex;
  flex-direction: column;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid #f0f2f5;
}

.setting-item:last-child {
  border-bottom: none;
}

.setting-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.setting-label {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
}

.setting-value {
  font-size: 14px;
  color: #606266;
}

.setting-desc {
  font-size: 13px;
  color: #909399;
}

@media (max-width: 768px) {
  .setting-item {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
