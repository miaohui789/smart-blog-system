import request from '@/utils/request'

export function getTagList(params) {
  return request({ url: '/admin/tags', method: 'get', params })
}

export function createTag(data) {
  return request({ url: '/admin/tags', method: 'post', data })
}

export function updateTag(id, data) {
  return request({ url: `/admin/tags/${id}`, method: 'put', data })
}

export function deleteTag(id) {
  return request({ url: `/admin/tags/${id}`, method: 'delete' })
}

export function batchDeleteTags(ids) {
  return request({ url: '/admin/tags/batch', method: 'delete', data: { ids } })
}
