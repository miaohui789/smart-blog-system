import request from '@/utils/request'

// 获取标签列表
export function getTagList() {
  return request({ url: '/tags', method: 'get' })
}

// 获取标签下的文章
export function getTagArticles(id, params) {
  return request({ url: `/tags/${id}/articles`, method: 'get', params })
}
