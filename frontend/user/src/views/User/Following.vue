<template>
  <div class="following-page">
    <!-- 返回按钮 -->
    <div class="back-nav">
      <button class="back-btn" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回</span>
      </button>
    </div>

    <div class="page-card glass-card">
      <div class="page-header">
        <h2>我的关注</h2>
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
              <span class="article-count">{{ user.articleCount || 0 }} 篇文章</span>
            </div>
          </div>
          <div class="user-actions">
            <button class="message-btn" @click="openChat(user.id)">
              <el-icon><ChatDotRound /></el-icon>
              私信
            </button>
            <button 
              class="follow-btn followed" 
              @click="handleUnfollow(user)"
            >
              已关注
            </button>
          </div>
        </div>
      </div>

      <el-empty v-else description="暂无关注" />

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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, ChatDotRound } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getFollowingList, unfollowUser } from '@/api/follow'
import Loading from '@/components/Loading/index.vue'
import VipUsername from '@/components/VipUsername/index.vue'

const router = useRouter()

const loading = ref(true)
const users = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
  }
}

function goUserProfile(userId) {
  router.push(`/user/${userId}`)
}

function openChat(userId) {
  router.push(`/message/${userId}`)
}

async function fetchFollowing() {
  loading.value = true
  try {
    const res = await getFollowingList({
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

async function handleUnfollow(user) {
  try {
    await ElMessageBox.confirm(
      `确定要取消关注 ${user.nickname} 吗？`,
      '提示',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await unfollowUser(user.id)
    users.value = users.value.filter(u => u.id !== user.id)
    total.value = Math.max(0, total.value - 1)
    ElMessage.success('已取消关注')
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

function handlePageChange(page) {
  currentPage.value = page
  fetchFollowing()
}

onMounted(() => {
  fetchFollowing()
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.following-page {
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
  
  &.vip-avatar {
    &.vip-level-1 { background: linear-gradient(135deg, #cd7f32, #b8860b); }
    &.vip-level-2 { background: linear-gradient(135deg, #c0c0c0, #a8a8a8); }
    &.vip-level-3 { background: linear-gradient(135deg, #ffd700, #ffb700); }
  }
  
  :deep(.el-avatar) {
    border-radius: 50% !important;
  }
}

.user-info {
  flex: 1;
  min-width: 0;
  max-width: calc(100% - 70px);
  overflow: hidden;
}

.user-intro {
  font-size: 13px;
  color: var(--text-muted);
  margin: 4px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}

.article-count {
  font-size: 12px;
  color: var(--text-disabled);
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
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
  
  &.followed:hover {
    border-color: #ef4444;
    color: #ef4444;
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
    
    .message-btn, .unfollow-btn {
      flex: 1;
      justify-content: center;
    }
  }
}
</style>
