import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken } from './auth'
import { closeWebSocket } from './websocket'
import router from '@/router'

const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API,
  timeout: 15000
})

let authRedirecting = false

function isLoginRequest(url = '') {
  return url.includes('/admin/auth/login')
}

function isCredentialError(message = '') {
  const loginErrorMessages = ['密码错误', '用户名不存在', '账号不存在', '密码不正确', '用户不存在']
  return loginErrorMessages.some(item => message.includes(item))
}

function redirectToLogin(message = '登录已失效，请重新登录') {
  closeWebSocket()
  removeToken()

  if (!authRedirecting) {
    authRedirecting = true
    ElMessage.warning(message)
  }

  const resetRedirectState = () => {
    window.setTimeout(() => {
      authRedirecting = false
    }, 300)
  }

  if (router.currentRoute.value.path === '/login') {
    resetRedirectState()
    return
  }

  router.replace('/login').finally(resetRedirectState)
}

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
      const requestUrl = response.config?.url || ''
      const responseMessage = res.message || ''

      if (isLoginRequest(requestUrl) && (res.code === 400 || res.code === 401 || isCredentialError(responseMessage))) {
        ElMessage.error('用户名或密码错误')
      } else if (res.code === 401) {
        redirectToLogin('登录已失效，请重新登录')
      } else {
        ElMessage.error(responseMessage || '请求失败')
      }
      
      return Promise.reject(new Error(responseMessage))
    }
    return res
  },
  error => {
    // 处理HTTP错误
    if (error.response) {
      const status = error.response.status
      const data = error.response.data
      const requestUrl = error.config?.url || ''
      const responseMessage = data?.message || ''
      
      if (status === 401) {
        if (isLoginRequest(requestUrl) && isCredentialError(responseMessage)) {
          ElMessage.error('用户名或密码错误')
        } else {
          redirectToLogin('账号已在其他设备登录，请重新登录')
        }
      } else if (status === 400) {
        ElMessage.error('用户名或密码错误')
      } else {
        ElMessage.error(responseMessage || error.message || '网络错误')
      }
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default service
