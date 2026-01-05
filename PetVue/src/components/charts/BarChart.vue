<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import * as echarts from 'echarts/core'
import { BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([BarChart, GridComponent, TooltipComponent, CanvasRenderer])

interface Props {
  data: { name: string; value: number }[]
  color?: string
  horizontal?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  color: '#7DD3C0',
  horizontal: false
})

const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

const option = computed(() => {
  const baseOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: 'rgba(255,255,255,0.95)',
      borderColor: '#eee',
      textStyle: { color: '#333' }
    },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true }
  }

  if (props.horizontal) {
    return {
      ...baseOption,
      xAxis: { type: 'value', axisLine: { show: false }, splitLine: { lineStyle: { color: '#F0F0F0' } } },
      yAxis: { type: 'category', data: props.data.map(d => d.name), axisLine: { lineStyle: { color: '#E5E5E5' } } },
      series: [{
        type: 'bar',
        data: props.data.map(d => d.value),
        itemStyle: { color: props.color, borderRadius: [0, 4, 4, 0] },
        barWidth: '60%'
      }]
    }
  }

  return {
    ...baseOption,
    xAxis: { type: 'category', data: props.data.map(d => d.name), axisLine: { lineStyle: { color: '#E5E5E5' } } },
    yAxis: { type: 'value', axisLine: { show: false }, splitLine: { lineStyle: { color: '#F0F0F0' } } },
    series: [{
      type: 'bar',
      data: props.data.map(d => d.value),
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: props.color },
          { offset: 1, color: `${props.color}80` }
        ]),
        borderRadius: [4, 4, 0, 0]
      },
      barWidth: '50%'
    }]
  }
})

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
