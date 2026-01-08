<template>
  <div class="dashboard">
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon article">
          <el-icon><Document /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.articleCount }}</span>
          <span class="stat-label">文章数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon comment">
          <el-icon><ChatDotRound /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.commentCount }}</span>
          <span class="stat-label">评论数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon user">
          <el-icon><User /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.userCount }}</span>
          <span class="stat-label">用户数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon view">
          <el-icon><View /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.viewCount }}</span>
          <span class="stat-label">访问量</span>
        </div>
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-card">
        <h3 class="card-title">访问趋势</h3>
        <LineChart :data="visitTrend" />
      </div>
    </div>

    <div class="content-row">
      <div class="list-card">
        <h3 class="card-title">热门文章</h3>
        <div class="article-list">
          <div v-for="(item, index) in hotArticles" :key="item.id" class="article-item">
            <span class="rank" :class="{ top: index < 3 }">{{ index + 1 }}</span>
            <span class="title">{{ item.title }}</span>
            <span class="views">{{ item.viewCount }} 阅读</span>
          </div>
          <div v-if="!hotArticles.length" class="empty">暂无数据</div>
        </div>
      </div>
      <div class="list-card">
        <h3 class="card-title">最新评论</h3>
        <div class="comment-list">
          <div v-for="item in recentComments" :key="item.id" class="comment-item">
            <el-avatar :size="32" :src="item.avatar">{{ item.nickname?.charAt(0) }}</el-avatar>
            <div class="comment-content">
              <div class="comment-header">
                <span class="nickname">{{ item.nickname }}</span>
                <span class="time">{{ item.createTime }}</span>
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
import { Document, ChatDotRound, User, View } from '@element-plus/icons-vue'
import { getStats, getVisitTrend, getHotArticles, getRecentComments } from '@/api/dashboard'
import LineChart from '@/components/Charts/LineChart.vue'

const stats = ref({ articleCount: 0, commentCount: 0, userCount: 0, viewCount: 0 })
const visitTrend = ref({ xAxis: [], series: [] })
const hotArticles = ref([])
const recentComments = ref([])

onMounted(async () => {
  try {
    const [statsRes, trendRes, articlesRes, commentsRes] = await Promise.all([
      getStats(),
      getVisitTrend(),
      getHotArticles(),
      getRecentComments()
    ])
    stats.value = statsRes.data || {}
    visitTrend.value = trendRes.data || {}
    hotArticles.value = articlesRes.data || []
    recentComments.value = commentsRes.data || []
  } catch (e) {
    console.error(e)
  }
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $spacing-lg;
  margin-bottom: $spacing-xl;

  @media (max-width: 1200px) {
    grid-template-columns: repeat(2, 1fr);
  }
  
  @media (max-width: 600px) {
    grid-template-columns: 1fr;
  }
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
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: $shadow-md;
  }
}

.stat-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: $radius-md;
  font-size: 24px;
  
  &.article {
    background: rgba($primary-color, 0.1);
    color: $primary-color;
  }
  
  &.comment {
    background: rgba($success-color, 0.1);
    color: $success-color;
  }
  
  &.user {
    background: rgba($info-color, 0.1);
    color: $info-color;
  }
  
  &.view {
    background: rgba($warning-color, 0.1);
    color: $warning-color;
  }
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  transition: color 0.3s;
}

.stat-label {
  font-size: 13px;
  color: var(--text-muted);
  transition: color 0.3s;
}

.charts-row {
  margin-bottom: $spacing-xl;
}

.chart-card,
.list-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: $radius-lg;
  padding: $spacing-lg;
  transition: background-color 0.3s, border-color 0.3s;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: $spacing-md;
  padding-bottom: $spacing-sm;
  border-bottom: 1px solid var(--border-color);
  transition: color 0.3s, border-color 0.3s;
}

.content-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-lg;
  
  @media (max-width: 900px) {
    grid-template-columns: 1fr;
  }
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.article-item {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-sm;
  border-radius: $radius-sm;
  transition: background-color 0.2s;
  
  &:hover {
    background: var(--bg-card-hover);
  }
  
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
    
    &.top {
      background: $primary-gradient;
      color: #fff;
    }
  }
  
  .title {
    flex: 1;
    font-size: 14px;
    color: var(--text-secondary);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  
  .views {
    font-size: 12px;
    color: var(--text-muted);
  }
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.comment-item {
  display: flex;
  gap: $spacing-sm;
}

.comment-content {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  margin-bottom: 4px;
  
  .nickname {
    font-size: 13px;
    font-weight: 500;
    color: var(--text-secondary);
  }
  
  .time {
    font-size: 12px;
    color: var(--text-muted);
  }
}

.comment-content .content {
  font-size: 13px;
  color: var(--text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.empty {
  text-align: center;
  padding: $spacing-lg;
  color: var(--text-muted);
  font-size: 14px;
}
</style>
