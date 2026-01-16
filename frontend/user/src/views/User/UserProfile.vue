<template>
  <div class="user-profile-page">
    <!-- 返回按钮 -->
    <div class="back-nav">
      <button class="back-btn" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回</span>
      </button>
    </div>

    <Loading v-if="loading" />

    <template v-else-if="userProfile">
      <!-- 注销提示 -->
      <div v-if="userProfile.status === 2" class="cancelled-banner">
        <el-icon><WarningFilled /></el-icon>
        <span>该用户已注销</span>
      </div>
      
      <!-- 冻结提示 -->
      <div v-else-if="userProfile.status === 0" class="frozen-banner">
        <el-icon><WarningFilled /></el-icon>
        <span>该用户已被冻结</span>
      </div>

      <!-- 用户信息卡片 -->
      <div class="profile-card glass-card">
        <div class="profile-header">
          <div class="avatar-wrapper" :class="{ 
            'vip-avatar': userProfile.vipLevel > 0 && userProfile.status !== 2, 
            ['vip-level-' + userProfile.vipLevel]: userProfile.vipLevel > 0 && userProfile.status !== 2, 
            'frozen': userProfile.status === 0,
            'cancelled': userProfile.status === 2
          }">
            <!-- 已注销用户显示默认头像 -->
            <el-avatar v-if="userProfile.status === 2" :size="100" class="cancelled-avatar">
              <el-icon :size="40"><UserFilled /></el-icon>
            </el-avatar>
            <el-avatar v-else :size="100" :src="userProfile.avatar">
              {{ userProfile.nickname?.charAt(0) || 'U' }}
            </el-avatar>
          </div>
          <div class="profile-info">
            <div class="name-row">
              <template v-if="userProfile.status === 2">
                <span class="profile-name cancelled-name">已注销用户</span>
                <span class="cancelled-tag">已注销</span>
              </template>
              <template v-else>
                <VipUsername 
                  :username="userProfile.nickname || userProfile.username" 
                  :vip-level="userProfile.vipLevel || 0"
                  class="profile-name"
                />
                <span v-if="userProfile.status === 0" class="frozen-tag">已冻结</span>
              </template>
            </div>
            <p class="profile-intro">{{ userProfile.status === 2 ? '该用户已注销账号' : (userProfile.intro || '这个人很懒，什么都没写~') }}</p>
            <div class="profile-stats">
              <div class="stat-item clickable" @click="scrollToArticles">
                <span class="stat-value">{{ userProfile.articleCount || 0 }}</span>
                <span class="stat-label">文章</span>
              </div>
              <div class="stat-item" :class="{ clickable: userProfile.status !== 2 }" @click="userProfile.status !== 2 && goFollowers()">
                <span class="stat-value">{{ userProfile.fansCount || 0 }}</span>
                <span class="stat-label">粉丝</span>
              </div>
              <div class="stat-item" :class="{ clickable: userProfile.status !== 2 }" @click="userProfile.status !== 2 && goFollowing()">
                <span class="stat-value">{{ userProfile.followCount || 0 }}</span>
                <span class="stat-label">关注</span>
              </div>
            </div>
          </div>
          <!-- 已注销用户不显示关注和私信按钮 -->
          <div class="profile-actions" v-if="!isSelf && userProfile.status !== 2">
            <button 
              class="follow-btn" 
              :class="{ followed: userProfile.isFollowed, disabled: userProfile.status === 0 }"
              @click="handleFollow"
              :disabled="followLoading || userProfile.status === 0"
              :title="userProfile.status === 0 ? '该用户已被冻结' : ''"
            >
              <el-icon v-if="!followLoading">
                <component :is="userProfile.isFollowed ? 'Check' : 'Plus'" />
              </el-icon>
              <span>{{ userProfile.isFollowed ? '已关注' : '关注' }}</span>
            </button>
            <button 
              class="message-btn" 
              :class="{ disabled: userProfile.status === 0 }"
              @click="openChat"
              :disabled="userProfile.status === 0"
              :title="userProfile.status === 0 ? '该用户已被冻结' : ''"
            >
              <el-icon><ChatDotRound /></el-icon>
              <span>私信</span>
            </button>
          </div>
        </div>
      </div>

      <!-- 文章列表 -->
      <div class="articles-section glass-card">
        <div class="section-header">
          <h3>
            <el-icon><Document /></el-icon>
            TA的文章
          </h3>
          <div class="header-actions">
            <!-- 搜索框 -->
            <div class="search-box">
              <el-input
                v-model="searchKeyword"
                placeholder="搜索文章..."
                :prefix-icon="Search"
                clearable
                @keyup.enter="handleSearch"
                @clear="handleSearch"
              />
            </div>
            <!-- 排序 -->
            <el-select v-model="sortBy" @change="fetchArticles" class="sort-select">
              <el-option label="最新发布" value="latest" />
              <el-option label="最多阅读" value="view" />
              <el-option label="最多点赞" value="like" />
              <el-option label="最多评论" value="comment" />
            </el-select>
          </div>
        </div>

        <div class="article-list" v-if="articles.length">
          <ArticleCard
            v-for="article in articles"
            :key="article.id"
            :article="article"
          />
        </div>
        <el-empty v-else description="暂无文章" />

        <div class="pagination-wrapper" v-if="total > pageSize">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </template>

    <el-empty v-else description="用户不存在" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Plus, Check, ChatDotRound, Document, Search, WarningFilled, UserFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getUserProfile, getUserArticles, followUser, unfollowUser } from '@/api/follow'
import { useUserStore } from '@/stores/user'
import Loading from '@/components/Loading/index.vue'
import ArticleCard from '@/components/ArticleCard/index.vue'
import VipUsername from '@/components/VipUsername/index.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const userProfile = ref(null)
const articles = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const sortBy = ref('latest')
const searchKeyword = ref('')
const followLoading = ref(false)

const isSelf = computed(() => {
  return userStore.userInfo?.id === Number(route.params.userId)
})

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
  }
}

async function fetchUserProfile() {
  loading.value = true
  try {
    const res = await getUserProfile(route.params.userId)
    if (res.code === 200) {
      userProfile.value = res.data
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function fetchArticles() {
  try {
    const res = await getUserArticles(route.params.userId, {
      page: currentPage.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value || undefined,
      sortBy: sortBy.value
    })
    if (res.code === 200) {
      articles.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (e) {
    console.error(e)
  }
}

function handleSearch() {
  currentPage.value = 1
  fetchArticles()
}

function handlePageChange(page) {
  currentPage.value = page
  fetchArticles()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

async function handleFollow() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  followLoading.value = true
  try {
    if (userProfile.value.isFollowed) {
      await unfollowUser(route.params.userId)
      userProfile.value.isFollowed = false
      userProfile.value.fansCount = Math.max(0, (userProfile.value.fansCount || 1) - 1)
      ElMessage.success('已取消关注')
    } else {
      await followUser(route.params.userId)
      userProfile.value.isFollowed = true
      userProfile.value.fansCount = (userProfile.value.fansCount || 0) + 1
      ElMessage.success('关注成功')
    }
  } catch (e) {
    console.error(e)
  } finally {
    followLoading.value = false
  }
}

function openChat() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  router.push(`/message/${route.params.userId}`)
}

// 滚动到文章列表
function scrollToArticles() {
  const articlesSection = document.querySelector('.articles-section')
  if (articlesSection) {
    articlesSection.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

// 跳转到粉丝页面
function goFollowers() {
  router.push(`/user/${route.params.userId}/followers`)
}

// 跳转到关注页面
function goFollowing() {
  router.push(`/user/${route.params.userId}/following`)
}

watch(() => route.params.userId, () => {
  if (route.params.userId) {
    currentPage.value = 1
    searchKeyword.value = ''
    sortBy.value = 'latest'
    fetchUserProfile()
    fetchArticles()
  }
}, { immediate: true })

onMounted(() => {
  fetchUserProfile()
  fetchArticles()
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.user-profile-page {
  max-width: 100%;
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

/* 冻结提示横幅 */
.frozen-banner {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 20px;
  margin-bottom: 16px;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: 8px;
  color: #ef4444;
  font-size: 14px;
  font-weight: 500;
  
  .el-icon {
    font-size: 18px;
  }
}

/* 注销提示横幅 */
.cancelled-banner {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 20px;
  margin-bottom: 16px;
  background: rgba(107, 114, 128, 0.1);
  border: 1px solid rgba(107, 114, 128, 0.3);
  border-radius: 8px;
  color: #6b7280;
  font-size: 14px;
  font-weight: 500;
  
  .el-icon {
    font-size: 18px;
  }
}

/* 冻结标签 */
.frozen-tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  margin-left: 8px;
  background: rgba(239, 68, 68, 0.15);
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: 4px;
  color: #ef4444;
  font-size: 12px;
  font-weight: 500;
}

/* 注销标签 */
.cancelled-tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  margin-left: 8px;
  background: rgba(107, 114, 128, 0.15);
  border: 1px solid rgba(107, 114, 128, 0.3);
  border-radius: 4px;
  color: #6b7280;
  font-size: 12px;
  font-weight: 500;
}

/* 已注销用户名样式 */
.cancelled-name {
  color: #6b7280 !important;
  font-size: 24px;
  font-weight: 600;
}

.profile-card {
  padding: 30px;
  margin-bottom: 24px;
}

.profile-header {
  display: flex;
  align-items: flex-start;
  gap: 24px;
  
  @media (max-width: 768px) {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
}

.avatar-wrapper {
  flex-shrink: 0;
  width: 108px;
  height: 108px;
  border-radius: 50%;
  padding: 4px;
  background: var(--bg-card-hover);
  display: flex;
  align-items: center;
  justify-content: center;
  
  :deep(.el-avatar) {
    width: 100px !important;
    height: 100px !important;
    border-radius: 50%;
  }
  
  &.vip-avatar {
    &.vip-level-1 {
      background: linear-gradient(135deg, #cd7f32, #b8860b);
    }
    &.vip-level-2 {
      background: linear-gradient(135deg, #c0c0c0, #a8a8a8);
    }
    &.vip-level-3 {
      background: linear-gradient(135deg, #ffd700, #ffb700);
    }
  }
  
  /* 冻结用户头像灰色边框 */
  &.frozen {
    background: linear-gradient(135deg, #6b7280, #4b5563) !important;
    
    :deep(.el-avatar) {
      filter: grayscale(50%);
    }
  }
  
  /* 已注销用户头像样式 */
  &.cancelled {
    background: linear-gradient(135deg, #9ca3af, #6b7280) !important;
    
    :deep(.el-avatar) {
      background: #4b5563;
      color: #9ca3af;
    }
  }
}

.profile-info {
  flex: 1;
}

.name-row {
  margin-bottom: 8px;
}

.profile-name {
  font-size: 24px;
}

.profile-intro {
  color: var(--text-muted);
  font-size: 14px;
  margin-bottom: 16px;
  line-height: 1.6;
}

.profile-stats {
  display: flex;
  gap: 32px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.2s;
  
  &.clickable {
    cursor: pointer;
    
    &:hover {
      background: rgba($primary-color, 0.1);
      
      .stat-value {
        color: $primary-color;
      }
    }
  }
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  transition: color 0.2s;
}

.stat-label {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 4px;
}

.profile-actions {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
  
  @media (max-width: 768px) {
    width: 100%;
    justify-content: center;
  }
}

.follow-btn, .message-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.follow-btn {
  background: $primary-color;
  border: none;
  color: #fff;
  
  &:hover:not(:disabled) {
    background: $primary-dark;
  }
  
  &.followed {
    background: var(--bg-card-hover);
    color: var(--text-secondary);
    border: 1px solid var(--border-color);
    
    &:hover:not(:disabled) {
      border-color: #ef4444;
      color: #ef4444;
    }
  }
  
  &.disabled, &:disabled {
    background: var(--bg-card-hover);
    color: var(--text-disabled);
    border: 1px solid var(--border-color);
    cursor: not-allowed;
    opacity: 0.6;
  }
}

.message-btn {
  background: transparent;
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
  
  &:hover:not(:disabled) {
    border-color: $primary-color;
    color: $primary-color;
  }
  
  &.disabled, &:disabled {
    color: var(--text-disabled);
    cursor: not-allowed;
    opacity: 0.6;
    
    &:hover {
      border-color: var(--border-color);
      color: var(--text-disabled);
    }
  }
}

.articles-section {
  padding: 24px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-color);
  
  h3 {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    color: var(--text-primary);
    
    .el-icon {
      color: $primary-color;
    }
  }
  
  @media (max-width: 768px) {
    flex-direction: column;
    gap: 12px;
  }
}

.header-actions {
  display: flex;
  gap: 12px;
  
  @media (max-width: 768px) {
    width: 100%;
    flex-direction: column;
  }
}

.search-box {
  width: 200px;
  
  @media (max-width: 768px) {
    width: 100%;
  }
}

.sort-select {
  width: 120px;
  
  @media (max-width: 768px) {
    width: 100%;
  }
}

.article-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  
  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
