<template>
  <div ref="chartRef" class="chart-container"></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { useThemeStore } from '@/stores/theme'

const props = defineProps({
  data: { type: Array, required: true },
  title: { type: String, default: '' }
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
  
  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      textStyle: { color: textColor }
    },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 8,
        borderColor: isDark ? '#1a1a1a' : '#ffffff',
        borderWidth: 2
      },
      label: { show: false },
      emphasis: {
        label: {
          show: true,
          fontSize: 14,
          fontWeight: 'bold',
          color: textColor
        }
      },
      labelLine: { show: false },
      data: (props.data || []).map((item, index) => ({
        ...item,
        itemStyle: {
          color: ['#a855f7', '#ec4899', '#3b82f6', '#22c55e', '#f59e0b', '#ef4444'][index % 6]
        }
      }))
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
