<template>
  <div class="tag-page">
    <div class="page-header">
      <h1 class="page-title">
        <el-icon><PriceTag /></el-icon>
        文章标签
      </h1>
      <p class="page-desc">共 {{ tags.length }} 个标签</p>
    </div>

    <Loading v-if="loading" />

    <div v-else class="tag-grid">
      <router-link
        v-for="tag in tags"
        :key="tag.id"
        :to="`/tag/${tag.id}`"
        class="tag-card"
        :style="{ '--tag-color': tag.color || '#4a9eff' }"
      >
        <div class="tag-icon">
          <el-icon><PriceTag /></el-icon>
        </div>
        <div class="tag-info">
          <span class="tag-name">{{ tag.name }}</span>
          <span class="tag-count">{{ tag.articleCount || 0 }} 篇文章</span>
        </div>
        <div class="tag-arrow">
          <el-icon><ArrowRight /></el-icon>
        </div>
      </router-link>
    </div>

    <el-empty v-if="!loading && !tags.length" description="暂无标签" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { PriceTag, ArrowRight } from '@element-plus/icons-vue'
import { getTagList } from '@/api/tag'
import Loading from '@/components/Loading/index.vue'

const tags = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const res = await getTagList()
    tags.value = res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
})
</script>

<style lang="scss" scoped>
.tag-page {
  max-width: 100%;
}

.page-header {
  margin-bottom: 32px;
  padding: 24px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 20px;
  transition: all 0.3s;
  
  // 自定义背景下强制透明
  body.has-custom-bg & {
    background: rgba(var(--bg-card-rgb), 0.65) !important;
    backdrop-filter: blur(12px);
    -webkit-backdrop-filter: blur(12px);
  }
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
  transition: color 0.3s;

  .el-icon {
    color: var(--primary-color);
  }
}

.page-desc {
  color: var(--text-muted);
  font-size: 14px;
  margin-left: 40px;
  transition: color 0.3s;
}

.loading-wrapper {
  padding: 40px;
  background: var(--bg-card);
  border-radius: 16px;
  transition: background-color 0.3s;
}

.tag-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}

.tag-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 20px;
  text-decoration: none;
  transition: all 0.3s ease;

  // 自定义背景下强制透明
  body.has-custom-bg & {
    background: rgba(var(--bg-card-rgb), 0.65) !important;
    backdrop-filter: blur(12px);
    -webkit-backdrop-filter: blur(12px);
  }

  &:hover {
    border-color: var(--tag-color);
    transform: translateY(-2px);
    box-shadow: 0 8px 24px var(--shadow-color);

    .tag-icon {
      background: var(--tag-color);
      color: #fff;
    }

    .tag-arrow {
      opacity: 1;
      transform: translateX(0);
    }
  }
}

.tag-icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-card-hover);
  border-radius: 14px;
  color: var(--tag-color);
  font-size: 20px;
  transition: all 0.3s;
}

.tag-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.tag-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  transition: color 0.3s;
}

.tag-count {
  font-size: 13px;
  color: var(--text-muted);
  transition: color 0.3s;
}

.tag-arrow {
  color: var(--text-muted);
  opacity: 0;
  transform: translateX(-8px);
  transition: all 0.3s;
}

@media (max-width: 768px) {
  .tag-grid {
    grid-template-columns: 1fr;
  }
}
</style>
