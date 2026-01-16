<template>
  <div class="vip-heat-page">
    <div class="page-header">
      <h2>加热记录</h2>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon total"><el-icon><Sunny /></el-icon></div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.totalHeats || 0 }}</span>
          <span class="stat-label">总加热次数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon today"><el-icon><Calendar /></el-icon></div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.todayHeats || 0 }}</span>
          <span class="stat-label">今日加热</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon value"><el-icon><DataLine /></el-icon></div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.totalHeatValue || 0 }}</span>
          <span class="stat-label">总加热值</span>
        </div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-select v-model="queryParams.userId" placeholder="选择用户" clearable filterable style="width: 180px">
        <el-option v-for="user in userOptions" :key="user.id" :label="user.nickname" :value="user.id" />
      </el-select>
      <el-select v-model="queryParams.articleId" placeholder="选择文章" clearable filterable style="width: 240px">
        <el-option v-for="article in articleOptions" :key="article.id" :label="article.title" :value="article.id" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 表格 -->
    <div class="table-card">
      <el-table :data="heatList" v-loading="loading">
        <el-table-column label="用户" min-width="160">
          <template #default="{ row }">
            <div class="user-info" v-if="row.user">
              <el-avatar :src="row.user.avatar" :size="36">{{ row.user.nickname?.charAt(0) }}</el-avatar>
              <span>{{ row.user.nickname }}</span>
            </div>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="文章" min-width="240">
          <template #default="{ row }">
            <span v-if="row.article" class="article-title">{{ row.article.title }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="加热值" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="danger">+{{ row.heatValue }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="加热时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchList"
          @current-change="fetchList"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Sunny, Calendar, DataLine } from '@element-plus/icons-vue'
import { getHeatList, getHeatStats } from '@/api/vip'
import { getUserList } from '@/api/user'
import { getArticleList } from '@/api/article'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const heatList = ref([])
const total = ref(0)
const stats = ref({})
const userOptions = ref([])
const articleOptions = ref([])

const queryParams = reactive({
  page: 1,
  size: 10,
  userId: null,
  articleId: null
})

onMounted(() => {
  fetchStats()
  fetchList()
  fetchUsers()
  fetchArticles()
})

const fetchStats = async () => {
  try {
    const res = await getHeatStats()
    if (res.code === 200) {
      stats.value = res.data || {}
    }
  } catch (e) { console.error(e) }
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getHeatList(queryParams)
    if (res.code === 200) {
      heatList.value = res.data?.list || []
      total.value = res.data?.total || 0
    }
  } finally {
    loading.value = false
  }
}

const fetchUsers = async () => {
  try {
    const res = await getUserList({ page: 1, pageSize: 1000 })
    userOptions.value = res.data?.list || []
  } catch (e) { console.error(e) }
}

const fetchArticles = async () => {
  try {
    const res = await getArticleList({ page: 1, pageSize: 1000 })
    articleOptions.value = res.data?.list || res.data?.records || []
  } catch (e) { console.error(e) }
}

const handleSearch = () => {
  queryParams.page = 1
  fetchList()
}

const handleReset = () => {
  queryParams.userId = null
  queryParams.articleId = null
  handleSearch()
}
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.vip-heat-page { padding: 20px; }

.page-header {
  margin-bottom: $spacing-lg;
  h2 { font-size: 20px; font-weight: 600; color: var(--text-primary); margin: 0; }
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-md;
  margin-bottom: $spacing-lg;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: $radius-lg;
  padding: $spacing-lg;
}

.stat-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: $radius-md;
  font-size: 24px;
  &.total { background: rgba(#ef4444, 0.1); color: #ef4444; }
  &.today { background: rgba($success-color, 0.1); color: $success-color; }
  &.value { background: rgba($primary-color, 0.1); color: $primary-color; }
}

.stat-info { display: flex; flex-direction: column; }
.stat-value { font-size: 24px; font-weight: 700; color: var(--text-primary); }
.stat-label { font-size: 13px; color: var(--text-muted); }

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.table-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: $radius-lg;
  padding: $spacing-lg;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.article-title {
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.text-muted { color: var(--text-muted); }

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-table) {
  background: transparent;
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: var(--bg-darker);
  --el-table-row-hover-bg-color: var(--bg-hover);
  --el-table-border-color: var(--border-color);
}

:deep(.el-input__wrapper),
:deep(.el-select__wrapper) {
  background: var(--bg-darker);
  box-shadow: none;
  border: 1px solid var(--border-color);
}
</style>
