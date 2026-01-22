/**
 * 复制文本到剪贴板
 * 支持现代浏览器和降级方案
 * @param {string} text - 要复制的文本
 * @returns {Promise<boolean>} - 复制是否成功
 */
export async function copyToClipboard(text) {
  // 方案1: 使用现代剪贴板API (需要HTTPS或localhost)
  if (navigator.clipboard && window.isSecureContext) {
    try {
      await navigator.clipboard.writeText(text)
      return true
    } catch (err) {
      console.warn('Clipboard API失败，尝试降级方案:', err)
      // 降级到方案2
      return fallbackCopy(text)
    }
  }
  
  // 方案2: 降级方案 - 使用传统的document.execCommand
  return fallbackCopy(text)
}

/**
 * 降级复制方法 - 兼容所有浏览器
 * @param {string} text - 要复制的文本
 * @returns {boolean} - 复制是否成功
 */
function fallbackCopy(text) {
  // 创建临时textarea元素
  const textarea = document.createElement('textarea')
  textarea.value = text
  
  // 设置样式，使其不可见但可选中
  textarea.style.position = 'fixed'
  textarea.style.top = '0'
  textarea.style.left = '0'
  textarea.style.width = '2em'
  textarea.style.height = '2em'
  textarea.style.padding = '0'
  textarea.style.border = 'none'
  textarea.style.outline = 'none'
  textarea.style.boxShadow = 'none'
  textarea.style.background = 'transparent'
  textarea.style.opacity = '0'
  textarea.style.pointerEvents = 'none'
  
  // 添加到DOM
  document.body.appendChild(textarea)
  
  // 选中文本
  textarea.focus()
  textarea.select()
  
  // 兼容iOS
  if (navigator.userAgent.match(/ipad|iphone/i)) {
    const range = document.createRange()
    range.selectNodeContents(textarea)
    const selection = window.getSelection()
    selection.removeAllRanges()
    selection.addRange(range)
    textarea.setSelectionRange(0, 999999)
  }
  
  let successful = false
  try {
    // 执行复制命令
    successful = document.execCommand('copy')
  } catch (err) {
    console.error('execCommand复制失败:', err)
  }
  
  // 清理
  document.body.removeChild(textarea)
  
  return successful
}

/**
 * 复制文本并显示Element Plus消息提示
 * @param {string} text - 要复制的文本
 * @param {Function} ElMessage - Element Plus的消息组件
 * @param {string} successMsg - 成功提示消息
 * @param {string} errorMsg - 失败提示消息
 */
export async function copyWithMessage(text, ElMessage, successMsg = '已复制', errorMsg = '复制失败，请手动复制') {
  const success = await copyToClipboard(text)
  if (success) {
    ElMessage.success(successMsg)
  } else {
    ElMessage.error(errorMsg)
  }
}
