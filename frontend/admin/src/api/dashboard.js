import request from '@/utils/request'

export function getStats() {
  return request({ url: '/dashboard/stats', method: 'get' })
}

export function getStatsExtended() {
  return request({ url: '/dashboard/stats-extended', method: 'get' })
}

export function getVisitTrend() {
  return request({ url: '/dashboard/visit-trend', method: 'get' })
}

export function getArticleTrend() {
  return request({ url: '/dashboard/article-trend', method: 'get' })
}

export function getCategoryStats() {
  return request({ url: '/dashboard/category-stats', method: 'get' })
}

export function getTagStats() {
  return request({ url: '/dashboard/tag-stats', method: 'get' })
}

export function getArticleStatus() {
  return request({ url: '/dashboard/article-status', method: 'get' })
}

export function getCommentStatus() {
  return request({ url: '/dashboard/comment-status', method: 'get' })
}

export function getHotArticles() {
  return request({ url: '/dashboard/hot-articles', method: 'get' })
}

export function getRecentComments() {
  return request({ url: '/dashboard/recent-comments', method: 'get' })
}

export function getRecentArticles() {
  return request({ url: '/dashboard/recent-articles', method: 'get' })
}

export function getActiveUsers() {
  return request({ url: '/dashboard/active-users', method: 'get' })
}

export function getTodayStats() {
  return request({ url: '/dashboard/today-stats', method: 'get' })
}
