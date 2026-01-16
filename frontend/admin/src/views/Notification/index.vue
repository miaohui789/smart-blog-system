<template>
  <div class="notification-page">
    <div class="page-header">
      <h2>通知管理</h2>
      <el-button type="primary" @click="showSendDialog">
        <el-icon><Promotion /></el-icon>
        发送系统通知
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon total"><el-icon><Bell /></el-icon></div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.totalNotifications || 0 }}</span>
          <span class="stat-label">总通知数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon unread"><el-icon><Message /></el-icon></div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.unreadNotifications || 0 }}</span>
          <span class="stat-label">未读通知</span>
        </div>
      </div>
      <div class="stat-card" v-for="(count, type) in stats.typeStats" :key="type">
        <div class="stat-icon" :class="type.toLowerCase()">
          <el-icon><component :is="getTypeIcon(type)" /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ count }}</span>
          <span class="stat-label">{{ getTypeName(type) }}</span>
        </div>
      </div>
    </div>

    <!-- 筛选 -->
    <div class="filter-card">
      <el-select v-model="query.type" placeholder="通知类型" clearable style="width: 140px">
        <el-option label="点赞" value="LIKE_ARTICLE" />
        <el-option label="收藏" value="FAVORITE_ARTICLE" />
        <el-option label="评论" value="COMMENT" />
        <el-option label="回复" value="REPLY" />
        <el-option label="关注" value="FOLLOW" />
        <el-option label="系统" value="SYSTEM" />
      </el-select>
      <el-select v-model="query.isRead" placeholder="状态" clearable style="width: 120px">
        <el-option label="未读" :value="0" />
        <el-option label="已读" :value="1" />
      </el-select>
      <el-select v-model="query.userId" placeholder="选择用户" clearable filterable style="width: 180px">
        <el-option v-for="user in userOptions" :key="user.id" :label="user.nickname" :value="user.id" />
      </el-select>
      <el-button type="primary" @click="fetchList"><el-icon><Search /></el-icon>搜索</el-button>
      <el-button @click="resetQuery">重置</el-button>
    </div>

    <!-- 表格 -->
    <div class="table-card">
      <el-table :data="list" v-loading="loading" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column label="接收用户" width="150">
          <template #default="{ row }">
            <div class="user-cell" v-if="row.user">
              <el-avatar :size="32" :src="row.user.avatar">{{ row.user.nickname?.charAt(0) }}</el-avatar>
              <span>{{ row.user.nickname }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)" size="small">{{ getTypeName(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="240" show-overflow-tooltip />
        <el-table-column label="触发用户" width="150">
          <template #default="{ row }">
            <div class="user-cell" v-if="row.sender">
              <el-avatar :size="32" :src="row.sender.avatar">{{ row.sender.nickname?.charAt(0) }}</el-avatar>
              <span>{{ row.sender.nickname }}</span>
            </div>
            <span v-else class="text-muted">系统</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isRead ? 'info' : 'warning'" size="small">
              {{ row.isRead ? '已读' : '未读' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-popconfirm title="确定删除该通知吗？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" text size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-footer">
        <el-button type="danger" :disabled="!selectedIds.length" @click="handleBatchDelete">
          批量删除 ({{ selectedIds.length }})
        </el-button>
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

    <!-- 发送通知对话框 -->
    <el-dialog v-model="sendDialogVisible" title="发送系统通知" width="500px">
      <el-form :model="sendForm" :rules="sendRules" ref="sendFormRef" label-width="100px">
        <el-form-item label="通知标题" prop="title">
          <el-input v-model="sendForm.title" placeholder="请输入通知标题" />
        </el-form-item>
        <el-form-item label="通知内容" prop="content">
          <el-input v-model="sendForm.content" type="textarea" :rows="4" placeholder="请输入通知内容" />
        </el-form-item>
        <el-form-item label="发送对象">
          <el-radio-group v-model="sendForm.sendToAll">
            <el-radio :value="true">所有用户</el-radio>
            <el-radio :value="false">指定用户</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="!sendForm.sendToAll" label="选择用户" prop="userIds">
          <el-select v-model="sendForm.userIds" multiple filterable placeholder="请选择用户" style="width: 100%">
            <el-option v-for="user in userOptions" :key="user.id" :label="user.nickname" :value="user.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="sendDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSend" :loading="sending">发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Bell, Message, Search, Promotion, Star, Collection, ChatDotRound, ChatLineRound, UserFilled, Setting } from '@element-plus/icons-vue'
import { getNotificationList, deleteNotification, batchDeleteNotifications, getNotificationStats, sendSystemNotification } from '@/api/notification'
import { getUserList } from '@/api/user'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const stats = ref({})
const userOptions = ref([])
const selectedIds = ref([])
const sendDialogVisible = ref(false)
const sending = ref(false)
const sendFormRef = ref()

const query = ref({ page: 1, pageSize: 20, type: '', isRead: null, userId: null })
const sendForm = ref({ title: '', content: '', sendToAll: true, userIds: [] })
const sendRules = {
  title: [{ required: true, message: '请输入通知标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入通知内容', trigger: 'blur' }]
}

const typeMap = {
  LIKE_ARTICLE: '点赞',
  FAVORITE_ARTICLE: '收藏',
  COMMENT: '评论',
  REPLY: '回复',
  FOLLOW: '关注',
  SYSTEM: '系统'
}

const typeIconMap = {
  LIKE_ARTICLE: Star,
  FAVORITE_ARTICLE: Collection,
  COMMENT: ChatDotRound,
  REPLY: ChatLineRound,
  FOLLOW: UserFilled,
  SYSTEM: Setting
}

function getTypeName(type) { return typeMap[type] || type }
function getTypeIcon(type) { return typeIconMap[type] || Bell }
function getTypeTagType(type) {
  const map = { LIKE_ARTICLE: 'danger', FAVORITE_ARTICLE: 'warning', COMMENT: '', REPLY: 'success', FOLLOW: 'info', SYSTEM: '' }
  return map[type] || ''
}

async function fetchStats() {
  try {
    const res = await getNotificationStats()
    stats.value = res.data || {}
  } catch (e) { console.error(e) }
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getNotificationList(query.value)
    list.value = res.data?.list || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

async function fetchUsers() {
  try {
    const res = await getUserList({ page: 1, pageSize: 1000 })
    userOptions.value = res.data?.list || []
  } catch (e) { console.error(e) }
}

function resetQuery() {
  query.value = { page: 1, pageSize: 20, type: '', isRead: null, userId: null }
  fetchList()
}

function handleSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id)
}

async function handleDelete(id) {
  await deleteNotification(id)
  ElMessage.success('删除成功')
  fetchList()
  fetchStats()
}

async function handleBatchDelete() {
  await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 条通知吗？`, '提示', { type: 'warning' })
  await batchDeleteNotifications(selectedIds.value)
  ElMessage.success('删除成功')
  selectedIds.value = []
  fetchList()
  fetchStats()
}

function showSendDialog() {
  sendForm.value = { title: '', content: '', sendToAll: true, userIds: [] }
  sendDialogVisible.value = true
}

async function handleSend() {
  await sendFormRef.value.validate()
  if (!sendForm.value.sendToAll && !sendForm.value.userIds.length) {
    ElMessage.warning('请选择接收用户')
    return
  }
  sending.value = true
  try {
    const res = await sendSystemNotification(sendForm.value)
    ElMessage.success(res.data || '发送成功')
    sendDialogVisible.value = false
    fetchList()
    fetchStats()
  } finally {
    sending.value = false
  }
}

onMounted(() => {
  fetchStats()
  fetchList()
  fetchUsers()
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.notification-page { padding: 20px; }

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;
  h2 { font-size: 20px; font-weight: 600; color: var(--text-primary); margin: 0; }
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
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
  padding: $spacing-md $spacing-lg;
}

.stat-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: $radius-md;
  font-size: 20px;
  &.total { background: rgba($primary-color, 0.1); color: $primary-color; }
  &.unread { background: rgba($warning-color, 0.1); color: $warning-color; }
  &.like_article { background: rgba(#ef4444, 0.1); color: #ef4444; }
  &.favorite_article { background: rgba(#f59e0b, 0.1); color: #f59e0b; }
  &.comment { background: rgba($primary-color, 0.1); color: $primary-color; }
  &.reply { background: rgba($success-color, 0.1); color: $success-color; }
  &.follow { background: rgba(#8b5cf6, 0.1); color: #8b5cf6; }
  &.system { background: rgba(#6b7280, 0.1); color: #6b7280; }
}

.stat-info { display: flex; flex-direction: column; }
.stat-value { font-size: 20px; font-weight: 700; color: var(--text-primary); }
.stat-label { font-size: 12px; color: var(--text-muted); }

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
}

.text-muted { color: var(--text-muted); }

.table-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: $spacing-lg;
}

:deep(.el-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: var(--bg-darker);
  --el-table-row-hover-bg-color: var(--bg-hover);
  --el-table-border-color: var(--border-color);
}

:deep(.el-dialog) {
  --el-dialog-bg-color: var(--bg-card);
  border-radius: $radius-lg;
}

:deep(.el-input__wrapper),
:deep(.el-select__wrapper),
:deep(.el-textarea__inner) {
  background: var(--bg-darker);
  box-shadow: none;
  border: 1px solid var(--border-color);
}
</style>
