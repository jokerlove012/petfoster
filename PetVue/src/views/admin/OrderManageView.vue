<script setup lang="ts">
import { ref, computed } from 'vue'
import { Search, Filter, Download, Eye, RefreshCw, Calendar } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const searchQuery = ref('')
const filterStatus = ref<string | null>(null)
const dateRange = ref<[string, string] | null>(null)
const currentPage = ref(1)
const pageSize = ref(20)

// 订单列表
const orders = ref([
  { id: 'ORD20250115001', user: '张三', userPhone: '138****1234', institution: '爱宠之家', pet: '旺财', petType: 'dog', service: '标准寄养', startDate: '2025-01-15', endDate: '2025-01-20', amount: 580, platformFee: 58, status: 'ongoing', paymentStatus: 'paid', paymentMethod: '微信支付', createdAt: '2025-01-14 10:30' },
  { id: 'ORD20250114002', user: '李四', userPhone: '139****5678', institution: '宠物乐园', pet: '小白', petType: 'cat', service: '豪华寄养', startDate: '2025-01-14', endDate: '2025-01-18', amount: 720, platformFee: 72, status: 'ongoing', paymentStatus: 'paid', paymentMethod: '支付宝', createdAt: '2025-01-13 15:20' },
  { id: 'ORD20250113003', user: '王五', userPhone: '137****9012', institution: '萌宠寄养', pet: '咪咪', petType: 'cat', service: '标准寄养', startDate: '2025-01-10', endDate: '2025-01-13', amount: 350, platformFee: 35, status: 'completed', paymentStatus: 'paid', paymentMethod: '微信支付', createdAt: '2025-01-09 09:15' },
  { id: 'ORD20250112004', user: '赵六', userPhone: '136****3456', institution: '爱宠之家', pet: '豆豆', petType: 'dog', service: 'VIP寄养', startDate: '2025-01-12', endDate: '2025-01-15', amount: 960, platformFee: 96, status: 'completed', paymentStatus: 'paid', paymentMethod: '银行卡', createdAt: '2025-01-11 14:00' },
  { id: 'ORD20250111005', user: '钱七', userPhone: '135****7890', institution: '温馨小窝', pet: '球球', petType: 'dog', service: '日托服务', startDate: '2025-01-11', endDate: '2025-01-11', amount: 120, platformFee: 12, status: 'cancelled', paymentStatus: 'refunded', paymentMethod: '微信支付', createdAt: '2025-01-10 08:30' }
])

const statusOptions = [
  { value: 'pending', label: '待确认', color: '#faad14' },
  { value: 'confirmed', label: '已确认', color: '#1890ff' },
  { value: 'ongoing', label: '进行中', color: '#52c41a' },
  { value: 'completed', label: '已完成', color: '#6B6560' },
  { value: 'cancelled', label: '已取消', color: '#ff4d4f' }
]

const paymentStatusOptions = [
  { value: 'pending', label: '待支付', color: '#faad14' },
  { value: 'paid', label: '已支付', color: '#52c41a' },
  { value: 'refunded', label: '已退款', color: '#ff4d4f' },
  { value: 'partial_refund', label: '部分退款', color: '#1890ff' }
]

const filteredOrders = computed(() => {
  let result = orders.value
  
  if (filterStatus.value) {
    result = result.filter(o => o.status === filterStatus.value)
  }
  
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(o =>
      o.id.toLowerCase().includes(query) ||
      o.user.toLowerCase().includes(query) ||
      o.institution.toLowerCase().includes(query) ||
      o.pet.toLowerCase().includes(query)
    )
  }
  
  return result
})

const totalOrders = computed(() => orders.value.length)
const totalRevenue = computed(() => orders.value.filter(o => o.paymentStatus === 'paid').reduce((sum, o) => sum + o.amount, 0))
const totalPlatformFee = computed(() => orders.value.filter(o => o.paymentStatus === 'paid').reduce((sum, o) => sum + o.platformFee, 0))

const getStatusLabel = (status: string) => statusOptions.find(s => s.value === status)?.label || status
const getStatusColor = (status: string) => statusOptions.find(s => s.value === status)?.color || '#999'
const getPaymentStatusLabel = (status: string) => paymentStatusOptions.find(s => s.value === status)?.label || status
const getPaymentStatusColor = (status: string) => paymentStatusOptions.find(s => s.value === status)?.color || '#999'
const getPetIcon = (type: string) => type === 'dog' ? '🐕' : type === 'cat' ? '🐱' : '🐾'

const viewOrder = (order: typeof orders.value[0]) => {
  ElMessage.info(`查看订单: ${order.id}`)
}

const refreshData = async () => {
  loading.value = true
  try {
    // TODO: 调用真实API刷新数据
    // await loadOrders()
    ElMessage.success('数据已刷新')
  } finally {
    loading.value = false
  }
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
        <span class="stat-value">{{ orders.filter(o => o.status === 'ongoing').length }}</span>
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
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          size="default"
        />
      </div>
    </div>

    <!-- 订单表格 -->
    <div class="table-card">
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
            <th>平台费</th>
            <th>支付状态</th>
            <th>订单状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="order in filteredOrders" :key="order.id">
            <td class="order-id">{{ order.id }}</td>
            <td class="user-cell">
              <span class="user-name">{{ order.user }}</span>
              <span class="user-phone">{{ order.userPhone }}</span>
            </td>
            <td>{{ order.institution }}</td>
            <td class="pet-cell">
              <span class="pet-icon">{{ getPetIcon(order.petType) }}</span>
              {{ order.pet }}
            </td>
            <td><span class="service-tag">{{ order.service }}</span></td>
            <td class="date-cell">
              <span>{{ order.startDate }}</span>
              <span class="date-separator">~</span>
              <span>{{ order.endDate }}</span>
            </td>
            <td class="amount">¥{{ order.amount }}</td>
            <td class="fee">¥{{ order.platformFee }}</td>
            <td>
              <span class="status-badge" :style="{ background: getPaymentStatusColor(order.paymentStatus) + '20', color: getPaymentStatusColor(order.paymentStatus) }">
                {{ getPaymentStatusLabel(order.paymentStatus) }}
              </span>
            </td>
            <td>
              <span class="status-badge" :style="{ background: getStatusColor(order.status) + '20', color: getStatusColor(order.status) }">
                {{ getStatusLabel(order.status) }}
              </span>
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
    <div class="pagination">
      <span class="total">共 {{ filteredOrders.length }} 条记录</span>
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="filteredOrders.length"
        :page-sizes="[10, 20, 50, 100]"
        layout="sizes, prev, pager, next"
      />
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

.btn-refresh, .btn-export {
  display: flex; align-items: center; gap: 8px; padding: 10px 20px;
  border: none; border-radius: 10px; font-size: 14px; cursor: pointer;
  .spinning { animation: spin 1s linear infinite; }
}
.btn-refresh { background: white; color: #6B6560; border: 1px solid #E5E5E5; &:disabled { opacity: 0.6; } }
.btn-export { background: #722ed1; color: white; }

@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }

.stats-row {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px;
  .stat-card {
    background: white; padding: 20px; border-radius: 12px; text-align: center;
    .stat-value { display: block; font-size: 24px; font-weight: 700; color: #2D2A26; }
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
  .filters { display: flex; gap: 12px; align-items: center;
    select { padding: 10px 16px; border: 1px solid #E5E5E5; border-radius: 10px; font-size: 14px; background: white; }
  }
}

.table-card { background: white; border-radius: 16px; overflow-x: auto; }

.data-table {
  width: 100%; border-collapse: collapse; min-width: 1200px;
  th, td { padding: 14px 12px; text-align: left; border-bottom: 1px solid #F0F0EF; white-space: nowrap; }
  th { background: #F8F8F7; font-size: 12px; font-weight: 600; color: #6B6560; }
  tbody tr:hover { background: #FAFAF9; }
}

.order-id { font-family: monospace; font-size: 13px; color: #6B6560; }

.user-cell {
  .user-name { display: block; font-weight: 500; }
  .user-phone { display: block; font-size: 12px; color: #9A958F; }
}

.pet-cell { display: flex; align-items: center; gap: 6px; .pet-icon { font-size: 16px; } }

.service-tag { display: inline-block; padding: 4px 10px; background: #F0F5FF; color: #1890ff; border-radius: 6px; font-size: 12px; }

.date-cell { font-size: 13px; .date-separator { margin: 0 4px; color: #9A958F; } }

.amount { font-weight: 700; color: #2D2A26; }
.fee { color: #722ed1; font-weight: 500; }

.status-badge { display: inline-block; padding: 4px 10px; border-radius: 12px; font-size: 12px; font-weight: 500; }

.action-btn {
  display: flex; align-items: center; gap: 4px; padding: 6px 12px;
  background: #E8F4FD; color: #1890ff; border: none; border-radius: 6px;
  font-size: 13px; cursor: pointer;
  &:hover { background: #D1E9FA; }
}

.pagination {
  display: flex; justify-content: space-between; align-items: center; margin-top: 20px; padding: 16px;
  background: white; border-radius: 12px;
  .total { font-size: 13px; color: #6B6560; }
}
</style>
