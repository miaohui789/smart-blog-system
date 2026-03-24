/**
 * 复制文本到剪贴板
 * 优先使用现代 Clipboard API，在非安全上下文中自动降级
 * @param {string} text
 * @returns {Promise<boolean>}
 */
export async function copyToClipboard(text) {
  const normalizedText = typeof text === 'string' ? text : String(text ?? '')

  if (navigator.clipboard && window.isSecureContext) {
    try {
      await navigator.clipboard.writeText(normalizedText)
      return true
    } catch (error) {
      console.warn('Clipboard API 复制失败，尝试降级方案:', error)
    }
  }

  return fallbackCopy(normalizedText)
}

function fallbackCopy(text) {
  const textarea = document.createElement('textarea')
  textarea.value = text
  textarea.setAttribute('readonly', 'readonly')
  textarea.style.position = 'fixed'
  textarea.style.top = '0'
  textarea.style.left = '0'
  textarea.style.width = '1px'
  textarea.style.height = '1px'
  textarea.style.padding = '0'
  textarea.style.border = '0'
  textarea.style.opacity = '0'
  textarea.style.pointerEvents = 'none'
  textarea.style.zIndex = '-1'

  document.body.appendChild(textarea)
  textarea.focus()
  textarea.select()
  textarea.setSelectionRange(0, textarea.value.length)

  let success = false
  try {
    success = document.execCommand('copy')
  } catch (error) {
    console.error('execCommand 复制失败:', error)
  }

  document.body.removeChild(textarea)
  return success
}

export async function copyWithMessage(text, ElMessage, successMsg = '已复制', errorMsg = '复制失败，请手动复制') {
  const success = await copyToClipboard(text)
  if (success) {
    ElMessage.success(successMsg)
    return true
  }

  ElMessage.error(errorMsg)
  return false
}
