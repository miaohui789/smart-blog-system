<template>
  <div class="home-page">
    <!-- 排序栏 -->
    <div class="sort-bar">
      <div class="sort-options">
        <button 
          v-for="option in sortOptions" 
          :key="option.value"
          class="sort-btn"
          :class="{ active: currentSort === option.value }"
          @click="handleSort(option.value)"
        >
          <el-icon v-if="option.icon"><component :is="option.icon" /></el-icon>
          <span>{{ option.label }}</span>
          <el-icon 
            v-if="currentSort === option.value && option.value !== 'latest' && option.value !== 'comprehensive'" 
            class="sort-arrow"
            :class="{ asc: sortOrder === 'asc' }"
          >
            <ArrowDown />
          </el-icon>
        </button>
      </div>
    </div>

    <!-- 置顶文章 -->
    <div 
      v-if="topArticle && currentPage === 1" 
      class="featured-article" 
      @click="goArticle(topArticle.id)"
    >
      <div class="featured-cover">
        <img :src="topArticle.cover || defaultCover" :alt="topArticle.title" @error="(e) => e.target.src = defaultCover" />
        <div class="featured-overlay"></div>
      </div>
      <div class="featured-content">
        <div class="featured-badge">
          <span>置顶</span>
        </div>
        <h2 class="featured-title">{{ topArticle.title }}</h2>
        <p class="featured-summary">{{ topArticle.summary }}</p>
        <div class="featured-meta">
          <span><el-icon><Calendar /></el-icon>{{ formatDate(topArticle.publishTime) }}</span>
          <span><el-icon><View /></el-icon>{{ topArticle.viewCount || 0 }}</span>
          <span><el-icon><Star /></el-icon>{{ topArticle.likeCount || 0 }}</span>
          <span><el-icon><CollectionTag /></el-icon>{{ topArticle.favoriteCount || 0 }}</span>
          <span><el-icon><ChatDotRound /></el-icon>{{ topArticle.commentCount || 0 }}</span>
        </div>
      </div>
    </div>

    <!-- 普通文章列表 -->
    <div class="article-section" v-if="!loading">
      <div class="section-header" v-if="topArticle && currentPage === 1 && currentSort === 'latest'">
        <h3>最新文章</h3>
      </div>
      <div class="normal-articles" v-if="normalArticles.length > 0">
        <ArticleCard
          v-for="article in normalArticles"
          :key="article.id"
          :article="article"
        />
      </div>
      <el-empty v-else-if="!loading && normalArticles.length === 0 && !topArticle" description="暂无文章" />
    </div>

    <Loading v-if="loading" />

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        :pager-count="5"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Calendar, View, ChatDotRound, Star, CollectionTag, ArrowDown, Sunny, ChatLineSquare, StarFilled, Collection, TrendCharts } from '@element-plus/icons-vue'
import { getArticleList } from '@/api/article'
import { formatDate } from '@/utils/format'
import ArticleCard from '@/components/ArticleCard/index.vue'
import Loading from '@/components/Loading/index.vue'

const router = useRouter()
const topArticle = ref(null)     // 置顶文章（只有1篇）
const normalArticles = ref([])   // 普通文章
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(8)          // 每页8篇普通文章
const total = ref(0)             // 普通文章总数
const currentSort = ref('latest')
const sortOrder = ref('desc')

// 排序选项
const sortOptions = [
  { value: 'latest', label: '最新', icon: null },
  { value: 'comprehensive', label: '综合', icon: TrendCharts },
  { value: 'heat', label: '热度', icon: Sunny },
  { value: 'view', label: '阅览', icon: View },
  { value: 'like', label: '点赞', icon: StarFilled },
  { value: 'favorite', label: '收藏', icon: Collection },
  { value: 'comment', label: '评论', icon: ChatLineSquare },
]

// 默认封面图
const defaultCover = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="800" height="400"%3E%3Cdefs%3E%3ClinearGradient id="g" x1="0%25" y1="0%25" x2="100%25" y2="100%25"%3E%3Cstop offset="0%25" stop-color="%23a855f7"/%3E%3Cstop offset="100%25" stop-color="%23ec4899"/%3E%3C/linearGradient%3E%3C/defs%3E%3Crect width="800" height="400" fill="url(%23g)"/%3E%3Ctext x="50%25" y="50%25" font-family="Arial" font-size="48" fill="white" text-anchor="middle" dy=".3em"%3E📝 Blog%3C/text%3E%3C/svg%3E'

function goArticle(id) {
  router.push(`/article/${id}`)
}

async function fetchArticles() {
  loading.value = true
  try {
    const res = await getArticleList({
      page: currentPage.value,
      pageSize: pageSize.value,
      sortBy: currentSort.value,
      sortOrder: sortOrder.value,
      excludeTop: 1  // 排除置顶文章
    })
    normalArticles.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

// 获取置顶文章（只获取1篇）
async function fetchTopArticles() {
  try {
    const res = await getArticleList({
      page: 1,
      pageSize: 1,  // 只获取1篇置顶文章
      onlyTop: 1
    })
    const list = res.data?.list || []
    topArticle.value = list.length > 0 ? list[0] : null
  } catch (e) {
    console.error(e)
  }
}

// 处理排序
function handleSort(sortValue) {
  if (currentSort.value === sortValue && sortValue !== 'latest' && sortValue !== 'comprehensive') {
    sortOrder.value = sortOrder.value === 'desc' ? 'asc' : 'desc'
  } else {
    currentSort.value = sortValue
    sortOrder.value = 'desc'
  }
  currentPage.value = 1
  fetchArticles()
}

function handlePageChange(page) {
  currentPage.value = page
  fetchArticles()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => {
  fetchTopArticles()
  fetchArticles()
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.home-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 文章区域 */
.article-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section-header {
  h3 {
    font-size: 18px;
    font-weight: 600;
    color: var(--text-primary);
    margin: 0;
    padding-left: 12px;
    border-left: 3px solid $primary-color;
  }
}

/* 排序栏 */
.sort-bar {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 12px 16px;
  transition: all 0.3s ease;
  
  // 自定义背景下强制透明
  body.has-custom-bg & {
    background: rgba(var(--bg-card-rgb), 0.65) !important;
    backdrop-filter: blur(12px);
    -webkit-backdrop-filter: blur(12px);
  }
}

.sort-options {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.sort-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: transparent;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  color: var(--text-secondary);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
  
  .el-icon {
    font-size: 16px;
  }
  
  .sort-arrow {
    font-size: 12px;
    transition: transform 0.2s ease;
    
    &.asc {
      transform: rotate(180deg);
    }
  }
  
  &:hover {
    background: var(--bg-hover);
    border-color: var(--primary-color);
    color: var(--primary-color);
  }
  
  &.active {
    background: rgba($primary-color, 0.1);
    border-color: var(--primary-color);
    color: var(--primary-color);
    font-weight: 500;
  }
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 置顶文章大卡片 */
.featured-article {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;

  &:hover {
    border-color: rgba($primary-color, 0.3);
    transform: translateY(-4px);
    box-shadow: 0 16px 32px var(--shadow-color);

    .featured-cover img {
      transform: scale(1.03);
    }
  }
}

.featured-cover {
  position: relative;
  height: 280px;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.6s ease;
  }

  .featured-overlay {
    position: absolute;
    inset: 0;
    background: linear-gradient(to top, rgba(10, 10, 15, 0.95) 0%, rgba(10, 10, 15, 0.5) 50%, transparent 100%);
  }
}

.featured-content {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 32px;
}

.featured-badge {
  margin-bottom: 12px;
  
  span {
    display: inline-block;
    padding: 4px 12px;
    background: $primary-color;
    border-radius: 6px;
    font-size: 12px;
    font-weight: 600;
    color: #fff;
  }
}

.featured-title {
  font-size: 28px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 12px;
  line-height: 1.3;
}

.featured-summary {
  font-size: 15px;
  color: #dfdfd6;
  line-height: 1.6;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.featured-meta {
  display: flex;
  gap: 20px;

  span {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: rgba(255, 255, 255, 0.5);
  }
}

/* 普通文章列表 */
.normal-articles {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;

  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.loading-wrapper {
  padding: 40px;
  background: var(--bg-card);
  border-radius: 16px;
  transition: background-color 0.3s;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

@media (max-width: 768px) {
  .sort-bar {
    padding: 10px 12px;
  }
  
  .sort-options {
    gap: 6px;
  }
  
  .sort-btn {
    padding: 6px 12px;
    font-size: 13px;
    
    span {
      display: none;
    }
    
    .el-icon:first-child {
      margin: 0;
    }
    
    &:first-child span {
      display: inline;
    }
  }

  .featured-cover {
    height: 200px;
  }

  .featured-content {
    padding: 20px;
  }

  .featured-title {
    font-size: 20px;
  }

  .featured-meta {
    gap: 12px;
  }
}
</style>
