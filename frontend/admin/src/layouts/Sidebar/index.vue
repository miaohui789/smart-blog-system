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
      <el-menu-item index="/dashboard">
        <el-icon><Odometer /></el-icon>
        <span>仪表盘</span>
      </el-menu-item>
      <el-sub-menu index="/article">
        <template #title>
          <el-icon><Document /></el-icon>
          <span>文章管理</span>
        </template>
        <el-menu-item index="/article/list">文章列表</el-menu-item>
        <el-menu-item index="/article/create">创建文章</el-menu-item>
      </el-sub-menu>
      <el-menu-item index="/category">
        <el-icon><Folder /></el-icon>
        <span>分类管理</span>
      </el-menu-item>
      <el-menu-item index="/tag">
        <el-icon><PriceTag /></el-icon>
        <span>标签管理</span>
      </el-menu-item>
      <el-menu-item index="/comment">
        <el-icon><ChatDotRound /></el-icon>
        <span>评论管理</span>
      </el-menu-item>
      <el-menu-item index="/user">
        <el-icon><User /></el-icon>
        <span>用户管理</span>
      </el-menu-item>
      <el-sub-menu index="/system">
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
import { useSettingsStore } from '@/stores/settings'
import { Odometer, Document, Folder, PriceTag, ChatDotRound, User, Setting } from '@element-plus/icons-vue'

const settingsStore = useSettingsStore()
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
