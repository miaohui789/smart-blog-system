import request from '@/utils/request'

// 获取通知列表
export function getNotificationList(params) {
  return request({ url: '/notifications', method: 'get', params })
}

// 获取未读通知数量
export function getUnreadCount() {
  return request({ url: '/notifications/unread-count', method: 'get' })
}

// 标记单条通知为已读
export function markAsRead(id) {
  return request({ url: `/notifications/${id}/read`, method: 'post' })
}

// 标记所有通知为已读
export function markAllAsRead(type) {
  return request({ 
    url: '/notifications/read-all', 
    method: 'post',
    params: type ? { type } : {}
  })
}

// 删除通知
export function deleteNotification(id) {
  return request({ url: `/notifications/${id}`, method: 'delete' })
}

// 清空所有通知
export function clearAllNotifications(type) {
  return request({ 
    url: '/notifications/clear', 
    method: 'delete',
    params: type ? { type } : {}
  })
}
