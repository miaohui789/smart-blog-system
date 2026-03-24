<template>
  <div class="home-page">
    <!-- 英雄区/欢迎介绍区 -->
    <div class="hero-banner">
      <div class="hero-content">
        <h1 class="hero-title flow-gradient">探索编程的无限可能</h1>
        <p class="hero-subtitle">分享技术心得，记录成长点滴。致力于打造高质量的开发者学习交流社区。</p>
      </div>
      <div class="hero-terminal">
        <div class="terminal-loader">
          <div class="terminal-header">
            <div class="terminal-title">System Initialize</div>
            <div class="terminal-controls">
              <div class="control close"></div>
              <div class="control minimize"></div>
              <div class="control maximize"></div>
            </div>
          </div>
          <div class="terminal-body">
            <div class="code-line"><span class="cmd">admin@smart-blog</span><span class="path">~ %</span> <span class="typing-text init">./start_system.sh</span></div>
            <div class="code-line delay-1">> Loading dependencies... <span class="success">[OK]</span></div>
            <div class="code-line delay-2">> Establishing secure connection... <span class="success">[OK]</span></div>
            <div class="code-line delay-3">> Initializing smart AI engine... <span class="success">[OK]</span></div>
            <div class="code-line delay-4 prompt">
              <span class="cmd">admin@smart-blog</span><span class="path">~ %</span> 
              <span class="text">Code Change World_</span>
            </div>
          </div>
        </div>
      </div>
    </div>

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
import { ref, computed, onMounted, onActivated } from 'vue'
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

// keep-alive 缓存激活时重新刷新数据（从文章详情返回后更新浏览量等数据）
onActivated(() => {
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
  // 暗色主题下进一步降低透明度，使背景可见
  :root[data-theme="dark"] body.has-custom-bg & {
    background: rgba(var(--bg-card-rgb), 0.25) !important;
    backdrop-filter: blur(14px);
    -webkit-backdrop-filter: blur(14px);
    border-color: rgba(255, 255, 255, 0.08) !important;
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

/* 英雄区样式 - 艺术感重塑 */
.hero-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 48px 56px;
  background: linear-gradient(145deg, var(--bg-card) 0%, rgba(var(--primary-color-rgb, 100, 108, 255), 0.03) 100%);
  border-radius: 20px;
  box-shadow: 0 10px 40px -10px rgba(0, 0, 0, 0.08), inset 0 1px 0 rgba(255, 255, 255, 0.05);
  margin-bottom: 24px;
  overflow: hidden;
  position: relative;
  transition: transform 0.4s cubic-bezier(0.165, 0.84, 0.44, 1), box-shadow 0.4s ease;
  border: 1px solid rgba(255, 255, 255, 0.05);

  &::before {
    content: '';
    position: absolute;
    top: -50%; left: -50%; width: 200%; height: 200%;
    background: radial-gradient(circle, rgba(var(--primary-color-rgb, 168, 85, 247), 0.05) 0%, transparent 60%);
    opacity: 0.5;
    pointer-events: none;
    z-index: 0;
  }

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 20px 40px -10px rgba(0, 0, 0, 0.12), inset 0 1px 0 rgba(255, 255, 255, 0.05);
  }

  .hero-content {
    flex: 1;
    z-index: 1;
    position: relative;
    
    @keyframes flowGradient {
      0%   { background-position: 0% center; }
      100% { background-position: -200% center; }
    }

    .hero-title {
      font-size: 46px;
      font-weight: 800;
      letter-spacing: -0.5px;
      margin: 0 0 20px;
      background: linear-gradient(90deg, #444 0%, #999 25%, #f0f0f0 50%, #999 75%, #444 100%);
      background-size: 200% auto;
      animation: flowGradient 5s linear infinite;
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      line-height: 1.2;
    }

    .hero-subtitle {
      font-size: 13px;
      color: var(--text-secondary);
      line-height: 1.8;
      margin: 0 0 10px;
      max-width: 480px;
      font-weight: 400;
      opacity: 0.75;
    }
  }

  .hero-terminal {
    flex-shrink: 0;
    margin-left: 40px;
    z-index: 1;
    perspective: 1000px;
    
    @keyframes blinkCursor {
      50% { border-right-color: transparent; }
    }
    
    @keyframes typeAndDelete {
      0%, 10% { width: 0; }
      45%, 55% { width: 11em; } 
      90%, 100% { width: 0; }
    }

    @keyframes simulateTyping {
      0% { width: 0; }
      100% { width: 100%; }
    }

    @keyframes fadeInCode {
      0% { opacity: 0; transform: translateY(5px); }
      100% { opacity: 1; transform: translateY(0); }
    }

    @keyframes floatTerminal {
      0%, 100% { transform: translateY(0) rotateX(5deg) rotateY(-5deg); }
      50% { transform: translateY(-10px) rotateX(8deg) rotateY(-3deg); }
    }

    .terminal-loader {
      --term-bg: rgba(20, 20, 25, 0.88);
      --term-border: rgba(255, 255, 255, 0.1);
      --term-header-bg: rgba(0, 0, 0, 0.3);
      --term-header-border: rgba(255, 255, 255, 0.05);
      --term-title-color: #7aa2f7;
      --term-body-color: #a9b1d6;
      --term-cmd-color: #9ece6a;
      --term-path-color: #bb9af7;
      --term-success-color: #7dcfff;
      --term-type-color: #c0caf5;
      --term-cursor-color: #7aa2f7;
      --term-shadow: 0 30px 60px -12px rgba(0, 0, 0, 0.6), 0 0 0 1px rgba(255,255,255,0.05) inset;
      --term-shine: rgba(255,255,255,0.2);

      border: 1px solid var(--term-border);
      background: var(--term-bg);
      backdrop-filter: blur(20px);
      color: var(--term-body-color);
      font-family: 'Fira Code', Consolas, "Courier New", monospace;
      font-size: 1.05em;
      width: 28em;
      min-height: 16em;
      box-shadow: var(--term-shadow);
      border-radius: 14px;
      position: relative;
      overflow: hidden;
      box-sizing: border-box;
      transform-style: preserve-3d;
      animation: floatTerminal 6s ease-in-out infinite;

      &::before {
        content: ''; position: absolute; top:0; left:0; right:0; height: 1px;
        background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
      }

      .terminal-header {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        height: 2.2em;
        background: var(--term-header-bg);
        border-bottom: 1px solid var(--term-header-border);
        padding: 0 1em;
        box-sizing: border-box;
        display: flex;
        justify-content: space-between;
        align-items: center;

        .terminal-title {
          line-height: 1.5em;
          color: var(--term-title-color);
          font-size: 0.7em;
          letter-spacing: 1.5px;
          text-transform: uppercase;
          font-weight: 600;
        }

        .terminal-controls {
          display: flex;
          gap: 6px;

          .control {
            display: inline-block;
            width: 10px;
            height: 10px;
            border-radius: 50%;
            transition: transform 0.2s ease;

            &.close { background-color: #ff5f56; box-shadow: 0 0 6px #ff5f5680; }
            &.minimize { background-color: #ffbd2e; box-shadow: 0 0 6px #ffbd2e80; }
            &.maximize { background-color: #27c93f; box-shadow: 0 0 6px #27c93f80; }
            
            &:hover {
              transform: scale(1.2);
            }
          }
        }
      }

      .terminal-body {
        padding: 3.5em 1.5em 1.5em;
        display: flex;
        flex-direction: column;
        gap: 8px;
        font-size: 0.9em;

        .code-line {
          opacity: 0;
          animation: fadeInCode 0.5s ease forwards;
          
          .cmd { color: var(--term-cmd-color); font-weight: bold; }
          .path { color: var(--term-path-color); }
          .success { color: var(--term-success-color); font-weight: bold; }
          
          &.delay-1 { animation-delay: 1s; }
          &.delay-2 { animation-delay: 1.5s; }
          &.delay-3 { animation-delay: 2s; }
          &.delay-4 { animation-delay: 2.8s; }

          .typing-text {
            display: inline-block;
            overflow: hidden;
            white-space: nowrap;
            vertical-align: bottom;
            color: var(--term-type-color);
            
            &.init {
              animation: simulateTyping 0.8s steps(20) forwards;
              animation-delay: 0.2s;
              width: 0; /* 起始隐藏 */
            }
          }

          &.prompt {
            margin-top: 8px;
            
            .text {
              display: inline-block;
              white-space: nowrap;
              overflow: hidden;
              border-right: 2px solid var(--term-cursor-color); 
              color: var(--term-cursor-color);
              animation: typeAndDelete 5s steps(18) infinite,
                         blinkCursor 0.8s step-end infinite alternate;
              font-weight: bold;
              letter-spacing: 0.5px;
              text-shadow: 0 0 8px rgba(122, 162, 247, 0.3);
              vertical-align: bottom;
              margin-left: 6px;
            }
          }
        }
      }
    }
  }

  @media (max-width: 768px) {
    flex-direction: column;
    padding: 32px 24px;
    
    .hero-terminal {
      margin-left: 0;
      margin-top: 32px;
      width: 100%;
      display: flex;
      justify-content: center;
      perspective: none;
      
      .terminal-loader {
        width: 100%;
        max-width: 24em; /* 移动端也稍微放大 */
        min-height: 14em;
        animation: none;
        transform: none !important;
      }
    }
    
    .hero-content {
      text-align: center;
      .hero-title {
        font-size: 36px;
      }
      .hero-subtitle {
        margin: 0 auto 10px;
        font-size: 16px;
      }
    }
  }
}

/* 亮色主题下终端样式覆盖 */
:root[data-theme="light"] .hero-banner .hero-terminal .terminal-loader {
  --term-bg: rgba(248, 250, 255, 0.95);
  --term-border: rgba(59, 130, 246, 0.2);
  --term-header-bg: rgba(226, 232, 240, 0.9);
  --term-header-border: rgba(0, 0, 0, 0.06);
  --term-title-color: #2563eb;
  --term-body-color: #334155;
  --term-cmd-color: #16a34a;
  --term-path-color: #7c3aed;
  --term-success-color: #0284c7;
  --term-type-color: #1e293b;
  --term-cursor-color: #2563eb;
  --term-shadow: 0 20px 40px -10px rgba(59, 130, 246, 0.15), 0 0 0 1px rgba(59,130,246,0.1) inset;
  --term-shine: rgba(59, 130, 246, 0.15);
  box-shadow: var(--term-shadow);
}
</style>
