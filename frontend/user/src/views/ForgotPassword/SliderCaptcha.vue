<template>
  <div class="slider-captcha">
    <div class="captcha-container" :class="{ success: isSuccess, error: isError }">
      <div class="captcha-bg">
        <div class="captcha-progress" :style="{ width: progressWidth }"></div>
      </div>
      <div 
        class="captcha-slider" 
        :style="{ left: sliderLeft }"
        @mousedown="onMouseDown"
        @touchstart="onTouchStart"
      >
        <div class="slider-icon">
          <el-icon v-if="isSuccess"><Check /></el-icon>
          <el-icon v-else-if="isError"><Close /></el-icon>
          <el-icon v-else><Right /></el-icon>
        </div>
      </div>
      <div class="captcha-text">
        <span v-if="isSuccess">验证成功</span>
        <span v-else-if="isError">验证失败，请重试</span>
        <span v-else>{{ isDragging ? '继续拖动' : '向右滑动完成验证' }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { Right, Check, Close } from '@element-plus/icons-vue'

const emit = defineEmits(['success'])

const sliderPosition = ref(0)
const isDragging = ref(false)
const isSuccess = ref(false)
const isError = ref(false)
const containerWidth = ref(300)
const sliderWidth = 44
const startX = ref(0)

const sliderLeft = computed(() => `${sliderPosition.value}px`)
const progressWidth = computed(() => `${sliderPosition.value + sliderWidth}px`)

// 鼠标事件
function onMouseDown(e) {
  if (isSuccess.value) return
  isDragging.value = true
  isError.value = false
  startX.value = e.clientX - sliderPosition.value
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
}

function onMouseMove(e) {
  if (!isDragging.value) return
  moveSlider(e.clientX - startX.value)
}

function onMouseUp() {
  if (!isDragging.value) return
  isDragging.value = false
  document.removeEventListener('mousemove', onMouseMove)
  document.removeEventListener('mouseup', onMouseUp)
  checkSuccess()
}

// 触摸事件
function onTouchStart(e) {
  if (isSuccess.value) return
  isDragging.value = true
  isError.value = false
  startX.value = e.touches[0].clientX - sliderPosition.value
  document.addEventListener('touchmove', onTouchMove)
  document.addEventListener('touchend', onTouchEnd)
}

function onTouchMove(e) {
  if (!isDragging.value) return
  moveSlider(e.touches[0].clientX - startX.value)
}

function onTouchEnd() {
  if (!isDragging.value) return
  isDragging.value = false
  document.removeEventListener('touchmove', onTouchMove)
  document.removeEventListener('touchend', onTouchEnd)
  checkSuccess()
}

// 移动滑块
function moveSlider(position) {
  const maxPosition = containerWidth.value - sliderWidth
  if (position < 0) position = 0
  if (position > maxPosition) position = maxPosition
  sliderPosition.value = position
}

// 检查是否成功
function checkSuccess() {
  const maxPosition = containerWidth.value - sliderWidth
  const threshold = maxPosition * 0.95 // 95%即为成功
  
  if (sliderPosition.value >= threshold) {
    isSuccess.value = true
    sliderPosition.value = maxPosition
    setTimeout(() => {
      emit('success')
    }, 500)
  } else {
    isError.value = true
    // 回弹动画
    const startPos = sliderPosition.value
    const duration = 300
    const startTime = Date.now()
    
    function animate() {
      const elapsed = Date.now() - startTime
      const progress = Math.min(elapsed / duration, 1)
      // 缓动函数
      const easeOut = 1 - Math.pow(1 - progress, 3)
      sliderPosition.value = startPos * (1 - easeOut)
      
      if (progress < 1) {
        requestAnimationFrame(animate)
      } else {
        setTimeout(() => {
          isError.value = false
        }, 500)
      }
    }
    animate()
  }
}

// 获取容器宽度
onMounted(() => {
  const container = document.querySelector('.captcha-container')
  if (container) {
    containerWidth.value = container.offsetWidth
  }
})

onUnmounted(() => {
  document.removeEventListener('mousemove', onMouseMove)
  document.removeEventListener('mouseup', onMouseUp)
  document.removeEventListener('touchmove', onTouchMove)
  document.removeEventListener('touchend', onTouchEnd)
})
</script>

<style scoped>
.slider-captcha {
  padding: 20px 0;
}

.captcha-container {
  position: relative;
  height: 44px;
  background: var(--bg-input);
  border: 1px solid var(--border-color);
  border-radius: 22px;
  overflow: hidden;
  transition: all 0.3s;
}

.captcha-container.success {
  border-color: #22c55e;
  background: rgba(34, 197, 94, 0.1);
}

.captcha-container.error {
  border-color: #ef4444;
  background: rgba(239, 68, 68, 0.1);
}

.captcha-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.captcha-progress {
  height: 100%;
  background: linear-gradient(90deg, rgba(168, 85, 247, 0.2), rgba(168, 85, 247, 0.3));
  border-radius: 22px;
  transition: width 0.05s;
}

.captcha-container.success .captcha-progress {
  background: linear-gradient(90deg, rgba(34, 197, 94, 0.2), rgba(34, 197, 94, 0.3));
}

.captcha-slider {
  position: absolute;
  top: 0;
  left: 0;
  width: 44px;
  height: 44px;
  background: #fff;
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  cursor: grab;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: box-shadow 0.3s;
  user-select: none;
  touch-action: none;
}

.captcha-slider:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.captcha-slider:active {
  cursor: grabbing;
}

.slider-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #a855f7;
  font-size: 20px;
}

.captcha-container.success .slider-icon {
  color: #22c55e;
}

.captcha-container.error .slider-icon {
  color: #ef4444;
}

.captcha-text {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: var(--text-muted);
  pointer-events: none;
  user-select: none;
}

.captcha-container.success .captcha-text {
  color: #22c55e;
}

.captcha-container.error .captcha-text {
  color: #ef4444;
}
</style>
