import request from '@/utils/request'

export function login(data) {
  return request({ url: '/auth/login', method: 'post', data })
}

export function logout() {
  return request({ url: '/auth/logout', method: 'post' })
}

export function getAdminInfo() {
  return request({ url: '/auth/info', method: 'get' })
}

export function getMenus() {
  return request({ url: '/auth/menus', method: 'get' })
}

export function getUserList(params) {
  return request({ url: '/users', method: 'get', params })
}

export function getUserDetail(id) {
  return request({ url: `/users/${id}`, method: 'get' })
}

export function createUser(data) {
  return request({ url: '/users', method: 'post', data })
}

export function updateUser(id, data) {
  return request({ url: `/users/${id}`, method: 'put', data })
}

export function deleteUser(id) {
  return request({ url: `/users/${id}`, method: 'delete' })
}

export function updateUserStatus(id, status) {
  return request({ url: `/users/${id}/status`, method: 'put', data: { status } })
}

export function resetPassword(id) {
  return request({ url: `/users/${id}/password`, method: 'put' })
}
