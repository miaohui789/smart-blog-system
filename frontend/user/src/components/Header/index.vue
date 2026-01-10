<template>
  <header class="header">
    <div class="header-container">
      <!-- 左侧：Logo + 搜索框 -->
      <div class="header-left">
        <router-link to="/" class="logo">
          <img v-if="configStore.siteLogo" :src="configStore.siteLogo" alt="logo" class="logo-img" />
          <span v-else class="logo-icon">✦</span>
          <span class="logo-text">{{ configStore.siteName }}</span>
        </router-link>
        
        <div class="search-box" @click="openSearch">
          <el-icon><Search /></el-icon>
          <span>搜索</span>
          <kbd>Ctrl K</kbd>
        </div>
      </div>

      <!-- 中间：导航菜单 -->
      <nav class="nav-tabs">
        <input type="radio" id="nav-home" name="nav-tabs" :checked="currentNav === '/'" @change="navigateTo('/')">
        <label class="nav-tab" for="nav-home">首页</label>
        <input type="radio" id="nav-category" name="nav-tabs" :checked="currentNav === '/category'" @change="navigateTo('/category')">
        <label class="nav-tab" for="nav-category">分类</label>
        <input type="radio" id="nav-tag" name="nav-tabs" :checked="currentNav === '/tag'" @change="navigateTo('/tag')">
        <label class="nav-tab" for="nav-tag">标签</label>
        <input type="radio" id="nav-archive" name="nav-tabs" :checked="currentNav === '/archive'" @change="navigateTo('/archive')">
        <label class="nav-tab" for="nav-archive">归档</label>
        <input type="radio" id="nav-about" name="nav-tabs" :checked="currentNav === '/about'" @change="navigateTo('/about')">
        <label class="nav-tab" for="nav-about">关于</label>
        <span class="nav-glider" :style="gliderStyle"></span>
      </nav>

      <!-- 右侧：功能按钮 -->
      <div class="header-right">
        <!-- 主题切换开关 -->
        <label class="theme-switch" @click.prevent="toggleTheme">
          <input 
            type="checkbox" 
            class="theme-switch__checkbox"
            :checked="themeStore.isDark"
          >
          <div class="theme-switch__container">
            <div class="theme-switch__clouds"></div>
            <div class="theme-switch__stars-container">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 144 55" fill="none">
                <path fill-rule="evenodd" clip-rule="evenodd" d="M135.831 3.00688C135.055 3.85027 134.111 4.29946 133 4.35447C134.111 4.40947 135.055 4.85867 135.831 5.71123C136.607 6.55462 136.996 7.56303 136.996 8.72727C136.996 7.95722 137.172 7.25134 137.525 6.59129C137.886 5.93124 138.372 5.39954 138.98 5.00535C139.598 4.60199 140.268 4.39114 141 4.35447C139.88 4.2903 138.936 3.85027 138.16 3.00688C137.384 2.16348 136.996 1.16425 136.996 0C136.996 1.16425 136.607 2.16348 135.831 3.00688ZM31 23.3545C32.1114 23.2995 33.0551 22.8503 33.8313 22.0069C34.6075 21.1635 34.9956 20.1642 34.9956 19C34.9956 20.1642 35.3837 21.1635 36.1599 22.0069C36.9361 22.8503 37.8798 23.2903 39 23.3545C38.2679 23.3911 37.5976 23.602 36.9802 24.0053C36.3716 24.3995 35.8864 24.9312 35.5248 25.5913C35.172 26.2513 34.9956 26.9572 34.9956 27.7273C34.9956 26.563 34.6075 25.5546 33.8313 24.7112C33.0551 23.8587 32.1114 23.4095 31 23.3545ZM0 36.3545C1.11136 36.2995 2.05513 35.8503 2.83131 35.0069C3.6075 34.1635 3.99559 33.1642 3.99559 32C3.99559 33.1642 4.38368 34.1635 5.15987 35.0069C5.93605 35.8503 6.87982 36.2903 8 36.3545C7.26792 36.3911 6.59757 36.602 5.98015 37.0053C5.37155 37.3995 4.88644 37.9312 4.52481 38.5913C4.172 39.2513 3.99559 39.9572 3.99559 40.7273C3.99559 39.563 3.6075 38.5546 2.83131 37.7112C2.05513 36.8587 1.11136 36.4095 0 36.3545ZM56.8313 24.0069C56.0551 24.8503 55.1114 25.2995 54 25.3545C55.1114 25.4095 56.0551 25.8587 56.8313 26.7112C57.6075 27.5546 57.9956 28.563 57.9956 29.7273C57.9956 28.9572 58.172 28.2513 58.5248 27.5913C58.8864 26.9312 59.3716 26.3995 59.9802 26.0053C60.5976 25.602 61.2679 25.3911 62 25.3545C60.8798 25.2903 59.9361 24.8503 59.1599 24.0069C58.3837 23.1635 57.9956 22.1642 57.9956 21C57.9956 22.1642 57.6075 23.1635 56.8313 24.0069ZM81 25.3545C82.1114 25.2995 83.0551 24.8503 83.8313 24.0069C84.6075 23.1635 84.9956 22.1642 84.9956 21C84.9956 22.1642 85.3837 23.1635 86.1599 24.0069C86.9361 24.8503 87.8798 25.2903 89 25.3545C88.2679 25.3911 87.5976 25.602 86.9802 26.0053C86.3716 26.3995 85.8864 26.9312 85.5248 27.5913C85.172 28.2513 84.9956 28.9572 84.9956 29.7273C84.9956 28.563 84.6075 27.5546 83.8313 26.7112C83.0551 25.8587 82.1114 25.4095 81 25.3545ZM136 36.3545C137.111 36.2995 138.055 35.8503 138.831 35.0069C139.607 34.1635 139.996 33.1642 139.996 32C139.996 33.1642 140.384 34.1635 141.16 35.0069C141.936 35.8503 142.88 36.2903 144 36.3545C143.268 36.3911 142.598 36.602 141.98 37.0053C141.372 37.3995 140.886 37.9312 140.525 38.5913C140.172 39.2513 139.996 39.9572 139.996 40.7273C139.996 39.563 139.607 38.5546 138.831 37.7112C138.055 36.8587 137.111 36.4095 136 36.3545ZM101.831 49.0069C101.055 49.8503 100.111 50.2995 99 50.3545C100.111 50.4095 101.055 50.8587 101.831 51.7112C102.607 52.5546 102.996 53.563 102.996 54.7273C102.996 53.9572 103.172 53.2513 103.525 52.5913C103.886 51.9312 104.372 51.3995 104.98 51.0053C105.598 50.602 106.268 50.3911 107 50.3545C105.88 50.2903 104.936 49.8503 104.16 49.0069C103.384 48.1635 102.996 47.1642 102.996 46C102.996 47.1642 102.607 48.1635 101.831 49.0069Z" fill="currentColor"></path>
              </svg>
            </div>
            <div class="theme-switch__circle-container">
              <div class="theme-switch__sun-moon-container">
                <div class="theme-switch__moon">
                  <div class="theme-switch__spot"></div>
                  <div class="theme-switch__spot"></div>
                  <div class="theme-switch__spot"></div>
                </div>
              </div>
            </div>
          </div>
        </label>

        <!-- 外部链接 -->
        <a href="https://github.com/miaohui789" target="_blank" class="external-link" title="GitHub">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
            <path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0024 12c0-6.63-5.37-12-12-12z"/>
          </svg>
        </a>
        <a href="https://space.bilibili.com/1589988727" target="_blank" class="external-link" title="Bilibili">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
            <path d="M17.813 4.653h.854c1.51.054 2.769.578 3.773 1.574 1.004.995 1.524 2.249 1.56 3.76v7.36c-.036 1.51-.556 2.769-1.56 3.773s-2.262 1.524-3.773 1.56H5.333c-1.51-.036-2.769-.556-3.773-1.56S.036 18.858 0 17.347v-7.36c.036-1.511.556-2.765 1.56-3.76 1.004-.996 2.262-1.52 3.773-1.574h.774l-1.174-1.12a1.234 1.234 0 0 1-.373-.906c0-.356.124-.659.373-.907l.027-.027c.267-.249.573-.373.92-.373.347 0 .653.124.92.373L9.653 4.44c.071.071.134.142.187.213h4.267a.836.836 0 0 1 .16-.213l2.853-2.747c.267-.249.573-.373.92-.373.347 0 .662.151.929.4.267.249.391.551.391.907 0 .355-.124.657-.373.906zM5.333 7.24c-.746.018-1.373.276-1.88.773-.506.498-.769 1.13-.786 1.894v7.52c.017.764.28 1.395.786 1.893.507.498 1.134.756 1.88.773h13.334c.746-.017 1.373-.275 1.88-.773.506-.498.769-1.129.786-1.893v-7.52c-.017-.765-.28-1.396-.786-1.894-.507-.497-1.134-.755-1.88-.773zM8 11.107c.373 0 .684.124.933.373.25.249.383.569.4.96v1.173c-.017.391-.15.711-.4.96-.249.25-.56.374-.933.374s-.684-.125-.933-.374c-.25-.249-.383-.569-.4-.96V12.44c0-.373.129-.689.386-.947.258-.257.574-.386.947-.386zm8 0c.373 0 .684.124.933.373.25.249.383.569.4.96v1.173c-.017.391-.15.711-.4.96-.249.25-.56.374-.933.374s-.684-.125-.933-.374c-.25-.249-.383-.569-.4-.96V12.44c.017-.391.15-.711.4-.96.249-.249.56-.373.933-.373z"/>
          </svg>
        </a>
        
        <template v-if="userStore.isLoggedIn">
          <router-link to="/write" class="write-btn">
            <div class="svg-wrapper-1">
              <div class="svg-wrapper">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="20" height="20">
                  <path fill="none" d="M0 0h24v24H0z"></path>
                  <path fill="currentColor" d="M1.946 9.315c-.522-.174-.527-.455.01-.634l19.087-6.362c.529-.176.832.12.684.638l-5.454 19.086c-.15.529-.455.547-.679.045L12 14l6-8-8 6-8.054-2.685z"></path>
                </svg>
              </div>
            </div>
            <span>写文章</span>
          </router-link>
          <el-dropdown @command="handleCommand" trigger="click">
            <div class="user-info">
              <div class="user-avatar">
                <img 
                  v-if="userStore.userInfo?.avatar" 
                  :src="userStore.userInfo.avatar" 
                  @error="onAvatarError" 
                />
                <span v-else>{{ avatarText }}</span>
              </div>
              <span class="user-greeting">{{ userStore.userInfo?.nickname || '用户' }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="articles">
                  <el-icon><Document /></el-icon>我的文章
                </el-dropdown-item>
                <el-dropdown-item command="favorites">
                  <el-icon><Star /></el-icon>我的收藏
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>设置
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <router-link to="/login" class="login-btn">登录</router-link>
        </template>
      </div>
    </div>

    <!-- 搜索弹窗 -->
    <Teleport to="body">
      <Transition name="search-fade">
        <div v-if="showSearch" class="search-overlay" @click.self="closeSearch">
          <div class="search-modal">
            <div class="search-input-wrapper">
              <el-icon class="search-icon"><Search /></el-icon>
              <input
                ref="searchInputRef"
                v-model="keyword"
                type="text"
                class="search-input"
                placeholder="搜索文章..."
                @keyup.enter="doSearch"
                @keyup.esc="closeSearch"
              />
              <kbd v-if="!keyword" class="esc-hint">ESC</kbd>
            </div>
            <div v-if="searchResults.length" class="search-results">
              <router-link
                v-for="item in searchResults"
                :key="item.id"
                :to="`/article/${item.id}`"
                class="search-result-item"
                @click="closeSearch"
              >
                <h4>{{ item.title }}</h4>
                <p>{{ item.summary }}</p>
              </router-link>
            </div>
            <div v-else-if="keyword && !searching" class="search-empty">
              未找到相关文章
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </header>
</template>

<script setup>
import { ref, computed, nextTick, watch, onMounted, onUnmounted } from 'vue'
import { Search, User, Star, Setting, SwitchButton, Document } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'
import { useConfigStore } from '@/stores/config'
import { searchArticles } from '@/api/article'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const themeStore = useThemeStore()
const configStore = useConfigStore()

const showSearch = ref(false)
const keyword = ref('')
const searchResults = ref([])
const searching = ref(false)
const searchInputRef = ref(null)
const avatarLoadError = ref(false)

// 导航相关
const navItems = ['/', '/category', '/tag', '/archive', '/about']
const currentNav = computed(() => {
  const path = route.path
  if (path === '/') return '/'
  return navItems.find(item => item !== '/' && path.startsWith(item)) || '/'
})

const gliderStyle = computed(() => {
  const index = navItems.indexOf(currentNav.value)
  return { transform: `translateX(${index * 100}%)` }
})

function navigateTo(path) {
  router.push(path)
}

const avatarText = computed(() => {
  const nickname = userStore.userInfo?.nickname
  return nickname ? nickname.charAt(0).toUpperCase() : 'U'
})

function toggleTheme(event) {
  themeStore.toggleTheme(event)
}

function onAvatarError() {
  avatarLoadError.value = true
}

watch(() => userStore.userInfo?.avatar, () => {
  avatarLoadError.value = false
})

function openSearch() {
  showSearch.value = true
  keyword.value = ''
  searchResults.value = []
  nextTick(() => searchInputRef.value?.focus())
}

function closeSearch() {
  showSearch.value = false
}

async function doSearch() {
  if (!keyword.value.trim()) return
  searching.value = true
  try {
    const res = await searchArticles({ keyword: keyword.value, pageSize: 5 })
    searchResults.value = res.data?.list || []
  } catch (e) {
    console.error(e)
  } finally {
    searching.value = false
  }
}

let searchTimer = null
watch(keyword, (val) => {
  clearTimeout(searchTimer)
  if (val.trim()) {
    searchTimer = setTimeout(doSearch, 300)
  } else {
    searchResults.value = []
  }
})

// 快捷键 Ctrl+K 打开搜索
function handleKeydown(e) {
  if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
    e.preventDefault()
    openSearch()
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})

function handleCommand(command) {
  switch (command) {
    case 'profile':
      router.push('/user/profile')
      break
    case 'articles':
      router.push('/user/articles')
      break
    case 'favorites':
      router.push('/user/favorites')
      break
    case 'settings':
      router.push('/user/settings')
      break
    case 'logout':
      userStore.logout()
      router.push('/')
      break
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(24, 24, 27, 0.85);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-bottom: 1px solid var(--border-color);
  transition: all 0.3s ease;
}

:root[data-theme="light"] .header {
  background: rgba(255, 255, 255, 0.85);
}

.header-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 24px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  
  .logo-img {
    width: 28px;
    height: 28px;
    object-fit: contain;
  }
  
  .logo-icon {
    font-size: 22px;
    color: $primary-color;
  }
  
  .logo-text {
    font-size: 18px;
    font-weight: 700;
    color: var(--text-primary);
  }
}

.search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  
  .el-icon {
    font-size: 14px;
    color: var(--text-muted);
  }
  
  span {
    font-size: 13px;
    color: var(--text-muted);
  }
  
  kbd {
    font-size: 11px;
    color: var(--text-disabled);
    background: var(--bg-card-hover);
    padding: 2px 6px;
    border-radius: 4px;
    margin-left: 16px;
  }
  
  &:hover {
    border-color: $primary-color;
    background: rgba($primary-color, 0.05);
  }
  
  @media (max-width: 768px) {
    span, kbd { display: none; }
    padding: 8px;
  }
}

/* 新导航样式 */
.nav-tabs {
  display: flex;
  position: relative;
  background: var(--bg-card);
  box-shadow: 0 0 1px 0 rgba(168, 85, 247, 0.15), 0 4px 12px 0 rgba(168, 85, 247, 0.1);
  padding: 6px;
  border-radius: 99px;
  
  input[type="radio"] {
    display: none;
  }
  
  @media (max-width: 900px) {
    display: none;
  }
}

.nav-tab {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 32px;
  width: 56px;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  border-radius: 99px;
  cursor: pointer;
  transition: color 0.2s ease;
  z-index: 2;
  
  &:hover {
    color: var(--text-primary);
  }
}

.nav-tabs input[type="radio"]:checked + .nav-tab {
  color: $primary-color;
}

.nav-glider {
  position: absolute;
  display: flex;
  height: 32px;
  width: 56px;
  background: rgba($primary-color, 0.15);
  z-index: 1;
  border-radius: 99px;
  transition: transform 0.25s ease-out;
  left: 6px;
}

:root[data-theme="light"] {
  .nav-tabs {
    background: #fff;
    box-shadow: 0 0 1px 0 rgba(24, 94, 224, 0.15), 0 6px 12px 0 rgba(24, 94, 224, 0.15);
  }
  
  .nav-glider {
    background: #e6eef9;
  }
  
  .nav-tabs input[type="radio"]:checked + .nav-tab {
    color: #185ee0;
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

/* 主题切换按钮 - 太阳月亮动画 */
.theme-switch {
  /* 将按钮从 View Transition 中排除，保持自身动画 */
  view-transition-name: theme-switch;
  
  --toggle-size: 10px;
  --container-width: 5.625em;
  --container-height: 2.5em;
  --container-radius: 6.25em;
  --container-light-bg: #3D7EAE;
  --container-night-bg: #1D1F2C;
  --circle-container-diameter: 3.375em;
  --sun-moon-diameter: 2.125em;
  --sun-bg: #ECCA2F;
  --moon-bg: #C4C9D1;
  --spot-color: #959DB1;
  --circle-container-offset: calc((var(--circle-container-diameter) - var(--container-height)) / 2 * -1);
  --stars-color: #fff;
  --clouds-color: #F3FDFF;
  --back-clouds-color: #AACADF;
  --transition: .5s cubic-bezier(0, -0.02, 0.4, 1.25);
  --circle-transition: .3s cubic-bezier(0, -0.02, 0.35, 1.17);
  
  &, *, *::before, *::after {
    box-sizing: border-box;
    margin: 0;
    padding: 0;
    font-size: var(--toggle-size);
  }
  
  .theme-switch__checkbox {
    display: none;
  }
  
  .theme-switch__container {
    width: var(--container-width);
    height: var(--container-height);
    background-color: var(--container-light-bg);
    border-radius: var(--container-radius);
    overflow: hidden;
    cursor: pointer;
    -webkit-box-shadow: 0em -0.062em 0.062em rgba(0, 0, 0, 0.25), 0em 0.062em 0.125em rgba(255, 255, 255, 0.94);
    box-shadow: 0em -0.062em 0.062em rgba(0, 0, 0, 0.25), 0em 0.062em 0.125em rgba(255, 255, 255, 0.94);
    -webkit-transition: var(--transition);
    -o-transition: var(--transition);
    transition: var(--transition);
    position: relative;
    
    &::before {
      content: "";
      position: absolute;
      z-index: 1;
      inset: 0;
      -webkit-box-shadow: 0em 0.05em 0.187em rgba(0, 0, 0, 0.25) inset;
      box-shadow: 0em 0.05em 0.187em rgba(0, 0, 0, 0.25) inset;
      border-radius: var(--container-radius);
    }
  }
  
  .theme-switch__circle-container {
    width: var(--circle-container-diameter);
    height: var(--circle-container-diameter);
    background-color: rgba(255, 255, 255, 0.1);
    position: absolute;
    left: var(--circle-container-offset);
    top: var(--circle-container-offset);
    border-radius: var(--container-radius);
    -webkit-box-shadow: inset 0 0 0 3.375em rgba(255, 255, 255, 0.1), 0 0 0 0.625em rgba(255, 255, 255, 0.1), 0 0 0 1.25em rgba(255, 255, 255, 0.1);
    box-shadow: inset 0 0 0 3.375em rgba(255, 255, 255, 0.1), 0 0 0 0.625em rgba(255, 255, 255, 0.1), 0 0 0 1.25em rgba(255, 255, 255, 0.1);
    display: -webkit-box;
    display: -ms-flexbox;
    display: flex;
    -webkit-transition: var(--circle-transition);
    -o-transition: var(--circle-transition);
    transition: var(--circle-transition);
    pointer-events: none;
  }
  
  .theme-switch__sun-moon-container {
    pointer-events: auto;
    position: relative;
    z-index: 2;
    width: var(--sun-moon-diameter);
    height: var(--sun-moon-diameter);
    margin: auto;
    border-radius: var(--container-radius);
    background-color: var(--sun-bg);
    -webkit-box-shadow: 0.062em 0.062em 0.062em 0em rgba(254, 255, 239, 0.61) inset, 0em -0.062em 0.062em 0em #a1872a inset;
    box-shadow: 0.062em 0.062em 0.062em 0em rgba(254, 255, 239, 0.61) inset, 0em -0.062em 0.062em 0em #a1872a inset;
    -webkit-filter: drop-shadow(0.062em 0.125em 0.125em rgba(0, 0, 0, 0.25)) drop-shadow(0em 0.062em 0.125em rgba(0, 0, 0, 0.25));
    filter: drop-shadow(0.062em 0.125em 0.125em rgba(0, 0, 0, 0.25)) drop-shadow(0em 0.062em 0.125em rgba(0, 0, 0, 0.25));
    overflow: hidden;
    -webkit-transition: var(--transition);
    -o-transition: var(--transition);
    transition: var(--transition);
  }
  
  .theme-switch__moon {
    -webkit-transform: translateX(100%);
    -ms-transform: translateX(100%);
    transform: translateX(100%);
    width: 100%;
    height: 100%;
    background-color: var(--moon-bg);
    border-radius: inherit;
    box-shadow: 0.062em 0.062em 0.062em 0em rgba(254, 255, 239, 0.61) inset, 0em -0.062em 0.062em 0em #969696 inset;
    -webkit-transition: var(--transition);
    -o-transition: var(--transition);
    transition: var(--transition);
    position: relative;
  }
  
  .theme-switch__spot {
    position: absolute;
    top: 0.75em;
    left: 0.312em;
    width: 0.75em;
    height: 0.75em;
    border-radius: var(--container-radius);
    background-color: var(--spot-color);
    box-shadow: 0em 0.0312em 0.062em rgba(0, 0, 0, 0.25) inset;
    
    &:nth-of-type(2) {
      width: 0.375em;
      height: 0.375em;
      top: 0.937em;
      left: 1.375em;
    }
    
    &:nth-last-of-type(3) {
      width: 0.25em;
      height: 0.25em;
      top: 0.312em;
      left: 0.812em;
    }
  }
  
  .theme-switch__clouds {
    width: 1.25em;
    height: 1.25em;
    background-color: var(--clouds-color);
    border-radius: var(--container-radius);
    position: absolute;
    bottom: -0.625em;
    left: 0.312em;
    box-shadow: 0.937em 0.312em var(--clouds-color), -0.312em -0.312em var(--back-clouds-color), 1.437em 0.375em var(--clouds-color), 0.5em -0.125em var(--back-clouds-color), 2.187em 0 var(--clouds-color), 1.25em -0.062em var(--back-clouds-color), 2.937em 0.312em var(--clouds-color), 2em -0.312em var(--back-clouds-color), 3.625em -0.062em var(--clouds-color), 2.625em 0em var(--back-clouds-color), 4.5em -0.312em var(--clouds-color), 3.375em -0.437em var(--back-clouds-color), 4.625em -1.75em 0 0.437em var(--clouds-color), 4em -0.625em var(--back-clouds-color), 4.125em -2.125em 0 0.437em var(--back-clouds-color);
    -webkit-transition: 0.5s cubic-bezier(0, -0.02, 0.4, 1.25);
    -o-transition: 0.5s cubic-bezier(0, -0.02, 0.4, 1.25);
    transition: 0.5s cubic-bezier(0, -0.02, 0.4, 1.25);
  }
  
  .theme-switch__stars-container {
    position: absolute;
    color: var(--stars-color);
    top: -100%;
    left: 0.312em;
    width: 2.75em;
    height: auto;
    -webkit-transition: var(--transition);
    -o-transition: var(--transition);
    transition: var(--transition);
    -webkit-transform: translateY(0);
    -ms-transform: translateY(0);
    transform: translateY(0);
  }
  
  /* 暗色模式状态 */
  .theme-switch__checkbox:checked + .theme-switch__container {
    background-color: var(--container-night-bg);
  }
  
  .theme-switch__checkbox:checked + .theme-switch__container .theme-switch__circle-container {
    left: calc(100% - var(--circle-container-offset) - var(--circle-container-diameter));
  }
  
  .theme-switch__checkbox:checked + .theme-switch__container .theme-switch__circle-container:hover {
    left: calc(100% - var(--circle-container-offset) - var(--circle-container-diameter) - 0.187em);
  }
  
  .theme-switch__circle-container:hover {
    left: calc(var(--circle-container-offset) + 0.187em);
  }
  
  .theme-switch__checkbox:checked + .theme-switch__container .theme-switch__moon {
    -webkit-transform: translate(0);
    -ms-transform: translate(0);
    transform: translate(0);
  }
  
  .theme-switch__checkbox:checked + .theme-switch__container .theme-switch__clouds {
    bottom: -4.062em;
  }
  
  .theme-switch__checkbox:checked + .theme-switch__container .theme-switch__stars-container {
    top: 50%;
    -webkit-transform: translateY(-50%);
    -ms-transform: translateY(-50%);
    transform: translateY(-50%);
  }
}

.theme-icon-enter-active,
.theme-icon-leave-active {
  transition: all 0.2s ease;
}

.theme-icon-enter-from {
  opacity: 0;
  transform: rotate(-90deg) scale(0.5);
}

.theme-icon-leave-to {
  opacity: 0;
  transform: rotate(90deg) scale(0.5);
}

.external-link {
  display: flex;
  align-items: center;
  color: var(--text-secondary);
  text-decoration: none;
  transition: all 0.2s;
  
  svg {
    transition: color 0.2s;
  }
  
  &:hover {
    color: var(--text-primary);
  }
}

.write-btn {
  display: flex;
  align-items: center;
  padding: 0.6em 1em;
  padding-left: 0.8em;
  background: $primary-color;
  border-radius: 12px;
  color: white;
  font-size: 13px;
  font-weight: 500;
  text-decoration: none;
  overflow: hidden;
  transition: all 0.2s ease;
  cursor: pointer;
  
  .svg-wrapper-1 {
    display: flex;
    align-items: center;
  }
  
  .svg-wrapper {
    display: flex;
    align-items: center;
    transition: transform 0.3s ease-in-out;
  }
  
  svg {
    display: block;
    transform-origin: center center;
    transition: transform 0.3s ease-in-out;
  }
  
  span {
    display: block;
    margin-left: 0.3em;
    transition: all 0.3s ease-in-out;
  }
  
  &:hover {
    background: $primary-dark;
    box-shadow: 0 4px 12px rgba($primary-color, 0.3);
    
    .svg-wrapper {
      animation: fly-1 0.6s ease-in-out infinite alternate;
    }
    
    svg {
      transform: translateX(1.2em) rotate(45deg) scale(1.1);
    }
    
    span {
      transform: translateX(4em);
    }
  }
  
  &:active {
    transform: scale(0.95);
  }
  
  @media (max-width: 768px) {
    span { display: none; }
    padding: 0.6em;
    
    &:hover {
      svg {
        transform: rotate(45deg) scale(1.1);
      }
    }
  }
}

@keyframes fly-1 {
  from {
    transform: translateY(0.1em);
  }
  to {
    transform: translateY(-0.1em);
  }
}

.login-btn {
  padding: 8px 20px;
  background: transparent;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.2s ease;
  
  &:hover {
    border-color: $primary-color;
    color: $primary-color;
    background: rgba($primary-color, 0.05);
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.user-greeting {
  font-size: 13px;
  color: var(--text-secondary);
  white-space: nowrap;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $primary-color;
  transition: all 0.2s;
  flex-shrink: 0;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  span {
    font-size: 13px;
    font-weight: 600;
    color: #fff;
  }
}

.user-info:hover .user-avatar {
  transform: scale(1.05);
}

/* 搜索弹窗 */
.search-overlay {
  position: fixed;
  inset: 0;
  background: var(--search-overlay-bg);
  backdrop-filter: blur(4px);
  z-index: 1000;
  display: flex;
  justify-content: center;
  padding-top: 100px;
}

.search-modal {
  width: 100%;
  max-width: 560px;
  margin: 0 20px;
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: 10px;
  
  .search-icon {
    font-size: 18px;
    color: var(--text-muted);
  }
  
  .search-input {
    flex: 1;
    background: none;
    border: none;
    outline: none;
    font-size: 15px;
    color: var(--text-secondary);
    
    &::placeholder {
      color: var(--text-disabled);
    }
  }
  
  .esc-hint {
    font-size: 11px;
    color: var(--text-disabled);
    background: var(--bg-card-hover);
    padding: 3px 8px;
    border-radius: 4px;
  }
}

.search-results {
  margin-top: 8px;
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: 10px;
  overflow: hidden;
}

.search-result-item {
  display: block;
  padding: 14px 18px;
  text-decoration: none;
  border-bottom: 1px solid var(--border-color);
  transition: background 0.15s;
  
  &:last-child {
    border-bottom: none;
  }
  
  &:hover {
    background: var(--bg-card-hover);
  }
  
  h4 {
    font-size: 14px;
    font-weight: 500;
    color: var(--text-secondary);
    margin-bottom: 4px;
  }
  
  p {
    font-size: 12px;
    color: var(--text-muted);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.search-empty {
  margin-top: 8px;
  padding: 32px;
  text-align: center;
  color: var(--text-disabled);
  font-size: 14px;
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: 10px;
}

.search-fade-enter-active,
.search-fade-leave-active {
  transition: opacity 0.15s ease;
}

.search-fade-enter-from,
.search-fade-leave-to {
  opacity: 0;
}
</style>
