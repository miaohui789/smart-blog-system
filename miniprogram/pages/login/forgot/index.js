// pages/login/forgot/index.js
const userApi = require('../../../api/user')

Page({
  data: {
    step: 1, // 当前步骤: 1-滑动验证, 2-邮箱验证, 3-重置密码
    email: '',
    code: '',
    password: '',
    confirmPassword: '',
    showPassword: false, // 默认隐藏密码
    showConfirmPassword: false, // 默认隐藏确认密码
    loading: false,
    codeSending: false,
    countdown: 0,
    timer: null,
    // 滑动验证相关
    sliderLeft: 0,
    sliderMoving: false,
    sliderSuccess: false
  },

  onLoad() {
    console.log('忘记密码页面加载，初始showPassword:', this.data.showPassword)
  },

  onUnload() {
    if (this.data.timer) {
      clearInterval(this.data.timer)
    }
  },

  goBack() {
    if (this.data.step > 1) {
      this.setData({ step: this.data.step - 1 })
    } else {
      wx.navigateBack()
    }
  },

  // 滑块验证相关
  onSliderStart(e) {
    if (this.data.sliderSuccess) return
    this.setData({ 
      sliderMoving: true,
      startX: e.touches[0].pageX
    })
  },

  onSliderMove(e) {
    if (!this.data.sliderMoving || this.data.sliderSuccess) return
    
    const moveX = e.touches[0].pageX - this.data.startX
    const maxMove = 310 // 滑块最大移动距离
    
    if (moveX >= 0 && moveX <= maxMove) {
      this.setData({ sliderLeft: moveX })
    }
  },

  onSliderEnd(e) {
    if (!this.data.sliderMoving || this.data.sliderSuccess) return
    
    const maxMove = 310
    const threshold = maxMove * 0.9 // 90%即可通过
    
    if (this.data.sliderLeft >= threshold) {
      // 验证成功
      this.setData({ 
        sliderLeft: maxMove,
        sliderSuccess: true,
        sliderMoving: false
      })
      
      wx.showToast({
        title: '验证成功',
        icon: 'success'
      })
      
      // 1秒后进入下一步
      setTimeout(() => {
        this.setData({ step: 2 })
      }, 1000)
    } else {
      // 验证失败，滑块回弹
      this.setData({ 
        sliderLeft: 0,
        sliderMoving: false
      })
      
      wx.showToast({
        title: '请滑动到最右侧',
        icon: 'none'
      })
    }
  },

  onEmailInput(e) {
    this.setData({ email: e.detail.value })
  },

  onCodeInput(e) {
    this.setData({ code: e.detail.value })
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
    userApi.sendResetCode(email).then(() => {
      console.log('发送重置密码验证码成功')
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

  // 验证验证码并进入下一步
  verifyCode() {
    const { email, code } = this.data

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

    // 直接进入下一步，验证码会在重置密码时验证
    this.setData({ step: 3 })
  },

  // 重置密码
  async resetPassword() {
    const { email, code, password, confirmPassword } = this.data

    if (!password) {
      wx.showToast({
        title: '请输入新密码',
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

    this.setData({ loading: true })

    try {
      console.log('重置密码参数:', { email, code, password: '***' })
      
      await userApi.resetPassword({
        email,
        code,
        password
      })

      wx.showToast({
        title: '密码重置成功',
        icon: 'success',
        duration: 2000
      })

      setTimeout(() => {
        wx.navigateBack()
      }, 2000)

    } catch (err) {
      console.error('重置密码失败:', err)
      const errorMsg = err.message || err.data?.message || '重置失败，请重试'
      wx.showToast({
        title: errorMsg,
        icon: 'none',
        duration: 2000
      })
      this.setData({ loading: false })
    }
  }
})
