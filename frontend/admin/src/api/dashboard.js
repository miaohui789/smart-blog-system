import request from '@/utils/request'

export function getStats() {
  return request({ url: '/admin/dashboard/stats', method: 'get' })
}

export function getStatsExtended() {
  return request({ url: '/admin/dashboard/stats-extended', method: 'get' })
}

export function getVisitTrend() {
  return request({ url: '/admin/dashboard/visit-trend', method: 'get' })
}

export function getArticleTrend() {
  return request({ url: '/admin/dashboard/article-trend', method: 'get' })
}

export function getCategoryStats() {
  return request({ url: '/admin/dashboard/category-stats', method: 'get' })
}

export function getTagStats() {
  return request({ url: '/admin/dashboard/tag-stats', method: 'get' })
}

export function getArticleStatus() {
  return request({ url: '/admin/dashboard/article-status', method: 'get' })
}

export function getCommentStatus() {
  return request({ url: '/admin/dashboard/comment-status', method: 'get' })
}

export function getHotArticles() {
  return request({ url: '/admin/dashboard/hot-articles', method: 'get' })
}

export function getRecentComments() {
  return request({ url: '/admin/dashboard/recent-comments', method: 'get' })
}

export function getRecentArticles() {
  return request({ url: '/admin/dashboard/recent-articles', method: 'get' })
}

export function getActiveUsers() {
  return request({ url: '/admin/dashboard/active-users', method: 'get' })
}

export function getTodayStats() {
  return request({ url: '/admin/dashboard/today-stats', method: 'get' })
}
