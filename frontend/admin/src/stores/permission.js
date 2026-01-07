import { defineStore } from 'pinia'
import { ref } from 'vue'

export const usePermissionStore = defineStore('permission', () => {
  const routes = ref([])
  const addRoutes = ref([])

  function setRoutes(newRoutes) {
    routes.value = newRoutes
    addRoutes.value = newRoutes
  }

  return { routes, addRoutes, setRoutes }
})
