import request from '@/utils/request'

export function getConfigs() {
  return request({ url: '/configs', method: 'get' })
}

export function updateConfigs(data) {
  return request({ url: '/configs', method: 'put', data })
}

export function uploadImage(data) {
  return request({ url: '/upload/image', method: 'post', data, headers: { 'Content-Type': 'multipart/form-data' } })
}

export function uploadFile(data) {
  return request({ url: '/upload/file', method: 'post', data, headers: { 'Content-Type': 'multipart/form-data' } })
}
