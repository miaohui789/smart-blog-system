import request from '@/utils/request'

export function getConfigs() {
  return request({ url: '/admin/configs', method: 'get' })
}

export function updateConfigs(data) {
  return request({ url: '/admin/configs', method: 'put', data })
}

export function resetConfigs(type) {
  return request({ url: `/admin/configs/reset/${type}`, method: 'post' })
}

export function getSearchStatus() {
  return request({ url: '/admin/search/status', method: 'get' })
}

export function rebuildSearchIndex(scope) {
  return request({ url: '/admin/search/rebuild', method: 'post', params: { scope } })
}

export function uploadImage(data) {
  return request({ url: '/admin/upload/image', method: 'post', data, headers: { 'Content-Type': 'multipart/form-data' } })
}

export function uploadFile(data) {
  return request({ url: '/admin/upload/file', method: 'post', data, headers: { 'Content-Type': 'multipart/form-data' } })
}
