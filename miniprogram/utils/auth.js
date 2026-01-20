// auth.js - 认证工具
const config = require('./config')

/**
 * 检查是否已登录
 */
function isLoggedIn() {
  const token = wx.getStorageSync(config.STORAGE_KEYS.TOKEN)
  return !!token
}

/**
 * 保存 token
 */
function setToken(token) {
  wx.setStorageSync(config.STORAGE_KEYS.TOKEN, token)
}

/**
 * 获取 token
 */
function getToken() {
  return wx.getStorageSync(config.STORAGE_KEYS.TOKEN)
}

/**
 * 清除 token
 */
function clearToken() {
  wx.removeStorageSync(config.STORAGE_KEYS.TOKEN)
}

/**
 * 保存用户信息
 */
function setUserInfo(userInfo) {
  wx.setStorageSync(config.STORAGE_KEYS.USER_INFO, userInfo)
  
  // 更新全局数据
  const app = getApp()
  if (app) {
    app.globalData.userInfo = userInfo
    app.globalData.isVip = userInfo.isVip
    app.globalData.vipLevel = userInfo.vipLevel
  }
}

/**
 * 获取用户信息
 */
function getUserInfo() {
  return wx.getStorageSync(config.STORAGE_KEYS.USER_INFO)
}

/**
 * 清除用户信息
 */
function clearUserInfo() {
  wx.removeStorageSync(config.STORAGE_KEYS.USER_INFO)
  
  // 清除全局数据
  const app = getApp()
  if (app) {
    app.globalData.userInfo = null
    app.globalData.isVip = false
    app.globalData.vipLevel = 0
  }
}

/**
 * 退出登录
 */
function logout() {
  clearToken()
  clearUserInfo()
  
  wx.showToast({
    title: '已退出登录',
    icon: 'success',
    duration: 2000
  })
  
  setTimeout(() => {
    wx.reLaunch({
      url: '/pages/index/index'
    })
  }, 2000)
}

/**
 * 清除所有认证信息（不显示提示）
 */
function clearAuth() {
  clearToken()
  clearUserInfo()
}

/**
 * 检查登录状态，未登录则跳转登录页
 */
function checkLogin() {
  if (!isLoggedIn()) {
    wx.showToast({
      title: '请先登录',
      icon: 'none',
      duration: 2000
    })
    
    setTimeout(() => {
      wx.navigateTo({
        url: '/pages/login/index/index'
      })
    }, 2000)
    
    return false
  }
  return true
}

module.exports = {
  isLoggedIn,
  setToken,
  getToken,
  clearToken,
  setUserInfo,
  getUserInfo,
  clearUserInfo,
  logout,
  clearAuth,
  checkLogin
}
