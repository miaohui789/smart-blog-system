import request from '@/utils/request'

// 获取文章评论列表
export function getCommentList(articleId, params) {
  return request({ url: `/articles/${articleId}/comments`, method: 'get', params })
}

// 发表评论
export function createComment(data) {
  return request({ url: '/comments', method: 'post', data })
}

// 删除评论
export function deleteComment(id) {
  return request({ url: `/comments/${id}`, method: 'delete' })
}

// 更新评论
export function updateComment(id, content) {
  return request({ url: `/comments/${id}`, method: 'put', data: { content } })
}

// 点赞评论
export function likeComment(id) {
  return request({ url: `/comments/${id}/like`, method: 'post' })
}
