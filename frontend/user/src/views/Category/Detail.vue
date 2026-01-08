<template>
  <div class="category-detail">
    <!-- 高端大气的头部区域 -->
    <div class="hero-header">
      <div class="hero-bg"></div>
      <div class="hero-content">
        <div class="icon-wrapper">
          <el-icon class="header-icon"><Folder /></el-icon>
        </div>
        <h1 class="page-title">{{ categoryName || '分类' }}</h1>
        <p class="page-desc">探索 {{ categoryName }} 相关的精选文章</p>
        <div class="stats-bar">
          <div class="stat-item">
            <span class="stat-value">{{ total }}</span>
            <span class="stat-label">篇文章</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-value">{{ currentPage }}</span>
            <span class="stat-label">/ {{ Math.ceil(total / pageSize) || 1 }} 页</span>
          </div>
        </div>
      </div>
    </div>
    
    <Loading v-if="loading" />
    
    <template v-else>
      <div class="content-section">
        <div class="section-header">
          <h2 class="section-title">
            <el-icon><Document /></el-icon>
            文章列表
          </h2>
          <span class="article-count">共 {{ total }} 篇</span>
        </div>
        
        <div class="article-grid">
          <ArticleCard v-for="article in articles" :key="article.id" :article="article" />
        </div>
        
        <el-empty v-if="!articles.length" description="该分类下暂无文章" />
        
        <div class="pagination-wrapper" v-if="total > pageSize">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Folder, Document } from '@element-plus/icons-vue'
import { getCategoryArticles, getCategoryList } from '@/api/category'
import ArticleCard from '@/components/ArticleCard/index.vue'
import Loading from '@/components/Loading/index.vue'

const route = useRoute()
const categoryName = ref('')
const articles = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

async function fetchArticles() {
  loading.value = true
  try {
    // 获取分类名称
    const categoryRes = await getCategoryList()
    const category = categoryRes.data?.find(c => c.id == route.params.id)
    categoryName.value = category?.name || '分类'
    
    // 获取文章列表
    const res = await getCategoryArticles(route.params.id, {
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
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.category-detail {
  min-height: 100vh;
}

// 高端大气的头部区域
.hero-header {
  position: relative;
  padding: 50px 30px;
  margin-bottom: $spacing-xl;
  border-radius: $radius-lg;
  overflow: hidden;
  text-align: center;
}

.hero-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, 
    rgba($primary-color, 0.2) 0%, 
    rgba(#6366f1, 0.15) 50%,
    rgba($primary-color, 0.1) 100%);
  backdrop-filter: blur(20px);
  border: 1px solid $border-color;
  border-radius: $radius-lg;
  
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: 
      radial-gradient(circle at 20% 30%, rgba($primary-color, 0.3) 0%, transparent 50%),
      radial-gradient(circle at 80% 70%, rgba(#6366f1, 0.25) 0%, transparent 50%);
  }
}

.hero-content {
  position: relative;
  z-index: 1;
}

.icon-wrapper {
  width: 70px;
  height: 70px;
  margin: 0 auto $spacing-md;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $primary-gradient;
  border-radius: 18px;
  box-shadow: 0 10px 40px rgba($primary-color, 0.4);
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

.header-icon {
  font-size: 32px;
  color: white;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: $text-primary;
  margin-bottom: $spacing-xs;
}

.page-desc {
  color: $text-secondary;
  font-size: 14px;
  margin-bottom: $spacing-lg;
}

.stats-bar {
  display: inline-flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-sm $spacing-lg;
  background: rgba(white, 0.05);
  border: 1px solid rgba(white, 0.1);
  border-radius: 50px;
  backdrop-filter: blur(10px);
}

.stat-item {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: $primary-color;
}

.stat-label {
  font-size: 13px;
  color: $text-muted;
}

.stat-divider {
  width: 1px;
  height: 20px;
  background: rgba(white, 0.15);
}

// 内容区域
.content-section {
  background: rgba(white, 0.02);
  border: 1px solid $border-color;
  border-radius: $radius-lg;
  padding: $spacing-lg;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-lg;
  padding-bottom: $spacing-md;
  border-bottom: 1px solid $border-color;
}

.section-title {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  font-size: 18px;
  font-weight: 600;
  color: $text-primary;
  
  .el-icon {
    color: $primary-color;
  }
}

.article-count {
  font-size: 13px;
  color: $text-muted;
  padding: 4px 12px;
  background: rgba($primary-color, 0.1);
  border-radius: 20px;
}

.article-grid {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding-top: $spacing-lg;
  margin-top: $spacing-lg;
  border-top: 1px solid $border-color;
}

:deep(.el-pagination) {
  --el-pagination-bg-color: transparent;
  --el-pagination-button-bg-color: rgba(white, 0.05);
  --el-pagination-hover-color: #{$primary-color};
}

// 响应式
@media (max-width: 768px) {
  .hero-header {
    padding: 30px 20px;
  }
  
  .icon-wrapper {
    width: 56px;
    height: 56px;
    border-radius: 14px;
  }
  
  .header-icon {
    font-size: 26px;
  }
  
  .page-title {
    font-size: 22px;
  }
  
  .stats-bar {
    padding: $spacing-xs $spacing-md;
    gap: $spacing-sm;
  }
  
  .stat-value {
    font-size: 18px;
  }
  
  .content-section {
    padding: $spacing-md;
  }
}
</style>
