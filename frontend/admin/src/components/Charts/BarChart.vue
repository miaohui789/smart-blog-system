<template>
  <div ref="chartRef" class="chart-container"></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { useThemeStore } from '@/stores/theme'

const props = defineProps({
  data: { type: Object, required: true },
  horizontal: { type: Boolean, default: false }
})

const themeStore = useThemeStore()
const chartRef = ref()
let chart = null
let isDisposed = false

function initChart() {
  if (!chartRef.value || isDisposed) return
  chart = echarts.init(chartRef.value)
  updateChart()
}

function updateChart() {
  if (!chart || isDisposed) return
  
  const isDark = themeStore.isDark
  const textColor = isDark ? '#a1a1aa' : '#71717a'
  const gridColor = isDark ? '#333' : '#e5e7eb'
  
  const xAxisConfig = {
    type: props.horizontal ? 'value' : 'category',
    data: props.horizontal ? undefined : (props.data.xAxis || []),
    axisLine: { lineStyle: { color: gridColor } },
    axisLabel: { color: textColor },
    splitLine: { lineStyle: { color: gridColor } }
  }
  
  const yAxisConfig = {
    type: props.horizontal ? 'category' : 'value',
    data: props.horizontal ? (props.data.xAxis || []) : undefined,
    axisLine: { lineStyle: { color: gridColor } },
    axisLabel: { color: textColor },
    splitLine: { lineStyle: { color: gridColor } }
  }
  
  const option = {
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
    xAxis: xAxisConfig,
    yAxis: yAxisConfig,
    series: [{
      data: props.data.series || [],
      type: 'bar',
      barWidth: '60%',
      itemStyle: {
        borderRadius: props.horizontal ? [0, 4, 4, 0] : [4, 4, 0, 0],
        color: new echarts.graphic.LinearGradient(
          props.horizontal ? 0 : 0,
          props.horizontal ? 0 : 1,
          props.horizontal ? 1 : 0,
          props.horizontal ? 0 : 0,
          [
            { offset: 0, color: '#a855f7' },
            { offset: 1, color: '#ec4899' }
          ]
        )
      }
    }]
  }
  chart.setOption(option)
}

function handleResize() {
  if (chart && !isDisposed) {
    chart.resize()
  }
}

watch(() => props.data, () => {
  nextTick(updateChart)
}, { deep: true })

watch(() => themeStore.isDark, () => {
  nextTick(updateChart)
})

onMounted(() => {
  nextTick(initChart)
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  isDisposed = true
  window.removeEventListener('resize', handleResize)
  if (chart) {
    chart.dispose()
    chart = null
  }
})
</script>

<style scoped>
.chart-container {
  width: 100%;
  height: 280px;
}
</style>
