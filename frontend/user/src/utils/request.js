import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, clearAuth } from './auth'
import router from '@/router'

const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API,
  timeout: 15000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    const showError = response.config.showError !== false // 默认显示错误
    
    if (res.code !== 200) {
      if (showError) {
        ElMessage.error(res.message || '请求失败')
      }
      
      // Token过期或被挤下线（登录流程中的请求跳过重定向，避免新token竞争问题）
      if (res.code === 401 && !response.config?.noAuthRedirect) {
        ElMessage.warning('登录已失效，请重新登录')
        clearAuth()
        // 使用 location.href 强制刷新，确保状态完全重置
        window.location.href = '/login'
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    console.error('响应错误:', error)
    const showError = error.config?.showError !== false
    
    // 处理401错误（可能是被挤下线，但登录流程中的请求跳过重定向）
    if (error.response && error.response.status === 401 && !error.config?.noAuthRedirect) {
      ElMessage.warning('登录已失效，请重新登录')
      clearAuth()
      window.location.href = '/login'
    } else if (showError) {
      ElMessage.error(error.response?.data?.message || error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default service
