// pages/user/settings/index.js
const auth = require('../../../utils/auth')
const userApi = require('../../../api/user')
const storage = require('../../../utils/storage')

Page({
  data: {
    userInfo: {},
    settings: {
      messageNotify: true,
      commentNotify: true,
      likeNotify: true,
      favoriteNotify: true,
      publicProfile: true,
      showFollowing: true
    },
    cacheSize: '0 KB'
  },

  onLoad() {
    this.loadUserInfo()
    this.loadSettings()
    this.calculateCacheSize()
  },

  onShow() {
    this.loadUserInfo()
  },

  // 返回
  goBack() {
    wx.navigateBack()
  },

  // 加载用户信息
  async loadUserInfo() {
    if (!auth.isLoggedIn()) {
      return
    }

    try {
      const userInfo = await userApi.getUserInfo()
      this.setData({ userInfo })
    } catch (err) {
      console.error('加载用户信息失败:', err)
    }
  },

  // 加载设置
  loadSettings() {
    const settings = storage.get('userSettings') || this.data.settings
    this.setData({ settings })
  },

  // 保存设置
  saveSettings() {
    storage.set('userSettings', this.data.settings)
  },

  // 开关切换
  handleSwitchChange(e) {
    const key = e.currentTarget.dataset.key
    const value = e.detail

    this.setData({
      [`settings.${key}`]: value
    })

    this.saveSettings()

    wx.showToast({
      title: '设置已保存',
      icon: 'success',
      duration: 1500
    })
  },

  // 跳转个人资料
  goToProfile() {
    wx.navigateTo({
      url: '/pages/user/profile/index'
    })
  },

  // 修改密码
  handleChangePassword() {
    wx.showModal({
      title: '修改密码',
      content: '请前往网页版修改密码',
      showCancel: false
    })
  },

  // 清除缓存
  handleClearCache() {
    wx.showModal({
      title: '清除缓存',
      content: '确定要清除所有缓存吗？',
      success: (res) => {
        if (res.confirm) {
          try {
            wx.clearStorageSync()
            
            // 重新保存必要的数据
            if (auth.isLoggedIn()) {
              const token = auth.getToken()
              const userInfo = this.data.userInfo
              auth.setToken(token)
              storage.set('userInfo', userInfo)
            }

            this.setData({ cacheSize: '0 KB' })

            wx.showToast({
              title: '清除成功',
              icon: 'success'
            })
          } catch (err) {
            console.error('清除缓存失败:', err)
            wx.showToast({
              title: '清除失败',
              icon: 'none'
            })
          }
        }
      }
    })
  },

  // 计算缓存大小
  calculateCacheSize() {
    try {
      const info = wx.getStorageInfoSync()
      const sizeKB = info.currentSize
      
      let cacheSize = ''
      if (sizeKB < 1024) {
        cacheSize = sizeKB + ' KB'
      } else {
        cacheSize = (sizeKB / 1024).toFixed(2) + ' MB'
      }

      this.setData({ cacheSize })
    } catch (err) {
      console.error('计算缓存大小失败:', err)
    }
  },

  // 关于我们
  handleAbout() {
    wx.showModal({
      title: '关于我们',
      content: '博客小程序 v1.0.0\n\n一个简洁优雅的博客平台',
      showCancel: false
    })
  },

  // 退出登录
  handleLogout() {
    wx.showModal({
      title: '退出登录',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          auth.logout()
          
          wx.showToast({
            title: '已退出登录',
            icon: 'success'
          })

          setTimeout(() => {
            wx.reLaunch({
              url: '/pages/index/index'
            })
          }, 1500)
        }
      }
    })
  }
})
