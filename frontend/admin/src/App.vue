<template>
  <router-view />
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useThemeStore } from '@/stores/theme'
import { useUserStore } from '@/stores/user'
import { TOKEN_KEY } from '@/utils/auth'

const themeStore = useThemeStore()
const userStore = useUserStore()
const router = useRouter()

function handleTokenStorageChange(event) {
  if (event.key !== TOKEN_KEY || event.oldValue === event.newValue) {
    return
  }

  const message = event.newValue
    ? '登录状态已变更，请重新登录'
    : '登录状态已失效，请重新登录'

  userStore.resetState()

  if (router.currentRoute.value.path !== '/login') {
    ElMessage.warning(message)
    router.replace('/login')
  }
}

onMounted(() => {
  themeStore.applyTheme()
  window.addEventListener('storage', handleTokenStorageChange)
})

onUnmounted(() => {
  window.removeEventListener('storage', handleTokenStorageChange)
})
</script>

<style lang="scss">
#app {
  min-height: 100vh;
  background-color: var(--bg-dark);
  transition: background-color 0.3s;
}
</style>
