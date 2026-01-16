<template>
  <div class="message-page">
    <!-- 返回按钮 -->
    <div class="back-nav">
      <button class="back-btn" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回</span>
      </button>
    </div>

    <div class="message-container glass-card">
      <!-- 会话列表 -->
      <div class="conversation-list" :class="{ 'mobile-hidden': activeConversation }">
        <div class="list-header">
          <h3>私信</h3>
          <span class="unread-badge" v-if="totalUnread > 0">{{ totalUnread }}</span>
        </div>
        
        <div class="conversations">
          <div 
            v-for="conv in conversations" 
            :key="conv.id"
            class="conversation-item"
            :class="{ active: activeConversation?.id === conv.id }"
            @click="selectConversation(conv)"
          >
            <div class="conv-avatar" :class="{ 'vip-avatar': conv.targetUser?.vipLevel > 0, ['vip-level-' + conv.targetUser?.vipLevel]: conv.targetUser?.vipLevel > 0 }">
              <el-avatar :size="48" :src="conv.targetUser?.avatar">
                {{ conv.targetUser?.nickname?.charAt(0) || 'U' }}
              </el-avatar>
            </div>
            <div class="conv-info">
              <div class="conv-header">
                <VipUsername 
                  :username="conv.targetUser?.nickname || '未知用户'" 
                  :vip-level="conv.targetUser?.vipLevel || 0"
                  class="conv-name"
                />
                <span class="conv-time">{{ formatTime(conv.lastMessageTime) }}</span>
              </div>
              <p class="conv-preview">{{ conv.lastMessage || '暂无消息' }}</p>
            </div>
            <span v-if="conv.unreadCount > 0" class="unread-dot">{{ conv.unreadCount > 99 ? '99+' : conv.unreadCount }}</span>
          </div>
          
          <el-empty v-if="!loading && !conversations.length" description="暂无私信" :image-size="80" />
        </div>
      </div>

      <!-- 聊天窗口 -->
      <div class="chat-window" :class="{ 'mobile-show': activeConversation }">
        <template v-if="activeConversation">
          <!-- 聊天头部 -->
          <div class="chat-header">
            <button class="mobile-back" @click="activeConversation = null">
              <el-icon><ArrowLeft /></el-icon>
            </button>
            <div class="chat-user" @click="goUserProfile">
              <el-avatar :size="36" :src="activeConversation.targetUser?.avatar">
                {{ activeConversation.targetUser?.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <VipUsername 
                :username="activeConversation.targetUser?.nickname || '未知用户'" 
                :vip-level="activeConversation.targetUser?.vipLevel || 0"
              />
            </div>
          </div>

          <!-- 消息列表 -->
          <div class="chat-messages" ref="messagesRef">
            <div class="load-more" v-if="hasMore">
              <el-button text @click="loadMoreMessages" :loading="loadingMore">加载更多</el-button>
            </div>
            <div 
              v-for="msg in messages" 
              :key="msg.id"
              class="message-item"
              :class="{ self: msg.isSelf }"
            >
              <el-avatar 
                v-if="!msg.isSelf" 
                :size="36" 
                :src="msg.sender?.avatar"
                class="msg-avatar"
              >
                {{ msg.sender?.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <div class="msg-content">
                <div class="msg-bubble">{{ msg.content }}</div>
                <span class="msg-time">{{ formatTime(msg.createTime) }}</span>
              </div>
              <el-avatar 
                v-if="msg.isSelf" 
                :size="36" 
                :src="userStore.userInfo?.avatar"
                class="msg-avatar"
              >
                {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
              </el-avatar>
            </div>
          </div>

          <!-- 输入框 -->
          <div class="chat-input">
            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="2"
              placeholder="输入消息..."
              resize="none"
              @keydown.enter.exact.prevent="sendMessage"
            />
            <button class="send-btn" @click="sendMessage" :disabled="!inputMessage.trim() || sending">
              <el-icon><Promotion /></el-icon>
            </button>
          </div>
        </template>

        <div v-else class="no-chat">
          <el-icon :size="64"><ChatDotRound /></el-icon>
          <p>选择一个会话开始聊天</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, ChatDotRound, Promotion } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { 
  getConversations, 
  getMessages, 
  sendMessage as sendMessageApi, 
  markAsRead,
  getUnreadCount,
  getConversationWith
} from '@/api/message'
import { useUserStore } from '@/stores/user'
import { useMessageStore } from '@/stores/message'
import { formatRelativeTime } from '@/utils/format'
import VipUsername from '@/components/VipUsername/index.vue'
import { initMessageWebSocket, closeMessageWebSocket, onNewMessage } from '@/utils/messageWebSocket'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const messageStore = useMessageStore()

const loading = ref(true)
const conversations = ref([])
const activeConversation = ref(null)
const messages = ref([])
const inputMessage = ref('')
const sending = ref(false)
const messagesRef = ref(null)
const totalUnread = ref(0)
const currentPage = ref(1)
const hasMore = ref(false)
const loadingMore = ref(false)

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
  }
}

function goUserProfile() {
  if (activeConversation.value?.targetUser?.id) {
    router.push(`/user/${activeConversation.value.targetUser.id}`)
  }
}

function formatTime(time) {
  if (!time) return ''
  return formatRelativeTime(time)
}

async function fetchConversations() {
  loading.value = true
  try {
    const res = await getConversations()
    if (res.code === 200) {
      conversations.value = res.data || []
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function fetchUnreadCount() {
  try {
    const res = await getUnreadCount()
    if (res.code === 200) {
      totalUnread.value = res.data || 0
    }
  } catch (e) {
    console.error(e)
  }
}

async function selectConversation(conv) {
  activeConversation.value = conv
  currentPage.value = 1
  messages.value = []
  await fetchMessages()
  
  // 标记已读
  if (conv.unreadCount > 0) {
    const prevUnread = conv.unreadCount
    await markAsRead(conv.id)
    conv.unreadCount = 0
    // 更新全局未读数
    messageStore.decrementUnread(prevUnread)
    fetchUnreadCount()
  }
  
  scrollToBottom()
}

async function fetchMessages() {
  try {
    const res = await getMessages(activeConversation.value.id, {
      page: currentPage.value,
      pageSize: 20
    })
    if (res.code === 200) {
      const newMessages = (res.data || []).reverse()
      if (currentPage.value === 1) {
        messages.value = newMessages
      } else {
        messages.value = [...newMessages, ...messages.value]
      }
      hasMore.value = newMessages.length === 20
    }
  } catch (e) {
    console.error(e)
  }
}

async function loadMoreMessages() {
  loadingMore.value = true
  currentPage.value++
  await fetchMessages()
  loadingMore.value = false
}

async function sendMessage() {
  if (!inputMessage.value.trim() || sending.value) return
  
  sending.value = true
  try {
    const res = await sendMessageApi({
      receiverId: activeConversation.value.targetUser.id,
      content: inputMessage.value.trim(),
      type: 1
    })
    if (res.code === 200) {
      // 添加到消息列表
      messages.value.push({
        id: res.data.id,
        content: inputMessage.value.trim(),
        senderId: userStore.userInfo.id,
        isSelf: true,
        createTime: new Date().toISOString(),
        sender: {
          id: userStore.userInfo.id,
          nickname: userStore.userInfo.nickname,
          avatar: userStore.userInfo.avatar
        }
      })
      inputMessage.value = ''
      scrollToBottom()
      
      // 更新会话列表
      activeConversation.value.lastMessage = res.data.content
      activeConversation.value.lastMessageTime = new Date().toISOString()
    }
  } catch (e) {
    ElMessage.error('发送失败')
  } finally {
    sending.value = false
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

// 处理收到新消息
function handleNewMessage(message) {
  // 如果是当前会话的消息
  if (activeConversation.value && message.senderId === activeConversation.value.targetUser.id) {
    messages.value.push({
      ...message,
      isSelf: false,
      sender: activeConversation.value.targetUser
    })
    scrollToBottom()
    // 标记已读
    markAsRead(activeConversation.value.id)
  } else {
    // 更新会话列表未读数
    const conv = conversations.value.find(c => 
      c.targetUser.id === message.senderId
    )
    if (conv) {
      conv.unreadCount = (conv.unreadCount || 0) + 1
      conv.lastMessage = message.content
      conv.lastMessageTime = message.createTime
    }
    totalUnread.value++
    // 更新全局store
    messageStore.incrementUnread()
  }
}

// 如果URL带有userId参数，直接打开与该用户的会话
async function initFromRoute() {
  const targetUserId = route.params.userId
  if (targetUserId) {
    try {
      const res = await getConversationWith(targetUserId)
      if (res.code === 200) {
        // 查找或创建会话
        await fetchConversations()
        const conv = conversations.value.find(c => c.targetUser?.id === Number(targetUserId))
        if (conv) {
          selectConversation(conv)
        }
      }
    } catch (e) {
      console.error(e)
    }
  }
}

onMounted(async () => {
  await fetchConversations()
  await fetchUnreadCount()
  await initFromRoute()
  
  // 初始化WebSocket监听
  if (userStore.userInfo?.id) {
    initMessageWebSocket(userStore.userInfo.id)
    onNewMessage(handleNewMessage)
  }
})

onUnmounted(() => {
  closeMessageWebSocket()
})

watch(() => route.params.userId, async (newUserId) => {
  if (newUserId) {
    await initFromRoute()
  }
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.message-page {
  height: calc(100vh - 180px);
  min-height: 500px;
}

.back-nav {
  margin-bottom: 20px;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  color: var(--text-secondary);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    border-color: $primary-color;
    color: $primary-color;
  }
}

.message-container {
  display: flex;
  height: calc(100% - 60px);
  overflow: hidden;
}

.conversation-list {
  width: 320px;
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.list-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 20px;
  border-bottom: 1px solid var(--border-color);
  
  h3 {
    font-size: 18px;
    font-weight: 600;
    color: var(--text-primary);
  }
}

.unread-badge {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  color: #fff;
  font-size: 11px;
  font-weight: 500;
  min-width: 18px;
  height: 18px;
  padding: 0 6px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 6px rgba(239, 68, 68, 0.4);
}

.conversations {
  flex: 1;
  overflow-y: auto;
}

.conversation-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  cursor: pointer;
  transition: background 0.2s;
  position: relative;
  
  &:hover {
    background: var(--bg-card-hover);
  }
  
  &.active {
    background: rgba($primary-color, 0.1);
  }
}

.conv-avatar {
  flex-shrink: 0;
  width: 52px;
  height: 52px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-card-hover);
  
  // 确保头像完全圆形
  :deep(.el-avatar) {
    width: 48px !important;
    height: 48px !important;
    min-width: 48px;
    min-height: 48px;
    border-radius: 50% !important;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      border-radius: 50%;
    }
  }
  
  &.vip-avatar {
    background: transparent;
    
    &.vip-level-1 {
      background: linear-gradient(135deg, #cd7f32, #b8860b);
      padding: 2px;
    }
    &.vip-level-2 {
      background: linear-gradient(135deg, #c0c0c0, #a8a8a8);
      padding: 2px;
    }
    &.vip-level-3 {
      background: linear-gradient(135deg, #ffd700, #ffb700);
      padding: 2px;
    }
    
    :deep(.el-avatar) {
      width: 46px !important;
      height: 46px !important;
      min-width: 46px;
      min-height: 46px;
    }
  }
}

.conv-info {
  flex: 1;
  min-width: 0;
}

.conv-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.conv-name {
  font-size: 14px;
}

.conv-time {
  font-size: 12px;
  color: var(--text-disabled);
}

.conv-preview {
  font-size: 13px;
  color: var(--text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.unread-dot {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-30%);
  background: linear-gradient(135deg, #ef4444, #dc2626);
  color: #fff;
  font-size: 10px;
  font-weight: 600;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 6px rgba(239, 68, 68, 0.4);
  line-height: 1;
}

.chat-window {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-color);
}

.mobile-back {
  display: none;
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 8px;
  
  &:hover {
    color: $primary-color;
  }
}

.chat-user {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  
  &:hover {
    opacity: 0.8;
  }
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.load-more {
  text-align: center;
  margin-bottom: 10px;
}

.message-item {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  
  &.self {
    flex-direction: row-reverse;
    
    .msg-bubble {
      background: $primary-color;
      color: #fff;
    }
    
    .msg-time {
      text-align: right;
    }
  }
}

.msg-avatar {
  flex-shrink: 0;
}

.msg-content {
  max-width: 70%;
}

.msg-bubble {
  padding: 12px 16px;
  background: var(--bg-card-hover);
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
}

.msg-time {
  font-size: 11px;
  color: var(--text-disabled);
  margin-top: 4px;
  padding: 0 4px;
}

.chat-input {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid var(--border-color);
  
  :deep(.el-textarea__inner) {
    min-height: 44px !important;
  }
}

.send-btn {
  width: 44px;
  height: 44px;
  background: $primary-color;
  border: none;
  border-radius: 10px;
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  flex-shrink: 0;
  
  &:hover:not(:disabled) {
    background: $primary-dark;
  }
  
  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
  
  .el-icon {
    font-size: 20px;
  }
}

.no-chat {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--text-disabled);
  
  p {
    margin-top: 16px;
    font-size: 14px;
  }
}

@media (max-width: 768px) {
  .conversation-list {
    width: 100%;
    border-right: none;
    
    &.mobile-hidden {
      display: none;
    }
  }
  
  .chat-window {
    display: none;
    
    &.mobile-show {
      display: flex;
    }
  }
  
  .mobile-back {
    display: block;
  }
}
</style>
