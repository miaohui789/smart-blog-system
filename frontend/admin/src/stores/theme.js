import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  const isDark = ref(true)
  
  // 从 localStorage 读取主题
  const savedTheme = localStorage.getItem('admin-theme')
  if (savedTheme) {
    isDark.value = savedTheme === 'dark'
  } else {
    isDark.value = window.matchMedia('(prefers-color-scheme: dark)').matches
  }

  // 应用主题
  function applyTheme() {
    document.documentElement.setAttribute('data-theme', isDark.value ? 'dark' : 'light')
    localStorage.setItem('admin-theme', isDark.value ? 'dark' : 'light')
  }

  // 切换主题（带动画）
  async function toggleTheme(event) {
    if (!document.startViewTransition) {
      isDark.value = !isDark.value
      applyTheme()
      return
    }

    const x = event?.clientX ?? window.innerWidth / 2
    const y = event?.clientY ?? window.innerHeight / 2
    
    const endRadius = Math.hypot(
      Math.max(x, window.innerWidth - x),
      Math.max(y, window.innerHeight - y)
    )

    const transition = document.startViewTransition(async () => {
      isDark.value = !isDark.value
      applyTheme()
    })

    transition.ready.then(() => {
      const clipPath = [
        `circle(0px at ${x}px ${y}px)`,
        `circle(${endRadius}px at ${x}px ${y}px)`
      ]
      
      document.documentElement.animate(
        { clipPath: isDark.value ? [...clipPath].reverse() : clipPath },
        {
          duration: 600,
          easing: 'ease-in-out',
          pseudoElement: isDark.value 
            ? '::view-transition-old(root)' 
            : '::view-transition-new(root)'
        }
      )
    })
  }

  applyTheme()

  return { isDark, toggleTheme, applyTheme }
})
