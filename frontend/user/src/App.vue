<template>
  <router-view />
</template>

<script setup>
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'

const userStore = useUserStore()
const themeStore = useThemeStore()

// 页面加载时，如果已登录则刷新用户信息
onMounted(() => {
  if (userStore.isLoggedIn) {
    userStore.fetchUserInfo()
  }
})
</script>

<style lang="scss">
#app {
  min-height: 100vh;
  background-color: var(--bg-dark);
  color: var(--text-secondary);
  transition: background-color 0.3s, color 0.3s;
}
</style>
