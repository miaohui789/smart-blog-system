<template>
  <article class="article-card" @click="goDetail">
    <div v-if="article.cover" class="card-cover">
      <img :src="article.cover" :alt="article.title" />
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
        </div>
        <div class="card-tags" v-if="article.tags?.length">
          <span v-for="tag in article.tags.slice(0, 2)" :key="tag.id" class="tag">
            #{{ tag.name }}
          </span>
        </div>
      </div>
    </div>
  </article>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { Calendar, View } from '@element-plus/icons-vue'
import { formatDate } from '@/utils/format'

const props = defineProps({
  article: { type: Object, required: true }
})

const router = useRouter()

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
  gap: 14px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: var(--text-disabled);
  transition: color 0.3s;

  .el-icon {
    font-size: 14px;
  }
}

.card-tags {
  display: flex;
  gap: 8px;
}

.tag {
  padding: 3px 8px;
  background: rgba($success-color, 0.1);
  color: $success-color;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
}
</style>
