<template>
  <span class="user-level-badge" :class="[`tier-${theme.tier}`, `effect-${theme.effect}`]" :style="badgeStyle" :title="`${theme.name} ${theme.label}`">
    <span class="badge-bg"></span>
    <span class="badge-glow"></span>
    <span class="badge-spark spark-a" v-if="hasSparks"></span>
    <span class="badge-spark spark-b" v-if="hasSparks"></span>
    <span class="badge-scan" v-if="hasScan"></span>
    <span class="badge-content">
      <span class="badge-level">Lv.{{ theme.level }}</span>
      <span class="badge-name">{{ theme.shortName }}</span>
    </span>
  </span>
</template>

<script setup>
import { computed } from 'vue'
import { getUserLevelTheme } from '@/utils/level'

const props = defineProps({
  level: {
    type: Number,
    default: 1
  }
})

const theme = computed(() => getUserLevelTheme(props.level))

const badgeStyle = computed(() => ({
  '--badge-gradient': theme.value.gradient,
  '--badge-text-gradient': theme.value.textGradient,
  '--badge-border': theme.value.border,
  '--badge-shadow': theme.value.shadow
}))

const hasSparks = computed(() => ['orbit', 'meteor', 'crown', 'genesis'].includes(theme.value.effect))
const hasScan = computed(() => ['shine', 'spectrum', 'crown', 'genesis'].includes(theme.value.effect))
</script>

<style scoped>
.user-level-badge {
  position: relative;
  display: inline-flex;
  align-items: center;
  border-radius: 12px;
  padding: 1px;
  background: var(--badge-border); /* 外边框使用 border 颜色，减少刺眼感 */
  box-shadow: 0 2px 6px var(--badge-shadow);
  vertical-align: middle;
  overflow: hidden;
  transition: all 0.3s ease;
  transform: translateZ(0);
  flex-shrink: 0; /* 防止被挤压导致竖排 */
  white-space: nowrap;
}

:root[data-theme="dark"] .user-level-badge {
  /* 暗色模式下减弱外发光，提高对比度 */
  box-shadow: 0 0 4px var(--badge-shadow);
}

.user-level-badge:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 10px var(--badge-shadow);
}

:root[data-theme="dark"] .user-level-badge:hover {
  box-shadow: 0 2px 8px var(--badge-shadow);
}

.badge-bg {
  position: absolute;
  inset: 1px;
  border-radius: 11px;
  background: var(--bg-card-solid);
  z-index: 1;
  transition: background-color 0.3s;
}

:root[data-theme="dark"] .badge-bg {
  /* 暗色模式下背景稍微加深，让文字更清晰 */
  background: rgba(30, 30, 35, 0.95);
}

.badge-glow {
  position: absolute;
  inset: 1px;
  border-radius: 11px;
  background: var(--badge-gradient);
  opacity: 0.15;
  z-index: 2;
  mix-blend-mode: normal; /* 移除 overlay，避免在暗色模式下对比度过高或失效 */
}

:root[data-theme="dark"] .badge-glow {
  opacity: 0.25; /* 暗色模式下由于背景较暗，适当提高发光层透明度，但使用 normal 模式 */
}

.badge-spark {
  position: absolute;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.8); /* 统一星光颜色为白色，避免暗色下变黑不亮 */
  opacity: 0.4;
  filter: blur(1px);
  z-index: 2;
}

.spark-a { top: 0; left: 10%; }
.spark-b { bottom: 0; right: 10%; }

.badge-scan {
  position: absolute;
  top: 0;
  left: -50%;
  width: 50%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent); /* 统一用白色半透明扫描线 */
  opacity: 0.15;
  transform: skewX(-20deg);
  z-index: 2;
}

.badge-content {
  position: relative;
  z-index: 3;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 1px 8px;
  border-radius: 11px;
  line-height: 1.2;
  white-space: nowrap; /* 修复竖排问题 */
}

/* 100级超级样式 - 名片长方形样式 */
.user-level-badge.tier-11 {
  border-radius: 6px; /* 更加方正 */
  padding: 1px;
  box-shadow: 0 4px 12px var(--badge-shadow);
  transform: scale(1.05); /* 稍微放大 */
  margin: 0 4px;
  background: var(--badge-gradient); /* 100级直接使用渐变作为外边框，取消多余嵌套感 */
  position: relative;
  z-index: 1;
}

.user-level-badge.tier-11 .badge-glow {
  display: block;
  opacity: 0.8;
  filter: blur(8px);
}

.user-level-badge.tier-11 .badge-bg {
  border-radius: 5px;
  background: var(--bg-card, #ffffff);
  position: absolute;
  inset: 1px;
  z-index: 2;
  overflow: hidden;
}

.user-level-badge.tier-11 .badge-bg::after {
  content: '';
  position: absolute;
  inset: 0;
  background: var(--badge-gradient);
  opacity: 0.15;
}

:root[data-theme="dark"] .user-level-badge.tier-11 .badge-bg {
  background: var(--bg-card, #1a1a1a);
}

:root[data-theme="dark"] .user-level-badge.tier-11 .badge-bg::after {
  opacity: 0.2;
}

.user-level-badge.tier-11 .badge-content {
  border-radius: 5px;
  padding: 2px 10px;
  gap: 6px;
  position: relative;
  z-index: 3;
}

.user-level-badge.tier-11 .badge-scan {
  z-index: 3;
}

.user-level-badge.tier-11 .badge-spark {
  z-index: 3;
}

.badge-level {
  font-size: 11px;
  font-weight: 900;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
  font-style: italic;
  background: var(--badge-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  padding-right: 0;
  white-space: nowrap;
  word-break: keep-all;
}

.badge-name {
  font-size: 11px;
  font-weight: 600;
  background: var(--badge-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  white-space: nowrap;
  word-break: keep-all;
}

/* Effects */
.effect-mist .badge-glow {
  animation: mistMove 4s ease-in-out infinite alternate;
}

.effect-flow,
.effect-flow .badge-level {
  background-size: 200% 200%;
  animation: bgFlow 3s ease infinite;
}

.effect-breathe {
  animation: breathe 3s ease-in-out infinite;
}

.effect-shine .badge-scan {
  animation: scan 2.5s ease-in-out infinite;
}

.effect-orbit .spark-a {
  animation: orbitA 3s linear infinite;
}
.effect-orbit .spark-b {
  animation: orbitB 3s linear infinite;
}

.effect-galaxy {
  animation: float 4s ease-in-out infinite;
}
.effect-galaxy .badge-glow {
  animation: pulseGlow 4s ease-in-out infinite;
}

.effect-pulse {
  animation: pulse 2s ease-in-out infinite;
}

.effect-meteor .spark-a {
  animation: meteor 2s linear infinite;
  animation-delay: 0s;
}
.effect-meteor .spark-b {
  animation: meteor 2s linear infinite;
  animation-delay: 1s;
}

.effect-spectrum,
.effect-spectrum .badge-level {
  background-size: 200% 200%;
  animation: bgFlow 2s linear infinite;
}
.effect-spectrum .badge-scan {
  animation: scan 2s linear infinite;
}

.effect-crown {
  animation: float 3s ease-in-out infinite;
}
.effect-crown .badge-scan {
  animation: scan 3s ease-in-out infinite;
}
.effect-crown .badge-glow {
  animation: pulseGlow 3s ease-in-out infinite;
}

.effect-genesis {
  animation: float 3s ease-in-out infinite, pulse 2s ease-in-out infinite;
}
.effect-genesis .badge-glow {
  animation: pulseGlow 2s ease-in-out infinite;
  opacity: 0.3;
}
.effect-genesis .badge-scan {
  animation: scan 1.5s ease-in-out infinite;
}
.effect-genesis .badge-level,
.effect-genesis .badge-name {
  animation: colorShift 4s linear infinite;
}

/* Keyframes */
@keyframes colorShift {
  0% { filter: hue-rotate(0deg); }
  100% { filter: hue-rotate(360deg); }
}

@keyframes mistMove {
  0% { opacity: 0.1; filter: blur(2px); }
  100% { opacity: 0.25; filter: blur(4px); }
}

@keyframes bgFlow {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

@keyframes breathe {
  0%, 100% { box-shadow: 0 2px 8px var(--badge-shadow); }
  50% { box-shadow: 0 6px 16px var(--badge-shadow); }
}

@keyframes scan {
  0% { left: -50%; }
  100% { left: 150%; }
}

@keyframes orbitA {
  0% { transform: translate(0, 0); }
  25% { transform: translate(20px, 12px); }
  50% { transform: translate(40px, 0); }
  75% { transform: translate(20px, -12px); }
  100% { transform: translate(0, 0); }
}

@keyframes orbitB {
  0% { transform: translate(0, 0); }
  25% { transform: translate(-20px, -12px); }
  50% { transform: translate(-40px, 0); }
  75% { transform: translate(-20px, 12px); }
  100% { transform: translate(0, 0); }
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-2px); }
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.03); box-shadow: 0 6px 16px var(--badge-shadow); }
}

@keyframes pulseGlow {
  0%, 100% { opacity: 0.15; }
  50% { opacity: 0.3; }
}

@keyframes meteor {
  0% { transform: translate(-20px, -10px); opacity: 0; }
  20% { opacity: 0.8; }
  80% { opacity: 0; }
  100% { transform: translate(40px, 20px); opacity: 0; }
}
</style>
