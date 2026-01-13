import request from '@/utils/request'

export function getProfile() {
  return request({ url: '/admin/profile', method: 'get' })
}

export function updateProfile(data) {
  return request({ url: '/admin/profile', method: 'put', data })
}

export function updatePassword(data) {
  return request({ url: '/admin/profile/password', method: 'put', data })
}

export function uploadAvatar(data) {
  return request({ url: '/admin/upload/image', method: 'post', data, headers: { 'Content-Type': 'multipart/form-data' } })
}

export function updateAvatar(avatar) {
  return request({ url: '/admin/profile', method: 'put', data: { avatar } })
}
