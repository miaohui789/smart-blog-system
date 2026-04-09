import request from '@/utils/request'

export function searchAll(params) {
  return request({ url: '/search', method: 'get', params })
}

export function getHotSearchBoard(params) {
  return request({ url: '/search/hot', method: 'get', params })
}
