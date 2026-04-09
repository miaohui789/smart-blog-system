import request from '@/utils/request'

/**
 * 获取用户经验概览
 */
export function getExpSummary() {
  return request({
    url: '/user/exp/summary',
    method: 'get'
  })
}

/**
 * 获取用户经验流水
 * @param {Object} params - 分页参数 {page, pageSize}
 */
export function getExpRecords(params) {
  return request({
    url: '/user/exp/records',
    method: 'get',
    params
  })
}

export function getSignInCalendar(params) {
  return request({
    url: '/user/exp/sign/calendar',
    method: 'get',
    params
  })
}

export function signInToday() {
  return request({
    url: '/user/exp/sign',
    method: 'post'
  })
}
