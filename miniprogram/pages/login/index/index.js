// pages/login/index/index.js
const userApi = require('../../../api/user')
const auth = require('../../../utils/auth')

Page({
  data: {
    username: '',
    password: '',
    showPassword: false, // 默认隐藏密码
    loading: false
  },

  onLoad() {
    console.log('登录页面加载，初始showPassword:', this.data.showPassword)
  },

  // 返回
  goBack() {
    wx.navigateBack({
      fail: () => {
        wx.switchTab({
          url: '/pages/index/index'
        })
      }
    })
  },

  // 输入用户名
  onUsernameInput(e) {
    this.setData({ username: e.detail.value })
  },

  // 输入密码
  onPasswordInput(e) {
    this.setData({ password: e.detail.value })
  },

  // 切换密码显示
  togglePassword() {
    const newValue = !this.data.showPassword
    console.log('切换密码显示:', newValue)
    this.setData({ showPassword: newValue })
  },

  // 账号密码登录
  async handleLogin() {
    const { username, password } = this.data

    // 验证
    if (!username) {
      wx.showToast({
        title: '请输入用户名',
        icon: 'none'
      })
      return
    }

    if (!password) {
      wx.showToast({
        title: '请输入密码',
        icon: 'none'
      })
      return
    }

    this.setData({ loading: true })

    try {
      // 调用登录接口
      console.log('登录参数:', { username, password: '***' })
      const res = await userApi.login({ username, password })
      
      // 保存token
      auth.setToken(res.token)
      
      // 获取用户信息
      const userInfo = await userApi.getUserInfo()
      auth.setUserInfo(userInfo)

      wx.showToast({
        title: '登录成功',
        icon: 'success'
      })

      // 跳转
      setTimeout(() => {
        const pages = getCurrentPages()
        if (pages.length > 1) {
          wx.navigateBack()
        } else {
          wx.switchTab({
            url: '/pages/index/index'
          })
        }
      }, 1500)

    } catch (err) {
      console.error('登录失败:', err)
      wx.showToast({
        title: err.message || '登录失败',
        icon: 'none'
      })
      this.setData({ loading: false })
    }
  },

  // 微信登录
  async handleWxLogin() {
    this.setData({ loading: true })

    try {
      const loginRes = await wx.login()
      
      if (!loginRes.code) {
        throw new Error('获取登录凭证失败')
      }

      const token = await userApi.wxLogin(loginRes.code)
      auth.setToken(token)
      
      const userInfo = await userApi.getUserInfo()
      auth.setUserInfo(userInfo)

      wx.showToast({
        title: '登录成功',
        icon: 'success'
      })

      setTimeout(() => {
        const pages = getCurrentPages()
        if (pages.length > 1) {
          wx.navigateBack()
        } else {
          wx.switchTab({
            url: '/pages/index/index'
          })
        }
      }, 1500)

    } catch (err) {
      console.error('微信登录失败:', err)
      wx.showToast({
        title: err.message || '登录失败',
        icon: 'none'
      })
      this.setData({ loading: false })
    }
  },

  // 跳转到忘记密码
  goToForgotPassword() {
    console.log('点击了忘记密码')
    wx.navigateTo({
      url: '/pages/login/forgot/index'
    })
  },

  // 跳转到注册
  goToRegister() {
    console.log('点击了立即注册')
    wx.navigateTo({
      url: '/pages/login/register/index'
    })
  }
})
