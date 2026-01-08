import request from '@/utils/request'

export function getCommentList(params) {
  return request({ url: '/comments', method: 'get', params })
}

export function updateCommentStatus(id, status) {
  return request({ url: `/comments/${id}/status`, method: 'put', data: { status } })
}

export function deleteComment(id) {
  return request({ url: `/comments/${id}`, method: 'delete' })
}

export function batchDeleteComments(ids) {
  return request({ url: '/comments/batch', method: 'delete', data: { ids } })
}
