<template>
  <div class="comment-list">
    <Loading v-if="loading" mini />
    <template v-else>
      <div v-for="comment in comments" :key="comment.id" class="comment-item">
        <div class="avatar-wrapper" :class="getVipAvatarClass(comment.user?.vipLevel)">
          <el-avatar :src="comment.user?.avatar" :size="44">
            {{ comment.user?.nickname?.charAt(0) || 'U' }}
          </el-avatar>
        </div>
        <div class="comment-content">
          <div class="comment-header">
            <VipUsername 
              :username="comment.user?.nickname || '匿名用户'" 
              :vip-level="comment.user?.vipLevel || 0" 
            />
            <span class="comment-time">{{ formatRelativeTime(comment.createTime) }}</span>
          </div>
          <p class="comment-text">{{ comment.content }}</p>
          <div class="comment-actions">
            <span class="action-item" @click="$emit('like', comment.id)">
              <el-icon><Star /></el-icon>
              {{ comment.likeCount || 0 }}
            </span>
            <span class="action-item" @click="$emit('reply', comment)">
              <el-icon><ChatDotRound /></el-icon>
              回复
            </span>
          </div>
          <div v-if="comment.replies?.length" class="comment-replies">
            <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
              <div class="avatar-wrapper small" :class="getVipAvatarClass(reply.user?.vipLevel)">
                <el-avatar :src="reply.user?.avatar" :size="32">
                  {{ reply.user?.nickname?.charAt(0) || 'U' }}
                </el-avatar>
              </div>
              <div class="reply-content">
                <VipUsername 
                  :username="reply.user?.nickname || '匿名用户'" 
                  :vip-level="reply.user?.vipLevel || 0" 
                />
                <span v-if="reply.replyUser" class="reply-to">
                  回复 <span class="reply-target">@{{ reply.replyUser.nickname }}</span>
                </span>
                <span class="reply-text">{{ reply.content }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { Star, ChatDotRound } from '@element-plus/icons-vue'
import { formatRelativeTime } from '@/utils/format'
import Loading from '@/components/Loading/index.vue'
import VipUsername from '@/components/VipUsername/index.vue'

defineProps({
  comments: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false }
})

defineEmits(['like', 'reply'])

function getVipAvatarClass(vipLevel) {
  if (!vipLevel || vipLevel <= 0) return ''
  return `vip-avatar vip-level-${vipLevel}`
}
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.comment-list {
  display: flex;
  flex-direction: column;
}

.comment-item {
  display: flex;
  gap: $spacing-md;
  padding: $spacing-lg 0;
  border-bottom: 1px solid var(--border-color);

  &:last-child {
    border-bottom: none;
  }
}

/* VIP头像边框样式 */
.avatar-wrapper {
  position: relative;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
}

.avatar-wrapper.vip-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  padding: 3px;
  box-sizing: border-box;
  
  :deep(.el-avatar) {
    width: 100% !important;
    height: 100% !important;
    border: none;
  }
}

.avatar-wrapper.vip-avatar.small {
  width: 38px;
  height: 38px;
}

.avatar-wrapper.vip-level-1 {
  background: linear-gradient(135deg, #cd7f32, #daa520);
}

.avatar-wrapper.vip-level-2 {
  background: linear-gradient(135deg, #a8a8a8, #e0e0e0);
}

.avatar-wrapper.vip-level-3 {
  background: linear-gradient(135deg, #ffd700, #ffb700);
  box-shadow: 0 0 10px rgba(255, 215, 0, 0.5);
}

.comment-content {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  margin-bottom: $spacing-sm;
  flex-wrap: wrap;
}

.comment-time {
  font-size: 12px;
  color: var(--text-muted);
}

.comment-text {
  color: var(--text-secondary);
  line-height: 1.7;
  margin-bottom: $spacing-sm;
  font-size: 14px;
  word-break: break-word;
}

.comment-actions {
  display: flex;
  gap: $spacing-lg;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-muted);
  cursor: pointer;
  padding: 4px 8px;
  border-radius: $radius-sm;
  transition: all 0.3s;

  &:hover {
    color: $primary-light;
    background: rgba($primary-color, 0.1);
  }
}

.comment-replies {
  margin-top: $spacing-md;
  padding: $spacing-md;
  background: var(--bg-darker);
  border-radius: $radius-md;
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.reply-item {
  display: flex;
  gap: $spacing-sm;
}

.reply-content {
  font-size: 13px;
  line-height: 1.6;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
}

.reply-to {
  color: var(--text-muted);
}

.reply-target {
  color: $primary-color;
}

.reply-text {
  color: var(--text-secondary);
  flex-basis: 100%;
  margin-top: 4px;
}
</style>
