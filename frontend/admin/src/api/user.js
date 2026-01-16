import request from '@/utils/request'

export function login(data) {
  return request({ url: '/admin/auth/login', method: 'post', data })
}

export function logout() {
  return request({ url: '/admin/auth/logout', method: 'post' })
}

export function getAdminInfo() {
  return request({ url: '/admin/auth/info', method: 'get' })
}

export function getMenus() {
  return request({ url: '/admin/auth/menus', method: 'get' })
}

export function getUserList(params) {
  return request({ url: '/admin/users', method: 'get', params })
}

export function getUserDetail(id) {
  return request({ url: `/admin/users/${id}`, method: 'get' })
}

export function createUser(data) {
  return request({ url: '/admin/users', method: 'post', data })
}

export function updateUser(id, data) {
  return request({ url: `/admin/users/${id}`, method: 'put', data })
}

export function deleteUser(id) {
  return request({ url: `/admin/users/${id}`, method: 'delete' })
}

export function cancelUser(id) {
  return request({ url: `/admin/users/${id}/cancel`, method: 'put' })
}

export function updateUserStatus(id, status) {
  return request({ url: `/admin/users/${id}/status`, method: 'put', data: { status } })
}

export function resetPassword(id) {
  return request({ url: `/admin/users/${id}/password`, method: 'put' })
}
