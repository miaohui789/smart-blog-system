/**
 * 全局WebSocket工具 - 用于通知推送和评论实时刷新
 * 优化版本：支持断线重连、消息队列、性能优化
 */

let ws = null
let reconnectTimer = null
let heartbeatTimer = null
let userId = null
let reconnectAttempts = 0
const MAX_RECONNECT_ATTEMPTS = 10
const RECONNECT_INTERVAL = 3000
const HEARTBEAT_INTERVAL = 25000

// 消息队列（断线时缓存消息）
let messageQueue = []

// 连接状态
let isConnecting = false

// 回调函数（使用Set避免重复注册）
const callbacks = {
  notification: new Set(),
  new_comment: new Set(),
  message: new Set(),
  force_logout: new Set(),
  unread_update: new Set()  // 未读数更新
}

const WS_BASE_URL = import.meta.env.VITE_WS_URL || `ws://${window.location.host}`

/**
 * 获取连接状态
 */
export function isConnected() {
  return ws && ws.readyState === WebSocket.OPEN
}

/**
 * 初始化WebSocket连接
 */
export function initWebSocket(uid) {
  if (!uid) return
  
  // 防止重复连接
  if (isConnecting) return
  if (ws && ws.readyState === WebSocket.OPEN && userId === uid) return
  
  userId = uid
  isConnecting = true
  
  // 关闭旧连接
  if (ws) {
    ws.close()
    ws = null
  }
  
  const url = `${WS_BASE_URL}/ws/user/${uid}`
  
  try {
    ws = new WebSocket(url)
    
    ws.onopen = () => {
      console.log('[WebSocket] 连接成功')
      isConnecting = false
      reconnectAttempts = 0
      startHeartbeat()
      
      // 发送队列中的消息
      flushMessageQueue()
    }
    
    ws.onmessage = (event) => {
      handleMessage(event.data)
    }
    
    ws.onclose = (event) => {
      console.log('[WebSocket] 连接关闭', event.code)
      isConnecting = false
      stopHeartbeat()
      
      // 非正常关闭时尝试重连
      if (userId && event.code !== 1000) {
        scheduleReconnect()
      }
    }
    
    ws.onerror = (error) => {
      console.error('[WebSocket] 连接错误')
      isConnecting = false
    }
  } catch (e) {
    console.error('[WebSocket] 创建连接失败:', e)
    isConnecting = false
    scheduleReconnect()
  }
}

/**
 * 处理接收到的消息
 */
function handleMessage(rawData) {
  try {
    const data = JSON.parse(rawData)
    console.log('[WebSocket] 收到消息:', data.type, data)
    
    // 心跳响应
    if (data.type === 'pong') return
    
    // 根据类型分发消息
    switch (data.type) {
      case 'FORCE_LOGOUT':
        callbacks.force_logout.forEach(cb => cb(data))
        break
      case 'notification':
        callbacks.notification.forEach(cb => cb(data.data))
        // 同时触发未读数更新
        callbacks.unread_update.forEach(cb => cb())
        break
      case 'new_comment':
        console.log('[WebSocket] 广播新评论:', data.data)
        callbacks.new_comment.forEach(cb => cb(data.data))
        break
      case 'message':
        console.log('[WebSocket] 收到私信, 回调数量:', callbacks.message.size)
        callbacks.message.forEach(cb => cb(data.data))
        // 私信也触发未读数更新
        callbacks.unread_update.forEach(cb => cb())
        break
      case 'unread_update':
        callbacks.unread_update.forEach(cb => cb(data.data))
        break
    }
  } catch (e) {
    console.error('[WebSocket] 解析消息失败:', e)
  }
}

/**
 * 安排重连
 */
function scheduleReconnect() {
  if (reconnectTimer) return
  if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
    console.log('[WebSocket] 达到最大重连次数')
    return
  }
  
  reconnectAttempts++
  const delay = Math.min(RECONNECT_INTERVAL * reconnectAttempts, 30000)
  
  console.log(`[WebSocket] ${delay/1000}秒后重连 (${reconnectAttempts}/${MAX_RECONNECT_ATTEMPTS})`)
  
  reconnectTimer = setTimeout(() => {
    reconnectTimer = null
    if (userId) {
      initWebSocket(userId)
    }
  }, delay)
}

/**
 * 关闭WebSocket连接
 */
export function closeWebSocket() {
  userId = null
  reconnectAttempts = 0
  
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
  stopHeartbeat()
  
  if (ws) {
    ws.close(1000, 'User logout')
    ws = null
  }
  
  // 清空回调
  Object.keys(callbacks).forEach(key => {
    callbacks[key].clear()
  })
  
  messageQueue = []
}

/**
 * 发送消息（支持离线队列）
 */
export function sendMessage(data) {
  const message = JSON.stringify(data)
  
  if (ws && ws.readyState === WebSocket.OPEN) {
    ws.send(message)
  } else {
    // 缓存消息，连接后发送
    messageQueue.push(message)
  }
}

/**
 * 发送队列中的消息
 */
function flushMessageQueue() {
  while (messageQueue.length > 0 && ws && ws.readyState === WebSocket.OPEN) {
    const message = messageQueue.shift()
    ws.send(message)
  }
}

/**
 * 通知服务器用户正在浏览某篇文章
 */
export function viewArticle(articleId) {
  sendMessage({ type: 'view_article', articleId: Number(articleId) })
}

/**
 * 通知服务器用户离开文章页面
 */
export function leaveArticle() {
  sendMessage({ type: 'leave_article' })
}

/**
 * 注册回调（返回取消注册函数）
 */
function registerCallback(type, callback) {
  if (!callbacks[type]) return () => {}
  callbacks[type].add(callback)
  return () => callbacks[type].delete(callback)
}

/**
 * 注册通知回调
 */
export function onNotification(callback) {
  return registerCallback('notification', callback)
}

/**
 * 注册新评论回调
 */
export function onNewComment(callback) {
  return registerCallback('new_comment', callback)
}

/**
 * 注册私信回调
 */
export function onMessage(callback) {
  return registerCallback('message', callback)
}

/**
 * 注册强制下线回调
 */
export function onForceLogout(callback) {
  return registerCallback('force_logout', callback)
}

/**
 * 注册未读数更新回调
 */
export function onUnreadUpdate(callback) {
  return registerCallback('unread_update', callback)
}

/**
 * 开始心跳
 */
function startHeartbeat() {
  stopHeartbeat()
  heartbeatTimer = setInterval(() => {
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify({ type: 'ping' }))
    }
  }, HEARTBEAT_INTERVAL)
}

/**
 * 停止心跳
 */
function stopHeartbeat() {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer)
    heartbeatTimer = null
  }
}
