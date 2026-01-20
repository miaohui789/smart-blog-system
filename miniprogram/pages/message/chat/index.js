// pages/message/chat/index.js
const auth = require('../../../utils/auth')
const messageApi = require('../../../api/message')
const { formatMessageTime } = require('../../../utils/format')
const websocket = require('../../../utils/websocket')

Page({
  data: {
    conversationId: '',
    targetUserId: '',  // 添加目标用户ID
    targetUser: null,
    userInfo: null,
    messages: [],
    inputText: '',
    scrollToView: '',
    page: 1,
    hasMore: true,
    loadingMore: false,
    showMenu: false,
    menuActions: [],
    currentMessage: null
  },

  onLoad(options) {
    const { userId, conversationId } = options
    
    console.log('聊天页面参数:', options)
    
    // 至少需要userId或conversationId之一
    if (!userId && !conversationId) {
      wx.showToast({
        title: '参数错误',
        icon: 'none'
      })
      setTimeout(() => wx.navigateBack(), 1500)
      return
    }

    this.setData({
      conversationId: conversationId || '',
      targetUserId: userId || '',  // 保存目标用户ID
      userInfo: auth.getUserInfo()
    })

    // 如果有userId，加载对方用户信息
    if (userId) {
      this.loadTargetUser(userId)
    }
    
    // 如果有conversationId，加载消息
    if (conversationId) {
      this.loadMessages()
    } else if (userId) {
      // 如果只有userId，尝试创建或获取会话
      this.createOrGetConversation(userId)
    }
    
    this.initWebSocket()
  },
  
  // 创建或获取会话
  async createOrGetConversation(userId) {
    try {
      console.log('获取与用户的会话，目标用户ID:', userId)
      // 通过userId获取会话
      const res = await messageApi.getConversationWith(userId)
      console.log('会话获取结果:', res)
      
      const conversationId = res.conversationId || res.id || res.data?.conversationId || res.data?.id
      
      if (conversationId) {
        this.setData({ conversationId })
        console.log('会话ID已设置:', conversationId)
        // 加载消息
        this.loadMessages()
      } else {
        console.log('未获取到conversationId，将在发送消息时创建')
        // 即使没有conversationId，也允许继续，发送消息时会创建
      }
    } catch (err) {
      console.error('获取会话失败:', err)
      // 即使失败也允许继续，conversationId会在发送时创建
      console.log('允许继续，发送消息时会创建会话')
    }
  },

  // 加载对方用户信息
  async loadTargetUser(userId) {
    try {
      const userApi = require('../../../api/user')
      const res = await userApi.getUserProfile(userId)
      const targetUser = res.data || res
      
      // 修复头像路径
      const baseUrl = getApp().globalData.baseUrl
      if (targetUser.avatar && !targetUser.avatar.startsWith('http')) {
        targetUser.avatar = baseUrl + targetUser.avatar
      }
      
      this.setData({ targetUser })
      
      // 设置导航栏标题
      wx.setNavigationBarTitle({
        title: targetUser.nickname || '聊天'
      })
    } catch (err) {
      console.error('加载用户信息失败:', err)
    }
  },

  // 加载消息列表
  async loadMessages() {
    if (this.data.loadingMore) return
    
    this.setData({ loadingMore: true })
    
    try {
      const res = await messageApi.getMessages(this.data.conversationId, {
        page: this.data.page,
        pageSize: 20
      })
      
      let newMessages = (res.data || res || []).reverse()
      
      // 处理时间显示
      newMessages = this.processMessageTime(newMessages)
      
      // 标记自己发送的消息并修复头像路径
      const baseUrl = getApp().globalData.baseUrl
      newMessages.forEach(msg => {
        msg.isSelf = msg.senderId === this.data.userInfo.id
        // 修复发送者头像
        if (msg.sender && msg.sender.avatar && !msg.sender.avatar.startsWith('http')) {
          msg.sender.avatar = baseUrl + msg.sender.avatar
        }
      })
      
      // 修复当前用户头像
      if (this.data.userInfo.avatar && !this.data.userInfo.avatar.startsWith('http')) {
        const userInfo = { ...this.data.userInfo }
        userInfo.avatar = baseUrl + userInfo.avatar
        this.setData({ userInfo })
      }
      
      this.setData({
        messages: [...newMessages, ...this.data.messages],
        page: this.data.page + 1,
        hasMore: newMessages.length === 20
      })
      
      // 首次加载滚动到底部
      if (this.data.page === 2) {
        this.scrollToBottom()
      }
    } catch (err) {
      console.error('加载消息失败:', err)
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      })
    } finally {
      this.setData({ loadingMore: false })
    }
  },

  // 加载更多
  loadMore() {
    if (this.data.hasMore && !this.data.loadingMore) {
      this.loadMessages()
    }
  },

  // 处理消息时间显示
  processMessageTime(messages) {
    return messages.map((msg, index) => {
      // 第一条消息显示时间
      if (index === 0) {
        msg.showTime = true
        msg.timeText = formatMessageTime(msg.createTime)
      } else {
        // 间隔超过5分钟显示时间
        const prevTime = new Date(messages[index - 1].createTime).getTime()
        const currTime = new Date(msg.createTime).getTime()
        if (currTime - prevTime > 5 * 60 * 1000) {
          msg.showTime = true
          msg.timeText = formatMessageTime(msg.createTime)
        }
      }
      return msg
    })
  },

  // 输入框变化
  onInputChange(e) {
    this.setData({ inputText: e.detail.value })
  },

  // 发送消息
  async sendMessage() {
    const content = this.data.inputText.trim()
    
    if (!content) {
      return
    }
    
    // 获取接收者ID
    const receiverId = this.data.targetUser?.id || this.data.targetUserId
    
    if (!receiverId) {
      wx.showToast({
        title: '接收者信息错误',
        icon: 'none'
      })
      return
    }
    
    try {
      const newMessage = await messageApi.sendMessage({
        receiverId: receiverId,
        content,
        type: 1
      })
      
      console.log('消息发送成功:', newMessage)
      
      // 如果之前没有conversationId，现在应该有了
      if (!this.data.conversationId && newMessage.conversationId) {
        this.setData({ conversationId: newMessage.conversationId })
        console.log('会话ID已更新:', newMessage.conversationId)
      }
      
      // 添加到消息列表
      const msg = {
        ...newMessage,
        isSelf: true,
        sender: this.data.userInfo
      }
      
      this.setData({
        messages: [...this.data.messages, msg],
        inputText: ''
      })
      
      this.scrollToBottom()
    } catch (err) {
      console.error('发送消息失败:', err)
      wx.showToast({
        title: err.message || '发送失败',
        icon: 'none'
      })
    }
  },

  // 滚动到底部
  scrollToBottom() {
    if (this.data.messages.length > 0) {
      const lastMsg = this.data.messages[this.data.messages.length - 1]
      this.setData({
        scrollToView: `msg-${lastMsg.id}`
      })
    }
  },

  // 长按显示菜单
  showMessageMenu(e) {
    const message = e.currentTarget.dataset.message
    
    if (message.isWithdrawn) return
    
    const actions = []
    
    // 自己的消息可以撤回
    if (message.isSelf) {
      actions.push({ name: '撤回', value: 'withdraw' })
    }
    
    actions.push({ name: '删除', value: 'delete' })
    
    this.setData({
      showMenu: true,
      menuActions: actions,
      currentMessage: message
    })
  },

  // 菜单选择
  async onMenuSelect(e) {
    const action = e.detail.value
    const message = this.data.currentMessage
    
    this.closeMenu()
    
    if (action === 'withdraw') {
      await this.withdrawMessage(message)
    } else if (action === 'delete') {
      await this.deleteMessage(message)
    }
  },

  // 撤回消息
  async withdrawMessage(message) {
    try {
      await messageApi.withdrawMessage(message.id)
      
      // 更新消息状态
      const messages = this.data.messages.map(msg => {
        if (msg.id === message.id) {
          return { ...msg, isWithdrawn: true, content: '' }
        }
        return msg
      })
      
      this.setData({ messages })
      
      wx.showToast({
        title: '已撤回',
        icon: 'success'
      })
    } catch (err) {
      wx.showToast({
        title: '撤回失败',
        icon: 'none'
      })
    }
  },

  // 删除消息
  async deleteMessage(message) {
    try {
      await messageApi.deleteMessage(message.id)
      
      const messages = this.data.messages.filter(msg => msg.id !== message.id)
      this.setData({ messages })
      
      wx.showToast({
        title: '已删除',
        icon: 'success'
      })
    } catch (err) {
      wx.showToast({
        title: '删除失败',
        icon: 'none'
      })
    }
  },

  // 关闭菜单
  closeMenu() {
    this.setData({
      showMenu: false,
      currentMessage: null
    })
  },

  // 初始化WebSocket
  initWebSocket() {
    console.log('========== 聊天页面初始化WebSocket ==========');
    console.log('当前conversationId:', this.data.conversationId);
    console.log('当前userInfo:', this.data.userInfo);
    console.log('对方用户:', this.data.targetUser);
    
    // 连接WebSocket
    websocket.connect('user')
    
    // 监听消息
    this.handleWebSocketMessage = (data) => {
      console.log('========== 聊天页面收到WebSocket消息 ==========');
      console.log('消息类型:', data.type);
      console.log('消息数据:', JSON.stringify(data, null, 2));
      
      if (data.type === 'message') {
        // 新消息
        const msg = data.data
        console.log('收到新消息:', msg);
        console.log('消息会话ID:', msg.conversationId, '类型:', typeof msg.conversationId);
        console.log('当前会话ID:', this.data.conversationId, '类型:', typeof this.data.conversationId);
        
        // 类型转换后再比较
        const msgConvId = Number(msg.conversationId)
        const currentConvId = Number(this.data.conversationId)
        console.log('转换后比较:', msgConvId, '===', currentConvId, '=', msgConvId === currentConvId);
        
        // 只处理当前会话的消息
        if (msgConvId === currentConvId) {
          console.log('✅ 会话ID匹配,处理消息');
          const baseUrl = getApp().globalData.baseUrl
          
          // 添加显示属性
          msg.isSelf = msg.senderId === this.data.userInfo.id
          console.log('消息是否自己发送:', msg.isSelf);
          
          // 修复头像 - 处理sender对象
          if (!msg.sender) {
            console.log('⚠️ 消息没有sender信息,需要补充');
            // 如果没有sender，从当前用户和对方用户中获取
            if (msg.isSelf) {
              msg.sender = {
                id: this.data.userInfo.id,
                nickname: this.data.userInfo.nickname,
                avatar: this.data.userInfo.avatar
              }
            } else {
              msg.sender = {
                id: this.data.targetUser.id,
                nickname: this.data.targetUser.nickname,
                avatar: this.data.targetUser.avatar
              }
            }
            console.log('补充sender信息:', msg.sender);
          } else {
            console.log('✅ 消息已有sender信息:', msg.sender);
          }
          
          // 修复头像路径
          if (msg.sender && msg.sender.avatar && !msg.sender.avatar.startsWith('http')) {
            msg.sender.avatar = baseUrl + msg.sender.avatar
            console.log('修复后的头像路径:', msg.sender.avatar);
          }
          
          // 添加到消息列表
          const messages = [...this.data.messages, msg]
          console.log('更新消息列表,新长度:', messages.length);
          this.setData({ messages })
          this.scrollToBottom()
          console.log('✅ 消息已添加并滚动到底部');
        } else {
          console.log('❌ 会话ID不匹配,忽略消息');
        }
      } else if (data.type === 'message_withdraw') {
        // 消息撤回
        const messageId = data.messageId
        console.log('收到撤回通知, messageId:', messageId);
        
        const messages = this.data.messages.map(msg => {
          if (msg.id === messageId) {
            console.log('✅ 找到撤回的消息,标记为已撤回');
            return { ...msg, isWithdrawn: true, content: '' }
          }
          return msg
        })
        this.setData({ messages })
      } else {
        console.log('⚠️ 未知消息类型:', data.type);
      }
    }
    
    console.log('注册WebSocket监听器');
    websocket.onMessage(this.handleWebSocketMessage)
    console.log('========== WebSocket初始化完成 ==========');
  },

  onShow() {
    // 标记已读
    if (this.data.conversationId) {
      this.markAsRead()
    }
  },

  // 标记已读
  async markAsRead() {
    // 检查conversationId是否存在
    if (!this.data.conversationId) {
      console.log('conversationId为空，跳过标记已读')
      return
    }
    
    try {
      await messageApi.markRead(this.data.conversationId)
      // 更新TabBar红点
      if (typeof this.getTabBar === 'function' && this.getTabBar()) {
        this.getTabBar().updateUnreadCount()
      }
    } catch (err) {
      console.error('标记已读失败:', err)
    }
  },

  // 页面卸载
  onUnload() {
    // 移除WebSocket监听
    if (this.handleWebSocketMessage) {
      websocket.offMessage(this.handleWebSocketMessage)
    }
    // 标记已读（只在有conversationId时执行）
    if (this.data.conversationId) {
      this.markAsRead()
    }
  }
})