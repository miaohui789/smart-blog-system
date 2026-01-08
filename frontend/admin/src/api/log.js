import request from '@/utils/request'

export function getOperationLogs(params) {
  return request({ url: '/logs/operation', method: 'get', params })
}

export function getLoginLogs(params) {
  return request({ url: '/logs/login', method: 'get', params })
}

export function cleanOperationLogs() {
  return request({ url: '/logs/operation/clean', method: 'delete' })
}

export function cleanLoginLogs() {
  return request({ url: '/logs/login/clean', method: 'delete' })
}
