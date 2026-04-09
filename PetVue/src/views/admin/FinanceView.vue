<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { DollarSign, TrendingUp, TrendingDown, Download, Calendar, CreditCard, ArrowUpRight, ArrowDownRight } from 'lucide-vue-next'
import { ElMessage, ElLoading } from 'element-plus'
import LineChart from '@/components/charts/LineChart.vue'
import BarChart from '@/components/charts/BarChart.vue'
import { adminApi } from '@/api/admin'
import { fenToYuan, formatAmount } from '@/utils/feeCalculator'

const selectedPeriod = ref<'week' | 'month' | 'year'>('month')

// 财务概览
const financeSummary = ref({
  totalRevenue: 0,
  revenueTrend: 0,
  platformFee: 0,
  feeTrend: 0,
  pendingSettlement: 0,
  completedSettlement: 0,
  refundAmount: 0,
  refundCount: 0
})

// 收入趋势
const revenueTrend = ref<{ name: string; value: number }[]>([])

// 机构收入排名
const institutionRevenue = ref<{ name: string; value: number }[]>([])

// 交易记录
const transactions = ref<any[]>([])

// 待结算机构
const pendingSettlements = ref<any[]>([])

const loading = ref(false)

const loadFinanceData = async () => {
  loading.value = true
  try {
    const [summaryRes, trendsRes, rankingRes, settlementsRes, transactionsRes] = await Promise.all([
      adminApi.getFinanceSummary(selectedPeriod.value),
      adminApi.getFinanceTrends(selectedPeriod.value),
      adminApi.getInstitutionRanking(),
      adminApi.getPendingSettlements(),
      adminApi.getTransactions(undefined, 1, 5)
    ])

    if (summaryRes.data) {
      financeSummary.value = summaryRes.data
    }
    if (trendsRes.data) {
      revenueTrend.value = trendsRes.data
    }
    if (rankingRes.data) {
      institutionRevenue.value = rankingRes.data
    }
    if (settlementsRes.data) {
      pendingSettlements.value = settlementsRes.data
    }
    if (transactionsRes.data?.list) {
      transactions.value = transactionsRes.data.list.map((t: any) => ({
        id: t.id,
        type: t.type,
        description: t.description,
        amount: t.type === 'income' ? fenToYuan(t.amount) : -fenToYuan(t.amount),
        fee: fenToYuan(t.fee),
        institution: t.institution || '',
        date: t.date,
        status: t.status
      }))
    }
  } catch (error) {
    console.error('加载财务数据失败:', error)
    ElMessage.error('加载财务数据失败')
  } finally {
    loading.value = false
  }
}

const getTransactionTypeLabel = (type: string) => {
  const map: Record<string, string> = { income: '收入', refund: '退款', settlement: '结算', recharge: '充值', withdrawal: '提现', payment: '支付' }
  return map[type] || type
}

const getTransactionTypeColor = (type: string) => {
  const map: Record<string, string> = { 
    income: '#52c41a', refund: '#ff4d4f', settlement: '#1890ff',
    recharge: '#722ed1', withdrawal: '#faad14', payment: '#13c2c2'
  }
  return map[type] || '#999'
}

const processSettlement = async (settlement: typeof pendingSettlements.value[0]) => {
  try {
    const loading = ElLoading.service({
      lock: true,
      text: '正在处理结算...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
    await adminApi.processSettlement(settlement.id)
    loading.close()
    ElMessage.success(`${settlement.institution} 结算处理成功`)
    await loadFinanceData()
  } catch (error) {
    console.error('处理结算失败:', error)
    ElMessage.error('处理结算失败')
  }
}

const exportFinanceReport = () => {
  ElMessage.success('正在导出财务报表...')
}

const changePeriod = (period: typeof selectedPeriod.value) => {
  selectedPeriod.value = period
  loadFinanceData()
}

onMounted(() => {
  loadFinanceData()
})
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
              {{ txn.amount > 0 ? '+' : '' }}¥{{ Math.abs(txn.amount).toLocaleString() }}
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
