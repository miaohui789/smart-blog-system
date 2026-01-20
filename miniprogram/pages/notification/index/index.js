// pages/notification/index/index.js
const auth = require('../../../utils/auth')
const notificationApi = require('../../../api/notification')
const formatUtil = require('../../../utils/format')

Page({
  data: {
    notifications: [],
    unreadCount: 0,
    loading: false,
    page: 1,
    pageSize: 20,
    hasMore: true
  },

  onLoad() {
    if (!auth.isLoggedIn()) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      })
      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
      return
    }
    this.loadData()
    this.loadUnreadCount()
  },

  onPullDownRefresh() {
    this.setData({
      page: 1,
      notifications: [],
      hasMore: true
    })
    this.loadData()
    this.loadUnreadCount()
    wx.stopPullDownRefresh()
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.setData({ page: this.data.page + 1 })
      this.loadData()
    }
  },

  // 返回
  goBack() {
    wx.navigateBack()
  },

  // 加载未读数量
  async loadUnreadCount() {
    try {
      const res = await notificationApi.getUnreadCount()
      this.setData({ unreadCount: res.count || res || 0 })
    } catch (err) {
      console.error('加载未读数量失败:', err)
    }
  },

  // 加载数据
  async loadData() {
    this.setData({ loading: true })

    try {
      const res = await notificationApi.getList({
        page: this.data.page,
        pageSize: this.data.pageSize
      })

      let records = []
      if (Array.isArray(res)) {
        records = res
      } else if (res.records) {
        records = res.records
      } else if (res.data) {
        records = Array.isArray(res.data) ? res.data : res.data.records || []
      }

      // 格式化时间
      records = records.map(item => ({
        ...item,
        createTime: formatUtil.formatRelativeTime(item.createTime)
      }))

      this.setData({
        notifications: this.data.page === 1 ? records : [...this.data.notifications, ...records],
        hasMore: records.length === this.data.pageSize
      })
    } catch (err) {
      console.error('加载通知失败:', err)
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  // 标记已读
  async handleMarkRead(e) {
    const id = e.currentTarget.dataset.id
    const isRead = e.currentTarget.dataset.read

    if (isRead) return

    try {
      await notificationApi.markRead(id)
      
      const notifications = this.data.notifications.map(item => {
        if (item.id === id) {
          return { ...item, isRead: true }
        }
        return item
      })

      this.setData({
        notifications,
        unreadCount: Math.max(0, this.data.unreadCount - 1)
      })
    } catch (err) {
      console.error('标记已读失败:', err)
    }
  },

  // 全部已读
  async handleMarkAllRead() {
    if (this.data.unreadCount === 0) {
      wx.showToast({
        title: '没有未读通知',
        icon: 'none'
      })
      return
    }

    try {
      await notificationApi.markAllRead()
      
      const notifications = this.data.notifications.map(item => ({
        ...item,
        isRead: true
      }))

      this.setData({
        notifications,
        unreadCount: 0
      })

      wx.showToast({
        title: '已全部标记为已读',
        icon: 'success'
      })
    } catch (err) {
      console.error('标记全部已读失败:', err)
      wx.showToast({
        title: '操作失败',
        icon: 'none'
      })
    }
  },

  // 删除通知
  async handleDelete(e) {
    const id = e.currentTarget.dataset.id

    wx.showModal({
      title: '提示',
      content: '确定要删除这条通知吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await notificationApi.remove(id)
            
            const notifications = this.data.notifications.filter(item => item.id !== id)
            this.setData({ notifications })

            wx.showToast({
              title: '已删除',
              icon: 'success'
            })
          } catch (err) {
            console.error('删除失败:', err)
            wx.showToast({
              title: '删除失败',
              icon: 'none'
            })
          }
        }
      }
    })
  },

  // 清空通知
  handleClear() {
    if (this.data.notifications.length === 0) {
      wx.showToast({
        title: '没有通知',
        icon: 'none'
      })
      return
    }

    wx.showModal({
      title: '提示',
      content: '确定要清空所有通知吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await notificationApi.clear()
            
            this.setData({
              notifications: [],
              unreadCount: 0
            })

            wx.showToast({
              title: '已清空',
              icon: 'success'
            })
          } catch (err) {
            console.error('清空失败:', err)
            wx.showToast({
              title: '清空失败',
              icon: 'none'
            })
          }
        }
      }
    })
  },

  // 点击通知
  handleNotificationClick(e) {
    const item = e.currentTarget.dataset.item
    
    // 标记已读
    if (!item.isRead) {
      this.handleMarkRead(e)
    }

    // 根据类型跳转
    if (item.type === 'COMMENT' && item.articleId) {
      wx.navigateTo({
        url: `/pages/article/detail/index?id=${item.articleId}`
      })
    } else if (item.type === 'LIKE' && item.articleId) {
      wx.navigateTo({
        url: `/pages/article/detail/index?id=${item.articleId}`
      })
    } else if (item.type === 'FOLLOW' && item.userId) {
      wx.navigateTo({
        url: `/pages/user/detail/index?id=${item.userId}`
      })
    }
  }
})