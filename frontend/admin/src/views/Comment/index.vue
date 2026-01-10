<template>
  <div class="comment-page">
    <div class="page-header">
      <h2>评论管理</h2>
    </div>

    <div class="filter-card">
      <el-input v-model="query.keyword" placeholder="搜索评论内容" clearable style="width: 200px" @keyup.enter="fetchList">
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="query.status" placeholder="审核状态" clearable style="width: 120px">
        <el-option label="待审核" :value="0" />
        <el-option label="已通过" :value="1" />
        <el-option label="已拒绝" :value="2" />
      </el-select>
      <el-button type="primary" @click="fetchList">
        <el-icon><Search /></el-icon>
        搜索
      </el-button>
      <el-button @click="resetQuery">重置</el-button>
    </div>

    <div class="table-card">
      <el-table :data="list" v-loading="loading">
        <el-table-column label="评论人" width="160">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :src="row.avatar" :size="32">{{ row.nickname?.charAt(0) }}</el-avatar>
              <span class="nickname">{{ row.nickname }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评论内容" min-width="240" show-overflow-tooltip />
        <el-table-column prop="articleTitle" label="所属文章" width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 0 ? 'warning' : 'danger'" size="small">
              {{ row.status === 1 ? '已通过' : row.status === 0 ? '待审核' : '已拒绝' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="评论时间" width="160" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <button v-if="row.status === 0" class="action-btn pass-btn" @click="handleAudit(row.id, 1)">通过</button>
            <button v-if="row.status === 0" class="action-btn reject-btn" @click="handleAudit(row.id, 2)">拒绝</button>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getCommentList, updateCommentStatus, deleteComment } from '@/api/comment'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = ref({ page: 1, pageSize: 10, keyword: '', status: null })

async function fetchList() {
  loading.value = true
  try {
    const res = await getCommentList(query.value)
    list.value = res.data?.list || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.value = { page: 1, pageSize: 10, keyword: '', status: null }
  fetchList()
}

async function handleAudit(id, status) {
  const action = status === 1 ? '通过' : '拒绝'
  await ElMessageBox.confirm(`确定${action}该评论吗？`, '提示', { type: 'warning' })
  await updateCommentStatus(id, status)
  ElMessage.success('操作成功')
  fetchList()
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除该评论吗？删除后不可恢复', '提示', { type: 'warning' })
  await deleteComment(id)
  ElMessage.success('删除成功')
  fetchList()
}

onMounted(fetchList)
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.comment-page {
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

.user-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  
  .nickname {
    font-size: 13px;
    color: var(--text-secondary);
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
