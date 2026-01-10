<template>
  <header class="navbar">
    <div class="navbar-left">
      <el-icon class="collapse-btn" @click="settingsStore.toggleSidebar">
        <Fold v-if="!settingsStore.sidebarCollapsed" />
        <Expand v-else />
      </el-icon>
      <Breadcrumb />
    </div>
    <div class="navbar-right">
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

      <el-dropdown @command="handleCommand">
        <div class="user-info">
          <el-avatar :size="32" :src="userStore.userInfo?.avatar">
            {{ avatarText }}
          </el-avatar>
          <span class="username">{{ userStore.userInfo?.nickname || '管理员' }}</span>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon>个人中心
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <el-icon><SwitchButton /></el-icon>退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Fold, Expand, Sunny, Moon, User, SwitchButton } from '@element-plus/icons-vue'
import { useSettingsStore } from '@/stores/settings'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'
import Breadcrumb from '@/components/Breadcrumb/index.vue'

const router = useRouter()
const settingsStore = useSettingsStore()
const userStore = useUserStore()
const themeStore = useThemeStore()

const avatarText = computed(() => {
  const nickname = userStore.userInfo?.nickname
  return nickname ? nickname.charAt(0).toUpperCase() : 'A'
})

function toggleTheme(event) {
  themeStore.toggleTheme(event)
}

function handleCommand(command) {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.navbar {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 $spacing-lg;
  background: rgba(26, 26, 26, 0.75);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--border-color);
  transition: background-color 0.3s, border-color 0.3s;
}

:root[data-theme="light"] .navbar {
  background: rgba(255, 255, 255, 0.75);
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: var(--text-secondary);
  transition: color 0.2s;

  &:hover {
    color: var(--primary-color);
  }
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: $spacing-lg;
}

.theme-switch {
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
  
  &.is-light .switch-track .switch-thumb {
    left: calc(100% - 25px);
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  cursor: pointer;
}

.username {
  color: var(--text-secondary);
  transition: color 0.3s;
}
</style>
