<template>
  <header class="header">
    <div class="header-container">
      <!-- 左侧：Logo + 搜索框 -->
      <div class="header-left">
        <router-link to="/" class="logo">
          <span class="logo-icon">✦</span>
          <span class="logo-text">My Blog</span>
        </router-link>
        
        <div class="search-box" @click="openSearch">
          <el-icon><Search /></el-icon>
          <span>搜索</span>
          <kbd>Ctrl K</kbd>
        </div>
      </div>

      <!-- 中间：导航菜单 -->
      <nav class="nav-menu">
        <router-link to="/" class="nav-item">首页</router-link>
        <router-link to="/category" class="nav-item">分类</router-link>
        <router-link to="/tag" class="nav-item">标签</router-link>
        <router-link to="/archive" class="nav-item">归档</router-link>
        <router-link to="/about" class="nav-item">关于</router-link>
      </nav>

      <!-- 右侧：功能按钮 -->
      <div class="header-right">
        <!-- 主题切换开关 -->
        <div 
          class="theme-switch" 
          :class="{ 'is-light': !themeStore.isDark }"
          @click="toggleTheme" 
          :title="themeStore.isDark ? '切换到亮色模式' : '切换到暗色模式'"
        >
          <div class="switch-track">
            <el-icon class="icon-sun"><Sunny /></el-icon>
            <el-icon class="icon-moon"><Moon /></el-icon>
            <div class="switch-thumb"></div>
          </div>
        </div>

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
            <el-icon><Edit /></el-icon>
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
import { Search, User, Star, Setting, SwitchButton, Edit, Document, Sunny, Moon } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'
import { searchArticles } from '@/api/article'

const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()

const showSearch = ref(false)
const keyword = ref('')
const searchResults = ref([])
const searching = ref(false)
const searchInputRef = ref(null)
const avatarLoadError = ref(false)

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
.header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(24, 24, 27, 0.75);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--border-color);
  transition: background-color 0.3s, border-color 0.3s;
}

:root[data-theme="light"] .header {
  background: rgba(255, 255, 255, 0.75);
}

.header-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  height: 56px;
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
  
  .logo-icon {
    font-size: 20px;
    color: #ef4444;
  }
  
  .logo-text {
    font-size: 18px;
    font-weight: 700;
    color: var(--text-primary);
    transition: color 0.3s;
  }
}

.search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  
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
    background: var(--bg-card-hover);
    border-color: var(--border-light);
  }
  
  @media (max-width: 768px) {
    span, kbd { display: none; }
    padding: 8px;
  }
}

.nav-menu {
  display: flex;
  align-items: center;
  gap: 8px;
  
  @media (max-width: 900px) {
    display: none;
  }
}

.nav-item {
  position: relative;
  color: var(--text-secondary);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  padding: 6px 14px;
  transition: color 0.2s;
  
  &::after {
    content: '';
    position: absolute;
    bottom: -2px;
    left: 50%;
    transform: translateX(-50%);
    width: 0;
    height: 2px;
    background: var(--text-primary);
    border-radius: 1px;
    transition: width 0.2s;
  }
  
  &:hover {
    color: var(--text-primary);
  }
  
  &.router-link-active {
    color: var(--text-primary);
    
    &::after {
      width: calc(100% - 28px);
    }
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.theme-switch {
  position: relative;
  cursor: pointer;
  
  .switch-track {
    position: relative;
    width: 56px;
    height: 28px;
    background: var(--bg-card);
    border: 1px solid var(--border-color);
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 6px;
    transition: all 0.3s;
    
    .el-icon {
      font-size: 14px;
      z-index: 1;
      transition: color 0.3s;
    }
    
    .icon-sun {
      color: #f59e0b;
    }
    
    .icon-moon {
      color: #8b5cf6;
    }
    
    .switch-thumb {
      position: absolute;
      left: 3px;
      width: 22px;
      height: 22px;
      background: var(--text-secondary);
      border-radius: 50%;
      transition: all 0.3s ease;
    }
  }
  
  &:hover .switch-track {
    border-color: var(--border-light);
  }
  
  // 亮色模式时滑块在右边
  &.is-light {
    .switch-track {
      .switch-thumb {
        left: calc(100% - 25px);
      }
    }
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
  gap: 6px;
  padding: 8px 16px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.2s;
  
  .el-icon {
    font-size: 14px;
  }
  
  &:hover {
    background: var(--bg-card-hover);
    border-color: var(--border-light);
    color: var(--text-primary);
  }
  
  @media (max-width: 768px) {
    span { display: none; }
    padding: 8px;
  }
}

.login-btn {
  padding: 8px 20px;
  background: var(--bg-card);
  border-radius: 6px;
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.2s;
  
  &:hover {
    background: var(--bg-card-hover);
    color: var(--text-primary);
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
  background: linear-gradient(135deg, #3b82f6, #8b5cf6);
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
