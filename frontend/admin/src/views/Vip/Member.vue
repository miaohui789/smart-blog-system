<template>
  <div class="vip-member-page">
    <div class="page-header">
      <h2>会员管理</h2>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="queryParams.keyword" placeholder="搜索用户名/昵称" clearable style="width: 200px" @keyup.enter="handleSearch" />
      <el-select v-model="queryParams.level" placeholder="VIP等级" clearable style="width: 120px">
        <el-option label="普通VIP" :value="1" />
        <el-option label="高级VIP" :value="2" />
        <el-option label="超级VIP" :value="3" />
      </el-select>
      <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 100px">
        <el-option label="有效" :value="1" />
        <el-option label="过期" :value="0" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 表格 -->
    <div class="table-card">
      <el-table :data="memberList" v-loading="loading">
        <el-table-column label="用户" min-width="180">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :src="row.avatar" :size="40" />
              <div class="info">
                <div class="nickname">{{ row.nickname || row.username }}</div>
                <div class="username">@{{ row.username }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="VIP等级" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getLevelType(row.level)">{{ getLevelName(row.level) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="用户等级" width="170" align="center">
          <template #default="{ row }">
            <UserLevelTag :level="row.userLevel || 1" />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '有效' : '过期' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="开始时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="到期时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.expireTime) }}</template>
        </el-table-column>
        <el-table-column label="今日加热" width="90" align="center" prop="heatCountToday" />
        <el-table-column label="今日下载" width="90" align="center" prop="downloadCountToday" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <button class="action-btn edit-btn" @click="handleEdit(row)">编辑</button>
            <button class="action-btn delete-btn" @click="handleDelete(row.id)">删除</button>
          </template>
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

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" title="编辑会员" width="450px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="VIP等级">
          <el-select v-model="editForm.level" style="width: 100%">
            <el-option label="普通VIP" :value="1" />
            <el-option label="高级VIP" :value="2" />
            <el-option label="超级VIP" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="到期时间">
          <el-date-picker v-model="editForm.expireTime" type="datetime" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getMemberList, updateMember, deleteMember } from '@/api/vip'
import { formatDateTime } from '@/utils/format'
import { ElMessage, ElMessageBox } from 'element-plus'
import UserLevelTag from '@/components/UserLevelTag/index.vue'

const loading = ref(false)
const submitLoading = ref(false)
const memberList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const currentId = ref(null)

const queryParams = reactive({
  page: 1,
  size: 10,
  keyword: '',
  level: null,
  status: null
})

const editForm = reactive({
  level: 1,
  expireTime: null
})

onMounted(() => {
  fetchList()
})

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getMemberList(queryParams)
    if (res.code === 200) {
      memberList.value = res.data.records
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.page = 1
  fetchList()
}

const handleReset = () => {
  queryParams.keyword = ''
  queryParams.level = null
  queryParams.status = null
  handleSearch()
}

const handleEdit = (row) => {
  currentId.value = row.id
  editForm.level = row.level
  editForm.expireTime = row.expireTime
  dialogVisible.value = true
}

const handleSubmit = async () => {
  submitLoading.value = true
  try {
    const res = await updateMember(currentId.value, editForm)
    if (res.code === 200) {
      ElMessage.success('更新成功')
      dialogVisible.value = false
      fetchList()
    } else {
      ElMessage.error(res.message)
    }
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定删除该会员的VIP身份吗？', '提示', { type: 'warning' })
  const res = await deleteMember(id)
  if (res.code === 200) {
    ElMessage.success('删除成功')
    fetchList()
  } else {
    ElMessage.error(res.message)
  }
}

const getLevelName = (level) => {
  const names = { 1: '普通VIP', 2: '高级VIP', 3: '超级VIP' }
  return names[level] || '未知'
}

const getLevelType = (level) => {
  const types = { 1: 'warning', 2: '', 3: 'success' }
  return types[level] || 'info'
}
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.vip-member-page {
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
  
  .info {
    line-height: 1.4;
  }
  
  .nickname {
    font-weight: 500;
    color: var(--text-primary);
  }
  
  .username {
    font-size: 12px;
    color: var(--text-muted);
  }
}

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
  --el-table-text-color: var(--text-primary);
  --el-table-header-text-color: var(--text-secondary);
}

:deep(.el-dialog) {
  --el-dialog-bg-color: var(--bg-card);
  border-radius: $radius-lg;
  
  .el-dialog__header {
    border-bottom: 1px solid var(--border-color);
    padding: 16px 20px;
  }
  
  .el-dialog__title {
    color: var(--text-primary);
  }
  
  .el-dialog__body {
    padding: 20px;
  }
}

:deep(.el-form-item__label) {
  color: var(--text-secondary);
}
</style>
