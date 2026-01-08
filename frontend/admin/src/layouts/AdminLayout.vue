<template>
  <div class="admin-layout" :class="{ collapsed: settingsStore.sidebarCollapsed }">
    <Sidebar />
    <div class="main-container">
      <Navbar />
      <TagsView v-if="settingsStore.showTagsView" />
      <main class="app-main">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { useSettingsStore } from '@/stores/settings'
import Sidebar from './Sidebar/index.vue'
import Navbar from './Navbar/index.vue'
import TagsView from './TagsView/index.vue'

const settingsStore = useSettingsStore()
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.admin-layout {
  display: flex;
  min-height: 100vh;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin-left: $sidebar-width;
  transition: margin-left 0.3s;
}

.admin-layout.collapsed .main-container {
  margin-left: $sidebar-collapsed-width;
}

.app-main {
  flex: 1;
  padding: $spacing-lg;
  background: var(--bg-dark);
  overflow: auto;
  transition: background-color 0.3s;
}
</style>
