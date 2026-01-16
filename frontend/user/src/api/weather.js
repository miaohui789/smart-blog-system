import request from '@/utils/request'

// 获取实时天气
export function getWeather() {
  return request({ 
    url: '/weather', 
    method: 'get',
    timeout: 35000  // 35秒超时，比后端30秒稍长
  })
}
