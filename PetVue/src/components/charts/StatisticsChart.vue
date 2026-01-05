<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'

// 图表数据
const chartData = ref([
  { month: '7月', revenue: 32000, orders: 98 },
  { month: '8月', revenue: 38000, orders: 112 },
  { month: '9月', revenue: 35000, orders: 105 },
  { month: '10月', revenue: 42000, orders: 128 },
  { month: '11月', revenue: 48000, orders: 145 },
  { month: '12月', revenue: 45680, orders: 156 }
])

// 计算最大值用于缩放
const maxRevenue = computed(() => Math.max(...chartData.value.map(d => d.revenue)))

// 计算柱状图高度百分比
const getBarHeight = (value: number) => {
  return (value / maxRevenue.value) * 100
}

// 格式化金额
const formatCurrency = (value: number) => {
  if (value >= 10000) {
    return `${(value / 10000).toFixed(1)}万`
  }
  return value.toLocaleString()
}
</script>

<template>
  <div class="statistics-chart">
    <!-- 图表区域 -->
    <div class="chart-container">
      <div class="chart-bars">
        <div 
          v-for="(item, index) in chartData"
          :key="index"
          class="bar-group"
        >
          <div class="bar-wrapper">
            <div 
              class="bar"
              :style="{ height: `${getBarHeight(item.revenue)}%` }"
            >
              <span class="bar-value">¥{{ formatCurrency(item.revenue) }}</span>
            </div>
          </div>
          <span class="bar-label">{{ item.month }}</span>
        </div>
      </div>
      
      <!-- Y轴刻度 -->
      <div class="y-axis">
        <span>¥{{ formatCurrency(maxRevenue) }}</span>
        <span>¥{{ formatCurrency(maxRevenue * 0.75) }}</span>
        <span>¥{{ formatCurrency(maxRevenue * 0.5) }}</span>
        <span>¥{{ formatCurrency(maxRevenue * 0.25) }}</span>
        <span>¥0</span>
      </div>
    </div>

    <!-- 图例 -->
    <div class="chart-legend">
      <div class="legend-item">
        <span class="legend-dot revenue"></span>
        <span>月收入</span>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.statistics-chart {
  width: 100%;
}

.chart-container {
  position: relative;
  height: 240px;
  padding-left: 60px;
}

.chart-bars {
  display: flex;
  justify-content: space-around;
  align-items: flex-end;
  height: 100%;
  padding-bottom: 30px;
  border-bottom: 1px solid var(--color-border);
}

.bar-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  max-width: 60px;
}

.bar-wrapper {
  height: 180px;
  width: 100%;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.bar {
  width: 32px;
  background: linear-gradient(180deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
  border-radius: var(--radius-sm) var(--radius-sm) 0 0;
  position: relative;
  transition: height 0.5s ease;
  min-height: 4px;
  
  &:hover {
    opacity: 0.9;
    
    .bar-value {
      opacity: 1;
      transform: translateX(-50%) translateY(-8px);
    }
  }
}

.bar-value {
  position: absolute;
  top: -24px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 11px;
  font-weight: 500;
  color: var(--color-text-primary);
  white-space: nowrap;
  opacity: 0;
  transition: all 0.2s;
  background: var(--color-surface);
  padding: 2px 6px;
  border-radius: var(--radius-sm);
  box-shadow: var(--shadow-sm);
}

.bar-label {
  margin-top: 8px;
  font-size: 12px;
  color: var(--color-text-secondary);
}

.y-axis {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 30px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  
  span {
    font-size: 11px;
    color: var(--color-text-tertiary);
  }
}

.chart-legend {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-top: 16px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--color-text-secondary);
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 2px;
  
  &.revenue {
    background: var(--color-primary);
  }
}
</style>
