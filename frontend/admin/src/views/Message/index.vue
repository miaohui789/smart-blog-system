<template>
  <div class="message-page">
    <div class="page-header">
      <h2>私信管理</h2>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon message"><el-icon><ChatDotRound /></el-icon></div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.totalMessages || 0 }}</span>
          <span class="stat-label">总私信数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon unread"><el-icon><Bell /></el-icon></div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.unreadMessages || 0 }}</span>
          <span class="stat-label">未读私信</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon conversation"><el-icon><Comment /></el-icon></div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.totalConversations || 0 }}</span>
          <span class="stat-label">会话数</span>
        </div>
      </div>
    </div>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" class="message-tabs">
      <el-tab-pane label="私信列表" name="messages">
        <div class="filter-card">
          <el-input v-model="query.keyword" placeholder="搜索私信内容" clearable style="width: 200px" @keyup.enter="fetchMessages">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-select v-model="query.userId" placeholder="选择用户" clearable filterable style="width: 180px">
            <el-option v-for="user in userOptions" :key="user.id" :label="user.nickname" :value="user.id" />
          </el-select>
          <el-button type="primary" @click="fetchMessages"><el-icon><Search /></el-icon>搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </div>

        <div class="table-card">
          <el-table :data="messages" v-loading="loading" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="50" />
            <el-table-column label="发送者" width="160">
              <template #default="{ row }">
                <div class="user-cell" v-if="row.sender">
                  <el-avatar :size="32" :src="row.sender.avatar">{{ row.sender.nickname?.charAt(0) }}</el-avatar>
                  <span>{{ row.sender.nickname }}</span>
                </div>
                <span v-else class="text-muted">-</span>
              </template>
            </el-table-column>
            <el-table-column label="接收者" width="160">
              <template #default="{ row }">
                <div class="user-cell" v-if="row.receiver">
                  <el-avatar :size="32" :src="row.receiver.avatar">{{ row.receiver.nickname?.charAt(0) }}</el-avatar>
                  <span>{{ row.receiver.nickname }}</span>
                </div>
                <span v-else class="text-muted">-</span>
              </template>
            </el-table-column>
            <el-table-column prop="content" label="内容" min-width="240" show-overflow-tooltip />
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
                <el-popconfirm title="确定删除该私信吗？" @confirm="handleDelete(row.id)">
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
              @current-change="fetchMessages"
              @size-change="fetchMessages"
            />
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="会话列表" name="conversations">
        <div class="filter-card">
          <el-select v-model="convQuery.userId" placeholder="选择用户" clearable filterable style="width: 180px">
            <el-option v-for="user in userOptions" :key="user.id" :label="user.nickname" :value="user.id" />
          </el-select>
          <el-button type="primary" @click="fetchConversations"><el-icon><Search /></el-icon>搜索</el-button>
        </div>

        <div class="table-card">
          <el-table :data="conversations" v-loading="convLoading">
            <el-table-column label="用户1" width="180">
              <template #default="{ row }">
                <div class="user-cell" v-if="row.user1">
                  <el-avatar :size="32" :src="row.user1.avatar">{{ row.user1.nickname?.charAt(0) }}</el-avatar>
                  <span>{{ row.user1.nickname }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="用户2" width="180">
              <template #default="{ row }">
                <div class="user-cell" v-if="row.user2">
                  <el-avatar :size="32" :src="row.user2.avatar">{{ row.user2.nickname?.charAt(0) }}</el-avatar>
                  <span>{{ row.user2.nickname }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="lastMessage" label="最后消息" min-width="200" show-overflow-tooltip />
            <el-table-column prop="messageCount" label="消息数" width="100" />
            <el-table-column label="最后时间" width="160">
              <template #default="{ row }">{{ formatDateTime(row.lastMessageTime) }}</template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="convQuery.page"
            v-model:page-size="convQuery.pageSize"
            :total="convTotal"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @current-change="fetchConversations"
            @size-change="fetchConversations"
            style="margin-top: 16px; justify-content: flex-end;"
          />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatDotRound, Bell, Comment, Search } from '@element-plus/icons-vue'
import { getMessageList, getConversationList, deleteMessage, batchDeleteMessages, getMessageStats } from '@/api/message'
import { getUserList } from '@/api/user'
import { formatDateTime } from '@/utils/format'

const activeTab = ref('messages')
const loading = ref(false)
const convLoading = ref(false)
const messages = ref([])
const conversations = ref([])
const total = ref(0)
const convTotal = ref(0)
const stats = ref({})
const userOptions = ref([])
const selectedIds = ref([])

const query = ref({ page: 1, pageSize: 20, keyword: '', userId: null })
const convQuery = ref({ page: 1, pageSize: 20, userId: null })

async function fetchStats() {
  try {
    const res = await getMessageStats()
    stats.value = res.data || {}
  } catch (e) { console.error(e) }
}

async function fetchMessages() {
  loading.value = true
  try {
    const res = await getMessageList(query.value)
    messages.value = res.data?.list || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

async function fetchConversations() {
  convLoading.value = true
  try {
    const res = await getConversationList(convQuery.value)
    conversations.value = res.data?.list || []
    convTotal.value = res.data?.total || 0
  } finally {
    convLoading.value = false
  }
}

async function fetchUsers() {
  try {
    const res = await getUserList({ page: 1, pageSize: 1000 })
    userOptions.value = res.data?.list || []
  } catch (e) { console.error(e) }
}

function resetQuery() {
  query.value = { page: 1, pageSize: 20, keyword: '', userId: null }
  fetchMessages()
}

function handleSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id)
}

async function handleDelete(id) {
  await deleteMessage(id)
  ElMessage.success('删除成功')
  fetchMessages()
  fetchStats()
}

async function handleBatchDelete() {
  await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 条私信吗？`, '提示', { type: 'warning' })
  await batchDeleteMessages(selectedIds.value)
  ElMessage.success('删除成功')
  selectedIds.value = []
  fetchMessages()
  fetchStats()
}

onMounted(() => {
  fetchStats()
  fetchMessages()
  fetchConversations()
  fetchUsers()
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.message-page { padding: 20px; }

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
  &.message { background: rgba($primary-color, 0.1); color: $primary-color; }
  &.unread { background: rgba($warning-color, 0.1); color: $warning-color; }
  &.conversation { background: rgba($success-color, 0.1); color: $success-color; }
}

.stat-info { display: flex; flex-direction: column; }
.stat-value { font-size: 24px; font-weight: 700; color: var(--text-primary); }
.stat-label { font-size: 13px; color: var(--text-muted); }

.message-tabs {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: $radius-lg;
  padding: $spacing-lg;
}

.filter-card {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-md;
  margin-bottom: $spacing-lg;
}

.table-card { background: transparent; }

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

:deep(.el-tabs__item) {
  color: var(--text-secondary);
  &.is-active { color: var(--primary-color); }
}

:deep(.el-input__wrapper),
:deep(.el-select__wrapper) {
  background: var(--bg-darker);
  box-shadow: none;
  border: 1px solid var(--border-color);
}
</style>
