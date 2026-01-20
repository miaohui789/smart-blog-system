// notification.js - 通知相关接口
const request = require('../utils/request')

/**
 * 获取通知列表
 */
function getList(params) {
  return request.get('/notifications', params)
}

/**
 * 获取未读通知数量
 */
function getUnreadCount() {
  return request.get('/notifications/unread-count')
}

/**
 * 标记单条通知已读
 */
function markRead(id) {
  return request.post(`/notifications/${id}/read`)
}

/**
 * 标记全部已读
 */
function markAllRead() {
  return request.post('/notifications/read-all')
}

/**
 * 删除通知
 */
function remove(id) {
  return request.delete(`/notifications/${id}`)
}

/**
 * 清空通知
 */
function clear() {
  return request.delete('/notifications/clear')
}

module.exports = {
  getList,
  getUnreadCount,
  markRead,
  markAllRead,
  remove,
  clear
}
