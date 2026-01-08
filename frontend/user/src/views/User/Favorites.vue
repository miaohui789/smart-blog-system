<template>
  <div class="favorites-page">
    <h1 class="page-title">
      <el-icon><Star /></el-icon>
      我的收藏
    </h1>
    <Loading v-if="loading" />
    <template v-else>
      <div class="article-list">
        <ArticleCard v-for="article in articles" :key="article.id" :article="article" />
      </div>
      <el-empty v-if="!articles.length" description="暂无收藏文章">
        <template #image>
          <el-icon :size="60" color="#71717a"><Star /></el-icon>
        </template>
        <el-button type="primary" @click="$router.push('/')">去发现好文章</el-button>
      </el-empty>
      <el-pagination
        v-if="total > pageSize"
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Star } from '@element-plus/icons-vue'
import { getFavorites } from '@/api/user'
import ArticleCard from '@/components/ArticleCard/index.vue'
import Loading from '@/components/Loading/index.vue'

const articles = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

async function fetchFavorites() {
  loading.value = true
  try {
    const res = await getFavorites({ page: currentPage.value, pageSize: pageSize.value })
    articles.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handlePageChange(page) {
  currentPage.value = page
  fetchFavorites()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(fetchFavorites)
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

.article-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
  margin-bottom: $spacing-xl;
}

:deep(.el-pagination) {
  justify-content: center;
}
</style>
