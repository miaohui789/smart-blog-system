import { defineStore } from 'pinia'
import { ref, watch } from 'vue'
import { saveTheme } from '@/api/user'
import { getToken } from '@/utils/auth'

export const useThemeStore = defineStore('theme', () => {
  const isDark = ref(true)
  const hasCustomBackground = ref(false)
  
  // 从 localStorage 读取主题
  const savedTheme = localStorage.getItem('theme')
  if (savedTheme) {
    isDark.value = savedTheme === 'dark'
  } else {
    // 检测系统主题偏好
    isDark.value = window.matchMedia('(prefers-color-scheme: dark)').matches
  }

  // 检查是否有自定义背景
  function checkCustomBackground() {
    const theme = isDark.value ? 'dark' : 'light'
    const skinData = localStorage.getItem(`${theme}Skin`)
    
    if (skinData) {
      try {
        const data = JSON.parse(skinData)
        hasCustomBackground.value = data.skinId !== 'default' && !!data.component
      } catch (e) {
        hasCustomBackground.value = false
      }
    } else {
      hasCustomBackground.value = false
    }
    
    // 更新CSS变量
    updateTransparency()
  }

  // 更新透明度
  function updateTransparency() {
    if (hasCustomBackground.value) {
      document.body.classList.add('has-custom-bg')
    } else {
      document.body.classList.remove('has-custom-bg')
    }
  }

  // 应用主题
  function applyTheme(options = {}) {
    const { syncServer = true } = options
    document.documentElement.setAttribute('data-theme', isDark.value ? 'dark' : 'light')
    localStorage.setItem('theme', isDark.value ? 'dark' : 'light')
    checkCustomBackground()
    
    if (syncServer && getToken()) {
      saveThemeMode()
    }
  }

  // 保存主题模式到服务器
  async function saveThemeMode() {
    try {
      const themeMode = isDark.value ? 'dark' : 'light'
      
      // 获取当前皮肤设置
      const darkSkinData = localStorage.getItem('darkSkin')
      const lightSkinData = localStorage.getItem('lightSkin')
      
      let darkSkin = 'default'
      let lightSkin = 'default'
      
      if (darkSkinData) {
        try {
          const data = JSON.parse(darkSkinData)
          darkSkin = data.skinId || 'default'
        } catch (e) {
          darkSkin = 'default'
        }
      }
      
      if (lightSkinData) {
        try {
          const data = JSON.parse(lightSkinData)
          lightSkin = data.skinId || 'default'
        } catch (e) {
          lightSkin = 'default'
        }
      }
      
      await saveTheme({
        themeMode,
        darkSkin,
        lightSkin
      })
      console.log('主题模式已保存到服务器')
    } catch (e) {
      console.error('保存主题模式失败:', e)
    }
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
      // 触发主题切换事件
      window.dispatchEvent(new Event('theme-changed'))
      return
    }
    
    // 使用 View Transitions API 实现圆形扩散效果
    const transition = document.startViewTransition(() => {
      isDark.value = !isDark.value
      applyTheme()
      // 触发主题切换事件
      window.dispatchEvent(new Event('theme-changed'))
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
          fill: 'forwards',  // 保持最终帧，防止动画结束后回退导致闪烁
          pseudoElement: isDark.value 
            ? '::view-transition-new(root)' 
            : '::view-transition-old(root)'
        }
      )
    })
  }

  // 初始化时应用主题
  applyTheme({ syncServer: false })

  return { isDark, hasCustomBackground, toggleTheme, applyTheme, checkCustomBackground }
})
