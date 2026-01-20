// vip.js - VIP 相关接口
const request = require('../utils/request')

/**
 * 激活 VIP
 */
function activate(keyCode) {
  return request.post('/vip/activate', { keyCode })
}

/**
 * 获取 VIP 信息
 */
function getInfo() {
  return request.get('/vip/info')
}

/**
 * 获取 VIP 权益说明
 */
function getPrivileges() {
  return request.get('/vip/privileges')
}

/**
 * 文章加热
 */
function heatArticle(articleId) {
  return request.post(`/vip/heat/${articleId}`)
}

module.exports = {
  activate,
  getInfo,
  getPrivileges,
  heatArticle
}
