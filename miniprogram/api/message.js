// message.js - 私信相关接口
const request = require('../utils/request')

/**
 * 获取会话列表
 */
function getConversations(params) {
  return request.get('/message/conversations', params)
}

/**
 * 获取会话详情（消息列表）
 */
function getConversation(conversationId, params) {
  return request.get(`/message/conversations/${conversationId}`, params)
}

/**
 * 获取与指定用户的会话
 */
function getConversationWith(userId) {
  return request.get(`/message/conversation-with/${userId}`)
}

/**
 * 发送私信
 */
function send(data) {
  return request.post('/message/send', data)
}

/**
 * 标记会话已读
 */
function markRead(conversationId) {
  return request.post(`/message/conversations/${conversationId}/read`)
}

/**
 * 获取未读消息数
 */
function getUnreadCount() {
  return request.get('/message/unread-count')
}

/**
 * 删除会话
 */
function deleteConversation(conversationId) {
  return request.delete(`/message/conversations/${conversationId}`)
}

/**
 * 获取消息列表
 */
function getMessages(conversationId, params) {
  return request.get(`/message/conversations/${conversationId}`, params)
}

/**
 * 发送消息
 */
function sendMessage(data) {
  return request.post('/message/send', data)
}

/**
 * 撤回消息
 */
function withdrawMessage(messageId) {
  return request.post(`/message/${messageId}/withdraw`)
}

/**
 * 删除消息
 */
function deleteMessage(messageId) {
  return request.delete(`/message/${messageId}`)
}

module.exports = {
  getConversations,
  getConversation,
  getConversationWith,
  getMessages,
  send,
  sendMessage,
  markRead,
  getUnreadCount,
  deleteConversation,
  withdrawMessage,
  deleteMessage
}
