<template>
  <div class="settings-page">
    <section class="settings-card">
      <div class="card-header">
        <div>
          <div class="section-kicker">Profile</div>
          <h2>资料设置</h2>
        </div>
      </div>

      <el-form :model="profileForm" label-position="top" class="settings-form">
        <div class="section-note">先完善基础资料，再决定哪些信息需要长期展示在个人控制台里。</div>
        <div class="form-grid">
          <el-form-item label="用户名">
            <el-input v-model="profileForm.username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="头像链接">
            <el-input v-model="profileForm.avatarUrl" placeholder="https://example.com/avatar.png" />
          </el-form-item>
        </div>

        <el-form-item label="个人简介">
          <el-input
            v-model="profileForm.bio"
            type="textarea"
            :rows="4"
            maxlength="120"
            show-word-limit
            placeholder="简要描述你的学习方向和目标"
          />
        </el-form-item>

        <div class="form-grid">
          <el-form-item label="GitHub">
            <el-input v-model="profileForm.githubUrl" placeholder="https://github.com/username" />
          </el-form-item>
          <el-form-item label="博客">
            <el-input v-model="profileForm.blogUrl" placeholder="https://yourblog.com" />
          </el-form-item>
        </div>

        <div class="form-actions">
          <el-button type="primary" :loading="profileSaving" @click="saveProfile">保存资料</el-button>
        </div>
      </el-form>
    </section>

    <section class="settings-card">
      <div class="card-header">
        <div>
          <div class="section-kicker">Security</div>
          <h2>账号安全</h2>
        </div>
      </div>

      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-position="top" class="settings-form">
        <div class="section-note">安全项单独管理，避免资料编辑和密码修改互相干扰。</div>
        <div class="form-grid">
          <el-form-item label="当前密码" prop="oldPassword">
            <el-input v-model="passwordForm.oldPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="passwordForm.newPassword" type="password" show-password />
          </el-form-item>
        </div>

        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>

        <div class="form-actions">
          <el-button type="primary" :loading="passwordSaving" @click="savePassword">修改密码</el-button>
        </div>
      </el-form>
    </section>

    <section class="settings-card">
      <div class="card-header">
        <div>
          <div class="section-kicker">Preferences</div>
          <h2>偏好设置</h2>
        </div>
      </div>

      <div class="preference-row">
        <div>
          <div class="preference-title">深色模式</div>
          <div class="preference-desc">切换当前页面的明暗主题</div>
        </div>
        <el-switch :model-value="themeStore.isDark" @change="handleThemeChange" />
      </div>
    </section>

    <section class="settings-card danger-card">
      <div class="card-header">
        <div>
          <div class="section-kicker">Danger Zone</div>
          <h2>退出登录</h2>
        </div>
      </div>

      <div class="preference-row">
        <div>
          <div class="preference-title">结束当前会话</div>
          <div class="preference-desc">退出当前账号并返回登录页</div>
        </div>
        <el-button type="danger" plain @click="handleLogout">退出登录</el-button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useThemeStore } from '@/stores/theme'
import { useUserStore } from '@/stores/user'
import { changePassword, updateProfile } from '@/api/userProfile'

const router = useRouter()
const themeStore = useThemeStore()
const userStore = useUserStore()

const passwordFormRef = ref(null)
const profileSaving = ref(false)
const passwordSaving = ref(false)

const profileForm = reactive({
  username: '',
  avatarUrl: '',
  bio: '',
  githubUrl: '',
  blogUrl: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为 6 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

const syncProfileForm = () => {
  profileForm.username = userStore.userInfo?.username || ''
  profileForm.avatarUrl = userStore.userInfo?.avatarUrl || ''
  profileForm.bio = userStore.userInfo?.bio || ''
  profileForm.githubUrl = userStore.userInfo?.githubUrl || ''
  profileForm.blogUrl = userStore.userInfo?.blogUrl || ''
}

const saveProfile = async () => {
  profileSaving.value = true
  try {
    const res = await updateProfile({ ...profileForm })
    if (res?.code !== 200) {
      throw new Error(res?.msg || '资料保存失败')
    }

    if (userStore.userInfo) {
      Object.assign(userStore.userInfo, { ...profileForm })
    }

    ElMessage.success('资料已更新')
  } catch (error) {
    ElMessage.error(error.message || '资料保存失败')
  } finally {
    profileSaving.value = false
  }
}

const savePassword = async () => {
  const valid = await passwordFormRef.value?.validate().catch(() => false)
  if (!valid) return

  passwordSaving.value = true
  try {
    const res = await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })

    if (res?.code !== 200) {
      throw new Error(res?.msg || '密码修改失败')
    }

    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    ElMessage.success('密码修改成功')
  } catch (error) {
    ElMessage.error(error.message || '密码修改失败')
  } finally {
    passwordSaving.value = false
  }
}

const handleThemeChange = (value) => {
  themeStore.setTheme(Boolean(value))
  ElMessage.success(value ? '已切换为深色模式' : '已切换为浅色模式')
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
    userStore.clearToken()
    router.push('/login')
    ElMessage.success('已退出登录')
  } catch {
    return
  }
}

watch(() => userStore.userInfo, syncProfileForm, { immediate: true, deep: true })
</script>

<style scoped>
.settings-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.settings-card {
  position: relative;
  overflow: hidden;
  border: 1px solid var(--border-color);
  border-radius: 22px;
  background: var(--bg-card);
  box-shadow: var(--shadow-md);
  padding: 22px;
}

.settings-card::before {
  content: '';
  position: absolute;
  inset: 0 auto auto 0;
  width: 100%;
  height: 1px;
  background: var(--gradient-line);
  opacity: 0.6;
}

.danger-card {
  border-color: rgba(244, 63, 94, 0.26);
}

.card-header {
  margin-bottom: 18px;
}

.section-kicker {
  margin-bottom: 8px;
  color: var(--brand-accent);
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.card-header h2 {
  margin: 0;
}

.settings-form {
  margin-top: 8px;
}

.section-note {
  margin-bottom: 16px;
  padding: 12px 14px;
  border: 1px solid var(--border-light);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.03);
  color: var(--text-secondary);
  font-size: 13px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 4px;
}

.preference-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 14px 0;
}

.preference-title {
  color: var(--text-primary);
  font-weight: 700;
}

.preference-desc {
  margin-top: 6px;
  color: var(--text-secondary);
}

@media (max-width: 720px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .preference-row {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
