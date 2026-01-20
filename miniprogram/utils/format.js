// format.js - 格式化工具

/**
 * 格式化时间
 * @param {String|Number|Date} time 时间
 * @param {String} format 格式
 */
function formatTime(time, format = 'YYYY-MM-DD HH:mm:ss') {
  if (!time) return ''
  
  const date = new Date(time)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  const second = String(date.getSeconds()).padStart(2, '0')
  
  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hour)
    .replace('mm', minute)
    .replace('ss', second)
}

/**
 * 格式化相对时间
 */
function formatRelativeTime(time) {
  if (!time) return ''
  
  const now = new Date().getTime()
  const past = new Date(time).getTime()
  const diff = now - past
  
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  const month = 30 * day
  const year = 365 * day
  
  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return Math.floor(diff / minute) + '分钟前'
  } else if (diff < day) {
    return Math.floor(diff / hour) + '小时前'
  } else if (diff < month) {
    return Math.floor(diff / day) + '天前'
  } else if (diff < year) {
    return Math.floor(diff / month) + '个月前'
  } else {
    return Math.floor(diff / year) + '年前'
  }
}

/**
 * 格式化数字（千分位）
 */
function formatNumber(num) {
  if (!num && num !== 0) return '0'
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

/**
 * 格式化数字（简化显示）
 */
function formatCount(count) {
  if (!count && count !== 0) return '0'
  
  if (count < 1000) {
    return count.toString()
  } else if (count < 10000) {
    return (count / 1000).toFixed(1) + 'k'
  } else if (count < 100000000) {
    return (count / 10000).toFixed(1) + 'w'
  } else {
    return (count / 100000000).toFixed(1) + '亿'
  }
}

/**
 * 格式化文件大小
 */
function formatFileSize(size) {
  if (!size && size !== 0) return '0B'
  
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let index = 0
  
  while (size >= 1024 && index < units.length - 1) {
    size /= 1024
    index++
  }
  
  return size.toFixed(2) + units[index]
}

/**
 * 截取字符串
 */
function truncate(str, length = 50, suffix = '...') {
  if (!str) return ''
  if (str.length <= length) return str
  return str.substring(0, length) + suffix
}

/**
 * 手机号脱敏
 */
function maskPhone(phone) {
  if (!phone) return ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

/**
 * 邮箱脱敏
 */
function maskEmail(email) {
  if (!email) return ''
  return email.replace(/(.{2}).*(@.*)/, '$1***$2')
}

/**
 * 格式化消息时间（用于时间分割线）
 */
function formatMessageTime(time) {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000)
  const msgDate = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const timeStr = `${hours}:${minutes}`
  
  if (msgDate.getTime() === today.getTime()) {
    return `今天 ${timeStr}`
  } else if (msgDate.getTime() === yesterday.getTime()) {
    return `昨天 ${timeStr}`
  } else {
    const month = date.getMonth() + 1
    const day = date.getDate()
    return `${date.getFullYear()}年${month}月${day}日 ${timeStr}`
  }
}

module.exports = {
  formatTime,
  formatRelativeTime,
  formatMessageTime,
  formatNumber,
  formatCount,
  formatFileSize,
  truncate,
  maskPhone,
  maskEmail
}
