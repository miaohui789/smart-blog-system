import { ElMessageBox } from 'element-plus'
import { getToken, removeToken } from './auth'
import router from '@/router'

let ws = null
let reconnectTimer = null
let heartbeatTimer = null

/**
 * 初始化WebSocket连接
 */
export function initWebSocket(userId) {
  if (!userId || !getToken()) return
  
  closeWebSocket()
  
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = import.meta.env.VITE_APP_WS_URL || `${protocol}//${window.location.hostname}:8080`
  const wsUrl = `${host}/ws/admin/${userId}`
  
  console.log('正在连接WebSocket:', wsUrl)
  
  try {
    ws = new WebSocket(wsUrl)
    
    ws.onopen = () => {
      console.log('WebSocket连接成功')
      startHeartbeat()
    }
    
    ws.onmessage = (event) => {
      console.log('WebSocket收到消息:', event.data)
      try {
        const data = JSON.parse(event.data)
        if (data.type === 'FORCE_LOGOUT') {
          handleForceLogout(data.message)
        }
      } catch (e) {
        // 心跳响应
      }
    }
    
    ws.onclose = (event) => {
      console.log('WebSocket连接关闭, code:', event.code)
      stopHeartbeat()
      if (event.code !== 1000 && event.code !== 1001 && getToken()) {
        scheduleReconnect(userId)
      }
    }
    
    ws.onerror = (error) => {
      console.error('WebSocket错误:', error)
    }
  } catch (e) {
    console.error('WebSocket初始化失败:', e)
  }
}

function handleForceLogout(message) {
  console.log('收到强制下线通知')
  closeWebSocket()
  removeToken()
  
  ElMessageBox.alert(message || '账号已在其他设备登录', '下线通知', {
    confirmButtonText: '重新登录',
    type: 'warning',
    showClose: false,
    closeOnClickModal: false,
    closeOnPressEscape: false
  }).finally(() => {
    // 强制刷新页面，路由守卫会检测到没有token自动跳转登录页
    window.location.reload()
  })
}

function startHeartbeat() {
  stopHeartbeat()
  heartbeatTimer = setInterval(() => {
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send('ping')
    }
  }, 30000)
}

function stopHeartbeat() {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer)
    heartbeatTimer = null
  }
}

function scheduleReconnect(userId) {
  if (reconnectTimer) return
  reconnectTimer = setTimeout(() => {
    reconnectTimer = null
    if (getToken()) {
      initWebSocket(userId)
    }
  }, 5000)
}

export function closeWebSocket() {
  stopHeartbeat()
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
  if (ws) {
    ws.close(1000)
    ws = null
  }
}
