// custom-tab-bar/index.js
Component({
  data: {
    selected: 0,
    color: "#64748b",
    selectedColor: "#3b82f6",
    unreadCount: 0,
    list: [
      {
        pagePath: "/pages/index/index",
        text: "首页",
        icon: "wap-home-o",
        selectedIcon: "wap-home"
      },
      {
        pagePath: "/pages/category/index/index",
        text: "分类",
        icon: "apps-o",
        selectedIcon: "apps"
      },
      {
        pagePath: "/pages/message/list/index",
        text: "消息",
        icon: "chat-o",
        selectedIcon: "chat",
        badge: true
      },
      {
        pagePath: "/pages/user/profile/index",
        text: "我的",
        icon: "user-o",
        selectedIcon: "user"
      }
    ]
  },

  attached() {
    this.updateUnreadCount()
    this.initWebSocketListener()
  },

  detached() {
    this.removeWebSocketListener()
  },

  methods: {
    switchTab(e) {
      const data = e.currentTarget.dataset
      const url = data.path
      
      wx.switchTab({ url })
    },

    // 更新未读数
    async updateUnreadCount() {
      try {
        const messageApi = require('../api/message')
        const res = await messageApi.getUnreadCount()
        const count = res.data || res || 0
        this.setData({ unreadCount: count })
        
        // 设置TabBar红点
        if (count > 0) {
          wx.showTabBarRedDot({ index: 2 })
        } else {
          wx.hideTabBarRedDot({ index: 2 })
        }
      } catch (err) {
        console.error('获取未读数失败:', err)
      }
    },

    // 初始化WebSocket监听
    initWebSocketListener() {
      const websocket = require('../utils/websocket')
      this.handleMessage = (data) => {
        console.log('TabBar收到消息:', data)
        if (data.type === 'message') {
          // 收到新消息，更新未读数
          console.log('更新TabBar未读数')
          this.updateUnreadCount()
        }
      }
      websocket.onMessage(this.handleMessage)
    },

    // 移除WebSocket监听
    removeWebSocketListener() {
      if (this.handleMessage) {
        const websocket = require('../utils/websocket')
        websocket.offMessage(this.handleMessage)
      }
    }
  }
})
