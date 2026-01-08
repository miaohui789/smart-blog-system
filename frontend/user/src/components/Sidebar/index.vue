<template>
  <div class="sidebar">
    <div class="sidebar-card glass-card">
      <h3 class="card-title">
        <el-icon><TrendCharts /></el-icon>
        热门文章
      </h3>
      <ul class="hot-list">
        <li v-for="(article, index) in hotArticles" :key="article.id" class="hot-item">
          <span class="hot-rank" :class="getRankClass(index)">{{ index + 1 }}</span>
          <router-link :to="`/article/${article.id}`" class="hot-title">
            {{ article.title }}
          </router-link>
        </li>
      </ul>
      <el-empty v-if="!hotArticles.length" description="暂无热门文章" :image-size="60" />
    </div>

    <div class="sidebar-card glass-card">
      <h3 class="card-title">
        <el-icon><PriceTag /></el-icon>
        标签云
      </h3>
      <TagCloud />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { TrendCharts, PriceTag } from '@element-plus/icons-vue'
import { getHotArticles } from '@/api/article'
import TagCloud from '@/components/TagCloud/index.vue'

const hotArticles = ref([])

function getRankClass(index) {
  if (index === 0) return 'gold'
  if (index === 1) return 'silver'
  if (index === 2) return 'bronze'
  return ''
}

onMounted(async () => {
  try {
    const res = await getHotArticles()
    hotArticles.value = res.data || []
  } catch (e) {
    console.error(e)
  }
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.sidebar {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
  position: sticky;
  top: 80px;
}

.sidebar-card {
  padding: $spacing-lg;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: $radius-lg;
  transition: background-color 0.3s, border-color 0.3s;
}

.card-title {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: $spacing-lg;
  padding-bottom: $spacing-md;
  border-bottom: 1px solid var(--border-color);
  transition: color 0.3s, border-color 0.3s;
  
  .el-icon {
    color: var(--text-muted);
  }
}

.hot-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hot-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.hot-rank {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 600;
  color: var(--text-disabled);
  background: var(--bg-card-hover);
  border-radius: 4px;
  flex-shrink: 0;
  transition: background-color 0.3s;

  &.gold {
    background: linear-gradient(135deg, #fbbf24, #f59e0b);
    color: #18181b;
  }

  &.silver {
    background: linear-gradient(135deg, #9ca3af, #6b7280);
    color: #18181b;
  }

  &.bronze {
    background: linear-gradient(135deg, #d97706, #b45309);
    color: #18181b;
  }
}

.hot-title {
  flex: 1;
  font-size: 13px;
  color: var(--text-secondary);
  text-decoration: none;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.2s;

  &:hover {
    color: var(--text-primary);
  }
}
</style>
