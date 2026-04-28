<template>
  <div class="login-container">
    <section class="visual-section">
      <div class="animated-grid"></div>

      <div class="floating-codes">
        <div class="code-card" style="top: 15%; left: 10%; animation-delay: 0s;">
          <pre><code>def fibonacci(n):
    if n &lt;= 1:
        return n
    return fibonacci(n - 1) + fibonacci(n - 2)</code></pre>
        </div>
        <div class="code-card" style="top: 45%; left: 20%; animation-delay: 1s;">
          <pre><code>public class Main {
    public static void main(String[] args) {
        System.out.println("Hello World");
    }
}</code></pre>
        </div>
        <div class="code-card" style="top: 70%; left: 8%; animation-delay: 2s;">
          <pre><code>const quickSort = (arr) =&gt; {
    if (arr.length &lt;= 1) return arr
    const pivot = arr[0]
    const left = arr.filter(x =&gt; x &lt; pivot)
    const right = arr.filter(x =&gt; x &gt; pivot)
    return [...quickSort(left), pivot, ...quickSort(right)]
}</code></pre>
        </div>
      </div>

      <div class="brand-intro">
        <div class="brand-logo">
          <el-icon :size="48"><Edit /></el-icon>
        </div>
        <h1>在线编程学习平台</h1>
        <p class="subtitle">提升编程技能，挑战算法难题</p>
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
    </section>

    <section class="login-section">
      <div class="glass-card">
        <div class="login-header">
          <h2>欢迎回来</h2>
          <p>登录您的账号继续学习</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" class="login-form">
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
                show-password
                @keyup.enter="handleLogin"
              />
            </div>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              class="login-button"
              @click="handleLogin"
            >
              <el-icon v-if="!loading"><Right /></el-icon>
              {{ loading ? '登录中...' : '登录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          <span>还没有账号？</span>
          <router-link to="/register" class="register-link">立即注册</router-link>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Edit, Right, User, Lock, Trophy, Monitor, ChatDotRound } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getSafeRedirectPath } from '@/utils/navigation'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!formRef.value) return

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const success = await userStore.login(form.username, form.password)
    if (success) {
      ElMessage.success('登录成功')
      router.replace(getSafeRedirectPath(route.query.redirect))
    } else {
      ElMessage.error('用户名或密码错误')
    }
  } catch (error) {
    ElMessage.error('登录失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  min-height: 100vh;
  background: var(--dark-bg-primary, #070b14);
  overflow: hidden;
}

.visual-section {
  position: relative;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background:
    radial-gradient(circle at top, rgba(0, 153, 255, 0.18), transparent 35%),
    linear-gradient(135deg, #06111f 0%, #0d1830 45%, #07121d 100%);
}

.animated-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(0, 153, 255, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 153, 255, 0.06) 1px, transparent 1px);
  background-size: 48px 48px;
  animation: gridMove 18s linear infinite;
}

.floating-codes {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.code-card {
  position: absolute;
  width: min(280px, 40vw);
  padding: 16px;
  border: 1px solid rgba(56, 189, 248, 0.22);
  border-radius: 16px;
  background: rgba(6, 17, 31, 0.72);
  backdrop-filter: blur(12px);
  box-shadow: 0 18px 36px rgba(0, 0, 0, 0.28);
  color: #c9ddff;
  font-family: var(--font-code, "Consolas", monospace);
  font-size: 12px;
  animation: float 6s ease-in-out infinite;
}

.code-card pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
}

.brand-intro {
  position: relative;
  z-index: 1;
  max-width: 540px;
  padding: 32px;
  text-align: center;
  color: #f8fbff;
}

.brand-logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 84px;
  height: 84px;
  margin-bottom: 24px;
  border-radius: 24px;
  background: linear-gradient(135deg, #0ea5e9 0%, #2563eb 100%);
  box-shadow: 0 20px 40px rgba(14, 165, 233, 0.28);
}

.brand-intro h1 {
  margin: 0 0 12px;
  font-size: 40px;
  font-weight: 700;
}

.subtitle {
  margin: 0 0 28px;
  color: rgba(226, 232, 240, 0.86);
  font-size: 18px;
}

.features {
  display: inline-flex;
  gap: 14px;
  flex-wrap: wrap;
  justify-content: center;
}

.feature-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.55);
  color: #dbeafe;
}

.login-section {
  display: flex;
  width: min(420px, 100%);
  align-items: center;
  justify-content: center;
  padding: 32px;
  background: linear-gradient(180deg, #f8fbff 0%, #eef4ff 100%);
}

.glass-card {
  width: 100%;
  padding: 36px 32px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.85);
  box-shadow: 0 24px 50px rgba(15, 23, 42, 0.14);
}

.login-header {
  margin-bottom: 28px;
  text-align: center;
}

.login-header h2 {
  margin: 0 0 8px;
  font-size: 30px;
  color: #0f172a;
}

.login-header p {
  margin: 0;
  color: #64748b;
}

.login-form {
  margin-bottom: 20px;
}

.input-wrapper {
  position: relative;
}

.input-icon {
  position: absolute;
  top: 50%;
  left: 14px;
  z-index: 1;
  transform: translateY(-50%);
  color: #64748b;
}

.custom-input :deep(.el-input__wrapper) {
  min-height: 48px;
  padding-left: 38px;
  border-radius: 14px;
  box-shadow: 0 0 0 1px rgba(148, 163, 184, 0.18) inset;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #2563eb inset;
}

.login-button {
  width: 100%;
  min-height: 48px;
  border: none;
  border-radius: 14px;
  background: linear-gradient(135deg, #0ea5e9 0%, #2563eb 100%);
  box-shadow: 0 16px 28px rgba(37, 99, 235, 0.24);
}

.login-footer {
  display: flex;
  justify-content: center;
  gap: 6px;
  color: #64748b;
}

.register-link {
  color: #2563eb;
  font-weight: 600;
  text-decoration: none;
}

@keyframes gridMove {
  from {
    background-position: 0 0;
  }
  to {
    background-position: 48px 48px;
  }
}

@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-18px);
  }
}

@media (max-width: 960px) {
  .login-container {
    flex-direction: column;
  }

  .visual-section {
    min-height: 40vh;
  }

  .login-section {
    width: 100%;
  }

  .brand-intro h1 {
    font-size: 32px;
  }
}
</style>
