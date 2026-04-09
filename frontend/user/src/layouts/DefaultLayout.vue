<template>
  <div class="default-layout">
    <!-- 动态背景组件 -->
    <Suspense v-if="currentBgComponent">
      <component :is="currentBgComponent" />
    </Suspense>
    
    <Header />
    <main class="main-content">
      <div class="container">
        <div
          class="content-wrapper"
          :class="{
            'is-study-layout': isStudyRoute,
            'is-study-sidebar-collapsed': isStudyRoute && studySidebarCollapsed
          }"
        >
          <div class="content-main">
            <router-view v-slot="{ Component, route }">
              <transition name="page-fade" mode="out-in" appear>
                <keep-alive :include="cachedViews" :max="10">
                  <component :is="Component" :key="route.path" />
                </keep-alive>
              </transition>
            </router-view>
          </div>
          <aside
            class="content-sidebar"
            :class="{
              'is-study-sidebar': isStudyRoute,
              'is-collapsed': isStudyRoute && studySidebarCollapsed
            }"
          >
            <Sidebar
              :study-mode="isStudyRoute"
              :collapsed="isStudyRoute && studySidebarCollapsed"
              @toggle="handleSidebarToggle"
            />
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
import { ref, onMounted, onUnmounted, shallowRef, defineAsyncComponent, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import Header from '@/components/Header/index.vue'
import Footer from '@/components/Footer/index.vue'
import Sidebar from '@/components/Sidebar/index.vue'
import BackToTop from '@/components/BackToTop/index.vue'
import { useThemeStore } from '@/stores/theme'
import { useUserStore } from '@/stores/user'

const themeStore = useThemeStore()
const userStore = useUserStore()
const router = useRouter()
const route = useRoute()
const cachedViews = ref(['Home', 'Category', 'Tag', 'Archive', 'About'])
const currentBgComponent = shallowRef(null)
const studySidebarCollapsed = ref(false)
const isStudyRoute = computed(() => route.path.startsWith('/study'))

// ========== 无操作自动退出 ==========
const INACTIVE_TIMEOUT = 30 * 60 * 1000 // 30 分钟
const ACTIVITY_EVENTS = ['mousemove', 'mousedown', 'keydown', 'scroll', 'touchstart', 'click']
let inactivityTimer = null

// 节流函数，避免滚动时频繁触发重置逻辑导致卡顿
function throttle(fn, delay) {
  let timer = null
  return function (...args) {
    if (!timer) {
      timer = setTimeout(() => {
        fn.apply(this, args)
        timer = null
      }, delay)
    }
  }
}

const resetInactivityTimer = throttle(() => {
  if (!userStore.isLoggedIn) return
  clearTimeout(inactivityTimer)
  inactivityTimer = setTimeout(handleInactivityLogout, INACTIVE_TIMEOUT)
}, 1000)

async function handleInactivityLogout() {
  if (!userStore.isLoggedIn) return
  try {
    await ElMessageBox.alert(
      '您已超过 30 分钟未操作，为保障账号安全，已自动退出登录。',
      '自动退出',
      { confirmButtonText: '重新登录', type: 'warning', showClose: false }
    )
  } catch (_) { /* 弹窗关闭 */ }
  await userStore.logout()
  router.push('/login')
}

function startInactivityWatch() {
  ACTIVITY_EVENTS.forEach(e => window.addEventListener(e, resetInactivityTimer, { passive: true }))
  resetInactivityTimer()
}

function stopInactivityWatch() {
  clearTimeout(inactivityTimer)
  ACTIVITY_EVENTS.forEach(e => window.removeEventListener(e, resetInactivityTimer))
}
// =====================================

// 背景组件映射 - 使用异步组件
const bgComponents = {
  MatrixRain: defineAsyncComponent(() => import('@/components/Background/MatrixRain.vue')),
  ColorGrid: defineAsyncComponent(() => import('@/components/Background/ColorGrid.vue')),
  HexagonPattern: defineAsyncComponent(() => import('@/components/Background/HexagonPattern.vue')),
  MovingDots: defineAsyncComponent(() => import('@/components/Background/MovingDots.vue')),
  GeometricPattern: defineAsyncComponent(() => import('@/components/Background/GeometricPattern.vue')),
  PurpleDots: defineAsyncComponent(() => import('@/components/Background/PurpleDots.vue')),
  StarSky: defineAsyncComponent(() => import('@/components/Background/StarSky.vue'))
}

// 加载当前皮肤
function loadCurrentSkin() {
  if (!userStore.isLoggedIn) {
    console.log('未登录，不加载自定义皮肤')
    currentBgComponent.value = null
    document.body.classList.remove('has-custom-bg')
    themeStore.hasCustomBackground = false
    return
  }
  
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

  themeStore.checkCustomBackground()
}

// 监听皮肤切换事件
function handleSkinChange(event) {
  // 只有登录用户才能切换皮肤
  if (!userStore.isLoggedIn) {
    console.log('未登录，忽略皮肤切换事件')
    return
  }
  
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

function handleSidebarToggle(nextValue) {
  studySidebarCollapsed.value = nextValue
}

onMounted(() => {
  loadCurrentSkin()
  window.addEventListener('skin-change', handleSkinChange)
  window.addEventListener('theme-changed', handleThemeChange)
  // 已登录则启动无操作计时器
  if (userStore.isLoggedIn) {
    startInactivityWatch()
  }
})

onUnmounted(() => {
  window.removeEventListener('skin-change', handleSkinChange)
  window.removeEventListener('theme-changed', handleThemeChange)
  stopInactivityWatch()
})

// 监听登录状态变化
watch(() => userStore.isLoggedIn, (isLoggedIn) => {
  if (!isLoggedIn) {
    console.log('用户退出登录，清除自定义背景')
    currentBgComponent.value = null
    document.body.classList.remove('has-custom-bg')
    themeStore.hasCustomBackground = false
    stopInactivityWatch()
  } else {
    console.log('用户登录，重新加载皮肤')
    loadCurrentSkin()
    startInactivityWatch()
  }
})

watch(
  () => route.path,
  (newPath, oldPath) => {
    const nowStudy = newPath.startsWith('/study')
    const wasStudy = oldPath?.startsWith('/study')
    if (nowStudy && !wasStudy) {
      studySidebarCollapsed.value = true
      return
    }
    if (!nowStudy) {
      studySidebarCollapsed.value = false
    }
  },
  { immediate: true }
)
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
  padding: calc(#{$spacing-xl} + 64px) 0 $spacing-xl;
  position: relative;
  z-index: 2;
}

.content-wrapper {
  display: flex;
  gap: $spacing-xl;
  align-items: flex-start;
  overflow-anchor: none;
}

.content-wrapper.is-study-layout {
  position: relative;
  gap: 0;
}

.content-main {
  flex: 1;
  min-width: 0;
  overflow-anchor: none;
}

.content-wrapper.is-study-layout .content-main {
  position: relative;
  width: 100%;
  padding-right: 344px;
}

.content-wrapper.is-study-layout.is-study-sidebar-collapsed .content-main {
  padding-right: 0;
}

.content-wrapper.is-study-layout .content-main::after {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  width: 344px;
  pointer-events: none;
  background: linear-gradient(270deg, rgba(var(--bg-card-rgb), 0.72), rgba(var(--bg-card-rgb), 0.18), transparent);
  transition: width 0.32s ease, opacity 0.22s ease;
}

.content-wrapper.is-study-layout.is-study-sidebar-collapsed .content-main::after {
  width: 0;
  opacity: 0;
}

.content-sidebar {
  width: 320px;
  flex-shrink: 0;
  position: sticky;
  top: 80px;
  align-self: flex-start;
  overflow: visible;
  overflow-anchor: none;
  transition: top 0.3s cubic-bezier(0.4, 0, 0.2, 1); /* 添加平滑过渡 */
  // 移除滚动条和高度限制，让内容自然显示
  // max-height: calc(100vh - 100px);
  // overflow-y: auto;

  @media (max-width: 992px) {
    display: none;
  }
}

.content-sidebar.is-study-sidebar {
  position: fixed;
  top: 112px;
  right: 0;
  right: max(16px, calc((100vw - 1200px) / 2 + 16px));
  transition: width 0.32s ease, opacity 0.24s ease;
  will-change: width, opacity;
}

.content-sidebar.is-study-sidebar.is-collapsed {
  width: 0;
  opacity: 1;
  overflow: visible;
}

@media (max-width: 992px) {
  .main-content {
    padding: calc(#{$spacing-lg} + 60px) 0 $spacing-lg;
  }

  .content-wrapper {
    gap: $spacing-lg;
  }

  .content-wrapper.is-study-layout .content-main {
    padding-right: 0;
  }

  .content-wrapper.is-study-layout .content-main::after {
    display: none;
  }
}

@media (max-width: 768px) {
  .main-content {
    padding: calc(16px + 56px) 0 16px;
  }

  .content-wrapper {
    gap: 16px;
  }
}

</style>
