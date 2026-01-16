import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUnreadCount } from '@/api/message'

export const useMessageStore = defineStore('message', () => {
  const unreadCount = ref(0)
  
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
    lastFetchTime = 0
  }

  return {
    unreadCount,
    fetchUnreadCount,
    incrementUnread,
    decrementUnread,
    resetUnread
  }
})
