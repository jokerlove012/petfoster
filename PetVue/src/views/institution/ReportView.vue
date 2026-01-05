<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { BarChart3, Download, TrendingUp, Users, Package, DollarSign } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'
import LineChart from '@/components/charts/LineChart.vue'
import BarChart from '@/components/charts/BarChart.vue'
import PieChart from '@/components/charts/PieChart.vue'
import { exportToCSV, exportToPDF } from '@/utils/reportExporter'
import api from '@/api/index'

const selectedPeriod = ref<'week' | 'month' | 'quarter' | 'year'>('month')
const loading = ref(false)

// 核心指标
const coreMetrics = ref({
  totalOrders: 0,
  ordersTrend: 0,
  totalRevenue: 0,
  revenueTrend: 0,
  avgOrderValue: 0,
  occupancyRate: 0
})

// 图表数据
const orderTrend = ref<{ name: string; value: number }[]>([])
const revenueTrend = ref<{ name: string; value: number }[]>([])
const petTypeDistribution = ref<{ name: string; value: number }[]>([])
const packageRanking = ref<{ name: string; value: number }[]>([])

// 加载报表数据
const loadReportData = async () => {
  loading.value = true
  try {
    const res = await api.get(`/institution/report?period=${selectedPeriod.value}`)
    if (res.code === 200 && res.data) {
      const data = res.data
      if (data.coreMetrics) {
        coreMetrics.value = {
          totalOrders: data.coreMetrics.totalOrders || 0,
          ordersTrend: data.coreMetrics.ordersTrend || 0,
          totalRevenue: Number(data.coreMetrics.totalRevenue) || 0,
          revenueTrend: data.coreMetrics.revenueTrend || 0,
          avgOrderValue: Number(data.coreMetrics.avgOrderValue) || 0,
          occupancyRate: data.coreMetrics.occupancyRate || 0
        }
      }
      orderTrend.value = data.orderTrend || []
      revenueTrend.value = data.revenueTrend || []
      petTypeDistribution.value = data.petTypeDistribution || []
      packageRanking.value = data.packageRanking || []
    }
  } catch (error) {
    console.error('加载报表数据失败:', error)
  } finally {
    loading.value = false
  }
}

const changePeriod = (period: typeof selectedPeriod.value) => {
  selectedPeriod.value = period
}

watch(selectedPeriod, () => {
  loadReportData()
})

onMounted(() => {
  loadReportData()
})

const handleExportCSV = () => {
  exportToCSV({
    title: '机构数据报表',
    headers: ['指标', '数值'],
    rows: [
      ['总订单数', coreMetrics.value.totalOrders.toString()],
      ['总收入', `¥${coreMetrics.value.totalRevenue}`],
      ['平均客单价', `¥${coreMetrics.value.avgOrderValue}`],
      ['入住率', `${coreMetrics.value.occupancyRate}%`]
    ]
  })
  ElMessage.success('CSV报表已导出')
}

const handleExportPDF = () => {
  exportToPDF({
    title: '机构数据报表',
    content: `总订单: ${coreMetrics.value.totalOrders}, 总收入: ¥${coreMetrics.value.totalRevenue}`
  })
  ElMessage.success('PDF报表已导出')
}
</script>

<template>
  <div class="report-view">
    <div class="page-header">
      <div class="header-left">
        <h1>📊 数据报表</h1>
        <p>查看机构运营数据分析</p>
      </div>
      <div class="header-actions">
        <div class="period-selector">
          <button v-for="p in ['week', 'month', 'quarter', 'year']" :key="p" 
            :class="{ active: selectedPeriod === p }" @click="changePeriod(p as any)">
            {{ { week: '本周', month: '本月', quarter: '本季度', year: '本年' }[p] }}
          </button>
        </div>
        <div class="export-btns">
          <button class="btn-export" @click="handleExportCSV"><Download :size="16" /> CSV</button>
          <button class="btn-export" @click="handleExportPDF"><Download :size="16" /> PDF</button>
        </div>
      </div>
    </div>

    <!-- 核心指标 -->
    <div class="metrics-grid">
      <div class="metric-card">
        <div class="metric-icon orders"><Package :size="24" /></div>
        <div class="metric-info">
          <span class="metric-value">{{ coreMetrics.totalOrders }}</span>
          <span class="metric-label">总订单数</span>
          <span class="metric-trend up"><TrendingUp :size="14" /> +{{ coreMetrics.ordersTrend }}%</span>
        </div>
      </div>
      <div class="metric-card">
        <div class="metric-icon revenue"><DollarSign :size="24" /></div>
        <div class="metric-info">
          <span class="metric-value">¥{{ coreMetrics.totalRevenue.toLocaleString() }}</span>
          <span class="metric-label">总收入</span>
          <span class="metric-trend up"><TrendingUp :size="14" /> +{{ coreMetrics.revenueTrend }}%</span>
        </div>
      </div>
      <div class="metric-card">
        <div class="metric-icon avg"><BarChart3 :size="24" /></div>
        <div class="metric-info">
          <span class="metric-value">¥{{ coreMetrics.avgOrderValue }}</span>
          <span class="metric-label">平均客单价</span>
        </div>
      </div>
      <div class="metric-card">
        <div class="metric-icon occupancy"><Users :size="24" /></div>
        <div class="metric-info">
          <span class="metric-value">{{ coreMetrics.occupancyRate }}%</span>
          <span class="metric-label">入住率</span>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-grid">
      <div class="chart-card">
        <h3>📈 订单趋势</h3>
        <LineChart :data="orderTrend" color="#722ed1" />
      </div>
      <div class="chart-card">
        <h3>💰 收入趋势</h3>
        <LineChart :data="revenueTrend" color="#52c41a" />
      </div>
      <div class="chart-card">
        <h3>🐾 宠物类型分布</h3>
        <PieChart :data="petTypeDistribution" />
      </div>
      <div class="chart-card">
        <h3>🏆 套餐销售排名</h3>
        <BarChart :data="packageRanking" color="#1890ff" :horizontal="true" />
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.report-view { max-width: 1200px; margin: 0 auto; padding: 24px; }

.page-header {
  display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; flex-wrap: wrap; gap: 16px;
  .header-left { h1 { font-size: 26px; font-weight: 700; margin: 0 0 6px; } p { color: #6B6560; margin: 0; } }
  .header-actions { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
}

.period-selector {
  display: flex; background: white; border-radius: 10px; padding: 4px;
  button { padding: 8px 14px; border: none; background: transparent; font-size: 13px; color: #6B6560; cursor: pointer; border-radius: 6px;
    &.active { background: #722ed1; color: white; }
  }
}

.export-btns { display: flex; gap: 8px;
  .btn-export { display: flex; align-items: center; gap: 6px; padding: 8px 14px; background: white; border: 1px solid #E8E6E3; border-radius: 8px; font-size: 13px; cursor: pointer;
    &:hover { border-color: #722ed1; color: #722ed1; }
  }
}

.metrics-grid {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px;
  @media (max-width: 900px) { grid-template-columns: repeat(2, 1fr); }
}

.metric-card {
  display: flex; align-items: center; gap: 16px; padding: 20px; background: white; border-radius: 14px;
  .metric-icon { width: 52px; height: 52px; border-radius: 12px; display: flex; align-items: center; justify-content: center;
    &.orders { background: linear-gradient(135deg, #F3EEFF, #E8DEFF); color: #722ed1; }
    &.revenue { background: linear-gradient(135deg, #E8F8E8, #D4F0D4); color: #52c41a; }
    &.avg { background: linear-gradient(135deg, #E6F4FF, #BAE0FF); color: #1890ff; }
    &.occupancy { background: linear-gradient(135deg, #FFF8E6, #FFEFC7); color: #faad14; }
  }
  .metric-info {
    .metric-value { display: block; font-size: 22px; font-weight: 700; color: #2D2A26; }
    .metric-label { font-size: 13px; color: #6B6560; }
    .metric-trend { display: flex; align-items: center; gap: 4px; font-size: 12px; margin-top: 4px;
      &.up { color: #52c41a; }
    }
  }
}

.charts-grid {
  display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px;
  @media (max-width: 900px) { grid-template-columns: 1fr; }
}

.chart-card {
  background: white; border-radius: 14px; padding: 20px;
  h3 { font-size: 15px; font-weight: 600; margin: 0 0 16px; }
}
</style>
