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
      <div class="profile-card glass-card" :class="['level-tier-' + userLevelTheme.tier]" :style="profileCardStyle">
        <div class="level-card-bg"></div>
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
                  :user-level="userProfile.userLevel || 1"
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

            <div class="profile-exp" v-if="userProfile.status !== 2 && userProfile.nextLevelNeedExp !== undefined">
              <ExpBar 
                :level="userProfile.userLevel" 
                :current-exp="userProfile.currentExp" 
                :next-level-need-exp="userProfile.nextLevelNeedExp" 
              />
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
            {{ isSelf ? '我的文章' : 'TA的文章' }}
          </h3>
          <div class="header-actions">
            <!-- 搜索框 -->
            <div class="search-box transparent-input">
              <el-input
                v-model="searchKeyword"
                placeholder="搜索文章..."
                :prefix-icon="Search"
                clearable
                @keyup.enter="handleSearch"
                @clear="handleSearch"
                class="transparent-bg"
              />
            </div>
            <!-- 排序 -->
            <el-select v-model="sortBy" @change="fetchArticles" class="sort-select transparent-input transparent-bg">
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
import { getUserLevelTheme } from '@/utils/level'
import Loading from '@/components/Loading/index.vue'
import ArticleCard from '@/components/ArticleCard/index.vue'
import VipUsername from '@/components/VipUsername/index.vue'
import ExpBar from '@/components/ExpBar/index.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const userProfile = ref(null)
const articles = ref([])

const userLevelTheme = computed(() => {
  return getUserLevelTheme(userProfile.value?.userLevel || 1)
})

const profileCardStyle = computed(() => {
  if (!userProfile.value) return {}
  const theme = userLevelTheme.value
  return {
    '--card-bg-gradient': theme.gradient,
    '--card-shadow': theme.shadow,
    '--card-text-gradient': theme.textGradient
  }
})

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
  position: relative;
  overflow: hidden;
  z-index: 1;
}

.level-card-bg {
  position: absolute;
  inset: 0;
  background: var(--card-bg-gradient);
  opacity: 0.08; /* 浅色模式下有一点颜色 */
  z-index: -1;
  transition: opacity 0.3s;
}

:root[data-theme="dark"] .level-card-bg {
  opacity: 0.12; /* 暗色模式下稍微明显一点 */
}

/* 1~10级主页名片卡片SVG花纹 */
.profile-card.level-tier-1::after {
  content: ''; position: absolute; top: 0; right: 0; width: 100%; height: 100%; z-index: 0; pointer-events: none; background-position: top right; background-repeat: no-repeat;
  background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="100%" height="100%" opacity="0.15"><path d="M 200 0 L 300 100 L 400 0" fill="none" stroke="%2394a3b8" stroke-width="3"/><circle cx="300" cy="50" r="8" fill="%2394a3b8"/></svg>');
}
:root[data-theme="dark"] .profile-card.level-tier-1::after { background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="100%" height="100%" opacity="0.3"><path d="M 200 0 L 300 100 L 400 0" fill="none" stroke="white" stroke-width="3"/><circle cx="300" cy="50" r="8" fill="white"/></svg>'); }

.profile-card.level-tier-2::after {
  content: ''; position: absolute; top: 0; right: 0; width: 100%; height: 100%; z-index: 0; pointer-events: none; background-position: top right; background-repeat: no-repeat;
  background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="100%" height="100%" opacity="0.15"><path d="M 150 100 Q 250 0 350 100" fill="none" stroke="%2338bdf8" stroke-width="3"/><circle cx="250" cy="50" r="8" fill="%2338bdf8"/></svg>');
}
:root[data-theme="dark"] .profile-card.level-tier-2::after { background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="100%" height="100%" opacity="0.3"><path d="M 150 100 Q 250 0 350 100" fill="none" stroke="white" stroke-width="3"/><circle cx="250" cy="50" r="8" fill="white"/></svg>'); }

.profile-card.level-tier-3::after {
  content: ''; position: absolute; top: 0; right: 0; width: 100%; height: 100%; z-index: 0; pointer-events: none; background-position: top right; background-repeat: no-repeat;
  background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="100%" height="100%" opacity="0.15"><circle cx="300" cy="50" r="40" fill="none" stroke="%2360a5fa" stroke-width="3"/><circle cx="300" cy="50" r="10" fill="%2360a5fa"/><circle cx="200" cy="100" r="6" fill="%2360a5fa"/></svg>');
}
:root[data-theme="dark"] .profile-card.level-tier-3::after { background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="100%" height="100%" opacity="0.3"><circle cx="300" cy="50" r="40" fill="none" stroke="white" stroke-width="3"/><circle cx="300" cy="50" r="10" fill="white"/><circle cx="200" cy="100" r="6" fill="white"/></svg>'); }

.profile-card.level-tier-4::after {
  content: ''; position: absolute; top: 0; right: 0; width: 100%; height: 100%; z-index: 0; pointer-events: none; background-position: top right; background-repeat: no-repeat;
  background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="100%" height="100%" opacity="0.15"><path d="M 100 100 Q 200 0 300 100 T 500 100" fill="none" stroke="%23818cf8" stroke-width="3"/><circle cx="200" cy="50" r="8" fill="%23818cf8"/></svg>');
}
:root[data-theme="dark"] .profile-card.level-tier-4::after { background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="100%" height="100%" opacity="0.3"><path d="M 100 100 Q 200 0 300 100 T 500 100" fill="none" stroke="white" stroke-width="3"/><circle cx="200" cy="50" r="8" fill="white"/></svg>'); }

.profile-card.level-tier-5::after {
  content: ''; position: absolute; top: 0; right: 0; width: 100%; height: 100%; z-index: 0; pointer-events: none; background-position: top right; background-repeat: no-repeat;
  background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="100%" height="100%" opacity="0.15"><path d="M 100 50 Q 200 80 300 50 T 500 50" fill="none" stroke="%23c084fc" stroke-width="2"/><path d="M 100 120 Q 200 150 300 120 T 500 120" fill="none" stroke="%23c084fc" stroke-width="2"/><circle cx="200" cy="85" r="8" fill="%23c084fc"/><circle cx="350" cy="85" r="4" fill="%23c084fc"/></svg>');
}
:root[data-theme="dark"] .profile-card.level-tier-5::after { background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="100%" height="100%" opacity="0.3"><path d="M 100 50 Q 200 80 300 50 T 500 50" fill="none" stroke="white" stroke-width="2"/><path d="M 100 120 Q 200 150 300 120 T 500 120" fill="none" stroke="white" stroke-width="2"/><circle cx="200" cy="85" r="8" fill="white"/><circle cx="350" cy="85" r="4" fill="white"/></svg>'); }

.profile-card.level-tier-6::after {
  content: ''; position: absolute; top: 0; right: 0; width: 100%; height: 100%; z-index: 0; pointer-events: none; background-position: top right; background-repeat: no-repeat;
  background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="100%" height="100%" opacity="0.15"><path d="M 100 150 A 150 150 0 0 1 400 150" fill="none" stroke="%23a78bfa" stroke-width="3"/><circle cx="250" cy="50" r="10" fill="%23a78bfa"/><circle cx="150" cy="80" r="4" fill="%23a78bfa"/></svg>');
}
:root[data-theme="dark"] .profile-card.level-tier-6::after { background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="100%" height="100%" opacity="0.3"><path d="M 100 150 A 150 150 0 0 1 400 150" fill="none" stroke="white" stroke-width="3"/><circle cx="250" cy="50" r="10" fill="white"/><circle cx="150" cy="80" r="4" fill="white"/></svg>'); }

.profile-card.level-tier-7::after {
  content: ''; position: absolute; top: 0; right: 0; width: 100%; height: 100%; z-index: 0; pointer-events: none; background-position: top right; background-repeat: no-repeat;
  background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="100%" height="100%" opacity="0.15"><ellipse cx="300" cy="80" rx="120" ry="40" fill="none" stroke="%23fb7185" stroke-width="3" transform="rotate(-15 300 80)"/><circle cx="300" cy="80" r="15" fill="%23fb7185"/><circle cx="150" cy="120" r="6" fill="%23fb7185"/></svg>');
}
:root[data-theme="dark"] .profile-card.level-tier-7::after { background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="100%" height="100%" opacity="0.3"><ellipse cx="300" cy="80" rx="120" ry="40" fill="none" stroke="white" stroke-width="3" transform="rotate(-15 300 80)"/><circle cx="300" cy="80" r="15" fill="white"/><circle cx="150" cy="120" r="6" fill="white"/></svg>'); }

.profile-card.level-tier-8::after {
  content: ''; position: absolute; top: 0; right: 0; width: 100%; height: 100%; z-index: 0; pointer-events: none; background-position: top right; background-repeat: no-repeat;
  background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="100%" height="100%" opacity="0.15"><path d="M 300 20 L 320 60 L 360 80 L 320 100 L 300 140 L 280 100 L 240 80 L 280 60 Z" fill="%23f472b6"/><circle cx="180" cy="40" r="8" fill="%23f472b6"/></svg>');
}
:root[data-theme="dark"] .profile-card.level-tier-8::after { background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="100%" height="100%" opacity="0.3"><path d="M 300 20 L 320 60 L 360 80 L 320 100 L 300 140 L 280 100 L 240 80 L 280 60 Z" fill="white"/><circle cx="180" cy="40" r="8" fill="white"/></svg>'); }

.profile-card.level-tier-9::after {
  content: ''; position: absolute; top: 0; right: 0; width: 100%; height: 100%; z-index: 0; pointer-events: none; background-position: top right; background-repeat: no-repeat;
  background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="100%" height="100%" opacity="0.15"><path d="M 200 120 L 300 20 L 400 120 Z" fill="none" stroke="%23fb923c" stroke-width="3"/><rect x="250" y="120" width="100" height="40" fill="%23fb923c" opacity="0.5"/><circle cx="300" cy="70" r="8" fill="%23fb923c"/></svg>');
}
:root[data-theme="dark"] .profile-card.level-tier-9::after { background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="100%" height="100%" opacity="0.3"><path d="M 200 120 L 300 20 L 400 120 Z" fill="none" stroke="white" stroke-width="3"/><rect x="250" y="120" width="100" height="40" fill="white" opacity="0.5"/><circle cx="300" cy="70" r="8" fill="white"/></svg>'); }

.profile-card.level-tier-10::after {
  content: ''; position: absolute; top: 0; right: 0; width: 100%; height: 100%; z-index: 0; pointer-events: none; background-position: top right; background-repeat: no-repeat;
  background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="100%" height="100%" opacity="0.15"><path d="M 150 80 Q 300 0 450 80 Q 300 160 150 80" fill="none" stroke="%23facc15" stroke-width="3"/><path d="M 220 40 L 380 120 M 220 120 L 380 40" stroke="%23facc15" stroke-width="2"/><circle cx="300" cy="80" r="15" fill="%23facc15"/></svg>');
}
:root[data-theme="dark"] .profile-card.level-tier-10::after { background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="100%" height="100%" opacity="0.3"><path d="M 150 80 Q 300 0 450 80 Q 300 160 150 80" fill="none" stroke="white" stroke-width="3"/><path d="M 220 40 L 380 120 M 220 120 L 380 40" stroke="white" stroke-width="2"/><circle cx="300" cy="80" r="15" fill="white"/></svg>'); }

/* 100级专属超级名片样式 */
.profile-card.level-tier-11 {
  background: var(--bg-card); /* 覆盖默认背景 */
}

.profile-card.level-tier-11 .level-card-bg {
  opacity: 1; /* 100级完全显示渐变背景 */
  background: linear-gradient(135deg, #fdf4ff 0%, #f3e8ff 50%, #e0e7ff 100%);
}

:root[data-theme="dark"] .profile-card.level-tier-11 .level-card-bg {
  background: linear-gradient(135deg, #2e1065 0%, #1e1b4b 50%, #0f172a 100%);
}

.profile-card.level-tier-11::after {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 100%;
  height: 100%;
  background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="100%" height="100%" opacity="0.4"><path d="M 0 100 Q 200 300 400 0 T 800 200" fill="none" stroke="%23a855f7" stroke-width="3"/><path d="M 0 150 Q 250 350 500 50 T 900 250" fill="none" stroke="%23a855f7" stroke-width="2" opacity="0.6"/><circle cx="150" cy="80" r="5" fill="%23a855f7"/><circle cx="350" cy="200" r="3" fill="%23a855f7"/><circle cx="600" cy="120" r="4" fill="%23a855f7"/><circle cx="750" cy="250" r="2.5" fill="%23a855f7"/><circle cx="50" cy="250" r="2" fill="%23a855f7"/><circle cx="250" cy="50" r="1.5" fill="%23a855f7"/><circle cx="450" cy="280" r="2" fill="%23a855f7"/><circle cx="850" cy="80" r="3" fill="%23a855f7"/></svg>');
  background-position: center;
  background-repeat: no-repeat;
  background-size: cover;
  opacity: 0.6;
  z-index: 0;
  pointer-events: none;
}

:root[data-theme="dark"] .profile-card.level-tier-11::after {
  background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="100%" height="100%" opacity="0.6"><path d="M 0 100 Q 200 300 400 0 T 800 200" fill="none" stroke="white" stroke-width="3"/><path d="M 0 150 Q 250 350 500 50 T 900 250" fill="none" stroke="white" stroke-width="2" opacity="0.6"/><circle cx="150" cy="80" r="5" fill="white"/><circle cx="350" cy="200" r="3" fill="white"/><circle cx="600" cy="120" r="4" fill="white"/><circle cx="750" cy="250" r="2.5" fill="white"/><circle cx="50" cy="250" r="2" fill="white"/><circle cx="250" cy="50" r="1.5" fill="white"/><circle cx="450" cy="280" r="2" fill="white"/><circle cx="850" cy="80" r="3" fill="white"/></svg>');
}

.profile-header {
  display: flex;
  align-items: flex-start;
  gap: 24px;
  position: relative;
  z-index: 1;
  
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

.profile-exp {
  margin-top: 16px;
  max-width: 400px;
}

.profile-actions {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
  
  @media (max-width: 768px) {
    width: 100%;
    justify-content: center;
    
    .follow-btn, .message-btn {
      flex: 1;
      justify-content: center;
    }
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
  align-items: center;
  
  @media (max-width: 768px) {
    width: 100%;
    flex-direction: column;
  }
}

.search-box {
  width: 200px;
  
  :deep(.el-input__wrapper) {
    border-radius: 24px !important;
    padding: 8px 16px !important;
    height: 36px !important;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06) !important;
    border: 1px solid var(--border-color) !important;
    background: transparent !important;
    backdrop-filter: none !important;
    transition: all 0.3s ease !important;
    overflow: hidden !important;
    
    &:hover {
      border-color: rgba(59, 130, 246, 0.4) !important;
      box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15) !important;
      background: rgba(59, 130, 246, 0.03) !important;
    }
    
    &.is-focus {
      background: transparent !important;
    }
  }
  
  :deep(.el-input__inner) {
    color: var(--text-primary) !important;
    font-size: 13px !important;
    height: 100% !important;
    line-height: 36px !important;
    background: transparent !important;
  }
  
  :deep(.el-input__prefix) {
    color: var(--text-muted) !important;
  }
  
  :deep(.el-input__prefix-inner) {
    display: flex !important;
    align-items: center !important;
  }
  
  @media (max-width: 768px) {
    width: 100%;
  }
}

// 强制覆盖全局样式 - 透明背景
.transparent-input {
  :deep(.el-input__wrapper) {
    background: transparent !important;
  }
}

.transparent-bg {
  :deep(.el-input__wrapper) {
    background: transparent !important;
  }
  
  :deep(.el-input__inner) {
    background: transparent !important;
  }
}

.user-profile-page .search-box :deep(.el-input__wrapper) {
  background: transparent !important;
}

.user-profile-page .sort-select :deep(.el-input__wrapper) {
  background: transparent !important;
}

.sort-select {
  width: 120px;
  
  :deep(.el-input__wrapper) {
    border-radius: 24px !important;
    padding: 8px 16px !important;
    height: 36px !important;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06) !important;
    border: 1px solid var(--border-color) !important;
    background: transparent !important;
    backdrop-filter: none !important;
    transition: all 0.3s ease !important;
    overflow: hidden !important;
    
    &:hover {
      border-color: rgba(59, 130, 246, 0.4) !important;
      box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15) !important;
      background: rgba(59, 130, 246, 0.03) !important;
    }
    
    &.is-focus {
      background: transparent !important;
    }
  }
  
  :deep(.el-input__inner) {
    color: var(--text-primary) !important;
    font-size: 13px !important;
    font-weight: 500 !important;
    height: 100% !important;
    line-height: 36px !important;
    background: transparent !important;
  }
  
  :deep(.el-input__suffix) {
    color: var(--text-muted) !important;
  }
  
  :deep(.el-input__suffix-inner) {
    display: flex !important;
    align-items: center !important;
  }
  
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
