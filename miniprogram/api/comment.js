// comment.js - 评论相关接口
const request = require('../utils/request')

/**
 * 获取文章评论列表
 */
function getList(articleId, params) {
  return request.get(`/articles/${articleId}/comments`, params)
}

/**
 * 发表评论
 */
function create(data) {
  return request.post('/comments', data)
}

/**
 * 删除评论
 */
function remove(id) {
  return request.delete(`/comments/${id}`)
}

/**
 * 更新评论
 */
function update(id, data) {
  return request.put(`/comments/${id}`, data)
}

/**
 * 点赞评论
 */
function like(id) {
  return request.post(`/comments/${id}/like`)
}

/**
 * 取消点赞评论
 */
function unlike(id) {
  return request.delete(`/comments/${id}/like`)
}

module.exports = {
  getList,
  create,
  remove,
  update,
  like,
  unlike
}
