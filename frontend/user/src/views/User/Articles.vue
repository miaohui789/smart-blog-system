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
                :type="article.status === 1 ? 'success' : 'info'" 
                size="small"
              >
                {{ article.status === 1 ? '已发布' : '草稿' }}
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
              <el-button text type="primary" @click="handleEdit(article.id)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button text type="danger" @click="handleDelete(article)">
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
import { Document, Edit, Delete, Calendar, View, ChatDotRound } from '@element-plus/icons-vue'
import { getMyArticles, deleteArticle } from '@/api/article'
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
    if (error !== 'cancel') {
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
  border-radius: $radius-lg;
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
  border-radius: $radius-lg;
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
  border-radius: $radius-md;
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
  gap: $spacing-sm;
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
    gap: $spacing-md;
    align-items: flex-start;
  }
}
</style>
