<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { RotateCcw, Search, Filter, Check, X, Eye, Clock, AlertCircle, CheckCircle } from 'lucide-vue-next'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin'

const searchKeyword = ref('')
const statusFilter = ref('all')
const dateRange = ref<[string, string] | null>(null)
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

interface Refund {
  id: string
  orderId?: string
  user?: string
  phone?: string
  institution?: string
  amount?: number
  reason?: string
  status?: string
  applyTime?: string
  images?: string[]
  processTime?: string
  rejectReason?: string
}

// 退款统计
const refundStats = ref({
  pending: 0,
  processing: 0,
  completed: 0,
  rejected: 0,
  totalAmount: 0
})

// 退款列表
const refundList = ref<Refund[]>([])

const filteredList = computed(() => {
  if (!searchKeyword.value) return refundList.value
  const query = searchKeyword.value.toLowerCase()
  return refundList.value.filter(item =>
    (item.orderId || item.id || '').toLowerCase().includes(query) ||
    (item.user || '').toLowerCase().includes(query)
  )
})

// 加载统计数据
const loadStats = async () => {
  try {
    const res = await adminApi.getRefundStats()
    if (res.code === 200 && res.data) {
      refundStats.value = res.data
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载退款列表
const loadRefunds = async () => {
  loading.value = true
  try {
    const status = statusFilter.value === 'all' ? undefined : statusFilter.value
    const res = await adminApi.getRefunds(status, currentPage.value, pageSize.value)
    if (res.code === 200 && res.data) {
      refundList.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('加载退款列表失败:', error)
    ElMessage.error('加载退款列表失败')
  } finally {
    loading.value = false
  }
}

const loadData = async () => {
  await Promise.all([loadStats(), loadRefunds()])
}

onMounted(() => {
  loadData()
})

watch([statusFilter, currentPage], () => {
  loadRefunds()
})

let searchTimeout: any
watch(searchKeyword, () => {
  if (searchTimeout) clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    currentPage.value = 1
    loadRefunds()
  }, 500)
})

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = { pending: '待处理', processing: '处理中', completed: '已完成', rejected: '已拒绝' }
  return map[status] || status
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = { pending: 'warning', processing: 'info', completed: 'success', rejected: 'danger' }
  return map[status] || 'info'
}

const viewDetail = async (refund: Refund) => {
  try {
    const res = await adminApi.getRefundDetail(refund.id)
    if (res.code === 200) {
      ElMessage.success('查看详情功能即将上线')
    }
  } catch (error) {
    ElMessage.info(`查看退款详情: ${refund.id}`)
  }
}

const approveRefund = async (refund: Refund) => {
  try {
    await ElMessageBox.confirm(`确认同意退款 ¥${refund.amount} 给用户 ${refund.user}？`, '确认退款', { type: 'warning' })
    const res = await adminApi.approveRefund(refund.id)
    if (res.code === 200) {
      ElMessage.success('退款已批准，款项将在1-3个工作日内退回')
      loadData()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const rejectRefund = async (refund: Refund) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝退款', { inputPattern: /.+/, inputErrorMessage: '请输入拒绝原因' })
    const res = await adminApi.rejectRefund(refund.id, value)
    if (res.code === 200) {
      ElMessage.success('已拒绝退款申请')
      loadData()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}
</script>

<template>
  <div class="refund-manage">
    <div class="page-header">
      <div class="header-left">
        <h1>💸 退款管理</h1>
        <p>处理用户退款申请</p>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card pending">
        <Clock :size="24" />
        <div class="stat-info">
          <span class="stat-value">{{ refundStats.pending }}</span>
          <span class="stat-label">待处理</span>
        </div>
      </div>
      <div class="stat-card processing">
        <RotateCcw :size="24" />
        <div class="stat-info">
          <span class="stat-value">{{ refundStats.processing }}</span>
          <span class="stat-label">处理中</span>
        </div>
      </div>
      <div class="stat-card completed">
        <CheckCircle :size="24" />
        <div class="stat-info">
          <span class="stat-value">{{ refundStats.completed }}</span>
          <span class="stat-label">已完成</span>
        </div>
      </div>
      <div class="stat-card amount">
        <AlertCircle :size="24" />
        <div class="stat-info">
          <span class="stat-value">¥{{ refundStats.totalAmount.toLocaleString() }}</span>
          <span class="stat-label">累计退款</span>
        </div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="search-box">
        <Search :size="18" />
        <input v-model="searchKeyword" placeholder="搜索订单号/用户名" />
      </div>
      <div class="status-tabs">
        <button v-for="s in ['all', 'pending', 'processing', 'completed', 'rejected']" :key="s" 
          :class="{ active: statusFilter === s }" @click="statusFilter = s">
          {{ { all: '全部', pending: '待处理', processing: '处理中', completed: '已完成', rejected: '已拒绝' }[s] }}
        </button>
      </div>
    </div>

    <!-- 退款列表 -->
    <div class="refund-list">
      <div v-for="item in filteredList" :key="item.id" class="refund-card">
        <div class="refund-header">
          <span class="refund-id">{{ item.id }}</span>
          <el-tag :type="getStatusType(item.status)" size="small">{{ getStatusLabel(item.status) }}</el-tag>
        </div>
        <div class="refund-body">
          <div class="info-row">
            <span class="label">订单号:</span>
            <span class="value">{{ item.orderId }}</span>
          </div>
          <div class="info-row">
            <span class="label">用户:</span>
            <span class="value">{{ item.user }} ({{ item.phone }})</span>
          </div>
          <div class="info-row">
            <span class="label">机构:</span>
            <span class="value">{{ item.institution }}</span>
          </div>
          <div class="info-row">
            <span class="label">退款金额:</span>
            <span class="value amount">¥{{ item.amount }}</span>
          </div>
          <div class="info-row">
            <span class="label">退款原因:</span>
            <span class="value">{{ item.reason }}</span>
          </div>
          <div class="info-row">
            <span class="label">申请时间:</span>
            <span class="value">{{ item.applyTime }}</span>
          </div>
        </div>
        <div class="refund-actions">
          <button class="btn-view" @click="viewDetail(item)"><Eye :size="14" /> 详情</button>
          <template v-if="item.status === 'pending'">
            <button class="btn-approve" @click="approveRefund(item)"><Check :size="14" /> 同意</button>
            <button class="btn-reject" @click="rejectRefund(item)"><X :size="14" /> 拒绝</button>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.refund-manage { max-width: 1200px; margin: 0 auto; padding: 24px; }

.page-header {
  margin-bottom: 24px;
  h1 { font-size: 26px; font-weight: 700; margin: 0 0 6px; }
  p { color: #6B6560; margin: 0; }
}

.stats-grid {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px;
  @media (max-width: 768px) { grid-template-columns: repeat(2, 1fr); }
}

.stat-card {
  display: flex; align-items: center; gap: 16px; padding: 20px;
  background: white; border-radius: 14px; box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  &.pending { color: #faad14; }
  &.processing { color: #1890ff; }
  &.completed { color: #52c41a; }
  &.amount { color: #722ed1; }
  .stat-info {
    .stat-value { display: block; font-size: 24px; font-weight: 700; color: #2D2A26; }
    .stat-label { font-size: 13px; color: #6B6560; }
  }
}

.filter-bar {
  display: flex; gap: 16px; margin-bottom: 20px; flex-wrap: wrap;
  .search-box {
    display: flex; align-items: center; gap: 10px; padding: 10px 16px;
    background: white; border-radius: 10px; flex: 1; min-width: 200px;
    input { border: none; outline: none; flex: 1; font-size: 14px; }
    color: #9A958F;
  }
  .status-tabs {
    display: flex; background: white; border-radius: 10px; padding: 4px;
    button {
      padding: 8px 16px; border: none; background: transparent; font-size: 13px;
      color: #6B6560; cursor: pointer; border-radius: 6px; white-space: nowrap;
      &.active { background: #722ed1; color: white; }
    }
  }
}

.refund-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(360px, 1fr)); gap: 16px; }

.refund-card {
  background: white; border-radius: 14px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  .refund-header {
    display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;
    .refund-id { font-weight: 600; color: #2D2A26; }
  }
  .refund-body {
    .info-row {
      display: flex; margin-bottom: 10px; font-size: 14px;
      .label { width: 80px; color: #9A958F; flex-shrink: 0; }
      .value { color: #2D2A26; &.amount { color: #ff4d4f; font-weight: 600; } }
    }
  }
  .refund-actions {
    display: flex; gap: 10px; margin-top: 16px; padding-top: 16px; border-top: 1px solid #F0EFED;
    button {
      display: flex; align-items: center; gap: 6px; padding: 8px 14px;
      border: none; border-radius: 8px; font-size: 13px; cursor: pointer;
      &.btn-view { background: #F8F8F7; color: #6B6560; }
      &.btn-approve { background: #52c41a; color: white; }
      &.btn-reject { background: #ff4d4f; color: white; }
    }
  }
}
</style>
