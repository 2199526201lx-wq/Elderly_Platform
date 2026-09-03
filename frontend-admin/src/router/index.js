import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  // ========== 自动登录（从统一登录页跳转过来） ==========
  { path: '/auto-login', name: 'AutoLogin', component: { template: '<div></div>' }, meta: { public: true },
    beforeEnter(to, from, next) {
      const userStore = useUserStore()
      const { accessToken, refreshToken, role, userId, realName } = to.query
      if (accessToken && refreshToken) {
        userStore.setTokens(accessToken, refreshToken)
        userStore.setUserInfo({ id: Number(userId), realName: realName || '', role: role || '' })
        next({ name: 'Dashboard', replace: true })
      } else {
        next({ name: 'RoleSelect' })
      }
    }
  },

  // ========== 管理端独立入口：根路径直接进管理端登录 ==========
  { path: '/', redirect: '/admin/login' },
  // /login 统一指向管理端登录：避免 5174 端口访问 /login 出现空白页，导致无法登录管理端
  { path: '/login', redirect: '/admin/login' },

  // ========== 管理端 ==========
  { path: '/admin/login', name: 'AdminLogin', component: () => import('../views/admin/Login.vue'), meta: { public: true } },
  {
    path: '/admin',
    component: () => import('../layouts/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/dashboard/Index.vue') },
      { path: 'members', name: 'Members', component: () => import('../views/members/Index.vue') },
      { path: 'members/:id', name: 'MemberDetail', component: () => import('../views/members/Detail.vue') },
      { path: 'health-record', name: 'HealthRecord', component: () => import('../views/health-record/Index.vue') },
      { path: 'appointment/packages', name: 'Packages', component: () => import('../views/appointment/Packages.vue') },
      { path: 'appointment/slots', name: 'Slots', component: () => import('../views/appointment/Slots.vue') },
      { path: 'appointment/list', name: 'AppointmentList', component: () => import('../views/appointment/List.vue') },
      { path: 'assessment', name: 'Assessment', component: () => import('../views/assessment/Index.vue') },
      { path: 'assessment/:id/questions', name: 'Questions', component: () => import('../views/assessment/Questions.vue') },
      { path: 'activity', name: 'Activity', component: () => import('../views/activity/Index.vue') },
      { path: 'message', name: 'Message', component: () => import('../views/message/Index.vue') },
      { path: 'config', name: 'Config', component: () => import('../views/config/Index.vue') },
    ],
  },

  // ========== 会员端 ==========
  { path: '/member/login', name: 'MemberLogin', component: () => import('../views/member/auth/Login.vue'), meta: { public: true } },
  { path: '/member/register', name: 'MemberRegister', component: () => import('../views/member/auth/Register.vue'), meta: { public: true } },
  { path: '/member/forgot-password', name: 'MemberForgotPassword', component: () => import('../views/member/auth/ForgotPassword.vue'), meta: { public: true } },
  {
    path: '/member',
    component: () => import('../views/member/components/LayoutWithTabBar.vue'),
    children: [
      { path: 'home', name: 'MemberHome', component: () => import('../views/member/home/index.vue') },
      { path: 'message', name: 'MemberMessage', component: () => import('../views/member/message/List.vue') },
      { path: 'message/:id', name: 'MemberMessageDetail', component: () => import('../views/member/message/Detail.vue') },
      { path: 'profile', name: 'MemberProfile', component: () => import('../views/member/profile/Index.vue') },
      { path: 'profile/edit', name: 'MemberProfileEdit', component: () => import('../views/member/profile/Edit.vue') },
      { path: 'profile/settings', name: 'MemberSettings', component: () => import('../views/member/profile/Settings.vue') },
      { path: 'profile/password', name: 'MemberChangePassword', component: () => import('../views/member/profile/ChangePassword.vue') },
      // 健康模块
      { path: 'health/record', name: 'HealthRecordInput', component: () => import('../views/member/health/Record.vue') },
      { path: 'health/history', name: 'HealthHistory', component: () => import('../views/member/health/History.vue') },
      { path: 'health/trend', name: 'HealthTrend', component: () => import('../views/member/health/Trend.vue') },
      { path: 'health/assessment', name: 'AssessmentList', component: () => import('../views/member/health/AssessmentList.vue') },
      { path: 'health/assessment/result-history', name: 'AssessmentResultHistory', component: () => import('../views/member/health/AssessmentResultHistory.vue') },
      { path: 'health/assessment/:id', name: 'AssessmentForm', component: () => import('../views/member/health/AssessmentForm.vue') },
      { path: 'health/assessment/result/:id', name: 'AssessmentResult', component: () => import('../views/member/health/AssessmentResult.vue') },
      { path: 'health/chat', name: 'Chat', component: () => import('../views/member/health/Chat.vue') },
      { path: 'health/guidance', name: 'Guidance', component: () => import('../views/member/health/Guidance.vue') },
      // 预约模块
      { path: 'appointment/packages', name: 'MemberPackages', component: () => import('../views/member/appointment/Packages.vue') },
      { path: 'appointment/book/:packageId', name: 'MemberBook', component: () => import('../views/member/appointment/Book.vue') },
      { path: 'appointment/list', name: 'MemberAppointments', component: () => import('../views/member/appointment/MyAppointments.vue') },
      // 活动模块
      { path: 'activity/list', name: 'MemberActivityList', component: () => import('../views/member/activity/List.vue') },
      { path: 'activity/:id', name: 'MemberActivityDetail', component: () => import('../views/member/activity/Detail.vue') },
      // 积分
      { path: 'points', name: 'MemberPoints', component: () => import('../views/member/points/Index.vue') },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const role = userStore.userInfo?.role
  if (to.meta.public) {
    // 公开页面：直接放行
    next()
  } else {
    // 非公开页面：需要登录
    if (!userStore.isLoggedIn) {
      next({ name: 'RoleSelect' })
    } else if (to.path.startsWith('/admin') && role !== 'ADMIN') {
      next({ name: 'MemberHome' })
    } else if (to.path.startsWith('/member') && role !== 'MEMBER') {
      next({ name: 'Dashboard' })
    } else {
      next()
    }
  }
})

export default router
