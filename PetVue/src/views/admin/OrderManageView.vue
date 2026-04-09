<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { Search, Filter, Download, Eye, RefreshCw, Calendar, X } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api/admin'

const loading = ref(false)
const searchQuery = ref('')
const filterStatus = ref<string | null>(null)
const dateRange = ref<[string, string] | null>(null)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

interface Order {
  id: string
  orderNumber?: string
  userId?: string
  userName?: string
  userPhone?: string
  institutionId?: string
  institutionName?: string
  petName?: string
  servicePackageName?: string
  startDate?: string
  endDate?: string
  totalPrice?: number
  status?: string
  paymentStatus?: string
  paymentMethod?: string
  createdAt?: string
}

// 订单列表
const orders = ref<Order[]>([])
const viewModalVisible = ref(false)
const currentOrder = ref<Order | null>(null)

const statusOptions = [
  { value: 'pending', label: '待确认', color: '#faad14' },
  { value: 'confirmed', label: '已确认', color: '#1890ff' },
  { value: 'in_progress', label: '进行中', color: '#52c41a' },
  { value: 'completed', label: '已完成', color: '#6B6560' },
  { value: 'cancelled', label: '已取消', color: '#ff4d4f' }
]

const paymentStatusOptions = [
  { value: 'pending', label: '待支付', color: '#faad14' },
  { value: 'paid', label: '已支付', color: '#52c41a' },
  { value: 'refunded', label: '已退款', color: '#ff4d4f' },
  { value: 'partial_refund', label: '部分退款', color: '#1890ff' }
]

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await adminApi.getOrders(
      filterStatus.value || undefined,
      currentPage.value,
      pageSize.value
    )
    if (res.code === 200 && res.data) {
      orders.value = res.data.list || []
      total.value = res.data.pagination?.total || 0
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})

// 监听筛选变化
watch([filterStatus, currentPage, pageSize], () => {
  loadData()
})

let searchTimeout: any
watch(searchQuery, () => {
  if (searchTimeout) clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    currentPage.value = 1
    loadData()
  }, 500)
})

const filteredOrders = computed(() => {
  if (!searchQuery.value) return orders.value
  const query = searchQuery.value.toLowerCase()
  return orders.value.filter(o =>
    (o.orderNumber || o.id || '').toLowerCase().includes(query) ||
    (o.userName || '').toLowerCase().includes(query) ||
    (o.institutionName || '').toLowerCase().includes(query) ||
    (o.petName || '').toLowerCase().includes(query)
  )
})

const totalOrders = computed(() => total.value)
const totalRevenue = computed(() => orders.value.filter(o => o.paymentStatus === 'paid').reduce((sum, o) => sum + (o.totalPrice || 0), 0))
const totalPlatformFee = computed(() => Math.floor(totalRevenue.value * 0.1))

const getStatusLabel = (status: string) => statusOptions.find(s => s.value === status)?.label || status
const getStatusColor = (status: string) => statusOptions.find(s => s.value === status)?.color || '#999'
const getPaymentStatusLabel = (status: string) => paymentStatusOptions.find(s => s.value === status)?.label || status
const getPaymentStatusColor = (status: string) => paymentStatusOptions.find(s => s.value === status)?.color || '#999'
const getPetIcon = () => '🐾'

const viewOrder = async (order: Order) => {
  try {
    const res = await adminApi.getOrderDetail(order.id)
    if (res.code === 200) {
      currentOrder.value = res.data
      viewModalVisible.value = true
    }
  } catch (error) {
    currentOrder.value = order
    viewModalVisible.value = true
  }
}

const refreshData = async () => {
  loadData()
  ElMessage.success('数据已刷新')
}

const exportOrders = () => {
  ElMessage.success('正在导出订单数据...')
}
</script>

<template>
  <div class="order-manage-view">
    <div class="page-header">
      <div class="header-left">
        <h1>📋 订单管理</h1>
        <p>查看和管理平台所有订单</p>
      </div>
      <div class="header-actions">
        <button class="btn-refresh" @click="refreshData" :disabled="loading">
          <RefreshCw :size="16" :class="{ spinning: loading }" /> 刷新
        </button>
        <button class="btn-export" @click="exportOrders">
          <Download :size="16" /> 导出
        </button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">加载中...</div>

    <template v-else>
      <!-- 统计卡片 -->
      <div class="stats-row">
        <div class="stat-card">
          <span class="stat-value">{{ totalOrders }}</span>
          <span class="stat-label">总订单数</span>
        </div>
        <div class="stat-card">
          <span class="stat-value">¥{{ totalRevenue.toLocaleString() }}</span>
          <span class="stat-label">总交易额</span>
        </div>
        <div class="stat-card">
          <span class="stat-value">¥{{ totalPlatformFee.toLocaleString() }}</span>
          <span class="stat-label">平台收入</span>
        </div>
        <div class="stat-card">
          <span class="stat-value">{{ orders.filter(o => o.status === 'in_progress').length }}</span>
          <span class="stat-label">进行中</span>
        </div>
      </div>

      <!-- 筛选栏 -->
      <div class="filter-bar">
        <div class="search-box">
          <Search :size="16" />
          <input v-model="searchQuery" placeholder="搜索订单号、用户、机构、宠物..." />
        </div>
        <div class="filters">
          <select v-model="filterStatus">
            <option :value="null">全部状态</option>
            <option v-for="opt in statusOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
          </select>
        </div>
      </div>

      <!-- 订单表格 -->
      <div v-if="filteredOrders.length === 0" class="empty-state">
        <span class="empty-icon">📋</span>
        <p>暂无订单数据</p>
      </div>

      <div v-else class="table-card">
        <table class="data-table">
          <thead>
            <tr>
              <th>订单号</th>
              <th>用户</th>
              <th>机构</th>
              <th>宠物</th>
              <th>服务</th>
              <th>日期</th>
              <th>金额</th>
              <th>支付状态</th>
              <th>订单状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in filteredOrders" :key="order.id">
              <td class="order-id">{{ order.orderNumber || order.id }}</td>
              <td class="user-cell">
                <span class="user-name">{{ order.userName || '-' }}</span>
                <span class="user-phone">{{ order.userPhone || '-' }}</span>
              </td>
              <td>{{ order.institutionName || '-' }}</td>
              <td class="pet-cell">
                <span class="pet-icon">{{ getPetIcon() }}</span>
                {{ order.petName || '-' }}
              </td>
              <td><span class="service-tag">{{ order.servicePackageName || '-' }}</span></td>
              <td class="date-cell">
                <span>{{ order.startDate ? new Date(order.startDate).toLocaleDateString('zh-CN') : '-' }}</span>
                <span v-if="order.endDate" class="date-separator">~</span>
                <span v-if="order.endDate">{{ new Date(order.endDate).toLocaleDateString('zh-CN') }}</span>
              </td>
              <td class="amount">¥{{ order.totalPrice?.toFixed(2) || '0.00' }}</td>
              <td>
                <span v-if="order.paymentStatus" class="status-badge" :style="{ background: getPaymentStatusColor(order.paymentStatus) + '20', color: getPaymentStatusColor(order.paymentStatus) }">
                  {{ getPaymentStatusLabel(order.paymentStatus) }}
                </span>
                <span v-else>-</span>
              </td>
              <td>
                <span v-if="order.status" class="status-badge" :style="{ background: getStatusColor(order.status) + '20', color: getStatusColor(order.status) }">
                  {{ getStatusLabel(order.status) }}
                </span>
                <span v-else>-</span>
              </td>
              <td>
                <button class="action-btn" @click="viewOrder(order)">
                  <Eye :size="16" /> 详情
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 分页 -->
      <div v-if="total > 0" class="pagination">
        <span class="total">共 {{ total }} 条记录</span>
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="sizes, prev, pager, next"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </template>

    <!-- 查看订单详情模态框 -->
    <div v-if="viewModalVisible" class="modal-overlay" @click.self="viewModalVisible = false">
      <div class="modal">
        <div class="modal-header">
          <h3>订单详情</h3>
          <button class="modal-close" @click="viewModalVisible = false">
            <X :size="20" />
          </button>
        </div>
        <div class="modal-body" v-if="currentOrder">
          <div class="detail-item">
            <span class="label">订单号</span>
            <span class="value">{{ currentOrder.orderNumber || currentOrder.id }}</span>
          </div>
          <div class="detail-item">
            <span class="label">用户</span>
            <span class="value">{{ currentOrder.userName || '-' }} ({{ currentOrder.userPhone || '-' }})</span>
          </div>
          <div class="detail-item">
            <span class="label">机构</span>
            <span class="value">{{ currentOrder.institutionName || '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="label">宠物</span>
            <span class="value">{{ currentOrder.petName || '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="label">服务</span>
            <span class="value">{{ currentOrder.servicePackageName || '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="label">服务日期</span>
            <span class="value">
              {{ currentOrder.startDate ? new Date(currentOrder.startDate).toLocaleDateString('zh-CN') : '-' }}
              {{ currentOrder.endDate ? ' ~ ' + new Date(currentOrder.endDate).toLocaleDateString('zh-CN') : '' }}
            </span>
          </div>
          <div class="detail-item">
            <span class="label">订单金额</span>
            <span class="value amount">¥{{ currentOrder.totalPrice?.toFixed(2) || '0.00' }}</span>
          </div>
          <div class="detail-item">
            <span class="label">支付状态</span>
            <span v-if="currentOrder.paymentStatus" class="status-badge" :style="{ background: getPaymentStatusColor(currentOrder.paymentStatus) + '20', color: getPaymentStatusColor(currentOrder.paymentStatus) }">
              {{ getPaymentStatusLabel(currentOrder.paymentStatus) }}
            </span>
            <span v-else>-</span>
          </div>
          <div class="detail-item">
            <span class="label">订单状态</span>
            <span v-if="currentOrder.status" class="status-badge" :style="{ background: getStatusColor(currentOrder.status) + '20', color: getStatusColor(currentOrder.status) }">
              {{ getStatusLabel(currentOrder.status) }}
            </span>
            <span v-else>-</span>
          </div>
          <div class="detail-item">
            <span class="label">创建时间</span>
            <span class="value">{{ currentOrder.createdAt ? new Date(currentOrder.createdAt).toLocaleString('zh-CN') : '-' }}</span>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="viewModalVisible = false">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.order-manage-view { max-width: 1500px; margin: 0 auto; padding: 24px; }

.page-header {
  display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px;
  .header-left {
    h1 { font-size: 26px; font-weight: 700; margin: 0 0 6px; }
    p { color: #6B6560; margin: 0; }
  }
  .header-actions { display: flex; gap: 12px; }
}

.loading-state, .empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #9A958F;
  .empty-icon {
    font-size: 48px;
    display: block;
    margin-bottom: 12px;
    opacity: 0.5;
  }
  p { margin: 0; }
}

.btn-refresh, .btn-export {
  display: flex; align-items: center; gap: 8px; padding: 10px 20px;
  border: none; border-radius: 10px; font-size: 14px; cursor: pointer;
}
.btn-refresh { background: white; color: #6B6560; border: 1px solid #E5E5E5; }
.btn-export { background: #722ed1; color: white; }

.spinning { animation: spin 1s linear infinite; }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }

.stats-row {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px;
  .stat-card {
    background: white; padding: 20px; border-radius: 12px; text-align: center;
    .stat-value { display: block; font-size: 28px; font-weight: 700; color: #2D2A26; }
    .stat-label { font-size: 13px; color: #6B6560; }
  }
}

.filter-bar {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; gap: 16px; flex-wrap: wrap;
  .search-box {
    display: flex; align-items: center; gap: 10px; padding: 10px 16px;
    background: white; border-radius: 10px; border: 1px solid #E5E5E5; flex: 1; max-width: 400px;
    input { border: none; outline: none; font-size: 14px; width: 100%; }
  }
  .filters { display: flex; gap: 12px;
    select { padding: 10px 16px; border: 1px solid #E5E5E5; border-radius: 10px; font-size: 14px; background: white; }
  }
}

.table-card { background: white; border-radius: 16px; overflow: hidden; }

.data-table {
  width: 100%; border-collapse: collapse;
  th, td { padding: 14px 16px; text-align: left; border-bottom: 1px solid #F0F0EF; }
  th { background: #F8F8F7; font-size: 13px; font-weight: 600; color: #6B6560; }
  tbody tr:hover { background: #FAFAF9; }
}

.order-id { font-weight: 600; }
.user-cell {
  .user-name { display: block; font-weight: 600; }
  .user-phone { display: block; font-size: 12px; color: #6B6560; }
}
.pet-cell {
  display: flex; align-items: center; gap: 8px;
  .pet-icon { font-size: 20px; }
}
.service-tag {
  display: inline-block; padding: 4px 12px; background: #F0F0EF; border-radius: 12px; font-size: 12px;
}
.date-cell {
  display: flex; flex-direction: column; gap: 4px;
  .date-separator { font-size: 12px; color: #9A958F; }
}
.amount { font-weight: 600; color: #722ed1; }

.status-badge {
  display: inline-block; padding: 4px 12px; border-radius: 12px; font-size: 12px; font-weight: 500;
}

.action-btn {
  display: flex; align-items: center; gap: 4px; padding: 6px 12px;
  background: #F8F8F7; border: none; border-radius: 8px; cursor: pointer;
  font-size: 13px; color: #6B6560;
  &:hover { background: #F0F0EF; }
}

.pagination {
  display: flex; justify-content: space-between; align-items: center; margin-top: 20px; padding: 16px;
  background: white; border-radius: 12px;
  .total { font-size: 13px; color: #6B6560; }
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: 16px;
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #F0F0EF;
  h3 {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
  }
}

.modal-close {
  background: none;
  border: none;
  cursor: pointer;
  color: #9A958F;
  display: flex;
  align-items: center;
  justify-content: center;
  &:hover {
    color: #6B6560;
  }
}

.modal-body {
  padding: 24px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #F0F0EF;
  &:last-child {
    border-bottom: none;
  }
  .label {
    font-size: 14px;
    color: #9A958F;
  }
  .value {
    font-size: 14px;
    font-weight: 500;
    color: #2D2A26;
  }
  .amount {
    color: #722ed1;
    font-weight: 600;
  }
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #F0F0EF;
}

.btn-cancel {
  padding: 10px 24px;
  border: 1px solid #E5E5E5;
  background: white;
  color: #6B6560;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  &:hover {
    background: #F8F8F7;
  }
}
</style>
