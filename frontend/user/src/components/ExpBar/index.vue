<template>
  <div class="exp-bar-wrapper" :class="{ 'is-dark': isDark }">
    <el-tooltip
      :content="tooltipContent"
      placement="top"
      :disabled="!showTooltip"
      effect="dark"
    >
      <div class="exp-progress" :style="{ width: width }">
        <div class="exp-info" v-if="showInfo">
          <span class="level-text">Lv.{{ level }}</span>
          <span class="exp-text">{{ currentExp }} / {{ nextLevelNeedExp }}</span>
        </div>
        <el-progress
          :percentage="progressPercentage"
          :stroke-width="strokeWidth"
          :show-text="false"
          :color="progressColor"
        />
      </div>
    </el-tooltip>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useDark } from '@vueuse/core'

const props = defineProps({
  level: {
    type: Number,
    default: 1
  },
  currentExp: {
    type: Number,
    default: 0
  },
  nextLevelNeedExp: {
    type: Number,
    default: 100
  },
  width: {
    type: String,
    default: '100%'
  },
  strokeWidth: {
    type: Number,
    default: 6
  },
  showInfo: {
    type: Boolean,
    default: true
  },
  showTooltip: {
    type: Boolean,
    default: true
  }
})

const isDark = useDark()

const progressPercentage = computed(() => {
  if (!props.nextLevelNeedExp || props.nextLevelNeedExp === 0) return 100
  const p = (props.currentExp / props.nextLevelNeedExp) * 100
  return Math.min(Math.max(p, 0), 100)
})

const tooltipContent = computed(() => {
  if (props.level >= 100) {
    return `已满级 (经验: ${props.currentExp})`
  }
  return `距离升级还需 ${props.nextLevelNeedExp - props.currentExp} 经验`
})

// 根据等级设置不同的进度条颜色，类似用户等级徽章的颜色
const progressColor = computed(() => {
  const level = props.level
  if (level >= 91) return '#ff4d4f' // 红
  if (level >= 61) return '#faad14' // 橙
  if (level >= 31) return '#722ed1' // 紫
  if (level >= 11) return '#1890ff' // 蓝
  return '#52c41a' // 绿
})
</script>

<style scoped>
.exp-bar-wrapper {
  display: flex;
  align-items: center;
}

.exp-progress {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.exp-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  line-height: 1;
}

.level-text {
  font-weight: bold;
  color: var(--el-text-color-primary);
}

.exp-text {
  color: var(--el-text-color-secondary);
  font-family: monospace;
}

/* 进度条动画 */
:deep(.el-progress-bar__inner) {
  transition: width 0.6s ease;
}

/* 暗色模式适配 */
.is-dark .level-text {
  color: var(--el-text-color-primary);
}
.is-dark .exp-text {
  color: var(--el-text-color-secondary);
}
</style>
