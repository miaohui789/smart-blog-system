import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUnreadCount } from '@/api/message'

export const useMessageStore = defineStore('message', () => {
  const unreadCount = ref(0)
  // 当前正在聊天的用户ID
  const currentChatUserId = ref(null)
  
  // 防抖标记
  let fetchTimer = null
  let lastFetchTime = 0
  const FETCH_DEBOUNCE = 2000

  // 获取未读私信数
  async function fetchUnreadCount(force = false) {
    const now = Date.now()
    
    if (!force && now - lastFetchTime < FETCH_DEBOUNCE) {
      return
    }
    
    if (fetchTimer) {
      clearTimeout(fetchTimer)
      fetchTimer = null
    }
    
    lastFetchTime = now
    
    try {
      const res = await getUnreadCount()
      if (res.code === 200) {
        unreadCount.value = res.data || 0
      }
    } catch (e) {
      console.error('获取私信未读数失败:', e)
    }
  }

  // 设置当前聊天用户
  function setCurrentChatUser(userId) {
    currentChatUserId.value = userId
  }

  // 清除当前聊天用户
  function clearCurrentChatUser() {
    currentChatUserId.value = null
  }

  // 检查是否正在和某用户聊天
  function isChattingWith(userId) {
    return currentChatUserId.value === userId
  }

  // 增加未读数
  function incrementUnread() {
    unreadCount.value++
  }

  // 减少未读数
  function decrementUnread(count = 1) {
    unreadCount.value = Math.max(0, unreadCount.value - count)
  }

  // 重置未读数
  function resetUnread() {
    unreadCount.value = 0
    currentChatUserId.value = null
    lastFetchTime = 0
  }

  return {
    unreadCount,
    currentChatUserId,
    fetchUnreadCount,
    setCurrentChatUser,
    clearCurrentChatUser,
    isChattingWith,
    incrementUnread,
    decrementUnread,
    resetUnread
  }
})
