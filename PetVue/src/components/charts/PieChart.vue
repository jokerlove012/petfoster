<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import * as echarts from 'echarts/core'
import { PieChart } from 'echarts/charts'
import { TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([PieChart, TooltipComponent, LegendComponent, CanvasRenderer])

interface Props {
  data: { name: string; value: number }[]
  colors?: string[]
}

const props = withDefaults(defineProps<Props>(), {
  colors: () => ['#FF7F6B', '#7DD3C0', '#FFB366', '#A78BFA', '#60A5FA']
})

const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

const option = computed(() => ({
  tooltip: {
    trigger: 'item',
    formatter: '{b}: {c} ({d}%)',
    backgroundColor: 'rgba(255,255,255,0.95)',
    borderColor: '#eee',
    textStyle: { color: '#333' }
  },
  legend: {
    orient: 'horizontal',
    bottom: '5%',
    left: 'center',
    textStyle: { color: '#666', fontSize: 12 },
    itemWidth: 12,
    itemHeight: 12,
    itemGap: 16
  },
  series: [{
    type: 'pie',
    radius: ['40%', '65%'],
    center: ['50%', '40%'],
    avoidLabelOverlap: false,
    itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
    label: { show: false },
    emphasis: {
      label: { show: true, fontSize: 14, fontWeight: 'bold' }
    },
    data: props.data.map((d, i) => ({
      ...d,
      itemStyle: { color: props.colors[i % props.colors.length] }
    }))
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
