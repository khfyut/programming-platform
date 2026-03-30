<template>
  <div v-loading="profileLoading" class="settings-page">
    <section class="settings-card">
      <div class="card-header">
        <div>
          <div class="section-kicker">Profile</div>
          <h2>资料设置</h2>
        </div>
        <el-tag :type="hasProfileChanges ? 'warning' : 'success'" effect="plain">
          {{ hasProfileChanges ? '有未保存更改' : '资料已同步' }}
        </el-tag>
      </div>

      <div class="profile-preview">
        <el-avatar :size="72" :src="sanitizedProfile.avatarUrl">
          {{ (sanitizedProfile.username || 'U').slice(0, 1).toUpperCase() }}
        </el-avatar>
        <div class="preview-copy">
          <strong>{{ sanitizedProfile.username || '未命名用户' }}</strong>
          <span>{{ sanitizedProfile.bio || '补充一点简介，这里会成为你个人主页上的第一印象。' }}</span>
          <div v-if="sanitizedProfile.githubUrl || sanitizedProfile.blogUrl" class="preview-links">
            <a v-if="sanitizedProfile.githubUrl" :href="sanitizedProfile.githubUrl" target="_blank" rel="noreferrer">GitHub</a>
            <a v-if="sanitizedProfile.blogUrl" :href="sanitizedProfile.blogUrl" target="_blank" rel="noreferrer">博客</a>
          </div>
        </div>
      </div>

      <el-form
        ref="profileFormRef"
        :model="profileForm"
        :rules="profileRules"
        label-position="top"
        class="settings-form"
      >
        <div class="section-note">
          先把对外展示的信息整理好。保存后，侧边栏、总览页和社区里的资料展示都会同步更新。
        </div>

        <div class="form-grid">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="profileForm.username" maxlength="20" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="头像链接" prop="avatarUrl">
            <el-input v-model="profileForm.avatarUrl" placeholder="https://example.com/avatar.png" />
          </el-form-item>
        </div>

        <el-form-item label="个人简介" prop="bio">
          <el-input
            v-model="profileForm.bio"
            type="textarea"
            :rows="4"
            maxlength="120"
            show-word-limit
            placeholder="简单描述你的学习方向、擅长内容和近期目标"
          />
        </el-form-item>

        <div class="form-grid">
          <el-form-item label="GitHub" prop="githubUrl">
            <el-input v-model="profileForm.githubUrl" placeholder="https://github.com/username" />
          </el-form-item>
          <el-form-item label="博客" prop="blogUrl">
            <el-input v-model="profileForm.blogUrl" placeholder="https://yourblog.com" />
          </el-form-item>
        </div>

        <div class="form-actions">
          <span class="form-hint">保存后会自动刷新当前账号资料。</span>
          <div class="action-group">
            <el-button :disabled="!hasProfileChanges || profileSaving" @click="resetProfileForm">重置</el-button>
            <el-button
              type="primary"
              :loading="profileSaving"
              :disabled="!hasProfileChanges"
              @click="saveProfile"
            >
              保存资料
            </el-button>
          </div>
        </div>
      </el-form>
    </section>

    <section class="settings-card">
      <div class="card-header">
        <div>
          <div class="section-kicker">Security</div>
          <h2>账号安全</h2>
        </div>
        <span class="card-caption">{{ passwordStrengthLabel }}</span>
      </div>

      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-position="top"
        class="settings-form"
      >
        <div class="section-note">
          密码单独管理，避免资料编辑和安全修改互相打断。建议使用至少 8 位，并混合字母和数字。
        </div>

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

        <div class="strength-track">
          <div class="strength-bar" :class="passwordStrengthClass" :style="{ width: `${passwordStrengthPercent}%` }"></div>
        </div>

        <div class="form-actions">
          <span class="form-hint">修改成功后不会自动退出，你可以继续当前工作流。</span>
          <div class="action-group">
            <el-button :disabled="passwordSaving" @click="resetPasswordForm">清空</el-button>
            <el-button type="primary" :loading="passwordSaving" @click="savePassword">修改密码</el-button>
          </div>
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
          <div class="preference-desc">切换当前页面的明暗主题，适合在不同环境下阅读和刷题。</div>
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
          <div class="preference-desc">退出当前账号并返回登录页。未保存的表单内容会丢失。</div>
        </div>
        <el-button type="danger" plain @click="handleLogout">退出登录</el-button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useThemeStore } from '@/stores/theme'
import { useUserStore } from '@/stores/user'
import { changePassword, updateProfile } from '@/api/userProfile'

const router = useRouter()
const themeStore = useThemeStore()
const userStore = useUserStore()

const createProfileDefaults = () => ({
  username: '',
  avatarUrl: '',
  bio: '',
  githubUrl: '',
  blogUrl: ''
})

const profileFormRef = ref(null)
const passwordFormRef = ref(null)
const profileLoading = ref(false)
const profileSaving = ref(false)
const passwordSaving = ref(false)

const profileForm = reactive(createProfileDefaults())
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const normalizeText = (value) => String(value || '').trim()

const isValidUrl = (value) => {
  if (!value) {
    return true
  }

  try {
    const parsed = new URL(value)
    return ['http:', 'https:'].includes(parsed.protocol)
  } catch {
    return false
  }
}

const validateOptionalUrl = (_rule, value, callback) => {
  if (!normalizeText(value) || isValidUrl(normalizeText(value))) {
    callback()
    return
  }

  callback(new Error('请输入有效的 http(s) 链接'))
}

const profileRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        const normalized = normalizeText(value)

        if (!normalized) {
          callback(new Error('请输入用户名'))
          return
        }

        if (normalized.length < 2) {
          callback(new Error('用户名至少需要 2 个字符'))
          return
        }

        callback()
      },
      trigger: 'blur'
    }
  ],
  avatarUrl: [{ validator: validateOptionalUrl, trigger: 'blur' }],
  githubUrl: [{ validator: validateOptionalUrl, trigger: 'blur' }],
  blogUrl: [{ validator: validateOptionalUrl, trigger: 'blur' }]
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        const normalized = normalizeText(value)

        if (normalized.length < 8) {
          callback(new Error('新密码至少需要 8 位'))
          return
        }

        if (normalized === normalizeText(passwordForm.oldPassword)) {
          callback(new Error('新密码不能与当前密码相同'))
          return
        }

        callback()
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (normalizeText(value) !== normalizeText(passwordForm.newPassword)) {
          callback(new Error('两次输入的新密码不一致'))
          return
        }

        callback()
      },
      trigger: 'blur'
    }
  ]
}

const sanitizedProfile = computed(() => ({
  username: normalizeText(profileForm.username),
  avatarUrl: normalizeText(profileForm.avatarUrl),
  bio: normalizeText(profileForm.bio),
  githubUrl: normalizeText(profileForm.githubUrl),
  blogUrl: normalizeText(profileForm.blogUrl)
}))

const hasProfileChanges = computed(() => {
  const source = {
    username: normalizeText(userStore.userInfo?.username),
    avatarUrl: normalizeText(userStore.userInfo?.avatarUrl),
    bio: normalizeText(userStore.userInfo?.bio),
    githubUrl: normalizeText(userStore.userInfo?.githubUrl),
    blogUrl: normalizeText(userStore.userInfo?.blogUrl)
  }

  return Object.keys(source).some((key) => source[key] !== sanitizedProfile.value[key])
})

const passwordStrengthPercent = computed(() => {
  const value = normalizeText(passwordForm.newPassword)

  if (!value) return 0

  let score = 25
  if (value.length >= 8) score += 25
  if (/[A-Z]/.test(value) && /[a-z]/.test(value)) score += 20
  if (/\d/.test(value)) score += 15
  if (/[^A-Za-z0-9]/.test(value)) score += 15

  return Math.min(score, 100)
})

const passwordStrengthLabel = computed(() => {
  if (passwordStrengthPercent.value >= 80) return '密码强度：较强'
  if (passwordStrengthPercent.value >= 50) return '密码强度：中等'
  if (passwordStrengthPercent.value > 0) return '密码强度：较弱'
  return '密码强度：待输入'
})

const passwordStrengthClass = computed(() => {
  if (passwordStrengthPercent.value >= 80) return 'strong'
  if (passwordStrengthPercent.value >= 50) return 'medium'
  return 'weak'
})

const syncProfileForm = () => {
  Object.assign(profileForm, createProfileDefaults(), {
    username: userStore.userInfo?.username || '',
    avatarUrl: userStore.userInfo?.avatarUrl || '',
    bio: userStore.userInfo?.bio || '',
    githubUrl: userStore.userInfo?.githubUrl || '',
    blogUrl: userStore.userInfo?.blogUrl || ''
  })
}

const ensureUserReady = async () => {
  if (userStore.userInfo?.id || !userStore.token) {
    return
  }

  profileLoading.value = true
  try {
    await userStore.fetchUserInfo()
  } catch (error) {
    console.error('加载当前用户资料失败:', error)
    ElMessage.error('当前账号资料加载失败')
  } finally {
    profileLoading.value = false
  }
}

const resetProfileForm = () => {
  syncProfileForm()
  profileFormRef.value?.clearValidate()
}

const resetPasswordForm = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordFormRef.value?.clearValidate()
}

const saveProfile = async () => {
  if (!hasProfileChanges.value) {
    ElMessage.info('当前没有需要保存的更改')
    return
  }

  const valid = await profileFormRef.value?.validate().catch(() => false)
  if (!valid) return

  profileSaving.value = true
  try {
    const payload = { ...sanitizedProfile.value }
    const res = await updateProfile(payload)

    if (res?.code !== 200) {
      throw new Error(res?.msg || '资料保存失败')
    }

    await userStore.fetchUserInfo()
    syncProfileForm()
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
      oldPassword: normalizeText(passwordForm.oldPassword),
      newPassword: normalizeText(passwordForm.newPassword)
    })

    if (res?.code !== 200) {
      throw new Error(res?.msg || '密码修改失败')
    }

    resetPasswordForm()
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
    await router.push('/login')
    ElMessage.success('已退出登录')
  } catch {
    return
  }
}

watch(() => userStore.userInfo, syncProfileForm, { immediate: true, deep: true })

onMounted(async () => {
  await ensureUserReady()
  syncProfileForm()
})
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
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
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

.card-caption {
  color: var(--text-secondary);
  font-size: 13px;
}

.profile-preview {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 18px;
  padding: 16px;
  border: 1px solid var(--border-light);
  border-radius: 18px;
  background:
    radial-gradient(circle at top left, rgba(0, 209, 255, 0.1), transparent 34%),
    rgba(255, 255, 255, 0.03);
}

.preview-copy {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 6px;
}

.preview-copy strong {
  color: var(--text-primary);
  font-size: 18px;
}

.preview-copy span {
  color: var(--text-secondary);
  line-height: 1.7;
}

.preview-links {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.preview-links a {
  color: var(--brand-primary);
  font-size: 13px;
  font-weight: 700;
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
  line-height: 1.7;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.form-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 6px;
}

.form-hint {
  color: var(--text-secondary);
  font-size: 13px;
}

.action-group {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}

.strength-track {
  position: relative;
  height: 8px;
  margin-top: 2px;
  overflow: hidden;
  border-radius: 999px;
  background: rgba(148, 163, 184, 0.18);
}

.strength-bar {
  height: 100%;
  border-radius: inherit;
  transition: width var(--transition-fast), background var(--transition-fast);
}

.strength-bar.weak {
  background: linear-gradient(90deg, #fb7185, #f97316);
}

.strength-bar.medium {
  background: linear-gradient(90deg, #f59e0b, #facc15);
}

.strength-bar.strong {
  background: linear-gradient(90deg, #22c55e, #14b8a6);
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
  line-height: 1.7;
}

@media (max-width: 720px) {
  .card-header,
  .form-actions,
  .profile-preview,
  .preference-row {
    align-items: flex-start;
    flex-direction: column;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .action-group {
    width: 100%;
    justify-content: stretch;
  }
}
</style>
