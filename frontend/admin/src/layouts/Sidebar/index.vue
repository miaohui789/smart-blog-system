<template>
  <aside class="sidebar" :class="{ collapsed: settingsStore.sidebarCollapsed }">
    <div class="logo">
      <span v-if="!settingsStore.sidebarCollapsed">博客管理</span>
      <span v-else>B</span>
    </div>
    <el-menu
      :default-active="$route.path"
      :collapse="settingsStore.sidebarCollapsed"
      background-color="#1a1a1a"
      text-color="#a1a1aa"
      active-text-color="#a855f7"
      router
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
  background: $bg-card;
  border-right: 1px solid $border-color;
  transition: width 0.3s;
  z-index: 100;

  &.collapsed {
    width: $sidebar-collapsed-width;
  }
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  color: $primary-color;
  border-bottom: 1px solid $border-color;
}

:deep(.el-menu) {
  border-right: none;
}
</style>
