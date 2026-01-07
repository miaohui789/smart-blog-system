import request from '@/utils/request'

// 获取分类列表
export function getCategoryList() {
  return request({ url: '/categories', method: 'get' })
}

// 获取分类下的文章
export function getCategoryArticles(id, params) {
  return request({ url: `/categories/${id}/articles`, method: 'get', params })
}
