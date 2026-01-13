import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken } from './auth'
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
      
      // Token过期或被挤下线
      if (res.code === 401) {
        ElMessage.warning('登录已失效，请重新登录')
        removeToken()
        router.push('/login')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    console.error('响应错误:', error)
    const showError = error.config?.showError !== false
    
    // 处理401错误（可能是被挤下线）
    if (error.response && error.response.status === 401) {
      ElMessage.warning('账号已在其他设备登录，请重新登录')
      removeToken()
      router.push('/login')
    } else if (showError) {
      ElMessage.error(error.response?.data?.message || error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default service
