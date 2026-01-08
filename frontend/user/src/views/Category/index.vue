<template>
  <div class="category-page">
    <h1 class="page-title">
      <el-icon><Folder /></el-icon>
      文章分类
    </h1>
    <Loading v-if="loading" />
    <div v-else class="category-list">
      <router-link
        v-for="category in categories"
        :key="category.id"
        :to="`/category/${category.id}`"
        class="category-card glass-card"
      >
        <div class="category-icon-wrapper">
          <el-icon class="category-icon"><Folder /></el-icon>
        </div>
        <span class="category-name">{{ category.name }}</span>
        <span class="category-count">{{ category.articleCount || 0 }} 篇文章</span>
      </router-link>
    </div>
    <el-empty v-if="!loading && !categories.length" description="暂无分类" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Folder } from '@element-plus/icons-vue'
import { getCategoryList } from '@/api/category'
import Loading from '@/components/Loading/index.vue'

const categories = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const res = await getCategoryList()
    categories.value = res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.page-title {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  font-size: 26px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: $spacing-xl;
  transition: color 0.3s;

  .el-icon {
    color: $primary-color;
  }
}

.category-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: $spacing-lg;
}

.category-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-xl;
  text-decoration: none;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: $radius-lg;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    transform: translateY(-4px);
    box-shadow: $shadow-glow;

    .category-icon-wrapper {
      transform: scale(1.1);
      background: $primary-gradient;
    }

    .category-icon {
      color: white;
    }
  }
}

.category-icon-wrapper {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba($primary-color, 0.15);
  border-radius: 50%;
  transition: all 0.3s;
}

.category-icon {
  font-size: 28px;
  color: $primary-color;
  transition: color 0.3s;
}

.category-name {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
  transition: color 0.3s;
}

.category-count {
  font-size: 13px;
  color: var(--text-muted);
  transition: color 0.3s;
}
</style>
