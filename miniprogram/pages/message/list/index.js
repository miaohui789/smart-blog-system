// pages/message/list/index.js
const auth = require('../../../utils/auth')
const messageApi = require('../../../api/message')
const { formatRelativeTime } = require('../../../utils/format')
const websocket = require('../../../utils/websocket')

Page({
  data: {
    loading: false,
    searchKeyword: '',
    conversations: [],
    allConversations: []
  },

  onLoad() {
    this.checkLogin()
  },

  onShow() {
    if (auth.isLoggedIn()) {
      this.loadConversations()
      this.initWebSocket()
      this.updateTabBarBadge()
    }
    
    // 设置 tabBar 选中状态
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({
        selected: 2
      })
    }
  },

  // 检查登录状态
  checkLogin() {
    if (!auth.isLoggedIn()) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      })
      setTimeout(() => {
        wx.navigateTo({
          url: '/pages/login/index/index'
        })
      }, 1500)
      return false
    }
    return true
  },

  // 加载会话列表
  async loadConversations() {
    this.setData({ loading: true })
    
    try {
      const res = await messageApi.getConversations()
      const conversations = res.data || res || []
      
      // 处理时间格式和头像路径
      const baseUrl = getApp().globalData.baseUrl
      conversations.forEach(conv => {
        if (conv.lastMessageTime) {
          conv.lastMessageTime = formatRelativeTime(conv.lastMessageTime)
        }
        // 修复头像路径
        if (conv.targetUser && conv.targetUser.avatar) {
          if (!conv.targetUser.avatar.startsWith('http')) {
            conv.targetUser.avatar = baseUrl + conv.targetUser.avatar
          }
        }
        // 模拟在线状态（实际应该从后端获取）
        // 可以根据最后消息时间判断，或者通过WebSocket实时更新
        if (conv.targetUser) {
          // 这里可以添加在线状态逻辑
          // conv.targetUser.isOnline = true/false
        }
      })
      
      this.setData({
        conversations,
        allConversations: conversations
      })
    } catch (err) {
      console.error('加载会话列表失败:', err)
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  // 搜索
  onSearchChange(e) {
    const keyword = e.detail.trim().toLowerCase()
    this.setData({ searchKeyword: keyword })
    
    if (!keyword) {
      this.setData({ conversations: this.data.allConversations })
      return
    }
    
    const filtered = this.data.allConversations.filter(conv => {
      return conv.targetUser.nickname.toLowerCase().includes(keyword)
    })
    
    this.setData({ conversations: filtered })
  },

  // 打开聊天窗口
  openChat(e) {
    const conversation = e.currentTarget.dataset.conversation
    wx.navigateTo({
      url: `/pages/message/chat/index?userId=${conversation.targetUser.id}&conversationId=${conversation.id}`
    })
  },

  // 初始化WebSocket
  initWebSocket() {
    console.log('========== 会话列表初始化WebSocket监听 ==========');
    console.log('当前会话数量:', this.data.conversations.length);
    
    // 监听新消息，更新会话列表
    this.handleWebSocketMessage = (data) => {
      console.log('========== 会话列表收到WebSocket消息 ==========');
      console.log('消息类型:', data.type);
      console.log('消息数据:', JSON.stringify(data, null, 2));
      
      if (data.type === 'message') {
        const msg = data.data
        console.log('新消息conversationId:', msg.conversationId);
        console.log('新消息内容:', msg.content);
        
        // 更新会话列表
        let found = false
        const conversations = this.data.conversations.map(conv => {
          if (conv.id === msg.conversationId) {
            found = true
            console.log('✅ 找到对应会话,更新会话列表');
            return {
              ...conv,
              lastMessage: msg.content,
              lastMessageTime: formatRelativeTime(msg.createTime),
              unreadCount: (conv.unreadCount || 0) + 1
            }
          }
          return conv
        })
        
        console.log('会话是否找到:', found);
        
        // 如果是新会话，重新加载列表
        if (!found) {
          console.log('❌ 未找到会话,重新加载列表');
          this.loadConversations()
        } else {
          console.log('✅ 更新会话列表和红点');
          this.setData({ conversations, allConversations: conversations })
          this.updateTabBarBadge()
        }
      } else {
        console.log('⚠️ 其他消息类型:', data.type);
      }
    }
    
    console.log('注册会话列表WebSocket监听器');
    websocket.onMessage(this.handleWebSocketMessage)
    console.log('========== 会话列表WebSocket初始化完成 ==========');
  },

  // 页面隐藏
  onHide() {
    // 移除WebSocket监听
    if (this.handleWebSocketMessage) {
      websocket.offMessage(this.handleWebSocketMessage)
    }
  },

  // 更新TabBar红点
  updateTabBarBadge() {
    const totalUnread = this.data.conversations.reduce((sum, conv) => {
      return sum + (conv.unreadCount || 0)
    }, 0)
    
    if (totalUnread > 0) {
      wx.showTabBarRedDot({ index: 2 })
    } else {
      wx.hideTabBarRedDot({ index: 2 })
    }
    
    // 更新自定义TabBar
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().updateUnreadCount()
    }
  }
})
