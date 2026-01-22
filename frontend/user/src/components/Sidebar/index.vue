<template>
  <div class="sidebar">
    <!-- 实时天气 -->
    <Weather />
    
    <div class="sidebar-card glass-card">
      <h3 class="card-title">
        <el-icon><TrendCharts /></el-icon>
        热门文章
        <router-link to="/archive" class="more-link">更多</router-link>
      </h3>
      <ul class="hot-list">
        <li v-for="(article, index) in displayedArticles" :key="article.id" class="hot-item">
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
        <router-link to="/tag" class="more-link">更多</router-link>
      </h3>
      <TagCloud :limit="20" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { TrendCharts, PriceTag } from '@element-plus/icons-vue'
import { getHotArticles } from '@/api/article'
import TagCloud from '@/components/TagCloud/index.vue'
import Weather from '@/components/Weather/index.vue'

const hotArticles = ref([])

// 限制最多显示8篇热门文章
const displayedArticles = computed(() => {
  return hotArticles.value.slice(0, 8)
})

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
  // 移除sticky定位，因为已在父容器设置
  // position: sticky;
  // top: 88px;
}

.sidebar-card {
  padding: $spacing-lg;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: $radius-lg;
  transition: all 0.2s ease;
  
  &:hover {
    border-color: rgba($primary-color, 0.2);
  }
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
  
  .el-icon {
    color: $primary-color;
  }
  
  .more-link {
    margin-left: auto;
    font-size: 12px;
    font-weight: 400;
    color: var(--text-muted);
    text-decoration: none;
    transition: color 0.2s;
    
    &:hover {
      color: $primary-color;
    }
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
  padding: 6px;
  border-radius: 8px;
  transition: background 0.2s ease;
  
  &:hover {
    background: rgba($primary-color, 0.05);
    
    .hot-title {
      color: $primary-color;
    }
  }
}

.hot-rank {
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 600;
  color: var(--text-disabled);
  background: var(--bg-card-hover);
  border-radius: 6px;
  flex-shrink: 0;

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
  transition: color 0.2s ease;
}
</style>
