// config.js - 配置文件
const constants = require('./constants')
const env = require('./env')

module.exports = {
  // API 基础地址（根据环境自动切换）
  BASE_URL: env.BASE_URL,
  
  // WebSocket 地址
  WS_URL: env.WS_URL,
  
  // 请求超时时间
  TIMEOUT: 10000,
  
  // 图片上传地址
  UPLOAD_URL: env.BASE_URL.replace('/api', '') + '/api/upload/image',
  
  // 默认头像
  DEFAULT_AVATAR: constants.DEFAULT_AVATAR,
  
  // 分页配置
  PAGE_SIZE: 10,
  
  // 缓存键名
  STORAGE_KEYS: {
    TOKEN: 'token',
    USER_INFO: 'userInfo',
    THEME: 'theme'
  }
}
