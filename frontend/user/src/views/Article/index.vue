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
              <!-- 已注销用户显示默认头像 -->
              <div class="author-avatar" :class="{ 'cancelled-avatar-wrapper': article.author.status === 2 }" @click="goToAuthorPage">
                <el-avatar v-if="article.author.status === 2" :size="36" class="cancelled-avatar">
                  <el-icon><UserFilled /></el-icon>
                </el-avatar>
                <el-avatar v-else :size="36" :src="article.author.avatar">
                  {{ article.author.nickname?.charAt(0) }}
                </el-avatar>
              </div>
              <!-- 已注销用户显示"已注销用户" -->
              <span v-if="article.author.status === 2" class="author-name cancelled-author" @click="goToAuthorPage">已注销用户</span>
              <span v-else class="author-name" @click="goToAuthorPage">{{ article.author.nickname }}</span>
              <!-- 作者已注销提示 -->
              <span v-if="article.author.status === 2" class="author-cancelled-tag">
                <el-icon><WarningFilled /></el-icon>
                该作者已注销
              </span>
              <!-- 作者被冻结提示 -->
              <span v-else-if="article.author.status === 0 || article.author.status === '0'" class="author-frozen-tag">
                <el-icon><WarningFilled /></el-icon>
                该作者因违反相关规定已被冻结
              </span>
              <!-- 关注按钮 - 作者未被冻结且未注销时显示 -->
              <button 
                v-else-if="userStore.isLoggedIn && userStore.userInfo?.id !== article.author.id && article.author.status !== 0"
                class="follow-btn"
                :class="{ followed: isFollowingAuthor }"
                @click="handleFollowAuthor"
                :disabled="followLoading"
              >
                <el-icon v-if="!followLoading"><Plus v-if="!isFollowingAuthor" /><Check v-else /></el-icon>
                <span>{{ isFollowingAuthor ? '已关注' : '关注' }}</span>
              </button>
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
          <MdPreview 
            v-if="article.content" 
            :modelValue="article.content" 
            :theme="editorTheme"
            :showCodeRowNumber="true"
            previewTheme="github"
          />
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

            <!-- VIP加热按钮 -->
            <div class="action-btn-wrapper vip-btn" @click="handleHeat" :class="{ disabled: heatingArticle }">
              <el-icon class="heat-icon"><Sunny /></el-icon>
              <span class="action-text">加热</span>
              <span v-if="vipInfo.isVip" class="vip-tag">VIP</span>
            </div>

            <!-- VIP下载按钮 -->
            <div class="action-btn-wrapper vip-btn" @click="handleDownload">
              <el-icon class="download-icon"><Download /></el-icon>
              <span class="action-text">下载</span>
              <span v-if="vipInfo.isVip" class="vip-tag">VIP</span>
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
          <div v-for="comment in comments" :key="comment.id" class="comment-card">
            <!-- 主评论 -->
            <div class="comment-main">
              <div class="comment-avatar clickable" :class="{ 
                'vip-avatar': comment.user?.vipLevel > 0 && comment.user?.status !== 2, 
                ['vip-level-' + comment.user?.vipLevel]: comment.user?.vipLevel > 0 && comment.user?.status !== 2,
                'cancelled-user-avatar': comment.user?.status === 2
              }" @click="goUserProfile(comment.user?.id)">
                <el-avatar v-if="comment.user?.status === 2" :size="42" class="cancelled-avatar">
                  <el-icon><UserFilled /></el-icon>
                </el-avatar>
                <el-avatar v-else :size="42" :src="comment.user?.avatar">
                  {{ comment.user?.nickname?.charAt(0) || 'U' }}
                </el-avatar>
              </div>
              <div class="comment-content-wrapper">
                <div class="comment-meta">
                  <template v-if="comment.user?.status === 2">
                    <span class="cancelled-username">已注销用户</span>
                  </template>
                  <VipUsername 
                    v-else
                    :username="comment.user?.nickname || '匿名用户'" 
                    :vip-level="comment.user?.vipLevel || 0"
                    class="clickable-name"
                    @click="goUserProfile(comment.user?.id)"
                  />
                  <span class="comment-time">{{ formatRelativeTime(comment.createTime) }}</span>
                </div>
                
                <!-- 编辑模式 -->
                <div v-if="editingCommentId === comment.id" class="comment-edit-form">
                  <el-input
                    v-model="editingContent"
                    type="textarea"
                    :rows="3"
                    placeholder="编辑评论内容..."
                    maxlength="500"
                    show-word-limit
                  />
                  <div class="edit-actions">
                    <el-button size="small" @click="cancelEdit">取消</el-button>
                    <el-button size="small" type="primary" @click="submitEdit(comment)" :loading="editSubmitting">保存</el-button>
                  </div>
                </div>
                
                <!-- 正常显示 -->
                <template v-else>
                  <p class="comment-text">{{ comment.content }}</p>
                  <div class="comment-actions">
                    <span class="action-btn" @click="handleLikeComment(comment)">
                      <el-icon><Star /></el-icon>
                      <em>{{ comment.likeCount || 0 }}</em>
                    </span>
                    <span class="action-btn" @click="handleReply(comment)">
                      <el-icon><ChatDotRound /></el-icon>
                      <em>回复</em>
                    </span>
                    <template v-if="userStore.userInfo?.id === comment.user?.id">
                      <span class="action-btn edit" @click="startEdit(comment)">
                        <el-icon><Edit /></el-icon>
                        <em>编辑</em>
                      </span>
                      <span class="action-btn delete" @click="handleDeleteComment(comment)">
                        <el-icon><Delete /></el-icon>
                        <em>删除</em>
                      </span>
                    </template>
                  </div>
                </template>
              </div>
            </div>
            
            <!-- 子评论区域 -->
            <div v-if="comment.replies && comment.replies.length" class="replies-container">
              <div class="replies-list">
                <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
                  <div class="reply-avatar clickable" :class="{ 
                    'vip-avatar': reply.user?.vipLevel > 0 && reply.user?.status !== 2, 
                    ['vip-level-' + reply.user?.vipLevel]: reply.user?.vipLevel > 0 && reply.user?.status !== 2,
                    'cancelled-user-avatar': reply.user?.status === 2
                  }" @click="goUserProfile(reply.user?.id)">
                    <el-avatar v-if="reply.user?.status === 2" :size="32" class="cancelled-avatar">
                      <el-icon><UserFilled /></el-icon>
                    </el-avatar>
                    <el-avatar v-else :size="32" :src="reply.user?.avatar">
                      {{ reply.user?.nickname?.charAt(0) || 'U' }}
                    </el-avatar>
                  </div>
                  <div class="reply-content-wrapper">
                    <div class="reply-meta">
                      <template v-if="reply.user?.status === 2">
                        <span class="cancelled-username">已注销用户</span>
                      </template>
                      <VipUsername 
                        v-else
                        :username="reply.user?.nickname || '匿名用户'" 
                        :vip-level="reply.user?.vipLevel || 0" 
                        size="small"
                        class="clickable-name"
                        @click="goUserProfile(reply.user?.id)"
                      />
                      <span v-if="reply.replyUser" class="reply-target">
                        回复 <em class="clickable-name" @click="goUserProfile(reply.replyUser?.id)">@{{ reply.replyUser.status === 2 ? '已注销用户' : reply.replyUser.nickname }}</em>
                      </span>
                      <span class="reply-time">{{ formatRelativeTime(reply.createTime) }}</span>
                    </div>
                    
                    <!-- 子评论编辑模式 -->
                    <div v-if="editingCommentId === reply.id" class="comment-edit-form">
                      <el-input
                        v-model="editingContent"
                        type="textarea"
                        :rows="2"
                        placeholder="编辑回复内容..."
                        maxlength="500"
                        show-word-limit
                      />
                      <div class="edit-actions">
                        <el-button size="small" @click="cancelEdit">取消</el-button>
                        <el-button size="small" type="primary" @click="submitEdit(reply)" :loading="editSubmitting">保存</el-button>
                      </div>
                    </div>
                    
                    <!-- 子评论正常显示 -->
                    <template v-else>
                      <p class="reply-text">{{ reply.content }}</p>
                      <div class="reply-actions">
                        <span class="action-btn" @click="handleReply(reply)">
                          <el-icon><ChatDotRound /></el-icon>
                          <em>回复</em>
                        </span>
                        <template v-if="userStore.userInfo?.id === reply.user?.id">
                          <span class="action-btn edit" @click="startEdit(reply)">
                            <el-icon><Edit /></el-icon>
                            <em>编辑</em>
                          </span>
                          <span class="action-btn delete" @click="handleDeleteComment(reply, comment)">
                            <el-icon><Delete /></el-icon>
                            <em>删除</em>
                          </span>
                        </template>
                      </div>
                    </template>
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
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Calendar, View, ChatDotRound, Star, Close, PriceTag, Document, ArrowLeft, Download, Sunny, Edit, Delete, Plus, Check, WarningFilled, UserFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getArticleDetail, likeArticle, unlikeArticle, favoriteArticle, unfavoriteArticle } from '@/api/article'
import { getCommentList, createComment, likeComment, deleteComment, updateComment } from '@/api/comment'
import { getVipInfo, heatArticle, downloadArticle } from '@/api/vip'
import { followUser, unfollowUser, getUserProfile } from '@/api/follow'
import { formatDate, formatRelativeTime } from '@/utils/format'
import { useUserStore } from '@/stores/user'
import { viewArticle, leaveArticle, onNewComment } from '@/utils/websocket'
import Loading from '@/components/Loading/index.vue'
import VipBadge from '@/components/VipBadge/index.vue'
import VipUsername from '@/components/VipUsername/index.vue'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/preview.css'
import { useThemeStore } from '@/stores/theme'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()

const article = ref(null)
const comments = ref([])
const totalComments = ref(0)
const loading = ref(true)
const submitting = ref(false)
const commentContent = ref('')
const replyTo = ref(null)
const vipInfo = ref({ isVip: false })
const heatingArticle = ref(false)

// 编辑器主题
const editorTheme = computed(() => themeStore.isDark ? 'dark' : 'light')

// 关注作者相关
const isFollowingAuthor = ref(false)
const followLoading = ref(false)

// 跳转到作者主页
function goToAuthorPage() {
  if (article.value?.author?.id) {
    router.push(`/user/${article.value.author.id}`)
  }
}

// 检查是否已关注作者
async function checkFollowStatus() {
  if (!userStore.isLoggedIn || !article.value?.author?.id) return
  if (userStore.userInfo?.id === article.value.author.id) return
  
  try {
    const res = await getUserProfile(article.value.author.id)
    if (res.code === 200 && res.data) {
      // 后端返回的字段是 isFollowed
      isFollowingAuthor.value = res.data.isFollowed || false
    }
  } catch (e) {
    console.error('检查关注状态失败', e)
  }
}

// 关注/取消关注作者
async function handleFollowAuthor() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  
  const authorId = article.value?.author?.id
  if (!authorId) return
  
  followLoading.value = true
  try {
    if (isFollowingAuthor.value) {
      await unfollowUser(authorId)
      isFollowingAuthor.value = false
      ElMessage.success('已取消关注')
    } else {
      await followUser(authorId)
      isFollowingAuthor.value = true
      ElMessage.success('关注成功')
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  } finally {
    followLoading.value = false
  }
}

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

// 跳转到用户详情页
function goUserProfile(userId) {
  if (userId) {
    router.push(`/user/${userId}`)
  }
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
  // 获取VIP信息
  fetchVipInfo()
  // 检查是否已关注作者
  checkFollowStatus()
}

// 获取VIP信息
async function fetchVipInfo() {
  if (!userStore.isLoggedIn) return
  try {
    const res = await getVipInfo()
    if (res.code === 200) {
      vipInfo.value = res.data
    }
  } catch (e) {
    console.error(e)
  }
}

// 文章加热
async function handleHeat() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  if (!vipInfo.value.isVip) {
    ElMessage.warning('VIP会员专属功能')
    return
  }
  heatingArticle.value = true
  try {
    const res = await heatArticle(article.value.id)
    if (res.code === 200) {
      ElMessage.success('加热成功！')
      article.value.heatCount = (article.value.heatCount || 0) + getHeatValue(vipInfo.value.level)
      vipInfo.value.heatCountToday++
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '加热失败')
  } finally {
    heatingArticle.value = false
  }
}

// 下载文章
async function handleDownload() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  if (!vipInfo.value.isVip) {
    ElMessage.warning('VIP会员专属功能')
    return
  }
  try {
    const res = await downloadArticle(article.value.id)
    const blob = new Blob([res], { type: 'text/markdown' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${article.value.title}.md`
    a.click()
    window.URL.revokeObjectURL(url)
    vipInfo.value.downloadCountToday++
    ElMessage.success('下载成功')
  } catch (e) {
    console.error('下载失败:', e)
    ElMessage.error(e.message || '下载失败')
  }
}

function getHeatValue(level) {
  const values = { 1: 10, 2: 30, 3: 100 }
  return values[level] || 10
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
      const res = await unlikeArticle(article.value.id)
      article.value.isLiked = false
      // 使用后端返回的实际数量
      if (res.data && typeof res.data.likeCount === 'number') {
        article.value.likeCount = res.data.likeCount
      } else {
        article.value.likeCount = Math.max(0, (article.value.likeCount || 1) - 1)
      }
    } else {
      const res = await likeArticle(article.value.id)
      article.value.isLiked = true
      // 使用后端返回的实际数量
      if (res.data && typeof res.data.likeCount === 'number') {
        article.value.likeCount = res.data.likeCount
      } else {
        article.value.likeCount = (article.value.likeCount || 0) + 1
      }
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
      const res = await unfavoriteArticle(article.value.id)
      article.value.isFavorited = false
      // 使用后端返回的实际数量
      if (res.data && typeof res.data.favoriteCount === 'number') {
        article.value.favoriteCount = res.data.favoriteCount
      } else {
        article.value.favoriteCount = Math.max(0, (article.value.favoriteCount || 1) - 1)
      }
    } else {
      const res = await favoriteArticle(article.value.id)
      article.value.isFavorited = true
      // 使用后端返回的实际数量
      if (res.data && typeof res.data.favoriteCount === 'number') {
        article.value.favoriteCount = res.data.favoriteCount
      } else {
        article.value.favoriteCount = (article.value.favoriteCount || 0) + 1
      }
    }
  } catch (e) {
    console.error(e)
  }
}

// 发表评论
async function submitComment() {
  if (!commentContent.value.trim()) return
  submitting.value = true
  
  const content = commentContent.value.trim()
  const parentId = replyTo.value ? (replyTo.value.parentId || replyTo.value.id) : null
  const replyUserId = replyTo.value?.user?.id
  const replyUserInfo = replyTo.value?.user ? {
    id: replyTo.value.user.id,
    nickname: replyTo.value.user.nickname
  } : null
  
  try {
    const data = { 
      articleId: Number(route.params.id), 
      content: content
    }
    if (parentId) {
      data.parentId = parentId
      data.replyUserId = replyUserId
    }
    
    // 先清空输入框，提升用户体验
    commentContent.value = ''
    const savedReplyTo = replyTo.value
    replyTo.value = null
    
    const res = await createComment(data)
    
    // 立即添加到列表（乐观更新）
    const newComment = {
      id: res.data?.id || Date.now(), // 使用返回的ID或临时ID
      content: content,
      parentId: parentId,
      replyUserId: replyUserId,
      createTime: new Date().toISOString(),
      likeCount: 0,
      user: {
        id: userStore.userInfo?.id,
        nickname: userStore.userInfo?.nickname,
        avatar: userStore.userInfo?.avatar,
        vipLevel: userStore.userInfo?.vipLevel || 0
      },
      replyUser: replyUserInfo,
      replies: []
    }
    
    if (parentId) {
      // 子评论 - 找到根评论并添加
      const rootComment = comments.value.find(c => c.id === parentId)
      if (rootComment) {
        if (!rootComment.replies) rootComment.replies = []
        rootComment.replies.push(newComment)
      }
    } else {
      // 顶级评论 - 添加到列表开头
      comments.value.unshift(newComment)
    }
    
    totalComments.value++
    if (article.value) {
      article.value.commentCount = (article.value.commentCount || 0) + 1
    }
    
    ElMessage.success('评论成功')
  } catch (e) {
    console.error(e)
    ElMessage.error('评论失败')
    // 恢复输入内容
    commentContent.value = content
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

// ========== 评论编辑删除功能 ==========
const editingCommentId = ref(null)
const editingContent = ref('')
const editSubmitting = ref(false)

// 开始编辑评论
function startEdit(comment) {
  editingCommentId.value = comment.id
  editingContent.value = comment.content
}

// 取消编辑
function cancelEdit() {
  editingCommentId.value = null
  editingContent.value = ''
}

// 提交编辑
async function submitEdit(comment) {
  if (!editingContent.value.trim()) {
    ElMessage.warning('评论内容不能为空')
    return
  }
  
  editSubmitting.value = true
  try {
    await updateComment(comment.id, editingContent.value.trim())
    comment.content = editingContent.value.trim()
    ElMessage.success('修改成功')
    cancelEdit()
  } catch (e) {
    console.error(e)
    ElMessage.error('修改失败')
  } finally {
    editSubmitting.value = false
  }
}

// 删除评论
async function handleDeleteComment(comment, parentComment = null) {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteComment(comment.id)
    ElMessage.success('删除成功')
    
    // 如果是子评论，从父评论的replies中移除
    if (parentComment && parentComment.replies) {
      const index = parentComment.replies.findIndex(r => r.id === comment.id)
      if (index > -1) {
        parentComment.replies.splice(index, 1)
      }
    } else {
      // 如果是主评论，从评论列表中移除
      const index = comments.value.findIndex(c => c.id === comment.id)
      if (index > -1) {
        comments.value.splice(index, 1)
      }
    }
    
    // 更新评论数
    if (article.value && article.value.commentCount > 0) {
      article.value.commentCount--
    }
    totalComments.value = Math.max(0, totalComments.value - 1)
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
      ElMessage.error('删除失败')
    }
  }
}

// 监听路由变化
watch(() => route.params.id, (newId) => {
  if (newId) {
    fetchArticle()
  }
})

// 存储取消订阅函数
let unsubscribeNewComment = null

onMounted(() => {
  fetchArticle()
  
  // 通知服务器正在浏览此文章
  viewArticle(route.params.id)
  
  // 监听新评论（实时刷新）
  unsubscribeNewComment = onNewComment((newComment) => {
    if (String(newComment.articleId) === String(route.params.id)) {
      // 收到新评论，添加到列表
      addNewCommentToList(newComment)
    }
  })
})

onUnmounted(() => {
  // 通知服务器离开文章页面
  leaveArticle()
  
  // 取消订阅
  if (unsubscribeNewComment) {
    unsubscribeNewComment()
    unsubscribeNewComment = null
  }
})

// 添加新评论到列表（优化版本）
function addNewCommentToList(newComment) {
  // 如果是自己发的评论，忽略（已经在submitComment中处理）
  if (newComment.user?.id === userStore.userInfo?.id) {
    return
  }
  
  // 使用nextTick确保DOM更新
  nextTick(() => {
    if (newComment.parentId && newComment.parentId > 0) {
      // 子评论 - 找到根评论并添加
      // 先在顶级评论中查找
      let rootComment = comments.value.find(c => c.id === newComment.parentId)
      
      // 如果没找到，可能parentId指向的是另一个子评论，需要找到根评论
      if (!rootComment) {
        for (const comment of comments.value) {
          if (comment.replies?.some(r => r.id === newComment.parentId)) {
            rootComment = comment
            break
          }
        }
      }
      
      if (rootComment) {
        if (!rootComment.replies) {
          rootComment.replies = []
        }
        // 检查是否已存在
        if (!rootComment.replies.find(r => r.id === newComment.id)) {
          rootComment.replies.push(newComment)
        }
      }
    } else {
      // 顶级评论 - 添加到列表开头
      if (!comments.value.find(c => c.id === newComment.id)) {
        comments.value.unshift(newComment)
        totalComments.value++
      }
    }
    
    // 更新文章评论数
    if (article.value) {
      article.value.commentCount = (article.value.commentCount || 0) + 1
    }
  })
}
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
  flex-wrap: wrap;
  
  .author-avatar {
    cursor: pointer;
    transition: transform 0.2s;
    
    &:hover {
      transform: scale(1.1);
    }
  }
  
  .author-name {
    font-size: 15px;
    color: var(--primary-color);
    font-weight: 500;
    cursor: pointer;
    transition: color 0.2s;
    
    &:hover {
      color: var(--primary-dark);
      text-decoration: underline;
    }
  }
  
  .author-frozen-tag {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 4px 10px;
    background: rgba(239, 68, 68, 0.1);
    border: 1px solid rgba(239, 68, 68, 0.3);
    border-radius: 4px;
    color: #ef4444;
    font-size: 12px;
    font-weight: 500;
    
    .el-icon {
      font-size: 14px;
    }
  }
  
  .author-cancelled-tag {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 4px 10px;
    background: rgba(107, 114, 128, 0.1);
    border: 1px solid rgba(107, 114, 128, 0.3);
    border-radius: 4px;
    color: #6b7280;
    font-size: 12px;
    font-weight: 500;
    
    .el-icon {
      font-size: 14px;
    }
  }
  
  .cancelled-avatar-wrapper {
    .cancelled-avatar {
      background: #4b5563;
      color: #9ca3af;
    }
  }
  
  .cancelled-author {
    color: #6b7280 !important;
    
    &:hover {
      color: #6b7280 !important;
      text-decoration: none !important;
    }
  }
  
  .follow-btn {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 6px 14px;
    border: 1px solid var(--primary-color);
    border-radius: 20px;
    background: transparent;
    color: var(--primary-color);
    font-size: 13px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
    
    .el-icon {
      font-size: 14px;
    }
    
    &:hover:not(:disabled) {
      background: var(--primary-color);
      color: #fff;
    }
    
    &.followed {
      background: var(--bg-card-hover);
      border-color: var(--border-color);
      color: var(--text-muted);
      
      &:hover:not(:disabled) {
        border-color: #ef4444;
        color: #ef4444;
        background: rgba(239, 68, 68, 0.1);
      }
    }
    
    &:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
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
  min-height: 200px;
  
  // md-editor-v3 预览组件样式覆盖
  :deep(.md-editor-preview-wrapper) {
    padding: 0;
    background: var(--bg-card) !important;
  }
  
  :deep(.md-editor-preview) {
    color: var(--text-secondary);
    font-size: 16px;
    line-height: 1.9;
    background: var(--bg-card) !important;
    
    // 标题样式
    h1, h2, h3, h4, h5, h6 {
      color: var(--text-primary);
      margin: 28px 0 16px;
      font-weight: 600;
    }
    
    h1 { font-size: 26px; }
    h2 { 
      font-size: 22px;
      padding-bottom: 10px;
      border-bottom: 1px solid var(--border-color);
    }
    h3 { font-size: 18px; }
    
    // 段落
    p {
      margin-bottom: 16px;
      color: var(--text-secondary);
    }
    
    // 代码块样式
    pre {
      border-radius: 10px;
      margin: 16px 0;
      border: 1px solid var(--border-color);
      overflow: hidden;
      
      code {
        background: transparent !important;
        font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', 'Monaco', monospace;
        font-size: 14px;
        line-height: 1.6;
        white-space: pre;
        overflow-x: auto;
      }
    }
  }
}

// 暗色主题代码块背景
:root[data-theme="dark"] {
  .article-body {
    :deep(.md-editor-preview) {
      pre {
        background: #1e1e1e !important;
      }
    }
  }
}

// 亮色主题代码块背景
:root[data-theme="light"] {
  .article-body {
    :deep(.md-editor-preview) {
      pre {
        background: #f4f4f5 !important;
      }
    }
    
    // 行内代码
    code:not(pre code) {
      background: rgba(59, 130, 246, 0.15) !important;
      color: var(--primary-color) !important;
      padding: 2px 8px;
      border-radius: 4px;
      font-family: 'JetBrains Mono', 'Fira Code', monospace;
      font-size: 13px;
    }
    
    // 链接
    a {
      color: var(--primary-color);
      text-decoration: underline;
      
      &:hover {
        opacity: 0.8;
      }
    }
    
    // 粗体
    strong {
      color: var(--text-primary);
      font-weight: 600;
    }
    
    // 图片
    img {
      max-width: 100%;
      height: auto;
      border-radius: 8px;
      margin: 16px 0;
      display: block;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }
    
    // 表格
    table {
      width: 100%;
      border-collapse: collapse;
      font-size: 14px;
      margin: 20px 0;
      border-radius: 8px;
      overflow: hidden;
      border: 1px solid var(--border-color);
      
      th, td {
        padding: 12px 16px;
        border: 1px solid var(--border-color);
        word-break: break-word;
      }
      
      th {
        background: var(--bg-card-hover);
        color: var(--text-primary);
        font-weight: 600;
      }
      
      td {
        background: transparent;
        color: var(--text-secondary);
      }
      
      tbody tr:hover td {
        background: rgba(59, 130, 246, 0.05);
      }
      
      tbody tr:nth-child(even) td {
        background: var(--bg-card-hover);
      }
      
      tbody tr:nth-child(even):hover td {
        background: rgba(59, 130, 246, 0.08);
      }
    }
    
    // 列表
    ul, ol {
      margin: 16px 0;
      padding-left: 24px;
      color: var(--text-secondary);
      
      li {
        margin: 8px 0;
        line-height: 1.7;
      }
    }
    
    // 引用
    blockquote {
      margin: 16px 0;
      padding: 12px 20px;
      border-left: 4px solid var(--primary-color);
      background: rgba(59, 130, 246, 0.1);
      border-radius: 4px;
      color: var(--text-secondary);
      
      p {
        margin: 0;
      }
    }
    
    // 分隔线
    hr {
      border: none;
      border-top: 1px solid var(--border-color);
      margin: 24px 0;
    }
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

/* VIP按钮样式 */
.action-btn-wrapper.vip-btn {
  position: relative;
  
  .heat-icon {
    font-size: 20px;
    color: #f97316;
  }
  
  .download-icon {
    font-size: 20px;
    color: #3b82f6;
  }
  
  .vip-tag {
    position: absolute;
    top: -6px;
    right: -6px;
    background: linear-gradient(135deg, #ffd700, #ffb700);
    color: #333;
    font-size: 10px;
    font-weight: 600;
    padding: 2px 6px;
    border-radius: 8px;
  }
  
  &.disabled {
    opacity: 0.6;
    pointer-events: none;
  }
  
  &:hover .heat-icon {
    color: #ea580c;
  }
  
  &:hover .download-icon {
    color: #2563eb;
  }
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
  gap: 16px;
}

/* 评论卡片 - 整体容器 */
.comment-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 20px;
  transition: all 0.3s ease;
  
  &:hover {
    border-color: rgba(168, 85, 247, 0.2);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  }
}

:root[data-theme="dark"] .comment-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

/* 主评论区域 */
.comment-main {
  display: flex;
  gap: 14px;
}

/* 头像样式 */
.comment-avatar,
.reply-avatar {
  flex-shrink: 0;
  border-radius: 50%;
}

.comment-avatar {
  width: 42px;
  height: 42px;
}

.reply-avatar {
  width: 32px;
  height: 32px;
}

/* VIP头像边框 */
.comment-avatar.vip-avatar,
.reply-avatar.vip-avatar {
  padding: 2px;
  box-sizing: content-box;
  
  :deep(.el-avatar) {
    width: 100% !important;
    height: 100% !important;
  }
}

.comment-avatar.vip-level-1,
.reply-avatar.vip-level-1 {
  background: linear-gradient(135deg, #cd7f32, #daa520);
}

.comment-avatar.vip-level-2,
.reply-avatar.vip-level-2 {
  background: linear-gradient(135deg, #a8a8a8, #e0e0e0);
}

.comment-avatar.vip-level-3,
.reply-avatar.vip-level-3 {
  background: linear-gradient(135deg, #ffd700, #ffb700);
  box-shadow: 0 0 8px rgba(255, 215, 0, 0.4);
}

/* 已注销用户头像样式 */
.comment-avatar.cancelled-user-avatar,
.reply-avatar.cancelled-user-avatar {
  .cancelled-avatar {
    background: #4b5563;
    color: #9ca3af;
  }
}

/* 已注销用户名样式 */
.cancelled-username {
  color: #6b7280;
  font-size: 14px;
}

/* 评论内容区域 */
.comment-content-wrapper,
.reply-content-wrapper {
  flex: 1;
  min-width: 0;
}

/* 评论元信息 */
.comment-meta,
.reply-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.comment-time,
.reply-time {
  font-size: 12px;
  color: var(--text-muted);
  margin-left: auto;
}

.reply-target {
  font-size: 12px;
  color: var(--text-muted);
  
  em {
    color: var(--primary-color);
    font-style: normal;
  }
}

/* 评论文本 */
.comment-text,
.reply-text {
  color: var(--text-secondary);
  line-height: 1.7;
  font-size: 14px;
  margin: 0 0 10px 0;
  word-break: break-word;
}

.reply-text {
  font-size: 13px;
}

/* 操作按钮 */
.comment-actions,
.reply-actions {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-muted);
  cursor: pointer;
  padding: 4px 10px;
  border-radius: 6px;
  transition: all 0.2s ease;
  background: transparent;
  
  .el-icon {
    font-size: 14px;
  }
  
  em {
    font-style: normal;
  }
  
  &:hover {
    color: var(--primary-color);
    background: rgba(168, 85, 247, 0.1);
  }
  
  &.edit:hover {
    color: #10b981;
    background: rgba(16, 185, 129, 0.1);
  }
  
  &.delete:hover {
    color: #ef4444;
    background: rgba(239, 68, 68, 0.1);
  }
}

:root[data-theme="light"] .action-btn {
  &:hover {
    background: rgba(168, 85, 247, 0.08);
  }
  
  &.edit:hover {
    background: rgba(16, 185, 129, 0.08);
  }
  
  &.delete:hover {
    background: rgba(239, 68, 68, 0.08);
  }
}

/* 子评论容器 */
.replies-container {
  margin-top: 16px;
  margin-left: 56px;
}

/* 子评论列表 */
.replies-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: var(--bg-darker);
  border-radius: 10px;
  padding: 14px;
  border: 1px solid var(--border-color);
}

:root[data-theme="light"] .replies-list {
  background: #f8f9fa;
  border-color: #e9ecef;
}

/* 子评论项 */
.reply-item {
  display: flex;
  gap: 10px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-color);
  
  &:last-child {
    padding-bottom: 0;
    border-bottom: none;
  }
}

/* 评论编辑表单 */
.comment-edit-form {
  margin-top: 10px;
  
  :deep(.el-textarea__inner) {
    background: var(--bg-input);
    border: 1px solid var(--border-color);
    color: var(--text-primary);
    border-radius: 8px;
    font-size: 14px;
    
    &::placeholder {
      color: var(--text-disabled);
    }
    
    &:focus {
      border-color: var(--primary-color);
      box-shadow: 0 0 0 2px rgba(168, 85, 247, 0.1);
    }
  }
}

.edit-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 10px;
}

/* 无评论 */
.no-comment {
  text-align: center;
  padding: 48px 0;
  color: var(--text-muted);
  
  .el-icon {
    margin-bottom: 16px;
    opacity: 0.5;
  }
  
  p {
    font-size: 15px;
  }
}

/* 响应式 */
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
  
  .comment-card {
    padding: 14px;
  }
  
  .comment-main {
    gap: 10px;
  }
  
  .comment-avatar {
    width: 36px;
    height: 36px;
  }
  
  .replies-container {
    margin-left: 10px;
    padding-left: 14px;
  }
  
  .replies-list {
    padding: 10px;
  }
  
  .reply-avatar {
    width: 28px;
    height: 28px;
  }
  
  .comment-time,
  .reply-time {
    margin-left: 0;
    width: 100%;
    margin-top: 4px;
  }
}

/* 可点击样式 */
.clickable {
  cursor: pointer;
  transition: transform 0.2s, opacity 0.2s;
  
  &:hover {
    transform: scale(1.05);
    opacity: 0.9;
  }
}

.clickable-name {
  cursor: pointer;
  
  &:hover {
    opacity: 0.8;
  }
}
</style>
