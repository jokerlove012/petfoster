<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Calendar, DollarSign, Users, Star, ArrowUp, ArrowDown, Bell, Settings, Download, RefreshCw, TrendingUp, Package, Clock, CheckCircle, AlertCircle, Phone, MessageSquare, FileText, PlusCircle } from 'lucide-vue-next'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import LineChart from '@/components/charts/LineChart.vue'
import PieChart from '@/components/charts/PieChart.vue'
import BarChart from '@/components/charts/BarChart.vue'
import { useBookingStore } from '@/stores/booking'
import { useNotificationStore } from '@/stores/notification'
import { exportToCSV, exportToPDF } from '@/utils/reportExporter'
import { institutionManageApi } from '@/api/institution'

const router = useRouter()
const bookingStore = useBookingStore()
const notificationStore = useNotificationStore()

const loading = ref(false)
const lastUpdated = ref(new Date().toLocaleTimeString())
const selectedPeriod = ref<'today' | 'week' | 'month' | 'year'>('month')
const showExportMenu = ref(false)
const autoRefreshEnabled = ref(true)
let refreshInterval: ReturnType<typeof setInterval> | null = null

// 统计数据
const stats = ref({
  totalOrders: 0,
  monthlyRevenue: 0,
  occupancyRate: 0,
  averageRating: 0,
  ordersTrend: 0,
  revenueTrend: 0,
  occupancyTrend: 0,
  ratingTrend: 0,
  totalPets: 0,
  completedOrders: 0,
  cancelledOrders: 0,
  repeatCustomers: 0
})

// 收入趋势数据
const revenueTrend = ref<{name: string, value: number}[]>([])

// 服务分布数据
const serviceDistribution = ref<{name: string, value: number}[]>([])

// 每周订单数据
const weeklyOrders = ref<{name: string, value: number}[]>([])

// 宠物类型分布
const petTypeDistribution = ref<{name: string, value: number}[]>([])

// 最近订单
const recentOrders = ref<any[]>([])

// 待处理事项
const pendingTasks = ref([
  { id: '1', type: 'order', title: '新预约待确认', count: 0, icon: '📋', color: '#1890ff', route: '/institution/orders?status=pending', priority: 'high' },
  { id: '2', type: 'health', title: '今日健康记录', count: 0, icon: '💊', color: '#52c41a', route: '/institution/health', priority: 'medium' },
  { id: '3', type: 'review', title: '待回复评价', count: 0, icon: '⭐', color: '#faad14', route: '/institution/reviews', priority: 'low' },
  { id: '4', type: 'complaint', title: '待处理投诉', count: 0, icon: '⚠️', color: '#ff4d4f', route: '/institution/complaints', priority: 'urgent' }
])

// 今日入住宠物
const todayPets = ref<any[]>([])

// 今日离店宠物
const checkoutPets = ref<any[]>([])

// 通知消息
const notifications = ref<any[]>([])

// 快捷操作
const quickActions = ref([
  { id: '1', title: '服务管理', icon: Package, color: '#52c41a', route: '/institution/services' },
  { id: '2', title: '客户列表', icon: Users, color: '#faad14', route: '/institution/customers' },
  { id: '3', title: '数据报表', icon: FileText, color: '#722ed1', route: '/institution/reports' }
])

// 房间状态概览
const roomStatus = ref({
  total: 30,
  occupied: 0,
  available: 30,
  maintenance: 0
})

// 加载仪表盘数据
const loadDashboardData = async () => {
  loading.value = true
  try {
    // 加载统计数据
    const statsRes = await institutionManageApi.getDashboardStats()
    if (statsRes.code === 200 && statsRes.data) {
      const data = statsRes.data
      stats.value = {
        totalOrders: data.totalOrders || 0,
        monthlyRevenue: data.monthlyRevenue || 0,
        occupancyRate: data.occupancyRate || 0,
        averageRating: data.averageRating || 0,
        ordersTrend: data.ordersTrend || 0,
        revenueTrend: data.revenueTrend || 0,
        occupancyTrend: data.occupancyTrend || 0,
        ratingTrend: data.ratingTrend || 0,
        totalPets: data.totalPets || 0,
        completedOrders: data.completedOrders || 0,
        cancelledOrders: data.cancelledOrders || 0,
        repeatCustomers: data.repeatCustomers || 0
      }
      if (data.roomStatus) {
        roomStatus.value = data.roomStatus
      }
      // 加载图表数据
      if (data.revenueTrendData && data.revenueTrendData.length > 0) {
        revenueTrend.value = data.revenueTrendData
      }
      if (data.serviceDistributionData && data.serviceDistributionData.length > 0) {
        serviceDistribution.value = data.serviceDistributionData
      }
      if (data.weeklyOrdersData && data.weeklyOrdersData.length > 0) {
        weeklyOrders.value = data.weeklyOrdersData
      }
      if (data.petTypeDistributionData && data.petTypeDistributionData.length > 0) {
        petTypeDistribution.value = data.petTypeDistributionData
      }
    }

    // 加载最近订单
    const ordersRes = await institutionManageApi.getRecentOrders(10)
    if (ordersRes.code === 200 && ordersRes.data) {
      recentOrders.value = ordersRes.data.map((order: any) => ({
        id: order.id,
        petName: order.petName || '未知',
        petType: order.petSpecies || 'dog',
        owner: order.userName || '未知',
        ownerPhone: order.userPhone || '',
        service: order.packageName || '标准寄养',
        startDate: order.startDate,
        endDate: order.endDate,
        amount: order.totalPrice || 0,
        status: order.status,
        room: '-'
      }))
    }

    // 加载今日入住
    const checkInRes = await institutionManageApi.getTodayCheckIn()
    if (checkInRes.code === 200 && checkInRes.data) {
      todayPets.value = checkInRes.data
    }

    // 加载今日离店
    const checkOutRes = await institutionManageApi.getTodayCheckOut()
    if (checkOutRes.code === 200 && checkOutRes.data) {
      checkoutPets.value = checkOutRes.data
    }

    // 更新待处理事项数量
    await loadPendingTasksCounts()

    lastUpdated.value = new Date().toLocaleTimeString()
  } catch (error) {
    console.error('加载仪表盘数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载待处理事项数量
const loadPendingTasksCounts = async () => {
  try {
    // 获取待确认订单数量
    const bookingsRes = await institutionManageApi.getBookings(1, 100)
    if (bookingsRes.code === 200 && bookingsRes.data) {
      const bookings = bookingsRes.data.list || bookingsRes.data || []
      // 待确认订单
      const pendingOrders = bookings.filter((b: any) => b.status === 'pending').length
      pendingTasks.value[0].count = pendingOrders
      
      // 今日健康记录待填写（进行中的订单数）
      const inProgressOrders = bookings.filter((b: any) => b.status === 'in_progress').length
      pendingTasks.value[1].count = inProgressOrders
    }
    
    // 获取待回复评价数量
    const reviewsRes = await institutionManageApi.getReviews(1, 100)
    if (reviewsRes.code === 200 && reviewsRes.data) {
      const reviews = reviewsRes.data.list || reviewsRes.data || []
      // 没有回复的评价
      const unrepliedReviews = reviews.filter((r: any) => !r.reply || !r.reply.content).length
      pendingTasks.value[2].count = unrepliedReviews
    }
    
    // 待处理投诉暂时设为0（如果有投诉API可以调用）
    pendingTasks.value[3].count = 0
  } catch (error) {
    console.error('加载待处理事项失败:', error)
  }
}

// 计算属性
const totalPending = computed(() => pendingTasks.value.reduce((sum, t) => sum + t.count, 0))
const unreadCount = computed(() => notifications.value.filter(n => !n.read).length)
const occupancyPercent = computed(() => Math.round((roomStatus.value.occupied / roomStatus.value.total) * 100))
const todayCheckInCount = computed(() => todayPets.value.length)
const todayCheckOutCount = computed(() => checkoutPets.value.length)
const pendingCheckIn = computed(() => todayPets.value.filter(p => p.status === 'waiting').length)
const pendingCheckOut = computed(() => checkoutPets.value.filter(p => p.status === 'pending').length)

// 格式化函数
const formatCurrency = (value: number) => `¥${value.toLocaleString()}`
const formatPercent = (value: number) => `${value}%`
const getTrendClass = (value: number) => value >= 0 ? 'positive' : 'negative'

const getStatusText = (status: string) => {
  const map: Record<string, string> = { 
    pending: '待确认', 
    confirmed: '已确认', 
    ongoing: '进行中', 
    in_progress: '进行中',
    completed: '已完成', 
    cancelled: '已取消' 
  }
  return map[status] || status
}

const getStatusClass = (status: string) => {
  const map: Record<string, string> = {
    pending: 'warning',
    confirmed: 'info',
    ongoing: 'success',
    in_progress: 'success',
    completed: 'default',
    cancelled: 'danger'
  }
  return map[status] || 'default'
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
  refreshInterval = setInterval(async () => {
    if (!loading.value) {
      await loadDashboardData()
    }
  }, 30000) // 每30秒刷新
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
      title: '机构运营报表',
      period: selectedPeriod.value,
      generatedAt: new Date().toISOString(),
      stats: stats.value,
      orders: recentOrders.value
    }
    
    if (format === 'csv') {
      await exportToCSV(reportData, `机构报表_${new Date().toLocaleDateString()}`)
    } else {
      await exportToPDF(reportData, `机构报表_${new Date().toLocaleDateString()}`)
    }
    ElMessage.success('报表导出成功')
  } catch (error) {
    ElMessage.error('导出失败，请重试')
  }
}

// 跳转到任务页面
const goToTask = (task: typeof pendingTasks.value[0]) => {
  if (task.route) {
    router.push(task.route)
  }
}

// 快捷操作
const handleQuickAction = (action: typeof quickActions.value[0]) => {
  router.push(action.route)
}

// 确认订单
const confirmOrder = async (orderId: string) => {
  try {
    await ElMessageBox.confirm('确认接受此预约？', '确认预约', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    // 调用后端API确认订单
    const res = await institutionManageApi.confirmBooking(orderId)
    if (res.code === 200) {
      const order = recentOrders.value.find(o => o.id === orderId)
      if (order) {
        order.status = 'confirmed'
        // 更新待处理任务计数
        const pendingTask = pendingTasks.value.find(t => t.type === 'order')
        if (pendingTask && pendingTask.count > 0) {
          pendingTask.count--
        }
        ElMessage.success('预约已确认')
        
        // 发送通知
        ElNotification({
          title: '订单已确认',
          message: `${order.petName}的寄养预约已确认`,
          type: 'success'
        })
      }
    } else {
      ElMessage.error(res.message || '确认失败')
    }
  } catch {
    // 用户取消
  }
}

// 拒绝订单
const rejectOrder = async (orderId: string) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝预约', {
      confirmButtonText: '确认拒绝',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入拒绝原因...',
      inputValidator: (val) => !!val || '请输入拒绝原因',
      type: 'warning'
    })
    
    // 调用后端API拒绝订单
    const res = await institutionManageApi.rejectBooking(orderId, reason)
    if (res.code === 200) {
      const order = recentOrders.value.find(o => o.id === orderId)
      if (order) {
        order.status = 'cancelled'
        const pendingTask = pendingTasks.value.find(t => t.type === 'order')
        if (pendingTask && pendingTask.count > 0) {
          pendingTask.count--
        }
        ElMessage.success('已拒绝预约')
      }
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch {
    // 用户取消
  }
}

// 查看订单详情
const viewOrderDetail = (orderId: string) => {
  router.push(`/institution/orders/${orderId}`)
}

// 办理入住
const handleCheckIn = async (pet: typeof todayPets.value[0]) => {
  try {
    await ElMessageBox.confirm(`确认为 ${pet.name} 办理入住？`, '办理入住', {
      confirmButtonText: '确认入住',
      cancelButtonText: '取消'
    })
    
    // 调用后端API办理入住
    const res = await institutionManageApi.checkIn(pet.orderId || pet.id)
    if (res.code === 200) {
      pet.status = 'checked_in'
      ElMessage.success(`${pet.name} 已成功入住 ${pet.room}`)
      // 刷新数据
      loadDashboardData()
    } else {
      ElMessage.error(res.message || '办理入住失败')
    }
  } catch {
    // 用户取消
  }
}

// 办理离店
const handleCheckOut = async (pet: typeof checkoutPets.value[0]) => {
  try {
    await ElMessageBox.confirm(`确认为 ${pet.name} 办理离店？`, '办理离店', {
      confirmButtonText: '确认离店',
      cancelButtonText: '取消'
    })
    
    // 调用后端API办理离店
    const res = await institutionManageApi.checkOut(pet.orderId || pet.id)
    if (res.code === 200) {
      pet.status = 'completed'
      ElMessage.success(`${pet.name} 已成功离店`)
      // 刷新数据
      loadDashboardData()
    } else {
      ElMessage.error(res.message || '办理离店失败')
    }
  } catch {
    // 用户取消
  }
}

// 订单表格中的办理离店
const handleOrderCheckOut = async (order: typeof recentOrders.value[0]) => {
  try {
    await ElMessageBox.confirm(`确认为 ${order.petName} 办理离店？订单金额 ¥${order.amount} 将转入机构钱包。`, '办理离店', {
      confirmButtonText: '确认离店',
      cancelButtonText: '取消'
    })
    
    // 调用后端API办理离店
    const res = await institutionManageApi.checkOut(order.id)
    if (res.code === 200) {
      order.status = 'completed'
      ElMessage.success(`${order.petName} 已成功离店，收入已到账`)
      // 刷新数据
      loadDashboardData()
    } else {
      ElMessage.error(res.message || '办理离店失败')
    }
  } catch {
    // 用户取消
  }
}

// 联系客户
const contactCustomer = (phone: string) => {
  ElMessage.info(`正在拨打 ${phone}...`)
  // 实际应用中可以调用电话API
}

// 发送消息
const sendMessage = (owner: string) => {
  router.push(`/institution/messages?to=${owner}`)
}

// 标记通知已读
const markAsRead = (notificationId: string) => {
  const notification = notifications.value.find(n => n.id === notificationId)
  if (notification) {
    notification.read = true
    if (notification.actionUrl) {
      router.push(notification.actionUrl)
    }
  }
}

// 全部标记已读
const markAllAsRead = () => {
  notifications.value.forEach(n => n.read = true)
  ElMessage.success('已全部标记为已读')
}

// 切换时间周期
const changePeriod = (period: typeof selectedPeriod.value) => {
  selectedPeriod.value = period
  refreshData()
}

// 记录健康状态
const recordHealth = (orderId: string) => {
  router.push(`/institution/health/${orderId}`)
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
  <div class="dashboard">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1>🏠 机构仪表盘</h1>
        <p>欢迎回来，查看您的机构运营数据</p>
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
        <div class="notification-btn" @click="markAllAsRead">
          <Bell :size="18" />
          <span v-if="unreadCount" class="notification-badge">{{ unreadCount }}</span>
        </div>
        <router-link to="/institution/settings" class="icon-btn">
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
      <div class="stat-card" @click="router.push('/institution/orders')">
        <div class="stat-icon orders"><Calendar :size="24" /></div>
        <div class="stat-content">
          <span class="stat-label">本月订单</span>
          <span class="stat-value">{{ stats.totalOrders }}</span>
          <span class="stat-trend" :class="getTrendClass(stats.ordersTrend)">
            <ArrowUp v-if="stats.ordersTrend >= 0" :size="14" /><ArrowDown v-else :size="14" />
            {{ Math.abs(stats.ordersTrend) }}%
          </span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon revenue"><DollarSign :size="24" /></div>
        <div class="stat-content">
          <span class="stat-label">本月收入</span>
          <span class="stat-value">{{ formatCurrency(stats.monthlyRevenue) }}</span>
          <span class="stat-trend" :class="getTrendClass(stats.revenueTrend)">
            <ArrowUp v-if="stats.revenueTrend >= 0" :size="14" /><ArrowDown v-else :size="14" />
            {{ Math.abs(stats.revenueTrend) }}%
          </span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon occupancy"><Users :size="24" /></div>
        <div class="stat-content">
          <span class="stat-label">入住率</span>
          <span class="stat-value">{{ formatPercent(stats.occupancyRate) }}</span>
          <span class="stat-trend" :class="getTrendClass(stats.occupancyTrend)">
            <ArrowUp v-if="stats.occupancyTrend >= 0" :size="14" /><ArrowDown v-else :size="14" />
            {{ Math.abs(stats.occupancyTrend) }}%
          </span>
        </div>
        <div class="stat-extra">
          <span>{{ roomStatus.occupied }}/{{ roomStatus.total }} 房间</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon rating"><Star :size="24" /></div>
        <div class="stat-content">
          <span class="stat-label">平均评分</span>
          <span class="stat-value">{{ stats.averageRating }}</span>
          <span class="stat-trend" :class="getTrendClass(stats.ratingTrend)">
            <ArrowUp v-if="stats.ratingTrend >= 0" :size="14" /><ArrowDown v-else :size="14" />
            {{ Math.abs(stats.ratingTrend) }}
          </span>
        </div>
      </div>
    </div>

    <!-- 房间状态概览 -->
    <div class="room-overview">
      <div class="room-stat">
        <span class="room-label">总房间</span>
        <span class="room-value">{{ roomStatus.total }}</span>
      </div>
      <div class="room-bar">
        <div class="bar-segment occupied" :style="{ width: `${(roomStatus.occupied / roomStatus.total) * 100}%` }">
          <span>已入住 {{ roomStatus.occupied }}</span>
        </div>
        <div class="bar-segment available" :style="{ width: `${(roomStatus.available / roomStatus.total) * 100}%` }">
          <span>空闲 {{ roomStatus.available }}</span>
        </div>
        <div class="bar-segment maintenance" :style="{ width: `${(roomStatus.maintenance / roomStatus.total) * 100}%` }">
          <span>维护 {{ roomStatus.maintenance }}</span>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-grid">
      <div class="chart-card">
        <div class="chart-header">
          <h3>📈 收入趋势</h3>
          <span class="chart-subtitle">近6个月</span>
        </div>
        <LineChart :data="revenueTrend" color="#FF7F6B" />
      </div>
      <div class="chart-card">
        <div class="chart-header">
          <h3>🍩 服务分布</h3>
          <span class="chart-subtitle">按类型统计</span>
        </div>
        <PieChart :data="serviceDistribution" />
      </div>
      <div class="chart-card">
        <div class="chart-header">
          <h3>📊 每周订单</h3>
          <span class="chart-subtitle">本周数据</span>
        </div>
        <BarChart :data="weeklyOrders" color="#7DD3C0" />
      </div>
    </div>

    <!-- 今日动态 -->
    <div class="today-section">
      <div class="today-card checkin">
        <div class="card-header">
          <h3>🐾 今日入住 ({{ todayCheckInCount }})</h3>
          <span v-if="pendingCheckIn > 0" class="pending-badge">{{ pendingCheckIn }} 待办理</span>
        </div>
        <div class="pet-list">
          <div v-for="pet in todayPets" :key="pet.id" class="pet-item" :class="{ 'checked-in': pet.status === 'checked_in' }">
            <span class="pet-avatar">{{ pet.avatar }}</span>
            <div class="pet-info">
              <span class="pet-name">{{ pet.name }}</span>
              <span class="pet-type">{{ pet.type }} · 房间 {{ pet.room }}</span>
            </div>
            <div class="pet-time">
              <span class="time">{{ pet.checkIn }}</span>
              <span class="owner">{{ pet.owner }}</span>
            </div>
            <div class="pet-actions">
              <button v-if="pet.status === 'waiting'" class="mini-btn primary" @click="handleCheckIn(pet)">
                <CheckCircle :size="14" /> 入住
              </button>
              <span v-else class="status-tag success">已入住</span>
              <button class="mini-btn ghost" @click="contactCustomer(pet.phone)" title="联系客户">
                <Phone :size="14" />
              </button>
            </div>
          </div>
        </div>
      </div>
      <div class="today-card checkout">
        <div class="card-header">
          <h3>👋 今日离店 ({{ todayCheckOutCount }})</h3>
          <span v-if="pendingCheckOut > 0" class="pending-badge warning">{{ pendingCheckOut }} 待准备</span>
        </div>
        <div class="pet-list">
          <div v-for="pet in checkoutPets" :key="pet.id" class="pet-item">
            <span class="pet-avatar">{{ pet.avatar }}</span>
            <div class="pet-info">
              <span class="pet-name">{{ pet.name }}</span>
              <span class="pet-type">{{ pet.type }} · 房间 {{ pet.room }}</span>
            </div>
            <div class="pet-action">
              <span class="checkout-time">{{ pet.checkOut }}</span>
              <span class="checkout-status" :class="pet.status">
                {{ pet.status === 'ready' ? '已准备' : pet.status === 'completed' ? '已离店' : '待准备' }}
              </span>
            </div>
            <div class="pet-actions">
              <button v-if="pet.status !== 'completed'" class="mini-btn primary" @click="handleCheckOut(pet)">
                <CheckCircle :size="14" /> 离店
              </button>
              <button class="mini-btn ghost" @click="contactCustomer(pet.phone)" title="联系客户">
                <Phone :size="14" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="main-grid">
      <!-- 订单表格 -->
      <div class="table-card">
        <div class="card-header">
          <h3>📋 最近订单</h3>
          <router-link to="/institution/orders" class="view-all">查看全部 →</router-link>
        </div>
        <div class="table-wrapper">
          <table class="data-table">
            <thead>
              <tr>
                <th>宠物</th>
                <th>主人</th>
                <th>服务</th>
                <th>日期</th>
                <th>房间</th>
                <th>金额</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="order in recentOrders" :key="order.id">
                <td class="pet-cell">
                  <span class="pet-avatar">{{ getPetIcon(order.petType) }}</span>
                  <span class="pet-name">{{ order.petName }}</span>
                </td>
                <td>
                  <div class="owner-cell">
                    <span>{{ order.owner }}</span>
                    <button class="contact-btn" @click="contactCustomer(order.ownerPhone)" title="联系客户">
                      <Phone :size="12" />
                    </button>
                  </div>
                </td>
                <td><span class="service-tag">{{ order.service }}</span></td>
                <td class="date-cell">{{ order.startDate }}</td>
                <td class="room-cell">{{ order.room }}</td>
                <td class="amount">¥{{ order.amount }}</td>
                <td><span class="status-badge" :class="order.status">{{ getStatusText(order.status) }}</span></td>
                <td class="action-cell">
                  <button v-if="order.status === 'pending'" class="action-link confirm" @click="confirmOrder(order.id)">确认</button>
                  <button v-if="order.status === 'pending'" class="action-link reject" @click="rejectOrder(order.id)">拒绝</button>
                  <button v-if="order.status === 'ongoing' || order.status === 'in_progress'" class="action-link health" @click="recordHealth(order.id)">记录</button>
                  <button v-if="order.status === 'confirmed' || order.status === 'in_progress'" class="action-link confirm" @click="handleOrderCheckOut(order)">离店</button>
                  <button class="action-link" @click="viewOrderDetail(order.id)">详情</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 右侧栏 -->
      <div class="right-sidebar">
        <!-- 待处理事项 -->
        <div class="section-card pending-card">
          <div class="card-header">
            <h3>⏳ 待处理事项</h3>
            <span class="total-badge">{{ totalPending }}</span>
          </div>
          <div class="task-list">
            <div 
              v-for="task in pendingTasks" 
              :key="task.id" 
              class="task-item" 
              :class="{ urgent: task.priority === 'urgent', high: task.priority === 'high' }" 
              @click="goToTask(task)"
            >
              <div class="task-left">
                <span class="task-icon">{{ task.icon }}</span>
                <span class="task-title">{{ task.title }}</span>
              </div>
              <span class="task-count" :style="{ background: task.color }">{{ task.count }}</span>
            </div>
          </div>
        </div>

        <!-- 宠物类型分布 -->
        <div class="section-card pet-type-card">
          <div class="card-header">
            <h3>🐾 宠物类型分布</h3>
          </div>
          <div class="pet-type-stats">
            <div v-for="item in petTypeDistribution" :key="item.name" class="pet-type-item">
              <span class="pet-type-icon">{{ item.name === '狗狗' ? '🐕' : item.name === '猫咪' ? '🐱' : '🐾' }}</span>
              <span class="pet-type-name">{{ item.name }}</span>
              <div class="pet-type-bar">
                <div class="bar-fill" :style="{ width: `${item.value}%` }"></div>
              </div>
              <span class="pet-type-value">{{ item.value }}%</span>
            </div>
          </div>
        </div>

        <!-- 通知消息 -->
        <div class="section-card notification-card">
          <div class="card-header">
            <h3>🔔 最新通知</h3>
            <button class="text-btn" @click="markAllAsRead">全部已读</button>
          </div>
          <div class="notification-list">
            <div 
              v-for="notification in notifications" 
              :key="notification.id" 
              class="notification-item" 
              :class="{ unread: !notification.read }" 
              @click="markAsRead(notification.id)"
            >
              <div class="notification-icon">
                <AlertCircle v-if="notification.type === 'system'" :size="16" />
                <Calendar v-else-if="notification.type === 'order'" :size="16" />
                <Star v-else :size="16" />
              </div>
              <div class="notification-content">
                <span class="notification-title">{{ notification.title }}</span>
                <span class="notification-text">{{ notification.content }}</span>
              </div>
              <span class="notification-time">{{ notification.time }}</span>
            </div>
          </div>
          <router-link to="/institution/notifications" class="view-all-link">查看全部通知 →</router-link>
        </div>
      </div>
    </div>
  </div>
</template>


<style lang="scss" scoped>
.dashboard { padding: 24px; max-width: 1500px; margin: 0 auto; background: #FAFAF9; min-height: 100vh; }

.page-header {
  display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; flex-wrap: wrap; gap: 16px;
  .header-left {
    h1 { font-size: 26px; font-weight: 700; margin: 0 0 6px; color: #2D2A26; }
    p { font-size: 14px; color: #6B6560; margin: 0; }
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
    &.active { background: #FF7F6B; color: white; }
  }
}

.icon-btn {
  width: 36px; height: 36px; border: none; background: white; border-radius: 10px;
  display: flex; align-items: center; justify-content: center; cursor: pointer;
  color: #6B6560; transition: all 0.2s; box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  &:hover { background: #F0F0EF; color: #2D2A26; }
  &:disabled { opacity: 0.5; cursor: not-allowed; }
  &.active { background: #E8F4FD; color: #1890ff; }
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
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 18px; margin-bottom: 20px;
  @media (max-width: 1024px) { grid-template-columns: repeat(2, 1fr); }
  @media (max-width: 640px) { grid-template-columns: 1fr; }
}

.stat-card {
  display: flex; align-items: center; gap: 16px; padding: 22px;
  background: white; border-radius: 16px; box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  transition: transform 0.2s, box-shadow 0.2s; cursor: pointer; position: relative;
  &:hover { transform: translateY(-2px); box-shadow: 0 4px 20px rgba(0,0,0,0.08); }
  .stat-extra {
    position: absolute; bottom: 8px; right: 16px;
    font-size: 11px; color: #9A958F;
  }
}

.stat-icon {
  width: 52px; height: 52px; border-radius: 14px; display: flex; align-items: center; justify-content: center;
  &.orders { background: linear-gradient(135deg, #E8F4FD 0%, #D1E9FA 100%); color: #1890ff; }
  &.revenue { background: linear-gradient(135deg, #E8F8E8 0%, #D4F0D4 100%); color: #52c41a; }
  &.occupancy { background: linear-gradient(135deg, #FFF8E6 0%, #FFEFC7 100%); color: #faad14; }
  &.rating { background: linear-gradient(135deg, #F3EEFF 0%, #E8DEFF 100%); color: #722ed1; }
}

.stat-content { flex: 1; display: flex; flex-direction: column; gap: 4px; }
.stat-label { font-size: 13px; color: #6B6560; }
.stat-value { font-size: 26px; font-weight: 700; color: #2D2A26; }
.stat-trend {
  display: flex; align-items: center; gap: 3px; font-size: 12px; font-weight: 600;
  &.positive { color: #52c41a; }
  &.negative { color: #ff4d4f; }
}

// 房间状态概览
.room-overview {
  display: flex; align-items: center; gap: 20px; padding: 16px 20px;
  background: white; border-radius: 12px; margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  .room-stat {
    display: flex; flex-direction: column;
    .room-label { font-size: 12px; color: #9A958F; }
    .room-value { font-size: 24px; font-weight: 700; color: #2D2A26; }
  }
  .room-bar {
    flex: 1; display: flex; height: 28px; border-radius: 14px; overflow: hidden; background: #F0F0EF;
    .bar-segment {
      display: flex; align-items: center; justify-content: center;
      font-size: 11px; font-weight: 600; color: white; transition: width 0.3s;
      span { white-space: nowrap; }
      &.occupied { background: linear-gradient(90deg, #52c41a, #73d13d); }
      &.available { background: linear-gradient(90deg, #1890ff, #40a9ff); }
      &.maintenance { background: linear-gradient(90deg, #faad14, #ffc53d); }
    }
  }
}

.charts-grid {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; margin-bottom: 24px;
  @media (max-width: 1200px) { grid-template-columns: repeat(2, 1fr); }
  @media (max-width: 768px) { grid-template-columns: 1fr; }
}

.chart-card {
  background: white; border-radius: 16px; padding: 20px; box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  border: 1px solid #F0F0EF;
}

.chart-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;
  h3 { font-size: 15px; font-weight: 600; margin: 0; color: #2D2A26; }
  .chart-subtitle { font-size: 12px; color: #9A958F; }
}

// 今日动态
.today-section {
  display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 24px;
  @media (max-width: 900px) { grid-template-columns: 1fr; }
}

.today-card {
  background: white; border-radius: 16px; padding: 20px; box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  &.checkin { border-left: 4px solid #52c41a; }
  &.checkout { border-left: 4px solid #faad14; }
  .card-header { 
    display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;
    h3 { font-size: 15px; font-weight: 600; margin: 0; }
    .pending-badge {
      font-size: 12px; padding: 4px 10px; border-radius: 12px;
      background: #E8F4FD; color: #1890ff;
      &.warning { background: #FEF3C7; color: #D97706; }
    }
  }
}

.pet-list { display: flex; flex-direction: column; gap: 12px; }

.pet-item {
  display: flex; align-items: center; gap: 12px; padding: 12px; background: #F8F8F7; border-radius: 12px;
  transition: all 0.2s;
  &.checked-in { background: #DCFCE7; }
  &:hover { background: #F0F0EF; }
  .pet-avatar { font-size: 24px; }
  .pet-info { flex: 1;
    .pet-name { display: block; font-weight: 600; font-size: 14px; }
    .pet-type { font-size: 12px; color: #6B6560; }
  }
  .pet-time, .pet-action { text-align: right; min-width: 70px;
    .time, .checkout-time { display: block; font-weight: 600; font-size: 14px; color: #2D2A26; }
    .owner { font-size: 12px; color: #6B6560; }
    .checkout-status {
      display: inline-block; font-size: 11px; padding: 3px 8px; border-radius: 6px; margin-top: 4px;
      &.ready { background: #DCFCE7; color: #16A34A; }
      &.pending { background: #FEF3C7; color: #D97706; }
      &.completed { background: #F3F4F6; color: #6B7280; }
    }
  }
  .pet-actions {
    display: flex; gap: 6px; align-items: center;
    .status-tag {
      font-size: 11px; padding: 4px 10px; border-radius: 6px;
      &.success { background: #DCFCE7; color: #16A34A; }
    }
  }
}

.mini-btn {
  display: flex; align-items: center; gap: 4px; padding: 6px 10px;
  border: none; border-radius: 6px; font-size: 12px; cursor: pointer; transition: all 0.2s;
  &.primary { background: #1890ff; color: white; &:hover { background: #40a9ff; } }
  &.ghost { background: transparent; color: #6B6560; &:hover { background: #F0F0EF; } }
}

.main-grid {
  display: grid; grid-template-columns: 1fr 360px; gap: 24px;
  @media (max-width: 1100px) { grid-template-columns: 1fr; }
}

.table-card, .section-card {
  background: white; border-radius: 16px; padding: 20px; box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  border: 1px solid #F0F0EF;
}

.card-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;
  h3 { font-size: 15px; font-weight: 600; margin: 0; color: #2D2A26; }
  .view-all { font-size: 13px; color: #FF7F6B; font-weight: 500; text-decoration: none; }
  .total-badge { background: #FF7F6B; color: white; font-size: 13px; font-weight: 700; padding: 4px 12px; border-radius: 12px; }
  .text-btn { background: none; border: none; font-size: 13px; color: #1890ff; cursor: pointer; }
}

.table-wrapper { overflow-x: auto; }

.data-table {
  width: 100%; border-collapse: collapse; font-size: 14px;
  th { text-align: left; padding: 12px 10px; font-weight: 600; color: #6B6560; background: #F8F8F7; font-size: 13px;
    &:first-child { border-radius: 8px 0 0 0; }
    &:last-child { border-radius: 0 8px 0 0; }
  }
  td { padding: 14px 10px; border-bottom: 1px solid #F0F0EF; }
  tbody tr:hover { background: #FAFAF9; }
  tbody tr:last-child td { border-bottom: none; }
}

.pet-cell { display: flex; align-items: center; gap: 8px;
  .pet-avatar { font-size: 18px; }
  .pet-name { font-weight: 600; }
}

.owner-cell {
  display: flex; align-items: center; gap: 6px;
  .contact-btn {
    width: 22px; height: 22px; border: none; background: #F0F0EF;
    border-radius: 6px; cursor: pointer; display: flex; align-items: center; justify-content: center;
    color: #6B6560; transition: all 0.2s;
    &:hover { background: #1890ff; color: white; }
  }
}

.service-tag { display: inline-block; padding: 3px 8px; background: #F0F5FF; color: #1890ff; border-radius: 6px; font-size: 12px; }
.date-cell { font-size: 13px; color: #6B6560; }
.room-cell { font-weight: 600; color: #722ed1; }
.amount { font-weight: 700; color: #FF7F6B; }

.status-badge {
  display: inline-block; font-size: 12px; padding: 4px 10px; border-radius: 16px; font-weight: 600;
  &.pending { background: #FEF3C7; color: #D97706; }
  &.confirmed { background: #DBEAFE; color: #2563EB; }
  &.ongoing, &.in_progress { background: #DCFCE7; color: #16A34A; }
  &.completed { background: #F3F4F6; color: #6B7280; }
  &.cancelled { background: #FEE2E2; color: #DC2626; }
}

.action-cell { display: flex; gap: 8px; flex-wrap: wrap; }
.action-link {
  background: none; border: none; font-size: 13px; color: #1890ff; cursor: pointer; padding: 0;
  &:hover { text-decoration: underline; }
  &.confirm { color: #52c41a; font-weight: 600; }
  &.reject { color: #ff4d4f; }
  &.health { color: #722ed1; }
}

.right-sidebar { display: flex; flex-direction: column; gap: 20px; }

.pending-card { border-top: 4px solid #FF7F6B; }
.task-list { display: flex; flex-direction: column; gap: 10px; }
.task-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 14px 16px; background: #F8F8F7; border-radius: 12px; cursor: pointer;
  transition: all 0.2s;
  &:hover { background: #F0F0EF; transform: translateX(4px); }
  &.urgent { background: #FEF2F2; border-left: 3px solid #ff4d4f; }
  &.high { background: #FFF7ED; border-left: 3px solid #faad14; }
}
.task-left { display: flex; align-items: center; gap: 12px; }
.task-icon { font-size: 18px; }
.task-title { font-size: 14px; color: #2D2A26; font-weight: 500; }
.task-count { min-width: 28px; height: 28px; border-radius: 14px; display: flex; align-items: center; justify-content: center; color: white; font-size: 13px; font-weight: 700; }

// 宠物类型分布
.pet-type-card { border-top: 4px solid #7DD3C0; }
.pet-type-stats { display: flex; flex-direction: column; gap: 12px; }
.pet-type-item {
  display: flex; align-items: center; gap: 10px;
  .pet-type-icon { font-size: 20px; }
  .pet-type-name { width: 50px; font-size: 13px; color: #2D2A26; }
  .pet-type-bar { flex: 1; height: 8px; background: #F0F0EF; border-radius: 4px; overflow: hidden;
    .bar-fill { height: 100%; background: linear-gradient(90deg, #7DD3C0, #4ECDC4); border-radius: 4px; }
  }
  .pet-type-value { width: 40px; text-align: right; font-size: 13px; font-weight: 600; color: #2D2A26; }
}

.notification-card { border-top: 4px solid #1890ff; }
.notification-list { display: flex; flex-direction: column; gap: 10px; }
.notification-item {
  display: flex; align-items: flex-start; gap: 12px;
  padding: 12px; background: #F8F8F7; border-radius: 10px; cursor: pointer;
  &.unread { background: #F0F7FF; border-left: 3px solid #1890ff; }
  .notification-icon { 
    width: 32px; height: 32px; border-radius: 8px; background: white;
    display: flex; align-items: center; justify-content: center; color: #6B6560;
  }
  .notification-content { flex: 1;
    .notification-title { display: block; font-weight: 600; font-size: 13px; margin-bottom: 4px; }
    .notification-text { font-size: 12px; color: #6B6560; line-height: 1.4; }
  }
  .notification-time { font-size: 11px; color: #9A958F; white-space: nowrap; }
}

.view-all-link {
  display: block; text-align: center; padding: 12px; margin-top: 12px;
  font-size: 13px; color: #1890ff; text-decoration: none; border-top: 1px solid #F0F0EF;
  &:hover { background: #F8F8F7; }
}
</style>
