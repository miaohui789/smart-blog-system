<template>
  <div class="comment-list">
    <Loading v-if="loading" mini />
    <template v-else>
      <div v-for="comment in comments" :key="comment.id" class="comment-item">
        <el-avatar :src="comment.user?.avatar" :size="44">
          {{ comment.user?.nickname?.charAt(0) || 'U' }}
        </el-avatar>
        <div class="comment-content">
          <div class="comment-header">
            <span class="comment-author">{{ comment.user?.nickname || '匿名用户' }}</span>
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
              <el-avatar :src="reply.user?.avatar" :size="32">
                {{ reply.user?.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <div class="reply-content">
                <span class="reply-author">{{ reply.user?.nickname || '匿名用户' }}</span>
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

defineProps({
  comments: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false }
})

defineEmits(['like', 'reply'])
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.comment-list {
  display: flex;
  flex-direction: column;
}

.loading-state {
  padding: $spacing-lg 0;
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

.comment-content {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  margin-bottom: $spacing-sm;
}

.comment-author {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 14px;
  transition: color 0.3s;
}

.comment-time {
  font-size: 12px;
  color: var(--text-muted);
  transition: color 0.3s;
}

.comment-text {
  color: var(--text-secondary);
  line-height: 1.7;
  margin-bottom: $spacing-sm;
  font-size: 14px;
  word-break: break-word;
  transition: color 0.3s;
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
  transition: background-color 0.3s;
}

.reply-item {
  display: flex;
  gap: $spacing-sm;
}

.reply-content {
  font-size: 13px;
  line-height: 1.6;
}

.reply-author {
  color: $primary-light;
  font-weight: 500;
  margin-right: 4px;
}

.reply-to {
  color: var(--text-muted);
  transition: color 0.3s;
}

.reply-target {
  color: $primary-color;
}

.reply-text {
  color: var(--text-secondary);
  transition: color 0.3s;
}
</style>
