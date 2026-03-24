import request from '@/utils/request'

// 获取所有AI配置列表
export function getAiConfigs() {
  return request({ url: '/admin/ai/configs', method: 'get' })
}

// 获取单个AI配置（兼容旧接口）
export function getAiConfig() {
  return request({ url: '/admin/ai/config', method: 'get' })
}

// 获取单个AI配置 by ID
export function getAiConfigById(id) {
  return request({ url: `/admin/ai/config/${id}`, method: 'get' })
}

// 新增AI配置
export function addAiConfig(data) {
  return request({ url: '/admin/ai/config', method: 'post', data })
}

// 更新AI配置
export function updateAiConfig(data) {
  return request({ url: '/admin/ai/config', method: 'put', data })
}

// 删除AI配置
export function deleteAiConfig(id) {
  return request({ url: `/admin/ai/config/${id}`, method: 'delete' })
}

// 设置默认模型
export function setDefaultAiConfig(id) {
  return request({ url: `/admin/ai/config/${id}/default`, method: 'put' })
}

// 设置学习模块AI评分默认模型
export function setDefaultStudyScoreAiConfig(id) {
  return request({ url: `/admin/ai/config/${id}/default-study-score`, method: 'put' })
}

// 测试AI连接
export function testAiConnection(data) {
  return request({ url: '/admin/ai/test', method: 'post', data })
}

// 测试学习模块AI评分
export function testStudyScore(data) {
  return request({ url: '/admin/ai/test-study-score', method: 'post', data })
}

// ============ AI Logo 管理 ============

// 获取所有Logo
export function getAiLogos() {
  return request({ url: '/admin/ai/logo/list', method: 'get' })
}

// 新增Logo（网络地址）
export function addAiLogo(data) {
  return request({ url: '/admin/ai/logo', method: 'post', data })
}

// 上传Logo图片
export function uploadAiLogo(data) {
  return request({ url: '/admin/ai/logo/upload', method: 'post', data, headers: { 'Content-Type': 'multipart/form-data' } })
}

// 更新Logo
export function updateAiLogo(data) {
  return request({ url: '/admin/ai/logo', method: 'put', data })
}

// 删除Logo
export function deleteAiLogo(id) {
  return request({ url: `/admin/ai/logo/${id}`, method: 'delete' })
}
