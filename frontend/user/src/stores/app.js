import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const searchVisible = ref(false)

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function toggleSearch() {
    searchVisible.value = !searchVisible.value
  }

  function closeSearch() {
    searchVisible.value = false
  }

  return {
    sidebarCollapsed,
    searchVisible,
    toggleSidebar,
    toggleSearch,
    closeSearch
  }
})
