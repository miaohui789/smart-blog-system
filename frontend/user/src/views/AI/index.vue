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
      <!-- 移动端侧边栏切换按钮 -->
      <button class="mobile-sidebar-toggle" @click="sidebarCollapsed = !sidebarCollapsed">
        <el-icon><Menu /></el-icon>
      </button>
      <!-- 欢迎页 -->
      <div class="welcome-screen" v-if="messages.length === 0">
        <div class="welcome-content">
          <div class="welcome-logo">
            <div class="loader-wrapper">
              <span class="loader-letter">V</span>
              <span class="loader-letter">i</span>
              <span class="loader-letter">b</span>
              <span class="loader-letter">e</span>
              <span class="loader-letter">&nbsp;</span>
              <span class="loader-letter">C</span>
              <span class="loader-letter">o</span>
              <span class="loader-letter">d</span>
              <span class="loader-letter">i</span>
              <span class="loader-letter">n</span>
              <span class="loader-letter">g</span>
              <div class="loader"></div>
            </div>
          </div>
          <h1 class="welcome-title">{{ randomGreeting }}</h1>
          <p class="welcome-subtitle" v-if="aiStatus.model">当前模型：{{ aiStatus.modelName || aiStatus.model }}</p>
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
            <!-- 思考过程（历史消息） -->
            <div class="thinking-block" v-if="msg.role === 'assistant' && msg.thinkingContent" :class="{ collapsed: msg.thinkingCollapsed !== false }">
              <div class="thinking-block-header" @click="msg.thinkingCollapsed = msg.thinkingCollapsed === false ? true : false">
                <svg class="thinking-icon" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                  <path d="M21 12a9 9 0 0 1-9 9m9-9a9 9 0 0 0-9-9m9 9H3m9 9a9 9 0 0 1-9-9m9 9c1.66 0 3-4.03 3-9s-1.34-9-3-9m0 18c-1.66 0-3-4.03-3-9s1.34-9 3-9m-9 9a9 9 0 0 1 9-9"/>
                </svg>
                <span class="thinking-block-title">已深度思考</span>
                <svg class="thinking-toggle-icon" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><polyline points="6 9 12 15 18 9"/></svg>
              </div>
              <div class="thinking-block-content" v-html="msg.renderedThinkingContent"></div>
            </div>
            <div class="message-reference-card" v-if="msg.role === 'user' && msg.referenceText">
              <div class="message-reference-text">
                <span class="message-reference-label">引用的 AI 内容</span>
                <p>{{ msg.referencePreview }}</p>
              </div>
            </div>
            <div class="message-content" :class="{ 'assistant-selectable': msg.role === 'assistant' }" v-html="msg.renderedContent"></div>
            <div class="knowledge-sources" v-if="msg.role === 'assistant' && msg.knowledgeSources?.length">
              <div class="knowledge-sources-header">
                <span class="knowledge-sources-kicker">回答参考</span>
                <h4>项目知识来源</h4>
                <p>已结合站内文章与面试题库内容生成当前回答</p>
              </div>
              <div class="knowledge-source-list">
                <button
                  v-for="source in msg.knowledgeSources"
                  :key="`${source.sourceType}-${source.sourceId}`"
                  class="knowledge-source-card"
                  :class="[`type-${source.sourceType}`]"
                  @click="openKnowledgeSource(source)"
                >
                  <div class="knowledge-source-top">
                    <span class="knowledge-source-type">{{ source.sourceType === 'study_question' ? '面试题' : '文章' }}</span>
                    <span class="knowledge-source-category">{{ source.categoryName || '未分类' }}</span>
                  </div>
                  <div class="knowledge-source-title">{{ source.title }}</div>
                  <div class="knowledge-source-summary">{{ source.summary || '暂无摘要' }}</div>
                  <div class="knowledge-source-meta">
                    <span v-if="source.difficultyLabel">{{ source.difficultyLabel }}</span>
                    <span v-if="source.studyStatusLabel">{{ source.studyStatusLabel }}</span>
                    <span v-if="source.publishTime">{{ source.publishTime }}</span>
                  </div>
                  <div class="knowledge-source-stats">
                    <span v-if="source.sourceType === 'study_question'">学习 {{ source.studyCount || 0 }}</span>
                    <span v-if="source.sourceType === 'study_question' && source.isFavorite === 1">已收藏</span>
                    <span v-if="source.sourceType === 'study_question' && source.isWrongQuestion === 1">错题</span>
                    <span v-if="source.sourceType === 'article'">浏览 {{ source.viewCount || 0 }}</span>
                    <span v-if="source.sourceType === 'article'">点赞 {{ source.likeCount || 0 }}</span>
                    <span v-if="source.sourceType === 'article'">收藏 {{ source.favoriteCount || 0 }}</span>
                  </div>
                  <div class="knowledge-source-action">
                    <span>查看详情</span>
                    <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.3" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M7 17 17 7"/>
                      <path d="M8 7h9v9"/>
                    </svg>
                  </div>
                </button>
              </div>
            </div>
            <div class="message-actions" v-if="msg.role === 'assistant'">
              <span class="think-duration" v-if="msg.thinkDuration != null">
                <svg viewBox="0 0 24 24" width="11" height="11" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                  <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
                </svg>
                生成用时 {{ msg.thinkDuration }}s
              </span>
              <button class="action-btn" @click="copyMessage(msg.content)" title="复制">
                <el-icon><DocumentCopy /></el-icon>
                <span>复制</span>
              </button>
            </div>
          </div>
          
          <!-- 正在生成 -->
          <div class="message-row assistant generating" v-if="isTyping">
            <!-- 深度思考进行中 -->
            <div class="thinking-block streaming" v-if="isThinkingPhase || streamingThinkingText">
              <div class="thinking-block-header" @click="thinkingStreamCollapsed = !thinkingStreamCollapsed">
                <svg class="thinking-icon spinning" v-if="isThinkingPhase" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                  <path d="M21 12a9 9 0 0 1-9 9m9-9a9 9 0 0 0-9-9m9 9H3m9 9a9 9 0 0 1-9-9m9 9c1.66 0 3-4.03 3-9s-1.34-9-3-9m0 18c-1.66 0-3-4.03-3-9s1.34-9 3-9m-9 9a9 9 0 0 1 9-9"/>
                </svg>
                <svg class="thinking-icon" v-else viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                  <path d="M21 12a9 9 0 0 1-9 9m9-9a9 9 0 0 0-9-9m9 9H3m9 9a9 9 0 0 1-9-9m9 9c1.66 0 3-4.03 3-9s-1.34-9-3-9m0 18c-1.66 0-3-4.03-3-9s1.34-9 3-9m-9 9a9 9 0 0 1 9-9"/>
                </svg>
                <span class="thinking-block-title">{{ isThinkingPhase ? '深度思考中...' : '已深度思考' }}</span>
                <span class="thinking-timer" v-if="isThinkingPhase">{{ thinkingSeconds }}s</span>
                <svg class="thinking-toggle-icon" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><polyline points="6 9 12 15 18 9"/></svg>
              </div>
              <div
                class="thinking-block-content"
                :class="{ collapsed: thinkingStreamCollapsed }"
                v-if="streamingThinkingText"
                ref="streamingThinkingContentRef"
                v-html="renderedStreamingThinking"
              ></div>
            </div>
            <!-- 等待回复（无思考内容时显示思考shimmer） -->
            <div class="thinking-shimmer" v-if="!streamingText && !streamingThinkingText">
              <span class="shimmer-text">正在思考 {{ thinkingSeconds }}s</span>
            </div>
            <div class="message-content" v-if="streamingText" v-html="renderedStreamingText"></div>
          </div>
        </div>
      </div>

      <button
        v-if="selectionAction.visible"
        type="button"
        tabindex="-1"
        class="selection-ask-btn"
        :style="{ left: `${selectionAction.x}px`, top: `${selectionAction.y}px` }"
        @pointerdown.stop.prevent="prepareSelectionQuote"
        @mousedown.stop.prevent
        @click.stop.prevent="applySelectionAsQuote"
      >
        <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
          <path d="M7 12h.01M12 12h.01M17 12h.01"/>
          <path d="M21 12c0 4.418-4.03 8-9 8a9.94 9.94 0 0 1-4.25-.94L3 20l1.24-3.72A7.53 7.53 0 0 1 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8Z" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <span>询问 AI</span>
      </button>

      <!-- 滚动到底部按钮 -->
      <transition name="scroll-btn-fade">
        <button
          v-if="showScrollBtn && !isResizing"
          class="scroll-to-bottom-btn"
          :class="{ generating: isTyping }"
          :style="{ bottom: (composerPanelHeight + 56) + 'px' }"
          @click="scrollToBottom(true)"
          title="滚动到底部"
        >
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="6 9 12 15 18 9"></polyline>
          </svg>
        </button>
      </transition>

      <!-- 输入区 -->
      <div class="input-section">
        <!-- 模型切换器 -->
        <div class="model-switcher" v-if="availableModels.length > 0 || aiStatus.model">
          <div class="model-pill" @click="showModelMenu = !showModelMenu" ref="modelDropdownRef">
            <img v-if="currentModelLogo" :src="currentModelLogo" class="model-pill-logo" />
            <svg v-else class="model-pill-icon" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
              <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
            </svg>
            <span class="model-pill-name">{{ currentModelName }}</span>
            <svg class="model-pill-arrow" :class="{ open: showModelMenu }" viewBox="0 0 24 24" width="12" height="12" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="6 9 12 15 18 9"/></svg>
          </div>
          <transition name="model-menu-fade">
            <div class="model-popup" v-if="showModelMenu">
              <div class="model-popup-header">切换模型</div>
              <div 
                v-for="m in availableModels" 
                :key="m.id" 
                class="model-popup-item"
                :class="{ active: m.selected }"
                @click="handleSwitchModel(m)"
              >
                <img v-if="m.logoUrl" :src="getLogoFullUrl(m.logoUrl)" class="model-popup-logo" />
                <div class="model-popup-info">
                  <span class="model-popup-name">{{ m.displayName }}</span>
                  <span class="model-popup-provider">{{ m.provider }}</span>
                </div>
                <svg v-if="m.selected" class="model-popup-check" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
              </div>
            </div>
          </transition>
        </div>
        <!-- 向上拖拽扩大输入框 -->
        <div class="resize-handle" @mousedown="startResize"></div>
        <div class="input-wrapper" :class="{ focused: inputFocused, disabled: !aiStatus.enabled, quoted: pendingQuote }" :style="{ height: composerPanelHeight + 'px' }">
          <transition name="quote-inline">
            <div class="input-reference-inline" v-if="pendingQuote">
              <span class="input-reference-inline-icon" aria-hidden="true">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.1" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M9 7 4 12l5 5"/>
                  <path d="M20 12H5"/>
                </svg>
              </span>
              <div class="input-reference-inline-body">
                <span class="input-reference-inline-label">引用自 AI 回复</span>
                <div class="input-reference-inline-text">{{ pendingQuote.preview }}</div>
              </div>
              <button class="input-reference-inline-remove" @click="clearPendingQuote" title="移除引用">
                <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round">
                  <path d="M18 6L6 18M6 6l12 12"/>
                </svg>
              </button>
            </div>
          </transition>
          <div class="composer-row">
            <textarea
              v-model="inputText"
              :placeholder="inputPlaceholder"
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
              <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                <path d="M12 4L12 20M12 4L6 10M12 4L18 10" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
              </svg>
            </button>
            <button 
              class="stop-btn" 
              v-else
              @click="stopGeneration"
              title="停止生成"
            >
              <!-- 方形停止图标 -->
              <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                <rect x="5" y="5" width="14" height="14" rx="2"/>
              </svg>
            </button>
          </div>
        </div>
        <p class="input-disclaimer">AI 也可能会犯错。请核查重要信息。</p>
        <p class="input-hint" v-if="!aiStatus.enabled">AI 功能未启用，请联系管理员开启</p>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'
import { Plus, Fold, Expand, Delete, Edit, QuestionFilled, MagicStick, Loading, HomeFilled, DocumentCopy, VideoPause, Menu } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAiStatus, getConversations, createConversation, getMessages, deleteConversation, updateConversationTitle, getAvailableModels, switchAiModel } from '@/api/ai'
import { copyToClipboard, copyWithMessage } from '@/utils/clipboard'
import { marked } from 'marked'
import hljs from 'highlight.js'

const userStore = useUserStore()
const themeStore = useThemeStore()
const route = useRoute()
const router = useRouter()
const QUOTED_MESSAGE_PATTERN = /^\[引用内容\]\n([\s\S]*?)\n\[\/引用内容\]\n\n([\s\S]*)$/
const KNOWLEDGE_META_PATTERN = /<!--AI_KNOWLEDGE_META_START-->([\s\S]*?)<!--AI_KNOWLEDGE_META_END-->/
const AI_ACTIVE_CONVERSATION_STORAGE_KEY = 'ai_active_conversation_id'
const MAX_QUOTE_LENGTH = 6000
const MAX_QUOTE_PREVIEW_LENGTH = 160
const MIN_INPUT_AREA_HEIGHT = 64
const MAX_INPUT_AREA_HEIGHT = 400
const QUOTE_PANEL_EXPANDED_HEIGHT = 60

const GREETINGS = [
  '，今天想搞点什么？',
  '，有什么我可以帮你的？',
  '，来聊点有意思的吧！',
  '，今天又要整什么活？',
  '，准备好 Vibe Coding 了吗？',
  '，需要我帮你写代码吗？',
  '，让我们开始今天的创作！',
  '，有问题尽管问我！',
]
const randomGreeting = computed(() => {
  const name = userStore.userInfo?.nickname || userStore.userInfo?.username || ''
  const suffix = GREETINGS[Math.floor(Math.random() * GREETINGS.length)]
  return `你好 ${name}${suffix}`
})

const sidebarCollapsed = ref(window.innerWidth <= 768)
const aiStatus = ref({})
const conversations = ref([])
const currentConversation = ref(null)
const messages = ref([])
const inputText = ref('')
const isTyping = ref(false)
const streamingText = ref('')
const streamingTextBuffer = ref('')
const messagesRef = ref(null)
const inputRef = ref(null)
const streamingThinkingContentRef = ref(null)
const inputFocused = ref(false)
const pendingQuote = ref(null)
const selectionAction = ref({
  visible: false,
  x: 0,
  y: 0,
  text: ''
})
let selectionUpdateTimer = null
let lockedSelectionQuoteText = ''

// 深度思考相关
const streamingThinkingText = ref('')
const streamingThinkingBuffer = ref('')
const isThinkingPhase = ref(false)
const thinkingStreamCollapsed = ref(false)

// 用于停止生成的 AbortController
let abortController = null
let textTypingTimer = null
let thinkingTypingTimer = null

// 模型选择
const availableModels = ref([])
const showModelMenu = ref(false)
const modelDropdownRef = ref(null)
const currentModelName = computed(() => {
  const selected = availableModels.value.find(m => m.selected)
  return selected ? selected.displayName : (aiStatus.value.modelName || aiStatus.value.model || '未选择')
})
const currentModelLogo = computed(() => {
  const selected = availableModels.value.find(m => m.selected)
  return selected && selected.logoUrl ? getLogoFullUrl(selected.logoUrl) : ''
})
let messageRenderSeed = 0

function getLogoFullUrl(url) {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('//')) return url
  // 对于上传的相对路径，拼接基础URL
  const base = import.meta.env.VITE_API_BASE_URL || ''
  return base + url
}

// 思考计时
const thinkingSeconds = ref(0)
let thinkingTimer = null

// 显示滚动到底部按钮
const showScrollBtn = ref(false)
// 正在拖拽调整输入框
const isResizing = ref(false)

// 输入框高度（拖拽调整）
const inputAreaHeight = ref(MIN_INPUT_AREA_HEIGHT)
const composerPanelHeight = computed(() => {
  return inputAreaHeight.value + (pendingQuote.value ? QUOTE_PANEL_EXPANDED_HEIGHT : 0)
})

function getTypewriterDelay(bufferLength) {
  if (bufferLength > 320) return 0
  if (bufferLength > 180) return 4
  if (bufferLength > 80) return 8
  return 16
}

function takeLeadingSymbol(text) {
  if (!text) {
    return { symbol: '', rest: '' }
  }
  const codePoint = text.codePointAt(0)
  const size = codePoint > 0xFFFF ? 2 : 1
  return {
    symbol: text.slice(0, size),
    rest: text.slice(size)
  }
}

function clearTypewriterTimers() {
  if (textTypingTimer) {
    clearTimeout(textTypingTimer)
    textTypingTimer = null
  }
  if (thinkingTypingTimer) {
    clearTimeout(thinkingTypingTimer)
    thinkingTypingTimer = null
  }
}

function scheduleTextTyping() {
  if (textTypingTimer || !streamingTextBuffer.value) return
  const pump = () => {
    if (!streamingTextBuffer.value) {
      textTypingTimer = null
      return
    }
    const { symbol, rest } = takeLeadingSymbol(streamingTextBuffer.value)
    streamingText.value += symbol
    streamingTextBuffer.value = rest
    scrollToBottom()
    textTypingTimer = setTimeout(pump, getTypewriterDelay(streamingTextBuffer.value.length))
  }
  textTypingTimer = setTimeout(pump, 0)
}

function scheduleThinkingTyping() {
  if (thinkingTypingTimer || !streamingThinkingBuffer.value) return
  const pump = () => {
    if (!streamingThinkingBuffer.value) {
      thinkingTypingTimer = null
      return
    }
    const { symbol, rest } = takeLeadingSymbol(streamingThinkingBuffer.value)
    streamingThinkingText.value += symbol
    streamingThinkingBuffer.value = rest
    scrollThinkingContentToBottom(true)
    thinkingTypingTimer = setTimeout(pump, getTypewriterDelay(streamingThinkingBuffer.value.length))
  }
  thinkingTypingTimer = setTimeout(pump, 0)
}

function appendStreamingText(content) {
  if (!content) return
  streamingTextBuffer.value += content
  scheduleTextTyping()
}

function appendStreamingThinking(content) {
  if (!content) return
  streamingThinkingBuffer.value += content
  scheduleThinkingTyping()
}

function flushStreamingBuffers() {
  clearTypewriterTimers()
  if (streamingThinkingBuffer.value) {
    streamingThinkingText.value += streamingThinkingBuffer.value
    streamingThinkingBuffer.value = ''
  }
  if (streamingTextBuffer.value) {
    streamingText.value += streamingTextBuffer.value
    streamingTextBuffer.value = ''
  }
  scrollThinkingContentToBottom(true)
  scrollToBottom(true)
}

function waitForStreamingBuffers(timeout = 12000) {
  if (!streamingTextBuffer.value && !streamingThinkingBuffer.value) {
    return Promise.resolve()
  }
  return new Promise(resolve => {
    const startedAt = Date.now()
    const check = () => {
      if ((!streamingTextBuffer.value && !streamingThinkingBuffer.value) || Date.now() - startedAt >= timeout) {
        resolve()
        return
      }
      setTimeout(check, 24)
    }
    check()
  })
}

function resetStreamingState() {
  clearTypewriterTimers()
  streamingText.value = ''
  streamingTextBuffer.value = ''
  streamingThinkingText.value = ''
  streamingThinkingBuffer.value = ''
}

function startResize(e) {
  e.preventDefault()
  const startY = e.clientY
  const startH = inputAreaHeight.value
  isResizing.value = true

  const onMouseMove = (e) => {
    e.preventDefault()
    const delta = startY - e.clientY  // 向上拖为正
    inputAreaHeight.value = Math.min(MAX_INPUT_AREA_HEIGHT, Math.max(MIN_INPUT_AREA_HEIGHT, startH + delta))
  }
  const onMouseUp = () => {
    isResizing.value = false
    document.removeEventListener('mousemove', onMouseMove)
    document.removeEventListener('mouseup', onMouseUp)
  }
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
}

// 实时渲染流式文本的 Markdown
const renderedStreamingText = computed(() => {
  if (!streamingText.value) return ''
  try {
    marked.setOptions({
      breaks: true,
      gfm: true,
      headerIds: false,
      mangle: false,
      renderer
    })
    return marked.parse(streamingText.value)
  } catch (e) {
    return streamingText.value.replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/\n/g, '<br>')
  }
})

// 实时渲染流式思考内容的 Markdown
const renderedStreamingThinking = computed(() => {
  if (!streamingThinkingText.value) return ''
  try {
    marked.setOptions({
      breaks: true,
      gfm: true,
      headerIds: false,
      mangle: false,
      renderer
    })
    return marked.parse(streamingThinkingText.value)
  } catch (e) {
    return streamingThinkingText.value.replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/\n/g, '<br>')
  }
})

const inputPlaceholder = computed(() => {
  if (!aiStatus.value.enabled) return 'AI 功能未启用'
  if (pendingQuote.value) return '已引用一段 AI 回复，输入你的问题，按 Enter 发送...'
  return '输入消息，按 Enter 发送...'
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

function buildQuotePreview(text) {
  const normalized = text.replace(/\s+/g, ' ').trim()
  if (!normalized) return '已引用一段 AI 回复'
  return normalized.length > MAX_QUOTE_PREVIEW_LENGTH
    ? normalized.slice(0, MAX_QUOTE_PREVIEW_LENGTH) + '...'
    : normalized
}

function parseQuotedMessage(content) {
  if (typeof content !== 'string') return null
  const matched = content.match(QUOTED_MESSAGE_PATTERN)
  if (!matched) return null
  const quoteText = matched[1]?.trim()
  const question = matched[2]?.trim()
  if (!quoteText || !question) return null
  return {
    quoteText,
    question,
    quotePreview: buildQuotePreview(quoteText)
  }
}

function normalizeMessage(message) {
  const normalized = { ...message }
  normalized.knowledgeSources = Array.isArray(normalized.knowledgeSources)
    ? normalized.knowledgeSources.map(normalizeKnowledgeSource).filter(Boolean)
    : []
  if (normalized.role === 'user') {
    const parsed = parseQuotedMessage(normalized.content)
    if (parsed) {
      normalized.rawContent = normalized.content
      normalized.content = parsed.question
      normalized.referenceText = parsed.quoteText
      normalized.referencePreview = parsed.quotePreview
    }
  } else if (normalized.role === 'assistant') {
    const parsedKnowledge = extractKnowledgeMeta(normalized.thinkingContent)
    normalized.thinkingContent = parsedKnowledge.thinkingContent
    if (parsedKnowledge.knowledgeSources.length) {
      normalized.knowledgeSources = parsedKnowledge.knowledgeSources
    }
  }
  normalized.renderedContent = renderMarkdown(normalized.content)
  normalized.renderedThinkingContent = normalized.thinkingContent ? renderMarkdown(normalized.thinkingContent) : ''
  normalized._renderKey = normalized._renderKey || `msg-render-${messageRenderSeed++}`
  return normalized
}

function extractKnowledgeMeta(thinkingContent) {
  if (!thinkingContent || typeof thinkingContent !== 'string') {
    return {
      thinkingContent: thinkingContent || '',
      knowledgeSources: []
    }
  }

  const matched = thinkingContent.match(KNOWLEDGE_META_PATTERN)
  if (!matched) {
    return {
      thinkingContent,
      knowledgeSources: []
    }
  }

  let knowledgeSources = []
  try {
    const parsed = JSON.parse(matched[1])
    knowledgeSources = Array.isArray(parsed) ? parsed.map(normalizeKnowledgeSource).filter(Boolean) : []
  } catch (e) {
    knowledgeSources = []
  }

  return {
    thinkingContent: thinkingContent.replace(KNOWLEDGE_META_PATTERN, '').trim(),
    knowledgeSources
  }
}

function normalizeKnowledgeSource(source) {
  if (!source || typeof source !== 'object') return null
  return {
    sourceType: source.sourceType || '',
    sourceId: source.sourceId ?? null,
    title: source.title || '未命名来源',
    summary: source.summary || '',
    categoryName: source.categoryName || '',
    routePath: source.routePath || '',
    publishTime: source.publishTime || '',
    difficultyLabel: source.difficultyLabel || '',
    studyStatusLabel: source.studyStatusLabel || '',
    isFavorite: source.isFavorite ?? 0,
    isWrongQuestion: source.isWrongQuestion ?? 0,
    studyCount: source.studyCount ?? 0,
    viewCount: source.viewCount ?? 0,
    likeCount: source.likeCount ?? 0,
    favoriteCount: source.favoriteCount ?? 0
  }
}

function normalizeConversationId(value) {
  if (value === undefined || value === null || value === '') return ''
  return String(value)
}

function persistActiveConversationId(conversationId) {
  const normalizedId = normalizeConversationId(conversationId)
  if (!normalizedId) {
    sessionStorage.removeItem(AI_ACTIVE_CONVERSATION_STORAGE_KEY)
    return
  }
  sessionStorage.setItem(AI_ACTIVE_CONVERSATION_STORAGE_KEY, normalizedId)
}

function getPreferredConversationId() {
  return normalizeConversationId(route.query.conversationId) || sessionStorage.getItem(AI_ACTIVE_CONVERSATION_STORAGE_KEY) || ''
}

function buildAiReturnQuery() {
  const query = { from: 'ai' }
  if (currentConversation.value?.id) {
    query.conversationId = normalizeConversationId(currentConversation.value.id)
  }
  return query
}

async function syncConversationRoute(conversationId) {
  const normalizedId = normalizeConversationId(conversationId)
  const nextQuery = { ...route.query }
  delete nextQuery.from
  if (normalizedId) {
    nextQuery.conversationId = normalizedId
  } else {
    delete nextQuery.conversationId
  }
  persistActiveConversationId(normalizedId)
  if (!Object.keys(nextQuery).length) {
    await router.replace({ path: '/ai' })
    return
  }
  await router.replace({ path: '/ai', query: nextQuery })
}

async function restoreConversationFromRoute() {
  const conversationId = getPreferredConversationId()
  if (!conversationId) return

  const matched = conversations.value.find(conv => normalizeConversationId(conv.id) === conversationId)
  if (!matched) return
  if (normalizeConversationId(currentConversation.value?.id) === conversationId) return

  await selectConversation(matched, { syncRoute: false })
}

function openKnowledgeSource(source) {
  const normalized = normalizeKnowledgeSource(source)
  if (!normalized?.routePath) {
    ElMessage.info('该来源暂时无法跳转')
    return
  }
  router.push({
    path: normalized.routePath,
    query: buildAiReturnQuery()
  })
}

function buildOutgoingMessage(question, quoteText) {
  if (!quoteText) return question
  return `[引用内容]\n${quoteText}\n[/引用内容]\n\n${question}`
}

function getClosestElement(node) {
  if (!node) return null
  return node.nodeType === Node.ELEMENT_NODE ? node : node.parentElement
}

function hideSelectionAction() {
  lockedSelectionQuoteText = ''
  if (!selectionAction.value.visible && !selectionAction.value.text && selectionAction.value.x === 0 && selectionAction.value.y === 0) {
    return
  }
  selectionAction.value = {
    visible: false,
    x: 0,
    y: 0,
    text: ''
  }
}

function clearTextSelection() {
  const selection = window.getSelection?.()
  if (!selection) return
  if (typeof selection.empty === 'function') {
    selection.empty()
    return
  }
  selection.removeAllRanges()
}

function clearPendingQuote() {
  pendingQuote.value = null
}

function resetQuoteState() {
  clearPendingQuote()
  hideSelectionAction()
  clearTextSelection()
}

function clearSelectionUpdateTimer() {
  if (selectionUpdateTimer) {
    clearTimeout(selectionUpdateTimer)
    selectionUpdateTimer = null
  }
}

function normalizeSelectionText(text) {
  return (text || '').replace(/\s+/g, ' ').trim()
}

function isBlockedSelectionArea(node) {
  const el = getClosestElement(node)
  if (!el) return true

  return Boolean(
    el.closest('.thinking-block') ||
    el.closest('.message-actions') ||
    el.closest('.selection-ask-btn') ||
    el.closest('.code-block-header') ||
    el.closest('.code-copy-btn') ||
    el.closest('.code-toggle-btn')
  )
}

function getAssistantSelectionScope(selection) {
  const anchorEl = getClosestElement(selection.anchorNode)
  const focusEl = getClosestElement(selection.focusNode)
  const anchorScope = anchorEl?.closest('.message-content.assistant-selectable')
  const focusScope = focusEl?.closest('.message-content.assistant-selectable')
  const anchorRow = anchorEl?.closest('.message-row.assistant')
  const focusRow = focusEl?.closest('.message-row.assistant')

  if (isBlockedSelectionArea(anchorEl) || isBlockedSelectionArea(focusEl)) return null
  if (!anchorScope || !focusScope || !anchorRow || !focusRow) return null
  if (anchorRow !== focusRow) return null
  if (anchorRow.classList.contains('generating')) return null
  if (anchorScope !== focusScope) return null

  return anchorScope
}

function resolveSelectionPosition(range, triggerEvent) {
  if (triggerEvent && typeof triggerEvent.clientX === 'number' && typeof triggerEvent.clientY === 'number') {
    return {
      x: Math.min(Math.max(triggerEvent.clientX, 92), window.innerWidth - 92),
      y: Math.max(triggerEvent.clientY - 14, 20)
    }
  }

  const rects = Array.from(range.getClientRects()).filter(rect => rect.width > 0 || rect.height > 0)
  const rect = rects[rects.length - 1] || range.getBoundingClientRect()
  return {
    x: Math.min(Math.max(rect.left + Math.min(rect.width, 160) / 2, 92), window.innerWidth - 92),
    y: Math.max(rect.top - 14, 20)
  }
}

function updateSelectionAction(triggerEvent) {
  const selection = window.getSelection()
  if (!selection || selection.rangeCount === 0 || selection.isCollapsed) {
    hideSelectionAction()
    return
  }

  const selectedText = selection.toString().trim()
  if (!selectedText) {
    hideSelectionAction()
    return
  }

  if (selectedText.length < 2) {
    hideSelectionAction()
    return
  }

  const scope = getAssistantSelectionScope(selection)
  if (!scope) {
    hideSelectionAction()
    return
  }

  const range = selection.getRangeAt(0)
  const selectedFragment = range.cloneContents().textContent?.trim() || ''
  if (!selectedFragment) {
    hideSelectionAction()
    return
  }

  const scopeText = scope.textContent?.trim() || ''
  if (!normalizeSelectionText(scopeText).includes(normalizeSelectionText(selectedFragment))) {
    hideSelectionAction()
    return
  }

  const position = resolveSelectionPosition(range, triggerEvent)

  selectionAction.value = {
    visible: true,
    text: selectedText,
    x: position.x,
    y: position.y
  }
}

function scheduleSelectionActionUpdate(triggerEvent) {
  clearSelectionUpdateTimer()
  selectionUpdateTimer = setTimeout(() => {
    updateSelectionAction(triggerEvent)
    selectionUpdateTimer = null
  }, 0)
}

function handleDocumentMouseUp(event) {
  if (event.button !== 0) return
  if (event.target?.closest('.selection-ask-btn')) return
  scheduleSelectionActionUpdate(event)
}

function handleDocumentKeyUp(event) {
  if (event.key === 'Escape') {
    hideSelectionAction()
    return
  }
  if (event.key === 'Shift' || event.key.startsWith('Arrow')) {
    scheduleSelectionActionUpdate(event)
  }
}

function handleSelectionChange() {
  const selection = window.getSelection()
  if (!selection || selection.rangeCount === 0 || selection.isCollapsed || !selection.toString().trim()) {
    hideSelectionAction()
  }
}

function prepareSelectionQuote() {
  clearSelectionUpdateTimer()
  lockedSelectionQuoteText = selectionAction.value.text.trim()
}

function applySelectionAsQuote() {
  clearSelectionUpdateTimer()
  let text = (lockedSelectionQuoteText || selectionAction.value.text || '').trim()
  if (!text) return

  let truncated = false
  if (text.length > MAX_QUOTE_LENGTH) {
    text = text.slice(0, MAX_QUOTE_LENGTH)
    truncated = true
  }

  pendingQuote.value = {
    text,
    preview: buildQuotePreview(text)
  }

  clearTextSelection()
  hideSelectionAction()

  requestAnimationFrame(() => {
    clearTextSelection()
    inputRef.value?.focus()
    requestAnimationFrame(() => clearTextSelection())
  })

  if (truncated) {
    ElMessage.warning('引用内容过长，已自动截断后再引用')
  }
}

// 复制消息
async function copyMessage(content) {
  await copyWithMessage(content, ElMessage, '已复制到剪贴板', '复制失败，请检查浏览器权限后重试')
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
    if (res.code === 200) {
      conversations.value = res.data || []
      return conversations.value
    }
  } catch (e) {
    console.error('获取会话列表失败', e)
  }
  return conversations.value
}

async function fetchAvailableModels() {
  try {
    const res = await getAvailableModels()
    if (res.code === 200) availableModels.value = res.data || []
  } catch (e) {
    console.error('获取模型列表失败', e)
  }
}

async function handleSwitchModel(model) {
  if (model.selected) {
    showModelMenu.value = false
    return
  }
  try {
    const res = await switchAiModel(model.id)
    if (res.code === 200) {
      // 更新选中状态
      availableModels.value.forEach(m => { m.selected = m.id === model.id })
      ElMessage.success(`已切换到 ${model.displayName}`)
      showModelMenu.value = false
      // 刷新AI状态
      fetchAiStatus()
    } else {
      ElMessage.error(res.message || '切换失败')
    }
  } catch (e) {
    ElMessage.error('切换模型失败')
  }
}

async function createNewChat() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  currentConversation.value = null
  messages.value = []
  resetQuoteState()
  await syncConversationRoute(null)
  
  if (window.innerWidth <= 768) {
    sidebarCollapsed.value = true
  }
}

async function selectConversation(conv, options = {}) {
  const { syncRoute = true } = options
  currentConversation.value = conv
  persistActiveConversationId(conv?.id)
  messages.value = []
  resetQuoteState()
  if (syncRoute) {
    await syncConversationRoute(conv?.id)
  }
  
  // 移动端自动收起侧边栏
  if (window.innerWidth <= 768) {
    sidebarCollapsed.value = true
  }

  try {
    const res = await getMessages(conv.id)
    if (res.code === 200) {
      messages.value = (res.data || []).map(normalizeMessage)
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
    if (normalizeConversationId(currentConversation.value?.id) === normalizeConversationId(id)) {
      currentConversation.value = null
      messages.value = []
      persistActiveConversationId(null)
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

function buildStoppedAssistantMessage(fullText, fullThinking, knowledgeSources) {
  const content = (fullText || streamingText.value || '').trimEnd()
  const thinkingContent = (fullThinking || streamingThinkingText.value || '').trimEnd()
  if (!content && !thinkingContent) {
    return null
  }
  const stoppedMsg = {
    role: 'assistant',
    content: content ? content + '\n\n*[生成已停止]*' : '*[生成已停止]*',
    thinkDuration: thinkingSeconds.value
  }
  if (thinkingContent) {
    stoppedMsg.thinkingContent = thinkingContent
    stoppedMsg.thinkingCollapsed = true
  }
  if (Array.isArray(knowledgeSources) && knowledgeSources.length) {
    stoppedMsg.knowledgeSources = knowledgeSources
  }
  return stoppedMsg
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
        await syncConversationRoute(res.data.id)
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
  
  const currentQuote = pendingQuote.value ? { ...pendingQuote.value } : null
  const userMessage = inputText.value.trim()
  const outboundMessage = buildOutgoingMessage(userMessage, currentQuote?.text || '')
  inputText.value = ''
  clearPendingQuote()
  messages.value.push(normalizeMessage({ role: 'user', content: outboundMessage }))
  userScrolled.value = false  // 用户发送消息时重置滚动状态
  scrollToBottom(true)  // 强制滚动到底部
  
  isTyping.value = true
  resetStreamingState()
  isThinkingPhase.value = false
  thinkingStreamCollapsed.value = false
  thinkingSeconds.value = 0
  thinkingTimer = setInterval(() => { thinkingSeconds.value++ }, 1000)
  
  const token = localStorage.getItem('blog_token')
  
  // 创建 AbortController 用于停止生成
  abortController = new AbortController()
  let fullText = ''
  let fullThinking = ''
  let knowledgeSources = []
  
  try {
    const response = await fetch('/api/ai/chat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      },
      body: JSON.stringify({
        conversationId: currentConversation.value.id,
        message: outboundMessage
      }),
      signal: abortController.signal
    })
    
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''
    let currentEventType = ''
    
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      
      // 检查是否被中止
      if (abortController?.signal.aborted) break
      
      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || '' // 保留最后一个不完整的行
      
      for (const line of lines) {
        if (line.startsWith('event:')) {
          currentEventType = line.slice(6).trim()
        } else if (line.startsWith('data:')) {
          const data = line.slice(5)
          if (data && data !== '[DONE]') {
            const content = data.replace(/\\n/g, '\n').replace(/\\t/g, '\t')
            
            if (currentEventType === 'thinking') {
              // 深度思考内容
              if (!isThinkingPhase.value) {
                isThinkingPhase.value = true
              }
              fullThinking += content
              appendStreamingThinking(content)
            } else if (currentEventType === 'knowledge') {
              try {
                const parsed = JSON.parse(content)
                knowledgeSources = Array.isArray(parsed)
                  ? parsed.map(normalizeKnowledgeSource).filter(Boolean)
                  : []
              } catch (e) {
                knowledgeSources = []
              }
            } else if (currentEventType === 'token') {
              // 正常回复内容 — 收到第一个token时结束思考阶段
              if (isThinkingPhase.value) {
                isThinkingPhase.value = false
                thinkingStreamCollapsed.value = true  // 思考完成后自动折叠
              }
              fullText += content
              appendStreamingText(content)
            }
          }
          currentEventType = '' // 每个data处理完后重置事件类型
        }
      }
    }
    
    if (fullText) {
      await waitForStreamingBuffers()
      const _duration = thinkingSeconds.value
      const msgData = { role: 'assistant', content: fullText, thinkDuration: _duration }
      // 保存思考内容
      if (fullThinking) {
        msgData.thinkingContent = fullThinking
        msgData.thinkingCollapsed = true  // 默认折叠
      }
      if (knowledgeSources.length) {
        msgData.knowledgeSources = knowledgeSources
      }
      messages.value.push(normalizeMessage(msgData))
      
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
      flushStreamingBuffers()
      const stoppedMsg = buildStoppedAssistantMessage(fullText, fullThinking, knowledgeSources)
      if (stoppedMsg) {
        messages.value.push(normalizeMessage(stoppedMsg))
      }
    } else {
      console.error('发送消息失败', e)
      ElMessage.error('发送失败: ' + e.message)
    }
  } finally {
    isTyping.value = false
    resetStreamingState()
    isThinkingPhase.value = false
    abortController = null
    if (thinkingTimer) { clearInterval(thinkingTimer); thinkingTimer = null }
  }
}

// 停止生成
function stopGeneration() {
  if (abortController) {
    abortController.abort()
    ElMessage.info('已停止生成')
  }
}

// 自定义 marked renderer：代码块带语言标签 + 复制按钮
const renderer = new marked.Renderer()
let codeBlockId = 0

renderer.code = function (code, language) {
  const lang = (language || '').split(' ')[0] || 'text'
  const displayLang = lang.charAt(0).toUpperCase() + lang.slice(1)
  const id = 'code-block-' + (codeBlockId++)
  const wrapperId = 'wrapper-' + id
  // 语法高亮
  let highlighted
  try {
    highlighted = hljs.getLanguage(lang)
      ? hljs.highlight(code, { language: lang, ignoreIllegals: true }).value
      : hljs.highlightAuto(code).value
  } catch (e) {
    highlighted = code.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;')
  }
  return `<div class="code-block-wrapper" id="${wrapperId}">
    <div class="code-block-header">
      <button class="code-toggle-btn" data-toggle-wrapper="${wrapperId}">
        <span class="code-lang">${displayLang}</span>
        <svg class="toggle-icon" viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><polyline points="18 15 12 9 6 15"/></svg>
      </button>
      <button class="code-copy-btn" data-code-id="${id}">
        <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"/><path d="M5 15H4a2 2 0 01-2-2V4a2 2 0 012-2h9a2 2 0 012 2v1"/></svg>
        <span>复制代码</span>
      </button>
    </div>
    <pre class="code-block-pre"><code id="${id}" class="hljs language-${lang}">${highlighted}</code></pre>
  </div>`
}

function renderMarkdown(text) {
  if (!text) return ''
  try {
    marked.setOptions({
      breaks: true,
      gfm: true,
      headerIds: false,
      mangle: false,
      renderer
    })
    return marked.parse(text)
  } catch (e) {
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

function scrollThinkingContentToBottom(force = false) {
  nextTick(() => {
    const thinkingEl = streamingThinkingContentRef.value
    if (!thinkingEl || (!force && thinkingStreamCollapsed.value)) return
    requestAnimationFrame(() => {
      thinkingEl.scrollTop = thinkingEl.scrollHeight
    })
  })
}

// 监听用户滚动
function handleScroll() {
  if (!messagesRef.value) return
  hideSelectionAction()
  const nearBottom = isNearBottom()
  // 如果用户不在底部，标记为已手动滚动
  userScrolled.value = !nearBottom
  // 距离底部 >300px 时显示按钮
  const { scrollTop, scrollHeight, clientHeight } = messagesRef.value
  showScrollBtn.value = scrollHeight - scrollTop - clientHeight > 300
}

onMounted(() => {
  fetchAiStatus()
  if (userStore.isLoggedIn) {
    fetchConversations().then(() => {
      restoreConversationFromRoute()
    })
    fetchAvailableModels()
  }
  // 事件委托：代码块折叠 + 复制
  document.addEventListener('click', handleCodeBlockClick)
  // 点击外部关闭模型菜单
  document.addEventListener('click', handleClickOutsideModelMenu)
  document.addEventListener('mouseup', handleDocumentMouseUp)
  document.addEventListener('keyup', handleDocumentKeyUp)
  document.addEventListener('selectionchange', handleSelectionChange)
  window.addEventListener('resize', hideSelectionAction)
})

function handleCodeBlockClick(e) {
  const toggleBtn = e.target.closest('[data-toggle-wrapper]')
  if (toggleBtn) {
    const wrapperId = toggleBtn.dataset.toggleWrapper
    const wrapper = document.getElementById(wrapperId)
    if (wrapper) wrapper.classList.toggle('collapsed')
    return
  }
  const copyBtn = e.target.closest('[data-code-id]')
  if (copyBtn) {
    const id = copyBtn.dataset.codeId
    const el = document.getElementById(id)
    if (!el) return
    copyToClipboard(el.textContent || '').then((success) => {
      if (!success) {
        ElMessage.error('复制失败，请检查浏览器权限后重试')
        return
      }

      const span = copyBtn.querySelector('span')
      if (span) {
        const orig = span.textContent
        span.textContent = '已复制!'
        setTimeout(() => { span.textContent = orig }, 2000)
      }
    }).catch(() => {
      ElMessage.error('复制失败，请检查浏览器权限后重试')
    })
  }
}

function handleClickOutsideModelMenu(e) {
  if (showModelMenu.value && modelDropdownRef.value && !modelDropdownRef.value.contains(e.target) && !e.target.closest('.model-menu')) {
    showModelMenu.value = false
  }
}

onUnmounted(() => {
  clearTypewriterTimers()
  clearSelectionUpdateTimer()
  document.removeEventListener('click', handleCodeBlockClick)
  document.removeEventListener('click', handleClickOutsideModelMenu)
  document.removeEventListener('mouseup', handleDocumentMouseUp)
  document.removeEventListener('keyup', handleDocumentKeyUp)
  document.removeEventListener('selectionchange', handleSelectionChange)
  window.removeEventListener('resize', hideSelectionAction)
})

watch(() => userStore.isLoggedIn, (loggedIn) => {
  if (loggedIn) {
    fetchConversations().then(() => {
      restoreConversationFromRoute()
    })
    fetchAiStatus()
    fetchAvailableModels()
  } else {
    conversations.value = []
    currentConversation.value = null
    messages.value = []
    availableModels.value = []
    persistActiveConversationId(null)
    resetQuoteState()
  }
})

watch(() => route.query.conversationId, () => {
  if (!userStore.isLoggedIn || !conversations.value.length) {
    return
  }
  restoreConversationFromRoute()
})

watch(streamingThinkingText, (value) => {
  if (!value) return
  scrollThinkingContentToBottom(true)
})

watch(thinkingStreamCollapsed, (collapsed) => {
  if (!collapsed && streamingThinkingText.value) {
    scrollThinkingContentToBottom(true)
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

// ========== 模型切换器（输入框上方） ==========
.model-switcher {
  position: absolute;
  left: 48px;
  top: -46px;
  display: flex;
  align-items: center;
  padding: 0;
  z-index: 20;
}

.model-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px 6px 10px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
  
  &:hover {
    background: var(--bg-hover, var(--bg-card));
    border-color: var(--primary-color);
  }
}

.model-pill-icon {
  color: var(--text-muted);
  flex-shrink: 0;
}

.model-pill-logo {
  width: 18px;
  height: 18px;
  border-radius: 4px;
  object-fit: contain;
  flex-shrink: 0;
}

.model-pill-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  max-width: 220px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.model-pill-arrow {
  color: var(--text-muted);
  transition: transform 0.2s;
  flex-shrink: 0;
  
  &.open {
    transform: rotate(180deg);
  }
}

.model-popup {
  position: absolute;
  bottom: calc(100% + 8px);
  left: 0;
  min-width: 280px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 14px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.18);
  padding: 6px;
  z-index: 200;
  max-height: 320px;
  overflow-y: auto;
  
  &::-webkit-scrollbar { width: 4px; }
  &::-webkit-scrollbar-thumb {
    background: var(--border-color);
    border-radius: 2px;
  }
}

.model-popup-header {
  font-size: 11px;
  font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 8px 12px 6px;
}

.model-popup-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.15s;
  
  &:hover {
    background: var(--bg-card-hover);
  }
  
  &.active {
    background: rgba(59, 130, 246, 0.08);
    
    .model-popup-name {
      color: $primary-color;
      font-weight: 600;
    }
  }
}

.model-popup-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
  flex: 1;
}

.model-popup-logo {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  object-fit: contain;
  flex-shrink: 0;
  margin-right: 10px;
}

.model-popup-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.model-popup-provider {
  font-size: 11px;
  color: var(--text-muted);
}

.model-popup-check {
  color: $primary-color;
  flex-shrink: 0;
  margin-left: 8px;
}

.model-menu-fade-enter-active,
.model-menu-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.model-menu-fade-enter-from,
.model-menu-fade-leave-to {
  opacity: 0;
  transform: translateY(6px);
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
  position: relative;
}

// 欢迎页
.welcome-screen {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  min-height: 0;
}

.welcome-content {
  text-align: center;
  max-width: 800px;
  padding: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.welcome-logo {
  margin-bottom: 32px;
}

// Generating 文字 Loader
.loader-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 120px;
  width: auto;
  margin: 2rem;
  font-family: "Poppins", "PingFang SC", sans-serif;
  font-size: 1.6em;
  font-weight: 600;
  user-select: none;
  color: var(--loader-text-color, #fff);
  scale: 2;
}

.loader {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  width: 100%;
  z-index: 1;
  background-color: transparent;
  mask: repeating-linear-gradient(
    90deg,
    transparent 0,
    transparent 6px,
    black 6px,
    black 9px
  );

  &::after {
    content: "";
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-image:
      radial-gradient(circle at 50% 50%, #ff0 0%, transparent 50%),
      radial-gradient(circle at 45% 45%, #f00 0%, transparent 45%),
      radial-gradient(circle at 55% 55%, #0ff 0%, transparent 45%),
      radial-gradient(circle at 45% 55%, #0f0 0%, transparent 45%),
      radial-gradient(circle at 55% 45%, #00f 0%, transparent 45%);
    mask: radial-gradient(
      circle at 50% 50%,
      transparent 0%,
      transparent 10%,
      black 25%
    );
    animation:
      loader-transform 2s infinite alternate,
      loader-opacity 4s infinite;
    animation-timing-function: cubic-bezier(0.6, 0.8, 0.5, 1);
  }
}

@keyframes loader-transform {
  0%   { transform: translate(-55%); }
  100% { transform: translate(55%); }
}

@keyframes loader-opacity {
  0%, 100% { opacity: 0; }
  15%       { opacity: 1; }
  65%       { opacity: 0; }
}

.loader-letter {
  display: inline-block;
  opacity: 0;
  animation: loader-letter-anim 4s infinite linear;
  z-index: 2;

  &:nth-child(1)  { animation-delay: 0.1s; }
  &:nth-child(2)  { animation-delay: 0.205s; }
  &:nth-child(3)  { animation-delay: 0.31s; }
  &:nth-child(4)  { animation-delay: 0.415s; }
  &:nth-child(5)  { animation-delay: 0.521s; }
  &:nth-child(6)  { animation-delay: 0.626s; }
  &:nth-child(7)  { animation-delay: 0.731s; }
  &:nth-child(8)  { animation-delay: 0.837s; }
  &:nth-child(9)  { animation-delay: 0.942s; }
  &:nth-child(10) { animation-delay: 1.047s; }
  &:nth-child(11) { animation-delay: 1.152s; }
}

@keyframes loader-letter-anim {
  0%   { filter: blur(0px); opacity: 0; }
  5%   { opacity: 1; text-shadow: 0 0 4px var(--loader-glow, #fff); filter: blur(0px); transform: scale(1.1) translateY(-2px); }
  20%  { opacity: 0.2; filter: blur(0px); }
  100% { filter: blur(5px); opacity: 0; }
}



.welcome-title {
  font-size: clamp(16px, 2vw, 22px);
  font-weight: bold;
  color: var(--text-primary);
  margin-bottom: 12px;
  white-space: nowrap;
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
  max-width: 800px;
  margin: 0 auto;
  padding: 24px 20px 40px;
}

.message-row {
  margin-bottom: 28px;
  
  // 用户消息：右对齐简约气泡
  &.user {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
     
    .message-content {
      max-width: 75%;
      background: #003f7a;
      color: #fff;
      border-radius: 20px 20px 4px 20px;
      padding: 12px 18px;
    }
  }
  
  // AI 消息：无背景全宽平铺（ChatGPT 风格）
  &.assistant {
    .message-content {
      background: transparent;
      border: none;
      border-radius: 0;
      padding: 0;
      color: var(--text-primary);
      max-width: 100%;
    }
  }
}

.message-reference-card {
  width: min(75%, 520px);
  display: flex;
  align-items: stretch;
  padding: 12px 16px;
  margin-bottom: 10px;
  border-radius: 16px 16px 8px 16px;
  background: linear-gradient(180deg, rgba(244, 248, 255, 0.98) 0%, rgba(238, 245, 255, 0.94) 100%);
  border: 1px solid rgba(59, 130, 246, 0.16);
  color: var(--text-secondary);
  position: relative;
  overflow: hidden;
  box-shadow: 0 10px 24px rgba(59, 130, 246, 0.08);
}

.message-reference-text {
  min-width: 0;

  p {
    margin: 6px 0 0;
    font-size: 13px;
    line-height: 1.6;
    color: var(--text-primary);
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
}

.message-reference-label {
  display: inline-block;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.24px;
  color: rgba(37, 99, 235, 0.92);
}

// 消息操作按钮
.message-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.knowledge-sources {
  margin-top: 18px;
  padding: 18px 18px 16px;
  border: 1px solid var(--border-color);
  border-radius: 18px;
  background: linear-gradient(180deg, rgba(var(--bg-card-rgb), 0.92) 0%, rgba(var(--bg-card-rgb), 0.82) 100%);
  box-shadow: 0 12px 30px var(--shadow-color);
}

.knowledge-sources-header {
  margin-bottom: 14px;

  h4 {
    margin: 4px 0 2px;
    font-size: 16px;
    line-height: 1.2;
    color: var(--text-primary);
    font-weight: 700;
  }

  p {
    margin: 0;
    font-size: 13px;
    color: var(--text-muted);
  }
}

.knowledge-sources-kicker {
  display: inline-flex;
  align-items: center;
  padding: 4px 9px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.04em;
  color: var(--primary-color);
  background: rgba(59, 130, 246, 0.12);
  border: 1px solid rgba(59, 130, 246, 0.18);
}

.knowledge-source-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.knowledge-source-card {
  width: 100%;
  text-align: left;
  padding: 15px 16px 14px;
  border-radius: 16px;
  border: 1px solid var(--border-color);
  background: rgba(var(--bg-card-rgb), 0.7);
  color: inherit;
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease, background-color 0.2s ease, box-shadow 0.2s ease;

  &:hover {
    transform: translateY(-2px);
    border-color: rgba(59, 130, 246, 0.28);
    box-shadow: 0 12px 24px rgba(59, 130, 246, 0.08);
  }

  &.type-study_question .knowledge-source-type {
    background: rgba(59, 130, 246, 0.12);
    color: #2563eb;
  }

  &.type-article .knowledge-source-type {
    background: rgba(16, 185, 129, 0.12);
    color: #059669;
  }
}

.knowledge-source-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 10px;
}

.knowledge-source-type {
  padding: 4px 8px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.03em;
}

.knowledge-source-category {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 12px;
  color: var(--text-muted);
}

.knowledge-source-title {
  font-size: 14px;
  line-height: 1.5;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.knowledge-source-summary {
  font-size: 12px;
  line-height: 1.7;
  color: var(--text-secondary);
  min-height: 40px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.knowledge-source-meta,
.knowledge-source-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 10px;

  span {
    display: inline-flex;
    align-items: center;
    padding: 4px 8px;
    border-radius: 999px;
    font-size: 11px;
    color: var(--text-muted);
    background: var(--bg-card-hover);
  }
}

.knowledge-source-action {
  margin-top: 12px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 600;
  color: var(--primary-color);
}

// 思考中文字流光效果
.thinking-shimmer {
  padding: 2px 0;
}

.shimmer-text {
  font-size: 15px;
  font-weight: 400;
  background: linear-gradient(
    90deg,
    #555 0%,
    #555 20%,
    #e0e0e0 45%,
    #ffffff 50%,
    #e0e0e0 55%,
    #555 80%,
    #555 100%
  );
  background-size: 300% 100%;
  background-clip: text;
  -webkit-background-clip: text;
  color: transparent;
  -webkit-text-fill-color: transparent;
  animation: text-shimmer 2s linear infinite;
}

@keyframes text-shimmer {
  0% { background-position: 100% 0; }
  100% { background-position: -100% 0; }
}

// ========== 深度思考块 ==========
.thinking-block {
  margin-bottom: 12px;
  border: 1px solid var(--border-color);
  border-radius: 12px;
  overflow: hidden;
  background: var(--bg-card);
  transition: all 0.3s ease;
  
  &.streaming {
    border-color: rgba(59, 130, 246, 0.3);
    box-shadow: 0 0 12px rgba(59, 130, 246, 0.08);
  }
  
  .thinking-block-header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 14px;
    cursor: pointer;
    user-select: none;
    transition: background 0.2s;
    
    &:hover {
      background: var(--bg-card-hover);
    }
  }
  
  .thinking-icon {
    color: var(--text-muted);
    flex-shrink: 0;
    
    &.spinning {
      animation: think-spin 2s linear infinite;
      color: $primary-color;
    }
  }
  
  @keyframes think-spin {
    to { transform: rotate(360deg); }
  }
  
  .thinking-block-title {
    font-size: 13px;
    font-weight: 500;
    color: var(--text-secondary);
    flex: 1;
  }
  
  .thinking-timer {
    font-size: 12px;
    color: var(--text-muted);
    font-variant-numeric: tabular-nums;
  }
  
  .thinking-toggle-icon {
    color: var(--text-muted);
    transition: transform 0.3s ease;
    flex-shrink: 0;
  }
  
  &.collapsed .thinking-toggle-icon,
  .thinking-block-content.collapsed + .thinking-toggle-icon {
    transform: rotate(-90deg);
  }
  
  &.collapsed .thinking-block-content {
    display: none;
  }
  
  .thinking-block-content {
    padding: 0 14px 12px;
    font-size: 13px;
    line-height: 1.7;
    color: var(--text-muted);
    border-top: 1px solid var(--border-color);
    max-height: 400px;
    overflow-y: auto;
    word-break: break-word;
    
    &.collapsed {
      display: none;
    }
    
    &::-webkit-scrollbar { width: 4px; }
    &::-webkit-scrollbar-thumb {
      background: var(--border-color);
      border-radius: 2px;
    }
    
    :deep(p) {
      margin: 8px 0;
      &:first-child { margin-top: 8px; }
      &:last-child { margin-bottom: 0; }
    }
    
    :deep(code) {
      font-size: 12px;
      background: var(--bg-main);
      padding: 1px 4px;
      border-radius: 3px;
    }
  }
}

// 生成用时标签
.think-duration {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-muted);
  padding: 2px 8px;
  border-radius: 10px;
  background: var(--bg-main);
  border: 1px solid var(--border-color);
  user-select: none;
}

.message-content {
  font-size: 15px;
  line-height: 1.7;
  word-break: break-word;
  overflow-wrap: break-word;
  
  :deep(p) {
    margin: 0 0 12px;
    &:last-child { margin-bottom: 0; }
  }
  
  :deep(pre) {
    background: transparent;
    padding: 0;
    border-radius: 0;
    overflow-x: auto;
    margin: 0;
    border: none;
    white-space: pre;
    word-break: normal;
    overflow-wrap: normal;
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

  // ── 代码块美化（语言标签 + 复制按钮） ──
  :deep(.code-block-wrapper) {
    margin: 12px 0;
    border-radius: 10px;
    overflow: hidden;
    border: 1px solid var(--border-color);
    background: var(--code-block-bg, #282828);

    .code-block-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 8px 14px;
      background: var(--code-header-bg, rgba(255, 255, 255, 0.06));
      border-bottom: 1px solid var(--code-header-border, rgba(255, 255, 255, 0.06));
    }

    // 左側语言标签 + 折叠按鈕
    .code-toggle-btn {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      background: transparent;
      border: none;
      cursor: pointer;
      color: rgba(255, 255, 255, 0.5);
      padding: 0;
      font-size: 12px;
      font-weight: 600;
      letter-spacing: 0.5px;
      transition: color 0.2s;

      &:hover { color: rgba(255, 255, 255, 0.85); }

      .toggle-icon {
        transition: transform 0.2s ease;
        flex-shrink: 0;
      }
    }

    // 收起状态
    &.collapsed {
      .code-block-pre { display: none; }
      .code-block-header { border-bottom: none; }
      .toggle-icon { transform: rotate(180deg); }
    }

    .code-copy-btn {
      display: inline-flex;
      align-items: center;
      gap: 5px;
      padding: 3px 10px;
      background: transparent;
      border: 1px solid rgba(255, 255, 255, 0.12);
      border-radius: 6px;
      color: rgba(255, 255, 255, 0.5);
      font-size: 11px;
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        background: rgba(255, 255, 255, 0.08);
        border-color: rgba(255, 255, 255, 0.25);
        color: rgba(255, 255, 255, 0.8);
      }

      svg {
        flex-shrink: 0;
      }
    }

    .code-block-pre {
      padding: 14px 16px;
      margin: 0;
      background: transparent;
      border: none;
      overflow-x: auto;

      code {
        font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace;
        font-size: 13px;
        line-height: 1.6;
        background: none;
        padding: 0;
        // 不强制 color，让 hljs token 颜色生效
      }
    }

    &::-webkit-scrollbar { height: 6px; }
    &::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.15); border-radius: 3px; }
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

.assistant-selectable {
  user-select: text;
  -webkit-user-select: text;
  cursor: text;

  :deep(*) {
    user-select: text;
    -webkit-user-select: text;
    cursor: text;
  }

  :deep(.code-block-header),
  :deep(.code-copy-btn),
  :deep(.code-toggle-btn),
  :deep(button),
  :deep(svg) {
    user-select: none;
    -webkit-user-select: none;
  }
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

.selection-ask-btn {
  position: fixed;
  transform: translate(-50%, -100%);
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-radius: 14px;
  border: 1px solid rgba(59, 130, 246, 0.18);
  background: #e5f3ff;
  color: #111827;
  box-shadow: 0 12px 28px rgba(59, 130, 246, 0.16);
  backdrop-filter: blur(12px);
  cursor: pointer;
  z-index: 1200;
  transition: transform 0.18s ease, box-shadow 0.18s ease, background 0.18s ease, border-color 0.18s ease, color 0.18s ease;
  user-select: none;
  -webkit-user-select: none;

  &:hover {
    transform: translate(-50%, calc(-100% - 2px));
    background: #d8ecff;
    border-color: rgba(59, 130, 246, 0.28);
    box-shadow: 0 16px 34px rgba(59, 130, 246, 0.22);
  }

  span {
    font-size: 14px;
    font-weight: 600;
    white-space: nowrap;
  }
}

// 输入区
.input-section {
  position: relative;
  padding: 0 48px 16px;
  background: transparent;
  flex-shrink: 0;
}

.message-actions,
.think-duration,
.thinking-block-header {
  user-select: none;
  -webkit-user-select: none;
}

.resize-handle {
  position: absolute;
  left: 50%;
  top: -6px;
  transform: translateX(-50%);
  width: 88px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: ns-resize;
  user-select: none;
  margin-bottom: 0;
  z-index: 12;

  // 中间拖拽条
  &::before {
    content: '';
    width: 36px;
    height: 4px;
    background: var(--border-color);
    border-radius: 2px;
    transition: background 0.2s, width 0.2s;
  }

  &:hover::before {
    background: $primary-color;
    width: 52px;
  }
}

.input-wrapper {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 12px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 24px;
  padding: 12px 14px 12px 16px;
  transition: height 0.24s cubic-bezier(0.22, 1, 0.36, 1), border-color 0.2s, box-shadow 0.2s, background 0.2s;
  box-sizing: border-box;
  overflow: hidden;
  
  &.focused {
    border-color: $primary-color;
    box-shadow: 0 0 0 3px rgba($primary-color, 0.1);
  }

  &.quoted {
    background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(250, 252, 255, 0.98) 100%);
  }
  
  &.disabled {
    opacity: 0.5;
  }
}

.input-reference-inline {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  min-height: 54px;
  padding: 11px 12px 10px;
  border-radius: 18px;
  background: linear-gradient(180deg, rgba(248, 250, 255, 0.98) 0%, rgba(242, 246, 255, 0.94) 100%);
  border: 1px solid rgba(59, 130, 246, 0.16);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.72), 0 10px 24px rgba(59, 130, 246, 0.08);
}

.input-reference-inline-icon {
  width: 18px;
  height: 18px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: $primary-color;
  margin-top: 2px;
}

.input-reference-inline-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.input-reference-inline-label {
  font-size: 11px;
  line-height: 1.2;
  font-weight: 600;
  letter-spacing: 0.24px;
  color: rgba(59, 130, 246, 0.86);
}

.input-reference-inline-text {
  color: var(--text-primary);
  font-size: 14px;
  line-height: 1.4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.input-reference-inline-remove {
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: transparent;
  color: rgba(15, 23, 42, 0.52);
  cursor: pointer;
  transition: background 0.18s ease, color 0.18s ease, transform 0.18s ease;

  &:hover {
    background: rgba(148, 163, 184, 0.16);
    color: var(--text-primary);
    transform: scale(1.04);
  }
}

.composer-row {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  min-height: 0;
  flex: 1;
}

.input-wrapper textarea {
  flex: 1;
  border: none;
  background: transparent;
  resize: none;
  font-size: 15px;
  line-height: 1.5;
  color: var(--text-primary);
  height: 100%;
  min-height: 24px;
  
  &::placeholder { color: var(--text-muted); }
  &:focus { outline: none; }
}

.quote-inline-enter-active,
.quote-inline-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease, max-height 0.22s ease, margin 0.22s ease;
}

.quote-inline-enter-from,
.quote-inline-leave-to {
  opacity: 0;
  transform: translateY(10px);
  max-height: 0;
}

.quote-inline-enter-to,
.quote-inline-leave-from {
  opacity: 1;
  transform: translateY(0);
  max-height: 72px;
}

.send-btn {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: $primary-color;
  color: #fff;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  flex-shrink: 0;
  
  &:hover:not(:disabled) {
    transform: scale(1.05);
    box-shadow: 0 4px 16px rgba($primary-color, 0.45);
    background: lighten($primary-color, 8%);
  }
  
  &:disabled {
    opacity: 0.35;
    cursor: not-allowed;
  }
}

.stop-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: $primary-color;
  color: #fff;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  flex-shrink: 0;
  
  &:hover {
    transform: scale(1.05);
    box-shadow: 0 4px 20px rgba($primary-color, 0.4);
  }
}

.input-disclaimer {
  text-align: center;
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 16px;
  margin-bottom: 4px;
  letter-spacing: 0.2px;
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

.mobile-sidebar-toggle {
  display: none;
  position: absolute;
  top: 16px;
  left: 16px;
  z-index: 90;
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  color: var(--text-primary);
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

// 响应式
@media (max-width: 768px) {
  .mobile-sidebar-toggle {
    display: flex;
  }
  
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    bottom: 0;
    z-index: 100;
    transition: transform 0.3s ease;
    
    &.collapsed {
      transform: translateX(-100%);
      width: 260px;
    }
  }
  
  .messages-wrapper, .input-section {
    padding: 16px;
  }

  .input-section {
    padding-top: 0;
    background: transparent;
  }

  .model-switcher {
    left: 16px;
    top: -44px;
  }
  
  .message-row.user .message-content {
    max-width: 90%;
  }

  .knowledge-sources {
    padding: 14px;
    border-radius: 16px;
  }

  .knowledge-source-list {
    grid-template-columns: 1fr;
  }
  
  .quick-prompts {
    flex-direction: column;
  }
}

// ========== 滚动到底部浮动按钮 ==========
.scroll-to-bottom-btn {
  position: absolute;
  bottom: 160px; // fallback，实际由 :style 动态覆盖
  left: 50%;
  transform: translateX(-50%);
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--bg-card);
  border: 1.5px solid var(--border-color);
  color: var(--text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 50;
  transition: border-color 0.2s, box-shadow 0.2s, opacity 0.2s;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);

  &:hover {
    border-color: $primary-color;
    color: $primary-color;
    transform: translateX(-50%) scale(1.08);
    box-shadow: 0 6px 24px rgba(59, 130, 246, 0.2);
  }

  // AI 输出中 → 边框旋转渐变动效
  &.generating {
    border-color: transparent;
    background-clip: padding-box;
    // 用 ::before 伪元素画旋转渐变边框
    &::before {
      content: '';
      position: absolute;
      inset: -2px;
      border-radius: 50%;
      background: conic-gradient(
        from 0deg,
        $primary-color 0%,
        #8b5cf6 30%,
        transparent 60%,
        transparent 100%
      );
      animation: border-spin 1.2s linear infinite;
      z-index: -1;
    }

    // 内圆遮挡（让旋转只作为边框可见）
    &::after {
      content: '';
      position: absolute;
      inset: 1.5px;
      border-radius: 50%;
      background: var(--bg-card);
      z-index: -1;
    }
  }
}

@keyframes border-spin {
  to { transform: rotate(360deg); }
}

// 按钮出入动画
.scroll-btn-fade-enter-active,
.scroll-btn-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}
.scroll-btn-fade-enter-from,
.scroll-btn-fade-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(12px);
}
</style>

<!-- hljs 暗色主题（默认） -->
<style>
/* loader 主题色变量 */
:root {
  --loader-text-color: #ffffff;
  --loader-glow: #ffffff;
}
:root[data-theme="light"] {
  --loader-text-color: #140e08;
  --loader-glow: #8d8379;
}

:root[data-theme="dark"] .hljs {
  background: transparent;
  color: #abb2bf;
}
:root[data-theme="dark"] .hljs-comment,
:root[data-theme="dark"] .hljs-quote { color: #5c6370; font-style: italic; }
:root[data-theme="dark"] .hljs-keyword,
:root[data-theme="dark"] .hljs-selector-tag,
:root[data-theme="dark"] .hljs-addition { color: #c678dd; }
:root[data-theme="dark"] .hljs-number,
:root[data-theme="dark"] .hljs-string,
:root[data-theme="dark"] .hljs-meta .hljs-meta-string,
:root[data-theme="dark"] .hljs-literal,
:root[data-theme="dark"] .hljs-doctag,
:root[data-theme="dark"] .hljs-regexp { color: #98c379; }
:root[data-theme="dark"] .hljs-title,
:root[data-theme="dark"] .hljs-section,
:root[data-theme="dark"] .hljs-name,
:root[data-theme="dark"] .hljs-selector-id,
:root[data-theme="dark"] .hljs-selector-class { color: #e06c75; }
:root[data-theme="dark"] .hljs-attribute,
:root[data-theme="dark"] .hljs-attr,
:root[data-theme="dark"] .hljs-variable,
:root[data-theme="dark"] .hljs-template-variable,
:root[data-theme="dark"] .hljs-type,
:root[data-theme="dark"] .hljs-selector-attr,
:root[data-theme="dark"] .hljs-selector-pseudo,
:root[data-theme="dark"] .hljs-link { color: #d19a66; }
:root[data-theme="dark"] .hljs-built_in,
:root[data-theme="dark"] .hljs-builtin-name { color: #56b6c2; }
:root[data-theme="dark"] .hljs-deletion { color: #e06c75; }

/* 亮色下用户气泡颜色 */
:root[data-theme="light"] .message-row.user .message-content {
  background: #e5f3ff;
  color: #1a3a5c;
}

/* 亮色下流光文字 */
:root[data-theme="light"] .shimmer-text {
  background: linear-gradient(
    90deg,
    #999 0%,
    #999 20%,
    #333 45%,
    #111 50%,
    #333 55%,
    #999 80%,
    #999 100%
  );
  background-size: 300% 100%;
  background-clip: text;
  -webkit-background-clip: text;
  color: transparent;
  -webkit-text-fill-color: transparent;
  animation: text-shimmer 2s linear infinite;
}

/* 亮色主题代码块容器 */
:root[data-theme="light"] {
  --code-block-bg: #f6f8fa;
  --code-header-bg: rgba(0, 0, 0, 0.04);
  --code-header-border: rgba(0, 0, 0, 0.06);
}

/* hljs 亮色主题 */
:root[data-theme="light"] .hljs {
  background: transparent;
  color: #24292e;
}
:root[data-theme="light"] .hljs-comment,
:root[data-theme="light"] .hljs-quote { color: #6a737d; font-style: italic; }
:root[data-theme="light"] .hljs-keyword,
:root[data-theme="light"] .hljs-selector-tag { color: #d73a49; }
:root[data-theme="light"] .hljs-number,
:root[data-theme="light"] .hljs-literal { color: #005cc5; }
:root[data-theme="light"] .hljs-string,
:root[data-theme="light"] .hljs-regexp,
:root[data-theme="light"] .hljs-addition { color: #032f62; }
:root[data-theme="light"] .hljs-title,
:root[data-theme="light"] .hljs-section,
:root[data-theme="light"] .hljs-name { color: #6f42c1; }
:root[data-theme="light"] .hljs-attribute,
:root[data-theme="light"] .hljs-attr { color: #005cc5; }
:root[data-theme="light"] .hljs-variable,
:root[data-theme="light"] .hljs-template-variable,
:root[data-theme="light"] .hljs-type { color: #e36209; }
:root[data-theme="light"] .hljs-built_in,
:root[data-theme="light"] .hljs-builtin-name { color: #005cc5; }
:root[data-theme="light"] .hljs-deletion { color: #b31d28; }

/* 亮色下代码块内 toggle/copy 按钮颜色 */
:root[data-theme="light"] .code-toggle-btn { color: rgba(0, 0, 0, 0.45) !important; }
:root[data-theme="light"] .code-toggle-btn:hover { color: rgba(0, 0, 0, 0.8) !important; }
:root[data-theme="light"] .code-copy-btn {
  color: rgba(0, 0, 0, 0.45) !important;
  border-color: rgba(0, 0, 0, 0.12) !important;
}
:root[data-theme="light"] .code-copy-btn:hover {
  background: rgba(0, 0, 0, 0.05) !important;
  border-color: rgba(0, 0, 0, 0.25) !important;
  color: rgba(0, 0, 0, 0.8) !important;
}

:root[data-theme="light"] .selection-ask-btn {
  background: #e5f3ff;
  color: #111827;
  border-color: rgba(59, 130, 246, 0.18);
  box-shadow: 0 12px 28px rgba(59, 130, 246, 0.16);
}

:root[data-theme="light"] .selection-ask-btn:hover {
  background: #d8ecff;
  border-color: rgba(59, 130, 246, 0.28);
  box-shadow: 0 16px 34px rgba(59, 130, 246, 0.22);
}

:root[data-theme="light"] .message-reference-card {
  background: linear-gradient(180deg, rgba(244, 248, 255, 0.98) 0%, rgba(238, 245, 255, 0.94) 100%);
  border-color: rgba(59, 130, 246, 0.16);
  box-shadow: 0 10px 24px rgba(59, 130, 246, 0.08);
}

:root[data-theme="light"] .message-reference-label {
  color: rgba(37, 99, 235, 0.92);
}

:root[data-theme="light"] .knowledge-sources {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(247, 249, 252, 0.96) 100%);
  border-color: rgba(15, 23, 42, 0.08);
}

:root[data-theme="light"] .knowledge-source-card {
  background: rgba(248, 250, 252, 0.88);
  border-color: rgba(15, 23, 42, 0.08);
  box-shadow: 0 8px 22px rgba(15, 23, 42, 0.06);
}

:root[data-theme="dark"] .input-reference-inline {
  background: linear-gradient(180deg, rgba(50, 54, 62, 0.94) 0%, rgba(39, 42, 48, 0.94) 100%);
  border-color: rgba(96, 165, 250, 0.18);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.04);
}

:root[data-theme="dark"] .input-wrapper.quoted {
  background: linear-gradient(180deg, rgba(31, 35, 41, 0.98) 0%, rgba(24, 27, 32, 0.98) 100%);
}

:root[data-theme="dark"] .input-reference-inline-label {
  color: rgba(147, 197, 253, 0.92);
}

:root[data-theme="dark"] .input-reference-inline-remove {
  color: rgba(255, 255, 255, 0.54);
}

:root[data-theme="dark"] .input-reference-inline-remove:hover {
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.88);
}

:root[data-theme="dark"] .selection-ask-btn {
  background: rgba(17, 17, 19, 0.96);
  color: #f8fafc;
  border-color: rgba(255, 255, 255, 0.08);
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.32);
}

:root[data-theme="dark"] .selection-ask-btn:hover {
  background: rgba(10, 10, 12, 0.98);
  border-color: rgba(255, 255, 255, 0.14);
  box-shadow: 0 18px 44px rgba(0, 0, 0, 0.4);
}

:root[data-theme="dark"] .message-reference-card {
  background: linear-gradient(180deg, rgba(40, 46, 56, 0.96) 0%, rgba(31, 36, 44, 0.96) 100%);
  border-color: rgba(96, 165, 250, 0.16);
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.2);
}

:root[data-theme="dark"] .message-reference-label {
  color: rgba(147, 197, 253, 0.94);
}

:root[data-theme="dark"] .knowledge-sources {
  background: linear-gradient(180deg, rgba(31, 36, 44, 0.96) 0%, rgba(24, 28, 35, 0.96) 100%);
  border-color: rgba(255, 255, 255, 0.08);
  box-shadow: 0 18px 34px rgba(0, 0, 0, 0.24);
}

:root[data-theme="dark"] .knowledge-source-card {
  background: rgba(255, 255, 255, 0.03);
  border-color: rgba(255, 255, 255, 0.06);
}
</style>
