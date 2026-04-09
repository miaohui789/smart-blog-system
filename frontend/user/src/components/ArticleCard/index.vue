<template>
  <article class="article-card" @click="goDetail">
    <div class="card-cover">
      <img :src="coverUrl" :alt="article.title" loading="lazy" decoding="async" @error="handleImageError" />
      <div class="cover-overlay"></div>
    </div>
    <div class="card-content">
      <div class="card-category" v-if="article.category">
        <span class="category-badge">{{ article.category.name }}</span>
      </div>
      <h3 class="card-title">{{ article.title }}</h3>
      <p class="card-summary">{{ article.summary }}</p>
      <div class="card-footer">
        <div class="card-meta">
          <span class="meta-item">
            <el-icon><Calendar /></el-icon>
            {{ formatDate(article.publishTime) }}
          </span>
          <span class="meta-item">
            <el-icon><View /></el-icon>
            {{ article.viewCount || 0 }}
          </span>
          <span class="meta-item">
            <el-icon><Star /></el-icon>
            {{ article.likeCount || 0 }}
          </span>
          <span class="meta-item">
            <el-icon><CollectionTag /></el-icon>
            {{ article.favoriteCount || 0 }}
          </span>
          <span class="meta-item">
            <el-icon><ChatDotRound /></el-icon>
            {{ article.commentCount || 0 }}
          </span>
        </div>
        
        <!-- 作者信息区域 -->
        <div class="card-author" v-if="article.author" @click.stop="goAuthorDetail">
          <VipUsername 
            :username="article.author.nickname || article.author.username"
            :userLevel="article.author.userLevel"
            :vipLevel="article.author.vipLevel"
            :showBadge="true"
          />
          <el-avatar 
            :size="24" 
            :src="article.author.avatar" 
            class="author-avatar"
          >
            {{ (article.author.nickname || article.author.username || 'U').charAt(0).toUpperCase() }}
          </el-avatar>
        </div>
      </div>
    </div>
  </article>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Calendar, View, Star, CollectionTag, ChatDotRound } from '@element-plus/icons-vue'
import { formatDate } from '@/utils/format'
import VipUsername from '@/components/VipUsername/index.vue'

const props = defineProps({
  article: { type: Object, required: true }
})

const router = useRouter()
const imageError = ref(false)

// 默认封面图（使用渐变色SVG作为占位图）
const defaultCover = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="400" height="200"%3E%3Cdefs%3E%3ClinearGradient id="g" x1="0%25" y1="0%25" x2="100%25" y2="100%25"%3E%3Cstop offset="0%25" stop-color="%23a855f7"/%3E%3Cstop offset="100%25" stop-color="%23ec4899"/%3E%3C/linearGradient%3E%3C/defs%3E%3Crect width="400" height="200" fill="url(%23g)"/%3E%3Ctext x="50%25" y="50%25" font-family="Arial" font-size="24" fill="white" text-anchor="middle" dy=".3em"%3E📝 Blog%3C/text%3E%3C/svg%3E'

const coverUrl = computed(() => {
  if (imageError.value || !props.article.cover) {
    return defaultCover
  }
  return props.article.cover
})

function handleImageError() {
  imageError.value = true
}

function goDetail() {
  router.push(`/article/${props.article.id}`)
}

function goAuthorDetail() {
  if (props.article.author?.id) {
    router.push(`/user/${props.article.author.id}`)
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.article-card {
  display: flex;
  flex-direction: column;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  z-index: 1;
  pointer-events: auto;

  &:hover {
    border-color: rgba($primary-color, 0.3);
    transform: translateY(-4px);
    box-shadow: 0 12px 32px var(--shadow-color);

    .card-cover img {
      transform: scale(1.05);
    }

    .cover-overlay {
      opacity: 0.2;
    }

    .card-title {
      color: $primary-color;
    }
  }
}

.card-cover {
  height: 160px;
  overflow: hidden;
  position: relative;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.4s ease;
  }
}

.cover-overlay {
  position: absolute;
  inset: 0;
  background: $primary-color;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.card-content {
  padding: 20px;
  display: flex;
  flex-direction: column;
  flex: 1;
  
  @media (max-width: 768px) {
    padding: 16px;
  }
}

.card-category {
  margin-bottom: 10px;
}

.category-badge {
  display: inline-block;
  padding: 4px 10px;
  background: rgba($primary-color, 0.1);
  color: $primary-color;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 10px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.3s ease;
}

.card-summary {
  font-size: 13px;
  color: var(--text-muted);
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 16px;
  flex: 1;
  transition: color 0.3s;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: flex-end; /* 底部对齐，防止头像和多行元信息高度不一致导致错位 */
  padding-top: 14px;
  border-top: 1px solid var(--border-color);
  transition: border-color 0.3s;
  min-height: 44px; /* 保证底部区域有足够高度 */

  /* 移动端适配：空间极小时允许垂直排列 */
  @media (max-width: 400px) {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}

.card-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 10px; /* 增加行间距 */
  flex: 1; /* 让左侧元信息占据剩余空间 */
  min-width: 0; /* 防止子元素撑破父容器 */
  margin-right: 12px; /* 与右侧作者信息保持间距 */

  @media (max-width: 400px) {
    margin-right: 0;
    width: 100%;
  }
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-disabled);
  transition: color 0.3s;
  white-space: nowrap; /* 防止单项换行 */

  .el-icon {
    font-size: 14px;
  }
}

.card-author {
  display: flex;
  align-items: center;
  gap: 8px;
  transition: opacity 0.3s;
  flex-shrink: 0; /* 防止作者信息区域被挤压 */
  
  &:hover {
    opacity: 0.8;
  }
  
  /* 调整等级标签和用户名的尺寸使其更适合小空间 */
  :deep(.vip-username) {
    font-size: 12px;
    display: flex;
    align-items: center;
    gap: 4px;
    
    .username-text {
      max-width: 80px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .badge-group {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }
  
  :deep(.user-level-badge) {
    transform: scale(0.85);
    transform-origin: right center;
    margin: 0;
  }

  .author-avatar {
    border: 1px solid var(--border-color);
    flex-shrink: 0; /* 强制头像不被挤压 */
    width: 24px !important;
    height: 24px !important;
    border-radius: 50% !important; /* 强制正圆形 */
    display: flex;
    align-items: center;
    justify-content: center;
  }

  /* 移动端适配：缩小字体和间距，隐藏部分非核心信息 */
  @media (max-width: 480px) {
    gap: 4px;
    
    :deep(.vip-username) {
      .username-text {
        max-width: 50px; /* 移动端进一步缩短用户名显示 */
        font-size: 11px;
      }
    }
    
    :deep(.user-level-badge) {
      transform: scale(0.7); /* 移动端进一步缩小等级标签 */
    }

    .author-avatar {
      width: 20px !important;
      height: 20px !important;
    }
  }
}
</style>
