// category.js - 分类相关接口
const request = require('../utils/request')

/**
 * 获取分类列表
 */
function getList() {
  return request.get('/categories')
}

/**
 * 获取分类详情
 */
function getDetail(id) {
  return request.get(`/categories/${id}`)
}

/**
 * 获取分类下的文章
 */
function getArticles(id, params) {
  return request.get(`/categories/${id}/articles`, params)
}

module.exports = {
  getList,
  getDetail,
  getArticles
}
