import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  // ========== 统一登录入口 ==========
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'Login', component: () => import('../views/auth/Login.vue'), meta: { public: true } },
  { path: '/register', name: 'Register', component: () => import('../views/auth/Register.vue'), meta: { public: true } },
  { path: '/forgot-password', name: 'ForgotPassword', component: () => import('../views/auth/ForgotPassword.vue'), meta: { public: true } },

  // ========== 主页面（需要 TabBar） ==========
  {
    path: '/member',
    component: () => import('../components/LayoutWithTabBar.vue'),
    children: [
      { path: 'home', name: 'Home', component: () => import('../views/home/index.vue') },
      { path: 'message', name: 'MessageList', component: () => import('../views/message/List.vue') },
      { path: 'message/:id', name: 'MessageDetail', component: () => import('../views/message/Detail.vue') },
      { path: 'profile', name: 'Profile', component: () => import('../views/profile/Index.vue') },
    ],
  },

  // ========== 健康模块 ==========
  { path: '/member/health/record', name: 'HealthRecord', component: () => import('../views/health/Record.vue') },
  { path: '/member/health/history', name: 'HealthHistory', component: () => import('../views/health/History.vue') },
  { path: '/member/health/trend', name: 'HealthTrend', component: () => import('../views/health/Trend.vue') },
  { path: '/member/health/assessment', name: 'AssessmentList', component: () => import('../views/health/AssessmentList.vue') },
  { path: '/member/health/assessment/result-history', name: 'AssessmentResultHistory', component: () => import('../views/health/AssessmentResultHistory.vue') },
  { path: '/member/health/assessment/:id', name: 'AssessmentForm', component: () => import('../views/health/AssessmentForm.vue') },
  { path: '/member/health/assessment/result/:id', name: 'AssessmentResult', component: () => import('../views/health/AssessmentResult.vue') },
  { path: '/member/health/chat', name: 'Chat', component: () => import('../views/health/Chat.vue') },
  { path: '/member/health/guidance', name: 'Guidance', component: () => import('../views/health/Guidance.vue') },

  // ========== 体检预约 ==========
  { path: '/member/appointment/packages', name: 'Packages', component: () => import('../views/appointment/Packages.vue') },
  { path: '/member/appointment/book/:packageId', name: 'Book', component: () => import('../views/appointment/Book.vue') },
  { path: '/member/appointment/list', name: 'MyAppointments', component: () => import('../views/appointment/MyAppointments.vue') },

  // ========== 社区活动 ==========
  { path: '/member/activity/list', name: 'ActivityList', component: () => import('../views/activity/List.vue') },
  { path: '/member/activity/:id', name: 'ActivityDetail', component: () => import('../views/activity/Detail.vue') },

  // ========== 积分 ==========
  { path: '/member/points', name: 'Points', component: () => import('../views/points/Index.vue') },

  // ========== 个人中心 ==========
  { path: '/member/profile/edit', name: 'ProfileEdit', component: () => import('../views/profile/Edit.vue') },
  { path: '/member/profile/settings', name: 'Settings', component: () => import('../views/profile/Settings.vue') },
  { path: '/member/profile/password', name: 'ChangePassword', component: () => import('../views/profile/ChangePassword.vue') },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const role = userStore.userInfo?.role
  if (to.meta.public) {
    // 公开页面直接放行
    next()
  } else {
    // 需要登录：如果不是会员角色，清除残留数据并引导回登录页
    // （管理员账号不应在会员端停留，避免污染本地存储导致跳回管理端）
    if (!userStore.isLoggedIn || role !== 'MEMBER') {
      userStore.logout()
      next({ name: 'Login' })
    } else {
      next()
    }
  }
})

export default router