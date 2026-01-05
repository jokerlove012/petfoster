<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAutoRefresh } from '@/composables/useAutoRefresh'
import { AppButton, AppCard } from '@/components/common'
import type { Booking, BookingStatus } from '@/types/booking'
import api from '@/api/index'

const router = useRouter()

// 订单数据
const orders = ref<Booking[]>([])
const loading = ref(false)

const activeTab = ref<'pending' | 'confirmed' | 'in_progress' | 'all'>('pending')

// 加载订单列表
const loadOrders = async () => {
  try {
    const res = await api.get('/institution/bookings')
    if (res.code === 200 && res.data) {
      orders.value = res.data.list || res.data || []
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

// 自动刷新（5秒）
const { isAutoRefresh } = useAutoRefresh(loadOrders, 5000)

const filteredOrders = computed(() => {
  if (activeTab.value === 'all') return orders.value
  return orders.value.filter(o => o.status === activeTab.value)
})

const tabs = [
  { key: 'pending', label: '待确认', count: computed(() => orders.value.filter(o => o.status === 'pending').length) },
  { key: 'confirmed', label: '已确认', count: computed(() => orders.value.filter(o => o.status === 'confirmed').length) },
  { key: 'in_progress', label: '进行中', count: computed(() => orders.value.filter(o => o.status === 'in_progress').length) },
  { key: 'all', label: '全部', count: computed(() => orders.value.length) }
]

const statusLabels: Record<BookingStatus, string> = {
  pending: '待确认',
  confirmed: '已确认',
  in_progress: '进行中',
  completed: '已完成',
  cancelled: '已取消'
}

const statusColors: Record<BookingStatus, string> = {
  pending: 'warning',
  confirmed: 'primary',
  in_progress: 'success',
  completed: 'info',
  cancelled: 'danger'
}

// 获取用户和宠物信息（从订单扩展数据）
const getUserInfo = (order: Booking) => ({
  name: (order as any).userName || '未知用户',
  phone: (order as any).userPhone || ''
})

const getPetInfo = (order: Booking) => ({
  name: (order as any).petName || '未知宠物',
  species: (order as any).petSpecies || 'dog',
  breed: (order as any).petBreed || ''
})

const confirmOrder = async (order: Booking) => {
  try {
    const res = await api.post(`/institution/bookings/${order.id}/confirm`)
    if (res.code === 200) {
      order.status = 'confirmed'
      ElMessage.success('订单已确认')
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const rejectOrder = async (order: Booking) => {
  try {
    const res = await api.post(`/institution/bookings/${order.id}/reject`, { reason: '机构拒绝' })
    if (res.code === 200) {
      order.status = 'cancelled'
      ElMessage.success('订单已拒绝')
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const checkIn = async (order: Booking) => {
  try {
    const res = await api.post(`/institution/bookings/${order.id}/check-in`)
    if (res.code === 200) {
      order.status = 'in_progress'
      order.checkInTime = new Date().toISOString()
      ElMessage.success('已办理入住')
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const checkOut = async (order: Booking) => {
  try {
    const res = await api.post(`/institution/bookings/${order.id}/check-out`)
    if (res.code === 200) {
      order.status = 'completed'
      order.checkOutTime = new Date().toISOString()
      ElMessage.success('已办理退房')
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const formatDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}

const formatPrice = (price: number) => `¥${price}`

const deleteOrder = async (order: Booking) => {
  try {
    await ElMessageBox.confirm('确定要删除此订单吗？删除后无法恢复。', '删除订单', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await api.delete(`/institution/bookings/${order.id}`)
    if (res.code === 200) {
      orders.value = orders.value.filter(o => o.id !== order.id)
      ElMessage.success('订单已删除')
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
  <div class="order-manage-view">
    <div class="page-header">
      <h1>订单管理</h1>
      <p>管理客户预约订单</p>
    </div>

    <!-- 标签页 -->
    <div class="tabs">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        class="tab-btn"
        :class="{ active: activeTab === tab.key }"
        @click="activeTab = tab.key as any"
      >
        {{ tab.label }}
        <span v-if="tab.count.value > 0" class="tab-count">{{ tab.count.value }}</span>
      </button>
    </div>

    <!-- 订单列表 -->
    <div class="orders-list">
      <div v-if="filteredOrders.length === 0" class="empty-state">
        <span class="empty-icon">📋</span>
        <p>暂无订单</p>
      </div>

      <AppCard
        v-for="order in filteredOrders"
        :key="order.id"
        class="order-card"
        shadow="sm"
      >
        <div class="order-header">
          <span class="order-number">{{ order.orderNumber }}</span>
          <el-tag :type="statusColors[order.status]" size="small">
            {{ statusLabels[order.status] }}
          </el-tag>
        </div>

        <div class="order-body">
          <div class="info-section">
            <h4>客户信息</h4>
            <p>{{ getUserInfo(order).name }} · {{ getUserInfo(order).phone }}</p>
          </div>

          <div class="info-section">
            <h4>宠物信息</h4>
            <p>
              {{ getPetInfo(order).species === 'dog' ? '🐕' : '🐱' }}
              {{ getPetInfo(order).name }} · {{ getPetInfo(order).breed }}
            </p>
          </div>

          <div class="info-section">
            <h4>寄养时间</h4>
            <p>{{ formatDate(order.startDate) }} - {{ formatDate(order.endDate) }} ({{ order.totalDays }}天)</p>
          </div>

          <div class="info-section">
            <h4>订单金额</h4>
            <p class="price">{{ formatPrice(order.totalPrice) }}</p>
          </div>
        </div>

        <div class="order-actions">
          <!-- 待确认状态 -->
          <template v-if="order.status === 'pending'">
            <AppButton type="primary" size="sm" @click="confirmOrder(order)">确认接单</AppButton>
            <AppButton type="ghost" size="sm" @click="rejectOrder(order)">拒绝</AppButton>
          </template>

          <!-- 已确认状态 -->
          <template v-else-if="order.status === 'confirmed'">
            <AppButton type="primary" size="sm" @click="checkIn(order)">办理入住</AppButton>
          </template>

          <!-- 进行中状态 -->
          <template v-else-if="order.status === 'in_progress'">
            <AppButton type="outline" size="sm" @click="router.push(`/institution/health/${order.id}`)">
              记录健康
            </AppButton>
            <AppButton type="primary" size="sm" @click="checkOut(order)">办理退房</AppButton>
          </template>

          <!-- 已完成或已取消状态可删除 -->
          <template v-if="order.status === 'completed' || order.status === 'cancelled'">
            <AppButton type="danger" size="sm" @click="deleteOrder(order)">删除</AppButton>
          </template>

          <!-- 通用操作 -->
          <AppButton type="ghost" size="sm" @click="router.push(`/order/${order.id}`)">
            查看详情
          </AppButton>
        </div>
      </AppCard>
    </div>
  </div>
</template>


<style lang="scss" scoped>
.order-manage-view {
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;

  h1 {
    font-family: var(--font-display);
    font-size: 28px;
    margin: 0 0 4px;
  }

  p {
    color: var(--color-text-secondary);
    margin: 0;
  }
}

.tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-border);
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  border: none;
  background: transparent;
  color: var(--color-text-secondary);
  font-size: 14px;
  font-weight: 500;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 150ms ease;

  &:hover {
    background: var(--color-neutral-100);
  }

  &.active {
    background: var(--color-primary);
    color: white;

    .tab-count {
      background: rgba(255, 255, 255, 0.2);
      color: white;
    }
  }

  .tab-count {
    padding: 2px 8px;
    background: var(--color-neutral-200);
    color: var(--color-text-secondary);
    font-size: 12px;
    border-radius: 10px;
  }
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;

  .empty-icon {
    font-size: 48px;
    display: block;
    margin-bottom: 12px;
    opacity: 0.5;
  }

  p {
    color: var(--color-text-muted);
    margin: 0;
  }
}

.order-card {
  .order-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid var(--color-border);

    .order-number {
      font-size: 14px;
      font-weight: 600;
      color: var(--color-text-primary);
    }
  }

  .order-body {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
    margin-bottom: 16px;

    @media (max-width: 768px) {
      grid-template-columns: repeat(2, 1fr);
    }
  }

  .info-section {
    h4 {
      font-size: 12px;
      font-weight: 500;
      color: var(--color-text-muted);
      margin: 0 0 4px;
    }

    p {
      font-size: 14px;
      color: var(--color-text-primary);
      margin: 0;

      &.price {
        font-size: 18px;
        font-weight: 700;
        color: var(--color-primary);
      }
    }
  }

  .order-actions {
    display: flex;
    gap: 8px;
    padding-top: 16px;
    border-top: 1px solid var(--color-border);
  }
}
</style>

