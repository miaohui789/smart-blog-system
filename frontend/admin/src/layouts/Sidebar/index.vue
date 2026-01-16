<template>
  <aside class="sidebar" :class="{ collapsed: settingsStore.sidebarCollapsed }">
    <div class="logo">
      <span class="logo-icon">✦</span>
      <span v-if="!settingsStore.sidebarCollapsed" class="logo-text">博客管理</span>
    </div>
    <el-menu
      :default-active="$route.path"
      :collapse="settingsStore.sidebarCollapsed"
      :background-color="'transparent'"
      :text-color="'var(--text-secondary)'"
      :active-text-color="'var(--primary-color)'"
      router
      class="sidebar-menu"
    >
      <!-- 仪表盘 - 所有角色可见 -->
      <el-menu-item index="/dashboard">
        <el-icon><Odometer /></el-icon>
        <span>仪表盘</span>
      </el-menu-item>
      
      <!-- 文章管理 - 超级管理员和内容编辑可见 -->
      <el-sub-menu index="/article" v-if="canManageContent">
        <template #title>
          <el-icon><Document /></el-icon>
          <span>文章管理</span>
        </template>
        <el-menu-item index="/article/list">文章列表</el-menu-item>
        <el-menu-item index="/article/create">创建文章</el-menu-item>
      </el-sub-menu>
      
      <!-- 分类管理 - 超级管理员和内容编辑可见 -->
      <el-menu-item index="/category" v-if="canManageContent">
        <el-icon><Folder /></el-icon>
        <span>分类管理</span>
      </el-menu-item>
      
      <!-- 标签管理 - 超级管理员和内容编辑可见 -->
      <el-menu-item index="/tag" v-if="canManageContent">
        <el-icon><PriceTag /></el-icon>
        <span>标签管理</span>
      </el-menu-item>
      
      <!-- 评论管理 - 超级管理员和内容编辑可见 -->
      <el-menu-item index="/comment" v-if="canManageContent">
        <el-icon><ChatDotRound /></el-icon>
        <span>评论管理</span>
      </el-menu-item>
      
      <!-- 用户管理 - 仅超级管理员可见 -->
      <el-menu-item index="/user" v-if="isAdmin">
        <el-icon><User /></el-icon>
        <span>用户管理</span>
      </el-menu-item>
      
      <!-- 会员管理 - 仅超级管理员可见 -->
      <el-sub-menu index="/vip" v-if="isAdmin">
        <template #title>
          <el-icon><Medal /></el-icon>
          <span>会员管理</span>
        </template>
        <el-menu-item index="/vip/member">会员列表</el-menu-item>
        <el-menu-item index="/vip/key">密钥管理</el-menu-item>
        <el-menu-item index="/vip/heat">加热记录</el-menu-item>
        <el-menu-item index="/vip/statistics">VIP统计</el-menu-item>
      </el-sub-menu>
      
      <!-- 社交管理 - 仅超级管理员可见 -->
      <el-sub-menu index="/social" v-if="isAdmin">
        <template #title>
          <el-icon><Connection /></el-icon>
          <span>社交管理</span>
        </template>
        <el-menu-item index="/social/follow">关注管理</el-menu-item>
        <el-menu-item index="/social/message">私信管理</el-menu-item>
        <el-menu-item index="/social/notification">通知管理</el-menu-item>
      </el-sub-menu>
      
      <!-- AI配置 - 仅超级管理员可见 -->
      <el-menu-item index="/ai" v-if="isAdmin">
        <el-icon><MagicStick /></el-icon>
        <span>AI配置</span>
      </el-menu-item>
      
      <!-- 系统管理 - 仅超级管理员可见 -->
      <el-sub-menu index="/system" v-if="isAdmin">
        <template #title>
          <el-icon><Setting /></el-icon>
          <span>系统管理</span>
        </template>
        <el-menu-item index="/system/role">角色管理</el-menu-item>
        <el-menu-item index="/system/menu">菜单管理</el-menu-item>
        <el-menu-item index="/system/config">系统配置</el-menu-item>
        <el-menu-item index="/system/log">操作日志</el-menu-item>
      </el-sub-menu>
    </el-menu>
  </aside>
</template>

<script setup>
import { computed } from 'vue'
import { useSettingsStore } from '@/stores/settings'
import { useUserStore } from '@/stores/user'
import { Odometer, Document, Folder, PriceTag, ChatDotRound, User, Setting, Medal, MagicStick, Connection } from '@element-plus/icons-vue'

const settingsStore = useSettingsStore()
const userStore = useUserStore()

// 判断是否是超级管理员
const isAdmin = computed(() => {
  const roles = userStore.roles || []
  const permissions = userStore.permissions || []
  
  // 检查是否有全部权限
  if (permissions.includes('*:*:*')) {
    return true
  }
  
  // 检查角色
  return roles.some(role => 
    role === '超级管理员' || 
    role === 'admin' || 
    role === 'ADMIN' ||
    role === 'super_admin'
  )
})

// 判断是否可以管理内容（超级管理员或内容编辑）
const canManageContent = computed(() => {
  const roles = userStore.roles || []
  const permissions = userStore.permissions || []
  
  // 超级管理员有所有权限
  if (permissions.includes('*:*:*')) {
    return true
  }
  
  // 检查是否有内容管理权限
  if (permissions.some(p => p.startsWith('article:'))) {
    return true
  }
  
  // 检查角色
  return roles.some(role => 
    role === '超级管理员' || 
    role === 'admin' || 
    role === '内容编辑' ||
    role === 'editor'
  )
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.sidebar {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  width: $sidebar-width;
  background: var(--bg-card);
  border-right: 1px solid var(--border-color);
  transition: width 0.3s, background-color 0.3s, border-color 0.3s;
  z-index: 100;
  display: flex;
  flex-direction: column;

  &.collapsed {
    width: $sidebar-collapsed-width;
  }
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-bottom: 1px solid var(--border-color);
  transition: border-color 0.3s;
  flex-shrink: 0;
  
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

.sidebar-menu {
  flex: 1;
  border-right: none !important;
  overflow-y: auto;
  
  :deep(.el-menu-item),
  :deep(.el-sub-menu__title) {
    color: var(--text-secondary);
    transition: color 0.3s, background-color 0.3s;
    
    &:hover {
      background: var(--bg-card-hover);
    }
  }
  
  :deep(.el-menu-item.is-active) {
    background: rgba(168, 85, 247, 0.1);
    color: var(--primary-color);
  }
  
  :deep(.el-sub-menu .el-menu) {
    background: transparent !important;
  }
  
  :deep(.el-sub-menu .el-menu-item) {
    padding-left: 50px !important;
  }
}
</style>
