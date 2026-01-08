<template>
  <router-view />
</template>

<script setup>
import { onMounted } from 'vue'
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()

onMounted(() => {
  themeStore.applyTheme()
})
</script>

<style lang="scss">
#app {
  min-height: 100vh;
  background-color: var(--bg-dark);
  transition: background-color 0.3s;
}
</style>
