import request from '@/utils/request'

// 获取关注关系列表
export function getFollowList(params) {
  return request({ url: '/admin/follows', method: 'get', params })
}

// 获取用户关注统计
export function getUserFollowStats(userId) {
  return request({ url: `/admin/follows/user/${userId}/stats`, method: 'get' })
}

// 删除关注关系
export function deleteFollow(id) {
  return request({ url: `/admin/follows/${id}`, method: 'delete' })
}

// 批量删除关注关系
export function batchDeleteFollows(ids) {
  return request({ url: '/admin/follows/batch', method: 'delete', data: ids })
}

// 获取关注统计
export function getFollowStats() {
  return request({ url: '/admin/follows/stats', method: 'get' })
}

// 获取用户关注列表
export function getUserFollowing(userId, params) {
  return request({ url: `/admin/follows/user/${userId}/following`, method: 'get', params })
}

// 获取用户粉丝列表
export function getUserFollowers(userId, params) {
  return request({ url: `/admin/follows/user/${userId}/followers`, method: 'get', params })
}
