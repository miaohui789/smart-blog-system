<template>
  <div class="message-page">
    <div class="message-container glass-card">
      <!-- 会话列表 -->
      <div class="conversation-list" :class="{ 'mobile-hidden': activeConversation }">
        <div class="list-header">
          <button class="back-btn" @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
          </button>
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
              <el-avatar :size="40" :src="conv.targetUser?.avatar">
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
            <!-- 删除会话按钮 -->
            <button 
              class="conv-delete-btn" 
              @click.stop="deleteConversation(conv)"
              title="删除会话"
            >
              <el-icon><Close /></el-icon>
            </button>
          </div>
          
          <el-empty v-if="!loading && !conversations.length" description="暂无私信" :image-size="80" />
        </div>
      </div>

      <!-- 聊天窗口 -->
      <div class="chat-window" :class="{ 'mobile-show': activeConversation }">
        <template v-if="activeConversation">
          <!-- 聊天头部 -->
          <div class="chat-header">
            <button class="mobile-back" @click="closeChatWindow">
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
            
            <template v-for="(msg, index) in messages">
              <!-- 时间分隔线 - 显示在消息组上方 -->
              <div 
                v-if="shouldShowTime(msg, index)" 
                :key="'t-' + msg.id" 
                class="time-divider"
              >
                {{ formatMessageTime(msg.createTime) }}
              </div>
              
              <!-- 撤回消息提示 -->
              <div v-if="msg.isWithdrawn" :key="'w-' + msg.id" class="withdrawn-notice">
                {{ msg.isSelf ? '你撤回了一条消息' : '"' + (activeConversation?.targetUser?.nickname || '对方') + '"撤回了一条消息' }}
              </div>
              
              <!-- 正常消息 -->
              <div 
                v-else
                :key="msg.id"
                class="message-row"
                :class="{ 'is-self': msg.isSelf }"
              >
                <!-- 对方消息：头像在左边 -->
                <el-avatar 
                  v-if="!msg.isSelf" 
                  :size="36" 
                  :src="msg.sender?.avatar || ''"
                  class="msg-avatar"
                >
                  {{ msg.sender?.nickname?.charAt(0) || 'U' }}
                </el-avatar>
                
                <div 
                  class="msg-bubble"
                  @contextmenu.prevent.stop="showContextMenu($event, msg)"
                >{{ msg.content }}</div>
                
                <!-- 自己消息：头像在右边 -->
                <el-avatar 
                  v-if="msg.isSelf" 
                  :size="36" 
                  :src="msg.sender?.avatar || userStore.userInfo?.avatar || ''"
                  class="msg-avatar"
                >
                  {{ msg.sender?.nickname?.charAt(0) || userStore.userInfo?.nickname?.charAt(0) || 'U' }}
                </el-avatar>
              </div>
            </template>
          </div>

          <!-- 右键菜单 -->
          <Teleport to="body">
            <Transition name="context-menu">
              <div 
                v-if="contextMenu.visible" 
                class="message-context-menu"
                :style="{ left: contextMenu.x + 'px', top: contextMenu.y + 'px' }"
                @click.stop
              >
                <div 
                  v-if="contextMenu.message?.isSelf" 
                  class="menu-item"
                  @click="withdrawMessage"
                >
                  撤回
                </div>
                <div class="menu-item" @click="deleteMessage">
                  删除
                </div>
              </div>
            </Transition>
          </Teleport>

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
import { ArrowLeft, ChatDotRound, Promotion, Delete, RefreshLeft, Close } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getConversations, 
  getMessages, 
  sendMessage as sendMessageApi, 
  markAsRead,
  getUnreadCount,
  getConversationWith,
  deleteMessage as deleteMessageApi,
  withdrawMessage as withdrawMessageApi,
  deleteConversation as deleteConversationApi
} from '@/api/message'
import { useUserStore } from '@/stores/user'
import { useMessageStore } from '@/stores/message'
import { formatRelativeTime } from '@/utils/format'
import VipUsername from '@/components/VipUsername/index.vue'
import { initMessageWebSocket, closeMessageWebSocket, onNewMessage } from '@/utils/messageWebSocket'
import { onMessageWithdraw } from '@/utils/websocket'

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

// 右键菜单
const contextMenu = ref({
  visible: false,
  x: 0,
  y: 0,
  message: null
})

// 显示右键菜单
function showContextMenu(event, msg) {
  // 撤回的消息不显示菜单
  if (!msg || msg.isWithdrawn) {
    hideContextMenu()
    return
  }
  
  // 阻止默认右键菜单
  event.preventDefault()
  event.stopPropagation()
  
  contextMenu.value = {
    visible: true,
    x: event.clientX,
    y: event.clientY,
    message: msg
  }
}

// 隐藏右键菜单
function hideContextMenu() {
  contextMenu.value.visible = false
  contextMenu.value.message = null
}

// 撤回消息
async function withdrawMessage() {
  const msg = contextMenu.value.message
  if (!msg) return
  
  try {
    const res = await withdrawMessageApi(msg.id)
    if (res.code === 200) {
      // 更新消息状态为已撤回
      msg.isWithdrawn = true
      msg.content = ''
      ElMessage.success('消息已撤回')
    } else {
      ElMessage.error(res.message || '撤回失败')
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '撤回失败，可能已超过撤回时限')
  } finally {
    hideContextMenu()
  }
}

// 删除消息
async function deleteMessage() {
  const msg = contextMenu.value.message
  if (!msg) return
  
  // 先关闭右键菜单
  hideContextMenu()
  
  try {
    await ElMessageBox.confirm('确定删除这条消息吗？', '提示', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await deleteMessageApi(msg.id)
    if (res.code === 200) {
      // 从列表中移除
      const index = messages.value.findIndex(m => m.id === msg.id)
      if (index > -1) {
        messages.value.splice(index, 1)
      }
      ElMessage.success('消息已删除')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 删除会话
async function deleteConversation(conv) {
  try {
    await ElMessageBox.confirm('确定删除与该用户的会话吗？删除后聊天记录将无法恢复', '提示', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await deleteConversationApi(conv.id)
    if (res.code === 200) {
      // 从列表中移除
      const index = conversations.value.findIndex(c => c.id === conv.id)
      if (index > -1) {
        conversations.value.splice(index, 1)
      }
      // 如果删除的是当前会话，清空聊天窗口
      if (activeConversation.value?.id === conv.id) {
        activeConversation.value = null
        messages.value = []
      }
      ElMessage.success('会话已删除')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 点击其他地方关闭菜单
function handleClickOutside(event) {
  if (contextMenu.value.visible) {
    hideContextMenu()
  }
}

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
  }
}

// 关闭聊天窗口（移动端返回）
function closeChatWindow() {
  activeConversation.value = null
  messageStore.clearCurrentChatUser()
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

// 格式化消息时间（用于时间分隔线）
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

// 判断是否显示时间分隔线（间隔超过5分钟显示）
function shouldShowTime(msg, index) {
  if (index === 0) return true
  const prevMsg = messages.value[index - 1]
  if (!prevMsg || !msg.createTime || !prevMsg.createTime) return false
  
  const current = new Date(msg.createTime).getTime()
  const prev = new Date(prevMsg.createTime).getTime()
  return current - prev > 5 * 60 * 1000 // 5分钟
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
  
  // 设置当前聊天用户，用于屏蔽该用户的私信通知
  messageStore.setCurrentChatUser(conv.targetUser?.id)
  
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

// 处理消息撤回通知
function handleMessageWithdraw(messageId) {
  // 在当前消息列表中查找并更新
  const msg = messages.value.find(m => m.id === messageId)
  if (msg) {
    msg.isWithdrawn = true
    msg.content = ''
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
  // 添加全宽页面类，隐藏侧边栏
  document.body.classList.add('full-width-page')
  
  await fetchConversations()
  await fetchUnreadCount()
  await initFromRoute()
  
  // 初始化WebSocket监听
  if (userStore.userInfo?.id) {
    initMessageWebSocket(userStore.userInfo.id)
    onNewMessage(handleNewMessage)
    // 监听消息撤回
    onMessageWithdraw(handleMessageWithdraw)
  }
  
  // 监听点击事件关闭右键菜单
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  // 移除全宽页面类
  document.body.classList.remove('full-width-page')
  
  closeMessageWebSocket()
  document.removeEventListener('click', handleClickOutside)
  // 清除当前聊天用户
  messageStore.clearCurrentChatUser()
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
  height: calc(100vh - 140px);
  max-height: 700px;
  min-height: 400px;
  margin: 0 auto;
}

.message-container {
  display: flex;
  height: 100%;
  overflow: hidden;
}

.conversation-list {
  width: 260px;
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.list-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-color);
  
  h3 {
    font-size: 16px;
    font-weight: 600;
    color: var(--text-primary);
    flex: 1;
  }
}

.back-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  background: transparent;
  border: none;
  border-radius: 6px;
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    background: var(--bg-card-hover);
    color: var(--text-primary);
  }
  
  .el-icon {
    font-size: 16px;
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
  gap: 10px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.2s;
  position: relative;
  
  &:hover {
    background: var(--bg-card-hover);
    
    .conv-delete-btn {
      opacity: 1;
    }
  }
  
  &.active {
    background: rgba($primary-color, 0.1);
  }
}

// 删除会话按钮
.conv-delete-btn {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  width: 22px;
  height: 22px;
  border: none;
  background: transparent;
  color: var(--text-muted);
  cursor: pointer;
  opacity: 0;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  
  &:hover {
    background: rgba(239, 68, 68, 0.1);
    color: #ef4444;
  }
  
  .el-icon {
    font-size: 12px;
  }
}

.conv-avatar {
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-card-hover);
  
  // 确保头像完全圆形
  :deep(.el-avatar) {
    width: 40px !important;
    height: 40px !important;
    min-width: 40px;
    min-height: 40px;
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
      width: 38px !important;
      height: 38px !important;
      min-width: 38px;
      min-height: 38px;
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
  margin-bottom: 2px;
}

.conv-name {
  font-size: 13px;
}

.conv-time {
  font-size: 11px;
  color: var(--text-disabled);
}

.conv-preview {
  font-size: 12px;
  color: var(--text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.unread-dot {
  position: absolute;
  right: 14px;
  top: 50%;
  transform: translateY(-30%);
  background: linear-gradient(135deg, #ef4444, #dc2626);
  color: #fff;
  font-size: 10px;
  font-weight: 600;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: 8px;
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

// 时间分隔线
.time-divider {
  text-align: center;
  padding: 16px 0;
  font-size: 12px;
  color: var(--text-disabled);
}

// 撤回消息提示
.withdrawn-notice {
  text-align: center;
  padding: 8px 0;
  font-size: 12px;
  color: #999;
}

// 消息行
.message-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 6px 0;
  
  // 自己发送的消息 - 靠右显示
  &.is-self {
    justify-content: flex-end;
    
    .msg-bubble {
      background: linear-gradient(135deg, #1890ff, #096dd9);
      color: #fff;
      border-radius: 18px 4px 18px 18px;
      order: 1;
    }
    
    .msg-avatar {
      order: 2;
    }
  }
  
  // 对方消息 - 靠左显示
  &:not(.is-self) {
    justify-content: flex-start;
    
    .msg-bubble {
      background: #f5f5f5;
      color: #333;
      border-radius: 4px 18px 18px 18px;
    }
  }
}

// 暗色主题适配
:root[data-theme="dark"] {
  .withdrawn-notice {
    color: var(--text-disabled);
  }
  
  .message-row:not(.is-self) .msg-bubble {
    background: var(--bg-card-hover);
    color: var(--text-primary);
  }
}

.msg-avatar {
  flex-shrink: 0;
  background: var(--bg-card-hover) !important;
  
  // 确保头像有默认背景色，防止加载时闪灰
  :deep(.el-avatar) {
    background: var(--bg-card-hover);
    color: var(--text-muted);
  }
}

.msg-bubble {
  max-width: 60%;
  padding: 10px 14px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
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

// 右键菜单样式（全局）
:global(.message-context-menu) {
  position: fixed;
  z-index: 9999;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.12);
  padding: 4px 0;
  min-width: 80px;
  
  .menu-item {
    padding: 8px 16px;
    font-size: 14px;
    color: #333;
    cursor: pointer;
    text-align: center;
    transition: background 0.15s;
    
    &:hover {
      background: #f5f5f5;
    }
  }
}

:root[data-theme="dark"] :global(.message-context-menu) {
  background: var(--bg-card);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.3);
  
  .menu-item {
    color: var(--text-primary);
    
    &:hover {
      background: var(--bg-card-hover);
    }
  }
}

// 右键菜单动画
.context-menu-enter-active,
.context-menu-leave-active {
  transition: all 0.15s ease;
}

.context-menu-enter-from,
.context-menu-leave-to {
  opacity: 0;
  transform: scale(0.95);
}
</style>
