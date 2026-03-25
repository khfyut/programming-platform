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
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '首页' }
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
        meta: { title: '题目详情', hideHeader: true }
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
        meta: { title: '学习中心' }
      },
      {
        path: 'learn/path/:id',
        name: 'LearningPath',
        component: () => import('@/views/LearningPath.vue'),
        meta: { title: '学习路径' }
      },
      {
        path: 'learn/path/:pathId/level/:levelId',
        name: 'LevelDetail',
        component: () => import('@/views/LevelDetail.vue'),
        meta: { title: '关卡详情' }
      },
      {
        path: 'learn/assessment',
        name: 'Assessment',
        component: () => import('@/views/Assessment.vue'),
        meta: { title: '能力测评' }
      },
      {
        path: 'learn/knowledge-graph',
        name: 'KnowledgeGraph',
        component: () => import('@/views/KnowledgeGraph.vue'),
        meta: { title: '知识图谱' }
      },
      {
        path: 'wrong-book',
        name: 'WrongBook',
        component: () => import('@/views/WrongBook.vue'),
        meta: { title: '错题本' }
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
      },
      {
        path: 'community',
        name: 'Community',
        component: () => import('@/views/Community.vue'),
        meta: { title: '学习社区' }
      },
      {
        path: 'community/post/:id',
        name: 'CommunityPost',
        component: () => import('@/views/CommunityPost.vue'),
        meta: { title: '帖子详情' }
      },
      {
        path: 'profile',
        name: 'UserProfile',
        component: () => import('@/views/UserProfile.vue'),
        meta: { title: '个人主页' }
      },
      {
        path: 'profile/:userId',
        name: 'UserProfileById',
        component: () => import('@/views/UserProfile.vue'),
        meta: { title: '用户主页' }
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
