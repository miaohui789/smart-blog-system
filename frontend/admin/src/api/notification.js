import request from '@/utils/request'

// 获取通知列表
export function getNotificationList(params) {
  return request({ url: '/admin/notifications', method: 'get', params })
}

// 发送系统通知
export function sendSystemNotification(data) {
  return request({ url: '/admin/notifications/system', method: 'post', data })
}

// 删除通知
export function deleteNotification(id) {
  return request({ url: `/admin/notifications/${id}`, method: 'delete' })
}

// 批量删除通知
export function batchDeleteNotifications(ids) {
  return request({ url: '/admin/notifications/batch', method: 'delete', data: ids })
}

// 获取通知统计
export function getNotificationStats() {
  return request({ url: '/admin/notifications/stats', method: 'get' })
}

// 清空用户通知
export function clearUserNotifications(userId) {
  return request({ url: `/admin/notifications/user/${userId}`, method: 'delete' })
}
