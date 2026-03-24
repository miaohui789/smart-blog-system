import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken } from './auth'
import router from '@/router'

const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API,
  timeout: 15000
})

service.interceptors.request.use(
  config => {
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      // 对于登录相关的错误，统一显示"用户名或密码错误"
      const loginErrorCodes = [400, 401]
      const loginErrorMessages = ['密码错误', '用户名不存在', '账号不存在', '密码不正确', '用户不存在']
      
      // 判断是否为登录错误
      const isLoginError = loginErrorCodes.includes(res.code) || 
                          loginErrorMessages.some(msg => res.message && res.message.includes(msg))
      
      if (isLoginError) {
        ElMessage.error('用户名或密码错误')
      } else if (res.code === 401) {
        ElMessage.warning('登录已失效，请重新登录')
        removeToken()
        router.push('/login')
      } else {
        ElMessage.error(res.message || '请求失败')
      }
      
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  error => {
    // 处理HTTP错误
    if (error.response) {
      const status = error.response.status
      const data = error.response.data
      
      if (status === 401) {
        // 判断是登录失败还是token失效
        if (data && (data.message === '密码错误' || data.message === '用户名不存在' || data.message === '账号不存在')) {
          ElMessage.error('用户名或密码错误')
        } else {
          ElMessage.warning('账号已在其他设备登录，请重新登录')
          removeToken()
          router.push('/login')
        }
      } else if (status === 400) {
        ElMessage.error('用户名或密码错误')
      } else {
        ElMessage.error(data?.message || error.message || '网络错误')
      }
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default service
