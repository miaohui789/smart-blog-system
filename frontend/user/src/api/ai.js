import request from '@/utils/request'

// 获取AI状态
export function getAiStatus() {
  return request({ url: '/ai/status', method: 'get' })
}

// 获取会话列表
export function getConversations() {
  return request({ url: '/ai/conversations', method: 'get' })
}

// 创建新会话
export function createConversation(title) {
  return request({ url: '/ai/conversations', method: 'post', data: { title } })
}

// 获取会话消息
export function getMessages(conversationId) {
  return request({ url: `/ai/conversations/${conversationId}/messages`, method: 'get' })
}

// 删除会话
export function deleteConversation(conversationId) {
  return request({ url: `/ai/conversations/${conversationId}`, method: 'delete' })
}

// 更新会话标题
export function updateConversationTitle(conversationId, title) {
  return request({ url: `/ai/conversations/${conversationId}/title`, method: 'put', data: { title } })
}

// 发送消息（SSE流式）- 返回EventSource
export function sendMessage(conversationId, message, onToken, onComplete, onError) {
  const baseUrl = ''  // 使用相对路径，由nginx代理
  const token = localStorage.getItem('blog_token')
  
  return fetch(`${baseUrl}/api/ai/chat`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : ''
    },
    body: JSON.stringify({ conversationId, message })
  }).then(response => {
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    
    function read() {
      reader.read().then(({ done, value }) => {
        if (done) {
          onComplete && onComplete()
          return
        }
        
        const text = decoder.decode(value)
        const lines = text.split('\n')
        
        for (const line of lines) {
          if (line.startsWith('data:')) {
            const data = line.slice(5).trim()
            if (data) {
              onToken && onToken(data)
            }
          } else if (line.startsWith('event:')) {
            const event = line.slice(6).trim()
            if (event === 'error') {
              // 下一行是错误数据
            } else if (event === 'done') {
              onComplete && onComplete()
              return
            }
          }
        }
        
        read()
      }).catch(err => {
        onError && onError(err.message)
      })
    }
    
    read()
  }).catch(err => {
    onError && onError(err.message)
  })
}
