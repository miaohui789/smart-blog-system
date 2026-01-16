import request from '@/utils/request'

// 获取私信列表
export function getMessageList(params) {
  return request({ url: '/admin/messages', method: 'get', params })
}

// 获取会话列表
export function getConversationList(params) {
  return request({ url: '/admin/messages/conversations', method: 'get', params })
}

// 删除私信
export function deleteMessage(id) {
  return request({ url: `/admin/messages/${id}`, method: 'delete' })
}

// 批量删除私信
export function batchDeleteMessages(ids) {
  return request({ url: '/admin/messages/batch', method: 'delete', data: ids })
}

// 获取私信统计
export function getMessageStats() {
  return request({ url: '/admin/messages/stats', method: 'get' })
}
