<script setup lang="ts">
import { ref, computed } from 'vue'
import { DollarSign, TrendingUp, TrendingDown, Download, Calendar, CreditCard, ArrowUpRight, ArrowDownRight } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'
import LineChart from '@/components/charts/LineChart.vue'
import BarChart from '@/components/charts/BarChart.vue'

const selectedPeriod = ref<'week' | 'month' | 'year'>('month')
const dateRange = ref<[string, string] | null>(null)

// 财务概览
const financeSummary = ref({
  totalRevenue: 458920,
  revenueTrend: 18.5,
  platformFee: 45892,
  feeTrend: 15.2,
  pendingSettlement: 12580,
  completedSettlement: 398450,
  refundAmount: 8650,
  refundCount: 23
})

// 收入趋势
const revenueTrend = ref([
  { name: '1月', value: 320000 },
  { name: '2月', value: 280000 },
  { name: '3月', value: 350000 },
  { name: '4月', value: 420000 },
  { name: '5月', value: 380000 },
  { name: '6月', value: 458920 }
])

// 机构收入排名
const institutionRevenue = ref([
  { name: '爱宠之家', value: 89600 },
  { name: '宠物乐园', value: 76800 },
  { name: '萌宠寄养', value: 68500 },
  { name: '温馨小窝', value: 52300 },
  { name: '宠爱有家', value: 45600 }
])

// 交易记录
const transactions = ref([
  { id: 'TXN001', type: 'income', description: '订单收入 - ORD20250115001', amount: 580, fee: 58, institution: '爱宠之家', date: '2025-01-15 10:30', status: 'completed' },
  { id: 'TXN002', type: 'income', description: '订单收入 - ORD20250114002', amount: 720, fee: 72, institution: '宠物乐园', date: '2025-01-14 15:20', status: 'completed' },
  { id: 'TXN003', type: 'refund', description: '退款 - ORD20250111005', amount: -120, fee: -12, institution: '温馨小窝', date: '2025-01-11 16:00', status: 'completed' },
  { id: 'TXN004', type: 'settlement', description: '结算 - 爱宠之家', amount: -25000, fee: 0, institution: '爱宠之家', date: '2025-01-10 09:00', status: 'completed' },
  { id: 'TXN005', type: 'income', description: '订单收入 - ORD20250109003', amount: 350, fee: 35, institution: '萌宠寄养', date: '2025-01-09 11:45', status: 'completed' }
])

// 待结算机构
const pendingSettlements = ref([
  { id: '1', institution: '爱宠之家', amount: 5680, orders: 12, lastSettlement: '2025-01-10' },
  { id: '2', institution: '宠物乐园', amount: 3450, orders: 8, lastSettlement: '2025-01-08' },
  { id: '3', institution: '萌宠寄养', amount: 2150, orders: 5, lastSettlement: '2025-01-05' }
])

const getTransactionTypeLabel = (type: string) => {
  const map: Record<string, string> = { income: '收入', refund: '退款', settlement: '结算' }
  return map[type] || type
}

const getTransactionTypeColor = (type: string) => {
  const map: Record<string, string> = { income: '#52c41a', refund: '#ff4d4f', settlement: '#1890ff' }
  return map[type] || '#999'
}

const processSettlement = (settlement: typeof pendingSettlements.value[0]) => {
  ElMessage.success(`正在处理 ${settlement.institution} 的结算...`)
}

const exportFinanceReport = () => {
  ElMessage.success('正在导出财务报表...')
}

const changePeriod = (period: typeof selectedPeriod.value) => {
  selectedPeriod.value = period
}
</script>

<template>
  <div class="finance-view">
    <div class="page-header">
      <div class="header-left">
        <h1>💰 财务管理</h1>
        <p>平台收入与结算管理</p>
      </div>
      <div class="header-actions">
        <div class="period-selector">
          <button v-for="p in ['week', 'month', 'year']" :key="p" class="period-btn" :class="{ active: selectedPeriod === p }" @click="changePeriod(p as any)">
            {{ { week: '本周', month: '本月', year: '本年' }[p] }}
          </button>
        </div>
        <button class="btn-export" @click="exportFinanceReport">
          <Download :size="16" /> 导出报表
        </button>
      </div>
    </div>

    <!-- 财务概览 -->
    <div class="stats-grid">
      <div class="stat-card revenue">
        <div class="stat-icon"><DollarSign :size="24" /></div>
        <div class="stat-content">
          <span class="stat-label">总交易额</span>
          <span class="stat-value">¥{{ financeSummary.totalRevenue.toLocaleString() }}</span>
          <span class="stat-trend up">
            <TrendingUp :size="14" /> +{{ financeSummary.revenueTrend }}%
          </span>
        </div>
      </div>
      <div class="stat-card fee">
        <div class="stat-icon"><CreditCard :size="24" /></div>
        <div class="stat-content">
          <span class="stat-label">平台收入</span>
          <span class="stat-value">¥{{ financeSummary.platformFee.toLocaleString() }}</span>
          <span class="stat-trend up">
            <TrendingUp :size="14" /> +{{ financeSummary.feeTrend }}%
          </span>
        </div>
      </div>
      <div class="stat-card pending">
        <div class="stat-icon"><Calendar :size="24" /></div>
        <div class="stat-content">
          <span class="stat-label">待结算</span>
          <span class="stat-value">¥{{ financeSummary.pendingSettlement.toLocaleString() }}</span>
        </div>
      </div>
      <div class="stat-card refund">
        <div class="stat-icon"><ArrowDownRight :size="24" /></div>
        <div class="stat-content">
          <span class="stat-label">退款金额</span>
          <span class="stat-value">¥{{ financeSummary.refundAmount.toLocaleString() }}</span>
          <span class="stat-sub">{{ financeSummary.refundCount }} 笔</span>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-grid">
      <div class="chart-card">
        <div class="card-header">
          <h3>📈 收入趋势</h3>
        </div>
        <LineChart :data="revenueTrend" color="#722ed1" />
      </div>
      <div class="chart-card">
        <div class="card-header">
          <h3>🏆 机构收入排名</h3>
        </div>
        <BarChart :data="institutionRevenue" color="#52c41a" :horizontal="true" />
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="main-grid">
      <!-- 交易记录 -->
      <div class="section-card">
        <div class="card-header">
          <h3>📝 交易记录</h3>
          <router-link to="/admin/finance/transactions" class="view-all">查看全部 →</router-link>
        </div>
        <div class="transactions-list">
          <div v-for="txn in transactions" :key="txn.id" class="transaction-item">
            <div class="txn-icon" :style="{ background: getTransactionTypeColor(txn.type) + '20', color: getTransactionTypeColor(txn.type) }">
              <ArrowUpRight v-if="txn.type === 'income'" :size="18" />
              <ArrowDownRight v-else :size="18" />
            </div>
            <div class="txn-info">
              <span class="txn-desc">{{ txn.description }}</span>
              <span class="txn-meta">{{ txn.institution }} · {{ txn.date }}</span>
            </div>
            <div class="txn-amount" :class="{ negative: txn.amount < 0 }">
              {{ txn.amount > 0 ? '+' : '' }}¥{{ Math.abs(txn.amount) }}
            </div>
          </div>
        </div>
      </div>

      <!-- 待结算 -->
      <div class="section-card">
        <div class="card-header">
          <h3>⏳ 待结算机构</h3>
        </div>
        <div class="settlements-list">
          <div v-for="item in pendingSettlements" :key="item.id" class="settlement-item">
            <div class="settlement-info">
              <span class="settlement-name">{{ item.institution }}</span>
              <span class="settlement-meta">{{ item.orders }} 笔订单 · 上次结算: {{ item.lastSettlement }}</span>
            </div>
            <div class="settlement-amount">¥{{ item.amount.toLocaleString() }}</div>
            <button class="btn-settle" @click="processSettlement(item)">结算</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.finance-view { max-width: 1400px; margin: 0 auto; padding: 24px; }

.page-header {
  display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; flex-wrap: wrap; gap: 16px;
  .header-left {
    h1 { font-size: 26px; font-weight: 700; margin: 0 0 6px; }
    p { color: #6B6560; margin: 0; }
  }
  .header-actions { display: flex; gap: 12px; align-items: center; }
}

.period-selector {
  display: flex; background: white; border-radius: 10px; padding: 4px;
  .period-btn {
    padding: 8px 16px; border: none; background: transparent; font-size: 13px;
    color: #6B6560; cursor: pointer; border-radius: 6px;
    &.active { background: #722ed1; color: white; }
  }
}

.btn-export {
  display: flex; align-items: center; gap: 8px; padding: 10px 20px;
  background: #722ed1; color: white; border: none; border-radius: 10px; font-size: 14px; cursor: pointer;
}

.stats-grid {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-bottom: 24px;
  @media (max-width: 1024px) { grid-template-columns: repeat(2, 1fr); }
}

.stat-card {
  display: flex; align-items: center; gap: 16px; padding: 24px;
  background: white; border-radius: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  .stat-icon {
    width: 56px; height: 56px; border-radius: 14px; display: flex; align-items: center; justify-content: center;
  }
  &.revenue .stat-icon { background: linear-gradient(135deg, #F3EEFF, #E8DEFF); color: #722ed1; }
  &.fee .stat-icon { background: linear-gradient(135deg, #E8F8E8, #D4F0D4); color: #52c41a; }
  &.pending .stat-icon { background: linear-gradient(135deg, #FFF8E6, #FFEFC7); color: #faad14; }
  &.refund .stat-icon { background: linear-gradient(135deg, #FEE2E2, #FECACA); color: #ff4d4f; }
  .stat-content { flex: 1;
    .stat-label { display: block; font-size: 13px; color: #6B6560; margin-bottom: 4px; }
    .stat-value { display: block; font-size: 24px; font-weight: 700; color: #2D2A26; }
    .stat-trend { display: flex; align-items: center; gap: 4px; font-size: 13px; font-weight: 600; margin-top: 4px;
      &.up { color: #52c41a; }
      &.down { color: #ff4d4f; }
    }
    .stat-sub { font-size: 12px; color: #9A958F; }
  }
}

.charts-grid {
  display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 24px;
  @media (max-width: 900px) { grid-template-columns: 1fr; }
}

.chart-card {
  background: white; border-radius: 16px; padding: 20px;
  .card-header { margin-bottom: 16px; h3 { font-size: 16px; font-weight: 600; margin: 0; } }
}

.main-grid {
  display: grid; grid-template-columns: 1fr 400px; gap: 24px;
  @media (max-width: 1024px) { grid-template-columns: 1fr; }
}

.section-card {
  background: white; border-radius: 16px; padding: 24px;
  .card-header {
    display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;
    h3 { font-size: 16px; font-weight: 600; margin: 0; }
    .view-all { font-size: 13px; color: #722ed1; text-decoration: none; }
  }
}

.transactions-list { display: flex; flex-direction: column; gap: 12px; }
.transaction-item {
  display: flex; align-items: center; gap: 14px; padding: 14px; background: #F8F8F7; border-radius: 12px;
  .txn-icon { width: 40px; height: 40px; border-radius: 10px; display: flex; align-items: center; justify-content: center; }
  .txn-info { flex: 1;
    .txn-desc { display: block; font-size: 14px; font-weight: 500; }
    .txn-meta { font-size: 12px; color: #9A958F; }
  }
  .txn-amount { font-size: 16px; font-weight: 700; color: #52c41a;
    &.negative { color: #ff4d4f; }
  }
}

.settlements-list { display: flex; flex-direction: column; gap: 12px; }
.settlement-item {
  display: flex; align-items: center; gap: 14px; padding: 14px; background: #F8F8F7; border-radius: 12px;
  .settlement-info { flex: 1;
    .settlement-name { display: block; font-size: 14px; font-weight: 600; }
    .settlement-meta { font-size: 12px; color: #9A958F; }
  }
  .settlement-amount { font-size: 16px; font-weight: 700; color: #2D2A26; }
  .btn-settle {
    padding: 8px 16px; background: #722ed1; color: white; border: none;
    border-radius: 8px; font-size: 13px; cursor: pointer;
    &:hover { background: #531dab; }
  }
}
</style>
