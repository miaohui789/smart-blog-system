import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getSiteConfig } from '@/api/config'

export const useConfigStore = defineStore('config', () => {
  // 网站配置
  const siteName = ref('My Blog')
  const siteDescription = ref('')
  const siteLogo = ref('')
  const siteKeywords = ref('')
  const siteFavicon = ref('')
  const siteFooter = ref('')
  const icp = ref('')
  
  // 是否已加载
  const loaded = ref(false)

  // 加载配置
  async function loadConfig() {
    if (loaded.value) return
    
    try {
      const res = await getSiteConfig()
      if (res.data) {
        siteName.value = res.data.name || 'My Blog'
        siteDescription.value = res.data.description || ''
        siteLogo.value = res.data.logo || ''
        siteKeywords.value = res.data.keywords || ''
        siteFavicon.value = res.data.favicon || ''
        siteFooter.value = res.data.footer || ''
        icp.value = res.data.icp || ''
        
        // 更新页面标题
        document.title = siteName.value
        
        // 更新favicon
        if (siteFavicon.value) {
          const link = document.querySelector("link[rel*='icon']") || document.createElement('link')
          link.type = 'image/x-icon'
          link.rel = 'shortcut icon'
          link.href = siteFavicon.value
          document.getElementsByTagName('head')[0].appendChild(link)
        }
        
        // 更新meta描述
        if (siteDescription.value) {
          let meta = document.querySelector('meta[name="description"]')
          if (!meta) {
            meta = document.createElement('meta')
            meta.name = 'description'
            document.head.appendChild(meta)
          }
          meta.content = siteDescription.value
        }
        
        // 更新meta关键词
        if (siteKeywords.value) {
          let meta = document.querySelector('meta[name="keywords"]')
          if (!meta) {
            meta = document.createElement('meta')
            meta.name = 'keywords'
            document.head.appendChild(meta)
          }
          meta.content = siteKeywords.value
        }
        
        loaded.value = true
      }
    } catch (e) {
      console.error('加载网站配置失败:', e)
    }
  }

  return {
    siteName,
    siteDescription,
    siteLogo,
    siteKeywords,
    siteFavicon,
    siteFooter,
    icp,
    loaded,
    loadConfig
  }
})
