// request.js - 网络请求封装
const config = require('./config')

let loadingCount = 0 // loading计数器

function showLoading(text) {
  if (loadingCount === 0) {
    wx.showLoading({
      title: text || '加载中...',
      mask: true
    })
  }
  loadingCount++
}

function hideLoading() {
  loadingCount--
  if (loadingCount <= 0) {
    loadingCount = 0
    wx.hideLoading()
  }
}

/**
 * 发起网络请求
 * @param {Object} options 请求配置
 */
function request(options) {
  return new Promise((resolve, reject) => {
    // 获取 token
    const token = wx.getStorageSync(config.STORAGE_KEYS.TOKEN)
    
    // 打印请求信息（调试用）
    console.log('=== 请求详情 ===')
    console.log('URL:', config.BASE_URL + options.url)
    console.log('Method:', options.method || 'GET')
    console.log('Data:', options.data)
    console.log('===============')
    
    // 显示加载提示
    if (options.loading !== false) {
      showLoading(options.loadingText)
    }

    wx.request({
      url: config.BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : '',
        ...options.header
      },
      timeout: options.timeout || config.TIMEOUT,
      success: (res) => {
        if (options.loading !== false) {
          hideLoading()
        }

        // 请求成功
        if (res.statusCode === 200) {
          if (res.data.code === 200) {
            resolve(res.data.data)
          } else {
            // 业务错误
            const errorMsg = res.data.message || '请求失败'
            console.error('业务错误:', {
              code: res.data.code,
              message: errorMsg,
              url: options.url
            })
            
            wx.showToast({
              title: errorMsg,
              icon: 'none',
              duration: 2000
            })
            reject(res.data)
          }
        } 
        // 未登录或 token 过期
        else if (res.statusCode === 401) {
          wx.removeStorageSync(config.STORAGE_KEYS.TOKEN)
          wx.removeStorageSync(config.STORAGE_KEYS.USER_INFO)
          
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
          
          reject(res)
        }
        // 其他错误
        else {
          console.error('HTTP错误:', {
            statusCode: res.statusCode,
            url: options.url,
            data: res.data
          })
          
          wx.showToast({
            title: `网络错误 (${res.statusCode})`,
            icon: 'none',
            duration: 2000
          })
          reject(res)
        }
      },
      fail: (err) => {
        if (options.loading !== false) {
          hideLoading()
        }
        
        console.error('网络请求失败:', {
          url: options.url,
          error: err
        })
        
        // 判断错误类型
        let errorMsg = '网络请求失败'
        if (err.errMsg) {
          if (err.errMsg.includes('timeout')) {
            errorMsg = '请求超时，请检查网络'
          } else if (err.errMsg.includes('fail')) {
            errorMsg = '网络连接失败'
          } else if (err.errMsg.includes('domain list')) {
            errorMsg = '请在开发者工具中关闭域名校验'
          }
        }
        
        wx.showToast({
          title: errorMsg,
          icon: 'none',
          duration: 3000
        })
        
        reject(err)
      }
    })
  })
}

module.exports = {
  get: (url, data, options = {}) => {
    return request({ url, data, method: 'GET', ...options })
  },
  
  post: (url, data, options = {}) => {
    return request({ url, data, method: 'POST', ...options })
  },
  
  put: (url, data, options = {}) => {
    return request({ url, data, method: 'PUT', ...options })
  },
  
  delete: (url, data, options = {}) => {
    return request({ url, data, method: 'DELETE', ...options })
  }
}
