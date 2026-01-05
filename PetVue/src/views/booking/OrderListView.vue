<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useBookingStore } from '@/stores'
import { useAutoRefresh } from '@/composables/useAutoRefresh'
import { bookingApi } from '@/api/booking'
import type { Booking, BookingStatus } from '@/types/booking'
import { AppButton } from '@/components/common'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const bookingStore = useBookingStore()

// 订单列表
const orders = ref<Booking[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 状态筛选
const statusFilter = ref<BookingStatus | 'all'>('all')
const statusOptions = [
  { value: 'all', label: '全部订单', icon: '📋' },
  { value: 'pending', label: '待确认', icon: '⏳' },
  { value: 'confirmed', label: '已确认', icon: '✅' },
  { value: 'in_progress', label: '进行中', icon: '🏠' },
  { value: 'completed', label: '已完成', icon: '🎉' },
  { value: 'cancelled', label: '已取消', icon: '❌' }
] as const

// 获取状态配置
const getStatusConfig = (status: BookingStatus) => {
  const configs: Record<BookingStatus, { label: string; color: string; bgColor: string }> = {
    pending: { label: '待确认', color: '#F59E0B', bgColor: 'rgba(245, 158, 11, 0.1)' },
    confirmed: { label: '已确认', color: '#3B82F6', bgColor: 'rgba(59, 130, 246, 0.1)' },
    in_progress: { label: '进行中', color: '#22C55E', bgColor: 'rgba(34, 197, 94, 0.1)' },
    completed: { label: '已完成', color: '#6B7280', bgColor: 'rgba(107, 114, 128, 0.1)' },
    cancelled: { label: '已取消', color: '#EF4444', bgColor: 'rgba(239, 68, 68, 0.1)' }
  }
  return configs[status]
}

// 加载订单列表
const loadOrders = async () => {
  try {
    const params: { status?: BookingStatus; page?: number; pageSize?: number } = {
      page: currentPage.value,
      pageSize: pageSize.value
    }
    if (statusFilter.value !== 'all') {
      params.status = statusFilter.value
    }
    const res = await bookingApi.getList(params)
    if (res.code === 200 && res.data) {
      orders.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('Failed to load orders:', error)
  }
}

// 首次加载
const initialLoad = async () => {
  loading.value = true
  try {
    await loadOrders()
  } finally {
    loading.value = false
  }
}

// 自动刷新（10秒）
const { isAutoRefresh } = useAutoRefresh(loadOrders, 10000)

// 筛选后的订单
const filteredOrders = computed(() => {
  if (statusFilter.value === 'all') {
    return orders.value
  }
  return orders.value.filter(order => order.status === statusFilter.value)
})

// 查看订单详情
const viewOrder = (orderId: string) => {
  router.push(`/order/${orderId}`)
}

// 格式化日期
const formatDate = (dateStr: string) => {
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    month: 'short',
    day: 'numeric'
  })
}

// 格式化价格
const formatPrice = (price: number) => {
  return `¥${price.toFixed(2)}`
}

// 获取机构名称
const getInstitutionName = (order: Booking) => {
  return (order as any).institutionName || '未知机构'
}

// 获取宠物名称
const getPetName = (order: Booking) => {
  return (order as any).petName || '未知宠物'
}

watch(statusFilter, () => {
  currentPage.value = 1
  initialLoad()
})

// 删除订单
const deleteOrder = async (orderId: string) => {
  try {
    await ElMessageBox.confirm('确定要删除此订单吗？删除后无法恢复。', '删除订单', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await bookingApi.delete(orderId)
    if (res.code === 200) {
      ElMessage.success('订单已删除')
      loadOrders()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch {
    // 用户取消
  }
}

onMounted(() => {
  initialLoad()
})
</script>

<template>
  <div class="order-list-view">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1>我的订单</h1>
      <p>查看和管理您的寄养订单</p>
    </div>

    <!-- 状态筛选 -->
    <div class="status-filter">
      <div 
        v-for="option in statusOptions"
        :key="option.value"
        class="filter-item"
        :class="{ active: statusFilter === option.value }"
        @click="statusFilter = option.value"
      >
        <span class="filter-icon">{{ option.icon }}</span>
        <span class="filter-label">{{ option.label }}</span>
      </div>
    </div>

    <!-- 订单列表 -->
    <div class="order-list">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <!-- 空状态 -->
      <div v-else-if="filteredOrders.length === 0" class="empty-state">
        <div class="empty-icon">📭</div>
        <h3>暂无订单</h3>
        <p>您还没有相关订单记录</p>
        <router-link to="/institutions">
          <AppButton type="primary">去预约</AppButton>
        </router-link>
      </div>

      <!-- 订单卡片 -->
      <div 
        v-else
        v-for="order in filteredOrders"
        :key="order.id"
        class="order-card"
        @click="viewOrder(order.id)"
      >
        <div class="order-header">
          <span class="order-number">{{ order.orderNumber }}</span>
          <span 
            class="order-status"
            :style="{ 
              color: getStatusConfig(order.status).color,
              background: getStatusConfig(order.status).bgColor
            }"
          >
            {{ getStatusConfig(order.status).label }}
          </span>
        </div>

        <div class="order-content">
          <div class="order-info">
            <div class="info-row">
              <span class="info-icon">🏠</span>
              <span class="info-text">{{ getInstitutionName(order) }}</span>
            </div>
            <div class="info-row">
              <span class="info-icon">🐾</span>
              <span class="info-text">{{ getPetName(order) }}</span>
            </div>
            <div class="info-row">
              <span class="info-icon">📅</span>
              <span class="info-text">
                {{ formatDate(order.startDate) }} - {{ formatDate(order.endDate) }}
                <span class="days-badge">{{ order.totalDays }}天</span>
              </span>
            </div>
          </div>

          <div class="order-price">
            <span class="price-label">订单金额</span>
            <span class="price-value">{{ formatPrice(order.totalPrice) }}</span>
          </div>
        </div>

        <div class="order-footer">
          <span class="order-time">
            下单时间：{{ new Date(order.createdAt).toLocaleString('zh-CN') }}
          </span>
          <div class="order-actions">
            <AppButton 
              v-if="order.status === 'in_progress'"
              type="primary" 
              size="sm"
              @click.stop="viewOrder(order.id)"
            >
              查看状态
            </AppButton>
            <AppButton 
              v-else-if="order.status === 'completed'"
              type="outline" 
              size="sm"
              @click.stop="viewOrder(order.id)"
            >
              查看详情
            </AppButton>
            <AppButton 
              v-else
              type="outline" 
              size="sm"
              @click.stop="viewOrder(order.id)"
            >
              查看详情
            </AppButton>
            <AppButton 
              v-if="order.status === 'completed' || order.status === 'cancelled'"
              type="danger" 
              size="sm"
              @click.stop="deleteOrder(order.id)"
            >
              删除
            </AppButton>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.order-list-view {
  padding: 24px;
  max-width: 800px;
  margin: 0 auto;
}

// 页面头部
.page-header {
  margin-bottom: 24px;
  
  h1 {
    font-size: 28px;
    font-weight: 700;
    margin: 0 0 8px;
  }
  
  p {
    font-size: 14px;
    color: var(--color-text-secondary);
    margin: 0;
  }
}

// 状态筛选
.status-filter {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  overflow-x: auto;
  padding-bottom: 8px;
  
  &::-webkit-scrollbar {
    display: none;
  }
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-full);
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s ease;
  
  &:hover {
    border-color: var(--color-primary-light);
  }
  
  &.active {
    background: var(--color-primary);
    border-color: var(--color-primary);
    color: white;
  }
  
  .filter-icon {
    font-size: 14px;
  }
  
  .filter-label {
    font-size: 14px;
    font-weight: 500;
  }
}

// 订单列表
.order-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

// 加载状态
.loading-state {
  text-align: center;
  padding: 48px 24px;
  
  .loading-spinner {
    width: 40px;
    height: 40px;
    border: 3px solid var(--color-border);
    border-top-color: var(--color-primary);
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin: 0 auto 16px;
  }
  
  p {
    color: var(--color-text-secondary);
    margin: 0;
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

// 空状态
.empty-state {
  text-align: center;
  padding: 64px 24px;
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  
  .empty-icon {
    font-size: 64px;
    margin-bottom: 16px;
  }
  
  h3 {
    font-size: 18px;
    font-weight: 600;
    margin: 0 0 8px;
  }
  
  p {
    font-size: 14px;
    color: var(--color-text-secondary);
    margin: 0 0 24px;
  }
}

// 订单卡片
.order-card {
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  padding: 20px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid var(--color-border);
  
  &:hover {
    box-shadow: var(--shadow-lg);
    border-color: transparent;
  }
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  
  .order-number {
    font-size: 14px;
    font-family: var(--font-mono, monospace);
    color: var(--color-text-secondary);
  }
  
  .order-status {
    padding: 4px 12px;
    border-radius: var(--radius-full);
    font-size: 13px;
    font-weight: 500;
  }
}

.order-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  
  @media (max-width: 640px) {
    flex-direction: column;
    gap: 16px;
  }
}

.order-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
  
  .info-icon {
    font-size: 16px;
  }
  
  .info-text {
    font-size: 14px;
    color: var(--color-text-primary);
  }
  
  .days-badge {
    margin-left: 8px;
    padding: 2px 8px;
    background: var(--color-primary-light);
    color: var(--color-primary);
    font-size: 12px;
    border-radius: var(--radius-sm);
  }
}

.order-price {
  text-align: right;
  
  .price-label {
    display: block;
    font-size: 12px;
    color: var(--color-text-secondary);
    margin-bottom: 4px;
  }
  
  .price-value {
    font-size: 24px;
    font-weight: 700;
    color: var(--color-primary);
  }
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid var(--color-border);
  
  .order-time {
    font-size: 12px;
    color: var(--color-text-tertiary);
  }
}

.order-actions {
  display: flex;
  gap: 8px;
}
</style>
