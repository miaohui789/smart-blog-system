<template>
  <span class="vip-username" :class="usernameClass">
    <span class="username-text">{{ username }}</span>
    <VipBadge v-if="vipLevel > 0" :level="vipLevel" :glow="glow" />
  </span>
</template>

<script setup>
import { computed } from 'vue'
import VipBadge from '@/components/VipBadge/index.vue'

const props = defineProps({
  username: {
    type: String,
    default: '匿名用户'
  },
  vipLevel: {
    type: Number,
    default: 0
  },
  glow: {
    type: Boolean,
    default: false
  }
})

const usernameClass = computed(() => {
  if (props.vipLevel > 0) {
    return `vip-level-${props.vipLevel}`
  }
  return ''
})
</script>

<style scoped>
.vip-username {
  display: inline-flex;
  align-items: center;
  gap: 2px;
}

.username-text {
  font-weight: 600;
  transition: all 0.3s ease;
}

/* 普通用户 */
.vip-username:not([class*="vip-level"]) .username-text {
  color: var(--text-primary);
}

/* 普通VIP - 铜色渐变 */
.vip-username.vip-level-1 .username-text {
  background: linear-gradient(135deg, #cd7f32, #daa520);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-weight: 700;
}

/* 高级VIP - 银色渐变 */
.vip-username.vip-level-2 .username-text {
  background: linear-gradient(135deg, #a8a8a8, #d4d4d4, #a8a8a8);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-weight: 700;
}

/* 超级VIP - 金色渐变 + 动画 */
.vip-username.vip-level-3 .username-text {
  background: linear-gradient(135deg, #ffd700, #ffb700, #ffd700, #ffe44d);
  background-size: 200% 200%;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-weight: 700;
  animation: goldGradient 3s ease infinite;
}

@keyframes goldGradient {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

/* 深色模式适配 */
:root[data-theme="dark"] .vip-username.vip-level-1 .username-text {
  background: linear-gradient(135deg, #e8a54b, #f0c060);
  -webkit-background-clip: text;
  background-clip: text;
}

:root[data-theme="dark"] .vip-username.vip-level-2 .username-text {
  background: linear-gradient(135deg, #c8c8c8, #e8e8e8, #c8c8c8);
  -webkit-background-clip: text;
  background-clip: text;
}

:root[data-theme="dark"] .vip-username.vip-level-3 .username-text {
  background: linear-gradient(135deg, #ffe44d, #ffd000, #ffe44d, #fff176);
  background-size: 200% 200%;
  -webkit-background-clip: text;
  background-clip: text;
}
</style>
