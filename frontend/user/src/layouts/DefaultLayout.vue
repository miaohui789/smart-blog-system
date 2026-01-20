<template>
  <div class="default-layout">
    <!-- 动态背景组件 -->
    <Suspense v-if="currentBgComponent">
      <component :is="currentBgComponent" />
    </Suspense>
    
    <Header />
    <main class="main-content">
      <div class="container">
        <div class="content-wrapper">
          <div class="content-main">
            <router-view v-slot="{ Component, route }">
              <transition name="page-fade" mode="out-in" appear>
                <keep-alive :include="cachedViews" :max="10">
                  <component :is="Component" :key="route.path" />
                </keep-alive>
              </transition>
            </router-view>
          </div>
          <aside class="content-sidebar">
            <Sidebar />
          </aside>
        </div>
      </div>
    </main>
    <Footer />
    
    <!-- 回到顶部按钮 -->
    <BackToTop />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, shallowRef, defineAsyncComponent } from 'vue'
import Header from '@/components/Header/index.vue'
import Footer from '@/components/Footer/index.vue'
import Sidebar from '@/components/Sidebar/index.vue'
import BackToTop from '@/components/BackToTop/index.vue'
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()
const cachedViews = ref(['Home', 'Category', 'Tag', 'Archive', 'About'])
const currentBgComponent = shallowRef(null)

// 背景组件映射 - 使用异步组件
const bgComponents = {
  MatrixRain: defineAsyncComponent(() => import('@/components/Background/MatrixRain.vue')),
  ColorGrid: defineAsyncComponent(() => import('@/components/Background/ColorGrid.vue')),
  HexagonPattern: defineAsyncComponent(() => import('@/components/Background/HexagonPattern.vue')),
  MovingDots: defineAsyncComponent(() => import('@/components/Background/MovingDots.vue')),
  GeometricPattern: defineAsyncComponent(() => import('@/components/Background/GeometricPattern.vue')),
  PurpleDots: defineAsyncComponent(() => import('@/components/Background/PurpleDots.vue'))
}

// 加载当前皮肤
function loadCurrentSkin() {
  const theme = themeStore.isDark ? 'dark' : 'light'
  const skinData = localStorage.getItem(`${theme}Skin`)
  
  console.log('加载皮肤 - 主题:', theme, '数据:', skinData)
  
  if (skinData) {
    try {
      const data = JSON.parse(skinData)
      console.log('解析皮肤数据:', data)
      if (data.component && bgComponents[data.component]) {
        console.log('设置背景组件:', data.component)
        currentBgComponent.value = bgComponents[data.component]
      } else {
        console.log('无背景组件')
        currentBgComponent.value = null
      }
    } catch (e) {
      console.error('解析皮肤数据失败:', e)
      currentBgComponent.value = null
    }
  } else {
    console.log('无皮肤数据')
    currentBgComponent.value = null
  }
}

// 监听皮肤切换事件
function handleSkinChange(event) {
  console.log('收到皮肤切换事件:', event.detail)
  const { theme, component } = event.detail
  const currentTheme = themeStore.isDark ? 'dark' : 'light'
  
  console.log('当前主题:', currentTheme, '切换主题:', theme)
  
  if (theme === currentTheme) {
    if (component && bgComponents[component]) {
      console.log('加载背景组件:', component, bgComponents[component])
      currentBgComponent.value = bgComponents[component]
      console.log('currentBgComponent.value:', currentBgComponent.value)
    } else {
      console.log('清除背景组件')
      currentBgComponent.value = null
    }
    // 更新透明度状态
    themeStore.checkCustomBackground()
  }
}

// 监听主题切换
function handleThemeChange() {
  loadCurrentSkin()
}

onMounted(() => {
  loadCurrentSkin()
  window.addEventListener('skin-change', handleSkinChange)
  window.addEventListener('theme-changed', handleThemeChange)
})

onUnmounted(() => {
  window.removeEventListener('skin-change', handleSkinChange)
  window.removeEventListener('theme-changed', handleThemeChange)
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.default-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
  background: var(--bg-dark);
  transition: background-color 0.3s;
  z-index: 1;
}

.main-content {
  flex: 1;
  padding: $spacing-xl 0;
  position: relative;
  z-index: 2;
}

.content-wrapper {
  display: flex;
  gap: $spacing-xl;
}

.content-main {
  flex: 1;
  min-width: 0;
}

.content-sidebar {
  width: 320px;
  flex-shrink: 0;

  @media (max-width: 992px) {
    display: none;
  }
}
</style>
