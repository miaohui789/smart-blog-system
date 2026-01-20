// user.js - 用户相关接口
const request = require('../utils/request')

/**
 * 微信登录
 */
function wxLogin(code) {
  return request.post('/auth/wx-login', { code })
}

/**
 * 获取用户信息
 */
function getUserInfo() {
  return request.get('/auth/info')
}

/**
 * 获取用户详情
 */
function getUserProfile(userId) {
  return request.get(`/user/${userId}/profile`)
}

/**
 * 获取用户文章列表
 */
function getUserArticles(userId, params) {
  return request.get(`/user/${userId}/articles`, params)
}

/**
 * 更新个人资料
 */
function updateProfile(data) {
  return request.put('/user/profile', data)
}

/**
 * 修改密码
 */
function updatePassword(data) {
  return request.put('/user/password', data)
}

/**
 * 上传头像
 */
function uploadAvatar(filePath) {
  return new Promise((resolve, reject) => {
    const config = require('../utils/config')
    const auth = require('../utils/auth')
    const token = auth.getToken()

    wx.uploadFile({
      url: config.BASE_URL + '/user/avatar',
      filePath,
      name: 'file',
      header: {
        'Authorization': token ? `Bearer ${token}` : ''
      },
      success: (res) => {
        const data = JSON.parse(res.data)
        if (data.code === 200) {
          resolve(data.data)
        } else {
          reject(data)
        }
      },
      fail: reject
    })
  })
}

/**
 * 关注用户
 */
function follow(userId) {
  return request.post(`/user/${userId}/follow`)
}

/**
 * 取消关注
 */
function unfollow(userId) {
  return request.delete(`/user/${userId}/follow`)
}

/**
 * 获取关注列表
 */
function getFollowing(params) {
  return request.get('/user/following', params)
}

/**
 * 获取粉丝列表
 */
function getFollowers(params) {
  return request.get('/user/followers', params)
}

/**
 * 获取指定用户的关注列表
 */
function getUserFollowing(userId, params) {
  return request.get(`/user/${userId}/following`, params)
}

/**
 * 获取指定用户的粉丝列表
 */
function getUserFollowers(userId, params) {
  return request.get(`/user/${userId}/followers`, params)
}

/**
 * 获取我的收藏
 */
function getFavorites(params) {
  return request.get('/user/favorites', params)
}

/**
 * 获取我的点赞
 */
function getLikes(params) {
  return request.get('/user/likes', params)
}

/**
 * 获取我的评论
 */
function getComments(params) {
  return request.get('/user/comments', params)
}

/**
 * 搜索用户（通过文章搜索接口获取）
 */
async function search(keyword, params) {
  const articleApi = require('./article')
  const res = await articleApi.search(keyword, params)
  // 返回用户列表
  return res.users || []
}

/**
 * 账号密码登录
 */
function login(data) {
  return request.post('/auth/login', data)
}

/**
 * 发送邮箱验证码（注册用）
 */
function sendEmailCode(email) {
  // POST请求，参数在URL查询字符串中，超时时间60秒，禁用loading
  return request.post(`/auth/send-code?email=${encodeURIComponent(email)}`, {}, { timeout: 60000, loading: false })
}

/**
 * 发送重置密码验证码
 */
function sendResetCode(email) {
  // POST请求，参数在URL查询字符串中，超时时间60秒，禁用loading
  return request.post(`/auth/forgot-password/send-code?email=${encodeURIComponent(email)}`, {}, { timeout: 60000, loading: false })
}

/**
 * 注册
 */
function register(data) {
  return request.post('/auth/register', data)
}

/**
 * 重置密码
 */
function resetPassword(data) {
  return request.post('/auth/forgot-password/reset', data)
}

module.exports = {
  wxLogin,
  getUserInfo,
  getUserProfile,
  getUserArticles,
  updateProfile,
  updatePassword,
  uploadAvatar,
  follow,
  unfollow,
  getFollowing,
  getFollowers,
  getUserFollowing,
  getUserFollowers,
  getFavorites,
  getLikes,
  getComments,
  search,
  login,
  sendEmailCode,
  sendResetCode,
  register,
  resetPassword
}
