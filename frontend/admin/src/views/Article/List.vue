<template>
  <div class="article-list">
    <div class="page-header">
      <h2>文章列表</h2>
      <el-button type="primary" @click="$router.push('/article/create')">
        <el-icon><Plus /></el-icon>
        创建文章
      </el-button>
    </div>

    <div class="filter-card">
      <el-input v-model="query.keyword" placeholder="搜索文章标题" clearable style="width: 200px" @keyup.enter="fetchList">
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="query.categoryId" placeholder="选择分类" clearable style="width: 140px">
        <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px">
        <el-option label="草稿" :value="0" />
        <el-option label="已发布" :value="1" />
        <el-option label="已下架" :value="2" />
      </el-select>
      <el-button type="primary" @click="fetchList">
        <el-icon><Search /></el-icon>
        搜索
      </el-button>
      <el-button @click="resetQuery">重置</el-button>
    </div>

    <div class="table-card">
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column label="数据" width="160">
          <template #default="{ row }">
            <span class="stat-item"><el-icon><View /></el-icon> {{ row.viewCount }}</span>
            <span class="stat-item"><el-icon><ChatDotRound /></el-icon> {{ row.commentCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 0 ? 'info' : 'danger'" size="small">
              {{ row.status === 1 ? '已发布' : row.status === 0 ? '草稿' : '已下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="置顶" width="80">
          <template #default="{ row }">
            <el-switch v-model="row.isTop" :active-value="1" :inactive-value="0" @change="handleTopChange(row)" />
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.publishTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <button v-if="row.status !== 1" class="action-btn publish-btn" @click="handlePublish(row)">发布</button>
            <button v-if="row.status === 1" class="action-btn offline-btn" @click="handleOffline(row)">下架</button>
            <button class="action-btn edit-btn" @click="$router.push(`/article/edit/${row.id}`)">编辑</button>
            <button class="action-btn delete-btn" @click="handleDelete(row.id)">删除</button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="fetchList"
        @size-change="fetchList"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { formatDateTime } from '@/utils/format'
import { Plus, Search, View, ChatDotRound } from '@element-plus/icons-vue'
import { getArticleList, deleteArticle, updateArticleTop, updateArticleStatus } from '@/api/article'
import { getCategoryList } from '@/api/category'

const list = ref([])
const categories = ref([])
const total = ref(0)
const loading = ref(false)
const query = ref({ page: 1, pageSize: 10, keyword: '', categoryId: null, status: null })

async function fetchList() {
  loading.value = true
  try {
    const res = await getArticleList(query.value)
    list.value = res.data?.list || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

async function fetchCategories() {
  const res = await getCategoryList()
  categories.value = res.data || []
}

function resetQuery() {
  query.value = { page: 1, pageSize: 10, keyword: '', categoryId: null, status: null }
  fetchList()
}

async function handleTopChange(row) {
  try {
    await updateArticleTop(row.id, row.isTop)
    ElMessage.success(row.isTop ? '已置顶' : '已取消置顶')
  } catch (e) {
    row.isTop = row.isTop ? 0 : 1
  }
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除该文章吗？', '提示', { type: 'warning' })
  await deleteArticle(id)
  ElMessage.success('删除成功')
  fetchList()
}

async function handlePublish(row) {
  await ElMessageBox.confirm('确定发布该文章吗？', '提示', { type: 'info' })
  await updateArticleStatus(row.id, 1)
  ElMessage.success('发布成功')
  fetchList()
}

async function handleOffline(row) {
  await ElMessageBox.confirm('确定下架该文章吗？', '提示', { type: 'warning' })
  await updateArticleStatus(row.id, 2)
  ElMessage.success('已下架')
  fetchList()
}

onMounted(() => {
  fetchList()
  fetchCategories()
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.article-list {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;

  h2 {
    font-size: 20px;
    font-weight: 600;
    color: var(--text-primary);
    margin: 0;
  }
}

.filter-card {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-md;
  padding: $spacing-lg;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: $radius-lg;
  margin-bottom: $spacing-lg;
}

.table-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: $radius-lg;
  padding: $spacing-lg;
}

.stat-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-right: 12px;
  font-size: 13px;
  color: var(--text-muted);
  
  .el-icon {
    font-size: 14px;
  }
}

:deep(.el-table) {
  background: transparent;
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: var(--bg-darker);
  --el-table-row-hover-bg-color: var(--bg-hover);
  --el-table-border-color: var(--border-color);
  --el-table-text-color: var(--text-primary);
  --el-table-header-text-color: var(--text-secondary);
}

:deep(.el-input__wrapper),
:deep(.el-select__wrapper) {
  background: var(--bg-darker);
  box-shadow: none;
  border: 1px solid var(--border-color);
  
  &:hover, &:focus {
    border-color: var(--primary-color);
  }
}

:deep(.el-input__inner) {
  color: var(--text-primary);
  
  &::placeholder {
    color: var(--text-muted);
  }
}

:deep(.el-pagination) {
  margin-top: $spacing-lg;
  justify-content: flex-end;
  
  .el-pagination__total,
  .el-pagination__jump {
    color: var(--text-secondary);
  }
  
  .el-pager li {
    background: var(--bg-darker);
    color: var(--text-primary);
    
    &.is-active {
      background: var(--primary-color);
      color: #fff;
    }
  }
  
  .btn-prev, .btn-next {
    background: var(--bg-darker);
    color: var(--text-primary);
  }
}
</style>
