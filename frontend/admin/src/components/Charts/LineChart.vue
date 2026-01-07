<template>
  <div ref="chartRef" class="chart-container"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  data: { type: Object, required: true }
})

const chartRef = ref()
let chart = null

function initChart() {
  chart = echarts.init(chartRef.value)
  updateChart()
}

function updateChart() {
  const option = {
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: props.data.xAxis || [],
      axisLine: { lineStyle: { color: '#333' } },
      axisLabel: { color: '#a1a1aa' }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: '#333' } },
      axisLabel: { color: '#a1a1aa' },
      splitLine: { lineStyle: { color: '#333' } }
    },
    series: [{
      data: props.data.series || [],
      type: 'line',
      smooth: true,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(168, 85, 247, 0.3)' },
          { offset: 1, color: 'rgba(168, 85, 247, 0)' }
        ])
      },
      lineStyle: { color: '#a855f7' },
      itemStyle: { color: '#a855f7' }
    }]
  }
  chart.setOption(option)
}

watch(() => props.data, updateChart, { deep: true })

onMounted(() => {
  initChart()
  window.addEventListener('resize', () => chart?.resize())
})

onUnmounted(() => {
  chart?.dispose()
})
</script>

<style scoped>
.chart-container {
  width: 100%;
  height: 300px;
}
</style>
