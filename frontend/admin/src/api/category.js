import request from '@/utils/request'

export function getCategoryList(params) {
  return request({ url: '/categories', method: 'get', params })
}

export function getCategoryTree() {
  return request({ url: '/categories/tree', method: 'get' })
}

export function createCategory(data) {
  return request({ url: '/categories', method: 'post', data })
}

export function updateCategory(id, data) {
  return request({ url: `/categories/${id}`, method: 'put', data })
}

export function deleteCategory(id) {
  return request({ url: `/categories/${id}`, method: 'delete' })
}
