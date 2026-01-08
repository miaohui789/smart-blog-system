<template>
  <div class="search-page">
    <div class="search-header glass-card">
      <h1 class="search-title">搜索文章</h1>
      <div class="search-input-wrapper">
        <el-input
          v-model="keyword"
          placeholder="输入关键词搜索..."
          size="large"
          :prefix-icon="Search"
          clearable
          @keyup.enter="handleSearch"
          @clear="handleClear"
        />
        <el-button type="primary" size="large" @click="handleSearch">
          搜索
        </el-button>
      </div>
    </div>

    <div v-if="loading" class="loading-wrapper">
      <Loading />
    </div>

    <div v-else-if="searched" class="search-result">
      <p class="result-summary">
        共找到 <span class="highlight">{{ total }}</span> 篇相关文章
      </p>
      <div class="article-list">
        <ArticleCard v-for="article in articles" :key="article.id" :article="article" />
      </div>
      <el-empty v-if="!articles.length" description="没有找到相关文章">
        <template #image>
          <el-icon :size="60" color="#71717a"><Search /></el-icon>
        </template>
      </el-empty>
      <el-pagination
        v-if="total > pageSize"
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>

    <div v-else class="search-tips glass-card">
      <el-icon :size="48" color="#71717a"><Search /></el-icon>
      <p>输入关键词开始搜索</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { searchArticles } from '@/api/article'
import ArticleCard from '@/components/ArticleCard/index.vue'
import Loading from '@/components/Loading/index.vue'

const route = useRoute()
const router = useRouter()

const keyword = ref('')
const articles = ref([])
const total = ref(0)
const searched = ref(false)
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)

async function handleSearch() {
  if (!keyword.value.trim()) return
  
  loading.value = true
  searched.value = true
  
  try {
    const res = await searchArticles({ 
      keyword: keyword.value,
      page: currentPage.value,
      pageSize: pageSize.value
    })
    articles.value = res.data?.list || res.data || []
    total.value = res.data?.total || articles.value.length
    
    // 更新 URL
    router.replace({ query: { keyword: keyword.value } })
  } catch (e) {
    console.error(e)
    articles.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleClear() {
  articles.value = []
  total.value = 0
  searched.value = false
  router.replace({ query: {} })
}

function handlePageChange(page) {
  currentPage.value = page
  handleSearch()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => {
  if (route.query.keyword) {
    keyword.value = route.query.keyword
    handleSearch()
  }
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.search-page {
  display: flex;
  flex-direction: column;
  gap: $spacing-xl;
}

.search-header {
  padding: $spacing-xl;
  text-align: center;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: $radius-lg;
  transition: background-color 0.3s, border-color 0.3s;
}

.search-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: $spacing-lg;
  transition: color 0.3s;
}

.search-input-wrapper {
  display: flex;
  gap: $spacing-md;
  max-width: 600px;
  margin: 0 auto;

  .el-input {
    flex: 1;
  }
}

.loading-wrapper {
  padding: $spacing-2xl;
  text-align: center;
}

.result-summary {
  color: var(--text-secondary);
  margin-bottom: $spacing-lg;
  font-size: 15px;
  transition: color 0.3s;

  .highlight {
    color: $primary-color;
    font-weight: 600;
  }
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
}

.search-tips {
  padding: $spacing-2xl;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-md;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: $radius-lg;
  transition: background-color 0.3s, border-color 0.3s;

  p {
    color: var(--text-muted);
    font-size: 15px;
    transition: color 0.3s;
  }
}

:deep(.el-pagination) {
  justify-content: center;
  margin-top: $spacing-xl;
}
</style>
