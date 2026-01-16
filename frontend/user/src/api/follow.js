import request from '@/utils/request'

// 获取用户详情
export function getUserProfile(userId) {
  return request({ url: `/user/${userId}/profile`, method: 'get' })
}

// 获取用户发布的文章
export function getUserArticles(userId, params) {
  return request({ url: `/user/${userId}/articles`, method: 'get', params })
}

// 关注用户
export function followUser(userId) {
  return request({ url: `/user/${userId}/follow`, method: 'post' })
}

// 取消关注
export function unfollowUser(userId) {
  return request({ url: `/user/${userId}/follow`, method: 'delete' })
}

// 获取我的关注列表
export function getFollowingList(params) {
  return request({ url: '/user/following', method: 'get', params })
}

// 获取我的粉丝列表
export function getFollowersList(userId, params) {
  // 如果传了userId，获取指定用户的粉丝；否则获取自己的粉丝
  const url = userId ? `/user/${userId}/followers` : '/user/followers'
  return request({ url, method: 'get', params })
}

// 获取指定用户的关注列表
export function getUserFollowingList(userId, params) {
  return request({ url: `/user/${userId}/following`, method: 'get', params })
}
