import request from '@/utils/request'

// 发送私信
export function sendMessage(data) {
  return request({ url: '/message/send', method: 'post', data })
}

// 获取会话列表
export function getConversations() {
  return request({ url: '/message/conversations', method: 'get' })
}

// 获取会话消息列表
export function getMessages(conversationId, params) {
  return request({ url: `/message/conversations/${conversationId}`, method: 'get', params })
}

// 标记会话已读
export function markAsRead(conversationId) {
  return request({ url: `/message/conversations/${conversationId}/read`, method: 'post' })
}

// 获取未读消息数
export function getUnreadCount() {
  return request({ url: '/message/unread-count', method: 'get' })
}

// 获取与指定用户的会话
export function getConversationWith(userId) {
  return request({ url: `/message/conversation-with/${userId}`, method: 'get' })
}
