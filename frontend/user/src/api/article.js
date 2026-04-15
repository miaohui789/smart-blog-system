import request from '@/utils/request'

// 获取文章列表
export function getArticleList(params) {
  return request({ url: '/articles', method: 'get', params })
}

// 获取文章详情
export function getArticleDetail(id) {
  return request({ url: `/articles/${id}`, method: 'get' })
}

// 获取推荐文章
export function getRecommendArticles() {
  return request({ url: '/articles/recommend', method: 'get' })
}

// 获取热门文章
export function getHotArticles() {
  return request({ url: '/articles/hot', method: 'get' })
}

// 获取文章归档
export function getArticleArchive() {
  return request({ url: '/articles/archive', method: 'get' })
}

// 搜索文章
export function searchArticles(params) {
  return request({ url: '/articles/search', method: 'get', params })
}

// 点赞文章
export function likeArticle(id) {
  return request({ url: `/articles/${id}/like`, method: 'post' })
}

// 取消点赞
export function unlikeArticle(id) {
  return request({ url: `/articles/${id}/like`, method: 'delete' })
}

// 收藏文章
export function favoriteArticle(id) {
  return request({ url: `/articles/${id}/favorite`, method: 'post' })
}

// 取消收藏
export function unfavoriteArticle(id) {
  return request({ url: `/articles/${id}/favorite`, method: 'delete' })
}

// 发布文章
export function createArticle(data) {
  return request({ url: '/articles', method: 'post', data })
}

// 更新文章
export function updateArticle(id, data) {
  return request({ url: `/articles/${id}`, method: 'put', data })
}

// 更新我的文章状态
export function updateMyArticleStatus(id, status) {
  return request({ url: `/articles/${id}/status`, method: 'put', data: { status } })
}

// 删除文章
export function deleteArticle(id) {
  return request({ url: `/articles/${id}`, method: 'delete' })
}

// 获取我的文章列表
export function getMyArticles(params) {
  return request({ url: '/articles/my', method: 'get', params })
}
