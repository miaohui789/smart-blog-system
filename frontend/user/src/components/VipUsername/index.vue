<template>
  <span class="vip-username" :class="[`tier-${levelTheme.tier}`, vipClass]">
    <span class="username-wrapper" :style="wrapperStyle">
      <span class="username-text" :style="usernameStyle">{{ username }}</span>
    </span>
    <span v-if="showBadge" class="badge-group">
      <UserLevelBadge :level="normalizedLevel" />
      <VipBadge v-if="vipLevel > 0" :level="vipLevel" :glow="glow" />
    </span>
  </span>
</template>

<script setup>
import { computed } from 'vue'
import VipBadge from '@/components/VipBadge/index.vue'
import UserLevelBadge from '@/components/UserLevelBadge/index.vue'
import { getUserLevelTheme, normalizeUserLevel } from '@/utils/level'

const props = defineProps({
  username: {
    type: String,
    default: '匿名用户'
  },
  userLevel: {
    type: Number,
    default: 1
  },
  vipLevel: {
    type: Number,
    default: 0
  },
  glow: {
    type: Boolean,
    default: false
  },
  showBadge: {
    type: Boolean,
    default: true
  }
})

const normalizedLevel = computed(() => normalizeUserLevel(props.userLevel))
const levelTheme = computed(() => getUserLevelTheme(normalizedLevel.value))

const vipClass = computed(() => {
  if (props.vipLevel > 0) {
    return `vip-level-${props.vipLevel}`
  }
  return ''
})

const wrapperStyle = computed(() => ({
  filter: `drop-shadow(0 0 10px ${levelTheme.value.shadow})`
}))

const usernameStyle = computed(() => ({
  backgroundImage: levelTheme.value.textGradient
}))
</script>

<style scoped>
.vip-username {
  display: inline-flex;
  align-items: center;
  flex-wrap: wrap; /* 允许换行 */
  gap: 8px; /* 增大名称和标签之间的间距 */
  max-width: 100%;
  position: relative;
  z-index: 1;
}

.username-wrapper {
  display: inline-flex;
}

.username-text {
  font-weight: 700;
  transition: all 0.3s ease;
  background-size: 220% 220%;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  white-space: nowrap;
}

.badge-group {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.tier-1 .username-text,
.tier-3 .username-text {
  animation: levelFloat 4.6s ease-in-out infinite;
}

.tier-2 .username-text,
.tier-6 .username-text,
.tier-9 .username-text {
  animation: levelFlow 3.6s ease infinite;
}

.tier-4 .username-text,
.tier-8 .username-text,
.tier-10 .username-text {
  animation: levelShine 2.8s linear infinite;
}

.tier-5 .username-text,
.tier-7 .username-text {
  animation: levelPulse 2.8s ease-in-out infinite;
}

.tier-11 .username-text {
  animation: levelGenesis 4s linear infinite;
}

.vip-username.vip-level-3 .username-text {
  text-shadow: 0 0 12px rgba(255, 215, 0, 0.22);
}

@keyframes levelGenesis {
  0% { filter: hue-rotate(0deg); }
  100% { filter: hue-rotate(360deg); }
}

@keyframes levelFloat {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-1px);
  }
}

@keyframes levelFlow {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

@keyframes levelShine {
  0% {
    background-position: 0% 50%;
  }
  100% {
    background-position: 200% 50%;
  }
}

@keyframes levelPulse {
  0%, 100% {
    opacity: 0.92;
  }
  50% {
    opacity: 1;
  }
}
</style>
