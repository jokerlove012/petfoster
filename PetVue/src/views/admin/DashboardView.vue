<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  RefreshCw,
  Download,
  Settings,
  Bell,
  Clock,
  Users,
  Building2,
  ShoppingCart,
  DollarSign,
  TrendingUp,
  TrendingDown,
  AlertTriangle,
  CheckCircle,
  XCircle,
  Eye,
  FileText,
  Shield,
  Activity,
  BarChart3,
  PieChart as PieChartIcon
} from 'lucide-vue-next'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import LineChart from '@/components/charts/LineChart.vue'
import PieChart from '@/components/charts/PieChart.vue'
import BarChart from '@/components/charts/BarChart.vue'
import { exportToCSV, exportToPDF } from '@/utils/reportExporter'
import { adminApi } from '@/api/admin'

const router = useRouter()
const loading = ref(false)
const lastUpdated = ref(new Date().toLocaleTimeString())
const selectedPeriod = ref<'today' | 'week' | 'month' | 'year'>('month')
const showExportMenu = ref(false)
const autoRefreshEnabled = ref(true)
let refreshInterval: ReturnType<typeof setInterval> | null = null

// 统计数据
const stats = ref([
  { label: '总用户数', value: 0, icon: '👥', change: 0, trend: 'up', route: '/admin/users' },
  { label: '总机构数', value: 0, icon: '🏠', change: 0, trend: 'up', route: '/admin/institutions' },
  { label: '本月订单', value: 0, icon: '📋', change: 0, trend: 'up', route: '/admin/orders' },
  { label: '本月收入', value: 0, icon: '💰', change: 0, trend: 'up', route: '/admin/finance' }
])

// 详细统计
const detailedStats = ref({
  activeUsers: 0,
  newUsersToday: 0,
  activeInstitutions: 0,
  pendingInstitutions: 0,
  completedOrders: 0,
  cancelledOrders: 0,
  avgOrderValue: 0,
  platformFee: 0,
  satisfactionRate: 0,
  complaintRate: 0
})

// 收入趋势数据
const revenueTrend = ref<{name: string, value: number}[]>([])

// 用户增长趋势
const userGrowthTrend = ref<{name: string, value: number}[]>([])

// 订单分布数据
const orderDistribution = ref<{name: string, value: number}[]>([])

// 用户类型分布
const userTypeDistribution = ref<{name: string, value: number}[]>([])

// 机构排名数据
const institutionRanking = ref<any[]>([])

// 地区分布
const regionDistribution = ref<{name: string, value: number}[]>([])

// 最近订单
const recentOrders = ref<any[]>([])

// 待处理事项
const pendingTasks = ref([
  { id: '1', title: '机构入驻审核', count: 0, icon: '🏠', color: '#1890ff', route: '/admin/institutions/review', priority: 'high' },
  { id: '2', title: '投诉待处理', count: 0, icon: '⚠️', color: '#ff4d4f', route: '/admin/complaints', priority: 'urgent' },
  { id: '3', title: '资质更新审核', count: 0, icon: '📄', color: '#faad14', route: '/admin/institutions/qualification', priority: 'medium' },
  { id: '4', title: '退款申请', count: 0, icon: '💰', color: '#52c41a', route: '/admin/refunds', priority: 'high' }
])

// 待审核机构
const pendingInstitutions = ref<any[]>([])

// 最新投诉
const recentComplaints = ref<any[]>([])

// 系统通知
const systemNotifications = ref<any[]>([])

// 快捷操作
const quickActions = ref([
  { id: '1', title: '机构审核', icon: Building2, color: '#1890ff', route: '/admin/institutions/review' },
  { id: '2', title: '投诉处理', icon: AlertTriangle, color: '#ff4d4f', route: '/admin/complaints' },
  { id: '3', title: '数据分析', icon: BarChart3, color: '#52c41a', route: '/admin/analytics' },
  { id: '4', title: '系统设置', icon: Settings, color: '#722ed1', route: '/admin/settings' }
])

// 加载仪表盘数据
const loadDashboardData = async () => {
  loading.value = true
  try {
    const res = await adminApi.getDashboardStats()
    if (res.code === 200 && res.data) {
      const data = res.data
      stats.value[0].value = data.totalUsers || 0
      stats.value[1].value = data.totalInstitutions || 0
      stats.value[2].value = data.totalOrders || 0
      stats.value[3].value = data.totalRevenue || 0
      
      // 详细统计
      detailedStats.value = {
        activeUsers: data.activeUsers || 0,
        newUsersToday: data.newUsersToday || 0,
        activeInstitutions: data.totalInstitutions || 0,
        pendingInstitutions: data.pendingInstitutions || 0,
        completedOrders: data.completedOrders || 0,
        cancelledOrders: data.cancelledOrders || 0,
        avgOrderValue: data.avgOrderValue || 0,
        platformFee: 0,
        satisfactionRate: 95,
        complaintRate: 0.5
      }
      
      // 待处理事项
      pendingTasks.value[0].count = data.pendingInstitutions || 0
      
      // 图表数据
      if (data.revenueTrend && data.revenueTrend.length > 0) {
        revenueTrend.value = data.revenueTrend
      }
      if (data.userGrowthTrend && data.userGrowthTrend.length > 0) {
        userGrowthTrend.value = data.userGrowthTrend
      }
      if (data.orderDistribution && data.orderDistribution.length > 0) {
        orderDistribution.value = data.orderDistribution
      }
      if (data.institutionRanking && data.institutionRanking.length > 0) {
        institutionRanking.value = data.institutionRanking
      }
      if (data.recentOrders && data.recentOrders.length > 0) {
        recentOrders.value = data.recentOrders
      }
    }
    
    // 加载待审核机构
    const instRes = await adminApi.getInstitutions('pending', 1, 5)
    if (instRes.code === 200 && instRes.data) {
      pendingInstitutions.value = (instRes.data.list || []).map((inst: any) => ({
        id: inst.id,
        name: inst.name,
        type: '新入驻',
        applicant: inst.phone || '',
        phone: inst.phone || '',
        date: inst.createdAt ? new Date(inst.createdAt).toLocaleDateString() : '',
        status: inst.status
      }))
    }
    
    // 加载投诉数据
    const complaintRes = await adminApi.getComplaints('pending', 1, 5)
    if (complaintRes.code === 200 && complaintRes.data) {
      const complaintList = complaintRes.data.list || []
      recentComplaints.value = complaintList.map((c: any) => ({
        id: c.id,
        type: c.type || '服务投诉',
        user: c.userName || c.userId || '用户',
        status: c.status || 'pending',
        priority: c.status === 'pending' ? 'urgent' : 'high',
        createdAt: c.createdAt
      }))
      // 更新待处理投诉数量
      pendingTasks.value[1].count = complaintRes.data.total || complaintList.length
    }
    
    lastUpdated.value = new Date().toLocaleTimeString()
  } catch (error) {
    console.error('加载仪表盘数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 计算属性
const totalPending = computed(() => pendingTasks.value.reduce((sum, t) => sum + t.count, 0))
const unreadNotifications = computed(() => systemNotifications.value.filter(n => !n.read).length)
const completionRate = computed(() => {
  if (stats.value[2].value === 0) return 0
  return Math.round((detailedStats.value.completedOrders / stats.value[2].value) * 100)
})

// 格式化函数
const formatNumber = (value: number) => value.toLocaleString()
const formatCurrency = (value: number) => `¥${value.toLocaleString()}`
const formatPercent = (value: number) => `${value}%`

const getStatusText = (status: string) => {
  const map: Record<string, string> = { pending: '待确认', ongoing: '进行中', completed: '已完成', cancelled: '已取消', processing: '处理中' }
  return map[status] || status
}

const getStatusClass = (status: string) => `status-${status}`

const getPriorityClass = (priority: string) => {
  const map: Record<string, string> = { urgent: 'priority-urgent', high: 'priority-high', medium: 'priority-medium', low: 'priority-low' }
  return map[priority] || ''
}

const getPetIcon = (type: string) => type === 'dog' ? '🐕' : type === 'cat' ? '🐱' : '🐾'

// 刷新数据
const refreshData = async () => {
  await loadDashboardData()
  ElMessage.success('数据已刷新')
}

// 自动刷新
const toggleAutoRefresh = () => {
  autoRefreshEnabled.value = !autoRefreshEnabled.value
  if (autoRefreshEnabled.value) {
    startAutoRefresh()
    ElMessage.success('已开启自动刷新')
  } else {
    stopAutoRefresh()
    ElMessage.info('已关闭自动刷新')
  }
}

const startAutoRefresh = () => {
  if (refreshInterval) clearInterval(refreshInterval)
  refreshInterval = setInterval(() => {
    if (!loading.value) {
      lastUpdated.value = new Date().toLocaleTimeString()
    }
  }, 60000)
}

const stopAutoRefresh = () => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
    refreshInterval = null
  }
}

// 导出报表
const exportReport = async (format: 'csv' | 'pdf') => {
  showExportMenu.value = false
  ElMessage.info(`正在导出${format.toUpperCase()}报表...`)

  try {
    const reportData = {
      title: '平台运营报表',
      period: selectedPeriod.value,
      generatedAt: new Date().toISOString(),
      stats: stats.value,
      detailedStats: detailedStats.value,
      orders: recentOrders.value
    }

    if (format === 'csv') {
      await exportToCSV(reportData, `平台报表_${new Date().toLocaleDateString()}`)
    } else {
      await exportToPDF(reportData, `平台报表_${new Date().toLocaleDateString()}`)
    }
    ElMessage.success('报表导出成功')
  } catch (error) {
    ElMessage.error('导出失败，请重试')
  }
}

// 切换时间周期
const changePeriod = (period: typeof selectedPeriod.value) => {
  selectedPeriod.value = period
  refreshData()
}

// 跳转到统计详情
const goToStatDetail = (stat: typeof stats.value[0]) => {
  if (stat.route) {
    router.push(stat.route)
  }
}

// 跳转到任务
const goToTask = (task: typeof pendingTasks.value[0]) => {
  router.push(task.route)
}

// 快捷操作
const handleQuickAction = (action: typeof quickActions.value[0]) => {
  router.push(action.route)
}

// 审核机构
const reviewInstitution = async (institution: typeof pendingInstitutions.value[0], action: 'approve' | 'reject') => {
  const actionText = action === 'approve' ? '通过' : '拒绝'
  try {
    if (action === 'reject') {
      const { value: reason } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝申请', {
        confirmButtonText: '确认拒绝',
        cancelButtonText: '取消',
        inputPlaceholder: '请输入拒绝原因...',
        inputValidator: (val) => !!val || '请输入拒绝原因'
      })
    } else {
      await ElMessageBox.confirm(`确认${actionText}该机构申请？`, '审核确认')
    }

    institution.status = action === 'approve' ? 'approved' : 'rejected'
    ElMessage.success(`已${actionText}申请`)

    // 更新待处理数量
    const task = pendingTasks.value.find(t => t.id === '1')
    if (task && task.count > 0) task.count--
  } catch {
    // 用户取消
  }
}

// 处理投诉
const handleComplaint = (complaint: typeof recentComplaints.value[0]) => {
  router.push('/admin/complaints')
}

// 查看订单详情
const viewOrderDetail = (orderId: string) => {
  router.push(`/order/${orderId}`)
}

// 标记通知已读
const markNotificationRead = (notification: typeof systemNotifications.value[0]) => {
  notification.read = true
}

// 全部标记已读
const markAllNotificationsRead = () => {
  systemNotifications.value.forEach(n => n.read = true)
  ElMessage.success('已全部标记为已读')
}

onMounted(() => {
  loadDashboardData()
  if (autoRefreshEnabled.value) {
    startAutoRefresh()
  }
})

onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<template>
  <div class="admin-dashboard">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1>🛡️ 管理后台</h1>
        <p>平台数据概览与运营管理</p>
      </div>
      <div class="header-actions">
        <div class="period-selector">
          <button
            v-for="period in ['today', 'week', 'month', 'year']"
            :key="period"
            class="period-btn"
            :class="{ active: selectedPeriod === period }"
            @click="changePeriod(period as any)"
          >
            {{ { today: '今日', week: '本周', month: '本月', year: '本年' }[period] }}
          </button>
        </div>
        <span class="last-updated">
          <Clock :size="14" />
          {{ lastUpdated }}
        </span>
        <button
          class="icon-btn"
          :class="{ active: autoRefreshEnabled }"
          @click="toggleAutoRefresh"
          title="自动刷新"
        >
          <RefreshCw :size="18" :class="{ spinning: loading || autoRefreshEnabled }" />
        </button>
        <div class="export-dropdown">
          <button class="icon-btn" @click="showExportMenu = !showExportMenu">
            <Download :size="18" />
          </button>
          <div v-if="showExportMenu" class="dropdown-menu">
            <button @click="exportReport('csv')">📊 导出 CSV</button>
            <button @click="exportReport('pdf')">📄 导出 PDF</button>
          </div>
        </div>
        <div class="notification-btn" @click="markAllNotificationsRead">
          <Bell :size="18" />
          <span v-if="unreadNotifications" class="notification-badge">{{ unreadNotifications }}</span>
        </div>
        <router-link to="/admin/settings" class="icon-btn">
          <Settings :size="18" />
        </router-link>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <div
        v-for="action in quickActions"
        :key="action.id"
        class="quick-action-btn"
        @click="handleQuickAction(action)"
      >
        <component :is="action.icon" :size="20" :style="{ color: action.color }" />
        <span>{{ action.title }}</span>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div v-for="stat in stats" :key="stat.label" class="stat-card" @click="goToStatDetail(stat)">
        <div class="stat-icon">{{ stat.icon }}</div>
        <div class="stat-info">
          <span class="stat-value">{{ stat.label.includes('收入') ? formatCurrency(stat.value) : formatNumber(stat.value) }}</span>
          <span class="stat-label">{{ stat.label }}</span>
        </div>
        <span class="stat-change" :class="stat.trend">
          <TrendingUp v-if="stat.trend === 'up'" :size="14" />
          <TrendingDown v-else :size="14" />
          {{ stat.change }}%
        </span>
      </div>
    </div>

    <!-- 详细指标 -->
    <div class="metrics-grid">
      <div class="metric-item">
        <span class="metric-label">活跃用户</span>
        <span class="metric-value">{{ formatNumber(detailedStats.activeUsers) }}</span>
      </div>
      <div class="metric-item">
        <span class="metric-label">今日新增</span>
        <span class="metric-value highlight">+{{ detailedStats.newUsersToday }}</span>
      </div>
      <div class="metric-item">
        <span class="metric-label">订单完成率</span>
        <span class="metric-value">{{ completionRate }}%</span>
      </div>
      <div class="metric-item">
        <span class="metric-label">平均客单价</span>
        <span class="metric-value">¥{{ detailedStats.avgOrderValue }}</span>
      </div>
      <div class="metric-item">
        <span class="metric-label">满意度</span>
        <span class="metric-value success">{{ detailedStats.satisfactionRate }}%</span>
      </div>
      <div class="metric-item">
        <span class="metric-label">投诉率</span>
        <span class="metric-value" :class="{ warning: detailedStats.complaintRate > 1 }">{{ detailedStats.complaintRate }}%</span>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-grid">
      <div class="chart-card">
        <div class="card-header">
          <h3>📈 收入趋势</h3>
          <span class="chart-subtitle">近6个月</span>
        </div>
        <LineChart :data="revenueTrend" color="#FF7F6B" />
      </div>
      <div class="chart-card">
        <div class="card-header">
          <h3>👥 用户增长</h3>
          <span class="chart-subtitle">近6个月</span>
        </div>
        <LineChart :data="userGrowthTrend" color="#1890ff" />
      </div>
      <div class="chart-card">
        <div class="card-header">
          <h3>🍩 订单分布</h3>
          <span class="chart-subtitle">按服务类型</span>
        </div>
        <PieChart :data="orderDistribution" />
      </div>
    </div>

    <!-- 第二行图表 -->
    <div class="charts-grid secondary">
      <div class="chart-card">
        <div class="card-header">
          <h3>🏆 机构订单排名</h3>
          <span class="chart-subtitle">TOP 5</span>
        </div>
        <BarChart :data="institutionRanking.map(i => ({ name: i.name, value: i.value }))" color="#7DD3C0" :horizontal="true" />
      </div>
      <div class="chart-card">
        <div class="card-header">
          <h3>📍 地区分布</h3>
          <span class="chart-subtitle">机构数量</span>
        </div>
        <PieChart :data="regionDistribution" :colors="['#FF7F6B', '#7DD3C0', '#FFB366', '#A78BFA', '#60A5FA']" />
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="dashboard-grid">
      <!-- 左侧主内容 -->
      <div class="main-content">
        <!-- 订单表格 -->
        <div class="dashboard-card orders-card">
          <div class="card-header">
            <h3>📋 最近订单</h3>
            <router-link to="/admin/orders" class="view-all">查看全部 →</router-link>
          </div>
          <div class="orders-table">
            <table>
              <thead>
                <tr>
                  <th>订单号</th>
                  <th>用户</th>
                  <th>机构</th>
                  <th>宠物</th>
                  <th>金额</th>
                  <th>支付方式</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="order in recentOrders" :key="order.id">
                  <td class="order-id">{{ order.id }}</td>
                  <td>{{ order.user }}</td>
                  <td>{{ order.institution }}</td>
                  <td class="pet-cell">
                    <span class="pet-icon">{{ getPetIcon(order.petType) }}</span>
                    {{ order.pet }}
                  </td>
                  <td class="amount">¥{{ order.amount }}</td>
                  <td class="payment">{{ order.paymentMethod }}</td>
                  <td><span class="status-badge" :class="getStatusClass(order.status)">{{ getStatusText(order.status) }}</span></td>
                  <td>
                    <button class="action-link" @click="viewOrderDetail(order.id)">详情</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- 待审核机构 -->
        <div class="dashboard-card review-card">
          <div class="card-header">
            <h3>🏠 待审核机构</h3>
            <router-link to="/admin/institutions/review" class="view-all">查看全部 →</router-link>
          </div>
          <div class="review-list">
            <div v-for="inst in pendingInstitutions" :key="inst.id" class="review-item">
              <div class="review-info">
                <div class="review-main">
                  <span class="review-name">{{ inst.name }}</span>
                  <span class="review-type" :class="inst.type === '新入驻' ? 'new' : 'update'">{{ inst.type }}</span>
                </div>
                <div class="review-meta">
                  <span>{{ inst.applicant }}</span>
                  <span>{{ inst.phone }}</span>
                  <span>{{ inst.date }}</span>
                </div>
              </div>
              <div class="review-actions">
                <button class="btn-approve" @click="reviewInstitution(inst, 'approve')">
                  <CheckCircle :size="14" /> 通过
                </button>
                <button class="btn-reject" @click="reviewInstitution(inst, 'reject')">
                  <XCircle :size="14" /> 拒绝
                </button>
                <button class="btn-view" @click="router.push(`/admin/institutions/${inst.id}`)">
                  <Eye :size="14" /> 查看
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧栏 -->
      <div class="sidebar">
        <!-- 待处理事项 -->
        <div class="dashboard-card pending-card">
          <div class="card-header">
            <h3>⏳ 待处理事项</h3>
            <span class="badge">{{ totalPending }}</span>
          </div>
          <div class="pending-list">
            <div
              v-for="task in pendingTasks"
              :key="task.id"
              class="pending-item"
              :class="getPriorityClass(task.priority)"
              @click="goToTask(task)"
            >
              <div class="pending-left">
                <span class="pending-icon">{{ task.icon }}</span>
                <span class="pending-title">{{ task.title }}</span>
              </div>
              <span class="pending-count" :style="{ background: task.color }">{{ task.count }}</span>
            </div>
          </div>
        </div>

        <!-- 最新投诉 -->
        <div class="dashboard-card complaints-card">
          <div class="card-header">
            <h3>⚠️ 最新投诉</h3>
            <router-link to="/admin/complaints" class="view-all">查看全部 →</router-link>
          </div>
          <div class="complaints-list">
            <div
              v-for="complaint in recentComplaints"
              :key="complaint.id"
              class="complaint-item"
              :class="getPriorityClass(complaint.priority)"
              @click="handleComplaint(complaint)"
            >
              <div class="complaint-info">
                <span class="complaint-id">{{ complaint.id }}</span>
                <span class="complaint-type">{{ complaint.type }}</span>
              </div>
              <div class="complaint-meta">
                <span>{{ complaint.user }}</span>
                <span class="complaint-status" :class="complaint.status">{{ getStatusText(complaint.status) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 系统通知 -->
        <div class="dashboard-card notifications-card">
          <div class="card-header">
            <h3>🔔 系统通知</h3>
            <button class="text-btn" @click="markAllNotificationsRead">全部已读</button>
          </div>
          <div class="notifications-list">
            <div
              v-for="notification in systemNotifications"
              :key="notification.id"
              class="notification-item"
              :class="{ unread: !notification.read, [notification.type]: true }"
              @click="markNotificationRead(notification)"
            >
              <div class="notification-icon">
                <AlertTriangle v-if="notification.type === 'warning'" :size="16" />
                <CheckCircle v-else-if="notification.type === 'success'" :size="16" />
                <Activity v-else :size="16" />
              </div>
              <div class="notification-content">
                <span class="notification-title">{{ notification.title }}</span>
                <span class="notification-text">{{ notification.content }}</span>
              </div>
              <span class="notification-time">{{ notification.time }}</span>
            </div>
          </div>
        </div>

        <!-- 机构排名详情 -->
        <div class="dashboard-card ranking-card">
          <div class="card-header">
            <h3>🏆 机构排名</h3>
          </div>
          <div class="ranking-list">
            <div v-for="(inst, index) in institutionRanking" :key="inst.name" class="ranking-item">
              <span class="ranking-number" :class="{ top: index < 3 }">{{ index + 1 }}</span>
              <div class="ranking-info">
                <span class="ranking-name">{{ inst.name }}</span>
                <div class="ranking-stats">
                  <span>⭐ {{ inst.rating }}</span>
                  <span>📦 {{ inst.value }}单</span>
                  <span>💰 ¥{{ formatNumber(inst.revenue) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.admin-dashboard { max-width: 1500px; margin: 0 auto; padding: 24px; background: #FAFAF9; min-height: 100vh; }

.page-header {
  display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; flex-wrap: wrap; gap: 16px;
  .header-left {
    h1 { font-size: 26px; font-weight: 700; margin: 0 0 6px; color: #2D2A26; }
    p { color: #6B6560; margin: 0; font-size: 14px; }
  }
  .header-actions {
    display: flex; align-items: center; gap: 12px; flex-wrap: wrap;
    .last-updated {
      display: flex; align-items: center; gap: 6px;
      font-size: 12px; color: #9A958F;
      background: white; padding: 6px 12px; border-radius: 8px;
    }
  }
}

.period-selector {
  display: flex; background: white; border-radius: 10px; padding: 4px; box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  .period-btn {
    padding: 6px 14px; border: none; background: transparent; font-size: 13px;
    color: #6B6560; cursor: pointer; border-radius: 6px; transition: all 0.2s;
    &:hover { background: #F0F0EF; }
    &.active { background: #722ed1; color: white; }
  }
}

.icon-btn {
  width: 36px; height: 36px; border: none; background: white; border-radius: 10px;
  display: flex; align-items: center; justify-content: center; cursor: pointer;
  color: #6B6560; transition: all 0.2s; box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  &:hover { background: #F0F0EF; color: #2D2A26; }
  &.active { background: #F3EEFF; color: #722ed1; }
  .spinning { animation: spin 1s linear infinite; }
}

.export-dropdown {
  position: relative;
  .dropdown-menu {
    position: absolute; top: 100%; right: 0; margin-top: 8px;
    background: white; border-radius: 10px; box-shadow: 0 4px 20px rgba(0,0,0,0.12);
    padding: 8px; min-width: 140px; z-index: 100;
    button {
      display: block; width: 100%; padding: 10px 14px; border: none; background: none;
      text-align: left; font-size: 13px; color: #2D2A26; cursor: pointer; border-radius: 6px;
      &:hover { background: #F8F8F7; }
    }
  }
}

@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }

.notification-btn {
  position: relative; width: 36px; height: 36px; background: white; border-radius: 10px;
  display: flex; align-items: center; justify-content: center; cursor: pointer;
  color: #6B6560; box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  .notification-badge {
    position: absolute; top: -4px; right: -4px; min-width: 18px; height: 18px;
    background: #ff4d4f; color: white; font-size: 11px; font-weight: 700;
    border-radius: 9px; display: flex; align-items: center; justify-content: center;
  }
}

// 快捷操作
.quick-actions {
  display: flex; gap: 12px; margin-bottom: 24px; flex-wrap: wrap;
  .quick-action-btn {
    display: flex; align-items: center; gap: 10px; padding: 14px 20px;
    background: white; border-radius: 12px; cursor: pointer;
    box-shadow: 0 2px 8px rgba(0,0,0,0.04); transition: all 0.2s;
    span { font-size: 14px; font-weight: 500; color: #2D2A26; }
    &:hover { transform: translateY(-2px); box-shadow: 0 4px 16px rgba(0,0,0,0.08); }
  }
}

.stats-grid {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-bottom: 20px;
  @media (max-width: 1024px) { grid-template-columns: repeat(2, 1fr); }
  @media (max-width: 640px) { grid-template-columns: 1fr; }
}

.stat-card {
  display: flex; align-items: center; gap: 16px; padding: 24px;
  background: white; border-radius: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  cursor: pointer; transition: all 0.2s;
  &:hover { transform: translateY(-2px); box-shadow: 0 4px 16px rgba(0,0,0,0.08); }
  .stat-icon { width: 56px; height: 56px; background: linear-gradient(135deg, #F3EEFF 0%, #E8DEFF 100%); border-radius: 14px; display: flex; align-items: center; justify-content: center; font-size: 28px; }
  .stat-info { flex: 1;
    .stat-value { display: block; font-size: 24px; font-weight: 700; color: #2D2A26; }
    .stat-label { font-size: 14px; color: #6B6560; }
  }
  .stat-change {
    display: flex; align-items: center; gap: 4px;
    font-size: 13px; font-weight: 600; padding: 4px 10px; border-radius: 20px;
    &.up { background: #DCFCE7; color: #16A34A; }
    &.down { background: #FEE2E2; color: #DC2626; }
  }
}

// 详细指标
.metrics-grid {
  display: flex; gap: 16px; padding: 16px 20px; background: white; border-radius: 12px;
  margin-bottom: 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); flex-wrap: wrap;
  .metric-item {
    flex: 1; min-width: 120px; text-align: center; padding: 8px 16px;
    border-right: 1px solid #F0F0EF;
    &:last-child { border-right: none; }
    .metric-label { display: block; font-size: 12px; color: #9A958F; margin-bottom: 4px; }
    .metric-value {
      font-size: 18px; font-weight: 700; color: #2D2A26;
      &.highlight { color: #1890ff; }
      &.success { color: #52c41a; }
      &.warning { color: #ff4d4f; }
    }
  }
}

.charts-grid {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; margin-bottom: 24px;
  &.secondary { grid-template-columns: repeat(2, 1fr); }
  @media (max-width: 1200px) { grid-template-columns: repeat(2, 1fr); &.secondary { grid-template-columns: 1fr; } }
  @media (max-width: 768px) { grid-template-columns: 1fr; }
}

.chart-card {
  background: white; border-radius: 16px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  .card-header {
    display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;
    h3 { font-size: 16px; font-weight: 600; margin: 0; }
    .chart-subtitle { font-size: 12px; color: #9A958F; }
  }
}

.dashboard-grid {
  display: grid; grid-template-columns: 1fr 400px; gap: 24px;
  @media (max-width: 1200px) { grid-template-columns: 1fr; }
}

.main-content { display: flex; flex-direction: column; gap: 24px; }

.dashboard-card {
  background: white; border-radius: 16px; padding: 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.card-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;
  h3 { font-size: 16px; font-weight: 600; margin: 0; }
  .view-all { font-size: 13px; color: #722ed1; font-weight: 500; text-decoration: none; }
  .badge { background: #722ed1; color: white; font-size: 12px; font-weight: 600; padding: 2px 10px; border-radius: 10px; }
  .text-btn { background: none; border: none; font-size: 13px; color: #1890ff; cursor: pointer; }
}

.orders-table {
  overflow-x: auto;
  table { width: 100%; border-collapse: collapse;
    th, td { padding: 12px; text-align: left; border-bottom: 1px solid #F0F0F0; }
    th { font-size: 12px; font-weight: 600; color: #6B6560; text-transform: uppercase; background: #F8F8F7; }
    td { font-size: 14px; }
    .order-id { font-family: monospace; color: #6B6560; }
    .pet-cell { display: flex; align-items: center; gap: 6px; .pet-icon { font-size: 16px; } }
    .amount { font-weight: 600; color: #722ed1; }
    .payment { font-size: 12px; color: #6B6560; }
  }
}

.action-link {
  background: none; border: none; font-size: 13px; color: #1890ff; cursor: pointer;
  &:hover { text-decoration: underline; }
}

.status-badge {
  font-size: 12px; padding: 4px 10px; border-radius: 20px; font-weight: 500;
  &.status-pending { background: #FEF3C7; color: #D97706; }
  &.status-ongoing, &.status-processing { background: #DBEAFE; color: #2563EB; }
  &.status-completed { background: #DCFCE7; color: #16A34A; }
  &.status-cancelled { background: #FEE2E2; color: #DC2626; }
}

// 待审核机构
.review-card { border-top: 4px solid #1890ff; }
.review-list { display: flex; flex-direction: column; gap: 12px; }
.review-item {
  display: flex; justify-content: space-between; align-items: center; gap: 16px;
  padding: 16px; background: #F8F8F7; border-radius: 12px;
  .review-info { flex: 1;
    .review-main { display: flex; align-items: center; gap: 10px; margin-bottom: 6px;
      .review-name { font-weight: 600; font-size: 15px; }
      .review-type {
        font-size: 11px; padding: 2px 8px; border-radius: 4px;
        &.new { background: #DCFCE7; color: #16A34A; }
        &.update { background: #DBEAFE; color: #2563EB; }
      }
    }
    .review-meta { display: flex; gap: 16px; font-size: 12px; color: #6B6560; }
  }
  .review-actions { display: flex; gap: 8px;
    button {
      display: flex; align-items: center; gap: 4px; padding: 6px 12px;
      border: none; border-radius: 6px; font-size: 12px; cursor: pointer; transition: all 0.2s;
    }
    .btn-approve { background: #DCFCE7; color: #16A34A; &:hover { background: #BBF7D0; } }
    .btn-reject { background: #FEE2E2; color: #DC2626; &:hover { background: #FECACA; } }
    .btn-view { background: #F0F0EF; color: #6B6560; &:hover { background: #E5E5E5; } }
  }
}

.sidebar { display: flex; flex-direction: column; gap: 20px; }

// 待处理事项
.pending-card { border-top: 4px solid #722ed1; }
.pending-list { display: flex; flex-direction: column; gap: 10px; }
.pending-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 14px 16px; background: #F8F8F7; border-radius: 12px; cursor: pointer;
  transition: all 0.2s;
  &:hover { background: #F0F0EF; transform: translateX(4px); }
  &.priority-urgent { background: #FEF2F2; border-left: 3px solid #ff4d4f; }
  &.priority-high { background: #FFF7ED; border-left: 3px solid #faad14; }
  .pending-left { display: flex; align-items: center; gap: 12px; }
  .pending-icon { font-size: 18px; }
  .pending-title { font-size: 14px; color: #2D2A26; font-weight: 500; }
  .pending-count { min-width: 28px; height: 28px; border-radius: 14px; display: flex; align-items: center; justify-content: center; color: white; font-size: 13px; font-weight: 700; }
}

// 投诉列表
.complaints-card { border-top: 4px solid #ff4d4f; }
.complaints-list { display: flex; flex-direction: column; gap: 10px; }
.complaint-item {
  padding: 12px 14px; background: #F8F8F7; border-radius: 10px; cursor: pointer;
  transition: all 0.2s;
  &:hover { background: #F0F0EF; }
  &.priority-urgent { border-left: 3px solid #ff4d4f; }
  &.priority-high { border-left: 3px solid #faad14; }
  .complaint-info { display: flex; justify-content: space-between; margin-bottom: 6px;
    .complaint-id { font-family: monospace; font-size: 12px; color: #6B6560; }
    .complaint-type { font-size: 13px; font-weight: 500; }
  }
  .complaint-meta { display: flex; justify-content: space-between; font-size: 12px; color: #9A958F;
    .complaint-status {
      padding: 2px 8px; border-radius: 4px;
      &.pending { background: #FEF3C7; color: #D97706; }
      &.processing { background: #DBEAFE; color: #2563EB; }
    }
  }
}

// 系统通知
.notifications-card { border-top: 4px solid #1890ff; }
.notifications-list { display: flex; flex-direction: column; gap: 10px; }
.notification-item {
  display: flex; align-items: flex-start; gap: 12px;
  padding: 12px; background: #F8F8F7; border-radius: 10px; cursor: pointer;
  &.unread { background: #F0F7FF; border-left: 3px solid #1890ff; }
  &.warning .notification-icon { color: #faad14; }
  &.success .notification-icon { color: #52c41a; }
  &.info .notification-icon { color: #1890ff; }
  .notification-icon {
    width: 32px; height: 32px; border-radius: 8px; background: white;
    display: flex; align-items: center; justify-content: center;
  }
  .notification-content { flex: 1;
    .notification-title { display: block; font-weight: 600; font-size: 13px; margin-bottom: 4px; }
    .notification-text { font-size: 12px; color: #6B6560; line-height: 1.4; }
  }
  .notification-time { font-size: 11px; color: #9A958F; white-space: nowrap; }
}

// 机构排名
.ranking-card { border-top: 4px solid #7DD3C0; }
.ranking-list { display: flex; flex-direction: column; gap: 10px; }
.ranking-item {
  display: flex; align-items: center; gap: 12px; padding: 12px; background: #F8F8F7; border-radius: 10px;
  .ranking-number {
    width: 28px; height: 28px; border-radius: 8px; background: #E5E5E5;
    display: flex; align-items: center; justify-content: center;
    font-size: 14px; font-weight: 700; color: #6B6560;
    &.top { background: linear-gradient(135deg, #FFD700, #FFA500); color: white; }
  }
  .ranking-info { flex: 1;
    .ranking-name { display: block; font-weight: 600; font-size: 14px; margin-bottom: 4px; }
    .ranking-stats { display: flex; gap: 12px; font-size: 12px; color: #6B6560; }
  }
}
</style>
