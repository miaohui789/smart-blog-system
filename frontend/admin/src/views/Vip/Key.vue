<template>
  <div class="vip-key-page">
    <div class="page-header">
      <h2>密钥管理</h2>
      <el-button type="primary" @click="showGenerateDialog">
        <el-icon><Plus /></el-icon>
        生成密钥
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="queryParams.keyword" placeholder="搜索密钥/备注" clearable style="width: 200px" @keyup.enter="handleSearch" />
      <el-select v-model="queryParams.level" placeholder="VIP等级" clearable style="width: 120px">
        <el-option label="普通VIP" :value="1" />
        <el-option label="高级VIP" :value="2" />
        <el-option label="超级VIP" :value="3" />
      </el-select>
      <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 100px">
        <el-option label="可用" :value="1" />
        <el-option label="已使用" :value="0" />
        <el-option label="已禁用" :value="2" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 表格 -->
    <div class="table-card">
      <el-table :data="keyList" v-loading="loading">
        <el-table-column label="密钥" min-width="220">
          <template #default="{ row }">
            <div class="key-code">
              <code>{{ row.keyCode }}</code>
              <el-button type="primary" link size="small" @click="copyKey(row.keyCode)">复制</el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="VIP等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getLevelType(row.level)" size="small">{{ getLevelName(row.level) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="有效天数" width="90" align="center" prop="durationDays" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusName(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="使用者" width="100" prop="usedByUsername">
          <template #default="{ row }">{{ row.usedByUsername || '-' }}</template>
        </el-table-column>
        <el-table-column label="使用时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.usedTime) }}</template>
        </el-table-column>
        <el-table-column label="备注" min-width="120" prop="remark" show-overflow-tooltip />
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 1">
              <button class="action-btn disable-btn" @click="handleDisable(row.id)">禁用</button>
            </template>
            <template v-else-if="row.status === 2">
              <button class="action-btn enable-btn" @click="handleEnable(row.id)">启用</button>
            </template>
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

    <!-- 生成密钥弹窗 -->
    <el-dialog v-model="generateDialogVisible" title="批量生成密钥" width="450px">
      <el-form :model="generateForm" label-width="100px">
        <el-form-item label="VIP等级">
          <el-select v-model="generateForm.level" style="width: 100%">
            <el-option label="普通VIP" :value="1" />
            <el-option label="高级VIP" :value="2" />
            <el-option label="超级VIP" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="有效天数">
          <el-input-number v-model="generateForm.durationDays" :min="1" :max="3650" style="width: 100%" />
        </el-form-item>
        <el-form-item label="生成数量">
          <el-input-number v-model="generateForm.count" :min="1" :max="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="密钥有效期">
          <el-input-number v-model="generateForm.expireDays" :min="1" placeholder="留空表示永不过期" style="width: 100%" />
          <div class="form-tip">密钥本身的有效期（天），过期后无法使用</div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="generateForm.remark" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="generateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleGenerate" :loading="generateLoading">生成</el-button>
      </template>
    </el-dialog>

    <!-- 生成结果弹窗 -->
    <el-dialog v-model="resultDialogVisible" title="生成成功" width="500px">
      <div class="result-content">
        <p>成功生成 {{ generatedKeys.length }} 个密钥：</p>
        <div class="key-list">
          <div v-for="key in generatedKeys" :key="key.id" class="key-item">
            <code>{{ key.keyCode }}</code>
            <el-button type="primary" link size="small" @click="copyKey(key.keyCode)">复制</el-button>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="copyAllKeys">复制全部</el-button>
        <el-button type="primary" @click="resultDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { getKeyList, generateKeys, updateKeyStatus, deleteKey } from '@/api/vip'
import { formatDateTime } from '@/utils/format'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const generateLoading = ref(false)
const keyList = ref([])
const total = ref(0)
const generateDialogVisible = ref(false)
const resultDialogVisible = ref(false)
const generatedKeys = ref([])

const queryParams = reactive({
  page: 1,
  size: 10,
  keyword: '',
  level: null,
  status: null
})

const generateForm = reactive({
  level: 1,
  durationDays: 30,
  count: 10,
  expireDays: null,
  remark: ''
})

onMounted(() => {
  fetchList()
})

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getKeyList(queryParams)
    if (res.code === 200) {
      keyList.value = res.data.records
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

const showGenerateDialog = () => {
  generateForm.level = 1
  generateForm.durationDays = 30
  generateForm.count = 10
  generateForm.expireDays = null
  generateForm.remark = ''
  generateDialogVisible.value = true
}

const handleGenerate = async () => {
  generateLoading.value = true
  try {
    const res = await generateKeys(generateForm)
    if (res.code === 200) {
      ElMessage.success('生成成功')
      generateDialogVisible.value = false
      generatedKeys.value = res.data
      resultDialogVisible.value = true
      fetchList()
    } else {
      ElMessage.error(res.message)
    }
  } finally {
    generateLoading.value = false
  }
}

const handleDisable = async (id) => {
  await ElMessageBox.confirm('确定禁用该密钥吗？', '提示', { type: 'warning' })
  const res = await updateKeyStatus(id, 2)
  if (res.code === 200) {
    ElMessage.success('已禁用')
    fetchList()
  } else {
    ElMessage.error(res.message)
  }
}

const handleEnable = async (id) => {
  const res = await updateKeyStatus(id, 1)
  if (res.code === 200) {
    ElMessage.success('已启用')
    fetchList()
  } else {
    ElMessage.error(res.message)
  }
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定删除该密钥吗？', '提示', { type: 'warning' })
  const res = await deleteKey(id)
  if (res.code === 200) {
    ElMessage.success('删除成功')
    fetchList()
  } else {
    ElMessage.error(res.message)
  }
}

const copyKey = (key) => {
  navigator.clipboard.writeText(key)
  ElMessage.success('已复制')
}

const copyAllKeys = () => {
  const text = generatedKeys.value.map(k => k.keyCode).join('\n')
  navigator.clipboard.writeText(text)
  ElMessage.success('已复制全部密钥')
}

const getLevelName = (level) => {
  const names = { 1: '普通', 2: '高级', 3: '超级' }
  return names[level] || '未知'
}

const getLevelType = (level) => {
  const types = { 1: 'warning', 2: '', 3: 'success' }
  return types[level] || 'info'
}

const getStatusName = (status) => {
  const names = { 0: '已使用', 1: '可用', 2: '已禁用' }
  return names[status] || '未知'
}

const getStatusType = (status) => {
  const types = { 0: 'info', 1: 'success', 2: 'danger' }
  return types[status] || 'info'
}
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.vip-key-page {
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

.key-code {
  display: flex;
  align-items: center;
  gap: 8px;
  
  code {
    font-family: 'Consolas', monospace;
    font-size: 12px;
    color: var(--text-primary);
  }
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.form-tip {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 4px;
}

.result-content p {
  margin-bottom: 12px;
  color: var(--text-primary);
}

.key-list {
  max-height: 300px;
  overflow-y: auto;
}

.key-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px;
  border-bottom: 1px solid var(--border-color);
  
  code {
    font-family: 'Consolas', monospace;
    color: var(--text-primary);
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
