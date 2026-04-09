<template>
  <el-dialog
    v-model="visible"
    title=""
    width="400px"
    :show-close="false"
    center
    align-center
    class="level-up-dialog"
    destroy-on-close
    @opened="onDialogOpen"
  >
    <div class="level-up-content">
      <div class="animation-container">
        <!-- 动态发光背景 -->
        <div class="glow-bg"></div>
        <!-- 替换为用户项目的等级标签，放大显示 -->
        <div class="level-badge-wrapper" :class="{ 'is-animate': isAnimate }">
          <UserLevelBadge :level="levelData.afterLevel" class="large-level-badge" />
        </div>
      </div>
      
      <h2 class="title">恭喜升级！</h2>
      <p class="subtitle">您的等级已提升至 <strong class="highlight-level">Lv.{{ levelData.afterLevel }}</strong></p>
      
      <div class="exp-info glass-panel">
        <div class="info-item">
          <span class="label">当前经验</span>
          <span class="value">{{ levelData.currentExp }}</span>
        </div>
        <div class="divider"></div>
        <div class="info-item">
          <span class="label">获得经验</span>
          <span class="value highlight">+{{ levelData.expChange }}</span>
        </div>
      </div>
      
      <!-- 动态经验条 -->
      <div class="exp-bar-wrapper">
        <div class="exp-bar-header">
          <span class="exp-bar-label">升级进度</span>
          <span class="exp-bar-text">{{ animatedExp }} / {{ nextNeed }}</span>
        </div>
        <div class="exp-bar-track">
          <div class="exp-bar-fill" :style="{ width: barWidth + '%' }">
            <div class="exp-bar-glow"></div>
          </div>
        </div>
      </div>
      
      <div class="action-btn">
        <el-button type="primary" class="receive-btn" round @click="close">开心收下</el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import UserLevelBadge from '@/components/UserLevelBadge/index.vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const visible = ref(false)
const levelData = ref({
  beforeLevel: 1,
  afterLevel: 2,
  expChange: 10,
  currentExp: 0
})

const isAnimate = ref(false)
const animatedExp = ref(0)

const nextNeed = computed(() => {
  return userStore.expSummary?.nextLevelNeedExp || 100
})

const barWidth = computed(() => {
  if (nextNeed.value <= 0) return 100
  return Math.min(100, Math.round((animatedExp.value / nextNeed.value) * 100))
})

const show = (data) => {
  levelData.value = { ...data }
  isAnimate.value = false
  animatedExp.value = 0
  visible.value = true
}

const onDialogOpen = () => {
  // 触发徽章进入动画
  isAnimate.value = true
  
  // 触发经验条数字递增动画
  const targetExp = levelData.value.currentExp
  const duration = 1000 // 1秒动画
  const steps = 60
  const stepValue = targetExp / steps
  const stepTime = duration / steps
  
  let currentStep = 0
  const timer = setInterval(() => {
    currentStep++
    if (currentStep >= steps) {
      animatedExp.value = targetExp
      clearInterval(timer)
    } else {
      animatedExp.value = Math.floor(stepValue * currentStep)
    }
  }, stepTime)
}

const close = () => {
  visible.value = false
}

defineExpose({
  show,
  close
})
</script>

<style scoped>
.level-up-content {
  text-align: center;
  padding: 10px 10px 20px;
  color: var(--text-primary);
}

.animation-container {
  position: relative;
  width: 160px;
  height: 160px;
  margin: 0 auto 20px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.glow-bg {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100%;
  height: 100%;
  background: radial-gradient(circle, rgba(var(--el-color-primary-rgb), 0.3) 0%, transparent 70%);
  border-radius: 50%;
  animation: pulseGlowBg 2s infinite alternate;
  z-index: 1;
}

.level-badge-wrapper {
  position: relative;
  z-index: 2;
  opacity: 0;
  transform: scale(0.5) translateY(20px);
  transition: all 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.level-badge-wrapper.is-animate {
  opacity: 1;
  transform: scale(2) translateY(0); /* 放大标签以突出显示 */
}

/* 兼容深色/浅色模式的卡片 */
.glass-panel {
  background: linear-gradient(180deg, rgba(var(--bg-card-rgb), 0.96), rgba(var(--bg-card-rgb), 0.84));
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 24px;
  display: flex;
  justify-content: space-around;
  align-items: center;
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.08);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
}

.divider {
  width: 1px;
  height: 30px;
  background-color: var(--border-color);
}

.title {
  margin-bottom: 8px;
  font-size: 24px;
  font-weight: bold;
  letter-spacing: 1px;
  background: linear-gradient(135deg, var(--el-text-color-primary), var(--el-text-color-regular));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.subtitle {
  color: var(--el-text-color-regular);
  margin-bottom: 24px;
  font-size: 14px;
}

.highlight-level {
  color: var(--el-color-primary);
  font-weight: 800;
  font-size: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
}

.info-item .label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.info-item .value {
  font-size: 20px;
  font-weight: bold;
  font-family: ui-monospace, SFMono-Regular, Monaco, monospace;
}

.info-item .highlight {
  color: var(--el-color-success);
}

/* 经验条样式 */
.exp-bar-wrapper {
  margin-bottom: 30px;
  text-align: left;
}

.exp-bar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 13px;
}

.exp-bar-label {
  color: var(--el-text-color-regular);
  font-weight: 500;
}

.exp-bar-text {
  color: var(--el-text-color-secondary);
  font-family: ui-monospace, SFMono-Regular, Monaco, monospace;
}

.exp-bar-track {
  height: 8px;
  background: rgba(var(--bg-card-rgb), 0.72);
  border-radius: 4px;
  overflow: hidden;
  position: relative;
  box-shadow: inset 0 0 0 1px var(--border-color);
}

.exp-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--el-color-primary-light-3), var(--el-color-primary));
  border-radius: 4px;
  transition: width 0.1s linear;
  position: relative;
}

.exp-bar-glow {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.4), transparent);
  animation: barScan 1.5s infinite linear;
}

.action-btn {
  margin-top: 10px;
}

.receive-btn {
  width: 160px;
  height: 40px;
  font-size: 16px;
  font-weight: bold;
  letter-spacing: 2px;
  box-shadow: 0 4px 12px rgba(var(--el-color-primary-rgb), 0.4);
  transition: all 0.3s ease;
}

.receive-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(var(--el-color-primary-rgb), 0.6);
}

@keyframes pulseGlowBg {
  0% { transform: translate(-50%, -50%) scale(0.8); opacity: 0.5; }
  100% { transform: translate(-50%, -50%) scale(1.2); opacity: 1; }
}

@keyframes barScan {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

:deep(.level-up-dialog) {
  background: transparent !important;
  box-shadow: none !important;
  /* 移动端适配：弹窗宽度适应屏幕 */
  @media (max-width: 480px) {
    width: 90% !important;
    max-width: 360px;
  }
}

:deep(.level-up-dialog .el-dialog) {
  border-radius: 20px;
  background: transparent;
  box-shadow: none;
}

:deep(.level-up-dialog .el-dialog__body) {
  padding: 24px 24px 20px;
  background: linear-gradient(180deg, rgba(var(--bg-card-rgb), 0.98), rgba(var(--bg-card-rgb), 0.92));
  border-radius: 20px;
  box-shadow: 0 28px 80px rgba(15, 23, 42, 0.22);
  border: 1px solid var(--border-color);
  overflow: hidden;
  backdrop-filter: blur(22px);
  -webkit-backdrop-filter: blur(22px);
}

:deep(.el-dialog__header) {
  display: none;
}
</style>
