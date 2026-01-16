<template>
  <span v-if="level > 0" class="vip-badge" :class="['level-' + level, { 'with-glow': glow }]" :title="levelTitle">
    <span class="badge-inner">
      <svg viewBox="0 0 24 24" class="vip-icon">
        <path fill="currentColor" d="M5 16L3 5l5.5 5L12 4l3.5 6L21 5l-2 11H5m14 3c0 .6-.4 1-1 1H6c-.6 0-1-.4-1-1v-1h14v1z"/>
      </svg>
      <span class="vip-text">{{ levelText }}</span>
    </span>
    <!-- 闪光效果层 -->
    <span class="shine-layer"></span>
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  level: {
    type: Number,
    default: 0
  },
  glow: {
    type: Boolean,
    default: false
  }
})

const levelText = computed(() => {
  const texts = { 1: 'VIP', 2: 'VIP', 3: 'SVIP' }
  return texts[props.level] || ''
})

const levelTitle = computed(() => {
  const titles = { 1: '普通VIP会员', 2: '高级VIP会员', 3: '超级VIP会员' }
  return titles[props.level] || ''
})
</script>

<style scoped>
.vip-badge {
  display: inline-flex;
  align-items: center;
  vertical-align: middle;
  margin-left: 4px;
  position: relative;
  overflow: hidden;
  border-radius: 4px;
}

.badge-inner {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.5px;
  position: relative;
  z-index: 1;
}

/* 闪光效果层 */
.shine-layer {
  position: absolute;
  top: 0;
  left: -100%;
  width: 50%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.4),
    transparent
  );
  z-index: 2;
  pointer-events: none;
}

/* 普通VIP - 铜色 */
.vip-badge.level-1 .badge-inner {
  background: linear-gradient(135deg, #cd7f32 0%, #b8860b 50%, #cd7f32 100%);
  color: #fff;
  box-shadow: 0 2px 4px rgba(205, 127, 50, 0.3);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.vip-badge.level-1 .shine-layer {
  animation: shine 4s ease-in-out infinite;
  animation-delay: 0s;
}

/* 高级VIP - 银色 */
.vip-badge.level-2 .badge-inner {
  background: linear-gradient(135deg, #e8e8e8 0%, #c0c0c0 50%, #e8e8e8 100%);
  color: #444;
  box-shadow: 0 2px 4px rgba(192, 192, 192, 0.4);
  text-shadow: 0 1px 1px rgba(255, 255, 255, 0.5);
}

.vip-badge.level-2 .shine-layer {
  animation: shine 3s ease-in-out infinite;
  animation-delay: 0.5s;
}

/* 超级VIP - 金色 + 特殊动效 */
.vip-badge.level-3 {
  animation: goldPulse 2s ease-in-out infinite;
}

.vip-badge.level-3 .badge-inner {
  background: linear-gradient(90deg, #ffd700, #ffb700, #ffd700, #ffe44d, #ffd700);
  background-size: 200% 100%;
  color: #5c4813;
  box-shadow: 0 2px 8px rgba(255, 215, 0, 0.6);
  text-shadow: 0 1px 1px rgba(255, 255, 255, 0.3);
  animation: goldGradient 2s linear infinite;
}

.vip-badge.level-3 .shine-layer {
  animation: shine 2s ease-in-out infinite;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.6),
    transparent
  );
}

/* 发光效果 */
.vip-badge.with-glow.level-1 {
  box-shadow: 0 0 8px rgba(205, 127, 50, 0.6);
}

.vip-badge.with-glow.level-2 {
  box-shadow: 0 0 8px rgba(192, 192, 192, 0.6);
}

.vip-badge.with-glow.level-3 {
  box-shadow: 0 0 12px rgba(255, 215, 0, 0.8);
}

.vip-icon {
  width: 12px;
  height: 12px;
  flex-shrink: 0;
}

.vip-text {
  font-size: 10px;
  line-height: 1;
}

/* 闪光动画 */
@keyframes shine {
  0% {
    left: -100%;
  }
  20% {
    left: 100%;
  }
  100% {
    left: 100%;
  }
}

/* 金色渐变流动 */
@keyframes goldGradient {
  0% {
    background-position: 0% 50%;
  }
  100% {
    background-position: 200% 50%;
  }
}

/* 金色脉冲 */
@keyframes goldPulse {
  0%, 100% {
    box-shadow: 0 0 4px rgba(255, 215, 0, 0.4);
  }
  50% {
    box-shadow: 0 0 12px rgba(255, 215, 0, 0.8);
  }
}
</style>
