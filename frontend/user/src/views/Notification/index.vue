<template>
  <div class="notification-page">
    <div class="page-card glass-card">
      <div class="page-header">
        <h2>消息通知</h2>
        <div class="header-actions">
          <el-button class="action-btn read-btn" @click="handleMarkAllRead" :disabled="!hasUnread">
            <el-icon><Check /></el-icon>
            全部已读
          </el-button>
          <el-button class="action-btn clear-btn" @click="handleClearAll" :disabled="!notifications.length">
            <el-icon><Delete /></el-icon>
            清空
          </el-button>
        </div>
      </div>

      <!-- 分类标签 -->
      <div class="type-tabs">
        <button 
          v-for="tab in tabs" 
          :key="tab.value"
          class="tab-btn"
          :class="{ active: currentType === tab.value }"
          @click="switchTab(tab.value)"
        >
          {{ tab.label }}
          <span v-if="getUnreadByType(tab.value)" class="badge">{{ getUnreadByType(tab.value) }}</span>
        </button>
      </div>

      <Loading v-if="loading" />

      <div class="notification-list" v-else-if="notifications.length">
        <div 
          v-for="item in notifications" 
          :key="item.id" 
          class="notification-item"
          :class="{ unread: !item.isRead }"
          @click="handleClick(item)"
        >
          <div class="item-left">
            <div class="avatar-wrapper" v-if="item.sender">
              <el-avatar :size="44" :src="item.sender.avatar">
                {{ item.sender.nickname?.charAt(0) || 'S' }}
              </el-avatar>
              <div class="type-icon" :class="item.type.toLowerCase()">
                <el-icon v-if="item.type === 'LIKE_ARTICLE'"><Star /></el-icon>
                <el-icon v-else-if="item.type === 'FAVORITE_ARTICLE'"><Collection /></el-icon>
                <el-icon v-else-if="item.type === 'COMMENT'"><ChatDotRound /></el-icon>
                <el-icon v-else-if="item.type === 'REPLY'"><ChatLineRound /></el-icon>
                <el-icon v-else-if="item.type === 'FOLLOW'"><UserFilled /></el-icon>
                <el-icon v-else><Bell /></el-icon>
              </div>
            </div>
            <div class="system-icon" v-else>
              <el-icon :size="24"><Bell /></el-icon>
            </div>
          </div>
          
          <div class="item-content">
            <p class="content-text">{{ item.content }}</p>
            <span class="time">{{ formatRelativeTime(item.createTime) }}</span>
          </div>
          
          <div class="item-actions">
            <el-button text size="small" @click.stop="handleDelete(item)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>
      </div>

      <el-empty v-else description="暂无通知" :image-size="120" />

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
import { useRouter } from 'vue-router'
import { Star, Collection, ChatDotRound, ChatLineRound, UserFilled, Bell, Check, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getNotificationList, markAsRead, markAllAsRead, deleteNotification, clearAllNotifications } from '@/api/notification'
import { useNotificationStore } from '@/stores/notification'
import { formatRelativeTime } from '@/utils/format'
import Loading from '@/components/Loading/index.vue'

const router = useRouter()
const notificationStore = useNotificationStore()

const loading = ref(true)
const notifications = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const currentType = ref('')

const tabs = [
  { label: '全部', value: '' },
  { label: '点赞', value: 'LIKE_ARTICLE' },
  { label: '收藏', value: 'FAVORITE_ARTICLE' },
  { label: '评论', value: 'COMMENT' },
  { label: '回复', value: 'REPLY' },
  { label: '关注', value: 'FOLLOW' },
  { label: '系统', value: 'SYSTEM' }
]

const hasUnread = computed(() => notificationStore.totalUnread > 0)

function getUnreadByType(type) {
  if (!type) return notificationStore.totalUnread
  const key = type.toLowerCase()
  return notificationStore.unreadCounts[key] || 0
}

async function fetchNotifications() {
  loading.value = true
  try {
    const res = await getNotificationList({
      page: currentPage.value,
      pageSize: pageSize.value,
      type: currentType.value || undefined
    })
    if (res.code === 200) {
      notifications.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function switchTab(type) {
  currentType.value = type
  currentPage.value = 1
  fetchNotifications()
}

function handlePageChange(page) {
  currentPage.value = page
  fetchNotifications()
}

async function handleClick(item) {
  // 标记为已读
  if (!item.isRead) {
    try {
      await markAsRead(item.id)
      item.isRead = 1
      notificationStore.fetchUnreadCount()
    } catch (e) {
      console.error(e)
    }
  }
  
  // 跳转到对应页面
  if (item.targetType === 'article' && item.targetId) {
    router.push(`/article/${item.targetId}`)
  } else if (item.type === 'FOLLOW' && item.sender?.id) {
    router.push(`/user/${item.sender.id}`)
  }
}

async function handleMarkAllRead() {
  try {
    await markAllAsRead(currentType.value || undefined)
    notifications.value.forEach(n => n.isRead = 1)
    notificationStore.clearUnread(currentType.value || undefined)
    ElMessage.success('已全部标记为已读')
  } catch (e) {
    console.error(e)
  }
}

async function handleDelete(item) {
  try {
    await deleteNotification(item.id)
    const index = notifications.value.findIndex(n => n.id === item.id)
    if (index > -1) {
      notifications.value.splice(index, 1)
      total.value--
    }
    if (!item.isRead) {
      notificationStore.fetchUnreadCount()
    }
    ElMessage.success('删除成功')
  } catch (e) {
    console.error(e)
  }
}

async function handleClearAll() {
  try {
    await ElMessageBox.confirm('确定要清空所有通知吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await clearAllNotifications(currentType.value || undefined)
    notifications.value = []
    total.value = 0
    notificationStore.fetchUnreadCount()
    ElMessage.success('清空成功')
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

onMounted(() => {
  fetchNotifications()
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.notification-page {
  max-width: 800px;
  margin: 0 auto;
}

.page-card {
  padding: 24px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  
  h2 {
    font-size: 18px;
    font-weight: 600;
    color: var(--text-primary);
  }
}

.header-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid var(--border-color);
  
  .el-icon {
    font-size: 14px;
  }
  
  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.read-btn {
  background: transparent;
  color: $primary-color;
  border-color: $primary-color;
  
  &:hover:not(:disabled) {
    background: rgba($primary-color, 0.1);
  }
}

.clear-btn {
  background: transparent;
  color: #ef4444;
  border-color: #ef4444;
  
  &:hover:not(:disabled) {
    background: rgba(239, 68, 68, 0.1);
  }
}

.type-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-color);
  flex-wrap: wrap;
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  background: var(--bg-card-hover);
  border: none;
  border-radius: 20px;
  color: var(--text-secondary);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    background: rgba($primary-color, 0.1);
    color: $primary-color;
  }
  
  &.active {
    background: $primary-color;
    color: #fff;
    
    .badge {
      background: rgba(255, 255, 255, 0.3);
      color: #fff;
    }
  }
  
  .badge {
    min-width: 18px;
    height: 18px;
    padding: 0 6px;
    background: #ef4444;
    border-radius: 9px;
    font-size: 11px;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  background: var(--bg-card-hover);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    background: rgba($primary-color, 0.05);
  }
  
  &.unread {
    background: rgba($primary-color, 0.08);
    border-left: 3px solid $primary-color;
  }
}

.item-left {
  flex-shrink: 0;
}

.avatar-wrapper {
  position: relative;
  
  .type-icon {
    position: absolute;
    bottom: -2px;
    right: -2px;
    width: 20px;
    height: 20px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 11px;
    color: #fff;
    
    &.like_article { background: #ef4444; }
    &.favorite_article { background: #f59e0b; }
    &.comment { background: $primary-color; }
    &.reply { background: #10b981; }
    &.follow { background: #8b5cf6; }
    &.system { background: #6b7280; }
  }
}

.system-icon {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: var(--bg-card);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
}

.item-content {
  flex: 1;
  min-width: 0;
}

.content-text {
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.5;
  margin-bottom: 4px;
}

.time {
  font-size: 12px;
  color: var(--text-muted);
}

.item-actions {
  flex-shrink: 0;
  opacity: 0;
  transition: opacity 0.2s;
  
  .notification-item:hover & {
    opacity: 1;
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
