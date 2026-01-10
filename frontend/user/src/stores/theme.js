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

  // 切换主题（带圆形扩散动画）
  function toggleTheme(event) {
    // 获取点击位置
    const x = event?.clientX ?? window.innerWidth / 2
    const y = event?.clientY ?? window.innerHeight / 2
    
    // 计算最大半径（从点击位置到最远角落的距离）
    const endRadius = Math.hypot(
      Math.max(x, window.innerWidth - x),
      Math.max(y, window.innerHeight - y)
    )
    
    // 检查浏览器是否支持 View Transitions API
    if (!document.startViewTransition) {
      isDark.value = !isDark.value
      applyTheme()
      return
    }
    
    // 使用 View Transitions API 实现圆形扩散效果
    const transition = document.startViewTransition(() => {
      isDark.value = !isDark.value
      applyTheme()
    })
    
    transition.ready.then(() => {
      // 根据切换方向决定动画方向
      const clipPath = isDark.value
        ? [ // 切换到暗色：圆形扩大
            `circle(0px at ${x}px ${y}px)`,
            `circle(${endRadius}px at ${x}px ${y}px)`
          ]
        : [ // 切换到亮色：圆形收缩（反向）
            `circle(${endRadius}px at ${x}px ${y}px)`,
            `circle(0px at ${x}px ${y}px)`
          ]
      
      // 执行圆形裁剪动画
      document.documentElement.animate(
        { clipPath },
        {
          duration: 500,
          easing: 'ease-out',
          pseudoElement: isDark.value 
            ? '::view-transition-new(root)' 
            : '::view-transition-old(root)'
        }
      )
    })
  }

  // 初始化时应用主题
  applyTheme()

  return { isDark, toggleTheme, applyTheme }
})
