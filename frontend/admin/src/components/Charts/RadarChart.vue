<template>
  <div ref="chartRef" class="chart-container"></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { useThemeStore } from '@/stores/theme'

const props = defineProps({
  data: { type: Object, required: true }
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
  const lineColor = isDark ? '#333' : '#e5e7eb'
  
  const option = {
    backgroundColor: 'transparent',
    tooltip: {},
    radar: {
      indicator: props.data.indicator || [],
      axisName: { color: textColor },
      splitLine: { lineStyle: { color: lineColor } },
      splitArea: { 
        areaStyle: { 
          color: isDark 
            ? ['rgba(168, 85, 247, 0.02)', 'rgba(168, 85, 247, 0.05)']
            : ['rgba(168, 85, 247, 0.02)', 'rgba(168, 85, 247, 0.05)']
        } 
      },
      axisLine: { lineStyle: { color: lineColor } }
    },
    series: [{
      type: 'radar',
      data: [{
        value: props.data.values || [],
        name: props.data.name || '数据',
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(168, 85, 247, 0.4)' },
            { offset: 1, color: 'rgba(168, 85, 247, 0.1)' }
          ])
        },
        lineStyle: { color: '#a855f7', width: 2 },
        itemStyle: { color: '#a855f7' }
      }]
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
