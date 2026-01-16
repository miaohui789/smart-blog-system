<template>
  <router-view />
</template>

<script setup>
import { onMounted, watch, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'
import { useNotificationStore } from '@/stores/notification'
import { useMessageStore } from '@/stores/message'
import { initWebSocket, closeWebSocket, onNotification, onForceLogout, onUnreadUpdate, onMessage } from '@/utils/websocket'
import { ElMessage, ElNotification } from 'element-plus'

const userStore = useUserStore()
const themeStore = useThemeStore()
const notificationStore = useNotificationStore()
const messageStore = useMessageStore()

// 存储取消订阅函数
let unsubscribes = []

// 页面加载时，如果已登录则刷新用户信息
onMounted(() => {
  if (userStore.isLoggedIn) {
    userStore.fetchUserInfo().then(() => {
      initWebSocketConnection()
    })
  }
})

// 监听登录状态变化
watch(() => userStore.isLoggedIn, (isLoggedIn) => {
  if (isLoggedIn && userStore.userInfo?.id) {
    initWebSocketConnection()
  } else {
    cleanupWebSocket()
  }
})

// 监听用户信息变化（登录后获取到用户ID）
watch(() => userStore.userInfo?.id, (newId, oldId) => {
  if (newId && newId !== oldId && userStore.isLoggedIn) {
    initWebSocketConnection()
  }
})

// 清理WebSocket连接和回调
function cleanupWebSocket() {
  // 取消所有订阅
  unsubscribes.forEach(unsub => unsub && unsub())
  unsubscribes = []
  closeWebSocket()
  notificationStore.resetUnread()
  messageStore.resetUnread()
}

// 初始化WebSocket连接
function initWebSocketConnection() {
  if (!userStore.userInfo?.id) return
  
  // 先清理旧的订阅
  unsubscribes.forEach(unsub => unsub && unsub())
  unsubscribes = []
  
  initWebSocket(userStore.userInfo.id)
  
  // 获取未读通知数
  notificationStore.fetchUnreadCount()
  // 获取未读私信数
  messageStore.fetchUnreadCount()
  
  // 监听新通知 - 使用桌面通知
  unsubscribes.push(onNotification((notification) => {
    // 立即更新未读数
    notificationStore.incrementUnread(notification.type)
    
    // 显示通知提示（使用Notification组件，更醒目）
    ElNotification({
      title: notification.title || '新消息',
      message: notification.content,
      type: 'info',
      duration: 4000,
      position: 'top-right',
      onClick: () => {
        // 点击通知跳转
        if (notification.targetType === 'article' && notification.targetId) {
          window.location.href = `/article/${notification.targetId}`
        }
      }
    })
  }))
  
  // 监听私信 - 实时更新私信未读数
  unsubscribes.push(onMessage((message) => {
    // 如果正在和发送者聊天，不显示通知，也不增加未读数
    if (messageStore.isChattingWith(message.senderId)) {
      return
    }
    
    // 增加私信未读数
    messageStore.incrementUnread()
    
    // 显示私信通知
    ElNotification({
      title: '新私信',
      message: message.content || '您收到一条新私信',
      type: 'info',
      duration: 4000,
      position: 'top-right',
      onClick: () => {
        window.location.href = '/message'
      }
    })
  }))
  
  // 监听未读数更新
  unsubscribes.push(onUnreadUpdate(() => {
    notificationStore.fetchUnreadCount()
    messageStore.fetchUnreadCount()
  }))
  
  // 监听强制下线
  unsubscribes.push(onForceLogout((data) => {
    ElMessage.warning(data.message || '账号已在其他设备登录')
    userStore.logout()
  }))
}

onUnmounted(() => {
  cleanupWebSocket()
})
</script>

<style lang="scss">
#app {
  min-height: 100vh;
  background-color: var(--bg-dark);
  color: var(--text-secondary);
  transition: background-color 0.3s, color 0.3s;
}
</style>
