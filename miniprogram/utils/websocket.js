// websocket.js - WebSocket 封装
const config = require('./config')
const auth = require('./auth')

class WebSocketManager {
  constructor() {
    this.socketTask = null
    this.isConnected = false
    this.reconnectTimer = null
    this.heartbeatTimer = null
    this.messageHandlers = []
    this.reconnectCount = 0
    this.maxReconnectCount = 5
  }

  /**
   * 连接 WebSocket
   */
  connect(platform = 'user') {
    if (this.isConnected) {
      console.log('WebSocket 已连接')
      return
    }

    const token = auth.getToken()
    const userInfo = auth.getUserInfo()
    if (!token || !userInfo || !userInfo.id) {
      console.error('未登录，无法连接 WebSocket')
      return
    }

    const url = `${config.WS_URL}/${platform}/${userInfo.id}`
    console.log('正在连接 WebSocket:', url)

    this.socketTask = wx.connectSocket({
      url,
      success: () => {
        console.log('WebSocket 连接请求已发送')
      },
      fail: (err) => {
        console.error('WebSocket 连接失败:', err)
      }
    })

    // 监听连接打开
    this.socketTask.onOpen(() => {
      console.log('========== WebSocket 连接成功 ==========')
      console.log('连接URL:', url)
      console.log('用户ID:', userInfo.id)
      this.isConnected = true
      this.reconnectCount = 0
      this.startHeartbeat()
      
      // 发送连接成功的测试消息
      wx.showToast({
        title: 'WebSocket已连接',
        icon: 'success',
        duration: 2000
      })
    })

    // 监听消息
    this.socketTask.onMessage((res) => {
      try {
        const data = JSON.parse(res.data)
        console.log('========== 收到 WebSocket 消息 ==========', data)
        
        // 触发所有消息处理器
        this.messageHandlers.forEach(handler => {
          try {
            handler(data)
          } catch (err) {
            console.error('消息处理器执行失败:', err)
          }
        })
      } catch (err) {
        console.error('解析 WebSocket 消息失败:', err, res.data)
      }
    })

    // 监听连接关闭
    this.socketTask.onClose(() => {
      console.log('========== WebSocket 连接已关闭 ==========')
      this.isConnected = false
      this.stopHeartbeat()
      this.reconnect(platform)
    })

    // 监听错误
    this.socketTask.onError((err) => {
      console.error('========== WebSocket 错误 ==========', err)
      this.isConnected = false
    })
  }

  /**
   * 发送消息
   */
  send(data) {
    if (!this.isConnected) {
      console.error('WebSocket 未连接')
      return false
    }

    try {
      this.socketTask.send({
        data: JSON.stringify(data),
        success: () => {
          console.log('消息发送成功:', data)
        },
        fail: (err) => {
          console.error('消息发送失败:', err)
        }
      })
      return true
    } catch (err) {
      console.error('发送消息异常:', err)
      return false
    }
  }

  /**
   * 注册消息处理器
   */
  onMessage(handler) {
    if (typeof handler === 'function') {
      this.messageHandlers.push(handler)
      console.log(`WebSocket注册监听器，当前总数: ${this.messageHandlers.length}`)
    }
  }

  /**
   * 移除消息处理器
   */
  offMessage(handler) {
    const index = this.messageHandlers.indexOf(handler)
    if (index > -1) {
      this.messageHandlers.splice(index, 1)
      console.log(`WebSocket移除监听器，当前总数: ${this.messageHandlers.length}`)
    }
  }

  /**
   * 开始心跳
   */
  startHeartbeat() {
    this.stopHeartbeat()
    
    this.heartbeatTimer = setInterval(() => {
      if (this.isConnected) {
        this.send({ type: 'heartbeat', timestamp: Date.now() })
      }
    }, 30000) // 每 30 秒发送一次心跳
  }

  /**
   * 停止心跳
   */
  stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }

  /**
   * 重新连接
   */
  reconnect(platform) {
    if (this.reconnectTimer) {
      return
    }

    if (this.reconnectCount >= this.maxReconnectCount) {
      console.log('达到最大重连次数，停止重连')
      return
    }

    this.reconnectCount++
    console.log(`尝试第 ${this.reconnectCount} 次重连...`)

    this.reconnectTimer = setTimeout(() => {
      this.connect(platform)
      this.reconnectTimer = null
    }, 3000) // 3 秒后重连
  }

  /**
   * 关闭连接
   */
  close() {
    this.stopHeartbeat()
    
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }

    if (this.socketTask) {
      this.socketTask.close({
        success: () => {
          console.log('WebSocket 已主动关闭')
        }
      })
      this.socketTask = null
    }

    this.isConnected = false
    this.messageHandlers = []
    this.reconnectCount = 0
  }
}

// 导出单例
module.exports = new WebSocketManager()
