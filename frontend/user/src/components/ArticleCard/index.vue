<template>
  <article class="article-card" @click="goDetail">
    <div v-if="article.cover" class="card-cover">
      <img :src="article.cover" :alt="article.title" />
    </div>
    <div class="card-content">
      <h3 class="card-title">{{ article.title }}</h3>
      <p class="card-summary">{{ article.summary }}</p>
      <div class="card-footer">
        <div class="card-meta">
          <span><el-icon><Calendar /></el-icon>{{ formatDate(article.publishTime) }}</span>
          <span><el-icon><View /></el-icon>{{ article.viewCount || 0 }}</span>
        </div>
        <div class="card-tags" v-if="article.tags?.length">
          <span v-for="tag in article.tags.slice(0, 2)" :key="tag.id" class="tag">
            {{ tag.name }}
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
.article-card {
  display: flex;
  flex-direction: column;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    border-color: var(--border-light);
    transform: translateY(-2px);
    box-shadow: 0 8px 24px var(--shadow-color);

    .card-cover img {
      transform: scale(1.03);
    }

    .card-title {
      color: var(--text-primary);
    }
  }
}

.card-cover {
  height: 140px;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s ease;
  }
}

.card-content {
  padding: 16px;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 8px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.2s;
}

.card-summary {
  font-size: 13px;
  color: var(--text-muted);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 12px;
  flex: 1;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid var(--border-color);
}

.card-meta {
  display: flex;
  gap: 12px;

  span {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: var(--text-disabled);
  }
}

.card-tags {
  display: flex;
  gap: 6px;
}

.tag {
  padding: 2px 8px;
  background: var(--bg-card-hover);
  color: var(--text-muted);
  border-radius: 4px;
  font-size: 11px;
  transition: background-color 0.3s, color 0.3s;
}
</style>
