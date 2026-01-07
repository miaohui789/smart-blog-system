import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

// 格式化日期
export function formatDate(date, format = 'YYYY-MM-DD') {
  if (!date) return ''
  return dayjs(date).format(format)
}

// 格式化日期时间
export function formatDateTime(date) {
  return formatDate(date, 'YYYY-MM-DD HH:mm:ss')
}

// 相对时间
export function formatRelativeTime(date) {
  if (!date) return ''
  return dayjs(date).fromNow()
}

// 格式化数字
export function formatNumber(num) {
  if (!num) return '0'
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}

// 截取文本
export function truncateText(text, length = 100) {
  if (!text) return ''
  if (text.length <= length) return text
  return text.slice(0, length) + '...'
}
