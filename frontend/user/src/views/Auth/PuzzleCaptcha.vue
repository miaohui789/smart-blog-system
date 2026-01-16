<template>
  <div class="puzzle-captcha">
    <div class="captcha-image-container" ref="containerRef">
      <!-- 背景图片 -->
      <div class="captcha-bg" :style="{ backgroundImage: `url(${currentImage})` }">
        <!-- 缺口位置 -->
        <div class="captcha-slot" :style="slotStyle"></div>
      </div>
      <!-- 滑块拼图 -->
      <div 
        class="captcha-piece" 
        :style="pieceStyle"
        :class="{ success: isSuccess, error: isError }"
      ></div>
      <!-- 加载中 -->
      <div v-if="loading" class="captcha-loading">
        <div class="loading-spinner"></div>
      </div>
      <!-- 结果提示 -->
      <div v-if="isSuccess" class="captcha-result success">
        <el-icon><Check /></el-icon>
        <span>验证成功</span>
      </div>
      <div v-if="isError" class="captcha-result error">
        <el-icon><Close /></el-icon>
        <span>验证失败</span>
      </div>
    </div>
    
    <!-- 滑动条 -->
    <div class="slider-container" :class="{ success: isSuccess, error: isError }">
      <div class="slider-track">
        <div class="slider-progress" :style="{ width: `${sliderPosition + 22}px` }"></div>
      </div>
      <div 
        class="slider-thumb" 
        :style="{ left: `${sliderPosition}px` }"
        @mousedown="onMouseDown"
        @touchstart="onTouchStart"
      >
        <el-icon v-if="isSuccess"><Check /></el-icon>
        <el-icon v-else-if="isError"><Close /></el-icon>
        <el-icon v-else><Right /></el-icon>
      </div>
      <div class="slider-text" v-if="!isDragging && !isSuccess && !isError">
        向右滑动完成拼图
      </div>
    </div>
    
    <!-- 刷新按钮 -->
    <div class="captcha-actions">
      <button class="refresh-btn" @click="refreshCaptcha" :disabled="loading">
        <el-icon><Refresh /></el-icon>
        换一张
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { Right, Check, Close, Refresh } from '@element-plus/icons-vue'

const emit = defineEmits(['success'])

// 拼图图片列表（使用在线图片）
const images = [
  'https://picsum.photos/320/160?random=1',
  'https://picsum.photos/320/160?random=2',
  'https://picsum.photos/320/160?random=3',
  'https://picsum.photos/320/160?random=4',
  'https://picsum.photos/320/160?random=5'
]

const containerRef = ref(null)
const loading = ref(true)
const currentImage = ref('')
const slotX = ref(0) // 缺口X位置
const slotY = ref(0) // 缺口Y位置
const sliderPosition = ref(0)
const isDragging = ref(false)
const isSuccess = ref(false)
const isError = ref(false)
const startX = ref(0)

const pieceSize = 44 // 拼图块大小
const containerWidth = 320
const containerHeight = 160
const tolerance = 5 // 容差范围

// 缺口样式
const slotStyle = computed(() => ({
  left: `${slotX.value}px`,
  top: `${slotY.value}px`,
  width: `${pieceSize}px`,
  height: `${pieceSize}px`
}))

// 拼图块样式
const pieceStyle = computed(() => ({
  left: `${sliderPosition.value}px`,
  top: `${slotY.value}px`,
  width: `${pieceSize}px`,
  height: `${pieceSize}px`,
  backgroundImage: `url(${currentImage.value})`,
  backgroundPosition: `-${slotX.value}px -${slotY.value}px`
}))

// 初始化
onMounted(() => {
  initCaptcha()
})

// 初始化验证码
function initCaptcha() {
  loading.value = true
  isSuccess.value = false
  isError.value = false
  sliderPosition.value = 0
  
  // 随机选择图片
  const randomIndex = Math.floor(Math.random() * images.length)
  currentImage.value = images[randomIndex] + '&t=' + Date.now()
  
  // 随机生成缺口位置（确保在合理范围内）
  slotX.value = Math.floor(Math.random() * (containerWidth - pieceSize - 100)) + 100
  slotY.value = Math.floor(Math.random() * (containerHeight - pieceSize - 20)) + 10
  
  // 模拟图片加载
  const img = new Image()
  img.onload = () => {
    loading.value = false
  }
  img.onerror = () => {
    // 加载失败使用备用图片
    currentImage.value = `data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='320' height='160'%3E%3Cdefs%3E%3ClinearGradient id='g' x1='0%25' y1='0%25' x2='100%25' y2='100%25'%3E%3Cstop offset='0%25' stop-color='%23667eea'/%3E%3Cstop offset='100%25' stop-color='%23764ba2'/%3E%3C/linearGradient%3E%3C/defs%3E%3Crect width='320' height='160' fill='url(%23g)'/%3E%3C/svg%3E`
    loading.value = false
  }
  img.src = currentImage.value
}

// 刷新验证码
function refreshCaptcha() {
  initCaptcha()
}

// 鼠标事件
function onMouseDown(e) {
  if (isSuccess.value || loading.value) return
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
  checkResult()
}

// 触摸事件
function onTouchStart(e) {
  if (isSuccess.value || loading.value) return
  isDragging.value = true
  isError.value = false
  startX.value = e.touches[0].clientX - sliderPosition.value
  document.addEventListener('touchmove', onTouchMove, { passive: false })
  document.addEventListener('touchend', onTouchEnd)
}

function onTouchMove(e) {
  if (!isDragging.value) return
  e.preventDefault()
  moveSlider(e.touches[0].clientX - startX.value)
}

function onTouchEnd() {
  if (!isDragging.value) return
  isDragging.value = false
  document.removeEventListener('touchmove', onTouchMove)
  document.removeEventListener('touchend', onTouchEnd)
  checkResult()
}

// 移动滑块
function moveSlider(position) {
  const maxPosition = containerWidth - pieceSize
  if (position < 0) position = 0
  if (position > maxPosition) position = maxPosition
  sliderPosition.value = position
}

// 检查结果
function checkResult() {
  const diff = Math.abs(sliderPosition.value - slotX.value)
  
  if (diff <= tolerance) {
    isSuccess.value = true
    sliderPosition.value = slotX.value // 对齐
    setTimeout(() => {
      emit('success')
    }, 800)
  } else {
    isError.value = true
    // 回弹动画
    animateBack()
  }
}

// 回弹动画
function animateBack() {
  const startPos = sliderPosition.value
  const duration = 300
  const startTime = Date.now()
  
  function animate() {
    const elapsed = Date.now() - startTime
    const progress = Math.min(elapsed / duration, 1)
    const easeOut = 1 - Math.pow(1 - progress, 3)
    sliderPosition.value = startPos * (1 - easeOut)
    
    if (progress < 1) {
      requestAnimationFrame(animate)
    } else {
      setTimeout(() => {
        isError.value = false
      }, 300)
    }
  }
  animate()
}

onUnmounted(() => {
  document.removeEventListener('mousemove', onMouseMove)
  document.removeEventListener('mouseup', onMouseUp)
  document.removeEventListener('touchmove', onTouchMove)
  document.removeEventListener('touchend', onTouchEnd)
})
</script>

<style scoped>
.puzzle-captcha {
  width: 320px;
  margin: 0 auto;
}

.captcha-image-container {
  position: relative;
  width: 320px;
  height: 160px;
  border-radius: 12px;
  overflow: hidden;
  background: var(--bg-input);
}

.captcha-bg {
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
}

.captcha-slot {
  position: absolute;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 4px;
  box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.3);
}

.captcha-piece {
  position: absolute;
  background-size: 320px 160px;
  border-radius: 4px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
  transition: box-shadow 0.3s;
}

.captcha-piece.success {
  box-shadow: 0 0 20px rgba(34, 197, 94, 0.6);
}

.captcha-piece.error {
  box-shadow: 0 0 20px rgba(239, 68, 68, 0.6);
}

.captcha-loading {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.captcha-result {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  animation: fadeIn 0.3s ease;
}

.captcha-result.success {
  background: rgba(34, 197, 94, 0.9);
  color: white;
}

.captcha-result.error {
  background: rgba(239, 68, 68, 0.9);
  color: white;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.slider-container {
  position: relative;
  height: 44px;
  margin-top: 12px;
  background: var(--bg-input);
  border: 1px solid var(--border-color);
  border-radius: 22px;
  overflow: hidden;
  transition: all 0.3s;
}

.slider-container.success {
  border-color: #22c55e;
  background: rgba(34, 197, 94, 0.1);
}

.slider-container.error {
  border-color: #ef4444;
  background: rgba(239, 68, 68, 0.1);
}

.slider-track {
  position: absolute;
  inset: 0;
}

.slider-progress {
  height: 100%;
  background: linear-gradient(90deg, rgba(168, 85, 247, 0.2), rgba(168, 85, 247, 0.3));
  border-radius: 22px;
  transition: width 0.05s;
}

.slider-container.success .slider-progress {
  background: linear-gradient(90deg, rgba(34, 197, 94, 0.2), rgba(34, 197, 94, 0.3));
}

.slider-thumb {
  position: absolute;
  top: 0;
  width: 44px;
  height: 44px;
  background: #fff;
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  cursor: grab;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #a855f7;
  font-size: 20px;
  transition: box-shadow 0.3s;
  user-select: none;
  touch-action: none;
}

.slider-thumb:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.slider-thumb:active {
  cursor: grabbing;
}

.slider-container.success .slider-thumb {
  color: #22c55e;
}

.slider-container.error .slider-thumb {
  color: #ef4444;
}

.slider-text {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  color: var(--text-muted);
  pointer-events: none;
  user-select: none;
}

.captcha-actions {
  display: flex;
  justify-content: center;
  margin-top: 12px;
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: transparent;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  color: var(--text-muted);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s;
}

.refresh-btn:hover:not(:disabled) {
  border-color: #a855f7;
  color: #a855f7;
}

.refresh-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
