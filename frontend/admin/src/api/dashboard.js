import request from '@/utils/request'

export function getStats() {
  return request({ url: '/dashboard/stats', method: 'get' })
}

export function getVisitTrend() {
  return request({ url: '/dashboard/visit-trend', method: 'get' })
}

export function getCategoryStats() {
  return request({ url: '/dashboard/category-stats', method: 'get' })
}
