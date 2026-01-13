import request from '@/utils/request'

// 用户登录
export function login(data) {
  return request({ url: '/auth/login', method: 'post', data })
}

// 用户注册
export function register(data) {
  return request({ url: '/auth/register', method: 'post', data })
}

// 发送邮箱验证码（注册用）
export function sendEmailCode(email) {
  return request({ 
    url: '/auth/send-code', 
    method: 'post', 
    params: { email },
    timeout: 60000,
    showError: false
  })
}

// 发送重置密码验证码
export function sendResetCode(email) {
  return request({ 
    url: '/auth/forgot-password/send-code', 
    method: 'post', 
    params: { email },
    timeout: 60000,
    showError: false
  })
}

// 重置密码
export function resetPassword(data) {
  return request({ url: '/auth/forgot-password/reset', method: 'post', data })
}

// 退出登录
export function logout() {
  return request({ url: '/auth/logout', method: 'post' })
}

// 获取用户信息
export function getUserInfo() {
  return request({ url: '/auth/info', method: 'get' })
}

// 获取个人资料
export function getProfile() {
  return request({ url: '/user/profile', method: 'get' })
}

// 更新个人资料
export function updateProfile(data) {
  return request({ url: '/user/profile', method: 'put', data })
}

// 修改密码
export function updatePassword(data) {
  return request({ url: '/user/password', method: 'put', data })
}

// 上传头像
export function uploadAvatar(data) {
  return request({ url: '/user/avatar', method: 'post', data })
}

// 获取我的收藏
export function getFavorites(params) {
  return request({ url: '/user/favorites', method: 'get', params })
}

// 获取我的评论
export function getMyComments(params) {
  return request({ url: '/user/comments', method: 'get', params })
}
