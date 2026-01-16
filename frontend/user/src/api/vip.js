import request from '@/utils/request'
import axios from 'axios'
import { getToken } from '@/utils/auth'

// 激活VIP
export function activateVip(data) {
  return request({ url: '/vip/activate', method: 'post', data })
}

// 获取VIP信息
export function getVipInfo() {
  return request({ url: '/vip/info', method: 'get' })
}

// 获取VIP权益说明
export function getVipPrivileges() {
  return request({ url: '/vip/privileges', method: 'get' })
}

// 文章加热
export function heatArticle(articleId) {
  return request({ url: `/vip/heat/${articleId}`, method: 'post' })
}

// 下载文章MD（使用原生axios，因为返回的是blob）
export function downloadArticle(articleId) {
  const baseURL = import.meta.env.VITE_APP_BASE_API
  const token = getToken()
  return axios({
    url: `${baseURL}/vip/download/${articleId}`,
    method: 'get',
    responseType: 'blob',
    headers: {
      'Authorization': token ? `Bearer ${token}` : ''
    }
  }).then(response => {
    // 检查是否是错误响应（text/plain 或 application/json）
    const contentType = response.headers['content-type']
    if (contentType && (contentType.includes('text/plain') || contentType.includes('application/json'))) {
      // 读取错误信息
      return new Promise((resolve, reject) => {
        const reader = new FileReader()
        reader.onload = () => reject(new Error(reader.result || '下载失败'))
        reader.onerror = () => reject(new Error('下载失败'))
        reader.readAsText(response.data)
      })
    }
    return response.data
  }).catch(error => {
    // 处理HTTP错误响应
    if (error.response && error.response.data instanceof Blob) {
      return new Promise((resolve, reject) => {
        const reader = new FileReader()
        reader.onload = () => reject(new Error(reader.result || '下载失败'))
        reader.onerror = () => reject(new Error('下载失败'))
        reader.readAsText(error.response.data)
      })
    }
    throw error
  })
}
