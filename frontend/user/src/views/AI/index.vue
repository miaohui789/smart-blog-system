<template>
  <div class="ai-container">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <!-- 顶部返回首页 -->
      <div class="sidebar-top">
        <router-link to="/" class="back-home">
          <el-icon><HomeFilled /></el-icon>
          <span v-if="!sidebarCollapsed">返回首页</span>
        </router-link>
      </div>
      
      <!-- 按钮组 -->
      <div class="sidebar-header">
        <button class="new-chat-btn" @click="createNewChat">
          <el-icon><Plus /></el-icon>
          <span v-if="!sidebarCollapsed">新对话</span>
        </button>
        <button class="collapse-btn" @click="sidebarCollapsed = !sidebarCollapsed">
          <el-icon><Fold v-if="!sidebarCollapsed" /><Expand v-else /></el-icon>
        </button>
      </div>

      <!-- 对话列表 -->
      <div class="conversation-list" v-if="!sidebarCollapsed">
        <div class="date-group" v-for="(group, date) in groupedConversations" :key="date">
          <div class="date-label">{{ date }}</div>
          <div 
            v-for="conv in group" 
            :key="conv.id"
            class="conversation-item"
            :class="{ active: currentConversation?.id === conv.id }"
            @click="selectConversation(conv)"
          >
            <span class="conv-title">{{ conv.title || '新对话' }}</span>
            <el-icon class="delete-btn" @click.stop="deleteChat(conv.id)"><Delete /></el-icon>
          </div>
        </div>
        <div class="empty-tip" v-if="conversations.length === 0">暂无对话记录</div>
      </div>
      
      <!-- 底部用户信息 -->
      <div class="sidebar-footer" v-if="!sidebarCollapsed">
        <div class="usage-info">
          <span class="usage-label">今日使用</span>
          <span class="usage-value">{{ aiStatus.todayUsage || 0 }} / {{ aiStatus.dailyLimit || '∞' }}</span>
        </div>
      </div>
    </aside>

    <!-- 主聊天区 -->
    <main class="chat-main">
      <!-- 欢迎页 -->
      <div class="welcome-screen" v-if="messages.length === 0">
        <div class="welcome-content">
          <div class="welcome-logo">
            <div id="ghost">
              <div id="red">
                <div id="pupil"></div>
                <div id="pupil1"></div>
                <div id="eye"></div>
                <div id="eye1"></div>
                <div id="top0"></div>
                <div id="top1"></div>
                <div id="top2"></div>
                <div id="top3"></div>
                <div id="top4"></div>
                <div id="st0"></div>
                <div id="st1"></div>
                <div id="st2"></div>
                <div id="st3"></div>
                <div id="st4"></div>
                <div id="st5"></div>
                <div id="an1"></div>
                <div id="an2"></div>
                <div id="an3"></div>
                <div id="an4"></div>
                <div id="an5"></div>
                <div id="an6"></div>
                <div id="an7"></div>
                <div id="an8"></div>
                <div id="an9"></div>
                <div id="an10"></div>
                <div id="an11"></div>
                <div id="an12"></div>
                <div id="an13"></div>
                <div id="an14"></div>
                <div id="an15"></div>
                <div id="an16"></div>
                <div id="an17"></div>
                <div id="an18"></div>
              </div>
              <div id="shadow"></div>
            </div>
          </div>
          <h1 class="gradient-title">你好{{ userStore.userInfo?.nickname ? userStore.userInfo.nickname : '' }}！有什么可以帮你的？</h1>
          <p class="welcome-subtitle" v-if="aiStatus.model">当前模型：{{ aiStatus.model }}</p>
          <div class="quick-prompts">
            <div class="prompt-card" @click="usePrompt('帮我写一段代码')">
              <el-icon><Edit /></el-icon>
              <span>帮我写代码</span>
            </div>
            <div class="prompt-card" @click="usePrompt('解释一下这个概念')">
              <el-icon><QuestionFilled /></el-icon>
              <span>解释概念</span>
            </div>
            <div class="prompt-card" @click="usePrompt('帮我优化这段文字')">
              <el-icon><MagicStick /></el-icon>
              <span>优化文字</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 消息列表 -->
      <div class="messages-container" ref="messagesRef" v-show="messages.length > 0" @scroll="handleScroll">
        <div class="messages-wrapper">
          <div 
            v-for="(msg, index) in messages" 
            :key="index"
            class="message-row"
            :class="msg.role"
          >
            <div class="message-inner">
              <div class="avatar">
                <el-avatar v-if="msg.role === 'user'" :size="36" :src="userStore.userInfo?.avatar">
                  {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
                </el-avatar>
                <div v-else class="ai-avatar-ball">
                  <div class="ball-circle"></div>
                </div>
              </div>
              <div class="message-body">
                <div class="message-header">
                  <span class="message-sender">{{ msg.role === 'user' ? (userStore.userInfo?.nickname || '我') : '小博' }}</span>
                </div>
                <div class="message-content" v-html="renderMarkdown(msg.content)"></div>
                <div class="message-actions" v-if="msg.role === 'assistant'">
                  <button class="action-btn" @click="copyMessage(msg.content)" title="复制">
                    <el-icon><DocumentCopy /></el-icon>
                    <span>复制</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 正在生成 -->
          <div class="message-row assistant generating" v-if="isTyping">
            <div class="message-inner">
              <div class="avatar">
                <div class="ai-avatar-ball thinking">
                  <div class="ball-circle"></div>
                </div>
              </div>
              <div class="message-body">
                <div class="message-header">
                  <span class="message-sender">小博</span>
                  <div class="typing-indicator">
                    <span></span><span></span><span></span>
                  </div>
                </div>
                <div class="message-content" v-if="streamingText" v-html="renderedStreamingText"></div>
                <div class="message-content thinking-text" v-else>思考中...</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区 -->
      <div class="input-section">
        <div class="input-wrapper" :class="{ focused: inputFocused, disabled: !aiStatus.enabled }">
          <textarea
            v-model="inputText"
            :placeholder="aiStatus.enabled ? '输入消息，按 Enter 发送...' : 'AI 功能未启用'"
            :disabled="!aiStatus.enabled || isTyping"
            @keydown.enter.exact.prevent="sendMessage"
            @focus="inputFocused = true"
            @blur="inputFocused = false"
            rows="1"
            ref="inputRef"
          ></textarea>
          <button 
            class="send-btn" 
            v-if="!isTyping"
            :disabled="!inputText.trim() || !aiStatus.enabled"
            @click="sendMessage"
          >
            <el-icon><Promotion /></el-icon>
          </button>
          <button 
            class="stop-btn" 
            v-else
            @click="stopGeneration"
            title="停止生成"
          >
            <el-icon><VideoPause /></el-icon>
          </button>
        </div>
        <p class="input-hint" v-if="!aiStatus.enabled">AI 功能未启用，请联系管理员开启</p>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'
import { Plus, Fold, Expand, Delete, Promotion, Edit, QuestionFilled, MagicStick, Loading, HomeFilled, DocumentCopy, VideoPause } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAiStatus, getConversations, createConversation, getMessages, deleteConversation, updateConversationTitle } from '@/api/ai'
import { marked } from 'marked'

const userStore = useUserStore()
const themeStore = useThemeStore()

const sidebarCollapsed = ref(false)
const aiStatus = ref({})
const conversations = ref([])
const currentConversation = ref(null)
const messages = ref([])
const inputText = ref('')
const isTyping = ref(false)
const streamingText = ref('')
const messagesRef = ref(null)
const inputRef = ref(null)
const inputFocused = ref(false)

// 用于停止生成的 AbortController
let abortController = null

// 实时渲染流式文本的 Markdown
const renderedStreamingText = computed(() => {
  if (!streamingText.value) return ''
  try {
    marked.setOptions({
      breaks: true,
      gfm: true,
      headerIds: false,
      mangle: false
    })
    return marked.parse(streamingText.value)
  } catch (e) {
    return streamingText.value.replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/\n/g, '<br>')
  }
})

// 按日期分组对话
const groupedConversations = computed(() => {
  const groups = {}
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000)
  const weekAgo = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000)
  
  conversations.value.forEach(conv => {
    const convDate = new Date(conv.createTime || conv.updateTime)
    let label = ''
    
    if (convDate >= today) {
      label = '今天'
    } else if (convDate >= yesterday) {
      label = '昨天'
    } else if (convDate >= weekAgo) {
      label = '最近7天'
    } else {
      label = `${convDate.getFullYear()}-${String(convDate.getMonth() + 1).padStart(2, '0')}`
    }
    
    if (!groups[label]) groups[label] = []
    groups[label].push(conv)
  })
  
  return groups
})

function usePrompt(text) {
  inputText.value = text
  inputRef.value?.focus()
}

// 复制消息
function copyMessage(content) {
  navigator.clipboard.writeText(content).then(() => {
    ElMessage.success('已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

async function fetchAiStatus() {
  try {
    const res = await getAiStatus()
    if (res.code === 200) aiStatus.value = res.data
  } catch (e) {
    console.error('获取AI状态失败', e)
  }
}

async function fetchConversations() {
  try {
    const res = await getConversations()
    if (res.code === 200) conversations.value = res.data || []
  } catch (e) {
    console.error('获取会话列表失败', e)
  }
}

async function createNewChat() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  currentConversation.value = null
  messages.value = []
}

async function selectConversation(conv) {
  currentConversation.value = conv
  messages.value = []
  try {
    const res = await getMessages(conv.id)
    if (res.code === 200) {
      messages.value = res.data || []
      scrollToBottom()
    }
  } catch (e) {
    console.error('获取消息失败', e)
  }
}

async function deleteChat(id) {
  try {
    await ElMessageBox.confirm('确定删除这个对话吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteConversation(id)
    conversations.value = conversations.value.filter(c => c.id !== id)
    if (currentConversation.value?.id === id) {
      currentConversation.value = null
      messages.value = []
    }
    ElMessage.success('已删除')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

function extractTitleFromResponse(text) {
  if (!text) return '新对话'
  const lines = text.split('\n').filter(line => line.trim())
  if (lines.length === 0) return '新对话'
  let title = lines[0].replace(/^#+\s*/, '').replace(/\*\*/g, '').replace(/\*/g, '').replace(/`/g, '')
  return title.length > 25 ? title.substring(0, 25) + '...' : title || '新对话'
}

async function sendMessage() {
  if (!inputText.value.trim() || isTyping.value) return
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  
  let isNewConversation = false
  if (!currentConversation.value) {
    try {
      const res = await createConversation()
      if (res.code === 200) {
        conversations.value.unshift(res.data)
        currentConversation.value = res.data
        isNewConversation = true
      } else {
        ElMessage.error('创建对话失败')
        return
      }
    } catch (e) {
      ElMessage.error('创建对话失败')
      return
    }
  }
  
  const userMessage = inputText.value.trim()
  inputText.value = ''
  messages.value.push({ role: 'user', content: userMessage })
  userScrolled.value = false  // 用户发送消息时重置滚动状态
  scrollToBottom(true)  // 强制滚动到底部
  
  isTyping.value = true
  streamingText.value = ''
  
  const token = localStorage.getItem('blog_token')
  
  // 创建 AbortController 用于停止生成
  abortController = new AbortController()
  
  try {
    const response = await fetch('/api/ai/chat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      },
      body: JSON.stringify({
        conversationId: currentConversation.value.id,
        message: userMessage
      }),
      signal: abortController.signal
    })
    
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let fullText = ''
    let buffer = ''
    
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      
      // 检查是否被中止
      if (abortController?.signal.aborted) break
      
      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || '' // 保留最后一个不完整的行
      
      for (const line of lines) {
        if (line.startsWith('data:')) {
          const data = line.slice(5)
          if (data && data !== '[DONE]') {
            // 恢复被 SSE 转义的换行符
            const content = data.replace(/\\n/g, '\n').replace(/\\t/g, '\t')
            fullText += content
            streamingText.value = fullText
            scrollToBottom()
          }
        }
      }
    }
    
    if (fullText) {
      messages.value.push({ role: 'assistant', content: fullText })
      
      if (isNewConversation && currentConversation.value) {
        const newTitle = extractTitleFromResponse(fullText)
        currentConversation.value.title = newTitle
        const convIndex = conversations.value.findIndex(c => c.id === currentConversation.value.id)
        if (convIndex !== -1) conversations.value[convIndex].title = newTitle
        try {
          await updateConversationTitle(currentConversation.value.id, newTitle)
        } catch (e) {
          console.error('更新标题失败', e)
        }
      }
    }
    
    fetchAiStatus()
  } catch (e) {
    // 如果是用户主动停止，不显示错误
    if (e.name === 'AbortError') {
      console.log('用户停止了生成')
      // 保存已生成的内容
      if (streamingText.value) {
        messages.value.push({ role: 'assistant', content: streamingText.value + '\n\n*[生成已停止]*' })
      }
    } else {
      console.error('发送消息失败', e)
      ElMessage.error('发送失败: ' + e.message)
    }
  } finally {
    isTyping.value = false
    streamingText.value = ''
    abortController = null
  }
}

// 停止生成
function stopGeneration() {
  if (abortController) {
    abortController.abort()
    ElMessage.info('已停止生成')
  }
}

function renderMarkdown(text) {
  if (!text) return ''
  try {
    // 配置 marked 选项
    marked.setOptions({
      breaks: true,
      gfm: true,
      headerIds: false,
      mangle: false
    })
    return marked.parse(text)
  } catch (e) {
    // 如果解析失败，返回转义后的文本
    return text.replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/\n/g, '<br>')
  }
}

// 判断用户是否在底部附近（允许50px的误差）
function isNearBottom() {
  if (!messagesRef.value) return true
  const { scrollTop, scrollHeight, clientHeight } = messagesRef.value
  return scrollHeight - scrollTop - clientHeight < 100
}

// 用户是否手动滚动过（用于判断是否应该自动滚动）
const userScrolled = ref(false)

function scrollToBottom(force = false) {
  nextTick(() => {
    if (messagesRef.value) {
      // 只有在强制滚动、用户没有手动滚动、或者用户在底部附近时才滚动
      if (force || !userScrolled.value || isNearBottom()) {
        messagesRef.value.scrollTop = messagesRef.value.scrollHeight
      }
    }
  })
}

// 监听用户滚动
function handleScroll() {
  if (!messagesRef.value) return
  // 如果用户不在底部，标记为已手动滚动
  userScrolled.value = !isNearBottom()
}

onMounted(() => {
  fetchAiStatus()
  if (userStore.isLoggedIn) fetchConversations()
})

watch(() => userStore.isLoggedIn, (loggedIn) => {
  if (loggedIn) {
    fetchConversations()
    fetchAiStatus()
  } else {
    conversations.value = []
    currentConversation.value = null
    messages.value = []
  }
})
</script>


<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.ai-container {
  display: flex;
  height: 100vh;
  background: var(--bg-main);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
}

// 侧边栏
.sidebar {
  width: 260px;
  background: var(--bg-card);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  
  &.collapsed {
    width: 68px;
  }
}

.sidebar-top {
  padding: 16px;
  border-bottom: 1px solid var(--border-color);
}

.back-home {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  color: var(--text-secondary);
  text-decoration: none;
  border-radius: 10px;
  font-size: 14px;
  transition: all 0.2s;
  
  &:hover {
    background: var(--bg-card-hover);
    color: var(--text-primary);
  }
  
  .collapsed & {
    justify-content: center;
    span { display: none; }
  }
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px;
  
  .collapsed & {
    flex-direction: column;
    gap: 12px;
  }
}

.new-chat-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 16px;
  background: transparent;
  border: 1px solid var(--border-color);
  border-radius: 10px;
  color: var(--text-primary);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    background: var(--bg-card-hover);
    border-color: $primary-color;
  }
  
  .collapsed & {
    width: 100%;
    padding: 12px;
    span { display: none; }
  }
}

.collapse-btn {
  padding: 12px;
  background: transparent;
  border: 1px solid var(--border-color);
  border-radius: 10px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    background: var(--bg-card-hover);
    color: var(--text-primary);
  }
  
  .collapsed & {
    width: 100%;
  }
}

.conversation-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
  
  &::-webkit-scrollbar { width: 4px; }
  &::-webkit-scrollbar-thumb {
    background: var(--border-color);
    border-radius: 2px;
  }
}

.date-group {
  margin-bottom: 8px;
}

.date-label {
  font-size: 12px;
  color: var(--text-muted);
  padding: 8px 12px 4px;
}

.conversation-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 10px;
  cursor: pointer;
  color: var(--text-secondary);
  transition: all 0.2s;
  margin-bottom: 4px;
  
  &:hover {
    background: var(--bg-card-hover);
    color: var(--text-primary);
  }
  
  &.active {
    background: rgba($primary-color, 0.1);
    color: $primary-color;
  }
  
  .conv-title {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 14px;
  }
  
  .delete-btn {
    opacity: 0;
    font-size: 14px;
    color: var(--text-muted);
    transition: all 0.2s;
    &:hover { color: #ef4444; }
  }
  
  &:hover .delete-btn { opacity: 1; }
}

.empty-tip {
  text-align: center;
  color: var(--text-muted);
  font-size: 13px;
  padding: 40px 20px;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid var(--border-color);
}

.usage-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  .usage-label { color: var(--text-muted); }
  .usage-value { color: var(--text-secondary); }
}

// 主聊天区
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--bg-main);
}

// 欢迎页
.welcome-screen {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.welcome-content {
  text-align: center;
  max-width: 600px;
  padding: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.welcome-logo {
  margin-bottom: 32px;
}

// 像素幽灵
#ghost {
  position: relative;
  scale: 0.7;
}

#red {
  animation: upNDown infinite 0.5s;
  position: relative;
  width: 140px;
  height: 140px;
  display: grid;
  grid-template-columns: repeat(14, 1fr);
  grid-template-rows: repeat(14, 1fr);
  grid-template-areas:
    "a1  a2  a3  a4  a5  top0  top0  top0  top0  a10 a11 a12 a13 a14"
    "b1  b2  b3  top1 top1 top1 top1 top1 top1 top1 top1 b12 b13 b14"
    "c1 c2 top2 top2 top2 top2 top2 top2 top2 top2 top2 top2 c13 c14"
    "d1 top3 top3 top3 top3 top3 top3 top3 top3 top3 top3 top3 top3 d14"
    "e1 top3 top3 top3 top3 top3 top3 top3 top3 top3 top3 top3 top3 e14"
    "f1 top3 top3 top3 top3 top3 top3 top3 top3 top3 top3 top3 top3 f14"
    "top4 top4 top4 top4 top4 top4 top4 top4 top4 top4 top4 top4 top4 top4"
    "top4 top4 top4 top4 top4 top4 top4 top4 top4 top4 top4 top4 top4 top4"
    "top4 top4 top4 top4 top4 top4 top4 top4 top4 top4 top4 top4 top4 top4"
    "top4 top4 top4 top4 top4 top4 top4 top4 top4 top4 top4 top4 top4 top4"
    "top4 top4 top4 top4 top4 top4 top4 top4 top4 top4 top4 top4 top4 top4"
    "top4 top4 top4 top4 top4 top4 top4 top4 top4 top4 top4 top4 top4 top4"
    "st0 st0 an4 st1 an7 st2 an10 an10 st3 an13 st4 an16 st5 st5"
    "an1 an2 an3 an5 an6 an8 an9 an9 an11 an12 an14 an15 an17 an18";
}

@keyframes upNDown {
  0%, 49% { transform: translateY(0px); }
  50%, 100% { transform: translateY(-10px); }
}

#top0, #top1, #top2, #top3, #top4, #st0, #st1, #st2, #st3, #st4, #st5 { background-color: red; }
#top0 { grid-area: top0; }
#top1 { grid-area: top1; }
#top2 { grid-area: top2; }
#top3 { grid-area: top3; }
#top4 { grid-area: top4; }
#st0 { grid-area: st0; }
#st1 { grid-area: st1; }
#st2 { grid-area: st2; }
#st3 { grid-area: st3; }
#st4 { grid-area: st4; }
#st5 { grid-area: st5; }

#an1 { grid-area: an1; animation: flicker0 infinite 0.5s; }
#an18 { grid-area: an18; animation: flicker0 infinite 0.5s; }
#an2 { grid-area: an2; animation: flicker1 infinite 0.5s; }
#an17 { grid-area: an17; animation: flicker1 infinite 0.5s; }
#an3 { grid-area: an3; animation: flicker1 infinite 0.5s; }
#an16 { grid-area: an16; animation: flicker1 infinite 0.5s; }
#an4 { grid-area: an4; animation: flicker1 infinite 0.5s; }
#an15 { grid-area: an15; animation: flicker1 infinite 0.5s; }
#an6 { grid-area: an6; animation: flicker0 infinite 0.5s; }
#an12 { grid-area: an12; animation: flicker0 infinite 0.5s; }
#an7 { grid-area: an7; animation: flicker0 infinite 0.5s; }
#an13 { grid-area: an13; animation: flicker0 infinite 0.5s; }
#an9 { grid-area: an9; animation: flicker1 infinite 0.5s; }
#an10 { grid-area: an10; animation: flicker1 infinite 0.5s; }
#an8 { grid-area: an8; animation: flicker0 infinite 0.5s; }
#an11 { grid-area: an11; animation: flicker0 infinite 0.5s; }

@keyframes flicker0 {
  0%, 49% { background-color: red; }
  50%, 100% { background-color: transparent; }
}

@keyframes flicker1 {
  0%, 49% { background-color: transparent; }
  50%, 100% { background-color: red; }
}

#eye, #eye1 {
  width: 40px;
  height: 50px;
  position: absolute;
  top: 30px;
}
#eye { left: 10px; }
#eye1 { right: 30px; }

#eye::before, #eye1::before {
  content: "";
  background-color: white;
  width: 20px;
  height: 50px;
  transform: translateX(10px);
  display: block;
  position: absolute;
}

#eye::after, #eye1::after {
  content: "";
  background-color: white;
  width: 40px;
  height: 30px;
  transform: translateY(10px);
  display: block;
  position: absolute;
}

#pupil, #pupil1 {
  width: 20px;
  height: 20px;
  background-color: blue;
  position: absolute;
  top: 50px;
  z-index: 1;
  animation: eyesMovement infinite 3s;
}
#pupil { left: 10px; }
#pupil1 { right: 50px; }

@keyframes eyesMovement {
  0%, 49% { transform: translateX(0px); }
  50%, 99% { transform: translateX(10px); }
  100% { transform: translateX(0px); }
}

#shadow {
  background-color: black;
  width: 140px;
  height: 140px;
  position: absolute;
  border-radius: 50%;
  transform: rotateX(80deg);
  filter: blur(20px);
  top: 80%;
  animation: shadowMovement infinite 0.5s;
}

@keyframes shadowMovement {
  0%, 49% { opacity: 0.5; }
  50%, 100% { opacity: 0.2; }
}

.gradient-title {
  font-size: 32px;
  font-weight: bold;
  background: linear-gradient(to right, #2d60ec, #3ccfda);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: hue-animate 9s linear infinite;
  margin-bottom: 12px;
}

@keyframes hue-animate {
  0%, 100% { filter: hue-rotate(0deg); }
  50% { filter: hue-rotate(360deg); }
}

.welcome-subtitle {
  font-size: 14px;
  color: var(--text-muted);
  margin-bottom: 48px;
}

.quick-prompts {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
}

.prompt-card {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  color: var(--text-secondary);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    background: var(--bg-card-hover);
    border-color: $primary-color;
    color: var(--text-primary);
    transform: translateY(-2px);
  }
}

// 消息区
.messages-container {
  flex: 1;
  overflow-y: auto;
  
  &::-webkit-scrollbar { width: 6px; }
  &::-webkit-scrollbar-thumb {
    background: var(--border-color);
    border-radius: 3px;
  }
}

.messages-wrapper {
  max-width: 100%;
  padding: 24px 48px;
}

.message-row {
  margin-bottom: 32px;
  
  &.user {
    .message-inner {
      flex-direction: row-reverse;
    }
    .message-body { align-items: flex-end; }
    .message-content {
      background: linear-gradient(135deg, #6366f1, #8b5cf6);
      color: #fff;
      border-radius: 20px 20px 4px 20px;
    }
  }
  
  &.assistant {
    .message-content {
      background: var(--bg-card);
      border: 1px solid var(--border-color);
      border-radius: 20px 20px 20px 4px;
      color: var(--text-primary);
    }
  }
}

.message-inner {
  display: flex;
  gap: 16px;
}

.avatar {
  flex-shrink: 0;
  .el-avatar { border: 2px solid var(--border-color); }
}

.ai-avatar-ball {
  width: 36px;
  height: 36px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  background-image: linear-gradient(rgb(186, 66, 255) 35%, rgb(0, 225, 255));
  border-radius: 50%;
  animation: ai-spinning 1.7s linear infinite;
  filter: blur(1px);
  box-shadow: 0px -5px 20px 0px rgb(186, 66, 255), 0px 5px 20px 0px rgb(0, 225, 255);
  
  .ball-circle {
    width: 100%;
    height: 100%;
    background-color: var(--bg-main);
    border-radius: 50%;
    filter: blur(10px);
  }
  
  &.thinking {
    animation: ai-spinning 1s linear infinite;
  }
}

@keyframes ai-spinning {
  to { transform: rotate(360deg); }
}

.message-body {
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-width: 70%;
  min-width: 0;
  overflow: hidden;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 4px;
}

.message-sender {
  font-size: 12px;
  color: var(--text-muted);
}

.typing-indicator {
  display: flex;
  gap: 4px;
  
  span {
    width: 6px;
    height: 6px;
    background: $primary-color;
    border-radius: 50%;
    animation: bounce 1.4s infinite ease-in-out;
    
    &:nth-child(1) { animation-delay: 0s; }
    &:nth-child(2) { animation-delay: 0.2s; }
    &:nth-child(3) { animation-delay: 0.4s; }
  }
}

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0.8); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}

.message-content {
  padding: 14px 18px;
  font-size: 15px;
  line-height: 1.7;
  word-break: break-word;
  overflow-wrap: break-word;
  
  :deep(p) {
    margin: 0 0 12px;
    &:last-child { margin-bottom: 0; }
  }
  
  :deep(pre) {
    background: var(--bg-main);
    padding: 16px;
    border-radius: 10px;
    overflow-x: auto;
    margin: 12px 0;
    border: 1px solid var(--border-color);
    white-space: pre-wrap;
    word-break: break-all;
  }
  
  :deep(code) {
    font-family: 'JetBrains Mono', 'Fira Code', monospace;
    font-size: 13px;
    background: var(--bg-main);
    padding: 2px 6px;
    border-radius: 4px;
  }
  
  :deep(pre code) {
    background: none;
    padding: 0;
  }
  
  :deep(ul), :deep(ol) {
    padding-left: 20px;
    margin: 8px 0;
  }
  
  :deep(h1), :deep(h2), :deep(h3), :deep(h4) {
    margin: 16px 0 8px;
    font-weight: 600;
    &:first-child { margin-top: 0; }
  }
  
  :deep(h1) { font-size: 1.5em; }
  :deep(h2) { font-size: 1.3em; }
  :deep(h3) { font-size: 1.1em; }
  
  :deep(blockquote) {
    border-left: 3px solid $primary-color;
    padding-left: 12px;
    margin: 12px 0;
    color: var(--text-secondary);
  }
  
  :deep(table) {
    border-collapse: collapse;
    width: 100%;
    margin: 12px 0;
    
    th, td {
      border: 1px solid var(--border-color);
      padding: 8px 12px;
      text-align: left;
    }
    
    th {
      background: var(--bg-main);
    }
  }
  
  :deep(a) {
    color: $primary-color;
    text-decoration: none;
    &:hover { text-decoration: underline; }
  }
  
  :deep(hr) {
    border: none;
    border-top: 1px solid var(--border-color);
    margin: 16px 0;
  }
}

.thinking-text {
  color: var(--text-muted);
  font-style: italic;
}

// 消息操作按钮
.message-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
  padding-top: 8px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: transparent;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  color: var(--text-muted);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    background: var(--bg-card-hover);
    border-color: $primary-color;
    color: var(--text-primary);
  }
  
  .el-icon {
    font-size: 14px;
  }
}

// 输入区
.input-section {
  padding: 20px 48px 32px;
  background: var(--bg-main);
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  padding: 12px 16px;
  transition: all 0.2s;
  
  &.focused {
    border-color: $primary-color;
    box-shadow: 0 0 0 3px rgba($primary-color, 0.1);
  }
  
  &.disabled {
    opacity: 0.5;
  }
  
  textarea {
    flex: 1;
    border: none;
    background: transparent;
    resize: none;
    font-size: 15px;
    line-height: 1.5;
    color: var(--text-primary);
    max-height: 200px;
    
    &::placeholder { color: var(--text-muted); }
    &:focus { outline: none; }
  }
}

.send-btn {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, $primary-color, #8b5cf6);
  color: #fff;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  font-size: 18px;
  
  &:hover:not(:disabled) {
    transform: scale(1.05);
    box-shadow: 0 4px 20px rgba($primary-color, 0.4);
  }
  
  &:disabled {
    opacity: 0.4;
    cursor: not-allowed;
  }
}

.stop-btn {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, #ef4444, #dc2626);
  color: #fff;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  font-size: 18px;
  animation: pulse 1.5s ease-in-out infinite;
  
  &:hover {
    transform: scale(1.05);
    box-shadow: 0 4px 20px rgba(#ef4444, 0.4);
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.input-hint {
  text-align: center;
  font-size: 12px;
  color: #ef4444;
  margin-top: 12px;
}

// 响应式
@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    bottom: 0;
    z-index: 100;
    
    &.collapsed {
      transform: translateX(-100%);
      width: 260px;
    }
  }
  
  .messages-wrapper, .input-section {
    padding: 16px;
  }
  
  .message-body {
    max-width: 90%;
  }
  
  .quick-prompts {
    flex-direction: column;
  }
}
</style>
