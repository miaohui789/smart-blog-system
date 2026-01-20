// app.js
const config = require('./utils/config')

App({
  globalData: {
    userInfo: null,
    token: null,
    isVip: false,
    vipLevel: 0,
    baseUrl: config.BASE_URL.replace('/api', '')
  },

  onLaunch() {
    // 小程序启动
    console.log('小程序启动')
    
    // 检查登录状态
    this.checkLoginStatus()
    
    // 获取系统信息
    this.getSystemInfo()
    
    // 初始化WebSocket
    this.initWebSocket()
  },

  onShow() {
    // 小程序显示
    console.log('小程序显示')
  },

  onHide() {
    // 小程序隐藏
    console.log('小程序隐藏')
  },

  // 检查登录状态
  checkLoginStatus() {
    const token = wx.getStorageSync('token')
    if (token) {
      this.globalData.token = token
      this.getUserInfo()
    }
  },

  // 获取用户信息
  async getUserInfo() {
    try {
      const userApi = require('./api/user')
      const userInfo = await userApi.getUserInfo()
      this.globalData.userInfo = userInfo
      this.globalData.isVip = userInfo.isVip
      this.globalData.vipLevel = userInfo.vipLevel
    } catch (err) {
      console.error('获取用户信息失败:', err)
    }
  },

  // 获取系统信息
  getSystemInfo() {
    wx.getSystemInfo({
      success: (res) => {
        this.globalData.systemInfo = res
        console.log('系统信息:', res)
      }
    })
  },

  // 初始化WebSocket
  initWebSocket() {
    const token = wx.getStorageSync('token')
    if (token) {
      const websocket = require('./utils/websocket')
      websocket.connect('user')
    }
  }
})
