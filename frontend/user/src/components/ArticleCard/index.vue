<template>
  <article class="article-card" @click="goDetail">
    <div class="card-cover">
      <img :src="coverUrl" :alt="article.title" @error="handleImageError" />
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
      </div>
    </div>
  </article>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Calendar, View, Star, CollectionTag, ChatDotRound } from '@element-plus/icons-vue'
import { formatDate } from '@/utils/format'

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
  align-items: center;
  padding-top: 14px;
  border-top: 1px solid var(--border-color);
  transition: border-color 0.3s;
}

.card-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-disabled);
  transition: color 0.3s;

  .el-icon {
    font-size: 14px;
  }
}
</style>
