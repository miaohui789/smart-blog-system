import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus, { ElMessage } from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

import App from './App.vue'
import router from './router'
import '@/assets/styles/global.scss'

const app = createApp(App)

// 配置 ElMessage 全局默认值，只显示一个消息
ElMessage.success = ((original) => (msg) => {
  ElMessage.closeAll()
  return original({ message: msg, duration: 1500, grouping: true })
})(ElMessage.success)

ElMessage.error = ((original) => (msg) => {
  ElMessage.closeAll()
  return original({ message: msg, duration: 2000, grouping: true })
})(ElMessage.error)

ElMessage.warning = ((original) => (msg) => {
  ElMessage.closeAll()
  return original({ message: msg, duration: 2000, grouping: true })
})(ElMessage.warning)

ElMessage.info = ((original) => (msg) => {
  ElMessage.closeAll()
  return original({ message: msg, duration: 1500, grouping: true })
})(ElMessage.info)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })

app.mount('#app')
