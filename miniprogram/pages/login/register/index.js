// pages/login/register/index.js
const userApi = require('../../../api/user')

Page({
  data: {
    email: '',
    code: '',
    username: '',
    password: '',
    confirmPassword: '',
    showPassword: false, // 默认隐藏密码
    showConfirmPassword: false, // 默认隐藏确认密码
    agreed: false,
    loading: false,
    codeSending: false,
    countdown: 0,
    timer: null
  },

  onLoad() {
    console.log('注册页面加载，初始showPassword:', this.data.showPassword)
  },

  onUnload() {
    if (this.data.timer) {
      clearInterval(this.data.timer)
    }
  },

  goBack() {
    wx.navigateBack()
  },

  onEmailInput(e) {
    this.setData({ email: e.detail.value })
  },

  onCodeInput(e) {
    this.setData({ code: e.detail.value })
  },

  onUsernameInput(e) {
    this.setData({ username: e.detail.value })
  },

  onPasswordInput(e) {
    this.setData({ password: e.detail.value })
  },

  onConfirmPasswordInput(e) {
    this.setData({ confirmPassword: e.detail.value })
  },

  togglePassword() {
    const newValue = !this.data.showPassword
    console.log('切换密码显示:', newValue)
    this.setData({ showPassword: newValue })
  },

  toggleConfirmPassword() {
    const newValue = !this.data.showConfirmPassword
    console.log('切换确认密码显示:', newValue)
    this.setData({ showConfirmPassword: newValue })
  },

  onAgreeChange(e) {
    this.setData({ agreed: e.detail })
  },

  // 发送验证码
  async sendCode() {
    const { email, countdown } = this.data

    // 如果正在倒计时，不允许再次点击
    if (countdown > 0) {
      return
    }

    if (!email) {
      wx.showToast({
        title: '请输入邮箱地址',
        icon: 'none'
      })
      return
    }

    const emailReg = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailReg.test(email)) {
      wx.showToast({
        title: '邮箱格式不正确',
        icon: 'none'
      })
      return
    }

    // 立即开始倒计时
    this.startCountdown()

    // 显示成功提示
    wx.showToast({
      title: '获取成功，请等待邮箱通知',
      icon: 'success',
      duration: 2000
    })

    // 后台异步发送验证码（不阻塞UI）
    userApi.sendEmailCode(email).then(() => {
      console.log('发送注册验证码成功')
      // 显示发送成功提示
      wx.showToast({
        title: '验证码已发送到邮箱',
        icon: 'success',
        duration: 2000
      })
    }).catch(err => {
      console.error('发送验证码失败:', err)
      // 静默处理，不影响用户体验
    })
  },

  startCountdown() {
    this.setData({ countdown: 90 })

    const timer = setInterval(() => {
      const countdown = this.data.countdown - 1
      if (countdown <= 0) {
        clearInterval(timer)
        this.setData({ countdown: 0, timer: null })
      } else {
        this.setData({ countdown })
      }
    }, 1000)

    this.setData({ timer })
  },

  // 注册
  async handleRegister() {
    const { email, code, username, password, confirmPassword, agreed } = this.data

    // 验证邮箱
    if (!email) {
      wx.showToast({
        title: '请输入邮箱地址',
        icon: 'none'
      })
      return
    }

    const emailReg = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailReg.test(email)) {
      wx.showToast({
        title: '邮箱格式不正确',
        icon: 'none'
      })
      return
    }

    // 验证验证码
    if (!code) {
      wx.showToast({
        title: '请输入验证码',
        icon: 'none'
      })
      return
    }

    if (code.length !== 6) {
      wx.showToast({
        title: '验证码为6位数字',
        icon: 'none'
      })
      return
    }

    // 验证用户名
    if (!username) {
      wx.showToast({
        title: '请输入用户名',
        icon: 'none'
      })
      return
    }

    if (username.length < 3 || username.length > 20) {
      wx.showToast({
        title: '用户名长度为3-20个字符',
        icon: 'none'
      })
      return
    }

    // 验证密码
    if (!password) {
      wx.showToast({
        title: '请输入密码',
        icon: 'none'
      })
      return
    }

    if (password.length < 6 || password.length > 20) {
      wx.showToast({
        title: '密码长度为6-20个字符',
        icon: 'none'
      })
      return
    }

    if (password !== confirmPassword) {
      wx.showToast({
        title: '两次密码不一致',
        icon: 'none'
      })
      return
    }

    // 验证协议
    if (!agreed) {
      wx.showToast({
        title: '请阅读并同意用户协议',
        icon: 'none'
      })
      return
    }

    this.setData({ loading: true })

    try {
      console.log('注册参数:', { email, code, username, password: '***' })
      
      await userApi.register({
        email,
        code,
        username,
        password
      })

      wx.showToast({
        title: '注册成功',
        icon: 'success',
        duration: 2000
      })

      setTimeout(() => {
        wx.navigateBack()
      }, 2000)

    } catch (err) {
      console.error('注册失败:', err)
      const errorMsg = err.message || err.data?.message || '注册失败，请重试'
      wx.showToast({
        title: errorMsg,
        icon: 'none',
        duration: 2000
      })
      this.setData({ loading: false })
    }
  },

  goToLogin() {
    wx.navigateBack()
  }
})
