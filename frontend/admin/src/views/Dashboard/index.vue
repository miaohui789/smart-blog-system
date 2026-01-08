<template>
  <div class="dashboard">
    <!-- 今日数据概览 -->
    <div class="today-overview">
      <div class="overview-item">
        <div class="overview-icon new-article">
          <el-icon><DocumentAdd /></el-icon>
        </div>
        <div class="overview-info">
          <span class="overview-value">{{ todayStats.newArticles || 0 }}</span>
          <span class="overview-label">今日新增文章</span>
        </div>
      </div>
      <div class="overview-item">
        <div class="overview-icon new-comment">
          <el-icon><ChatLineSquare /></el-icon>
        </div>
        <div class="overview-info">
          <span class="overview-value">{{ todayStats.newComments || 0 }}</span>
          <span class="overview-label">今日新增评论</span>
        </div>
      </div>
      <div class="overview-item">
        <div class="overview-icon new-user">
          <el-icon><UserFilled /></el-icon>
        </div>
        <div class="overview-info">
          <span class="overview-value">{{ todayStats.newUsers || 0 }}</span>
          <span class="overview-label">今日新增用户</span>
        </div>
      </div>
      <div class="overview-item">
        <div class="overview-icon pending">
          <el-icon><Bell /></el-icon>
        </div>
        <div class="overview-info">
          <span class="overview-value warning">{{ extendedStats.pendingCommentCount || 0 }}</span>
          <span class="overview-label">待审核评论</span>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card" v-for="stat in statsCards" :key="stat.key">
        <div class="stat-icon" :class="stat.iconClass">
          <el-icon><component :is="stat.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ formatNumber(extendedStats[stat.key] || 0) }}</span>
          <span class="stat-label">{{ stat.label }}</span>
        </div>
      </div>
    </div>

    <!-- 图表区域第一行 -->
    <div class="charts-row">
      <div class="chart-card large">
        <h3 class="card-title">
          <span>访问趋势</span>
          <span class="card-subtitle">最近7天</span>
        </h3>
        <LineChart :data="visitTrend" />
      </div>
      <div class="chart-card">
        <h3 class="card-title">文章状态分布</h3>
        <PieChart :data="articleStatus" />
      </div>
    </div>

    <!-- 图表区域第二行 -->
    <div class="charts-row">
      <div class="chart-card">
        <h3 class="card-title">分类文章统计</h3>
        <BarChart :data="categoryStats" :horizontal="true" />
      </div>
      <div class="chart-card">
        <h3 class="card-title">评论状态分布</h3>
        <PieChart :data="commentStatus" />
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="content-row">
      <div class="list-card">
        <h3 class="card-title">
          <span>热门文章</span>
          <router-link to="/article/list" class="more-link">查看更多</router-link>
        </h3>
        <div class="article-list">
          <div v-for="(item, index) in hotArticles" :key="item.id" class="article-item">
            <span class="rank" :class="{ top: index < 3 }">{{ index + 1 }}</span>
            <div class="article-info">
              <span class="title">{{ item.title }}</span>
              <div class="article-meta">
                <span><el-icon><View /></el-icon> {{ item.viewCount }}</span>
                <span><el-icon><Star /></el-icon> {{ item.likeCount }}</span>
              </div>
            </div>
          </div>
          <div v-if="!hotArticles.length" class="empty">暂无数据</div>
        </div>
      </div>
      <div class="list-card">
        <h3 class="card-title">
          <span>最新评论</span>
          <router-link to="/comment" class="more-link">查看更多</router-link>
        </h3>
        <div class="comment-list">
          <div v-for="item in recentComments" :key="item.id" class="comment-item">
            <el-avatar :size="32" :src="item.avatar">{{ item.nickname?.charAt(0) }}</el-avatar>
            <div class="comment-content">
              <div class="comment-header">
                <span class="nickname">{{ item.nickname }}</span>
                <span class="time">{{ formatTime(item.createTime) }}</span>
              </div>
              <p class="content">{{ item.content }}</p>
            </div>
          </div>
          <div v-if="!recentComments.length" class="empty">暂无数据</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Document, ChatDotRound, User, View, Star, DocumentAdd, ChatLineSquare, UserFilled, Bell, Folder, PriceTag, Collection } from '@element-plus/icons-vue'
import { getStatsExtended, getVisitTrend, getCategoryStats, getArticleStatus, getCommentStatus, getHotArticles, getRecentComments, getTodayStats } from '@/api/dashboard'
import LineChart from '@/components/Charts/LineChart.vue'
import PieChart from '@/components/Charts/PieChart.vue'
import BarChart from '@/components/Charts/BarChart.vue'

const extendedStats = ref({})
const todayStats = ref({})
const visitTrend = ref({ xAxis: [], series: [] })
const categoryStats = ref({ xAxis: [], series: [] })
const articleStatus = ref([])
const commentStatus = ref([])
const hotArticles = ref([])
const recentComments = ref([])

const statsCards = [
  { key: 'articleCount', label: '文章总数', icon: Document, iconClass: 'article' },
  { key: 'commentCount', label: '评论总数', icon: ChatDotRound, iconClass: 'comment' },
  { key: 'userCount', label: '用户总数', icon: User, iconClass: 'user' },
  { key: 'viewCount', label: '总浏览量', icon: View, iconClass: 'view' },
  { key: 'likeCount', label: '总点赞数', icon: Star, iconClass: 'like' },
  { key: 'favoriteCount', label: '总收藏数', icon: Collection, iconClass: 'favorite' },
  { key: 'categoryCount', label: '分类数', icon: Folder, iconClass: 'category' },
  { key: 'tagCount', label: '标签数', icon: PriceTag, iconClass: 'tag' }
]

function formatNumber(num) {
  if (num >= 10000) return (num / 10000).toFixed(1) + 'w'
  if (num >= 1000) return (num / 1000).toFixed(1) + 'k'
  return num
}

function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  return date.toLocaleDateString()
}

onMounted(async () => {
  try {
    const [statsRes, todayRes, visitRes, categoryRes, articleStatusRes, commentStatusRes, hotRes, commentsRes] = await Promise.all([
      getStatsExtended(), getTodayStats(), getVisitTrend(), getCategoryStats(),
      getArticleStatus(), getCommentStatus(), getHotArticles(), getRecentComments()
    ])
    extendedStats.value = statsRes.data || {}
    todayStats.value = todayRes.data || {}
    visitTrend.value = visitRes.data || {}
    const catData = categoryRes.data || []
    categoryStats.value = { xAxis: catData.map(c => c.name), series: catData.map(c => c.value) }
    articleStatus.value = articleStatusRes.data || []
    commentStatus.value = commentStatusRes.data || []
    hotArticles.value = hotRes.data || []
    recentComments.value = commentsRes.data || []
  } catch (e) { console.error(e) }
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.dashboard { padding: 0; }

.today-overview {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $spacing-md;
  margin-bottom: $spacing-lg;
  @media (max-width: 1200px) { grid-template-columns: repeat(2, 1fr); }
  @media (max-width: 600px) { grid-template-columns: 1fr; }
}

.overview-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  background: linear-gradient(135deg, var(--bg-card) 0%, var(--bg-darker) 100%);
  border: 1px solid var(--border-color);
  border-radius: $radius-lg;
  padding: $spacing-md $spacing-lg;
}

.overview-icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: $radius-md;
  font-size: 22px;
  &.new-article { background: rgba(#22c55e, 0.15); color: #22c55e; }
  &.new-comment { background: rgba(#3b82f6, 0.15); color: #3b82f6; }
  &.new-user { background: rgba(#a855f7, 0.15); color: #a855f7; }
  &.pending { background: rgba(#f59e0b, 0.15); color: #f59e0b; }
}

.overview-info { display: flex; flex-direction: column; }
.overview-value { font-size: 22px; font-weight: 700; color: var(--text-primary); &.warning { color: #f59e0b; } }
.overview-label { font-size: 12px; color: var(--text-muted); }

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $spacing-md;
  margin-bottom: $spacing-xl;
  @media (max-width: 1200px) { grid-template-columns: repeat(2, 1fr); }
  @media (max-width: 600px) { grid-template-columns: 1fr; }
}

.stat-card {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: $radius-lg;
  padding: $spacing-lg;
  transition: all 0.3s;
  &:hover { transform: translateY(-2px); box-shadow: $shadow-md; }
}

.stat-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: $radius-md;
  font-size: 24px;
  flex-shrink: 0;
  &.article { background: rgba($primary-color, 0.1); color: $primary-color; }
  &.comment { background: rgba($success-color, 0.1); color: $success-color; }
  &.user { background: rgba($info-color, 0.1); color: $info-color; }
  &.view { background: rgba($warning-color, 0.1); color: $warning-color; }
  &.like { background: rgba(#ec4899, 0.1); color: #ec4899; }
  &.favorite { background: rgba(#f59e0b, 0.1); color: #f59e0b; }
  &.category { background: rgba(#06b6d4, 0.1); color: #06b6d4; }
  &.tag { background: rgba(#8b5cf6, 0.1); color: #8b5cf6; }
}

.stat-info { display: flex; flex-direction: column; flex: 1; }
.stat-value { font-size: 24px; font-weight: 700; color: var(--text-primary); }
.stat-label { font-size: 13px; color: var(--text-muted); }

.charts-row {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: $spacing-lg;
  margin-bottom: $spacing-lg;
  @media (max-width: 1200px) { grid-template-columns: 1fr; }
}

.chart-card, .list-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: $radius-lg;
  padding: $spacing-lg;
}

.card-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: $spacing-md;
  padding-bottom: $spacing-sm;
  border-bottom: 1px solid var(--border-color);
  .card-subtitle { font-size: 12px; font-weight: 400; color: var(--text-muted); }
  .more-link { font-size: 13px; font-weight: 400; color: var(--primary-color); text-decoration: none; &:hover { text-decoration: underline; } }
}

.content-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-lg;
  @media (max-width: 900px) { grid-template-columns: 1fr; }
}

.article-list { display: flex; flex-direction: column; gap: $spacing-sm; }

.article-item {
  display: flex;
  align-items: flex-start;
  gap: $spacing-sm;
  padding: $spacing-sm;
  border-radius: $radius-sm;
  transition: background-color 0.2s;
  &:hover { background: var(--bg-card-hover); }
  .rank {
    width: 24px;
    height: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--bg-input);
    border-radius: 4px;
    font-size: 12px;
    font-weight: 600;
    color: var(--text-muted);
    flex-shrink: 0;
    &.top { background: $primary-gradient; color: #fff; }
  }
  .article-info { flex: 1; min-width: 0; }
  .title { display: block; font-size: 14px; color: var(--text-secondary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-bottom: 4px; }
  .article-meta { display: flex; gap: $spacing-md; font-size: 12px; color: var(--text-muted); span { display: flex; align-items: center; gap: 2px; } }
}

.comment-list { display: flex; flex-direction: column; gap: $spacing-md; }
.comment-item { display: flex; gap: $spacing-sm; }
.comment-content { flex: 1; min-width: 0; }
.comment-header { display: flex; align-items: center; gap: $spacing-sm; margin-bottom: 4px; .nickname { font-size: 13px; font-weight: 500; color: var(--text-secondary); } .time { font-size: 12px; color: var(--text-muted); } }
.comment-content .content { font-size: 13px; color: var(--text-muted); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin: 0; }
.empty { text-align: center; padding: $spacing-lg; color: var(--text-muted); font-size: 14px; }
</style>
