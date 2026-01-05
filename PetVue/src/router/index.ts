import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

// 扩展路由元信息类型
declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    guest?: boolean
    requiresAuth?: boolean
    roles?: string[]
  }
}

const routes: RouteRecordRaw[] = [
  // 公共路由
  {
    path: '/',
    name: 'home',
    component: () => import('@/views/HomeView.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { title: '登录', guest: true }
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/auth/RegisterView.vue'),
    meta: { title: '注册', guest: true }
  },

  // 机构相关路由
  {
    path: '/institutions',
    name: 'institutionList',
    component: () => import('@/views/institution/InstitutionListView.vue'),
    meta: { title: '寄养机构' }
  },
  {
    path: '/institutions/:id',
    name: 'institutionDetail',
    component: () => import('@/views/institution/InstitutionDetailView.vue'),
    meta: { title: '机构详情' }
  },

  // 预约相关路由（需要登录）
  {
    path: '/booking/create/:institutionId',
    name: 'bookingCreate',
    component: () => import('@/views/booking/BookingCreateView.vue'),
    meta: { title: '创建预约', requiresAuth: true }
  },
  {
    path: '/booking/payment/:id',
    name: 'bookingPayment',
    component: () => import('@/views/booking/PaymentView.vue'),
    meta: { title: '支付订单', requiresAuth: true }
  },
  {
    path: '/booking/result/:id',
    name: 'paymentResult',
    component: () => import('@/views/booking/PaymentResultView.vue'),
    meta: { title: '支付结果', requiresAuth: true }
  },

  // 用户中心路由（需要登录）
  {
    path: '/user',
    name: 'userCenter',
    redirect: '/user/orders',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'profile',
        name: 'userProfile',
        component: () => import('@/views/user/ProfileView.vue'),
        meta: { title: '个人资料', requiresAuth: true }
      },
      {
        path: 'orders',
        name: 'userOrders',
        component: () => import('@/views/booking/OrderListView.vue'),
        meta: { title: '我的订单', requiresAuth: true }
      },
      {
        path: 'favorites',
        name: 'userFavorites',
        component: () => import('@/views/user/FavoritesView.vue'),
        meta: { title: '我的收藏', requiresAuth: true }
      },
      {
        path: 'history',
        name: 'browsingHistory',
        component: () => import('@/views/user/BrowsingHistoryView.vue'),
        meta: { title: '浏览历史', requiresAuth: true }
      }
    ]
  },

  // 订单详情（独立路由）
  {
    path: '/order/:id',
    name: 'orderDetail',
    component: () => import('@/views/booking/OrderDetailView.vue'),
    meta: { title: '订单详情', requiresAuth: true }
  },

  // 通知中心
  {
    path: '/notifications',
    name: 'notifications',
    component: () => import('@/views/notification/NotificationCenterView.vue'),
    meta: { title: '消息中心', requiresAuth: true }
  },

  // 帮助中心
  {
    path: '/help',
    name: 'helpCenter',
    component: () => import('@/views/support/HelpCenterView.vue'),
    meta: { title: '帮助中心' }
  },

  // 欢迎引导页
  {
    path: '/welcome',
    name: 'welcome',
    component: () => import('@/views/onboarding/WelcomeView.vue'),
    meta: { title: '欢迎', guest: true }
  },

  // 地图搜索
  {
    path: '/map',
    name: 'mapSearch',
    component: () => import('@/views/map/MapSearchView.vue'),
    meta: { title: '地图找店' }
  },

  // 客服支持路由
  {
    path: '/support/ticket',
    name: 'ticketSubmit',
    component: () => import('@/views/support/TicketSubmitView.vue'),
    meta: { title: '提交工单', requiresAuth: true }
  },
  {
    path: '/support/my-tickets',
    name: 'myTickets',
    component: () => import('@/views/support/MyTicketsView.vue'),
    meta: { title: '我的工单', requiresAuth: true }
  },
  {
    path: '/support/complaint',
    name: 'complaintForm',
    component: () => import('@/views/support/ComplaintFormView.vue'),
    meta: { title: '投诉建议', requiresAuth: true }
  },

  // 机构管理端路由
  {
    path: '/institution/dashboard',
    name: 'institutionDashboard',
    component: () => import('@/views/institution/DashboardView.vue'),
    meta: { title: '机构仪表盘', requiresAuth: true, roles: ['institution_staff'] }
  },
  {
    path: '/institution/services',
    name: 'institutionServices',
    component: () => import('@/views/institution/ServiceManageView.vue'),
    meta: { title: '套餐管理', requiresAuth: true, roles: ['institution_staff'] }
  },
  {
    path: '/institution/orders',
    name: 'institutionOrders',
    component: () => import('@/views/institution/OrderManageView.vue'),
    meta: { title: '订单管理', requiresAuth: true, roles: ['institution_staff'] }
  },
  {
    path: '/institution/orders/new',
    name: 'institutionOrderNew',
    component: () => import('@/views/institution/OrderCreateView.vue'),
    meta: { title: '新增订单', requiresAuth: true, roles: ['institution_staff'] }
  },
  {
    path: '/institution/orders/:id',
    name: 'institutionOrderDetail',
    component: () => import('@/views/institution/OrderDetailView.vue'),
    meta: { title: '订单详情', requiresAuth: true, roles: ['institution_staff'] }
  },
  {
    path: '/institution/customers',
    name: 'institutionCustomers',
    component: () => import('@/views/institution/CustomerListView.vue'),
    meta: { title: '客户管理', requiresAuth: true, roles: ['institution_staff'] }
  },
  {
    path: '/institution/health-records',
    name: 'institutionHealthRecords',
    component: () => import('@/views/institution/HealthRecordView.vue'),
    meta: { title: '健康记录', requiresAuth: true, roles: ['institution_staff'] }
  },
  {
    path: '/institution/health',
    name: 'institutionHealth',
    component: () => import('@/views/institution/HealthRecordView.vue'),
    meta: { title: '健康记录', requiresAuth: true, roles: ['institution_staff'] }
  },
  {
    path: '/institution/health/:orderId',
    name: 'institutionHealthDetail',
    component: () => import('@/views/institution/HealthRecordView.vue'),
    meta: { title: '健康记录', requiresAuth: true, roles: ['institution_staff'] }
  },
  {
    path: '/institution/reviews',
    name: 'institutionReviews',
    component: () => import('@/views/institution/ReviewManageView.vue'),
    meta: { title: '评价管理', requiresAuth: true, roles: ['institution_staff'] }
  },
  {
    path: '/institution/profile',
    name: 'institutionProfile',
    component: () => import('@/views/institution/InstitutionSettingsView.vue'),
    meta: { title: '机构资料', requiresAuth: true, roles: ['institution_staff'] }
  },
  {
    path: '/institution/settings',
    name: 'institutionSettings',
    component: () => import('@/views/institution/InstitutionSettingsView.vue'),
    meta: { title: '机构资料', requiresAuth: true, roles: ['institution_staff'] }
  },
  {
    path: '/institution/complaints',
    name: 'institutionComplaints',
    component: () => import('@/views/institution/ComplaintManageView.vue'),
    meta: { title: '投诉管理', requiresAuth: true, roles: ['institution_staff'] }
  },
  {
    path: '/institution/notifications',
    name: 'institutionNotifications',
    component: () => import('@/views/institution/NotificationView.vue'),
    meta: { title: '通知中心', requiresAuth: true, roles: ['institution_staff'] }
  },
  {
    path: '/institution/reports',
    name: 'institutionReports',
    component: () => import('@/views/institution/ReportView.vue'),
    meta: { title: '数据报表', requiresAuth: true, roles: ['institution_staff'] }
  },
  {
    path: '/institution/messages',
    name: 'institutionMessages',
    component: () => import('@/views/institution/MessageView.vue'),
    meta: { title: '消息中心', requiresAuth: true, roles: ['institution_staff'] }
  },

  // 管理员路由
  {
    path: '/admin/dashboard',
    name: 'adminDashboard',
    component: () => import('@/views/admin/DashboardView.vue'),
    meta: { title: '管理后台', requiresAuth: true, roles: ['admin'] }
  },
  {
    path: '/admin/analytics',
    name: 'adminAnalytics',
    component: () => import('@/views/admin/AnalyticsView.vue'),
    meta: { title: '数据分析', requiresAuth: true, roles: ['admin'] }
  },
  {
    path: '/admin/institutions',
    name: 'adminInstitutions',
    component: () => import('@/views/admin/InstitutionReviewView.vue'),
    meta: { title: '机构审核', requiresAuth: true, roles: ['admin'] }
  },
  {
    path: '/admin/institutions/review',
    name: 'adminInstitutionsReview',
    component: () => import('@/views/admin/InstitutionReviewView.vue'),
    meta: { title: '机构审核', requiresAuth: true, roles: ['admin'] }
  },
  {
    path: '/admin/institutions/qualification',
    name: 'adminInstitutionsQualification',
    component: () => import('@/views/admin/QualificationReviewView.vue'),
    meta: { title: '资质审核', requiresAuth: true, roles: ['admin'] }
  },
  {
    path: '/admin/institutions/:id',
    name: 'adminInstitutionDetail',
    component: () => import('@/views/admin/InstitutionReviewView.vue'),
    meta: { title: '机构详情', requiresAuth: true, roles: ['admin'] }
  },
  {
    path: '/admin/complaints',
    name: 'adminComplaints',
    component: () => import('@/views/admin/ComplaintManageView.vue'),
    meta: { title: '投诉管理', requiresAuth: true, roles: ['admin'] }
  },
  {
    path: '/admin/users',
    name: 'adminUsers',
    component: () => import('@/views/admin/UserManageView.vue'),
    meta: { title: '用户管理', requiresAuth: true, roles: ['admin'] }
  },
  {
    path: '/admin/orders',
    name: 'adminOrders',
    component: () => import('@/views/admin/OrderManageView.vue'),
    meta: { title: '订单管理', requiresAuth: true, roles: ['admin'] }
  },
  {
    path: '/admin/finance',
    name: 'adminFinance',
    component: () => import('@/views/admin/FinanceView.vue'),
    meta: { title: '财务管理', requiresAuth: true, roles: ['admin'] }
  },
  {
    path: '/admin/refunds',
    name: 'adminRefunds',
    component: () => import('@/views/admin/RefundManageView.vue'),
    meta: { title: '退款管理', requiresAuth: true, roles: ['admin'] }
  },
  {
    path: '/admin/qualifications',
    name: 'adminQualifications',
    component: () => import('@/views/admin/QualificationReviewView.vue'),
    meta: { title: '资质审核', requiresAuth: true, roles: ['admin'] }
  },
  {
    path: '/admin/settings',
    name: 'adminSettings',
    component: () => import('@/views/admin/SystemSettingsView.vue'),
    meta: { title: '系统设置', requiresAuth: true, roles: ['admin'] }
  },

  // 用户设置
  {
    path: '/settings',
    name: 'settings',
    component: () => import('@/views/settings/NotificationSettingsView.vue'),
    meta: { title: '设置', requiresAuth: true }
  },

  // 钱包相关路由
  {
    path: '/wallet',
    name: 'wallet',
    component: () => import('@/views/wallet/WalletView.vue'),
    meta: { title: '我的钱包', requiresAuth: true }
  },
  {
    path: '/wallet/recharge',
    name: 'walletRecharge',
    component: () => import('@/views/wallet/RechargeView.vue'),
    meta: { title: '充值', requiresAuth: true }
  },
  {
    path: '/wallet/withdraw',
    name: 'walletWithdraw',
    component: () => import('@/views/wallet/WithdrawView.vue'),
    meta: { title: '提现', requiresAuth: true }
  },
  {
    path: '/wallet/transactions',
    name: 'walletTransactions',
    component: () => import('@/views/wallet/TransactionListView.vue'),
    meta: { title: '交易记录', requiresAuth: true }
  },
  {
    path: '/wallet/accounts',
    name: 'walletAccounts',
    component: () => import('@/views/wallet/WithdrawalAccountsView.vue'),
    meta: { title: '提现账户', requiresAuth: true }
  },

  // AI 助手
  {
    path: '/ai/chat',
    name: 'aiChat',
    component: () => import('@/views/ai/AiChatView.vue'),
    meta: { title: 'AI 宠物助手' }
  },
  {
    path: '/ai/history',
    name: 'aiHistory',
    component: () => import('@/views/ai/ConversationHistoryView.vue'),
    meta: { title: 'AI 对话历史', requiresAuth: true }
  },

  // 404 页面
  {
    path: '/:pathMatch(.*)*',
    name: 'notFound',
    component: () => import('@/views/NotFoundView.vue'),
    meta: { title: '页面未找到' }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(_to, _from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    return { top: 0 }
  }
})

// 导航守卫
router.beforeEach((to, _from, next) => {
  // 更新页面标题
  document.title = `${to.meta.title || '宠物寄养平台'} - Pet Foster`

  // 检查是否需要认证（使用 sessionStorage，与 auth store 保持一致）
  const token = sessionStorage.getItem('token')
  const isAuthenticated = !!token

  if (to.meta.requiresAuth && !isAuthenticated) {
    // 需要登录但未登录，跳转到登录页
    next({ name: 'login', query: { redirect: to.fullPath } })
    return
  }

  // 注释掉这个逻辑，允许已登录用户访问登录/注册页面（方便切换账号）
  // if (to.meta.guest && isAuthenticated) {
  //   next({ name: 'home' })
  //   return
  // }

  next()
})

export default router
