// tag.js - 标签相关接口
const request = require('../utils/request')

/**
 * 获取标签列表
 */
function getList() {
  return request.get('/tags')
}

/**
 * 获取标签详情
 */
function getDetail(id) {
  return request.get(`/tags/${id}`)
}

/**
 * 获取标签下的文章
 */
function getArticles(id, params) {
  return request.get(`/tags/${id}/articles`, params)
}

module.exports = {
  getList,
  getDetail,
  getArticles
}
