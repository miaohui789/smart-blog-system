<template>
  <div class="my-articles">
    <div class="page-header">
      <h1 class="page-title">
        <el-icon><Document /></el-icon>
        我的文章
      </h1>
      <router-link to="/write" class="write-btn">
        <el-button type="primary">
          <el-icon><Edit /></el-icon>
          写文章
        </el-button>
      </router-link>
    </div>

    <div class="filter-bar glass-card">
      <el-radio-group v-model="statusFilter" @change="handleFilterChange">
        <el-radio-button :label="null">全部</el-radio-button>
        <el-radio-button :label="1">已发布</el-radio-button>
        <el-radio-button :label="0">草稿</el-radio-button>
        <el-radio-button :label="2">已取消发布</el-radio-button>
      </el-radio-group>
    </div>

    <Loading v-if="loading" />

    <template v-else>
      <div class="article-list" v-if="articles.length">
        <div v-for="article in articles" :key="article.id" class="article-item glass-card">
          <div class="article-cover" v-if="article.cover">
            <img :src="article.cover" :alt="article.title" />
          </div>
          <div class="article-info">
            <div class="article-header">
              <router-link :to="`/article/${article.id}`" class="article-title">
                {{ article.title }}
              </router-link>
              <el-tag
                :type="getStatusTagType(article.status)"
                size="small"
              >
                {{ getStatusText(article.status) }}
              </el-tag>
            </div>
            <p class="article-summary">{{ article.summary || '暂无摘要' }}</p>
            <div class="article-meta">
              <span class="meta-item">
                <el-icon><Calendar /></el-icon>
                {{ formatDate(article.createTime) }}
              </span>
              <span class="meta-item" v-if="article.status === 1">
                <el-icon><View /></el-icon>
                {{ article.viewCount || 0 }} 阅读
              </span>
              <span class="meta-item" v-if="article.status === 1">
                <el-icon><ChatDotRound /></el-icon>
                {{ article.commentCount || 0 }} 评论
              </span>
            </div>
            <div class="article-actions">
              <el-button class="action-btn edit-btn" text type="primary" @click="handleEdit(article.id)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button
                v-if="article.status === 1 || article.status === 2"
                class="action-btn status-btn"
                text
                :type="article.status === 1 ? 'warning' : 'success'"
                @click="handleStatusChange(article)"
              >
                <el-icon>
                  <CloseBold v-if="article.status === 1" />
                  <Promotion v-else />
                </el-icon>
                {{ article.status === 1 ? '取消发布' : '重新发布' }}
              </el-button>
              <el-button class="action-btn delete-btn" text type="danger" @click="handleDelete(article)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <el-empty v-else description="暂无文章，快去写一篇吧~">
        <router-link to="/write">
          <el-button type="primary">写文章</el-button>
        </router-link>
      </el-empty>

      <el-pagination
        v-if="total > pageSize"
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="fetchArticles"
      />
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Edit, Delete, Calendar, View, ChatDotRound, CloseBold, Promotion } from '@element-plus/icons-vue'
import { getMyArticles, deleteArticle, updateMyArticleStatus } from '@/api/article'
import { formatDate } from '@/utils/format'
import Loading from '@/components/Loading/index.vue'

const router = useRouter()
const articles = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const statusFilter = ref(null)

async function fetchArticles() {
  loading.value = true
  try {
    const res = await getMyArticles({
      page: currentPage.value,
      pageSize: pageSize.value,
      status: statusFilter.value
    })
    articles.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (error) {
    ElMessage.error('获取文章列表失败')
  } finally {
    loading.value = false
  }
}

function handleFilterChange() {
  currentPage.value = 1
  fetchArticles()
}

function handleEdit(id) {
  router.push(`/write/${id}`)
}

function getStatusText(status) {
  if (status === 1) {
    return '已发布'
  }
  if (status === 2) {
    return '已取消发布'
  }
  return '草稿'
}

function getStatusTagType(status) {
  if (status === 1) {
    return 'success'
  }
  if (status === 2) {
    return 'warning'
  }
  return 'info'
}

async function handleStatusChange(article) {
  const isPublished = article.status === 1
  const targetStatus = isPublished ? 2 : 1
  const actionText = isPublished ? '取消发布' : '重新发布'
  const successText = isPublished ? '文章已取消发布，仅自己可见' : '文章已重新发布'

  try {
    await ElMessageBox.confirm(
      `确定要${actionText}文章「${article.title}」吗？`,
      `${actionText}确认`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: isPublished ? 'warning' : 'info'
      }
    )

    await updateMyArticleStatus(article.id, targetStatus)
    ElMessage.success(successText)
    fetchArticles()
  } catch (error) {
    if (!['cancel', 'close'].includes(error)) {
      ElMessage.error(`${actionText}失败`)
    }
  }
}

async function handleDelete(article) {
  try {
    await ElMessageBox.confirm(
      `确定要删除文章「${article.title}」吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteArticle(article.id)
    ElMessage.success('删除成功')
    fetchArticles()
  } catch (error) {
    if (!['cancel', 'close'].includes(error)) {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(fetchArticles)
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.my-articles {
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-xl;
  padding: $spacing-lg;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: $radius-xl;
  transition: all 0.3s;
}

.page-title {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  transition: color 0.3s;
  
  .el-icon {
    color: $primary-color;
  }
}

.filter-bar {
  padding: $spacing-md;
  margin-bottom: $spacing-lg;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: $radius-xl;
  transition: background-color 0.3s, border-color 0.3s;
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  margin-bottom: $spacing-xl;
}

.article-item {
  display: flex;
  gap: $spacing-lg;
  padding: $spacing-lg;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: $radius-xl;
  transition: all 0.3s;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: $shadow-glow;
  }
}

.article-cover {
  width: 180px;
  height: 120px;
  flex-shrink: 0;
  border-radius: $radius-lg;
  overflow: hidden;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.article-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.article-header {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  margin-bottom: $spacing-sm;
}

.article-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  text-decoration: none;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.3s;
  
  &:hover {
    color: $primary-color;
  }
}

.article-summary {
  color: var(--text-muted);
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: $spacing-sm;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  transition: color 0.3s;
}

.article-meta {
  display: flex;
  gap: $spacing-lg;
  color: var(--text-muted);
  font-size: 13px;
  margin-bottom: $spacing-sm;
  transition: color 0.3s;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.article-actions {
  margin-top: auto;
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-sm;
}

.article-actions :deep(.el-button) {
  margin-left: 0;
}

.article-actions :deep(.action-btn) {
  --action-accent: #{$primary-color};
  --action-accent-bg: rgba(59, 130, 246, 0.12);
  --action-accent-shadow: rgba(59, 130, 246, 0.18);
  height: 38px;
  padding: 0 16px;
  border-radius: 999px;
  border: 1px solid color-mix(in srgb, var(--action-accent) 24%, var(--border-color));
  background: color-mix(in srgb, var(--action-accent-bg) 55%, rgba(var(--bg-card-rgb), 0.92));
  color: var(--action-accent);
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.01em;
  box-shadow: 0 10px 24px -18px var(--action-accent-shadow);
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease,
    background-color 0.2s ease, color 0.2s ease;

  .el-icon {
    font-size: 14px;
    margin-right: 4px;
  }

  &:hover {
    transform: translateY(-1px);
    border-color: color-mix(in srgb, var(--action-accent) 45%, var(--border-color));
    background: color-mix(in srgb, var(--action-accent-bg) 80%, rgba(var(--bg-card-rgb), 0.98));
    box-shadow: 0 14px 30px -18px var(--action-accent-shadow);
  }

  &:active {
    transform: translateY(0);
    box-shadow: 0 8px 18px -16px var(--action-accent-shadow);
  }
}

.article-actions :deep(.edit-btn) {
  --action-accent: #{$primary-color};
  --action-accent-bg: rgba(59, 130, 246, 0.12);
  --action-accent-shadow: rgba(59, 130, 246, 0.22);
}

.article-actions :deep(.status-btn.el-button--warning) {
  --action-accent: #{$warning-color};
  --action-accent-bg: rgba(245, 158, 11, 0.14);
  --action-accent-shadow: rgba(245, 158, 11, 0.24);
}

.article-actions :deep(.status-btn.el-button--success) {
  --action-accent: #{$success-color};
  --action-accent-bg: rgba(34, 197, 94, 0.14);
  --action-accent-shadow: rgba(34, 197, 94, 0.24);
}

.article-actions :deep(.delete-btn) {
  --action-accent: #{$error-color};
  --action-accent-bg: rgba(239, 68, 68, 0.14);
  --action-accent-shadow: rgba(239, 68, 68, 0.22);
}

:root[data-theme="light"] .article-actions :deep(.action-btn) {
  background: color-mix(in srgb, var(--action-accent-bg) 92%, #ffffff);
  box-shadow: 0 10px 24px -20px var(--action-accent-shadow);
}

:deep(.el-pagination) {
  justify-content: center;
}

// 响应式
@media (max-width: 768px) {
  .article-item {
    flex-direction: column;
  }
  
  .article-cover {
    width: 100%;
    height: 160px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
    
    .write-btn {
      width: 100%;
      display: block;
      
      .el-button {
        width: 100%;
      }
    }
  }
  
  .filter-bar {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
    
    &::-webkit-scrollbar {
      display: none;
    }
    scrollbar-width: none;
    
    :deep(.el-radio-group) {
      display: flex;
      flex-wrap: nowrap;
    }
  }
}
</style>
