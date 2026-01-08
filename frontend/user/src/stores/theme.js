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

  // 切换主题（带动画）
  async function toggleTheme(event) {
    // 检查浏览器是否支持 View Transitions API
    if (!document.startViewTransition) {
      isDark.value = !isDark.value
      applyTheme()
      return
    }

    // 获取点击位置
    const x = event?.clientX ?? window.innerWidth / 2
    const y = event?.clientY ?? window.innerHeight / 2
    
    // 计算最大半径
    const endRadius = Math.hypot(
      Math.max(x, window.innerWidth - x),
      Math.max(y, window.innerHeight - y)
    )

    // 使用 View Transitions API 实现圆形扩散动画
    const transition = document.startViewTransition(async () => {
      isDark.value = !isDark.value
      applyTheme()
    })

    transition.ready.then(() => {
      const clipPath = [
        `circle(0px at ${x}px ${y}px)`,
        `circle(${endRadius}px at ${x}px ${y}px)`
      ]
      
      // 切换到暗色：旧视图（亮色）从全屏收缩到点击位置消失
      // 切换到亮色：新视图（亮色）从点击位置扩散到全屏
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

  // 初始化时应用主题
  applyTheme()

  return { isDark, toggleTheme, applyTheme }
})
