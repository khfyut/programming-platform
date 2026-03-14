<template>
  <div class="register-container">
    <!-- 左侧视觉区 -->
    <div class="visual-section">
      <!-- 动态网格背景 -->
      <div class="animated-grid"></div>

      <!-- 浮动的代码片段 -->
      <div class="floating-codes">
        <div class="code-card" style="top: 15%; left: 10%; animation-delay: 0s;">
          <pre><code>class Solution:
    def twoSum(self, nums, target):
        seen = {}
        for i, num in enumerate(nums):
            if target - num in seen:
                return [seen[target - num], i]
            seen[num] = i</code></pre>
        </div>
        <div class="code-card" style="top: 45%; left: 20%; animation-delay: 1s;">
          <pre><code>function quickSort(arr) {
    if (arr.length <= 1) return arr
    const pivot = arr[0]
    const left = arr.filter(x => x < pivot)
    const right = arr.filter(x => x > pivot)
    return [...quickSort(left), pivot, ...quickSort(right)]
}</code></pre>
        </div>
        <div class="code-card" style="top: 70%; left: 8%; animation-delay: 2s;">
          <pre><code>public class BinarySearch {
    public static int search(int[] nums, int target) {
        int left = 0, right = nums.length - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            if (nums[mid] == target) return mid;
            if (nums[mid] < target) left = mid + 1;
            else right = mid - 1;
        }
        return -1;
    }
}</code></pre>
        </div>
      </div>

      <!-- 品牌介绍 -->
      <div class="brand-intro">
        <div class="brand-logo">
          <el-icon :size="48"><Edit /></el-icon>
        </div>
        <h1>加入编程学习社区</h1>
        <p class="subtitle">开启你的编程学习之旅</p>
        <div class="features">
          <div class="feature-item">
            <el-icon><Trophy /></el-icon>
            <span>海量题库</span>
          </div>
          <div class="feature-item">
            <el-icon><Monitor /></el-icon>
            <span>在线运行</span>
          </div>
          <div class="feature-item">
            <el-icon><ChatDotRound /></el-icon>
            <span>AI 答疑</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧注册区 -->
    <div class="register-section">
      <div class="glass-card">
        <div class="register-header">
          <h2>创建账户</h2>
          <p>填写信息开始学习</p>
        </div>

        <el-form :model="form" :rules="rules" ref="formRef" class="register-form">
          <el-form-item prop="username">
            <div class="input-wrapper">
              <el-icon class="input-icon"><User /></el-icon>
              <el-input
                v-model="form.username"
                placeholder="用户名"
                size="large"
                class="custom-input"
              />
            </div>
          </el-form-item>

          <el-form-item prop="password">
            <div class="input-wrapper">
              <el-icon class="input-icon"><Lock /></el-icon>
              <el-input
                v-model="form.password"
                type="password"
                placeholder="密码"
                size="large"
                class="custom-input"
              />
            </div>
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <div class="input-wrapper">
              <el-icon class="input-icon"><Lock /></el-icon>
              <el-input
                v-model="form.confirmPassword"
                type="password"
                placeholder="确认密码"
                size="large"
                class="custom-input"
                @keyup.enter="handleRegister"
              />
            </div>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              @click="handleRegister"
              class="register-button"
            >
              <el-icon v-if="!loading"><Right /></el-icon>
              {{ loading ? '注册中...' : '注册' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="register-footer">
          <span>已有账号？</span>
          <router-link to="/login" class="login-link">立即登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Edit, Right, User, Lock, Trophy, Monitor, ChatDotRound } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少 6 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  const valid = await formRef.value.validate()
  if (!valid) return

  loading.value = true
  try {
    const success = await userStore.register(form.username, form.password)
    if (success) {
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } else {
      ElMessage.error('注册失败，用户名可能已存在')
    }
  } catch (error) {
    ElMessage.error('注册失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  min-height: 100vh;
  background: var(--dark-bg-primary);
  overflow: hidden;
}

/* ==================== 左侧视觉区 ==================== */
.visual-section {
  flex: 1;
  position: relative;
  background: var(--gradient-dark);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 动态网格背景 */
.animated-grid {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image:
    linear-gradient(rgba(0, 102, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 102, 255, 0.03) 1px, transparent 1px);
  background-size: 50px 50px;
  animation: gridMove 20s linear infinite;
}

@keyframes gridMove {
  0% {
    background-position: 0 0;
  }
  100% {
    background-position: 50px 50px;
  }
}

/* 浮动的代码片段 */
.floating-codes {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.code-card {
  position: absolute;
  background: rgba(22, 27, 34, 0.8);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 102, 255, 0.2);
  border-radius: 12px;
  padding: 16px;
  font-family: var(--font-code);
  font-size: 12px;
  color: var(--dark-text-secondary);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  animation: float 6s ease-in-out infinite;
  max-width: 280px;
}

.code-card pre {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(1deg);
  }
}

/* 品牌介绍 */
.brand-intro {
  position: relative;
  z-index: 10;
  text-align: center;
  color: white;
  animation: fadeInUp 1s ease-out;
}

.brand-logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  background: var(--gradient-brand);
  border-radius: 20px;
  margin-bottom: 24px;
  box-shadow: 0 8px 32px rgba(0, 102, 255, 0.4);
  animation: glow 3s ease-in-out infinite;
}

.brand-intro h1 {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 12px;
  background: linear-gradient(135deg, #FFFFFF 0%, #A8DADC 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.subtitle {
  font-size: 18px;
  color: var(--dark-text-secondary);
  margin-bottom: 32px;
}

.features {
  display: flex;
  gap: 24px;
  justify-content: center;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 24px;
  background: rgba(22, 27, 34, 0.6);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 102, 255, 0.2);
  border-radius: 12px;
  transition: all var(--transition-base);
}

.feature-item:hover {
  transform: translateY(-4px);
  border-color: rgba(0, 102, 255, 0.4);
  box-shadow: 0 8px 24px rgba(0, 102, 255, 0.2);
}

.feature-item .el-icon {
  font-size: 24px;
  color: var(--brand-primary);
}

.feature-item span {
  font-size: 14px;
  color: var(--dark-text-secondary);
}

/* ==================== 右侧注册区 ==================== */
.register-section {
  flex: 0 0 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
  background: var(--dark-bg-secondary);
  position: relative;
}

.register-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at top left, rgba(0, 102, 255, 0.05) 0%, transparent 50%);
  pointer-events: none;
}

/* 玻璃拟态卡片 */
.glass-card {
  width: 100%;
  max-width: 400px;
  padding: 48px 40px;
  background: rgba(22, 27, 34, 0.8);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  animation: fadeInUp 0.8s ease-out 0.3s backwards;
}

.register-header {
  text-align: center;
  margin-bottom: 40px;
}

.register-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: var(--dark-text-primary);
  margin-bottom: 8px;
}

.register-header p {
  font-size: 14px;
  color: var(--dark-text-tertiary);
  margin: 0;
}

/* 注册表单 */
.register-form {
  margin-bottom: 32px;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  max-width: 360px;
  margin: 0 auto 20px;
}

.input-icon {
  position: absolute;
  left: 16px;
  color: var(--dark-text-tertiary);
  font-size: 18px;
  z-index: 1;
}

.custom-input {
  width: 100%;
}

:deep(.custom-input .el-input__wrapper) {
  padding-left: 48px;
  background: rgba(33, 38, 45, 0.8);
  border: 1px solid var(--dark-border-color);
  border-radius: 12px;
  box-shadow: none;
  transition: all var(--transition-base);
  width: 100% !important;
  max-width: none !important;
}

:deep(.custom-input .el-input__wrapper:hover) {
  border-color: var(--brand-primary);
  background: rgba(33, 38, 45, 0.9);
}

:deep(.custom-input .el-input__wrapper.is-focus) {
  border-color: var(--brand-primary);
  background: rgba(33, 38, 45, 0.9);
  box-shadow: 0 0 0 3px rgba(0, 102, 255, 0.1);
}

:deep(.custom-input .el-input__inner) {
  color: var(--dark-text-primary);
  font-size: 15px;
  height: 48px;
}

:deep(.custom-input .el-input__inner::placeholder) {
  color: var(--dark-text-tertiary);
}

/* 注册按钮 */
.register-button {
  width: 100%;
  max-width: 360px;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  background: var(--gradient-brand);
  border: none;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 102, 255, 0.3);
  transition: all var(--transition-base);
  margin: 0 auto;
  display: block;
  padding: 0 20px;
}

.register-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 102, 255, 0.4);
}

.register-button:active:not(:disabled) {
  transform: translateY(0);
}

/* 注册页脚 */
.register-footer {
  text-align: center;
  font-size: 14px;
  color: var(--dark-text-tertiary);
}

.login-link {
  color: var(--brand-primary);
  font-weight: 600;
  text-decoration: none;
  transition: all var(--transition-fast);
  margin-left: 4px;
}

.login-link:hover {
  color: var(--brand-primary-hover);
  text-decoration: underline;
}

/* ==================== 响应式设计 ==================== */
@media (max-width: 1024px) {
  .visual-section {
    flex: 1;
  }

  .register-section {
    flex: 0 0 400px;
  }

  .brand-intro h1 {
    font-size: 28px;
  }

  .subtitle {
    font-size: 16px;
  }

  .features {
    gap: 16px;
  }

  .feature-item {
    padding: 12px 16px;
  }

  .feature-item span {
    font-size: 12px;
  }
}

@media (max-width: 768px) {
  .register-container {
    flex-direction: column;
  }

  .visual-section {
    flex: 0 0 40vh;
  }

  .register-section {
    flex: 1;
    padding: 24px;
  }

  .glass-card {
    padding: 32px 24px;
    max-width: 100%;
  }

  .code-card {
    display: none;
  }

  .features {
    gap: 12px;
  }

  .feature-item {
    padding: 10px 12px;
  }
}

@media (max-width: 480px) {
  .register-section {
    padding: 16px;
  }

  .glass-card {
    padding: 24px 20px;
  }

  .brand-intro h1 {
    font-size: 24px;
  }

  .subtitle {
    font-size: 14px;
  }

  .features {
    flex-direction: column;
    gap: 8px;
  }

  .feature-item {
    flex-direction: row;
    justify-content: center;
    padding: 12px;
  }
}
</style>
