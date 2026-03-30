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
    path: '/paths',
    redirect: '/learn'
  },
  {
    path: '/path/:id',
    redirect: (to) => `/learn/path/${to.params.id}`
  },
  {
    path: '/editor',
    redirect: '/problem/1'
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { requiresAuth: false, hideHeader: true }
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard/learn',
        redirect: '/learn'
      },
      {
        path: 'dashboard/learn/path/:id',
        redirect: (to) => `/learn/path/${to.params.id}`
      },
      {
        path: 'dashboard/learn/path/:pathId/level/:levelId',
        redirect: (to) => `/learn/path/${to.params.pathId}/level/${to.params.levelId}`
      },
      {
        path: 'dashboard/problem/:id',
        redirect: (to) => `/problem/${to.params.id}`
      },
      {
        path: 'learning-path/:id',
        redirect: (to) => `/learn/path/${to.params.id}`
      },
      {
        path: 'knowledge-graph',
        redirect: '/learn/knowledge-graph'
      },
      {
        path: 'code-run/:id',
        redirect: (to) => `/problem/${to.params.id}`
      },
      {
        path: 'submissions/:id',
        redirect: (to) => ({
          path: '/submissions',
          query: { submitId: to.params.id }
        })
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
        component: () => import('@/views/profile/Index.vue'),
        meta: { title: '个人主页' },
        children: [
          {
            path: '',
            name: 'UserProfile',
            component: () => import('@/views/profile/Overview.vue'),
            meta: { title: '个人主页' }
          },
          {
            path: 'analysis',
            name: 'ProfileAnalysis',
            component: () => import('@/views/profile/LearningAnalysis.vue'),
            meta: { title: '学习分析' }
          },
          {
            path: 'submissions',
            name: 'ProfileSubmissions',
            component: () => import('@/views/profile/Submissions.vue'),
            meta: { title: '提交记录' }
          },
          {
            path: 'collections',
            name: 'ProfileCollections',
            component: () => import('@/views/profile/Collections.vue'),
            meta: { title: '收藏与扩展' }
          },
          {
            path: 'settings',
            name: 'ProfileSettings',
            component: () => import('@/views/profile/Settings.vue'),
            meta: { title: '资料设置' }
          }
        ]
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

router.beforeEach(async (to) => {
  const userStore = useUserStore()
  const token = userStore.token

  if (token && !userStore.userInfo) {
    try {
      await userStore.fetchUserInfo()
    } catch (error) {
      console.error('Failed to hydrate user info before navigation:', error)
    }
  }

  if (to.meta.requiresAuth && !token) {
    return '/login'
  }

  if (to.meta.requiresAdmin && Number(userStore.userInfo?.role) !== 1) {
    return '/'
  }

  if ((to.path === '/login' || to.path === '/register') && token) {
    return '/learn'
  }

  return true
})

export default router
