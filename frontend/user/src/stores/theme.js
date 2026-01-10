import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  const isDark = ref(true)
  
  // 从 localStorage 读取主题
  const savedTheme = localStorage.getItem('theme')
  if (savedTheme) {
    isDark.value = savedTheme === 'dark'
  } else {
    // 检测系统主题偏好
    isDark.value = window.matchMedia('(prefers-color-scheme: dark)').matches
  }

  // 应用主题
  function applyTheme() {
    document.documentElement.setAttribute('data-theme', isDark.value ? 'dark' : 'light')
    localStorage.setItem('theme', isDark.value ? 'dark' : 'light')
  }

  // 切换主题（简单切换，让按钮自己的 CSS 动画生效）
  function toggleTheme(event) {
    isDark.value = !isDark.value
    applyTheme()
  }

  // 初始化时应用主题
  applyTheme()

  return { isDark, toggleTheme, applyTheme }
})
