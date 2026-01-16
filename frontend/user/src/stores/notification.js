import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getUnreadCount } from '@/api/notification'

export const useNotificationStore = defineStore('notification', () => {
  const unreadCounts = ref({
    total: 0,
    like_article: 0,
    favorite_article: 0,
    comment: 0,
    reply: 0,
    follow: 0,
    system: 0
  })

  // 防抖标记
  let fetchTimer = null
  let lastFetchTime = 0
  const FETCH_DEBOUNCE = 2000 // 2秒内不重复请求

  const totalUnread = computed(() => unreadCounts.value.total)
  
  // 互动消息未读数（点赞+收藏+评论+回复）
  const interactionUnread = computed(() => 
    unreadCounts.value.like_article + 
    unreadCounts.value.favorite_article + 
    unreadCounts.value.comment + 
    unreadCounts.value.reply
  )

  // 获取未读数（带防抖）
  async function fetchUnreadCount(force = false) {
    const now = Date.now()
    
    // 防抖：2秒内不重复请求（除非强制）
    if (!force && now - lastFetchTime < FETCH_DEBOUNCE) {
      return
    }
    
    // 清除之前的定时器
    if (fetchTimer) {
      clearTimeout(fetchTimer)
      fetchTimer = null
    }
    
    lastFetchTime = now
    
    try {
      const res = await getUnreadCount()
      if (res.code === 200) {
        unreadCounts.value = {
          total: res.data.total || 0,
          like_article: res.data.like_article || 0,
          favorite_article: res.data.favorite_article || 0,
          comment: res.data.comment || 0,
          reply: res.data.reply || 0,
          follow: res.data.follow || 0,
          system: res.data.system || 0
        }
      }
    } catch (e) {
      console.error('获取未读数失败:', e)
    }
  }

  // 延迟获取未读数（用于批量更新后）
  function debouncedFetch() {
    if (fetchTimer) {
      clearTimeout(fetchTimer)
    }
    fetchTimer = setTimeout(() => {
      fetchUnreadCount(true)
    }, 500)
  }

  // 增加未读数（乐观更新）
  function incrementUnread(type) {
    const key = type.toLowerCase()
    if (unreadCounts.value[key] !== undefined) {
      unreadCounts.value[key]++
      unreadCounts.value.total++
    }
  }

  // 减少未读数
  function decrementUnread(type) {
    const key = type.toLowerCase()
    if (unreadCounts.value[key] !== undefined && unreadCounts.value[key] > 0) {
      unreadCounts.value[key]--
      unreadCounts.value.total = Math.max(0, unreadCounts.value.total - 1)
    }
  }

  // 清空指定类型的未读数
  function clearUnread(type) {
    if (type) {
      const key = type.toLowerCase()
      if (unreadCounts.value[key] !== undefined) {
        unreadCounts.value.total = Math.max(0, unreadCounts.value.total - unreadCounts.value[key])
        unreadCounts.value[key] = 0
      }
    } else {
      Object.keys(unreadCounts.value).forEach(key => {
        unreadCounts.value[key] = 0
      })
    }
  }

  // 重置所有未读数
  function resetUnread() {
    unreadCounts.value = {
      total: 0,
      like_article: 0,
      favorite_article: 0,
      comment: 0,
      reply: 0,
      follow: 0,
      system: 0
    }
    lastFetchTime = 0
  }

  return {
    unreadCounts,
    totalUnread,
    interactionUnread,
    fetchUnreadCount,
    debouncedFetch,
    incrementUnread,
    decrementUnread,
    clearUnread,
    resetUnread
  }
})
