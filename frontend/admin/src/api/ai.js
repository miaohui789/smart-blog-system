import request from '@/utils/request'

// 获取AI配置
export function getAiConfig() {
  return request({ url: '/admin/ai/config', method: 'get' })
}

// 更新AI配置
export function updateAiConfig(data) {
  return request({ url: '/admin/ai/config', method: 'put', data })
}

// 测试AI连接
export function testAiConnection() {
  return request({ url: '/admin/ai/test', method: 'post' })
}
