<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import * as echarts from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([LineChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

interface Props {
  data: { name: string; value: number }[]
  title?: string
  color?: string
  areaStyle?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  title: '',
  color: '#FF7F6B',
  areaStyle: true
})

const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

const option = computed(() => ({
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(255,255,255,0.95)',
    borderColor: '#eee',
    textStyle: { color: '#333' }
  },
  grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
  xAxis: {
    type: 'category',
    data: props.data.map(d => d.name),
    axisLine: { lineStyle: { color: '#E5E5E5' } },
    axisLabel: { color: '#999' }
  },
  yAxis: {
    type: 'value',
    axisLine: { show: false },
    splitLine: { lineStyle: { color: '#F0F0F0' } },
    axisLabel: { color: '#999' }
  },
  series: [{
    data: props.data.map(d => d.value),
    type: 'line',
    smooth: true,
    symbol: 'circle',
    symbolSize: 8,
    lineStyle: { color: props.color, width: 3 },
    itemStyle: { color: props.color },
    areaStyle: props.areaStyle ? {
      color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: `${props.color}40` },
        { offset: 1, color: `${props.color}05` }
      ])
    } : undefined
  }]
}))

onMounted(() => {
  if (chartRef.value) {
    chart = echarts.init(chartRef.value)
    chart.setOption(option.value)
    window.addEventListener('resize', () => chart?.resize())
  }
})

watch(() => props.data, () => chart?.setOption(option.value), { deep: true })
</script>

<template>
  <div ref="chartRef" class="chart-container"></div>
</template>

<style scoped>
.chart-container { width: 100%; height: 280px; }
</style>
