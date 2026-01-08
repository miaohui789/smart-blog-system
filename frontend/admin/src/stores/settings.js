import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useSettingsStore = defineStore('settings', () => {
  const sidebarCollapsed = ref(false)
  const showTagsView = ref(true)

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function toggleTagsView() {
    showTagsView.value = !showTagsView.value
  }

  return { sidebarCollapsed, showTagsView, toggleSidebar, toggleTagsView }
})
