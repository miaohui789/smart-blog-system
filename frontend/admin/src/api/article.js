import request from '@/utils/request'

export function getArticleList(params) {
  return request({ url: '/admin/articles', method: 'get', params })
}

export function getArticleDetail(id) {
  return request({ url: `/admin/articles/${id}`, method: 'get' })
}

export function createArticle(data) {
  return request({ url: '/admin/articles', method: 'post', data })
}

export function updateArticle(id, data) {
  return request({ url: `/admin/articles/${id}`, method: 'put', data })
}

export function deleteArticle(id) {
  return request({ url: `/admin/articles/${id}`, method: 'delete' })
}

export function batchDeleteArticles(ids) {
  return request({ url: '/admin/articles/batch', method: 'delete', data: { ids } })
}

export function updateArticleStatus(id, status) {
  return request({ url: `/admin/articles/${id}/status`, method: 'put', data: { status } })
}

export function updateArticleTop(id, isTop) {
  return request({ url: `/admin/articles/${id}/top`, method: 'put', data: { isTop } })
}
