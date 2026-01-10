<template>
  <div class="article-detail-page">
    <!-- 加载状态 -->
    <Loading v-if="loading" />

    <!-- 文章内容 -->
    <template v-else-if="article">
      <!-- 返回导航 -->
      <div class="back-nav">
        <button class="back-btn" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回</span>
        </button>
      </div>
      
      <article class="article-card glass-card">
        <!-- 文章头部 -->
        <header class="article-header">
          <div class="title-row">
            <h1 class="article-title">{{ article.title }}</h1>
            <button class="go-comment-btn" @click="scrollToComment">
              <el-icon><ChatDotRound /></el-icon>
              <span>评论</span>
            </button>
          </div>
          <div class="article-info">
            <div class="author-info" v-if="article.author">
              <el-avatar :size="36" :src="article.author.avatar">
                {{ article.author.nickname?.charAt(0) }}
              </el-avatar>
              <span class="author-name">{{ article.author.nickname }}</span>
            </div>
            <div class="meta-info">
              <span><el-icon><Calendar /></el-icon>{{ formatDate(article.publishTime) }}</span>
              <span><el-icon><View /></el-icon>{{ article.viewCount }} 阅读</span>
              <span><el-icon><ChatDotRound /></el-icon>{{ article.commentCount }} 评论</span>
            </div>
          </div>
        </header>

        <!-- 封面图 -->
        <div v-if="article.cover" class="article-cover">
          <img :src="article.cover" :alt="article.title" />
        </div>

        <!-- 标签 -->
        <div class="article-tags" v-if="article.tags && article.tags.length">
          <router-link
            v-for="tag in article.tags"
            :key="tag.id"
            :to="`/tag/${tag.id}`"
            class="tag-item"
            :style="{ backgroundColor: (tag.color || '#4a9eff') + '20', color: tag.color || '#4a9eff' }"
          >
            <el-icon><PriceTag /></el-icon>
            {{ tag.name }}
          </router-link>
        </div>

        <!-- 摘要 -->
        <div v-if="article.summary" class="article-summary">
          <div class="summary-icon">
            <el-icon><Document /></el-icon>
          </div>
          <p>{{ article.summary }}</p>
        </div>

        <!-- 文章正文 -->
        <div class="article-body">
          <div v-if="articleContent" class="content-html" v-html="articleContent"></div>
          <div v-else-if="article.content" class="content-raw">{{ article.content }}</div>
          <el-empty v-else description="暂无内容" />
        </div>

        <!-- 文章底部操作 -->
        <footer class="article-footer">
          <div class="action-buttons">
            <!-- 点赞按钮 - 爱心动画 -->
            <div class="action-btn-wrapper" @click="handleLike">
              <div class="heart-container" :class="{ checked: article.isLiked }">
                <div class="svg-container">
                  <svg viewBox="0 0 24 24" class="svg-outline" xmlns="http://www.w3.org/2000/svg">
                    <path d="M17.5,1.917a6.4,6.4,0,0,0-5.5,3.3,6.4,6.4,0,0,0-5.5-3.3A6.8,6.8,0,0,0,0,8.967c0,4.547,4.786,9.513,8.8,12.88a4.974,4.974,0,0,0,6.4,0C19.214,18.48,24,13.514,24,8.967A6.8,6.8,0,0,0,17.5,1.917Zm-3.585,18.4a2.973,2.973,0,0,1-3.83,0C4.947,16.006,2,11.87,2,8.967a4.8,4.8,0,0,1,4.5-5.05A4.8,4.8,0,0,1,11,8.967a1,1,0,0,0,2,0,4.8,4.8,0,0,1,4.5-5.05A4.8,4.8,0,0,1,22,8.967C22,11.87,19.053,16.006,13.915,20.313Z"></path>
                  </svg>
                  <svg viewBox="0 0 24 24" class="svg-filled" xmlns="http://www.w3.org/2000/svg">
                    <path d="M17.5,1.917a6.4,6.4,0,0,0-5.5,3.3,6.4,6.4,0,0,0-5.5-3.3A6.8,6.8,0,0,0,0,8.967c0,4.547,4.786,9.513,8.8,12.88a4.974,4.974,0,0,0,6.4,0C19.214,18.48,24,13.514,24,8.967A6.8,6.8,0,0,0,17.5,1.917Z"></path>
                  </svg>
                  <svg class="svg-celebrate" width="100" height="100" xmlns="http://www.w3.org/2000/svg">
                    <polygon points="10,10 20,20"></polygon>
                    <polygon points="10,50 20,50"></polygon>
                    <polygon points="20,80 30,70"></polygon>
                    <polygon points="90,10 80,20"></polygon>
                    <polygon points="90,50 80,50"></polygon>
                    <polygon points="80,80 70,70"></polygon>
                  </svg>
                </div>
              </div>
              <span class="action-text">{{ article.isLiked ? '已点赞' : '点赞' }}</span>
              <em class="action-count">{{ article.likeCount || 0 }}</em>
            </div>

            <!-- 收藏按钮 - 书签动画 -->
            <div class="action-btn-wrapper" @click="handleFavorite">
              <label class="ui-bookmark" :class="{ checked: article.isFavorited }">
                <div class="bookmark">
                  <svg viewBox="0 0 32 32">
                    <g>
                      <path d="M27 4v27a1 1 0 0 1-1.625.781L16 24.281l-9.375 7.5A1 1 0 0 1 5 31V4a4 4 0 0 1 4-4h14a4 4 0 0 1 4 4z"></path>
                    </g>
                  </svg>
                </div>
              </label>
              <span class="action-text">{{ article.isFavorited ? '已收藏' : '收藏' }}</span>
              <em class="action-count">{{ article.favoriteCount || 0 }}</em>
            </div>
          </div>
        </footer>
      </article>

      <!-- 评论区 -->
      <section id="comment-section" class="comment-section glass-card">
        <h3 class="section-title">
          <el-icon><ChatDotRound /></el-icon>
          评论区 <span class="comment-count">({{ totalComments }})</span>
        </h3>

        <!-- 发表评论 -->
        <div class="comment-form">
          <el-avatar :size="40" :src="userStore.userInfo?.avatar">
            {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
          </el-avatar>
          <div class="form-wrapper">
            <el-input
              v-model="commentContent"
              type="textarea"
              :rows="3"
              :placeholder="userStore.isLoggedIn ? '说说你的看法...' : '请先登录后发表评论'"
              :disabled="!userStore.isLoggedIn"
              resize="none"
            />
            <div class="form-footer">
              <span v-if="replyTo" class="reply-info">
                回复 @{{ replyTo.user?.nickname }}
                <el-icon @click="cancelReply" class="close-icon"><Close /></el-icon>
              </span>
              <button 
                class="submit-comment-btn"
                :disabled="!userStore.isLoggedIn || !commentContent.trim() || submitting"
                @click="submitComment"
              >
                {{ submitting ? '发送中...' : '发表评论' }}
                <div class="icon">
                  <svg
                    height="24"
                    width="24"
                    viewBox="0 0 24 24"
                    xmlns="http://www.w3.org/2000/svg"
                  >
                    <path d="M0 0h24v24H0z" fill="none"></path>
                    <path
                      d="M16.172 11l-5.364-5.364 1.414-1.414L20 12l-7.778 7.778-1.414-1.414L16.172 13H4v-2z"
                      fill="currentColor"
                    ></path>
                  </svg>
                </div>
              </button>
            </div>
          </div>
        </div>

        <!-- 评论列表 -->
        <div class="comment-list" v-if="comments.length > 0">
          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <el-avatar :size="44" :src="comment.user?.avatar">
              {{ comment.user?.nickname?.charAt(0) || 'U' }}
            </el-avatar>
            <div class="comment-body">
              <div class="comment-header">
                <span class="comment-author">{{ comment.user?.nickname || '匿名用户' }}</span>
                <span class="comment-time">{{ formatRelativeTime(comment.createTime) }}</span>
              </div>
              <p class="comment-content">{{ comment.content }}</p>
              <div class="comment-actions">
                <span class="action-item" @click="handleLikeComment(comment)">
                  <el-icon><Star /></el-icon> {{ comment.likeCount || 0 }}
                </span>
                <span class="action-item" @click="handleReply(comment)">
                  <el-icon><ChatDotRound /></el-icon> 回复
                </span>
              </div>
              
              <!-- 子评论 -->
              <div v-if="comment.replies && comment.replies.length" class="reply-list">
                <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
                  <el-avatar :size="32" :src="reply.user?.avatar">
                    {{ reply.user?.nickname?.charAt(0) || 'U' }}
                  </el-avatar>
                  <div class="reply-body">
                    <span class="reply-author">{{ reply.user?.nickname }}</span>
                    <span v-if="reply.replyUser" class="reply-to">
                      回复 <em>@{{ reply.replyUser.nickname }}</em>
                    </span>
                    <span class="reply-content">{{ reply.content }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 无评论 -->
        <div v-else class="no-comment">
          <el-icon :size="48"><ChatDotRound /></el-icon>
          <p>暂无评论，快来抢沙发吧~</p>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Calendar, View, ChatDotRound, Star, Close, PriceTag, Document, ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getArticleDetail, likeArticle, unlikeArticle, favoriteArticle, unfavoriteArticle } from '@/api/article'
import { getCommentList, createComment, likeComment } from '@/api/comment'
import { formatDate, formatRelativeTime } from '@/utils/format'
import { useUserStore } from '@/stores/user'
import Loading from '@/components/Loading/index.vue'
import hljs from 'highlight.js'
import 'highlight.js/styles/atom-one-dark.css'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const article = ref(null)
const comments = ref([])
const totalComments = ref(0)
const loading = ref(true)
const submitting = ref(false)
const commentContent = ref('')
const replyTo = ref(null)

// 滚动到评论区
function scrollToComment() {
  const commentSection = document.getElementById('comment-section')
  if (commentSection) {
    commentSection.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

// 返回上一页
function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
  }
}

// 代码高亮并添加复制按钮
function highlightCode() {
  nextTick(() => {
    document.querySelectorAll('.article-body pre code').forEach((block) => {
      // 获取 pre 元素
      const pre = block.parentElement
      if (!pre) return
      
      // 检查是否已经处理过（通过自定义属性标记）
      if (pre.dataset.highlighted === 'true') return
      pre.dataset.highlighted = 'true'
      
      // 执行代码高亮
      hljs.highlightElement(block)
      
      // 设置 pre 为相对定位
      pre.style.position = 'relative'
      
      // 创建复制按钮
      const copyBtn = document.createElement('button')
      copyBtn.className = 'copy-btn'
      copyBtn.innerHTML = `
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
          <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
        </svg>
        <span>复制</span>
      `
      
      // 点击复制
      copyBtn.addEventListener('click', async () => {
        const code = block.textContent || ''
        try {
          await navigator.clipboard.writeText(code)
          copyBtn.innerHTML = `
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="20 6 9 17 4 12"></polyline>
            </svg>
            <span>已复制</span>
          `
          copyBtn.classList.add('copied')
          ElMessage.success('复制成功')
          
          // 2秒后恢复
          setTimeout(() => {
            copyBtn.innerHTML = `
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
                <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
              </svg>
              <span>复制</span>
            `
            copyBtn.classList.remove('copied')
          }, 2000)
        } catch (err) {
          ElMessage.error('复制失败')
        }
      })
      
      pre.appendChild(copyBtn)
    })
  })
}

// 计算属性：处理文章内容
const articleContent = computed(() => {
  if (!article.value) return ''
  
  const content = article.value.content
  const contentHtml = article.value.contentHtml
  
  // 优先使用 contentHtml
  if (contentHtml && contentHtml.trim()) {
    // 如果 contentHtml 中还有未解析的 Markdown 图片语法，也进行处理
    let html = contentHtml
    // 处理可能遗留的 Markdown 图片语法
    html = html.replace(/!\[([^\]]*)\]\(([^)]+)\)/g, '<img src="$2" alt="$1" class="article-img" />')
    return html
  }
  
  // 否则解析 markdown content
  if (content && content.trim()) {
    return parseMarkdown(content)
  }
  
  return ''
})

// Markdown 解析函数
function parseMarkdown(text) {
  if (!text) return ''
  
  // 处理字面量 \n 转换为真正的换行
  let content = text.replace(/\\n/g, '\n')
  
  // 处理代码块
  content = content.replace(/```(\w*)\n?([\s\S]*?)```/g, (match, lang, code) => {
    return `<pre class="code-block"><code class="language-${lang || 'text'}">${escapeHtml(code.trim())}</code></pre>`
  })
  
  // 处理行内代码
  content = content.replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>')
  
  // 处理标题
  content = content.replace(/^### (.*$)/gm, '<h3>$1</h3>')
  content = content.replace(/^## (.*$)/gm, '<h2>$1</h2>')
  content = content.replace(/^# (.*$)/gm, '<h1>$1</h1>')
  
  // 处理粗体和斜体
  content = content.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
  content = content.replace(/\*([^*]+)\*/g, '<em>$1</em>')
  
  // 处理图片 ![alt](url) - 必须在链接之前处理
  content = content.replace(/!\[([^\]]*)\]\(([^)]+)\)/g, '<img src="$2" alt="$1" class="article-img" />')
  
  // 处理链接 [text](url)
  content = content.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank">$1</a>')
  
  // 处理段落和换行
  const paragraphs = content.split(/\n\n+/)
  content = paragraphs.map(p => {
    p = p.trim()
    if (!p) return ''
    if (p.startsWith('<h') || p.startsWith('<pre') || p.startsWith('<ul') || p.startsWith('<ol') || p.startsWith('<img')) {
      return p
    }
    return `<p>${p.replace(/\n/g, '<br>')}</p>`
  }).join('\n')
  
  return content
}

function escapeHtml(text) {
  const map = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' }
  return text.replace(/[&<>"']/g, m => map[m])
}

// 获取文章详情
async function fetchArticle() {
  loading.value = true
  try {
    const res = await getArticleDetail(route.params.id)
    if (res && res.data) {
      article.value = res.data
    }
  } catch (e) {
    console.error('获取文章失败:', e)
    ElMessage.error('获取文章失败')
  } finally {
    loading.value = false
  }
  
  // 单独获取评论，不影响文章加载
  fetchComments()
}

// 获取评论列表
async function fetchComments() {
  try {
    const res = await getCommentList(route.params.id, { page: 1, pageSize: 100 })
    if (res && res.data) {
      comments.value = res.data.list || []
      totalComments.value = res.data.total || 0
    }
  } catch (e) {
    console.error('获取评论失败:', e)
  }
}

// 点赞文章
async function handleLike() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    if (article.value.isLiked) {
      await unlikeArticle(article.value.id)
      article.value.isLiked = false
      article.value.likeCount = Math.max(0, (article.value.likeCount || 1) - 1)
    } else {
      await likeArticle(article.value.id)
      article.value.isLiked = true
      article.value.likeCount = (article.value.likeCount || 0) + 1
    }
  } catch (e) {
    console.error(e)
  }
}

// 收藏文章
async function handleFavorite() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    if (article.value.isFavorited) {
      await unfavoriteArticle(article.value.id)
      article.value.isFavorited = false
      article.value.favoriteCount = Math.max(0, (article.value.favoriteCount || 1) - 1)
    } else {
      await favoriteArticle(article.value.id)
      article.value.isFavorited = true
      article.value.favoriteCount = (article.value.favoriteCount || 0) + 1
    }
  } catch (e) {
    console.error(e)
  }
}

// 发表评论
async function submitComment() {
  if (!commentContent.value.trim()) return
  submitting.value = true
  try {
    const data = { 
      articleId: Number(route.params.id), 
      content: commentContent.value 
    }
    if (replyTo.value) {
      data.parentId = replyTo.value.id
      data.replyUserId = replyTo.value.user?.id
    }
    await createComment(data)
    commentContent.value = ''
    replyTo.value = null
    ElMessage.success('评论成功')
    await fetchComments()
    if (article.value) {
      article.value.commentCount = (article.value.commentCount || 0) + 1
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('评论失败')
  } finally {
    submitting.value = false
  }
}

// 回复评论
function handleReply(comment) {
  replyTo.value = comment
  commentContent.value = ''
}

// 取消回复
function cancelReply() {
  replyTo.value = null
}

// 点赞评论
async function handleLikeComment(comment) {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    await likeComment(comment.id)
    comment.likeCount = (comment.likeCount || 0) + 1
  } catch (e) {
    console.error(e)
  }
}

// 监听路由变化
watch(() => route.params.id, (newId) => {
  if (newId) {
    fetchArticle()
  }
})

// 监听文章内容变化，执行代码高亮
watch(articleContent, () => {
  highlightCode()
})

onMounted(() => {
  fetchArticle()
})
</script>

<style lang="scss" scoped>
.article-detail-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

/* 返回导航 */
.back-nav {
  margin-bottom: 16px;
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
  transition: all 0.2s ease;
  
  .el-icon {
    font-size: 16px;
  }
  
  &:hover {
    color: var(--primary-color);
    border-color: var(--primary-color);
    background: rgba(59, 130, 246, 0.05);
  }
}

.loading-container {
  padding: 40px;
  background: var(--bg-card);
  border-radius: 16px;
  transition: background-color 0.3s;
}

.glass-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  transition: background-color 0.3s, border-color 0.3s;
}

.article-card {
  padding: 32px;
  margin-bottom: 24px;
}

.article-header {
  margin-bottom: 24px;
}

.title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.article-title {
  flex: 1;
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.4;
  transition: color 0.3s;
}

/* 评论按钮 */
.go-comment-btn {
  --color: #560bad;
  flex-shrink: 0;
  font-family: inherit;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: 8em;
  height: 2.6em;
  line-height: 2.5em;
  position: relative;
  cursor: pointer;
  overflow: hidden;
  border: 2px solid var(--color);
  transition: color 0.5s;
  z-index: 1;
  font-size: 14px;
  border-radius: 6px;
  font-weight: 500;
  color: var(--color);
  background: transparent;
  
  .el-icon {
    font-size: 16px;
    transition: color 0.5s;
  }
  
  &:before {
    content: "";
    position: absolute;
    z-index: -1;
    background: var(--color);
    height: 150px;
    width: 200px;
    border-radius: 50%;
    top: 100%;
    left: 100%;
    transition: all 0.7s;
  }
  
  &:hover {
    color: #fff;
    
    .el-icon {
      color: #fff;
    }
    
    &:before {
      top: -30px;
      left: -30px;
    }
  }
  
  &:active:before {
    background: #3a0ca3;
    transition: background 0s;
  }
}

.article-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
  .author-name {
    font-size: 15px;
    color: var(--primary-color);
    font-weight: 500;
  }
}

.meta-info {
  display: flex;
  gap: 20px;
  span {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 14px;
    color: var(--text-muted);
  }
}

.article-cover {
  margin-bottom: 24px;
  border-radius: 12px;
  overflow: hidden;
  max-height: 200px;
  img {
    width: 100%;
    height: 200px;
    object-fit: cover;
  }
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 24px;
}

.tag-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  text-decoration: none;
  transition: all 0.3s;
  &:hover {
    transform: translateY(-2px);
    opacity: 0.9;
  }
}

.article-summary {
  display: flex;
  gap: 16px;
  padding: 20px;
  margin-bottom: 24px;
  background: rgba(59, 130, 246, 0.1);
  border-left: 4px solid var(--primary-color);
  border-radius: 8px;
  .summary-icon {
    color: var(--primary-color);
    font-size: 24px;
  }
  p {
    color: var(--text-secondary);
    line-height: 1.8;
    margin: 0;
    font-size: 15px;
  }
}

.article-body {
  color: var(--text-secondary);
  font-size: 16px;
  line-height: 1.9;
  min-height: 200px;

  .content-raw {
    white-space: pre-wrap;
    word-break: break-word;
  }

  :deep(h1), :deep(h2), :deep(h3) {
    color: var(--text-primary);
    margin: 28px 0 16px;
    font-weight: 600;
  }
  :deep(h1) { font-size: 26px; }
  :deep(h2) { 
    font-size: 22px;
    padding-bottom: 10px;
    border-bottom: 1px solid var(--border-color);
  }
  :deep(h3) { font-size: 18px; }
  :deep(p) { margin-bottom: 16px; }
  :deep(.code-block) {
    background: #282c34;
    padding: 16px;
    border-radius: 8px;
    overflow-x: auto;
    margin: 16px 0;
    code {
      font-family: 'Fira Code', 'Consolas', 'Monaco', monospace;
      font-size: 14px;
      line-height: 1.6;
      white-space: pre;
    }
  }
  
  // highlight.js 代码块样式
  :deep(pre) {
    background: #282c34;
    padding: 16px;
    border-radius: 8px;
    overflow-x: auto;
    margin: 16px 0;
    position: relative;
    
    code {
      font-family: 'Fira Code', 'Consolas', 'Monaco', monospace;
      font-size: 14px;
      line-height: 1.6;
      background: transparent;
      padding: 0;
    }
    
    &.hljs {
      padding: 16px;
    }
    
    // 复制按钮样式
    .copy-btn {
      position: absolute;
      top: 8px;
      right: 8px;
      display: flex;
      align-items: center;
      gap: 4px;
      padding: 6px 10px;
      background: rgba(255, 255, 255, 0.1);
      border: 1px solid rgba(255, 255, 255, 0.15);
      border-radius: 6px;
      color: rgba(255, 255, 255, 0.7);
      font-size: 12px;
      cursor: pointer;
      opacity: 0;
      transition: all 0.2s ease;
      
      svg {
        width: 14px;
        height: 14px;
      }
      
      &:hover {
        background: rgba(255, 255, 255, 0.2);
        color: #fff;
      }
      
      &.copied {
        background: rgba(34, 197, 94, 0.2);
        border-color: rgba(34, 197, 94, 0.3);
        color: #22c55e;
      }
    }
    
    &:hover .copy-btn {
      opacity: 1;
    }
  }
  
  :deep(code.hljs) {
    padding: 16px;
    border-radius: 8px;
  }
  :deep(.inline-code) {
    background: rgba(59, 130, 246, 0.15);
    color: var(--primary-color);
    padding: 2px 8px;
    border-radius: 4px;
    font-family: 'Fira Code', monospace;
    font-size: 14px;
  }
  :deep(a) {
    color: var(--primary-color);
    text-decoration: underline;
  }
  :deep(strong) {
    color: var(--text-primary);
  }
  
  // 文章内图片样式
  :deep(.article-img),
  :deep(img:not(.el-avatar img)) {
    max-width: 100%;
    height: auto;
    border-radius: 8px;
    margin: 16px 0;
    display: block;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
}

.article-footer {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--border-color);
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 40px;
}

/* 按钮包装器 */
.action-btn-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 20px;
  background: var(--bg-card-hover);
  border: 1px solid var(--border-color);
  border-radius: 25px;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    background: var(--bg-card);
    border-color: var(--border-light);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
  
  .action-text {
    font-size: 14px;
    color: var(--text-secondary);
    font-weight: 500;
    transition: color 0.3s;
  }
  
  .action-count {
    font-style: normal;
    font-size: 14px;
    color: var(--text-muted);
    transition: color 0.3s;
  }
}

/* 爱心点赞按钮 */
.heart-container {
  --heart-color: var(--text-muted);
  position: relative;
  width: 28px;
  height: 28px;
  transition: 0.3s;
  
  .svg-container {
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
  }
  
  .svg-outline,
  .svg-filled {
    fill: var(--heart-color);
    position: absolute;
    width: 24px;
    height: 24px;
  }
  
  .svg-filled {
    display: none;
  }
  
  .svg-celebrate {
    position: absolute;
    display: none;
    stroke: var(--heart-color);
    fill: var(--heart-color);
    stroke-width: 2px;
    width: 60px;
    height: 60px;
    left: 50%;
    top: 50%;
    transform: translate(-50%, -50%);
  }
  
  &.checked {
    --heart-color: #ff5b89;
    
    .svg-filled {
      display: block;
      animation: keyframes-svg-filled 1s;
    }
    
    .svg-outline {
      display: none;
    }
    
    .svg-celebrate {
      display: block;
      animation: keyframes-svg-celebrate 0.5s forwards;
    }
  }
}

.action-btn-wrapper:hover .heart-container {
  --heart-color: #ff5b89;
}

@keyframes keyframes-svg-filled {
  0% { transform: scale(0); }
  25% { transform: scale(1.2); }
  50% { transform: scale(1); filter: brightness(1.5); }
}

@keyframes keyframes-svg-celebrate {
  0% { transform: translate(-50%, -50%) scale(0); }
  50% { opacity: 1; filter: brightness(1.5); }
  100% { transform: translate(-50%, -50%) scale(1.4); opacity: 0; }
}

/* 书签收藏按钮 */
.ui-bookmark {
  --icon-size: 24px;
  --icon-secondary-color: var(--text-muted);
  --icon-hover-color: var(--text-secondary);
  --icon-primary-color: #f59e0b;
  --icon-circle-border: 1px solid var(--icon-primary-color);
  --icon-circle-size: 35px;
  --icon-anmt-duration: 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  
  .bookmark {
    width: var(--icon-size);
    height: auto;
    fill: var(--icon-secondary-color);
    cursor: pointer;
    transition: 0.2s;
    display: flex;
    justify-content: center;
    align-items: center;
    position: relative;
    transform-origin: top;
    
    svg {
      width: var(--icon-size);
      height: var(--icon-size);
    }
    
    &::after {
      content: "";
      position: absolute;
      width: 10px;
      height: 10px;
      box-shadow: 
        0 30px 0 -4px var(--icon-primary-color),
        30px 0 0 -4px var(--icon-primary-color),
        0 -30px 0 -4px var(--icon-primary-color),
        -30px 0 0 -4px var(--icon-primary-color),
        -22px 22px 0 -4px var(--icon-primary-color),
        -22px -22px 0 -4px var(--icon-primary-color),
        22px -22px 0 -4px var(--icon-primary-color),
        22px 22px 0 -4px var(--icon-primary-color);
      border-radius: 50%;
      transform: scale(0);
    }
    
    &::before {
      content: "";
      position: absolute;
      border-radius: 50%;
      border: var(--icon-circle-border);
      opacity: 0;
    }
  }
  
  &.checked .bookmark {
    fill: var(--icon-primary-color);
    animation: bookmark var(--icon-anmt-duration) forwards;
    
    &::after {
      animation: circles var(--icon-anmt-duration) cubic-bezier(0.175, 0.885, 0.32, 1.275) forwards;
      animation-delay: var(--icon-anmt-duration);
    }
    
    &::before {
      animation: circle var(--icon-anmt-duration) cubic-bezier(0.175, 0.885, 0.32, 1.275) forwards;
      animation-delay: var(--icon-anmt-duration);
    }
  }
}

.action-btn-wrapper:hover .ui-bookmark .bookmark {
  fill: var(--icon-hover-color);
}

.action-btn-wrapper:hover .ui-bookmark.checked .bookmark {
  fill: var(--icon-primary-color);
}

@keyframes bookmark {
  50% { transform: scaleY(0.6); }
  100% { transform: scaleY(1); }
}

@keyframes circle {
  from { width: 0; height: 0; opacity: 0; }
  90% { width: var(--icon-circle-size); height: var(--icon-circle-size); opacity: 1; }
  to { opacity: 0; }
}

@keyframes circles {
  from { transform: scale(0); }
  40% { opacity: 1; }
  to { transform: scale(0.8); opacity: 0; }
}

.comment-section {
  padding: 32px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 28px;
  .el-icon { color: var(--primary-color); }
  .comment-count {
    font-size: 16px;
    color: var(--text-muted);
    font-weight: normal;
  }
}

.comment-form {
  display: flex;
  gap: 16px;
  margin-bottom: 32px;
  padding-bottom: 28px;
  border-bottom: 1px solid var(--border-color);
}

.form-wrapper {
  flex: 1;
  :deep(.el-textarea__inner) {
    background: var(--bg-input);
    border: 1px solid var(--border-color);
    color: var(--text-primary);
    &::placeholder { color: var(--text-disabled); }
    &:focus { border-color: var(--primary-color); }
  }
}

.form-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.submit-comment-btn {
  background: var(--primary-color);
  color: white;
  font-family: inherit;
  padding: 0.35em;
  padding-left: 1.2em;
  font-size: 15px;
  font-weight: 500;
  border-radius: 0.9em;
  border: none;
  letter-spacing: 0.05em;
  display: flex;
  align-items: center;
  box-shadow: inset 0 0 1.6em -0.6em var(--primary-dark);
  overflow: hidden;
  position: relative;
  height: 2.6em;
  padding-right: 3.3em;
  cursor: pointer;
  transition: all 0.3s;
  
  .icon {
    background: white;
    margin-left: 1em;
    position: absolute;
    display: flex;
    align-items: center;
    justify-content: center;
    height: 2em;
    width: 2em;
    border-radius: 0.7em;
    box-shadow: 0.1em 0.1em 0.6em 0.2em var(--primary-dark);
    right: 0.3em;
    transition: all 0.3s;
    
    svg {
      width: 1.1em;
      transition: transform 0.3s;
      color: var(--primary-color);
    }
  }
  
  &:hover:not(:disabled) {
    .icon {
      width: calc(100% - 0.6em);
    }
    
    .icon svg {
      transform: translateX(0.1em);
    }
  }
  
  &:active:not(:disabled) .icon {
    transform: scale(0.95);
  }
  
  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

.reply-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--primary-color);
  background: rgba(59, 130, 246, 0.1);
  padding: 6px 14px;
  border-radius: 20px;
  .close-icon {
    cursor: pointer;
    &:hover { color: #ef4444; }
  }
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.comment-item {
  display: flex;
  gap: 16px;
}

.comment-body {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.comment-author {
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary);
}

.comment-time {
  font-size: 13px;
  color: var(--text-muted);
}

.comment-content {
  color: var(--text-secondary);
  line-height: 1.7;
  margin-bottom: 10px;
  font-size: 14px;
}

.comment-actions {
  display: flex;
  gap: 20px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-muted);
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.3s;
  &:hover {
    color: var(--primary-color);
    background: rgba(59, 130, 246, 0.1);
  }
}

.reply-list {
  margin-top: 16px;
  padding: 16px;
  background: var(--bg-darker);
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.reply-item {
  display: flex;
  gap: 12px;
}

.reply-body {
  font-size: 13px;
  line-height: 1.6;
}

.reply-author {
  color: var(--primary-color);
  font-weight: 500;
  margin-right: 6px;
}

.reply-to {
  color: var(--text-muted);
  margin-right: 6px;
  em {
    color: var(--primary-color);
    font-style: normal;
  }
}

.reply-content {
  color: var(--text-secondary);
}

.no-comment {
  text-align: center;
  padding: 48px 0;
  color: var(--text-muted);
  .el-icon { margin-bottom: 16px; }
  p { font-size: 15px; }
}

@media (max-width: 768px) {
  .article-detail-page { padding: 12px; }
  .article-card, .comment-section { padding: 20px; }
  .article-title { font-size: 24px; }
  .article-info { flex-direction: column; align-items: flex-start; }
  .meta-info { flex-wrap: wrap; gap: 12px; }
  .action-buttons {
    flex-direction: column;
    .action-btn { justify-content: center; }
  }
}
</style>
