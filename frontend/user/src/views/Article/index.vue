<template>
  <div class="article-detail-page">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>

    <!-- 文章内容 -->
    <template v-else-if="article">
      <article class="article-card glass-card">
        <!-- 文章头部 -->
        <header class="article-header">
          <h1 class="article-title">{{ article.title }}</h1>
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
            <el-button 
              :class="['action-btn', { active: article.isLiked }]"
              @click="handleLike"
            >
              <el-icon><Star /></el-icon>
              <span>{{ article.isLiked ? '已点赞' : '点赞' }}</span>
              <em>{{ article.likeCount || 0 }}</em>
            </el-button>
            <el-button 
              :class="['action-btn', { active: article.isFavorited }]"
              @click="handleFavorite"
            >
              <el-icon><Collection /></el-icon>
              <span>{{ article.isFavorited ? '已收藏' : '收藏' }}</span>
              <em>{{ article.favoriteCount || 0 }}</em>
            </el-button>
          </div>
        </footer>
      </article>

      <!-- 评论区 -->
      <section class="comment-section glass-card">
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
              <el-button 
                type="primary" 
                :disabled="!userStore.isLoggedIn || !commentContent.trim()" 
                :loading="submitting"
                @click="submitComment"
              >
                发表评论
              </el-button>
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
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Calendar, View, ChatDotRound, Star, Collection, Close, PriceTag, Document } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getArticleDetail, likeArticle, unlikeArticle, favoriteArticle, unfavoriteArticle } from '@/api/article'
import { getCommentList, createComment, likeComment } from '@/api/comment'
import { formatDate, formatRelativeTime } from '@/utils/format'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()

const article = ref(null)
const comments = ref([])
const totalComments = ref(0)
const loading = ref(true)
const submitting = ref(false)
const commentContent = ref('')
const replyTo = ref(null)

// 计算属性：处理文章内容
const articleContent = computed(() => {
  if (!article.value) return ''
  
  const content = article.value.content
  const contentHtml = article.value.contentHtml
  
  // 优先使用 contentHtml
  if (contentHtml && contentHtml.trim()) {
    return contentHtml
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
  
  // 处理链接
  content = content.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank">$1</a>')
  
  // 处理段落和换行
  const paragraphs = content.split(/\n\n+/)
  content = paragraphs.map(p => {
    p = p.trim()
    if (!p) return ''
    if (p.startsWith('<h') || p.startsWith('<pre') || p.startsWith('<ul') || p.startsWith('<ol')) {
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

.article-title {
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.4;
  margin-bottom: 20px;
  transition: color 0.3s;
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
    background: var(--bg-darker);
    padding: 16px;
    border-radius: 8px;
    overflow-x: auto;
    margin: 16px 0;
    code {
      font-family: 'Fira Code', 'Consolas', monospace;
      font-size: 14px;
      color: var(--text-secondary);
      white-space: pre;
    }
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
}

.article-footer {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--border-color);
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 28px;
  background: var(--bg-card-hover);
  border: 1px solid var(--border-color);
  border-radius: 25px;
  color: var(--text-muted);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  em {
    font-style: normal;
    color: var(--text-disabled);
  }
  &:hover {
    background: rgba(59, 130, 246, 0.15);
    border-color: rgba(59, 130, 246, 0.3);
    color: var(--primary-color);
  }
  &.active {
    background: rgba(59, 130, 246, 0.2);
    border-color: var(--primary-color);
    color: var(--primary-color);
    em { color: var(--primary-color); }
  }
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
