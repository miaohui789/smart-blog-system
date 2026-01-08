<template>
  <div class="log-page">
    <h2 class="page-title">操作日志</h2>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="操作日志" name="operation">
        <el-table :data="operationLogs" v-loading="loading">
          <el-table-column prop="module" label="模块" width="100" />
          <el-table-column prop="description" label="操作描述" />
          <el-table-column prop="requestMethod" label="请求方式" width="100" />
          <el-table-column prop="ip" label="IP地址" width="130" />
          <el-table-column prop="costTime" label="耗时(ms)" width="100" />
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 1 ? '成功' : '失败' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="操作时间" width="160" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="登录日志" name="login">
        <el-table :data="loginLogs" v-loading="loading">
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="ip" label="IP地址" width="130" />
          <el-table-column prop="ipSource" label="IP来源" />
          <el-table-column prop="browser" label="浏览器" width="120" />
          <el-table-column prop="os" label="操作系统" width="120" />
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 1 ? '成功' : '失败' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="登录时间" width="160" />
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-pagination
      v-model:current-page="query.page"
      v-model:page-size="query.pageSize"
      :total="total"
      layout="total, prev, pager, next"
      @current-change="fetchLogs"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getOperationLogs, getLoginLogs } from '@/api/log'

const activeTab = ref('operation')
const operationLogs = ref([])
const loginLogs = ref([])
const total = ref(0)
const loading = ref(false)
const query = ref({ page: 1, pageSize: 10 })

async function fetchLogs() {
  loading.value = true
  try {
    if (activeTab.value === 'operation') {
      const res = await getOperationLogs(query.value)
      operationLogs.value = res.data?.list || []
      total.value = res.data?.total || 0
    } else {
      const res = await getLoginLogs(query.value)
      loginLogs.value = res.data?.list || []
      total.value = res.data?.total || 0
    }
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
  query.value.page = 1
  fetchLogs()
}

onMounted(fetchLogs)
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.log-page {
  padding: 20px;
  background: var(--bg-card);
  border-radius: $radius-lg;
}

.page-title {
  font-size: 20px;
  color: var(--text-primary);
  margin: 0 0 $spacing-xl 0;
}

:deep(.el-tabs__item) {
  color: var(--text-secondary);
  
  &.is-active {
    color: var(--primary-color);
  }
  
  &:hover {
    color: var(--primary-color);
  }
}

:deep(.el-tabs__active-bar) {
  background-color: var(--primary-color);
}

:deep(.el-tabs__nav-wrap::after) {
  background-color: var(--border-color);
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
