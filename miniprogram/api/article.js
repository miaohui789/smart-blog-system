// article.js - 文章相关接口
const request = require('../utils/request')

/**
 * 获取文章列表
 */
function getList(params) {
  return request.get('/articles', params)
}

/**
 * 获取文章详情
 */
function getDetail(id) {
  return request.get(`/articles/${id}`)
}

/**
 * 搜索文章
 */
function search(keyword, params) {
  return request.get('/articles/search', { keyword, ...params })
}

/**
 * 获取推荐文章
 */
function getRecommend() {
  return request.get('/articles/recommend')
}

/**
 * 获取热门文章
 */
function getHot() {
  return request.get('/articles/hot')
}

/**
 * 获取文章归档
 */
function getArchive() {
  return request.get('/articles/archive')
}

/**
 * 点赞文章
 */
function like(id) {
  return request.post(`/articles/${id}/like`)
}

/**
 * 取消点赞
 */
function unlike(id) {
  return request.delete(`/articles/${id}/like`)
}

/**
 * 收藏文章
 */
function favorite(id) {
  return request.post(`/articles/${id}/favorite`)
}

/**
 * 取消收藏
 */
function unfavorite(id) {
  return request.delete(`/articles/${id}/favorite`)
}

/**
 * 下载文章（VIP 功能）
 */
function download(id) {
  return request.get(`/articles/${id}/download`)
}

module.exports = {
  getList,
  getDetail,
  search,
  getRecommend,
  getHot,
  getArchive,
  like,
  unlike,
  favorite,
  unfavorite,
  download
}
