import request from '@/utils/request'

export function getRoleList(params) {
  return request({ url: '/admin/roles', method: 'get', params })
}

export function getAllRoles() {
  return request({ url: '/admin/roles/all', method: 'get' })
}

export function createRole(data) {
  return request({ url: '/admin/roles', method: 'post', data })
}

export function updateRole(id, data) {
  return request({ url: `/admin/roles/${id}`, method: 'put', data })
}

export function deleteRole(id) {
  return request({ url: `/admin/roles/${id}`, method: 'delete' })
}

export function updateRoleMenus(id, menuIds) {
  return request({ url: `/admin/roles/${id}/menus`, method: 'put', data: { menuIds } })
}
