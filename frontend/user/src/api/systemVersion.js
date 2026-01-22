import request from '@/utils/request'

// 获取版本历史列表
export function getSystemVersionList() {
  return request({
    url: '/system-version/list',
    method: 'get'
  })
}
