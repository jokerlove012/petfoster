<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElDropdown, ElDropdownMenu, ElDropdownItem, ElDatePicker } from 'element-plus'
import { AppCard } from '@/components/common'
import { adminApi } from '@/api/admin'
import { exportToCSV, exportToPDF, createPlatformReport, formatDateRange } from '@/utils/reportExporter'

const dateMode = ref<'quick' | 'custom'>('quick')
const quickRange = ref('month')
const customDateRange = ref<[Date, Date] | null>(null)
const isExporting = ref(false)
const loading = ref(false)

const userStats = ref({ total: 0, newThisMonth: 0, activeUsers: 0, growth: 0 })
const orderStats = ref({ total: 0, thisMonth: 0, revenue: 0, avgOrderValue: 0 })
const institutionStats = ref({ total: 0, active: 0, pending: 0, suspended: 0 })
const trendData = ref<any[]>([])
const topInstitutions = ref<any[]>([])

const loadDashboardData = async () => {
  loading.value = true
  try {
    let period = 'month'
    let startDate: string | undefined
    let endDate: string | undefined
    
    if (dateMode.value === 'quick') {
      period = quickRange.value
    } else if (customDateRange.value) {
      const formatDate = (date: Date) => {
        const d = new Date(date)
        const year = d.getFullYear()
        const month = String(d.getMonth() + 1).padStart(2, '0')
        const day = String(d.getDate()).padStart(2, '0')
        return `${year}-${month}-${day}`
      }
      startDate = formatDate(customDateRange.value[0])
      endDate = formatDate(customDateRange.value[1])
    }
    
    console.log('AnalyticsView 加载数据 - mode:', dateMode.value, 'period:', period, 'startDate:', startDate, 'endDate:', endDate)
    
    const res = await adminApi.getDashboardStats(period, startDate, endDate)
    console.log('AnalyticsView API 响应:', res)
    
    if (res.data) {
      const data = res.data
      console.log('AnalyticsView 数据:', data)
      userStats.value = {
        total: data.totalUsers || 0,
        newThisMonth: data.newUsersToday || 0,
        activeUsers: data.activeUsers || 0,
        growth: 12.5
      }
      orderStats.value = {
        total: data.totalOrders || 0,
        thisMonth: data.completedOrders || 0,
        revenue: data.totalRevenue || 0,
        avgOrderValue: data.avgOrderValue || 0
      }
      institutionStats.value = {
        total: data.totalInstitutions || 0,
        active: data.totalInstitutions || 0,
        pending: data.pendingInstitutions || 0,
        suspended: 0
      }
      if (data.revenueTrend) {
        trendData.value = data.revenueTrend.map((item: any) => ({
          month: item.name,
          revenue: parseFloat(item.value) || 0
        }))
      }
      if (data.institutionRanking) {
        topInstitutions.value = data.institutionRanking.map((item: any) => ({
          name: item.name,
          orders: item.value || 0,
          revenue: parseFloat(item.revenue) || 0,
          rating: item.rating || 0
        }))
      }
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

const formatPrice = (price: number) => `¥${price.toLocaleString()}`

const handleExport = async (format: 'csv' | 'pdf') => {
  isExporting.value = true
  try {
    const report = createPlatformReport(quickRange.value, userStats.value, orderStats.value, institutionStats.value, trendData.value, topInstitutions.value)
    const filename = `平台数据报表_${formatDateRange(quickRange.value).replace(/\s/g, '_')}`
    if (format === 'csv') {
      exportToCSV(report.sections, filename)
      ElMessage.success('CSV 报表导出成功')
    } else {
      exportToPDF(report, filename)
      ElMessage.success('PDF 报表已生成')
    }
  } catch (error) {
    ElMessage.error('导出失败，请重试')
  } finally {
    isExporting.value = false
  }
}

const handleQuickRangeChange = (event: Event) => {
  const target = event.target as HTMLSelectElement
  dateMode.value = 'quick'
  quickRange.value = target.value
  customDateRange.value = null
  loadDashboardData()
}

const handleDateRangeChange = (range: any) => {
  console.log('AnalyticsView 日期范围变化:', range)
  if (range && range.length === 2) {
    dateMode.value = 'custom'
    customDateRange.value = range
    loadDashboardData()
  }
}

onMounted(() => { loadDashboardData() })
</script>

<template>
  <div class="analytics-view" v-loading="loading">
    <div class="page-header">
      <div class="header-info">
        <h1>数据分析</h1>
        <p>平台运营数据概览</p>
      </div>
      <div class="header-actions">
        <div class="date-selector">
          <div class="mode-tabs">
            <button 
              :class="['mode-tab', { active: dateMode === 'quick' }]"
              @click="dateMode = 'quick'; customDateRange = null; loadDashboardData()"
            >
              快捷选择
            </button>
            <button 
              :class="['mode-tab', { active: dateMode === 'custom' }]"
              @click="dateMode = 'custom'"
            >
              自定义
            </button>
          </div>
          
          <template v-if="dateMode === 'quick'">
            <select :value="quickRange" @change="handleQuickRangeChange" class="quick-select">
              <option value="week">本周</option>
              <option value="month">本月</option>
              <option value="quarter">本季度</option>
              <option value="year">本年</option>
            </select>
          </template>
          
          <template v-else>
            <el-date-picker
              v-model="customDateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              @change="handleDateRangeChange"
              class="date-range-picker"
            />
          </template>
        </div>
        <ElDropdown trigger="click" @command="handleExport">
          <button class="export-btn" :disabled="isExporting">
            <span v-if="isExporting">导出中...</span>
            <span v-else>📥 导出报表</span>
          </button>
          <template #dropdown>
            <ElDropdownMenu>
              <ElDropdownItem command="csv"><span class="dropdown-icon">📊</span>导出 CSV</ElDropdownItem>
              <ElDropdownItem command="pdf"><span class="dropdown-icon">📄</span>导出 PDF</ElDropdownItem>
            </ElDropdownMenu>
          </template>
        </ElDropdown>
      </div>
    </div>

    <!-- 核心指标 -->
    <div class="metrics-grid">
      <AppCard class="metric-card" shadow="sm">
        <div class="metric-icon users">👥</div>
        <div class="metric-content">
          <span class="metric-value">{{ userStats.total.toLocaleString() }}</span>
          <span class="metric-label">总用户数</span>
          <span class="metric-change positive">+{{ userStats.growth }}%</span>
        </div>
      </AppCard>
      <AppCard class="metric-card" shadow="sm">
        <div class="metric-icon orders">📋</div>
        <div class="metric-content">
          <span class="metric-value">{{ orderStats.thisMonth.toLocaleString() }}</span>
          <span class="metric-label">已完成订单</span>
        </div>
      </AppCard>
      <AppCard class="metric-card" shadow="sm">
        <div class="metric-icon revenue">💰</div>
        <div class="metric-content">
          <span class="metric-value">{{ formatPrice(orderStats.revenue) }}</span>
          <span class="metric-label">总收入</span>
        </div>
      </AppCard>
      <AppCard class="metric-card" shadow="sm">
        <div class="metric-icon institutions">🏠</div>
        <div class="metric-content">
          <span class="metric-value">{{ institutionStats.active }}</span>
          <span class="metric-label">活跃机构</span>
        </div>
      </AppCard>
    </div>

    <div class="charts-row">
      <AppCard class="chart-card" shadow="sm" padding="lg">
        <h3>收入趋势</h3>
        <div class="simple-chart">
          <div class="chart-bars">
            <div v-for="item in trendData" :key="item.month" class="chart-bar-group">
              <div class="chart-bar" :style="{ height: Math.max(20, item.revenue / 50) + 'px' }" :title="`收入: ${formatPrice(item.revenue)}`"></div>
              <span class="bar-label">{{ item.month }}</span>
            </div>
          </div>
        </div>
      </AppCard>
      <AppCard class="ranking-card" shadow="sm" padding="lg">
        <h3>机构排行榜</h3>
        <div class="ranking-list">
          <div v-for="(inst, index) in topInstitutions" :key="inst.name" class="ranking-item">
            <span class="rank" :class="{ top: index < 3 }">{{ index + 1 }}</span>
            <div class="inst-info">
              <span class="inst-name">{{ inst.name }}</span>
              <span class="inst-stats">{{ inst.orders }}单 · {{ formatPrice(inst.revenue) }}</span>
            </div>
            <div class="inst-rating"><span class="star">★</span><span>{{ inst.rating }}</span></div>
          </div>
          <div v-if="topInstitutions.length === 0" class="empty-ranking">暂无数据</div>
        </div>
      </AppCard>
    </div>

    <div class="stats-row">
      <AppCard shadow="sm" padding="lg">
        <h3>用户统计</h3>
        <div class="stat-items">
          <div class="stat-row"><span class="stat-name">今日新增用户</span><span class="stat-num">{{ userStats.newThisMonth }}</span></div>
          <div class="stat-row"><span class="stat-name">活跃用户</span><span class="stat-num">{{ userStats.activeUsers }}</span></div>
          <div class="stat-row"><span class="stat-name">平均订单金额</span><span class="stat-num">{{ formatPrice(orderStats.avgOrderValue) }}</span></div>
        </div>
      </AppCard>
      <AppCard shadow="sm" padding="lg">
        <h3>机构统计</h3>
        <div class="stat-items">
          <div class="stat-row"><span class="stat-name">总机构数</span><span class="stat-num">{{ institutionStats.total }}</span></div>
          <div class="stat-row"><span class="stat-name">待审核</span><span class="stat-num warning">{{ institutionStats.pending }}</span></div>
          <div class="stat-row"><span class="stat-name">总订单数</span><span class="stat-num">{{ orderStats.total }}</span></div>
        </div>
      </AppCard>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.analytics-view { max-width: 1200px; margin: 0 auto; }
.page-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;
  h1 { font-family: var(--font-display); font-size: 28px; margin: 0 0 4px; }
  p { color: var(--color-text-secondary); margin: 0; }
  .header-actions { display: flex; gap: 12px; align-items: center; }
  .date-selector { display: flex; flex-direction: column; gap: 8px; }
  .mode-tabs { display: flex; gap: 4px; background: var(--color-surface); padding: 4px; border-radius: var(--radius-md); }
  .mode-tab {
    padding: 6px 12px; border: none; background: transparent; cursor: pointer; font-size: 13px; border-radius: var(--radius-sm);
    &.active { background: var(--color-primary); color: white; }
  }
  .quick-select { padding: 10px 16px; border: 1px solid var(--color-border); border-radius: var(--radius-md); font-size: 14px; background: var(--color-surface); }
  .date-range-picker { :deep(.el-input__wrapper) { padding: 10px 16px; border-radius: var(--radius-md); } }
  .export-btn {
    padding: 10px 16px; border: none; border-radius: var(--radius-md);
    background: linear-gradient(135deg, var(--color-primary) 0%, #FF8F5C 100%);
    color: white; font-size: 14px; font-weight: 500; cursor: pointer;
    &:hover:not(:disabled) { transform: translateY(-1px); box-shadow: 0 4px 12px rgba(255, 107, 53, 0.3); }
    &:disabled { opacity: 0.6; cursor: not-allowed; }
  }
}
.metrics-grid {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-bottom: 24px;
  @media (max-width: 1024px) { grid-template-columns: repeat(2, 1fr); }
  @media (max-width: 640px) { grid-template-columns: 1fr; }
}
.metric-card {
  display: flex; align-items: center; gap: 16px;
  .metric-icon { width: 56px; height: 56px; border-radius: var(--radius-lg); display: flex; align-items: center; justify-content: center; font-size: 28px;
    &.users { background: #dbeafe; } &.orders { background: #fef3c7; } &.revenue { background: #dcfce7; } &.institutions { background: #fce7f3; }
  }
  .metric-content { flex: 1;
    .metric-value { display: block; font-size: 24px; font-weight: 700; }
    .metric-label { display: block; font-size: 13px; color: var(--color-text-muted); margin-bottom: 4px; }
    .metric-change { font-size: 12px; font-weight: 600; &.positive { color: var(--color-success); } }
  }
}
.charts-row { display: grid; grid-template-columns: 2fr 1fr; gap: 20px; margin-bottom: 24px;
  @media (max-width: 1024px) { grid-template-columns: 1fr; }
}
.chart-card, .ranking-card { h3 { font-size: 16px; font-weight: 600; margin: 0 0 20px; } }
.simple-chart {
  .chart-bars { display: flex; align-items: flex-end; justify-content: space-between; height: 120px; padding-top: 20px; }
  .chart-bar-group { display: flex; flex-direction: column; align-items: center; gap: 8px; }
  .chart-bar { width: 40px; background: linear-gradient(180deg, var(--color-primary), var(--color-accent)); border-radius: 4px 4px 0 0; min-height: 20px; }
  .bar-label { font-size: 12px; color: var(--color-text-muted); }
}
.ranking-list { display: flex; flex-direction: column; gap: 12px; }
.ranking-item {
  display: flex; align-items: center; gap: 12px;
  .rank { width: 24px; height: 24px; background: var(--color-neutral-200); border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 600;
    &.top { background: var(--color-primary); color: white; }
  }
  .inst-info { flex: 1; .inst-name { display: block; font-weight: 500; } .inst-stats { font-size: 12px; color: var(--color-text-muted); } }
  .inst-rating { display: flex; align-items: center; gap: 4px; font-size: 14px; font-weight: 500; .star { color: var(--color-warning); } }
}
.empty-ranking { text-align: center; color: var(--color-text-muted); padding: 20px; }
.stats-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px;
  @media (max-width: 768px) { grid-template-columns: 1fr; }
  h3 { font-size: 16px; font-weight: 600; margin: 0 0 16px; }
}
.stat-items { display: flex; flex-direction: column; gap: 12px; }
.stat-row {
  display: flex; justify-content: space-between; align-items: center; padding: 8px 0; border-bottom: 1px solid var(--color-border);
  &:last-child { border-bottom: none; }
  .stat-name { color: var(--color-text-secondary); }
  .stat-num { font-weight: 600; &.warning { color: var(--color-warning); } &.danger { color: var(--color-error); } }
}
.dropdown-icon { margin-right: 8px; }
</style>
