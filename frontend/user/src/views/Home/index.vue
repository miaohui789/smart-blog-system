<template>
  <div class="home-page">
    <!-- 文章列表 -->
    <div class="article-list">
      <template v-if="!loading && articles.length">
        <!-- 置顶/推荐文章 - 大卡片 -->
        <div v-if="featuredArticle" class="featured-article" @click="goArticle(featuredArticle.id)">
          <div class="featured-cover">
            <img v-if="featuredArticle.cover" :src="featuredArticle.cover" :alt="featuredArticle.title" />
            <div class="featured-overlay"></div>
          </div>
          <div class="featured-content">
            <div class="featured-badge" v-if="featuredArticle.isTop">
              <span>置顶</span>
            </div>
            <h2 class="featured-title">{{ featuredArticle.title }}</h2>
            <p class="featured-summary">{{ featuredArticle.summary }}</p>
            <div class="featured-meta">
              <span><el-icon><Calendar /></el-icon>{{ formatDate(featuredArticle.publishTime) }}</span>
              <span><el-icon><View /></el-icon>{{ featuredArticle.viewCount }}</span>
              <span><el-icon><ChatDotRound /></el-icon>{{ featuredArticle.commentCount }}</span>
            </div>
          </div>
        </div>

        <!-- 普通文章列表 -->
        <div class="normal-articles">
          <ArticleCard
            v-for="article in normalArticles"
            :key="article.id"
            :article="article"
          />
        </div>
      </template>
    </div>

    <Loading v-if="loading" />

    <el-empty v-if="!loading && !articles.length" description="暂无文章" />

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

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Calendar, View, ChatDotRound } from '@element-plus/icons-vue'
import { getArticleList } from '@/api/article'
import { formatDate } from '@/utils/format'
import ArticleCard from '@/components/ArticleCard/index.vue'
import Loading from '@/components/Loading/index.vue'

const router = useRouter()
const articles = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 置顶/推荐文章（第一篇）
const featuredArticle = computed(() => {
  if (currentPage.value === 1 && articles.value.length > 0) {
    return articles.value[0]
  }
  return null
})

// 普通文章（除第一篇外）
const normalArticles = computed(() => {
  if (currentPage.value === 1 && articles.value.length > 1) {
    return articles.value.slice(1)
  }
  return articles.value
})

function goArticle(id) {
  router.push(`/article/${id}`)
}

async function fetchArticles() {
  loading.value = true
  try {
    const res = await getArticleList({
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

onMounted(() => {
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
