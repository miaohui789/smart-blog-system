// storage.js - 本地存储工具

/**
 * 设置存储
 */
function set(key, value) {
  try {
    wx.setStorageSync(key, value)
    return true
  } catch (err) {
    console.error('存储失败:', err)
    return false
  }
}

/**
 * 获取存储
 */
function get(key, defaultValue = null) {
  try {
    const value = wx.getStorageSync(key)
    return value || defaultValue
  } catch (err) {
    console.error('读取存储失败:', err)
    return defaultValue
  }
}

/**
 * 删除存储
 */
function remove(key) {
  try {
    wx.removeStorageSync(key)
    return true
  } catch (err) {
    console.error('删除存储失败:', err)
    return false
  }
}

/**
 * 清空所有存储
 */
function clear() {
  try {
    wx.clearStorageSync()
    return true
  } catch (err) {
    console.error('清空存储失败:', err)
    return false
  }
}

/**
 * 获取存储信息
 */
function getInfo() {
  try {
    return wx.getStorageInfoSync()
  } catch (err) {
    console.error('获取存储信息失败:', err)
    return null
  }
}

module.exports = {
  set,
  get,
  remove,
  clear,
  getInfo
}
