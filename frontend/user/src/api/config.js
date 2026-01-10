import request from '@/utils/request'

// 获取网站配置
export function getSiteConfig() {
  return request.get('/config/site')
}
