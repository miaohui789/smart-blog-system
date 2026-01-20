<template>
  <div class="skin-page">
    <div class="skin-container">
      <div class="page-header">
        <div class="header-content">
          <h1 class="page-title">我的皮肤</h1>
          <p class="page-desc">选择你喜欢的背景主题，让页面更具个性</p>
        </div>
      </div>
      
      <div class="theme-section">
        <h2 class="section-title">
          <el-icon><Moon /></el-icon>
          暗色主题背景
        </h2>
        <div class="skin-grid">
          <div 
            v-for="skin in darkSkins" 
            :key="skin.id"
            class="skin-card glass-card"
            :class="{ active: currentDarkSkin === skin.id }"
            @click="selectSkin('dark', skin.id)"
            @mouseenter="handlePreviewEnter(skin.id)"
            @mouseleave="handlePreviewLeave"
          >
            <div class="skin-preview" :style="{ background: skin.preview }">
              <!-- 黑客帝国预览 -->
              <div v-if="skin.id === 'matrix'" class="matrix-preview-container">
                <div v-if="previewSkin === 'matrix'" class="mini-matrix">
                  <div class="mini-matrix-column" v-for="n in 8" :key="n"></div>
                </div>
                <div v-else class="matrix-preview-hint">
                  <el-icon class="preview-icon"><VideoCameraFilled /></el-icon>
                  <span class="hint-text">黑客帝国</span>
                </div>
              </div>
              <!-- 暗夜紫预览 -->
              <div v-else-if="skin.id === 'dark-2'" class="colorgrid-preview-container">
                <div v-if="previewSkin === 'dark-2'" class="mini-colorgrid"></div>
                <div v-else class="colorgrid-preview-hint">
                  <el-icon class="preview-icon"><VideoCameraFilled /></el-icon>
                  <span class="hint-text">彩色斑点</span>
                </div>
              </div>
              <!-- 墨绿预览 -->
              <div v-else-if="skin.id === 'dark-3'" class="hexagon-preview-container">
                <div v-if="previewSkin === 'dark-3'" class="mini-hexagon"></div>
                <div v-else class="hexagon-preview-hint">
                  <el-icon class="preview-icon"><VideoCameraFilled /></el-icon>
                  <span class="hint-text">方块</span>
                </div>
              </div>
              <div v-else class="default-preview">
                <span class="skin-name">{{ skin.name }}</span>
              </div>
            </div>
            <div class="skin-info">
              <span class="skin-label">{{ skin.name }}</span>
              <div class="skin-check">
                <el-icon v-if="currentDarkSkin === skin.id"><Select /></el-icon>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="theme-section">
        <h2 class="section-title">
          <el-icon><Sunny /></el-icon>
          亮色主题背景
        </h2>
        <div class="skin-grid">
          <div 
            v-for="skin in lightSkins" 
            :key="skin.id"
            class="skin-card glass-card"
            :class="{ active: currentLightSkin === skin.id }"
            @click="selectSkin('light', skin.id)"
            @mouseenter="handlePreviewEnter(skin.id)"
            @mouseleave="handlePreviewLeave"
          >
            <div class="skin-preview" :style="{ background: skin.preview }">
              <!-- 清新蓝预览 -->
              <div v-if="skin.id === 'light-1'" class="movingdots-preview-container">
                <div v-if="previewSkin === 'light-1'" class="mini-movingdots"></div>
                <div v-else class="movingdots-preview-hint">
                  <el-icon class="preview-icon"><VideoCameraFilled /></el-icon>
                  <span class="hint-text">清新蓝</span>
                </div>
              </div>
              <!-- 紫色骨头预览 -->
              <div v-else-if="skin.id === 'light-2'" class="purpledots-preview-container">
                <div v-if="previewSkin === 'light-2'" class="mini-purpledots"></div>
                <div v-else class="purpledots-preview-hint">
                  <el-icon class="preview-icon"><VideoCameraFilled /></el-icon>
                  <span class="hint-text">紫色骨头</span>
                </div>
              </div>
              <!-- 温暖橙预览 -->
              <div v-else-if="skin.id === 'light-3'" class="geometric-preview-container">
                <div v-if="previewSkin === 'light-3'" class="mini-geometric"></div>
                <div v-else class="geometric-preview-hint">
                  <el-icon class="preview-icon"><VideoCameraFilled /></el-icon>
                  <span class="hint-text">立体</span>
                </div>
              </div>
              <div v-else class="default-preview">
                <span class="skin-name">{{ skin.name }}</span>
              </div>
            </div>
            <div class="skin-info">
              <span class="skin-label">{{ skin.name }}</span>
              <div class="skin-check">
                <el-icon v-if="currentLightSkin === skin.id"><Select /></el-icon>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Select, Moon, Sunny, VideoCameraFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { saveTheme, getTheme } from '@/api/user'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const currentDarkSkin = ref('default')
const currentLightSkin = ref('default')
const previewSkin = ref(null)

const darkSkins = ref([
  { id: 'default', name: '默认', preview: '#18181b', component: null },
  { id: 'matrix', name: '黑客帝国', preview: 'linear-gradient(135deg, #000000 0%, #003300 100%)', component: 'MatrixRain' },
  { id: 'dark-2', name: '彩色斑点', preview: 'linear-gradient(135deg, #f093fb 0%, #f5576c 25%, #ffd140 50%, #4facfe 75%, #00f2fe 100%)', component: 'ColorGrid' },
  { id: 'dark-3', name: '方块', preview: 'linear-gradient(135deg, #2d2d2d 0%, #1d1d1d 100%)', component: 'HexagonPattern' }
])

const lightSkins = ref([
  { id: 'default', name: '默认', preview: '#f4f4f5', component: null },
  { id: 'light-1', name: '清新蓝', preview: 'linear-gradient(135deg, #87ceeb 0%, #3498db 100%)', component: 'MovingDots' },
  { id: 'light-2', name: '紫色骨头', preview: 'linear-gradient(135deg, #c09bd8 0%, #9f84bd 100%)', component: 'PurpleDots' },
  { id: 'light-3', name: '立体', preview: 'linear-gradient(135deg, #e5e5e5 0%, #9ca3af 100%)', component: 'GeometricPattern' }
])

function selectSkin(theme, skinId) {
  const skin = theme === 'dark' 
    ? darkSkins.value.find(s => s.id === skinId)
    : lightSkins.value.find(s => s.id === skinId)
  
  console.log('选择皮肤:', theme, skinId, skin)
  
  if (!skin) return
  
  if (theme === 'dark') {
    currentDarkSkin.value = skinId
  } else {
    currentLightSkin.value = skinId
  }
  
  // 保存到 localStorage
  const skinData = {
    theme,
    skinId,
    component: skin.component
  }
  console.log('保存皮肤数据:', skinData)
  localStorage.setItem(`${theme}Skin`, JSON.stringify(skinData))
  
  // 触发全局事件通知背景组件更新
  console.log('触发skin-change事件')
  window.dispatchEvent(new CustomEvent('skin-change', { detail: skinData }))
  
  // 如果用户已登录，保存到服务器
  if (userStore.isLoggedIn) {
    saveThemeToServer()
  }
  
  ElMessage.success('皮肤已切换')
}

// 保存主题设置到服务器
async function saveThemeToServer() {
  try {
    // 获取当前主题模式
    const themeMode = localStorage.getItem('theme') || 'dark'
    
    await saveTheme({
      themeMode,
      darkSkin: currentDarkSkin.value,
      lightSkin: currentLightSkin.value
    })
    console.log('主题设置已保存到服务器')
  } catch (e) {
    console.error('保存主题设置失败:', e)
  }
}

function handlePreviewEnter(skinId) {
  previewSkin.value = skinId
}

function handlePreviewLeave() {
  previewSkin.value = null
}

onMounted(() => {
  // 如果用户已登录，从服务器加载主题设置
  if (userStore.isLoggedIn) {
    loadThemeFromServer()
  } else {
    // 未登录时从 localStorage 加载
    loadThemeFromLocal()
  }
})

// 从服务器加载主题设置
async function loadThemeFromServer() {
  try {
    const res = await getTheme()
    if (res.code === 200 && res.data) {
      const { darkSkin, lightSkin } = res.data
      
      // 更新当前选择
      currentDarkSkin.value = darkSkin || 'default'
      currentLightSkin.value = lightSkin || 'default'
      
      // 同步到 localStorage
      if (darkSkin) {
        const darkSkinObj = darkSkins.value.find(s => s.id === darkSkin)
        if (darkSkinObj) {
          localStorage.setItem('darkSkin', JSON.stringify({
            theme: 'dark',
            skinId: darkSkin,
            component: darkSkinObj.component
          }))
        }
      }
      
      if (lightSkin) {
        const lightSkinObj = lightSkins.value.find(s => s.id === lightSkin)
        if (lightSkinObj) {
          localStorage.setItem('lightSkin', JSON.stringify({
            theme: 'light',
            skinId: lightSkin,
            component: lightSkinObj.component
          }))
        }
      }
      
      // 触发背景更新
      const currentTheme = localStorage.getItem('theme') || 'dark'
      const currentSkinId = currentTheme === 'dark' ? darkSkin : lightSkin
      const currentSkinObj = currentTheme === 'dark' 
        ? darkSkins.value.find(s => s.id === currentSkinId)
        : lightSkins.value.find(s => s.id === currentSkinId)
      
      if (currentSkinObj) {
        window.dispatchEvent(new CustomEvent('skin-change', { 
          detail: {
            theme: currentTheme,
            skinId: currentSkinId,
            component: currentSkinObj.component
          }
        }))
      }
    }
  } catch (e) {
    console.error('加载主题设置失败:', e)
    // 失败时从 localStorage 加载
    loadThemeFromLocal()
  }
}

// 从 localStorage 加载主题设置
function loadThemeFromLocal() {
  const darkSkinData = localStorage.getItem('darkSkin')
  const lightSkinData = localStorage.getItem('lightSkin')
  
  if (darkSkinData) {
    try {
      const data = JSON.parse(darkSkinData)
      currentDarkSkin.value = data.skinId || data || 'default'
    } catch (e) {
      currentDarkSkin.value = darkSkinData || 'default'
    }
  }
  
  if (lightSkinData) {
    try {
      const data = JSON.parse(lightSkinData)
      currentLightSkin.value = data.skinId || data || 'default'
    } catch (e) {
      currentLightSkin.value = lightSkinData || 'default'
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.skin-page {
  min-height: calc(100vh - 80px);
  padding: $spacing-xl 0;
  
  .header-content {
    border-radius: 20px !important;
  }
}

.skin-container {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: $spacing-2xl;
  background: transparent;
  border-radius: 20px !important;
  
  .header-content {
    border-radius: 20px !important;
  }
}

.header-content {
  padding: $spacing-xl;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 20px !important;
  overflow: hidden;
}

// 自定义背景下强制透明但保持圆角
body.has-custom-bg .skin-page .page-header {
  background: transparent !important;
  border-radius: 20px !important;
  
  .header-content {
    background: rgba(var(--bg-card-rgb), 0.65) !important;
    backdrop-filter: blur(12px) !important;
    -webkit-backdrop-filter: blur(12px) !important;
    border-radius: 20px !important;
  }
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: $spacing-sm;
}

.page-desc {
  font-size: 14px;
  color: var(--text-muted);
  line-height: 1.6;
}

.theme-section {
  margin-bottom: $spacing-2xl;
}

.section-title {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: $spacing-lg;
  padding-bottom: $spacing-md;
  border-bottom: 1px solid var(--border-color);
  
  .el-icon {
    color: $primary-color;
    font-size: 20px;
  }
}

.skin-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: $spacing-lg;
}

.skin-card {
  position: relative;
  cursor: pointer;
  border-radius: $radius-md;
  overflow: hidden;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    border-color: rgba($primary-color, 0.3);
    box-shadow: $shadow-md;
  }
  
  &.active {
    border-color: $primary-color;
    box-shadow: 0 0 0 2px rgba($primary-color, 0.15);
    
    .skin-info {
      background: rgba($primary-color, 0.08);
    }
  }
}

.skin-preview {
  height: 150px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.default-preview {
  position: relative;
  z-index: 1;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, rgba(0,0,0,0.3), rgba(0,0,0,0.1));
  }
}

.skin-name {
  position: relative;
  z-index: 1;
  color: rgba(255, 255, 255, 0.9);
  font-size: 16px;
  font-weight: 600;
  text-shadow: 0 2px 8px rgba(0,0,0,0.4);
}

.matrix-preview-hint {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-sm;
  width: 100%;
  height: 100%;
  justify-content: center;
  
  .preview-icon {
    font-size: 36px;
    color: #00ff41;
    filter: drop-shadow(0 0 10px rgba(0, 255, 65, 0.6));
  }
  
  .hint-text {
    color: #00ff41;
    font-size: 13px;
    font-weight: 600;
    text-shadow: 0 0 10px rgba(0, 255, 65, 0.8);
  }
}

.matrix-preview-container {
  position: relative;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.mini-matrix {
  position: absolute;
  inset: 0;
  display: flex;
  justify-content: space-around;
  overflow: hidden;
}

.mini-matrix-column {
  position: relative;
  width: 12px;
  height: 100%;
  font-size: 10px;
  line-height: 11px;
  font-weight: 500;
  animation: miniFall linear infinite;
  white-space: nowrap;
  
  &::before {
    content: "public class void if else for while return new this";
    position: absolute;
    top: 0;
    left: 0;
    background: linear-gradient(to bottom, #ffffff 0%, #00ff41 20%, #00dd33 40%, #009911 60%, #005500 80%, transparent 100%);
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
    writing-mode: vertical-lr;
    letter-spacing: 1px;
  }
  
  &:nth-child(1) { animation-delay: -1s; animation-duration: 3s; }
  &:nth-child(2) { animation-delay: -1.5s; animation-duration: 3.5s; }
  &:nth-child(3) { animation-delay: -0.8s; animation-duration: 2.8s; }
  &:nth-child(4) { animation-delay: -1.2s; animation-duration: 3.2s; }
  &:nth-child(5) { animation-delay: -1.8s; animation-duration: 3.8s; }
  &:nth-child(6) { animation-delay: -0.5s; animation-duration: 2.5s; }
  &:nth-child(7) { animation-delay: -1.3s; animation-duration: 3.3s; }
  &:nth-child(8) { animation-delay: -0.9s; animation-duration: 2.9s; }
  
  &:nth-child(odd)::before {
    content: "public static void main String args System";
  }
  
  &:nth-child(even)::before {
    content: "class extends implements interface abstract";
  }
}

@keyframes miniFall {
  0% {
    transform: translateY(-100%);
    opacity: 1;
  }
  100% {
    transform: translateY(200%);
    opacity: 0;
  }
}

.colorgrid-preview-container {
  position: relative;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.colorgrid-preview-hint {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-sm;
  width: 100%;
  height: 100%;
  justify-content: center;
  
  .preview-icon {
    font-size: 36px;
    color: #ffffff;
    filter: drop-shadow(0 0 10px rgba(255, 255, 255, 0.6));
  }
  
  .hint-text {
    color: #ffffff;
    font-size: 13px;
    font-weight: 600;
    text-shadow: 0 0 10px rgba(255, 255, 255, 0.8);
  }
}

.mini-colorgrid {
  position: absolute;
  inset: 0;
  --c: 5px;
  background-color: #000;
  background-image: 
    radial-gradient(circle at 50% 50%, #0000 1px, #000 0 var(--c), #0000 var(--c)),
    radial-gradient(circle at 50% 50%, #0000 1px, #000 0 var(--c), #0000 var(--c)),
    radial-gradient(circle at 50% 50%, #f00, #f000 60%),
    radial-gradient(circle at 50% 50%, #ff0, #ff00 60%),
    radial-gradient(circle at 50% 50%, #0f0, #0f00 60%),
    radial-gradient(ellipse at 50% 50%, #00f, #00f0 60%);
  background-size: 
    8px 13.856406px,
    8px 13.856406px,
    150% 150%,
    150% 150%,
    150% 150%,
    150% 13.856406px;
  --p: 0px 0px, 4px 6.928203px;
  background-position: var(--p), 0% 0%, 0% 0%, 0% 0px;
  animation: miniWee 20s linear infinite, miniFilt 3s linear infinite;
}

@keyframes miniFilt {
  0% {
    filter: hue-rotate(0deg);
  }
  to {
    filter: hue-rotate(360deg);
  }
}

@keyframes miniWee {
  0% {
    background-position: var(--p), 400% 200%, 500% -200%, -600% -300%, 200% 27.712812px;
  }
  to {
    background-position: var(--p), 0% 0%, 0% 0%, 0% 0%, 0% 0%;
  }
}

.hexagon-preview-container {
  position: relative;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.hexagon-preview-hint {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-sm;
  width: 100%;
  height: 100%;
  justify-content: center;
  
  .preview-icon {
    font-size: 36px;
    color: #d1d5db;
    filter: drop-shadow(0 0 10px rgba(209, 213, 219, 0.6));
  }
  
  .hint-text {
    color: #e5e7eb;
    font-size: 13px;
    font-weight: 600;
    text-shadow: 0 0 10px rgba(229, 231, 235, 0.8);
  }
}

.mini-hexagon {
  position: absolute;
  inset: 0;
  --s: 80px;
  --c1: #1d1d1d;
  --c2: #4e4f51;
  --c3: #3c3c3c;
  background: 
    repeating-conic-gradient(from 30deg, #0000 0 120deg, var(--c3) 0 180deg)
      calc(0.5 * var(--s)) calc(0.5 * var(--s) * 0.577),
    repeating-conic-gradient(from 30deg, var(--c1) 0 60deg, var(--c2) 0 120deg, var(--c3) 0 180deg);
  background-size: var(--s) calc(var(--s) * 0.577);
}

.movingdots-preview-container {
  position: relative;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.movingdots-preview-hint {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-sm;
  width: 100%;
  height: 100%;
  justify-content: center;
  
  .preview-icon {
    font-size: 36px;
    color: #ffffff;
    filter: drop-shadow(0 0 10px rgba(255, 255, 255, 0.8));
  }
  
  .hint-text {
    color: #ffffff;
    font-size: 13px;
    font-weight: 600;
    text-shadow: 0 0 10px rgba(255, 255, 255, 0.8);
  }
}

.mini-movingdots {
  position: absolute;
  inset: 0;
  background: lightblue;
  overflow: hidden;
  
  &::before {
    content: "";
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: 
      radial-gradient(circle, #3498db 10%, transparent 20%),
      radial-gradient(circle, transparent 10%, #3498db 20%);
    background-size: 20px 20px;
    animation: miniMoveBackground 4s linear infinite;
  }
}

@keyframes miniMoveBackground {
  0% {
    transform: translate(0, 0);
  }
  100% {
    transform: translate(20%, 20%);
  }
}

.geometric-preview-container {
  position: relative;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.geometric-preview-hint {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-sm;
  width: 100%;
  height: 100%;
  justify-content: center;
  
  .preview-icon {
    font-size: 36px;
    color: #4b5563;
    filter: drop-shadow(0 0 10px rgba(75, 85, 99, 0.6));
  }
  
  .hint-text {
    color: #6b7280;
    font-size: 13px;
    font-weight: 600;
    text-shadow: 0 0 10px rgba(107, 114, 128, 0.8);
  }
}

.mini-geometric {
  position: absolute;
  inset: 0;
  --s: 50px;
  --c1: #f2f2f2;
  --c2: #cdcbcc;
  --c3: #999999;
  --_g: 0 120deg, #0000 0;
  background: 
    conic-gradient(from 0deg at calc(500% / 6) calc(100% / 3), var(--c3) var(--_g)),
    conic-gradient(from -120deg at calc(100% / 6) calc(100% / 3), var(--c2) var(--_g)),
    conic-gradient(from 120deg at calc(100% / 3) calc(500% / 6), var(--c1) var(--_g)),
    conic-gradient(from 120deg at calc(200% / 3) calc(500% / 6), var(--c1) var(--_g)),
    conic-gradient(from -180deg at calc(100% / 3) 50%, var(--c2) 60deg, var(--c1) var(--_g)),
    conic-gradient(from 60deg at calc(200% / 3) 50%, var(--c1) 60deg, var(--c3) var(--_g)),
    conic-gradient(from -60deg at 50% calc(100% / 3), var(--c1) 120deg, var(--c2) 0 240deg, var(--c3) 0);
  background-size: calc(var(--s) * 1.732) var(--s);
}

.purpledots-preview-container {
  position: relative;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.purpledots-preview-hint {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-sm;
  width: 100%;
  height: 100%;
  justify-content: center;
  
  .preview-icon {
    font-size: 36px;
    color: #ffffff;
    filter: drop-shadow(0 0 10px rgba(255, 255, 255, 0.8));
  }
  
  .hint-text {
    color: #ffffff;
    font-size: 13px;
    font-weight: 600;
    text-shadow: 0 0 10px rgba(255, 255, 255, 0.8);
  }
}

.mini-purpledots {
  position: absolute;
  inset: 0;
  --color: #c09bd8;
  background-color: #9f84bd;
  
  &::after {
    content: "";
    position: absolute;
    inset: 0;
    opacity: 0.8;
    background: 
      radial-gradient(circle, var(--color) 15%, transparent 15%),
      radial-gradient(circle, var(--color) 15%, transparent 15%) 4px -4px,
      radial-gradient(circle, var(--color) 15%, transparent 15%) 4px -1px,
      radial-gradient(circle, var(--color) 15%, transparent 15%) 7px 1px,
      radial-gradient(circle, var(--color) 15%, transparent 15%) 10px 3px,
      radial-gradient(circle, var(--color) 15%, transparent 15%) 13px 5px,
      radial-gradient(circle, var(--color) 15%, transparent 15%) 13px 8px,
      radial-gradient(circle, var(--color) 15%, transparent 15%) 16px 4px;
    background-size: 35px 35px;
  }
}

.skin-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-md $spacing-lg;
  background: var(--bg-card);
  border-top: 1px solid var(--border-color);
  transition: background 0.3s ease;
}

.skin-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.skin-check {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: $primary-color;
  color: #fff;
  font-size: 14px;
  opacity: 0;
  transform: scale(0);
  transition: all 0.3s ease;
  
  .active & {
    opacity: 1;
    transform: scale(1);
  }
  
  .el-icon {
    font-size: 12px;
  }
}
</style>
