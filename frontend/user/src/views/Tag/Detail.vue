<template>
  <div class="tag-detail">
    <div class="detail-header glass-card">
      <el-icon class="header-icon"><PriceTag /></el-icon>
      <h1 class="page-title"># {{ tagName || '标签' }}</h1>
      <p class="page-desc">共 {{ total }} 篇文章</p>
    </div>
    
    <Loading v-if="loading" />
    
    <template v-else>
      <div class="article-list">
        <ArticleCard v-for="article in articles" :key="article.id" :article="article" />
      </div>
      
      <el-empty v-if="!articles.length" description="该标签下暂无文章" />
      
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
import { ref, onMounted, onActivated, watch } from 'vue'
import { useRoute } from 'vue-router'
import { PriceTag } from '@element-plus/icons-vue'
import { getTagArticles, getTagList } from '@/api/tag'
import ArticleCard from '@/components/ArticleCard/index.vue'
import Loading from '@/components/Loading/index.vue'

const route = useRoute()
const tagName = ref('')
const articles = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

async function fetchArticles() {
  loading.value = true
  try {
    // 获取标签名称
    const tagRes = await getTagList()
    const tag = tagRes.data?.find(t => t.id == route.params.id)
    tagName.value = tag?.name || '标签'
    
    // 获取文章列表
    const res = await getTagArticles(route.params.id, {
      page: currentPage.value,
      pageSize: pageSize.value
    })
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
  fetchArticles()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

watch(() => route.params.id, () => {
  currentPage.value = 1
  fetchArticles()
})
onMounted(fetchArticles)
// keep-alive 激活时重新刷新（从文章详情返回后更新浏览量等数据）
onActivated(fetchArticles)
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.detail-header {
  padding: $spacing-xl;
  text-align: center;
  margin-bottom: $spacing-xl;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: $radius-lg;
  transition: background-color 0.3s, border-color 0.3s;
}

.header-icon {
  font-size: 48px;
  color: $primary-color;
  margin-bottom: $spacing-md;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  background: $primary-gradient;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: $spacing-sm;
}

.page-desc {
  color: var(--text-muted);
  font-size: 14px;
  transition: color 0.3s;
}

.article-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: $spacing-xl;
  
  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

:deep(.el-pagination) {
  justify-content: center;
}
</style>
