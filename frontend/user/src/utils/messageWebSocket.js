/**
 * 私信WebSocket工具
 * 注意：私信消息通过主 WebSocket 推送，这里只是提供兼容接口
 * 实际连接复用 websocket.js 中的连接
 */

import { onMessage as onWsMessage } from './websocket'

let messageCallback = null
let unsubscribe = null

/**
 * 初始化私信WebSocket连接（实际复用主WebSocket）
 */
export function initMessageWebSocket(userId) {
  // 先取消之前的订阅
  if (unsubscribe) {
    unsubscribe()
  }
  
  // 注册主 WebSocket 的消息回调
  unsubscribe = onWsMessage((data) => {
    console.log('[MessageWS] 收到私信:', data)
    if (messageCallback) {
      messageCallback(data)
    }
  })
}

/**
 * 关闭WebSocket连接
 */
export function closeMessageWebSocket() {
  if (unsubscribe) {
    unsubscribe()
    unsubscribe = null
  }
  messageCallback = null
}

/**
 * 注册新消息回调
 */
export function onNewMessage(callback) {
  messageCallback = callback
}

/**
 * 发送消息（私信通过 HTTP API 发送，不通过 WebSocket）
 */
export function sendWsMessage(data) {
  // 私信发送通过 HTTP API，不需要 WebSocket
  console.log('[MessageWS] 私信通过 HTTP API 发送')
}
