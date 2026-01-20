// env.js - 环境配置

// 判断是否为开发环境
const isDev = typeof __wxConfig !== 'undefined' && __wxConfig.envVersion === 'develop'

// 开发环境配置
const devConfig = {
  BASE_URL: 'http://localhost:8080/api',
  WS_URL: 'ws://localhost:8080/ws'
}

// 生产环境配置
const prodConfig = {
  BASE_URL: 'https://your-production-domain.com/api',
  WS_URL: 'wss://your-production-domain.com/ws'
}

// 导出当前环境配置
module.exports = isDev ? devConfig : prodConfig
