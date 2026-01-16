import request from '@/utils/request'

// ========== 会员管理 ==========

// 获取会员列表
export function getMemberList(params) {
  return request({ url: '/admin/vip/members', method: 'get', params })
}

// 更新会员
export function updateMember(id, data) {
  return request({ url: `/admin/vip/members/${id}`, method: 'put', data })
}

// 删除会员
export function deleteMember(id) {
  return request({ url: `/admin/vip/members/${id}`, method: 'delete' })
}

// ========== 密钥管理 ==========

// 获取密钥列表
export function getKeyList(params) {
  return request({ url: '/admin/vip/keys', method: 'get', params })
}

// 批量生成密钥
export function generateKeys(data) {
  return request({ url: '/admin/vip/keys/generate', method: 'post', data })
}

// 更新密钥状态
export function updateKeyStatus(id, status) {
  return request({ url: `/admin/vip/keys/${id}/status`, method: 'put', params: { status } })
}

// 删除密钥
export function deleteKey(id) {
  return request({ url: `/admin/vip/keys/${id}`, method: 'delete' })
}

// ========== 统计 ==========

// 获取VIP统计数据
export function getVipStatistics() {
  return request({ url: '/admin/vip/statistics', method: 'get' })
}

// ========== 加热记录 ==========

// 获取加热记录列表
export function getHeatList(params) {
  return request({ url: '/admin/vip/heats', method: 'get', params })
}

// 获取加热统计
export function getHeatStats() {
  return request({ url: '/admin/vip/heats/stats', method: 'get' })
}
