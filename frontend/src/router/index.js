import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/problems'
      },
      {
        path: 'problems',
        name: 'Problems',
        component: () => import('@/views/Problems.vue'),
        meta: { title: '题库' }
      },
      {
        path: 'problem/:id',
        name: 'ProblemDetail',
        component: () => import('@/views/ProblemDetail.vue'),
        meta: { title: '题目详情' }
      },
      {
        path: 'code-run',
        name: 'CodeRun',
        component: () => import('@/views/CodeRun.vue'),
        meta: { title: '在线运行' }
      },
      {
        path: 'ai',
        name: 'AI',
        component: () => import('@/views/AI.vue'),
        meta: { title: 'AI 答疑' }
      },
      {
        path: 'learn',
        name: 'Learn',
        component: () => import('@/views/Learn.vue'),
        meta: { title: '学习记录' }
      },
      {
        path: 'submissions',
        name: 'Submissions',
        component: () => import('@/views/Submissions.vue'),
        meta: { title: '提交记录' }
      },
      {
        path: 'admin',
        name: 'Admin',
        component: () => import('@/views/Admin.vue'),
        meta: { title: '管理后台', requiresAdmin: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const token = userStore.token

  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.meta.requiresAdmin && userStore.userInfo?.role !== 1) {
    next('/')
  } else if ((to.path === '/login' || to.path === '/register') && token) {
    next('/')
  } else {
    next()
  }
})

export default router
