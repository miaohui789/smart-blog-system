<template>
  <div class="followers-page">
    <!-- 返回按钮 -->
    <div class="back-nav">
      <button class="back-btn" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回</span>
      </button>
    </div>

    <div class="page-card glass-card">
      <div class="page-header">
        <h2>{{ isOwnPage ? '我的粉丝' : 'TA的粉丝' }}</h2>
        <span class="count">{{ total }} 人</span>
      </div>

      <Loading v-if="loading" />

      <div class="user-list" v-else-if="users.length">
        <div v-for="user in users" :key="user.id" class="user-item">
          <div class="user-left" @click="goUserProfile(user.id)">
            <div class="avatar-wrapper" :class="{ 'vip-avatar': user.vipLevel > 0, ['vip-level-' + user.vipLevel]: user.vipLevel > 0 }">
              <el-avatar :size="50" :src="user.avatar">
                {{ user.nickname?.charAt(0) || 'U' }}
              </el-avatar>
            </div>
            <div class="user-info">
              <VipUsername 
                :username="user.nickname" 
                :user-level="user.userLevel || 1"
                :vip-level="user.vipLevel || 0"
              />
              <p class="user-intro">{{ user.intro || '这个人很懒，什么都没写~' }}</p>
            </div>
          </div>
          <div class="user-actions">
            <button class="message-btn" @click="openChat(user.id)">
              <el-icon><ChatDotRound /></el-icon>
              私信
            </button>
            <button 
              v-if="isOwnPage"
              class="follow-btn" 
              :class="{ followed: user.isFollowed }"
              @click="handleFollow(user)"
            >
              {{ user.isFollowed ? '已关注' : '回关' }}
            </button>
          </div>
        </div>
      </div>

      <el-empty v-else description="暂无粉丝" />

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
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, ChatDotRound } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getFollowersList, followUser, unfollowUser } from '@/api/follow'
import { useUserStore } from '@/stores/user'
import Loading from '@/components/Loading/index.vue'
import VipUsername from '@/components/VipUsername/index.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const users = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const isOwnPage = computed(() => {
  return userStore.userInfo?.id === Number(route.params.userId)
})

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push(`/user/${route.params.userId}`)
  }
}

function goUserProfile(userId) {
  router.push(`/user/${userId}`)
}

function openChat(userId) {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  router.push(`/message/${userId}`)
}

async function fetchFollowers() {
  loading.value = true
  try {
    const res = await getFollowersList(route.params.userId, {
      page: currentPage.value,
      pageSize: pageSize.value
    })
    if (res.code === 200) {
      users.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function handleFollow(user) {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  try {
    if (user.isFollowed) {
      await unfollowUser(user.id)
      user.isFollowed = false
      ElMessage.success('已取消关注')
    } else {
      await followUser(user.id)
      user.isFollowed = true
      ElMessage.success('关注成功')
    }
  } catch (e) {
    console.error(e)
  }
}

function handlePageChange(page) {
  currentPage.value = page
  fetchFollowers()
}

watch(() => route.params.userId, () => {
  if (route.params.userId) {
    currentPage.value = 1
    fetchFollowers()
  }
}, { immediate: true })

onMounted(() => {
  fetchFollowers()
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.followers-page {
  max-width: 800px;
  margin: 0 auto;
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
  border-radius: 16px;
  color: var(--text-secondary);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    border-color: $primary-color;
    color: $primary-color;
  }
}

.page-card {
  padding: 24px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  padding: 16px 20px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  
  h2 {
    font-size: 18px;
    font-weight: 600;
    color: var(--text-primary);
  }
  
  .count {
    font-size: 14px;
    color: var(--text-muted);
  }
}

.user-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.user-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: var(--bg-card-hover);
  border-radius: 20px;
  transition: all 0.2s;
  
  &:hover {
    background: rgba($primary-color, 0.05);
  }
}

.user-left {
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.avatar-wrapper {
  flex-shrink: 0;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  padding: 3px;
  background: var(--bg-card);
  display: flex;
  align-items: center;
  justify-content: center;
  
  :deep(.el-avatar) {
    width: 50px !important;
    height: 50px !important;
    border-radius: 50% !important;
  }
  
  &.vip-avatar {
    &.vip-level-1 { background: linear-gradient(135deg, #cd7f32, #b8860b); }
    &.vip-level-2 { background: linear-gradient(135deg, #c0c0c0, #a8a8a8); }
    &.vip-level-3 { background: linear-gradient(135deg, #ffd700, #ffb700); }
  }
}

.user-info {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.user-intro {
  font-size: 13px;
  color: var(--text-muted);
  margin-top: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}

.user-actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.message-btn, .follow-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  border-radius: 12px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.message-btn {
  background: transparent;
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
  
  &:hover {
    border-color: $primary-color;
    color: $primary-color;
  }
}

.follow-btn {
  background: $primary-color;
  border: none;
  color: #fff;
  
  &:hover {
    background: $primary-dark;
  }
  
  &.followed {
    background: var(--bg-card);
    border: 1px solid var(--border-color);
    color: var(--text-secondary);
    
    &:hover {
      border-color: #ef4444;
      color: #ef4444;
    }
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

@media (max-width: 640px) {
  .user-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .user-left {
    width: 100%;
  }

  .user-actions {
    width: 100%;
    justify-content: flex-end;
    
    .message-btn, .follow-btn {
      flex: 1;
      justify-content: center;
    }
  }
}
</style>
