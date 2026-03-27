<template>
  <div class="archive-page">
    <div class="archive-header glass-card">
      <h1 class="page-title">
        <el-icon><Calendar /></el-icon>
        文章归档
      </h1>
      <p class="archive-summary">共 <span class="highlight">{{ totalCount }}</span> 篇文章</p>
    </div>
    
    <Loading v-if="loading" />
    
    <div v-else class="timeline">
      <div v-for="(group, yearMonth) in archiveData" :key="yearMonth" class="timeline-group">
        <div class="timeline-header">
          <div class="timeline-dot"></div>
          <h2 class="timeline-title">{{ yearMonth }}</h2>
          <span class="timeline-count">{{ group.length }} 篇</span>
        </div>
        <div class="timeline-items">
          <router-link
            v-for="article in group"
            :key="article.id"
            :to="`/article/${article.id}`"
            class="timeline-item glass-card"
          >
            <span class="item-date">{{ formatDate(article.publishTime, 'MM-DD') }}</span>
            <span class="item-title">{{ article.title }}</span>
          </router-link>
        </div>
      </div>
    </div>
    
    <el-empty v-if="!loading && !Object.keys(archiveData).length" description="暂无文章" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Calendar } from '@element-plus/icons-vue'
import { getArticleArchive } from '@/api/article'
import { formatDate } from '@/utils/format'
import Loading from '@/components/Loading/index.vue'

const archiveData = ref({})
const loading = ref(false)

const totalCount = computed(() => {
  return Object.values(archiveData.value).reduce((sum, arr) => sum + arr.length, 0)
})

onMounted(async () => {
  loading.value = true
  try {
    const res = await getArticleArchive()
    archiveData.value = res.data || {}
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.archive-header {
  padding: $spacing-xl;
  margin-bottom: $spacing-xl;
  text-align: center;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: $radius-lg;
  transition: background-color 0.3s, border-color 0.3s;
}

.page-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-sm;
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: $spacing-sm;
  transition: color 0.3s;

  .el-icon {
    color: $primary-color;
  }
}

.archive-summary {
  color: var(--text-muted);
  font-size: 15px;
  transition: color 0.3s;

  .highlight {
    color: $primary-color;
    font-weight: 600;
  }
}

.timeline {
  position: relative;
  padding-left: 30px;

  &::before {
    content: '';
    position: absolute;
    left: 8px;
    top: 0;
    bottom: 0;
    width: 2px;
    background: linear-gradient(to bottom, $primary-color, rgba($primary-color, 0.2));
    border-radius: 1px;
  }
}

.timeline-group {
  margin-bottom: $spacing-xl;
}

.timeline-header {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  margin-bottom: $spacing-md;
  position: relative;
}

.timeline-dot {
  position: absolute;
  left: -26px;
  width: 14px;
  height: 14px;
  background: $primary-gradient;
  border-radius: 50%;
  box-shadow: 0 0 10px rgba($primary-color, 0.5);
}

.timeline-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  transition: color 0.3s;
}

.timeline-count {
  font-size: 13px;
  color: var(--text-muted);
  background: rgba($primary-color, 0.1);
  padding: 2px 10px;
  border-radius: 10px;
  transition: color 0.3s;
}

.timeline-items {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.timeline-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md $spacing-lg;
  text-decoration: none;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: $radius-lg;
  transition: all 0.3s;

  &:hover {
    transform: translateX(4px);
    
    .item-title {
      color: $primary-light;
    }
  }
}

.item-date {
  font-size: 13px;
  color: var(--text-muted);
  min-width: 50px;
  font-family: monospace;
  transition: color 0.3s;
}

.item-title {
  color: var(--text-secondary);
  transition: color 0.3s;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

// 响应式
@media (max-width: 768px) {
  .archive-header {
    padding: 20px;
    margin-bottom: 20px;
  }

  .timeline {
    padding-left: 24px;
    
    &::before {
      left: 6px;
    }
  }
  
  .timeline-dot {
    left: -23px;
    width: 10px;
    height: 10px;
  }
  
  .timeline-item {
    padding: 12px;
    gap: 10px;
  }
  
  .item-date {
    min-width: 42px;
  }
}
</style>
