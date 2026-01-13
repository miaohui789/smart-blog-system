import request from '@/utils/request'

export function getCategoryList(params) {
  return request({ url: '/admin/categories', method: 'get', params })
}

export function getCategoryTree() {
  return request({ url: '/admin/categories/tree', method: 'get' })
}

export function createCategory(data) {
  return request({ url: '/admin/categories', method: 'post', data })
}

export function updateCategory(id, data) {
  return request({ url: `/admin/categories/${id}`, method: 'put', data })
}

export function deleteCategory(id) {
  return request({ url: `/admin/categories/${id}`, method: 'delete' })
}
