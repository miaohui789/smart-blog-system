import request from '@/utils/request'

export function getCommentList(params) {
  return request({ url: '/admin/comments', method: 'get', params })
}

export function updateCommentStatus(id, status) {
  return request({ url: `/admin/comments/${id}/status`, method: 'put', data: { status } })
}

export function deleteComment(id) {
  return request({ url: `/admin/comments/${id}`, method: 'delete' })
}

export function batchDeleteComments(ids) {
  return request({ url: '/admin/comments/batch', method: 'delete', data: { ids } })
}
